package com.example.myapplication.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.settings.BranchConfig
import com.example.myapplication.ui.sync.SyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    vm: SyncViewModel = hiltViewModel()
) {
    val branches by vm.branches().collectAsState(initial = emptyList())
    val currentId by vm.currentId().collectAsState(initial = null)

    // id da filial que está sendo editada (pode ser diferente da ativa, se quiser)
    var editingId by remember { mutableStateOf<String?>(null) }

    var nome by remember { mutableStateOf("") }
    var backendUrl by remember { mutableStateOf("") }
    var dbServer by remember { mutableStateOf("") }
    var dbName by remember { mutableStateOf("") }
    var apiToken by remember { mutableStateOf("") }

    // helper pra limpar o formulário e "desgrudar" de qualquer filial existente
    fun limparFormulario() {
        editingId = null
        nome = ""
        backendUrl = ""
        dbServer = ""
        dbName = ""
        apiToken = ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Configurações • Filiais", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34),
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Linha com título do formulário + botão "Nova filial"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingId != null) "Editar filial" else "Nova filial",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                TextButton(onClick = { limparFormulario() }) {
                    Text("Nova filial")
                }
            }

            // Formulário da filial (nova ou edição)
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome da filial") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = backendUrl,
                onValueChange = { backendUrl = it },
                label = { Text("URL BACKEND (ex: http://192.168.15.11:8000/)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dbServer,
                onValueChange = { dbServer = it },
                label = { Text("IP/Host do servidor de dados (ex: 192.168.15.11 ou amadeu.myftp.org)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dbName,
                onValueChange = { dbName = it },
                label = { Text("Nome do banco de dados") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = apiToken,
                onValueChange = { apiToken = it },
                label = { Text("X-Api-Token (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            val isEditingExisting = editingId != null && branches.any { it.id == editingId }

            // Adicionar / Atualizar filial
            Button(
                onClick = {
                    // se estiver editando, usa o mesmo id; se for nova, gera um
                    val id = editingId ?: System.currentTimeMillis().toString()

                    val nova = BranchConfig(
                        id = id,
                        nome = nome.ifBlank { "Sem nome" },
                        backendUrl = backendUrl.trim(),
                        dbServer = dbServer.trim(),
                        dbName = dbName.trim(),
                        apiToken = apiToken.trim()
                    )

                    val atualizada =
                        if (branches.any { it.id == id }) {
                            // Atualiza filial existente
                            branches.map { if (it.id == id) nova else it }
                        } else {
                            // Adiciona nova
                            branches + nova
                        }

                    vm.saveBranches(atualizada)
                    vm.setCurrentBranch(id)
                    editingId = id // passa a editar essa recém salva
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F3A34),
                    contentColor = Color.White
                )
            ) {
                Text(if (isEditingExisting) "Atualizar filial" else "Adicionar filial")
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 1.dp,
                color = Color(0xFFE0E0E0)
            )

            // Lista de filiais
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(branches, key = { it.id }) { b ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    b.nome + if (currentId == b.id) "  • ATIVA" else "",
                                    fontWeight = if (currentId == b.id) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    b.backendUrl,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val extras = buildList {
                                    if (b.dbServer.isNotBlank()) add("DB host: ${b.dbServer}")
                                    if (b.dbName.isNotBlank()) add("DB: ${b.dbName}")
                                    if (b.apiToken.isNotBlank()) add("X-Api-Token: ****")
                                }
                                if (extras.isNotEmpty()) {
                                    Text(
                                        extras.joinToString("   "),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Ativar + carregar dados no formulário para edição
                                OutlinedButton(
                                    onClick = {
                                        vm.setCurrentBranch(b.id)
                                        editingId = b.id
                                        nome = b.nome
                                        backendUrl = b.backendUrl
                                        dbServer = b.dbServer
                                        dbName = b.dbName
                                        apiToken = b.apiToken
                                    },
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Ativar/Editar")
                                }

                                OutlinedButton(
                                    onClick = {
                                        val atualizada = branches.filterNot { it.id == b.id }
                                        vm.saveBranches(atualizada)

                                        if (currentId == b.id) {
                                            // se deletei a ativa, escolho outra ou limpo
                                            atualizada.firstOrNull()?.let {
                                                vm.setCurrentBranch(it.id)
                                            } ?: vm.setCurrentBranch("")
                                        }

                                        if (editingId == b.id) {
                                            limparFormulario()
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Red
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text("Excluir")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.data.settings.BranchConfig
import com.example.myapplication.ui.sync.SyncViewModel
import java.text.Normalizer
import java.util.Locale

@Composable
fun ConfigFiliaisScreen(
    onBack: () -> Unit = {},
    vm: SyncViewModel = hiltViewModel()
) {
    val branches by vm.branches().collectAsState(initial = emptyList())
    val currentId by vm.currentId().collectAsState(initial = null)

    // filial em edição (null = estamos na lista)
    var editingBranch by remember { mutableStateOf<BranchConfig?>(null) }
    val isEditing = editingBranch != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Nova filial / edição"
                        else "Configurações · Filiais",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditing) {
                            editingBranch = null       // volta para lista
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (!isEditing) {
                FloatingActionButton(
                    onClick = {
                        // nova filial -> formulário em branco
                        editingBranch = null
                        editingBranch = BranchConfig(
                            id = "",
                            nome = "",
                            backendUrl = "",
                            dbServer = "",
                            dbName = "",
                            apiToken = ""
                        )
                    },
                    containerColor = Color(0xFF0F3A34),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nova filial")
                }
            }
        }
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFBAC4C7),
                            Color(0xFF1E2E38)
                        )
                    )
                )
        ) {
            if (editingBranch == null || !isEditing) {
                BranchListContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    branches = branches,
                    currentId = currentId,
                    onSelect = { id -> vm.setCurrentBranch(id) },
                    onEdit = { branch -> editingBranch = branch }
                )
            } else {
                BranchFormContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    original = editingBranch,
                    allBranches = branches,
                    onSaveBranches = { vm.saveBranches(it) },
                    onSetCurrent = { vm.setCurrentBranch(it) },
                    onFinish = { editingBranch = null }
                )
            }
        }
    }
}

/**
 * LISTA DE FILIAIS
 */
@Composable
private fun BranchListContent(
    modifier: Modifier = Modifier,
    branches: List<BranchConfig>,
    currentId: String?,
    onSelect: (String) -> Unit,
    onEdit: (BranchConfig) -> Unit
) {
    if (branches.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nenhuma filial cadastrada.\nToque em + para adicionar.",
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(branches) { branch ->
            val isCurrent = branch.id == currentId
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit(branch) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFFE0F2F1) else Color.White
                )
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(branch.nome, style = MaterialTheme.typography.titleMedium)
                        if (isCurrent) {
                            AssistChip(onClick = {}, label = { Text("Atual") })
                        } else {
                            TextButton(onClick = { onSelect(branch.id) }) {
                                Text("Tornar atual")
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text("Backend: ${branch.backendUrl}", style = MaterialTheme.typography.bodySmall)
                    Text("DB: ${branch.dbServer}/${branch.dbName}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/**
 * FORMULÁRIO DE NOVA / EDIÇÃO DE FILIAL
 */
@Composable
private fun BranchFormContent(
    modifier: Modifier = Modifier,
    original: BranchConfig?,
    allBranches: List<BranchConfig>,
    onSaveBranches: (List<BranchConfig>) -> Unit,
    onSetCurrent: (String) -> Unit,
    onFinish: () -> Unit
) {
    var nome by remember { mutableStateOf(TextFieldValue(original?.nome ?: "")) }
    var backendUrl by remember { mutableStateOf(TextFieldValue(original?.backendUrl ?: "")) }
    var dbServer by remember { mutableStateOf(TextFieldValue(original?.dbServer ?: "")) }
    var dbName by remember { mutableStateOf(TextFieldValue(original?.dbName ?: "")) }
    var apiToken by remember { mutableStateOf(TextFieldValue(original?.apiToken ?: "")) }

    val isEdit = original != null && original.id.isNotBlank()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val nomeStr = nome.text.trim()
                if (nomeStr.isEmpty()) return@Button

                val id = original?.id?.takeIf { it.isNotBlank() } ?: nomeStr.toSlug()

                val newBranch = BranchConfig(
                    id = id,
                    nome = nomeStr,
                    backendUrl = backendUrl.text.trim(),
                    dbServer = dbServer.text.trim(),
                    dbName = dbName.text.trim(),
                    apiToken = apiToken.text.trim()
                )

                val updated = if (isEdit) {
                    allBranches.map { if (it.id == original!!.id) newBranch else it }
                } else {
                    allBranches + newBranch
                }

                onSaveBranches(updated)
                onSetCurrent(id)
                onFinish()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0F3A34),
                contentColor = Color.White
            )
        ) {
            Text(if (isEdit) "Salvar alterações" else "Adicionar filial")
        }

        TextButton(
            onClick = onFinish,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Cancelar")
        }
    }
}

/**
 * Helper: transforma "Loja Amadeu" em "loja_amadeu"
 */
private fun String.toSlug(): String {
    val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    return normalized
        .lowercase(Locale.getDefault())
        .replace("[^a-z0-9]+".toRegex(), "_")
        .trim('_')
}

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.sync.SyncUiState
import com.example.myapplication.ui.sync.SyncViewModel
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType

/** Alvo do sincronismo */
enum class SyncTarget { LOJA, DEPOSITO }

/**
 * Tela de sincronismo de inventário integrada ao SyncViewModel/Hilt,
 * com seletor de FILIAL (multi-servidor).
 */
@Composable
fun InventorySyncScreen(
    onBack: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    vm: SyncViewModel = hiltViewModel()
) {
    // Flows expostos pelo ViewModel
    val branches by vm.branches().collectAsState(initial = emptyList())
    val currentId by vm.currentId().collectAsState(initial = null)
    val current = branches.firstOrNull { it.id == currentId }

    // Estado local da tela
    var exp by remember { mutableStateOf(false) }
    var target by remember { mutableStateOf(SyncTarget.LOJA) }
    val ui by vm.ui.collectAsState(initial = SyncUiState())

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                title = { Text("Sincronizar inventário", color = Color.White) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configurações",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34),
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            // Gradiente no topo (padrão do app)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E2E38),
                                Color(0xFFBAC4C7),
                                Color.White
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ---------------------------
                // Seletor de FILIAL (multi-host)
                // ---------------------------
                if (branches.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = exp,
                        onExpandedChange = { exp = !exp }
                    ) {
                        TextField(
                            value = current?.nome ?: "Selecione a filial…",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Filial ativa") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = exp)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = exp,
                            onDismissRequest = { exp = false }
                        ) {
                            branches.forEach { b ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "${b.nome}  (${b.backendUrl})",
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    onClick = {
                                        exp = false
                                        vm.setCurrentBranch(b.id)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // Nenhuma filial configurada: botão para criar filiais de exemplo
                    OutlinedButton(
                        onClick = { vm.saveExampleBranches() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Criar filiais de exemplo")
                    }
                }

                // Opções de alvo (loja/deposito)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Escolha qual estoque sincronizar:",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        OptionRow(
                            selected = target == SyncTarget.LOJA,
                            label = "Estoque da Loja",
                            onClick = { target = SyncTarget.LOJA }
                        )
                        HorizontalDivider()
                        OptionRow(
                            selected = target == SyncTarget.DEPOSITO,
                            label = "Estoque do Depósito",
                            onClick = { target = SyncTarget.DEPOSITO }
                        )
                    }
                }

                // Botão principal (padrão visual do menu, cantos pequenos)
                Button(
                    onClick = {
                        val estoque = if (target == SyncTarget.LOJA) "loja" else "deposito"
                        vm.sync(estoque = estoque, filtro = null) // pageSize padrão 500
                    },
                    enabled = !ui.syncing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F3A34),
                        contentColor = Color.White
                    )
                ) {
                    if (ui.syncing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Sincronizando…")
                    } else {
                        Text("Fazer sincronismo")
                    }
                }

                // Mensagem de status (igual estilo da exportação)
                ui.message?.let { msg ->
                    // considera sucesso se a mensagem começar com "Sincronismo concluído"
                    val success =
                        msg.startsWith("Sincronismo concluído", ignoreCase = true)

                    Text(
                        text = (if (success) "✅ " else "❌ ") + msg,
                        color = if (success) Color(0xFF145B4D) else Color.Red
                    )
                }
            }
        }
    }
}

/** Linha clicável com RadioButton + texto */
@Composable
private fun OptionRow(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color.Black)
    }
}

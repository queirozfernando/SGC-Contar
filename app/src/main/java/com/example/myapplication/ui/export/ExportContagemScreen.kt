@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.myapplication.ui.export

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.sync.SyncViewModel
import kotlinx.coroutines.delay

@Composable
fun ExportContagemScreen(
    onBack: () -> Unit = {},
    vm: ExportVm = hiltViewModel(),
    apiToken: String? = null,
    bearerJwt: String? = null
) {
    val ui by vm.ui.collectAsState()

    // VM de sincronismo para ler FILIAL ATIVA e tipo de estoque
    val syncVm: SyncViewModel = hiltViewModel()
    val branches by syncVm.branches().collectAsState(initial = emptyList())
    val currentId by syncVm.currentId().collectAsState(initial = null)
    val currentBranch = branches.firstOrNull { it.id == currentId }
    val filialNome = currentBranch?.nome ?: "Nenhuma filial ativa"

    // tipo de estoque salvo no sincronismo: "loja" | "deposito" | null
    val lastEstoque by syncVm.lastEstoque().collectAsState(initial = null)
    val estoqueTipoRaw = lastEstoque.orEmpty()

    val estoqueLabel = when (estoqueTipoRaw) {
        "loja" -> "Loja"
        "deposito" -> "Depósito"
        else -> "Não definido (sincronize antes)"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exportar contagem", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
        }
    ) { pad ->
        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFBAC4C7), Color(0xFF1E2E38))
                        )
                    )
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Filial ativa:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF0F3A34)
                )
                Text(
                    text = filialNome,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F3A34)
                )

                Text(
                    text = "Tipo de estoque:",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF0F3A34)
                )
                Text(
                    text = estoqueLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF0F3A34)
                )

                Spacer(Modifier.height(8.dp))

                ExportPrimaryButton(
                    enabled = !ui.loading && currentBranch != null,
                    onClick = {
                        // Deixa o repositório resolver loja/nome de arquivo
                        val lojaCodigo =
                            currentBranch?.dbName
                                ?.takeIf { it.isNotBlank() }
                                ?: currentBranch?.nome
                                ?: "desconhecida"

                        val deviceId = android.os.Build.MODEL ?: "ANDROID_DEVICE"
                        val operador = "OPERADOR"
                        val tokenDaFilial = currentBranch?.apiToken?.takeIf { it.isNotBlank() }

                        vm.exportar(
                            loja = lojaCodigo,
                            deviceId = deviceId,
                            operador = operador,
                            apiToken = tokenDaFilial ?: apiToken,
                            bearerJwt = bearerJwt,
                            nomeArquivoOpcional = null  // deixa o repo gerar
                        )
                    }
                ) {
                    Text(if (ui.loading) "Exportando..." else "Exportar contagem")
                }

                if (ui.loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }

                ui.message?.let {
                    Text(
                        text = if (ui.ok)
                            "✅ ${ui.message}"
                        else
                            "❌ $it",
                        color = if (ui.ok) Color(0xFF145B4D) else Color.Red
                    )
                }
            }
        }
    }
}

/** Botão estilizado — igual aos botões do menu */
@Composable
private fun ExportPrimaryButton(
    enabled: Boolean,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }

    LaunchedEffect(pressed) {
        if (pressed) {
            delay(120)
            pressed = false
        }
    }

    Button(
        onClick = {
            pressed = true
            onClick()
        },
        enabled = enabled,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) Color(0xFF145B4D) else Color(0xFF0F3A34),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF0F3A34).copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (pressed) 2.dp else 6.dp,
            pressedElevation = 8.dp,
            disabledElevation = 0.dp
        ),
        content = content
    )
}

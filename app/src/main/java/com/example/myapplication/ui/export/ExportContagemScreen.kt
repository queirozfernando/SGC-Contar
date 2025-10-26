package com.example.myapplication.ui.export

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.ContagemEntity
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.repo.ContagemRepository
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.export.ExportUtils
import com.example.myapplication.ui.components.MenuPrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportContagemScreen(
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as InventoryApp

    val productRepo = remember { ProductRepository(context, app.db.productDao()) }
    val contagemRepo = remember { ContagemRepository(app.db.contagemDao()) }
    val scope = rememberCoroutineScope()

    var status by remember { mutableStateOf("Escolha se deseja exportar todos os produtos ou somente os contados.") }
    var isRunning by remember { mutableStateOf(false) }
    var apenasContados by remember { mutableStateOf(false) }

    var produtos by remember { mutableStateOf<List<ProductEntity>>(emptyList()) }
    var contagens by remember { mutableStateOf<List<ContagemEntity>>(emptyList()) }

    // Carregar dados
    LaunchedEffect(Unit) {
        isRunning = true
        try {
            withContext(Dispatchers.IO) {
                produtos = productRepo.getAll()
                contagens = contagemRepo.getAll()
            }
            val contados = contagens.map { it.productId }.toSet().size
            status = "Produtos: ${produtos.size}  •  Contados: $contados"
        } catch (t: Throwable) {
            status = "Falhou ao carregar: ${t.message}"
        } finally {
            isRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Exportar contagem (CSV)", color = Color.White) },
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
            // 🎨 Gradiente no topo — igual ao ImportEstoqueScreen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Status / contadores
                Text(text = status, color = Color.White, modifier = Modifier.padding(top = 12.dp))

                if (isRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                // Linha do switch — mesmas cores do ImportEstoqueScreen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Switch(
                        checked = apenasContados,
                        enabled = !isRunning,
                        onCheckedChange = { apenasContados = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFE6F2EF),
                            checkedTrackColor = Color(0xFF0F3A34),
                            uncheckedThumbColor = Color(0xFFCCCCCC),
                            uncheckedTrackColor = Color(0xFF777777)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exportar somente contados",
                        color = Color.White
                    )
                }

                // 🔘 Botões no mesmo padrão/posição do ImportEstoqueScreen
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuPrimaryButton(
                        enabled = !isRunning && produtos.isNotEmpty(),
                        onClick = {
                            scope.launch {
                                isRunning = true
                                status = if (apenasContados) "Gerando CSV (somente contados)..." else "Gerando CSV..."
                                try {
                                    val listaContagens = withContext(Dispatchers.IO) { contagemRepo.getAll() }
                                    val produtosParaExportar =
                                        if (apenasContados) {
                                            val idsContados = listaContagens.map { it.productId }.toSet()
                                            produtos.filter { it.id in idsContados }
                                        } else produtos

                                    val uri = ExportUtils.exportToCsv(context, produtosParaExportar, listaContagens)

                                    // Share sheet
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(
                                            share,
                                            if (apenasContados) "Exportar contados (CSV)" else "Exportar inventário (CSV)"
                                        )
                                    )

                                    val contados = listaContagens.map { it.productId }.toSet().size
                                    status = "Exportado! Produtos: ${produtosParaExportar.size}  •  Contados: $contados"
                                } catch (t: Throwable) {
                                    status = "Falhou ao exportar: ${t.message}"
                                } finally {
                                    isRunning = false
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text(text = "Exportar CSV") }

                    MenuPrimaryButton(
                        enabled = !isRunning,
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text(text = "Concluir") }
                }
            }
        }
    }
}

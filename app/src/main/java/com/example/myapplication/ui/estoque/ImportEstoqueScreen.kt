package com.example.myapplication.ui.estoque

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.CsvImporter
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.ui.components.MenuPrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportEstoqueScreen(
    onDone: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as InventoryApp
    val repo = remember { ProductRepository(context, app.db.productDao()) }

    val scope = rememberCoroutineScope()

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var status by remember {
        mutableStateOf("Selecione um CSV no formato: id;ean;nome;uom;stq;updated_at")
    }
    var isRunning by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<CsvImporter.Progress?>(null) }
    var clearBefore by remember { mutableStateOf(false) }
    var importedList by remember { mutableStateOf<List<ProductEntity>>(emptyList()) }

    val pickCsv = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        pickedUri = uri
        if (uri != null) {
            isRunning = true
            status = if (clearBefore) "Limpando e importando..." else "Importando..."
            progress = null
            importedList = emptyList()

            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        repo.importFromCsv(uri, clearBefore) { p ->
                            scope.launch { progress = p }
                        }
                    }
                    importedList = withContext(Dispatchers.IO) { repo.getAll() }
                    status =
                        "Concluído! Processados=${progress?.processed ?: 0} • Inseridos=${progress?.inserted ?: 0} • Ignorados=${progress?.skipped ?: 0}"
                } catch (t: Throwable) {
                    status = "Falhou: ${t.message}"
                } finally {
                    isRunning = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Carregar estoque (CSV)",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34), // 💚 mesma cor dos botões
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
            // 🎨 Gradiente no topo
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
                Text(
                    text = status,
                    color = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                )

                if (isRunning) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    val p = progress
                    if (p != null) {
                        Text(
                            text = "Processados: ${p.processed} • Inseridos: ${p.inserted} • Ignorados: ${p.skipped}",
                            color = Color.White
                        )
                    } else {
                        Text(text = "Preparando…", color = Color.White)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Switch(
                        checked = clearBefore,
                        enabled = !isRunning,
                        onCheckedChange = { clearBefore = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFE6F2EF),       // bolinha clara (igual texto dos botões)
                            checkedTrackColor = Color(0xFF0F3A34),       // faixa verde escuro (igual fundo do botão)
                            uncheckedThumbColor = Color(0xFFCCCCCC),     // bolinha cinza
                            uncheckedTrackColor = Color(0xFF777777)      // faixa cinza escura
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Limpar tabela antes de importar", color = Color.White)
                }

                // 🔘 Botões iguais ao menu
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MenuPrimaryButton(
                        enabled = !isRunning,
                        onClick = {
                            pickCsv.launch(arrayOf("text/csv", "text/*", "application/octet-stream"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text(text = "Escolher CSV") }

                    MenuPrimaryButton(
                        enabled = !isRunning,
                        onClick = onDone,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text(text = "Concluir") }
                }

                // 🧾 Lista de registros importados
                if (importedList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Registros importados: ${importedList.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF0F3A34)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color.White)
                            .padding(vertical = 8.dp)
                    ) {
                        // Cabeçalho
                        item {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ID",
                                    modifier = Modifier.width(50.dp),
                                    color = Color.Gray,
                                    textAlign = TextAlign.End
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "EAN",
                                    modifier = Modifier.weight(0.25f),
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Nome",
                                    modifier = Modifier.weight(0.55f),
                                    color = Color.Gray
                                )
                                Text(
                                    text = "UNID",
                                    modifier = Modifier
                                        .width(70.dp)
                                        .padding(start = 8.dp),
                                    color = Color.Gray
                                )
                            }
                            HorizontalDivider()
                        }

                        // Itens
                        items(importedList) { p ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = p.id.toString(),
                                    modifier = Modifier.width(50.dp),
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.End
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = p.ean ?: "-",
                                    modifier = Modifier.weight(0.25f),
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = p.nome,
                                    modifier = Modifier.weight(0.55f),
                                    color = Color.Black
                                )
                                Text(
                                    text = p.uom,
                                    modifier = Modifier
                                        .width(70.dp)
                                        .padding(start = 8.dp),
                                    color = Color.Gray
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

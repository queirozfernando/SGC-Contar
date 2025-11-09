package com.example.myapplication.ui.estoque

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.ProductEntity
import com.example.myapplication.data.repo.ProductRepository
import com.example.myapplication.ui.components.MenuPrimaryButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportEstoqueScreen(
    onDone: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val app = ctx.applicationContext as InventoryApp

    // ✅ Passe somente o DAO
    val productRepo = remember { ProductRepository(app.db.productDao()) }

    var fileName by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Selecione um CSV para importar o estoque.") }
    var successCount by remember { mutableStateOf(0) }
    var failCount by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }

    // Guardamos a Uri selecionada no estado
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri == null) return@rememberLauncherForActivityResult
            // Persist permission (opcional)
            try {
                ctx.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Throwable) { /* ignore */ }

            fileName = uri.toString()
            selectedUri = uri
        }
    )

    // 🚀 Executa a importação quando selectedUri muda
    LaunchedEffect(selectedUri) {
        val uri = selectedUri ?: return@LaunchedEffect
        status = "Arquivo selecionado. Iniciando importação…"
        isRunning = true

        val result = importFromCsv(
            context = ctx,
            uri = uri,
            onRow = { row ->
                // Monte a entidade mínima necessária para atualizar o estoque
                val entity = ProductEntity(
                    id   = row.id,
                    ean  = row.ean,
                    nome = "(sem nome)",        // ✅ evita vazio nulo (schema exige String não-nulo)
                    uom  = "UN",                // ✅ String não-nulo; antes estava null
                    stq  = row.qtd
                )
                productRepo.upsert(entity)
            }
        )

        successCount = result.success
        failCount = result.fail
        status = "Importação concluída. Sucesso: $successCount  •  Falhas: $failCount"
        isRunning = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Importar Estoque (CSV)", color = Color.White) },
                navigationIcon = {
                    TextButton(onClick = onDone) { Text("Voltar", color = Color.White) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F3A34),
                    titleContentColor = Color.White
                )
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = status,
                color = Color(0xFF0F3A34),
                fontWeight = FontWeight.Medium
            )

            if (fileName != null) {
                Text(text = "Arquivo: $fileName", color = Color.Gray)
            }

            MenuPrimaryButton(
                onClick = {
                    launcher.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isRunning
            ) {
                Text(if (isRunning) "Processando…" else "Selecionar CSV")
            }

            if (isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(8.dp))

            Text("Sucessos: $successCount   |   Falhas: $failCount")
        }
    }
}

/* ====================== Utilitários CSV ====================== */

private data class CsvRow(val id: Long, val ean: String?, val qtd: Double)
private data class ImportResult(val success: Int, val fail: Int)

/**
 * Lê o CSV de [uri] e chama [onRow] para cada linha válida.
 * Aceita cabeçalhos comuns: id, ean, qty/quantidade/qtd/stq/estoque.
 */
private suspend fun importFromCsv(
    context: Context,
    uri: Uri,
    onRow: suspend (CsvRow) -> Unit
): ImportResult = withContext(Dispatchers.IO) {
    var ok = 0
    var bad = 0

    val cr = context.contentResolver
    cr.openInputStream(uri)?.use { input ->
        BufferedReader(InputStreamReader(input)).use { br ->
            val headerLine = br.readLine() ?: return@use
            val headers = headerLine.split(';', ',', '\t')
                .map { it.trim().lowercase() }

            val idxId  = headers.indexOfFirst { it in setOf("id", "produtos_id", "produto_id") }
            val idxEan = headers.indexOfFirst { it in setOf("ean", "ean13", "codigo_barras") }
            val idxQty = headers.indexOfFirst { it in setOf("qty", "quantidade", "qtd", "stq", "estoque") }

            if (idxId == -1 || idxQty == -1) {
                return@withContext ImportResult(success = 0, fail = 1)
            }

            var line: String?
            while (true) {
                line = br.readLine() ?: break
                if (line!!.isBlank()) continue

                val cols = line!!.split(';', ',', '\t')

                val idStr  = cols.getOrNull(idxId)?.trim()
                val eanStr = cols.getOrNull(idxEan)?.trim()
                val qtyStr = cols.getOrNull(idxQty)?.trim()

                val id  = idStr?.toLongOrNull()
                val qty = qtyStr?.replace(',', '.')?.toDoubleOrNull()

                if (id == null || qty == null) {
                    bad++
                    continue
                }

                try {
                    onRow(CsvRow(id = id, ean = eanStr, qtd = qty))
                    ok++
                } catch (_: Throwable) {
                    bad++
                }
            }
        }
    }

    ImportResult(success = ok, fail = bad)
}

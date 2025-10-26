package com.example.myapplication.data.local

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.yield
import java.io.BufferedReader
import java.io.InputStreamReader

class CsvImporter(
    private val context: Context,
    private val productDao: ProductDao
) {

    data class Progress(val processed: Int, val inserted: Int, val skipped: Int)

    /** Importa produtos a partir de um arquivo CSV. */
    suspend fun importFromUri(
        uri: Uri,
        onProgress: (Progress) -> Unit = {}
    ) {
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Não foi possível abrir o arquivo." }

            val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))

            var headerRead = false
            val batch = ArrayList<ProductEntity>(2000)
            var processed = 0
            var inserted = 0
            var skipped = 0

            suspend fun flushAndReport() {
                if (batch.isNotEmpty()) {
                    productDao.upsertAll(batch)
                    inserted += batch.size
                    batch.clear()
                    onProgress(Progress(processed, inserted, skipped))
                    yield() // cede tempo ao scheduler para não travar a UI
                }
            }

            reader.useLines { seq ->
                seq.forEach { raw ->
                    val line = raw.trimEnd('\r', '\n')
                    if (!headerRead) { headerRead = true; return@forEach } // pula cabeçalho
                    if (line.isBlank()) return@forEach

                    val cols = parseCsvLine(line, ';')

                    // Esperado: id;ean;nome;uom;stq;updated_at(opcional)
                    if (cols.size < 5) { skipped++; processed++; return@forEach }

                    val id = cols[0].toLongOrNull()
                    val eanIn = cols.getOrNull(1)?.trim().orEmpty()
                    val nome = cols.getOrNull(2)?.trim().orEmpty()
                    val uom  = cols.getOrNull(3)?.trim()?.ifBlank { "UN" } ?: "UN"
                    val stq  = cols.getOrNull(4)?.replace(",", ".")?.toDoubleOrNull()
                    val updatedAt = cols.getOrNull(5)?.takeIf { it.isNotBlank() }

                    if (id == null || nome.isBlank() || stq == null) {
                        skipped++; processed++; return@forEach
                    }

                    val normEan = normalizeEanOrNull(eanIn)

                    batch += ProductEntity(
                        id = id,
                        ean = normEan,
                        nome = nome.take(120),
                        uom = uom,
                        stq = stq,
                        updatedAt = updatedAt
                    )

                    processed++
                    if (batch.size >= 2000) {
                        // grava lote intermediário
                        flushAndReport()
                    }
                }
            }

            // grava lote final e notifica progresso
            flushAndReport()
        }
    }

    /** Divide linha de CSV respeitando aspas. */
    private fun parseCsvLine(line: String, sep: Char): List<String> {
        val out = ArrayList<String>(8)
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '"') {
                    val next = if (i + 1 < line.length) line[i + 1] else null
                    if (next == '"') { sb.append('"'); i++ } else { inQuotes = false }
                } else sb.append(ch)
            } else {
                when (ch) {
                    '"' -> inQuotes = true
                    sep -> { out += sb.toString(); sb.setLength(0) }
                    else -> sb.append(ch)
                }
            }
            i++
        }
        out += sb.toString()
        return out
    }

    /** Normaliza EAN (remove não-dígitos e completa até 13). */
    private fun normalizeEanOrNull(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        if (digits.isBlank()) return null
        return when (digits.length) {
            8, 12 -> digits.padStart(13, '0')
            13, 14 -> digits
            else -> digits
        }
    }
}

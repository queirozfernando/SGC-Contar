package com.example.myapplication.export

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.myapplication.data.local.ContagemEntity
import com.example.myapplication.data.local.ProductEntity
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gera um CSV (UTF-8, separador ';') com produtos + contagens e:
 *  - salva em /data/data/<package>/files/exports (área privada)
 *  - cria cópia em /storage/emulated/0/Download/
 *  - retorna Uri FileProvider para compartilhamento seguro.
 */
object ExportUtils {

    data class ExportRow(
        val id: Long,
        val nome: String,
        val uom: String?,
        val ean: String?,
        val estoqueImportado: Double?,
        val qtdContada: Double?
    )

    private fun List<ProductEntity>.toMapById(): Map<Long, ProductEntity> =
        associateBy { it.id }

    fun buildRows(
        produtos: List<ProductEntity>,
        contagens: List<ContagemEntity>
    ): List<ExportRow> {
        val mapProduto = produtos.toMapById()
        return produtos.map { p ->
            val c = contagens.firstOrNull { it.productId == p.id }
            ExportRow(
                id = p.id,
                nome = p.nome,
                uom = p.uom,
                ean = p.ean,
                estoqueImportado = p.stq,
                qtdContada = c?.qty
            )
        }
    }

    private fun Double?.toCsvNumber(): String =
        when (this) {
            null -> ""
            else -> {
                val raw = if (this % 1.0 == 0.0) this.toLong().toString() else this.toString()
                raw.replace(',', '.')
            }
        }

    private fun String?.csvEscape(): String {
        if (this.isNullOrEmpty()) return ""
        val escaped = this.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    /**
     * Cria o arquivo CSV no storage interno + cópia em Download e retorna o Uri via FileProvider.
     */
    fun exportToCsv(
        context: Context,
        produtos: List<ProductEntity>,
        contagens: List<ContagemEntity>,
        fileNamePrefix: String = "inventario"
    ): Uri {
        val rows = buildRows(produtos, contagens)

        val header = listOf(
            "id", "nome", "uom", "ean",
            "estoque_importado", "qtd_contada"
        )

        val sb = StringBuilder()
        sb.append(header.joinToString(";")).append("\n")

        rows.forEach { r ->
            sb.append(
                listOf(
                    r.id.toString(),
                    r.nome.csvEscape(),
                    r.uom.csvEscape(),
                    r.ean.csvEscape(),
                    r.estoqueImportado.toCsvNumber(),
                    r.qtdContada.toCsvNumber()
                ).joinToString(";")
            ).append("\n")
        }

        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${fileNamePrefix}_${ts}.csv"

        // 🔹 Pasta privada (para FileProvider)
        val dir = File(context.filesDir, "exports").apply { if (!exists()) mkdirs() }
        val outFile = File(dir, fileName)

        FileOutputStream(outFile).use { fos ->
            fos.write(sb.toString().toByteArray(StandardCharsets.UTF_8))
            fos.flush()
        }

        // 🔹 Cópia adicional no diretório público "Download"
        try {
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val copyFile = File(downloadsDir, fileName)
            outFile.copyTo(copyFile, overwrite = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 🔹 Retorna o URI compatível com FileProvider
        val authority = context.packageName + ".fileprovider"
        return FileProvider.getUriForFile(context, authority, outFile)
    }
}

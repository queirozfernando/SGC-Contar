package com.example.myapplication.ui.export

import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.remote.export.ExportApi
import com.example.myapplication.data.remote.export.InventoryExportPayload
import com.example.myapplication.data.remote.export.InventoryItemPayload
import com.example.myapplication.data.settings.SettingsRepository
import com.example.myapplication.ui.export.dto.ContagemImportIn
import com.example.myapplication.ui.export.dto.ContagemItemIn
import com.example.myapplication.ui.export.dto.ImportResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Singleton
class ExportRepository @Inject constructor(
    private val db: AppDb,
    private val api: ExportApi,
    private val settings: SettingsRepository
) {

    /**
     * === JÁ EXISTIA ===
     * Continua casado com o inventory_import.py (se ainda estiver usando).
     */
    suspend fun exportarContagem(
        loja: String?,
        deviceId: String?,
        operador: String?,
        apiToken: String?,          // X-Api-Token (opcional)
        bearerJwt: String?,         // Authorization: Bearer <jwt> (opcional)
        nomeArquivo: String? = null // se nulo, geramos automaticamente
    ): ImportResponse {

        val currentId = settings.currentId().first()
            ?: error("Nenhuma filial ativa nas configurações.")

        val branches = settings.branches().first()
        val branch = branches.firstOrNull { it.id == currentId }
            ?: error("Filial ativa ($currentId) não encontrada na lista de filiais.")

        val estoqueTipo = settings.lastEstoque().first().orEmpty().ifBlank { "loja" }

        val lojaFinal = loja
            ?: branch.dbName.takeIf { it.isNotBlank() }
            ?: branch.nome
            ?: "desconhecida"

        val nomeArquivoFinal = nomeArquivo ?: gerarNomeArquivoPadrao(estoqueTipo)

        val contagens = db.contagemDao().getAll()
        if (contagens.isEmpty()) {
            error("Não há contagens para exportar.")
        }

        val produtosPorId = db.productDao().getAll().associateBy { it.id }

        val items = contagens.map { c ->
            val p = produtosPorId[c.productId]
            ContagemItemIn(
                produto_id = c.productId,
                ean = c.ean ?: p?.ean,
                nome = p?.nome,
                unidade = p?.uom,
                estoque_importado = p?.stq ?: 0.0,
                qtd_contada = c.qty
            )
        }

        val body = ContagemImportIn(
            loja = lojaFinal,
            device_id = deviceId,
            operador = operador,
            nome_arquivo = nomeArquivoFinal,
            items = items
        )

        val tokenDaFilial = branch.apiToken.takeIf { it.isNotBlank() }
        val xApi = apiToken ?: tokenDaFilial ?: "dev"

        val bearerHeader = when {
            bearerJwt.isNullOrBlank() -> "Bearer dev"
            bearerJwt.startsWith("Bearer ") -> bearerJwt
            else -> "Bearer $bearerJwt"
        }

        return api.importContagem(
            body = body,
            apiKey = xApi,
            bearer = bearerHeader
        )
    }

    /**
     * === NOVO MÉTODO ===
     * Usa o endpoint /inventory/export (inventory_export.py)
     * com headers X-DB-Host e X-DB-Name.
     */
    suspend fun exportarParaERP(): ExportResult {
        val currentId = settings.currentId().first()
            ?: error("Nenhuma filial ativa nas configurações.")

        val branches = settings.branches().first()
        val branch = branches.firstOrNull { it.id == currentId }
            ?: error("Filial ativa ($currentId) não encontrada na lista de filiais.")

        val estoqueTipo = settings.lastEstoque().first().orEmpty().ifBlank { "loja" }

        val lojaFinal = branch.dbName.takeIf { it.isNotBlank() }
            ?: branch.nome
            ?: "desconhecida"

        val nomeArquivoFinal = gerarNomeArquivoPadrao(estoqueTipo)

        val contagens = db.contagemDao().getAll()
        if (contagens.isEmpty()) {
            error("Não há contagens para exportar.")
        }

        val produtosPorId = db.productDao().getAll().associateBy { it.id }

        val items = contagens.map { c ->
            val p = produtosPorId[c.productId]
            InventoryItemPayload(
                id = c.productId,
                ean = (c.ean ?: p?.ean).orEmpty(),
                nome = (p?.nome).orEmpty(),
                uom = (p?.uom).orEmpty(),
                qty = c.qty,
                stq_atual = p?.stq ?: 0.0
            )
        }

        val payload = InventoryExportPayload(
            estoque = estoqueTipo,
            filename = nomeArquivoFinal,
            loja = lojaFinal,
            items = items
        )

        // Headers do banco – mesmos que você preenche no Swagger
        val host = branch.dbServer.takeIf { it.isNotBlank() } ?: "amadeu.myftp.org"
        val dbName = branch.dbName.takeIf { it.isNotBlank() } ?: "amadeu"

        val response = api.exportInventory(
            host = host,
            dbName = dbName,
            payload = payload
        )

        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw RuntimeException("Erro ao exportar inventário: ${response.code()} - $errorBody")
        }
        return ExportResult(
            filename = nomeArquivoFinal,
            totalItems = items.size
        )

    }

    private fun gerarNomeArquivoPadrao(
        estoque: String
    ): String {
        val estoqueSlug = estoque.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "indefinido" }

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val stamp = sdf.format(Date())

        return "contagem_${estoqueSlug}_${stamp}.json"
    }
}

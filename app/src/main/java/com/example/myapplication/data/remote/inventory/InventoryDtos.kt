package com.example.myapplication.data.remote.inventory

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ====== GET /inventory/sync  (mantém sua estrutura)
@JsonClass(generateAdapter = true)
data class CatalogItemDto(
    @Json(name = "id")   val id: Long,
    @Json(name = "ean")  val ean: String? = null,
    @Json(name = "nome") val nome: String = "",
    @Json(name = "uom")  val uom: String? = null,
    @Json(name = "stq")  val stq: Double? = null,
    // backend agora envia também este campo; deixamos opcional p/ compatibilidade
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class CatalogResponse(
    @Json(name = "items")  val items: List<CatalogItemDto>? = emptyList(),
    @Json(name = "total")  val total: Int? = null,
    // novos (opcionais) — o backend envia limit/offset na resposta
    @Json(name = "limit")  val limit: Int? = null,
    @Json(name = "offset") val offset: Int? = null
)


// ====== POST /inventory/export
@JsonClass(generateAdapter = true)
data class ExportItemInDto(
    @Json(name = "id")        val id: Long,
    @Json(name = "ean")       val ean: String? = null,
    @Json(name = "nome")      val nome: String,
    @Json(name = "uom")       val uom: String,
    @Json(name = "qty")       val qty: Double,
    @Json(name = "stq_atual") val stqAtual: Double? = null
)

@JsonClass(generateAdapter = true)
data class ExportPayloadDto(
    // "loja" | "deposito"
    @Json(name = "estoque")  val estoque: String,
    @Json(name = "filename") val filename: String? = null,
    @Json(name = "loja")     val loja: String? = null,
    @Json(name = "items")    val items: List<ExportItemInDto>
)

@JsonClass(generateAdapter = true)
data class ExportResultDto(
    @Json(name = "ok")          val ok: Boolean,
    @Json(name = "host")        val host: String,
    @Json(name = "database")    val database: String,
    @Json(name = "contagem_id") val contagemId: Long,
    @Json(name = "filename")    val filename: String,
    @Json(name = "total_itens") val totalItens: Int
)

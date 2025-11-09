package com.example.myapplication.ui.export.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ContagemItemIn(
    val produto_id: Long,
    val ean: String? = null,
    val nome: String? = null,
    val unidade: String? = null,
    val estoque_importado: Double? = null,
    val qtd_contada: Double
)

@JsonClass(generateAdapter = true)
data class ContagemImportIn(
    val loja: String? = null,
    val device_id: String? = null,
    val operador: String? = null,
    val nome_arquivo: String? = null,
    val items: List<ContagemItemIn>
)

@JsonClass(generateAdapter = true)
data class ImportResponse(
    val status: String,
    val contagem_id: Long,
    val nome_arquivo: String,
    val imported: Int,
    val upsert: Boolean
)

package com.example.myapplication.data.remote.export

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InventoryExportPayload(
    val estoque: String,
    val filename: String,
    val loja: String,
    val items: List<InventoryItemPayload>
)

@JsonClass(generateAdapter = true)
data class InventoryItemPayload(
    val id: Long,
    val ean: String,
    val nome: String,
    val uom: String,
    val qty: Double,
    val stq_atual: Double
)

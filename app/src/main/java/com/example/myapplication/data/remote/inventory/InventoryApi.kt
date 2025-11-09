package com.example.myapplication.data.remote.inventory

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface InventoryApi {

    // === GET /inventory/sync ===
    @GET("/inventory/sync")
    suspend fun syncInventory(
        @Header("X-DB-Host") dbHost: String,
        @Header("X-DB-Name") dbName: String,
        @Query("estoque") estoque: String,      // "loja" | "deposito"
        @Query("limit")   limit: Int,
        @Query("offset")  offset: Int,
        @Header("X-Api-Token") apiKey: String? = null,
        @Header("Authorization") bearer: String? = null
    ): CatalogResponse


    // === POST /inventory/export ===
    @POST("inventory/export")
    suspend fun exportCounting(
        @Header("X-DB-Host") dbHost: String,
        @Header("X-DB-Name") dbName: String,
        @Body payload: ExportPayloadDto
    ): ExportResultDto
}

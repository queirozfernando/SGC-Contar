package com.example.myapplication.data.remote.export

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import com.example.myapplication.ui.export.dto.ContagemImportIn
import com.example.myapplication.ui.export.dto.ImportResponse

interface ExportApi {

    // se ainda usar o /inventory/import
    @POST("/inventory/import")
    suspend fun importContagem(
        @Body body: ContagemImportIn,
        @Header("X-Api-Token") apiKey: String,
        @Header("Authorization") bearer: String
    ): ImportResponse

    // nosso /inventory/export do Swagger
    @POST("/inventory/export")
    suspend fun exportInventory(
        @Header("X-DB-Host") host: String,
        @Header("X-DB-Name") dbName: String,
        @Body payload: InventoryExportPayload
    ): Response<Unit>
}

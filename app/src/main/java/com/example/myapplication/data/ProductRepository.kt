package com.example.myapplication.data.repo

import android.content.Context
import android.net.Uri
import com.example.myapplication.data.local.CsvImporter
import com.example.myapplication.data.local.ProductDao
import com.example.myapplication.data.local.ProductEntity

class ProductRepository(
    private val context: Context,
    private val dao: ProductDao
) {
    suspend fun getByEan(ean: String) = dao.getByEan(ean)
    suspend fun upsert(p: ProductEntity) = dao.upsert(p)

    suspend fun importFromCsv(
        uri: Uri,
        clearBefore: Boolean,
        onProgress: (CsvImporter.Progress) -> Unit = {}
    ) {
        if (clearBefore) dao.clearAll()
        CsvImporter(context, dao).importFromUri(uri, onProgress)
    }

    // ✅ necessário para a listagem pós-importação
    suspend fun getAll(): List<ProductEntity> = dao.getAll()
}

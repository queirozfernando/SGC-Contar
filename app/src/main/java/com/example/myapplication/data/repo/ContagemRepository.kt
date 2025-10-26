package com.example.myapplication.data.repo

import com.example.myapplication.data.local.ContagemDao
import com.example.myapplication.data.local.ContagemEntity

class ContagemRepository(
    private val dao: ContagemDao
) {
    suspend fun upsertForProduct(productId: Long, ean: String?, qty: Double) {
        dao.upsert(
            ContagemEntity(
                productId = productId,
                ean = ean,
                qty = qty,
                ts = System.currentTimeMillis()
            )
        )
    }

    suspend fun getAll(): List<ContagemEntity> = dao.getAll()

    suspend fun getMap(): Map<Long, ContagemEntity> =
        dao.getAll().associateBy { it.productId }

    suspend fun clearAll() = dao.clearAll()
}

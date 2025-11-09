package com.example.myapplication.data.repo

import com.example.myapplication.data.local.ContagemDao
import com.example.myapplication.data.local.ContagemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContagemRepository @Inject constructor(
    private val dao: ContagemDao
) {

    suspend fun getAll(): List<ContagemEntity> = dao.getAll()

    suspend fun clearAll() = dao.clearAll()

    /**
     * Salva a contagem para um produto.
     * Se já existir linha para o productId, REPLACE troca pela nova.
     */
    suspend fun upsertForProduct(
        productId: Long,
        ean: String?,
        qty: Double
    ) {
        dao.upsert(
            ContagemEntity(
                productId = productId,
                ean = ean,
                qty = qty
            )
        )
    }
}

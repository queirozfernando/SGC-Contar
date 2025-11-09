package com.example.myapplication.data.repo

import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.local.ProductEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    private val db: AppDb,
    private val syncRepo: InventorySyncRepository
) {
    private val productDao = db.productDao()

    suspend fun getAllLocal(): List<ProductEntity> = productDao.getAll()

    suspend fun refreshFromRemote(estoque: String, pageSize: Int = 500) {
        syncRepo.pullAndSave(estoque = estoque, pageSize = pageSize)
    }
}

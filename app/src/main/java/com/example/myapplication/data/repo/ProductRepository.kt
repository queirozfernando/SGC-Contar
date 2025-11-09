package com.example.myapplication.data.repo

import com.example.myapplication.data.local.ProductDao
import com.example.myapplication.data.local.ProductEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val dao: ProductDao
) {
    suspend fun getAll(): List<ProductEntity> = dao.getAll()
    suspend fun upsert(entity: ProductEntity) = dao.upsertAll(listOf(entity))
    suspend fun upsertAll(list: List<ProductEntity>) = dao.upsertAll(list)

    // 👇 novo
    suspend fun count(): Int = dao.count()
}

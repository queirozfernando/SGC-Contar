package com.example.myapplication.data.local

import androidx.room.*

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE ean = :ean LIMIT 1")
    suspend fun getByEan(ean: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(p: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Query("SELECT * FROM products ORDER BY nome")
    suspend fun getAll(): List<ProductEntity>

}

package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {

    // Insere ou atualiza em lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ProductEntity>)

    // Lista todos os produtos (usado por CatalogRepository, ExportRepository etc.)
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<ProductEntity>

    // Quantidade de registros (diagnóstico)
    @Query("SELECT COUNT(*) FROM product")
    suspend fun count(): Int

    // Limpa todos os produtos (usado antes do sync de outra filial)
    @Query("DELETE FROM product")
    suspend fun clearAll()
}

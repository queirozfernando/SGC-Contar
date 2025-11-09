package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContagemDao {

    @Query("SELECT * FROM contagem")
    suspend fun getAll(): List<ContagemEntity>

    @Query("DELETE FROM contagem")
    suspend fun clearAll()

    // como a PK é productId, REPLACE faz o "upsert"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ContagemEntity)
}

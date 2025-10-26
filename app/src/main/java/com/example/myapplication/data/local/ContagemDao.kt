package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContagemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(c: ContagemEntity)

    @Query("SELECT * FROM contagens")
    suspend fun getAll(): List<ContagemEntity>

    @Query("DELETE FROM contagens")
    suspend fun clearAll()
}

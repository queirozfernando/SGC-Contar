package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProductEntity::class,
        ContagemEntity::class     // ⬅️ precisa estar com ::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun contagemDao(): ContagemDao
}

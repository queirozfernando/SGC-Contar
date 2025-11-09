package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, ContagemEntity::class],
    version = 6,                // 👈 subi a versão só pra garantir recriação
    exportSchema = false
)
abstract class AppDb : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun contagemDao(): ContagemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun getDatabase(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    "app.db"
                )
                    // 🔥 se a versão mudar, o Room apaga o banco antigo
                    // e recria as tabelas com o schema novo
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

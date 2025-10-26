package com.example.myapplication

import android.app.Application
import androidx.room.Room
import com.example.myapplication.data.local.AppDb

class InventoryApp : Application() {
    lateinit var db: AppDb
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            AppDb::class.java,
            "inventory.db"
        )
            // 🔧 Atualizado conforme nova API do Room
            .fallbackToDestructiveMigration(true)
            .build()
    }
}

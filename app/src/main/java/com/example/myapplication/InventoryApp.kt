package com.example.myapplication

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.example.myapplication.data.local.AppDb

@HiltAndroidApp
class InventoryApp : Application() {
    // se você usa o db direto em telas utilitárias:
    val db by lazy { AppDb.getDatabase(this) }
}

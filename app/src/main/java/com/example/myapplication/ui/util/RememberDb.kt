package com.example.myapplication.ui.util

import android.content.Context
import com.example.myapplication.InventoryApp
import com.example.myapplication.data.local.AppDb

/**
 * Helper para obter o Room DB a partir de qualquer Context.
 * Mantém o mesmo nome getInstance para compatibilidade com usos antigos.
 */
object RememberDb {

    @JvmStatic
    fun getInstance(context: Context): AppDb {
        val app = context.applicationContext as InventoryApp
        return app.db
    }
}

/** Opcional: atalho idiomático em Kotlin */
val Context.appDb: AppDb
    get() = (applicationContext as InventoryApp).db

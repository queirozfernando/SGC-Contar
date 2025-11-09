package com.example.myapplication.data

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

// Singleton global — evita múltiplas instâncias
val Context.appDataStore by preferencesDataStore("sgc_contar_prefs")

object AppPreferencesKeys {
    val KEY_DOWNLOADS_TREE = stringPreferencesKey("downloads_tree_uri")
}

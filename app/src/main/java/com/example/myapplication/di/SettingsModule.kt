package com.example.myapplication.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// mesmo delegate do seu AppDataStore.kt
private val Context.appDataStore by preferencesDataStore("sgc_contar_prefs")

@Module
@InstallIn(SingletonComponent::class)
object SettingsModule {
    private val Context.appDataStore by preferencesDataStore("sgc_contar_prefs")

    @Provides @Singleton
    fun provideAppDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.appDataStore
}


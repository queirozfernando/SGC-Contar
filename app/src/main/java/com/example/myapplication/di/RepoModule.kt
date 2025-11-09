package com.example.myapplication.di

import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.remote.export.ExportApi
import com.example.myapplication.data.remote.inventory.InventoryApi
import com.example.myapplication.data.repo.ExportRepository
import com.example.myapplication.data.repo.InventorySyncRepository
import com.example.myapplication.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepoModule {

    @Provides @Singleton
    fun provideInventorySyncRepository(
        db: AppDb,
        inventoryApi: InventoryApi,
        settings: SettingsRepository
    ): InventorySyncRepository = InventorySyncRepository(db, inventoryApi, settings)

    @Provides @Singleton
    fun provideExportRepository(
        db: AppDb,
        exportApi: ExportApi
    ): ExportRepository = ExportRepository(db, exportApi)
}

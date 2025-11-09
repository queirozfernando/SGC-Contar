package com.example.myapplication.di

import com.example.myapplication.data.remote.export.ExportApi
import com.example.myapplication.data.remote.inventory.InventoryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideExportApi(retrofit: Retrofit): ExportApi {
        return retrofit.create(ExportApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInventoryApi(retrofit: Retrofit): InventoryApi {
        return retrofit.create(InventoryApi::class.java)
    }
}

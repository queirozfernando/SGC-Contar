package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.local.ProductDao
import com.example.myapplication.data.local.ContagemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideAppDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "app.db")
            .fallbackToDestructiveMigration(true)   // ⬅️ derruba se schema mudou
            .build()



    @Provides
    fun provideProductDao(db: AppDb): ProductDao = db.productDao()

    @Provides
    fun provideContagemDao(db: AppDb): ContagemDao = db.contagemDao()
}

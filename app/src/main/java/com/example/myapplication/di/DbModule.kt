package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDb
import com.example.myapplication.data.local.ContagemDao
import com.example.myapplication.data.local.ProductDao
import com.example.myapplication.data.repo.ProductRepository   // 👈 IMPORT CORRETO!
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDb =
        Room.databaseBuilder(context, AppDb::class.java, "inventory.db")
            .fallbackToDestructiveMigration() // em prod: use migrações
            .build()

    @Provides
    fun provideProductDao(db: AppDb): ProductDao = db.productDao()

    @Provides
    fun provideContagemDao(db: AppDb): ContagemDao = db.contagemDao()

    @Provides
    @Singleton
    fun provideProductRepository(
        @ApplicationContext context: Context,
        productDao: ProductDao
    ): ProductRepository = ProductRepository(context, productDao) // 👈 tipo de retorno EXISTE
}

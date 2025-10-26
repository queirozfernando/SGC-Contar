package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "products",
    indices = [Index(value = ["ean"], unique = false)]
)
data class ProductEntity(
    @PrimaryKey val id: Long,
    val ean: String?,
    val nome: String,
    val uom: String,
    val stq: Double,
    val updatedAt: String?
)

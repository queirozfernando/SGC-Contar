package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product")
data class ProductEntity(
    @PrimaryKey val id: Long,
    val ean: String? = null,
    val nome: String,
    val uom: String? = null,
    val stq: Double = 0.0
)

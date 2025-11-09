package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contagem")
data class ContagemEntity(
    @PrimaryKey val productId: Long,
    val ean: String? = null,
    val qty: Double = 0.0
)

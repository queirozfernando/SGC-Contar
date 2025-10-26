package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Contagem por produto. Usamos productId como chave primária
 * para manter 1 contagem corrente por item.
 */
@Entity(tableName = "contagens")
data class ContagemEntity(
    @PrimaryKey val productId: Long,
    val ean: String?,      // guardamos para facilitar export e conferência
    val qty: Double,       // quantidade contada
    val ts: Long           // timestamp (System.currentTimeMillis)
)

package com.example.appventas.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "venta_items")
data class VentaItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ventaId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double
)
package com.example.appventas.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val referenceCode: String,
    val nombre: String,
    val precioVendedor: Double,
    val precioPublico: Double,
    val quantityInStock: Int
)
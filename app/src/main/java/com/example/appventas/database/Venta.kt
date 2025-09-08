package com.example.appventas.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val clienteId: Int,
    val sellerId: Int,
    val fecha: Date,
    val total: Double,
    val fechaPactadaPago: Date? = null
)
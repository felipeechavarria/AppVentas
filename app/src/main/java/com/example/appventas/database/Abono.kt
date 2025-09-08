package com.example.appventas.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "abonos")
data class Abono(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ventaId: Int, // El ID de la venta a la que pertenece este abono
    val monto: Double, // La cantidad de dinero abonada
    val fecha: Date // La fecha en que se realizó el pago
)
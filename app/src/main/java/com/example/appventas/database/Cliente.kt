package com.example.appventas.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val sellerId: Int // ID del Vendedor que registró este cliente
)
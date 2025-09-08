package com.example.appventas.database

import androidx.room.Embedded
import androidx.room.Relation

data class VentaWithItems(
    @Embedded val venta: Venta,
    @Relation(
        parentColumn = "id",
        entityColumn = "ventaId"
    )
    val items: List<VentaItem>
)
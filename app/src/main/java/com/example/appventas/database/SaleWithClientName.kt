package com.example.appventas.database

import java.util.Date

data class SaleWithClientName(
    val ventaId: Int,
    val clientName: String,
    val saleDate: Date,
    val totalAmount: Double
)
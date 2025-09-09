package com.example.appventas.database

data class DebtDetail(
    val inventoryItemId: Int,
    val productName: String,
    val quantity: Int,
    val debtAmount: Double
)
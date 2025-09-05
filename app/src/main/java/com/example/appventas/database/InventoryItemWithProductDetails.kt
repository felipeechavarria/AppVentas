package com.example.appventas.database

data class InventoryItemWithProductDetails(
    val inventoryId: Int,
    val productId: Int,
    val referenceCode: String,
    val productName: String,
    val quantity: Int,
    val status: String
)
package com.example.appventas.database

import androidx.room.Embedded

data class DebtSummary(
    @Embedded val user: User,
    val totalDebt: Double
)
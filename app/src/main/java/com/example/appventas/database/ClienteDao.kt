package com.example.appventas.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(cliente: Cliente)

    @Query("SELECT * FROM clientes WHERE sellerId = :sellerId ORDER BY nombre ASC")
    fun getClientsForSeller(sellerId: Int): Flow<List<Cliente>>

    @Query("SELECT * FROM clientes WHERE sellerId = :sellerId")
    fun getAll(sellerId: Int): Flow<List<Cliente>>
}
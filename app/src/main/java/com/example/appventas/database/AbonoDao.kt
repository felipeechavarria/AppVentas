package com.example.appventas.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AbonoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(abono: Abono)

    @Query("SELECT * FROM abonos WHERE ventaId = :ventaId ORDER BY fecha DESC")
    fun getAbonosForSale(ventaId: Int): Flow<List<Abono>>
}
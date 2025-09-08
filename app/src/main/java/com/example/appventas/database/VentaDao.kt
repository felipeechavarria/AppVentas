package com.example.appventas.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.appventas.database.Venta
import kotlinx.coroutines.flow.Flow
import androidx.room.Transaction

@Dao
interface VentaDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVenta(venta: Venta): Long // Devuelve el ID de la venta insertada

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVentaItem(item: VentaItem)
    @Query("""
        SELECT 
            v.id as ventaId,
            c.nombre as clientName,
            v.fecha as saleDate,
            v.total as totalAmount
        FROM ventas v
        JOIN clientes c ON v.clienteId = c.id
        WHERE v.sellerId = :sellerId
        ORDER BY v.fecha DESC
    """)
    fun getSalesForSeller(sellerId: Int): Flow<List<SaleWithClientName>>

    @Query("""
    SELECT 
        v.id as ventaId,
        c.nombre as clientName,
        v.fecha as saleDate,
        v.total as totalAmount
    FROM ventas v
    JOIN clientes c ON v.clienteId = c.id
    WHERE v.id = :saleId
    """)
    fun getSaleDetails(saleId: Int): Flow<SaleWithClientName>

    @Transaction
    @Query("SELECT * FROM ventas WHERE id = :ventaId")
    suspend fun getVentaWithItems(ventaId: Int): VentaWithItems?
}
package com.example.appventas.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Update

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem)

    @Query("""
        SELECT 
            i.id as inventoryId, 
            i.productId,
            p.referenceCode,
            p.nombre as productName,
            i.quantity,
            i.status
        FROM inventory_items i
        JOIN productos p ON i.productId = p.id
        WHERE i.sellerId = :sellerId
    """)
    fun getInventoryForSeller(sellerId: Int): Flow<List<InventoryItemWithProductDetails>>

    @Update
    suspend fun update(item: InventoryItem)

    @Query("SELECT * FROM inventory_items WHERE sellerId = :sellerId AND status = 'Aceptado'")
    fun getAcceptedInventoryForSeller(sellerId: Int): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE sellerId = :sellerId AND productId = :productId AND status = 'Aceptado'")
    suspend fun findAcceptedItem(sellerId: Int, productId: Int): InventoryItem?
}
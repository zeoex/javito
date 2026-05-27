package com.javito.shoplist.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItem>)

    @Transaction
    @Query("SELECT * FROM purchases WHERE purchaseMonth = :month ORDER BY createdAt DESC")
    fun getPurchasesByMonth(month: String): Flow<List<PurchaseWithItems>>

    @Query("SELECT SUM(totalAmount) FROM purchases WHERE purchaseMonth = :month")
    fun getTotalByMonth(month: String): Flow<Double?>

    @Query("SELECT DISTINCT purchaseMonth FROM purchases ORDER BY createdAt DESC")
    fun getAvailableMonths(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM purchases ORDER BY createdAt DESC LIMIT 50")
    fun getRecentPurchases(): Flow<List<PurchaseWithItems>>

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long)
}

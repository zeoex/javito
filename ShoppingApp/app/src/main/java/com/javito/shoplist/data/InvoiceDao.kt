package com.javito.shoplist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Insert
    suspend fun insert(invoice: Invoice): Long

    @Delete
    suspend fun delete(invoice: Invoice)

    @Query("SELECT * FROM invoices WHERE purchaseMonth = :month ORDER BY createdAt DESC")
    fun getByMonth(month: String): Flow<List<Invoice>>

    @Query("SELECT SUM(totalAmount) FROM invoices WHERE purchaseMonth = :month")
    fun getTotalByMonth(month: String): Flow<Double?>

    @Query("SELECT DISTINCT purchaseMonth FROM invoices ORDER BY createdAt DESC")
    fun getAvailableMonths(): Flow<List<String>>
}

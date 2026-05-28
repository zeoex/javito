package com.javito.shoplist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GastoDao {

    @Insert
    suspend fun insert(gasto: Gasto): Long

    @Delete
    suspend fun delete(gasto: Gasto)

    @Query("SELECT * FROM gastos WHERE gastoMonth = :month ORDER BY createdAt DESC")
    fun getByMonth(month: String): Flow<List<Gasto>>

    @Query("SELECT SUM(amount) FROM gastos WHERE gastoMonth = :month")
    fun getTotalByMonth(month: String): Flow<Double?>

    @Query("SELECT DISTINCT gastoMonth FROM gastos ORDER BY createdAt DESC")
    fun getAvailableMonths(): Flow<List<String>>

    @Insert
    suspend fun insertAll(gastos: List<Gasto>)

    @Query("SELECT * FROM gastos ORDER BY createdAt DESC")
    suspend fun getAllGastosOnce(): List<Gasto>
}

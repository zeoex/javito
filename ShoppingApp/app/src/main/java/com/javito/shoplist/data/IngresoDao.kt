package com.javito.shoplist.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface IngresoDao {
    @Insert
    suspend fun insert(ingreso: Ingreso): Long

    @Delete
    suspend fun delete(ingreso: Ingreso)

    @Query("SELECT * FROM ingresos WHERE ingresoMonth = :month ORDER BY createdAt DESC")
    fun getByMonth(month: String): Flow<List<Ingreso>>

    @Query("SELECT SUM(amount) FROM ingresos WHERE ingresoMonth = :month")
    fun getTotalByMonth(month: String): Flow<Double?>
}

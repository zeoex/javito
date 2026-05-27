package com.javito.shoplist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingresos")
data class Ingreso(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val source: String,
    val amount: Double,
    val paymentMethod: String,
    val ingresoDate: String,
    val ingresoMonth: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

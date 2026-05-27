package com.javito.shoplist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val category: String,       // "Carnicería", "Despensa", "Verdulería", "Varios"
    val amount: Double,
    val paymentMethod: String,  // "Efectivo", "Digital"
    val gastoDate: String,      // "dd/MM/yyyy"
    val gastoMonth: String,     // "MM/yyyy"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

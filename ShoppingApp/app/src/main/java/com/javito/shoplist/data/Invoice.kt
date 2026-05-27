package com.javito.shoplist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeName: String,
    val receiptNumber: String = "",
    val purchaseDate: String,       // "dd/MM/yyyy"
    val purchaseMonth: String,      // "MM/yyyy"
    val totalAmount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

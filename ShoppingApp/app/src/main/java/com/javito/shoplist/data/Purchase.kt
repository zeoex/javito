package com.javito.shoplist.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val storeName: String,
    val purchaseDate: String,       // stored as "dd/MM/yyyy"
    val purchaseMonth: String,      // stored as "MM/yyyy" for filtering
    val totalAmount: Double,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

package com.javito.shoplist.data

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseWithItems(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItem>
)

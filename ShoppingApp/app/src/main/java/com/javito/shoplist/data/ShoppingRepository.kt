package com.javito.shoplist.data

import kotlinx.coroutines.flow.Flow

class ShoppingRepository(
    private val shoppingItemDao: ShoppingItemDao,
    private val purchaseDao: PurchaseDao
) {

    // ── Shopping list ──────────────────────────────────────────────────────────

    val allShoppingItems: Flow<List<ShoppingItem>> = shoppingItemDao.getAllItems()

    suspend fun insertShoppingItem(item: ShoppingItem) = shoppingItemDao.insert(item)

    suspend fun updateShoppingItem(item: ShoppingItem) = shoppingItemDao.update(item)

    suspend fun deleteShoppingItem(item: ShoppingItem) = shoppingItemDao.delete(item)

    suspend fun deleteCheckedItems() = shoppingItemDao.deleteCheckedItems()

    // ── Purchases ──────────────────────────────────────────────────────────────

    /**
     * Saves the purchase header and all line items in a single transaction.
     * The [purchaseId] is auto-generated and assigned to each item before insert.
     */
    suspend fun savePurchaseWithItems(purchase: Purchase, items: List<PurchaseItem>) {
        val newId = purchaseDao.insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = newId) }
        purchaseDao.insertItems(itemsWithId)
    }

    fun getPurchasesByMonth(month: String): Flow<List<PurchaseWithItems>> =
        purchaseDao.getPurchasesByMonth(month)

    fun getTotalByMonth(month: String): Flow<Double?> =
        purchaseDao.getTotalByMonth(month)

    fun getAvailableMonths(): Flow<List<String>> =
        purchaseDao.getAvailableMonths()

    suspend fun deletePurchase(id: Long) = purchaseDao.deletePurchaseById(id)
}

package com.javito.shoplist.ui.purchase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.Purchase
import com.javito.shoplist.data.PurchaseItem
import com.javito.shoplist.data.ShoppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PurchaseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShoppingRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = ShoppingRepository(db.shoppingItemDao(), db.purchaseDao())
    }

    /**
     * Persists the purchase header and its line items.
     * [onSuccess] is called on the main thread when the operation succeeds.
     */
    fun savePurchase(
        purchase: Purchase,
        items: List<PurchaseItem>,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.savePurchaseWithItems(purchase, items)
                // Switch to main for callback
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e) }
            }
        }
    }
}

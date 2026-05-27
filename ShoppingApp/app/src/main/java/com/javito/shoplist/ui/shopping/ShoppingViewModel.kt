package com.javito.shoplist.ui.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.ShoppingItem
import com.javito.shoplist.data.ShoppingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ShoppingRepository(db.shoppingItemDao(), db.purchaseDao())

    val allItems = repository.allShoppingItems.asLiveData()

    fun insertItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertShoppingItem(item)
        }
    }

    fun updateItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateShoppingItem(item)
        }
    }

    fun deleteItem(item: ShoppingItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteShoppingItem(item)
        }
    }

    fun deleteCheckedItems() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCheckedItems()
        }
    }
}

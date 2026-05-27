package com.javito.shoplist.ui.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.ShoppingItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())

    val allItems = repository.allShoppingItems.asLiveData()
    val templateItems = repository.templateItems.asLiveData()

    fun saveAsTemplate(items: List<ShoppingItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllTemplateItems()
            val tpl = items.map { it.copy(id = 0, isTemplate = true, isChecked = false) }
            repository.insertAllShoppingItems(tpl)
        }
    }

    fun loadTemplate(templateItems: List<ShoppingItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItems = templateItems.map {
                it.copy(id = 0, isTemplate = false, isChecked = false, createdAt = System.currentTimeMillis())
            }
            repository.insertAllShoppingItems(newItems)
        }
    }

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

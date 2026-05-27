package com.javito.shoplist.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.PurchaseWithItems
import com.javito.shoplist.data.ShoppingRepository
import java.util.Calendar

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ShoppingRepository

    /**
     * Currently selected month in "MM/yyyy" format, e.g. "05/2026".
     */
    private val _selectedMonth = MutableLiveData<String>()
    val selectedMonth: LiveData<String> get() = _selectedMonth

    /** Purchases belonging to [selectedMonth]. */
    val purchases: LiveData<List<PurchaseWithItems>>

    /** Sum of all totals for [selectedMonth]. Null when there are no purchases. */
    val monthTotal: LiveData<Double?>

    init {
        val db = AppDatabase.getInstance(application)
        repository = ShoppingRepository(db.shoppingItemDao(), db.purchaseDao())

        // Default to current month
        val cal = Calendar.getInstance()
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)
        _selectedMonth.value = "$month/$year"

        purchases = _selectedMonth.switchMap { month ->
            repository.getPurchasesByMonth(month).asLiveData()
        }

        monthTotal = _selectedMonth.switchMap { month ->
            repository.getTotalByMonth(month).asLiveData()
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
    }

    /**
     * Returns the list of the last [count] months as "MM/yyyy" strings,
     * starting from the current month and going backwards.
     */
    fun recentMonths(count: Int = 12): List<String> {
        val result = mutableListOf<String>()
        val cal = Calendar.getInstance()
        repeat(count) {
            val m = String.format("%02d", cal.get(Calendar.MONTH) + 1)
            val y = cal.get(Calendar.YEAR)
            result.add("$m/$y")
            cal.add(Calendar.MONTH, -1)
        }
        return result
    }
}

package com.javito.shoplist.ui.facturas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.Invoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class FacturasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    private val _currentMonth = MutableLiveData<String>()

    /** Live list of invoices for the current calendar month. */
    val invoices: LiveData<List<Invoice>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db.shoppingItemDao(), db.invoiceDao(), db.gastoDao())

        val cal = Calendar.getInstance()
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)
        _currentMonth.value = "$month/$year"

        invoices = _currentMonth.switchMap { month ->
            repository.getInvoicesByMonth(month).asLiveData()
        }
    }

    fun saveInvoice(
        invoice: Invoice,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveInvoice(invoice)
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e) }
            }
        }
    }

    fun deleteInvoice(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteInvoice(invoice)
        }
    }
}

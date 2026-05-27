package com.javito.shoplist.ui.estadisticas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.Gasto
import com.javito.shoplist.data.Invoice
import java.util.Calendar

class EstadisticasViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(db.shoppingItemDao(), db.invoiceDao(), db.gastoDao())

    private val _selectedMonth = MutableLiveData<String>()
    val selectedMonth: LiveData<String> get() = _selectedMonth

    val invoices: LiveData<List<Invoice>> = _selectedMonth.switchMap { m ->
        repository.getInvoicesByMonth(m).asLiveData()
    }

    val gastos: LiveData<List<Gasto>> = _selectedMonth.switchMap { m ->
        repository.getGastosByMonth(m).asLiveData()
    }

    val invoiceTotal: LiveData<Double?> = _selectedMonth.switchMap { m ->
        repository.getInvoiceTotalByMonth(m).asLiveData()
    }

    val gastoTotal: LiveData<Double?> = _selectedMonth.switchMap { m ->
        repository.getGastoTotalByMonth(m).asLiveData()
    }

    val grandTotal: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun recalc() { value = (invoiceTotal.value ?: 0.0) + (gastoTotal.value ?: 0.0) }
        addSource(invoiceTotal) { recalc() }
        addSource(gastoTotal) { recalc() }
    }

    val categoryBreakdown: LiveData<Map<String, Double>> = MediatorLiveData<Map<String, Double>>().apply {
        addSource(gastos) { list ->
            value = list.groupBy { it.category }.mapValues { (_, items) -> items.sumOf { it.amount } }
        }
    }

    val paymentBreakdown: LiveData<Map<String, Double>> = MediatorLiveData<Map<String, Double>>().apply {
        addSource(gastos) { list ->
            value = list.groupBy { it.paymentMethod }.mapValues { (_, items) -> items.sumOf { it.amount } }
        }
    }

    init {
        val cal = Calendar.getInstance()
        _selectedMonth.value = String.format("%02d/%04d",
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    fun selectMonth(month: String) { _selectedMonth.value = month }

    fun recentMonths(count: Int = 24): List<String> {
        val result = mutableListOf<String>()
        val cal = Calendar.getInstance()
        repeat(count) {
            result.add(String.format("%02d/%04d",
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)))
            cal.add(Calendar.MONTH, -1)
        }
        return result
    }
}

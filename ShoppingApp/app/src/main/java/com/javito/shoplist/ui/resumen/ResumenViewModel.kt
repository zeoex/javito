package com.javito.shoplist.ui.resumen

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import java.util.Calendar

class ResumenViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())

    private val _selectedMonth = MutableLiveData<String>()
    val selectedMonth: LiveData<String> get() = _selectedMonth

    val gastoTotal: LiveData<Double?> = _selectedMonth.switchMap { m ->
        repository.getGastoTotalByMonth(m).asLiveData()
    }

    val ingresoTotal: LiveData<Double?> = _selectedMonth.switchMap { m ->
        repository.getIngresoTotalByMonth(m).asLiveData()
    }

    val balance: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun recalc() { value = (ingresoTotal.value ?: 0.0) - (gastoTotal.value ?: 0.0) }
        addSource(ingresoTotal) { recalc() }
        addSource(gastoTotal) { recalc() }
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

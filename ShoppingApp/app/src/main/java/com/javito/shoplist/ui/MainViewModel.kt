package com.javito.shoplist.ui

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

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())

    private val _currentMonth = MutableLiveData<String>()

    val gastoTotal: LiveData<Double?> = _currentMonth.switchMap { m ->
        repository.getGastoTotalByMonth(m).asLiveData()
    }

    val ingresoTotal: LiveData<Double?> = _currentMonth.switchMap { m ->
        repository.getIngresoTotalByMonth(m).asLiveData()
    }

    val balance: LiveData<Double> = MediatorLiveData<Double>().apply {
        fun recalc() {
            value = (ingresoTotal.value ?: 0.0) - (gastoTotal.value ?: 0.0)
        }
        addSource(ingresoTotal) { recalc() }
        addSource(gastoTotal) { recalc() }
    }

    val monthLabel: LiveData<String> = MediatorLiveData<String>().apply {
        addSource(_currentMonth) { m ->
            val p = m.split("/")
            val n = p.getOrNull(0)?.toIntOrNull() ?: return@addSource
            val y = p.getOrNull(1) ?: return@addSource
            val names = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
            value = "${names.getOrElse(n - 1) { m }} $y"
        }
    }

    init {
        val cal = Calendar.getInstance()
        _currentMonth.value = String.format("%02d/%04d",
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }
}

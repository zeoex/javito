package com.javito.shoplist.ui.ingresos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.Ingreso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class IngresosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    private val _currentMonth = MutableLiveData<String>()

    val ingresos: LiveData<List<Ingreso>>
    val ingresoTotal: LiveData<Double?>

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())

        val cal = Calendar.getInstance()
        _currentMonth.value = String.format("%02d/%04d",
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))

        ingresos = _currentMonth.switchMap { month ->
            repository.getIngresosByMonth(month).asLiveData()
        }

        ingresoTotal = _currentMonth.switchMap { month ->
            repository.getIngresoTotalByMonth(month).asLiveData()
        }
    }

    fun saveIngreso(ingreso: Ingreso, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveIngreso(ingreso)
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e) }
            }
        }
    }

    fun deleteIngreso(ingreso: Ingreso) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIngreso(ingreso)
        }
    }
}

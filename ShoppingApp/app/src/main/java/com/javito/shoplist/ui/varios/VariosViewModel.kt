package com.javito.shoplist.ui.varios

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.Gasto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class VariosViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    private val _currentMonth = MutableLiveData<String>()

    /** Live list of gastos for the current calendar month. */
    val gastos: LiveData<List<Gasto>>

    init {
        val db = AppDatabase.getInstance(application)
        repository = AppRepository(db.shoppingItemDao(), db.invoiceDao(), db.gastoDao(), db.ingresoDao())

        val cal = Calendar.getInstance()
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)
        _currentMonth.value = "$month/$year"

        gastos = _currentMonth.switchMap { month ->
            repository.getGastosByMonth(month).asLiveData()
        }
    }

    fun saveGasto(
        gasto: Gasto,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveGasto(gasto)
                launch(Dispatchers.Main) { onSuccess() }
            } catch (e: Exception) {
                launch(Dispatchers.Main) { onError(e) }
            }
        }
    }

    fun deleteGasto(gasto: Gasto) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGasto(gasto)
        }
    }
}

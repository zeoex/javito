package com.javito.shoplist.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.javito.shoplist.R
import com.javito.shoplist.data.AppDatabase
import com.javito.shoplist.data.AppRepository
import com.javito.shoplist.data.BackupManager
import com.javito.shoplist.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { doExport(it) }
    }

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { doImport(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        observeBalance()
        setupBackupButton()
    }

    private fun observeBalance() {
        viewModel.monthLabel.observe(this) { label ->
            binding.tvBalanceMonth.text = "Saldo · $label"
        }
        viewModel.balance.observe(this) { bal ->
            binding.tvGlobalBalance.text = "$${String.format("%.2f", bal)}"
            val colorRes = if (bal >= 0) R.color.balance_positive else R.color.balance_negative
            binding.tvGlobalBalance.setTextColor(ContextCompat.getColor(this, colorRes))
        }
        viewModel.ingresoTotal.observe(this) { total ->
            binding.tvHeaderIngresos.text = "$${String.format("%.2f", total ?: 0.0)}"
        }
        viewModel.gastoTotal.observe(this) { gasto ->
            binding.tvHeaderGastos.text = "$${String.format("%.2f", gasto ?: 0.0)}"
        }
    }

    private fun setupBackupButton() {
        binding.btnBackup.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Respaldo de datos")
                .setMessage("¿Qué querés hacer?")
                .setPositiveButton("Exportar") { _, _ ->
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    createDocumentLauncher.launch("javito-backup-$date.json")
                }
                .setNeutralButton("Importar") { _, _ ->
                    openDocumentLauncher.launch(arrayOf("application/json", "*/*"))
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    private fun doExport(uri: Uri) {
        lifecycleScope.launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val repo = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())
                withContext(Dispatchers.IO) {
                    val items = repo.getAllItemsForBackup()
                    val templates = repo.getAllTemplateItemsForBackup()
                    val gastos = repo.getAllGastosForBackup()
                    val ingresos = repo.getAllIngresosForBackup()
                    BackupManager.export(applicationContext, uri, items, templates, gastos, ingresos)
                }
                Toast.makeText(this@MainActivity, "Backup exportado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doImport(uri: Uri) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Importar backup")
            .setMessage("Los datos del archivo se agregarán a los existentes. ¿Continuar?")
            .setPositiveButton("Importar") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val db = AppDatabase.getInstance(applicationContext)
                        val repo = AppRepository(db.shoppingItemDao(), db.gastoDao(), db.ingresoDao())
                        withContext(Dispatchers.IO) {
                            val data = BackupManager.import(applicationContext, uri)
                            if (data.shoppingItems.isNotEmpty()) repo.restoreShoppingItems(data.shoppingItems)
                            if (data.gastos.isNotEmpty()) repo.restoreGastos(data.gastos)
                            if (data.ingresos.isNotEmpty()) repo.restoreIngresos(data.ingresos)
                        }
                        Toast.makeText(this@MainActivity, "Datos importados correctamente", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "Error al importar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}

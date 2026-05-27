package com.javito.shoplist.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.javito.shoplist.R
import com.javito.shoplist.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        observeBalance()
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

        viewModel.gastoTotal.observe(this) { gastos ->
            val facturas = viewModel.invoiceTotal.value ?: 0.0
            binding.tvHeaderGastos.text = "$${String.format("%.2f", (gastos ?: 0.0) + facturas)}"
        }

        viewModel.invoiceTotal.observe(this) { facturas ->
            val gastos = viewModel.gastoTotal.value ?: 0.0
            binding.tvHeaderGastos.text = "$${String.format("%.2f", (facturas ?: 0.0) + gastos)}"
        }
    }

    override fun onSupportNavigateUp(): Boolean =
        navController.navigateUp() || super.onSupportNavigateUp()
}

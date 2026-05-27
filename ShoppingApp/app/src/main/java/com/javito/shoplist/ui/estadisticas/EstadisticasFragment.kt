package com.javito.shoplist.ui.estadisticas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.javito.shoplist.databinding.FragmentEstadisticasBinding
import com.javito.shoplist.ui.facturas.InvoiceAdapter
import com.javito.shoplist.ui.varios.GastoAdapter

class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EstadisticasViewModel by viewModels()
    private lateinit var invoiceAdapter: InvoiceAdapter
    private lateinit var gastoAdapter: GastoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadisticasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        setupMonthNavigation()
        observeData()
    }

    private fun setupRecyclers() {
        invoiceAdapter = InvoiceAdapter(onDeleteClick = {})
        binding.recyclerInvoicesStat.apply {
            adapter = invoiceAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        gastoAdapter = GastoAdapter(onDeleteClick = {})
        binding.recyclerGastosStat.apply {
            adapter = gastoAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupMonthNavigation() {
        val months = viewModel.recentMonths(24)
        var idx = 0

        fun update(i: Int) {
            viewModel.selectMonth(months[i])
            binding.tvMonthLabel.text = monthLabel(months[i])
            binding.btnPrevMonth.isEnabled = i < months.size - 1
            binding.btnNextMonth.isEnabled = i > 0
        }

        update(0)
        binding.btnPrevMonth.setOnClickListener { if (idx < months.size - 1) { idx++; update(idx) } }
        binding.btnNextMonth.setOnClickListener { if (idx > 0) { idx--; update(idx) } }
    }

    private fun observeData() {
        viewModel.grandTotal.observe(viewLifecycleOwner) {
            binding.tvGrandTotal.text = "$${fmt(it)}"
        }

        viewModel.invoiceTotal.observe(viewLifecycleOwner) {
            binding.tvFacturasTotalStat.text = "$${fmt(it ?: 0.0)}"
        }

        viewModel.invoices.observe(viewLifecycleOwner) { list ->
            invoiceAdapter.submitList(list)
            binding.tvFacturasCount.text = "${list.size} factura${if (list.size != 1) "s" else ""}"
        }

        viewModel.gastoTotal.observe(viewLifecycleOwner) {
            binding.tvVariosTotalStat.text = "$${fmt(it ?: 0.0)}"
        }

        viewModel.gastos.observe(viewLifecycleOwner) { list ->
            gastoAdapter.submitList(list)
        }

        viewModel.categoryBreakdown.observe(viewLifecycleOwner) { map ->
            binding.tvCatCarniceria.text = "$${fmt(map["Carnicería"] ?: 0.0)}"
            binding.tvCatDespensa.text = "$${fmt(map["Despensa"] ?: 0.0)}"
            binding.tvCatVerduleria.text = "$${fmt(map["Verdulería"] ?: 0.0)}"
            binding.tvCatVarios.text = "$${fmt(map["Varios"] ?: 0.0)}"
        }

        viewModel.paymentBreakdown.observe(viewLifecycleOwner) { map ->
            binding.tvEfectivoTotal.text = "$${fmt(map["Efectivo"] ?: 0.0)}"
            binding.tvDigitalTotal.text = "$${fmt(map["Digital"] ?: 0.0)}"
        }
    }

    private fun monthLabel(m: String): String {
        val p = m.split("/")
        val n = p.getOrNull(0)?.toIntOrNull() ?: return m
        val y = p.getOrNull(1) ?: return m
        val names = listOf("Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre")
        return "${names.getOrElse(n - 1) { m }} $y"
    }

    private fun fmt(v: Double) = String.format("%.2f", v)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

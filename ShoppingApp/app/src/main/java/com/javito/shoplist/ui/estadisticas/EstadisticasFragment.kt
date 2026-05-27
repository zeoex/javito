package com.javito.shoplist.ui.estadisticas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.javito.shoplist.databinding.FragmentEstadisticasBinding
import com.javito.shoplist.ui.ingresos.IngresoAdapter
import com.javito.shoplist.ui.varios.GastoAdapter

class EstadisticasFragment : Fragment() {

    private var _binding: FragmentEstadisticasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EstadisticasViewModel by viewModels()
    private lateinit var gastoAdapter: GastoAdapter
    private lateinit var ingresoAdapter: IngresoAdapter

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
        gastoAdapter = GastoAdapter(onDeleteClick = {})
        binding.recyclerGastosStat.apply {
            adapter = gastoAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        ingresoAdapter = IngresoAdapter(onDeleteClick = {})
        binding.recyclerIngresosStat.apply {
            adapter = ingresoAdapter
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
        viewModel.balance.observe(viewLifecycleOwner) {
            binding.tvGrandTotal.text = "$${fmt(it)}"
        }
        viewModel.gastoTotal.observe(viewLifecycleOwner) {
            binding.tvVariosTotalStat.text = "$${fmt(it ?: 0.0)}"
        }
        viewModel.gastos.observe(viewLifecycleOwner) { list ->
            gastoAdapter.submitList(list)
        }
        viewModel.ingresoTotal.observe(viewLifecycleOwner) {
            binding.tvIngresosTotalStat.text = "$${fmt(it ?: 0.0)}"
        }
        viewModel.ingresos.observe(viewLifecycleOwner) { list ->
            ingresoAdapter.submitList(list)
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
        viewModel.sourceBreakdown.observe(viewLifecycleOwner) { map ->
            binding.tvSrcSalario.text = "$${fmt(map["Salario"] ?: 0.0)}"
            binding.tvSrcFreelance.text = "$${fmt(map["Freelance"] ?: 0.0)}"
            binding.tvSrcOtros.text = "$${fmt((map["Venta"] ?: 0.0) + (map["Alquiler"] ?: 0.0) + (map["Otros"] ?: 0.0))}"
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

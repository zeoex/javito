package com.javito.shoplist.ui.resumen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.javito.shoplist.databinding.FragmentResumenBinding
import androidx.core.content.ContextCompat

class ResumenFragment : Fragment() {

    private var _binding: FragmentResumenBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResumenViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResumenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonthNavigation()
        observeData()
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
        viewModel.invoiceTotal.observe(viewLifecycleOwner) {
            binding.tvFacturasTotal.text = "$${fmt(it ?: 0.0)}"
        }
        viewModel.gastoTotal.observe(viewLifecycleOwner) {
            binding.tvVariosTotal.text = "$${fmt(it ?: 0.0)}"
        }
        viewModel.grandTotal.observe(viewLifecycleOwner) {
            binding.tvGrandTotal.text = "$${fmt(it)}"
        }
        viewModel.ingresoTotal.observe(viewLifecycleOwner) {
            binding.tvIngresosTotal.text = "$${fmt(it ?: 0.0)}"
        }
        viewModel.balance.observe(viewLifecycleOwner) { bal ->
            binding.tvBalance.text = "$${fmt(bal)}"
            val color = if (bal >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
            binding.tvBalance.setTextColor(ContextCompat.getColor(requireContext(), color))
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

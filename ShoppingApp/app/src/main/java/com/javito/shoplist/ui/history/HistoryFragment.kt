package com.javito.shoplist.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.javito.shoplist.databinding.FragmentHistoryBinding
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: PurchaseHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupMonthNavigation()
        observeViewModel()
    }

    // ── RecyclerView ────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = PurchaseHistoryAdapter()
        binding.recyclerPurchases.apply {
            this.adapter = this@HistoryFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ── Month navigation ────────────────────────────────────────────────────────

    private fun setupMonthNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            navigateMonth(direction = -1)
        }
        binding.btnNextMonth.setOnClickListener {
            navigateMonth(direction = +1)
        }
    }

    /**
     * Moves the selected month forward (+1) or backward (-1) from the currently
     * displayed month label.
     */
    private fun navigateMonth(direction: Int) {
        val current = viewModel.selectedMonth.value ?: return
        // current format "MM/yyyy"
        val parts = current.split("/")
        if (parts.size != 2) return
        val month = parts[0].toIntOrNull() ?: return
        val year = parts[1].toIntOrNull() ?: return

        val cal = Calendar.getInstance().apply {
            set(Calendar.MONTH, month - 1)
            set(Calendar.YEAR, year)
            add(Calendar.MONTH, direction)
        }

        val newMonth = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val newYear = cal.get(Calendar.YEAR)
        viewModel.selectMonth("$newMonth/$newYear")
    }

    // ── Observers ───────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        // Update month label when selection changes
        viewModel.selectedMonth.observe(viewLifecycleOwner) { monthYear ->
            binding.tvMonthLabel.text = formatMonthLabel(monthYear)
        }

        // Update purchases list
        viewModel.purchases.observe(viewLifecycleOwner) { purchases ->
            adapter.submitList(purchases)

            val isEmpty = purchases.isEmpty()
            binding.emptyHistory.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerPurchases.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // Update purchase count
            val count = purchases.size
            binding.tvPurchaseCount.text = when (count) {
                0 -> "Sin compras"
                1 -> "1 compra"
                else -> "$count compras"
            }
        }

        // Update monthly total
        viewModel.monthTotal.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = if (total != null && total > 0) {
                "$${String.format("%,.2f", total)}"
            } else {
                "$0,00"
            }
        }
    }

    /**
     * Converts "MM/yyyy" (e.g. "05/2026") to a human-readable label like "Mayo 2026".
     */
    private fun formatMonthLabel(monthYear: String): String {
        val parts = monthYear.split("/")
        if (parts.size != 2) return monthYear
        val month = parts[0].toIntOrNull() ?: return monthYear
        val year = parts[1]

        val monthNames = listOf(
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        )
        val name = if (month in 1..12) monthNames[month - 1] else monthYear
        return "$name $year"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

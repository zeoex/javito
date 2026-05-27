package com.javito.shoplist.ui.varios

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.javito.shoplist.data.Gasto
import com.javito.shoplist.databinding.DialogAddGastoBinding
import com.javito.shoplist.databinding.FragmentVariosBinding
import java.util.Calendar

class VariosFragment : Fragment() {

    private var _binding: FragmentVariosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VariosViewModel by viewModels()
    private lateinit var adapter: GastoAdapter

    private val categories = listOf("Carnicería", "Despensa", "Verdulería", "Cuidado Personal", "Viajes", "Varios")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVariosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = GastoAdapter { gasto -> showDeleteConfirmation(gasto) }
        binding.recyclerGastos.apply {
            this.adapter = this@VariosFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabAddGasto.setOnClickListener { showAddGastoDialog() }
    }

    private fun observeViewModel() {
        viewModel.gastos.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            val empty = list.isEmpty()
            binding.emptyGastos.visibility = if (empty) View.VISIBLE else View.GONE
            binding.recyclerGastos.visibility = if (empty) View.GONE else View.VISIBLE
        }
    }

    private fun showAddGastoDialog() {
        val db = DialogAddGastoBinding.inflate(layoutInflater)

        val catAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        db.acvCategory.setAdapter(catAdapter)
        db.acvCategory.setText(categories[0], false)

        val cal = Calendar.getInstance()
        db.etDate.setText(String.format("%02d/%02d/%04d",
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)))
        db.etDate.setOnClickListener { showDatePicker(db) }
        db.tilDate.setEndIconOnClickListener { showDatePicker(db) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo Gasto")
            .setView(db.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = db.etDescription.text.toString().trim()
                val category = db.acvCategory.text.toString().trim().ifBlank { categories[0] }
                val amountStr = db.etAmount.text.toString().trim()
                val dateStr = db.etDate.text.toString().trim()
                val notes = db.etNotes.text.toString().trim()
                val paymentMethod = if (db.chipEfectivo.isChecked) "Efectivo" else "Digital"

                var err = false
                if (description.isBlank()) { db.tilDescription.error = "Requerido"; err = true }
                else db.tilDescription.error = null
                if (amountStr.isBlank() || amountStr.toDoubleOrNull() == null) {
                    db.tilAmount.error = "Monto válido requerido"; err = true
                } else db.tilAmount.error = null
                if (err) return@setPositiveButton

                viewModel.saveGasto(
                    Gasto(
                        description = description,
                        category = category,
                        amount = amountStr.toDouble(),
                        paymentMethod = paymentMethod,
                        gastoDate = dateStr,
                        gastoMonth = parseMonth(dateStr),
                        notes = notes
                    ),
                    onSuccess = {
                        Snackbar.make(binding.root, "Gasto guardado", Snackbar.LENGTH_SHORT).show()
                    },
                    onError = { e ->
                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePicker(db: DialogAddGastoBinding) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            db.etDate.setText(String.format("%02d/%02d/%04d", d, m + 1, y))
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun parseMonth(date: String): String {
        val p = date.split("/")
        return if (p.size == 3) "${p[1]}/${p[2]}"
        else {
            val c = Calendar.getInstance()
            String.format("%02d/%04d", c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR))
        }
    }

    private fun showDeleteConfirmation(gasto: Gasto) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar gasto")
            .setMessage("¿Eliminar \"${gasto.description}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteGasto(gasto)
                Snackbar.make(binding.root, "Gasto eliminado", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

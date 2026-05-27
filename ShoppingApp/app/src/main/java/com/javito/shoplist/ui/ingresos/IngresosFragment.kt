package com.javito.shoplist.ui.ingresos

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
import com.javito.shoplist.data.Ingreso
import com.javito.shoplist.databinding.DialogAddIngresoBinding
import com.javito.shoplist.databinding.FragmentIngresosBinding
import java.util.Calendar

class IngresosFragment : Fragment() {

    private var _binding: FragmentIngresosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IngresosViewModel by viewModels()
    private lateinit var adapter: IngresoAdapter

    private val sources = listOf("Salario", "Freelance", "Venta", "Alquiler", "Otros")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIngresosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = IngresoAdapter { ingreso -> showDeleteConfirmation(ingreso) }
        binding.recyclerIngresos.apply {
            this.adapter = this@IngresosFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabAddIngreso.setOnClickListener { showAddIngresoDialog() }
    }

    private fun observeViewModel() {
        viewModel.ingresos.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.emptyIngresos.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.ingresoTotal.observe(viewLifecycleOwner) { total ->
            binding.tvIngresoTotal.text = "$${String.format("%.2f", total ?: 0.0)}"
        }

        viewModel.efectivoTotal.observe(viewLifecycleOwner) { total ->
            binding.tvEfectivoTotal.text = "$${String.format("%.2f", total)}"
        }

        viewModel.digitalTotal.observe(viewLifecycleOwner) { total ->
            binding.tvDigitalTotal.text = "$${String.format("%.2f", total)}"
        }
    }

    private fun showAddIngresoDialog() {
        val db = DialogAddIngresoBinding.inflate(layoutInflater)

        val srcAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, sources)
        db.acvSource.setAdapter(srcAdapter)
        db.acvSource.setText(sources[0], false)

        val cal = Calendar.getInstance()
        db.etDate.setText(String.format("%02d/%02d/%04d",
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)))
        db.etDate.setOnClickListener { showDatePicker(db) }
        db.tilDate.setEndIconOnClickListener { showDatePicker(db) }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo Ingreso")
            .setView(db.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val description = db.etDescription.text.toString().trim()
                val source = db.acvSource.text.toString().trim().ifBlank { sources[0] }
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

                viewModel.saveIngreso(
                    Ingreso(
                        description = description,
                        source = source,
                        amount = amountStr.toDouble(),
                        paymentMethod = paymentMethod,
                        ingresoDate = dateStr,
                        ingresoMonth = parseMonth(dateStr),
                        notes = notes
                    ),
                    onSuccess = {
                        Snackbar.make(binding.root, "Ingreso guardado", Snackbar.LENGTH_SHORT).show()
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

    private fun showDatePicker(db: DialogAddIngresoBinding) {
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

    private fun showDeleteConfirmation(ingreso: Ingreso) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar ingreso")
            .setMessage("¿Eliminar \"${ingreso.description}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteIngreso(ingreso)
                Snackbar.make(binding.root, "Ingreso eliminado", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

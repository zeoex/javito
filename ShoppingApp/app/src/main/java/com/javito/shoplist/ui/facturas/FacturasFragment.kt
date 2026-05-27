package com.javito.shoplist.ui.facturas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.javito.shoplist.data.Invoice
import com.javito.shoplist.databinding.DialogAddInvoiceBinding
import com.javito.shoplist.databinding.FragmentFacturasBinding
import java.util.Calendar

class FacturasFragment : Fragment() {

    private var _binding: FragmentFacturasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FacturasViewModel by viewModels()
    private lateinit var adapter: InvoiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFacturasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    // ── RecyclerView ────────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        adapter = InvoiceAdapter { invoice ->
            showDeleteConfirmation(invoice)
        }
        binding.recyclerInvoices.apply {
            this.adapter = this@FacturasFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    // ── FAB ─────────────────────────────────────────────────────────────────────

    private fun setupFab() {
        binding.fabAddInvoice.setOnClickListener {
            showAddInvoiceDialog()
        }
    }

    // ── Observers ───────────────────────────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.invoices.observe(viewLifecycleOwner) { invoices ->
            adapter.submitList(invoices)
            val isEmpty = invoices.isEmpty()
            binding.emptyInvoices.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.recyclerInvoices.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    // ── Add Invoice Dialog ───────────────────────────────────────────────────────

    private fun showAddInvoiceDialog() {
        val dialogBinding = DialogAddInvoiceBinding.inflate(layoutInflater)

        // DatePickerDialog on etDate click
        dialogBinding.etDate.setOnClickListener {
            showDatePicker { day, month, year ->
                dialogBinding.etDate.setText(String.format("%02d/%02d/%04d", day, month, year))
            }
        }
        dialogBinding.etDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showDatePicker { day, month, year ->
                    dialogBinding.etDate.setText(String.format("%02d/%02d/%04d", day, month, year))
                }
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva Factura")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { dialog, _ ->
                val storeName = dialogBinding.etStoreName.text.toString().trim()
                val receiptNumber = dialogBinding.etReceiptNumber.text.toString().trim()
                val dateStr = dialogBinding.etDate.text.toString().trim()
                val amountStr = dialogBinding.etTotalAmount.text.toString().trim()
                val notes = dialogBinding.etNotes.text.toString().trim()

                // Validate required fields
                var hasError = false

                if (storeName.isBlank()) {
                    dialogBinding.tilStoreName.error = "Ingresa el nombre del comercio"
                    hasError = true
                } else {
                    dialogBinding.tilStoreName.error = null
                }

                if (dateStr.isBlank()) {
                    dialogBinding.tilDate.error = "Ingresa la fecha"
                    hasError = true
                } else {
                    dialogBinding.tilDate.error = null
                }

                if (amountStr.isBlank()) {
                    dialogBinding.tilTotalAmount.error = "Ingresa el monto"
                    hasError = true
                } else {
                    dialogBinding.tilTotalAmount.error = null
                }

                if (hasError) return@setPositiveButton

                val totalAmount = amountStr.toDoubleOrNull()
                if (totalAmount == null) {
                    dialogBinding.tilTotalAmount.error = "Monto inválido"
                    return@setPositiveButton
                }

                // Parse "dd/MM/yyyy" → purchaseMonth "MM/yyyy"
                val purchaseMonth = parsePurchaseMonth(dateStr)

                val invoice = Invoice(
                    storeName = storeName,
                    receiptNumber = receiptNumber,
                    purchaseDate = dateStr,
                    purchaseMonth = purchaseMonth,
                    totalAmount = totalAmount,
                    notes = notes
                )

                viewModel.saveInvoice(
                    invoice,
                    onSuccess = {
                        Snackbar.make(binding.root, "Factura guardada", Snackbar.LENGTH_SHORT).show()
                    },
                    onError = { e ->
                        Snackbar.make(
                            binding.root,
                            "Error al guardar: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (day: Int, month: Int, year: Int) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // month from DatePickerDialog is 0-based
                onDateSelected(day, month + 1, year)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /** Converts "dd/MM/yyyy" → "MM/yyyy". Falls back to current month on parse failure. */
    private fun parsePurchaseMonth(dateStr: String): String {
        val parts = dateStr.split("/")
        return if (parts.size == 3) {
            "${parts[1]}/${parts[2]}"
        } else {
            val cal = Calendar.getInstance()
            String.format("%02d/%04d", cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
        }
    }

    // ── Delete Confirmation ──────────────────────────────────────────────────────

    private fun showDeleteConfirmation(invoice: Invoice) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar factura")
            .setMessage("¿Eliminar la factura de \"${invoice.storeName}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteInvoice(invoice)
                Snackbar.make(binding.root, "Factura eliminada", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

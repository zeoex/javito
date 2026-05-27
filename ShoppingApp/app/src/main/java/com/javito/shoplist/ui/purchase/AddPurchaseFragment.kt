package com.javito.shoplist.ui.purchase

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.javito.shoplist.data.Purchase
import com.javito.shoplist.data.PurchaseItem
import com.javito.shoplist.databinding.FragmentAddPurchaseBinding
import java.util.Calendar

class AddPurchaseFragment : Fragment() {

    private var _binding: FragmentAddPurchaseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PurchaseViewModel by viewModels()

    private val pendingItems = mutableListOf<PurchaseItem>()
    private lateinit var itemAdapter: PurchaseItemAdapter

    private val units = listOf("", "kg", "g", "L", "mL", "unid.", "pack", "caja", "bolsa")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDatePicker()
        setupUnitDropdown()
        setupItemsRecyclerView()
        setupAddItemButton()
        setupSaveButton()
    }

    // ── Date picker ─────────────────────────────────────────────────────────

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        updateDateField(calendar)

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.tilDate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val picked = Calendar.getInstance().apply { set(year, month, day) }
                updateDateField(picked)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateField(cal: Calendar) {
        val day = String.format("%02d", cal.get(Calendar.DAY_OF_MONTH))
        val month = String.format("%02d", cal.get(Calendar.MONTH) + 1)
        val year = cal.get(Calendar.YEAR)
        binding.etDate.setText("$day/$month/$year")
    }

    // ── Unit dropdown ────────────────────────────────────────────────────────

    private fun setupUnitDropdown() {
        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units
        )
        binding.acvItemUnit.setAdapter(unitAdapter)
    }

    // ── Pending items list ──────────────────────────────────────────────────

    private fun setupItemsRecyclerView() {
        itemAdapter = PurchaseItemAdapter { item ->
            pendingItems.remove(item)
            refreshAdapter()
        }
        binding.recyclerItems.apply {
            adapter = itemAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun refreshAdapter() {
        itemAdapter.submitList(pendingItems.toList())
    }

    // ── Add item ─────────────────────────────────────────────────────────────

    private fun setupAddItemButton() {
        binding.btnAddItem.setOnClickListener {
            addItemFromInlineForm()
        }
    }

    private fun addItemFromInlineForm() {
        val name = binding.etItemName.text.toString().trim()
        val quantityStr = binding.etItemQty.text.toString().trim()
        val unit = binding.acvItemUnit.text.toString().trim()
        val priceStr = binding.etItemPrice.text.toString().trim()

        var valid = true
        if (name.isBlank()) {
            binding.tilItemName.error = "Requerido"
            valid = false
        } else {
            binding.tilItemName.error = null
        }
        if (priceStr.isBlank() || priceStr.toDoubleOrNull() == null) {
            binding.tilItemPrice.error = "Precio válido requerido"
            valid = false
        } else {
            binding.tilItemPrice.error = null
        }
        if (!valid) return

        val quantity = if (quantityStr.isBlank()) "1" else quantityStr
        val price = priceStr.toDouble()

        pendingItems.add(
            PurchaseItem(
                purchaseId = 0,
                name = name,
                quantity = quantity,
                unit = unit,
                price = price
            )
        )
        refreshAdapter()

        // Clear inline form fields
        binding.etItemName.text?.clear()
        binding.etItemQty.text?.clear()
        binding.acvItemUnit.setText("", false)
        binding.etItemPrice.text?.clear()
        binding.etItemName.requestFocus()
    }

    // ── Save purchase ───────────────────────────────────────────────────────

    private fun setupSaveButton() {
        binding.btnSavePurchase.setOnClickListener {
            if (!validateForm()) return@setOnClickListener

            val dateText = binding.etDate.text.toString().trim()
            // purchaseMonth is "MM/yyyy" derived from "dd/MM/yyyy"
            val purchaseMonth = dateText.substring(3) // "MM/yyyy"

            val purchase = Purchase(
                receiptNumber = binding.etReceiptNumber.text.toString().trim(),
                storeName = binding.etStoreName.text.toString().trim(),
                purchaseDate = dateText,
                purchaseMonth = purchaseMonth,
                totalAmount = binding.etTotalAmount.text.toString().trim().toDouble(),
                notes = binding.etNotes.text.toString().trim()
            )

            binding.btnSavePurchase.isEnabled = false

            viewModel.savePurchase(
                purchase = purchase,
                items = pendingItems.toList(),
                onSuccess = {
                    Snackbar.make(binding.root, "Compra guardada", Snackbar.LENGTH_SHORT).show()
                    clearForm()
                    binding.btnSavePurchase.isEnabled = true
                },
                onError = { e ->
                    Snackbar.make(
                        binding.root,
                        "Error al guardar: ${e.message}",
                        Snackbar.LENGTH_LONG
                    ).show()
                    binding.btnSavePurchase.isEnabled = true
                }
            )
        }
    }

    private fun validateForm(): Boolean {
        var valid = true

        if (binding.etReceiptNumber.text.toString().isBlank()) {
            binding.tilReceiptNumber.error = "Requerido"
            valid = false
        } else {
            binding.tilReceiptNumber.error = null
        }

        if (binding.etStoreName.text.toString().isBlank()) {
            binding.tilStoreName.error = "Requerido"
            valid = false
        } else {
            binding.tilStoreName.error = null
        }

        if (binding.etDate.text.toString().isBlank()) {
            binding.tilDate.error = "Requerido"
            valid = false
        } else {
            binding.tilDate.error = null
        }

        val totalStr = binding.etTotalAmount.text.toString().trim()
        if (totalStr.isBlank() || totalStr.toDoubleOrNull() == null) {
            binding.tilTotalAmount.error = "Monto válido requerido"
            valid = false
        } else {
            binding.tilTotalAmount.error = null
        }

        return valid
    }

    private fun clearForm() {
        binding.etReceiptNumber.text?.clear()
        binding.etStoreName.text?.clear()
        updateDateField(Calendar.getInstance())
        binding.etTotalAmount.text?.clear()
        binding.etNotes.text?.clear()
        pendingItems.clear()
        refreshAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

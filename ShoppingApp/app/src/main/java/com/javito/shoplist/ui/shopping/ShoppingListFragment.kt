package com.javito.shoplist.ui.shopping

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
import com.javito.shoplist.R
import com.javito.shoplist.data.ShoppingItem
import com.javito.shoplist.databinding.DialogAddItemBinding
import com.javito.shoplist.databinding.FragmentShoppingListBinding

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingViewModel by viewModels()
    private lateinit var adapter: ShoppingAdapter

    private val units = listOf("", "kg", "g", "L", "mL", "unid.", "pack", "caja", "bolsa")

    private val categories = listOf(
        "General",
        "Frutas y Verduras",
        "Lácteos",
        "Carnes",
        "Limpieza",
        "Bebidas",
        "Panadería",
        "Otros"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        observeItems()
        setupToolbarMenu()
    }

    private fun setupRecyclerView() {
        adapter = ShoppingAdapter(
            onCheckedChange = { item, isChecked ->
                viewModel.updateItem(item.copy(isChecked = isChecked))
            },
            onDeleteClick = { item ->
                showDeleteConfirmation(item)
            }
        )
        binding.recyclerShopping.apply {
            this.adapter = this@ShoppingListFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupFab() {
        binding.fabAdd.setOnClickListener {
            showAddItemDialog()
        }
    }

    private fun observeItems() {
        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.templateItems.observe(viewLifecycleOwner) { /* keep live */ }
    }

    private fun setupToolbarMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_shopping)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save_template -> {
                    val current = viewModel.allItems.value ?: emptyList()
                    if (current.isEmpty()) {
                        Snackbar.make(binding.root, "La lista está vacía", Snackbar.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveAsTemplate(current)
                        Snackbar.make(binding.root, "Plantilla guardada (${current.size} productos)", Snackbar.LENGTH_SHORT).show()
                    }
                    true
                }
                R.id.action_load_template -> {
                    val template = viewModel.templateItems.value ?: emptyList()
                    if (template.isEmpty()) {
                        Snackbar.make(binding.root, "No hay plantilla guardada aún", Snackbar.LENGTH_SHORT).show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Cargar plantilla")
                            .setMessage("Se agregarán ${template.size} productos a la lista actual. ¿Continuar?")
                            .setPositiveButton("Cargar") { _, _ ->
                                viewModel.loadTemplate(template)
                                Snackbar.make(binding.root, "Plantilla cargada", Snackbar.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Cancelar", null)
                            .show()
                    }
                    true
                }
                R.id.action_delete_checked -> {
                    viewModel.deleteCheckedItems()
                    Snackbar.make(binding.root, "Tachados eliminados", Snackbar.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun showAddItemDialog() {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)

        // Setup unit dropdown
        val unitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            units
        )
        dialogBinding.acvUnit.setAdapter(unitAdapter)

        // Setup category dropdown
        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categories
        )
        dialogBinding.acvCategory.setAdapter(categoryAdapter)
        dialogBinding.acvCategory.setText(categories[0], false)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Agregar producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Agregar") { dialog, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                val quantityStr = dialogBinding.etQuantity.text.toString().trim()
                val unit = dialogBinding.acvUnit.text.toString().trim()
                val category = dialogBinding.acvCategory.text.toString().trim()
                    .ifBlank { categories[0] }

                if (name.isBlank()) {
                    dialogBinding.tilName.error = "Ingresa un nombre"
                    return@setPositiveButton
                }

                val quantity = quantityStr.toDoubleOrNull() ?: 1.0

                viewModel.insertItem(
                    ShoppingItem(
                        name = name,
                        quantity = quantity,
                        unit = unit,
                        category = category
                    )
                )
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmation(item: ShoppingItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Eliminar \"${item.name}\" de la lista?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.deleteItem(item)
                Snackbar.make(binding.root, "\"${item.name}\" eliminado", Snackbar.LENGTH_SHORT)
                    .show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

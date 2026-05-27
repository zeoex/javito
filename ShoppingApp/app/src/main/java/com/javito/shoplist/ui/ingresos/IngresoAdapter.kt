package com.javito.shoplist.ui.ingresos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.Ingreso
import com.javito.shoplist.databinding.ItemIngresoBinding

class IngresoAdapter(
    private val onDeleteClick: (Ingreso) -> Unit
) : ListAdapter<Ingreso, IngresoAdapter.IngresoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngresoViewHolder {
        val binding = ItemIngresoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return IngresoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngresoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IngresoViewHolder(
        private val binding: ItemIngresoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingreso: Ingreso) {
            binding.apply {
                tvDescription.text = ingreso.description
                tvSource.text = ingreso.source
                tvPaymentMethod.text = ingreso.paymentMethod
                tvAmount.text = "$${String.format("%.2f", ingreso.amount)}"
                tvDate.text = ingreso.ingresoDate

                root.setOnLongClickListener {
                    onDeleteClick(ingreso)
                    true
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Ingreso>() {
            override fun areItemsTheSame(old: Ingreso, new: Ingreso) = old.id == new.id
            override fun areContentsTheSame(old: Ingreso, new: Ingreso) = old == new
        }
    }
}

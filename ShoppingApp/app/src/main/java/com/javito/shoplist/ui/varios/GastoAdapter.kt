package com.javito.shoplist.ui.varios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.Gasto
import com.javito.shoplist.databinding.ItemGastoBinding

class GastoAdapter(
    private val onDeleteClick: (Gasto) -> Unit
) : ListAdapter<Gasto, GastoAdapter.GastoViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GastoViewHolder {
        val binding = ItemGastoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GastoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GastoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GastoViewHolder(
        private val binding: ItemGastoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(gasto: Gasto) {
            binding.apply {
                tvDescription.text = gasto.description
                tvCategory.text = gasto.category
                tvAmount.text = "$${String.format("%.2f", gasto.amount)}"
                tvPaymentMethod.text = gasto.paymentMethod
                tvDate.text = gasto.gastoDate

                root.setOnLongClickListener {
                    onDeleteClick(gasto)
                    true
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Gasto>() {
            override fun areItemsTheSame(old: Gasto, new: Gasto) = old.id == new.id
            override fun areContentsTheSame(old: Gasto, new: Gasto) = old == new
        }
    }
}

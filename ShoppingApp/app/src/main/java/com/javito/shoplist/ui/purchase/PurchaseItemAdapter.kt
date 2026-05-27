package com.javito.shoplist.ui.purchase

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.PurchaseItem
import com.javito.shoplist.databinding.ItemPurchaseItemBinding

class PurchaseItemAdapter(
    private val onDeleteClick: (PurchaseItem) -> Unit
) : ListAdapter<PurchaseItem, PurchaseItemAdapter.PurchaseItemViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseItemViewHolder {
        val binding = ItemPurchaseItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PurchaseItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseItemViewHolder(
        private val binding: ItemPurchaseItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PurchaseItem) {
            binding.apply {
                tvItemName.text = item.name
                tvItemQtyUnit.text = buildString {
                    append(item.quantity)
                    if (item.unit.isNotBlank()) append(" ${item.unit}")
                }
                tvItemPrice.text = "$${String.format("%.2f", item.price)}"
                btnDeleteItem.setOnClickListener { onDeleteClick(item) }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PurchaseItem>() {
            override fun areItemsTheSame(old: PurchaseItem, new: PurchaseItem) =
                old.id == new.id && old.name == new.name

            override fun areContentsTheSame(old: PurchaseItem, new: PurchaseItem) =
                old == new
        }
    }
}

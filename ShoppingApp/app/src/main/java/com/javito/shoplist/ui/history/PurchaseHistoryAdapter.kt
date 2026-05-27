package com.javito.shoplist.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.PurchaseItem
import com.javito.shoplist.data.PurchaseWithItems
import com.javito.shoplist.databinding.ItemPurchaseBinding
import com.javito.shoplist.databinding.ItemPurchaseItemBinding

class PurchaseHistoryAdapter :
    ListAdapter<PurchaseWithItems, PurchaseHistoryAdapter.PurchaseHistoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseHistoryViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PurchaseHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseHistoryViewHolder(
        private val binding: ItemPurchaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var expanded = false

        fun bind(purchaseWithItems: PurchaseWithItems) {
            val purchase = purchaseWithItems.purchase
            binding.apply {
                tvStoreName.text = purchase.storeName
                tvTotalAmount.text = "$${String.format("%.2f", purchase.totalAmount)}"
                tvReceiptNumber.text = "Comprobante #${purchase.receiptNumber}"
                tvDate.text = purchase.purchaseDate

                // Setup nested items RecyclerView
                val itemsAdapter = PurchaseItemsReadOnlyAdapter()
                recyclerPurchaseItemsDetail.apply {
                    adapter = itemsAdapter
                    layoutManager = LinearLayoutManager(binding.root.context)
                    isNestedScrollingEnabled = false
                }
                itemsAdapter.submitList(purchaseWithItems.items)

                // Reset expansion state on rebind
                expanded = false
                layoutItemsExpand.visibility = View.GONE
                btnExpand.text = "Ver artículos (${purchaseWithItems.items.size})"

                btnExpand.setOnClickListener {
                    expanded = !expanded
                    layoutItemsExpand.visibility = if (expanded) View.VISIBLE else View.GONE
                    btnExpand.text = if (expanded) {
                        "Ocultar artículos"
                    } else {
                        "Ver artículos (${purchaseWithItems.items.size})"
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PurchaseWithItems>() {
            override fun areItemsTheSame(old: PurchaseWithItems, new: PurchaseWithItems) =
                old.purchase.id == new.purchase.id

            override fun areContentsTheSame(old: PurchaseWithItems, new: PurchaseWithItems) =
                old == new
        }
    }
}

// Read-only adapter for items shown inside a purchase history card
class PurchaseItemsReadOnlyAdapter :
    ListAdapter<PurchaseItem, PurchaseItemsReadOnlyAdapter.InlineItemViewHolder>(ITEM_DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InlineItemViewHolder {
        val binding = ItemPurchaseItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InlineItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InlineItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InlineItemViewHolder(
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
                // Hide delete button in read-only context
                btnDeleteItem.visibility = View.GONE
            }
        }
    }

    companion object {
        private val ITEM_DIFF = object : DiffUtil.ItemCallback<PurchaseItem>() {
            override fun areItemsTheSame(old: PurchaseItem, new: PurchaseItem) =
                old.id == new.id

            override fun areContentsTheSame(old: PurchaseItem, new: PurchaseItem) =
                old == new
        }
    }
}

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

class PurchaseAdapter :
    ListAdapter<PurchaseWithItems, PurchaseAdapter.PurchaseViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PurchaseViewHolder {
        val binding = ItemPurchaseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PurchaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PurchaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PurchaseViewHolder(
        private val binding: ItemPurchaseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        /** Whether the detail items section is currently visible. */
        private var expanded = false

        fun bind(purchaseWithItems: PurchaseWithItems) {
            val purchase = purchaseWithItems.purchase
            val items = purchaseWithItems.items

            // Reset expansion state on recycle
            expanded = false
            binding.layoutItemsExpand.visibility = View.GONE
            binding.btnExpand.text = "Ver artículos"

            binding.tvStoreName.text = purchase.storeName
            binding.tvTotalAmount.text = "$${String.format("%,.2f", purchase.totalAmount)}"
            binding.tvReceiptNumber.text = "Comprobante #${purchase.receiptNumber}"
            binding.tvDate.text = purchase.purchaseDate

            // Setup nested RecyclerView for purchase items
            val itemAdapter = PurchaseItemDetailAdapter(items)
            binding.recyclerPurchaseItemsDetail.apply {
                adapter = itemAdapter
                layoutManager = LinearLayoutManager(context)
                isNestedScrollingEnabled = false
            }

            // Expand / collapse
            val toggleExpand = {
                expanded = !expanded
                binding.layoutItemsExpand.visibility =
                    if (expanded) View.VISIBLE else View.GONE
                binding.btnExpand.text =
                    if (expanded) "Ocultar artículos" else "Ver artículos"
            }

            binding.btnExpand.setOnClickListener { toggleExpand() }
            binding.root.setOnClickListener { if (items.isNotEmpty()) toggleExpand() }
        }
    }

    // ── Inner adapter for the purchase-item detail rows ─────────────────────

    private class PurchaseItemDetailAdapter(
        private val items: List<PurchaseItem>
    ) : RecyclerView.Adapter<PurchaseItemDetailAdapter.DetailViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
            val binding = ItemPurchaseItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return DetailViewHolder(binding)
        }

        override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        inner class DetailViewHolder(
            private val binding: ItemPurchaseItemBinding
        ) : RecyclerView.ViewHolder(binding.root) {

            fun bind(item: PurchaseItem) {
                binding.tvItemName.text = item.name
                binding.tvItemQtyUnit.text = buildString {
                    append(item.quantity)
                    if (item.unit.isNotBlank()) append(" ${item.unit}")
                }
                binding.tvItemPrice.text = "$${String.format("%.2f", item.price)}"
                // In the detail view, the delete button is hidden
                binding.btnDeleteItem.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PurchaseWithItems>() {
            override fun areItemsTheSame(
                old: PurchaseWithItems,
                new: PurchaseWithItems
            ) = old.purchase.id == new.purchase.id

            override fun areContentsTheSame(
                old: PurchaseWithItems,
                new: PurchaseWithItems
            ) = old == new
        }
    }
}

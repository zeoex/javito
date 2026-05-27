package com.javito.shoplist.ui.facturas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.Invoice
import com.javito.shoplist.databinding.ItemInvoiceBinding

class InvoiceAdapter(
    private val onDeleteClick: (Invoice) -> Unit
) : ListAdapter<Invoice, InvoiceAdapter.InvoiceViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemInvoiceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InvoiceViewHolder(
        private val binding: ItemInvoiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(invoice: Invoice) {
            binding.apply {
                tvStoreName.text = invoice.storeName
                tvDate.text = invoice.purchaseDate
                tvAmount.text = "$${String.format("%.2f", invoice.totalAmount)}"

                if (invoice.receiptNumber.isNotBlank()) {
                    tvReceiptNumber.text = "Comp. #${invoice.receiptNumber}"
                    tvReceiptNumber.visibility = View.VISIBLE
                } else {
                    tvReceiptNumber.visibility = View.GONE
                }

                root.setOnLongClickListener {
                    onDeleteClick(invoice)
                    true
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Invoice>() {
            override fun areItemsTheSame(old: Invoice, new: Invoice) = old.id == new.id
            override fun areContentsTheSame(old: Invoice, new: Invoice) = old == new
        }
    }
}

package com.javito.shoplist.ui.shopping

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.javito.shoplist.data.ShoppingItem
import com.javito.shoplist.databinding.ItemShoppingBinding

class ShoppingAdapter(
    private val onCheckedChange: (ShoppingItem, Boolean) -> Unit,
    private val onDeleteClick: (ShoppingItem) -> Unit
) : ListAdapter<ShoppingItem, ShoppingAdapter.ShoppingViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val binding = ItemShoppingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ShoppingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ShoppingViewHolder(
        private val binding: ItemShoppingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShoppingItem) {
            binding.apply {
                // Detach listener before changing state to avoid feedback loops
                checkboxItem.setOnCheckedChangeListener(null)
                checkboxItem.isChecked = item.isChecked

                tvName.text = item.name
                tvQuantity.text = buildString {
                    append(
                        if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString()
                        else item.quantity.toString()
                    )
                    if (item.unit.isNotBlank()) append(" ${item.unit}")
                }
                chipCategory.text = item.category.ifBlank { "General" }

                // Strike-through when checked
                val strikeFlag = Paint.STRIKE_THRU_TEXT_FLAG
                if (item.isChecked) {
                    tvName.paintFlags = tvName.paintFlags or strikeFlag
                    tvQuantity.paintFlags = tvQuantity.paintFlags or strikeFlag
                } else {
                    tvName.paintFlags = tvName.paintFlags and strikeFlag.inv()
                    tvQuantity.paintFlags = tvQuantity.paintFlags and strikeFlag.inv()
                }

                checkboxItem.setOnCheckedChangeListener { _, isChecked ->
                    onCheckedChange(item, isChecked)
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(item)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ShoppingItem>() {
            override fun areItemsTheSame(old: ShoppingItem, new: ShoppingItem) =
                old.id == new.id

            override fun areContentsTheSame(old: ShoppingItem, new: ShoppingItem) =
                old == new
        }
    }
}

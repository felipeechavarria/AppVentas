package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.DebtDetail

class DebtDetailAdapter(
    private val onLiquidateClicked: (DebtDetail) -> Unit
) : ListAdapter<DebtDetail, DebtDetailAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_debt_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onLiquidateClicked)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productName: TextView = itemView.findViewById(R.id.tvProductName)
        private val productDetails: TextView = itemView.findViewById(R.id.tvProductDetails)
        private val btnLiquidate: Button = itemView.findViewById(R.id.btnLiquidate)

        fun bind(item: DebtDetail, onLiquidateClicked: (DebtDetail) -> Unit) {
            productName.text = item.productName
            productDetails.text = "Cant: ${item.quantity} | Deuda: $${"%.2f".format(item.debtAmount)}"
            btnLiquidate.setOnClickListener { onLiquidateClicked(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DebtDetail>() {
        override fun areItemsTheSame(oldItem: DebtDetail, newItem: DebtDetail): Boolean {
            return oldItem.inventoryItemId == newItem.inventoryItemId
        }
        override fun areContentsTheSame(oldItem: DebtDetail, newItem: DebtDetail): Boolean {
            return oldItem == newItem
        }
    }
}
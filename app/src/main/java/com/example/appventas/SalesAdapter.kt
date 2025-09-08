package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.SaleWithClientName
import java.text.SimpleDateFormat
import java.util.Locale

class SalesAdapter(private val onItemClicked: (SaleWithClientName) -> Unit) : ListAdapter<SaleWithClientName, SalesAdapter.SaleViewHolder>(SalesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        return SaleViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val clientNameTextView: TextView = itemView.findViewById(R.id.tvSaleClientName)
        private val dateTextView: TextView = itemView.findViewById(R.id.tvSaleDate)
        private val totalTextView: TextView = itemView.findViewById(R.id.tvSaleTotal)

        fun bind(sale: SaleWithClientName) {
            clientNameTextView.text = sale.clientName
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = sdf.format(sale.saleDate)
            totalTextView.text = "Total: $${"%.2f".format(sale.totalAmount)}"
        }

        companion object {
            fun create(parent: ViewGroup): SaleViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_sale, parent, false)
                return SaleViewHolder(view)
            }
        }
    }

    class SalesComparator : DiffUtil.ItemCallback<SaleWithClientName>() {
        override fun areItemsTheSame(oldItem: SaleWithClientName, newItem: SaleWithClientName): Boolean {
            return oldItem.ventaId == newItem.ventaId
        }

        override fun areContentsTheSame(oldItem: SaleWithClientName, newItem: SaleWithClientName): Boolean {
            return oldItem == newItem
        }
    }
}
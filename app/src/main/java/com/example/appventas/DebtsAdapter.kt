package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.DebtSummary

// 1. AÑADIMOS EL PARÁMETRO AL CONSTRUCTOR
class DebtsAdapter(private val onItemClicked: (DebtSummary) -> Unit) : ListAdapter<DebtSummary, DebtsAdapter.DebtViewHolder>(DebtComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtViewHolder {
        return DebtViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: DebtViewHolder, position: Int) {
        val current = getItem(position)
        // 2. ASIGNAMOS LA ACCIÓN DE CLIC A CADA ELEMENTO
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sellerName: TextView = itemView.findViewById(R.id.tvSellerName)
        private val debtAmount: TextView = itemView.findViewById(R.id.tvDebtAmount)

        fun bind(debtSummary: DebtSummary) {
            sellerName.text = debtSummary.user.username
            debtAmount.text = "Deuda Total: $${"%.2f".format(debtSummary.totalDebt)}"
        }

        companion object {
            fun create(parent: ViewGroup): DebtViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_debt_summary, parent, false)
                return DebtViewHolder(view)
            }
        }
    }

    class DebtComparator : DiffUtil.ItemCallback<DebtSummary>() {
        override fun areItemsTheSame(oldItem: DebtSummary, newItem: DebtSummary): Boolean {
            return oldItem.user.id == newItem.user.id
        }

        override fun areContentsTheSame(oldItem: DebtSummary, newItem: DebtSummary): Boolean {
            return oldItem == newItem
        }
    }
}
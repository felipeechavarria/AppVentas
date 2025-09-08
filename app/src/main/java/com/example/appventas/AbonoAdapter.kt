package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.Abono
import java.text.SimpleDateFormat
import java.util.Locale

class AbonoAdapter : ListAdapter<Abono, AbonoAdapter.AbonoViewHolder>(AbonosComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbonoViewHolder {
        return AbonoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: AbonoViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class AbonoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.tvAbonoDate)
        private val amountTextView: TextView = itemView.findViewById(R.id.tvAbonoAmount)

        fun bind(abono: Abono) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateTextView.text = sdf.format(abono.fecha)
            amountTextView.text = "$${"%.2f".format(abono.monto)}"
        }

        companion object {
            fun create(parent: ViewGroup): AbonoViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_abono, parent, false)
                return AbonoViewHolder(view)
            }
        }
    }

    class AbonosComparator : DiffUtil.ItemCallback<Abono>() {
        override fun areItemsTheSame(oldItem: Abono, newItem: Abono): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Abono, newItem: Abono): Boolean {
            return oldItem == newItem
        }
    }
}
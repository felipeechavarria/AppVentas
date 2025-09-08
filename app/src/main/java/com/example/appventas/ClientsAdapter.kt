package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.Cliente

class ClientsAdapter : ListAdapter<Cliente, ClientsAdapter.ClientViewHolder>(ClientsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        return ClientViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvClientName)
        private val phoneTextView: TextView = itemView.findViewById(R.id.tvClientPhone)
        private val addressTextView: TextView = itemView.findViewById(R.id.tvClientAddress)

        fun bind(cliente: Cliente) {
            nameTextView.text = cliente.nombre
            phoneTextView.text = cliente.telefono
            addressTextView.text = cliente.direccion
        }

        companion object {
            fun create(parent: ViewGroup): ClientViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_cliente, parent, false)
                return ClientViewHolder(view)
            }
        }
    }

    class ClientsComparator : DiffUtil.ItemCallback<Cliente>() {
        override fun areItemsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Cliente, newItem: Cliente): Boolean {
            return oldItem == newItem
        }
    }
}
package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.Producto

// Adaptador simple para mostrar los productos en la venta actual
class SaleItemsAdapter(private val items: List<Pair<Producto, Int>>) :
    RecyclerView.Adapter<SaleItemsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(android.R.id.text1)
        val productDetails: TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (product, quantity) = items[position]
        holder.productName.text = product.nombre
        holder.productDetails.text = "Cantidad: $quantity - Subtotal: $${quantity * product.precioPublico}"
    }

    override fun getItemCount() = items.size
}
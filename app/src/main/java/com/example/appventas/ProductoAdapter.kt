package com.example.appventas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.Producto
import com.example.appventas.database.InventoryItemWithProductDetails

class ProductoAdapter(
    private val userRole: String,
    private val onAssignClicked: (Producto) -> Unit,
    private val onAcceptClicked: (InventoryItemWithProductDetails) -> Unit,
    private val onRejectClicked: (InventoryItemWithProductDetails) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(UniversalDiffCallback()) {

    companion object {
        private const val TYPE_SUPERVISOR = 0
        private const val TYPE_SELLER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (userRole == "Supervisor") TYPE_SUPERVISOR else TYPE_SELLER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return if (viewType == TYPE_SUPERVISOR) {
            SupervisorViewHolder(view)
        } else {
            SellerViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SupervisorViewHolder -> {
                val producto = getItem(position) as Producto
                holder.bind(producto, onAssignClicked)
            }
            is SellerViewHolder -> {
                val inventoryItem = getItem(position) as InventoryItemWithProductDetails
                holder.bind(inventoryItem, onAcceptClicked, onRejectClicked)
            }
        }
    }

    // ViewHolder para la vista del Supervisor
    class SupervisorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val referenceCodeItemView: TextView = itemView.findViewById(R.id.tvReferenceCode)
        private val nombreItemView: TextView = itemView.findViewById(R.id.tvNombreProducto)
        private val detailsItemView: TextView = itemView.findViewById(R.id.tvDetails)

        fun bind(producto: Producto, onAssignClicked: (Producto) -> Unit) {
            referenceCodeItemView.text = producto.referenceCode
            nombreItemView.text = producto.nombre
            detailsItemView.text = "Stock: ${producto.quantityInStock} | Vendedor: $${producto.precioVendedor}"
            itemView.setOnClickListener { onAssignClicked(producto) }
        }
    }

    // ViewHolder para la vista del Vendedor
    class SellerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val referenceCodeItemView: TextView = itemView.findViewById(R.id.tvReferenceCode)
        private val nombreItemView: TextView = itemView.findViewById(R.id.tvNombreProducto)
        private val detailsItemView: TextView = itemView.findViewById(R.id.tvDetails)
        private val sellerActionsLayout: LinearLayout = itemView.findViewById(R.id.sellerActionsLayout)
        private val btnAccept: Button = itemView.findViewById(R.id.btnAccept)
        private val btnReject: Button = itemView.findViewById(R.id.btnReject)

        fun bind(
            item: InventoryItemWithProductDetails,
            onAcceptClicked: (InventoryItemWithProductDetails) -> Unit,
            onRejectClicked: (InventoryItemWithProductDetails) -> Unit
        ) {
            referenceCodeItemView.text = item.referenceCode
            nombreItemView.text = item.productName
            detailsItemView.text = "Asignado: ${item.quantity} | Estado: ${item.status}"

            if (item.status == "Asignado") {
                sellerActionsLayout.visibility = View.VISIBLE
                btnAccept.setOnClickListener { onAcceptClicked(item) }
                btnReject.setOnClickListener { onRejectClicked(item) }
            } else {
                sellerActionsLayout.visibility = View.GONE
            }
        }
    }

    // Un comparador universal para ambos tipos de listas
    class UniversalDiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem is Producto && newItem is Producto) {
                return oldItem.id == newItem.id
            }
            if (oldItem is InventoryItemWithProductDetails && newItem is InventoryItemWithProductDetails) {
                return oldItem.inventoryId == newItem.inventoryId
            }
            return false
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            if (oldItem is Producto && newItem is Producto) {
                return oldItem == newItem
            }
            if (oldItem is InventoryItemWithProductDetails && newItem is InventoryItemWithProductDetails) {
                return oldItem == newItem
            }
            return false
        }
    }
}
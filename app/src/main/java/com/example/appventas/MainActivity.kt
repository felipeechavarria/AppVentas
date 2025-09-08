package com.example.appventas

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import com.example.appventas.database.InventoryItem
import com.example.appventas.database.Producto
import com.example.appventas.database.User
import com.example.appventas.database.InventoryItemWithProductDetails
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ProductoAdapter
    private var userRole: String? = null
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userRole = intent.getStringExtra("USER_ROLE")
        userId = intent.getIntExtra("USER_ID", -1)

        database = AppDatabase.getDatabase(this.applicationContext)
        recyclerView = findViewById(R.id.recyclerViewProductos)

        adapter = ProductoAdapter(
            userRole = userRole ?: "",
            onAssignClicked = { producto: Producto -> // <-- TIPO AÑADIDO
                showAssignDialog(producto)
            },
            onAcceptClicked = { inventoryItem: InventoryItemWithProductDetails -> // <-- TIPO AÑADIDO
                updateInventoryItemStatus(inventoryItem, "Aceptado")
            },
            onRejectClicked = { inventoryItem: InventoryItemWithProductDetails -> // <-- TIPO AÑADIDO
                updateInventoryItemStatus(inventoryItem, "Rechazado")
            }
        )
        recyclerView.adapter = adapter

        if (userRole == "Supervisor") {
            loadAllProducts()
        } else {
            loadSellerInventory(userId)
        }

        setupUIBasedOnRole()
    }

    private fun setupUIBasedOnRole() {
        val formAddProduct = findViewById<LinearLayout>(R.id.formAddProduct)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnManageClients = findViewById<Button>(R.id.btnManageClients)
        val btnRegisterSale = findViewById<Button>(R.id.btnRegisterSale)

        btnChangePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }

        if (userRole == "Supervisor") {
            formAddProduct.visibility = View.VISIBLE
            btnAddUser.visibility = View.VISIBLE
            btnManageClients.visibility = View.GONE
            btnRegisterSale.visibility = View.GONE
            setupSupervisorButtons()
        } else { // Vendedor
            formAddProduct.visibility = View.GONE
            btnAddUser.visibility = View.GONE
            btnManageClients.visibility = View.VISIBLE
            btnRegisterSale.visibility = View.VISIBLE

            // Lógica del botón para el Vendedor
            btnManageClients.setOnClickListener {
                val intent = Intent(this, ClientsActivity::class.java)
                intent.putExtra("SELLER_ID", userId)
                startActivity(intent)
            }
            btnRegisterSale.setOnClickListener {
                val intent = Intent(this, SalesActivity::class.java)
                intent.putExtra("SELLER_ID", userId)
                startActivity(intent)
            }
        }
    }

    private fun loadAllProducts() {
        lifecycleScope.launch {
            database.productoDao().obtenerTodos().collect { listaProductos ->
                adapter.submitList(listaProductos)
            }
        }
    }

    private fun loadSellerInventory(sellerId: Int) {
        if (sellerId == -1) return
        lifecycleScope.launch {
            // Usamos la nueva función del DAO que hace el JOIN
            database.inventoryDao().getInventoryForSeller(sellerId).collect { inventoryList ->
                // La lista ya viene combinada, la pasamos directamente al adaptador
                adapter.submitList(inventoryList as List<Any>)
            }
        }
    }

    private fun updateInventoryItemStatus(item: InventoryItemWithProductDetails, newStatus: String) {
        lifecycleScope.launch {
            // 1. Preparamos el item de inventario con su nuevo estado
            val inventoryItem = InventoryItem(
                id = item.inventoryId,
                productId = item.productId,
                sellerId = userId,
                quantity = item.quantity,
                status = newStatus // El nuevo estado ("Aceptado" o "Rechazado")
            )
            // Actualizamos el item en la base de datos
            database.inventoryDao().update(inventoryItem)

            // --- LÓGICA NUEVA PARA DEVOLVER EL STOCK ---
            if (newStatus == "Rechazado") {
                // Buscamos el producto original en la base de datos
                val product = database.productoDao().getProductById(item.productId)
                if (product != null) {
                    // Calculamos el nuevo stock sumando la cantidad rechazada
                    val newStock = product.quantityInStock + item.quantity
                    val updatedProduct = product.copy(quantityInStock = newStock)

                    // Actualizamos el producto con el stock devuelto
                    database.productoDao().update(updatedProduct)
                }
            }

            // Mostramos la confirmación al usuario
            runOnUiThread {
                val message = if (newStatus == "Rechazado") "Producto rechazado y stock devuelto" else "Estado actualizado a: $newStatus"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSupervisorButtons() {
        val etReferenceCode = findViewById<EditText>(R.id.etReferenceCode)
        val etNombreProducto = findViewById<EditText>(R.id.etNombreProducto)
        val etQuantityInStock = findViewById<EditText>(R.id.etQuantityInStock)
        val etPrecioVendedor = findViewById<EditText>(R.id.etPrecioVendedor)
        val etPrecioPublico = findViewById<EditText>(R.id.etPrecioPublico)
        val btnGuardar = findViewById<Button>(R.id.btnGuardar)
        val btnAddUser = findViewById<Button>(R.id.btnAddUser)

        btnAddUser.setOnClickListener {
            val intent = Intent(this, AddUserActivity::class.java)
            startActivity(intent)
        }

        btnGuardar.setOnClickListener {
            // Leemos los valores DENTRO del listener, justo cuando se hace clic
            val referenceCode = etReferenceCode.text.toString()
            val nombre = etNombreProducto.text.toString()
            val quantityStr = etQuantityInStock.text.toString()
            val precioVendedorStr = etPrecioVendedor.text.toString()
            val precioPublicoStr = etPrecioPublico.text.toString()

            if (referenceCode.isNotEmpty() && nombre.isNotEmpty() && quantityStr.isNotEmpty() && precioVendedorStr.isNotEmpty() && precioPublicoStr.isNotEmpty()) {
                val nuevoProducto = Producto(
                    referenceCode = referenceCode,
                    nombre = nombre,
                    quantityInStock = quantityStr.toInt(),
                    precioVendedor = precioVendedorStr.toDouble(),
                    precioPublico = precioPublicoStr.toDouble()
                )

                lifecycleScope.launch {
                    database.productoDao().insertar(nuevoProducto)
                }

                Toast.makeText(this, "Producto guardado", Toast.LENGTH_SHORT).show()
                etReferenceCode.text.clear()
                etNombreProducto.text.clear()
                etQuantityInStock.text.clear()
                etPrecioVendedor.text.clear()
                etPrecioPublico.text.clear()
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAssignDialog(producto: Producto) {
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_assign_product, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val productName = dialogView.findViewById<TextView>(R.id.tvDialogProductName)
        val spinnerSellers = dialogView.findViewById<Spinner>(R.id.spinnerSellers)
        val etQuantity = dialogView.findViewById<EditText>(R.id.etQuantity)

        productName.text = "Asignar: ${producto.nombre}"
        val alertDialog = builder.create()

        lifecycleScope.launch {
            val sellers = database.userDao().getAllSellers().first()
            val sellerNames = sellers.map { it.username }

            val spinnerAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, sellerNames)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerSellers.adapter = spinnerAdapter

            if (sellers.isNotEmpty()) {
                alertDialog.show()
            } else {
                Toast.makeText(this@MainActivity, "No hay vendedores registrados para asignar", Toast.LENGTH_LONG).show()
            }
        }

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Asignar") { dialog, _ ->
            if (spinnerSellers.selectedItem != null) {
                val selectedSellerName = spinnerSellers.selectedItem.toString()
                val quantityStr = etQuantity.text.toString()
                if (quantityStr.isNotEmpty()) {
                    val quantity = quantityStr.toInt() // <-- El nombre correcto es 'quantity'

                    // Usamos 'quantity' para la validación
                    if (quantity > producto.quantityInStock) {
                        Toast.makeText(applicationContext, "No hay suficiente stock. Disponible: ${producto.quantityInStock}", Toast.LENGTH_LONG).show()
                        return@setButton
                    }

                    lifecycleScope.launch {
                        val sellers = database.userDao().getAllSellers().first()
                        val selectedSeller = sellers.find { it.username == selectedSellerName }

                        if (selectedSeller != null) {
                            val updatedProduct = producto.copy(quantityInStock = producto.quantityInStock - quantity)
                            database.productoDao().update(updatedProduct)

                            val inventoryItem = InventoryItem(
                                productId = producto.id,
                                sellerId = selectedSeller.id,
                                quantity = quantity, // <-- Usamos 'quantity' aquí también
                                status = "Asignado"
                            )
                            database.inventoryDao().insert(inventoryItem)

                            runOnUiThread {
                                Toast.makeText(applicationContext, "Producto asignado", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(applicationContext, "Ingresa una cantidad", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancelar") { dialog, _ ->
            dialog.cancel()
        }
    }
}
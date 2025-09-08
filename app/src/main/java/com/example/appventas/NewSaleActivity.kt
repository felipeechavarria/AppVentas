package com.example.appventas

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import com.example.appventas.database.Cliente
import com.example.appventas.database.InventoryItem
import com.example.appventas.database.Producto
import com.example.appventas.database.Venta
import com.example.appventas.database.VentaItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
import android.app.DatePickerDialog

class NewSaleActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var sellerId: Int = -1
    private var availableProducts = listOf<Pair<InventoryItem, Producto>>()
    private val saleItems = mutableListOf<Pair<Producto, Int>>()
    private lateinit var saleItemsAdapter: SaleItemsAdapter
    private var fechaPactadaSeleccionada: Date? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_sale)

        database = AppDatabase.getDatabase(this)
        sellerId = intent.getIntExtra("SELLER_ID", -1)

        val spinnerClients = findViewById<Spinner>(R.id.spinnerClients)
        val spinnerProducts = findViewById<Spinner>(R.id.spinnerProducts)
        val etQuantity = findViewById<EditText>(R.id.etSaleQuantity)
        val btnAddProduct = findViewById<Button>(R.id.btnAddProductToSale)
        val btnFinalizeSale = findViewById<Button>(R.id.btnFinalizeSale)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val recyclerViewSaleItems = findViewById<RecyclerView>(R.id.recyclerViewSaleItems)
        val etDueDate = findViewById<EditText>(R.id.etDueDate)

        etDueDate.setOnClickListener {
            showDatePickerDialog()
        }

        // ... (resto del onCreate) ...
        loadClients(spinnerClients)
        loadProducts(spinnerProducts)

        btnAddProduct.setOnClickListener { addProductToSale() }
        btnFinalizeSale.setOnClickListener { finalizeSale() }


    // Configurar el RecyclerView para los ítems de la venta
        recyclerViewSaleItems.layoutManager = LinearLayoutManager(this)
        saleItemsAdapter = SaleItemsAdapter(saleItems)
        recyclerViewSaleItems.adapter = saleItemsAdapter

        // Cargar datos en los Spinners
        loadClients(spinnerClients)
        loadProducts(spinnerProducts)

        // Lógica de los botones
        btnAddProduct.setOnClickListener { addProductToSale() }
        btnFinalizeSale.setOnClickListener { finalizeSale() }
    }

    private fun loadClients(spinner: Spinner) {
        lifecycleScope.launch {
            val clients = database.clienteDao().getAll(sellerId).first()
            val clientNames = clients.map { it.nombre }
            val adapter = ArrayAdapter(
                this@NewSaleActivity,
                android.R.layout.simple_spinner_item,
                clientNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun loadProducts(spinner: Spinner) {
        lifecycleScope.launch {
            val inventory = database.inventoryDao().getAcceptedInventoryForSeller(sellerId).first()
            val productDetailsList = mutableListOf<Pair<InventoryItem, Producto>>()
            val productDisplayNames = mutableListOf<String>()

            for (item in inventory) {
                val product = database.productoDao().getProductById(item.productId)
                if (product != null) {
                    productDetailsList.add(Pair(item, product))
                    productDisplayNames.add("${product.nombre} (Disp: ${item.quantity})")
                }
            }
            availableProducts = productDetailsList

            val adapter = ArrayAdapter(
                this@NewSaleActivity,
                android.R.layout.simple_spinner_item,
                productDisplayNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun addProductToSale() {
        val spinnerProducts = findViewById<Spinner>(R.id.spinnerProducts)
        val etQuantity = findViewById<EditText>(R.id.etSaleQuantity)
        val quantityStr = etQuantity.text.toString()

        if (spinnerProducts.selectedItem == null) {
            Toast.makeText(this, "No hay productos disponibles", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Ingresa una cantidad", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedProductIndex = spinnerProducts.selectedItemPosition
        val (inventoryItem, product) = availableProducts[selectedProductIndex]
        val quantityToSell = quantityStr.toInt()

        if (quantityToSell > inventoryItem.quantity) {
            Toast.makeText(
                this,
                "No hay suficiente stock. Disponible: ${inventoryItem.quantity}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        saleItems.add(Pair(product, quantityToSell))
        saleItemsAdapter.notifyDataSetChanged()
        updateTotal()
        etQuantity.text.clear()
    }

    private fun updateTotal() {
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val total = saleItems.sumOf { (product, quantity) ->
            product.precioPublico * quantity
        }
        tvTotal.text = "Total: $${"%.2f".format(total)}"
    }

    private fun showDatePickerDialog() {
        val etDueDate = findViewById<EditText>(R.id.etDueDate)
        val calendar = Calendar.getInstance()

        // 1. Creamos un "oyente" que se activará cuando el usuario elija una fecha.
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            // Guardamos la fecha para usarla al finalizar la venta
            fechaPactadaSeleccionada = selectedDate.time

            // Formateamos la fecha para mostrarla en el EditText
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            etDueDate.setText(sdf.format(selectedDate.time))
        }

        // 2. Creamos y mostramos el diálogo, pasándole el "oyente" que creamos.
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun finalizeSale() {
        val spinnerClients = findViewById<Spinner>(R.id.spinnerClients)
        if (spinnerClients.selectedItem == null) {
            Toast.makeText(this, "Selecciona un cliente", Toast.LENGTH_SHORT).show()
            return
        }
        if (saleItems.isEmpty()) {
            Toast.makeText(this, "Añade al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // 1. Obtenemos la lista de clientes y el cliente seleccionado AHORA
            val clients = database.clienteDao().getAll(sellerId).first()
            val selectedClient = clients[spinnerClients.selectedItemPosition]

            val etDueDate = findViewById<EditText>(R.id.etDueDate)
            val dueDateStr = etDueDate.text.toString()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaPactada = if (dueDateStr.isNotEmpty()) sdf.parse(dueDateStr) else null

            // 2. Crear la venta usando el 'selectedClient' que ya obtuvimos
            val total = saleItems.sumOf { it.first.precioPublico * it.second }
            val newVenta = Venta(
                clienteId = selectedClient.id,
                sellerId = sellerId,
                fecha = Date(),
                total = total,
                fechaPactadaPago = fechaPactadaSeleccionada
            )
            val ventaId = database.ventaDao().insertVenta(newVenta)

            // 3. Guardar los items y actualizar el stock
            for ((product, quantitySold) in saleItems) {
                val ventaItem = VentaItem(
                    ventaId = ventaId.toInt(),
                    productId = product.id,
                    quantity = quantitySold,
                    unitPrice = product.precioPublico
                )
                database.ventaDao().insertVentaItem(ventaItem)

                val inventoryItem = database.inventoryDao().findAcceptedItem(sellerId, product.id)
                if (inventoryItem != null) {
                    val updatedItem =
                        inventoryItem.copy(quantity = inventoryItem.quantity - quantitySold)
                    database.inventoryDao().update(updatedItem)
                }
            }

            runOnUiThread {
                Toast.makeText(
                    this@NewSaleActivity,
                    "Venta registrada con éxito",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }


}
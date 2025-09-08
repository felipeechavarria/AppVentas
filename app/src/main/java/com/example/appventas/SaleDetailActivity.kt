package com.example.appventas

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.Abono
import com.example.appventas.database.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SaleDetailActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var saleId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sale_detail)

        database = AppDatabase.getDatabase(this)
        saleId = intent.getIntExtra("SALE_ID", -1)
        if (saleId == -1) {
            finish()
            return
        }

        val abonoAdapter = AbonoAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAbonos)
        recyclerView.adapter = abonoAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnRegisterAbono = findViewById<Button>(R.id.btnRegisterAbono)
        val etAbonoAmount = findViewById<EditText>(R.id.etAbonoAmount)
        val btnGenerateReceipt = findViewById<Button>(R.id.btnGenerateReceipt)

        // Referencias a los TextViews que actualizaremos
        val tvClientName = findViewById<TextView>(R.id.tvDetailClientName)
        val tvSaleDate = findViewById<TextView>(R.id.tvDetailSaleDate)
        val tvTotal = findViewById<TextView>(R.id.tvDetailTotal)
        val tvBalance = findViewById<TextView>(R.id.tvDetailBalance)

        lifecycleScope.launch {
            database.abonoDao().getAbonosForSale(saleId).collectLatest { abonos ->
                abonoAdapter.submitList(abonos)

                val saleDetails = database.ventaDao().getSaleDetails(saleId).first()
                val totalAbonado = abonos.sumOf { it.monto }
                val saldoPendiente = saleDetails.totalAmount - totalAbonado
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                tvClientName.text = "Cliente: ${saleDetails.clientName}"
                tvSaleDate.text = "Fecha: ${sdf.format(saleDetails.saleDate)}"
                tvTotal.text = "Total Venta: $${"%.2f".format(saleDetails.totalAmount)}"
                tvBalance.text = "Saldo Pendiente: $${"%.2f".format(saldoPendiente)}"
            }
        }

        btnRegisterAbono.setOnClickListener {
            val amountStr = etAbonoAmount.text.toString()
            if (amountStr.isNotEmpty()) {
                val amount = amountStr.toDouble()
                val newAbono = Abono(ventaId = saleId, monto = amount, fecha = Date())
                lifecycleScope.launch {
                    database.abonoDao().insert(newAbono)
                    runOnUiThread {
                        etAbonoAmount.text.clear()
                        Toast.makeText(applicationContext, "Abono registrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnGenerateReceipt.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            } else {
                generateAndSaveReceipt()
            }
        }
    }

    private fun generateAndSaveReceipt() {
        lifecycleScope.launch {
            val saleDetails = database.ventaDao().getSaleDetails(saleId).first()
            val abonos = database.abonoDao().getAbonosForSale(saleId).first()
            val ventaWithItems = database.ventaDao().getVentaWithItems(saleId)
            val totalAbonado = abonos.sumOf { it.monto }
            val saldoPendiente = saleDetails.totalAmount - totalAbonado

            val inflater = LayoutInflater.from(this@SaleDetailActivity)
            val receiptView = inflater.inflate(R.layout.layout_receipt, null)

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            receiptView.findViewById<TextView>(R.id.tvReceiptSaleId).text = "Venta N°: ${saleDetails.ventaId}"
            receiptView.findViewById<TextView>(R.id.tvReceiptClientName).text = "Cliente: ${saleDetails.clientName}"
            receiptView.findViewById<TextView>(R.id.tvReceiptSaleDate).text = "Fecha de Venta: ${sdf.format(saleDetails.saleDate)}"

            val tvDueDate = receiptView.findViewById<TextView>(R.id.tvReceiptPaymentDueDate)
            if (ventaWithItems?.venta?.fechaPactadaPago != null) {
                tvDueDate.text = "Fecha Pactada de Pago: ${sdf.format(ventaWithItems.venta.fechaPactadaPago!!)}"
                tvDueDate.visibility = View.VISIBLE
            }

            val itemsLayout = receiptView.findViewById<LinearLayout>(R.id.llReceiptItems)
            itemsLayout.removeAllViews()

            ventaWithItems?.items?.forEach { item ->
                val product = database.productoDao().getProductById(item.productId)
                if (product != null) {
                    val row = LinearLayout(this@SaleDetailActivity).apply {
                        orientation = LinearLayout.HORIZONTAL
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        addView(TextView(this@SaleDetailActivity).apply { text = product.nombre; layoutParams = LinearLayout.LayoutParams(0, -2, 3f); setTextColor(resources.getColor(android.R.color.black)) })
                        addView(TextView(this@SaleDetailActivity).apply { text = item.quantity.toString(); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(0, -2, 1f); setTextColor(resources.getColor(android.R.color.black)) })
                        addView(TextView(this@SaleDetailActivity).apply { text = "$${"%.2f".format(item.unitPrice)}"; gravity = Gravity.END; layoutParams = LinearLayout.LayoutParams(0, -2, 1.5f); setTextColor(resources.getColor(android.R.color.black)) })
                        addView(TextView(this@SaleDetailActivity).apply { text = "$${"%.2f".format(item.quantity * item.unitPrice)}"; gravity = Gravity.END; layoutParams = LinearLayout.LayoutParams(0, -2, 1.5f); setTextColor(resources.getColor(android.R.color.black)) })
                    }
                    itemsLayout.addView(row)
                }
            }

            receiptView.findViewById<TextView>(R.id.tvReceiptTotal).text = "Total Venta: $${"%.2f".format(saleDetails.totalAmount)}"
            receiptView.findViewById<TextView>(R.id.tvReceiptPaid).text = "Total Abonado: $${"%.2f".format(totalAbonado)}"
            receiptView.findViewById<TextView>(R.id.tvReceiptBalance).text = "Saldo Pendiente: $${"%.2f".format(saldoPendiente)}"

            val bitmap = createBitmapFromView(receiptView)
            saveBitmapToGallery(bitmap, "recibo_venta_${saleDetails.ventaId}.png")
        }
    }

    private fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(350.dpToPx(), View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    private fun saveBitmapToGallery(bitmap: Bitmap, displayName: String) {
        val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = contentResolver
        val uri = resolver.insert(imageCollection, contentValues)

        uri?.let {
            // AQUÍ ESTÁ LA CORRECCIÓN: el '?.' antes de '.use'
            resolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            runOnUiThread {
                Toast.makeText(this, "Recibo guardado en la galería", Toast.LENGTH_LONG).show()
            }
        } ?: runOnUiThread { Toast.makeText(this, "No se pudo guardar el recibo", Toast.LENGTH_SHORT).show() }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            generateAndSaveReceipt()
        } else {
            Toast.makeText(this, "Permiso de almacenamiento necesario para guardar el recibo", Toast.LENGTH_LONG).show()
        }
    }
}
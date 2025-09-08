package com.example.appventas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class SalesActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var sellerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales)

        database = AppDatabase.getDatabase(this)
        sellerId = intent.getIntExtra("SELLER_ID", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSales)
        val adapter = SalesAdapter { sale ->
            val intent = Intent(this, SaleDetailActivity::class.java)
            intent.putExtra("SALE_ID", sale.ventaId)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        // Lanzamos la corutina para observar las ventas
        lifecycleScope.launch {
            database.ventaDao().getSalesForSeller(sellerId).collect { salesList ->
                adapter.submitList(salesList)
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabNewSale)
        fab.setOnClickListener {
            val intent = Intent(this, NewSaleActivity::class.java)
            intent.putExtra("SELLER_ID", sellerId)
            startActivity(intent)
        }
    }
}
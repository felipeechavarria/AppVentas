package com.example.appventas

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import kotlinx.coroutines.launch
import java.util.Date

class DebtDetailActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var sellerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debt_detail)

        database = AppDatabase.getDatabase(this)
        sellerId = intent.getIntExtra("SELLER_ID", -1)
        val sellerName = intent.getStringExtra("SELLER_NAME")

        val header = findViewById<TextView>(R.id.tvSellerNameHeader)
        header.text = "Deuda de: $sellerName"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDebtDetails)
        val adapter = DebtDetailAdapter { debtDetail ->
            // Acción al hacer clic en "Liquidar"
            liquidateDebtItem(debtDetail)
        }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            database.inventoryDao().getDebtDetailsForSeller(sellerId).collect { details ->
                adapter.submitList(details)
            }
        }
    }

    private fun liquidateDebtItem(item: com.example.appventas.database.DebtDetail) {
        lifecycleScope.launch {
            database.inventoryDao().liquidateItem(item.inventoryItemId, Date())
            runOnUiThread {
                Toast.makeText(applicationContext, "Ítem liquidado", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
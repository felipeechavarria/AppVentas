package com.example.appventas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import kotlinx.coroutines.launch
import android.content.Intent

class DebtsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debts)

        database = AppDatabase.getDatabase(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewDebts)
        val adapter = DebtsAdapter { debtSummary ->
            val intent = Intent(this, DebtDetailActivity::class.java)
            // Enviamos el ID y el nombre del vendedor a la siguiente pantalla
            intent.putExtra("SELLER_ID", debtSummary.user.id)
            intent.putExtra("SELLER_NAME", debtSummary.user.username)
            startActivity(intent)
                }
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            database.inventoryDao().getDebtsBySeller().collect { debts ->
                adapter.submitList(debts)
            }
        }
    }
}
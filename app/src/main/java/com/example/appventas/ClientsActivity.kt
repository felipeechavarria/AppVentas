package com.example.appventas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.appventas.database.AppDatabase
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ClientsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var sellerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients)

        database = AppDatabase.getDatabase(this)
        sellerId = intent.getIntExtra("SELLER_ID", -1)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewClients)
        val adapter = ClientsAdapter()
        recyclerView.adapter = adapter

        lifecycleScope.launch {
            database.clienteDao().getClientsForSeller(sellerId).collect { clientList ->
                adapter.submitList(clientList)
            }
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabAddClient)
        fab.setOnClickListener {
            val intent = Intent(this, AddClientActivity::class.java)
            intent.putExtra("SELLER_ID", sellerId)
            startActivity(intent)
        }
    }
}
package com.example.appventas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appventas.database.AppDatabase
import com.example.appventas.database.Cliente
import kotlinx.coroutines.launch

class AddClientActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var sellerId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_client)

        database = AppDatabase.getDatabase(this)
        sellerId = intent.getIntExtra("SELLER_ID", -1)

        val etName = findViewById<EditText>(R.id.etClientName)
        val etPhone = findViewById<EditText>(R.id.etClientPhone)
        val etAddress = findViewById<EditText>(R.id.etClientAddress)
        val btnSave = findViewById<Button>(R.id.btnSaveClient)

        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty() && address.isNotEmpty() && sellerId != -1) {
                val newClient = Cliente(
                    nombre = name,
                    telefono = phone,
                    direccion = address,
                    sellerId = sellerId
                )
                lifecycleScope.launch {
                    database.clienteDao().insert(newClient)
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Cliente guardado", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
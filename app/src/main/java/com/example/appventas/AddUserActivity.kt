package com.example.appventas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appventas.database.AppDatabase
import com.example.appventas.database.User
import kotlinx.coroutines.launch

class AddUserActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_user)

        database = AppDatabase.getDatabase(this)

        val etUsername = findViewById<EditText>(R.id.etNewUsername)
        val etPassword = findViewById<EditText>(R.id.etNewPassword)
        val rgRoles = findViewById<RadioGroup>(R.id.rgRoles)
        val btnSaveUser = findViewById<Button>(R.id.btnSaveUser)

        btnSaveUser.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            // Obtenemos el ID del RadioButton seleccionado
            val selectedRoleId = rgRoles.checkedRadioButtonId
            val selectedRadioButton = findViewById<RadioButton>(selectedRoleId)
            val role = selectedRadioButton.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val newUser = User(username = username, password = password, role = role)

                lifecycleScope.launch {
                    database.userDao().insert(newUser)
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Usuario guardado", Toast.LENGTH_SHORT).show()
                        finish() // Cierra la pantalla de registro y vuelve a la anterior
                    }
                }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
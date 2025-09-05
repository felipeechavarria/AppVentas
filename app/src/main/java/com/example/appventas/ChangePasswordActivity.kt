package com.example.appventas

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appventas.database.AppDatabase
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        database = AppDatabase.getDatabase(this)
        currentUserId = intent.getIntExtra("USER_ID", -1)

        val etCurrentPassword = findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)

        btnUpdatePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Las nuevas contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = database.userDao().findById(currentUserId) // Necesitaremos crear esta función

                if (user == null) {
                    Toast.makeText(this@ChangePasswordActivity, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                if (user.password != currentPassword) {
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "La contraseña actual es incorrecta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Si todo es correcto, actualizamos el usuario
                    val updatedUser = user.copy(password = newPassword)
                    database.userDao().update(updatedUser)
                    runOnUiThread {
                        Toast.makeText(this@ChangePasswordActivity, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                        finish() // Cerramos la pantalla
                    }
                }
            }
        }
    }
}
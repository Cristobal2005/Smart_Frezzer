package com.example.smartfrezzer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val correo = etCorreo.text.toString()
            val pass = etPassword.text.toString()

            if (correo.isNotEmpty() && pass.isNotEmpty()) {

                auth.signInWithEmailAndPassword(correo, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        entrarApp()
                    } else {

                        auth.createUserWithEmailAndPassword(correo, pass).addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                entrarApp()
                            } else {
                                Toast.makeText(this, "Error de credenciales", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun entrarApp() {
        startActivity(Intent(this, SeleccionActivity::class.java))
        finish()
    }
}
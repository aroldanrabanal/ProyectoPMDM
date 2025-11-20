package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.findme.databinding.ActivityRegisterBinding
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrarse.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || pass.length < 6) {
                Toast.makeText(this, "Rellena todos los campos correctamente", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registrarUsuario(email, pass, nombre)
        }

        binding.tvYaTengoCuenta.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registrarUsuario(email: String, password: String, nombre: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                val uid = auth.currentUser!!.uid

                val usuario = hashMapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "email" to email,
                    "online" to true,
                    "ultimoAcceso" to System.currentTimeMillis()
                )

                db.collection("usuarios").document(uid).set(usuario)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Bienvenido $nombre!", Toast.LENGTH_LONG).show()
                        irAMain()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error guardando datos: ${e.message}", Toast.LENGTH_LONG).show()
                        irAMain() // entra igual aunque falle
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error en registro: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
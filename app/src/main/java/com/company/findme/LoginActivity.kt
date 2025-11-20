package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.findme.databinding.ActivityLoginBinding
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Si ya está logueado → directo a Main
        //if (auth.currentUser != null) {
        //    actualizarEstadoYEntrar()
        //    return
        //}
//
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Rellena email y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    actualizarEstadoYEntrar()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

        binding.tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun actualizarEstadoYEntrar() {
        val uid = auth.currentUser!!.uid
        val updates = hashMapOf<String, Any>(
            "online" to true,
            "ultimoAcceso" to System.currentTimeMillis()
        )

        db.collection("usuarios").document(uid)
            .update(updates)
            .addOnSuccessListener { irAMain() }
            .addOnFailureListener {
                Toast.makeText(this, "Error actualizando estado", Toast.LENGTH_SHORT).show()
                irAMain()
            }
    }

    private fun irAMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
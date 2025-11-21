package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.company.findme.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Si no hay usuario logueado → Login
        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración del Navigation Component
        setSupportActionBar(binding.toolbar)
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // CARGAR NOMBRE + FOTO DE PERFIL EN TIEMPO REAL
        Firebase.auth.currentUser?.let { user ->
            Firebase.firestore.collection("usuarios").document(user.uid)
                .addSnapshotListener { doc, error ->
                    if (error != null) {
                        Log.w("Firestore", "Error escuchando usuario", error)
                        return@addSnapshotListener
                    }

                    if (doc != null && doc.exists()) {
                        val nombre = doc.getString("nombre") ?: "Usuario"
                        val fotoUrl = doc.getString("fotoPerfil")

                        // Actualizar nombre
                        binding.tvNombreUsuario.text = nombre

                        // Actualizar foto con Glide
                        if (!fotoUrl.isNullOrEmpty()) {
                            Glide.with(this@MainActivity)
                                .load(fotoUrl)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .circleCrop()
                                .into(binding.ivFotoPerfil)
                        } else {
                            binding.ivFotoPerfil.setImageResource(R.drawable.ic_person)
                        }
                    }
                }
        }

        // Click en el header → ir a Perfil
        binding.headerPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        probarConexionFirestore()
    }

    private fun probarConexionFirestore() {
        val db = Firebase.firestore
        val datosPrueba = hashMapOf(
            "nombre" to "Prueba de conexión",
            "timestamp" to System.currentTimeMillis(),
            "dispositivo" to android.os.Build.MODEL
        )

        db.collection("pruebas_conexion")
            .document("test_${System.currentTimeMillis()}")
            .set(datosPrueba)
            .addOnSuccessListener {
                Log.d("Firestore", "¡Conexión exitosa!")
                Toast.makeText(this, "Firestore conectado ✓", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al escribir", e)
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
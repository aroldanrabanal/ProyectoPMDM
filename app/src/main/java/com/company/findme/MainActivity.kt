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
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.company.findme.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.messaging

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        Firebase.messaging.token.addOnSuccessListener { token ->
            Firebase.auth.currentUser?.let { user ->
                Firebase.firestore.collection("usuarios").document(user.uid)
                    .update("fcmToken", token)
            }
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
        )
        binding.navView.setupWithNavController(navController)

        binding.headerPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }
        binding.navView.setupWithNavController(navController)

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

                        binding.tvNombreUsuario.text = nombre

                        if (!fotoUrl.isNullOrEmpty()) {
                            Glide.with(this@MainActivity)
                                .load(fotoUrl)
                                .placeholder(R.drawable.ic_person)
                                .error(R.drawable.ic_person)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .circleCrop()
                                .into(binding.ivFotoPerfil)
                        } else {
                            binding.ivFotoPerfil.setImageResource(R.drawable.ic_person)
                        }
                    }
                }
        }

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
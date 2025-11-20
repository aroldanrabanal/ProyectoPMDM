package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.company.findme.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Firebase.auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        binding.tvNombreUsuario.text = "Cargando..."

        Firebase.auth.currentUser?.let { user ->
            Firebase.firestore.collection("usuarios").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: "Usuario"
                    binding.tvNombreUsuario.text = nombre
                }
        }

        binding.headerPerfil.setOnClickListener {
            if (this !is PerfilActivity) {
                startActivity(Intent(this, PerfilActivity::class.java))
            }
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
                Log.d("Firestore", "¡Conexión exitosa! Documento escrito correctamente")
                Toast.makeText(this, "Firestore conectado ✓", Toast.LENGTH_LONG).show()
                leerDatosPrueba()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al escribir documento", e)
                Toast.makeText(this, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun leerDatosPrueba() {
        val db = Firebase.firestore
        db.collection("pruebas_conexion")
            .get()
            .addOnSuccessListener { result ->
                Log.d("Firestore", "Lectura exitosa. Documentos encontrados: ${result.size()}")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al leer", e)
            }
    }
}
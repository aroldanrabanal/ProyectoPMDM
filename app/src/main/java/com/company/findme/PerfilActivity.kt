package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.company.findme.databinding.ActivityPerfilBinding
import com.company.findme.databinding.DialogEditarNombreBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Perfil"

        cargarDatosUsuario()

        binding.btnEditarNombre.setOnClickListener {
            mostrarDialogEditarNombre()
        }

        binding.btnCerrarSesion.setOnClickListener {
            db.collection("usuarios").document(user.uid)
                .update("online", false)
            Firebase.auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun cargarDatosUsuario() {
        db.collection("usuarios").document(user.uid)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val nombre = doc.getString("nombre") ?: "Usuario"
                    binding.tvNombre.text = nombre
                    binding.tvEmail.text = doc.getString("email") ?: user.email
                }
            }
    }

    private fun mostrarDialogEditarNombre() {
        val dialogBinding = DialogEditarNombreBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Editar nombre")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val btnGuardar = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            btnGuardar.setOnClickListener {
                val nuevoNombre = dialogBinding.etNuevoNombre.text.toString().trim()

                if (nuevoNombre.length < 2) {
                    dialogBinding.etNuevoNombre.error = "Nombre muy corto"
                    return@setOnClickListener
                }

                Firebase.firestore.collection("usuarios").document(Firebase.auth.currentUser!!.uid)
                    .update("nombre", nuevoNombre)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nombre actualizado ✓", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show()
                    }
            }

            Firebase.firestore.collection("usuarios").document(Firebase.auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val nombre = doc.getString("nombre") ?: ""
                    dialogBinding.etNuevoNombre.setText(nombre)
                    dialogBinding.etNuevoNombre.setSelection(nombre.length)
                }
        }

        dialog.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
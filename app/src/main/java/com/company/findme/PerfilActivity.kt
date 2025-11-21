package com.company.findme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.company.findme.databinding.ActivityPerfilBinding
import com.company.findme.databinding.DialogEditarNombreBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser!!

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { subirFotoCloudinary(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Mi Perfil"

        cargarDatosUsuario()

        binding.ivFotoPerfilGrande.setOnClickListener {
            binding.overlayFoto.alpha = 0.6f
            binding.ivIconoCamara.alpha = 1f

            pickImageLauncher.launch("image/*")

            it.postDelayed({
                binding.overlayFoto.alpha = 0f
                binding.ivIconoCamara.alpha = 0f
            }, 500)
        }

        binding.btnEditarNombre.setOnClickListener {
            mostrarDialogEditarNombre()
        }

        binding.btnCerrarSesion.setOnClickListener {
            db.collection("usuarios").document(user.uid).update("online", false)
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
                    val email = doc.getString("email") ?: user.email
                    val fotoUrl = doc.getString("fotoPerfil")

                    binding.tvNombre.text = nombre
                    binding.tvEmail.text = email

                    if (!fotoUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(fotoUrl)
                            .placeholder(R.drawable.ic_person)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .error(R.drawable.ic_person)
                            .into(binding.ivFotoPerfilGrande)
                    } else {
                        binding.ivFotoPerfilGrande.setImageResource(R.drawable.ic_person)
                    }
                }
            }
    }

    private fun subirFotoCloudinary(uri: Uri) = lifecycleScope.launch {
        withContext(Dispatchers.Main) {
            Toast.makeText(this@PerfilActivity, "Subiendo foto...", Toast.LENGTH_SHORT).show()
        }

        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: return@launch
            inputStream?.close()

            val config = mapOf(
                "cloud_name" to "dfaegihzi",
                "api_key" to "191848238714461",
                "api_secret" to "u1lf8ee0PkT26U2vjy6Igz1ueWI"
            )

            val cloudinary = Cloudinary(config)

            val options = ObjectUtils.asMap(
                "folder", "findme_perfiles",
                "public_id", "${user.uid}_perfil",
                "overwrite", true,
                "invalidate", true,
                "upload_preset", "android"
            )

            val result = withContext(Dispatchers.IO) {
                cloudinary.uploader().upload(bytes, options)
            }

            val secureUrl = (result as Map<*, *>)["secure_url"] as String

            db.collection("usuarios").document(user.uid)
                .update("fotoPerfil", secureUrl)
                .addOnSuccessListener {
                    runOnUiThread {
                        Toast.makeText(this@PerfilActivity, "Foto actualizada ✓", Toast.LENGTH_SHORT).show()
                    }
                }

        } catch (e: Exception) {
            runOnUiThread {
                Toast.makeText(this@PerfilActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("Cloudinary", "Error subiendo foto", e)
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

                db.collection("usuarios").document(user.uid)
                    .update("nombre", nuevoNombre)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Nombre actualizado ✓", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
            }

            db.collection("usuarios").document(user.uid).get()
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
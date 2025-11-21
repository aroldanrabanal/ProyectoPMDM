package com.company.findme.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.findme.R
import com.company.findme.Usuario
import com.company.findme.databinding.ItemUsuarioBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class UsuariosAdapter : RecyclerView.Adapter<UsuariosAdapter.ViewHolder>() {

    private var usuarios = mutableListOf<Usuario>()

    class ViewHolder(val binding: ItemUsuarioBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUsuarioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = usuarios[position]
        val currentUserId = Firebase.auth.currentUser!!.uid

        with(holder.binding) {
            tvNombre.text = usuario.nombre
            tvEstado.text = if (usuario.online) "En línea" else "Desconectado"

            Glide.with(root.context)
                .load(usuario.fotoPerfil)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivFoto)

            // Evitar que te añadas a ti mismo
            if (usuario.uid == currentUserId) {
                btnAgregar.isEnabled = false
                btnAgregar.text = "Tú"
                return@with
            }

            // Comprobar si ya es amigo o tiene solicitud pendiente
            Firebase.firestore.collection("usuarios")
                .document(currentUserId)
                .collection("solicitudes_enviadas")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        btnAgregar.text = "Pendiente"
                        btnAgregar.isEnabled = false
                    }
                }

            Firebase.firestore.collection("usuarios")
                .document(currentUserId)
                .collection("amigos")
                .document(usuario.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        btnAgregar.text = "Amigo"
                        btnAgregar.isEnabled = false
                    }
                }

            btnAgregar.setOnClickListener {
                enviarSolicitudAmistad(currentUserId, usuario.uid)
                btnAgregar.text = "Pendiente"
                btnAgregar.isEnabled = false
            }
        }
    }

    private fun enviarSolicitudAmistad(de: String, a: String) {
        val db = Firebase.firestore

        val solicitud = hashMapOf(
            "de" to de,
            "timestamp" to System.currentTimeMillis()
        )

        // Enviar solicitud
        db.collection("usuarios").document(a)
            .collection("solicitudes_recibidas")
            .document(de)
            .set(solicitud)

        db.collection("usuarios").document(de)
            .collection("solicitudes_enviadas")
            .document(a)
            .set(solicitud)
    }

    override fun getItemCount() = usuarios.size

    fun actualizarLista(nuevaLista: List<Usuario>) {
        usuarios.clear()
        usuarios.addAll(nuevaLista.filter { it.uid != Firebase.auth.currentUser!!.uid })
        notifyDataSetChanged()
    }
}
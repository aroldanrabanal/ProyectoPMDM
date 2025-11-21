package com.company.findme.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.company.findme.R
import com.company.findme.databinding.ItemSolicitudBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class SolicitudesAdapter(
    private val onAceptar: (String) -> Unit,
    private val onRechazar: (String) -> Unit
) : RecyclerView.Adapter<SolicitudesAdapter.VH>() {

    private var solicitudes = mutableListOf<Pair<String, Map<String, Any>>>() // uid + datos

    class VH(val binding: ItemSolicitudBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemSolicitudBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val (uidEmisor, data) = solicitudes[position]
        val nombre = data["nombre"] as? String ?: "Usuario"
        val foto = data["fotoPerfil"] as? String ?: ""

        with(holder.binding) {
            tvNombre.text = nombre
            Glide.with(root.context)
                .load(foto)
                .placeholder(R.drawable.ic_person)
                .circleCrop()
                .into(ivFoto)

            btnAceptar.setOnClickListener { onAceptar(uidEmisor) }
            btnRechazar.setOnClickListener { onRechazar(uidEmisor) }
        }
    }

    override fun getItemCount() = solicitudes.size

    fun actualizar(lista: List<Pair<String, Map<String, Any>>>) {
        solicitudes.clear()
        solicitudes.addAll(lista)
        notifyDataSetChanged()
    }
}
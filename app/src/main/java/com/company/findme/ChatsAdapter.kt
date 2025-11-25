package com.company.findme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import java.text.SimpleDateFormat
import java.util.*

class ChatsAdapter(
    private val chats: MutableList<ChatPreview>,
    private val onClick: (userId: String, nombre: String, fotoUrl: String?) -> Unit
) : RecyclerView.Adapter<ChatsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foto: ShapeableImageView = itemView.findViewById(R.id.ivFotoChat)
        val nombre: TextView = itemView.findViewById(R.id.tvNombreChat)
        val ultimo: TextView = itemView.findViewById(R.id.tvUltimoMensaje)
        val hora: TextView = itemView.findViewById(R.id.tvHoraChat)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = chats[position]
        holder.nombre.text = chat.nombre
        holder.ultimo.text = chat.ultimoMensaje
        holder.hora.text = chat.timestamp?.toDate()?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
        } ?: ""

        Glide.with(holder.itemView.context)
            .load(chat.fotoUrl)
            .placeholder(R.drawable.ic_person)
            .circleCrop()
            .into(holder.foto)

        holder.itemView.setOnClickListener {
            onClick(chat.userId, chat.nombre, chat.fotoUrl)
        }
    }

    override fun getItemCount() = chats.size
}
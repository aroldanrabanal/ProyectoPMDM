package com.company.findme

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val mensajes: List<Mensaje>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TIPO_ENVIADO = 1
        const val TIPO_RECIBIDO = 2
    }

    inner class EnviadoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto: TextView = itemView.findViewById(R.id.textoMensajeEnviado)
        val hora: TextView = itemView.findViewById(R.id.horaMensajeEnviado)
    }

    inner class RecibidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val texto: TextView = itemView.findViewById(R.id.textoMensajeRecibido)
        val hora: TextView = itemView.findViewById(R.id.horaMensajeRecibido)
    }

    override fun getItemViewType(position: Int) =
        if (mensajes[position].remitenteId == currentUserId) TIPO_ENVIADO else TIPO_RECIBIDO

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TIPO_ENVIADO -> EnviadoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_enviado, parent, false)
        )
        else -> RecibidoViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_mensaje_recibido, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mensaje = mensajes[position]
        val hora = mensaje.timestamp?.toDate()?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
        } ?: "•••"

        when (holder) {
            is EnviadoViewHolder -> {
                holder.texto.text = mensaje.texto
                holder.hora.text = hora
            }
            is RecibidoViewHolder -> {
                holder.texto.text = mensaje.texto
                holder.hora.text = hora
            }
        }
    }

    override fun getItemCount() = mensajes.size
}
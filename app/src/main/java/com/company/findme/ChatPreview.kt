package com.company.findme

import com.google.firebase.Timestamp

data class ChatPreview(
    val userId: String,
    val nombre: String,
    val ultimoMensaje: String,
    val timestamp: Timestamp?,
    val fotoUrl: String?
)
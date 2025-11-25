package com.company.findme

import com.google.firebase.Timestamp

data class Mensaje(
    val texto: String = "",
    val remitenteId: String = "",
    val timestamp: Timestamp? = null
)
package com.company.findme

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Nuevo token: $token")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnSuccessListener { Log.d("FCM", "Token guardado") }
                .addOnFailureListener { e -> Log.e("FCM", "Error guardando token", e) }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM", "Notificación recibida: ${message.notification?.title}")
    }
}
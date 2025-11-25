// ChatActivity.kt
package com.company.findme

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.auth
import com.google.firebase.firestore.*
import com.google.firebase.Firebase

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editMensaje: EditText
    private lateinit var btnEnviar: ImageButton

    private lateinit var adapter: ChatAdapter
    private val mensajes = mutableListOf<Mensaje>()

    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private lateinit var miId: String
    private lateinit var amigoId: String
    private lateinit var chatId: String
    private var listener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // 1. Verificar usuario autenticado
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: debes iniciar sesión", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        miId = currentUser.uid

        // 2. Obtener datos del amigo
        amigoId = intent.getStringExtra("amigoId") ?: run {
            finish()
            return
        }
        val amigoNombre = intent.getStringExtra("amigoNombre") ?: "Chat"
        supportActionBar?.title = amigoNombre
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 3. Generar ID único del chat (ordenado para que ambos usuarios tengan el mismo)
        chatId = if (miId < amigoId) {
            "${miId}_$amigoId"
        } else {
            "${amigoId}_$miId"
        }

        // 4. Inicializar vistas
        recyclerView = findViewById(R.id.recyclerChat)
        editMensaje = findViewById(R.id.editTextMensaje)
        btnEnviar = findViewById(R.id.buttonEnviar)

        // 5. Configurar RecyclerView
        adapter = ChatAdapter(mensajes, miId)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = this@ChatActivity.adapter
            // Scroll automático cuando se abre el teclado
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    postDelayed({
                        if (mensajes.isNotEmpty()) scrollToPosition(mensajes.size - 1)
                    }, 100)
                }
            }
        }

        // 6. Eventos
        btnEnviar.setOnClickListener { enviarMensaje() }

        // 7. Cargar mensajes y crear chat si no existe
        cargarMensajes()
        crearChatSiNoExiste()
    }

    private fun crearChatSiNoExiste() {
        db.collection("chats").document(chatId)
            .set(
                hashMapOf(
                    "participantes" to listOf(miId, amigoId),
                    "timestamp" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
    }

    private fun enviarMensaje() {
        val texto = editMensaje.text.toString().trim()
        if (texto.isEmpty()) return

        val mensaje = hashMapOf(
            "texto" to texto,
            "remitenteId" to miId,
            "timestamp" to FieldValue.serverTimestamp()
        )

        // Guardar mensaje en la subcolección
        db.collection("chats").document(chatId).collection("mensajes")
            .add(mensaje)
            .addOnFailureListener {
                Toast.makeText(this, "Error al enviar", Toast.LENGTH_SHORT).show()
            }

        // Actualizar documento principal del chat (para la lista de chats)
        db.collection("chats").document(chatId)
            .set(
                hashMapOf(
                    "ultimoMensaje" to texto,
                    "timestamp" to FieldValue.serverTimestamp(),
                    "participantes" to listOf(miId, amigoId)
                ),
                SetOptions.merge()
            )

        editMensaje.text.clear()
    }

    private fun cargarMensajes() {
        listener = db.collection("chats")
            .document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error al cargar mensajes", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                mensajes.clear()
                snapshot?.documents?.forEach { doc ->
                    val texto = doc.getString("texto") ?: ""
                    val remitenteId = doc.getString("remitenteId") ?: ""
                    val timestamp = doc.getTimestamp("timestamp")
                    mensajes.add(Mensaje(texto, remitenteId, timestamp))
                }

                adapter.notifyDataSetChanged()
                if (mensajes.isNotEmpty()) {
                    recyclerView.scrollToPosition(mensajes.size - 1)
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
    }
}
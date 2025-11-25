package com.company.findme

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.Firebase

class ChatsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private val listaChats = mutableListOf<ChatPreview>()
    private lateinit var adapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recyclerView = view.findViewById(R.id.recycler_chats)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ChatsAdapter(listaChats) { userId, nombre, fotoUrl ->
            startActivity(Intent(requireContext(), ChatActivity::class.java).apply {
                putExtra("amigoId", userId)
                putExtra("amigoNombre", nombre)
                putExtra("fotoUrl", fotoUrl)
            })
        }
        recyclerView.adapter = adapter

        cargarSoloAmigosConChat()
        return view
    }

    private fun cargarSoloAmigosConChat() {
        val miId = Firebase.auth.currentUser?.uid ?: return
        val db = Firebase.firestore

        // 1. Escuchamos SOLO los chats donde yo participo (como antes, pero más eficiente)
        db.collection("chats")
            .whereArrayContains("participantes", miId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null) return@addSnapshotListener

                listaChats.clear()

                for (doc in snapshot.documents) {
                    val participantes = doc.get("participantes") as? List<String> ?: continue
                    val amigoId = participantes.firstOrNull { it != miId } ?: continue

                    // 2. COMPROBAMOS QUE SEA AMIGO (subcolección "amigos")
                    db.collection("usuarios").document(miId).collection("amigos").document(amigoId).get()
                        .addOnSuccessListener { amigoDoc ->
                            if (amigoDoc.exists()) { // ← SOLO SI ES AMIGO
                                val ultimoMensaje = doc.getString("ultimoMensaje") ?: "Sin mensajes"
                                val timestamp = doc.getTimestamp("timestamp")

                                // 3. Cargamos datos del amigo
                                db.collection("usuarios").document(amigoId).get()
                                    .addOnSuccessListener { userDoc ->
                                        val nombre = userDoc.getString("nombre") ?: "Usuario"
                                        val foto = userDoc.getString("fotoPerfil")

                                        val preview = ChatPreview(amigoId, nombre, ultimoMensaje, timestamp, foto)
                                        if (listaChats.none { it.userId == amigoId }) {
                                            listaChats.add(preview)
                                        }
                                        ordenarLista()
                                    }
                            }
                        }
                }
            }
    }

    private fun ordenarLista() {
        listaChats.sortByDescending { it.timestamp?.seconds ?: 0L }
        adapter.notifyDataSetChanged()
    }
}
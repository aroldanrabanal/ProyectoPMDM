package com.company.findme.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.company.findme.adapter.SolicitudesAdapter
import com.company.findme.databinding.FragmentNotificationsBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val adapter = SolicitudesAdapter(
        onAceptar = { uid -> aceptarSolicitud(uid) },
        onRechazar = { uid -> rechazarSolicitud(uid) }
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerSolicitudes.adapter = adapter
        cargarSolicitudes()
    }

    private fun cargarSolicitudes() {
        val miUid = Firebase.auth.currentUser!!.uid
        Firebase.firestore.collection("usuarios").document(miUid)
            .collection("solicitudes_recibidas")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || snapshot.isEmpty) {
                    binding.tvSinSolicitudes.visibility = View.VISIBLE
                    binding.recyclerSolicitudes.visibility = View.GONE
                    return@addSnapshotListener
                }

                binding.tvSinSolicitudes.visibility = View.GONE
                binding.recyclerSolicitudes.visibility = View.VISIBLE

                val lista = mutableListOf<Pair<String, Map<String, Any>>>()
                for (doc in snapshot.documents) {
                    val uidEmisor = doc.id
                    Firebase.firestore.collection("usuarios").document(uidEmisor).get()
                        .addOnSuccessListener { userDoc ->
                            val data = userDoc.data ?: return@addOnSuccessListener
                            data["nombre"] = userDoc.getString("nombre") ?: "Usuario"
                            data["fotoPerfil"] = userDoc.getString("fotoPerfil") ?: ""
                            lista.add(Pair(uidEmisor, data))
                            adapter.actualizar(lista)
                        }
                }
            }
    }

    private fun aceptarSolicitud(uidEmisor: String) {
        val miUid = Firebase.auth.currentUser!!.uid
        val db = Firebase.firestore

        val batch = db.batch()

        batch.set(db.collection("usuarios").document(miUid).collection("amigos").document(uidEmisor), mapOf("timestamp" to System.currentTimeMillis()))
        batch.set(db.collection("usuarios").document(uidEmisor).collection("amigos").document(miUid), mapOf("timestamp" to System.currentTimeMillis()))

        batch.delete(db.collection("usuarios").document(miUid).collection("solicitudes_recibidas").document(uidEmisor))
        batch.delete(db.collection("usuarios").document(uidEmisor).collection("solicitudes_enviadas").document(miUid))

        batch.commit()
    }

    private fun rechazarSolicitud(uidEmisor: String) {
        val miUid = Firebase.auth.currentUser!!.uid
        val db = Firebase.firestore
        db.collection("usuarios").document(miUid).collection("solicitudes_recibidas").document(uidEmisor).delete()
        db.collection("usuarios").document(uidEmisor).collection("solicitudes_enviadas").document(miUid).delete()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.company.findme.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.company.findme.UsuariosAdapter
import com.company.findme.databinding.FragmentHomeBinding
import com.company.findme.Usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val adapter = UsuariosAdapter()
    private var listener: ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerUsuarios.layoutManager = LinearLayoutManager(context)
        binding.recyclerUsuarios.adapter = adapter

        cargarUsuarios()
    }

    private fun cargarUsuarios() {
        listener = FirebaseFirestore.getInstance()
            .collection("usuarios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val lista = snapshot?.documents?.mapNotNull { doc ->
                    Usuario(
                        uid = doc.id,
                        nombre = doc.getString("nombre") ?: "Sin nombre",
                        fotoPerfil = doc.getString("fotoPerfil") ?: "",
                        online = doc.getBoolean("online") ?: false
                    )
                } ?: emptyList()

                adapter.actualizarLista(lista)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
        _binding = null
    }
}
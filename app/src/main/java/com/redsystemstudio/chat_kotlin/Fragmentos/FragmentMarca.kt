package com.redsystemstudio.chat_kotlin.Fragmentos

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorMarca
import com.redsystemstudio.chat_kotlin.Modelos.Marca
import com.redsystemstudio.chat_kotlin.R

class FragmentMarca : Fragment() {

    private lateinit var marcas: ArrayList<Marca>
    private lateinit var adaptador: AdaptadorMarca
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_marca, container, false)

        dbReference = FirebaseDatabase.getInstance().getReference("Marcas")
        marcas = ArrayList()

        recyclerView = view.findViewById(R.id.RV_marcas)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adaptador = AdaptadorMarca(this, marcas)
        recyclerView.adapter = adaptador

        val btnAgregar: View = view.findViewById(R.id.btn_agregar_marca)
        btnAgregar.setOnClickListener {
            mostrarDialogoAgregarMarca()
        }

        cargarMarcas()

        return view
    }

    private fun mostrarDialogoAgregarMarca() {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialogo_agregar_marca, null)
        val editText = view.findViewById<EditText>(R.id.edit_text_marca)

        AlertDialog.Builder(requireContext())
            .setTitle("Nueva Marca")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreMarca = editText.text.toString().trim()
                if (nombreMarca.isNotEmpty()) {
                    agregarMarca(nombreMarca)
                } else {
                    Toast.makeText(context, "El nombre de la marca no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun agregarMarca(nombreMarca: String) {
        try {
            val key = dbReference.push().key ?: return
            val marca = Marca(key, nombreMarca)
            
            dbReference.child(key).setValue(marca)
                .addOnSuccessListener {
                    Toast.makeText(context, "Marca agregada exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error al agregar marca: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun mostrarDialogoEditarMarca(marca: Marca) {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialogo_agregar_marca, null)
        val editText = view.findViewById<EditText>(R.id.edit_text_marca)
        editText.setText(marca.nombreMarca)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Marca")
            .setView(view)
            .setPositiveButton("Actualizar") { _, _ ->
                val nombreMarca = editText.text.toString().trim()
                if (nombreMarca.isNotEmpty()) {
                    marca.nombreMarca = nombreMarca
                    dbReference.child(marca.idMarca).setValue(marca)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Marca actualizada exitosamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error al actualizar marca: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    fun eliminarMarca(marca: Marca) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Marca")
            .setMessage("¿Está seguro de que desea eliminar esta marca?")
            .setPositiveButton("Sí") { _, _ ->
                dbReference.child(marca.idMarca).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Marca eliminada exitosamente", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error al eliminar marca: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cargarMarcas() {
        dbReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val marca = snapshot.getValue(Marca::class.java)
                    if (marca != null) {
                        marcas.add(marca)
                        marcas.sortBy { it.nombreMarca }
                        adaptador.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    Log.e("FragmentMarca", "Error al añadir marca", e)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val marca = snapshot.getValue(Marca::class.java)
                if (marca != null) {
                    val index = marcas.indexOfFirst { it.idMarca == marca.idMarca }
                    if (index != -1) {
                        marcas[index] = marca
                        marcas.sortBy { it.nombreMarca }
                        adaptador.notifyDataSetChanged()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val marca = snapshot.getValue(Marca::class.java)
                if (marca != null) {
                    val index = marcas.indexOfFirst { it.idMarca == marca.idMarca }
                    if (index != -1) {
                        marcas.removeAt(index)
                        adaptador.notifyItemRemoved(index)
                        adaptador.notifyItemRangeChanged(index, marcas.size)
                    }
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("FragmentMarca", "Error al cargar marcas", error.toException())
                Toast.makeText(context, "Error al cargar marcas: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
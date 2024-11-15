package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import com.redsystemstudio.chat_kotlin.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AdaptadorTallasModeloGrid(
    private var productos: List<ProductoNegocio1>,
    private val onTallaClick: (ProductoNegocio1) -> Unit
) : RecyclerView.Adapter<AdaptadorTallasModeloGrid.TallaViewHolder>() {

    private val tallasUnicas = mutableSetOf<String>()
    private val tallasRef = FirebaseDatabase.getInstance().getReference("Tallas")
    private val nombresTallas = mutableMapOf<String, String>()

    init {
        actualizarTallasUnicas()
        cargarNombresTallas()
    }

    class TallaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTalla: TextView = view.findViewById(R.id.tvTalla)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TallaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_talla_modelo_grid, parent, false)
        return TallaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TallaViewHolder, position: Int) {
        val idTalla = tallasUnicas.elementAt(position)
        holder.tvTalla.text = nombresTallas[idTalla] ?: "Cargando..."
        holder.itemView.setOnClickListener {
            onTallaClick(productos.first { it.idTalla == idTalla })
        }
    }

    override fun getItemCount() = tallasUnicas.size

    fun actualizarProductos(nuevosProductos: List<ProductoNegocio1>) {
        productos = nuevosProductos
        actualizarTallasUnicas()
        cargarNombresTallas()
        notifyDataSetChanged()
    }

    private fun actualizarTallasUnicas() {
        tallasUnicas.clear()
        productos.forEach { producto ->
            tallasUnicas.add(producto.idTalla)
        }
    }

    private fun cargarNombresTallas() {
        tallasUnicas.forEach { idTalla ->
            tallasRef.child(idTalla).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombreTalla = snapshot.child("nombreTalla").getValue(String::class.java)
                    if (nombreTalla != null) {
                        nombresTallas[idTalla] = nombreTalla
                        notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Manejar error si es necesario
                }
            })
        }
    }
}
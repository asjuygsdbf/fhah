package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Fragmentos.FragmentMarca
import com.redsystemstudio.chat_kotlin.Modelos.Marca
import com.redsystemstudio.chat_kotlin.R

class AdaptadorMarca(
    private val fragment: FragmentMarca,
    private val listaMarcas: List<Marca>
) : RecyclerView.Adapter<AdaptadorMarca.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_marca, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = listaMarcas.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val marca = listaMarcas[position]
        
        holder.apply {
            nombreMarca.text = marca.nombreMarca
            
            editarButton.setOnClickListener {
                fragment.mostrarDialogoEditarMarca(marca)
            }

            eliminarButton.setOnClickListener {
                fragment.eliminarMarca(marca)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreMarca: TextView = itemView.findViewById(R.id.texto_marca)
        val editarButton: ImageButton = itemView.findViewById(R.id.editar_button)
        val eliminarButton: ImageButton = itemView.findViewById(R.id.eliminar_button)
    }
}
package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import com.redsystemstudio.chat_kotlin.R

class AdaptadorModelosGrid(
    private var productos: List<ProductoNegocio1>,
    private val onModeloClick: (String) -> Unit
) : RecyclerView.Adapter<AdaptadorModelosGrid.ModeloViewHolder>() {

    private val modelosUnicos = mutableSetOf<Pair<String, String>>()

    init {
        actualizarModelosUnicos()
    }

    class ModeloViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreModelo: TextView = view.findViewById(R.id.tvNombreModelo)
        val tvCodigoModelo: TextView = view.findViewById(R.id.tvCodigoModelo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModeloViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_modelo_grid, parent, false)
        return ModeloViewHolder(view)
    }

    override fun onBindViewHolder(holder: ModeloViewHolder, position: Int) {
        val modelo = modelosUnicos.elementAt(position)
        holder.tvNombreModelo.text = modelo.first // nombre del modelo
        holder.tvCodigoModelo.text = "Código: ${modelo.second}" // código del modelo
        holder.itemView.setOnClickListener { onModeloClick(modelo.first) }
    }

    override fun getItemCount() = modelosUnicos.size

    fun actualizarProductos(nuevosProductos: List<ProductoNegocio1>) {
        productos = nuevosProductos
        actualizarModelosUnicos()
        notifyDataSetChanged()
    }

    private fun actualizarModelosUnicos() {
        modelosUnicos.clear()
        productos.forEach { producto ->
            modelosUnicos.add(Pair(producto.modelo, producto.codigoModelo))
        }
    }
}

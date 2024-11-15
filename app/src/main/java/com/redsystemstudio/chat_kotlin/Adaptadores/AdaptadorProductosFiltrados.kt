package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import com.redsystemstudio.chat_kotlin.R

class AdaptadorProductosFiltrados(
    private var productos: List<ProductoNegocio1>,
    private val onEditClick: (ProductoNegocio1) -> Unit,
    private val onDeleteClick: (ProductoNegocio1) -> Unit
) : RecyclerView.Adapter<AdaptadorProductosFiltrados.ProductoViewHolder>() {

    private var productosFiltrados = productos.toList()

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreProducto: TextView = view.findViewById(R.id.tvNombreProducto)
        val tvCodigo: TextView = view.findViewById(R.id.tvCodigo)
        val tvColor: TextView = view.findViewById(R.id.tvColor)
        val tvStock: TextView = view.findViewById(R.id.tvStock)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto_filtrado, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productosFiltrados[position]
        holder.tvNombreProducto.text = producto.nombre
        holder.tvCodigo.text = "Cod. ${producto.codigoBarras}"
        holder.tvColor.text = "Color: ${producto.color}"
        holder.tvStock.text = "Stock: ${producto.cantidad}"
        holder.tvPrecio.text = String.format("S/. %.2f", producto.precio)

        if (producto.cantidad <= 1) {
            holder.tvStock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        } else {
            holder.tvStock.setTextColor(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        holder.btnEditar.isClickable = true
        holder.btnEliminar.isClickable = true
        
        holder.btnEditar.setOnClickListener { onEditClick(producto) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(producto) }
    }

    override fun getItemCount() = productosFiltrados.size

    fun actualizarProductos(nuevosProductos: List<ProductoNegocio1>) {
        productos = nuevosProductos
        filtrarProductos("")
    }

    fun filtrarProductos(query: String) {
        productosFiltrados = if (query.isEmpty()) {
            productos
        } else {
            productos.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.codigoBarras.contains(query, ignoreCase = true) ||
                it.color.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }
} 
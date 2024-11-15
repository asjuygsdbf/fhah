package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio2
import com.redsystemstudio.chat_kotlin.R

class AdaptadorProductos(
    private var productos: List<ProductoNegocio2>,
    private val onEditClick: (ProductoNegocio2) -> Unit,
    private val onDeleteClick: (ProductoNegocio2) -> Unit
) : RecyclerView.Adapter<AdaptadorProductos.ProductoViewHolder>() {

    private var productosFiltrados = productos.toList()
    private var productosFull = productos.toList()

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreProducto: TextView = view.findViewById(R.id.tvNombreProducto)
        val codigo: TextView = view.findViewById(R.id.tvCodigo)
        val stock: TextView = view.findViewById(R.id.tvStock)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditar)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productosFiltrados[position]
        
        holder.nombreProducto.text = producto.nombre
        holder.codigo.text = "Cod. ${producto.codigo}"
        holder.stock.text = "Stock: ${producto.cantidad}"

        if (producto.cantidad <= 1) {
            holder.stock.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
        } else {
            holder.stock.setTextColor(holder.itemView.context.getColor(android.R.color.black))
        }

        holder.btnEditar.setOnClickListener { onEditClick(producto) }
        holder.btnEliminar.setOnClickListener { onDeleteClick(producto) }
    }

    override fun getItemCount() = productosFiltrados.size

    fun actualizarProductos(nuevosProductos: List<ProductoNegocio2>) {
        productosFull = nuevosProductos.toList()
        productosFiltrados = nuevosProductos.toList()
        notifyDataSetChanged()
    }

    fun filtrarProductos(query: String) {
        productosFiltrados = if (query.isEmpty()) {
            productosFull
        } else {
            productosFull.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.codigo.contains(query, ignoreCase = true) ||
                it.nombre.lowercase().contains(query.lowercase()) ||
                it.codigo.lowercase().contains(query.lowercase())
            }
        }
        notifyDataSetChanged()
    }
} 
package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.redsystemstudio.chat_kotlin.Modelos.Proveedor
import com.redsystemstudio.chat_kotlin.R

class AdaptadorProveedoresGrid(
    private var proveedores: List<Proveedor>,
    private val onProveedorClick: (Proveedor) -> Unit
) : RecyclerView.Adapter<AdaptadorProveedoresGrid.ProveedorViewHolder>() {

    class ProveedorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivLogo: ImageView = view.findViewById(R.id.ivLogoProveedor)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreProveedor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProveedorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_proveedor_grid, parent, false)
        return ProveedorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProveedorViewHolder, position: Int) {
        val proveedor = proveedores[position]
        
        holder.tvNombre.text = proveedor.nombreProveedor
        
        Glide.with(holder.itemView.context)
            .load(proveedor.imagen)
            .centerCrop()
            .into(holder.ivLogo)

        holder.itemView.setOnClickListener { onProveedorClick(proveedor) }
    }

    override fun getItemCount() = proveedores.size

    fun actualizarProveedores(nuevosProveedores: List<Proveedor>) {
        proveedores = nuevosProveedores
        notifyDataSetChanged()
    }
} 
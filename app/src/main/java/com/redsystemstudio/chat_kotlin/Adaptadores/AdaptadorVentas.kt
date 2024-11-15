package com.redsystemstudio.chat_kotlin.Adaptadores

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.redsystemstudio.chat_kotlin.Modelos.VentaNegocio2
import com.redsystemstudio.chat_kotlin.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdaptadorVentas(
    private var ventas: List<VentaNegocio2>,
    private val onVerDetalleClick: (VentaNegocio2) -> Unit
) : RecyclerView.Adapter<AdaptadorVentas.VentaViewHolder>() {

    private var ventasFiltradas = ventas.toList()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    class VentaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvCliente: TextView = view.findViewById(R.id.tvCliente)
        val tvMetodoPago: TextView = view.findViewById(R.id.tvMetodoPago)
        val tvTotal: TextView = view.findViewById(R.id.tvTotal)
        val btnVerDetalle: ImageButton = view.findViewById(R.id.btnVerDetalle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VentaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_venta, parent, false)
        return VentaViewHolder(view)
    }

    override fun onBindViewHolder(holder: VentaViewHolder, position: Int) {
        val venta = ventasFiltradas[position]
        
        holder.tvFecha.text = dateFormat.format(Date(venta.fecha))
        holder.tvCliente.text = "Cliente: ${venta.nombreCliente}"
        holder.tvMetodoPago.text = "Método: ${venta.metodoPago}"
        holder.tvTotal.text = String.format("S/. %.2f", venta.importeTotal)
        
        holder.btnVerDetalle.setOnClickListener { onVerDetalleClick(venta) }
    }

    override fun getItemCount() = ventasFiltradas.size

    fun actualizarVentas(nuevasVentas: List<VentaNegocio2>) {
        ventas = nuevasVentas
        filtrarVentas("")
    }

    fun filtrarVentas(query: String, fechaInicio: Long = 0, fechaFin: Long = Long.MAX_VALUE) {
        ventasFiltradas = ventas.filter { venta ->
            val coincideBusqueda = if (query.isEmpty()) {
                true
            } else {
                venta.nombreCliente.contains(query, ignoreCase = true) ||
                venta.dniCliente.contains(query, ignoreCase = true)
            }
            
            val coincideFecha = venta.fecha in fechaInicio..fechaFin
            
            coincideBusqueda && coincideFecha
        }.sortedByDescending { it.fecha } // Ordenar por fecha, más reciente primero
        
        notifyDataSetChanged()
    }

    fun obtenerTotalVentas(): Double {
        return ventasFiltradas.sumOf { it.importeTotal }
    }

    fun obtenerNumeroVentas(): Int {
        return ventasFiltradas.size
    }

    fun obtenerPromedioVenta(): Double {
        return if (ventasFiltradas.isEmpty()) 0.0
        else ventasFiltradas.sumOf { it.importeTotal } / ventasFiltradas.size
    }
} 
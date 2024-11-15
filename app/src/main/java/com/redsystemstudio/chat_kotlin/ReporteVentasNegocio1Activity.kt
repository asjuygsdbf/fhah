package com.redsystemstudio.chat_kotlin

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorVentasNegocio1
import com.redsystemstudio.chat_kotlin.Modelos.DetalleVentaNegocio1
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import com.redsystemstudio.chat_kotlin.Modelos.VentaNegocio1
import java.text.SimpleDateFormat
import java.util.*

class ReporteVentasNegocio1Activity : AppCompatActivity() {

    private lateinit var rvVentas: RecyclerView
    private lateinit var etBuscar: EditText
    private lateinit var btnFechaInicio: Button
    private lateinit var btnFechaFin: Button
    private lateinit var tvTotalVentas: TextView
    private lateinit var tvNumeroVentas: TextView
    private lateinit var tvPromedioVenta: TextView
    private lateinit var btnRegresar: Button
    private lateinit var adaptador: AdaptadorVentasNegocio1

    private val database = FirebaseDatabase.getInstance()
    private val ventasRef = database.getReference("Ventas_Negocio1")
    private val detallesVentaRef = database.getReference("DetallesVenta_Negocio1")
    private val productosRef = database.getReference("Productos_Negocio1")
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var fechaInicio: Long = 0
    private var fechaFin: Long = Long.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_ventas)

        initializeViews()
        setupRecyclerView()
        setupListeners()
        cargarVentas()
    }

    private fun initializeViews() {
        rvVentas = findViewById(R.id.rvVentas)
        etBuscar = findViewById(R.id.etBuscar)
        btnFechaInicio = findViewById(R.id.btnFechaInicio)
        btnFechaFin = findViewById(R.id.btnFechaFin)
        tvTotalVentas = findViewById(R.id.tvTotalVentas)
        tvNumeroVentas = findViewById(R.id.tvNumeroVentas)
        tvPromedioVenta = findViewById(R.id.tvPromedioVenta)
        btnRegresar = findViewById(R.id.btnRegresar)

        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorVentasNegocio1(emptyList()) { venta ->
            mostrarDetalleVenta(venta)
        }
        rvVentas.layoutManager = LinearLayoutManager(this)
        rvVentas.adapter = adaptador
    }

    private fun setupListeners() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adaptador.filtrarVentas(s.toString(), fechaInicio, fechaFin)
                actualizarResumen()
            }
        })

        btnFechaInicio.setOnClickListener { mostrarSelectorFecha(true) }
        btnFechaFin.setOnClickListener { mostrarSelectorFecha(false) }
    }

    private fun mostrarSelectorFecha(esInicio: Boolean) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                if (esInicio) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    fechaInicio = calendar.timeInMillis
                    btnFechaInicio.text = dateFormat.format(calendar.time)
                } else {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    fechaFin = calendar.timeInMillis
                    btnFechaFin.text = dateFormat.format(calendar.time)
                }
                adaptador.filtrarVentas(etBuscar.text.toString(), fechaInicio, fechaFin)
                actualizarResumen()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun cargarVentas() {
        ventasRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val ventas = mutableListOf<VentaNegocio1>()
                for (ventaSnapshot in snapshot.children) {
                    val venta = ventaSnapshot.getValue(VentaNegocio1::class.java)
                    venta?.let { ventas.add(it) }
                }
                adaptador.actualizarVentas(ventas)
                actualizarResumen()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReporteVentasNegocio1Activity,
                    "Error al cargar ventas: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarResumen() {
        tvTotalVentas.text = String.format("S/. %.2f", adaptador.obtenerTotalVentas())
        tvNumeroVentas.text = adaptador.obtenerNumeroVentas().toString()
        tvPromedioVenta.text = String.format("S/. %.2f", adaptador.obtenerPromedioVenta())
    }

    private fun mostrarDetalleVenta(venta: VentaNegocio1) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialogo_detalle_venta, null)
        
        val tvClienteDetalle = dialogView.findViewById<TextView>(R.id.tvClienteDetalle)
        val tvFechaDetalle = dialogView.findViewById<TextView>(R.id.tvFechaDetalle)
        val tableDetalles = dialogView.findViewById<TableLayout>(R.id.tableDetalles)
        val tvTotalDetalle = dialogView.findViewById<TextView>(R.id.tvTotalDetalle)
        val btnDeshacerVenta = dialogView.findViewById<Button>(R.id.btnDeshacerVenta)
        val btnEliminarVenta = dialogView.findViewById<Button>(R.id.btnEliminarVenta)

        tvClienteDetalle.text = "Cliente: ${venta.nombreCliente}"
        tvFechaDetalle.text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(Date(venta.fecha))

        // Lista para almacenar los detalles de la venta
        val detallesVenta = mutableListOf<DetalleVentaNegocio1>()

        // Cargar detalles de la venta
        detallesVentaRef.orderByChild("idVenta").equalTo(venta.idVenta)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (detalleSnapshot in snapshot.children) {
                        val detalle = detalleSnapshot.getValue(DetalleVentaNegocio1::class.java)
                        detalle?.let {
                            detallesVenta.add(it)
                            agregarFilaDetalle(tableDetalles, it)
                        }
                    }
                    tvTotalDetalle.text = String.format("Total: S/. %.2f", venta.importeTotal)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReporteVentasNegocio1Activity,
                        "Error al cargar detalles: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Configurar botones
        btnDeshacerVenta.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Deshacer Venta")
                .setMessage("¿Está seguro que desea deshacer esta venta? Se restaurará el stock de los productos.")
                .setPositiveButton("Sí") { _, _ ->
                    deshacerVenta(venta, detallesVenta)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        btnEliminarVenta.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Eliminar Venta")
                .setMessage("¿Está seguro que desea eliminar esta venta? Esta acción no se puede deshacer.")
                .setPositiveButton("Sí") { _, _ ->
                    eliminarVenta(venta, detallesVenta)
                    dialog.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }

        dialogView.findViewById<Button>(R.id.btnCerrar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun deshacerVenta(venta: VentaNegocio1, detalles: List<DetalleVentaNegocio1>) {
        val actualizaciones = HashMap<String, Any>()

        // Restaurar el stock de cada producto
        var productosActualizados = 0
        detalles.forEach { detalle ->
            productosRef.child(detalle.idProducto).get().addOnSuccessListener { snapshot ->
                val producto = snapshot.getValue(ProductoNegocio1::class.java)
                producto?.let {
                    val nuevoStock = it.cantidad + detalle.cantidad
                    actualizaciones["/Productos_Negocio1/${detalle.idProducto}/cantidad"] = nuevoStock
                }
                
                productosActualizados++
                if (productosActualizados == detalles.size) {
                    // Una vez que tenemos todos los productos actualizados, procedemos a eliminar la venta
                    eliminarVenta(venta, detalles, actualizaciones)
                }
            }
        }
    }

    private fun eliminarVenta(
        venta: VentaNegocio1, 
        detalles: List<DetalleVentaNegocio1>,
        actualizacionesStock: HashMap<String, Any>? = null
    ) {
        // Si hay actualizaciones de stock, las incluimos en la transacción
        val actualizaciones = actualizacionesStock ?: HashMap()

        // Eliminar los detalles
        detalles.forEach { detalle ->
            actualizaciones["/DetallesVenta_Negocio1/${detalle.idDetalle}"] = HashMap<String, Any>()
        }

        // Eliminar la venta
        actualizaciones["/Ventas_Negocio1/${venta.idVenta}"] = HashMap<String, Any>()

        // Realizar todas las operaciones en una sola transacción
        database.reference.updateChildren(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(this, 
                    if (actualizacionesStock != null) 
                        "Venta deshecha exitosamente" 
                    else 
                        "Venta eliminada exitosamente", 
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, 
                    if (actualizacionesStock != null)
                        "Error al deshacer venta: ${e.message}"
                    else
                        "Error al eliminar venta: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun agregarFilaDetalle(tableDetalles: TableLayout, detalle: DetalleVentaNegocio1) {
        productosRef.child(detalle.idProducto)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val producto = snapshot.getValue(ProductoNegocio1::class.java)
                    producto?.let {
                        val row = TableRow(this@ReporteVentasNegocio1Activity).apply {
                            layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.MATCH_PARENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            setPadding(12, 12, 12, 12)
                            setBackgroundColor(resources.getColor(android.R.color.white, theme))
                        }

                        // Producto (2 partes de peso)
                        row.addView(TextView(this@ReporteVentasNegocio1Activity).apply {
                            text = it.nombre
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
                            gravity = android.view.Gravity.START
                            setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                        })

                        // Cantidad (1 parte de peso)
                        row.addView(TextView(this@ReporteVentasNegocio1Activity).apply {
                            text = detalle.cantidad.toString()
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = android.view.Gravity.CENTER
                            setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                        })

                        // Precio (1 parte de peso)
                        row.addView(TextView(this@ReporteVentasNegocio1Activity).apply {
                            text = String.format("%.2f", detalle.precioVenta)
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = android.view.Gravity.END
                            setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                        })

                        // Total (1 parte de peso)
                        row.addView(TextView(this@ReporteVentasNegocio1Activity).apply {
                            text = String.format("%.2f", detalle.total)
                            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                            gravity = android.view.Gravity.END
                            setTextColor(resources.getColor(android.R.color.darker_gray, theme))
                        })

                        tableDetalles.addView(row)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ReporteVentasNegocio1Activity,
                        "Error al cargar producto: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
} 
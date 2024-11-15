package com.redsystemstudio.chat_kotlin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio2
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.ImageButton
import com.redsystemstudio.chat_kotlin.Modelos.VentaNegocio2
import com.redsystemstudio.chat_kotlin.Modelos.DetalleVentaNegocio2

class RegistroVentaActivity : AppCompatActivity() {

    private lateinit var spinnerCodigo: Spinner
    private lateinit var etCantidad: EditText
    private lateinit var etPrecio: EditText
    private lateinit var etNombres: EditText
    private lateinit var etDNI: EditText
    private lateinit var etMetodo: EditText
    private lateinit var btnAgregar: Button
    private lateinit var btnRegresar: Button
    private lateinit var btnAceptar: Button
    private lateinit var tableProductos: TableLayout
    private lateinit var tvCantidadDisponible: TextView
    private lateinit var codigosAdapter: ArrayAdapter<String>
    
    private val listaProductos = mutableListOf<ProductoNegocio2>()
    private val database = FirebaseDatabase.getInstance()
    private val productosRef = database.getReference("Productos_Negocio2")
    private val ID_NEGOCIO_2 = "negocio2"
    private val ventasRef = database.getReference("Ventas_Negocio2")
    private val detallesVentaRef = database.getReference("DetallesVenta_Negocio2")
    private val productosEnVenta = mutableListOf<DetalleVentaNegocio2>()
    private var importeTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_venta)

        initializeViews()
        setupSpinner()
        setupListeners()
        cargarProductos()
    }

    private fun initializeViews() {
        spinnerCodigo = findViewById(R.id.spinnerCodigo)
        etCantidad = findViewById(R.id.etCantidad)
        etPrecio = findViewById(R.id.etPrecio)
        etNombres = findViewById(R.id.etNombres)
        etDNI = findViewById(R.id.etDNI)
        etMetodo = findViewById(R.id.etMetodo)
        btnAgregar = findViewById(R.id.btnAgregar)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnAceptar = findViewById(R.id.btnAceptar)
        tableProductos = findViewById(R.id.tableProductos)
        tvCantidadDisponible = findViewById(R.id.tvCantidadDisponible)
    }

    private fun setupSpinner() {
        codigosAdapter = ArrayAdapter(this, R.layout.spinner_item_layout, mutableListOf<String>())
        codigosAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCodigo.adapter = codigosAdapter

        spinnerCodigo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < listaProductos.size) {
                    val productoSeleccionado = listaProductos[position]
                    etPrecio.setText(productoSeleccionado.precioVenta.toString())
                    tvCantidadDisponible.text = "Cantidad disponible: ${productoSeleccionado.cantidad}"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                etPrecio.text.clear()
                tvCantidadDisponible.text = "Cantidad disponible: 0"
            }
        }
    }

    private fun cargarProductos() {
        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ProductoNegocio2::class.java)
                    producto?.let { listaProductos.add(it) }
                }
                
                // Ordenar productos por código
                listaProductos.sortBy { it.codigo }
                
                // Actualizar el adapter con el formato requerido
                val productosFormateados = listaProductos.map { 
                    "${it.codigo} - ${it.nombre}"
                }
                
                codigosAdapter.clear()
                codigosAdapter.addAll(productosFormateados)
                codigosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroVentaActivity,
                    "Error al cargar productos: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener {
            finish()
        }

        btnAgregar.setOnClickListener {
            agregarProductoATabla()
        }

        btnAceptar.setOnClickListener {
            registrarVenta()
        }
    }

    private fun agregarProductoATabla() {
        val cantidad = etCantidad.text.toString().toIntOrNull()
        val precio = etPrecio.text.toString().toDoubleOrNull()
        val productoSeleccionado = getProductoSeleccionado()

        if (cantidad == null || precio == null || productoSeleccionado == null) {
            Toast.makeText(this, "Por favor complete todos los campos correctamente", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el producto ya está en la tabla
        if (productoYaAgregado(productoSeleccionado)) {
            Toast.makeText(this, "Este producto ya ha sido agregado", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que la cantidad no supere el stock disponible
        if (cantidad > productoSeleccionado.cantidad) {
            Toast.makeText(this, 
                "La cantidad no puede superar el stock disponible (${productoSeleccionado.cantidad})", 
                Toast.LENGTH_SHORT).show()
            return
        }

        val total = cantidad * precio
        importeTotal += total

        val detalle = DetalleVentaNegocio2(
            idDetalle = "",
            idVenta = "",
            idProducto = productoSeleccionado.idProducto,
            cantidad = cantidad,
            precioVenta = precio,
            total = total
        )
        productosEnVenta.add(detalle)

        val tableRow = TableRow(this).apply {
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 8)
            tag = productoSeleccionado.idProducto
        }

        // Agregar las columnas
        val nombreProducto = TextView(this).apply {
            text = productoSeleccionado.nombre
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val cantidadView = TextView(this).apply {
            text = cantidad.toString()
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val precioView = TextView(this).apply {
            text = String.format("%.2f", precio)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val totalView = TextView(this).apply {
            text = String.format("%.2f", total)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Modificar el botón eliminar
        val btnEliminar = ImageButton(this).apply {
            setImageResource(android.R.drawable.ic_delete)
            background = null
            layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_VERTICAL
                marginStart = 8
            }
            setOnClickListener {
                // Eliminar el detalle de la lista
                val detalleAEliminar = productosEnVenta.find { 
                    it.idProducto == productoSeleccionado.idProducto 
                }
                detalleAEliminar?.let {
                    productosEnVenta.remove(it)
                    importeTotal -= it.total  // Restar el total del producto eliminado
                }
                tableProductos.removeView(tableRow)
            }
        }

        // Agregar las vistas a la fila
        tableRow.addView(nombreProducto)
        tableRow.addView(cantidadView)
        tableRow.addView(precioView)
        tableRow.addView(totalView)
        tableRow.addView(btnEliminar)

        // Ajustar el layout de la fila para centrar todo verticalmente
        tableRow.gravity = android.view.Gravity.CENTER_VERTICAL

        // Agregar la fila a la tabla
        tableProductos.addView(tableRow)

        // Limpiar los campos
        etCantidad.text.clear()
        spinnerCodigo.setSelection(0)
    }

    private fun productoYaAgregado(producto: ProductoNegocio2): Boolean {
        for (i in 0 until tableProductos.childCount) {
            val row = tableProductos.getChildAt(i)
            if (row is TableRow && row.tag == producto.idProducto) {
                return true
            }
        }
        return false
    }

    private fun getProductoSeleccionado(): ProductoNegocio2? {
        val position = spinnerCodigo.selectedItemPosition
        return if (position >= 0 && position < listaProductos.size) {
            listaProductos[position]
        } else null
    }

    private fun registrarVenta() {
        val nombres = etNombres.text.toString().trim()
        val dni = etDNI.text.toString().trim()
        val metodo = etMetodo.text.toString().trim()

        if (nombres.isEmpty() || dni.isEmpty() || metodo.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos del cliente", Toast.LENGTH_SHORT).show()
            return
        }

        if (productosEnVenta.isEmpty()) {
            Toast.makeText(this, "Agregue al menos un producto", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear la venta
        val ventaKey = ventasRef.push().key ?: return
        val venta = VentaNegocio2(
            idVenta = ventaKey,
            idNegocio = ID_NEGOCIO_2,
            nombreCliente = nombres,
            dniCliente = dni,
            metodoPago = metodo,
            importeTotal = importeTotal
        )

        // Guardar la venta y sus detalles
        val actualizaciones = HashMap<String, Any>()
        actualizaciones["/Ventas_Negocio2/$ventaKey"] = venta

        // Preparar los detalles con sus IDs
        productosEnVenta.forEach { detalle ->
            val detalleKey = detallesVentaRef.push().key ?: return
            detalle.idDetalle = detalleKey
            detalle.idVenta = ventaKey
            actualizaciones["/DetallesVenta_Negocio2/$detalleKey"] = detalle

            // Actualizar el stock del producto
            actualizaciones["/Productos_Negocio2/${detalle.idProducto}/cantidad"] = 
                getProductoById(detalle.idProducto)?.cantidad?.minus(detalle.cantidad) ?: 0
        }

        // Realizar todas las operaciones en una sola transacción
        database.reference.updateChildren(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(this, "Venta registrada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al registrar la venta: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun getProductoById(idProducto: String): ProductoNegocio2? {
        return listaProductos.find { it.idProducto == idProducto }
    }
} 
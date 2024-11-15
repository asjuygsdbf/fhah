package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import com.redsystemstudio.chat_kotlin.Modelos.VentaNegocio1
import com.redsystemstudio.chat_kotlin.Modelos.DetalleVentaNegocio1

class RegistroVentaNegocio1Activity : AppCompatActivity() {

    private lateinit var spinnerCodigo: Spinner
    private lateinit var btnEscanear: Button
    private lateinit var tvTalla: TextView
    private lateinit var etCantidad: EditText
    private lateinit var tvCantidadDisponible: TextView
    private lateinit var etPrecio: EditText
    private lateinit var btnAgregar: Button
    private lateinit var etNombres: EditText
    private lateinit var etDNI: EditText
    private lateinit var etMetodo: EditText
    private lateinit var btnAceptar: Button
    private lateinit var btnRegresar: Button
    private lateinit var tableProductos: TableLayout
    private lateinit var codigosAdapter: ArrayAdapter<String>
    private val listaProductos = mutableListOf<ProductoNegocio1>()

    private val database = FirebaseDatabase.getInstance()
    private val productosRef = database.getReference("Productos_Negocio1")
    private val ventasRef = database.getReference("Ventas_Negocio1")
    private val detallesVentaRef = database.getReference("DetallesVenta_Negocio1")
    private val ID_NEGOCIO_1 = "negocio1"
    private val SCANNER_REQUEST_CODE = 100
    private var productoSeleccionado: ProductoNegocio1? = null
    private val productosEnVenta = mutableListOf<DetalleVentaNegocio1>()
    private var importeTotal = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_venta_negocio1)

        initializeViews()
        setupListeners()
    }

    private fun initializeViews() {
        spinnerCodigo = findViewById(R.id.spinnerCodigo)
        btnEscanear = findViewById(R.id.btnEscanear)
        tvTalla = findViewById(R.id.tvTalla)
        etCantidad = findViewById(R.id.etCantidad)
        tvCantidadDisponible = findViewById(R.id.tvCantidadDisponible)
        etPrecio = findViewById(R.id.etPrecio)
        btnAgregar = findViewById(R.id.btnAgregar)
        etNombres = findViewById(R.id.etNombres)
        etDNI = findViewById(R.id.etDNI)
        etMetodo = findViewById(R.id.etMetodo)
        btnAceptar = findViewById(R.id.btnAceptar)
        btnRegresar = findViewById(R.id.btnRegresar)
        tableProductos = findViewById(R.id.tableProductos)
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener { finish() }
        btnEscanear.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivityForResult(intent, SCANNER_REQUEST_CODE)
        }
        btnAgregar.setOnClickListener { agregarProductoATabla() }
        btnAceptar.setOnClickListener { registrarVenta() }

        // Configurar el spinner de códigos
        codigosAdapter = ArrayAdapter(this, R.layout.spinner_item_layout, mutableListOf<String>())
        codigosAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerCodigo.adapter = codigosAdapter

        // Cargar productos para el spinner
        productosRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listaProductos.clear()
                for (productoSnapshot in snapshot.children) {
                    val producto = productoSnapshot.getValue(ProductoNegocio1::class.java)
                    producto?.let { listaProductos.add(it) }
                }
                
                // Ordenar productos por código
                listaProductos.sortBy { it.codigoBarras }
                
                // Actualizar el adapter con el formato requerido
                val productosFormateados = listaProductos.map { 
                    "${it.codigoBarras} - ${it.nombre}"
                }
                
                codigosAdapter.clear()
                codigosAdapter.addAll(productosFormateados)
                codigosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroVentaNegocio1Activity,
                    "Error al cargar productos: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })

        spinnerCodigo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < listaProductos.size) {
                    productoSeleccionado = listaProductos[position]
                    mostrarDatosProducto(productoSeleccionado!!)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                limpiarDatosProducto()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCANNER_REQUEST_CODE && resultCode == RESULT_OK) {
            val codigoBarras = data?.getStringExtra("SCAN_RESULT")
            codigoBarras?.let {
                // Buscar el índice del producto con ese código de barras
                val index = listaProductos.indexOfFirst { producto -> 
                    producto.codigoBarras == codigoBarras 
                }
                if (index >= 0) {
                    spinnerCodigo.setSelection(index)
                } else {
                    Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun mostrarDatosProducto(producto: ProductoNegocio1) {
        tvTalla.text = "Talla: Cargando..."
        tvCantidadDisponible.text = "Cantidad disponible: ${producto.cantidad}"
        etPrecio.setText(producto.precio.toString())
        productoSeleccionado = producto

        // Cargar nombre de la talla
        database.getReference("Tallas").child(producto.idTalla)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombreTalla = snapshot.child("nombreTalla").getValue(String::class.java)
                    tvTalla.text = "Talla: ${nombreTalla ?: "No disponible"}"
                }

                override fun onCancelled(error: DatabaseError) {
                    tvTalla.text = "Talla: Error al cargar"
                }
            })
    }

    private fun limpiarDatosProducto() {
        productoSeleccionado = null
        tvTalla.text = "Talla: "
        tvCantidadDisponible.text = "Cantidad disponible: 0"
        etPrecio.text.clear()
        etCantidad.text.clear()
    }

    private fun agregarProductoATabla() {
        val cantidad = etCantidad.text.toString().toIntOrNull()
        val precio = etPrecio.text.toString().toDoubleOrNull()

        if (cantidad == null || precio == null || productoSeleccionado == null) {
            Toast.makeText(this, "Por favor complete todos los campos correctamente", 
                Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar si el producto ya está en la tabla
        if (productoYaAgregado(productoSeleccionado!!)) {
            Toast.makeText(this, "Este producto ya ha sido agregado", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que la cantidad no supere el stock disponible
        if (cantidad > productoSeleccionado!!.cantidad) {
            Toast.makeText(this, 
                "La cantidad no puede superar el stock disponible (${productoSeleccionado!!.cantidad})", 
                Toast.LENGTH_SHORT).show()
            return
        }

        val total = cantidad * precio
        importeTotal += total

        val detalle = DetalleVentaNegocio1(
            idDetalle = "",
            idVenta = "",
            idProducto = productoSeleccionado!!.idProducto,
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
            tag = productoSeleccionado!!.idProducto
        }

        // Agregar las columnas
        val nombreProducto = TextView(this).apply {
            text = productoSeleccionado!!.nombre
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tallaView = TextView(this).apply {
            text = tvTalla.text.toString().replace("Talla: ", "")
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
            
            // Guardamos el ID del producto para usarlo al eliminar
            val idProductoActual = productoSeleccionado!!.idProducto
            val totalActual = total

            setOnClickListener {
                try {
                    val detalleAEliminar = productosEnVenta.find { 
                        it.idProducto == idProductoActual 
                    }
                    detalleAEliminar?.let {
                        productosEnVenta.remove(it)
                        importeTotal -= totalActual
                    }
                    tableProductos.removeView(tableRow)
                } catch (e: Exception) {
                    Toast.makeText(this@RegistroVentaNegocio1Activity,
                        "Error al eliminar fila: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Agregar las vistas a la fila
        tableRow.addView(nombreProducto)
        tableRow.addView(tallaView)
        tableRow.addView(cantidadView)
        tableRow.addView(precioView)
        tableRow.addView(totalView)
        tableRow.addView(btnEliminar)

        // Ajustar el layout de la fila para centrar todo verticalmente
        tableRow.gravity = android.view.Gravity.CENTER_VERTICAL

        // Agregar la fila a la tabla
        tableProductos.addView(tableRow)

        // Limpiar los campos
        limpiarDatosProducto()
    }

    private fun productoYaAgregado(producto: ProductoNegocio1): Boolean {
        for (i in 0 until tableProductos.childCount) {
            val row = tableProductos.getChildAt(i)
            if (row is TableRow && row.tag == producto.idProducto) {
                return true
            }
        }
        return false
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
        val venta = VentaNegocio1(
            idVenta = ventaKey,
            idNegocio = ID_NEGOCIO_1,
            nombreCliente = nombres,
            dniCliente = dni,
            metodoPago = metodo,
            importeTotal = importeTotal
        )

        // Guardar la venta y sus detalles
        val actualizaciones = HashMap<String, Any>()
        actualizaciones["/Ventas_Negocio1/$ventaKey"] = venta

        // Preparar los detalles con sus IDs y actualizar stock
        productosEnVenta.forEach { detalle ->
            val detalleKey = detallesVentaRef.push().key ?: return
            detalle.idDetalle = detalleKey
            detalle.idVenta = ventaKey
            actualizaciones["/DetallesVenta_Negocio1/$detalleKey"] = detalle

            // Actualizar el stock del producto
            actualizaciones["/Productos_Negocio1/${detalle.idProducto}/cantidad"] = 
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

    private fun getProductoById(idProducto: String): ProductoNegocio1? {
        return productoSeleccionado?.takeIf { it.idProducto == idProducto }
    }

    private fun mostrarDialogoEliminarProducto(producto: ProductoNegocio1) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Está seguro que desea eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto(producto: ProductoNegocio1) {
        productosRef.child(producto.idProducto).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Producto eliminado exitosamente", 
                    Toast.LENGTH_SHORT).show()
                // La lista se actualizará automáticamente por el listener
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar producto: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
}
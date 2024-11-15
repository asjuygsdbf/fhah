package com.redsystemstudio.chat_kotlin

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Modelos.Marca
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio2

class RegistroProductoActivity : AppCompatActivity() {
    
    private lateinit var spinnerMarca: Spinner
    private lateinit var btnAgregarMarca: ImageButton
    private lateinit var etCodigo: EditText
    private lateinit var etNombre: EditText
    private lateinit var etCantidad: EditText
    private lateinit var etCosto: EditText
    private lateinit var etPrecio: EditText
    private lateinit var btnAceptar: Button
    private lateinit var btnRegresar: Button
    
    private val marcasList = mutableListOf<Marca>()
    private lateinit var marcasAdapter: ArrayAdapter<String>
    private val database = FirebaseDatabase.getInstance()
    private val marcasRef = database.getReference("Marcas")
    private val ID_NEGOCIO_2 = "negocio2" // Identificador fijo para el segundo negocio
    private val productosRef = database.getReference("Productos_Negocio2")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_producto)

        initializeViews()
        setupSpinner()
        setupListeners()
        cargarMarcas()
    }

    private fun initializeViews() {
        spinnerMarca = findViewById(R.id.spinnerMarca)
        btnAgregarMarca = findViewById(R.id.btnAgregarMarca)
        etCodigo = findViewById(R.id.etCodigo)
        etNombre = findViewById(R.id.etNombre)
        etCantidad = findViewById(R.id.etCantidad)
        etCosto = findViewById(R.id.etCosto)
        etPrecio = findViewById(R.id.etPrecio)
        btnAceptar = findViewById(R.id.btnAceptar)
        btnRegresar = findViewById(R.id.btnRegresar)
    }

    private fun setupSpinner() {
        marcasAdapter = ArrayAdapter(this, R.layout.spinner_item_layout, 
            marcasList.map { it.nombreMarca })
        marcasAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMarca.adapter = marcasAdapter

        spinnerMarca.setOnLongClickListener {
            val selectedPosition = spinnerMarca.selectedItemPosition
            if (selectedPosition >= 0 && marcasList.isNotEmpty()) {
                mostrarDialogoEliminarMarca(marcasList[selectedPosition])
            }
            true
        }
    }

    private fun setupListeners() {
        btnAgregarMarca.setOnClickListener {
            mostrarDialogoAgregarMarca()
        }

        spinnerMarca.setOnLongClickListener {
            val selectedPosition = spinnerMarca.selectedItemPosition
            if (selectedPosition >= 0 && marcasList.isNotEmpty()) {
                val marca = marcasList[selectedPosition]
                AlertDialog.Builder(this)
                    .setTitle("Opciones de marca")
                    .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoEditarMarca(marca)
                            1 -> mostrarDialogoEliminarMarca(marca)
                        }
                    }
                    .show()
            }
            true
        }

        btnAceptar.setOnClickListener {
            validarYGuardarProducto()
        }

        btnRegresar.setOnClickListener {
            finish()
        }
    }

    private fun cargarMarcas() {
        marcasRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val marca = snapshot.getValue(Marca::class.java)
                if (marca != null) {
                    marcasList.add(marca)
                    actualizarSpinner()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val marcaActualizada = snapshot.getValue(Marca::class.java)
                if (marcaActualizada != null) {
                    val index = marcasList.indexOfFirst { it.idMarca == marcaActualizada.idMarca }
                    if (index != -1) {
                        marcasList[index] = marcaActualizada
                        actualizarSpinner()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val marcaEliminada = snapshot.getValue(Marca::class.java)
                if (marcaEliminada != null) {
                    marcasList.removeIf { it.idMarca == marcaEliminada.idMarca }
                    actualizarSpinner()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroProductoActivity, 
                    "Error al cargar marcas: ${error.message}", 
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarSpinner() {
        marcasList.sortBy { it.nombreMarca }
        marcasAdapter = ArrayAdapter(this, R.layout.spinner_item_layout, 
            marcasList.map { it.nombreMarca })
        marcasAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerMarca.adapter = marcasAdapter
    }

    private fun mostrarDialogoAgregarMarca() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Nombre de la marca"
        
        builder.setTitle("Agregar nueva marca")
            .setView(input)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreMarca = input.text.toString().trim()
                if (nombreMarca.isNotEmpty()) {
                    guardarMarca(nombreMarca)
                }
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }
        
        builder.show()
    }

    private fun guardarMarca(nombreMarca: String) {
        // Primero verificamos si ya existe una marca con ese nombre
        marcasRef.orderByChild("nombreMarca").equalTo(nombreMarca)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Ya existe una marca con ese nombre
                        Toast.makeText(this@RegistroProductoActivity, 
                            "Ya existe una marca con ese nombre", 
                            Toast.LENGTH_SHORT).show()
                    } else {
                        // No existe, podemos crear la nueva marca
                        crearNuevaMarca(nombreMarca)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoActivity,
                        "Error al verificar marca: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun crearNuevaMarca(nombreMarca: String) {
        val key = marcasRef.push().key ?: return
        val marca = Marca(key, nombreMarca)
        
        marcasRef.child(key).setValue(marca)
            .addOnSuccessListener {
                Toast.makeText(this, "Marca guardada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar marca: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEliminarMarca(marca: Marca) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Marca")
            .setMessage("¿Está seguro que desea eliminar la marca ${marca.nombreMarca}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarMarca(marca)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarMarca(marca: Marca) {
        // Verificar si la marca está siendo utilizada en productos
        // TODO: Agregar verificación cuando implementes productos
        
        marcasRef.child(marca.idMarca).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, 
                    "Marca eliminada exitosamente", 
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, 
                    "Error al eliminar marca: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditarMarca(marca: Marca) {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.setText(marca.nombreMarca)
        
        builder.setTitle("Editar marca")
            .setView(input)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = input.text.toString().trim()
                if (nuevoNombre.isNotEmpty() && nuevoNombre != marca.nombreMarca) {
                    verificarYActualizarMarca(marca, nuevoNombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarYActualizarMarca(marca: Marca, nuevoNombre: String) {
        marcasRef.orderByChild("nombreMarca").equalTo(nuevoNombre)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoActivity,
                            "Ya existe una marca con ese nombre",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        actualizarMarca(marca, nuevoNombre)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoActivity,
                        "Error al verificar marca: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarMarca(marca: Marca, nuevoNombre: String) {
        marca.nombreMarca = nuevoNombre
        marcasRef.child(marca.idMarca).setValue(marca)
            .addOnSuccessListener {
                Toast.makeText(this,
                    "Marca actualizada exitosamente",
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this,
                    "Error al actualizar marca: ${e.message}",
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun validarYGuardarProducto() {
        val codigo = etCodigo.text.toString().trim()
        val nombre = etNombre.text.toString().trim()
        val cantidadStr = etCantidad.text.toString().trim()
        val costoStr = etCosto.text.toString().trim()
        val precioStr = etPrecio.text.toString().trim()
        
        if (spinnerMarca.selectedItem == null) {
            Toast.makeText(this, "Debe seleccionar una marca", Toast.LENGTH_SHORT).show()
            return
        }

        if (codigo.isEmpty() || nombre.isEmpty() || cantidadStr.isEmpty() || 
            costoStr.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val cantidad = cantidadStr.toInt()
            val costo = costoStr.toDouble()
            val precio = precioStr.toDouble()

            if (cantidad <= 0 || costo <= 0 || precio <= 0) {
                Toast.makeText(this, "Los valores numéricos deben ser mayores a 0", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener la marca seleccionada
            val marcaSeleccionada = marcasList[spinnerMarca.selectedItemPosition]

            // Verificar si ya existe un producto con el mismo código
            verificarCodigoYGuardar(codigo, nombre, cantidad, costo, precio, marcaSeleccionada.idMarca)

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Los valores numéricos no son válidos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarCodigoYGuardar(
        codigo: String, 
        nombre: String, 
        cantidad: Int, 
        costo: Double, 
        precio: Double,
        idMarca: String
    ) {
        productosRef.orderByChild("codigo").equalTo(codigo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoActivity,
                            "Ya existe un producto con ese código",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        guardarProducto(codigo, nombre, cantidad, costo, precio, idMarca)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoActivity,
                        "Error al verificar el código: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun guardarProducto(
        codigo: String, 
        nombre: String, 
        cantidad: Int, 
        costo: Double, 
        precio: Double,
        idMarca: String
    ) {
        val key = productosRef.push().key ?: return
        
        val producto = ProductoNegocio2(
            idProducto = key,
            idNegocio = ID_NEGOCIO_2,
            idMarca = idMarca,
            codigo = codigo,
            nombre = nombre,
            cantidad = cantidad,
            precioCompra = costo,
            precioVenta = precio
        )

        productosRef.child(key).setValue(producto)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto guardado exitosamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        etCodigo.text.clear()
        etNombre.text.clear()
        etCantidad.text.clear()
        etCosto.text.clear()
        etPrecio.text.clear()
        spinnerMarca.setSelection(0)
    }
} 
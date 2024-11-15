package com.redsystemstudio.chat_kotlin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorProductos
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio2
import com.redsystemstudio.chat_kotlin.Modelos.Marca
import android.widget.ArrayAdapter

class ListaProductosActivity : AppCompatActivity() {

    private lateinit var rvProductos: RecyclerView
    private lateinit var etBuscar: EditText
    private lateinit var btnRegresar: Button
    private lateinit var adaptador: AdaptadorProductos
    private lateinit var productosRef: DatabaseReference
    private lateinit var layoutAlertaStock: LinearLayout
    private val listaProductos = mutableListOf<ProductoNegocio2>()
    private val marcasRef = FirebaseDatabase.getInstance().getReference("Marcas")
    private val marcasList = mutableListOf<Marca>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_productos)

        initializeViews()
        setupRecyclerView()
        setupSearchListener()
        cargarProductos()
    }

    private fun initializeViews() {
        rvProductos = findViewById(R.id.rvProductos)
        etBuscar = findViewById(R.id.etBuscar)
        btnRegresar = findViewById(R.id.btnRegresar)
        layoutAlertaStock = findViewById(R.id.layoutAlertaStock)
        productosRef = FirebaseDatabase.getInstance().getReference("Productos_Negocio2")

        layoutAlertaStock.visibility = View.GONE

        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorProductos(
            productos = listaProductos,
            onEditClick = { producto -> mostrarDialogoEditar(producto) },
            onDeleteClick = { producto -> mostrarDialogoEliminar(producto) }
        )
        rvProductos.layoutManager = LinearLayoutManager(this)
        rvProductos.adapter = adaptador
    }

    private fun setupSearchListener() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Realizar la búsqueda mientras el usuario escribe
                adaptador.filtrarProductos(s?.toString() ?: "")
            }
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun verificarStock() {
        val hayProductosBajoStock = listaProductos.any { it.cantidad <= 1 }
        layoutAlertaStock.visibility = if (hayProductosBajoStock) View.VISIBLE else View.GONE
    }

    private fun cargarProductos() {
        productosRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val producto = snapshot.getValue(ProductoNegocio2::class.java)
                if (producto != null) {
                    listaProductos.add(producto)
                    listaProductos.sortBy { it.nombre }
                    adaptador.actualizarProductos(listaProductos)
                    verificarStock()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val productoActualizado = snapshot.getValue(ProductoNegocio2::class.java)
                if (productoActualizado != null) {
                    val index = listaProductos.indexOfFirst { it.idProducto == productoActualizado.idProducto }
                    if (index != -1) {
                        listaProductos[index] = productoActualizado
                        listaProductos.sortBy { it.nombre }
                        adaptador.actualizarProductos(listaProductos)
                        verificarStock()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val productoEliminado = snapshot.getValue(ProductoNegocio2::class.java)
                if (productoEliminado != null) {
                    listaProductos.removeIf { it.idProducto == productoEliminado.idProducto }
                    adaptador.actualizarProductos(listaProductos)
                    verificarStock()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListaProductosActivity,
                    "Error al cargar productos: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun mostrarDialogoEliminar(producto: ProductoNegocio2) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Producto")
            .setMessage("¿Está seguro que desea eliminar el producto ${producto.nombre}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProducto(producto)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProducto(producto: ProductoNegocio2) {
        productosRef.child(producto.idProducto).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Producto eliminado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditar(producto: ProductoNegocio2) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialogo_editar_producto, null)
        
        val spinnerMarca = dialogView.findViewById<Spinner>(R.id.spinnerMarca)
        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigo)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etStock = dialogView.findViewById<EditText>(R.id.etStock)

        // Cargar las marcas para el spinner
        marcasRef.get().addOnSuccessListener { snapshot ->
            marcasList.clear()
            snapshot.children.forEach { marcaSnapshot ->
                val marca = marcaSnapshot.getValue(Marca::class.java)
                marca?.let { marcasList.add(it) }
            }

            val marcasAdapter = ArrayAdapter(
                this,
                R.layout.spinner_item_layout,
                marcasList.map { it.nombreMarca }
            )
            marcasAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            spinnerMarca.adapter = marcasAdapter

            // Seleccionar la marca actual del producto
            val marcaIndex = marcasList.indexOfFirst { it.idMarca == producto.idMarca }
            if (marcaIndex != -1) {
                spinnerMarca.setSelection(marcaIndex)
            }
        }

        // Establecer valores actuales
        etCodigo.setText(producto.codigo)
        etNombre.setText(producto.nombre)
        etStock.setText(producto.cantidad.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nuevoCodigo = etCodigo.text.toString().trim()
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevoStock = etStock.text.toString().toIntOrNull()
                val marcaSeleccionada = marcasList.getOrNull(spinnerMarca.selectedItemPosition)

                if (nuevoCodigo.isEmpty() || nuevoNombre.isEmpty() || nuevoStock == null || marcaSeleccionada == null) {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (nuevoStock < 0) {
                    Toast.makeText(this, "El stock no puede ser negativo", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Verificar si el código ya existe (solo si cambió)
                if (nuevoCodigo != producto.codigo) {
                    verificarCodigoYActualizar(
                        dialog,
                        producto,
                        nuevoCodigo,
                        nuevoNombre,
                        nuevoStock,
                        marcaSeleccionada.idMarca
                    )
                } else {
                    actualizarProducto(
                        dialog,
                        producto,
                        nuevoCodigo,
                        nuevoNombre,
                        nuevoStock,
                        marcaSeleccionada.idMarca
                    )
                }
            }
        }

        dialog.show()
    }

    private fun verificarCodigoYActualizar(
        dialog: AlertDialog,
        producto: ProductoNegocio2,
        nuevoCodigo: String,
        nuevoNombre: String,
        nuevoStock: Int,
        nuevaMarcaId: String
    ) {
        productosRef.orderByChild("codigo").equalTo(nuevoCodigo)
            .get().addOnSuccessListener { snapshot ->
                if (snapshot.exists() && !snapshot.children.first().key.equals(producto.idProducto)) {
                    Toast.makeText(this, "Ya existe un producto con ese código", Toast.LENGTH_SHORT).show()
                } else {
                    actualizarProducto(dialog, producto, nuevoCodigo, nuevoNombre, nuevoStock, nuevaMarcaId)
                }
            }
    }

    private fun actualizarProducto(
        dialog: AlertDialog,
        producto: ProductoNegocio2,
        nuevoCodigo: String,
        nuevoNombre: String,
        nuevoStock: Int,
        nuevaMarcaId: String
    ) {
        val productoActualizado = producto.copy(
            codigo = nuevoCodigo,
            nombre = nuevoNombre,
            cantidad = nuevoStock,
            idMarca = nuevaMarcaId
        )

        productosRef.child(producto.idProducto).setValue(productoActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar producto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 
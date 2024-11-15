package com.redsystemstudio.chat_kotlin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorProductosFiltrados
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import android.widget.Toast
import android.view.View
import android.widget.LinearLayout

class ListaProductosFiltradosActivity : AppCompatActivity() {

    private lateinit var rvProductosFiltrados: RecyclerView
    private lateinit var btnRegresar: Button
    private lateinit var etBuscar: EditText
    private lateinit var tvInfoFiltros: TextView
    private lateinit var adaptador: AdaptadorProductosFiltrados
    private val productosRef = FirebaseDatabase.getInstance().getReference("Productos_Negocio1")
    private var idProveedor: String = ""
    private var nombreModelo: String = ""
    private var idTalla: String = ""
    private lateinit var layoutAlertaStock: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_productos_filtrados)

        idProveedor = intent.getStringExtra("ID_PROVEEDOR") ?: ""
        nombreModelo = intent.getStringExtra("NOMBRE_MODELO") ?: ""
        idTalla = intent.getStringExtra("ID_TALLA") ?: ""

        initializeViews()
        setupRecyclerView()
        setupSearchListener()
        cargarProductos()
    }

    private fun initializeViews() {
        rvProductosFiltrados = findViewById(R.id.rvProductosFiltrados)
        btnRegresar = findViewById(R.id.btnRegresar)
        etBuscar = findViewById(R.id.etBuscar)
        tvInfoFiltros = findViewById(R.id.tvInfoFiltros)
        layoutAlertaStock = findViewById(R.id.layoutAlertaStock)
        
        layoutAlertaStock.visibility = View.GONE
        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorProductosFiltrados(
            productos = emptyList(),
            onEditClick = { producto -> mostrarDialogoEditar(producto) },
            onDeleteClick = { producto -> mostrarDialogoEliminar(producto) }
        )
        rvProductosFiltrados.layoutManager = LinearLayoutManager(this)
        rvProductosFiltrados.adapter = adaptador
    }

    private fun setupSearchListener() {
        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                adaptador.filtrarProductos(s?.toString() ?: "")
            }
        })
    }

    private fun verificarStock(productos: List<ProductoNegocio1>) {
        val hayProductosBajoStock = productos.any { it.cantidad <= 1 }
        layoutAlertaStock.visibility = if (hayProductosBajoStock) View.VISIBLE else View.GONE
    }

    private fun cargarProductos() {
        productosRef
            .orderByChild("idProveedor")
            .equalTo(idProveedor)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productos = mutableListOf<ProductoNegocio1>()
                    for (productoSnapshot in snapshot.children) {
                        val producto = productoSnapshot.getValue(ProductoNegocio1::class.java)
                        if (producto != null && 
                            producto.modelo == nombreModelo && 
                            producto.idTalla == idTalla) {
                            productos.add(producto)
                        }
                    }
                    adaptador.actualizarProductos(productos)
                    actualizarInfoFiltros(productos.size)
                    verificarStock(productos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListaProductosFiltradosActivity,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarInfoFiltros(cantidad: Int) {
        tvInfoFiltros.text = "Se encontraron $cantidad productos"
    }

    private fun mostrarDialogoEditar(producto: ProductoNegocio1) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialogo_editar_producto_filtrado, null)

        val etNombre = dialogView.findViewById<EditText>(R.id.etNombre)
        val etCantidad = dialogView.findViewById<EditText>(R.id.etCantidad)
        val etCosto = dialogView.findViewById<EditText>(R.id.etCosto)
        val etPrecio = dialogView.findViewById<EditText>(R.id.etPrecio)

        etNombre.setText(producto.nombre)
        etCantidad.setText(producto.cantidad.toString())
        etCosto.setText(producto.costo.toString())
        etPrecio.setText(producto.precio.toString())

        AlertDialog.Builder(this)
            .setTitle("Editar Producto")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevaCantidad = etCantidad.text.toString().toIntOrNull()
                val nuevoCosto = etCosto.text.toString().toDoubleOrNull()
                val nuevoPrecio = etPrecio.text.toString().toDoubleOrNull()

                if (nuevoNombre.isEmpty() || nuevaCantidad == null || 
                    nuevoCosto == null || nuevoPrecio == null) {
                    Toast.makeText(this, "Todos los campos son obligatorios", 
                        Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                actualizarProducto(producto, nuevoNombre, nuevaCantidad, nuevoCosto, nuevoPrecio)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun actualizarProducto(
        producto: ProductoNegocio1,
        nuevoNombre: String,
        nuevaCantidad: Int,
        nuevoCosto: Double,
        nuevoPrecio: Double
    ) {
        val productoActualizado = producto.copy(
            nombre = nuevoNombre,
            cantidad = nuevaCantidad,
            costo = nuevoCosto,
            precio = nuevoPrecio
        )

        productosRef.child(producto.idProducto).setValue(productoActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto actualizado exitosamente", 
                    Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar producto: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEliminar(producto: ProductoNegocio1) {
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
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar producto: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
} 
package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorModelosGrid
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import android.widget.Toast

class ListaModelosActivity : AppCompatActivity() {

    private lateinit var rvModelos: RecyclerView
    private lateinit var btnRegresar: Button
    private lateinit var tvNombreProveedor: TextView
    private lateinit var adaptador: AdaptadorModelosGrid
    private val productosRef = FirebaseDatabase.getInstance().getReference("Productos_Negocio1")
    private var idProveedor: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_modelos)

        idProveedor = intent.getStringExtra("ID_PROVEEDOR") ?: ""
        val nombreProveedor = intent.getStringExtra("NOMBRE_PROVEEDOR") ?: ""

        initializeViews()
        setupRecyclerView()
        tvNombreProveedor.text = "Modelos de $nombreProveedor"
        cargarProductos()
    }

    private fun initializeViews() {
        rvModelos = findViewById(R.id.rvModelos)
        btnRegresar = findViewById(R.id.btnRegresar)
        tvNombreProveedor = findViewById(R.id.tvNombreProveedor)
        
        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorModelosGrid(emptyList()) { modelo ->
            val intent = Intent(this, ListaTallasModeloActivity::class.java).apply {
                putExtra("ID_PROVEEDOR", idProveedor)
                putExtra("NOMBRE_MODELO", modelo)
            }
            startActivity(intent)
        }
        
        rvModelos.layoutManager = GridLayoutManager(this, 2)
        rvModelos.adapter = adaptador
    }

    private fun cargarProductos() {
        productosRef.orderByChild("idProveedor").equalTo(idProveedor)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productos = mutableListOf<ProductoNegocio1>()
                    for (productoSnapshot in snapshot.children) {
                        val producto = productoSnapshot.getValue(ProductoNegocio1::class.java)
                        producto?.let { productos.add(it) }
                    }
                    adaptador.actualizarProductos(productos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListaModelosActivity,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
} 
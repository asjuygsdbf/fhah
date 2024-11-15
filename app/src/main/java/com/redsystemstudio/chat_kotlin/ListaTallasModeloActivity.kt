package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorTallasModeloGrid
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import android.widget.Toast

class ListaTallasModeloActivity : AppCompatActivity() {

    private lateinit var rvTallasModelo: RecyclerView
    private lateinit var btnRegresar: Button
    private lateinit var tvModeloInfo: TextView
    private lateinit var adaptador: AdaptadorTallasModeloGrid
    private val productosRef = FirebaseDatabase.getInstance().getReference("Productos_Negocio1")
    private var idProveedor: String = ""
    private var nombreModelo: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_tallas_modelo)

        idProveedor = intent.getStringExtra("ID_PROVEEDOR") ?: ""
        nombreModelo = intent.getStringExtra("NOMBRE_MODELO") ?: ""

        initializeViews()
        setupRecyclerView()
        tvModeloInfo.text = "Tallas del modelo $nombreModelo"
        cargarProductos()
    }

    private fun initializeViews() {
        rvTallasModelo = findViewById(R.id.rvTallasModelo)
        btnRegresar = findViewById(R.id.btnRegresar)
        tvModeloInfo = findViewById(R.id.tvModeloInfo)

        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorTallasModeloGrid(emptyList()) { producto ->
            val intent = Intent(this, ListaProductosFiltradosActivity::class.java).apply {
                putExtra("ID_PROVEEDOR", idProveedor)
                putExtra("NOMBRE_MODELO", nombreModelo)
                putExtra("ID_TALLA", producto.idTalla)
            }
            startActivity(intent)
        }

        rvTallasModelo.layoutManager = GridLayoutManager(this, 2)
        rvTallasModelo.adapter = adaptador
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
                        if (producto != null && producto.modelo == nombreModelo) {
                            productos.add(producto)
                        }
                    }
                    adaptador.actualizarProductos(productos)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ListaTallasModeloActivity,
                        "Error al cargar productos: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }
}

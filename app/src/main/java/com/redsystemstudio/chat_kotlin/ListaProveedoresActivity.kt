package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.redsystemstudio.chat_kotlin.Adaptadores.AdaptadorProveedoresGrid
import com.redsystemstudio.chat_kotlin.Modelos.Proveedor
import android.widget.Toast

class ListaProveedoresActivity : AppCompatActivity() {

    private lateinit var rvProveedores: RecyclerView
    private lateinit var btnRegresar: Button
    private lateinit var adaptador: AdaptadorProveedoresGrid
    private val proveedoresRef = FirebaseDatabase.getInstance().getReference("Proveedores")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_proveedores)

        initializeViews()
        setupRecyclerView()
        cargarProveedores()
    }

    private fun initializeViews() {
        rvProveedores = findViewById(R.id.rvProveedores)
        btnRegresar = findViewById(R.id.btnRegresar)
        
        btnRegresar.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adaptador = AdaptadorProveedoresGrid(emptyList()) { proveedor ->
            val intent = Intent(this, ListaModelosActivity::class.java).apply {
                putExtra("ID_PROVEEDOR", proveedor.idProveedor)
                putExtra("NOMBRE_PROVEEDOR", proveedor.nombreProveedor)
            }
            startActivity(intent)
        }
        
        rvProveedores.layoutManager = GridLayoutManager(this, 2)
        rvProveedores.adapter = adaptador
    }

    private fun cargarProveedores() {
        proveedoresRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val proveedores = mutableListOf<Proveedor>()
                for (proveedorSnapshot in snapshot.children) {
                    val proveedor = proveedorSnapshot.getValue(Proveedor::class.java)
                    proveedor?.let { proveedores.add(it) }
                }
                proveedores.sortBy { it.nombreProveedor }
                adaptador.actualizarProveedores(proveedores)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListaProveedoresActivity,
                    "Error al cargar proveedores: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }
} 
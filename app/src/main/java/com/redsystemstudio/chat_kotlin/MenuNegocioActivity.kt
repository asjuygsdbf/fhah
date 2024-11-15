package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuNegocioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_negocio)

        val btnRegistrarProducto = findViewById<Button>(R.id.btnRegistrarProducto)
        val btnVerProductos = findViewById<Button>(R.id.btnVerProductos)
        val btnRegistrarVenta = findViewById<Button>(R.id.btnRegistrarVenta)
        val btnReporteVentas = findViewById<Button>(R.id.btnReporteVentas)
        val btnRegresar = findViewById<Button>(R.id.btnRegresar)

        btnRegresar.setOnClickListener {
            finish()
        }

        btnRegistrarProducto.setOnClickListener {
            val intent = Intent(this, RegistroProductoActivity::class.java)
            startActivity(intent)
        }

        btnVerProductos.setOnClickListener {
            val intent = Intent(this, ListaProductosActivity::class.java)
            startActivity(intent)
        }

        btnRegistrarVenta.setOnClickListener {
            val intent = Intent(this, RegistroVentaActivity::class.java)
            startActivity(intent)
        }

        btnReporteVentas.setOnClickListener {
            val intent = Intent(this, ReporteVentasActivity::class.java)
            startActivity(intent)
        }
    }
} 
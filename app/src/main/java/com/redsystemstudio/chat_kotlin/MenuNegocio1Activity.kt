package com.redsystemstudio.chat_kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MenuNegocio1Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_negocio1)

        val btnRegistrarProducto = findViewById<Button>(R.id.btnRegistrarProducto)
        val btnVerProductos = findViewById<Button>(R.id.btnVerProductos)
        val btnRegistrarVenta = findViewById<Button>(R.id.btnRegistrarVenta)
        val btnReporteVentas = findViewById<Button>(R.id.btnReporteVentas)
        val btnRegresar = findViewById<Button>(R.id.btnRegresar)

        btnRegresar.setOnClickListener {
            finish()
        }

        btnRegistrarProducto.setOnClickListener {
            val intent = Intent(this, RegistroProductoNegocio1Activity::class.java)
            startActivity(intent)
        }

        btnVerProductos.setOnClickListener {
            val intent = Intent(this, ListaProveedoresActivity::class.java)
            startActivity(intent)
        }

        btnRegistrarVenta.setOnClickListener {
            val intent = Intent(this, RegistroVentaNegocio1Activity::class.java)
            startActivity(intent)
        }

        btnReporteVentas.setOnClickListener {
            val intent = Intent(this, ReporteVentasNegocio1Activity::class.java)
            startActivity(intent)
        }
    }
} 
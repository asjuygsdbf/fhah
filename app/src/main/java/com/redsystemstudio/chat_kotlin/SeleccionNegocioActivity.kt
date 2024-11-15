package com.redsystemstudio.chat_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class SeleccionNegocioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_negocio)
        
        val btnNegocio1 = findViewById<ImageButton>(R.id.btnNegocio1)
        val btnNegocio2 = findViewById<ImageButton>(R.id.btnNegocio2)
        
        btnNegocio1.setOnClickListener {
            val intent = Intent(this, MenuNegocio1Activity::class.java)
            startActivity(intent)
        }
        
        btnNegocio2.setOnClickListener {
            val intent = Intent(this, MenuNegocioActivity::class.java)
            startActivity(intent)
        }
    }
} 
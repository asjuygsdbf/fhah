package com.redsystemstudio.chat_kotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.redsystemstudio.chat_kotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Ir directamente a la selección de negocio
        val intent = Intent(this, SeleccionNegocioActivity::class.java)
        startActivity(intent)
        finish()
    }
}

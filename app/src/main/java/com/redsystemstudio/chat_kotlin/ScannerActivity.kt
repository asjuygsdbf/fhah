package com.redsystemstudio.chat_kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*

class ScannerActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    private val CAMERA_PERMISSION_REQUEST = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        if (checkCameraPermission()) {
            setupScanner()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupScanner() {
        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                val intent = intent
                intent.putExtra("SCAN_RESULT", it.text)
                setResult(RESULT_OK, intent)
                finish()
            }
        }

        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
                Toast.makeText(this, "Error al escanear: ${it.message}",
                    Toast.LENGTH_SHORT).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupScanner()
            } else {
                Toast.makeText(this,
                    "Se necesita permiso de cámara para escanear",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::codeScanner.isInitialized) {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        if (::codeScanner.isInitialized) {
            codeScanner.releaseResources()
        }
        super.onPause()
    }
} 
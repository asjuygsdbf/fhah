package com.redsystemstudio.chat_kotlin

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.redsystemstudio.chat_kotlin.Modelos.Proveedor
import com.redsystemstudio.chat_kotlin.Modelos.Talla
import com.redsystemstudio.chat_kotlin.Modelos.ProductoNegocio1
import java.io.ByteArrayOutputStream
import android.view.View
import com.bumptech.glide.Glide
import com.redsystemstudio.chat_kotlin.Modelos.Modelo

class RegistroProductoNegocio1Activity : AppCompatActivity() {

    private lateinit var etCodigoBarras: EditText
    private lateinit var btnEscanear: Button
    private lateinit var spinnerModelo: Spinner
    private lateinit var etNombre: EditText
    private lateinit var spinnerProveedor: Spinner
    private lateinit var etCodigoModelo: EditText
    private lateinit var spinnerTalla: Spinner
    private lateinit var btnAgregarTalla: ImageButton
    private lateinit var etColor: EditText
    private lateinit var etCosto: EditText
    private lateinit var etPrecio: EditText
    private lateinit var btnAceptar: Button
    private lateinit var btnRegresar: Button
    private lateinit var btnAgregarProveedor: ImageButton
    private lateinit var btnAgregarModelo: ImageButton
    private val proveedoresList = mutableListOf<Proveedor>()
    private lateinit var proveedoresAdapter: ArrayAdapter<String>
    private val database = FirebaseDatabase.getInstance()
    private val proveedoresRef = database.getReference("Proveedores")
    private val tallasRef = database.getReference("Tallas")
    private val SCANNER_REQUEST_CODE = 100
    private val tallasList = mutableListOf<Talla>()
    private lateinit var tallasAdapter: ArrayAdapter<String>
    private lateinit var btnSubirImagen: ImageButton
    private val PICK_IMAGE_REQUEST = 101
    private val TAKE_PHOTO_REQUEST = 102
    private val ID_NEGOCIO_1 = "negocio1"
    private val productosRef = database.getReference("Productos_Negocio1")
    private val storageRef = FirebaseStorage.getInstance().reference
    private var imagenUri: Uri? = null
    private var imagenProveedorUri: Uri? = null
    private val PICK_PROVEEDOR_IMAGE = 103
    private val TAKE_PROVEEDOR_PHOTO = 104
    private lateinit var dialogProveedorActual: AlertDialog
    private lateinit var btnImagenProveedorActual: ImageButton
    private val modelosList = mutableListOf<Modelo>()
    private lateinit var modelosAdapter: ArrayAdapter<String>
    private val modelosRef = database.getReference("Modelos")
    private lateinit var etCantidad: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_producto_negocio1)

        initializeViews()
        setupSpinnerProveedor()
        setupSpinnerTalla()
        setupSpinnerModelo()
        setupListeners()
        cargarProveedores()
        cargarTallas()
        cargarModelos()
    }

    private fun initializeViews() {
        etCodigoBarras = findViewById(R.id.etCodigoBarras)
        btnEscanear = findViewById(R.id.btnEscanear)
        spinnerModelo = findViewById(R.id.spinnerModelo)
        etNombre = findViewById(R.id.etNombre)
        spinnerProveedor = findViewById(R.id.spinnerProveedor)
        etCodigoModelo = findViewById(R.id.etCodigoModelo)
        spinnerTalla = findViewById(R.id.spinnerTalla)
        btnAgregarTalla = findViewById(R.id.btnAgregarTalla)
        etColor = findViewById(R.id.etColor)
        etCosto = findViewById(R.id.etCosto)
        etPrecio = findViewById(R.id.etPrecio)
        btnAceptar = findViewById(R.id.btnAceptar)
        btnRegresar = findViewById(R.id.btnRegresar)
        btnAgregarProveedor = findViewById(R.id.btnAgregarProveedor)
        btnAgregarModelo = findViewById(R.id.btnAgregarModelo)
        etCantidad = findViewById(R.id.etCantidad)
    }

    private fun setupListeners() {
        btnRegresar.setOnClickListener {
            finish()
        }

        btnAceptar.setOnClickListener {
            validarYGuardarProducto()
        }

        btnEscanear.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivityForResult(intent, SCANNER_REQUEST_CODE)
        }

        btnAgregarProveedor.setOnClickListener {
            mostrarDialogoAgregarProveedor()
        }

        btnAgregarTalla.setOnClickListener {
            mostrarDialogoAgregarTalla()
        }

        btnAgregarModelo.setOnClickListener {
            mostrarDialogoAgregarModelo()
        }

        spinnerProveedor.setOnLongClickListener {
            val selectedPosition = spinnerProveedor.selectedItemPosition
            if (selectedPosition >= 0 && proveedoresList.isNotEmpty()) {
                val proveedor = proveedoresList[selectedPosition]
                AlertDialog.Builder(this)
                    .setTitle("Opciones de proveedor")
                    .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoEditarProveedor(proveedor)
                            1 -> mostrarDialogoEliminarProveedor(proveedor)
                        }
                    }
                    .show()
            }
            true
        }

        spinnerTalla.setOnLongClickListener {
            val selectedPosition = spinnerTalla.selectedItemPosition
            if (selectedPosition >= 0 && tallasList.isNotEmpty()) {
                val talla = tallasList[selectedPosition]
                AlertDialog.Builder(this)
                    .setTitle("Opciones de talla")
                    .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoEditarTalla(talla)
                            1 -> mostrarDialogoEliminarTalla(talla)
                        }
                    }
                    .show()
            }
            true
        }

        spinnerModelo.setOnLongClickListener {
            val selectedPosition = spinnerModelo.selectedItemPosition
            if (selectedPosition >= 0 && modelosList.isNotEmpty()) {
                val modelo = modelosList[selectedPosition]
                AlertDialog.Builder(this)
                    .setTitle("Opciones de modelo")
                    .setItems(arrayOf("Editar", "Eliminar")) { _, which ->
                        when (which) {
                            0 -> mostrarDialogoEditarModelo(modelo)
                            1 -> mostrarDialogoEliminarModelo(modelo)
                        }
                    }
                    .show()
            }
            true
        }
    }

    private fun setupSpinnerProveedor() {
        proveedoresAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            proveedoresList.map { it.nombreProveedor })
        proveedoresAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerProveedor.adapter = proveedoresAdapter
    }

    private fun cargarProveedores() {
        proveedoresRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val proveedor = snapshot.getValue(Proveedor::class.java)
                if (proveedor != null) {
                    proveedoresList.add(proveedor)
                    actualizarSpinnerProveedor()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val proveedorActualizado = snapshot.getValue(Proveedor::class.java)
                if (proveedorActualizado != null) {
                    val index = proveedoresList.indexOfFirst { it.idProveedor == proveedorActualizado.idProveedor }
                    if (index != -1) {
                        proveedoresList[index] = proveedorActualizado
                        actualizarSpinnerProveedor()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val proveedorEliminado = snapshot.getValue(Proveedor::class.java)
                if (proveedorEliminado != null) {
                    proveedoresList.removeIf { it.idProveedor == proveedorEliminado.idProveedor }
                    actualizarSpinnerProveedor()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroProductoNegocio1Activity,
                    "Error al cargar proveedores: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarSpinnerProveedor() {
        proveedoresList.sortBy { it.nombreProveedor }
        proveedoresAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            proveedoresList.map { it.nombreProveedor })
        proveedoresAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerProveedor.adapter = proveedoresAdapter
    }

    private fun mostrarDialogoAgregarProveedor() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_proveedor, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreProveedor)
        val etTelefono = dialogView.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = dialogView.findViewById<EditText>(R.id.etDireccion)
        btnImagenProveedorActual = dialogView.findViewById(R.id.btnSubirImagenProveedor)

        dialogProveedorActual = AlertDialog.Builder(this)
            .setTitle("Agregar nuevo proveedor")
            .setView(dialogView)
            .setPositiveButton("Agregar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        btnImagenProveedorActual.setOnClickListener {
            mostrarOpcionesImagenProveedor()
        }

        dialogProveedorActual.setOnShowListener {
            dialogProveedorActual.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nombre = etNombre.text.toString().trim()
                val telefono = etTelefono.text.toString().trim()
                val direccion = etDireccion.text.toString().trim()

                if (nombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (imagenProveedorUri == null) {
                    Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                verificarYGuardarProveedor(nombre, telefono, direccion, dialogProveedorActual)
            }
        }

        dialogProveedorActual.show()
    }

    private fun mostrarOpcionesImagenProveedor() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> tomarFotoProveedor()
                    1 -> seleccionarDeGaleriaProveedor()
                }
            }
            .show()
    }

    private fun tomarFotoProveedor() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intent, TAKE_PROVEEDOR_PHOTO)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir la cámara: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarDeGaleriaProveedor() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        try {
            startActivityForResult(intent, PICK_PROVEEDOR_IMAGE)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir la galería: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SCANNER_REQUEST_CODE -> {
                    val scanResult = data?.getStringExtra("SCAN_RESULT")
                    scanResult?.let {
                        etCodigoBarras.setText(it)
                    }
                }

                PICK_PROVEEDOR_IMAGE -> {
                    data?.data?.let { uri ->
                        imagenProveedorUri = uri
                        btnImagenProveedorActual.apply {
                            setImageURI(uri)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            background = null
                        }
                    }
                }

                TAKE_PROVEEDOR_PHOTO -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let {
                        val bytes = ByteArrayOutputStream()
                        it.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        val path = MediaStore.Images.Media.insertImage(
                            contentResolver,
                            it,
                            "Title",
                            null
                        )
                        imagenProveedorUri = Uri.parse(path)
                        btnImagenProveedorActual.apply {
                            setImageBitmap(it)
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            background = null
                        }
                    }
                }
            }
        }
    }

    private fun getCurrentFocusedDialog(): Dialog? {
        val decorView = window.decorView
        val rootView = decorView.findViewById<View>(android.R.id.content)
        return (rootView?.findViewWithTag<View>("dialog")?.parent as? Dialog)
            ?: (rootView?.findViewById<View>(android.R.id.content)?.findViewWithTag<View>("dialog")?.parent as? Dialog)
    }

    private fun verificarYGuardarProveedor(
        nombre: String,
        telefono: String,
        direccion: String,
        dialog: AlertDialog
    ) {
        val progressDialog = AlertDialog.Builder(this)
            .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress, null))
            .setCancelable(false)
            .create()

        progressDialog.show()

        proveedoresRef.orderByChild("nombreProveedor").equalTo(nombre)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        progressDialog.dismiss()
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe un proveedor con ese nombre",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        subirImagenYGuardarProveedor(nombre, telefono, direccion, dialog, progressDialog)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar proveedor: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun subirImagenYGuardarProveedor(
        nombre: String,
        telefono: String,
        direccion: String,
        dialog: AlertDialog,
        progressDialog: AlertDialog
    ) {
        val imagenRef = storageRef.child("proveedores/${System.currentTimeMillis()}_${nombre}")

        imagenProveedorUri?.let { uri ->
            imagenRef.putFile(uri)
                .addOnSuccessListener {
                    imagenRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        guardarProveedor(nombre, telefono, direccion, downloadUrl.toString(), dialog, progressDialog)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this,
                        "Error al subir la imagen: ${e.message}",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun guardarProveedor(
        nombre: String,
        telefono: String,
        direccion: String,
        urlImagen: String,
        dialog: AlertDialog,
        progressDialog: AlertDialog
    ) {
        val key = proveedoresRef.push().key ?: return
        val proveedor = Proveedor(key, nombre, telefono, direccion, urlImagen)

        proveedoresRef.child(key).setValue(proveedor)
            .addOnSuccessListener {
                progressDialog.dismiss()
                dialog.dismiss()
                Toast.makeText(this, "Proveedor guardado exitosamente", Toast.LENGTH_SHORT).show()
                imagenProveedorUri = null
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al guardar proveedor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditarProveedor(proveedor: Proveedor) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_proveedor, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreProveedor)
        val etTelefono = dialogView.findViewById<EditText>(R.id.etTelefono)
        val etDireccion = dialogView.findViewById<EditText>(R.id.etDireccion)
        btnImagenProveedorActual = dialogView.findViewById(R.id.btnSubirImagenProveedor)

        // Cargar los datos actuales
        etNombre.setText(proveedor.nombreProveedor)
        etTelefono.setText(proveedor.telefono)
        etDireccion.setText(proveedor.direccion)

        // Cargar la imagen actual del proveedor
        if (proveedor.imagen.isNotEmpty()) {
            imagenProveedorUri = Uri.parse(proveedor.imagen)
            Glide.with(this)
                .load(proveedor.imagen)
                .into(btnImagenProveedorActual)
            btnImagenProveedorActual.scaleType = ImageView.ScaleType.CENTER_CROP
            btnImagenProveedorActual.background = null
        }

        dialogProveedorActual = AlertDialog.Builder(this)
            .setTitle("Editar proveedor")
            .setView(dialogView)
            .setPositiveButton("Actualizar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        btnImagenProveedorActual.setOnClickListener {
            mostrarOpcionesImagenProveedor()
        }

        dialogProveedorActual.setOnShowListener {
            dialogProveedorActual.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nuevoNombre = etNombre.text.toString().trim()
                val nuevoTelefono = etTelefono.text.toString().trim()
                val nuevaDireccion = etDireccion.text.toString().trim()

                if (nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (imagenProveedorUri == null) {
                    Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (nuevoNombre != proveedor.nombreProveedor) {
                    verificarYActualizarProveedor(proveedor, nuevoNombre, nuevoTelefono, nuevaDireccion)
                } else {
                    actualizarProveedor(proveedor, nuevoNombre, nuevoTelefono, nuevaDireccion)
                }
            }
        }

        dialogProveedorActual.show()
    }

    private fun verificarYActualizarProveedor(
        proveedor: Proveedor,
        nuevoNombre: String,
        nuevoTelefono: String,
        nuevaDireccion: String
    ) {
        proveedoresRef.orderByChild("nombreProveedor").equalTo(nuevoNombre)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe un proveedor con ese nombre",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        actualizarProveedor(proveedor, nuevoNombre, nuevoTelefono, nuevaDireccion)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar proveedor: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarProveedor(
        proveedor: Proveedor,
        nuevoNombre: String,
        nuevoTelefono: String,
        nuevaDireccion: String
    ) {
        val progressDialog = AlertDialog.Builder(this)
            .setView(LayoutInflater.from(this).inflate(R.layout.dialog_progress, null))
            .setCancelable(false)
            .create()

        progressDialog.show()

        // Subir la nueva imagen si se ha seleccionado una
        if (imagenProveedorUri != null) {
            val imagenRef = storageRef.child("proveedores/${System.currentTimeMillis()}_${nuevoNombre}")

            imagenRef.putFile(imagenProveedorUri!!)
                .addOnSuccessListener {
                    imagenRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        val proveedorActualizado = proveedor.copy(
                            nombreProveedor = nuevoNombre,
                            telefono = nuevoTelefono,
                            direccion = nuevaDireccion,
                            imagen = downloadUrl.toString() // Actualizar la URL de la imagen
                        )

                        proveedoresRef.child(proveedor.idProveedor).setValue(proveedorActualizado)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                                Toast.makeText(this, "Proveedor actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                imagenProveedorUri = null
                            }
                            .addOnFailureListener { e ->
                                progressDialog.dismiss()
                                Toast.makeText(this, "Error al actualizar proveedor: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al subir la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Si no se seleccionó una nueva imagen, solo actualizar los otros campos
            val proveedorActualizado = proveedor.copy(
                nombreProveedor = nuevoNombre,
                telefono = nuevoTelefono,
                direccion = nuevaDireccion
            )

            proveedoresRef.child(proveedor.idProveedor).setValue(proveedorActualizado)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Proveedor actualizado exitosamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error al actualizar proveedor: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun mostrarDialogoEliminarProveedor(proveedor: Proveedor) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Proveedor")
            .setMessage("¿Está seguro que desea eliminar el proveedor ${proveedor.nombreProveedor}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarProveedor(proveedor)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarProveedor(proveedor: Proveedor) {
        proveedoresRef.child(proveedor.idProveedor).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Proveedor eliminado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar proveedor: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSpinnerTalla() {
        tallasAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            tallasList.map { it.nombreTalla })
        tallasAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTalla.adapter = tallasAdapter
    }

    private fun mostrarDialogoAgregarTalla() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_talla, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreTalla)

        AlertDialog.Builder(this)
            .setTitle("Agregar nueva talla")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val nombreTalla = etNombre.text.toString().trim()
                if (nombreTalla.isNotEmpty()) {
                    verificarYGuardarTalla(nombreTalla)
                } else {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarYGuardarTalla(nombreTalla: String) {
        tallasRef.orderByChild("nombreTalla").equalTo(nombreTalla)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe una talla con ese nombre",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        guardarTalla(nombreTalla)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar talla: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun guardarTalla(nombreTalla: String) {
        val key = tallasRef.push().key ?: return
        val talla = Talla(key, nombreTalla)

        tallasRef.child(key).setValue(talla)
            .addOnSuccessListener {
                Toast.makeText(this, "Talla guardada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar talla: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditarTalla(talla: Talla) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_talla, null)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreTalla)
        etNombre.setText(talla.nombreTalla)

        AlertDialog.Builder(this)
            .setTitle("Editar talla")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoNombre = etNombre.text.toString().trim()
                if (nuevoNombre.isNotEmpty()) {
                    if (nuevoNombre != talla.nombreTalla) {
                        verificarYActualizarTalla(talla, nuevoNombre)
                    } else {
                        actualizarTalla(talla, nuevoNombre)
                    }
                } else {
                    Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarYActualizarTalla(talla: Talla, nuevoNombre: String) {
        tallasRef.orderByChild("nombreTalla").equalTo(nuevoNombre)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe una talla con ese nombre",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        actualizarTalla(talla, nuevoNombre)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar talla: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarTalla(talla: Talla, nuevoNombre: String) {
        val tallaActualizada = talla.copy(nombreTalla = nuevoNombre)
        tallasRef.child(talla.idTalla).setValue(tallaActualizada)
            .addOnSuccessListener {
                Toast.makeText(this, "Talla actualizada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar talla: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEliminarTalla(talla: Talla) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Talla")
            .setMessage("¿Está seguro que desea eliminar la talla ${talla.nombreTalla}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarTalla(talla)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarTalla(talla: Talla) {
        tallasRef.child(talla.idTalla).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Talla eliminada exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar talla: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarTallas() {
        tallasRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val talla = snapshot.getValue(Talla::class.java)
                if (talla != null) {
                    tallasList.add(talla)
                    actualizarSpinnerTalla()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val tallaActualizada = snapshot.getValue(Talla::class.java)
                if (tallaActualizada != null) {
                    val index = tallasList.indexOfFirst { it.idTalla == tallaActualizada.idTalla }
                    if (index != -1) {
                        tallasList[index] = tallaActualizada
                        actualizarSpinnerTalla()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val tallaEliminada = snapshot.getValue(Talla::class.java)
                if (tallaEliminada != null) {
                    tallasList.removeIf { it.idTalla == tallaEliminada.idTalla }
                    actualizarSpinnerTalla()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroProductoNegocio1Activity,
                    "Error al cargar tallas: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarSpinnerTalla() {
        tallasList.sortBy { it.nombreTalla }
        tallasAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            tallasList.map { it.nombreTalla })
        tallasAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerTalla.adapter = tallasAdapter
    }

    private fun mostrarOpcionesImagen() {
        val opciones = arrayOf("Tomar foto", "Seleccionar de galería")

        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> tomarFoto()
                    1 -> seleccionarDeGaleria()
                }
            }
            .show()
    }

    private fun tomarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir la cámara: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun seleccionarDeGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        try {
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al abrir la galería: ${e.message}",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun validarYGuardarProducto() {
        val codigoBarras = etCodigoBarras.text.toString().trim()
        val codigoModelo = etCodigoModelo.text.toString().trim()
        val nombreModelo = spinnerModelo.selectedItem?.toString() ?: ""
        val nombre = etNombre.text.toString().trim()
        val color = etColor.text.toString().trim()
        val cantidadStr = etCantidad.text.toString().trim()
        val costoStr = etCosto.text.toString().trim()
        val precioStr = etPrecio.text.toString().trim()

        // Validaciones
        if (spinnerProveedor.selectedItem == null) {
            Toast.makeText(this, "Debe seleccionar un proveedor", Toast.LENGTH_SHORT).show()
            return
        }

        if (spinnerModelo.selectedItem == null) {
            Toast.makeText(this, "Debe seleccionar un modelo", Toast.LENGTH_SHORT).show()
            return
        }

        if (spinnerTalla.selectedItem == null) {
            Toast.makeText(this, "Debe seleccionar una talla", Toast.LENGTH_SHORT).show()
            return
        }

        if (codigoBarras.isEmpty() || codigoModelo.isEmpty() || 
            nombre.isEmpty() || color.isEmpty() || cantidadStr.isEmpty() || 
            costoStr.isEmpty() || precioStr.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val cantidad = cantidadStr.toInt()
            val costo = costoStr.toDouble()
            val precio = precioStr.toDouble()

            if (cantidad <= 0) {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return
            }

            if (costo <= 0 || precio <= 0) {
                Toast.makeText(this, "Los valores numéricos deben ser mayores a 0", 
                    Toast.LENGTH_SHORT).show()
                return
            }

            val proveedorSeleccionado = proveedoresList[spinnerProveedor.selectedItemPosition]
            val tallaSeleccionada = tallasList[spinnerTalla.selectedItemPosition]

            verificarCodigoYGuardar(
                codigoBarras,
                codigoModelo,
                nombreModelo,
                nombre,
                color,
                cantidad,
                costo,
                precio,
                proveedorSeleccionado.idProveedor,
                tallaSeleccionada.idTalla
            )

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Los valores numéricos no son válidos", 
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun verificarCodigoYGuardar(
        codigoBarras: String,
        codigoModelo: String,
        nombreModelo: String,
        nombre: String,
        color: String,
        cantidad: Int,
        costo: Double,
        precio: Double,
        idProveedor: String,
        idTalla: String
    ) {
        productosRef.orderByChild("codigoBarras").equalTo(codigoBarras)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe un producto con ese código de barras",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        guardarProducto(
                            codigoBarras,
                            codigoModelo,
                            nombreModelo,
                            nombre,
                            color,
                            cantidad,
                            costo,
                            precio,
                            idProveedor,
                            idTalla
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar el código: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun guardarProducto(
        codigoBarras: String,
        codigoModelo: String,
        nombreModelo: String,
        nombre: String,
        color: String,
        cantidad: Int,
        costo: Double,
        precio: Double,
        idProveedor: String,
        idTalla: String
    ) {
        val key = productosRef.push().key ?: return
        
        val producto = ProductoNegocio1(
            idProducto = key,
            idNegocio = ID_NEGOCIO_1,
            idProveedor = idProveedor,
            idTalla = idTalla,
            codigoBarras = codigoBarras,
            codigoModelo = codigoModelo,
            modelo = nombreModelo,
            nombre = nombre,
            color = color,
            cantidad = cantidad,
            costo = costo,
            precio = precio
        )

        productosRef.child(key).setValue(producto)
            .addOnSuccessListener {
                Toast.makeText(this, "Producto guardado exitosamente", 
                    Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar producto: ${e.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }

    private fun limpiarCampos() {
        etCodigoBarras.text.clear()
        etCodigoModelo.text.clear()
        etNombre.text.clear()
        etColor.text.clear()
        etCosto.text.clear()
        etPrecio.text.clear()
        etCantidad.text.clear()
        spinnerProveedor.setSelection(0)
        spinnerTalla.setSelection(0)
        spinnerModelo.setSelection(0)
    }

    private fun setupSpinnerModelo() {
        modelosAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            modelosList.map { "${it.nombreModelo}" })
        modelosAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerModelo.adapter = modelosAdapter

        spinnerModelo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val modeloSeleccionado = modelosList[position]
                etCodigoModelo.setText(modeloSeleccionado.codigoModelo)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                etCodigoModelo.setText("")
            }
        }
    }

    private fun cargarModelos() {
        modelosRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val modelo = snapshot.getValue(Modelo::class.java)
                if (modelo != null) {
                    modelosList.add(modelo)
                    actualizarSpinnerModelo()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val modeloActualizado = snapshot.getValue(Modelo::class.java)
                if (modeloActualizado != null) {
                    val index = modelosList.indexOfFirst { it.idModelo == modeloActualizado.idModelo }
                    if (index != -1) {
                        modelosList[index] = modeloActualizado
                        actualizarSpinnerModelo()
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val modeloEliminado = snapshot.getValue(Modelo::class.java)
                if (modeloEliminado != null) {
                    modelosList.removeIf { it.idModelo == modeloEliminado.idModelo }
                    actualizarSpinnerModelo()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RegistroProductoNegocio1Activity,
                    "Error al cargar modelos: ${error.message}",
                    Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun actualizarSpinnerModelo() {
        modelosList.sortBy { it.nombreModelo }
        modelosAdapter = ArrayAdapter(this, R.layout.spinner_item_layout,
            modelosList.map { it.nombreModelo })
        modelosAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerModelo.adapter = modelosAdapter
    }

    private fun mostrarDialogoAgregarModelo() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_modelo, null)
        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigoModelo)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreModelo)

        AlertDialog.Builder(this)
            .setTitle("Agregar nuevo modelo")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val codigo = etCodigo.text.toString().trim()
                val nombre = etNombre.text.toString().trim()

                if (codigo.isEmpty() || nombre.isEmpty()) {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                verificarYGuardarModelo(codigo, nombre)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarYGuardarModelo(codigo: String, nombre: String) {
        modelosRef.orderByChild("codigoModelo").equalTo(codigo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe un modelo con ese código",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        guardarModelo(codigo, nombre)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar modelo: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun guardarModelo(codigo: String, nombre: String) {
        val key = modelosRef.push().key ?: return
        val modelo = Modelo(key, codigo, nombre)

        modelosRef.child(key).setValue(modelo)
            .addOnSuccessListener {
                Toast.makeText(this, "Modelo guardado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar modelo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEditarModelo(modelo: Modelo) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialogo_modelo, null)
        val etCodigo = dialogView.findViewById<EditText>(R.id.etCodigoModelo)
        val etNombre = dialogView.findViewById<EditText>(R.id.etNombreModelo)

        // Cargar datos actuales
        etCodigo.setText(modelo.codigoModelo)
        etNombre.setText(modelo.nombreModelo)

        AlertDialog.Builder(this)
            .setTitle("Editar modelo")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { _, _ ->
                val nuevoCodigo = etCodigo.text.toString().trim()
                val nuevoNombre = etNombre.text.toString().trim()

                if (nuevoCodigo.isEmpty() || nuevoNombre.isEmpty()) {
                    Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (nuevoCodigo != modelo.codigoModelo) {
                    verificarYActualizarModelo(modelo, nuevoCodigo, nuevoNombre)
                } else {
                    actualizarModelo(modelo, nuevoCodigo, nuevoNombre)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun verificarYActualizarModelo(modelo: Modelo, nuevoCodigo: String, nuevoNombre: String) {
        modelosRef.orderByChild("codigoModelo").equalTo(nuevoCodigo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegistroProductoNegocio1Activity,
                            "Ya existe un modelo con ese código",
                            Toast.LENGTH_SHORT).show()
                    } else {
                        actualizarModelo(modelo, nuevoCodigo, nuevoNombre)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegistroProductoNegocio1Activity,
                        "Error al verificar modelo: ${error.message}",
                        Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun actualizarModelo(modelo: Modelo, nuevoCodigo: String, nuevoNombre: String) {
        val modeloActualizado = modelo.copy(
            codigoModelo = nuevoCodigo,
            nombreModelo = nuevoNombre
        )

        modelosRef.child(modelo.idModelo).setValue(modeloActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "Modelo actualizado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar modelo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun mostrarDialogoEliminarModelo(modelo: Modelo) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar Modelo")
            .setMessage("¿Está seguro que desea eliminar el modelo ${modelo.nombreModelo}?")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarModelo(modelo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarModelo(modelo: Modelo) {
        modelosRef.child(modelo.idModelo).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Modelo eliminado exitosamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar modelo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
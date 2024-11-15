package com.redsystemstudio.chat_kotlin.Modelos

data class ProductoNegocio1(
    var idProducto: String = "",
    var idNegocio: String = "",
    var idProveedor: String = "",
    var idTalla: String = "",
    var codigoBarras: String = "",
    var codigoModelo: String = "",
    var modelo: String = "",
    var nombre: String = "",
    var color: String = "",
    var cantidad: Int = 0,
    var costo: Double = 0.0,
    var precio: Double = 0.0
) {
    constructor() : this("", "", "", "", "", "", "", "", "", 0, 0.0, 0.0)
} 
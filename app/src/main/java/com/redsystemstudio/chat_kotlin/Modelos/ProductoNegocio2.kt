package com.redsystemstudio.chat_kotlin.Modelos

data class ProductoNegocio2(
    var idProducto: String = "",
    var idNegocio: String = "",
    var idMarca: String = "",
    var codigo: String = "",
    var nombre: String = "",
    var cantidad: Int = 0,
    var precioCompra: Double = 0.0,
    var precioVenta: Double = 0.0
) {
    constructor() : this("", "", "", "", "", 0, 0.0, 0.0)
} 
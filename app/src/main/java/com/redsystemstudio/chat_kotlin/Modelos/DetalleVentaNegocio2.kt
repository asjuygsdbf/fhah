package com.redsystemstudio.chat_kotlin.Modelos

data class DetalleVentaNegocio2(
    var idDetalle: String = "",
    var idVenta: String = "",
    var idProducto: String = "",
    var cantidad: Int = 0,
    var precioVenta: Double = 0.0,
    var total: Double = 0.0
) {
    constructor() : this("", "", "", 0, 0.0, 0.0)
} 
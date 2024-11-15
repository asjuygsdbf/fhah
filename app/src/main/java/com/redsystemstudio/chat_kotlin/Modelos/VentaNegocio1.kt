package com.redsystemstudio.chat_kotlin.Modelos

data class VentaNegocio1(
    var idVenta: String = "",
    var idNegocio: String = "",
    var nombreCliente: String = "",
    var dniCliente: String = "",
    var metodoPago: String = "",
    var estado: String = "Completada",
    var importeTotal: Double = 0.0,
    var fecha: Long = System.currentTimeMillis()
) {
    constructor() : this("", "", "", "", "", "Completada", 0.0, 0)
} 
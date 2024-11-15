package com.redsystemstudio.chat_kotlin.Modelos

class DetalleVenta {
    var idDetalle: Int = 0
    var idVenta: Int = 0
    var cantidad: Int = 0
    var precioVenta: Double = 0.0

    constructor()

    constructor(
        idDetalle: Int,
        idVenta: Int,
        cantidad: Int,
        precioVenta: Double
    ) {
        this.idDetalle = idDetalle
        this.idVenta = idVenta
        this.cantidad = cantidad
        this.precioVenta = precioVenta
    }
}
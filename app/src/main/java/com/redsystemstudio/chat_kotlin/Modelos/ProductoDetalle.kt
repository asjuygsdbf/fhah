package com.redsystemstudio.chat_kotlin.Modelos

class ProductoDetalle {
    var idDetalle: Int = 0
    var idProducto: Int = 0

    constructor()

    constructor(
        idDetalle: Int,
        idProducto: Int
    ) {
        this.idDetalle = idDetalle
        this.idProducto = idProducto
    }
}
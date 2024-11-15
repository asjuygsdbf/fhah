package com.redsystemstudio.chat_kotlin.Modelos

class MarcaProducto {
    var idMarcaProducto: Int = 0
    var idMarca: Int = 0
    var imagen: String = ""
    var codigoB: String = ""
    var codigoB2: String = ""
    var cantidad: Int = 0

    constructor()

    constructor(
        idMarcaProducto: Int,
        idMarca: Int,
        imagen: String,
        codigoB: String,
        codigoB2: String,
        cantidad: Int
    ) {
        this.idMarcaProducto = idMarcaProducto
        this.idMarca = idMarca
        this.imagen = imagen
        this.codigoB = codigoB
        this.codigoB2 = codigoB2
        this.cantidad = cantidad
    }
}
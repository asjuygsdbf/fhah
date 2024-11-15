package com.redsystemstudio.chat_kotlin.Modelos

class Inventario {
    var idInventario: Int = 0
    var idProveedor: Int = 0
    var idTalla: Int = 0
    var idColor: Int = 0
    var cantidad: Int = 0
    var imagen: String = ""
    var codigoB: String = ""
    var codigoB2: String = ""

    constructor()

    constructor(
        idInventario: Int,
        idProveedor: Int,
        idTalla: Int,
        idColor: Int,
        cantidad: Int,
        imagen: String,
        codigoB: String,
        codigoB2: String
    ) {
        this.idInventario = idInventario
        this.idProveedor = idProveedor
        this.idTalla = idTalla
        this.idColor = idColor
        this.cantidad = cantidad
        this.imagen = imagen
        this.codigoB = codigoB
        this.codigoB2 = codigoB2
    }
}
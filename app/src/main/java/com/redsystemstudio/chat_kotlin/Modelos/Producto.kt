package com.redsystemstudio.chat_kotlin.Modelos

class Producto {
    var idProducto: Int = 0
    var idMarcaProducto: Int = 0
    var idInventario: Int = 0
    var nombre: String = ""
    var modelo: String = ""
    var precioCompra: Double = 0.0
    var precioVenta: Double = 0.0

    constructor()

    constructor(
        idProducto: Int,
        idMarcaProducto: Int,
        idInventario: Int,
        nombre: String,
        modelo: String,
        precioCompra: Double,
        precioVenta: Double
    ) {
        this.idProducto = idProducto
        this.idMarcaProducto = idMarcaProducto
        this.idInventario = idInventario
        this.nombre = nombre
        this.modelo = modelo
        this.precioCompra = precioCompra
        this.precioVenta = precioVenta
    }
}
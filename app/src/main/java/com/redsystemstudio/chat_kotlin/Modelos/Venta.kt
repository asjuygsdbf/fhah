package com.redsystemstudio.chat_kotlin.Modelos

class Venta {
    var idVenta: Int = 0
    var idCliente: Int = 0
    var idUsuario: Int = 0
    var saldo: Double = 0.0
    var estado: String = ""
    var importeTotal: Double = 0.0

    constructor()

    constructor(
        idVenta: Int,
        idCliente: Int,
        idUsuario: Int,
        saldo: Double,
        estado: String,
        importeTotal: Double
    ) {
        this.idVenta = idVenta
        this.idCliente = idCliente
        this.idUsuario = idUsuario
        this.saldo = saldo
        this.estado = estado
        this.importeTotal = importeTotal
    }
}
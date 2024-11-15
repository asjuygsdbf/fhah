package com.redsystemstudio.chat_kotlin.Modelos

class Cliente {
    var idCliente: Int = 0
    var nombreCliente: String = ""
    var apellidoCliente: String = ""
    var dni: String = ""
    var ruc: String = ""

    constructor()

    constructor(
        idCliente: Int,
        nombreCliente: String,
        apellidoCliente: String,
        dni: String,
        ruc: String
    ) {
        this.idCliente = idCliente
        this.nombreCliente = nombreCliente
        this.apellidoCliente = apellidoCliente
        this.dni = dni
        this.ruc = ruc
    }
}
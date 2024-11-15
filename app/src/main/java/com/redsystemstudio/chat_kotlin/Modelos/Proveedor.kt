package com.redsystemstudio.chat_kotlin.Modelos

data class Proveedor(
    var idProveedor: String = "",
    var nombreProveedor: String = "",
    var telefono: String = "",
    var direccion: String = "",
    var imagen: String = ""
) {
    constructor() : this("", "", "", "", "")
}
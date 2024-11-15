package com.redsystemstudio.chat_kotlin.Modelos

data class Talla(
    var idTalla: String = "",
    var nombreTalla: String = ""
) {
    constructor() : this("", "")
}
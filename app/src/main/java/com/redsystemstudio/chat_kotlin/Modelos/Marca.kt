package com.redsystemstudio.chat_kotlin.Modelos

data class Marca(
    var idMarca: String = "",
    var nombreMarca: String = ""
) {
    constructor() : this("", "")
}
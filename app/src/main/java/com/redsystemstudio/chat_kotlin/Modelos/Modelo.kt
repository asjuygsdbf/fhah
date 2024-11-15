package com.redsystemstudio.chat_kotlin.Modelos

data class Modelo(
    var idModelo: String = "",
    var codigoModelo: String = "",
    var nombreModelo: String = ""
) {
    constructor() : this("", "", "")
} 
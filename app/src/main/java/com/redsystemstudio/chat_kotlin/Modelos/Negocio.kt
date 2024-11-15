package com.redsystemstudio.chat_kotlin.Modelos

data class Negocio(
    var idNegocio: String = "",
    var nombreNegocio: String = "",
    var logo: String = ""
) {
    constructor() : this("", "", "")
} 
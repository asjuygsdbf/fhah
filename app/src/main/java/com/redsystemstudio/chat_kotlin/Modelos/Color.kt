package com.redsystemstudio.chat_kotlin.Modelos

class Color {
    var idColor: Int = 0
    var nombreColor: String = ""

    constructor()

    constructor(
        idColor: Int,
        nombreColor: String
    ) {
        this.idColor = idColor
        this.nombreColor = nombreColor
    }
}
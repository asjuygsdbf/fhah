package com.redsystemstudio.chat_kotlin.Modelos

class RolUsuario {
    var idRol: Int = 0
    var nombreRol: String = ""

    constructor()

    constructor(
        idRol: Int,
        nombreRol: String
    ) {
        this.idRol = idRol
        this.nombreRol = nombreRol
    }
}
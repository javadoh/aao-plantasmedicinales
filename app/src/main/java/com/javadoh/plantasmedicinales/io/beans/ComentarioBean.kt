package com.javadoh.plantasmedicinales.io.beans

import java.io.Serializable

data class ComentarioBean(
    var id: Int = 0,
    var nombreUsuario: String? = null,
    var emailUsuario: String? = null,
    var comentario: String? = null,
    var estado: String? = null,

    // GOOGLE PLAY SERVICES
    var ciudad: String? = null,
    var pais: String? = null,
    var sexoUsuario: String? = null,
    var fechaNacUsuario: String? = null,
    var imgFbUrlUsuario: String? = null
) : Serializable
package com.javadoh.plantasmedicinales.io.beans

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ComentarioBean(
    var id: Int = 0,
    @SerializedName("nombreUsuario")
    var nombreUsuario: String? = null,
    @SerializedName("emailUsuario")
    var emailUsuario: String? = null,
    @SerializedName("comentario")
    var comentario: String? = null,
    @SerializedName("estado")
    var estado: String? = null,

    // GOOGLE PLAY SERVICES
    @SerializedName("ciudad")
    var ciudad: String? = null,
    @SerializedName("pais")
    var pais: String? = null,
    @SerializedName("sexoUsuario")
    var sexoUsuario: String? = null,
    @SerializedName("fechaNacUsuario")
    var fechaNacUsuario: String? = null,
    @SerializedName("imgFbUrlUsuario")
    var imgFbUrlUsuario: String? = null
) : Serializable
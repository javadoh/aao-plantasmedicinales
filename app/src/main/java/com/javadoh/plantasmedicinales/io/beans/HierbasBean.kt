package com.javadoh.plantasmedicinales.io.beans

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.ArrayList

data class HierbasBean(
    var id: Int = 0,
    @SerializedName("nombre", alternate = ["name"])
    var nombre: String? = null,
    @SerializedName("nombreCientifico", alternate = ["nombrecientifico", "scientificName"])
    var nombreCientifico: String? = null,
    @SerializedName("descripcion", alternate = ["description"])
    var descripcion: String? = null,
    @SerializedName("seccion", alternate = ["section"])
    var seccion: String? = null,
    @SerializedName("indicaciones", alternate = ["indications"])
    var indicaciones: String? = null,
    @SerializedName("contraIndicaciones", alternate = ["contraindicaciones", "contraindications"])
    var contraIndicaciones: String? = null,
    @SerializedName("empleo", alternate = ["use"])
    var empleo: String? = null,
    @SerializedName("ubicacion", alternate = ["location"])
    var ubicacion: String? = null,
    @SerializedName("imgurl", alternate = ["imageUrl"])
    var imgurl: String? = null,
    @SerializedName("estado", alternate = ["status"])
    var estado: String? = null,
    @SerializedName("gastronomia", alternate = ["gastronomy"])
    var gastronomia: String? = null,
    @SerializedName("localization")
    var localization: String? = null,

    @SerializedName("alias")
    var alias: List<String>? = null,
    @SerializedName("propiedades")
    var propiedades: List<String>? = null,
    @SerializedName("sintomas")
    var sintomas: List<String>? = null,
    @SerializedName("comentarios")
    var comentarios: ArrayList<ComentarioBean>? = null
) : Serializable {

    // Custom getters/setters to match your original Java logic:
    // If the list is null, it initializes and returns a new ArrayList.

    var propiedadesHierba: List<String>
        get() {
            if (propiedades == null) {
                propiedades = ArrayList()
            }
            return propiedades!!
        }
        set(value) {
            propiedades = value
        }

    var sintomasHierba: List<String>
        get() {
            if (sintomas == null) {
                sintomas = ArrayList()
            }
            return sintomas!!
        }
        set(value) {
            sintomas = value
        }

    // Helper functions to safely get Alias and Comentarios (matches Java logic)
    fun getSafeAlias(): List<String> {
        if (alias == null) alias = ArrayList()
        return alias!!
    }

    fun getSafeComentarios(): ArrayList<ComentarioBean> {
        if (comentarios == null) comentarios = ArrayList()
        return comentarios!!
    }
}
package com.javadoh.plantasmedicinales.io.beans

import java.io.Serializable
import java.util.ArrayList

data class HierbasBean(
    var id: Int = 0,
    var nombre: String? = null,
    var nombreCientifico: String? = null,
    var descripcion: String? = null,
    var seccion: String? = null,
    var indicaciones: String? = null,
    var contraIndicaciones: String? = null,
    var empleo: String? = null,
    var ubicacion: String? = null,
    var imgurl: String? = null,
    var estado: String? = null,
    var gastronomia: String? = null,
    var localization: String? = null,

    var alias: List<String>? = null,
    var propiedades: List<String>? = null,
    var sintomas: List<String>? = null,
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
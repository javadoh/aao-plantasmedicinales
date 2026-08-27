package com.javadoh.plantasmedicinales.utils.bean

import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import java.io.Serializable
import java.util.ArrayList

object MemoryBeanAux : Serializable {

    private var _listaHierbas: ArrayList<HierbasBean>? = null

    // Replicates your null-safe getter logic
    var listaHierbas: ArrayList<HierbasBean>
        get() {
            if (_listaHierbas == null) {
                _listaHierbas = ArrayList()
            }
            return _listaHierbas!!
        }
        set(value) {
            _listaHierbas = value
        }

    var posicionHierba: Int = 0
    var userFbData: Array<String>? = null
    var userFbUlrImage: String? = null
    var hierbaImagePath: String? = null
}
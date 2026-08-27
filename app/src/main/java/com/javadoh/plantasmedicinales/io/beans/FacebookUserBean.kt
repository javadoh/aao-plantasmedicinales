package com.javadoh.plantasmedicinales.io.beans

import java.io.Serializable

data class FacebookUserBean(
    var id: String? = null,
    var name: String? = null,
    var gender: String? = null,
    var birthday: String? = null,
    var email: String? = null,
    var location: LocationFaceBean? = null
) : Serializable
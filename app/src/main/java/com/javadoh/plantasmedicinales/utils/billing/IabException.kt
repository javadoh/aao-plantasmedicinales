package com.javadoh.plantasmedicinales.utils.billing

class IabException(val result: IabResult, cause: Exception? = null) : Exception(result.message, cause) {
    constructor(response: Int, message: String) : this(IabResult(response, message))
    constructor(response: Int, message: String, cause: Exception) : this(IabResult(response, message), cause)
}
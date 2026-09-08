package com.javadoh.plantasmedicinales.utils.billing

class IabException : Exception {
    val result: IabResult

    constructor(response: Int, message: String) : this(IabResult(response, message))

    constructor(r: IabResult) : this(r, null)

    constructor(response: Int, message: String, cause: Exception?) : this(IabResult(response, message), cause)

    constructor(r: IabResult, cause: Exception?) : super(r.message, cause) {
        result = r
    }
}
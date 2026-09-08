package com.javadoh.plantasmedicinales.utils.billing

class IabResult(val response: Int, val message: String) {
    val isSuccess: Boolean
        get() = response == IabHelper.BILLING_RESPONSE_RESULT_OK

    val isFailure: Boolean
        get() = !isSuccess

    override fun toString(): String = "IabResult: $message (response: $response)"
}
package com.javadoh.plantasmedicinales.utils.billing

class IabResult(val response: Int, message: String?) {
    val message: String = if (message.isNullOrBlank()) {
        IabHelper.getResponseDesc(response)
    } else {
        "$message (response: ${IabHelper.getResponseDesc(response)})"
    }

    val isSuccess: Boolean get() = response == IabHelper.BILLING_RESPONSE_RESULT_OK
    val isFailure: Boolean get() = !isSuccess

    override fun toString(): String = "IabResult: $message"
}
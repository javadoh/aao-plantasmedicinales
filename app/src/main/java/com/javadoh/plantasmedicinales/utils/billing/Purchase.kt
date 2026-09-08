package com.javadoh.plantasmedicinales.utils.billing

import org.json.JSONException
import org.json.JSONObject

class Purchase(
    val itemType: String,
    val originalJson: String,
    val signature: String
) {
    var orderId: String? = null
    var packageName: String? = null
    var sku: String = ""
    var purchaseTime: Long = 0
    var purchaseState: Int = 0
    var developerPayload: String? = null
    var token: String = ""

    init {
        val o = JSONObject(originalJson)
        orderId = o.optString("orderId")
        packageName = o.optString("packageName")
        sku = o.optString("productId")
        purchaseTime = o.optLong("purchaseTime")
        purchaseState = o.optInt("purchaseState")
        developerPayload = o.optString("developerPayload")
        token = o.optString("token", o.optString("purchaseToken"))
    }

    override fun toString(): String = "PurchaseInfo(type:$itemType):$originalJson"
}
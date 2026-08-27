package com.javadoh.plantasmedicinales.utils.billing

import org.json.JSONObject

data class Purchase(val itemType: String, val originalJson: String, val signature: String) {
    val orderId: String
    val packageName: String
    val sku: String
    val purchaseTime: Long
    val purchaseState: Int
    val developerPayload: String
    val token: String

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
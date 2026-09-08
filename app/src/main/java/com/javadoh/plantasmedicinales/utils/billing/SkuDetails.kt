package com.javadoh.plantasmedicinales.utils.billing

import org.json.JSONObject

class SkuDetails(val itemType: String, val json: String) {
    val sku: String
    val type: String
    val price: String
    val title: String
    val description: String

    init {
        val o = JSONObject(json)
        sku = o.optString("productId")
        type = o.optString("type")
        price = o.optString("price")
        title = o.optString("title")
        description = o.optString("description")
    }

    override fun toString(): String = "SkuDetails:$json"
}
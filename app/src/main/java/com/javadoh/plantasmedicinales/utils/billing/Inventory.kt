package com.javadoh.plantasmedicinales.utils.billing

import java.util.HashMap

class Inventory {
    private val mSkuMap = HashMap<String, SkuDetails>()
    private val mPurchaseMap = HashMap<String, Purchase>()

    fun getSkuDetails(sku: String): SkuDetails? = mSkuMap[sku]

    fun getPurchase(sku: String): Purchase? = mPurchaseMap[sku]

    fun hasPurchase(sku: String): Boolean = mPurchaseMap.containsKey(sku)

    fun hasDetails(sku: String): Boolean = mSkuMap.containsKey(sku)

    fun erasePurchase(sku: String) {
        mPurchaseMap.remove(sku)
    }

    fun getAllOwnedSkus(itemType: String): List<String> {
        val result = ArrayList<String>()
        for (p in mPurchaseMap.values) {
            if (p.itemType == itemType) {
                result.add(p.sku)
            }
        }
        return result
    }

    fun addSkuDetails(d: SkuDetails) {
        mSkuMap[d.sku] = d
    }

    fun addPurchase(p: Purchase) {
        mPurchaseMap[p.sku] = p
    }
}
package com.javadoh.plantasmedicinales.utils.billing

class Inventory {
    private val skuMap = mutableMapOf<String, SkuDetails>()
    private val purchaseMap = mutableMapOf<String, Purchase>()

    fun getSkuDetails(sku: String): SkuDetails? = skuMap[sku]
    fun getPurchase(sku: String): Purchase? = purchaseMap[sku]
    fun hasPurchase(sku: String): Boolean = purchaseMap.containsKey(sku)
    fun hasDetails(sku: String): Boolean = skuMap.containsKey(sku)

    fun erasePurchase(sku: String) {
        purchaseMap.remove(sku)
    }

    val allOwnedSkus: List<String> get() = purchaseMap.keys.toList()
    val allPurchases: List<Purchase> get() = purchaseMap.values.toList()

    fun getAllOwnedSkus(itemType: String): List<String> {
        return purchaseMap.values.filter { it.itemType == itemType }.map { it.sku }
    }

    fun addSkuDetails(d: SkuDetails) {
        skuMap[d.sku] = d
    }

    fun addPurchase(p: Purchase) {
        purchaseMap[p.sku] = p
    }
}
package com.javadoh.plantasmedicinales.utils

import android.app.Activity
import android.util.Log
import com.android.billingclient.api.*
import com.javadoh.plantasmedicinales.io.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingManager(private val activity: Activity, private val billingListener: BillingListener) {

    interface BillingListener {
        fun onBillingSetupFinished()
        fun onPurchaseSuccess()
        fun onPurchaseFailure(error: String)
    }

    private var billingClient: BillingClient = BillingClient.newBuilder(activity)
        .setListener { billingResult, purchases ->
            onPurchasesUpdated(billingResult, purchases)
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private var productDetails: ProductDetails? = null

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    queryProducts()
                    checkPurchases()
                    billingListener.onBillingSetupFinished()
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected. Retrying...")
                // In a real app, you might want to implement a retry logic here
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(Constants.REMOVE_ADD_PRODUCT)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val productDetailsResult = billingClient.queryProductDetails(params)
            val billingResult = productDetailsResult.billingResult
            val productDetailsList = productDetailsResult.productDetailsList

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetails = productDetailsList?.find { it.productId == Constants.REMOVE_ADD_PRODUCT }
                Log.d(TAG, "Product details queried: $productDetails")
            } else {
                Log.e(TAG, "Query products failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun checkPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            val purchasesResult = billingClient.queryPurchasesAsync(params)
            val billingResult = purchasesResult.billingResult
            val purchases = purchasesResult.purchasesList

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.products.contains(Constants.REMOVE_ADD_PRODUCT) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                        Constants.isAdsDisabled = true
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase)
                        }
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow() {
        val details = productDetails
        if (details == null) {
            Log.e(TAG, "Product details not available")
            billingListener.onPurchaseFailure("Product details not available")
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase")
        } else {
            Log.e(TAG, "Purchase error: ${billingResult.debugMessage}")
            billingListener.onPurchaseFailure(billingResult.debugMessage)
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            } else {
                Constants.isAdsDisabled = true
                billingListener.onPurchaseSuccess()
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged")
                Constants.isAdsDisabled = true
                billingListener.onPurchaseSuccess()
            } else {
                Log.e(TAG, "Acknowledge failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun onDestroy() {
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "BillingManager"
    }
}

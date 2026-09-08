package com.javadoh.plantasmedicinales.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.javadoh.plantasmedicinales.io.Constants
import com.javadoh.plantasmedicinales.utils.billing.IabHelper
import com.javadoh.plantasmedicinales.utils.billing.IabResult
import com.javadoh.plantasmedicinales.utils.billing.Inventory
import com.javadoh.plantasmedicinales.utils.billing.Purchase

class GoogleInAppPayUtils(private val activity: Activity) {

    companion object {
        val TAG: String = GoogleInAppPayUtils::class.java.name
        val SKU_REMOVE_ADS: String = Constants.REMOVE_ADD_PRODUCT
        const val RC_REQUEST = 10111
    }

    private var mHelper: IabHelper? = null
    private val base64EncodedPublicKey = Constants.APP_KEY_ACC
    private var isAdsDisabled = false
    private val payload = "ANY_PAYLOAD_STRING"

    fun onCreate() {
        Log.d(TAG, "Creating IAB helper.")
        mHelper = IabHelper(activity, base64EncodedPublicKey)
        mHelper?.enableDebugLogging(true)

        Log.d(TAG, "Starting setup.")
        mHelper?.startSetup(object : IabHelper.OnIabSetupFinishedListener {
            override fun onIabSetupFinished(result: IabResult) {
                Log.d(TAG, "Setup finished.")

                if (!result.isSuccess) {
                    complain("Problem setting up in-app billing: $result")
                    Constants.isInAppSetupCreated = false
                    return
                }

                if (mHelper == null) {
                    Constants.isInAppSetupCreated = false
                    return
                }

                Constants.isInAppSetupCreated = true
                Log.d(TAG, "Setup successful. Querying inventory.")

                // Passed the explicit parameters required by the Kotlin IabHelper
                mHelper?.queryInventoryAsync(true, null, mGotInventoryListener)
            }
        })
    }

    private val mGotInventoryListener = object : IabHelper.QueryInventoryFinishedListener {
        override fun onQueryInventoryFinished(result: IabResult, inventory: Inventory?) {
            Log.d(TAG, "Query inventory finished.")

            if (mHelper == null) return

            if (result.isFailure || inventory == null) {
                complain("Failed to query inventory: $result")
                return
            }

            Log.d(TAG, "Query inventory was successful.")

            val removeAdsPurchase = inventory.getPurchase(SKU_REMOVE_ADS)
            Constants.isAdsDisabled = removeAdsPurchase != null && verifyDeveloperPayload(removeAdsPurchase)
            if (Constants.isAdsDisabled) {
                removeAds()
            }

            Log.d(TAG, "User has ${if (Constants.isAdsDisabled) "REMOVED ADS" else "NOT REMOVED ADS"}")
            Log.d(TAG, "Initial inventory query finished; enabling main UI.")
        }
    }

    fun purchaseRemoveAds() {
        activity.runOnUiThread {
            mHelper?.launchPurchaseFlow(
                activity, SKU_REMOVE_ADS,
                RC_REQUEST, mPurchaseFinishedListener, payload
            )
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        Log.d(TAG, "onActivityResult($requestCode,$resultCode,$data)")
        if (mHelper == null) return true

        return if (mHelper?.handleActivityResult(requestCode, resultCode, data) != true) {
            false
        } else {
            Log.d(TAG, "onActivityResult handled by IABUtil.")
            true
        }
    }

    private fun verifyDeveloperPayload(p: Purchase): Boolean {
        // String payload = p.developerPayload
        return true
    }

    private val mPurchaseFinishedListener = object : IabHelper.OnIabPurchaseFinishedListener {
        override fun onIabPurchaseFinished(result: IabResult, purchase: Purchase?) {
            Log.d(TAG, "Purchase finished: $result, purchase: $purchase")

            if (mHelper == null) return

            if (result.isFailure || purchase == null) {
                complain("Error purchasing: $result")
                return
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.")
                return
            }

            Log.d(TAG, "Purchase successful.")

            if (purchase.sku == SKU_REMOVE_ADS) {
                removeAds()
            }
        }
    }

    private fun removeAds() {
        isAdsDisabled = true
    }

    fun onDestroy() {
        Log.d(TAG, "Destroying helper.")
        mHelper?.dispose()
        mHelper = null
    }

    private fun complain(message: String) {
        Log.e(TAG, "**** TrivialDrive Error: $message")
    }

    private fun alert(message: String) {
        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                .setMessage(message)
                .setNeutralButton("OK", null)
                .create()
                .show()
        }
    }
}
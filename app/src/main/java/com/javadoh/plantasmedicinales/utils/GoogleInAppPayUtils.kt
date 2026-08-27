package com.javadoh.plantasmedicinales.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.javadoh.plantasmedicinales.io.Constants
import com.javadoh.plantasmedicinales.utils.billing.IabHelper
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
        mHelper?.startSetup { result ->
            Log.d(TAG, "Setup finished.")

            if (!result.isSuccess) {
                complain("Problem setting up in-app billing: $result")
                Constants.isInAppSetupCreated = false
                return@startSetup
            }

            if (mHelper == null) {
                Constants.isInAppSetupCreated = false
                return@startSetup
            }

            Constants.isInAppSetupCreated = true
            Log.d(TAG, "Setup successful. Querying inventory.")
            mHelper?.queryInventoryAsync(mGotInventoryListener)
        }
    }

    private val mGotInventoryListener = IabHelper.QueryInventoryFinishedListener { result, inventory ->
        Log.d(TAG, "Query inventory finished.")

        if (mHelper == null) return@QueryInventoryFinishedListener

        if (result.isFailure) {
            complain("Failed to query inventory: $result")
            return@QueryInventoryFinishedListener
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

    private val mPurchaseFinishedListener = IabHelper.OnIabPurchaseFinishedListener { result, purchase ->
        Log.d(TAG, "Purchase finished: $result, purchase: $purchase")

        if (mHelper == null) return@OnIabPurchaseFinishedListener

        if (result.isFailure) {
            complain("Error purchasing: $result")
            return@OnIabPurchaseFinishedListener
        }
        if (!verifyDeveloperPayload(purchase)) {
            complain("Error purchasing. Authenticity verification failed.")
            return@OnIabPurchaseFinishedListener
        }

        Log.d(TAG, "Purchase successful.")

        if (purchase.sku == SKU_REMOVE_ADS) {
            removeAds()
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
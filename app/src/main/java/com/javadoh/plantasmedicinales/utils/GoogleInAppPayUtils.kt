package com.javadoh.plantasmedicinales.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.Constants

class GoogleInAppPayUtils(private val activity: Activity) : BillingManager.BillingListener {

    companion object {
        val TAG: String = GoogleInAppPayUtils::class.java.name
    }

    private var billingManager: BillingManager? = null

    fun onCreate() {
        Log.d(TAG, "Initializing BillingManager.")
        billingManager = BillingManager(activity, this)
        billingManager?.startConnection()
    }

    override fun onBillingSetupFinished() {
        Log.d(TAG, "Billing setup finished.")
        Constants.isInAppSetupCreated = true
        // Check for existing purchases is already done in BillingManager.startConnection()
    }

    override fun onPurchaseSuccess() {
        Log.d(TAG, "Purchase successful.")
        Constants.isAdsDisabled = true
        activity.runOnUiThread {
            Toast.makeText(activity, activity.getString(R.string.compra_realizada_store), Toast.LENGTH_LONG).show()
        }
    }

    override fun onPurchaseFailure(error: String) {
        Log.e(TAG, "Purchase failed: $error")
        activity.runOnUiThread {
            alert(activity.getString(R.string.errorTienda) + ": " + error)
        }
    }

    fun purchaseRemoveAds() {
        if (!Constants.isInAppSetupCreated) {
            alert(activity.getString(R.string.errorTienda))
            return
        }

        activity.runOnUiThread {
            try {
                billingManager?.launchPurchaseFlow()
            } catch (e: Exception) {
                Log.e(TAG, "Error launching purchase flow", e)
                alert(activity.getString(R.string.errorTienda))
            }
        }
    }

    // This method is no longer strictly needed for BillingClient 8.0.0 as it doesn't rely on onActivityResult,
    // but we keep it for signature compatibility with current Activities if needed, or we can remove it.
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return false // BillingClient 8.0.0 handles its own result through the listener
    }

    fun onDestroy() {
        Log.d(TAG, "Destroying billing manager.")
        billingManager?.onDestroy()
        billingManager = null
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

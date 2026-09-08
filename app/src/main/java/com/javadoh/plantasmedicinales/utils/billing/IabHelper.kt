package com.javadoh.plantasmedicinales.utils.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import com.android.vending.billing.IInAppBillingService
import org.json.JSONException
import java.util.ArrayList

class IabHelper(ctx: Context, private val mSignatureBase64: String) {
    private var mDebugLog = false
    private var mDebugTag = "IabHelper"
    private var mSetupDone = false
    private var mDisposed = false
    private var mSubscriptionsSupported = false
    private var mAsyncInProgress = false
    private var mAsyncOperation = ""
    private var mContext: Context? = ctx.applicationContext
    private var mService: IInAppBillingService? = null
    private var mServiceConn: ServiceConnection? = null
    private var mRequestCode = 0
    private var mPurchasingItemType = ""
    private var mPurchaseListener: OnIabPurchaseFinishedListener? = null

    companion object {
        const val BILLING_RESPONSE_RESULT_OK = 0
        const val BILLING_RESPONSE_RESULT_USER_CANCELED = 1
        const val BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE = 3
        const val BILLING_RESPONSE_RESULT_ITEM_UNAVAILABLE = 4
        const val BILLING_RESPONSE_RESULT_DEVELOPER_ERROR = 5
        const val BILLING_RESPONSE_RESULT_ERROR = 6
        const val BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED = 7
        const val BILLING_RESPONSE_RESULT_ITEM_NOT_OWNED = 8

        const val IABHELPER_ERROR_BASE = -1000
        const val IABHELPER_REMOTE_EXCEPTION = -1001
        const val IABHELPER_BAD_RESPONSE = -1002
        const val IABHELPER_VERIFICATION_FAILED = -1003
        const val IABHELPER_SEND_INTENT_FAILED = -1004
        const val IABHELPER_USER_CANCELLED = -1005
        const val IABHELPER_UNKNOWN_PURCHASE_RESPONSE = -1006
        const val IABHELPER_MISSING_TOKEN = -1007
        const val IABHELPER_UNKNOWN_ERROR = -1008
        const val IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE = -1009
        const val IABHELPER_INVALID_CONSUMPTION = -1010

        const val RESPONSE_CODE = "RESPONSE_CODE"
        const val RESPONSE_GET_SKU_DETAILS_LIST = "DETAILS_LIST"
        const val RESPONSE_BUY_INTENT = "BUY_INTENT"
        const val RESPONSE_INAPP_PURCHASE_DATA = "INAPP_PURCHASE_DATA"
        const val RESPONSE_INAPP_SIGNATURE = "INAPP_DATA_SIGNATURE"
        const val RESPONSE_INAPP_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST"
        const val RESPONSE_INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST"
        const val RESPONSE_INAPP_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST"
        const val INAPP_CONTINUATION_TOKEN = "INAPP_CONTINUATION_TOKEN"
        const val ITEM_TYPE_INAPP = "inapp"
        const val ITEM_TYPE_SUBS = "subs"
        const val GET_SKU_DETAILS_ITEM_LIST = "ITEM_ID_LIST"

        fun getResponseDesc(code: Int): String {
            val iabMsgs = "0:OK/1:User Canceled/2:Unknown/3:Billing Unavailable/4:Item unavailable/5:Developer Error/6:Error/7:Item Already Owned/8:Item not owned".split("/")
            val iabHelperMsgs = "0:OK/-1001:Remote exception during initialization/-1002:Bad response received/-1003:Purchase signature verification failed/-1004:Send intent failed/-1005:User cancelled/-1006:Unknown purchase response/-1007:Missing token/-1008:Unknown error/-1009:Subscriptions not available/-1010:Invalid consumption attempt".split("/")
            return when {
                code <= IABHELPER_ERROR_BASE -> {
                    val index = IABHELPER_ERROR_BASE - code
                    if (index in iabHelperMsgs.indices) iabHelperMsgs[index] else "$code:Unknown IAB Helper Error"
                }
                code < 0 || code >= iabMsgs.size -> "$code:Unknown"
                else -> iabMsgs[code]
            }
        }
    }

    fun enableDebugLogging(enable: Boolean, tag: String = "IabHelper") {
        checkNotDisposed()
        mDebugLog = enable
        mDebugTag = tag
    }

    interface OnIabSetupFinishedListener {
        fun onIabSetupFinished(result: IabResult)
    }

    fun startSetup(listener: OnIabSetupFinishedListener?) {
        checkNotDisposed()
        check(!mSetupDone) { "IAB helper is already set up." }

        logDebug("Starting in-app billing setup.")
        mServiceConn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                logDebug("Billing service disconnected.")
                mService = null
            }

            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                if (mDisposed) return
                logDebug("Billing service connected.")
                mService = IInAppBillingService.Stub.asInterface(service)
                val packageName = mContext!!.packageName
                try {
                    val response = mService!!.isBillingSupported(3, packageName, ITEM_TYPE_INAPP)
                    if (response != BILLING_RESPONSE_RESULT_OK) {
                        listener?.onIabSetupFinished(IabResult(response, "Error checking for billing v3 support."))
                        mSubscriptionsSupported = false
                        return
                    }
                    val subResponse = mService!!.isBillingSupported(3, packageName, ITEM_TYPE_SUBS)
                    mSubscriptionsSupported = subResponse == BILLING_RESPONSE_RESULT_OK
                    mSetupDone = true
                } catch (e: RemoteException) {
                    listener?.onIabSetupFinished(IabResult(IABHELPER_REMOTE_EXCEPTION, "RemoteException while setting up in-app billing."))
                    e.printStackTrace()
                    return
                }
                listener?.onIabSetupFinished(IabResult(BILLING_RESPONSE_RESULT_OK, "Setup successful."))
            }
        }

        val serviceIntent = Intent("com.android.vending.billing.InAppBillingService.BIND")
        serviceIntent.setPackage("com.android.vending")
        val intentServices = mContext!!.packageManager.queryIntentServices(serviceIntent, 0)

        if (intentServices.isNotEmpty()) {
            mContext!!.bindService(serviceIntent, mServiceConn!!, Context.BIND_AUTO_CREATE)
        } else {
            listener?.onIabSetupFinished(IabResult(BILLING_RESPONSE_RESULT_BILLING_UNAVAILABLE, "Billing service unavailable on device."))
        }
    }

    fun dispose() {
        logDebug("Disposing.")
        mSetupDone = false
        if (mServiceConn != null) {
            logDebug("Unbinding from service.")
            try {
                mContext?.unbindService(mServiceConn!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mDisposed = true
        mContext = null
        mServiceConn = null
        mService = null
        mPurchaseListener = null
    }

    private fun checkNotDisposed() {
        check(!mDisposed) { "IabHelper was disposed of, so it cannot be used." }
    }

    fun subscriptionsSupported(): Boolean {
        checkNotDisposed()
        return mSubscriptionsSupported
    }

    interface OnIabPurchaseFinishedListener {
        fun onIabPurchaseFinished(result: IabResult, info: Purchase?)
    }

    fun launchPurchaseFlow(
        act: Activity, sku: String, requestCode: Int,
        listener: OnIabPurchaseFinishedListener?, extraData: String = ""
    ) {
        launchPurchaseFlow(act, sku, ITEM_TYPE_INAPP, requestCode, listener, extraData)
    }

    private fun launchPurchaseFlow(
        act: Activity, sku: String, itemType: String, requestCode: Int,
        listener: OnIabPurchaseFinishedListener?, extraData: String
    ) {
        checkNotDisposed()
        checkSetupDone("launchPurchaseFlow")
        flagStartAsync("launchPurchaseFlow")

        if (itemType == ITEM_TYPE_SUBS && !mSubscriptionsSupported) {
            val r = IabResult(IABHELPER_SUBSCRIPTIONS_NOT_AVAILABLE, "Subscriptions are not available.")
            flagEndAsync()
            listener?.onIabPurchaseFinished(r, null)
            return
        }

        try {
            logDebug("Constructing buy intent for $sku, item type: $itemType")
            if (mService != null) {
                val buyIntentBundle = mService!!.getBuyIntent(3, mContext!!.packageName, sku, itemType, extraData)
                val response = getResponseCodeFromBundle(buyIntentBundle)
                if (response != BILLING_RESPONSE_RESULT_OK) {
                    logError("Unable to buy item, Error response: ${getResponseDesc(response)}")
                    flagEndAsync()
                    listener?.onIabPurchaseFinished(IabResult(response, "Unable to buy item"), null)
                    return
                }

                val pendingIntent = buyIntentBundle.getParcelable<PendingIntent>(RESPONSE_BUY_INTENT)
                logDebug("Launching buy intent for $sku. Request code: $requestCode")
                mRequestCode = requestCode
                mPurchaseListener = listener
                mPurchasingItemType = itemType

                act.startIntentSenderForResult(
                    pendingIntent!!.intentSender, requestCode, Intent(),
                    0, 0, 0
                )
            }
        } catch (e: SendIntentException) {
            logError("SendIntentException while launching purchase flow for sku $sku")
            e.printStackTrace()
            flagEndAsync()
            listener?.onIabPurchaseFinished(IabResult(IABHELPER_SEND_INTENT_FAILED, "Failed to send intent."), null)
        } catch (e: RemoteException) {
            logError("RemoteException while launching purchase flow for sku $sku")
            e.printStackTrace()
            flagEndAsync()
            listener?.onIabPurchaseFinished(IabResult(IABHELPER_REMOTE_EXCEPTION, "Remote exception while starting purchase flow"), null)
        }
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != mRequestCode) return false

        checkNotDisposed()
        checkSetupDone("handleActivityResult")
        flagEndAsync()

        if (data == null) {
            logError("Null data in IAB activity result.")
            mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_BAD_RESPONSE, "Null data in IAB result"), null)
            return true
        }

        val responseCode = getResponseCodeFromIntent(data)
        val purchaseData = data.getStringExtra(RESPONSE_INAPP_PURCHASE_DATA)
        val dataSignature = data.getStringExtra(RESPONSE_INAPP_SIGNATURE)

        if (resultCode == Activity.RESULT_OK && responseCode == BILLING_RESPONSE_RESULT_OK) {
            logDebug("Successful resultcode from purchase activity.")
            if (purchaseData == null || dataSignature == null) {
                logError("BUG: either purchaseData or dataSignature is null.")
                mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_UNKNOWN_ERROR, "IAB returned null purchaseData or dataSignature"), null)
                return true
            }

            try {
                val purchase = Purchase(mPurchasingItemType, purchaseData, dataSignature)
                if (!Security.verifyPurchase(mSignatureBase64!!, purchaseData, dataSignature)) {
                    logError("Purchase signature verification FAILED for sku ${purchase.sku}")
                    mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_VERIFICATION_FAILED, "Signature verification failed for sku ${purchase.sku}"), purchase)
                    return true
                }
                logDebug("Purchase signature successfully verified.")
                mPurchaseListener?.onIabPurchaseFinished(IabResult(BILLING_RESPONSE_RESULT_OK, "Success"), purchase)
            } catch (e: JSONException) {
                logError("Failed to parse purchase data.")
                e.printStackTrace()
                mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_BAD_RESPONSE, "Failed to parse purchase data."), null)
            }
        } else if (resultCode == Activity.RESULT_OK) {
            logDebug("Result code was OK but in-app billing response was not OK: ${getResponseDesc(responseCode)}")
            mPurchaseListener?.onIabPurchaseFinished(IabResult(responseCode, "Problem purchasing item."), null)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            logDebug("Purchase canceled - Response: ${getResponseDesc(responseCode)}")
            mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_USER_CANCELLED, "User canceled."), null)
        } else {
            logError("Purchase failed. Result code: $resultCode. Response: ${getResponseDesc(responseCode)}")
            mPurchaseListener?.onIabPurchaseFinished(IabResult(IABHELPER_UNKNOWN_PURCHASE_RESPONSE, "Unknown purchase response."), null)
        }
        return true
    }

    @Throws(IabException::class)
    fun queryInventory(querySkuDetails: Boolean, moreItemSkus: List<String>?): Inventory {
        checkNotDisposed()
        checkSetupDone("queryInventory")
        try {
            val inv = Inventory()
            var r = queryPurchases(inv, ITEM_TYPE_INAPP)
            if (r != BILLING_RESPONSE_RESULT_OK) throw IabException(r, "Error refreshing inventory (querying owned items).")

            if (querySkuDetails) {
                r = querySkuDetails(ITEM_TYPE_INAPP, inv, moreItemSkus)
                if (r != BILLING_RESPONSE_RESULT_OK) throw IabException(r, "Error refreshing inventory (querying prices of items).")
            }

            if (mSubscriptionsSupported) {
                r = queryPurchases(inv, ITEM_TYPE_SUBS)
                if (r != BILLING_RESPONSE_RESULT_OK) throw IabException(r, "Error refreshing inventory (querying owned subscriptions).")

                if (querySkuDetails) {
                    r = querySkuDetails(ITEM_TYPE_SUBS, inv, moreItemSkus)
                    if (r != BILLING_RESPONSE_RESULT_OK) throw IabException(r, "Error refreshing inventory (querying prices of subscriptions).")
                }
            }
            return inv
        } catch (e: RemoteException) {
            throw IabException(IABHELPER_REMOTE_EXCEPTION, "Remote exception while refreshing inventory.", e)
        } catch (e: JSONException) {
            throw IabException(IABHELPER_BAD_RESPONSE, "Error parsing JSON response while refreshing inventory.", e)
        }
    }

    interface QueryInventoryFinishedListener {
        fun onQueryInventoryFinished(result: IabResult, inv: Inventory?)
    }

    fun queryInventoryAsync(querySkuDetails: Boolean, moreSkus: List<String>?, listener: QueryInventoryFinishedListener?) {
        val handler = Handler(Looper.getMainLooper())
        checkNotDisposed()
        checkSetupDone("queryInventory")
        flagStartAsync("refresh inventory")
        Thread {
            var result = IabResult(BILLING_RESPONSE_RESULT_OK, "Inventory refresh successful.")
            var inv: Inventory? = null
            try {
                inv = queryInventory(querySkuDetails, moreSkus)
            } catch (ex: IabException) {
                result = ex.result
            }

            flagEndAsync()
            if (!mDisposed && listener != null) {
                handler.post { listener.onQueryInventoryFinished(result, inv) }
            }
        }.start()
    }

    private fun checkSetupDone(operation: String) {
        check(mSetupDone) { "IAB helper is not set up. Can't perform operation: $operation" }
    }

    private fun getResponseCodeFromBundle(b: Bundle): Int {
        val o = b.get(RESPONSE_CODE) ?: return BILLING_RESPONSE_RESULT_OK
        return when (o) {
            is Int -> o
            is Long -> o.toInt()
            else -> throw RuntimeException("Unexpected type for bundle response code: ${o.javaClass.name}")
        }
    }

    private fun getResponseCodeFromIntent(i: Intent): Int {
        val o = i.extras?.get(RESPONSE_CODE) ?: return BILLING_RESPONSE_RESULT_OK
        return when (o) {
            is Int -> o
            is Long -> o.toInt()
            else -> throw RuntimeException("Unexpected type for intent response code: ${o.javaClass.name}")
        }
    }

    private fun flagStartAsync(operation: String) {
        check(!mAsyncInProgress) { "Can't start async operation ($operation) because another async operation($mAsyncOperation) is in progress." }
        mAsyncOperation = operation
        mAsyncInProgress = true
        logDebug("Starting async operation: $operation")
    }

    private fun flagEndAsync() {
        logDebug("Ending async operation: $mAsyncOperation")
        mAsyncOperation = ""
        mAsyncInProgress = false
    }

    @Throws(JSONException::class, RemoteException::class)
    private fun queryPurchases(inv: Inventory, itemType: String): Int {
        logDebug("Querying owned items, item type: $itemType")
        var verificationFailed = false
        var continueToken: String? = null

        do {
            logDebug("Calling getPurchases with continuation token: $continueToken")
            val ownedItems = mService!!.getPurchases(3, mContext!!.packageName, itemType, continueToken)

            val response = getResponseCodeFromBundle(ownedItems)
            if (response != BILLING_RESPONSE_RESULT_OK) return response

            if (!ownedItems.containsKey(RESPONSE_INAPP_ITEM_LIST) ||
                !ownedItems.containsKey(RESPONSE_INAPP_PURCHASE_DATA_LIST) ||
                !ownedItems.containsKey(RESPONSE_INAPP_SIGNATURE_LIST)) {
                return IABHELPER_BAD_RESPONSE
            }

            val ownedSkus = ownedItems.getStringArrayList(RESPONSE_INAPP_ITEM_LIST) ?: ArrayList()
            val purchaseDataList = ownedItems.getStringArrayList(RESPONSE_INAPP_PURCHASE_DATA_LIST) ?: ArrayList()
            val signatureList = ownedItems.getStringArrayList(RESPONSE_INAPP_SIGNATURE_LIST) ?: ArrayList()

            for (i in purchaseDataList.indices) {
                val purchaseData = purchaseDataList[i]
                val signature = signatureList[i]
                val sku = ownedSkus[i]
                if (Security.verifyPurchase(mSignatureBase64, purchaseData, signature)) {
                    logDebug("Sku is owned: $sku")
                    inv.addPurchase(Purchase(itemType, purchaseData, signature))
                } else {
                    logWarn("Purchase signature verification **FAILED**. Not adding item.")
                    verificationFailed = true
                }
            }

            continueToken = ownedItems.getString(INAPP_CONTINUATION_TOKEN)
        } while (!TextUtils.isEmpty(continueToken))

        return if (verificationFailed) IABHELPER_VERIFICATION_FAILED else BILLING_RESPONSE_RESULT_OK
    }

    @Throws(RemoteException::class, JSONException::class)
    private fun querySkuDetails(itemType: String, inv: Inventory, moreSkus: List<String>?): Int {
        logDebug("Querying SKU details.")
        val skuList = ArrayList<String>()
        skuList.addAll(inv.getAllOwnedSkus(itemType))
        if (moreSkus != null) {
            for (sku in moreSkus) {
                if (!skuList.contains(sku)) {
                    skuList.add(sku)
                }
            }
        }

        if (skuList.isEmpty()) return BILLING_RESPONSE_RESULT_OK

        val querySkus = Bundle().apply { putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, skuList) }
        val skuDetails = mService!!.getSkuDetails(3, mContext!!.packageName, itemType, querySkus)

        if (!skuDetails.containsKey(RESPONSE_GET_SKU_DETAILS_LIST)) {
            val response = getResponseCodeFromBundle(skuDetails)
            return if (response != BILLING_RESPONSE_RESULT_OK) response else IABHELPER_BAD_RESPONSE
        }

        val responseList = skuDetails.getStringArrayList(RESPONSE_GET_SKU_DETAILS_LIST) ?: ArrayList()
        for (thisResponse in responseList) {
            val d = SkuDetails(itemType, thisResponse)
            logDebug("Got sku details: $d")
            inv.addSkuDetails(d)
        }
        return BILLING_RESPONSE_RESULT_OK
    }

    private fun logDebug(msg: String) {
        if (mDebugLog) Log.d(mDebugTag, msg)
    }

    private fun logError(msg: String) {
        Log.e(mDebugTag, "In-app billing error: $msg")
    }

    private fun logWarn(msg: String) {
        Log.w(mDebugTag, "In-app billing warning: $msg")
    }
}
package com.javadoh.plantasmedicinales.ui.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.appevents.AppEventsLogger
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.Constants
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.fragments.DetailResFragment
import com.javadoh.plantasmedicinales.utils.GoogleInAppPayUtils
import java.security.MessageDigest

class DetailResActivity : AppCompatActivity() {

    private var accessToken: AccessToken? = null
    private lateinit var accessTokenTracker: AccessTokenTracker
    private lateinit var hierba: HierbasBean
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var inAppPayApi: GoogleInAppPayUtils

    companion object {
        val TAG: String = DetailResActivity::class.java.name

        fun printKeyHash(context: Context): String? {
            var key: String? = null
            try {
                val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                info?.signatures?.forEach { signature ->
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    key = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                    Log.e("Key Hash=", key)
                }
            } catch (e: Exception) {
                Log.e("Exception", e.toString())
            }
            return key
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val hashKey = printKeyHash(this)
        Log.d(TAG, "KEYHASH: $hashKey")

        inAppPayApi = GoogleInAppPayUtils(this)
        try {
            if (Constants.internetOn) inAppPayApi.onCreate()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing billing", e)
        }

        AppEventsLogger.activateApp(application)

        accessToken = AccessToken.getCurrentAccessToken()
        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldToken: AccessToken?, newToken: AccessToken?) {
                accessToken = newToken
            }
        }

        accessToken?.let { Log.d(TAG, "AccessToken: ${it.token}") }

        setContentView(R.layout.detail_res_herb_activity)

        if (!Constants.isAdsDisabled) {
            mAdView = findViewById(R.id.adBannerView)
            mAdView?.loadAd(AdRequest.Builder().build())
            requestNewInterstitial()
        }

        if (savedInstanceState == null) {
            hierba = intent.getSerializableExtra("HIERBAS_BEAN") as HierbasBean
            val dataUser = intent.getStringArrayExtra("DATA_USER")

            val hierbaObject = Bundle().apply {
                putSerializable("HIERBAS_BEAN", hierba)
                putStringArray("DATA_USER", dataUser)
            }

            val fragment = DetailResFragment.newInstance(hierbaObject)
            supportFragmentManager.beginTransaction()
                .replace(R.id.detail_res_fragment_id, fragment, "")
                .addToBackStack(null)
                .commit()
        }

        if (!Constants.isAdsDisabled) {
            val viewFrameDetail = findViewById<FrameLayout>(R.id.detail_res_fragment_id)
            val viewFrameParam = viewFrameDetail.layoutParams as RelativeLayout.LayoutParams
            viewFrameParam.addRule(RelativeLayout.ABOVE, R.id.adBannerView)
            viewFrameDetail.layoutParams = viewFrameParam
        }
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_buscador, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_faq)?.isVisible = false
        menu.findItem(R.id.action_store)?.isVisible = true
        menu.findItem(R.id.action_favoritos)?.isVisible = false
        menu.findItem(R.id.action_compartir)?.isVisible = true
        menu.findItem(R.id.action_enable_disable_sound)?.isVisible = false
        
        val loginItem = menu.findItem(R.id.action_logout_facebook)
        loginItem?.isVisible = true
        if (AccessToken.getCurrentAccessToken() != null) {
            loginItem?.title = getString(R.string.action_logout_facebook)
        } else {
            loginItem?.title = getString(R.string.action_login_facebook)
        }
        
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_store -> showStoreDialog()
            R.id.action_faq -> {
                val vistaDialogoFaq = LayoutInflater.from(this).inflate(R.layout.dialog_faq_from_menu, null)
                AlertDialog.Builder(this)
                    .setView(vistaDialogoFaq)
                    .setTitle("FAQ")
                    .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }
                    .show()
            }
            R.id.action_favoritos -> Toast.makeText(this, getString(R.string.incomingFav), Toast.LENGTH_SHORT).show()
            R.id.action_compartir -> {
                var imagePath = hierba.imgurl?.replace(Regex("\\.(png|jpg|jpeg)$"), "") ?: ""
                try {
                    val uri = "@drawable/$imagePath"
                    val imageResource = resources.getIdentifier(uri, "drawable", packageName)

                    @Suppress("DEPRECATION")
                    val imageUri = Uri.parse(
                        MediaStore.Images.Media.insertImage(
                            contentResolver, BitmapFactory.decodeResource(resources, imageResource), null, null
                        )
                    )

                    val shareTxtIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tituloSharePlant) + hierba.nombre + getString(R.string.tituloSharePlantSubject))
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.tituloSharePlant) + hierba.nombre + getString(R.string.tituloSharePlant2))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(Intent.createChooser(shareTxtIntent, "Share"))
                } catch (ex: Exception) {
                    Toast.makeText(this, getString(R.string.errorGral) + ex, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.action_logout_facebook -> startActivity(Intent(this, FacebookLoginComment::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (!Constants.isAdsDisabled) {
            mInterstitialAd?.show(this)
        }
    }

    override fun onDestroy() {
        inAppPayApi.onDestroy()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!inAppPayApi.onActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showStoreDialog() {
        val vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_store_from_menu, null)
        val alertDialog = AlertDialog.Builder(this).setView(vistaDialogo).create()

        val txtTituloProducto = vistaDialogo.findViewById<TextView>(R.id.txtTituloProducto)
        val btnPagar = vistaDialogo.findViewById<Button>(R.id.buttonPay)

        txtTituloProducto.text = getString(R.string.subTituloPago)

        if (Constants.isAdsDisabled) {
            btnPagar.text = getString(R.string.compra_realizada_store)
            btnPagar.isEnabled = false
        } else {
            btnPagar.setOnClickListener {
                if (Constants.internetOn) {
                    inAppPayApi.purchaseRemoveAds()
                    alertDialog.dismiss()
                } else {
                    Toast.makeText(baseContext, getString(R.string.errorNoInternet), Toast.LENGTH_SHORT).show()
                }
            }
        }

        alertDialog.show()
    }

    private fun requestNewInterstitial() {
        InterstitialAd.load(this, getString(R.string.adintersticial), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                mInterstitialAd = ad
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() { requestNewInterstitial() }
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) { mInterstitialAd = null }
        })
    }
}
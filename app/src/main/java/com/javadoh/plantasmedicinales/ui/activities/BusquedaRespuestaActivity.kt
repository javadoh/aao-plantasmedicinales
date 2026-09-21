package com.javadoh.plantasmedicinales.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.Constants
import com.javadoh.plantasmedicinales.io.beans.HierbasBean
import com.javadoh.plantasmedicinales.ui.adapters.BusquedaRespuestaAdapter
import com.javadoh.plantasmedicinales.ui.fragments.RateDialogFragment
import com.javadoh.plantasmedicinales.utils.AsyncHttpTask
import com.javadoh.plantasmedicinales.utils.WindowInsetsHelper
import java.net.URLEncoder

class BusquedaRespuestaActivity : AppCompatActivity() {

    private var hierbasBeanList = ArrayList<HierbasBean>()
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: BusquedaRespuestaAdapter
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var showedAd = false
    private var dataUser: Array<String>? = null
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_respuesta_main)

        if (!Constants.isAdsDisabled) {
            mAdView = findViewById(R.id.adBannerView)
            mAdView?.loadAd(AdRequest.Builder().build())
            requestNewInterstitial()
        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        WindowInsetsHelper.applyStatusBarPadding(toolbar)
        WindowInsetsHelper.applyNavigationBarPadding(findViewById(R.id.rootRespuesta))

        mRecyclerView = findViewById(R.id.recycler_view)
        if (!Constants.isAdsDisabled) {
            val viewParamsLayout = mRecyclerView.layoutParams as RelativeLayout.LayoutParams
            viewParamsLayout.addRule(RelativeLayout.ABOVE, R.id.adBannerView)
            mRecyclerView.layoutParams = viewParamsLayout
        }

        mRecyclerView.layoutManager = LinearLayoutManager(this)

        val busquedaSesion = intent.extras
        val txtBusqueda = busquedaSesion?.getString("TXT_BUSQUEDA") ?: ""
        val tipoBusqueda = busquedaSesion?.getString("TXT_TIPO_BUSQUEDA") ?: ""
        dataUser = busquedaSesion?.getStringArray("DATA_USER")

        adapter = BusquedaRespuestaAdapter(this, hierbasBeanList, dataUser)
        progressBar = findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        val btnRegresar = findViewById<FloatingActionButton>(R.id.btn_regresar_detail)
        val floatLayoutParams = btnRegresar.layoutParams as RelativeLayout.LayoutParams
        if (!Constants.isAdsDisabled) floatLayoutParams.addRule(RelativeLayout.ABOVE, R.id.adBannerView)
        floatLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mRecyclerView.id)
        btnRegresar.layoutParams = floatLayoutParams
        btnRegresar.setOnClickListener { finish() }

        try {
            val encodedBusqueda = URLEncoder.encode(txtBusqueda, "utf-8")
            url = if (tipoBusqueda.equals("hierbas", ignoreCase = true)) {
                "${Constants.URL_SERVIDOR_RMT_APP_HIERBAS}${Constants.GET_HIERBAS_OPERACION}?nombre=$encodedBusqueda&localization=${getString(R.string.flagLocalization)}"
            } else {
                "${Constants.URL_SERVIDOR_RMT_APP_HIERBAS}${Constants.GET_SINTOMAS_OPERACION}?sintoma=$encodedBusqueda&localization=${getString(R.string.flagLocalization)}"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
        }

        AsyncHttpTask(this, mRecyclerView, adapter, progressBar, hierbasBeanList, findViewById(R.id.img_no_records), findViewById(R.id.img_no_conex), dataUser).execute(url)
    }

    private fun requestNewInterstitial() {
        InterstitialAd.load(this, getString(R.string.adintersticial), AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                mInterstitialAd = ad
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() { requestNewInterstitial() }
                }
                if (!showedAd) {
                    mInterstitialAd?.show(this@BusquedaRespuestaActivity)
                    showedAd = true
                }
            }
            override fun onAdFailedToLoad(error: LoadAdError) { mInterstitialAd = null }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_buscador, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_faq)?.isVisible = true
        menu.findItem(R.id.action_store)?.isVisible = false
        menu.findItem(R.id.action_compartir)?.isVisible = false
        menu.findItem(R.id.action_enable_disable_sound)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_faq) {
            val vista = LayoutInflater.from(this).inflate(R.layout.dialog_faq_from_menu, null)
            AlertDialog.Builder(this).setView(vista).setTitle("FAQ")
                .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        RateDialogFragment.show(this, supportFragmentManager)
    }
}
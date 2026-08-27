package com.javadoh.plantasmedicinales.ui.activities

BuscadorActivity.kt

Kotlin
package com.javadoh.plantasmedicinalesnaturales.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.textfield.TextInputLayout
import com.javadoh.plantasmedicinalesnaturales.R
import com.javadoh.plantasmedicinalesnaturales.io.Constants
import com.javadoh.plantasmedicinalesnaturales.utils.GoogleInAppPayUtils
import java.io.IOException
import java.util.regex.Pattern

class BuscadorActivity : AppCompatActivity(), View.OnClickListener {

    private val permissions = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.INTERNET
        // READ/WRITE_EXTERNAL_STORAGE removed or managed differently for SDK 35 compatibility
    )

    private lateinit var editTxtBusqueda: EditText
    private lateinit var btnHierbas: Button
    private lateinit var btnSintomas: Button
    private lateinit var checkBoxTerms: CheckBox
    private lateinit var layoutTextInputSearch: TextInputLayout
    private var mAdView: AdView? = null
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var inAppPayApi: GoogleInAppPayUtils

    private val player = MediaPlayer()
    private var mediaLength = 0
    private var dataUser: Array<String>? = null

    companion object {
        private const val REQUEST_CODE = 12
        val TAG: String = BuscadorActivity::class.java.name
        private var tipoDeBusqueda: String? = null
        private var inputValidationOk: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscador)

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        volumeControlStream = AudioManager.STREAM_MUSIC
        setPlayer(this)
        player.isLooping = true
        player.start()

        inAppPayApi = GoogleInAppPayUtils(this)
        editTxtBusqueda = findViewById(R.id.txt_busqueda)
        layoutTextInputSearch = findViewById(R.id.layoutTextInputSearch)
        editTxtBusqueda.addTextChangedListener(MyTextWatcher(editTxtBusqueda))

        btnHierbas = findViewById(R.id.btn_busqueda_hierbas)
        btnSintomas = findViewById(R.id.btn_busqueda_sintomas)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        checkBoxTerms = findViewById(R.id.idCheckTerms)
        checkBoxTerms.setOnClickListener {
            if (!checkBoxTerms.isChecked) {
                Toast.makeText(baseContext, getString(R.string.errorHomeCheck), Toast.LENGTH_LONG).show()
            } else {
                savePreferences("CheckBox_Value", true)
            }
        }

        btnHierbas.setOnClickListener(this)
        btnSintomas.setOnClickListener(this)

        setSupportActionBar(toolbar)
        supportActionBar?.setLogo(R.mipmap.ic_launcher)

        val textTermsConditionsHome = findViewById<TextView>(R.id.textTermsConditionsHome)
        val textTermsConditionsClickable = findViewById<TextView>(R.id.textTermsConditionsClickable)
        textTermsConditionsHome.setText(R.string.terminos_pantalla_inicio)
        textTermsConditionsClickable.isClickable = true
        textTermsConditionsClickable.movementMethod = LinkMovementMethod.getInstance()
        textTermsConditionsClickable.setOnClickListener {
            val vistaDialogo = LayoutInflater.from(this).inflate(R.layout.dialog_terms_conditions, null)
            AlertDialog.Builder(this)
                .setView(vistaDialogo)
                .setTitle(getString(R.string.titleTerms))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.btn_accept)) { dialog, _ ->
                    savePreferences("CheckBox_Value", true)
                    checkBoxTerms.isChecked = true
                    dialog.cancel()
                }
                .setNegativeButton(getString(R.string.btn_cancelar)) { dialog, _ -> dialog.cancel() }
                .show()
        }

        loadSavedPreferences()

        try {
            if (Constants.internetOn) inAppPayApi.onCreate()
            else Toast.makeText(baseContext, getString(R.string.errorNoInternet), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(baseContext, getString(R.string.errorInAppPayCreated), Toast.LENGTH_LONG).show()
        }

        if (!Constants.isAdsDisabled) {
            mAdView = findViewById(R.id.adBannerView)
            mAdView?.loadAd(AdRequest.Builder().build())
            requestNewInterstitial()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_busqueda_hierbas -> tipoDeBusqueda = "hierbas"
            R.id.btn_busqueda_sintomas -> tipoDeBusqueda = "sintomas"
        }

        try {
            if (inputValidationOk == "OK" || editTxtBusqueda.text.toString().isEmpty()) {
                if (Constants.isAdsDisabled) savePreferences("Premium_User", true)

                if (checkBoxTerms.isChecked) {
                    val intent = Intent(applicationContext, BusquedaRespuestaActivity::class.java).apply {
                        putExtra("TXT_BUSQUEDA", editTxtBusqueda.text.toString())
                        putExtra("TXT_TIPO_BUSQUEDA", tipoDeBusqueda)
                        putExtra("DATA_USER", dataUser)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(baseContext, getString(R.string.errorHomeCheck), Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(baseContext, getString(R.string.errorInputSearchChars), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, getString(R.string.errorGral), e)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_buscador, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_enable_disable_sound -> player.stop()
            R.id.action_faq -> {
                val view = LayoutInflater.from(this).inflate(R.layout.dialog_faq_from_menu, null)
                AlertDialog.Builder(this).setView(view).setTitle("FAQ")
                    .setPositiveButton("OK") { dialog, _ -> dialog.cancel() }.show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        mediaLength = player.currentPosition
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
        player.seekTo(mediaLength)
        player.start()
    }

    override fun onDestroy() {
        inAppPayApi.onDestroy()
        super.onDestroy()
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

    private fun setPlayer(context: Context) {
        try {
            val afd = context.resources.openRawResourceFd(R.raw.riverbirds)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            player.prepare()
        } catch (e: IOException) {
            Log.e(TAG, getString(R.string.errorGral), e)
        }
    }

    private fun loadSavedPreferences() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        checkBoxTerms.isChecked = prefs.getBoolean("CheckBox_Value", false)
        Constants.isAdsDisabled = prefs.getBoolean("Premium_User", false)
    }

    private fun savePreferences(key: String, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(key, value).apply()
    }

    private fun validateSearch(view: View): Boolean {
        val text = editTxtBusqueda.text.toString()
        val matcher = Pattern.compile("[^A-Za-zÑñáéíóúÁÉÍÓÚ, ]", Pattern.CASE_INSENSITIVE).matcher(text)
        return if (matcher.find()) {
            editTxtBusqueda.error = getString(R.string.errorInputSearchChars)
            if (view.requestFocus()) window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            inputValidationOk = "ERROR"
            false
        } else {
            layoutTextInputSearch.isErrorEnabled = false
            inputValidationOk = "OK"
            true
        }
    }

    private inner class MyTextWatcher(private val view: View) : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { validateSearch(view) }
    }
}
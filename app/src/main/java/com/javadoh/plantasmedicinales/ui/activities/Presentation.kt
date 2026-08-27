package com.javadoh.plantasmedicinales.ui.activities

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.javadoh.plantasmedicinales.R
import com.javadoh.plantasmedicinales.io.Constants
import java.io.IOException

class Presentation : AppCompatActivity() {

    private val player = MediaPlayer()
    private lateinit var imagePpal: ImageView

    companion object {
        val TAG: String = Presentation::class.java.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            isConnectedToInternet()
        } catch (e: Exception) {
            Log.d(TAG, "Information: Google Play Services needed.")
        }

        setContentView(R.layout.presentation_fade_in_out)
        imagePpal = findViewById(R.id.imgpresentacion)

        volumeControlStream = AudioManager.STREAM_MUSIC
        setPlayer(this)
        player.isLooping = false
        player.start()

        val animation = AnimationUtils.loadAnimation(this, R.anim.fadein)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                val animationEnd = AnimationUtils.loadAnimation(this@Presentation, R.anim.fadeout)
                imagePpal.startAnimation(animationEnd)

                animationEnd.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        startActivity(Intent(applicationContext, BuscadorActivity::class.java))
                        finish()
                    }
                })
            }
        })
        imagePpal.startAnimation(animation)
    }

    private fun isConnectedToInternet() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = cm.getNetworkCapabilities(network)
        Constants.internetOn = capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                )
    }

    private fun setPlayer(context: Context) {
        try {
            val afd = context.resources.openRawResourceFd(R.raw.javadoh)
            player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
            player.prepare()
        } catch (e: IOException) {
            Log.e(TAG, getString(R.string.errorGral), e)
        }
    }
}
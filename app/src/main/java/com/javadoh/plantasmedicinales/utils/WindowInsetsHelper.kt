package com.javadoh.plantasmedicinales.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Since targetSdk 35+ enforces edge-to-edge rendering, content draws behind the
 * status bar/camera cutout and the navigation bar. These helpers pad specific views
 * so toolbars stay below the status bar and bottom content (e.g. floating buttons)
 * stays above the navigation bar, without giving up the edge-to-edge background.
 */
object WindowInsetsHelper {

    fun applyStatusBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    fun applyNavigationBarPadding(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
    }
}

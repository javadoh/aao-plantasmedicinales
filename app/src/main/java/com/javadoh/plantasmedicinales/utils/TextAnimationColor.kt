package com.javadoh.plantasmedicinales.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.javadoh.plantasmedicinales.R

class TextAnimationColor(private val mContext: Activity) : View.OnTouchListener {

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        if (view !is TextView) return false

        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                view.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryText))
                view.setLineSpacing(1f, 1f)
                view.background = ContextCompat.getDrawable(mContext, R.drawable.detail)
                view.textSize = 16f
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                view.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryText))
                view.setBackgroundColor(0x00000000)
                view.textSize = 12f
            }
        }
        return false
    }
}
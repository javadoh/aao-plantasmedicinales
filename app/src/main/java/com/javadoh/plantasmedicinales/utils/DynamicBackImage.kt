package com.javadoh.plantasmedicinales.utils

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import pl.droidsonroids.gif.GifImageView
import kotlin.math.min

class DynamicBackImage(context: Context, attrs: AttributeSet?) : GifImageView(context, attrs) {

    private var mAdjustViewBounds: Boolean = false

    override fun setAdjustViewBounds(adjustViewBounds: Boolean) {
        mAdjustViewBounds = adjustViewBounds
        super.setAdjustViewBounds(adjustViewBounds)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mDrawable = drawable
        if (mDrawable == null || !mAdjustViewBounds) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }

        val mDrawableWidth = mDrawable.intrinsicWidth
        val mDrawableHeight = mDrawable.intrinsicHeight
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        if (heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY) {
            val width = heightSize * mDrawableWidth / mDrawableHeight
            if (isInScrollingContainer()) {
                setMeasuredDimension(width, heightSize)
            } else {
                setMeasuredDimension(min(width, widthSize), min(heightSize, heightSize))
            }
        } else if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            val height = widthSize * mDrawableHeight / mDrawableWidth
            if (isInScrollingContainer()) {
                setMeasuredDimension(widthSize, height)
            } else {
                setMeasuredDimension(min(widthSize, widthSize), min(height, heightSize))
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun isInScrollingContainer(): Boolean {
        var p = parent
        while (p is ViewGroup) {
            if (p.shouldDelayChildPressedState()) return true
            p = p.parent
        }
        return false
    }
}
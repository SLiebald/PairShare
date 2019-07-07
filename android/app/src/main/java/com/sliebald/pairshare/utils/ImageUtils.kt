package com.sliebald.pairshare.utils

import android.graphics.Bitmap

/**
 * Helper class for manipulating images.
 */
object ImageUtils {
    /**
     * Resize the given bitmap to a smaller size (Create a thumbnail).
     */
    fun getResizedBitmap(img: Bitmap, maxSize: Int): Bitmap {
        var width = img.width
        var height = img.height

        val ratio = width.toDouble() / height.toDouble()
        if (ratio > 1) {
            width = maxSize
            height = (width / ratio).toInt()
        } else {
            height = maxSize
            width = (height * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(img,
                width,
                height,
                true)
    }
}

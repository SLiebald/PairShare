package com.sliebald.pairshare.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import kotlin.math.roundToInt


/**
 * Helper class for manipulating images.
 * Based on https://stackoverflow.com/questions/14066038/why-does-an-image-captured-using-camera
 * -intent-gets-rotated-on-some-devices-on-a
 */
object ImageUtils {

    fun getResizedBitmap(uri: Uri, maxSize: Int, context: Context): Bitmap {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(imageStream, null, options)
        imageStream?.close()

        options.inSampleSize = calculateInSampleSize(options, maxSize)
        options.inJustDecodeBounds = false

        imageStream = context.contentResolver.openInputStream(uri)
        var img = BitmapFactory.decodeStream(imageStream, null, options)

        img = rotateImageIfRequired(context, img!!, uri)
        return img
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxSize: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > maxSize || width > maxSize) {

            // Calculate ratios of height and width to requested height and width
            val heightRatio = (height.toFloat() / maxSize.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / maxSize.toFloat()).roundToInt()

            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).
            val totalPixels = (width * height).toFloat()

            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = maxSize * maxSize * 2

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(context: Context, img: Bitmap, uri: Uri): Bitmap {

        val input = context.contentResolver.openInputStream(uri)
        val ei = ExifInterface(input)

        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }





}

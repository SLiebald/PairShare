package com.sliebald.pairshare.ui.imagePopup

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.ProgressBar
import androidx.palette.graphics.Palette
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import com.google.firebase.storage.StorageReference
import com.sliebald.pairshare.MyApplication
import com.sliebald.pairshare.R
import com.sliebald.pairshare.utils.GlideApp

// adapted from http://www.tutorialsface.com/2017/10/whatsapp-like-full-screen-imageview-android-with-zoom-blur-bg-in-popup-window-example-tutorial/

class ImagePopup(mContext: Context, layout: Int, v: View, imageRef: StorageReference, bitmap:
Bitmap?) : PopupWindow((mContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        .inflate(R.layout.popup_image, null), MATCH_PARENT, MATCH_PARENT) {

    internal var view: View
    internal var photoView: PhotoView
    internal var loading: ProgressBar
    private var parent: ViewGroup


    init {

        elevation = 5.0f
        this.view = contentView
        val closeButton = view.findViewById(R.id.ib_close) as ImageButton
        isOutsideTouchable = true

        isFocusable = true
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener { dismiss() }

        //---------Begin customising this popup--------------------

        photoView = view.findViewById(R.id.image)
        loading = view.findViewById(R.id.loading)
        photoView.maximumScale = 6f
        parent = photoView.parent as ViewGroup
        // ImageUtils.setZoomable(imageView);
        //----------------------------
        if (bitmap != null) {
            loading.visibility = View.GONE
            onPalette(Palette.from(bitmap).generate())
            photoView.setImageBitmap(bitmap)
        } else {
            loading.isIndeterminate = true
            loading.visibility = View.VISIBLE

            val progressDrawable = CircularProgressDrawable(MyApplication.context)

            GlideApp.with(MyApplication.context)
                    .asBitmap()
                    .load(imageRef)
                    .placeholder(progressDrawable)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target:
                        Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                            loading.isIndeterminate = false
                            loading.setBackgroundColor(Color.LTGRAY)
                            return false
                        }

                        override fun onResourceReady(resource: Bitmap?, model: Any?, target:
                        Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            onPalette(Palette.from(resource!!).generate())
                            photoView.setImageBitmap(resource)
                            loading.visibility = View.GONE
                            return false
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(photoView)

            showAtLocation(v, Gravity.CENTER, 0, 0)
        }
        //------------------------------

    }

    //TODO: fix palette as background?
    fun onPalette(palette: Palette?) {
        if (null != palette) {
            val parent = photoView.parent.parent as ViewGroup
            parent.setBackgroundColor(palette.getDarkVibrantColor(Color.GRAY))
        }
    }

    companion object {
        private val instance: ImagePopup? = null
    }

}
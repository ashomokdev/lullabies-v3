package com.ashomok.lullabies.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.ashomok.lullabies.AlbumArtCache
import kotlinx.coroutines.*
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.URL
import kotlin.math.min

class BitmapHelperKt {
    companion object {
        private val TAG = LogHelper.makeLogTag(BitmapHelperKt::class.java)

        // Max read limit that we allow our input stream to mark/reset.
        private const val MAX_READ_LIMIT_PER_IMG = 1024 * 1024

        @JvmStatic
        fun fetchAndRescaleBitmap(
                path: String, MAX_ART_WIDTH: Int, MAX_ART_HEIGHT: Int,
                MAX_ART_WIDTH_ICON: Int, MAX_ART_HEIGHT_ICON: Int,
                callback: (Array<Bitmap>) -> Unit) {
            CoroutineScope(Job() + Dispatchers.Main).launch {

                val bitmap: Bitmap? = fetchAndRescale(path, MAX_ART_WIDTH, MAX_ART_HEIGHT)
                val icon: Bitmap? = scaleBitmap(bitmap!!, MAX_ART_WIDTH_ICON, MAX_ART_HEIGHT_ICON)
                val result: Array<Bitmap> = arrayOf(bitmap, icon!!)
                callback(result)
            }
        }

        private suspend fun fetchAndRescale(path: String, width: Int, height: Int): Bitmap? {
            // Move the execution of the coroutine to the I/O dispatcher
            return withContext(Dispatchers.IO) {
                var inputStream: InputStream? = null
                try {
                    val url: URL
                    url = if (path.contains("http")) { //provided by RemoteJSONSource
                        URL(path)
                    } else { //provided by LocalJSONSource
                        val aClass: Class<out AlbumArtCache?> = AlbumArtCache::class.java
                        aClass.getResource(path)
                    }
                    val urlConnection = url.openConnection()
                    inputStream = BufferedInputStream(urlConnection.getInputStream())
                    inputStream.mark(MAX_READ_LIMIT_PER_IMG)
                    val scaleFactor = findScaleFactor(width, height, inputStream)
                    LogHelper.d(TAG, "Scaling bitmap ", path, " by factor ", scaleFactor,
                            " to support ", width, "x", height, "requested dimension")
                    inputStream.reset()
                    scaleBitmap(scaleFactor, inputStream)
                } finally {
                    inputStream?.close()
                }
            }
        }

        private suspend fun scaleBitmap(src: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap? {
            return withContext(Dispatchers.IO) {
                val scaleFactor = Math.min(
                        maxWidth.toDouble() / src.width, maxHeight.toDouble() / src.height)
                Bitmap.createScaledBitmap(src,
                        (src.width * scaleFactor).toInt(), (src.height * scaleFactor).toInt(), false)
            }
        }

        private fun scaleBitmap(scaleFactor: Int, inputStream: InputStream?): Bitmap? {

            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false
            bmOptions.inSampleSize = scaleFactor
            return BitmapFactory.decodeStream(inputStream, null, bmOptions)
        }

        private fun findScaleFactor(targetW: Int, targetH: Int, `is`: InputStream?): Int {

            // Get the dimensions of the bitmap
            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`is`, null, bmOptions)
            val actualW = bmOptions.outWidth
            val actualH = bmOptions.outHeight

            // Determine how much to scale down the image
            return min(actualW / targetW, actualH / targetH)
        }
    }
}
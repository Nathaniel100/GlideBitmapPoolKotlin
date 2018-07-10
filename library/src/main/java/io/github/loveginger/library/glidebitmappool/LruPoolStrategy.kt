package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap

interface LruPoolStrategy {
  fun put(bitmap: Bitmap)

  fun get(width: Int, height: Int, config: Bitmap.Config)

  fun removeLast()

  fun logBitmap(bitmap: Bitmap): String

  fun logBitmap(width: Int, height: Int, config: Bitmap.Config)

  fun getSize(bitmap: Bitmap)
}
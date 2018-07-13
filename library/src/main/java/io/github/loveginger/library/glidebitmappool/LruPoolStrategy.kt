package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap

interface LruPoolStrategy {

  fun put(bitmap: Bitmap)

  fun get(width: Int, height: Int, config: Bitmap.Config?): Bitmap?

  fun removeLast(): Bitmap?

  fun createBitmap(width: Int, height: Int, config: Bitmap.Config?): Bitmap

  fun logBitmap(bitmap: Bitmap): String

  fun logBitmap(width: Int, height: Int, config: Bitmap.Config?): String

  fun getSize(bitmap: Bitmap?): Long
}
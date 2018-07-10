package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap

interface BitmapPool {
  fun get(width: Int, height: Int, config: Bitmap.Config?): Bitmap

  fun put(bitmap: Bitmap)

  fun getMaxSize(): Long

  fun setSizeMultipiler(sizeMultiplier: Float)

  fun clearMemory()

  fun trimMemory(/*ComponentCallbacks2.TrimMemoryLevel*/ level: Int)
}
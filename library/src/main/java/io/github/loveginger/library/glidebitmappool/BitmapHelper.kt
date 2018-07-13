package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import android.os.Build

interface BitmapHelper {
  fun createBitmap(width: Int, height: Int, config: Bitmap.Config?): Bitmap

  fun getBitmapString(bitmap: Bitmap): String

  fun getBitmapString(width: Int, height: Int, config: Bitmap.Config?): String

  fun getBitmapByteSize(bitmap: Bitmap): Int

  fun getBitmapByteSize(width: Int, height: Int, config: Bitmap.Config?): Int

  fun getBytesPerPixel(config: Bitmap.Config?): Int

}

object BitmapHelperObject : BitmapHelper {

  val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888

  override fun createBitmap(width: Int, height: Int, config: Bitmap.Config?): Bitmap {
    return Bitmap.createBitmap(width, height, config ?: DEFAULT_CONFIG)
  }

  override fun getBitmapString(bitmap: Bitmap): String {
    return getBitmapString(bitmap.width, bitmap.height, bitmap.config)
  }

  override fun getBitmapString(width: Int, height: Int, config: Bitmap.Config?): String {
    return "[$width x $height], $config"
  }

  override fun getBitmapByteSize(bitmap: Bitmap): Int {
    if (bitmap.isRecycled) {

    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      // Workaround for KitKat initial release NPE in Bitmap, fixed in MR1.
      try {
        return bitmap.allocationByteCount
      } catch (e: NullPointerException) {
        // Do nothing
      }
    }
    return bitmap.height * bitmap.rowBytes
  }

  override fun getBitmapByteSize(width: Int, height: Int, config: Bitmap.Config?): Int {
    return width * height * getBytesPerPixel(config)
  }

  override fun getBytesPerPixel(config: Bitmap.Config?): Int {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      if (config == Bitmap.Config.RGBA_F16) {
        return 8
      }
    }
    return when (config) {
      Bitmap.Config.ALPHA_8 -> 1
      Bitmap.Config.RGB_565, Bitmap.Config.ARGB_4444 -> 2
      Bitmap.Config.ARGB_8888 -> 4
      else -> 4
    }
  }
}
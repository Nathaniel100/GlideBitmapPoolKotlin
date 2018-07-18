package io.github.loveginger.library.glidebitmappool

import android.annotation.TargetApi
import android.content.ComponentCallbacks2
import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Color
import android.os.Build
import android.support.annotation.VisibleForTesting
import java.util.Collections
import kotlin.collections.HashSet

class LruBitmapPool(private val initializeMaxSize: Long,
    private val allowedConfigs: Set<Bitmap.Config?> = defaultAllowedConfigs,
    private val strategy: LruPoolStrategy = defaultStrategy) : BitmapPool {
  private val tracker: BitmapTracker = NullBitmapTracker()

  @JvmField
  @VisibleForTesting
  var maxSize: Long = initializeMaxSize

  @JvmField
  @VisibleForTesting
  var currentSize: Long = 0

  @JvmField
  @VisibleForTesting
  var hits: Int = 0

  @JvmField
  @VisibleForTesting
  var misses: Int = 0

  @JvmField
  @VisibleForTesting
  var puts: Int = 0

  @JvmField
  @VisibleForTesting
  var evictions: Int = 0

  @Synchronized
  override fun get(width: Int, height: Int, config: Config?): Bitmap {
    return getDirtyOrNull(width, height, config).apply { this?.eraseColor(Color.TRANSPARENT) }
        ?: strategy.createBitmap(width, height, config)
  }

  @Synchronized
  override fun getDirty(width: Int, height: Int, config: Config?): Bitmap {
    return getDirtyOrNull(width, height, config) ?: strategy.createBitmap(width, height, config)
  }


  @Synchronized
  private fun getDirtyOrNull(width: Int, height: Int, config: Config?): Bitmap? {
    assertNotHardwareConfig(config)
    val result = strategy.get(width, height, config ?: DEFAULT_CONFIG)
    if (result == null) {
      if (Logger.isLoggable(TAG, Logger.DEBUG)) {
        Logger.d(TAG, "Missing bitmap=${strategy.logBitmap(width, height, config)}")
      }
      misses++
    } else {
      hits++
      currentSize -= strategy.getSize(result)
      tracker.remove(result)
      normalize(result)
    }

    if (Logger.isLoggable(TAG, Logger.VERBOSE)) {
      Logger.v(TAG, "Get bitmap=${strategy.logBitmap(width, height, config)}")
    }

    dump()
    return result
  }

  private fun normalize(bitmap: Bitmap) {
    bitmap.setHasAlpha(true)
    maybeSetPreMultipied(bitmap)
  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private fun maybeSetPreMultipied(bitmap: Bitmap) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      bitmap.isPremultiplied = true
    }
  }


  @Synchronized
  override fun put(bitmap: Bitmap) {
    if (bitmap.isRecycled) {
      throw IllegalStateException("Cannot pool recycled bitmap")
    }

    if (!bitmap.isMutable || strategy.getSize(bitmap) > maxSize || !allowedConfigs.contains(
            bitmap.config)) {
      if (Logger.isLoggable(TAG, Logger.VERBOSE)) {
        Logger.v(TAG, "Reject bitmap from pool," +
            " bitmap: ${strategy.logBitmap(bitmap)}," +
            " is mutable: ${bitmap.isMutable}," +
            " is allowed config: ${allowedConfigs.contains(bitmap.config)}")
      }
      bitmap.recycle()
      return
    }

    val size = strategy.getSize(bitmap)
    strategy.put(bitmap)
    tracker.add(bitmap)
    puts++
    currentSize += size

    if (Logger.isLoggable(TAG, Logger.VERBOSE)) {
      Logger.v(TAG, "Put bitmap in pool=${strategy.logBitmap(bitmap)}")
    }

    dump()

    evict()
  }

  override fun getMaxSize(): Long {
    return maxSize
  }

  override fun setSizeMultipiler(sizeMultiplier: Float) {
    maxSize = (initializeMaxSize * sizeMultiplier).toLong()
    evict()
  }

  override fun clearMemory() {
    if (Logger.isLoggable(TAG, Logger.DEBUG)) {
      Logger.d(TAG, "clearMemory")
    }
    trimToSize(0)
  }

  override fun trimMemory(level: Int) {
    if (Logger.isLoggable(TAG, Logger.DEBUG)) {
      Logger.d(TAG, "trimMemory, level=$level")
    }
    if (level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
      clearMemory()
    } else if (level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN
        || level == ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
      trimToSize(getMaxSize() / 2)
    }
  }

  private fun dump() {
    if (Logger.isLoggable(TAG, Logger.VERBOSE)) {
      dumpUnchecked()
    }
  }

  private fun dumpUnchecked() {
    Logger.v(TAG, "Hits=$hits, misses=$misses, puts=$misses, evictions=$evictions," +
        " currentSize=$currentSize, maxSize=$maxSize\nStrategy=$strategy")
  }

  private fun evict() {
    trimToSize(maxSize)
  }

  @Synchronized
  private fun trimToSize(size: Long) {
    while (currentSize > size) {
      val removed = strategy.removeLast()
      if (removed == null) {
        if (Logger.isLoggable(TAG, Logger.WARN)) {
          Logger.w(TAG, "Size mismatch, resetting")
          dumpUnchecked()
        }
        currentSize = 0
        return
      }
      tracker.remove(removed)
      currentSize -= strategy.getSize(removed)
      evictions++
      if (Logger.isLoggable(TAG, Logger.DEBUG)) {
        Logger.d(TAG, "Evicting bitmap=${strategy.logBitmap(removed)}")
      }
      dump()
      removed.recycle()
    }
  }

  private interface BitmapTracker {
    fun add(bitmap: Bitmap)

    fun remove(bitmap: Bitmap)
  }

  private class NullBitmapTracker : BitmapTracker {
    override fun add(bitmap: Bitmap) {
      // do nothing
    }

    override fun remove(bitmap: Bitmap) {
      // do nothing
    }

  }

  companion object {
    const val TAG = "LruBitmapPool"
    val DEFAULT_CONFIG = Bitmap.Config.ARGB_8888

    val defaultAllowedConfigs: Set<Bitmap.Config?>
      get() {
        val configs: HashSet<Bitmap.Config?> = HashSet()
        configs.addAll(Bitmap.Config.values())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          configs.add(null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          configs.remove(Bitmap.Config.HARDWARE)
        }
        return Collections.unmodifiableSet(configs)
      }

    val defaultStrategy: LruPoolStrategy
      get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          SizeConfigStrategy(BitmapHelperObject)
        } else {
          AttributeStrategy(BitmapHelperObject)
        }
      }


    @TargetApi(Build.VERSION_CODES.O)
    private fun assertNotHardwareConfig(config: Config?) {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        return
      }

      if (config == Bitmap.Config.HARDWARE) {
        throw IllegalArgumentException("Cannot create a mutable Bitmap with config: $config.")
      }
    }
  }
}
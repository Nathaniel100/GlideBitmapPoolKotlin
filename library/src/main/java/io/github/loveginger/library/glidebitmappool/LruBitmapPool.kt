package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.os.Build
import java.util.Collections

class LruBitmapPool(private val initializeMaxSize: Long,
    private val allowedConfigs: Set<Bitmap.Config?> = defaultAllowedConfigs,
    private val strategy: LruPoolStrategy = defaultStrategy) : BitmapPool {
  private val tracker: BitmapTracker = NullBitmapTracker()

  private var maxSize: Long = initializeMaxSize
  private var currentSize: Long = 0
  private var hits: Int = 0
  private var misses: Int = 0
  private var puts: Int = 0
  private var evictions: Int = 0

  @Synchronized
  override fun get(width: Int, height: Int, config: Config?): Bitmap {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  @Synchronized
  override fun getDirty(width: Int, height: Int, config: Config?): Bitmap {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
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
    return maxSize;
  }

  override fun setSizeMultipiler(sizeMultiplier: Float) {
    maxSize = (initializeMaxSize * sizeMultiplier).toLong()
    evict()
  }

  override fun clearMemory() {
    trimToSize(0)
  }

  override fun trimMemory(level: Int) {
    TODO(
        "not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  private fun dump() {

  }

  private fun evict() {
    trimToSize(maxSize)
  }

  @Synchronized
  private fun trimToSize(size: Long) {
    while (currentSize > size) {
      val removed = strategy.removeLast()
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

    val defaultAllowedConfigs: Set<Bitmap.Config?>
      get() {
        val configs: HashSet<Bitmap.Config?> = HashSet()
        configs.addAll(Bitmap.Config.values())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          configs.add(null)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
          configs.remove(Bitmap.Config.HARDWARE);
        }
        return Collections.unmodifiableSet(configs)
      }

    val defaultStrategy: LruPoolStrategy
      get() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          TODO("SizeConfigStrategy")
        } else {
          TODO("AttributeStrategy")
        }
      }
  }
}
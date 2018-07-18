package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import android.os.Build
import android.support.annotation.RequiresApi
import java.util.*
import kotlin.collections.HashMap


/*
SizeConfigStrategy

- 实现了LruPoolStrategy
- Key: 作为索引的key，找到对应的bitmap， 实现了poolable
- KeyPool: Key池，用于管理回收key，避免多次GC
- GroupedLinkedMap<Key, Bitmap>: 提供了LRU实现
- Map<Bitmap.Config, NavigableMap<Integer, Integer>>: key为Config，value是一个有序的Map，其key为bitmap的size，value是已存放的个数；
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class SizeConfigStrategy(private val bitmapHelper: BitmapHelper) : LruPoolStrategy {
  companion object {
    private const val MAX_SIZE_MULTIPLE = 80
    private val ARGB_8888_IN_CONFIGS: Array<Bitmap.Config>

    init {
      var result = arrayOf(Bitmap.Config.ARGB_8888)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result = Arrays.copyOf(result, result.size + 1)
        result[result.size - 1] = Bitmap.Config.RGBA_F16
      }
      ARGB_8888_IN_CONFIGS = result
    }

    private val RGBA_F16_IN_CONFIGS = ARGB_8888_IN_CONFIGS

    // We probably could allow ARGB_4444 and RGB_565 to decode into each other, but ARGB_4444 is
    // deprecated and we'd rather be safe.
    private val RGB_565_IN_CONFIGS = arrayOf(Bitmap.Config.RGB_565)
    private val ARGB_4444_IN_CONFIGS = arrayOf(Bitmap.Config.ARGB_4444)
    private val ALPHA_8_IN_CONFIGS = arrayOf(Bitmap.Config.ALPHA_8)

    private fun getInConfigs(requested: Bitmap.Config): Array<Bitmap.Config> {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (Bitmap.Config.RGBA_F16 == requested) {
          return RGBA_F16_IN_CONFIGS
        }
      }

      return when (requested) {
        Bitmap.Config.ARGB_8888 -> ARGB_8888_IN_CONFIGS
        Bitmap.Config.RGB_565 -> RGB_565_IN_CONFIGS
        Bitmap.Config.ARGB_4444 -> ARGB_4444_IN_CONFIGS
        Bitmap.Config.ALPHA_8 -> ALPHA_8_IN_CONFIGS
        else -> arrayOf(requested)
      }
    }
  }

  private val keyPool = KeyPool.create(bitmapHelper)
  private val groupedLinkedMap = GroupedLinkedMap<Key, Bitmap>()
  private val sortedSizes: MutableMap<Bitmap.Config, NavigableMap<Int, Int>> = HashMap()

  override fun put(bitmap: Bitmap) {
    val size = bitmapHelper.getBitmapByteSize(bitmap)
    val key = keyPool.get(size, bitmap.config)
    groupedLinkedMap.put(key, bitmap)
    val sizes = getSizesForConfig(bitmap.config)
    val current = sizes[size]
    sizes[size] = if (current == null) 1 else current + 1
  }

  private fun getSizesForConfig(config: Bitmap.Config): NavigableMap<Int, Int> {
    return sortedSizes[config] ?: TreeMap<Int, Int>().also { sortedSizes[config] = it }
  }

  override fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap? {
    val size = bitmapHelper.getBitmapByteSize(width, height, config)
    val bestKey = findBestKey(size, config)

    val result = groupedLinkedMap.get(bestKey)
    if (result != null) {
      // Decrement must be called before reconfigure
      decrementBitmapOfSize(bestKey.size, result)
      result.reconfigure(width, height, result.config ?: Bitmap.Config.ARGB_8888)
    }
    return result
  }

  private fun findBestKey(size: Int, config: Bitmap.Config): Key {
    var result = keyPool.get(size, config)
    for (possibleConfig in getInConfigs(config)) {
      val sizesForPossibleConfig = getSizesForConfig(possibleConfig)
      val possibleSize = sizesForPossibleConfig.ceilingKey(size)
      if (possibleSize != null && possibleSize < size * MAX_SIZE_MULTIPLE) {
        if (possibleSize != size || possibleConfig != config) {
          keyPool.offer(result)
          result = keyPool.get(possibleSize, possibleConfig)
        }
        break
      }
    }
    return result
  }

  override fun removeLast(): Bitmap? {
    val removed = groupedLinkedMap.removeLast()
    if (removed != null) {
      val removedSize = bitmapHelper.getBitmapByteSize(removed)
      decrementBitmapOfSize(removedSize, removed)
    }
    return removed
  }

  private fun decrementBitmapOfSize(size: Int, bitmap: Bitmap) {
    val sizes = getSizesForConfig(bitmap.config)
    val current = sizes[size] ?: throw NullPointerException(
        "Tried to decrement empty size, size: $size, removed: ${logBitmap(bitmap)}, this: $this")
    when (current) {
      1 -> sizes.remove(size)
      else -> sizes[size] = current - 1
    }
  }

  override fun createBitmap(width: Int, height: Int, config: Bitmap.Config?): Bitmap {
    return bitmapHelper.createBitmap(width, height, config)
  }

  override fun logBitmap(bitmap: Bitmap): String {
    val size = bitmapHelper.getBitmapByteSize(bitmap)
    return bitmapHelper.getBitmapString(size, bitmap.config)
  }

  override fun logBitmap(width: Int, height: Int, config: Bitmap.Config?): String {
    val size = bitmapHelper.getBitmapByteSize(width, height, config)
    return bitmapHelper.getBitmapString(size, config)
  }

  override fun getSize(bitmap: Bitmap?): Long {
    return if (bitmap == null) 0 else bitmapHelper.getBitmapByteSize(bitmap).toLong()
  }

  override fun toString(): String {
    val sb = StringBuilder()
        .append("SizeConfigStrategy{groupedMap=")
        .append(groupedLinkedMap)
        .append(", sortedSizes=(")
    for ((k, v) in sortedSizes) {
      sb.append(k).append('[').append(v).append("], ")
    }
    if (!sortedSizes.isEmpty()) {
      sb.replace(sb.length - 2, sb.length, "")
    }
    return sb.append(")}").toString()
  }

  private class Key(private val bitmapHelper: BitmapHelper,
      private val keyPool: KeyPool) : Poolable {
    var size: Int = 0
    private var config: Bitmap.Config? = null

    fun init(size: Int, config: Bitmap.Config?) {
      this.size = size
      this.config = config
    }

    override fun offer() {
      keyPool.offer(this)
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Key) return false

      if (size != other.size) return false
      if (config != other.config) return false

      return true
    }

    override fun hashCode(): Int {
      var result = size
      result = 31 * result + (config?.hashCode() ?: 0)
      return result
    }

    override fun toString(): String {
      return bitmapHelper.getBitmapString(size, config)
    }
  }

  private class KeyPool(private val bitmapHelper: BitmapHelper) : BaseKeyPool<Key>() {

    companion object {
      fun create(bitmapHelper: BitmapHelper) = KeyPool(bitmapHelper)
    }

    fun get(size: Int, config: Bitmap.Config?): Key = get().apply { init(size, config) }

    override fun create() = Key(bitmapHelper, this)
  }

}
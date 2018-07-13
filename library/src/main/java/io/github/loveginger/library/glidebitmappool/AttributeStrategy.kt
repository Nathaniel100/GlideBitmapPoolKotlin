package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap

class AttributeStrategy(private val bitmapHelper: BitmapHelper) : LruPoolStrategy {
  private val keyPool = KeyPool.create(bitmapHelper)
  private val groupedLinkedMap = GroupedLinkedMap<Key, Bitmap>()

  override fun put(bitmap: Bitmap) {
    val key = keyPool.get(bitmap.width, bitmap.height, bitmap.config)
    groupedLinkedMap.put(key, bitmap)
  }

  override fun get(width: Int, height: Int, config: Bitmap.Config?): Bitmap? {
    val key = keyPool.get(width, height, config)
    return groupedLinkedMap.get(key)
  }

  override fun removeLast(): Bitmap? {
    return groupedLinkedMap.removeLast()
  }

  override fun createBitmap(width: Int, height: Int, config: Bitmap.Config?): Bitmap {
    return bitmapHelper.createBitmap(width, height, config)
  }

  override fun logBitmap(bitmap: Bitmap): String {
    return bitmapHelper.getBitmapString(bitmap)
  }

  override fun logBitmap(width: Int, height: Int, config: Bitmap.Config?): String {
    return bitmapHelper.getBitmapString(width, height, config)
  }

  override fun getSize(bitmap: Bitmap?): Long {
    return if (bitmap == null) 0 else bitmapHelper.getBitmapByteSize(bitmap).toLong()
  }

  private class Key(private val bitmapHelper: BitmapHelper,
      private val keyPool: KeyPool) : Poolable {
    var width: Int = 0
    var height: Int = 0
    var config: Bitmap.Config? = null

    fun init(width: Int, height: Int, config: Bitmap.Config?) {
      this.width = width
      this.height = height
      this.config = config
    }

    override fun offer() {
      keyPool.offer(this)
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Key) return false

      if (width != other.width) return false
      if (height != other.height) return false
      if (config != other.config) return false

      return true
    }

    override fun hashCode(): Int {
      var result = width
      result = 31 * result + height
      result = 31 * result + (config?.hashCode() ?: 0)
      return result
    }

    override fun toString(): String {
      return bitmapHelper.getBitmapString(width, height, config)
    }
  }

  private class KeyPool private constructor(
      private val bitmapHelper: BitmapHelper) : BaseKeyPool<Key>() {
    companion object {
      fun create(bitmapHelper: BitmapHelper) = KeyPool(bitmapHelper)
    }

    fun get(width: Int, height: Int, config: Bitmap.Config?): Key {
      return get().apply { init(width, height, config) }
    }

    override fun create(): Key {
      return Key(bitmapHelper, this)
    }
  }
}
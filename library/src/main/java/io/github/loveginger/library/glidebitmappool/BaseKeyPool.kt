package io.github.loveginger.library.glidebitmappool

import java.util.*

abstract class BaseKeyPool<K : Poolable>(private val keyPool: Queue<K> = ArrayDeque()) {
  fun get(): K {
    return keyPool.poll() ?: create()
  }

  fun offer(key: K) {
    if (keyPool.size < MAX_SIZE) {
      keyPool.offer(key)
    }
  }

  abstract fun create(): K

  companion object {
    private const val MAX_SIZE = 20
  }
}
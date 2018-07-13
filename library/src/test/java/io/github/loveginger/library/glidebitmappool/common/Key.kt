package io.github.loveginger.library.glidebitmappool.common

import io.github.loveginger.library.glidebitmappool.Poolable

data class Key(val data: Int) : Poolable {
  override fun offer() {
  }
}
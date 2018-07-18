package io.github.loveginger.library.glidebitmappool

import java.util.*


class PrettyPrintTreeMap<K, V> : TreeMap<K, V>() {
  override fun toString(): String {
    val builder = StringBuilder()
    builder.append("( ")
    for ((k, v) in this) {
      builder.append("{$k:$v}, ")
    }
    if (!isEmpty()) {
      builder.replace(builder.length - 2, builder.length, "")
    }
    return builder.toString()
  }
}
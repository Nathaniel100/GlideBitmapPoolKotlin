package io.github.loveginger.library.glidebitmappool

class LinkedEntry<K, V>(val key: K?) {
  var prev: LinkedEntry<K, V> = this
  var next: LinkedEntry<K, V> = this
  private var values: ArrayList<V>? = null

  constructor() : this(null)

  fun removeLast(): V? {
    val size = size()
    return if (size > 0) values!!.removeAt(size - 1) else null
  }

  fun size() = if (values == null) 0 else values!!.size

  fun add(v: V) {
    if (values == null) {
      values = ArrayList()
    }
    values!!.add(v)
  }
}

fun <K, V> removeEntry(entry: LinkedEntry<K, V>) {
  entry.prev.next = entry.next
  entry.next.prev = entry.prev
}

fun <K, V> updateEntry(entry: LinkedEntry<K, V>) {
  entry.prev.next = entry
  entry.next.prev = entry
}
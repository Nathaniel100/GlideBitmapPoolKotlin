package io.github.loveginger.library.glidebitmappool

class GroupedLinkedMap<in K : Poolable, V> {
  private val head = LinkedEntry<K, V>()
  private val keyToEntry = HashMap<K, LinkedEntry<K, V>>()

  fun put(key: K, value: V) {
    var entry = keyToEntry[key]
    if (entry == null) {
      entry = LinkedEntry(key)
      makeTail(entry)
      keyToEntry[key] = entry
    } else {
      key.offer()
    }

    entry.add(value)
  }

  fun get(key: K): V? {
    var entry = keyToEntry[key]
    if (entry == null) {
      entry = LinkedEntry(key)
      keyToEntry[key] = entry
    } else {
      key.offer()
    }

    makeHead(entry)
    return entry.removeLast()
  }

  fun removeLast(): V? {
    var tail = head.prev
    while (tail != head) {
      val removed = tail.removeLast()
      if (removed != null) {
        return removed
      } else {
        removeEntry(tail)
        keyToEntry.remove(tail.key)
        tail.key?.offer()
      }

      tail = tail.prev
    }

    return null
  }

  private fun makeTail(entry: LinkedEntry<K, V>) {
    removeEntry(entry)
    entry.prev = head.prev
    entry.next = head
    updateEntry(entry)
  }

  private fun makeHead(entry: LinkedEntry<K, V>) {
    removeEntry(entry)
    entry.prev = head
    entry.next = head.next
    updateEntry(entry)
  }
}
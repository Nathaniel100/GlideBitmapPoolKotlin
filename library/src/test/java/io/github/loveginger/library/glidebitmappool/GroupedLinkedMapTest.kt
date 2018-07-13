package io.github.loveginger.library.glidebitmappool

import io.github.loveginger.library.glidebitmappool.common.Key
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class GroupedLinkedMapTest {
  private val groupedLinkedMap = GroupedLinkedMap<Key, String>()

  @Test
  fun get_emptyMap() {
    val result = groupedLinkedMap.get(Key(1))
    assertThat(result, nullValue())
  }

  @Test
  fun get_notMatchKey() {
    groupedLinkedMap.put(Key(1), "1")

    val result = groupedLinkedMap.get(Key(2))
    assertThat(result, nullValue())
  }

  @Test
  fun putAndGet_putEqualsGet() {
    groupedLinkedMap.put(Key(1), "1")
    val result: String? = groupedLinkedMap.get(Key(1))
    assertThat(result, `is`("1"))
  }

  @Test
  fun putAndGet_putLessThanGet() {
    groupedLinkedMap.put(Key(1), "1")

    val result1: String? = groupedLinkedMap.get(Key(1))
    assertThat(result1, `is`("1"))

    val result2: String? = groupedLinkedMap.get(Key(1))
    assertThat(result2, nullValue())
  }

  @Test
  fun putAndGet_putLargerThanGet() {
    groupedLinkedMap.put(Key(1), "1")
    groupedLinkedMap.put(Key(1), "1")
    groupedLinkedMap.put(Key(1), "1")

    val result1: String? = groupedLinkedMap.get(Key(1))
    assertThat(result1, `is`("1"))

    val result2: String? = groupedLinkedMap.get(Key(1))
    assertThat(result2, `is`("1"))
  }

  @Test
  fun removeLast_emptyMap() {
    val result = groupedLinkedMap.removeLast()
    assertThat(result, nullValue())
  }

  @Test
  fun removeLast_oneValue() {
    groupedLinkedMap.put(Key(1), "1")
    val result = groupedLinkedMap.removeLast()
    assertThat(result, `is`("1"))
  }

  @Test
  fun removeLast_moreValues() {
    groupedLinkedMap.put(Key(1), "1")
    groupedLinkedMap.put(Key(2), "2")
    groupedLinkedMap.put(Key(3), "3")

    val result1 = groupedLinkedMap.removeLast()
    val result2 = groupedLinkedMap.removeLast()
    val result3 = groupedLinkedMap.removeLast()
    val result4 = groupedLinkedMap.removeLast()
    assertThat(result1, `is`("3"))
    assertThat(result2, `is`("2"))
    assertThat(result3, `is`("1"))
    assertThat(result4, nullValue())
  }

  @Test
  fun removeLast_lru() {
    groupedLinkedMap.put(Key(1), "1")
    groupedLinkedMap.put(Key(2), "2")
    groupedLinkedMap.put(Key(3), "3")

    groupedLinkedMap.get(Key(3)) // get key(3) to make it at head, not last

    val result = groupedLinkedMap.removeLast()
    assertThat(result, `is`("2"))
  }


}
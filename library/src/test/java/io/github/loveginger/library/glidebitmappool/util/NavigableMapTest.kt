package io.github.loveginger.library.glidebitmappool.util

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*


class NavigableMapTest {

  private val map: NavigableMap<Int, Int> = TreeMap<Int, Int>()

  @Before
  fun setup() {
    for (i in 1..10) {
      map[i] = i
    }
  }

  @Test
  fun lowerEntry_normal() {
    val entry = map.lowerEntry(5)
    assertThat(entry.value, `is`(4))
  }

  @Test
  fun lowerEntry_noLower() {
    val entry = map.lowerEntry(0)
    assertNull(entry)
  }

  @Test
  fun lowerEntry_lowest() {
    val entry = map.lowerEntry(1)
    assertNull(entry)
  }

  @Test
  fun lowerEntry_largest() {
    val entry = map.lowerEntry(10)
    assertThat(entry.value, `is`(9))
  }

  @Test
  fun lowerEntry_noLarger() {
    val entry = map.lowerEntry(11)
    assertThat(entry.value, `is`(10))
  }

  @Test
  fun floorEntry_normal() {
    val entry = map.floorEntry(5)
    assertThat(entry.value, `is`(5))
  }

  @Test
  fun floorEntry_noLower() {
    val entry = map.floorEntry(0)
    assertNull(entry)
  }

  @Test
  fun floorEntry_lowest() {
    val entry = map.floorEntry(1)
    assertThat(entry.value, `is`(1))
  }

  @Test
  fun floorEntry_largest() {
    val entry = map.floorEntry(10)
    assertThat(entry.value, `is`(10))
  }

  @Test
  fun floorEntry_noLarger() {
    val entry = map.floorEntry(11)
    assertThat(entry.value, `is`(10))
  }

  @Test
  fun ceilingEntry_normal() {
    val entry = map.ceilingEntry(5)
    assertThat(entry.value, `is`(5))
  }

  @Test
  fun ceilingEntry_noLower() {
    val entry = map.ceilingEntry(0)
    assertThat(entry.value, `is`(1))
  }

  @Test
  fun ceilingEntry_lowest() {
    val entry = map.ceilingEntry(1)
    assertThat(entry.value, `is`(1))
  }

  @Test
  fun ceilingEntry_largest() {
    val entry = map.ceilingEntry(10)
    assertThat(entry.value, `is`(10))
  }

  @Test
  fun ceilingEntry_noLarger() {
    val entry = map.ceilingEntry(11)
    assertNull(entry)
  }

  @Test
  fun higherEntry_normal() {
    val entry = map.higherEntry(5)
    assertThat(entry.value, `is`(6))
  }

  @Test
  fun higherEntry_noLower() {
    val entry = map.higherEntry(0)
    assertThat(entry.value, `is`(1))
  }

  @Test
  fun higherEntry_lowest() {
    val entry = map.higherEntry(1)
    assertThat(entry.value, `is`(2))
  }

  @Test
  fun higherEntry_largest() {
    val entry = map.higherEntry(10)
    assertNull(entry)
  }

  @Test
  fun higherEntry_noLarger() {
    val entry = map.higherEntry(11)
    assertNull(entry)
  }

  @Test
  fun descendingKeySet() {
    val keySet = map.descendingKeySet()
    assertThat(keySet, hasItems(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
    assertThat(keySet.first(), `is`(10))
    assertThat(keySet.last(), `is`(1))
  }

  @Test
  fun firstEntry() {
    val entry = map.firstEntry()
    assertThat(entry.value, `is`(1))
  }

  @Test
  fun lastEntry() {
    val entry = map.lastEntry()
    assertThat(entry.value, `is`(10))
  }
}
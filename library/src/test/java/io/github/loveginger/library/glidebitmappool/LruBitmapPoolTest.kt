package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import android.graphics.Color
import com.nhaarman.mockitokotlin2.mock
import io.github.loveginger.library.glidebitmappool.common.NoLoggerRule
import io.github.loveginger.library.glidebitmappool.common.equalOrLargerThan
import io.github.loveginger.library.glidebitmappool.common.equalOrLessThan
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LruBitmapPoolTest {

  @Mock
  lateinit var strategy: LruPoolStrategy

  @Mock
  lateinit var configs: Set<Bitmap.Config?>

  @Mock
  lateinit var mockBitmap: Bitmap

  @JvmField
  @Rule
  val noLoggerRule = NoLoggerRule()

  lateinit var bitmapPool: LruBitmapPool

  private val MAX_SIZE = 100L

  @Before
  fun setup() {
    bitmapPool = LruBitmapPool(MAX_SIZE, configs, strategy)
  }

  @Test
  fun maxSize() {
    assertThat(bitmapPool.getMaxSize(), `is`(MAX_SIZE))
  }

  @Test(expected = IllegalStateException::class)
  fun put_bitmapRecycled() {
    `when`(mockBitmap.isRecycled).thenReturn(true)

    bitmapPool.put(mockBitmap)
  }

  @Test
  fun put_bitmapMutableIsFalse() {
    `when`(mockBitmap.isMutable).thenReturn(false)

    bitmapPool.put(mockBitmap)

    verify(mockBitmap).recycle()
    verify(strategy, never()).put(mockBitmap)
  }

  @Test
  fun put_bitmapLargerThanMaxSize() {
    `when`(mockBitmap.isMutable).thenReturn(true)
    `when`(strategy.getSize(mockBitmap)).thenReturn(MAX_SIZE + 1)

    bitmapPool.put(mockBitmap)

    verify(mockBitmap).recycle()
    verify(strategy, never()).put(mockBitmap)
  }

  @Test
  fun put_bitmapNotAllowedConfig() {
    `when`(mockBitmap.isMutable).thenReturn(true)
    `when`(strategy.getSize(mockBitmap)).thenReturn(MAX_SIZE - 1)
    `when`(mockBitmap.config).thenReturn(Bitmap.Config.ARGB_8888)
    `when`(configs.contains(Bitmap.Config.ARGB_8888)).thenReturn(false)

    bitmapPool.put(mockBitmap)

    verify(mockBitmap).recycle()
    verify(strategy, never()).put(mockBitmap)
  }

  @Test
  fun put_bitmap_noEvict() {
    val mockSize = MAX_SIZE - 1
    `when`(mockBitmap.isMutable).thenReturn(true)
    `when`(strategy.getSize(mockBitmap)).thenReturn(mockSize)
    `when`(mockBitmap.config).thenReturn(Bitmap.Config.ARGB_8888)
    `when`(configs.contains(Bitmap.Config.ARGB_8888)).thenReturn(true)

    bitmapPool.put(mockBitmap)

    verify(mockBitmap, never()).recycle()
    verify(strategy).put(mockBitmap)
    assertThat(bitmapPool.currentSize, `is`(mockSize))
  }

  @Test
  fun put_bitmap_evict() {
    `when`(mockBitmap.isMutable).thenReturn(true)
    `when`(strategy.getSize(any<Bitmap>())).thenReturn(MAX_SIZE - 1)
    `when`(mockBitmap.config).thenReturn(Bitmap.Config.ARGB_8888)
    `when`(configs.contains(Bitmap.Config.ARGB_8888)).thenReturn(true)
    bitmapPool.currentSize = MAX_SIZE
    val mockBitmapRemove = mock<Bitmap> { }
    `when`(strategy.removeLast()).thenReturn(mockBitmapRemove)

    bitmapPool.put(mockBitmap)

    verify(mockBitmap, never()).recycle()
    verify(strategy).put(mockBitmap)
    verify(strategy, atLeastOnce()).removeLast()
    verify(mockBitmapRemove).recycle()
    assertThat(bitmapPool.currentSize, equalOrLessThan(MAX_SIZE))
    assertThat(bitmapPool.evictions, equalOrLargerThan(1))
  }

  @Test
  fun get_misses() {
    val width = 100
    val height = 200
    val config = Bitmap.Config.ARGB_8888
    `when`(strategy.get(width, height, config)).thenReturn(null)
    `when`(strategy.createBitmap(width, height, config)).thenReturn(mockBitmap)

    val result = bitmapPool.get(width, height, config)

    verify(strategy).get(width, height, config)
    verify(mockBitmap, never()).eraseColor(Color.TRANSPARENT)
    verify(strategy).createBitmap(width, height, config)
    assertThat(result, `is`(mockBitmap))
    assertThat(bitmapPool.misses, `is`(1))
    assertThat(bitmapPool.hits, `is`(0))
  }

  @Test
  fun get_hits() {
    val width = 100
    val height = 200
    val config = Bitmap.Config.ARGB_8888
    `when`(strategy.get(width, height, config)).thenReturn(mockBitmap)

    val result = bitmapPool.get(width, height, config)

    verify(strategy).get(width, height, config)
    verify(mockBitmap).eraseColor(Color.TRANSPARENT)
    assertThat(result, `is`(mockBitmap))
    assertThat(bitmapPool.misses, `is`(0))
    assertThat(bitmapPool.hits, `is`(1))

  }

  @Test
  fun getDirty_misses() {
    val width = 100
    val height = 200
    val config = Bitmap.Config.ARGB_8888
    `when`(strategy.get(width, height, config)).thenReturn(null)
    `when`(strategy.createBitmap(width, height, config)).thenReturn(mockBitmap)

    val result = bitmapPool.getDirty(width, height, config)

    verify(strategy).get(width, height, config)
    verify(strategy).createBitmap(width, height, config)
    assertThat(result, `is`(mockBitmap))
    assertThat(bitmapPool.misses, `is`(1))
    assertThat(bitmapPool.hits, `is`(0))
  }

  @Test
  fun getDirty_hits() {
    val width = 100
    val height = 200
    val config = Bitmap.Config.ARGB_8888
    `when`(strategy.get(width, height, config)).thenReturn(mockBitmap)

    val result = bitmapPool.getDirty(width, height, config)

    verify(strategy).get(width, height, config)
    assertThat(result, `is`(mockBitmap))
    assertThat(bitmapPool.misses, `is`(0))
    assertThat(bitmapPool.hits, `is`(1))
  }
}
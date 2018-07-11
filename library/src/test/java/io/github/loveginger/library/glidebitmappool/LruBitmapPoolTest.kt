package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import io.github.loveginger.library.glidebitmappool.common.NoLoggerRule
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyZeroInteractions
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

  @Before
  fun setup() {
    bitmapPool = LruBitmapPool(100, configs, strategy)
  }

  @Test
  fun maxSize() {
    assertThat(bitmapPool.getMaxSize(), `is`(100L))
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
    verifyZeroInteractions(strategy)
  }
}
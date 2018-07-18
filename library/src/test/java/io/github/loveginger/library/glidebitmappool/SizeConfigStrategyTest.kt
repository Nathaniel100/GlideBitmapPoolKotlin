package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.github.loveginger.library.glidebitmappool.common.NoLoggerRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify

class SizeConfigStrategyTest {

  @JvmField
  @Rule
  val noLoggerRule = NoLoggerRule()

  val size1: Int = 1000
  val bitmapWidth1: Int = 100
  val bitmapHeight1: Int = 200
  val bitmapConfig1: Bitmap.Config = Bitmap.Config.ARGB_4444
  var mockBitmap1: Bitmap = mock {
    on { width } doReturn (bitmapWidth1)
    on { height } doReturn (bitmapHeight1)
    on { config } doReturn (bitmapConfig1)
  }

  val size2: Int = 10000
  val bitmapWidth2: Int = 1000
  val bitmapHeight2: Int = 2000
  val bitmapConfig2: Bitmap.Config = Bitmap.Config.ARGB_4444
  var mockBitmap2: Bitmap = mock {
    on { width } doReturn (bitmapWidth2)
    on { height } doReturn (bitmapHeight2)
    on { config } doReturn (bitmapConfig2)
  }

  var bitmapHelper: BitmapHelper = mock {
    on { createBitmap(bitmapWidth1, bitmapHeight1, bitmapConfig1) } doReturn (mockBitmap1)
    on { getBitmapByteSize(mockBitmap1) } doReturn (size1)
    on { getBitmapByteSize(bitmapWidth1, bitmapHeight1, bitmapConfig1) } doReturn (size1)

    on { createBitmap(bitmapWidth2, bitmapHeight2, bitmapConfig2) } doReturn (mockBitmap2)
    on { getBitmapByteSize(mockBitmap2) } doReturn (size2)
    on { getBitmapByteSize(bitmapWidth2, bitmapHeight2, bitmapConfig2) } doReturn (size2)
  }

  var strategy = SizeConfigStrategy(bitmapHelper)

  @Test
  fun putAndGet() {
    strategy.put(mockBitmap1)
    val result = strategy.get(bitmapWidth1, bitmapHeight1, bitmapConfig1)
    assertThat(result, `is`(mockBitmap1))
  }

  @Test
  fun putAndGet_sameConfig_noLargerSize() {
    strategy.put(mockBitmap1)

    val result = strategy.get(bitmapWidth2, bitmapHeight2, bitmapConfig2)
    assertThat(result, nullValue())
  }

  @Test
  fun putAndGet_sameConfig_hasLargerSize() {
    strategy.put(mockBitmap2)

    val result = strategy.get(bitmapWidth1, bitmapHeight1, bitmapConfig1)
    assertThat(result, `is`(mockBitmap2))
  }

  @Test
  fun putAndGet_getMoreThanPut() {
    strategy.put(mockBitmap1)

    var result = strategy.get(bitmapWidth1, bitmapHeight1, bitmapConfig1)
    assertThat(result, `is`(mockBitmap1))

    result = strategy.get(bitmapWidth1, bitmapHeight1, bitmapConfig1)
    assertThat(result, nullValue())
  }

  @Test
  fun createBitmap() {
    strategy.createBitmap(bitmapWidth1, bitmapHeight1, bitmapConfig1)

    verify(bitmapHelper).createBitmap(bitmapWidth1, bitmapHeight1, bitmapConfig1)
  }

  @Test
  fun removeLast() {
    strategy.put(mockBitmap1)
    strategy.put(mockBitmap2)

    val result = strategy.removeLast()

    assertThat(result, `is`(mockBitmap2))
  }

  @Test
  fun removeLast_lastAfterGet() {
    strategy.put(mockBitmap1)
    strategy.put(mockBitmap2)

    val getResult = strategy.get(bitmapWidth2, bitmapHeight2, bitmapConfig2)
    assertThat(getResult, `is`(mockBitmap2))

    val result = strategy.removeLast()
    assertThat(result, `is`(mockBitmap1))
  }

  @Test
  fun removeLast_empty() {
    val result = strategy.removeLast()
    assertThat(result, nullValue())
  }

  @Test
  fun removeLast_emptyAfterPut() {
    strategy.put(mockBitmap1)
    strategy.put(mockBitmap2)
    strategy.removeLast()
    strategy.removeLast()
    val result = strategy.removeLast()
    assertThat(result, nullValue())
  }
}
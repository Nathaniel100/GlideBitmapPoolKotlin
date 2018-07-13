package io.github.loveginger.library.glidebitmappool

import android.graphics.Bitmap
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.github.loveginger.library.glidebitmappool.common.NoLoggerRule
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

class AttributeStrategyTest {

  @JvmField
  @Rule
  val noLoggerRule = NoLoggerRule()

  val bitmapWidth1: Int = 100
  val bitmapHeight1: Int = 200
  val bitmapConfig1: Bitmap.Config = Bitmap.Config.ARGB_4444
  var mockBitmap1: Bitmap = mock {
    on { width } doReturn (bitmapWidth1)
    on { height } doReturn (bitmapHeight1)
    on { config } doReturn (bitmapConfig1)
  }

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
  }

  var strategy: AttributeStrategy = AttributeStrategy(bitmapHelper)

  @Test
  fun putAndGet() {
    strategy.put(mockBitmap1)

    val result = strategy.get(bitmapWidth1, bitmapHeight1, bitmapConfig1)
    assertThat(result, `is`(mockBitmap1))
  }

  @Test
  fun putAndGet_keyNotMatch() {
    strategy.put(mockBitmap1)

    val result = strategy.get(bitmapWidth2, bitmapHeight2, bitmapConfig2)
    assertThat(result, nullValue())
  }

  @Test
  fun putAndGet_getLargerThanPut() {
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
}
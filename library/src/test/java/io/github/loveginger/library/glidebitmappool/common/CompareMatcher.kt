package io.github.loveginger.library.glidebitmappool.common

import org.hamcrest.CoreMatchers.*
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher


/**
 * Created by wufan on 2018-07-12.
 */
class LargerThan<T : Comparable<T>>(private val expected: T) : TypeSafeMatcher<T>() {

  override fun matchesSafely(item: T): Boolean {
    return item > expected
  }

  override fun describeTo(description: Description?) {
    description?.appendText("Larger than $expected")
  }
}

fun <T : Comparable<T>> largerThan(expected: T): Matcher<T> = LargerThan(expected)

fun <T : Comparable<T>> lessThan(expected: T): Matcher<T> = not(either(equalTo(expected)).or(
    largerThan(expected)))

fun <T : Comparable<T>> equalOrLessThan(expected: T): Matcher<T> = not(largerThan(expected))

fun <T : Comparable<T>> equalOrLargerThan(expected: T): Matcher<T> = either(equalTo(expected)).or(
    largerThan(expected))

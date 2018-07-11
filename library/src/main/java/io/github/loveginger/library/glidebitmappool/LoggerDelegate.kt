package io.github.loveginger.library.glidebitmappool

import android.util.Log

interface LoggerDelegate {
  fun log(priority: Int, tag: String, msg: String): Int

  fun isLoggable(tag: String, priority: Int): Boolean
}

class DefaultLoggerDelegate : LoggerDelegate {
  override fun log(priority: Int, tag: String, msg: String) = Log.println(priority, tag, msg)

  override fun isLoggable(tag: String, priority: Int) = Log.isLoggable(tag, priority)
}


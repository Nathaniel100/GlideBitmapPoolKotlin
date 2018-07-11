package io.github.loveginger.library.glidebitmappool

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.net.UnknownHostException


object Logger {
  var delegate: LoggerDelegate = DefaultLoggerDelegate()

  /**
   * Priority constant for the println method; use Log.v.
   */
  val VERBOSE = Log.VERBOSE

  /**
   * Priority constant for the println method; use Log.d.
   */
  val DEBUG = Log.DEBUG

  /**
   * Priority constant for the println method; use Log.i.
   */
  val INFO = Log.INFO

  /**
   * Priority constant for the println method; use Log.w.
   */
  val WARN = Log.WARN

  /**
   * Priority constant for the println method; use Log.e.
   */
  val ERROR = Log.ERROR

  /**
   * Priority constant for the println method.
   */
  val ASSERT = Log.ASSERT

  fun v(tag: String, msg: String): Int {
    return println(VERBOSE, tag, msg)
  }

  /**
   * Send a [.VERBOSE] log message and log the exception.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  fun v(tag: String, msg: String, tr: Throwable): Int {
    return println(VERBOSE, tag, msg + '\n'.toString() + getStackTraceString(tr))
  }

  /**
   * Send a [.DEBUG] log message.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun d(tag: String, msg: String): Int {
    return println(DEBUG, tag, msg)
  }

  /**
   * Send a [.DEBUG] log message and log the exception.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  fun d(tag: String, msg: String, tr: Throwable): Int {
    return println(DEBUG, tag, msg + '\n'.toString() + getStackTraceString(tr))
  }

  /**
   * Send an [.INFO] log message.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun i(tag: String, msg: String): Int {
    return println(INFO, tag, msg)
  }

  /**
   * Send a [.INFO] log message and log the exception.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  fun i(tag: String, msg: String, tr: Throwable): Int {
    return println(INFO, tag, msg + '\n'.toString() + getStackTraceString(tr))
  }

  /**
   * Send a [.WARN] log message.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun w(tag: String, msg: String): Int {
    return println(WARN, tag, msg)
  }

  /**
   * Send a [.WARN] log message and log the exception.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  fun w(tag: String, msg: String, tr: Throwable): Int {
    return println(WARN, tag, msg + '\n'.toString() + getStackTraceString(tr))
  }

  /*
     * Send a {@link #WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param tr An exception to log
     */
  fun w(tag: String, tr: Throwable): Int {
    return println(WARN, tag, getStackTraceString(tr))
  }

  /**
   * Send an [.ERROR] log message.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   */
  fun e(tag: String, msg: String): Int {
    return println(ERROR, tag, msg)
  }

  /**
   * Send a [.ERROR] log message and log the exception.
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @param tr An exception to log
   */
  fun e(tag: String, msg: String, tr: Throwable): Int {
    return println(ERROR, tag, msg + '\n'.toString() + getStackTraceString(tr))
  }

  /**
   * Handy function to get a loggable stack trace from a Throwable
   * @param tr An exception to log
   */
  private fun getStackTraceString(tr: Throwable?): String {
    if (tr == null) {
      return ""
    }

    // This is to reduce the amount of log spew that apps do in the non-error
    // condition of the network being unavailable.
    var t = tr
    while (t != null) {
      if (t is UnknownHostException) {
        return ""
      }
      t = t.cause
    }

    val sw = StringWriter()
    val pw = PrintWriter(sw)
    tr.printStackTrace(pw)
    pw.flush()
    return sw.toString()
  }

  /**
   * Low-level logging call.
   * @param priority The priority/type of this log message
   * @param tag Used to identify the source of a log message.  It usually identifies
   * the class or activity where the log call occurs.
   * @param msg The message you would like logged.
   * @return The number of bytes written.
   */
  fun println(priority: Int, tag: String, msg: String): Int {
    return delegate.log(priority, tag, msg)
  }

  fun isLoggable(tag: String, priority: Int): Boolean {
    return delegate.isLoggable(tag, priority)
  }


}
package io.github.loveginger.library.glidebitmappool.common

import io.github.loveginger.library.glidebitmappool.Logger
import io.github.loveginger.library.glidebitmappool.LoggerDelegate
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement


class NoLoggerRule : TestRule {
  override fun apply(base: Statement?, description: Description?): Statement {
    return object : Statement() {
      override fun evaluate() {
        Logger.delegate = object : LoggerDelegate {
          override fun log(priority: Int, tag: String, msg: String): Int {
            return 0
          }

          override fun isLoggable(tag: String, priority: Int): Boolean {
            return false
          }
        }
      }
    }
  }


}
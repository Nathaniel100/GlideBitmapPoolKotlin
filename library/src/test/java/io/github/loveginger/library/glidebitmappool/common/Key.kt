// Copyright (c) CFCA 2018. All Rights Reserved.

package io.github.loveginger.library.glidebitmappool.common

import io.github.loveginger.library.glidebitmappool.Poolable


/**
 * Created by wufan on 2018-07-13.
 */
data class Key(val data: Int) : Poolable {
  override fun offer() {
  }
}
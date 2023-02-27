package com.agora.entfulldemo.home.constructor

import androidx.annotation.DrawableRes
import io.agora.scene.base.AgoraScene

/**
 * @author create by zhangwei03
 */
data class ScenesModel constructor(
    val scene: AgoraScene,
    val clazzName: String,
    val name: String,
    @DrawableRes val background: Int,
    @DrawableRes val icon: Int,
    val active: Boolean = false
)

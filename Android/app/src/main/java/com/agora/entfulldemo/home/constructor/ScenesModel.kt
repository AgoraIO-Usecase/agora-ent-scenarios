package com.agora.entfulldemo.home.constructor

import androidx.annotation.DrawableRes

/**
 * @author create by zhangwei03
 */
data class ScenesModel constructor(
    val type: ScenesConstructor.SceneType,
    val name: String,
    @DrawableRes val background: Int,
    @DrawableRes val icon: Int,
    val active: Boolean = false,
)

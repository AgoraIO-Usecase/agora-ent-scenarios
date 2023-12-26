package com.agora.entfulldemo.login.constructor

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class AdvertiseModel constructor(
    @DrawableRes val drawableId: Int,
    @StringRes val titleId: Int,
    @StringRes val contentId: Int
)

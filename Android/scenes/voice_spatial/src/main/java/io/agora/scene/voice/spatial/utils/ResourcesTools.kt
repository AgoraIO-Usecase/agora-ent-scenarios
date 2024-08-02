package io.agora.scene.voice.spatial.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import java.util.*

object ResourcesTools {

    private var isZh = false

    @JvmStatic
    fun getIsZh(): Boolean = isZh

    @JvmStatic
    fun getActivityFromView(view: View?): Activity? {
        if (null != view) {
            var context = view.context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
        }
        return null
    }

    @JvmStatic
    fun isZh(context: Context): Boolean {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
        isZh = locale.country == "CN"
        return isZh
    }
}
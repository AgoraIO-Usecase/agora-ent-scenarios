package io.agora.voice.common.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
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
    @ColorInt
    fun getColor(resources: Resources, @ColorRes id: Int, theme: Resources.Theme? = null): Int {
        return ResourcesCompat.getColor(resources, id, theme)
    }

    @JvmStatic
    fun getDrawableId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
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
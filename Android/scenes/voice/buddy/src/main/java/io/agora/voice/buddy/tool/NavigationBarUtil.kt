package io.agora.voice.buddy.tool

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup

private const val RES_NAME_NAV_BAR = "navigationBarBackground"

private val Context.navBarResId
    get() = resources.getIdentifier(
        "navigation_bar_height",
        "dimen", "android"
    )

/**
 * 获取虚拟导航栏的高度，必须在布局绘制完成之后调用才能获取到正确的值（可以在onWindowFocusChanged()中调用）
 * 单位为px
 */
val Context.navBarHeight: Int
    get() {
        val resourceId = navBarResId
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

/**
 * 手机是否有虚拟导航栏
 */
val Context.hasNavBar
    @JvmName("hasNavBar")
    get() = navBarResId != 0

/**
 * 当前虚拟导航栏是否显示
 */
val Activity.isNavBarShowed: Boolean
    get()  {
        val viewGroup = window.decorView as ViewGroup? ?: return false
        return (0 until viewGroup.childCount).firstOrNull {
            viewGroup.getChildAt(it).id != View.NO_ID
                && this.resources.getResourceEntryName(viewGroup.getChildAt(it).id) == RES_NAME_NAV_BAR
        } != null
    }



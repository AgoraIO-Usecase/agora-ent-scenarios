package io.agora.voice.common.utils

import android.app.Activity
import android.app.Service
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

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

object NavigationUtils {

    /**
     * 获取虚拟导航栏(NavigationBar)的高度，可能未显示
     */
    fun getNavigationBarHeight(context: Context): Int {
        var result = 0
        val resources = context.resources
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
        return result
    }

    /**
     * 获取虚拟导航栏(NavigationBar)是否显示
     * @return true 表示虚拟导航栏显示，false 表示虚拟导航栏未显示
     */
    fun hasNavigationBar(context: Context) = when {
        getNavigationBarHeight(context) == 0 -> false
        RomUtils.isHuaweiRom() && isHuaWeiHideNav(context) -> false
        // 打开会导致小米机型里带底部手势栏的被判断为全屏
//        RomUtils.isMiuiRom() && isMiuiFullScreen(context) -> false
        RomUtils.isVivoRom() && isVivoFullScreen(context) -> false
        else -> isHasNavigationBar(context)
    }

    /**
     * 华为手机是否隐藏了虚拟导航栏
     * @return true 表示隐藏了，false 表示未隐藏
     */
    private fun isHuaWeiHideNav(context: Context) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Settings.System.getInt(context.contentResolver, "navigationbar_is_min", 0)
        } else {
            Settings.Global.getInt(context.contentResolver, "navigationbar_is_min", 0)
        } != 0

    /**
     * 小米手机是否开启手势操作
     * @return true 表示使用的是手势，false 表示使用的是虚拟导航栏(NavigationBar)，默认是false
     */
    private fun isMiuiFullScreen(context: Context) =
        Settings.Global.getInt(context.contentResolver, "force_fsg_nav_bar", 0) != 0

    /**
     * Vivo手机是否开启手势操作
     * @return true 表示使用的是手势，false 表示使用的是虚拟导航栏(NavigationBar)，默认是false
     */
    private fun isVivoFullScreen(context: Context) =
        Settings.Secure.getInt(context.contentResolver, "navigation_gesture_on", 0) != 0

    /**
     * 根据屏幕真实高度与显示高度，判断虚拟导航栏是否显示
     * @return true 表示虚拟导航栏显示，false 表示虚拟导航栏未显示
     */
    private fun isHasNavigationBar(context: Context): Boolean {
        val windowManager: WindowManager =
            context.getSystemService(Service.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay

        val realDisplayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(realDisplayMetrics)
        }
        val realHeight = realDisplayMetrics.heightPixels
        val realWidth = realDisplayMetrics.widthPixels

        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)
        val displayHeight = displayMetrics.heightPixels
        val displayWidth = displayMetrics.widthPixels

        // 部分无良厂商的手势操作，显示高度 + 导航栏高度，竟然大于物理高度，对于这种情况，直接默认未启用导航栏
//        if (displayHeight > displayWidth) {
//            if (displayHeight + getNavigationBarHeight(context) > realHeight) return false
//        } else {
//            if (displayWidth + getNavigationBarHeight(context) > realWidth) return false
//        }

        return realWidth - displayWidth > 0 || realHeight - displayHeight > 0
    }
}
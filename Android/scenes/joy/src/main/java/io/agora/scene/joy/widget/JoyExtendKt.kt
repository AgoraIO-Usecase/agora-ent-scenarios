package io.agora.scene.joy.widget

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import java.util.Random

val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

val Int.getRandomString
    get() = run {
        val str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val random = Random()
        val sb = StringBuffer()
        for (i in 0 until this) {
            val number = random.nextInt(62)
            sb.append(str[number])
        }
        sb.toString()
    }

private const val TAG = "Joy_screen"

/**
 * 获取屏幕宽度
 */
val Context.screenWidth: Int
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

/**
 * 获取屏幕高度
 */
val Context.screenHeight: Int
    get() {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

/**
 * 判断和设置是否全屏，赋值为true设置成全屏
 */
var Activity.isFullScreen: Boolean
    get() {
        val flag = WindowManager.LayoutParams.FLAG_FULLSCREEN
        return (window.attributes.flags and flag) == flag
    }
    set(value) {
        if (value) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }

/**
 * 是否是竖屏
 */
val Activity.isPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT


/**
 * 是否是横屏
 */
val Activity.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

private const val RES_NAME_NAV_BAR = "navigationBarBackground"
private val Context.navBarResId
    get() = resources.getIdentifier("navigation_bar_height", "dimen", "android")

private val Context.statusBarResId
    get() = resources.getIdentifier("status_bar_height", "dimen", "android")


/**
 * 当前虚拟导航栏是否显示
 */
val Activity.isNavBarShowed: Boolean
    get() {
        val viewGroup = window.decorView as ViewGroup? ?: return false
        return (0 until viewGroup.childCount).firstOrNull {
            viewGroup.getChildAt(it).id != View.NO_ID
                    && this.resources.getResourceEntryName(viewGroup.getChildAt(it).id) == RES_NAME_NAV_BAR
        } != null
    }

/**
 * 获取虚拟导航栏的高度，必须在布局绘制完成之后调用才能获取到正确的值（可以在onWindowFocusChanged()中调用）
 * 单位为px
 */
val Context.navBarHeight: Int
    get() {
        val resourceId = navBarResId
        return if (resourceId != 0 && checkNavigationBarShow(this)) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

val Context.statusBarHeight: Int
    get() {
        val resourceId = statusBarResId
        return if (resourceId != 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

/**
 * 手机是否有虚拟导航栏
 */
val Context.hasNavBar
    @JvmName("hasNavBar")
    get() = navBarResId != 0 && checkNavigationBarShow(this)

private fun checkNavigationBarShow(context: Context): Boolean {
    val display = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.display
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    val appUsableSize = Point()
    val realScreenSize = Point()

    display?.getSize(appUsableSize)
    display?.getRealSize(realScreenSize)

    Log.d(TAG, "checkNavigationBar ${realScreenSize.y} ${appUsableSize.y}")
    return realScreenSize.y != appUsableSize.y
}
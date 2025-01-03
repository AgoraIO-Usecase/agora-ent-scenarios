package io.agora.scene.base.utils

import android.app.Activity
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

fun displayWidth(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return metrics.widthPixels
}

fun displayHeight(): Int {
    val metrics = Resources.getSystem().displayMetrics
    return metrics.heightPixels
}

/**
 * Get screen width
 */
val Context.screenWidth: Int
    get() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.x
    }

/**
 * Get screen height
 */
val Context.screenHeight: Int
    get() {
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val point = Point()
        wm.defaultDisplay.getRealSize(point)
        return point.y
    }

/**
 * Check and set fullscreen mode. Set to true to enable fullscreen
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
 * Check if screen is in portrait mode
 */
val Activity.isPortrait: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT


/**
 * Check if screen is in landscape mode
 */
val Activity.isLandscape: Boolean
    get() = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

private const val RES_NAME_NAV_BAR = "navigationBarBackground"
private val Context.navBarResId
    get() = resources.getIdentifier(
        "navigation_bar_height",
        "dimen", "android"
    )

private val Context.statusBarResId
    get() = resources.getIdentifier("status_bar_height", "dimen", "android")


/**
 * Get navigation bar height in pixels. Must be called after layout is drawn (e.g. in onWindowFocusChanged())
 */
val Context.navBarHeight: Int
    get() {
        val resourceId = navBarResId
        return if (resourceId != 0) {
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
 * Check if device has virtual navigation bar
 */
val Context.hasNavBar
    @JvmName("hasNavBar")
    get() = navBarResId != 0

/**
 * Check if navigation bar is currently visible
 */
val Activity.isNavBarShowed: Boolean
    get() {
        val viewGroup = window.decorView as ViewGroup? ?: return false
        return (0 until viewGroup.childCount).firstOrNull {
            viewGroup.getChildAt(it).id != View.NO_ID
                    && this.resources.getResourceEntryName(viewGroup.getChildAt(it).id) == RES_NAME_NAV_BAR
        } != null
    }
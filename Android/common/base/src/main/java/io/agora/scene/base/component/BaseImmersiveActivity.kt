package io.agora.scene.base.component

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding

/**
 * Base Activity with immersive status bar and navigation bar
 * Status bar is transparent but keeps system icons visible
 * Navigation bar is hidden (immersive mode)
 */
abstract class BaseImmersiveActivity<T : ViewBinding> : BaseViewBindingActivity<T>() {

    /**
     * Setup immersive status bar and navigation bar
     * Status bar is transparent but keeps system icons visible
     * Navigation bar is hidden (immersive mode)
     */
    protected fun setupImmersiveNavigationBar() {
        window?.let { window ->
            // Use WindowCompat to set edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Set status bar and navigation bar transparent
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            // Use WindowInsetsControllerCompat (Android 11+) or SystemUiVisibility (older versions)
            val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
            if (windowInsetsController != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ uses new API
                // Hide navigation bar only, status bar remains visible (but transparent, content extends to status bar area)
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                // Set status bar icons to light color (white)
                windowInsetsController.isAppearanceLightStatusBars = false
            } else {
                // Older versions use SystemUiVisibility
                // Use LAYOUT_FULLSCREEN to extend content to status bar, but don't use FULLSCREEN to hide status bar icons
                val decorView = window.decorView
                var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                // Ensure LIGHT_STATUS_BAR flag is not set, so status bar icons are light (white)
                // If LIGHT_STATUS_BAR was previously set, clear it
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                decorView.systemUiVisibility = flags
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure immersive navigation bar is also effective in onResume
        setupImmersiveNavigationBar()
    }
}
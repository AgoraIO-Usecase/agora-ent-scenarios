package io.agora.scene.widget.dialog

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * Base BottomSheetDialog with immersive navigation bar
 * Navigation bar is hidden (immersive mode), status bar remains visible
 */
abstract class BaseImmersiveBottomSheetDialog : BottomSheetDialog {

    constructor(context: Context, theme: Int) : super(context, theme) {
        setupImmersiveMode()
    }

    /**
     * Setup immersive mode (hide navigation bar only, keep status bar visible)
     */
    protected fun setupImmersiveMode() {
        window?.let { window ->
            // Use WindowCompat to set edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Set navigation bar transparent only, status bar remains unchanged
            window.navigationBarColor = Color.TRANSPARENT

            // Use WindowInsetsControllerCompat (Android 11+) or SystemUiVisibility (older versions)
            val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
            if (windowInsetsController != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ uses new API - hide navigation bar only
                windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
                windowInsetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                // Older versions use SystemUiVisibility - hide navigation bar only
                val decorView = window.decorView
                val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                decorView.systemUiVisibility = flags
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val container = findViewById<View>(R.id.container)
        container?.let { view ->
            // Handle WindowInsets, keep status bar space, let content extend to navigation bar area
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
                // Keep status bar space, don't keep navigation bar space (because navigation bar is hidden)
                v.setPaddingRelative(0, statusBars.top, 0, 0)
                WindowInsetsCompat.CONSUMED
            }
        }

        // Ensure immersive mode is also effective in onStart
        setupImmersiveMode()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Maintain immersive mode
        setupImmersiveMode()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Restore navigation bar (optional, if needed)
        window?.let { window ->
            val windowInsetsController = ViewCompat.getWindowInsetsController(window.decorView)
            if (windowInsetsController != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Restore navigation bar only, status bar remains unchanged
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                // Older versions restore navigation bar
                val decorView = window.decorView
                val flags = decorView.systemUiVisibility and
                    (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY).inv()
                decorView.systemUiVisibility = flags
            }
        }
    }
}
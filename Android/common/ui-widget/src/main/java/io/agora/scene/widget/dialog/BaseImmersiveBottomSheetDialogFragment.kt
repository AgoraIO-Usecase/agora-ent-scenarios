package io.agora.scene.widget.dialog

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * Base BottomSheetDialogFragment with immersive navigation bar
 * Navigation bar is hidden (immersive mode), status bar remains visible
 */
abstract class BaseImmersiveBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var targetView: View? = null

    /**
     * Setup immersive mode for the dialog
     * Should be called in onViewCreated
     * 
     * @param targetView The view to apply padding for window insets (usually the main content view)
     */
    protected fun setupImmersiveMode(targetView: View) {
        this.targetView = targetView
        val window = requireDialog().window ?: return
        
        // Set background transparent
        WindowCompat.setDecorFitsSystemWindows(window, false)
        requireDialog().setOnShowListener {
            (targetView.parent as? ViewGroup)?.setBackgroundColor(Color.TRANSPARENT)
        }
        
        // Set navigation bar transparent only, status bar remains unchanged
        window.navigationBarColor = Color.TRANSPARENT
        
        // Setup immersive navigation bar
        setupImmersiveNavigationBar(window)
        
        // Handle window insets - keep status bar space, don't keep navigation bar space (because navigation bar is hidden)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _: View?, insets: WindowInsetsCompat ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            // Don't set padding, let content extend fully to navigation bar area
            targetView.setPadding(0, 0, 0, statusBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Setup immersive navigation bar (hide navigation bar only, keep status bar visible)
     */
    private fun setupImmersiveNavigationBar(window: Window) {
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

    override fun onStart() {
        super.onStart()
        // Ensure immersive mode is also effective in onStart
        dialog?.window?.let { window ->
            setupImmersiveNavigationBar(window)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure immersive mode is also effective in onResume
        dialog?.window?.let { window ->
            setupImmersiveNavigationBar(window)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Restore navigation bar
        dialog?.window?.let { window ->
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


package io.agora.scene.voice.ui.widget.primary

import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.agora.voice.common.utils.*

class KeyboardStatusWatcher(
    private val activity: FragmentActivity,
    private val lifecycleOwner: LifecycleOwner,
    private val listener: (isKeyboardShowed: Boolean, keyboardHeight: Int) -> Unit
) : PopupWindow(activity), ViewTreeObserver.OnGlobalLayoutListener {

    private val rootView by lazy { activity.window.decorView.rootView }

    private val TAG = "Keyboard-Tag"

    /**
     * 可见区域高度
     */
    private var visibleHeight = 0

    /**
     * 软键盘是否显示
     */
    var isKeyboardShowed = false
        private set

    /**
     * 最近一次弹出的软键盘高度
     */
    var keyboardHeight = 0
        private set

    /**
     * PopupWindow 布局
     */
    private val popupView by lazy {
        FrameLayout(activity).also {
            it.layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            //监听布局大小变化
            it.viewTreeObserver.addOnGlobalLayoutListener(this)
        }
    }

    init {
        //初始化 PopupWindow
        contentView = popupView
        //软键盘弹出时，PopupWindow 要调整大小
        softInputMode =
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED
        //宽度设为0，避免遮挡界面
        width = 0
        height = ViewGroup.LayoutParams.MATCH_PARENT
        setBackgroundDrawable(ColorDrawable(0))
        rootView.post { showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0) }

        //activity 销毁时或者 Fragment onDestroyView 时必须关闭 popupWindow ，避免内存泄漏
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                dismiss()
            }
        })
    }

    /**
     * 监听布局大小变化
     */
    override fun onGlobalLayout() {
        val rect = Rect()
        //获取当前可见区域
        popupView.getWindowVisibleDisplayFrame(rect)
        if (visibleHeight == (rect.bottom - rect.top)) {
            //可见区域高度不变时不必执行下面代码，避免重复监听
            return
        } else {
            visibleHeight = (rect.bottom - rect.top)
        }
        //粗略计算高度的变化值，后面会根据状态栏和导航栏修正
        val heightDiff = rootView.height - visibleHeight
        //这里取了一个大概值，当窗口高度变化值超过屏幕的 1/3 时，视为软键盘弹出
        if (heightDiff > activity.screenHeight / 3) {
            isKeyboardShowed = true
            //非全屏时减去状态栏高度
            keyboardHeight =
                if (activity.isFullScreen) heightDiff else heightDiff - StatusBarCompat.getStatusBarHeight(activity)
            //导航栏显示时减去其高度，但横屏时导航栏在侧边，故不必扣除高度
//            Log.d(TAG, "navBarHeight = ${activity.navBarHeight}   ")
//            Log.d(TAG, "hasNavBar = ${activity.hasNavBar}   ")
//            Log.d(TAG, "isNavBarShowed = ${NavigationUtils.hasNavigationBar(activity)}   ")
//            Log.d(TAG, "isPortrait = ${activity.isPortrait}   ")
            if (activity.hasNavBar && NavigationUtils.hasNavigationBar(activity) && activity.isPortrait) {
                keyboardHeight -= activity.navBarHeight
                LogTools.d(TAG, "keyboardHeight = $keyboardHeight   ")
            }
        } else {
            //软键盘隐藏时键盘高度为0
            isKeyboardShowed = false
            keyboardHeight = 0
        }
        listener.invoke(isKeyboardShowed, keyboardHeight)
    }
}

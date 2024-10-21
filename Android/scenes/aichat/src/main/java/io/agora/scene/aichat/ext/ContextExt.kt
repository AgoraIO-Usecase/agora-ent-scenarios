package io.agora.scene.aichat.ext

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.lifecycle.lifecycleScope
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.widget.toast.CustomToast

/**
 * Judge whether the current process is the main process.
 */
fun Context.isMainProcess(): Boolean {
    val processName: String? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        Application.getProcessName()
    } else {
        var processName: String? = null
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread", false,
                Application::class.java.classLoader
            )
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
        }
        processName
    }
    return this.applicationInfo.packageName == processName
}

/**
 * Set whether to keep the screen on.
 */
fun Context.keepScreenOn(keepOn: Boolean = true) {
    // keep screen on
    val activity = this as? android.app.Activity
    activity?.window?.run {
        if (keepOn) {
            addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

/**
 * Get the lifecycle scope of the current context.
 */
fun Context.mainScope(): kotlinx.coroutines.CoroutineScope {
    return (this as? androidx.appcompat.app.AppCompatActivity)?.lifecycleScope
        ?: (this as? androidx.fragment.app.FragmentActivity)?.lifecycleScope
        ?: kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main.immediate)
}

/**
 * Get the IO scope of the current context.
 */
fun Context.ioScope(): kotlinx.coroutines.CoroutineScope {
    return kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
}

/**
 * Get the boolean value of the resource.
 */
fun Context.getBooleanResource(@BoolRes resourceId: Int): Boolean {
    var enable = false
    try {
        enable = resources.getBoolean(resourceId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return enable
}

/**
 * Get the boolean value of the resource.
 */
fun Context.getIntegerResource(@IntegerRes resourceId: Int): Int {
    var value = -1
    try {
        value = resources.getInteger(resourceId)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return value
}

/**
 * Get screen info by context.
 */
fun Context.getScreenInfo(): FloatArray {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val info = FloatArray(5)
    val dm = DisplayMetrics()
    manager.defaultDisplay.getMetrics(dm)
    info[0] = dm.widthPixels.toFloat()
    info[1] = dm.heightPixels.toFloat()
    info[2] = dm.densityDpi.toFloat()
    info[3] = dm.density
    info[4] = dm.scaledDensity
    return info
}

fun isSdcardExist(): Boolean {
    return android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED
}

/**
 * Hide soft keyboard.
 */
fun Activity.hideSoftKeyboard() {
    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
        it.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

/**
 * Only for internal use.
 */
internal operator fun Context.plus(id: String?): String {
    return "${this.hashCode()}-$id"
}

fun Context.copyTextToClipboard(text: String, needToast: Boolean = false) {
    val clipboardManager = getSystemService(ClipboardManager::class.java)
    clipboardManager?.let {
        it.setPrimaryClip(ClipData.newPlainText("label", text))
        if (needToast) {
            ToastUtils.showToast("已复制到剪切板：\n${text}")
        }
    }
}

fun Context.vibrate( milliseconds: Long = 100) {
    val appContext = this.applicationContext
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(milliseconds)
    }
}

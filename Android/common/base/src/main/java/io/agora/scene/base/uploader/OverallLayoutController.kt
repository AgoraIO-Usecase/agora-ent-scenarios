package io.agora.scene.base.uploader

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.widget.Toast

object OverallLayoutController {

    const val REQUEST_FLOAT_CODE = 1001

    // 应用浮窗显示状态，退后后台回到前台需要重新展示
    var isReceptionShow = false
        private set(value) {
            field = value
        }

    @JvmStatic
    fun checkOverlayPermission(context: Activity, completion: () -> Unit) {
        if (commonROMPermissionCheck(context)) {
            completion()
        } else {
            Toast.makeText(context, "请开启悬浮窗权限", Toast.LENGTH_SHORT).show()
            context.startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            }, REQUEST_FLOAT_CODE)
        }
    }

    /**
     * 判断悬浮窗权限权限
     */
    @JvmStatic
    fun commonROMPermissionCheck(context: Context): Boolean {
        var result = true
        try {
            val clazz: Class<*> = Settings::class.java
            val canDrawOverlays =
                clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
            result = canDrawOverlays.invoke(null, context) as Boolean
        } catch (e: Exception) {
            Log.e("ServiceUtils", Log.getStackTraceString(e))
        }
        return result
    }

    private var floatCallback: FloatCallBack? = null

    /**
     * 开启悬浮窗
     *
     * @param context
     */
    @JvmStatic
    fun startMonkServer(context: Context) {
        val intent = Intent(context, FloatMonkService::class.java)
        context.startService(intent)
    }

    /**
     * 关闭悬浮窗
     */
    @JvmStatic
    fun stopMonkServer(context: Context) {
        val intent = Intent(context, FloatMonkService::class.java)
        context.stopService(intent)
    }

    /**
     * 注册监听
     */
    @JvmStatic
    fun registerCallLittleMonk(callLittleMonk: FloatCallBack?) {
        floatCallback = callLittleMonk
    }

    /**
     * 悬浮窗的显示
     */
    @JvmStatic
    fun show() {
        floatCallback?.show()
        isReceptionShow = true
    }

    /**
     * 悬浮窗的隐藏
     */
    @JvmStatic
    fun hide() {
        floatCallback?.hide()
        isReceptionShow = false
    }

    /**
     * 进入app 悬浮窗显示
     */
    @JvmStatic
    fun showBackHome() {
        if (isReceptionShow) {
            floatCallback?.show()
        }
    }

    /**
     * 退到桌面悬浮窗隐藏
     */
    @JvmStatic
    fun hideBackHome() {
        if (isReceptionShow) {
            floatCallback?.hide()
        }
    }

    @JvmStatic
    fun uploadStatus(uploadStatus: UploadStatus, uuid: String) {
        floatCallback?.uploadStatus(uploadStatus, uuid)
    }
    @JvmStatic
    fun  setOnRepeatUploadListener(listener: () -> Unit){
        floatCallback?.setOnRepeatUploadListener(listener)
    }
}
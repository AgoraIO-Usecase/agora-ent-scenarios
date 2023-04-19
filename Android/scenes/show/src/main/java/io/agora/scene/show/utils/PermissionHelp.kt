package io.agora.scene.show.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class PermissionHelp(val activity: ComponentActivity) {

    private var granted: (() -> Unit)? = null
    private var unGranted: (() -> Unit)? = null
    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val granted = granted
            val unGranted = unGranted
            this.granted = null
            this.unGranted = null

            if (isGranted) {
                granted?.invoke()
            } else {
                unGranted?.invoke()
            }
        }
    private val appSettingLauncher =
        activity.registerForActivityResult(object : ActivityResultContract<String, Boolean>(){
            private var input: String? = null

            override fun createIntent(context: Context, input: String?): Intent {
                this.input = input
                return Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.parse("package:" + context.packageName)
                }
            }

            override fun parseResult(resultCode: Int, intent: Intent?): Boolean {
                return ContextCompat.checkSelfPermission(
                    activity,
                    input ?: ""
                ) == PackageManager.PERMISSION_GRANTED
            }

        }){ isGranted ->
            val granted = granted
            val unGranted = unGranted
            this.granted = null
            this.unGranted = null

            if (isGranted) {
                granted?.invoke()
            } else {
                unGranted?.invoke()
            }
        }


    /**
     * 检查摄像头和麦克风权限
     *
     * @param force 是：如果权限被禁用则会跳转到系统应用权限设置页面
     */
    fun checkCameraAndMicPerms(
        granted: () -> Unit,
        unGranted: () -> Unit,
        force: Boolean = false
    ) {
        checkCameraPerm({
            checkMicPerm(granted, unGranted, force)
        }, unGranted, force)
    }

    /**
     * 检查麦克风权限
     *
     * @param force 是：如果权限被禁用则会跳转到系统应用权限设置页面
     */
    fun checkMicPerm(
        granted: () -> Unit,
        unGranted: () -> Unit,
        force: Boolean = false
    ) {
        checkPermission(Manifest.permission.RECORD_AUDIO, granted, force, unGranted)
    }

    /**
     * 检查摄像头权限
     *
     * @param force 是：如果权限被禁用则会跳转到系统应用权限设置页面
     */
    fun checkCameraPerm(
        granted: () -> Unit,
        unGranted: () -> Unit,
        force: Boolean = false
    ) {
        checkPermission(Manifest.permission.CAMERA, granted, force, unGranted)
    }

    /**
     * 检查外置存储权限
     *
     * @param force 是：如果权限被禁用则会跳转到系统应用权限设置页面
     */
    fun checkStoragePerm(
        granted: () -> Unit,
        unGranted: () -> Unit,
        force: Boolean = false
    ) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, granted, force, unGranted)
        }, force, unGranted)
    }

    private fun checkPermission(
        perm: String,
        granted: () -> Unit,
        force: Boolean,
        unGranted: () -> Unit
    ) {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                perm
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                granted.invoke()
            }
            activity.shouldShowRequestPermissionRationale(perm) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected, and what
                // features are disabled if it's declined. In this UI, include a
                // "cancel" or "no thanks" button that lets the user continue
                // using your app without granting the permission.
                // showInContextUI(...)
                if (force) {
                    launchAppSetting(
                        perm,
                        granted,
                        unGranted
                    )
                } else {
                    unGranted.invoke()
                }
            }
            else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                launchPermissionRequest(
                    perm,
                    granted,
                    unGranted
                )
            }
        }
    }

    private fun launchPermissionRequest(
        perm: String,
        granted: () -> Unit,
        unGranted: () -> Unit
    ) {
        this.granted = granted
        this.unGranted = unGranted
        requestPermissionLauncher.launch(perm)
    }

    private fun launchAppSetting(
        perm: String,
        granted: () -> Unit,
        unGranted: () -> Unit
    ) {
        this.granted = granted
        this.unGranted = unGranted
        appSettingLauncher.launch(perm)
    }
}
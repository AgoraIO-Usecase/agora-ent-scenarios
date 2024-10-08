package io.agora.scene.show

import AGManifest
import AGResourceManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.faceunity.wrapper.faceunity
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.DynamicLoadUtil
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.BeautyManager
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.debugSettings.DebugSettingDialog
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.show.widget.PresetDialog
import io.agora.scene.show.widget.beauty.MultiBeautyDialog
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil
import kotlinx.coroutines.launch
import kotlin.random.Random

/*
 * 主播开播前预览页面 activity
 */
class LivePrepareActivity : BaseViewBindingActivity<ShowLivePrepareActivityBinding>() {
    private val tag = "LivePrepareActivity"
    private val mService by lazy { ShowServiceProtocol.get() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mRoomId by lazy { getRandomRoomId() }

    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    // 设备打分， 通过设备打分接口确定视频最佳配置
    private val deviceScore by lazy { RtcEngineInstance.rtcEngine.queryDeviceScore() }

    private var isFinishToLiveDetail = false

    private var view: View? = null

    override fun getViewBinding(inflater: LayoutInflater): ShowLivePrepareActivityBinding {
        return ShowLivePrepareActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.tvRoomId.text = getString(R.string.show_room_id, mRoomId)
        binding.etRoomName.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mInputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.ivClose.setOnClickListener {
            view?.let {
                binding.flVideoContainer.removeView(it)
            }
            finish()
        }

        binding.ivCopy.setOnClickListener {
            // Copy to system clipboard
            copy2Clipboard(mRoomId)
        }
        binding.btnStartLive.setOnClickListener {
            createAndStartLive(binding.etRoomName.text.toString())
        }
        binding.tvRotate.setOnClickListener {
            mRtcEngine.switchCamera()
        }
        binding.tvBeauty.setOnClickListener {
            showBeautyDialog()
        }
        binding.tvSetting.setOnClickListener {
            if (AgoraApplication.the().isDebugModeOpen) {
                showDebugModeDialog()
            } else {
                showPresetDialog()
            }
        }
        binding.tvContent.text =
            String.format(resources.getString(R.string.show_beauty_loading), "", "0%")

        if (BuildConfig.BEAUTY_RESOURCE.isEmpty()) {
            binding.statusPrepareViewLrc.isVisible = false
            // 美颜资源文件已放在assets目录内
            BeautyManager.initialize(this@LivePrepareActivity, mRtcEngine)

            val videoView = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                SurfaceView(this@LivePrepareActivity)
            } else {
                TextureView(this@LivePrepareActivity)
            }
            BeautyManager.setupLocalVideo(videoView.apply {
                binding.flVideoContainer.addView(this)
            }, Constants.RENDER_MODE_HIDDEN)
        } else {
            // 设置preview视图
            val videoView = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                SurfaceView(this@LivePrepareActivity)
            } else {
                TextureView(this@LivePrepareActivity)
            }
            mRtcEngine.setupLocalVideo(VideoCanvas(videoView.apply {
                view = this
                binding.flVideoContainer.addView(this)
            }))

            // 下载美颜资源, 下载成功后默认应用商汤美颜
            downloadBeautyResource()
        }

        toggleVideoRun = Runnable {
            initRtcEngine()
        }
        requestCameraPermission(true)

        // 显示开播前视频配置预设对话框
        showPresetDialog()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }

    private var toggleVideoRun: Runnable? = null

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission,
            { getPermissions() }
        ) { launchAppSetting(permission) }
    }

    override fun getPermissions() {
        Thread {
            if (toggleVideoRun != null) {
                toggleVideoRun?.run()
                toggleVideoRun = null
            }
        }.start()
    }

    private fun showPresetDialog() = PresetDialog(this, deviceScore, RtcConnection(mRoomId, UserManager.getInstance().user.id.toInt())).show()
    private fun showDebugModeDialog() = DebugSettingDialog(this).show()

    override fun onResume() {
        super.onResume()
        // 开启摄像头采集
        mRtcEngine.startPreview()
    }

    override fun onPause() {
        super.onPause()
        if (!isFinishToLiveDetail) {
            mRtcEngine.stopPreview()
        }
    }

    override fun finish() {
        super.finish()
        if (!isFinishToLiveDetail) {
            RtcEngineInstance.resetVirtualBackground()
            BeautyManager.destroy()
        }
    }

    private fun initRtcEngine() {
        // 开启摄像头前设置摄像头采集分辨率
        var frameRate: Int
        val index = if (deviceScore >= 90) { // 高端机
            frameRate = 24
            PictureQualityDialog.QUALITY_INDEX_1080P
        } else if (deviceScore >= 75) { // 中端机
            frameRate = 24
            PictureQualityDialog.QUALITY_INDEX_720P
        } else { // 低端机
            frameRate = 15
            PictureQualityDialog.QUALITY_INDEX_720P
        }

        val cacheQualityResolution = PictureQualityDialog.getCacheQualityResolution(index)
        mRtcEngine.setCameraCapturerConfiguration(
            CameraCapturerConfiguration(
                CameraCapturerConfiguration.CaptureFormat(
                    cacheQualityResolution.width,
                    cacheQualityResolution.height,
                    frameRate
                )
            )
        )
        // reset virtual background config
        RtcEngineInstance.virtualBackgroundSource.backgroundSourceType = 0
        RtcEngineInstance.rtcEngine.enableVirtualBackground(false, VirtualBackgroundSource(), SegmentationProperty())
    }

    private fun showBeautyDialog() {
        MultiBeautyDialog(this).apply {
            show()
        }
    }

    private fun copy2Clipboard(roomId: String) {
        val clipboardManager = getSystemService(ClipboardManager::class.java)
        clipboardManager.setPrimaryClip(ClipData.newPlainText(roomId, roomId))
        ToastUtils.showToast(R.string.show_live_prepare_room_clipboard_copyed)
    }

    // 创建房间并开始直播
    private fun createAndStartLive(roomName: String) {
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.show_live_prepare_room_empty)
            binding.etRoomName.requestFocus()
            mInputMethodManager.showSoftInput(binding.etRoomName, 0)
            return
        }

        isFinishToLiveDetail = true
        LiveDetailActivity.launch(this@LivePrepareActivity, ShowRoomDetailModel(
            mRoomId,
            roomName,
            1,
            UserManager.getInstance().user.id.toString(),
            UserManager.getInstance().user.headUrl,
            UserManager.getInstance().user.name,
            TimeUtils.currentTimeMillis().toDouble(),
            TimeUtils.currentTimeMillis().toDouble(),
        ))
        finish()
    }

    // 下载美颜资源, 下载成功后默认应用商汤美颜
    private fun downloadBeautyResource() {
        // 下载资源过程中不允许点击其余的按钮
        binding.tvSetting.isEnabled = false
        binding.tvBeauty.isEnabled = false
        binding.tvRotate.isEnabled = false
        binding.btnStartLive.isEnabled = false

        val beautyResource = AGResourceManager(this)
        var manifest: AGManifest? = null
        beautyResource.checkResource(BuildConfig.BEAUTY_RESOURCE)

        lifecycleScope.launch {
            var downloadSuccess = false
            // 调用processFile处理文件
            beautyResource.downloadManifest(
                url = BuildConfig.BEAUTY_RESOURCE,
                progressHandler = {
                    // 下载成功，可以更新UI
                    ShowLogger.d(tag, "download process: $it")
                },
                completionHandler = { agManifest, e ->
                    if (e == null) {
                        ShowLogger.d(tag, "download success: $agManifest")
                        manifest = agManifest
                    } else {
                        ShowLogger.d(tag, "download failed: ${e.message}")
                        binding.statusPrepareViewLrc.isVisible = false
                        ToastUtils.showToastLong(R.string.show_beauty_loading_failed)
                        downloadSuccess = false
                    }
                }
            )

            manifest?.files?.forEach { resource ->
                ShowLogger.d(tag, "Processing ${resource.url}")
                binding.statusPrepareViewLrc.isVisible = true
                binding.pbLoading.progress = 0
                binding.tvContent.text =
                    String.format(resources.getString(R.string.show_beauty_loading), getBeautySDKName(resource.uri), "0%")

                beautyResource.downloadAndUnZipResource(
                    resource = resource,
                    progressHandler = {
                        binding.pbLoading.progress = it
                        binding.tvContent.text = String.format(resources.getString(R.string.show_beauty_loading), getBeautySDKName(resource.uri), "$it%")
                    },
                    completionHandler = { _, e ->
                        if (e == null) {
                            // 下载成功，可以更新UI
                            ShowLogger.d(tag, "download success: ${resource.uri}")
                            downloadSuccess = true
                        } else {
                            // 下载失败，更新UI显示错误信息
                            ShowLogger.e(tag, e, "download failed: ${e.message}")
                            binding.statusPrepareViewLrc.isVisible = false
                            ToastUtils.showToastLong(R.string.show_beauty_loading_failed)
                            downloadSuccess = false
                        }
                    }
                )
            }

            if (!downloadSuccess) {
                return@launch
            }

            // 动态加载so文件
            val arch = System.getProperty("os.arch")
            ShowLogger.d("hugo", "os.arch: $arch")
            if (arch == "armv7") {
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_bytedance/lib/armeabi-v7a/", "libeffect")
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_faceunity/lib/armeabi-v7a/", "libfuai")
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_faceunity/lib/armeabi-v7a/", "libCNamaSDK")
            } else if (arch == "aarch64") {
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_bytedance/lib/arm64-v8a/", "libeffect")
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_faceunity/lib/arm64-v8a/", "libfuai")
                DynamicLoadUtil.loadSoFile(this@LivePrepareActivity, "${this@LivePrepareActivity.getExternalFilesDir("")?.absolutePath}/assets/beauty_faceunity/lib/arm64-v8a/", "libCNamaSDK")
            }
            faceunity.LoadConfig.loadLibrary(this@LivePrepareActivity.getDir("libs", Context.MODE_PRIVATE).absolutePath)

            // 下载成功后初始化美颜场景化API
            binding.statusPrepareViewLrc.isVisible = false
            binding.tvSetting.isEnabled = true
            binding.tvBeauty.isEnabled = true
            binding.tvRotate.isEnabled = true
            binding.btnStartLive.isEnabled = true

            BeautyManager.initialize(this@LivePrepareActivity, mRtcEngine)
            BeautyManager.setupLocalVideo(SurfaceView(this@LivePrepareActivity).apply {
                binding.flVideoContainer.addView(this)
            }, Constants.RENDER_MODE_HIDDEN)
            view?.let {
                binding.flVideoContainer.removeView(it)
            }
        }
    }


    private fun getRandomRoomId() =
        (Random(TimeUtils.currentTimeMillis()).nextInt(10000) + 100000).toString()


    private fun getBeautySDKName(uri: String): String {
        return when (uri) {
            "beauty_sensetime" -> "商汤"
            "beauty_faceunity" -> "相芯"
            "beauty_bytedance" -> "火山引擎"
            else -> ""
        }
    }
}
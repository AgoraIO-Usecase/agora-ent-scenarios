package io.agora.scene.show

import AGManifest
import AGResourceManager
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.BeautyManager
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.debugSettings.DebugSettingDialog
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.show.widget.PresetDialog
import io.agora.scene.show.widget.beauty.MultiBeautyDialog
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import java.lang.Runnable
import kotlin.random.Random

/*
 * 主播开播前预览页面 activity
 */
@RequiresApi(Build.VERSION_CODES.M)
class LivePrepareActivity : BaseViewBindingActivity<ShowLivePrepareActivityBinding>() {
    private val tag = "LivePrepareActivity"
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mThumbnailId by lazy { getRandomThumbnailId() }
    private val mRoomId by lazy { getRandomRoomId() }

    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var isFinishToLiveDetail = false

    // 美颜资源下载协程
    private var resourceDownloadJob: Job? = null

    private var view: SurfaceView? = null

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
            resourceDownloadJob?.cancel()
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
        binding.tvHD.setOnClickListener {
            showPictureQualityDialog()
        }
        binding.tvSetting.setOnClickListener {
            if (AgoraApplication.the().isDebugModeOpen) {
                showDebugModeDialog()
            } else {
                showPresetDialog()
            }
        }

        if (BuildConfig.BEAUTY_RESOURCE.isEmpty()) {
            binding.statusPrepareViewLrc.isVisible = false
            // 美颜资源文件已放在assets目录内
            BeautyManager.initialize(this@LivePrepareActivity, mRtcEngine)
            BeautyManager.setupLocalVideo(SurfaceView(this@LivePrepareActivity).apply {
                binding.flVideoContainer.addView(this)
            }, Constants.RENDER_MODE_HIDDEN)
        } else {
            // 设置preview视图
            mRtcEngine.setupLocalVideo(VideoCanvas(SurfaceView(this@LivePrepareActivity).apply {
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

    private fun showPresetDialog() = PresetDialog(this, mRtcEngine.queryDeviceScore(), RtcConnection(mRoomId, UserManager.getInstance().user.id.toInt())).show()
    private fun showDebugModeDialog() = DebugSettingDialog(this).show()

    override fun onResume() {
        super.onResume()
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
        val cacheQualityResolution = PictureQualityDialog.getCacheQualityResolution()
        mRtcEngine.setCameraCapturerConfiguration(
            CameraCapturerConfiguration(
                CameraCapturerConfiguration.CaptureFormat(
                    cacheQualityResolution.width,
                    cacheQualityResolution.height,
                    15
                )
            )
        )
        // reset virtual background config
        RtcEngineInstance.virtualBackgroundSource.backgroundSourceType = 0
        RtcEngineInstance.rtcEngine.enableVirtualBackground(false, VirtualBackgroundSource(), SegmentationProperty())
//        mRtcEngine.startPreview()
    }

    private fun showPictureQualityDialog() {
        PictureQualityDialog(this).apply {
            setOnQualitySelectListener { _, _, size ->
                mRtcEngine.setCameraCapturerConfiguration(
                    CameraCapturerConfiguration(
                        CameraCapturerConfiguration.CaptureFormat(
                            size.width,
                            size.height,
                            15
                        )
                    )
                )
            }
            show()
        }
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

    private fun createAndStartLive(roomName: String) {
        if (TextUtils.isEmpty(roomName)) {
            ToastUtils.showToast(R.string.show_live_prepare_room_empty)
            binding.etRoomName.requestFocus()
            mInputMethodManager.showSoftInput(binding.etRoomName, 0)
            return
        }

        binding.btnStartLive.isEnabled = false
        mService.createRoom(mRoomId, roomName, mThumbnailId, {
            runOnUiThread {
                isFinishToLiveDetail = true
                LiveDetailActivity.launch(this@LivePrepareActivity, it)
                finish()
            }
        }, {
            runOnUiThread {
                ToastUtils.showToast(it.message)
                binding.btnStartLive.isEnabled = true
            }
        })
    }

    // 下载美颜资源, 下载成功后默认应用商汤美颜
    @OptIn(DelicateCoroutinesApi::class)
    private fun downloadBeautyResource() {
        // 下载资源过程中不允许点击其余的按钮
        binding.tvSetting.isEnabled = false
        binding.tvBeauty.isEnabled = false
        binding.tvRotate.isEnabled = false
        binding.btnStartLive.isEnabled = false

        val beautyResource = AGResourceManager(this)
        var manifest: AGManifest? = null
        resourceDownloadJob = GlobalScope.launch(Dispatchers.IO) {
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
                    }
                }
            )

            manifest?.files?.forEach { resource ->
                async(Dispatchers.Main) {
                    ShowLogger.d(tag, "Processing ${resource.url}")
                    binding.statusPrepareViewLrc.isVisible = true
                    binding.pbLoading.progress = 0
                    binding.tvContent.text =
                        String.format(resources.getString(R.string.show_beauty_loading), resource.uri, "0%")
                }
                beautyResource.downloadAndUnZipResource(
                    resource = resource,
                    progressHandler = {
                        binding.pbLoading.progress = it
                        binding.tvContent.text = String.format(resources.getString(R.string.show_beauty_loading), resource.uri, "$it%")
                    },
                    completionHandler = { _, e ->
                        if (e == null) {
                            // 下载成功，可以更新UI
                            ShowLogger.d(tag, "download success: ${resource.uri}")
                        } else {
                            // 下载失败，更新UI显示错误信息
                            ShowLogger.e(tag, e, "download failed: ${e.message}")
                            binding.statusPrepareViewLrc.isVisible = false
                            ToastUtils.showToastLong(R.string.show_beauty_loading_failed)
                            resourceDownloadJob?.cancel()
                        }
                    }
                )
            }

            // 下载成功后初始化美颜场景化API
            withContext(Dispatchers.Main) {
                binding.statusPrepareViewLrc.isVisible = false
                binding.tvSetting.isEnabled = true
                binding.tvBeauty.isEnabled = true
                binding.tvRotate.isEnabled = true
                binding.btnStartLive.isEnabled = true

                BeautyManager.initialize(this@LivePrepareActivity, mRtcEngine)
                BeautyManager.setupLocalVideo(SurfaceView(this@LivePrepareActivity).apply {
                    binding.flVideoContainer.addView(this)
                }, Constants.RENDER_MODE_HIDDEN)
            }
        }
    }


    private fun getRandomRoomId() =
        (Random(TimeUtils.currentTimeMillis()).nextInt(10000) + 100000).toString()

    private fun getRandomThumbnailId() =
        Random(TimeUtils.currentTimeMillis()).nextInt(0, 3).toString()

}
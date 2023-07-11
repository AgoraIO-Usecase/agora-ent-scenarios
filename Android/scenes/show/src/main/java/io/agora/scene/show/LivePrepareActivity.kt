package io.agora.scene.show

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
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.beauty.sensetime.*
import io.agora.rtc2.Constants
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.debugSettings.DebugSettingDialog
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.widget.BeautyDialog
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.show.widget.PresetDialog
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.M)
class LivePrepareActivity : BaseViewBindingActivity<ShowLivePrepareActivityBinding>() {

    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mThumbnailId by lazy { getRandomThumbnailId() }
    private val mRoomId by lazy { getRandomRoomId() }
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }

    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var isFinishToLiveDetail = false

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
        binding.ivRoomCover.setImageResource(getThumbnailIcon(mThumbnailId))
        binding.tvRoomId.text = getString(R.string.show_room_id, mRoomId)
        binding.etRoomName.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mInputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        binding.ivClose.setOnClickListener {
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
        mBeautyProcessor.initialize(
            rtcEngine = mRtcEngine,
            captureMode = CaptureMode.Custom,
            statsEnable = true,
            eventCallback = object : IEventCallback {
                override fun onBeautyStats(stats: BeautyStats) {
                    ShowLogger.d("hugo", "BeautyStats stats = $stats")
                }
            }
        )
        mBeautyProcessor.setBeautyEnable(true)
        mBeautyProcessor.getSenseTimeBeautyAPI().setupLocalVideo(SurfaceView(this).apply {
            binding.flVideoContainer.addView(this)
        }, Constants.RENDER_MODE_HIDDEN)

        toggleVideoRun = Runnable {
            mBeautyProcessor.reset()
            initRtcEngine()
            showPresetDialog()
        }
        requestCameraPermission(true)
    }

    private var toggleVideoRun: Runnable? = null

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission,
            { getPermissions() }
        ) { launchAppSetting(permission) }
    }

    override fun getPermissions() {
        if (toggleVideoRun != null) {
            toggleVideoRun?.run()
            toggleVideoRun = null
        }
    }

    private fun showPresetDialog() = PresetDialog(this).show()
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
        BeautyDialog(this).apply {
            setBeautyProcessor(mBeautyProcessor)
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


    private fun getRandomRoomId() =
        (Random(TimeUtils.currentTimeMillis()).nextInt(10000) + 100000).toString()

    private fun getRandomThumbnailId() =
        Random(TimeUtils.currentTimeMillis()).nextInt(0, 3).toString()

    @DrawableRes
    private fun getThumbnailIcon(thumbnailId: String) = when (thumbnailId) {
        "0" -> R.mipmap.show_room_cover_0
        "1" -> R.mipmap.show_room_cover_1
        "2" -> R.mipmap.show_room_cover_2
        "3" -> R.mipmap.show_room_cover_3
        else -> R.mipmap.show_room_cover_0
    }

}
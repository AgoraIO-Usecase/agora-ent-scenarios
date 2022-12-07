package io.agora.scene.show

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import io.agora.rtc2.video.*
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.AdvanceSettingDialog
import io.agora.scene.show.widget.BeautyDialog
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.widget.utils.StatusBarUtil

class LivePrepareActivity : ComponentActivity() {

    private val mBinding by lazy { ShowLivePrepareActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mThumbnailId by lazy { ShowRoomDetailModel.getRandomThumbnailId() }
    private val mRoomId by lazy { ShowRoomDetailModel.getRandomRoomId() }
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }

    private val mPermissionHelp = PermissionHelp(this)
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var isFinishToLiveDetail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mBinding.ivRoomCover.setImageResource(ShowRoomDetailModel.getThumbnailIcon(mThumbnailId))
        mBinding.tvRoomId.text = getString(R.string.show_room_id, mRoomId)
        mBinding.etRoomName.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                mInputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        mBinding.ivClose.setOnClickListener {
            finish()
        }

        mBinding.ivCopy.setOnClickListener {
            // Copy to system clipboard
            copy2Clipboard(mRoomId)
        }
        mBinding.btnStartLive.setOnClickListener {
            createAndStartLive(mBinding.etRoomName.text.toString())
        }
        mBinding.tvRotate.setOnClickListener {
            mRtcEngine.switchCamera()
        }
        mBinding.tvBeauty.setOnClickListener {
            showBeautyDialog()
        }
        mBinding.tvHD.setOnClickListener {
            showPictureQualityDialog()
        }
        mBinding.tvSetting.setOnClickListener {
            showAdvanceSettingDialog()
        }

        checkRequirePerms {
            initRtcEngine()
            showAdvanceSettingDialog().apply {
                setDismissWhenPresetDone(true)
            }
        }
    }

    private fun showAdvanceSettingDialog() =
        AdvanceSettingDialog(this).apply {
            setOnSwitchChangeListener { _, itemId, isChecked ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SWITCH_EAR_BACK -> {
                        mRtcEngine.enableInEarMonitoring(isChecked)
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE -> {
                        mRtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":${isChecked}}")
                        mRtcEngine.setParameters("{\"engine.video.codec_type\":\"${if(isChecked) 3 else 2}\"}")
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_COLOR_ENHANCE -> {
                        mRtcEngine.setColorEnhanceOptions(isChecked, ColorEnhanceOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_DARK_ENHANCE -> {
                        mRtcEngine.setLowlightEnhanceOptions(isChecked, LowLightEnhanceOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE -> {
                        mRtcEngine.setVideoDenoiserOptions(isChecked, VideoDenoiserOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE -> {
                        mRtcEngine.setParameters("{\"rtc.video.enable_pvc\":${isChecked}}")
                    }
                }
            }
            setOnSelectorChangeListener { dialog, itemId, selected ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SELECTOR_RESOLUTION -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            val resolution = dialog.getResolution(selected)
                            dimensions = VideoEncoderConfiguration.VideoDimensions(resolution.width, resolution.height)
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                    AdvanceSettingDialog.ITEM_ID_SELECTOR_FRAMERATE -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            frameRate = dialog.getFrameRate(selected)
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                }
            }
            setOnSeekbarChangeListener { _, itemId, value ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_BITRATE -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            bitrate = value
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_VOCAL_VOLUME -> {
                        mRtcEngine.adjustRecordingSignalVolume(value)
                    }
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_MUSIC_VOLUME -> {
                        mRtcEngine.adjustAudioMixingVolume(value)
                    }
                }
            }
            show()
        }


    private fun checkRequirePerms(force: Boolean = false, granted: () -> Unit) {
        mPermissionHelp.checkCameraPerm(
            {
                mPermissionHelp.checkStoragePerm(
                    granted, { showPermissionLeakDialog(granted) }, force
                )
            },
            { showPermissionLeakDialog(granted) },
            force
        )
    }

    private fun showPermissionLeakDialog(yes: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setMessage(R.string.show_live_perms_leak_tip)
            setCancelable(false)
            setPositiveButton(R.string.show_live_yes) { dialog, _ ->
                dialog.dismiss()
                checkRequirePerms(true, yes)
            }
            setNegativeButton(R.string.show_live_no) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            show()
        }
    }

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
        mRtcEngine.setupLocalVideo(
            VideoCanvas(SurfaceView(this).apply {
                mBinding.flVideoContainer.addView(this)
            })
        )
        mRtcEngine.startPreview()
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
            mBinding.etRoomName.requestFocus()
            mInputMethodManager.showSoftInput(mBinding.etRoomName, 0)
            return
        }

        mBinding.btnStartLive.isEnabled = false
        mService.createRoom(mRoomId, roomName, mThumbnailId, {
            mService.joinRoom(it.roomId, { roomDetailInfo ->
                runOnUiThread {
                    isFinishToLiveDetail = true
                    LiveDetailActivity.launch(this@LivePrepareActivity, roomDetailInfo)
                    finish()
                }
            }, { ex ->
                ToastUtils.showToast(ex.message)
                runOnUiThread {
                    mBinding.btnStartLive.isEnabled = true
                }
            })
        }, {
            runOnUiThread {
                ToastUtils.showToast(it.message)
                mBinding.btnStartLive.isEnabled = true
            }
        })
    }

}
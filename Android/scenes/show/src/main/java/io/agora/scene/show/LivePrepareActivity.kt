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
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.CameraCapturerConfiguration.CAMERA_DIRECTION
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.beauty.IBeautyProcessor
import io.agora.scene.show.beauty.bytedance.BeautyByteDanceImpl
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.BeautyDialog
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.widget.utils.StatusBarUtil

class LivePrepareActivity : ComponentActivity() {

    private val mBinding by lazy { ShowLivePrepareActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mThumbnailId by lazy { ShowRoomDetailModel.getRandomThumbnailId() }
    private val mRoomId by lazy { ShowRoomDetailModel.getRandomRoomId() }
    private val mBeautyProcessor: IBeautyProcessor by lazy { BeautyByteDanceImpl(this) }

    private val mPermissionHelp = PermissionHelp(this)
    private var mRtcEngine: RtcEngineEx? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mBinding.ivRoomCover.setImageResource(ShowRoomDetailModel.getThumbnailIcon(mThumbnailId))
        mBinding.tvRoomId.text = getString(R.string.show_room_id, mRoomId)
        mBinding.etRoomName.setOnEditorActionListener { v, actionId, event ->
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
            VideoSetting.cameraIsFront = !VideoSetting.cameraIsFront
            mRtcEngine?.switchCamera()
        }
        mBinding.tvBeauty.setOnClickListener {
            showBeautyDialog()
        }
        mBinding.tvHD.setOnClickListener {
            showPictureQualityDialog()
        }

        checkRequirePerms {
            initRtcEngine()
        }
    }

    private fun checkRequirePerms(force: Boolean = false, granted: () -> Unit) {
        mPermissionHelp.checkCameraPerm({
            mPermissionHelp.checkStoragePerm(
                granted, { showPermissionLeakDialog(granted) }, force
            )
        }, { showPermissionLeakDialog(granted) }, force
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
        mRtcEngine?.startPreview()
    }

    override fun onPause() {
        super.onPause()
        mRtcEngine?.stopPreview()
    }


    override fun onDestroy() {
        super.onDestroy()
        releaseRtcEngine()
    }

    private fun initRtcEngine() {
        val config = RtcEngineConfig()
        config.mContext = application
        config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                ToastUtils.showToast(
                    "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err)
                )
            }
        }
        mRtcEngine = RtcEngine.create(config) as RtcEngineEx?
        mRtcEngine?.registerVideoFrameObserver(mBeautyProcessor)
        mRtcEngine?.enableVideo()
        mRtcEngine?.setupLocalVideo(
            VideoCanvas(SurfaceView(this).apply {
                mBinding.flVideoContainer.addView(this)
            })
        )
        updateRtcVideoConfig()
        mRtcEngine?.startPreview()
    }

    private fun updateRtcVideoConfig() {
        mRtcEngine?.setCameraCapturerConfiguration(
            CameraCapturerConfiguration(
                if (VideoSetting.cameraIsFront) CAMERA_DIRECTION.CAMERA_FRONT else CAMERA_DIRECTION.CAMERA_REAR,
                CameraCapturerConfiguration.CaptureFormat(
                    VideoSetting.cameraResolution.width,
                    VideoSetting.cameraResolution.height,
                    15
                )
            )
        )
    }

    private fun showPictureQualityDialog() {
        PictureQualityDialog(this).apply {
            setSelectQuality(
                VideoSetting.cameraResolution.width,
                VideoSetting.cameraResolution.height
            )
            setOnQualitySelectListener { dialog, qualityIndex, size ->
                VideoSetting.cameraResolution = size
                updateRtcVideoConfig()
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
                    releaseRtcEngine()
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

    private fun releaseRtcEngine() {
        mRtcEngine?.apply {
            mBeautyProcessor.release()
            stopPreview()
            mRtcEngine = null
            RtcEngine.destroy()
        }
    }
}
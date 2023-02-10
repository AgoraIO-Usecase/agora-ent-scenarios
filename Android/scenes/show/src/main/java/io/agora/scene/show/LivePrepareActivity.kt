package io.agora.scene.show

import android.app.Application
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
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLivePrepareActivityBinding
import io.agora.scene.show.debugSettings.DebugSettingDialog
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.BeautyDialog
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.show.widget.PresetDialog
import io.agora.scene.widget.utils.StatusBarUtil
import kotlin.random.Random

class LivePrepareActivity : ComponentActivity() {

    private val mBinding by lazy { ShowLivePrepareActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val mInputMethodManager by lazy { getSystemService(InputMethodManager::class.java) }

    private val mThumbnailId by lazy { getRandomThumbnailId() }
    private val mRoomId by lazy { getRandomRoomId() }
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }

    private val mPermissionHelp = PermissionHelp(this)
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var isFinishToLiveDetail = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mBinding.ivRoomCover.setImageResource(getThumbnailIcon(mThumbnailId))
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
            if (AgoraApplication.the().isDebugModeOpen) {
                showDebugModeDialog()
            } else {
                showPresetDialog()
            }
        }

        checkRequirePerms {
            mBeautyProcessor.reset()
            initRtcEngine()
            showPresetDialog()
        }
    }

    private fun showPresetDialog() = PresetDialog(this).show()
    private fun showDebugModeDialog() = DebugSettingDialog(this).show()

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
            runOnUiThread {
                isFinishToLiveDetail = true
                LiveDetailActivity.launch(this@LivePrepareActivity, it)
                finish()
            }
        }, {
            runOnUiThread {
                ToastUtils.showToast(it.message)
                mBinding.btnStartLive.isEnabled = true
            }
        })
    }


    private fun getRandomRoomId() = (Random(TimeUtils.currentTimeMillis()).nextInt(10000) + 100000).toString()

    private fun getRandomThumbnailId() = Random(TimeUtils.currentTimeMillis()).nextInt(0, 3).toString()

    @DrawableRes
    private fun getThumbnailIcon(thumbnailId: String) = when (thumbnailId) {
        "0" -> R.mipmap.show_room_cover_0
        "1" -> R.mipmap.show_room_cover_1
        "2" -> R.mipmap.show_room_cover_2
        "3" -> R.mipmap.show_room_cover_3
        else -> R.mipmap.show_room_cover_0
    }

}
package io.agora.scene.showTo1v1.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.LogUploader
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.CallRole
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.audioscenarioapi.AudioScenarioType
import io.agora.audioscenarioapi.SceneType
import io.agora.onetoone.*
import io.agora.scene.base.AgoraScenes
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallDetailActivityBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceListenerProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceNetworkStatus
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDetailSettingDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.DashboardFragment
import io.agora.scene.showTo1v1.ui.view.OnClickJackingListener
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.dialog.showRoomDurationNotice
import io.agora.scene.widget.utils.StatusBarUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

fun Int.number2K(): String {
    if (this < 1000) return this.toString()
    val format = DecimalFormat("0.#")
    // Rounding mode for decimals, RoundingMode.FLOOR means direct truncation
    format.roundingMode = RoundingMode.FLOOR
    return "${format.format(this / 1000f)}k"
}

/*
 * Live streaming single room activity
 */
class RoomDetailActivity : BaseViewBindingActivity<ShowTo1v1CallDetailActivityBinding>() {

    companion object {
        const val TAG = "ShowTo1v1_List"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val EXTRA_ROOM_CALL_CONNECTED = "callConnected"
        private const val ContentInspectName = "ShowTo1v1"

        /**
         * @param callConnected true - already connected    
         */
        fun launch(context: Context, callConnected: Boolean, roomInfo: ShowTo1v1RoomInfo) {
            val intent = Intent(context, RoomDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_CALL_CONNECTED, callConnected)
                putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
            }
            context.startActivity(intent)
        }
    }

    private var mDashboardFragment: DashboardFragment? = null

    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mService by lazy { mShowTo1v1Manger.mService }

    private val mRoomInfo: ShowTo1v1RoomInfo by lazy {
        (intent.getParcelableExtra(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!!
    }

    private val mCallConnected: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ROOM_CALL_CONNECTED, false)
    }

    private val isRoomOwner by lazy { mRoomInfo.userId == UserManager.getInstance().user.id.toString() }

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }

    private val mainHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private var mCallDialog: CallDialog? = null
    private var mCallSettingDialogs: CallDetailSettingDialog? = null

    // Current call state
    private var mCallState = CallStateType.Connected

    private val mainChannelMediaOptions by lazy {
        ChannelMediaOptions()
    }

    private val dataFormat by lazy {
        SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
    }

    private var cameraOn = true
    private var micOn = true

    private val timerRoomRun = object : Runnable {
        override fun run() {
            if (mCallState == CallStateType.Connected && mTimeLinkAt > 0) {
                binding.tvCallingTime.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mTimeLinkAt))
            } else {
                binding.tvTime.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt))
            }
            mainHandler.postDelayed(this, 1000)
        }
    }

    private var mTimeLinkAt: Long = 0

    // Job object for canceling coroutines
    private var imageLoadingJob: Job? = null

    // Live streaming textureView
//    private val mShowTextureView by lazy { SurfaceView(this) }
    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1CallDetailActivityBinding {
        return ShowTo1v1CallDetailActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        StatusBarUtil.hideStatusBar(window, true)

        val roomLeftTime = SceneConfigManager.oneOnOneExpireTime * 1000 - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt)
        if (roomLeftTime > 0) {
            initRtcEngine()
            initServiceWithJoinRoom()

            // If viewer, reinitialize callApi
            if (isRoomOwner) {
                mShowTo1v1Manger.prepareCall(CallRole.CALLEE, mRoomInfo.roomId) {
                    mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                }
            } else if (mCallConnected) {
                mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                setRtcHandler()
            } else {
                mShowTo1v1Manger.prepareCall(CallRole.CALLER, mRoomInfo.roomId) {
                    mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                }
            }
            mainHandler.post(timerRoomRun)

            showRoomDurationNotice(SceneConfigManager.oneOnOneExpireTime)
        } else {
            ToastUtils.showToast(getString(R.string.show_to1v1_end_tips))
            finish()
        }
    }

    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.layoutTop.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onExitRoom()
        }
    }

    private fun onExitRoom(){
        if (isGoingFinish) return
        isGoingFinish = true
        stopCallAnimator()
        mainHandler.removeCallbacks(timerRoomRun)
        mainHandler.removeCallbacks(connectedViewCloseRun)
        mainHandler.removeCallbacksAndMessages(null)

        mShowTo1v1Manger.mCallApi.removeListener(callApiListener)
        if (isRoomOwner) {
            mShowTo1v1Manger.deInitialize()
        } else {
            onHangup()
        }
        mShowTo1v1Manger.mRemoteUser = null
        mShowTo1v1Manger.mConnectedChannelId = null
        destroy()
        finish()
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        // Default remote is big window, local is small window
        (mShowTo1v1Manger.mRemoteVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mRemoteVideoView)
        if (binding.vDragBigWindow.canvasContainer.childCount > 0) {
            binding.vDragBigWindow.canvasContainer.removeAllViews()
        }
        binding.vDragBigWindow.canvasContainerAddView(mShowTo1v1Manger.mRemoteVideoView)
        (mShowTo1v1Manger.mLocalVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mLocalVideoView)
        if (binding.vDragSmallWindow.canvasContainer.childCount > 0) {
            binding.vDragSmallWindow.canvasContainer.removeAllViews()
        }
        binding.vDragSmallWindow.canvasContainerAddView(mShowTo1v1Manger.mLocalVideoView)

        imageLoadingJob = lifecycleScope.launch {
            // Load image in IO thread
            val bitmap = loadImageInBackground(mRoomInfo.avatar)
            // Switch to main thread to update UI
            binding.ivUserAvatar.setImageBitmap(bitmap)
        }

        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvNickname.text = mRoomInfo.userName
        binding.tvRoomNum.text = mRoomInfo.roomId

        binding.ivClose.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                ShowTo1v1Logger.d(TAG, "click close button!")
                onExitRoom()
            }
        })

        binding.ivMore.setOnClickListener(object :OnClickJackingListener(){
            override fun onClickJacking(view: View) {
                TopFunctionDialog(this@RoomDetailActivity).show()
            }
        })

        binding.ivSetting.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                onShowSettingDialog()
            }
        })
        binding.ivDashboardClose.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                binding.ivDashboardClose.visibility = View.INVISIBLE
                binding.flDashboard.visibility = View.INVISIBLE
                mDashboardFragment?.updateVisible(false)
            }
        })

        binding.ivHangup.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                ShowTo1v1Logger.d(TAG, "click hangup")
                if (mCallConnected) {
                    onExitRoom()
                } else {
                    onHangup()
                }
            }
        })
        binding.layoutCallPrivately.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                ShowTo1v1Logger.d(TAG, "click call privately")
                toggleSelfVideo(true) {
                    mShowTo1v1Manger.prepareCall(CallRole.CALLER, mRoomInfo.roomId, callback = {
                        if (it) {
                            mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                            mShowTo1v1Manger.mCallApi.call(mRoomInfo.getIntUserId(), completion = { error ->
                                if (error != null && mCallState == CallStateType.Calling) {
                                    ToastUtils.showToast(getString(R.string.show_to1v1_call_failed, error.code.toString()))
                                    // Call failed immediately, hang up
                                    mShowTo1v1Manger.mCallApi.cancelCall {  }
                                    mCallDialog?.let {
                                        if (it.isShowing) it.dismiss()
                                        mCallDialog = null
                                    }
                                }
                            })
                        } else {
                            // Failed state needs to release resources and re-init
                            mShowTo1v1Manger.deInitialize()
                        }
                    })
                }
                toggleSelfAudio(true) {}
            }
        })

        binding.includeConnectedView.root.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                ShowTo1v1Logger.d(TAG, "click close connection view")
                mainHandler.removeCallbacks(connectedViewCloseRun)
                animateConnectedViewClose()
            }

        })
        binding.vDragSmallWindow.setOnViewClick {
            ShowTo1v1Logger.d(TAG, "click switch video")
            exchangeDragWindow()
        }
        binding.layoutCall.isVisible = false
        if (isRoomOwner) {
            binding.layoutCallPrivatelyBg.isVisible = false
            binding.layoutCallPrivately.isVisible = false
        } else {
            binding.layoutCallPrivatelyBg.isVisible = !mCallConnected
            binding.layoutCallPrivately.isVisible = !mCallConnected
            binding.includeComeSoonView.root.isVisible = true
        }
        var resourceId: Int
        try {
            resourceId = resources.getIdentifier(mRoomInfo.bgImage(), "drawable", packageName)
        } catch (e: Exception) {
            resourceId = R.drawable.show_to1v1_user_bg1
            ShowTo1v1Logger.e(TAG, e,"getResources error!")
        }
        val drawable = ContextCompat.getDrawable(this, resourceId)
        Glide.with(this).load(drawable).into(binding.ivRoomCover)
        val fragment = DashboardFragment.newInstance(mRoomInfo, mShowTo1v1Manger.mConnectedChannelId)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        mDashboardFragment = fragment

//        binding.flDashboard.visibility = View.VISIBLE
//        binding.ivDashboardClose.visibility = View.VISIBLE
        mDashboardFragment?.updateVisible(true)
        binding.vDragBigWindow.setComeBackSoonViewStyle(false)
        binding.vDragSmallWindow.setComeBackSoonViewStyle(true)
    }

    // Load image in background thread
    private suspend fun loadImageInBackground(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(this@RoomDetailActivity)
                    .asBitmap()
                    .load(url)
                    .apply(RequestOptions.circleCropTransform())
                    .submit()
                    .get() // Wait for loading to complete and get Bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun onCallSend(user: ShowTo1v1UserInfo) {
        val dialog = CallSendDialog(this, user)
        dialog.setListener(object : CallSendDialog.CallSendDialogListener {
            override fun onSendViewDidClickHangup() {
                mShowTo1v1Manger.mCallApi.cancelCall(null)
            }
        })
        dialog.show()
        mCallDialog = dialog
    }

    private fun onShowSettingDialog(needShow: Boolean = true) {
        val dialog = CallDetailSettingDialog(this@RoomDetailActivity, cameraOn, micOn)
        dialog.setListener(object : CallDetailSettingDialog.CallDetailSettingItemListener {
            override fun onClickDashboard() {
                binding.flDashboard.visibility = View.VISIBLE
                binding.ivDashboardClose.visibility = View.VISIBLE
                mDashboardFragment?.updateVisible(true)
            }

            override fun onCameraSwitch(isCameraOn: Boolean) {
                cameraOn = isCameraOn
                if (cameraOn) {
                    binding.vDragSmallWindow.showComeBackSoonView(false)
                } else {
                    binding.vDragSmallWindow.showComeBackSoonView(true)
                }
                mShowTo1v1Manger.switchCamera(isCameraOn)
            }

            override fun onMicSwitch(isMicOn: Boolean) {
                micOn = isMicOn
                mShowTo1v1Manger.switchMic(isMicOn)
            }
        })
        dialog.hideCameraAndMicBtn(mCallState != CallStateType.Connected && !mCallConnected)
        dialog.show()
        if (!needShow) {
            dialog.dismiss()
        }
        mCallSettingDialogs = dialog
    }

    private var toggleVideoRun: Runnable? = null
    private var toggleAudioRun: Runnable? = null

    override fun getPermissions() {
        toggleVideoRun?.let {
            it.run()
            toggleVideoRun = null
        }
        toggleAudioRun?.let {
            it.run()
            toggleAudioRun = null
        }
    }

    private fun toggleSelfVideo(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleVideoRun = Runnable { callback.invoke() }
            requestCameraPermission(true)
        } else {
            callback.invoke()
        }
    }

    private fun toggleSelfAudio(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(
            permission,
            {
                if (isRoomOwner) getPermissions()
            },
            {
                launchAppSetting(permission)
            }
        )
    }

    private fun initRtcEngine() {
        toggleSelfVideo(isRoomOwner) {
            if (isRoomOwner) {
                mRtcEngine.startPreview()
            }
            joinChannel()
            if (mCallConnected) {
                updateCallState(CallStateType.Connected)
            } else {
                updateCallState(CallStateType.Idle)
            }
        }
        toggleSelfAudio(isRoomOwner) {
            if (isRoomOwner) {
                mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Show, AudioScenarioType.Show_Host)
            }
        }
        if (!isRoomOwner) {
            mRtcEngine.adjustUserPlaybackSignalVolumeEx(mRoomInfo.userId.toInt(), 100, mMainRtcConnection)
        }
    }

    private fun joinChannel() {
        val rtcConnection = mMainRtcConnection
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId

        if (!isRoomOwner && mRtcEngine.queryDeviceScore() < 75) {
            // Low-end device viewers enable hardware decoding by default when joining channel (to solve high resolution stuttering), but hardware decoding in 410 branch adds 200ms startup delay
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // Low-end device viewers enable zero-copy downlink by default when joining channel, but zero-copy downlink conflicts with super-resolution, so super-resolution is disabled by default on low-end devices
            mRtcEngine.setParameters("\"rtc.video.decoder_out_byte_frame\": true")
        } else {
            // Host disables hardware decoding by default when joining channel
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 0}")
        }

        mainChannelMediaOptions.clientRoleType =
            if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        mainChannelMediaOptions.autoSubscribeVideo = true
        mainChannelMediaOptions.autoSubscribeAudio = true
        mainChannelMediaOptions.publishCameraTrack = isRoomOwner
        mainChannelMediaOptions.publishMicrophoneTrack = isRoomOwner
        // If viewer, set audienceLatencyLevel to AUDIENCE_LATENCY_LEVEL_LOW_LATENCY (ultra-low latency)
        if (!isRoomOwner) {
            mainChannelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }
        mRtcEngine.joinChannelEx(
            mShowTo1v1Manger.generalToken(),
            rtcConnection,
            mainChannelMediaOptions,
            null)
        if (isRoomOwner) {
            enableContentInspectEx(true, mMainRtcConnection)
            AudioModeration.moderationAudio(
                channelName, uid, AudioModeration.AgoraChannelType.broadcast, ContentInspectName
            )
            mRtcEngine.setVideoEncoderConfigurationEx(
                VideoEncoderConfiguration().apply {
                    dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                    frameRate = 24
                    degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                },
                rtcConnection
            )
        }
    }

    private fun setupVideoView(publish: Boolean) {

        if (isRoomOwner) {
            if (publish) {
                mRtcEngine.setupLocalVideo(VideoCanvas(binding.textureVideo, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            } else {
                mRtcEngine.setupLocalVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            }
        } else {
            if (publish) {
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(binding.textureVideo, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.getIntUserId()),
                    mMainRtcConnection
                )
            } else {
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.getIntUserId()),
                    mMainRtcConnection
                )
            }
        }
    }

    //================== Service Operation ===============
    private fun initServiceWithJoinRoom() {
        mService?.joinRoom(mRoomInfo, completion = { error ->
            if (error == null) { // success

            } else { //failed
                ToastUtils.showToast(getString(R.string.show_to1v1_enter_room_failed, error.message))
                onExitRoom()
            }
        })

        mService?.subscribeListener(object : ShowTo1v1ServiceListenerProtocol {
            override fun onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus) {

            }

            override fun onUserListDidChanged(userNum: Int) {
                binding.tvNumCount.text = userNum.number2K()
            }

            override fun onRoomDidDestroy(roomId: String) {
                if (mRoomInfo.roomId == roomId) {
                    ToastUtils.showToast(R.string.show_to1v1_end_tips)
                    onExitRoom()
                }
            }

            override fun onRoomTimeUp() {
                ToastUtils.showToast(R.string.show_to1v1_end_tips)
                onExitRoom()
            }
        })
    }

    override fun onPause() {
        stopCallAnimator()
        super.onPause()
    }


    private var callAnimatorSet: AnimatorSet? = null

    private fun startCallAnimator() {
        if (callAnimatorSet == null) {
            callAnimatorSet = binding.layoutCallPrivatelyBg.breathAnim()
        }
        callAnimatorSet?.cancel()
        callAnimatorSet?.start()
    }

    private fun stopCallAnimator() {
        callAnimatorSet?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (!isRoomOwner && binding.layoutCallPrivatelyBg.isVisible) {
            startCallAnimator()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    override fun onDestroy() {
        super.onDestroy()
        imageLoadingJob?.cancel()
        if (SceneConfigManager.logUpload) {
            LogUploader.uploadLog(AgoraScenes.ShowTo1v1)
        }
    }

    override fun finish() {
        onBackPressedCallback.remove()
        super.finish()
    }

    private fun destroy() {
        mService?.leaveRoom(mRoomInfo, completion = {})
        if (isRoomOwner) {
            mRtcEngine.leaveChannelEx(mMainRtcConnection)
            mRtcEngine.stopPreview()
        } else {
            mainChannelMediaOptions.autoSubscribeVideo = true
            mainChannelMediaOptions.autoSubscribeAudio = false
            mRtcEngine.updateChannelMediaOptionsEx(mainChannelMediaOptions, mMainRtcConnection)
        }
    }

    private fun publishMedia(publish: Boolean) {
        mainChannelMediaOptions.publishMicrophoneTrack = isRoomOwner && publish
        mainChannelMediaOptions.publishCameraTrack = isRoomOwner && publish
        mainChannelMediaOptions.autoSubscribeVideo = publish
        mainChannelMediaOptions.autoSubscribeAudio = publish
        mRtcEngine.updateChannelMediaOptionsEx(mainChannelMediaOptions, mMainRtcConnection)
    }

    private val connectedViewCloseRun = Runnable {
        animateConnectedViewClose()
    }

    // Connection open animation
    private var openAnimator: Animator? = null

    private fun animateConnectedViewOpen() {
        if (binding.includeConnectedView.root.isVisible) return
        openAnimator?.cancel() // Stop previous animation
        binding.includeConnectedView.root.isVisible = true
        openAnimator = createOpenAnimator().apply {
            start()
        }
    }

    private fun createOpenAnimator(): AnimatorSet {
        val alphaAnim = ObjectAnimator.ofFloat(binding.includeConnectedView.root, "alpha", 0.5f, 1.0f).apply {
            duration = 200
        }

        val translationYAnim = ObjectAnimator.ofFloat(
            binding.includeConnectedView.root,
            "translationY",
            -binding.includeConnectedView.root.height.toFloat(),
            0f
        ).apply {
            duration = 500
        }

        return AnimatorSet().apply {
            playTogether(alphaAnim, translationYAnim)
        }
    }

    private var closeAnimator: Animator? = null

    private fun animateConnectedViewClose() {
        if (!binding.includeConnectedView.root.isVisible) return

        closeAnimator?.cancel() // Stop previous animation

        closeAnimator = createCloseAnimator().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.includeConnectedView.root.isVisible = false
                }
            })
            start()
        }
    }

    private fun createCloseAnimator(): AnimatorSet {
        val alphaAnim = ObjectAnimator.ofFloat(binding.includeConnectedView.root, "alpha", 1.0f, 0.5f).apply {
            duration = 200
        }

        val translationYAnim = ObjectAnimator.ofFloat(
            binding.includeConnectedView.root,
            "translationY",
            0f,
            -binding.includeConnectedView.root.height.toFloat()
        ).apply {
            duration = 500
        }

        return AnimatorSet().apply {
            playTogether(alphaAnim, translationYAnim)
        }
    }

    private var exchanged = false

    private fun exchangeDragWindow() {
        val paramsBig = FrameLayout.LayoutParams(binding.vDragBigWindow.width, binding.vDragBigWindow.height)
        paramsBig.topMargin = binding.vDragBigWindow.top
        paramsBig.leftMargin = binding.vDragBigWindow.left
        val paramsSmall = FrameLayout.LayoutParams(binding.vDragSmallWindow.width, binding.vDragSmallWindow.height)
        paramsSmall.topMargin = binding.vDragSmallWindow.top
        paramsSmall.leftMargin = binding.vDragSmallWindow.left
        binding.vDragBigWindow.layoutParams = paramsSmall
        binding.vDragSmallWindow.layoutParams = paramsBig
        if (binding.vDragBigWindow.layoutParams.height > binding.vDragSmallWindow.layoutParams.height) {
            binding.vDragSmallWindow.bringToFront()
            binding.vDragSmallWindow.setSmallType(true)
            binding.vDragSmallWindow.setOnViewClick {
                exchangeDragWindow()
            }
            binding.vDragBigWindow.setOnViewClick(null)
            binding.vDragBigWindow.setSmallType(false)

            mShowTo1v1Manger.mRemoteUser?.let {
                binding.tvCallingNickname.text = it.userName
                binding.tvCallingUid.text = it.userId
                GlideApp.with(this)
                    .load(it.avatar)
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivCallingAvatar)
            }
        } else {
            binding.vDragBigWindow.bringToFront()
            binding.vDragBigWindow.setSmallType(true)
            binding.vDragBigWindow.setOnViewClick {
                exchangeDragWindow()
            }
            binding.vDragSmallWindow.setOnViewClick(null)
            binding.vDragSmallWindow.setSmallType(false)
            mShowTo1v1Manger.mCurrentUser.let {
                binding.tvCallingNickname.text = it.userName
                binding.tvCallingUid.text = it.userId
                GlideApp.with(this)
                    .load(it.avatar)
                    .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.ivCallingAvatar)
            }
        }
        exchanged = !exchanged
    }

    private fun enableContentInspectEx(enable: Boolean, connection: RtcConnection) {
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", ContentInspectName)
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectConfig.ContentInspectModule()
            module.interval = 60
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            val ret = mRtcEngine.enableContentInspectEx(enable, contentInspectConfig, connection)
            ShowTo1v1Logger.d(TAG, "enableContentInspectEx $ret")
        } catch (_: JSONException) {
        }
    }

    @Volatile
    private var isGoingFinish = false
    private fun onHangup() {
        mShowTo1v1Manger.mRemoteUser?.let {
            if (isRoomOwner) {
                mShowTo1v1Manger.mCallApi.hangup(it.getIntUserId(), reason = "hangup by user", null)
            } else {
                mShowTo1v1Manger.mCallApi.hangup(mRoomInfo.getIntUserId(), reason = "hangup by user", null)
            }
        }
    }

    private fun finishCallDialog() {
        mCallDialog?.let {
            if (it.isShowing) it.dismiss()
            mCallDialog = null
        }
    }

    private val callApiListener = object : ICallApiListener {

        override fun callDebugInfo(message: String, logLevel: CallLogLevel) {
            super.callDebugInfo(message, logLevel)
            when (logLevel) {
                CallLogLevel.Normal, CallLogLevel.Warning -> ShowTo1v1Logger.d(TAG, "callDebugInfo $message")
                CallLogLevel.Error -> ShowTo1v1Logger.e(TAG, null, "callDebugInfo $message")
            }
        }

        override fun onCallEventChanged(event: CallEvent, eventReason: String?) {
            super.onCallEventChanged(event, eventReason)
            when (event) {
                CallEvent.LocalLeft -> {
                    onHangup()
                }

                CallEvent.RemoteLeft -> {
                    // Caller offline, hang up
                    eventReason?.let {
                        if (it.toInt() == Constants.USER_OFFLINE_DROPPED) {
                            ToastUtils.showToast(getString(R.string.show_to1v1_end_linking_tips2))
                        }
                    }
                    onHangup()
                }

                else -> {}
            }
        }

        override fun onCallError(
            errorEvent: CallErrorEvent,
            errorType: CallErrorCodeType,
            errorCode: Int,
            message: String?
        ) {
            super.onCallError(errorEvent, errorType, errorCode, message)
            ShowTo1v1Logger.e(TAG, Exception(message),"onCallError: errorEvent$errorEvent, errorType:$errorType,errorCode:$errorCode")
        }

        override fun canJoinRtcOnCalling(eventInfo: Map<String, Any>): Boolean {
            return true
        }

        override fun onCallStateChanged(
            state: CallStateType,
            stateReason: CallStateReason,
            eventReason: String,
            eventInfo: Map<String, Any>
        ) {
            val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
            if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
            ShowTo1v1Logger.d(TAG, "RoomDetail onCallStateChanged state:${state.name},stateReason:${stateReason.name},eventReason:${eventReason}")
            updateCallState(state)
            when (state) {
                CallStateType.Prepared -> {
                    when (stateReason) {
                        CallStateReason.RemoteHangup -> {
                            ToastUtils.showToast(R.string.show_to1v1_end_linking_tips)
                            if (mCallConnected && !isRoomOwner) {
                                onExitRoom()
                            }
                        }

                        CallStateReason.CallingTimeout,
                        CallStateReason.RemoteRejected -> {
                            if (!isRoomOwner) {
                                ToastUtils.showToast(getString(R.string.show_to1v1_no_answer))
                            }
                        }

                        CallStateReason.RemoteCallBusy -> {
                            if (!isRoomOwner) {
                                ToastUtils.showToast(getString(R.string.show_to1v1_call_toast_remote_busy))
                            }
                        }

                        else -> {}
                    }
                    finishCallDialog()
                    mShowTo1v1Manger.mRemoteUser = null
                    mShowTo1v1Manger.mConnectedChannelId = null

                    if (isRoomOwner) {
                        // Call ended, restore audio configuration
                        mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Show, AudioScenarioType.Show_Host)
                    }
                }

                CallStateType.Calling -> {
                    val fromUserId = eventInfo[CallApiImpl.kFromUserId] as? Int ?: 0
                    val fromRoomId = eventInfo[CallApiImpl.kFromRoomId] as? String ?: ""
                    val toUserId = eventInfo[CallApiImpl.kRemoteUserId] as? Int ?: 0
                    if (mShowTo1v1Manger.mRemoteUser != null && mShowTo1v1Manger.mRemoteUser!!.userId != fromUserId
                            .toString()
                    ) {
                        mShowTo1v1Manger.mCallApi.reject(fromUserId, "already calling") { err ->
                        }
                        return
                    }
                    // Process only if the triggering user is yourself
                    if (mShowTo1v1Manger.mCurrentUser.userId == toUserId.toString()) {
                        // Received caller
                        mShowTo1v1Manger.isCaller = false
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        val userMap = eventInfo[CallApiImpl.kFromUserExtension] as JSONObject

                        mShowTo1v1Manger.mRemoteUser = ShowTo1v1UserInfo(
                            userMap.getString("userId"),
                            userMap.getString("userName"),
                            userMap.getString("avatar"),
                            userMap.optString("objectId", ""),
                            userMap.optLong("createdAt", 0L)
                        )
                        mShowTo1v1Manger.mCallApi.accept(fromUserId) {}
                    } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                        // Caller
                        mShowTo1v1Manger.isCaller = true
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        mShowTo1v1Manger.mRemoteUser = mRoomInfo
                        onCallSend(mShowTo1v1Manger.mRemoteUser!!)
                    }


                    // Set video best practice
                    mShowTo1v1Manger.mRtcEngine.setVideoEncoderConfigurationEx(
                        VideoEncoderConfiguration().apply {
                            dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                            frameRate = 24
                            degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                        },
                        RtcConnection(mShowTo1v1Manger.mConnectedChannelId, mShowTo1v1Manger.mCurrentUser.userId.toInt())
                    )
                    mShowTo1v1Manger.mRtcEngine.setParameters("{\"che.video.videoCodecIndex\": 2}")
                }

                CallStateType.Connecting -> {
                    if (stateReason == CallStateReason.LocalAccepted || stateReason == CallStateReason.RemoteAccepted) {
                        ShowTo1v1Logger.d(TAG, "call Connecting LocalAccepted or RemoteAccepted")
                    }
                }

                CallStateType.Connected -> {
                    finishCallDialog()
                    // Enable video and audio moderation
                    val channelId = mShowTo1v1Manger.mConnectedChannelId ?: ""
                    val localUid = mShowTo1v1Manger.mCurrentUser.userId.toInt()

                    // Video moderation
                    enableContentInspectEx(true, RtcConnection(channelId, localUid))
                    // Audio moderation
                    AudioModeration.moderationAudio(
                        channelId, localUid.toLong(), AudioModeration.AgoraChannelType.broadcast,
                        "ShowTo1v1"
                    )

                    cameraOn = true
                    micOn = true
                    setRtcHandler()

                    binding.root.postDelayed({
                        // Set audio best practice
                        if (mShowTo1v1Manger.isCaller) {
                            // Caller
                            mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Caller)
                        } else {
                            // Called
                            mShowTo1v1Manger.scenarioApi.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Callee)
                        }
                    }, 500)
                }

                CallStateType.Failed -> {
                    finishCallDialog()
                    ToastUtils.showToast(eventReason)
                }

                CallStateType.Idle -> TODO()
            }
        }
    }

    private fun updateCallState(state: CallStateType) {
        if (isGoingFinish) return
        mCallState = state
        mDashboardFragment?.updateCallState(mCallState)
        when (mCallState) {
            CallStateType.Calling -> {
                publishMedia(false)
                setupVideoView(false)
            }

            CallStateType.Prepared,
            CallStateType.Idle,
            CallStateType.Failed -> {
                mTimeLinkAt = 0
                mShowTo1v1Manger.mRtcEngine.enableLocalAudio(true)
                mShowTo1v1Manger.mRtcEngine.enableLocalVideo(true)
                publishMedia(true)
                setupVideoView(true)

                if (!isRoomOwner && !isGoingFinish) {
                    binding.layoutCallPrivatelyBg.isVisible = true
                    binding.layoutCallPrivately.isVisible = true
                    startCallAnimator()
                }
                binding.layoutNumCount.isVisible = true
                binding.ivClose.isVisible = true
                binding.groupHangup.isVisible = false
                binding.layoutCallingTop.isVisible = false
                binding.layoutRoomTop.isVisible = true
                binding.layoutCall.isVisible = false
                binding.textureVideo.isVisible = true
                if (exchanged) {
                    // Restore default window
                    exchangeDragWindow()
                }

                animateConnectedViewClose()

                mCallSettingDialogs?.dismiss()
            }

            CallStateType.Connected -> {
                mCallSettingDialogs?.dismiss()
                binding.vDragBigWindow.showComeBackSoonView(false)
                binding.vDragSmallWindow.showComeBackSoonView(false)
                mShowTo1v1Manger.mConnectedChannelId?.let {
                    mDashboardFragment?.renewCallChannel(it)
                }
                mTimeLinkAt = TimeUtils.currentTimeMillis()
                if (exchanged) {
                    binding.vDragBigWindow.setSmallType(true)
                    binding.vDragSmallWindow.setSmallType(false)
                } else {
                    binding.vDragBigWindow.setSmallType(false)
                    binding.vDragSmallWindow.setSmallType(true)
                }
                mShowTo1v1Manger.mRemoteUser?.let {
                    binding.vDragBigWindow.setUserName(it.userName)
                    // Top left shows the room name and nickname of the big window
                    binding.tvCallingNickname.text = it.userName
                    binding.tvCallingUid.text = it.userId
                    GlideApp.with(this)
                        .load(it.avatar)
                        .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivCallingAvatar)
                }
                if (mShowTo1v1Manger.mRemoteUser == null) {
                    ShowTo1v1Logger.e(TAG, Exception("Connected but remoteUser is null"))
                }
                mShowTo1v1Manger.mCurrentUser.let {
                    binding.vDragSmallWindow.setUserName(it.userName)
                }
                binding.textureVideo.isVisible = false
                binding.layoutCall.isVisible = true
                binding.layoutCallingTop.isVisible = true

                binding.layoutRoomTop.isVisible = false
                binding.layoutNumCount.isVisible = false
                binding.ivClose.isVisible = false
                binding.groupHangup.isVisible = true

                if (isRoomOwner) {
                    mShowTo1v1Manger.mRemoteUser?.let {
                        GlideApp.with(this)
                            .load(it.avatar)
                            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                            .apply(RequestOptions.circleCropTransform())
                            .into(binding.includeConnectedView.ivUserAvatar)
                        binding.includeConnectedView.tvNickname.text = it.userName
                    }
                    animateConnectedViewOpen()
                    mainHandler.removeCallbacks(connectedViewCloseRun)
                    mainHandler.postDelayed(connectedViewCloseRun, 5000)
                } else {
                    binding.layoutCallPrivatelyBg.isVisible = false
                    binding.layoutCallPrivately.isVisible = false
                    stopCallAnimator()
                }
            }

            else -> {}
        }
    }

    private fun setRtcHandler() {
        // Monitor video stream status callback after call starts, used to display corresponding UI when video stream status changes
        // Since CallAPI uses joinChannelEx to join the channel, addHandlerEx needs to be used here to register the listener
        mShowTo1v1Manger.mRtcEngine.addHandlerEx(
            object : IRtcEngineEventHandler() {
                override fun onRemoteVideoStateChanged(
                    uid: Int,
                    state: Int,
                    reason: Int,
                    elapsed: Int
                ) {
                    super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                    if (state == Constants.REMOTE_VIDEO_STATE_STOPPED || state == Constants.REMOTE_VIDEO_STATE_FAILED) {
                        // Remote video stopped receiving
                        runOnUiThread {
                            binding.vDragBigWindow.showComeBackSoonView(true)
                        }
                    } else if (state == Constants.REMOTE_VIDEO_STATE_STARTING || state == Constants.REMOTE_VIDEO_STATE_DECODING) {
                        // Remote video playing normally
                        runOnUiThread {
                            binding.vDragBigWindow.showComeBackSoonView(false)
                        }
                    }
                }
            },
            RtcConnection(mShowTo1v1Manger.mConnectedChannelId, mShowTo1v1Manger.mCurrentUser.userId.toInt())
        )
    }
}

private fun View.breathAnim(): AnimatorSet {
    val scaleXAnima = ObjectAnimator.ofFloat(this, "scaleX", 0.9f, 1f)
        .apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = 800
        }

    val scaleYAnima = ObjectAnimator.ofFloat(this, "scaleY", 0.8f, 1f)
        .apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            duration = 800
        }
    val animatorSet = AnimatorSet().apply {
        playTogether(scaleXAnima, scaleYAnima)
        start()
    }

    return animatorSet
}
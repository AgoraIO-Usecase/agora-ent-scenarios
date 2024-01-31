package io.agora.scene.showTo1v1.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.CallRole
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.callapi.*
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallDetailActivityBinding
import io.agora.scene.showTo1v1.service.ROOM_AVAILABLE_DURATION
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceListenerProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceNetworkStatus
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDetailSettingDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.DashboardFragment
import io.agora.scene.showTo1v1.ui.view.OnClickJackingListener
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.utils.StatusBarUtil
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
    //未保留小数的舍弃规则，RoundingMode.FLOOR表示直接舍弃。
    format.roundingMode = RoundingMode.FLOOR
    return "${format.format(this / 1000f)}k"
}

/*
 * 秀场直播单房间 activity
 */
class RoomDetailActivity : BaseViewBindingActivity<ShowTo1v1CallDetailActivityBinding>() {

    companion object {
        const val TAG = "ShowTo1v1_List"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val EXTRA_ROOM_CALL_CONNECTED = "callConnected"
        private const val ContentInspectName = "ShowTo1v1"

        /**
         * @param callConnected true 已经连接
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

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }

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

    // 当前呼叫状态
    private var mCallState = CallStateType.Idle

    private val mainChannelMediaOptions by lazy {
        ChannelMediaOptions()
    }

    private val dataFormat by lazy {
        SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
    }

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

    // 秀场 textureView
//    private val mShowTextureView by lazy { SurfaceView(this) }
    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1CallDetailActivityBinding {
        return ShowTo1v1CallDetailActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        StatusBarUtil.hideStatusBar(window, true)

        val roomLeftTime = ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt)
        if (roomLeftTime > 0) {
            initRtcEngine()
            initServiceWithJoinRoom()

            // 被呼叫需要重新初始化 callApi
            if (isRoomOwner) {
                mShowTo1v1Manger.prepareCall(CallRole.CALLEE, mRoomInfo.roomId) {
                    mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                }
            } else if (mCallConnected) {
                mShowTo1v1Manger.mCallApi.addListener(callApiListener)
            } else {
                mShowTo1v1Manger.prepareCall(CallRole.CALLER, mRoomInfo.roomId) {
                    mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                }
            }
            mainHandler.post(timerRoomRun)
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

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        // 默认远端都是大窗, 本地是小窗
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

        Glide.with(this)
            .load(mRoomInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvNickname.text = mRoomInfo.userName
        binding.tvRoomNum.text = mRoomInfo.roomId

        binding.ivClose.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click close end!")
                onBackPressed()
            }
        })

        binding.ivMore.setOnClickListener(object :OnClickJackingListener(){
            override fun onClickJacking(view: View) {
                TopFunctionDialog(this@RoomDetailActivity).show()
            }
        })

        binding.ivSetting.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click setting")
                onShowSettingDialog()
            }
        })
        binding.ivDashboardClose.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click dashboard close")
                binding.ivDashboardClose.visibility = View.INVISIBLE
                binding.flDashboard.visibility = View.INVISIBLE
                mDashboardFragment?.updateVisible(false)
            }
        })

        binding.ivHangup.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click hangup")
                if (mCallConnected) {
                    onBackPressed()
                } else {
                    onHangup()
                }
            }
        })
        binding.layoutCallPrivately.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click call privately")
                toggleSelfVideo(true) {
                    mShowTo1v1Manger.prepareCall(CallRole.CALLER, mRoomInfo.roomId, callback = {
                        mShowTo1v1Manger.mCallApi.addListener(callApiListener)
                        mShowTo1v1Manger.mCallApi.call(mRoomInfo.getIntUserId(), completion = {
                            if (it != null) {
                                mShowTo1v1Manger.mCallApi.removeListener(callApiListener)
                                mShowTo1v1Manger.deInitialize()
                            }
                        })
                    })
                }
                toggleSelfAudio(true) {}
            }
        })

        binding.includeConnectedView.root.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                Log.d(TAG, "click close connection view")
                mainHandler.removeCallbacks(connectedViewCloseRun)
                animateConnectedViewClose()
            }

        })
        binding.vDragSmallWindow.setOnViewClick {
            Log.d(TAG, "click switch video")
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
            Log.e(TAG, "getResources ${e.message}")
        }
        val drawable = ContextCompat.getDrawable(this, resourceId)
        Glide.with(this).load(drawable).into(binding.ivRoomCover)
        val fragment = DashboardFragment.newInstance(mRoomInfo, mShowTo1v1Manger.mConnectedChannelId)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        mDashboardFragment = fragment

        binding.flDashboard.visibility = View.VISIBLE
        binding.ivDashboardClose.visibility = View.VISIBLE
        mDashboardFragment?.updateVisible(true)
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
        val dialog = CallDetailSettingDialog(this@RoomDetailActivity)
        dialog.setListener(object : CallDetailSettingDialog.CallDetailSettingItemListener {
            override fun onClickDashboard() {
                binding.flDashboard.visibility = View.VISIBLE
                binding.ivDashboardClose.visibility = View.VISIBLE
                mDashboardFragment?.updateVisible(true)
            }
        })
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

    private val mainRtcListener = object : IRtcEngineEventHandler() {
        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            if (!isRoomOwner && uid == mRoomInfo.getIntUserId()) {
                runOnUiThread {
                    // 主播离线，退出房间
                    ToastUtils.showToast(R.string.show_to1v1_end_tips)
                    onBackPressed()
                }
            }
        }
    }

    private fun initRtcEngine() {
        mRtcEngine.addHandlerEx(mainRtcListener, mMainRtcConnection)
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

        }
    }

    private fun joinChannel() {
        val rtcConnection = mMainRtcConnection
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId

        if (!isRoomOwner && mRtcEngine.queryDeviceScore() < 75) {
            // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
            mRtcEngine.setParameters("\"rtc.video.decoder_out_byte_frame\": true")
        } else {
            // 主播加入频道前默认关闭硬解
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 0}")
        }

        mainChannelMediaOptions.clientRoleType =
            if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        mainChannelMediaOptions.autoSubscribeVideo = true
        mainChannelMediaOptions.autoSubscribeAudio = true
        mainChannelMediaOptions.publishCameraTrack = isRoomOwner
        mainChannelMediaOptions.publishMicrophoneTrack = isRoomOwner
        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
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
        mService.joinRoom(mRoomInfo, completion = { error ->
            if (error == null) { // success

            } else { //failed
                ToastUtils.showToast(getString(R.string.show_to1v1_end_tips))
                onBackPressed()
            }
        })
        mService.subscribeListener(object : ShowTo1v1ServiceListenerProtocol {
            override fun onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus) {

            }

            override fun onUserListDidChanged(userList: List<ShowTo1v1UserInfo>) {
                binding.tvNumCount.text = userList.size.number2K()
            }

            override fun onRoomDidDestroy(roomInfo: ShowTo1v1RoomInfo) {
                if (mRoomInfo.roomId == roomInfo.roomId) {
                    onBackPressed()
                }
            }

            override fun onRoomTimeUp() {
                ToastUtils.showToast(R.string.show_to1v1_end_tips)
                onBackPressed()
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
        // 取消 Glide 异步任务
        //Glide.with(this).pauseRequests()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (isGoingFinish) return
        isGoingFinish = true
        stopCallAnimator()
        Log.d(TAG, "RoomDetail onBackPressed")
        mRtcEngine.removeHandlerEx(mainRtcListener, mMainRtcConnection)
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
    }

    private fun destroy() {
        mService.leaveRoom(mRoomInfo, completion = {})
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

    // 连接打开动画
    private var openAnimator: Animator? = null

    private fun animateConnectedViewOpen() {
        if (binding.includeConnectedView.root.isVisible) return
        openAnimator?.cancel() // 停止之前的动画
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

        closeAnimator?.cancel() // 停止之前的动画

        closeAnimator = createCloseAnimator().apply {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
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
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            val ret = mRtcEngine.enableContentInspectEx(enable, contentInspectConfig, connection)
            Log.d(TAG, "enableContentInspectEx $ret")
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

        override fun onCallEventChanged(event: CallEvent) {
            super.onCallEventChanged(event)
            when (event) {
                CallEvent.LocalLeave -> {
                    onHangup()
                }

                CallEvent.RemoteLeave -> {
                    // 主叫方离线，挂断
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
            ShowTo1v1Logger.d(TAG, "onCallError: errorEvent$errorEvent, errorType:$errorType, errorCode:$errorCode, message:$message")
        }

        override fun onCallStateChanged(
            state: CallStateType,
            stateReason: CallStateReason,
            eventReason: String,
            eventInfo: Map<String, Any>
        ) {
            val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
            if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
            Log.d(TAG, "RoomDetail state:${state.name},stateReason:${stateReason.name},eventReason:${eventReason}")
            updateCallState(state)
            when (state) {
                CallStateType.Prepared -> {
                    when (stateReason) {
                        CallStateReason.RemoteHangup -> {
                            ToastUtils.showToast(R.string.show_to1v1_end_linking_tips)
                            if (mCallConnected && !isRoomOwner) {
                                onBackPressed()
                            }
                        }

                        CallStateReason.CallingTimeout,
                        CallStateReason.RemoteRejected -> {
                            if (!isRoomOwner) {
                                ToastUtils.showToast(getString(R.string.show_to1v1_no_answer))
                            }
                        }

                        else -> {}
                    }
                    finishCallDialog()
                    mShowTo1v1Manger.mRemoteUser = null
                    mShowTo1v1Manger.mConnectedChannelId = null
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
                    // 触发状态的用户是自己才处理
                    if (mShowTo1v1Manger.mCurrentUser.userId == toUserId.toString()) {
                        // 收到大哥拨打电话
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        val userMap = eventInfo[CallApiImpl.kFromUserExtension] as JSONObject
                        mShowTo1v1Manger.mRemoteUser = ShowTo1v1UserInfo(
                            userMap.getString("userId"),
                            userMap.getString("userName"),
                            userMap.getString("avatar"),
                            userMap.getString("objectId"),
                            userMap.getLong("createdAt")
                        )
                        mShowTo1v1Manger.mCallApi.accept(fromUserId) {}
                    } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                        // 大哥拨打电话
                        mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                        mShowTo1v1Manger.mRemoteUser = mRoomInfo
                        onCallSend(mShowTo1v1Manger.mRemoteUser!!)
                    }
                }

                CallStateType.Connecting -> {
                    if (stateReason == CallStateReason.LocalAccepted || stateReason == CallStateReason.RemoteAccepted) {
                        Log.d(TAG, "call Connecting LocalAccepted or RemoteAccepted")
                    }
                }

                CallStateType.Connected -> {
                    finishCallDialog()
                    // 开启鉴黄鉴暴
                    val channelId = mShowTo1v1Manger.mConnectedChannelId ?: ""
                    val localUid = mShowTo1v1Manger.mCurrentUser.userId.toInt()

                    // 视频鉴定
                    enableContentInspectEx(true, RtcConnection(channelId, localUid))
                    // 语音鉴定
                    AudioModeration.moderationAudio(
                        channelId, localUid.toLong(), AudioModeration.AgoraChannelType.broadcast,
                        "ShowTo1v1"
                    )
                }

                CallStateType.Failed -> {
                    finishCallDialog()
                    ToastUtils.showToast(eventReason)
                }
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

                publishMedia(true)
                setupVideoView(true)

                if (!isRoomOwner && !isGoingFinish) {
                    binding.layoutCallPrivatelyBg.isVisible = true
                    binding.layoutCallPrivately.isVisible = true
                    startCallAnimator()
                }
                binding.layoutNumCount.isVisible = true
                binding.groupHangup.isVisible = false
                binding.layoutCallingTop.isVisible = false
                binding.layoutRoomTop.isVisible = true
                binding.layoutCall.isVisible = false
                binding.textureVideo.isVisible = true
                if (exchanged) {
                    // 恢复默认窗口
                    exchangeDragWindow()
                }

                animateConnectedViewClose()
            }

            CallStateType.Connected -> {
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
                    // 左上角是大窗的房间和昵称
                    binding.tvCallingNickname.text = it.userName
                    binding.tvCallingUid.text = it.userId
                    GlideApp.with(this)
                        .load(it.avatar)
                        .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.ivCallingAvatar)
                }
                if (mShowTo1v1Manger.mRemoteUser == null) {
                    Log.d(TAG, "Connected but remoteUser is null")
                }
                mShowTo1v1Manger.mCurrentUser.let {
                    binding.vDragSmallWindow.setUserName(it.userName)
                }
                binding.textureVideo.isVisible = false
                binding.layoutCall.isVisible = true
                binding.layoutCallingTop.isVisible = true

                binding.layoutRoomTop.isVisible = false
                binding.layoutNumCount.isVisible = false
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
//                mainLooper.queue.addIdleHandler {
//                    Log.d(TAG, "animateConnectedViewOpen -- queueIdle -- 1")
//                    // workaround
//                    onShowSettingDialog(false)
//                    false
//                }
            }

            else -> {}
        }
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
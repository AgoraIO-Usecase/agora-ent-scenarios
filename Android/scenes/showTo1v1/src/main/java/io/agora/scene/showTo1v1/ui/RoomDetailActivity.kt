package io.agora.scene.showTo1v1.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.showTo1v1.callAPI.CallReason
import io.agora.scene.showTo1v1.callAPI.CallStateType
import io.agora.scene.showTo1v1.callAPI.ICallApi
import io.agora.scene.showTo1v1.callAPI.ICallApiListener
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.ShowTo1v1Logger
import io.agora.scene.showTo1v1.callAPI.CallApiImpl
import io.agora.scene.showTo1v1.callAPI.CallRole
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallDetailActivityBinding
import io.agora.scene.showTo1v1.service.ROOM_AVAILABLE_DURATION
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDetailSettingDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialogState
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.DashboardFragment
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherAPI
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.StatusBarUtil
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class RoomDetailActivity : BaseViewBindingActivity<ShowTo1v1CallDetailActivityBinding>(), ICallApiListener {

    companion object {
        private const val TAG = "RoomDetailActivity"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val EXTRA_ROOM_NEED_CALL = "needCall"

        fun launch(context: Context, needCall: Boolean, roomInfo: ShowTo1v1RoomInfo) {
            val intent = Intent(context, RoomDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_NEED_CALL, needCall)
                putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
            }
            context.startActivity(intent)
        }
    }

    private var dashboard: DashboardFragment? = null

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mCallApi by lazy { ICallApi.getImplInstance() }
    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mRtcVideoSwitcher by lazy { mShowTo1v1Manger.mVideoSwitcher }

    private val mRoomInfo: ShowTo1v1RoomInfo by lazy {
        (intent.getParcelableExtra(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!!
    }

    private val mNeedCall: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ROOM_NEED_CALL, false)
    }

    private val isRoomOwner by lazy { mRoomInfo.userId == UserManager.getInstance().user.id.toString() }

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }

    private var mCallDialog: CallDialog? = null

    // 当前呼叫状态
    private var mCallState = CallStateType.Idle

    private val timerRoomEndRun = Runnable {
        destroy() // 房间到了限制时间
        ToastUtils.showToast(R.string.show_to1v1_end_tips)
        ShowTo1v1Logger.d(TAG, "timer end!")
        onHangup()
    }

    private val timerLinkingEndRun = Runnable {
        destroy() // 拨打电话时间到了
        ToastUtils.showToast(R.string.show_to1v1_end_linking_tips)
        ShowTo1v1Logger.d(TAG, "timer linking end!")
        onHangup()
    }

    private val dataFormat by lazy {
        SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
    }

    private val timerRoomRun = object : Runnable {
        override fun run() {
            binding.tvTime.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt))
            binding.tvTime.postDelayed(this, 1000)
        }
    }

    private val mTextureView by lazy { TextureView(this) }
    override fun getViewBinding(inflater: LayoutInflater): ShowTo1v1CallDetailActivityBinding {
        return ShowTo1v1CallDetailActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setOnApplyWindowInsetsListener()
        StatusBarUtil.hideStatusBar(window, true)
        // 被呼叫需要重新初始化 callApi
        if (isRoomOwner) {
            reInitCallApi(CallRole.CALLEE)
        } else {
            mCallApi.addListener(this)
        }
        binding.tvTime.post(timerRoomRun)
    }


    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.layoutTop.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun init() {
        super.init()
        val roomLeftTime =
            ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong())
        if (roomLeftTime > 0) {
            binding.root.postDelayed(timerRoomEndRun, ROOM_AVAILABLE_DURATION)
            initRtcEngine()
            initServiceWithJoinRoom()
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        Glide.with(this)
            .load(mRoomInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvNickname.text = mRoomInfo.userName
        binding.tvRoomNum.text = mRoomInfo.roomId

        binding.ivClose.setOnClickListener {
            finish()
        }
        binding.ivSetting.setOnClickListener {
            val dialog = CallDetailSettingDialog(this)
            dialog.setListener(object : CallDetailSettingDialog.CallDetailSettingItemListener {
                override fun onClickDashboard() {
                    binding.flDashboard.visibility = View.VISIBLE
                    binding.ivClose.visibility = View.VISIBLE
                    dashboard?.updateVisible(true)
                }
            })
            dialog.show()
        }
        binding.ivDashboardClose.setOnClickListener {
            binding.ivClose.visibility = View.INVISIBLE
            binding.flDashboard.visibility = View.INVISIBLE
            dashboard?.updateVisible(false)
        }
        binding.ivDashboardClose.setOnClickListener {
            binding.flDashboard.isVisible = false
        }
        binding.layoutCallPrivately.setOnClickListener {
            reInitCallApi(CallRole.CALLER, callback = {
                mCallApi.call(mRoomInfo.get1v1ChannelId(), mRoomInfo.getIntUserId(), null)
            })
        }
        binding.vDragWindow.isVisible = false
        if (isRoomOwner) {
            binding.layoutCallPrivatelyBg.isVisible = false
            binding.layoutCallPrivately.isVisible = false
        } else {
            binding.layoutCallPrivatelyBg.isVisible = mNeedCall
            binding.layoutCallPrivately.isVisible = mNeedCall
            if (mNeedCall) {
                binding.layoutCallPrivatelyBg.breathAnim()
            }
        }
        val fragment = DashboardFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        dashboard = fragment
    }

    private fun reInitCallApi(role: CallRole, callback: (() -> Unit)? = null) {
        if (isRoomOwner) {
            mShowTo1v1Manger.mCallTokenConfig.rtcToken = ""
            mShowTo1v1Manger.mCallTokenConfig.rtmToken = ""
        }
        mShowTo1v1Manger.reInitCallApi(role, mRoomInfo.roomId, callback = {
            callback?.invoke()
            mCallApi.addListener(this)
        })
    }

    private fun onCallSend(user: ShowTo1v1UserInfo) {
        val dialog = CallSendDialog(this, user)
        dialog.setListener(object : CallSendDialog.CallSendDialogListener {
            override fun onSendViewDidClickHangup() {
                mCallApi.cancelCall(null)
            }
        })
        dialog.show()
        mCallDialog = dialog
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
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.root.removeCallbacks(timerRoomEndRun)
        binding.root.removeCallbacks(timerLinkingEndRun)
        binding.tvTime.removeCallbacks(timerRoomRun)
        mCallApi.removeListener(this)
    }

    private fun initRtcEngine() {
        val eventListener = VideoSwitcherAPI.IChannelEventListener(
            onChannelJoined = { connection ->

            },
        )
        toggleSelfVideo(true) {
            if (isRoomOwner) {
                ShowTo1v1Logger.d(TAG, "joinRoom from scroll")
                joinChannel(eventListener)
            } else {
                ShowTo1v1Logger.d(TAG, "joinRoom from click")
                mRtcVideoSwitcher.setChannelEvent(
                    mRoomInfo.roomId,
                    UserManager.getInstance().user.id.toInt(),
                    eventListener
                )
            }
            setupVideoView(true)
        }
        toggleSelfAudio(true) {

        }
    }

    private fun joinChannel(eventListener: VideoSwitcherAPI.IChannelEventListener) {
        val rtcConnection = mMainRtcConnection
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId

        AudioModeration.moderationAudio(
            channelName,
            uid,
            AudioModeration.AgoraChannelType.broadcast,
            "show"
        )

        if (!isRoomOwner && mRtcEngine.queryDeviceScore() < 75) {
            // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
            mRtcEngine.setParameters("\"rtc.video.decoder_out_byte_frame\": true")
        } else {
            // 主播加入频道前默认关闭硬解
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 0}")
        }

        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType =
            if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = true
        channelMediaOptions.publishCameraTrack = isRoomOwner
        channelMediaOptions.publishMicrophoneTrack = isRoomOwner
        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
        if (!isRoomOwner) {
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }
        mRtcVideoSwitcher.joinChannel(
            rtcConnection,
            channelMediaOptions,
            eventListener
        )
    }

    private fun setupVideoView(publish: Boolean) {
        if (isRoomOwner) {
            if (publish) {
                binding.llContainer.removeAllViews()
                mRtcEngine.setupLocalVideo(VideoCanvas(mTextureView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                mRtcEngine.startPreview()
                binding.llContainer.addView(mTextureView)
            } else {
                mRtcEngine.setupLocalVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                mRtcEngine.stopPreview()
                binding.llContainer.removeAllViews()
            }
        } else {
            if (publish) {
                binding.llContainer.removeAllViews()
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(
                        mTextureView, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.getIntUserId()
                    ), mMainRtcConnection
                )
                binding.llContainer.addView(mTextureView)
            } else {
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.getIntUserId()), mMainRtcConnection
                )
                binding.llContainer.removeAllViews()
            }
        }
    }

    //================== Service Operation ===============
    private fun initServiceWithJoinRoom() {
        mService.joinRoom(mRoomInfo, completion = { error ->
            if (error == null) { // success
                initService()
            } else { //failed

            }
        })
    }

    private fun initService() {

    }

    private fun destroy(): Boolean {
        mService.leaveRoom(mRoomInfo, completion = {})
        return mRtcVideoSwitcher.leaveChannel(mMainRtcConnection, true)
    }

    private fun publishMedia(publish: Boolean) {
        val options = ChannelMediaOptions()
        options.publishMicrophoneTrack = publish
        options.publishCameraTrack = publish
        options.autoSubscribeVideo = publish
        options.autoSubscribeAudio = publish
        mRtcEngine.updateChannelMediaOptionsEx(options, mMainRtcConnection)
    }

    private fun updateCallState(state: CallStateType) {
        mCallState = state
        when (mCallState) {
            CallStateType.Calling -> {
                publishMedia(false)
                setupVideoView(false)
                binding.vDragWindow.isVisible = true
            }

            CallStateType.Prepared,
            CallStateType.Idle,
            CallStateType.Failed -> {
                publishMedia(true)
                setupVideoView(true)
                binding.vDragWindow.isVisible = false
            }

            CallStateType.Connected -> {
                if (isRoomOwner) {
                    binding.llContainer.removeAllViews()
                    binding.llContainer.addView(mShowTo1v1Manger.mLocalVideoView)
                    binding.vDragWindow.canvasContainer.removeAllViews()
                    binding.vDragWindow.canvasContainer.addView(mShowTo1v1Manger.mRemoteVideoView)
                } else {
                    binding.llContainer.removeAllViews()
                    binding.llContainer.addView(mShowTo1v1Manger.mRemoteVideoView)
                    binding.vDragWindow.canvasContainer.removeAllViews()
                    binding.vDragWindow.canvasContainer.addView(mShowTo1v1Manger.mLocalVideoView)
                }
            }

            else -> {}
        }
    }

    override fun onCallStateChanged(
        state: CallStateType, stateReason: CallReason, eventReason: String, elapsed: Long, eventInfo: Map<String, Any>
    ) {
        val publisher = eventInfo[CallApiImpl.kPublisher] ?: mShowTo1v1Manger.mCurrentUser.userId
        if (publisher != mShowTo1v1Manger.mCurrentUser.userId) return
        updateCallState(state)
        when (state) {
            CallStateType.Prepared -> {
                when (stateReason) {
                    CallReason.RemoteHangup -> {
                        ToastUtils.showToast(getString(R.string.show_to1v1_end_linking_tips))
                    }

                    CallReason.CallingTimeout -> {
                        ToastUtils.showToast(getString(R.string.show_to1v1_no_answer))
                    }

                    else -> {}
                }
                mCallDialog?.let {
                    if (it.isShowing) it.dismiss()
                    mCallDialog = null
                }
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
                    mCallApi.reject(fromRoomId, fromUserId, "already calling") { err ->
                    }
                    return
                }
                // 触发状态的用户是自己才处理
                if (mShowTo1v1Manger.mCurrentUser.userId == toUserId.toString()) {
                    // 收到大哥拨打电话
                    mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                    // 大哥拨打电话
                    mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                    onCallSend(mShowTo1v1Manger.mCurrentUser)
                }
            }

            CallStateType.Connecting -> mCallDialog?.updateCallState(CallDialogState.Connecting)
            CallStateType.Connected -> {
                if (mShowTo1v1Manger.mRemoteUser == null) return
                mCallDialog?.let {
                    if (it.isShowing) it.dismiss()
                    mCallDialog = null
                }
                // 开启鉴黄鉴暴
                val channelId = mShowTo1v1Manger.mRemoteUser?.get1v1ChannelId() ?: ""
                val localUid = mShowTo1v1Manger.mCurrentUser.userId.toInt()
                enableContentInspectEx(RtcConnection(channelId, localUid))
                val channelName = mShowTo1v1Manger.mConnectedChannelId ?: return
                val uid = mShowTo1v1Manger.mCurrentUser.userId.toLong()
                AudioModeration.moderationAudio(
                    channelName, uid, AudioModeration.AgoraChannelType.broadcast,
                    "ShowTo1v1"
                )
            }

            CallStateType.Failed -> {
                mCallDialog?.let {
                    if (it.isShowing) it.dismiss()
                    mCallDialog = null
                }
                ToastUtils.showToast(eventReason)
            }
        }
    }

    private fun enableContentInspectEx(connection: RtcConnection) {
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "ShowTo1v1")
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectConfig.ContentInspectModule()
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            val ret = mRtcEngine.enableContentInspectEx(true, contentInspectConfig, connection)
            Log.d(TAG, "enableContentInspectEx $ret")
        } catch (_: JSONException) {
        }
    }

    private fun onHangup() {
        mCallApi.hangup(mRoomInfo.roomId, completion = {

        })
        finish()
    }
}

private fun View.breathAnim() {
    val scaleAnima = ScaleAnimation(
        0.9f, 1f, 0.8f, 1f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    scaleAnima.duration = 800
    scaleAnima.repeatCount = Animation.INFINITE
    scaleAnima.repeatMode = Animation.REVERSE
    this.startAnimation(scaleAnima)
}
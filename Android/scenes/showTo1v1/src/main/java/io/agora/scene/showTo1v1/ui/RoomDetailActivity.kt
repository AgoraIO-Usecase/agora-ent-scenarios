package io.agora.scene.showTo1v1.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.core.content.ContextCompat
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
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcher
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.callAPI.CallApiImpl
import io.agora.scene.showTo1v1.callAPI.CallRole
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallDetailActivityBinding
import io.agora.scene.showTo1v1.service.ROOM_AVAILABLE_DURATION
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceListenerProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceNetworkStatus
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.ui.dialog.CallDetailSettingDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialog
import io.agora.scene.showTo1v1.ui.dialog.CallDialogState
import io.agora.scene.showTo1v1.ui.dialog.CallSendDialog
import io.agora.scene.showTo1v1.ui.fragment.DashboardFragment
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
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

class RoomDetailActivity : BaseViewBindingActivity<ShowTo1v1CallDetailActivityBinding>(), ICallApiListener {

    companion object {
        private const val TAG = "ShowTo1v1_Detail"
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
    private val mCallApi by lazy { ICallApi.getImplInstance() }
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

    private var mCallDialog: CallDialog? = null

    // 当前呼叫状态
    private var mCallState = CallStateType.Idle

    private val timerRoomEndRun = Runnable {
        ToastUtils.showToast(R.string.show_to1v1_end_tips)
        Log.d(TAG, "timer end!")
        onBackPressed()
    }

    private val dataFormat by lazy {
        SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
    }

    private val timerRoomRun = object : Runnable {
        override fun run() {
            if (mCallState == CallStateType.Connected && mTimeLinkAt > 0) {
                binding.tvTime.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mTimeLinkAt))
            } else {
                binding.tvTime.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt))
            }
            binding.tvTime.postDelayed(this, 1000)
        }
    }

    private val connectedRun = Runnable { binding.includeConnectedView.root.isVisible = false }

    private var mTimeLinkAt: Long = 0

    private val mTextureView by lazy { TextureView(this) }
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
            binding.root.postDelayed(timerRoomEndRun, roomLeftTime)
            initRtcEngine()
            initServiceWithJoinRoom()

            // 被呼叫需要重新初始化 callApi
            if (isRoomOwner) {
                reInitCallApi(CallRole.CALLEE)
            } else if (mCallConnected) {
                mCallApi.addListener(this)
            } else {
                reInitCallApi(CallRole.CALLER)
            }
            binding.root.post(timerRoomRun)
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
        Glide.with(this)
            .load(mRoomInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvNickname.text = mRoomInfo.userName
        binding.tvRoomNum.text = mRoomInfo.roomId

        binding.ivClose.setOnClickListener {
            Log.d(TAG, "click close end!")
            onBackPressed()
        }
        binding.ivSetting.setOnClickListener {
            val dialog = CallDetailSettingDialog(this)
            dialog.setListener(object : CallDetailSettingDialog.CallDetailSettingItemListener {
                override fun onClickDashboard() {
                    binding.flDashboard.visibility = View.VISIBLE
                    binding.ivDashboardClose.visibility = View.VISIBLE
                    mDashboardFragment?.updateVisible(true)
                }
            })
            dialog.show()
        }
        binding.ivDashboardClose.setOnClickListener {
            binding.ivDashboardClose.visibility = View.INVISIBLE
            binding.flDashboard.visibility = View.INVISIBLE
            mDashboardFragment?.updateVisible(false)
        }
        binding.ivHangup.setOnClickListener {
            if (mCallConnected) {
                onBackPressed()
            } else {
                onHangup()
            }
        }
        binding.layoutCallPrivately.setOnClickListener {
            reInitCallApi(CallRole.CALLER, callback = {
                mCallApi.call(mRoomInfo.roomId, mRoomInfo.getIntUserId(), null)
            })
        }
        binding.vDragWindow.isVisible = false
        if (isRoomOwner) {
            binding.layoutCallPrivatelyBg.isVisible = false
            binding.layoutCallPrivately.isVisible = false
        } else {
            binding.layoutCallPrivatelyBg.isVisible = !mCallConnected
            binding.layoutCallPrivately.isVisible = !mCallConnected
            binding.includeComeSoonView.root.isVisible = true
            if (!mCallConnected) {
                binding.layoutCallPrivatelyBg.breathAnim()
            }
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
        val fragment = DashboardFragment.newInstance(mRoomInfo)
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        mDashboardFragment = fragment
    }

    private fun reInitCallApi(role: CallRole, callback: (() -> Unit)? = null) {
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
        binding.root.removeCallbacks(timerRoomRun)
        binding.root.removeCallbacks(connectedRun)
        mCallApi.removeListener(this)
    }

    private fun initRtcEngine() {

        val eventListener = VideoSwitcher.IChannelEventListener(
            onChannelJoined = {

            })
        toggleSelfVideo(true) {
            joinChannel(eventListener)
            if (mCallConnected) {
                updateCallState(CallStateType.Connected)
            } else {
                updateCallState(CallStateType.Idle)
            }
        }
        toggleSelfAudio(true) {

        }
    }

    private fun joinChannel(eventListener: VideoSwitcher.IChannelEventListener) {
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
        mShowTo1v1Manger.mVideoSwitcher.joinChannel(
            rtcConnection,
            channelMediaOptions,
            mShowTo1v1Manger.generalToken(),
            eventListener,
            false
        )
        if (isRoomOwner) {
            enableContentInspectEx(mMainRtcConnection)
            AudioModeration.moderationAudio(
                channelName, uid, AudioModeration.AgoraChannelType.broadcast, ContentInspectName
            )
        }
    }

    private fun setupVideoView(publish: Boolean) {
        (mTextureView.parent as? ViewGroup)?.removeView(mTextureView)
        if (isRoomOwner) {
            if (publish) {
                binding.llVideoContainer.isVisible = true
                mRtcEngine.setupLocalVideo(VideoCanvas(mTextureView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
                binding.llVideoContainer.addView(mTextureView)
                mRtcEngine.startPreview()
            } else {
                binding.llVideoContainer.isVisible = false
                mRtcEngine.setupLocalVideo(VideoCanvas(null, VideoCanvas.RENDER_MODE_HIDDEN, 0))
            }
        } else {
            if (publish) {
                binding.llVideoContainer.isVisible = true
                (mTextureView.parent as? ViewGroup)?.removeView(mTextureView)
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(mTextureView, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.getIntUserId()),
                    mMainRtcConnection
                )
                binding.llVideoContainer.addView(mTextureView)
            } else {
                binding.llVideoContainer.isVisible = false
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
                onBackPressed()
            }
        })
    }

    override fun onBackPressed() {
        if (isGoingFinish) return
        isGoingFinish = true
        destroy()
        onHangup()
        super.onBackPressed()
    }

    private fun destroy() {
        mService.leaveRoom(mRoomInfo, completion = {})
        if (isRoomOwner) {
            mShowTo1v1Manger.mVideoSwitcher.leaveChannel(mMainRtcConnection, true)
            mRtcEngine.stopPreview()
        } else {
            val options = ChannelMediaOptions()
            options.autoSubscribeVideo = true
            options.autoSubscribeAudio = false
            mRtcEngine.updateChannelMediaOptionsEx(options, mMainRtcConnection)
        }
    }

    private fun publishMedia(publish: Boolean) {
        val options = ChannelMediaOptions()
        options.publishMicrophoneTrack = isRoomOwner && publish
        options.publishCameraTrack = isRoomOwner && publish
        options.autoSubscribeVideo = publish
        options.autoSubscribeAudio = publish
        mRtcEngine.updateChannelMediaOptionsEx(options, mMainRtcConnection)
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

                binding.tvTime.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.show_to1v1_dot, 0, 0, 0
                )
                if (!isRoomOwner) {
                    binding.layoutCallPrivatelyBg.isVisible = true
                    binding.layoutCallPrivately.isVisible = true
                    binding.layoutCallPrivatelyBg.breathAnim()
                }
                binding.vDragWindow.isVisible = false
                binding.layoutNumCount.isVisible = true
                binding.ivHangup.isVisible = false
                binding.tvHangup.isVisible = false

                binding.llCallContainer.isVisible = false
            }

            CallStateType.Connected -> {


                mTimeLinkAt = System.currentTimeMillis()
                binding.layoutNumCount.isVisible = false
                binding.ivHangup.isVisible = true
                binding.tvHangup.isVisible = true
                binding.vDragWindow.isVisible = true

                binding.llCallContainer.isVisible = true
                if (isRoomOwner) {
                    (mShowTo1v1Manger.mLocalVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mLocalVideoView)
                    binding.llCallContainer.addView(mShowTo1v1Manger.mLocalVideoView)

                    (mShowTo1v1Manger.mRemoteVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mRemoteVideoView)
                    binding.vDragWindow.canvasContainer.addView(mShowTo1v1Manger.mRemoteVideoView)

                    binding.includeConnectedView.root.isVisible = true
                    mShowTo1v1Manger.mRemoteUser?.let {
                        GlideApp.with(this)
                            .load(it.avatar)
                            .error(R.mipmap.userimage)
                            .transform(CenterCropRoundCornerTransform(100))
                            .into(binding.includeConnectedView.ivUserAvatar)
                        binding.includeConnectedView.tvNickname.text = it.userName
                        binding.vDragWindow.setUserName(it.userName)
                    }
                    binding.root.postDelayed(connectedRun, 5000)
                } else {
                    (mShowTo1v1Manger.mRemoteVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mRemoteVideoView)
                    binding.llCallContainer.addView(mShowTo1v1Manger.mRemoteVideoView)

                    (mShowTo1v1Manger.mLocalVideoView.parent as? ViewGroup)?.removeView(mShowTo1v1Manger.mLocalVideoView)
                    binding.vDragWindow.canvasContainer.addView(mShowTo1v1Manger.mLocalVideoView)

                    binding.vDragWindow.setUserName(mShowTo1v1Manger.mCurrentUser.userName)
                    binding.layoutCallPrivatelyBg.isVisible = false
                    binding.layoutCallPrivately.isVisible = false
                    binding.layoutCallPrivatelyBg.clearAnimation()
                }
                binding.tvTime.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.show_to1v1_room_detail_connection, 0, 0, 0
                )
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
                    (eventInfo[CallApiImpl.kFromUserExtension] as? Map<String, Any>)?.let {
                        mShowTo1v1Manger.mRemoteUser = ShowTo1v1UserInfo(it)
                    }
                } else if (mShowTo1v1Manger.mCurrentUser.userId == fromUserId.toString()) {
                    // 大哥拨打电话
                    mShowTo1v1Manger.mConnectedChannelId = fromRoomId
                    mShowTo1v1Manger.mRemoteUser = mRoomInfo
                    onCallSend(mShowTo1v1Manger.mRemoteUser!!)
                }
            }

            CallStateType.Connecting -> mCallDialog?.updateCallState(CallDialogState.Connecting)
            CallStateType.Connected -> {
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
            jsonObject.put("sceneName", ContentInspectName)
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

    private var isGoingFinish = false
    private fun onHangup() {
        if (isRoomOwner) {
            mShowTo1v1Manger.mRemoteUser?.get1v1ChannelId()?.let {
                mCallApi.hangup(it, null)
            }
        } else {
            mCallApi.hangup(mRoomInfo.roomId, null)
        }
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
package io.agora.scene.showTo1v1.ui.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.GlideApp
import io.agora.scene.base.GlideOptions
import io.agora.scene.base.component.BaseBindingFragment
import io.agora.scene.base.manager.UserManager
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListFragmentBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceProtocol
import io.agora.scene.showTo1v1.ui.RoomListActivity
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcher
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherAPI
import io.agora.scene.widget.utils.BlurTransformation
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class RoomListFragment : BaseBindingFragment<ShowTo1v1RoomListFragmentBinding>() {

    companion object {

        private const val TAG = "ShowTo1v1_ListFragment"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun newInstance(romInfo: ShowTo1v1RoomInfo) = RoomListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, romInfo)
            }
        }
    }

    private val mService by lazy { ShowTo1v1ServiceProtocol.getImplInstance() }
    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mRtcVideoSwitcher by lazy { mShowTo1v1Manger.mVideoSwitcher }

    private val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!! }

    private var isPageLoaded = false

    private val mMainRtcConnection by lazy {
        RtcConnection(mRoomInfo.roomId, UserManager.getInstance().user.id.toInt())
    }

    private var onClickCallingListener: OnClickCallingListener? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): ShowTo1v1RoomListFragmentBinding {
        return ShowTo1v1RoomListFragmentBinding.inflate(inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onClickCallingListener = activity as? RoomListActivity
        if (isPageLoaded) {
            startLoadPage(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(enabled = isVisible) {
            onBackPressed()
        }
    }

    override fun initView() {
        super.initView()
        // setupRemoteVideo
        activity?.let {
            mRtcVideoSwitcher.setupRemoteVideo(
                mMainRtcConnection,
                VideoSwitcher.VideoCanvasContainer(it, binding.layoutVideoContainer, mRoomInfo.userId.toInt())
            )
        }
        binding.tvUserName.text = mRoomInfo.userName
        binding.tvRoomName.text = mRoomInfo.roomName
        context?.let { context ->
            var resourceId: Int
            try {
                resourceId = resources.getIdentifier(mRoomInfo.bgImage(), "drawable", context.packageName)
            } catch (e: Exception) {
                resourceId = R.drawable.show_to1v1_user_bg1
                Log.e(TAG, "getResources ${e.message}")
            }
            val drawable = ContextCompat.getDrawable(context, resourceId)
            Glide.with(this).load(drawable).into(binding.ivRoomCover)
            Glide.with(this)
                .load(drawable).apply(GlideOptions.bitmapTransform(BlurTransformation(context)))
                .into(binding.ivBackground)
            GlideApp.with(this)
                .load(mRoomInfo.avatar)
                .error(R.mipmap.userimage)
                .transform(CenterCropRoundCornerTransform(100))
                .into(binding.ivUserAvatar)
            Glide.with(this)
                .asGif()
                .load(R.drawable.show_to1v1_wave_living)
                .into(binding.ivLiving)
        }
        binding.ivConnect.setOnClickListener {
            onClickCallingListener?.onClickCall(true, mRoomInfo)
        }
        binding.layoutVideoContainer.setOnClickListener {
            onClickCallingListener?.onClickCall(false, mRoomInfo)
        }
        binding.ivConnectBG.breathAnim()
    }

    private fun onBackPressed() {
        activity?.finish()
    }

    fun startLoadPageSafely() {
        Log.d(TAG, "Fragment PageLoad startLoadPageSafely, roomId=${mRoomInfo.roomId}")
        isPageLoaded = true
        activity ?: return
        startLoadPage(true)
    }

    private fun startLoadPage(isScrolling: Boolean) {
        Log.d(TAG, "Fragment PageLoad start load, roomId=${mRoomInfo.roomId}")
        isPageLoaded = true

        initRtcEngine(isScrolling) {}
    }

    fun stopLoadPage(isScrolling: Boolean) {
        Log.d(TAG, "Fragment PageLoad stop load, roomId=${mRoomInfo.roomId}")
        isPageLoaded = false
        destroy(isScrolling) // 切页或activity销毁
    }

    private fun destroy(isScrolling: Boolean): Boolean {
        mService.leaveRoom(mRoomInfo, completion = {})
        return return mRtcVideoSwitcher.leaveChannel(mMainRtcConnection, !isScrolling)
    }

    //================== RTC Operation ===================

    private fun initRtcEngine(isScrolling: Boolean, onJoinChannelSuccess: () -> Unit) {
        val eventListener = VideoSwitcherAPI.IChannelEventListener(
            onChannelJoined = {
                onJoinChannelSuccess.invoke()
                // 静音
                val options = ChannelMediaOptions()
                options.autoSubscribeAudio = false
                mRtcEngine.updateChannelMediaOptionsEx(options, mMainRtcConnection)
            }
        )

        if (isScrolling) {
            Log.d(TAG, "joinRoom from scroll")
            joinChannel(eventListener)
        } else {
            Log.d(TAG, "joinRoom from click")
            mRtcVideoSwitcher.setChannelEvent(
                mRoomInfo.roomId, UserManager.getInstance().user.id.toInt(), eventListener
            )
        }
    }

    private fun joinChannel(eventListener: VideoSwitcherAPI.IChannelEventListener) {
        val rtcConnection = mMainRtcConnection
        if (mRtcEngine.queryDeviceScore() < 75) {
            // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
            mRtcEngine.setParameters("\"rtc.video.decoder_out_byte_frame\": true")
        } else {
            // 默认关闭硬解
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 0}")
        }

        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = false
        channelMediaOptions.publishCameraTrack = false
        channelMediaOptions.publishMicrophoneTrack = false
        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
        channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        mRtcVideoSwitcher.joinChannel(rtcConnection, channelMediaOptions, eventListener)
    }

    interface OnClickCallingListener {
        /**
         * 点击连接或者点击小窗进入直播页面
         * @param needCall true 需要直接 call; false 不需要 call
         * @param roomInfo 房间数据
         */
        fun onClickCall(needCall: Boolean, roomInfo: ShowTo1v1RoomInfo)
    }
}

private fun View.breathAnim() {
    val scaleAnima = ScaleAnimation(
        0.8f, 1f, 0.8f, 1f,
        Animation.RELATIVE_TO_SELF, 0.5f,
        Animation.RELATIVE_TO_SELF, 0.5f
    )
    scaleAnima.duration = 800
    scaleAnima.repeatCount = Animation.INFINITE
    scaleAnima.repeatMode = Animation.REVERSE
    this.startAnimation(scaleAnima)
}
package io.agora.scene.showTo1v1.ui.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcher
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.ShowTo1v1Manger
import io.agora.scene.showTo1v1.databinding.ShowTo1v1RoomListFragmentBinding
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.ui.RoomListActivity
import io.agora.scene.showTo1v1.ui.view.OnClickJackingListener
import io.agora.scene.widget.utils.BlurTransformation
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform

class RoomListFragment : BaseBindingFragment<ShowTo1v1RoomListFragmentBinding>() {

    companion object {

        const val TAG = "ShowTo1v1_List"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun newInstance(romInfo: ShowTo1v1RoomInfo) = RoomListFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, romInfo)
            }
        }
    }

    private val mShowTo1v1Manger by lazy { ShowTo1v1Manger.getImpl() }
    private val mRtcEngine by lazy { mShowTo1v1Manger.mRtcEngine }
    private val mRtcVideoSwitcher by lazy { mShowTo1v1Manger.mVideoSwitcher }

    private val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowTo1v1RoomInfo)!! }

    private var isPageLoaded = false

    private val mMainRtcConnection by lazy {
        RtcConnection(mRoomInfo.roomId, UserManager.getInstance().user.id.toInt())
    }

    private var onFragmentListener: OnFragmentListener? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): ShowTo1v1RoomListFragmentBinding {
        return ShowTo1v1RoomListFragmentBinding.inflate(inflater)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onFragmentListener = activity as? RoomListActivity
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
        onFragmentListener?.onFragmentViewCreated()
    }

    override fun initView() {
        super.initView()
        val currentUser = mShowTo1v1Manger.mCurrentUser
        binding.tvUserName.text = mRoomInfo.userName
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvCurrentName.text = currentUser.userName
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
            GlideApp.with(this)
                .load(currentUser.avatar)
                .error(R.mipmap.userimage)
                .transform(CenterCropRoundCornerTransform(100))
                .into(binding.ivCurrentAvatar)
            Glide.with(this)
                .asGif()
                .load(R.drawable.show_to1v1_wave_living)
                .into(binding.ivLiving)
        }
        binding.ivConnect.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                // 自己创建的房间不让呼叫
                if (mRoomInfo.userId == mShowTo1v1Manger.mCurrentUser.userId) return
                onFragmentListener?.onFragmentClickCall(true, mRoomInfo)
            }
        })
        binding.layoutVideoContainer.setOnClickListener(object : OnClickJackingListener() {
            override fun onClickJacking(view: View) {
                onFragmentListener?.onFragmentClickCall(false, mRoomInfo)
            }
        })
    }

    private var connectAnimatorSet: AnimatorSet? = null

    private fun startConnectAnimator() {
        if (connectAnimatorSet == null) {
            connectAnimatorSet = binding.ivConnectBG.breathAnim()
        }
        connectAnimatorSet?.cancel()
        connectAnimatorSet?.start()
    }

    private fun stopConnectAnimator(){
        connectAnimatorSet?.cancel()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "fragment onResume ${mRoomInfo.roomId}")
        startConnectAnimator()
    }

    override fun onPause() {
        stopConnectAnimator()
        Log.d(TAG, "fragment onPause ${mRoomInfo.roomId}")
        super.onPause()
    }

    private fun onBackPressed() {
        activity?.finish()
    }

    fun startLoadPageSafely() {
        isPageLoaded = true
        activity ?: return
        startLoadPage(true)
    }

    fun onReloadPage() {
        Log.d(TAG, "onReloadPage, roomId=${mRoomInfo.roomId}")
//        startLoadPage(false)
    }

    fun onResumePage() {
        activity?.let {
            mRtcVideoSwitcher.setupRemoteVideo(
                mMainRtcConnection,
                VideoSwitcher.VideoCanvasContainer(it, binding.layoutVideoContainer, mRoomInfo.userId.toInt())
            )
        }
    }

    private fun startLoadPage(isScrolling: Boolean) {
        isPageLoaded = true

        initRtcEngine(isScrolling)

        activity?.let {
            mRtcVideoSwitcher.setupRemoteVideo(
                mMainRtcConnection,
                VideoSwitcher.VideoCanvasContainer(it, binding.layoutVideoContainer, mRoomInfo.userId.toInt())
            )
        }
    }


    fun stopLoadPage(isScrolling: Boolean) {
        Log.d(TAG, "Fragment PageLoad stop load, roomId=${mRoomInfo.roomId}")
        isPageLoaded = false
        destroy(isScrolling) // 切页或activity销毁
    }

    private fun destroy(isScrolling: Boolean): Boolean {
        return return mRtcVideoSwitcher.leaveChannel(mMainRtcConnection, !isScrolling)
    }

    //================== RTC Operation ===================

    private val eventListener = VideoSwitcher.IChannelEventListener(
        onChannelJoined = {
            // 静音
            val options = ChannelMediaOptions()
            options.autoSubscribeAudio = false
            mRtcEngine.updateChannelMediaOptionsEx(options, mMainRtcConnection)
        }
    )

    private fun initRtcEngine(isScrolling: Boolean) {
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

    private fun joinChannel(eventListener: VideoSwitcher.IChannelEventListener) {
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
        mRtcVideoSwitcher.joinChannel(
            rtcConnection, channelMediaOptions, mShowTo1v1Manger.generalToken(),
            eventListener, true
        )
    }

    interface OnFragmentListener {
        /**
         * 点击连接或者点击小窗进入直播页面
         * @param needCall true 需要直接 call; false 不需要 call
         * @param roomInfo 房间数据
         */
        fun onFragmentClickCall(needCall: Boolean, roomInfo: ShowTo1v1RoomInfo)

        /**
         * fragment onViewCreated 回调
         */
        fun onFragmentViewCreated()
    }
}

private fun View.breathAnim(): AnimatorSet {
    val scaleXAnima = ObjectAnimator.ofFloat(this, "scaleX", 0.8f, 1f)
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
    }
    return animatorSet
}
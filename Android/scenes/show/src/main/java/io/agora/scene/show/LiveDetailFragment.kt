package io.agora.scene.show

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.os.SystemClock
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.data.MediaPlayerSource
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
import io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
import io.agora.rtc2.video.ContentInspectConfig.ContentInspectModule
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.audio.AudioScenarioApi
import io.agora.scene.show.audio.AudioScenarioType
import io.agora.scene.show.audio.SceneType
import io.agora.scene.show.beauty.BeautyManager
import io.agora.scene.show.databinding.ShowLiveDetailFragmentBinding
import io.agora.scene.show.databinding.ShowLiveDetailMessageItemBinding
import io.agora.scene.show.databinding.ShowLivingEndDialogBinding
import io.agora.scene.show.debugSettings.DebugAudienceSettingDialog
import io.agora.scene.show.debugSettings.DebugSettingDialog
import io.agora.scene.show.service.ShowInteractionInfo
import io.agora.scene.show.service.ShowInteractionStatus
import io.agora.scene.show.service.ShowInvitationType
import io.agora.scene.show.service.ShowMessage
import io.agora.scene.show.service.ShowMicSeatApply
import io.agora.scene.show.service.ShowMicSeatInvitation
import io.agora.scene.show.service.ShowPKInvitation
import io.agora.scene.show.service.ShowRoomDetailModel
import io.agora.scene.show.service.ShowServiceProtocol
import io.agora.scene.show.service.ShowSubscribeStatus
import io.agora.scene.show.service.ShowUser
import io.agora.scene.show.service.isRobotRoom
import io.agora.scene.show.widget.AdvanceSettingAudienceDialog
import io.agora.scene.show.widget.AdvanceSettingDialog
import io.agora.scene.show.widget.MusicEffectDialog
import io.agora.scene.show.widget.PictureQualityDialog
import io.agora.scene.show.widget.SettingDialog
import io.agora.scene.show.widget.TextInputDialog
import io.agora.scene.show.widget.beauty.MultiBeautyDialog
import io.agora.scene.show.widget.link.LiveLinkAudienceSettingsDialog
import io.agora.scene.show.widget.link.LiveLinkDialog
import io.agora.scene.show.widget.link.OnLinkDialogActionListener
import io.agora.scene.show.widget.pk.LivePKDialog
import io.agora.scene.show.widget.pk.LivePKSettingsDialog
import io.agora.scene.show.widget.pk.LiveRoomConfig
import io.agora.scene.show.widget.pk.OnPKDialogActionListener
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.videoloaderapi.OnPageScrollEventHandler
import io.agora.videoloaderapi.VideoLoader
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/*
 * 直播间内页面
 */
class LiveDetailFragment : Fragment() {
    private val TAG = this.toString()

    companion object {

        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val EXTRA_CREATE_ROOM = "createRoom"

        fun newInstance(
            roomDetail: ShowRoomDetailModel,
            handler: OnPageScrollEventHandler,
            position: Int,
            createRoom: Boolean
        ) = LiveDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, roomDetail)
                putBoolean(EXTRA_CREATE_ROOM, createRoom)
            }
            mHandler = handler
            mPosition = position
        }
    }

    val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowRoomDetailModel)!! }
    private lateinit var mHandler: OnPageScrollEventHandler
    private var mPosition: Int = 0
    private val mBinding by lazy {
        ShowLiveDetailFragmentBinding.inflate(
            LayoutInflater.from(
                requireContext()
            )
        )
    }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val isRoomOwner by lazy { mRoomInfo.ownerId == UserManager.getInstance().user.id.toString() }

    private var mMessageAdapter: BindingSingleAdapter<ShowMessage, ShowLiveDetailMessageItemBinding>? =
        null
    private val mMusicEffectDialog by lazy { MusicEffectDialog(requireContext()) }
    private val mSettingDialog by lazy { SettingDialog(requireContext()) }
    private val mLinkSettingDialog by lazy { LiveLinkAudienceSettingsDialog(requireContext()) }
    private val mPKSettingsDialog by lazy { LivePKSettingsDialog(requireContext()) }
    private val mLinkDialog by lazy { LiveLinkDialog() }
    private val mPKDialog by lazy { LivePKDialog() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }
    private val mRtcVideoLoaderApi by lazy { VideoLoader.getImplInstance(mRtcEngine) }
    private fun showDebugModeDialog() = DebugSettingDialog(requireContext()).show()
    private fun showAudienceDebugModeDialog() = DebugAudienceSettingDialog(requireContext()).show()

    // 当前互动状态
    private var interactionInfo: ShowInteractionInfo? = null
    private var isPKCompetition: Boolean = false

    private var mLinkInvitationCountDownLatch: CountDownTimer? = null
    private var mPKInvitationCountDownLatch: CountDownTimer? = null
    private var mPKCountDownLatch: CountDownTimer? = null

    private var isAudioOnlyMode = false
    private var isPageLoaded = false

    private var localVideoCanvas: LocalVideoCanvasWrap? = null

    private val scenarioApi by lazy { AudioScenarioApi(mRtcEngine) }

    private val timerRoomEndRun = Runnable {
        destroy(false) // 房间到了限制时间
        showLivingEndLayout() // 房间到了限制时间
        ShowLogger.d("showLivingEndLayout", "timer end!")
    }

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }

    private var mMicInvitationDialog: AlertDialog?= null
    private var mPKInvitationDialog: AlertDialog?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ShowLogger.d(TAG, "Fragment Lifecycle: onCreateView")
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ShowLogger.d(TAG, "Fragment Lifecycle: onViewCreated")
        initView()
        activity?.onBackPressedDispatcher?.addCallback(enabled = isVisible) {
            onBackPressed()
        }
        // 需求：打开直播显示
        changeStatisticVisible(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ShowLogger.d(TAG, "Fragment Lifecycle: onAttach")
        onMeLinkingListener = (activity as? LiveDetailActivity)
        if (isPageLoaded) {
            startLoadPage()
        }
    }

    override fun onDetach() {
        super.onDetach()
        ShowLogger.d(TAG, "Fragment Lifecycle: onDetach")
    }

    private fun runOnUiThread(run: Runnable) {
        val activity = activity ?: return
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            run.run()
        } else {
            activity.runOnUiThread(run)
        }
    }

    fun startLoadPageSafely() {
        isPageLoaded = true
        activity ?: return
        startLoadPage()
    }

    fun onPageLoaded() {
        updatePKingMode()
    }

    private fun startLoadPage() {
        ShowLogger.d(TAG, "Fragment PageLoad start load, roomId=${mRoomInfo.roomId}")
        isPageLoaded = true
        subscribeMediaTime = SystemClock.elapsedRealtime()
        if (mRoomInfo.isRobotRoom()) {
            initRtcEngine()
            initServiceWithJoinRoom()
        } else {
            val roomLeftTime =
                ShowServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong())
            if (roomLeftTime > 0) {
                mBinding.root.postDelayed(
                    timerRoomEndRun,
                    ShowServiceProtocol.ROOM_AVAILABLE_DURATION
                )
                initRtcEngine()
                initServiceWithJoinRoom()
            }
        }

        startTopLayoutTimer()
    }

    fun stopLoadPage(isScrolling: Boolean) {
        ShowLogger.d(TAG, "Fragment PageLoad stop load, roomId=${mRoomInfo.roomId}")
        isPageLoaded = false
        destroy(isScrolling) // 切页或activity销毁
    }

    private fun destroy(isScrolling: Boolean): Boolean {
        mBinding.root.removeCallbacks(timerRoomEndRun)
        releaseCountdown()
        destroyService()
        return destroyRtcEngine(isScrolling)
    }

    private fun onBackPressed() {
        if (isRoomOwner) {
            showEndRoomDialog()
        } else {
            stopLoadPage(false)
            activity?.finish()
        }
    }

    //================== UI Operation ===============

    private fun initView() {
        initLivingEndLayout()
        initTopLayout()
        initBottomLayout()
        initMessageLayout()

        // Render host video
        if (needRender) {
            mRtcVideoLoaderApi.renderVideo(
                VideoLoader.AnchorInfo(
                    mRoomInfo.roomId,
                    mRoomInfo.ownerId.toInt(),
                    RtcEngineInstance.generalToken()
                ),
                UserManager.getInstance().user.id.toInt(),
                VideoLoader.VideoCanvasContainer(
                    viewLifecycleOwner,
                    mBinding.videoLinkingLayout.videoContainer,
                    mRoomInfo.ownerId.toInt()
                )
            )
        }
    }

    private fun initVideoView() {
        activity?.let {
            if (isRoomOwner) {
                setupLocalVideo(
                    VideoLoader.VideoCanvasContainer(
                        it,
                        mBinding.videoLinkingLayout.videoContainer,
                        0
                    )
                )
            }
        }
    }

    private var needRender = false
    fun initAnchorVideoView(info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer? {
        // 判断是否此时view还没有创建，即在View创建后第一时间渲染视频
        needRender = activity == null
        activity?.let {
            if (interactionInfo != null && interactionInfo!!.interactStatus == ShowInteractionStatus.pking) {
                if (info.channelId == mRoomInfo.roomId) {
                    return VideoLoader.VideoCanvasContainer(
                        viewLifecycleOwner,
                        mBinding.videoPKLayout.iBroadcasterAView,
                        mRoomInfo.ownerId.toInt()
                    )
                } else if (info.channelId == interactionInfo!!.roomId) {
                    return VideoLoader.VideoCanvasContainer(
                        viewLifecycleOwner,
                        mBinding.videoPKLayout.iBroadcasterBView,
                        interactionInfo!!.userId.toInt()
                    )
                }
            } else {
                return VideoLoader.VideoCanvasContainer(
                    viewLifecycleOwner,
                    mBinding.videoLinkingLayout.videoContainer,
                    mRoomInfo.ownerId.toInt()
                )
            }
        }
        return null
    }

    private fun initAudioModeration() {
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId

        AudioModeration.moderationAudio(
            channelName,
            uid,
            AudioModeration.AgoraChannelType.broadcast,
            "show"
        )
    }

    private fun initLivingEndLayout() {
        val livingEndLayout = mBinding.livingEndLayout
        livingEndLayout.root.isVisible =
            ShowServiceProtocol.ROOM_AVAILABLE_DURATION < (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong()) && !isRoomOwner && !mRoomInfo.isRobotRoom()
        livingEndLayout.tvUserName.text = mRoomInfo.ownerName
        Glide.with(this@LiveDetailFragment)
            .load(mRoomInfo.ownerAvatar)
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .into(livingEndLayout.ivAvatar)
        livingEndLayout.ivClose.setOnClickListener {
            stopLoadPage(false)
            activity?.finish()
        }
    }

    private fun initTopLayout() {
        val topLayout = mBinding.topLayout
        Glide.with(this)
            .load(mRoomInfo.ownerAvatar)
            .error(R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(topLayout.ivOwnerAvatar)
        topLayout.tvRoomName.text = mRoomInfo.roomName
        topLayout.tvRoomId.text = getString(R.string.show_room_id, mRoomInfo.roomId)
        topLayout.tvUserCount.text = mRoomInfo.roomUserCount.toString()
        topLayout.ivClose.setOnClickListener { onBackPressed() }
        topLayout.ivMore.setOnClickListener {
            context?.let {
                TopFunctionDialog(it).show()
            }
        }
    }

    private fun startTopLayoutTimer() {
        val topLayout = mBinding.topLayout
        val dataFormat =
            SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
        Log.d(
            TAG,
            "TopTimer curr=${TimeUtils.currentTimeMillis()}, createAt=${mRoomInfo.createdAt.toLong()}, diff=${TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong()}, time=${
                dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong()))
            }"
        )
        topLayout.tvTimer.post(object : Runnable {
            override fun run() {
                topLayout.tvTimer.text =
                    dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong()))
                topLayout.tvTimer.postDelayed(this, 1000)
                topLayout.tvTimer.tag = this
            }
        })
    }

    private fun initBottomLayout() {
        val bottomLayout = mBinding.bottomLayout
        bottomLayout.layoutChat.setOnClickListener {
            showMessageInputDialog()
        }
        bottomLayout.ivSetting.setOnClickListener {
            showSettingDialog()
        }
        bottomLayout.ivBeauty.setOnClickListener {
            showBeautyDialog()
        }
        bottomLayout.ivMusic.setOnClickListener {
            showMusicEffectDialog()
        }
        bottomLayout.ivLinking.setOnClickListener {
            // 如果是机器人
            if (mRoomInfo.isRobotRoom()) {
                ToastUtils.showToast(context?.getString(R.string.show_tip1))
                return@setOnClickListener
            }
            bottomLayout.vLinkingDot.isVisible = false
            if (!isRoomOwner) {
                // 观众发送连麦申请
                if (!(interactionInfo != null && interactionInfo!!.userId == UserManager.getInstance().user.id.toString())) {
                    // 观众发视频流
                    prepareLinkingMode()
                    mService.createMicSeatApply(mRoomInfo.roomId, {
                        // success
                        mLinkDialog.setOnApplySuccess(it)
                    })
                }
            }
            showLinkingDialog()
        }
        bottomLayout.flPK.setOnClickListener {
            bottomLayout.vPKDot.isVisible = false
            if (isRoomOwner) {
                showPKDialog()
            }
        }
        refreshBottomLayout()
    }

    private fun initMessageLayout() {
        val messageLayout = mBinding.messageLayout
        mMessageAdapter =
            object : BindingSingleAdapter<ShowMessage, ShowLiveDetailMessageItemBinding>() {
                override fun onBindViewHolder(
                    holder: BindingViewHolder<ShowLiveDetailMessageItemBinding>, position: Int
                ) {
                    val item = getItem(position) ?: return
                    holder.binding.text.text = SpannableStringBuilder().append(
                        "${item.userName}: ",
                        ForegroundColorSpan(Color.parseColor("#A6C4FF")),
                        SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
                    ).append(
                        item.message,
                        ForegroundColorSpan(Color.WHITE),
                        SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
                    )
                }
            }
        (messageLayout.rvMessage.layoutManager as LinearLayoutManager).let {
            it.stackFromEnd = true
        }
        messageLayout.rvMessage.adapter = mMessageAdapter
    }

    private fun refreshBottomLayout() {
        val context = context ?: return
        val bottomLayout = mBinding.bottomLayout
        if (isRoomOwner) {
            // 房主

            // 房主都能控制视频
            bottomLayout.ivSetting.isVisible = true
            bottomLayout.ivMusic.isVisible = true
            bottomLayout.ivBeauty.isVisible = true

            if (isPKing()) {
                // PK状态
                // 房主一定是PK的一方
                bottomLayout.ivLinking.isEnabled = false
                bottomLayout.flPK.isEnabled = true
                bottomLayout.flPK.isVisible = true
            } else if (isLinking()) {
                // 连麦状态
                // 房主一定是连麦的一方
                bottomLayout.flPK.isEnabled = false
                bottomLayout.flLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList = null
                mSettingDialog.apply {
                    resetSettingsItem(false)
                }
            } else {
                // 单主播状态
                // 房主是主播
                bottomLayout.flPK.isEnabled = true
                bottomLayout.ivLinking.isEnabled = true
                bottomLayout.flPK.isVisible = true
                bottomLayout.flLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList =
                    ColorStateList.valueOf(context.resources.getColor(R.color.grey_7e))
                mSettingDialog.apply {
                    resetSettingsItem(false)
                }
            }

        } else {
            // 观众

            bottomLayout.ivSetting.isVisible = true
            // 观众没有PK权限
            bottomLayout.flPK.isVisible = false


            if (isPKing()) {
                // PK状态
                // PK是房主和房主的事，和观众无关，观众只能看，同时无法再连麦
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false
                bottomLayout.flLinking.isVisible = false
            } else if (isLinking()) {
                // 连麦状态
                if (isMeLinking()) {
                    // 连麦中的一方
                    bottomLayout.ivMusic.isVisible = false
                    bottomLayout.ivBeauty.isVisible = false

                    bottomLayout.flLinking.isVisible = true
                    bottomLayout.ivLinking.imageTintList = null
                } else {
                    // 只是观看者，不参与连麦
                    bottomLayout.ivMusic.isVisible = false
                    bottomLayout.ivBeauty.isVisible = false
                    bottomLayout.flLinking.isVisible = false
                }
            } else {
                // 单主播状态
                // 普通观众，只有发起连麦申请的按钮
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false

                bottomLayout.flLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList =
                    ColorStateList.valueOf(context.resources.getColor(R.color.grey_7e))
            }
        }
    }

    private fun showMessageInputDialog() {
        TextInputDialog(requireContext())
            .setMaxInput(80)
            .setOnInsertHeightChangeListener {
                mBinding.messageLayout.root.layoutParams =
                    (mBinding.messageLayout.root.layoutParams as MarginLayoutParams).apply {
                        bottomMargin = it
                    }
            }
            .setOnSentClickListener { dialog, msg ->
                mService.sendChatMessage(mRoomInfo.roomId, msg)
                dialog.dismiss()
            }
            .show()
    }

    private fun refreshTopUserCount(count: Int) =
        runOnUiThread { mBinding.topLayout.tvUserCount.text = count.toString() }

    private fun changeStatisticVisible() {
        val visible = !mBinding.topLayout.tlStatistic.isVisible
        changeStatisticVisible(visible)
    }

    private fun changeStatisticVisible(visible: Boolean) {
        val topBinding = mBinding.topLayout
        topBinding.tlStatistic.isVisible = visible
        topBinding.ivStatisticClose.isVisible = visible
        refreshStatisticInfo(0, 0)
        topBinding.ivStatisticClose.setOnClickListener {
            topBinding.tlStatistic.isVisible = false
            topBinding.ivStatisticClose.isVisible = false
        }
    }

    private fun refreshStatisticInfo(
        upLinkBps: Int? = null, downLinkBps: Int? = null,
        audioBitrate: Int? = null, audioLossPackage: Int? = null,
        cpuAppUsage: Double? = null, cpuTotalUsage: Double? = null,
        // 编码分辨率、接收分辨率
        encodeVideoSize: Size? = null, receiveVideoSize: Size? = null,
        // 编码帧率、接收帧率
        encodeFps: Int? = null, receiveFPS: Int? = null,
        // 下行延迟
        downDelay: Int? = null,
        // 上行丢包率、下行丢包率
        upLossPackage: Int? = null, downLossPackage: Int? = null,
        // 上行码率、下行码率
        upBitrate: Int? = null, downBitrate: Int? = null,
        codecType: Int? = null
    ) {
        activity ?: return
        val topBinding = mBinding.topLayout
        val statisticBinding = topBinding.tlStatistic
        val visible = statisticBinding.isVisible
        if (!visible) {
            return
        }
        // 编码分辨率
        encodeVideoSize?.let {
            topBinding.tvEncodeResolution.text =
                getString(R.string.show_statistic_encode_resolution, "${it.height}x${it.width}")
        }
        if (topBinding.tvEncodeResolution.text.isEmpty()) topBinding.tvEncodeResolution.text =
            getString(R.string.show_statistic_encode_resolution, "--")
        // 接收分辨率
        receiveVideoSize?.let {
            topBinding.tvReceiveResolution.text =
                getString(R.string.show_statistic_receive_resolution, "${it.height}x${it.width}")
        }
        if (topBinding.tvReceiveResolution.text.isEmpty()) topBinding.tvReceiveResolution.text =
            getString(R.string.show_statistic_receive_resolution, "--")
        // 编码帧率
        encodeFps?.let {
            topBinding.tvStatisticEncodeFPS.text =
                getString(R.string.show_statistic_encode_fps, it.toString())
        }
        if (topBinding.tvStatisticEncodeFPS.text.isEmpty()) topBinding.tvStatisticEncodeFPS.text =
            getString(R.string.show_statistic_encode_fps, "--")
        // 接收帧率
        receiveFPS?.let {
            topBinding.tvStatisticReceiveFPS.text =
                getString(R.string.show_statistic_receive_fps, it.toString())
        }
        if (topBinding.tvStatisticReceiveFPS.text.isEmpty()) topBinding.tvStatisticReceiveFPS.text =
            getString(R.string.show_statistic_receive_fps, "--")
        // 下行延迟
        downDelay?.let {
            topBinding.tvStatisticDownDelay.text =
                getString(R.string.show_statistic_delay, it.toString())
        }
        if (topBinding.tvStatisticDownDelay.text.isEmpty()) topBinding.tvStatisticDownDelay.text =
            getString(R.string.show_statistic_delay, "--")
        // 上行丢包率
        upLossPackage?.let {
            topBinding.tvStatisticUpLossPackage.text =
                getString(R.string.show_statistic_up_loss_package, it.toString())
        }
        if (topBinding.tvStatisticUpLossPackage.text.isEmpty()) topBinding.tvStatisticUpLossPackage.text =
            getString(R.string.show_statistic_up_loss_package, "--")
        // 下行丢包率
        downLossPackage?.let {
            topBinding.tvStatisticDownLossPackage.text =
                getString(R.string.show_statistic_down_loss_package, it.toString())
        }
        if (topBinding.tvStatisticDownLossPackage.text.isEmpty()) topBinding.tvStatisticDownLossPackage.text =
            getString(R.string.show_statistic_down_loss_package, "--")
        // 上行码率
        upBitrate?.let {
            topBinding.tvStatisticUpBitrate.text =
                getString(R.string.show_statistic_up_bitrate, it.toString())
        }
        if (topBinding.tvStatisticUpBitrate.text.isEmpty()) topBinding.tvStatisticUpBitrate.text =
            getString(R.string.show_statistic_up_bitrate, "--")
        // 下行码率
        downBitrate?.let {
            topBinding.tvStatisticDownBitrate.text =
                getString(R.string.show_statistic_down_bitrate, it.toString())
        }
        if (topBinding.tvStatisticDownBitrate.text.isEmpty()) topBinding.tvStatisticDownBitrate.text =
            getString(R.string.show_statistic_down_bitrate, "--")
        // 上行网络
        topBinding.tvStatisticUpNet.isVisible = !isAudioOnlyMode
        upLinkBps?.let {
            topBinding.tvStatisticUpNet.text =
                getString(R.string.show_statistic_up_net_speech, (it / 8192).toString())
        }
        if (topBinding.tvStatisticUpNet.text.isEmpty()) topBinding.tvStatisticUpNet.text =
            getString(R.string.show_statistic_up_net_speech, "--")
        // 下行网络
        topBinding.tvStatisticDownNet.isVisible = !isAudioOnlyMode
        downLinkBps?.let {
            topBinding.tvStatisticDownNet.text =
                getString(R.string.show_statistic_down_net_speech, (it / 8192).toString())
        }
        if (topBinding.tvStatisticDownNet.text.isEmpty()) topBinding.tvStatisticDownNet.text =
            getString(R.string.show_statistic_down_net_speech, "--")
        // 秒开时间
        topBinding.tvQuickStartTime.isVisible = true
        if (isRoomOwner) {
            topBinding.tvQuickStartTime.text =
                getString(R.string.show_statistic_quick_start_time, "--")
        } else {
            // TODO
            topBinding.tvQuickStartTime.text =
                getString(R.string.show_statistic_quick_start_time, quickStartTime.toString())
        }
        // 机型等级
        topBinding.tvStatisticDeviceGrade.isVisible = true
        val score = mRtcEngine.queryDeviceScore()
        if (score >= 90) {
            topBinding.tvStatisticDeviceGrade.text = getString(
                R.string.show_device_grade,
                getString(R.string.show_setting_preset_device_high)
            ) + "（$score）"
        } else if (score >= 75) {
            topBinding.tvStatisticDeviceGrade.text = getString(
                R.string.show_device_grade,
                getString(R.string.show_setting_preset_device_medium)
            ) + "（$score）"
        } else {
            topBinding.tvStatisticDeviceGrade.text = getString(
                R.string.show_device_grade,
                getString(R.string.show_setting_preset_device_low)
            ) + "（$score）"
        }
        // H265开关
        topBinding.tvStatisticH265.isVisible = true
        if (isRoomOwner) {
            codecType?.let {
                topBinding.tvStatisticH265.text = getString(
                    R.string.show_statistic_h265,
                    if (it == 3) getString(R.string.show_setting_opened) else getString(R.string.show_setting_closed)
                )
            }
        } else {
            topBinding.tvStatisticH265.text = getString(R.string.show_statistic_h265, "--")
        }
        // 超分开关
        topBinding.tvStatisticSR.isVisible = true
        if (isRoomOwner) {
            topBinding.tvStatisticSR.text = getString(R.string.show_statistic_sr, "--")
        } else {
            topBinding.tvStatisticSR.text = getString(
                R.string.show_statistic_sr,
                if (VideoSetting.getCurrAudienceEnhanceSwitch()) getString(R.string.show_setting_opened) else getString(
                    R.string.show_setting_closed
                )
            )
        }
        // pvc开关
        topBinding.tvStatisticPVC.isVisible = true
        if (isRoomOwner) {
            topBinding.tvStatisticPVC.text = getString(
                R.string.show_statistic_pvc,
                if (VideoSetting.getCurrBroadcastSetting().video.PVC) getString(R.string.show_setting_opened) else getString(
                    R.string.show_setting_closed
                )
            )
        } else {
            topBinding.tvStatisticPVC.text = getString(R.string.show_statistic_pvc, "--")
        }

        // 小流开关
        topBinding.tvStatisticLowStream.isVisible = true
        if (isRoomOwner) {
            topBinding.tvStatisticLowStream.text = getString(
                R.string.show_statistic_low_stream,
                if (VideoSetting.getCurrLowStreamSetting() == null) getString(R.string.show_setting_closed) else getString(
                    R.string.show_setting_opened
                )
            )
        } else {
            topBinding.tvStatisticLowStream.text =
                getString(R.string.show_statistic_low_stream, "--")
        }

        // svc开关
        topBinding.tvStatisticSVC.isVisible = true
        if (isRoomOwner) {
            topBinding.tvStatisticSVC.text = getString(
                R.string.show_statistic_svc,
                if (VideoSetting.getCurrLowStreamSetting()?.SVC == true) getString(R.string.show_setting_opened) else getString(
                    R.string.show_setting_closed
                )
            )
        } else {
            topBinding.tvStatisticSVC.text = getString(R.string.show_statistic_svc, "--")
        }

        // 本地uid
        topBinding.tvLocalUid.text =
            getString(R.string.show_local_uid, "${UserManager.getInstance().user.id}")
    }

    private fun refreshViewDetailLayout(status: Int) {
        when (status) {
            ShowInteractionStatus.idle -> {
                if (interactionInfo?.interactStatus == ShowInteractionStatus.linking) {
                    ToastUtils.showToast(R.string.show_link_is_stopped)
                } else if (interactionInfo?.interactStatus == ShowInteractionStatus.pking) {
                    ToastUtils.showToast(R.string.show_pk_is_stopped)
                }

                mBinding.videoLinkingAudienceLayout.root.isVisible = false
                mBinding.videoPKLayout.root.isVisible = false
                mBinding.videoLinkingLayout.root.isVisible = true
            }

            ShowInteractionStatus.linking -> {
                mBinding.videoPKLayout.root.isVisible = false
                mBinding.videoLinkingLayout.root.isVisible = true
                mBinding.videoLinkingAudienceLayout.root.isVisible = true
            }

            ShowInteractionStatus.pking -> {
                mBinding.videoLinkingLayout.root.isVisible = false
                mBinding.videoLinkingAudienceLayout.root.isVisible = false
                mBinding.videoPKLayout.root.isVisible = true
            }
        }
    }

    private fun refreshPKTimeCount() {
        if (interactionInfo != null && interactionInfo!!.interactStatus == ShowInteractionStatus.pking) {
            if (mPKCountDownLatch != null) {
                mPKCountDownLatch!!.cancel()
                mPKCountDownLatch = null
            }
            mPKCountDownLatch =
                object : CountDownTimer(ShowServiceProtocol.PK_AVAILABLE_DURATION - 1, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val min: Long = (millisUntilFinished / 1000) / 60
                        val sec: Long = (millisUntilFinished / 1000) % 60
                        activity ?: return
                        mBinding.videoPKLayout.iPKTimeText.text =
                            getString(
                                R.string.show_count_time_for_pk,
                                min.toString(),
                                sec.toString()
                            )
                    }

                    override fun onFinish() {
                        mService.stopInteraction(mRoomInfo.roomId)
                    }
                }.start()
        } else {
            if (mPKCountDownLatch != null) {
                mPKCountDownLatch!!.cancel()
                mPKCountDownLatch = null
            }
        }
    }


    private fun insertMessageItem(msg: ShowMessage) = runOnUiThread {
        mMessageAdapter?.let {
            it.insertLast(msg)
            mBinding.messageLayout.rvMessage.scrollToPosition(it.itemCount - 1)
        }
    }

    private fun showSettingDialog() {
        mSettingDialog.apply {
            setHostView(isRoomOwner || isMeLinking())
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    SettingDialog.ITEM_ID_CAMERA -> mRtcEngine.switchCamera()
                    SettingDialog.ITEM_ID_QUALITY -> showPictureQualityDialog(this)
                    SettingDialog.ITEM_ID_VIDEO -> {
                        // 设置弹框设置摄像头，需要同步到PK 弹框中摄像头状态，后续需要统一获取
                        if (activity is LiveDetailActivity) {
                            (activity as LiveDetailActivity).toggleSelfVideo(activated, callback = {
                                enableLocalVideo(activated)
                                mPKSettingsDialog.resetItemStatus(
                                    LivePKSettingsDialog.ITEM_ID_CAMERA,
                                    activated
                                )
                            })
                        }
                    }

                    SettingDialog.ITEM_ID_MIC -> {
                        if (activity is LiveDetailActivity) {
                            (activity as LiveDetailActivity).toggleSelfAudio(activated, callback = {
                                mService.muteAudio(
                                    mRoomInfo.roomId,
                                    !activated
                                )
                                enableLocalAudio(activated)
                            })
                        }
                    }

                    SettingDialog.ITEM_ID_STATISTIC -> changeStatisticVisible()
                    SettingDialog.ITEM_ID_SETTING -> {
                        if (AgoraApplication.the().isDebugModeOpen) {
                            if (isHostView()) showDebugModeDialog() else showAudienceDebugModeDialog()
                        } else {
                            if (isHostView()) showAdvanceSettingDialog() else AdvanceSettingAudienceDialog(
                                context
                            ).show()
                        }
                    }
                }
            }
            show()
        }
    }

    private fun showAdvanceSettingDialog() {
        AdvanceSettingDialog(requireContext(), mMainRtcConnection) { volume ->
            adjustAudioMixingVolume(volume)
        }.apply {
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE, true)
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE, true)
            show()
        }
    }

    private fun showPictureQualityDialog(parentDialog: SettingDialog) {
        PictureQualityDialog(requireContext()).apply {
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

            setOnShowListener { parentDialog.dismiss() }
            setOnDismissListener { parentDialog.show() }
            show()
        }
    }

    private fun showBeautyDialog() {
        MultiBeautyDialog(requireContext()).apply {
            show()
        }
    }

    private fun showEndRoomDialog() {
        AlertDialog.Builder(requireContext(), R.style.show_alert_dialog)
            .setTitle(R.string.show_tip)
            .setMessage(R.string.show_live_end_room_or_not)
            .setPositiveButton(R.string.show_setting_confirm) { dialog, id ->
                stopLoadPage(false)
                activity?.finish()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.show_setting_cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showMusicEffectDialog() {
        mMusicEffectDialog.setOnItemSelectedListener { musicEffectDialog, itemId ->
            when (itemId) {
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_NONE -> {
                    stopAudioMixing()
                }

                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY -> {
                    startAudioMixing(
                        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/happy.mp3",
                        false,
                        -1
                    )
                }

                MusicEffectDialog.ITEM_ID_BACK_MUSIC_ROMANTIC -> {
                    startAudioMixing(
                        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/romantic.mp3",
                        false,
                        -1
                    )
                }

                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY2 -> {
                    startAudioMixing(
                        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/relax.mp3",
                        false,
                        -1
                    )
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_ORIGINAL -> {
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CONVERSION_OFF)
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_SWEET -> {
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_SWEET)
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_ZHONGXIN -> {
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_NEUTRAL)
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_WENZHONG -> {
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_SOLID)
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_MOHUAN -> {
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_BASS)
                }

                MusicEffectDialog.ITEM_ID_MIXING_NONE -> {
                    mRtcEngine.setAudioEffectPreset(Constants.AUDIO_EFFECT_OFF)
                }

                MusicEffectDialog.ITEM_ID_MIXING_KTV -> {
                    mRtcEngine.setAudioEffectPreset(Constants.ROOM_ACOUSTICS_KTV)
                }

                MusicEffectDialog.ITEM_ID_MIXING_CONCERT -> {
                    mRtcEngine.setAudioEffectPreset(Constants.ROOM_ACOUSTICS_VOCAL_CONCERT)
                }

                MusicEffectDialog.ITEM_ID_MIXING_LUYINPEN -> {
                    mRtcEngine.setAudioEffectPreset(Constants.ROOM_ACOUSTICS_STUDIO)
                }

                MusicEffectDialog.ITEM_ID_MIXING_KONGKUANG -> {
                    mRtcEngine.setAudioEffectPreset(Constants.ROOM_ACOUSTICS_SPACIAL)
                }
            }
        }
        mMusicEffectDialog.show()
    }

    private fun showLinkSettingsDialog() {
        mLinkSettingDialog.apply {
            setAudienceInfo(interactionInfo!!.userName)
            resetSettingsItem()
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LiveLinkAudienceSettingsDialog.ITEM_ID_STOP_LINK -> {
                        if (interactionInfo != null) {
                            mService.stopInteraction(mRoomInfo.roomId, {
                                // success
                                dismiss()
                            })
                        }
                    }
                }
            }
            show()
        }
    }

    private var linkStartTime = 0L

    private fun showLinkingDialog() {
        mLinkDialog.setIsRoomOwner(isRoomOwner)
        mLinkDialog.setLinkDialogActionListener(object : OnLinkDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LiveLinkDialog) {
                mService.getAllMicSeatApplyList(mRoomInfo.roomId, {
                    mLinkDialog.setSeatApplyList(interactionInfo, it)
                })
            }

            // 主播点击同意连麦申请
            override fun onAcceptMicSeatApplyChosen(
                dialog: LiveLinkDialog,
                view: View,
                seatApply: ShowMicSeatApply
            ) {
                if (interactionInfo != null) {
                    ToastUtils.showToast(R.string.show_cannot_accept)
                    return
                }
                linkStartTime = TimeUtils.currentTimeMillis()
                view.isEnabled = false
                mService.acceptMicSeatApply(mRoomInfo.roomId, seatApply.userId,
                    success = {
                        view.isEnabled = true
                        ToastUtils.showToast("accept message successfully!")
                    },
                    error = {
                        view.isEnabled = true
                        ToastUtils.showToast("accept message failed!")
                    })
            }

            // 在线用户列表刷新
            override fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog) {
                mService.getAllUserList(mRoomInfo.roomId, {
                    val list =
                        it.filter { it.userId != UserManager.getInstance().user.id.toString() }
                    mLinkDialog.setSeatInvitationList(list)
                })
            }

            // 主播邀请用户连麦
            override fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, view: View, userItem: ShowUser) {
                if (interactionInfo != null) {
                    ToastUtils.showToast(R.string.show_cannot_invite)
                    return
                }
                view.isEnabled = false
                mService.createMicSeatInvitation(mRoomInfo.roomId, userItem.userId, success = {
                    view.isEnabled = true
                    ToastUtils.showToast("invite successfully!")
                }, error = {
                    view.isEnabled = true
                    ToastUtils.showToast("invite failed!")
                })
            }

            // 主播或连麦者停止连麦
            override fun onStopLinkingChosen(dialog: LiveLinkDialog, view: View) {
                if (interactionInfo != null) {
                    view.isEnabled = false
                    mService.stopInteraction(mRoomInfo.roomId, success = {
                        view.isEnabled = true
                    }, error = {
                        view.isEnabled = true
                        ToastUtils.showToast("stop linking failed!")
                    })
                }
            }

            // 观众撤回连麦申请
            override fun onStopApplyingChosen(dialog: LiveLinkDialog, view: View, apply: ShowMicSeatApply?) {
                updateIdleMode()
                view.isEnabled = false
                mService.cancelMicSeatApply(
                    mRoomInfo.roomId,
                    apply?.userId ?: "",
                    success = {
                        view.isEnabled = true
                    },
                    error = {
                        view.isEnabled = true
                        ToastUtils.showToast("cancel apply failed!")
                    })
            }
        })

        if (!mLinkDialog.isVisible) {
            val ft = childFragmentManager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            mLinkDialog.show(ft, "LinkDialog")
        }
    }

    private fun showInvitationDialog(invitation: ShowMicSeatInvitation) {
        if (mMicInvitationDialog?.isShowing == true) {
            return
        }
        prepareLinkingMode()
        mMicInvitationDialog = AlertDialog.Builder(requireContext(), R.style.show_alert_dialog).apply {
            setTitle(getString(R.string.show_ask_for_link, invitation.userName))

            setPositiveButton(R.string.show_setting_confirm) { dialog, which ->
                if (mLinkInvitationCountDownLatch != null) {
                    mLinkInvitationCountDownLatch!!.cancel()
                    mLinkInvitationCountDownLatch = null
                }
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.acceptMicSeatInvitation(mRoomInfo.roomId, invitation.id,
                    success = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        ToastUtils.showToast("accept invitation successfully!")
                    },
                    error = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        ToastUtils.showToast("accept invitation failed!")
                    }
                )
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, which ->
                updateIdleMode()
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.rejectMicSeatInvitation(mRoomInfo.roomId, invitation.id,
                    success = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        ToastUtils.showToast("reject invitation successfully!")
                        dialog.dismiss()
                    },
                    error = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        ToastUtils.showToast("reject invitation failed!")
                    }
                )
            }
        }.create()
        mMicInvitationDialog?.show()
        if (mLinkInvitationCountDownLatch != null) {
            mLinkInvitationCountDownLatch!!.cancel()
            mLinkInvitationCountDownLatch = null
        }
        mLinkInvitationCountDownLatch = object : CountDownTimer(15 * 1000 - 1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mMicInvitationDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.text =
                    "${getString(R.string.show_setting_cancel)}(" + millisUntilFinished / 1000 + "s)"
            }

            override fun onFinish() {
                mService.rejectMicSeatInvitation(mRoomInfo.roomId, invitation.id)
                mMicInvitationDialog?.dismiss()
            }
        }.start()
    }

    private fun showPKDialog() {
        mPKDialog.setPKDialogActionListener(object : OnPKDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LivePKDialog) {
                mService.getAllPKUserList(mRoomInfo.roomId, { pkUserList ->
                    mPKDialog.setOnlineBroadcasterList(
                        interactionInfo,
                        pkUserList.map {
                            LiveRoomConfig(
                                ShowRoomDetailModel(
                                    it.roomId,
                                    "",
                                    0,
                                    it.userId,
                                    it.avatar,
                                    it.userName,
                                ),
                                it.status,
                                false
                            )
                        }
                    )
                })
            }

            override fun onInviteButtonChosen(
                dialog: LivePKDialog,
                view: View,
                roomItem: LiveRoomConfig
            ) {
                if (roomItem.isRobotRoom()) {
                    ToastUtils.showToast(context?.getString(R.string.show_tip1))
                    return
                }
                if (isRoomOwner) {
                    val roomDetail = roomItem.convertToShowRoomDetailModel()
                    prepareRoomInfo = roomDetail
                    preparePKingMode(roomDetail.roomId)
                    view.isEnabled = false
                    mService.createPKInvitation(mRoomInfo.roomId, roomDetail.roomId, success = {
                        view.isEnabled = true
                        ToastUtils.showToast("invite message successfully!")
                    }) {
                        view.isEnabled = true
                        ToastUtils.showToast("invite message failed!")
                    }
                }
            }

            override fun onStopPKingChosen(dialog: LivePKDialog) {
                mService.stopInteraction(mRoomInfo.roomId)
            }
        })
        if (!mPKDialog.isVisible) {
            val ft = childFragmentManager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            mPKDialog.show(ft, "PKDialog")
        }
    }

    var pkStartTime = 0L

    private fun showPKInvitationDialog(name: String, pkInvitation: ShowPKInvitation) {
        if(mPKInvitationDialog?.isShowing == true){
            return
        }
        mPKInvitationDialog = AlertDialog.Builder(requireContext(), R.style.show_alert_dialog).apply {
            setCancelable(false)
            setTitle(getString(R.string.show_ask_for_pk, name))
            setPositiveButton(R.string.show_setting_confirm) { dialog, which ->
                if (mPKInvitationCountDownLatch != null) {
                    mPKInvitationCountDownLatch!!.cancel()
                    mPKInvitationCountDownLatch = null
                }
                pkStartTime = TimeUtils.currentTimeMillis()
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.acceptPKInvitation(mRoomInfo.roomId, pkInvitation.id, {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    ToastUtils.showToast("accept message successfully!")
                }) {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    ToastUtils.showToast("accept message failed!")
                }
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, which ->
                updateIdleMode()
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.rejectPKInvitation(mRoomInfo.roomId, pkInvitation.id, {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    dialog.dismiss()
                }) {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    ToastUtils.showToast("reject message failed!")
                }
                isPKCompetition = false
            }
        }.create()
        mPKInvitationDialog?.show()
        if (mPKInvitationCountDownLatch != null) {
            mPKInvitationCountDownLatch!!.cancel()
            mPKInvitationCountDownLatch = null
        }
        mPKInvitationCountDownLatch = object : CountDownTimer(15 * 1000 - 1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mPKInvitationDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.text =
                    "取消(" + millisUntilFinished / 1000 + "s)"
            }

            override fun onFinish() {
                updateIdleMode()
                mService.rejectPKInvitation(mRoomInfo.roomId, pkInvitation.id) { }
                mPKInvitationDialog?.dismiss()
            }
        }.start()
    }

    private fun showPKSettingsDialog() {
        mPKSettingsDialog.apply {
            resetSettingsItem(false)
            setPKInfo(interactionInfo!!.userName)
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LivePKSettingsDialog.ITEM_ID_CAMERA -> {
                        if (activity is LiveDetailActivity) {
                            (activity as LiveDetailActivity).toggleSelfVideo(activated, callback = {
                                enableLocalVideo(activated)
                                // pk 弹框设置摄像头，需要同步到设置弹框中摄像头状态，后续需要统一获取
                                mSettingDialog.resetItemStatus(
                                    SettingDialog.ITEM_ID_VIDEO,
                                    activated
                                )
                            })
                        }

                    }

                    LivePKSettingsDialog.ITEM_ID_SWITCH_CAMERA -> mRtcEngine.switchCamera()
                    LivePKSettingsDialog.ITEM_ID_MIC -> {
                        if (activity is LiveDetailActivity) {
                            (activity as LiveDetailActivity).toggleSelfAudio(activated, callback = {
                                mService.muteAudio(mRoomInfo.roomId, !activated)
                                mRtcEngine.muteLocalAudioStreamEx(
                                    !activated,
                                    RtcConnection(
                                        interactionInfo!!.roomId,
                                        UserManager.getInstance().user.id.toInt()
                                    )
                                )
                            })
                        }
                    }

                    LivePKSettingsDialog.ITEM_ID_STOP_PK -> {
                        if (interactionInfo != null) {
                            mService.stopInteraction(mRoomInfo.roomId, {
                                // success
                                dismiss()
                            })
                        }
                    }
                }
            }
            show()
        }
    }

    private fun enableComeBackSoonView(enable: Boolean) {
        if (isPKing()) {
            // TODO
        } else {
            mBinding.livingComeSoonLayout.root.isVisible = enable
            if (enable) {
                mBinding.topLayout.root.bringToFront()
                mBinding.bottomLayout.root.bringToFront()
                if (isLinking()) {
                    mBinding.videoLinkingAudienceLayout.root.bringToFront()
                }
            }
        }
    }

    //================== Service Operation ===============

    private fun initServiceWithJoinRoom() {
        val create = arguments?.getBoolean(EXTRA_CREATE_ROOM, false) ?: false
        if (create) {
            mService.createRoom(
                mRoomInfo.roomId,
                mRoomInfo.roomName,
                success = {
                    mService.sendChatMessage(
                        mRoomInfo.roomId,
                        getString(R.string.show_live_chat_coming)
                    )
                    initService()
                },
                error = {
                    runOnUiThread {
                        destroy(false)
                        // 进房Error
                        showLivingEndLayout() // 进房Error
                        ShowLogger.d("showLivingEndLayout", "join room error!:${it.message}")
                    }
                })
        } else {
            mService.joinRoom(mRoomInfo.roomId,
                success = {
                    mService.sendChatMessage(
                        mRoomInfo.roomId,
                        getString(R.string.show_live_chat_coming)
                    )
                    initService()
                },
                error = {
                    runOnUiThread {
                        destroy(false)
                        // 进房Error
                        showLivingEndLayout() // 进房Error
                        ShowLogger.d("showLivingEndLayout", "join room error!:${it.message}")
                    }
                })
        }
    }

    private fun initService() {
        reFetchUserList()
        mService.subscribeReConnectEvent(mRoomInfo.roomId) {
            context ?: return@subscribeReConnectEvent
            reFetchUserList()
            reFetchPKInvitationList()
            reFetchInteractionList()
        }
        mService.subscribeCurrRoomEvent(mRoomInfo.roomId) { status, _ ->
            if (status == ShowSubscribeStatus.deleted) {
                destroy(false) // 房间被房主关闭
                showLivingEndLayout()// 房间被房主关闭
                ShowLogger.d("showLivingEndLayout", "room delete by owner!")
            }
        }
        mService.subscribeMicSeatInvitation(mRoomInfo.roomId) { _, invitation ->
            invitation ?: return@subscribeMicSeatInvitation
            context ?: return@subscribeMicSeatInvitation
            if (invitation.type == ShowInvitationType.invitation) {
                showInvitationDialog(invitation)
            }
        }
        mService.subscribeUser(mRoomInfo.roomId) { status, user ->
            reFetchUserList()

            if (status == ShowSubscribeStatus.updated) {
                if (interactionInfo?.interactStatus == ShowInteractionStatus.linking && interactionInfo?.userId == user?.userId) {
                    mBinding.videoLinkingAudienceLayout.userName.isActivated = !(user?.muteAudio ?: false)
                }
            }
        }
        mService.subscribeMessage(mRoomInfo.roomId) { _, showMessage ->
            insertMessageItem(showMessage)
        }
        mService.subscribeMicSeatApply(mRoomInfo.roomId) { _, list ->
            if (isRoomOwner) {
                mBinding.bottomLayout.vLinkingDot.isVisible = list.isNotEmpty() || isMeLinking()
            }
            mLinkDialog.setSeatApplyList(interactionInfo, list)
        }
        mService.subscribeInteractionChanged(mRoomInfo.roomId) { status, info ->
            context ?: return@subscribeInteractionChanged
            if (status == ShowSubscribeStatus.updated && info != null) {
                // 开始互动
                if (interactionInfo == null) {
                    interactionInfo = info
                    // UI
                    updateVideoSetting(true)
                    refreshBottomLayout()
                    refreshViewDetailLayout(info.interactStatus)
                    mLinkDialog.setOnSeatStatus(info.userName, info.interactStatus)
                    mPKDialog.setPKInvitationItemStatus(info.userName, info.interactStatus)
                    // RTC
                    updateLinkingMode()
                    updatePKingMode()
                    refreshPKTimeCount()
                } else {
                    // 互动中状态更新
                    interactionInfo = info
                }
                mMicInvitationDialog?.dismiss()
                mPKInvitationDialog?.dismiss()
            } else {
                // 停止互动
                // UI
                refreshViewDetailLayout(ShowInteractionStatus.idle)
                mLinkDialog.setOnSeatStatus("", null)
                mPKDialog.setPKInvitationItemStatus("", null)
                // RTC
                updateIdleMode()
                interactionInfo = null
                refreshBottomLayout()
                refreshPKTimeCount()
                updateVideoSetting(false)
                onMeLinkingListener?.onMeLinking(false)
            }
        }

        mService.subscribePKInvitationChanged(mRoomInfo.roomId) { status, info ->
            info ?: return@subscribePKInvitationChanged
            if (info.userId == UserManager.getInstance().user.id.toString()
                && context != null
            ) {
                if (info.type == ShowInvitationType.invitation) {
                    isPKCompetition = true
                    preparePKingMode(info.fromRoomId)
                    showPKInvitationDialog(info.fromUserName, info)
                }
            }
        }

        mService.getInteractionInfo(mRoomInfo.roomId, { interactionInfo ->
            this.interactionInfo = interactionInfo
            refreshBottomLayout()
            val isPkMode = interactionInfo?.interactStatus == ShowInteractionStatus.pking
            updateVideoSetting(isPkMode)
            if (interactionInfo != null) {
                refreshViewDetailLayout(interactionInfo.interactStatus)
                if (interactionInfo.interactStatus == ShowInteractionStatus.linking) {
                    updateLinkingMode()
                } else if (interactionInfo.interactStatus == ShowInteractionStatus.pking) {
                    updatePKingMode()
                }
            } else {
                refreshViewDetailLayout(ShowInteractionStatus.idle)
            }
        })
    }

    private fun reFetchUserList() {
        mService.getAllUserList(mRoomInfo.roomId, {
            refreshTopUserCount(it.size)
        })
    }

    private fun reFetchPKInvitationList() {
        ShowLogger.d(TAG, "reFetchPKInvitationList")
    }

    private fun reFetchInteractionList() {
        mService.getInteractionInfo(mRoomInfo.roomId, { info ->
            if (info != null && this.interactionInfo == null) {
                interactionInfo = info
                // UI
                updateVideoSetting(true)
                refreshBottomLayout()
                refreshViewDetailLayout(info.interactStatus)
                mLinkDialog.setOnSeatStatus(info.userName, info.interactStatus)
                mPKDialog.setPKInvitationItemStatus(info.userName, info.interactStatus)
                // RTC
                updateLinkingMode()
                updatePKingMode()
                refreshPKTimeCount()
            }

            if (info == null && this.interactionInfo != null) {
                // UI
                refreshViewDetailLayout(ShowInteractionStatus.idle)
                mLinkDialog.setOnSeatStatus("", null)
                mPKDialog.setPKInvitationItemStatus("", null)
                // RTC
                updateIdleMode()
                isPKCompetition = false
                interactionInfo = null
                refreshBottomLayout()
                refreshPKTimeCount()
                updateVideoSetting(false)
                onMeLinkingListener?.onMeLinking(false)
            }
        })
    }

    private fun isMeLinking() =
        isLinking() && interactionInfo?.userId == UserManager.getInstance().user.id.toString()

    private fun isLinking() = (interactionInfo?.interactStatus
        ?: ShowInteractionStatus.idle) == ShowInteractionStatus.linking

    private fun isPKing() = (interactionInfo?.interactStatus
        ?: ShowInteractionStatus.idle) == ShowInteractionStatus.pking

    private fun destroyService() {
        mService.leaveRoom(mRoomInfo.roomId)
    }

    private fun showLivingEndLayout() {
        if (isRoomOwner) {
            val context = activity ?: return
            AlertDialog.Builder(context, R.style.show_alert_dialog)
                .setView(
                    ShowLivingEndDialogBinding.inflate(LayoutInflater.from(requireContext()))
                        .apply {
                            Glide.with(this@LiveDetailFragment)
                                .load(mRoomInfo.ownerAvatar)
                                .into(ivAvatar)
                        }.root
                )
                .setCancelable(false)
                .setPositiveButton(R.string.show_living_end_back_room_list) { dialog, _ ->
                    activity?.finish()
                    dialog.dismiss()
                }
                .show()
        } else {
            mBinding.livingEndLayout.root.isVisible = true
            mBinding.livingEndLayout.root.bringToFront()
        }
    }

    //================== RTC Operation ===================

    private var quickStartTime = 0L
    private var subscribeMediaTime = 0L
    private fun initRtcEngine() {
        val eventListener = object : IRtcEngineEventHandler() {
            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                if (interactionInfo != null && interactionInfo!!.userId == uid.toString() && interactionInfo!!.interactStatus == ShowInteractionStatus.pking) {
                    mService.stopInteraction(mRoomInfo.roomId)
                }
            }

            override fun onLocalVideoStateChanged(
                source: Constants.VideoSourceType?,
                state: Int,
                error: Int
            ) {
                super.onLocalVideoStateChanged(source, state, error)
                if (isRoomOwner) {
                    isAudioOnlyMode = state == Constants.LOCAL_VIDEO_STREAM_STATE_STOPPED
                }
            }

            override fun onRemoteVideoStateChanged(
                uid: Int,
                state: Int,
                reason: Int,
                elapsed: Int
            ) {
                super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                if (uid == mRoomInfo.ownerId.toInt()) {
                    isAudioOnlyMode = state == Constants.REMOTE_VIDEO_STATE_STOPPED

                    runOnUiThread {
                        if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                            enableComeBackSoonView(true)
                        } else if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                            enableComeBackSoonView(false)
                        }
                    }
                }

                if (state == Constants.REMOTE_VIDEO_STATE_DECODING
                    && (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED || reason == Constants.REMOTE_VIDEO_STATE_REASON_LOCAL_UNMUTED)
                ) {
                    val durationFromSubscribe = SystemClock.elapsedRealtime() - subscribeMediaTime
                    quickStartTime = durationFromSubscribe
                }
            }

            override fun onRtcStats(stats: RtcStats) {
                super.onRtcStats(stats)
                runOnUiThread {
                    refreshStatisticInfo(
                        cpuAppUsage = stats.cpuAppUsage,
                        cpuTotalUsage = stats.cpuTotalUsage,
                    )
                }
            }

            override fun onLocalVideoStats(
                source: Constants.VideoSourceType,
                stats: LocalVideoStats
            ) {
                super.onLocalVideoStats(source, stats)
                runOnUiThread {
                    refreshStatisticInfo(
                        upBitrate = stats.sentBitrate,
                        encodeFps = stats.encoderOutputFrameRate,
                        upLossPackage = stats.txPacketLossRate,
                        encodeVideoSize = Size(stats.encodedFrameWidth, stats.encodedFrameHeight),
                        codecType = stats.codecType
                    )
                }
            }

            override fun onLocalAudioStats(stats: LocalAudioStats) {
                super.onLocalAudioStats(stats)
                runOnUiThread {
                    refreshStatisticInfo(
                        audioBitrate = stats.sentBitrate,
                        audioLossPackage = stats.txPacketLossRate
                    )
                }
            }

            override fun onRemoteVideoStats(stats: RemoteVideoStats) {
                super.onRemoteVideoStats(stats)
                val isLinkingAudience =
                    isRoomOwner && isLinking() && stats.uid.toString() == interactionInfo?.userId
                if (stats.uid == mRoomInfo.ownerId.toInt() || isLinkingAudience) {
                    runOnUiThread {
                        refreshStatisticInfo(
                            downBitrate = stats.receivedBitrate,
                            receiveFPS = stats.decoderOutputFrameRate,
                            downLossPackage = stats.packetLossRate,
                            receiveVideoSize = Size(stats.width, stats.height),
                            downDelay = stats.delay
                        )
                    }
                }
            }

            override fun onRemoteAudioStats(stats: RemoteAudioStats) {
                super.onRemoteAudioStats(stats)
                // 连麦观众
                val isLinkingAudience =
                    isRoomOwner && isLinking() && stats.uid.toString() == interactionInfo?.userId
                if (stats.uid == mRoomInfo.ownerId.toInt() || isLinkingAudience) {
                    runOnUiThread {
                        refreshStatisticInfo(
                            audioBitrate = stats.receivedBitrate,
                            audioLossPackage = stats.audioLossRate
                        )
                    }
                }
            }

            override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo) {
                super.onUplinkNetworkInfoUpdated(info)
                runOnUiThread {
                    refreshStatisticInfo(
                        upLinkBps = info.video_encoder_target_bitrate_bps
                    )
                }
            }

            override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo) {
                super.onDownlinkNetworkInfoUpdated(info)
                runOnUiThread {
                    refreshStatisticInfo(
                        downLinkBps = info.bandwidth_estimation_bps
                    )
                }
            }

            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                enableContentInspectEx()
            }

            override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
                if (interactionInfo?.userId == uid.toString()) {
                    if (linkStartTime != 0L) {
                        ShowLogger.d(
                            TAG,
                            "Interaction user first video frame from host accept linking: ${TimeUtils.currentTimeMillis() - linkStartTime}"
                        )
                        linkStartTime = 0L
                    } else {
                        ShowLogger.d(
                            TAG,
                            "Interaction user first video frame from user accept linking: ${TimeUtils.currentTimeMillis() - (interactionInfo?.createdAt?.toLong() ?: 0L)}"
                        )
                    }
                }
            }
        }

        if (activity is LiveDetailActivity) {
            (activity as LiveDetailActivity).toggleSelfVideo(
                isRoomOwner || isMeLinking(),
                callback = {
                    joinChannel(eventListener)
                    initVideoView()
                    initAudioModeration()
                })
            (activity as LiveDetailActivity).toggleSelfAudio(
                isRoomOwner || isMeLinking(),
                callback = {
                    // nothing
                    scenarioApi.initialize()
                    if (isRoomOwner) {
                        scenarioApi.setAudioScenario(SceneType.Show, AudioScenarioType.Show_Host)
                    } else if (isMeLinking()) {
                        scenarioApi.setAudioScenario(
                            SceneType.Show,
                            AudioScenarioType.Show_InteractiveAudience
                        )
                    }
                })
        }
    }

    private fun enableContentInspectEx() {
        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "show")
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectModule()
            module.interval = 60
            module.type = CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            mRtcEngine.enableContentInspectEx(true, contentInspectConfig, mMainRtcConnection)
        } catch (_: JSONException) {

        }
    }

    // 观众配置
    private fun setEnhance(stats: IRtcEngineEventHandler.RemoteVideoStats) {
        // 只处理房主推送过来的视频流信息
        if (mRoomInfo.ownerId != stats.uid.toString()) {
            return
        }
        var showTip = false
        var superResolution: VideoSetting.SuperResolution = VideoSetting.SuperResolution.SR_NONE
        when (VideoSetting.getCurrBroadcastSetting()) {
            VideoSetting.RecommendBroadcastSetting.LowDevice1v1, VideoSetting.RecommendBroadcastSetting.MediumDevice1v1, VideoSetting.RecommendBroadcastSetting.HighDevice1v1 -> when (VideoSetting.getCurrAudiencePlaySetting()) {
                // 画质增强、高端机
                VideoSetting.AudiencePlaySetting.ENHANCE_HIGH -> {
                    // 1080P
                    if (stats.width == VideoSetting.Resolution.V_1080P.height && stats.height == VideoSetting.Resolution.V_1080P.width) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.height && stats.height == VideoSetting.Resolution.V_720P.width) {
                        // 将画质增强-高端机 及 画质增强-中端机的“超分倍数“改成 20,超级画质
                        superResolution = VideoSetting.SuperResolution.SR_SUPER
                    }
                    // 540P、480P
                    else if ((stats.width == VideoSetting.Resolution.V_540P.height && stats.height == VideoSetting.Resolution.V_540P.width)
                        || (stats.width == VideoSetting.Resolution.V_480P.height && stats.height == VideoSetting.Resolution.V_480P.width)
                    ) {
                        superResolution = VideoSetting.SuperResolution.SR_1_33
                    }
                    // 360P以及以下
                    else {
                        superResolution = VideoSetting.SuperResolution.SR_2
                    }
                }
                // 画质增强、中端机
                VideoSetting.AudiencePlaySetting.ENHANCE_MEDIUM -> {
                    // 1080P
                    if (stats.width == VideoSetting.Resolution.V_1080P.height && stats.height == VideoSetting.Resolution.V_1080P.width) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.height && stats.height == VideoSetting.Resolution.V_720P.width) {
                        // 将画质增强-高端机 及 画质增强-中端机的“超分倍数“改成 20,超级画质
                        superResolution = VideoSetting.SuperResolution.SR_SUPER
                    }
                    // 360P以及以下
                    else {
                        superResolution = VideoSetting.SuperResolution.SR_1_33
                    }
                }
                // 画质增强、低端机
                VideoSetting.AudiencePlaySetting.ENHANCE_LOW -> {
                    superResolution = VideoSetting.SuperResolution.SR_NONE
                }
            }

            VideoSetting.RecommendBroadcastSetting.LowDevicePK, VideoSetting.RecommendBroadcastSetting.MediumDevicePK, VideoSetting.RecommendBroadcastSetting.HighDevicePK -> when (VideoSetting.getCurrAudiencePlaySetting()) {
                // 画质增强、高端机
                VideoSetting.AudiencePlaySetting.ENHANCE_HIGH -> {
                    superResolution = VideoSetting.SuperResolution.SR_1_33
                }
                // 画质增强、中端机
                VideoSetting.AudiencePlaySetting.ENHANCE_MEDIUM -> {
                    superResolution = VideoSetting.SuperResolution.SR_1_33
                }
                // 画质增强、低端机
                VideoSetting.AudiencePlaySetting.ENHANCE_LOW -> {
                    superResolution = VideoSetting.SuperResolution.SR_NONE
                }
            }
        }
        // 不要重复设置
        if (superResolution == VideoSetting.getCurrAudienceSetting().video.SR) {
            return
        }
        if (showTip) {
            ToastUtils.showToast(context?.getString(R.string.show_setting_quality_enhance_tip2))
        }
        VideoSetting.updateAudioSetting(SR = superResolution)
    }

    private fun destroyRtcEngine(isScrolling: Boolean): Boolean {
        if (isRoomOwner) {
            mRtcEngine.stopPreview()
            mRtcEngine.leaveChannelEx(mMainRtcConnection)
            mMediaPlayer?.destroy()

            if (isPKing()) {
                mRtcEngine.leaveChannelEx(
                    RtcConnection(
                        interactionInfo!!.roomId,
                        UserManager.getInstance().user.id.toInt()
                    )
                )
            }
            mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CONVERSION_OFF)
            mRtcEngine.setAudioEffectPreset(Constants.AUDIO_EFFECT_OFF)
        }
        return true
    }

    private fun enableLocalAudio(enable: Boolean) {
        mRtcEngine.muteLocalAudioStreamEx(!enable, mMainRtcConnection)
        if (enable) {
            VideoSetting.updateBroadcastSetting(
                inEarMonitoring = VideoSetting.getCurrBroadcastSetting().audio.inEarMonitoring
            )
        }
    }

    private fun enableLocalVideo(enable: Boolean) {
        mRtcEngine.muteLocalVideoStreamEx(!enable, mMainRtcConnection)
        if (enable) {
            mRtcEngine.startPreview()
        } else {
            mRtcEngine.stopPreview()
        }
        if (isRoomOwner) {
            enableComeBackSoonView(!enable)
        }
    }

    private fun joinChannel(eventListener: IRtcEngineEventHandler) {
        if (!isRoomOwner && mRtcEngine.queryDeviceScore() < 75) {
            // 低端机观众加入频道前默认开启硬解（解决看高分辨率卡顿问题），但是在410分支硬解码会带来200ms的秒开耗时增加
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // 低端机观众加入频道前默认开启下行零拷贝，下行零拷贝和超分有冲突， 低端机默认关闭超分
            mRtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
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
            channelMediaOptions.audienceLatencyLevel = AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }

        if (isRoomOwner) {
            mRtcEngine.joinChannelEx(
                RtcEngineInstance.generalToken(),
                mMainRtcConnection,
                channelMediaOptions,
                eventListener
            )
        } else {
            mRtcEngine.addHandlerEx(eventListener, mMainRtcConnection)
        }
    }

    private fun setupLocalVideo(container: VideoLoader.VideoCanvasContainer) {
        localVideoCanvas?.let {
            if (it.lifecycleOwner == container.lifecycleOwner && it.renderMode == container.renderMode && it.uid == container.uid) {
                val videoView = it.view
                val viewIndex = container.container.indexOfChild(videoView)
                if (viewIndex == container.viewIndex) {
                    return
                }
                (videoView.parent as? ViewGroup)?.removeView(videoView)
                container.container.addView(videoView, container.viewIndex)
                return
            }
        }
        var videoView = container.container.getChildAt(container.viewIndex)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (videoView !is SurfaceView) {
                videoView = SurfaceView(container.container.context)
                container.container.addView(videoView, container.viewIndex)
            }
        } else {
            if (videoView !is TextureView) {
                videoView = TextureView(container.container.context)
                container.container.addView(videoView, container.viewIndex)
            }
        }

        val local = LocalVideoCanvasWrap(
            container.lifecycleOwner,
            videoView, container.renderMode, container.uid
        )
        BeautyManager.setupLocalVideo(videoView, container.renderMode)
    }

    private fun updateVideoSetting(isPkMode: Boolean) {
        VideoSetting.setIsPkMode(isPkMode)
        if (isRoomOwner || isMeLinking()) {
            VideoSetting.updateBroadcastSetting(
                when (interactionInfo?.interactStatus) {
                    ShowInteractionStatus.pking -> VideoSetting.LiveMode.PK
                    else -> VideoSetting.LiveMode.OneVOne
                },
                isLinkAudience = !isRoomOwner,
                rtcConnection = mMainRtcConnection
            )
        } else {
            VideoSetting.updateAudienceSetting()
        }
    }


    private fun updateIdleMode() {
        ShowLogger.d(TAG, "Interaction >> updateIdleMode")
        if (interactionInfo?.interactStatus == ShowInteractionStatus.pking) {
            // 退出连麦多频道，主播需要离开对方频道
            if (isRoomOwner) {
                mRtcEngine.leaveChannelEx(
                    RtcConnection(
                        interactionInfo!!.roomId,
                        UserManager.getInstance().user.id.toInt()
                    )
                )
            } else {
                mHandler.updateRoomInfo(
                    position = mPosition,
                    VideoLoader.RoomInfo(
                        mRoomInfo.roomId, arrayListOf(
                            VideoLoader.AnchorInfo(
                                mRoomInfo.roomId,
                                mRoomInfo.ownerId.toInt(),
                                RtcEngineInstance.generalToken()
                            )
                        )
                    )
                )
            }
        } else if (prepareRkRoomId.isNotEmpty()) {
            mRtcEngine.leaveChannelEx(
                RtcConnection(
                    prepareRkRoomId,
                    UserManager.getInstance().user.id.toInt()
                )
            )
        }
        prepareRkRoomId = ""

        if (isRoomOwner) {
            enableLocalAudio(true)
            enableLocalVideo(true)
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
            activity?.let {
                setupLocalVideo(
                    VideoLoader.VideoCanvasContainer(
                        it,
                        mBinding.videoLinkingLayout.videoContainer,
                        0
                    )
                )
            }
            refreshStatisticInfo(
                receiveVideoSize = Size(0, 0),
                downBitrate = 0
            )
        } else {
            val channelMediaOptions = ChannelMediaOptions()
            val rtcConnection = mMainRtcConnection
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection)
            refreshStatisticInfo(
                encodeVideoSize = Size(0, 0),
                upBitrate = 0,
            )
        }
    }

    private fun prepareLinkingMode() {
        ShowLogger.d(TAG, "Interaction >> prepareLinkingMode")

        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.publishCameraTrack = true
        channelMediaOptions.publishMicrophoneTrack = false
        channelMediaOptions.publishCustomAudioTrack = false
        channelMediaOptions.enableAudioRecordingOrPlayout = true
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = true
        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER

        (activity as? LiveDetailActivity)?.let {
            it.toggleSelfVideo(true) { hasPermission ->
                if (hasPermission) {
                    mRtcEngine.updateChannelMediaOptionsEx(
                        channelMediaOptions,
                        mMainRtcConnection
                    )
                }
            }
        }
    }

    private fun updateLinkingMode() {
        // 开始连麦
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.linking) return
        val rtcConnection = mMainRtcConnection
        ShowLogger.d(TAG, "Interaction >> updateLinkingMode")

        mBinding.videoLinkingAudienceLayout.userName.text = interactionInfo!!.userName
        mBinding.videoLinkingAudienceLayout.userName.bringToFront()
        mBinding.videoLinkingAudienceLayout.userName.isActivated = true
        if (isRoomOwner) {
            // 连麦主播视角
            mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                showLinkSettingsDialog()
            }
            enableLocalAudio(true)
            mService.muteAudio(mRoomInfo.roomId, false)
            // pk摄像头默认开启 todo 统一入口获取摄像头状态
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
            mPKSettingsDialog.resetItemStatus(LivePKSettingsDialog.ITEM_ID_CAMERA, true)
            enableLocalVideo(true)
            activity?.let {
                setupLocalVideo(
                    VideoLoader.VideoCanvasContainer(
                        it,
                        mBinding.videoLinkingLayout.videoContainer,
                        0
                    )
                )
                val view = TextureView(it)
                mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
                mBinding.videoLinkingAudienceLayout.videoContainer.addView(view)
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(
                        view,
                        1,
                        interactionInfo?.userId!!.toInt()
                    ),
                    rtcConnection
                )
            }
        } else {
            // 连麦观众视角
            if (interactionInfo?.userId.equals(UserManager.getInstance().user.id.toString())) {
                // 连麦中观众不允许切换房间
                onMeLinkingListener?.onMeLinking(true)
                mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                    showLinkSettingsDialog()
                }
                // 重新连麦，恢复摄像头开启状态
                mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
                enableLocalAudio(true)
                val channelMediaOptions = ChannelMediaOptions()
                channelMediaOptions.publishCameraTrack = true
                channelMediaOptions.publishMicrophoneTrack = true
                channelMediaOptions.publishCustomAudioTrack = false
                channelMediaOptions.enableAudioRecordingOrPlayout = true
                channelMediaOptions.autoSubscribeVideo = true
                channelMediaOptions.autoSubscribeAudio = true
                channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                if (activity is LiveDetailActivity) {
                    (activity as LiveDetailActivity).toggleSelfVideo(true, callback = {
                        if (it) {
                            // 有权限
                            mRtcEngine.updateChannelMediaOptionsEx(
                                channelMediaOptions,
                                rtcConnection
                            )
                            val context = activity ?: return@toggleSelfVideo
                            val textureView = TextureView(context)
                            mBinding.videoLinkingAudienceLayout.videoContainer.addView(textureView)
                            mRtcEngine.setupLocalVideo(VideoCanvas(
                                textureView
                            ).apply {
                                mirrorMode = VIDEO_MIRROR_MODE_DISABLED
                            })
                        } else {
                            // 没有权限
                            mService.stopInteraction(mRoomInfo.roomId)
                        }
                    })
                    (activity as LiveDetailActivity).toggleSelfAudio(true, callback = {
                        // nothing
                        scenarioApi.setAudioScenario(
                            SceneType.Show,
                            AudioScenarioType.Show_InteractiveAudience
                        )
                    })
                }
            } else {
                // 其他观众视角
                activity?.let {
                    val view = TextureView(it)
                    mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
                    mBinding.videoLinkingAudienceLayout.videoContainer.addView(view)
                    mRtcEngine.setupRemoteVideoEx(
                        VideoCanvas(
                            view,
                            1,
                            interactionInfo?.userId!!.toInt()
                        ),
                        rtcConnection
                    )
                }
            }
        }
    }

    private var prepareRkRoomId = ""
    private var prepareRoomInfo: ShowRoomDetailModel? = null

    private fun preparePKingMode(pkRoomId: String) {
        ShowLogger.d(TAG, "Interaction >> preparePKingMode pkRoomId=$pkRoomId")
        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.publishCameraTrack = false
        channelMediaOptions.publishMicrophoneTrack = false
        channelMediaOptions.publishCustomAudioTrack = false
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = false
        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.audienceLatencyLevel =
            Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
        channelMediaOptions.isInteractiveAudience = true
        val pkRtcConnection = RtcConnection(
            pkRoomId,
            UserManager.getInstance().user.id.toInt()
        )
        mRtcEngine.joinChannelEx(
            RtcEngineInstance.generalToken(),
            pkRtcConnection,
            channelMediaOptions,
            object : IRtcEngineEventHandler() {
                override fun onRemoteVideoStats(stats: RemoteVideoStats) {
                    super.onRemoteVideoStats(stats)
                    if (isRoomOwner) {
                        activity?.runOnUiThread {
                            refreshStatisticInfo(
                                downBitrate = stats.receivedBitrate,
                                receiveFPS = stats.decoderOutputFrameRate,
                                downLossPackage = stats.packetLossRate,
                                receiveVideoSize = Size(stats.width, stats.height),
                                downDelay = stats.delay
                            )
                        }
                    }
                }

                override fun onRemoteAudioStats(stats: RemoteAudioStats) {
                    super.onRemoteAudioStats(stats)
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            audioBitrate = stats.receivedBitrate,
                            audioLossPackage = stats.audioLossRate
                        )
                    }
                }

                override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo) {
                    super.onDownlinkNetworkInfoUpdated(info)
                    activity?.runOnUiThread {
                        refreshStatisticInfo(downLinkBps = info.bandwidth_estimation_bps)
                    }
                }

                override fun onFirstRemoteVideoFrame(
                    uid: Int,
                    width: Int,
                    height: Int,
                    elapsed: Int
                ) {
                    super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
                    if (interactionInfo?.userId == uid.toString()) {
                        if (pkStartTime != 0L) {
                            ShowLogger.d(
                                TAG,
                                "Interaction user first video frame from host accept pking : ${TimeUtils.currentTimeMillis() - pkStartTime}"
                            )
                            pkStartTime = 0L
                        } else {
                            ShowLogger.d(
                                TAG,
                                "Interaction user first video frame from host accepted pking : ${TimeUtils.currentTimeMillis() - (interactionInfo?.createdAt?.toLong() ?: 0L)}"
                            )
                            pkStartTime = 0L
                        }
                    }
                }
            }
        )
        prepareRkRoomId = pkRoomId
    }

    private var pkAgainstView: View? = null
    private fun updatePKingMode() {
        // 开始pk
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.pking) return
        ShowLogger.d(TAG, "Interaction >> updatePKingMode pkRoomId=${interactionInfo!!.roomId}")
        val eventListener = object : IRtcEngineEventHandler() {
            override fun onRemoteVideoStats(stats: RemoteVideoStats) {
                super.onRemoteVideoStats(stats)
                if (isRoomOwner) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            downBitrate = stats.receivedBitrate,
                            receiveFPS = stats.decoderOutputFrameRate,
                            downLossPackage = stats.packetLossRate,
                            receiveVideoSize = Size(stats.width, stats.height),
                            downDelay = stats.delay
                        )
                    }
                }
            }

            override fun onRemoteAudioStats(stats: RemoteAudioStats) {
                super.onRemoteAudioStats(stats)
                activity?.runOnUiThread {
                    refreshStatisticInfo(
                        audioBitrate = stats.receivedBitrate,
                        audioLossPackage = stats.audioLossRate
                    )
                }
            }

            override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo) {
                super.onDownlinkNetworkInfoUpdated(info)
                activity?.runOnUiThread {
                    refreshStatisticInfo(downLinkBps = info.bandwidth_estimation_bps)
                }
            }

            override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
                if (interactionInfo?.userId == uid.toString()) {
                    if (pkStartTime != 0L) {
                        ShowLogger.d(
                            TAG,
                            "Interaction user first video frame from host accept pking : ${TimeUtils.currentTimeMillis() - pkStartTime}"
                        )
                        pkStartTime = 0L
                    } else {
                        ShowLogger.d(
                            TAG,
                            "Interaction user first video frame from host accepted pking : ${TimeUtils.currentTimeMillis() - (interactionInfo?.createdAt?.toLong() ?: 0L)}"
                        )
                        pkStartTime = 0L
                    }
                }
            }
        }

        mBinding.videoPKLayout.userNameA.text = mRoomInfo.ownerName
        mBinding.videoPKLayout.userNameB.text = interactionInfo!!.userName
        if (isRoomOwner) {
            // pk 主播
            mBinding.livingComeSoonLayout.root.isVisible = false
            mBinding.videoPKLayout.iBroadcasterBView.setOnClickListener {
                showPKSettingsDialog()
            }
            activity?.let {
                setupLocalVideo(
                    VideoLoader.VideoCanvasContainer(
                        it,
                        mBinding.videoPKLayout.iBroadcasterAView,
                        0,
                        viewIndex = 0
                    )
                )
            }
            enableLocalAudio(true)
            if (isRoomOwner) {
                // 连麦摄像头默认开启 todo 统一入口获取摄像头状态
                mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
                mPKSettingsDialog.resetItemStatus(LivePKSettingsDialog.ITEM_ID_CAMERA, true)
                enableLocalVideo(true)
            }
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            channelMediaOptions.audienceLatencyLevel =
                Constants.AUDIENCE_LATENCY_LEVEL_ULTRA_LOW_LATENCY
            channelMediaOptions.isInteractiveAudience = true
            val pkRtcConnection = RtcConnection(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toInt()
            )
            mRtcEngine.joinChannelEx(
                RtcEngineInstance.generalToken(),
                pkRtcConnection,
                channelMediaOptions,
                eventListener
            )
            activity?.let {
                mBinding.videoPKLayout.iBroadcasterBView.removeView(pkAgainstView)
                pkAgainstView = TextureView(it)
                mBinding.videoPKLayout.iBroadcasterBView.addView(pkAgainstView, 0)
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(
                        pkAgainstView,
                        1,
                        interactionInfo?.userId!!.toInt(),
                    ),
                    pkRtcConnection
                )
            }
        } else {
            // 观众
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
            mHandler.updateRoomInfo(
                position = mPosition,
                VideoLoader.RoomInfo(
                    mRoomInfo.roomId, arrayListOf(
                        VideoLoader.AnchorInfo(
                            mRoomInfo.roomId,
                            mRoomInfo.ownerId.toInt(),
                            RtcEngineInstance.generalToken()
                        ),
                        VideoLoader.AnchorInfo(
                            interactionInfo!!.roomId,
                            interactionInfo?.userId!!.toInt(),
                            RtcEngineInstance.generalToken()
                        ),
                    )
                )
            )
        }
    }

    private fun releaseCountdown() {
        if (mLinkInvitationCountDownLatch != null) {
            mLinkInvitationCountDownLatch!!.cancel()
            mLinkInvitationCountDownLatch = null
        }
        if (mPKInvitationCountDownLatch != null) {
            mPKInvitationCountDownLatch!!.cancel()
            mPKInvitationCountDownLatch = null
        }
        if (mPKCountDownLatch != null) {
            mPKCountDownLatch!!.cancel()
            mPKCountDownLatch = null
        }
        (mBinding.topLayout.tvTimer.tag as? Runnable)?.let {
            it.run()
            mBinding.topLayout.tvTimer.removeCallbacks(it)
            mBinding.topLayout.tvTimer.tag = null
        }
    }

    private var onMeLinkingListener: OnMeLinkingListener? = null

    interface OnMeLinkingListener {
        fun onMeLinking(isLinking: Boolean)
    }

    inner class LocalVideoCanvasWrap constructor(
        val lifecycleOwner: LifecycleOwner,
        view: View,
        renderMode: Int,
        uid: Int
    ) : DefaultLifecycleObserver, VideoCanvas(view, renderMode, uid) {

        init {
            lifecycleOwner.lifecycle.addObserver(this)
            if (localVideoCanvas != this) {
                localVideoCanvas?.release()
                localVideoCanvas = this
            }
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            if (lifecycleOwner == owner) {
                release()
            }
        }

        fun release() {
            lifecycleOwner.lifecycle.removeObserver(this)
            view = null
            mRtcEngine.setupLocalVideo(this)
            localVideoCanvas = null
        }
    }

    // 播放音乐相关接口
    private var mMediaPlayer: IMediaPlayer? = null
    private fun startAudioMixing(
        filePath: String,
        loopbackOnly: Boolean,
        cycle: Int
    ) {
        val mediaPlayer = mMediaPlayer ?: mRtcEngine.createMediaPlayer()
        mMediaPlayer = mediaPlayer
        mediaPlayer.stop()
        mediaPlayer.openWithMediaSource(MediaPlayerSource().apply {
            url = filePath
            isAutoPlay = true
        })
        mediaPlayer.setLoopCount(if (cycle >= 0) 0 else Int.MAX_VALUE)

        if (!loopbackOnly) {
            val mediaOptions = ChannelMediaOptions()
            mediaOptions.publishMediaPlayerId = mediaPlayer.mediaPlayerId
            // TODO: 没开启麦克风权限情况下，publishMediaPlayerAudioTrack = true 会自动停止音频播放
            mediaOptions.publishMediaPlayerAudioTrack = true
            mRtcEngine.updateChannelMediaOptionsEx(mediaOptions, mMainRtcConnection)
        }
    }

    private fun stopAudioMixing() {
        // 停止播放，拿到connection对应的MediaPlayer并停止释放
        mMediaPlayer?.stop()

        // 停止推流，使用updateChannelMediaOptionEx
        val mediaOptions = ChannelMediaOptions()
        mediaOptions.publishMediaPlayerAudioTrack = false
        mRtcEngine.updateChannelMediaOptionsEx(mediaOptions, mMainRtcConnection)
    }

    private fun adjustAudioMixingVolume(volume: Int) {
        mMediaPlayer?.adjustPlayoutVolume(volume)
        mMediaPlayer?.adjustPublishSignalVolume(volume)
    }
}
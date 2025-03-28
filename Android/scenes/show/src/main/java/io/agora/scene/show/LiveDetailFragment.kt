package io.agora.scene.show

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.audioscenarioapi.AudioScenarioApi
import io.agora.audioscenarioapi.AudioScenarioType
import io.agora.audioscenarioapi.SceneType
import io.agora.base.VideoFrame
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.data.MediaPlayerSource
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
import io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_DISABLED
import io.agora.rtc2.Constants.VIDEO_MIRROR_MODE_ENABLED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.LeaveChannelOptions
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
import io.agora.rtc2.video.ContentInspectConfig.ContentInspectModule
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.scene.base.AgoraTokenType
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.TokenGeneratorType
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
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
import io.agora.scene.widget.toast.CustomToast
import io.agora.videoloaderapi.OnPageScrollEventHandler
import io.agora.videoloaderapi.VideoLoader
import io.agora.videoloaderapi.VideoLoaderImpl
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/*
 * Live streaming room page
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
    private val mService by lazy { ShowServiceProtocol.get() }
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

    // Current interaction status
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
        destroy(false) // Room has reached its limit time
        showLivingEndLayout() // Room has reached its limit time
        ShowLogger.d("showLivingEndLayout", "timer end!")
    }

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }

    private val mBackPressedCallback by lazy {
        object: OnBackPressedCallback(isVisible){
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
    }

    private var mMicInvitationDialog: AlertDialog?= null
    private var mPKInvitationDialog: AlertDialog?= null

    private var mPKEventHandler: IRtcEngineEventHandler? = null

    private val mUserMuteAudioStateMap = mutableMapOf<Int, Boolean>()

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
        activity?.onBackPressedDispatcher?.addCallback(mBackPressedCallback)
        // Requirement: Show live streaming
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
        mBackPressedCallback.isEnabled = true
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
        mBackPressedCallback.isEnabled = false
        destroy(isScrolling) // Page switch or activity destroyed
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
        // Check if the view hasn't been created yet, i.e., render video immediately after view creation
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
            AudioModeration.AgoraChannelType.Broadcast,
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
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
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
        bottomLayout.ivLinking.setOnClickListener {view ->
            // If it's a robot
            if (mRoomInfo.isRobotRoom()) {
                val context = context?:return@setOnClickListener
                CustomToast.show(context.getString(R.string.show_tip1))
                return@setOnClickListener
            }
            if (!isRoomOwner) {
                // Audience sends a linking request
                if (interactionInfo == null
                    || interactionInfo?.interactStatus == ShowInteractionStatus.idle
                    || interactionInfo?.userId != UserManager.getInstance().user.id.toString()
                ) {
                    // Audience sends video stream
                    prepareLinkingMode()
                    view.isClickable = false
                    mService.createMicSeatApply(mRoomInfo.roomId, {
                        // success
                        view.isClickable = true
                        mLinkDialog.setOnApplySuccess(it)
                    }) {
                        view.isClickable = true
                        val context = context?:return@createMicSeatApply
                        CustomToast.show(
                            context.getString(
                                R.string.show_create_micseat_apply_error,
                                it.message
                            )
                        )
                    }
                } else {
                    ShowLogger.d(TAG, "audience not create mic seat apply. interactionInfo=$interactionInfo")
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
        bottomLayout.flLinking.isVisible = true

        if (isRoomOwner) {
            // Host
            bottomLayout.flPK.isVisible = true

            // Host can control video
            bottomLayout.ivSetting.isVisible = true
            bottomLayout.ivMusic.isVisible = true
            bottomLayout.ivBeauty.isVisible = true

            if (isPKing()) {
                // PK status
                // Host is definitely one of the PK parties

            } else if (isLinking()) {
                // Linking status
                // Host is definitely one of the linking parties
                bottomLayout.ivLinking.imageTintList = null
                mSettingDialog.apply {
                    resetSettingsItem(false)
                }
            } else {
                // Single host status
                // Host is the host
                bottomLayout.ivLinking.imageTintList =
                    ColorStateList.valueOf(context.resources.getColor(io.agora.scene.widget.R.color.grey_7e))
                mSettingDialog.apply {
                    resetSettingsItem(false)
                }
            }

        } else {
            // Audience

            bottomLayout.ivSetting.isVisible = true
            // Audience does not have PK permissions
            bottomLayout.flPK.isVisible = false

            if (isPKing()) {
                // PK status
                // PK is between the host and the host, and has nothing to do with the audience. The audience can only watch, and cannot link again
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false
            } else if (isLinking()) {
                // Linking status
                if (isMeLinking()) {
                    // One of the linking parties
                    bottomLayout.ivMusic.isVisible = false
                    bottomLayout.ivBeauty.isVisible = false

                    bottomLayout.ivLinking.imageTintList = null
                } else {
                    // Just a viewer, not participating in linking
                    bottomLayout.ivMusic.isVisible = false
                    bottomLayout.ivBeauty.isVisible = false
                }
            } else {
                // Single host status
                // Regular audience, only has the button to initiate linking request
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false

                bottomLayout.ivLinking.imageTintList =
                    ColorStateList.valueOf(context.resources.getColor(io.agora.scene.widget.R.color.grey_7e))
            }
        }
    }

    private fun showMessageInputDialog() {
        val context = context ?: return
        TextInputDialog(context)
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
        // Encoding resolution, receiving resolution
        encodeVideoSize: Size? = null, receiveVideoSize: Size? = null,
        // Encoding frame rate, receiving frame rate
        encodeFps: Int? = null, receiveFPS: Int? = null,
        // Downlink delay
        downDelay: Int? = null,
        // Uplink packet loss rate, downlink packet loss rate
        upLossPackage: Int? = null, downLossPackage: Int? = null,
        // Uplink bitrate, downlink bitrate
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
        // Encoding resolution
        encodeVideoSize?.let {
            topBinding.tvEncodeResolution.text =
                getString(R.string.show_statistic_encode_resolution, "${it.height}x${it.width}")
        }
        if (topBinding.tvEncodeResolution.text.isEmpty()) topBinding.tvEncodeResolution.text =
            getString(R.string.show_statistic_encode_resolution, "--")
        // Receiving resolution
        receiveVideoSize?.let {
            topBinding.tvReceiveResolution.text =
                getString(R.string.show_statistic_receive_resolution, "${it.height}x${it.width}")
        }
        if (topBinding.tvReceiveResolution.text.isEmpty()) topBinding.tvReceiveResolution.text =
            getString(R.string.show_statistic_receive_resolution, "--")
        // Encoding frame rate
        encodeFps?.let {
            topBinding.tvStatisticEncodeFPS.text =
                getString(R.string.show_statistic_encode_fps, it.toString())
        }
        if (topBinding.tvStatisticEncodeFPS.text.isEmpty()) topBinding.tvStatisticEncodeFPS.text =
            getString(R.string.show_statistic_encode_fps, "--")
        // Receiving frame rate
        receiveFPS?.let {
            topBinding.tvStatisticReceiveFPS.text =
                getString(R.string.show_statistic_receive_fps, it.toString())
        }
        if (topBinding.tvStatisticReceiveFPS.text.isEmpty()) topBinding.tvStatisticReceiveFPS.text =
            getString(R.string.show_statistic_receive_fps, "--")
        // Downlink delay
        downDelay?.let {
            topBinding.tvStatisticDownDelay.text =
                getString(R.string.show_statistic_delay, it.toString())
        }
        if (topBinding.tvStatisticDownDelay.text.isEmpty()) topBinding.tvStatisticDownDelay.text =
            getString(R.string.show_statistic_delay, "--")
        // Uplink packet loss rate
        upLossPackage?.let {
            topBinding.tvStatisticUpLossPackage.text =
                getString(R.string.show_statistic_up_loss_package, it.toString())
        }
        if (topBinding.tvStatisticUpLossPackage.text.isEmpty()) topBinding.tvStatisticUpLossPackage.text =
            getString(R.string.show_statistic_up_loss_package, "--")
        // Downlink packet loss rate
        downLossPackage?.let {
            topBinding.tvStatisticDownLossPackage.text =
                getString(R.string.show_statistic_down_loss_package, it.toString())
        }
        if (topBinding.tvStatisticDownLossPackage.text.isEmpty()) topBinding.tvStatisticDownLossPackage.text =
            getString(R.string.show_statistic_down_loss_package, "--")
        // Uplink bitrate
        upBitrate?.let {
            topBinding.tvStatisticUpBitrate.text =
                getString(R.string.show_statistic_up_bitrate, it.toString())
        }
        if (topBinding.tvStatisticUpBitrate.text.isEmpty()) topBinding.tvStatisticUpBitrate.text =
            getString(R.string.show_statistic_up_bitrate, "--")
        // Downlink bitrate
        downBitrate?.let {
            topBinding.tvStatisticDownBitrate.text =
                getString(R.string.show_statistic_down_bitrate, it.toString())
        }
        if (topBinding.tvStatisticDownBitrate.text.isEmpty()) topBinding.tvStatisticDownBitrate.text =
            getString(R.string.show_statistic_down_bitrate, "--")
        // Uplink network
        topBinding.tvStatisticUpNet.isVisible = !isAudioOnlyMode
        upLinkBps?.let {
            topBinding.tvStatisticUpNet.text =
                getString(R.string.show_statistic_up_net_speech, (it / 8192).toString())
        }
        if (topBinding.tvStatisticUpNet.text.isEmpty()) topBinding.tvStatisticUpNet.text =
            getString(R.string.show_statistic_up_net_speech, "--")
        // Downlink network
        topBinding.tvStatisticDownNet.isVisible = !isAudioOnlyMode
        downLinkBps?.let {
            topBinding.tvStatisticDownNet.text =
                getString(R.string.show_statistic_down_net_speech, (it / 8192).toString())
        }
        if (topBinding.tvStatisticDownNet.text.isEmpty()) topBinding.tvStatisticDownNet.text =
            getString(R.string.show_statistic_down_net_speech, "--")
        // Quick start time
        topBinding.tvQuickStartTime.isVisible = true
        if (isRoomOwner) {
            topBinding.tvQuickStartTime.text =
                getString(R.string.show_statistic_quick_start_time, "--")
        } else {
            // TODO
            topBinding.tvQuickStartTime.text =
                getString(R.string.show_statistic_quick_start_time, (mRtcVideoLoaderApi as VideoLoaderImpl).getProfiler(mRoomInfo.roomId, mRoomInfo.ownerId.toInt()).perceivedCost.toString())
        }
        // Device grade
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
        // H265 switch
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
        // Super resolution switch
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
        // pvc switch
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

        // Local uid
        topBinding.tvLocalUid.text =
            getString(R.string.show_local_uid, "${UserManager.getInstance().user.id}")
    }

    private fun refreshViewDetailLayout(status: Int) {
        when (status) {
            ShowInteractionStatus.idle -> {
                if (interactionInfo?.interactStatus == ShowInteractionStatus.linking) {
                    CustomToast.show(R.string.show_link_is_stopped)
                } else if (interactionInfo?.interactStatus == ShowInteractionStatus.pking) {
                    CustomToast.show(R.string.show_pk_is_stopped)
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
                object : CountDownTimer((ShowServiceProtocol.PK_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - interactionInfo!!.createdAt)).toLong(), 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val min: Long = (millisUntilFinished / 1000) / 60
                        val sec: Long = (millisUntilFinished / 1000) % 60 + 1
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
                    SettingDialog.ITEM_ID_CAMERA -> {
                        RtcEngineInstance.isFrontCamera = !RtcEngineInstance.isFrontCamera
                        mRtcEngine.switchCamera()
                        setMirrorMode()
                    }
                    SettingDialog.ITEM_ID_QUALITY -> showPictureQualityDialog(this)
                    SettingDialog.ITEM_ID_VIDEO -> {
                        // Set the dialog box to select the camera, which needs to be synchronized with the camera status in the PK dialog box, and will be unified later
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
                            if (isRoomOwner) showDebugModeDialog() else showAudienceDebugModeDialog()
                        } else {
                            if (isRoomOwner) showAdvanceSettingDialog() else AdvanceSettingAudienceDialog(
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
        val context = context?:return
        AdvanceSettingDialog(context, mMainRtcConnection) { volume ->
            adjustAudioMixingVolume(volume)
        }.apply {
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE, true)
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE, true)
            show()
        }
    }

    private fun showPictureQualityDialog(parentDialog: SettingDialog) {
        val context = context ?: return
        PictureQualityDialog(context).apply {
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
        val context = context ?: return
        MultiBeautyDialog(context).apply {
            show()
        }
    }

    private fun showEndRoomDialog() {
        val context = context ?: return
        AlertDialog.Builder(context, R.style.show_alert_dialog)
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

            // Host clicks to accept the linking request
            override fun onAcceptMicSeatApplyChosen(
                dialog: LiveLinkDialog,
                view: View,
                seatApply: ShowMicSeatApply
            ) {
                if (interactionInfo != null) {
                    CustomToast.show(R.string.show_cannot_accept)
                    return
                }
                linkStartTime = TimeUtils.currentTimeMillis()
                view.isEnabled = false
                mService.acceptMicSeatApply(mRoomInfo.roomId, seatApply.userId,
                    success = {
                        view.isEnabled = true
                        CustomToast.show("accept message successfully!")
                    },
                    error = {
                        view.isEnabled = true
                        val context = context?:return@acceptMicSeatApply
                        CustomToast.show(
                            context.getString(
                                R.string.show_accept_micseat_apply_error,
                                it.message
                            )
                        )
                    })
            }

            // Refresh the list of online users
            override fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog) {
                mService.getAllUserList(mRoomInfo.roomId, {
                    val list =
                        it.filter { it.userId != UserManager.getInstance().user.id.toString() }
                    mLinkDialog.setSeatInvitationList(list)
                })
            }

            // Host invites users to link
            override fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, view: View, userItem: ShowUser) {
                if (interactionInfo != null) {
                    CustomToast.show(R.string.show_cannot_invite)
                    return
                }
                view.isEnabled = false
                mService.createMicSeatInvitation(mRoomInfo.roomId, userItem.userId, success = {
                    view.isEnabled = true
                    CustomToast.show("invite successfully!")
                }, error = {
                    view.isEnabled = true
                    val context = context?:return@createMicSeatInvitation
                    CustomToast.show(context.getString(
                        R.string.show_create_micseat_invitation_error,
                        it.message
                    ))
                })
            }

            // Host or linking party stops linking
            override fun onStopLinkingChosen(dialog: LiveLinkDialog, view: View) {
                if (interactionInfo != null) {
                    view.isEnabled = false
                    mService.stopInteraction(mRoomInfo.roomId, success = {
                        view.isEnabled = true
                    }, error = {
                        view.isEnabled = true
                        CustomToast.show("stop linking failed!")
                    })
                }
            }

            // Audience withdraws the linking request
            override fun onStopApplyingChosen(dialog: LiveLinkDialog, view: View, apply: ShowMicSeatApply?) {
                updateIdleMode()
                view.isEnabled = false
                mService.cancelMicSeatApply(
                    mRoomInfo.roomId,
                    success = {
                        view.isEnabled = true
                    },
                    error = {
                        view.isEnabled = true
                        CustomToast.show("cancel apply failed!")
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
        val context = context ?: return
        mMicInvitationDialog = AlertDialog.Builder(context, R.style.show_alert_dialog).apply {
            setCancelable(false)
            setTitle(getString(R.string.show_ask_for_link, invitation.userName))
            setPositiveButton(R.string.show_setting_confirm, null)
            setNegativeButton(R.string.show_setting_cancel) { dialog, which ->
                updateIdleMode()
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.rejectMicSeatInvitation(mRoomInfo.roomId, invitation.id,
                    success = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        CustomToast.show("reject invitation successfully!")
                        dismissMicInvitaionDialog()
                    },
                    error = {
                        (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                        CustomToast.show("reject invitation failed!")
                    }
                )
            }
        }.create()
        mMicInvitationDialog?.setOnShowListener {
            mMicInvitationDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.let { btn ->
                btn.setOnClickListener {
                    btn.isEnabled = false
                    mService.acceptMicSeatInvitation(mRoomInfo.roomId, invitation.id,
                        success = {
                            btn.isEnabled = true
                            CustomToast.show("accept invitation successfully!")
                            dismissMicInvitaionDialog()
                        },
                        error = { error ->
                            btn.isEnabled = true
                            val context = context?:return@acceptMicSeatInvitation
                            CustomToast.show(
                                context.getString(
                                    R.string.show_accept_micseat_invitation_error,
                                    error.message
                                )
                            )
                        }
                    )
                }
            }
        }
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
                    val context = context?:return
                    CustomToast.show(context.getString(R.string.show_tip1))
                    return
                }
                if (interactionInfo != null) {
                    CustomToast.show(R.string.show_cannot_invite_pk)
                    return
                }
                if (isRoomOwner) {
                    val roomDetail = roomItem.convertToShowRoomDetailModel()
                    prepareRoomInfo = roomDetail
                    preparePKingMode(roomDetail.roomId)
                    view.isEnabled = false
                    mService.createPKInvitation(mRoomInfo.roomId, roomDetail.roomId, success = {
                        view.isEnabled = true
                        CustomToast.show("invite message successfully!")
                    }) {
                        view.isEnabled = true
                        val context = context?:return@createPKInvitation
                        CustomToast.show(context.getString(
                            R.string.show_create_pk_invitation_error,
                            it.message
                        ))
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
        val context = context ?: return
        mPKInvitationDialog = AlertDialog.Builder(context, R.style.show_alert_dialog).apply {
            setCancelable(false)
            setTitle(getString(R.string.show_ask_for_pk, name))
            setPositiveButton(R.string.show_setting_confirm, null)
            setNegativeButton(R.string.show_setting_cancel) { dialog, which ->
                updateIdleMode()
                (dialog as? AlertDialog)?.getButton(which)?.isEnabled = false
                mService.rejectPKInvitation(mRoomInfo.roomId, pkInvitation.id, {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    dismissPKInvitationDialog()
                }) {
                    (dialog as? AlertDialog)?.getButton(which)?.isEnabled = true
                    CustomToast.show("reject message failed!")
                }
                isPKCompetition = false
            }
        }.create()
        mPKInvitationDialog?.setOnShowListener {
            mPKInvitationDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.let { btn ->
                btn.setOnClickListener {
                    pkStartTime = TimeUtils.currentTimeMillis()
                    btn.isEnabled = false
                    mService.acceptPKInvitation(mRoomInfo.roomId, pkInvitation.id, {
                        btn.isEnabled = true
                        CustomToast.show("accept message successfully!")
                        dismissPKInvitationDialog()
                    }) {
                        btn.isEnabled = true
                        val context = context?:return@acceptPKInvitation
                        CustomToast.show(context.getString(
                            R.string.show_accept_pk_invitation_error,
                            it.message
                        ))
                    }
                }
            }
        }
        mPKInvitationDialog?.show()
        if (mPKInvitationCountDownLatch != null) {
            mPKInvitationCountDownLatch!!.cancel()
            mPKInvitationCountDownLatch = null
        }
        mPKInvitationCountDownLatch = object : CountDownTimer(15 * 1000 - 1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mPKInvitationDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.text =
                    "Cancel(" + millisUntilFinished / 1000 + "s)"
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
                                // pk dialog box sets the camera, which needs to be synchronized with the camera status in the settings dialog box, and will be unified later
                                mSettingDialog.resetItemStatus(
                                    SettingDialog.ITEM_ID_VIDEO,
                                    activated
                                )
                            })
                        }

                    }

                    LivePKSettingsDialog.ITEM_ID_SWITCH_CAMERA -> {
                        RtcEngineInstance.isFrontCamera = !RtcEngineInstance.isFrontCamera
                        mRtcEngine.switchCamera()
                    }
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
            mBinding.videoPKLayout.iBroadcasterAViewOverlay.isVisible = enable
        } else {
            mBinding.livingComeSoonLayout.root.isVisible = enable
            if (enable) {
                if (isLinking()) {
                    mBinding.videoLinkingAudienceLayout.root.bringToFront()
                }
                mBinding.topLayout.root.bringToFront()
                mBinding.bottomLayout.root.bringToFront()
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
                    initService()
                },
                error = {
                    runOnUiThread {
                        destroy(false)
                        // Room creation error
                        showLivingEndLayout(true) // Room creation error
                        ShowLogger.d("showLivingEndLayout", "create room error!:${it.message}")
                    }
                })
        } else {
            mService.joinRoom(mRoomInfo.roomId,
                success = {
                    initService()
                },
                error = {
                    runOnUiThread {
                        destroy(false)
                        // Room creation error
                        showLivingEndLayout(true) // Room creation error
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
                destroy(false) // Room closed by host
                showLivingEndLayout()// Room closed by host
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

            if (status == ShowSubscribeStatus.added && user != null) {
                insertMessageItem(
                    ShowMessage(
                        user.userId,
                        user.userName,
                        getString(R.string.show_live_chat_coming)
                    )
                )
            } else if (status == ShowSubscribeStatus.deleted && user != null) {
                insertMessageItem(
                    ShowMessage(
                        user.userId,
                        user.userName,
                        getString(R.string.show_live_chat_leaving)
                    )
                )
            }
        }
        mService.subscribeMessage(mRoomInfo.roomId) { _, showMessage ->
            insertMessageItem(showMessage)
        }
        mService.subscribeMicSeatApply(mRoomInfo.roomId) { _, list ->
            mBinding.bottomLayout.vLinkingDot.isVisible = list.isNotEmpty() && isRoomOwner
            mLinkDialog.setSeatApplyList(interactionInfo, list)
        }
        mService.subscribeInteractionChanged(mRoomInfo.roomId) { status, info ->
            context ?: return@subscribeInteractionChanged
            if (status == ShowSubscribeStatus.updated
                && info != null
                && info.interactStatus != ShowInteractionStatus.idle) {
                // Start interaction
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

                dismissMicInvitaionDialog()
                dismissPKInvitationDialog()
            } else {
                // Stop interaction
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
            context ?: return@subscribePKInvitationChanged
            when(info.type){
                ShowInvitationType.invitation -> {
                    isPKCompetition = true
                    preparePKingMode(info.fromRoomId)
                    showPKInvitationDialog(info.fromUserName, info)
                }
                ShowInvitationType.reject -> {
                    isPKCompetition = false
                    updateIdleMode()
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
                    refreshPKTimeCount()
                }
            } else {
                refreshViewDetailLayout(ShowInteractionStatus.idle)
            }
        })
    }

    private fun dismissPKInvitationDialog() {
        mPKInvitationDialog?.dismiss()
        mPKInvitationCountDownLatch?.cancel()
        mPKInvitationCountDownLatch = null
    }

    private fun dismissMicInvitaionDialog() {
        mMicInvitationDialog?.dismiss()
        mLinkInvitationCountDownLatch?.cancel()
        mLinkInvitationCountDownLatch = null
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

    private fun showLivingEndLayout(fromError: Boolean = false) {
        if (isRoomOwner) {
            val context = activity ?: return
            AlertDialog.Builder(context, R.style.show_alert_dialog)
                .setView(
                    ShowLivingEndDialogBinding.inflate(LayoutInflater.from(context))
                        .apply {
                            if (fromError) {
                                tvTitle.setText(R.string.show_living_end_title_error)
                            }
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
            if (fromError) {
                mBinding.livingEndLayout.tvLivingEnd.setText(R.string.show_live_detail_living_end_error)
            }
            mBinding.livingEndLayout.root.isVisible = true
            mBinding.livingEndLayout.root.bringToFront()
        }
    }

    //================== RTC Operation ===================

    private val eventListener = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            super.onUserJoined(uid, elapsed)
            if (uid != mAudioMxingChannel?.localUid) {
                mRtcEngine.muteRemoteAudioStreamEx(uid, false, mMainRtcConnection)
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            if (uid != mAudioMxingChannel?.localUid) {
                mRtcEngine.muteRemoteAudioStreamEx(uid, true, mMainRtcConnection)
            }
        }

        override fun onLocalVideoStateChanged(
            source: Constants.VideoSourceType?,
            state: Int,
            error: Int
        ) {
            super.onLocalVideoStateChanged(source, state, error)
            ShowLogger.d(TAG, "onLocalVideoStateChanged: $state")
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
            ShowLogger.d(TAG, "onRemoteVideoStateChanged: uid=$uid, state=$state, reason=$reason")
            if (uid == mRoomInfo.ownerId.toInt()) {
                isAudioOnlyMode = state == Constants.REMOTE_VIDEO_STATE_STOPPED

                runOnUiThread {
                    if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                        enableComeBackSoonView(true)
                    } else if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                        enableComeBackSoonView(false)
                    }
                }
            } else if (isLinking() && uid == interactionInfo?.userId?.toInt()) {
                runOnUiThread {
                    if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                        mBinding.videoLinkingAudienceLayout.videoOverlay.isVisible = true
                    } else if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                        mBinding.videoLinkingAudienceLayout.videoOverlay.isVisible = false
                    }
                }
            }
        }

        override fun onLocalAudioStateChanged(state: Int, reason: Int) {
            super.onLocalAudioStateChanged(state, reason)
            ShowLogger.d(TAG, "onLocalAudioStateChanged: state=$state, reason=$reason")
            runOnUiThread {
                if (state == Constants.LOCAL_AUDIO_STREAM_STATE_STOPPED) {
                    if (isMeLinking()) {
                        mBinding.videoLinkingAudienceLayout.userName.isActivated = false
                    }
                    mBinding.videoPKLayout.userNameA.isActivated = false
                } else if (state == Constants.LOCAL_AUDIO_STREAM_STATE_ENCODING) {
                    if (isMeLinking()) {
                        mBinding.videoLinkingAudienceLayout.userName.isActivated = true
                    }
                    mBinding.videoPKLayout.userNameA.isActivated = true
                }
            }
        }

        override fun onAudioPublishStateChanged(
            channel: String?,
            oldState: Int,
            newState: Int,
            elapseSinceLastState: Int
        ) {
            super.onAudioPublishStateChanged(channel, oldState, newState, elapseSinceLastState)
            ShowLogger.d(TAG, "onAudioPublishStateChanged: channel=$channel, oldState=$oldState, newState=$newState, elapseSinceLastState=$elapseSinceLastState")
            runOnUiThread {
                if (newState == 1) {
                    if (isMeLinking()) {
                        mBinding.videoLinkingAudienceLayout.userName.isActivated = false
                    }
                    mBinding.videoPKLayout.userNameA.isActivated = false
                } else if (newState == 3) {
                    if (isMeLinking()) {
                        mBinding.videoLinkingAudienceLayout.userName.isActivated = true
                    }
                    mBinding.videoPKLayout.userNameA.isActivated = true
                }
            }
        }

        override fun onRemoteAudioStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
            ShowLogger.d(TAG, "onRemoteAudioStateChanged: uid=$uid, state=$state, reason=$reason")
            val unmute = state == Constants.REMOTE_AUDIO_STATE_DECODING
            mUserMuteAudioStateMap[uid] = !unmute
            if (isLinking() && uid == interactionInfo?.userId?.toInt()) {
                runOnUiThread {
                    mBinding.videoLinkingAudienceLayout.userName.isActivated = unmute
                }
            } else if (isPKing()) {
                runOnUiThread {
                    mBinding.videoPKLayout.userNameA.isActivated = unmute
                }
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
            // Linking audience
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

    private fun initRtcEngine() {
        if (activity is LiveDetailActivity) {
            (activity as LiveDetailActivity).toggleSelfVideo(
                isRoomOwner || isMeLinking(),
                callback = {
                    setMirrorMode()
                    joinChannel(eventListener)
                    initVideoView()
                    initAudioModeration()
                })
            (activity as LiveDetailActivity).toggleSelfAudio(
                isRoomOwner || isMeLinking(),
                callback = {
                    // nothing
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
        // ------------------ Enable content inspection service ------------------
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

    // Audience configuration
    private fun setEnhance(stats: IRtcEngineEventHandler.RemoteVideoStats) {
        // Only process video stream information pushed by the host
        if (mRoomInfo.ownerId != stats.uid.toString()) {
            return
        }
        var showTip = false
        var superResolution: VideoSetting.SuperResolution = VideoSetting.SuperResolution.SR_NONE
        when (VideoSetting.getCurrBroadcastSetting()) {
            VideoSetting.RecommendBroadcastSetting.LowDevice1v1, VideoSetting.RecommendBroadcastSetting.MediumDevice1v1, VideoSetting.RecommendBroadcastSetting.HighDevice1v1 -> when (VideoSetting.getCurrAudiencePlaySetting()) {
                // Quality enhancement, high-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_HIGH -> {
                    // 1080P
                    if (stats.width == VideoSetting.Resolution.V_1080P.height && stats.height == VideoSetting.Resolution.V_1080P.width) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.height && stats.height == VideoSetting.Resolution.V_720P.width) {
                        // Change the "super resolution" of quality enhancement-high-end machine and quality enhancement-medium machine to 20, super quality
                        superResolution = VideoSetting.SuperResolution.SR_SUPER
                    }
                    // 540P, 480P
                    else if ((stats.width == VideoSetting.Resolution.V_540P.height && stats.height == VideoSetting.Resolution.V_540P.width)
                        || (stats.width == VideoSetting.Resolution.V_480P.height && stats.height == VideoSetting.Resolution.V_480P.width)
                    ) {
                        superResolution = VideoSetting.SuperResolution.SR_1_33
                    }
                    // 360P and below
                    else {
                        superResolution = VideoSetting.SuperResolution.SR_2
                    }
                }
                // Quality enhancement, medium-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_MEDIUM -> {
                    // 1080P
                    if (stats.width == VideoSetting.Resolution.V_1080P.height && stats.height == VideoSetting.Resolution.V_1080P.width) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.height && stats.height == VideoSetting.Resolution.V_720P.width) {
                        // Change the "super resolution" of quality enhancement-high-end machine and quality enhancement-medium machine to 20, super quality
                        superResolution = VideoSetting.SuperResolution.SR_SUPER
                    }
                    // 360P and below
                    else {
                        superResolution = VideoSetting.SuperResolution.SR_1_33
                    }
                }
                // Quality enhancement, low-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_LOW -> {
                    superResolution = VideoSetting.SuperResolution.SR_NONE
                }
            }

            VideoSetting.RecommendBroadcastSetting.LowDevicePK, VideoSetting.RecommendBroadcastSetting.MediumDevicePK, VideoSetting.RecommendBroadcastSetting.HighDevicePK -> when (VideoSetting.getCurrAudiencePlaySetting()) {
                // Quality enhancement, high-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_HIGH -> {
                    superResolution = VideoSetting.SuperResolution.SR_1_33
                }
                // Quality enhancement, medium-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_MEDIUM -> {
                    superResolution = VideoSetting.SuperResolution.SR_1_33
                }
                // Quality enhancement, low-end machine
                VideoSetting.AudiencePlaySetting.ENHANCE_LOW -> {
                    superResolution = VideoSetting.SuperResolution.SR_NONE
                }
            }
        }
        // Don't repeat the setting
        if (superResolution == VideoSetting.getCurrAudienceSetting().video.SR) {
            return
        }
        if (showTip) {
            context?.let {
                CustomToast.show(it.getString(R.string.show_setting_quality_enhance_tip2))
            }
        }
        VideoSetting.updateAudioSetting(SR = superResolution)
    }

    private fun destroyRtcEngine(isScrolling: Boolean): Boolean {
        if (isRoomOwner) {
            mRtcEngine.stopPreview()
            mRtcEngine.leaveChannelEx(mMainRtcConnection)
            stopAudioMixing()

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
        } else {
            mRtcEngine.removeHandlerEx(eventListener, mMainRtcConnection)
            if(isPKing()){
                mPKEventHandler?.let {
                    mRtcEngine.removeHandlerEx(it, RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()))
                    mPKEventHandler = null
                }
            }
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
        } else if (isMeLinking()) {
            mBinding.videoLinkingAudienceLayout.videoOverlay.isVisible = !enable
        }
    }

    private fun joinChannel(eventListener: IRtcEngineEventHandler) {
        if (!isRoomOwner && mRtcEngine.queryDeviceScore() < 75) {
            // Low-end machine audience joins the channel before default hard decoding (to solve the problem of high resolution carding), but hard decoding in the 410 branch will increase the startup time by 200ms
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 1}")
            // Low-end machine audience joins the channel before default downlink zero copy, downlink zero copy and super resolution are conflicting, low-end machine defaults to off super resolution
            mRtcEngine.setParameters("{\"rtc.video.decoder_out_byte_frame\": true}")
        } else {
            // Host joins the channel before default hard decoding
            mRtcEngine.setParameters("{\"che.hardware_decoding\": 0}")
        }

        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType =
            if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = false
        channelMediaOptions.publishCameraTrack = isRoomOwner
        channelMediaOptions.publishMicrophoneTrack = isRoomOwner
        // If it's an audience, set the audienceLatencyLevel of ChannelMediaOptions to AUDIENCE_LATENCY_LEVEL_LOW_LATENCY (ultra-low latency)
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
        if (videoView !is TextureView) {
            videoView = TextureView(container.container.context)
            container.container.addView(videoView, container.viewIndex)
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
            // Exit linking multi-channel, host needs to leave the other channel
            if (isRoomOwner) {
                mRtcEngine.leaveChannelEx(
                    RtcConnection(
                        interactionInfo!!.roomId,
                        UserManager.getInstance().user.id.toInt()
                    )
                )
            } else {
                mPKEventHandler?.let {
                    mRtcEngine.removeHandlerEx(mPKEventHandler, RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()))
                    mPKEventHandler = null
                }
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
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_MIC, true)
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

            mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener(null)
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
        // Start linking
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.linking) return
        val rtcConnection = mMainRtcConnection
        ShowLogger.d(TAG, "Interaction >> updateLinkingMode")

        mBinding.videoLinkingAudienceLayout.userName.text = interactionInfo!!.userName
        mBinding.videoLinkingAudienceLayout.userName.bringToFront()
        mBinding.videoLinkingAudienceLayout.userName.isActivated = !mUserMuteAudioStateMap.getOrDefault(
            interactionInfo!!.userId.toInt(),
            false
        )
        mBinding.videoLinkingAudienceLayout.videoOverlay.isVisible = false
        if (isRoomOwner) {
            // Linking host perspective
            mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                showLinkSettingsDialog()
            }
            enableLocalAudio(true)
            mService.muteAudio(mRoomInfo.roomId, false)
            // pk camera default on todo unified entry to get camera status
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_MIC, true)
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
                        Constants.RENDER_MODE_HIDDEN,
                        interactionInfo?.userId!!.toInt()
                    ),
                    rtcConnection
                )
            }
        } else {
            // Linking audience perspective
            if (interactionInfo?.userId.equals(UserManager.getInstance().user.id.toString())) {
                // Linking audience in the audience channel is not allowed to switch rooms
                onMeLinkingListener?.onMeLinking(true)
                mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                    showLinkSettingsDialog()
                }
                // Re-link, restore camera on state
                mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
                mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_MIC, true)
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
                            // Has permission
                            mRtcEngine.updateChannelMediaOptionsEx(
                                channelMediaOptions,
                                rtcConnection
                            )
                            val context = activity ?: return@toggleSelfVideo
                            val textureView = TextureView(context)
                            mBinding.videoLinkingAudienceLayout.videoContainer.addView(textureView)
                            mRtcEngine.setupLocalVideo(VideoCanvas(
                                textureView
                            ))
                        } else {
                            // No permission
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
                // Other audience perspective
                activity?.let {
                    val view = TextureView(it)
                    mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
                    mBinding.videoLinkingAudienceLayout.videoContainer.addView(view)
                    mRtcEngine.setupRemoteVideoEx(
                        VideoCanvas(
                            view,
                            Constants.RENDER_MODE_HIDDEN,
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

    private fun setMirrorMode(){
        if (isRoomOwner) return
        if (RtcEngineInstance.isFrontCamera){
            RtcEngineInstance.videoEncoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_ENABLED
        }else{
            RtcEngineInstance.videoEncoderConfiguration.mirrorMode = VideoEncoderConfiguration.MIRROR_MODE_TYPE.MIRROR_MODE_AUTO
        }
        mRtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
    }

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
        val ret = mRtcEngine.joinChannelEx(
            RtcEngineInstance.generalToken(),
            pkRtcConnection,
            channelMediaOptions,
            object : IRtcEngineEventHandler() {}
        )
        if (ret == Constants.ERR_JOIN_CHANNEL_REJECTED) {
            mRtcEngine.updateChannelMediaOptionsEx(
                channelMediaOptions,
                pkRtcConnection
            )
        }
        prepareRkRoomId = pkRoomId
    }

    private var pkAgainstView: View? = null
    private fun updatePKingMode() {
        // Start pk
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

            override fun onRemoteVideoStateChanged(
                uid: Int,
                state: Int,
                reason: Int,
                elapsed: Int
            ) {
                super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                ShowLogger.d(TAG, "onRemoteVideoStateChanged pk : uid=$uid, state=$state, reason=$reason")
                if(isPKing() && uid == interactionInfo?.userId?.toInt()){
                    runOnUiThread {
                        if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_MUTED) {
                            mBinding.videoPKLayout.iBroadcasterBViewOverlay.isVisible = true
                        } else if (reason == Constants.REMOTE_VIDEO_STATE_REASON_REMOTE_UNMUTED) {
                            mBinding.videoPKLayout.iBroadcasterBViewOverlay.isVisible = false
                        }
                    }
                }
            }

            override fun onRemoteAudioStateChanged(
                uid: Int,
                state: Int,
                reason: Int,
                elapsed: Int
            ) {
                super.onRemoteAudioStateChanged(uid, state, reason, elapsed)
                ShowLogger.d(TAG, "onRemoteAudioStateChanged pk : uid=$uid, state=$state, reason=$reason")
                if(isPKing() && uid == interactionInfo?.userId?.toInt()){
                    runOnUiThread {
                        if (state == Constants.REMOTE_AUDIO_STATE_STOPPED) {
                            mBinding.videoPKLayout.userNameB.isActivated = false
                        } else if (state == Constants.REMOTE_AUDIO_STATE_DECODING) {
                            mBinding.videoPKLayout.userNameB.isActivated = true
                        }
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

        mBinding.livingComeSoonLayout.root.isVisible = false
        mBinding.videoPKLayout.userNameA.text = mRoomInfo.ownerName
        mBinding.videoPKLayout.userNameA.isActivated = true
        mBinding.videoPKLayout.userNameB.text = interactionInfo!!.userName
        mBinding.videoPKLayout.userNameB.isActivated = true
        mBinding.videoPKLayout.iBroadcasterAViewOverlay.isVisible = false
        mBinding.videoPKLayout.iBroadcasterBViewOverlay.isVisible = false
        if (isRoomOwner) {
            // pk host
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
            mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_MIC, true)
            enableLocalAudio(true)
            if (isRoomOwner) {
                // Linking camera default on todo unified entry to get camera status
                mSettingDialog.resetItemStatus(SettingDialog.ITEM_ID_VIDEO, true)
                mPKSettingsDialog.resetItemStatus(LivePKSettingsDialog.ITEM_ID_CAMERA, true)
                enableLocalVideo(true)
            }
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.autoSubscribeAudio = true
            val pkRtcConnection = RtcConnection(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toInt()
            )

            val ret = mRtcEngine.joinChannelEx(
                RtcEngineInstance.generalToken(),
                pkRtcConnection,
                channelMediaOptions,
                eventListener
            )
            if (ret == Constants.ERR_JOIN_CHANNEL_REJECTED) {
                mRtcEngine.updateChannelMediaOptionsEx(
                    channelMediaOptions,
                    pkRtcConnection
                )
                mPKEventHandler = eventListener
                mRtcEngine.addHandlerEx(eventListener, pkRtcConnection)
            }
            activity?.let {
                mBinding.videoPKLayout.iBroadcasterBView.removeView(pkAgainstView)
                pkAgainstView = TextureView(it)
                mBinding.videoPKLayout.iBroadcasterBView.addView(pkAgainstView, 0)
                mRtcEngine.muteRemoteAudioStreamEx(interactionInfo?.userId!!.toInt(), false, pkRtcConnection)
                mRtcEngine.setupRemoteVideoEx(
                    VideoCanvas(
                        pkAgainstView,
                        Constants.RENDER_MODE_HIDDEN,
                        interactionInfo?.userId!!.toInt(),
                    ),
                    pkRtcConnection
                )
            }
        } else {
            // Audience
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
            mPKEventHandler = eventListener
            mRtcEngine.addHandlerEx(mPKEventHandler, RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()))
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

    // Music-related interfaces
    private var mAudioMxingChannel: RtcConnection? = null
    private var mMediaPlayer: IMediaPlayer? = null
    private var mAudioMixing = false
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
        adjustAudioMixingVolume(VideoSetting.getCurrBroadcastSetting().audio.audioMixingVolume)
        mediaPlayer.setLoopCount(if (cycle >= 0) 0 else Int.MAX_VALUE)
        mAudioMixing = true
        if (!loopbackOnly && mAudioMxingChannel == null) {
            val uid = UserManager.getInstance().user.id.toInt() + 100000
            val channel = RtcConnection(mRoomInfo.roomId, uid)
            mAudioMxingChannel = channel

            val mediaOptions = ChannelMediaOptions()
            mediaOptions.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
            mediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            mediaOptions.publishMediaPlayerId = mediaPlayer.mediaPlayerId
            mediaOptions.publishMediaPlayerAudioTrack = true
            mediaOptions.publishCameraTrack = false
            mediaOptions.autoSubscribeAudio = false
            mediaOptions.autoSubscribeVideo = false
            mediaOptions.enableAudioRecordingOrPlayout = false

            TokenGenerator.generateToken(channel.channelId, channel.localUid.toString(),
                TokenGeneratorType.Token007,
                AgoraTokenType.Rtc,
                success = {
                    ShowLogger.d("RoomListActivity", "generateToken success， uid：${channel.localUid}")
                    if (!mAudioMixing) {
                        return@generateToken
                    }
                    val ret = mRtcEngine.joinChannelEx(
                        it,
                        channel,
                        mediaOptions,
                        object : IRtcEngineEventHandler() {
                            override fun onError(err: Int) {
                                super.onError(err)
                                CustomToast.show("startAudioMixing joinChannelEx onError, error code: $err, ${RtcEngine.getErrorDescription(err)}")
                            }
                        }
                    )
                    if(ret != Constants.ERR_OK){
                        CustomToast.show("startAudioMixing joinChannelEx failed, error code: $ret, ${RtcEngine.getErrorDescription(ret)}")
                    }
                },
                failure = {
                    ShowLogger.e("RoomListActivity", it, "generateToken failure：$it")
                    mAudioMxingChannel = null
                    CustomToast.show(it?.message ?: "generate token failure")
                })
        }
    }

    private fun stopAudioMixing() {
        mAudioMixing = false

        // Stop playback, get the connection corresponding MediaPlayer and stop releasing
        mMediaPlayer?.stop()

        // Stop streaming, use updateChannelMediaOptionEx
        mAudioMxingChannel?.let {
            val options = LeaveChannelOptions()
            options.stopMicrophoneRecording = false
            mRtcEngine.leaveChannelEx(it, options)
            mAudioMxingChannel = null
        }
    }

    private fun adjustAudioMixingVolume(volume: Int) {
        mMediaPlayer?.adjustPlayoutVolume(volume)
        mMediaPlayer?.adjustPublishSignalVolume(volume)
    }
}
package io.agora.scene.show

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.*
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLiveDetailFragmentBinding
import io.agora.scene.show.databinding.ShowLiveDetailMessageItemBinding
import io.agora.scene.show.databinding.ShowLivingEndDialogBinding
import io.agora.scene.show.service.*
import io.agora.scene.show.widget.*
import io.agora.scene.show.widget.link.LiveLinkAudienceSettingsDialog
import io.agora.scene.show.widget.link.LiveLinkDialog
import io.agora.scene.show.widget.link.OnLinkDialogActionListener
import io.agora.scene.show.widget.pk.LivePKDialog
import io.agora.scene.show.widget.pk.LivePKSettingsDialog
import io.agora.scene.show.widget.pk.LiveRoomConfig
import io.agora.scene.show.widget.pk.OnPKDialogActionListener
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class LiveDetailFragment : Fragment() {
    private val TAG = this.toString()

    companion object {

        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun newInstance(roomDetail: ShowRoomDetailModel) = LiveDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_ROOM_DETAIL_INFO, roomDetail)
            }
        }

    }

    val mRoomInfo by lazy { (arguments?.getParcelable(EXTRA_ROOM_DETAIL_INFO) as? ShowRoomDetailModel)!! }
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
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }
    private val mPermissionHelp by lazy { (requireActivity() as? LiveDetailActivity)?.mPermissionHelp!! }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }
    private val mRtcVideoSwitcher by lazy { RtcEngineInstance.videoSwitcher }

    // 当前互动状态
    private var interactionInfo: ShowInteractionInfo? = null
    private var isPKCompetition: Boolean = false
    private var deletedPKInvitation: ShowPKInvitation? = null

    private var mLinkInvitationCountDownLatch: CountDownTimer? = null
    private var mPKInvitationCountDownLatch: CountDownTimer? = null
    private var mPKCountDownLatch: CountDownTimer? = null

    private var isAudioOnlyMode = false

    private val timerRoomEndRun = Runnable {
        if (destroy()) {// 20分钟后自动关播
            showLivingEndLayout()
        }
    }

    private val mMainRtcConnection by lazy { RtcConnection(mRoomInfo.roomId, UserManager.getInstance().user.id.toInt()) }

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
        requireActivity().onBackPressedDispatcher.addCallback(enabled = isVisible) {
            onBackPressed()
        }
        // 需求：打开直播显示
        changeStatisticVisible(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ShowLogger.d(TAG, "Fragment Lifecycle: onDestroyView")
    }

    override fun onResume() {
        super.onResume()
        ShowLogger.d(TAG, "Fragment Lifecycle: onResume")

        if (mRoomInfo.isRobotRoom()) {
            initRtcEngine {
                if (isResumed) {
                    initServiceWithJoinRoom()
                }
            }
        } else {
            val roomLeftTime =
                ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong())
            if (roomLeftTime > 0) {
                mBinding.root.postDelayed(timerRoomEndRun, ROOM_AVAILABLE_DURATION)
                initRtcEngine {
                    if (isResumed) {
                        initServiceWithJoinRoom()
                    }
                }
            }
        }

        startTopLayoutTimer()
    }

    override fun onPause() {
        super.onPause()
        ShowLogger.d(TAG, "Fragment Lifecycle: onPause")
        if (!isRoomOwner) destroy() // onPause
    }

    private fun destroy(): Boolean {
        VideoSetting.resetBroadcastSetting()
        mBinding.root.removeCallbacks(timerRoomEndRun)
        releaseCountdown()
        destroyService()
        return destroyRtcEngine()
    }

    private fun onBackPressed() {
        if (isRoomOwner) {
            showEndRoomDialog()
        } else {
            requireActivity().finish()
        }
    }

    //================== UI Operation ===============

    private fun initView() {
        initLivingEndLayout()
        initTopLayout()
        initBottomLayout()
        initMessageLayout()

        // Render host video
        // initVideoView()
    }

    private fun initVideoView() {
        if (isRoomOwner) {
            mRtcVideoSwitcher.setupLocalVideo(
                VideoSwitcher.VideoCanvasContainer(
                    requireActivity(),
                    mBinding.videoLinkingLayout.videoContainer,
                    0
                )
            )
        } else {
            mRtcVideoSwitcher.setupRemoteVideo(
                mMainRtcConnection,
                VideoSwitcher.VideoCanvasContainer(
                    requireActivity(),
                    mBinding.videoLinkingLayout.videoContainer,
                    mRoomInfo.ownerId.toInt()
                )
            )
        }
    }

    private fun initLivingEndLayout() {
        val livingEndLayout = mBinding.livingEndLayout
        livingEndLayout.root.isVisible = ROOM_AVAILABLE_DURATION < (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt.toLong()) && !isRoomOwner && !mRoomInfo.isRobotRoom()
        livingEndLayout.tvUserName.text = mRoomInfo.ownerName
        Glide.with(this@LiveDetailFragment)
            .load(mRoomInfo.ownerAvatar)
            .error(R.mipmap.show_default_avatar)
            .into(livingEndLayout.ivAvatar)
        livingEndLayout.ivClose.setOnClickListener {
            requireActivity().finish()
        }
    }

    private fun initTopLayout() {
        val topLayout = mBinding.topLayout
        Glide.with(this)
            .load(mRoomInfo.ownerAvatar)
            .error(R.mipmap.show_default_avatar)
            .into(topLayout.ivOwnerAvatar)
        topLayout.tvRoomName.text = mRoomInfo.roomName
        topLayout.tvRoomId.text = getString(R.string.show_room_id, mRoomInfo.roomId)
        topLayout.tvUserCount.text = mRoomInfo.roomUserCount.toString()
        topLayout.ivClose.setOnClickListener { onBackPressed() }
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
        bottomLayout.tvChat.setOnClickListener {
            showMessageInputDialog()
        }
        bottomLayout.ivSetting.setOnClickListener {
            if (interactionInfo != null && interactionInfo!!.interactStatus == ShowInteractionStatus.pking.value && isRoomOwner) {
                showPKSettingsDialog()
            } else {
                showSettingDialog()
            }
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
                    mService.createMicSeatApply({
                        // success
                        mLinkDialog.setOnApplySuccess()
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
                    ColorStateList.valueOf(requireContext().getColor(R.color.grey_7e))
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
                    bottomLayout.ivMusic.isVisible = true
                    bottomLayout.ivBeauty.isVisible = true

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
                    ColorStateList.valueOf(requireContext().getColor(R.color.grey_7e))
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
                mService.sendChatMessage(msg)
                dialog.dismiss()
            }
            .show()
    }

    private fun refreshTopUserCount(count: Int) =
        activity?.runOnUiThread { mBinding.topLayout.tvUserCount.text = count.toString() }

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
        // 上行延迟、下行延迟
        upDelay: Int? = null, downDelay: Int? = null,
        // 上行丢包率、下行丢包率
        upLossPackage: Int? = null, downLossPackage: Int? = null,
        // 上行码率、下行码率
        upBitrate: Int? = null, downBitrate: Int? = null
    ) {
        val topBinding = mBinding.topLayout
        val statisticBinding = topBinding.tlStatistic
        val visible = statisticBinding.isVisible
        if (!visible) {
            return
        }
        // 编码分辨率
        encodeVideoSize?.let { topBinding.tvEncodeResolution.text = getString(R.string.show_statistic_encode_resolution, "${it.width}x${it.height}") }
        // 接收分辨率
        receiveVideoSize?.let { topBinding.tvReceiveResolution.text = getString(R.string.show_statistic_receive_resolution, "${it.width}x${it.height}") }
        // 编码帧率
        encodeFps?.let { topBinding.tvStatisticEncodeFPS.text = getString(R.string.show_statistic_encode_fps, it.toString()) }
        // 接收帧率
        receiveFPS?.let { topBinding.tvStatisticReceiveFPS.text = getString(R.string.show_statistic_receive_fps, it.toString()) }
        // 上行延迟
        upDelay?.let { topBinding.tvStatisticUpDelay.text = getString(R.string.show_statistic_delay, it.toString()) }
        // 下行延迟
        downDelay?.let { topBinding.tvStatisticDownDelay.text = getString(R.string.show_statistic_delay, it.toString()) }
        // 上行丢包率
        upLossPackage?.let { topBinding.tvStatisticUpLossPackage.text = getString(R.string.show_statistic_up_loss_package, it.toString()) }
        // 下行丢包率
        downLossPackage?.let { topBinding.tvStatisticDownLossPackage.text = getString(R.string.show_statistic_down_loss_package, it.toString()) }
        // 上行码率
        upBitrate?.let { topBinding.tvStatisticUpBitrate.text = getString(R.string.show_statistic_bitrate, it.toString()) }
        // 下行码率
        downBitrate?.let { topBinding.tvStatisticDownBitrate.text = getString(R.string.show_statistic_bitrate, it.toString()) }
        // 上行网络
        topBinding.tvStatisticUpNet.isVisible = !isAudioOnlyMode
        upLinkBps?.let { topBinding.tvStatisticUpNet.text = getString(R.string.show_statistic_up_net_speech, (it / 1000).toString()) }
        // 下行网络
        topBinding.tvStatisticDownNet.isVisible = !isAudioOnlyMode
        downLinkBps?.let { topBinding.tvStatisticDownNet.text = getString(R.string.show_statistic_down_net_speech, (it / 1000).toString()) }
    }

    private fun refreshViewDetailLayout(status: Int) {
        when (status) {
            ShowInteractionStatus.idle.value -> {
                if (interactionInfo?.interactStatus == ShowInteractionStatus.onSeat.value) {
                    ToastUtils.showToast(R.string.show_link_is_stopped)
                } else if (interactionInfo?.interactStatus == ShowInteractionStatus.pking.value) {
                    ToastUtils.showToast(R.string.show_pk_is_stopped)
                }

                mBinding.videoLinkingAudienceLayout.root.isVisible = false
                mBinding.videoPKLayout.root.isVisible = false
                mBinding.videoLinkingLayout.root.isVisible = true
            }
            ShowInteractionStatus.onSeat.value -> {
                mBinding.videoPKLayout.root.isVisible = false
                mBinding.videoLinkingLayout.root.isVisible = true
                mBinding.videoLinkingAudienceLayout.root.isVisible = true
            }
            ShowInteractionStatus.pking.value -> {
                mBinding.videoLinkingLayout.root.isVisible = false
                mBinding.videoLinkingAudienceLayout.root.isVisible = false
                mBinding.videoPKLayout.root.isVisible = true
            }
        }
    }

    private fun refreshPKTimeCount() {
        if (interactionInfo != null && interactionInfo!!.interactStatus == ShowInteractionStatus.pking.value) {
            if (mPKCountDownLatch != null) {
                mPKCountDownLatch!!.cancel()
                mPKCountDownLatch = null
            }
            mPKCountDownLatch = object : CountDownTimer(120 * 1000 - 1, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val min: Long = (millisUntilFinished / 1000) / 60
                    val sec: Long = (millisUntilFinished / 1000) % 60
                    mBinding.videoPKLayout.iPKTimeText.text =
                        getString(R.string.show_count_time_for_pk, min, sec)
                }

                override fun onFinish() {
                    mService.stopInteraction(interactionInfo!!)
                }
            }.start()
        } else {
            if (mPKCountDownLatch != null) {
                mPKCountDownLatch!!.cancel()
                mPKCountDownLatch = null
            }
        }
    }

    private fun refreshMicMuteStatus() {
        if (interactionInfo == null) return
        if (interactionInfo!!.interactStatus == ShowInteractionStatus.onSeat.value) {
            mBinding.videoLinkingAudienceLayout.userName.isActivated = !interactionInfo!!.muteAudio
        } else if (interactionInfo!!.interactStatus == ShowInteractionStatus.pking.value) {
            mBinding.videoPKLayout.userNameA.isActivated = !interactionInfo!!.ownerMuteAudio
            mBinding.videoPKLayout.userNameB.isActivated = !interactionInfo!!.muteAudio
        }
    }

    private fun showPermissionLeakDialog(yes: () -> Unit, no: (() -> Unit)? = null) {
        AlertDialog.Builder(requireContext()).apply {
            setMessage(R.string.show_live_perms_leak_tip)
            setCancelable(false)
            setPositiveButton(R.string.show_live_yes) { dialog, _ ->
                dialog.dismiss()
                checkRequirePerms(true, no, yes)
            }
            setNegativeButton(R.string.show_live_no) { dialog, _ ->
                dialog.dismiss()
                if (no == null) {
                    requireActivity().finish()
                } else {
                    no.invoke()
                }
            }
            show()
        }
    }

    private fun insertMessageItem(msg: ShowMessage) = activity?.runOnUiThread {
        mMessageAdapter?.let {
            it.insertLast(msg)
            mBinding.messageLayout.rvMessage.scrollToPosition(it.itemCount - 1)
        }
    }

    private fun showSettingDialog() {
        mSettingDialog.apply {
            setHostView(isRoomOwner || isMeLinking())
            if (isMeLinking()) {
                resetSettingsItem(interactionInfo!!.muteAudio)
            }
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    SettingDialog.ITEM_ID_CAMERA -> mRtcEngine.switchCamera()
                    SettingDialog.ITEM_ID_QUALITY -> showPictureQualityDialog(this)
                    SettingDialog.ITEM_ID_VIDEO -> mRtcEngine.muteLocalVideoStreamEx(!activated, mMainRtcConnection)
                    SettingDialog.ITEM_ID_MIC -> {
                        if (!isRoomOwner) {
                            mService.muteAudio(!activated, interactionInfo!!.userId)
                        } else {
                            enableLocalAudio(activated)
                        }
                    }
                    SettingDialog.ITEM_ID_STATISTIC -> changeStatisticVisible()
                    SettingDialog.ITEM_ID_SETTING -> if (isHostView()) showAdvanceSettingDialog() else AdvanceSettingAudienceDialog(context).show()
                }
            }
            show()
        }
    }

    private fun showAdvanceSettingDialog() {
        AdvanceSettingDialog(requireContext()).apply {
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE, true)
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SEEKBAR_BITRATE, true)
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
        BeautyDialog(requireContext()).apply {
            setBeautyProcessor(mBeautyProcessor)
            show()
        }
    }

    private fun showEndRoomDialog() {
        AlertDialog.Builder(requireContext(), R.style.show_alert_dialog)
            .setTitle(R.string.show_tip)
            .setMessage(R.string.show_live_end_room_or_not)
            .setPositiveButton(R.string.show_setting_confirm) { dialog, id ->
                requireActivity().finish()
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
                    mRtcEngine.stopAudioMixing()
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY -> {
                    mRtcEngine.startAudioMixing("/assets/happy.wav", false, -1)
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_ROMANTIC -> {
                    mRtcEngine.startAudioMixing("/assets/happy.wav", false, -1)
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY2 -> {
                    mRtcEngine.startAudioMixing("/assets/romantic.wav", false, -1)
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
            resetSettingsItem(interactionInfo!!.muteAudio)
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LiveLinkAudienceSettingsDialog.ITEM_ID_MIC -> {
                        mService.muteAudio(!activated, interactionInfo!!.userId)
                    }
                    LiveLinkAudienceSettingsDialog.ITEM_ID_STOP_LINK -> {
                        if (interactionInfo != null) {
                            mService.stopInteraction(interactionInfo!!, {
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

    private fun showLinkingDialog() {
        mLinkDialog.setIsRoomOwner(isRoomOwner)
        mLinkDialog.setLinkDialogActionListener(object : OnLinkDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LiveLinkDialog) {
                mService.getAllMicSeatApplyList({
                    mLinkDialog.setSeatApplyList(interactionInfo, it)
                })
            }

            // 主播点击同意连麦申请
            override fun onAcceptMicSeatApplyChosen(
                dialog: LiveLinkDialog,
                seatApply: ShowMicSeatApply
            ) {
                if (interactionInfo != null) {
                    ToastUtils.showToast(R.string.show_cannot_accept)
                    return
                }
                mService.acceptMicSeatApply(seatApply)
            }

            // 在线用户列表刷新
            override fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog) {
                mService.getAllUserList({
                    val list =
                        it.filter { it.userId != UserManager.getInstance().user.id.toString() }
                    mLinkDialog.setSeatInvitationList(list)
                })
            }

            // 主播邀请用户连麦
            override fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, userItem: ShowUser) {
                if (interactionInfo != null) {
                    ToastUtils.showToast(R.string.show_cannot_invite)
                    return
                }
                mService.createMicSeatInvitation(userItem)
            }

            // 主播或连麦者停止连麦
            override fun onStopLinkingChosen(dialog: LiveLinkDialog) {
                if (interactionInfo != null) {
                    mService.stopInteraction(interactionInfo!!, {
                        // success
                    })
                }
            }

            // 观众撤回连麦申请
            override fun onStopApplyingChosen(dialog: LiveLinkDialog) {
                mService.cancelMicSeatApply { }
            }
        })

        if (!mLinkDialog.isVisible) {
            val ft = childFragmentManager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            mLinkDialog.show(ft, "LinkDialog")
        }
    }

    private fun showInvitationDialog() {
        val dialog = AlertDialog.Builder(requireContext(), R.style.show_alert_dialog).apply {
            setTitle(getString(R.string.show_ask_for_link, mRoomInfo.ownerName))
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                if (mLinkInvitationCountDownLatch != null) {
                    mLinkInvitationCountDownLatch!!.cancel()
                    mLinkInvitationCountDownLatch = null
                }
                mService.acceptMicSeatInvitation()
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                mService.rejectMicSeatInvitation()
                dialog.dismiss()
            }
        }.create()
        dialog.show()
        if (mLinkInvitationCountDownLatch != null) {
            mLinkInvitationCountDownLatch!!.cancel()
            mLinkInvitationCountDownLatch = null
        }
        mLinkInvitationCountDownLatch = object : CountDownTimer(15 * 1000 - 1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text =
                    "取消(" + millisUntilFinished / 1000 + "s)"
            }

            override fun onFinish() {
                mService.rejectMicSeatInvitation()
                dialog.dismiss()
            }
        }.start()
    }

    private fun showPKDialog() {
        mPKDialog.setPKDialogActionListener(object : OnPKDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LivePKDialog) {
                mService.getAllPKUserList({ roomList ->
                    mService.getAllPKInvitationList(true, { invitationList ->
                        mPKDialog.setOnlineBroadcasterList(
                            interactionInfo,
                            roomList,
                            invitationList
                        )
                    })
                })
            }

            override fun onInviteButtonChosen(dialog: LivePKDialog, roomItem: LiveRoomConfig) {
                if (roomItem.isRobotRoom()) {
                    ToastUtils.showToast(context?.getString(R.string.show_tip1))
                    return
                }
                if (isRoomOwner) {
                    mService.createPKInvitation(roomItem.convertToShowRoomDetailModel())
                }
            }

            override fun onStopPKingChosen(dialog: LivePKDialog) {
                mService.stopInteraction(interactionInfo!!)
            }
        })
        if (!mPKDialog.isVisible) {
            val ft = childFragmentManager.beginTransaction()
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            mPKDialog.show(ft, "PKDialog")
        }
    }

    private fun showPKInvitationDialog(name: String) {
        val dialog = AlertDialog.Builder(requireContext(), R.style.show_alert_dialog).apply {
            setTitle(getString(R.string.show_ask_for_pk, name))
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                if (mPKInvitationCountDownLatch != null) {
                    mPKInvitationCountDownLatch!!.cancel()
                    mPKInvitationCountDownLatch = null
                }
                mService.acceptPKInvitation { }
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                mService.rejectPKInvitation { }
                dialog.dismiss()
            }
        }.create()
        dialog.show()
        if (mPKInvitationCountDownLatch != null) {
            mPKInvitationCountDownLatch!!.cancel()
            mPKInvitationCountDownLatch = null
        }
        mPKInvitationCountDownLatch = object : CountDownTimer(15 * 1000 - 1, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text =
                    "取消(" + millisUntilFinished / 1000 + "s)"
            }

            override fun onFinish() {
                mService.rejectPKInvitation { }
                dialog.dismiss()
            }
        }.start()
    }

    private fun showPKSettingsDialog() {
        mPKSettingsDialog.apply {
            resetSettingsItem(interactionInfo!!.ownerMuteAudio)
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LivePKSettingsDialog.ITEM_ID_CAMERA -> mRtcEngine.enableLocalVideo(activated)
                    LivePKSettingsDialog.ITEM_ID_SWITCH_CAMERA -> mRtcEngine.switchCamera()
                    LivePKSettingsDialog.ITEM_ID_MIC -> {
                        mService.muteAudio(!activated, mRoomInfo.ownerId)
                    }
                    LivePKSettingsDialog.ITEM_ID_STOP_PK -> {
                        if (interactionInfo != null) {
                            mService.stopInteraction(interactionInfo!!, {
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

    //================== Service Operation ===============

    private fun initServiceWithJoinRoom() {
        mService.joinRoom(mRoomInfo.roomId, {
            if (isResumed) {
                initService()
            }
        }, {
            if (isResumed && (it is RoomException && it.currRoomNo != mRoomInfo.roomId) && !isRoomOwner) {
                activity?.runOnUiThread {
                    destroy() // 进房Error
                    showLivingEndLayout()
                }
            }
        })
    }

    private fun initService() {
        reFetchUserList()
        mService.subscribeReConnectEvent {
            reFetchUserList()
            reFetchPKInvitationList()
        }
        mService.subscribeCurrRoomEvent { status, _ ->
            if (status == ShowServiceProtocol.ShowSubscribeStatus.deleted) {
                if (destroy()) {// status == ShowServiceProtocol.ShowSubscribeStatus.deleted
                    showLivingEndLayout()
                }
            }
        }
        mService.subscribeUser { status, user ->
            reFetchUserList()
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && user != null) {
                if (user.status == ShowRoomRequestStatus.waitting.value) {
                    if (isRoomOwner) {
                        mLinkDialog.setSeatInvitationItemStatus(
                            ShowUser(
                                user.userId,
                                user.avatar,
                                user.userName,
                                user.status
                            )
                        )
                    } else if (user.userId == UserManager.getInstance().user.id.toString()) {
                        showInvitationDialog()
                    }
                } else {
                    mLinkDialog.setSeatInvitationItemStatus(
                        ShowUser(
                            user.userId,
                            user.avatar,
                            user.userName,
                            user.status
                        )
                    )
                }
            }
        }
        mService.subscribeMessage { _, showMessage ->
            insertMessageItem(showMessage)
        }
        mService.subscribeMicSeatApply { _, _ ->
            mService.getAllMicSeatApplyList({ list ->
                if (isRoomOwner) {
                    mBinding.bottomLayout.vLinkingDot.isVisible =
                        list.any { it.status == ShowRoomRequestStatus.waitting.value }
                }
                mLinkDialog.setSeatApplyList(interactionInfo, list)
            })
        }
        mService.subscribeInteractionChanged { status, info ->
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && info != null) {
                // 开始互动
                if (interactionInfo == null) {
                    if (deletedPKInvitation != null) {
                        mService.stopInteraction(info, {
                            // success
                        })
                        deletedPKInvitation = null
                        return@subscribeInteractionChanged
                    }
                    interactionInfo = info
                    // UI
                    updateVideoSetting()
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
                    updateAudioMuteStatus()
                    refreshMicMuteStatus()
                }
            } else {
                // 停止互动
                // UI
                refreshViewDetailLayout(ShowInteractionStatus.idle.value)
                mLinkDialog.setOnSeatStatus("", null)
                mPKDialog.setPKInvitationItemStatus("", null)
                // RTC
                updateIdleMode()
                interactionInfo = null
                refreshBottomLayout()
                refreshPKTimeCount()
                updateVideoSetting()
            }
        }

        mService.sendChatMessage(getString(R.string.show_live_chat_coming))
        mService.subscribePKInvitationChanged { status, info ->
            mService.getAllPKUserList({ roomList ->
                mService.getAllPKInvitationList(true, { invitationList ->
                    mPKDialog.setOnlineBroadcasterList(interactionInfo, roomList, invitationList)
                })
            })
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && info != null) {
                if (info.status == ShowRoomRequestStatus.waitting.value && info.userId == UserManager.getInstance().user.id.toString()) {
                    isPKCompetition = true
                    showPKInvitationDialog(info.fromName)
                }
            } else {
                if (info != null && info.userId == UserManager.getInstance().user.id.toString()) {
                    deletedPKInvitation = info
                    if (interactionInfo != null) {
                        mService.stopInteraction(interactionInfo!!, {
                            // success
                        })
                        deletedPKInvitation = null
                    }
                }
            }
        }

        mService.getAllInterationList({
            val interactionInfo = it.getOrNull(0)
            this.interactionInfo = interactionInfo
            if (interactionInfo != null && isRoomOwner) {
                mService.stopInteraction(interactionInfo)
            }
            refreshBottomLayout()
            updateVideoSetting()
            if (interactionInfo != null) {
                refreshViewDetailLayout(interactionInfo.interactStatus)
                if (interactionInfo.interactStatus == ShowInteractionStatus.onSeat.value) {
                    updateLinkingMode()
                } else if (interactionInfo.interactStatus == ShowInteractionStatus.pking.value) {
                    updatePKingMode()
                }
            } else {
                refreshViewDetailLayout(ShowInteractionStatus.idle.value)
            }
        })
    }

    private fun reFetchUserList() {
        mService.getAllUserList({
            refreshTopUserCount(it.size)
        })
    }

    private fun reFetchPKInvitationList() {
        mService.getAllPKInvitationList(false, { list ->
            list.forEach {
                if (it.userId == UserManager.getInstance().user.id.toString()
                    && it.status == ShowRoomRequestStatus.waitting.value
                ) {
                    showPKInvitationDialog(it.fromName)
                }
            }
        })
    }

    private fun isMeLinking() =
        isLinking() && interactionInfo?.userId == UserManager.getInstance().user.id.toString()

    private fun isLinking() = (interactionInfo?.interactStatus
        ?: ShowInteractionStatus.idle.value) == ShowInteractionStatus.onSeat.value

    private fun isPKing() = (interactionInfo?.interactStatus
        ?: ShowInteractionStatus.idle.value) == ShowInteractionStatus.pking.value

    private fun destroyService() {
        if (interactionInfo != null &&
            ((interactionInfo!!.interactStatus == ShowInteractionStatus.pking.value) && isRoomOwner)
        ) {
            mService.stopInteraction(interactionInfo!!)
        }
        mService.leaveRoom()
    }

    private fun showLivingEndLayout() {
        if (isRoomOwner) {
            AlertDialog.Builder(requireContext(), R.style.show_alert_dialog)
                .setView(ShowLivingEndDialogBinding.inflate(LayoutInflater.from(requireContext())).apply {
                    Glide.with(this@LiveDetailFragment)
                        .load(mRoomInfo.ownerAvatar)
                        .into(ivAvatar)
                }.root)
                .setCancelable(false)
                .setPositiveButton(R.string.show_living_end_back_room_list) { dialog, _ ->
                    requireActivity().finish()
                    dialog.dismiss()
                }
                .show()
        } else {
            mBinding.livingEndLayout.root.isVisible = true
        }
    }

    //================== RTC Operation ===================

    private fun initRtcEngine(onJoinChannelSuccess: () -> Unit) {
        val eventListener = VideoSwitcher.IChannelEventListener(
            onUserOffline = { uid ->
                if (interactionInfo != null && interactionInfo!!.userId == uid.toString()) {
                    mService.stopInteraction(interactionInfo!!)
                }
            },
            onLocalVideoStateChanged = { state ->
                if (isRoomOwner) {
                    isAudioOnlyMode = state == Constants.LOCAL_VIDEO_STREAM_STATE_STOPPED
                }
            },
            onRemoteVideoStateChanged = { uid, state ->
                if (uid == mRoomInfo.ownerId.toInt()) {
                    isAudioOnlyMode = state == Constants.REMOTE_VIDEO_STATE_STOPPED
                }
            },
            onRtcStats = { stats ->
                if (isRoomOwner) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            upDelay = stats.lastmileDelay,
                            cpuAppUsage = stats.cpuAppUsage,
                            cpuTotalUsage = stats.cpuTotalUsage,
                        )
                    }
                }
            },
            onLocalVideoStats = { stats ->
                if (isRoomOwner) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            upBitrate = stats.sentBitrate,
                            encodeFps = stats.sentFrameRate,
                            upLossPackage = stats.txPacketLossRate,
                            encodeVideoSize = Size(stats.captureFrameWidth, stats.captureFrameHeight)
                        )
                    }
                }
            },
            onLocalAudioStats = { stats ->
                if (isRoomOwner) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            audioBitrate = stats.sentBitrate,
                            audioLossPackage = stats.txPacketLossRate
                        )
                    }
                }
            },
            onRemoteVideoStats = { stats ->
                setEnhance(stats)
                if (stats.uid == mRoomInfo.ownerId.toInt()) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            downBitrate = stats.receivedBitrate,
                            receiveFPS = stats.decoderOutputFrameRate,
                            downLossPackage = stats.packetLossRate,
                            downDelay = stats.delay,
                            receiveVideoSize = Size(stats.width, stats.height)
                        )
                    }
                }
            },
            onRemoteAudioStats = { stats ->
                if (stats.uid == mRoomInfo.ownerId.toInt()) {
                    activity?.runOnUiThread {
                        refreshStatisticInfo(
                            audioBitrate = stats.receivedBitrate,
                            audioLossPackage = stats.audioLossRate,
                        )
                    }
                }
            },
            onUplinkNetworkInfoUpdated = { info ->
                activity?.runOnUiThread {
                    refreshStatisticInfo(
                        upLinkBps = (info.video_encoder_target_bitrate_bps ?: 0) / 1000
                    )
                }
            },
            onDownlinkNetworkInfoUpdated = { info ->
                activity?.runOnUiThread {
                    refreshStatisticInfo(
                        downLinkBps = info.bandwidth_estimation_bps
                    )
                }
            },
            onContentInspectResult = { result ->
                if (result > 1) {
                    ToastUtils.showToast(R.string.show_content)
                }
            },
            onChannelJoined = {
                onJoinChannelSuccess.invoke()
            }
        )

        checkRequirePerms {
            // Render host video
            initVideoView()
            joinChannel(eventListener)
        }

        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "show")
            jsonObject.put("id", UserManager.getInstance().user.id)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module1 = ContentInspectModule()
            module1.interval = 30
            module1.type = CONTENT_INSPECT_TYPE_SUPERVISE
            val module2 = ContentInspectModule()
            module2.interval = 30
            module2.type = CONTENT_INSPECT_TYPE_MODERATION
            contentInspectConfig.modules = arrayOf(module1, module2)
            contentInspectConfig.moduleCount = 2
            mRtcEngine.enableContentInspect(true, contentInspectConfig)
        }
        catch (_: JSONException) {

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
            VideoSetting.RecommendBroadcastSetting.LowDevice1v1, VideoSetting.RecommendBroadcastSetting.MediumDevice1v1, VideoSetting.RecommendBroadcastSetting.HighDevice1v1 -> when (VideoSetting.currAudiencePlaySetting) {
                // 画质增强、高端机
                VideoSetting.AudiencePlaySetting.ENHANCE_HIGH -> {
                    // 1080P
                    if (stats.width == VideoSetting.Resolution.V_1080P.width && stats.height == VideoSetting.Resolution.V_1080P.height) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true;
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.width && stats.height == VideoSetting.Resolution.V_720P.height) {
                        superResolution = VideoSetting.SuperResolution.SR_1_5
                    }
                    // 540P、480P
                    else if ((stats.width == VideoSetting.Resolution.V_540P.width && stats.height == VideoSetting.Resolution.V_540P.height) || (stats.width == VideoSetting.Resolution.V_480P.width && stats.height == VideoSetting.Resolution.V_480P.height)) {
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
                    if (stats.width == VideoSetting.Resolution.V_1080P.width && stats.height == VideoSetting.Resolution.V_1080P.height) {
                        superResolution = VideoSetting.SuperResolution.SR_NONE
                        showTip = true;
                    }
                    // 720P
                    else if (stats.width == VideoSetting.Resolution.V_720P.width && stats.height == VideoSetting.Resolution.V_720P.height) {
                        superResolution = VideoSetting.SuperResolution.SR_1
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
            VideoSetting.RecommendBroadcastSetting.LowDevicePK, VideoSetting.RecommendBroadcastSetting.MediumDevicePK, VideoSetting.RecommendBroadcastSetting.HighDevicePK -> when (VideoSetting.currAudiencePlaySetting) {
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

    private fun destroyRtcEngine(): Boolean {
        return mRtcVideoSwitcher.leaveChannel(mMainRtcConnection)
    }

    private fun enableLocalAudio(enable: Boolean) {
        mRtcEngine.muteLocalAudioStreamEx(!enable, mMainRtcConnection)
        if (enable) {
            VideoSetting.updateBroadcastSetting(
                inEarMonitoring = VideoSetting.getCurrBroadcastSetting().audio.inEarMonitoring
            )
        }
    }

    private fun joinChannel(eventListener: VideoSwitcher.IChannelEventListener) {
        val rtcConnection = mMainRtcConnection ?: return
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId

        AudioModeration.moderationAudio(
            channelName,
            uid,
            AudioModeration.AgoraChannelType.broadcast,
            "show"
        )

        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType =
            if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = true
        channelMediaOptions.publishCameraTrack = isRoomOwner
        channelMediaOptions.publishMicrophoneTrack = isRoomOwner
        mRtcVideoSwitcher.joinChannel(
            rtcConnection,
            channelMediaOptions,
            eventListener
        )
    }

    private fun updateVideoSetting() {
        if (isRoomOwner || isMeLinking()) {
            VideoSetting.updateBroadcastSetting(
                when (interactionInfo?.interactStatus) {
                    ShowInteractionStatus.pking.value -> VideoSetting.LiveMode.PK
                    else -> VideoSetting.LiveMode.OneVOne
                }
            )
        } else {
            VideoSetting.updateAudienceSetting()
        }
    }

    private fun updateAudioMuteStatus() {
        if (interactionInfo == null) return
        if (interactionInfo!!.interactStatus == ShowInteractionStatus.onSeat.value) {
            if (interactionInfo!!.userId == UserManager.getInstance().user.id.toString()) {
                enableLocalAudio(!interactionInfo!!.muteAudio)
            }
        } else if (interactionInfo!!.interactStatus == ShowInteractionStatus.pking.value) {
            if (isRoomOwner) {
                enableLocalAudio(!interactionInfo!!.ownerMuteAudio)
            }
        }
    }

    private fun updateIdleMode() {
        if (interactionInfo?.interactStatus == ShowInteractionStatus.pking.value) {
            // 退出连麦多频道
            mRtcVideoSwitcher.leaveChannel(
                RtcConnection(
                    interactionInfo!!.roomId,
                    UserManager.getInstance().user.id.toInt()
                )
            )
        }

        if (isRoomOwner) {
            enableLocalAudio(true)
            mRtcVideoSwitcher.setupLocalVideo(VideoSwitcher.VideoCanvasContainer(requireActivity(), mBinding.videoLinkingLayout.videoContainer, 0))
        } else {
            val channelMediaOptions = ChannelMediaOptions()
            val rtcConnection = mMainRtcConnection ?: return
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection)
            mRtcVideoSwitcher.setupRemoteVideo(
                rtcConnection,
                VideoSwitcher.VideoCanvasContainer(requireActivity(), mBinding.videoLinkingLayout.videoContainer, mRoomInfo.ownerId.toInt())
            )
        }
    }

    private fun updateLinkingMode() {
        // 开始连麦
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.onSeat.value) return
        val rtcConnection = mMainRtcConnection ?: return

        mBinding.videoLinkingAudienceLayout.userName.text = interactionInfo!!.userName
        mBinding.videoLinkingAudienceLayout.userName.bringToFront()
        mBinding.videoLinkingAudienceLayout.userName.isActivated =
            interactionInfo?.muteAudio?.not() ?: false
        if (isRoomOwner) {
            // 连麦主播视角
            mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                showLinkSettingsDialog()
            }
            enableLocalAudio(true)
            mRtcVideoSwitcher.setupLocalVideo(
                VideoSwitcher.VideoCanvasContainer(
                    requireActivity(),
                    mBinding.videoLinkingLayout.videoContainer,
                    0
                )
            )
            mRtcVideoSwitcher.setupRemoteVideo(
                rtcConnection,
                VideoSwitcher.VideoCanvasContainer(
                    requireActivity(),
                    mBinding.videoLinkingAudienceLayout.videoContainer,
                    interactionInfo?.userId!!.toInt()
                )
            )
        } else {
            // 连麦观众视角
            if (interactionInfo?.userId.equals(UserManager.getInstance().user.id.toString())) {
                mBinding.videoLinkingAudienceLayout.videoContainer.setOnClickListener {
                    showLinkSettingsDialog()
                }
                enableLocalAudio(true)
                val channelMediaOptions = ChannelMediaOptions()
                channelMediaOptions.publishCameraTrack = true
                channelMediaOptions.publishMicrophoneTrack = true
                channelMediaOptions.publishCustomAudioTrack = false
                channelMediaOptions.enableAudioRecordingOrPlayout = true
                channelMediaOptions.autoSubscribeVideo = true
                channelMediaOptions.autoSubscribeAudio = true
                channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                checkRequirePerms(
                    denied = {
                        mService.stopInteraction(interactionInfo!!)
                    },
                    granted = {
                        mRtcEngine.updateChannelMediaOptionsEx(channelMediaOptions, rtcConnection)

                        mRtcVideoSwitcher.setupRemoteVideo(
                            rtcConnection,
                            VideoSwitcher.VideoCanvasContainer(
                                requireActivity(),
                                mBinding.videoLinkingLayout.videoContainer,
                                mRoomInfo.ownerId.toInt()
                            )
                        )
                        mRtcVideoSwitcher.setupLocalVideo(
                            VideoSwitcher.VideoCanvasContainer(
                                requireActivity(),
                                mBinding.videoLinkingAudienceLayout.videoContainer,
                                0
                            )
                        )

                    })
            } else {
                // 其他观众视角
                mRtcVideoSwitcher.setupRemoteVideo(
                    rtcConnection,
                    VideoSwitcher.VideoCanvasContainer(
                        requireActivity(),
                        mBinding.videoLinkingAudienceLayout.videoContainer,
                        interactionInfo?.userId!!.toInt()
                    )
                )
                mRtcVideoSwitcher.setupRemoteVideo(
                    rtcConnection,
                    VideoSwitcher.VideoCanvasContainer(
                        requireActivity(),
                        mBinding.videoLinkingLayout.videoContainer,
                        mRoomInfo.ownerId.toInt()
                    )
                )
            }
        }
    }

    private fun updatePKingMode() {
        // 开始pk
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.pking.value) return
        val rtcConnection = mMainRtcConnection ?: return
        mBinding.videoPKLayout.userNameA.text = mRoomInfo.ownerName
        mBinding.videoPKLayout.userNameA.isActivated = interactionInfo!!.ownerMuteAudio.not()
        mBinding.videoPKLayout.userNameB.text = interactionInfo!!.userName
        mBinding.videoPKLayout.userNameB.isActivated = interactionInfo!!.muteAudio.not()
        if (isRoomOwner) {
            // pk 主播
            mBinding.videoPKLayout.iBroadcasterBView.setOnClickListener {
                showPKSettingsDialog()
            }
            mRtcVideoSwitcher.setupLocalVideo(VideoSwitcher.VideoCanvasContainer(requireActivity(), mBinding.videoPKLayout.iBroadcasterAView, 0, viewIndex = 0))
            enableLocalAudio(true)
            mRtcEngine.enableLocalVideo(true)
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = true
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = false
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            val pkRtcConnection = RtcConnection(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toInt()
            )
            mRtcVideoSwitcher.joinChannel(
                pkRtcConnection, channelMediaOptions, VideoSwitcher.IChannelEventListener()
            )
            mRtcVideoSwitcher.setupRemoteVideo(
                pkRtcConnection,
                VideoSwitcher.VideoCanvasContainer(
                    requireActivity(),
                    mBinding.videoPKLayout.iBroadcasterBView,
                    interactionInfo?.userId!!.toInt(),
                    viewIndex = 0
                )
            )
        } else {
            // 观众
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = false
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            val pkRtcConnection = RtcConnection(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toInt()
            )
            mRtcVideoSwitcher.joinChannel(
                pkRtcConnection, channelMediaOptions, VideoSwitcher.IChannelEventListener()
            )

            mRtcVideoSwitcher.setupRemoteVideo(
                pkRtcConnection,
                VideoSwitcher.VideoCanvasContainer(requireActivity(), mBinding.videoPKLayout.iBroadcasterBView, interactionInfo?.userId!!.toInt(), viewIndex = 0)
            )

            mRtcVideoSwitcher.setupRemoteVideo(
                rtcConnection,
                VideoSwitcher.VideoCanvasContainer(requireActivity(), mBinding.videoPKLayout.iBroadcasterAView, mRoomInfo.ownerId.toInt(), viewIndex = 0)
            )
        }
    }

    private fun checkRequirePerms(
        force: Boolean = false,
        denied: (() -> Unit)? = null,
        granted: () -> Unit
    ) {
        if (!isRoomOwner && !isMeLinking()) {
            granted.invoke()
            return
        }
        mPermissionHelp.checkCameraAndMicPerms(
            {
                mPermissionHelp.checkStoragePerm({
                    granted.invoke()
                }, {
                    showPermissionLeakDialog(granted)
                })
            },
            {
                showPermissionLeakDialog(granted)
            },
            force
        )
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
}
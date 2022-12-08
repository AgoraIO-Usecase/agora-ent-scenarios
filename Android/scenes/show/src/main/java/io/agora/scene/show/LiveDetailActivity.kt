package io.agora.scene.show

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.TextureView
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.*
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.databinding.ShowLiveDetailMessageItemBinding
import io.agora.scene.show.service.*
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.*
import io.agora.scene.show.widget.link.LiveLinkAudienceSettingsDialog
import io.agora.scene.show.widget.link.LiveLinkDialog
import io.agora.scene.show.widget.link.OnLinkDialogActionListener
import io.agora.scene.show.widget.pk.LivePKDialog
import io.agora.scene.show.widget.pk.OnPKDialogActionListener
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil
import java.text.SimpleDateFormat
import java.util.*

class LiveDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context, roomDetail: ShowRoomDetailModel) {
            context.startActivity(Intent(context, LiveDetailActivity::class.java).apply {
                putExtra(EXTRA_ROOM_DETAIL_INFO, roomDetail)
            })
        }
    }

    private val mRoomInfo by lazy { intent.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as ShowRoomDetailModel }
    private val mBinding by lazy { ShowLiveDetailActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mService by lazy { ShowServiceProtocol.getImplInstance() }
    private val isRoomOwner by lazy { mRoomInfo.ownerId == UserManager.getInstance().user.id.toString() }

    private var mMessageAdapter: BindingSingleAdapter<ShowMessage, ShowLiveDetailMessageItemBinding>? =
        null
    private val mMusicEffectDialog by lazy { MusicEffectDialog(this) }
    private val mSettingDialog by lazy { SettingDialog(this) }
    private val mLinkSettingDialog by lazy { LiveLinkAudienceSettingsDialog(this) }
    private val mLinkDialog by lazy { LiveLinkDialog() }
    private val mPKDialog by lazy { LivePKDialog() }
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }
    private lateinit var mPermissionHelp: PermissionHelp
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }
    private var mRtcEngineHandler: IRtcEngineEventHandler? = null

    // 当前互动状态
    private var interactionInfo: ShowInteractionInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        setContentView(mBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPermissionHelp = PermissionHelp(this)
        initView()
        initService()
        initRtcEngine()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyService()
        destroyRtcEngine()
    }

    //================== UI Operation ===============

    private fun initView() {
        initTopLayout()
        initBottomLayout()
        initMessageLayout()
    }

    private fun initTopLayout() {
        val topLayout = mBinding.topLayout
        Glide.with(this)
            .load(mRoomInfo.ownerAvater)
            .into(topLayout.ivOwnerAvatar)
        topLayout.tvRoomName.text = mRoomInfo.roomName
        topLayout.tvRoomId.text = getString(R.string.show_room_id, mRoomInfo.roomId)
        topLayout.tvUserCount.text = mRoomInfo.roomUserCount.toString()
        topLayout.ivClose.setOnClickListener { onBackPressed() }

        // Start Timer counter
        val dataFormat =
            SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT+0") }
        topLayout.tvTimer.post(object : Runnable {
            override fun run() {
                topLayout.tvTimer.text =
                    dataFormat.format(System.currentTimeMillis() - mRoomInfo.createdAt)
                topLayout.tvTimer.postDelayed(this, 1000)
            }
        })

    }

    private fun initBottomLayout() {
        val bottomLayout = mBinding.bottomLayout
        bottomLayout.tvChat.setOnClickListener {
            showMessageInputDialog()
        }
        bottomLayout.ivSetting.setOnClickListener {
            showSettingDialog()
        }
        bottomLayout.ivBeauty.setOnClickListener{
            showBeautyDialog()
        }
        bottomLayout.ivMusic.setOnClickListener {
            showMusicEffectDialog()
        }
        bottomLayout.ivLinking.setOnClickListener {
            ShowLinkingDialog()
        }
        bottomLayout.ivPK.setOnClickListener {
            if (isRoomOwner) {
                ShowPKDialog()
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

            if(isPKing()){
                // PK状态
                // 房主一定是PK的一方
                bottomLayout.ivLinking.isVisible = false
                bottomLayout.ivSetting.isVisible = true
                bottomLayout.ivMusic.isVisible = true
                bottomLayout.ivBeauty.isVisible = true
                bottomLayout.ivPK.isVisible = true
            }
            else if(isLinking()){
                // 连麦状态
                // 房主一定是连麦的一方
                bottomLayout.ivPK.isVisible = false
                bottomLayout.ivLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList = null
            }
            else{
                // 单主播状态
                // 房主是主播
                bottomLayout.ivPK.isVisible = true
                bottomLayout.ivLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList = null
            }

        } else {
            // 观众

            // 观众没有PK权限
            bottomLayout.ivPK.isVisible = false

            if(isPKing()){
                // PK状态
                // PK是房主和房主的事，和观众无关，观众只能看，同时无法再连麦
                bottomLayout.ivSetting.isVisible = false
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false
                bottomLayout.ivLinking.isVisible = false
            }
            else if (isLinking()){
                // 连麦状态
                if (isMeLinking()) {
                    // 连麦中的一方
                    bottomLayout.ivSetting.isVisible = true
                    bottomLayout.ivMusic.isVisible = true
                    bottomLayout.ivBeauty.isVisible = true

                    bottomLayout.ivLinking.isVisible = true
                    bottomLayout.ivLinking.imageTintList = null
                } else {
                    // 只是观看者，不参与连麦
                    bottomLayout.ivSetting.isVisible = false
                    bottomLayout.ivMusic.isVisible = false
                    bottomLayout.ivBeauty.isVisible = false
                    bottomLayout.ivLinking.isVisible = false
                }
            }
            else{
                // 单主播状态
                // 普通观众，只有发起连麦申请的按钮
                bottomLayout.ivSetting.isVisible = false
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false

                bottomLayout.ivLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList =
                    ColorStateList.valueOf(getColor(R.color.grey_7e))
            }
        }
    }

    private fun showMessageInputDialog() {
        TextInputDialog(this).setOnInsertHeightChangeListener {
            mBinding.messageLayout.root.layoutParams =
                (mBinding.messageLayout.root.layoutParams as MarginLayoutParams).apply {
                    bottomMargin = it
                }
        }.setOnSentClickListener { dialog, msg ->
            mService.sendChatMessage(msg)
        }.show()
    }

    private fun refreshTopUserCount(count: Int) =
        runOnUiThread { mBinding.topLayout.tvUserCount.text = count.toString() }

    private fun changeStatisticVisible(){
        val topBinding = mBinding.topLayout
        val visible = !topBinding.tlStatistic.isVisible
        topBinding.tlStatistic.isVisible = visible
        topBinding.ivStatisticClose.isVisible = visible
        refreshStatisticInfo(0, 0, 0, 0, 0, 0)
        topBinding.ivStatisticClose.setOnClickListener {
            topBinding.tlStatistic.isVisible = false
            topBinding.ivStatisticClose.isVisible = false
        }
    }

    private fun refreshStatisticInfo(bitrate: Int? = null, fps: Int? = null, delay: Int? = null,
                                     lossPackage: Int? = null, upLinkBps: Int? = null, downLinkBps: Int? = null){
        val topBinding = mBinding.topLayout
        val statisticBinding = topBinding.tlStatistic
        val visible = statisticBinding.isVisible
        if(!visible){
            return
        }
        bitrate?.let { topBinding.tvStatisticBitrate.text = getString(R.string.show_statistic_bitrate, it.toString()) }
        fps?.let { topBinding.tvStatisticFPS.text = getString(R.string.show_statistic_fps, it.toString()) }
        delay?.let { topBinding.tvStatisticDelay.text = getString(R.string.show_statistic_delay, it.toString()) }
        lossPackage?.let { topBinding.tvStatisticLossPackage.text = getString(R.string.show_statistic_loss_package, it.toString()) }
        upLinkBps?.let { topBinding.tvStatisticUpNet.text = getString(R.string.show_statistic_up_net_speech, (it / 1000).toString()) }
        downLinkBps?.let { topBinding.tvStatisticDownNet.text = getString(R.string.show_statistic_down_net_speech, (it / 1000).toString()) }
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
                    SettingDialog.ITEM_ID_VIDEO -> mRtcEngine.enableLocalVideo(activated)
                    SettingDialog.ITEM_ID_MIC -> mRtcEngine.enableLocalAudio(activated)
                    SettingDialog.ITEM_ID_STATISTIC -> changeStatisticVisible()
                    SettingDialog.ITEM_ID_SETTING -> showAdvanceSettingDialog()
                }
            }
            show()
        }
    }

    private fun showAdvanceSettingDialog() {
        AdvanceSettingDialog(this).apply {
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE, true)
            setOnSwitchChangeListener { _, itemId, isChecked ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SWITCH_EAR_BACK -> {
                        mRtcEngine.enableInEarMonitoring(isChecked)
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_COLOR_ENHANCE -> {
                        mRtcEngine.setColorEnhanceOptions(isChecked, ColorEnhanceOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_DARK_ENHANCE -> {
                        mRtcEngine.setLowlightEnhanceOptions(isChecked, LowLightEnhanceOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE -> {
                        mRtcEngine.setVideoDenoiserOptions(isChecked, VideoDenoiserOptions())
                    }
                    AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE -> {
                        mRtcEngine.setParameters("{\"rtc.video.enable_pvc\":${isChecked}}")
                    }
                }
            }
            setOnSelectorChangeListener { dialog, itemId, selected ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SELECTOR_RESOLUTION -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            val resolution = dialog.getResolution(selected)
                            dimensions = VideoEncoderConfiguration.VideoDimensions(resolution.width, resolution.height)
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                    AdvanceSettingDialog.ITEM_ID_SELECTOR_FRAMERATE -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            frameRate = dialog.getFrameRate(selected)
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                }
            }
            setOnSeekbarChangeListener { _, itemId, value ->
                when (itemId) {
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_BITRATE -> {
                        RtcEngineInstance.videoEncoderConfiguration.apply {
                            bitrate = value
                            mRtcEngine.setVideoEncoderConfiguration(this)
                        }
                    }
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_VOCAL_VOLUME -> {
                        mRtcEngine.adjustRecordingSignalVolume(value)
                    }
                    AdvanceSettingDialog.ITEM_ID_SEEKBAR_MUSIC_VOLUME -> {
                        mRtcEngine.adjustAudioMixingVolume(value)
                    }
                }
            }
            show()
        }
    }

    private fun showPictureQualityDialog(parentDialog: SettingDialog) {
        PictureQualityDialog(this).apply {
            setOnQualitySelectListener { _, _, size ->
                mRtcEngine.setCameraCapturerConfiguration(CameraCapturerConfiguration(
                        CameraCapturerConfiguration.CaptureFormat(size.width,
                            size.height,
                            15)
                    ))
            }

            setOnShowListener { parentDialog.dismiss() }
            setOnDismissListener { parentDialog.show() }
            show()
        }
    }

    private fun showBeautyDialog() {
        BeautyDialog(this).apply {
            setBeautyProcessor(mBeautyProcessor)
            show()
        }
    }

    private fun showMusicEffectDialog(){
        mMusicEffectDialog.setOnItemSelectedListener { musicEffectDialog, itemId ->
            when(itemId){
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_NONE ->{
                    mRtcEngine.stopAudioMixing()
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY ->{
                    mRtcEngine.startAudioMixing("/assets/happy.wav", false, -1)
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_ROMANTIC ->{
                    mRtcEngine.startAudioMixing("/assets/happy.wav", false, -1)
                }
                MusicEffectDialog.ITEM_ID_BACK_MUSIC_JOY2 ->{
                    mRtcEngine.startAudioMixing("/assets/romantic.wav", false, -1)
                }

                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_ORIGINAL ->{
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CONVERSION_OFF)
                }
                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_SWEET ->{
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_SWEET)
                }
                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_ZHONGXIN ->{
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_NEUTRAL)
                }
                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_WENZHONG ->{
                    mRtcEngine.setVoiceConversionPreset(Constants.VOICE_CHANGER_SOLID)
                }
                MusicEffectDialog.ITEM_ID_BEAUTY_VOICE_MOHUAN ->{
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

    private fun ShowLinkSettingsDialog() {
        mLinkSettingDialog.apply {
            setAudienceInfo(interactionInfo!!.userName)
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LiveLinkAudienceSettingsDialog.ITEM_ID_MIC -> mRtcEngine.enableLocalAudio(activated)
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

    private fun ShowLinkingDialog() {
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
                    ToastUtils.showToast("正在互动， 请互动结束后再接受连麦申请");
                    return
                }
                mService.acceptMicSeatApply(seatApply)
            }

            // 在线用户列表刷新
            override fun onOnlineAudienceRefreshing(dialog: LiveLinkDialog) {
                mService.getAllUserList({
                    val list = it.filter { !it.userId.equals(UserManager.getInstance().user.id.toString())  }
                    mLinkDialog.setSeatInvitationList(list)
                })
            }

            // 主播邀请用户连麦
            override fun onOnlineAudienceInvitation(dialog: LiveLinkDialog, user: ShowUser) {
                if (interactionInfo != null) {
                    ToastUtils.showToast("正在互动， 请互动结束后再邀请观众连麦");
                    return
                }
                mService.createMicSeatInvitation(user)
            }

            // 主播或连麦者停止连麦
            override fun onStopLinkingChosen(dialog: LiveLinkDialog) {
                if (interactionInfo != null) {
                    mService.stopInteraction(interactionInfo!!, {
                        // success
                    })
                }
            }

            // 观众发送连麦申请
            override fun onApplyOnSeat(dialog: LiveLinkDialog) {
                if (interactionInfo != null && interactionInfo!!.userId == UserManager.getInstance().user.id.toString()) {
                    ToastUtils.showToast("正在互动， 请互动结束后再申请连麦");
                    return
                }
                mService.createMicSeatApply {  }
            }

            // 观众撤回连麦申请
            override fun onStopApplyingChosen(dialog: LiveLinkDialog) {
                mService.cancelMicSeatApply {  }
            }
        })

        val ft = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        mLinkDialog.show(ft, "LinkDialog")
    }

    private val invitationDialog by lazy {
        AlertDialog.Builder(this, R.style.show_alert_dialog).apply {
            setTitle("主播邀请你加入连麦")
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                mService.acceptMicSeatInvitation()
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                mService.rejectMicSeatInvitation()
                dialog.dismiss()
            }
        }.create()
    }
    private fun ShowInvitationDialog() {
        invitationDialog.show()
    }

    private fun ShowPKDialog() {
        mPKDialog.setLinkDialogActionListener(object : OnPKDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LivePKDialog) {
                mService.getAllPKUserList({
                    mPKDialog.setOnlineBoardcasterList(it)
                })
            }

            override fun onInviteButtonChosen(dialog: LivePKDialog, roomItem: ShowRoomDetailModel) {
                //TODO("Not yet implemented")
            }
        })
        val ft = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        mPKDialog.dialog?.show()
    }


    //================== Service Operation ===============

    private fun initService() {
        mService.subscribeUser { status, user ->
            mService.getAllUserList({
                refreshTopUserCount(it.size)
            })
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && user != null) {
                if (user.status == ShowRoomRequestStatus.waitting.value) {
                    if (isRoomOwner) {
                        mLinkDialog.setSeatInvitationItemStatus(ShowUser(
                            user.userId,
                            user.avatar,
                            user.userName,
                            user.status
                        ))
                    } else if (user.userId.equals(UserManager.getInstance().user.id.toString())) {
                        ShowInvitationDialog()
                    }
                } else {
                    mLinkDialog.setSeatInvitationItemStatus(ShowUser(
                        user.userId,
                        user.avatar,
                        user.userName,
                        user.status
                    ))
                }
            }
        }
        mService.subscribeMessage { _, showMessage ->
            insertMessageItem(showMessage)
        }
        mService.subscribeMicSeatApply { _, _ ->
            mService.getAllMicSeatApplyList({
                mLinkDialog.setSeatApplyList(interactionInfo, it)
            })
        }
        mService.subscribeInteractionChanged { status, info ->
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && info != null ) {
                interactionInfo = info
                refreshBottomLayout()
                mLinkDialog.setOnSeatStatus(info.userName, info.interactStatus)
                if (info.interactStatus == ShowInteractionStatus.onSeat.value) {
                    // 开始连麦
                    val boardcasterVideoView = TextureView(this)
                    val audienceVideoView = TextureView(this)
                    mBinding.videoSinglehostLayout.videoContainer.removeAllViews()
                    mBinding.videoSinglehostLayout.root.isVisible = false
                    mBinding.videoLinkingLayout.root.isVisible = true
                    mBinding.videoLinkingAudienceLayout.root.isVisible = true
                    mBinding.videoLinkingAudienceLayout.root.bringToFront()
                    mBinding.videoLinkingLayout.videoContainer.addView(boardcasterVideoView)
                    mBinding.videoLinkingAudienceLayout.videoContainer.addView(audienceVideoView)
                    if (isRoomOwner) {
                        // 连麦主播视角
                        audienceVideoView.setOnClickListener {
                            // 主播弹出view
                            ShowLinkSettingsDialog()
                        }
                        mRtcEngine.setupLocalVideo(VideoCanvas(boardcasterVideoView))
                        mRtcEngine.setupRemoteVideo(
                            VideoCanvas(
                                audienceVideoView,
                                Constants.RENDER_MODE_HIDDEN,
                                info.userId.toInt()
                            )
                        )
                    } else {
                        // 连麦观众视角
                        if (info.userId.equals(UserManager.getInstance().user.id.toString())) {
                            val channelMediaOptions = ChannelMediaOptions()
                            channelMediaOptions.publishCameraTrack = true;
                            channelMediaOptions.publishMicrophoneTrack = true;
                            channelMediaOptions.publishCustomAudioTrack = false;
                            channelMediaOptions.enableAudioRecordingOrPlayout = true;
                            channelMediaOptions.autoSubscribeVideo = true;
                            channelMediaOptions.autoSubscribeAudio = true;
                            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                            mRtcEngine.updateChannelMediaOptions(channelMediaOptions)
                            mRtcEngine.setupLocalVideo(VideoCanvas(audienceVideoView))
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    boardcasterVideoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    mRoomInfo.ownerId.toInt()
                                )
                            )
                        } else {
                            // 其他观众视角
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    audienceVideoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    info.userId.toInt()
                                )
                            )
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    boardcasterVideoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    mRoomInfo.ownerId.toInt()
                                )
                            )
                        }
                    }
                } else if (info != null && info.interactStatus == ShowInteractionStatus.pking.value) {
                    // TODO PK RTC + UI
                }
            } else {
                // stop 互动
                interactionInfo = null
                mLinkDialog.setOnSeatStatus("", null)
                val boardcasterVideoView = SurfaceView(this)
                mBinding.videoLinkingLayout.videoContainer.removeAllViews()
                mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
                mBinding.videoLinkingLayout.root.isVisible = false
                mBinding.videoLinkingAudienceLayout.root.isVisible = false
                mBinding.videoSinglehostLayout.root.isVisible = true
                mBinding.videoSinglehostLayout.videoContainer.addView(boardcasterVideoView)
                if (isRoomOwner) {
                    mRtcEngine.setupLocalVideo(VideoCanvas(boardcasterVideoView))
                } else {
                    val channelMediaOptions = ChannelMediaOptions()
                    channelMediaOptions.publishCameraTrack = false;
                    channelMediaOptions.publishMicrophoneTrack = false;
                    channelMediaOptions.publishCustomAudioTrack = false;
                    channelMediaOptions.enableAudioRecordingOrPlayout = true;
                    channelMediaOptions.autoSubscribeVideo = true;
                    channelMediaOptions.autoSubscribeAudio = true;
                    channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                    mRtcEngine.updateChannelMediaOptions(channelMediaOptions)
                    mRtcEngine.setupRemoteVideo(
                        VideoCanvas(
                            boardcasterVideoView,
                            Constants.RENDER_MODE_HIDDEN,
                            mRoomInfo.ownerId.toInt()
                        )
                    )
                }
            }
        }
    }

    private fun isMeLinking() = isLinking() && interactionInfo?.userId == UserManager.getInstance().user.id.toString()

    private fun isLinking() = (interactionInfo?.interactStatus ?: ShowInteractionStatus.idle.value) == ShowInteractionStatus.onSeat.value

    private fun isPKing() = (interactionInfo?.interactStatus ?: ShowInteractionStatus.idle.value) == ShowInteractionStatus.pking.value

    private fun destroyService() {
        mService.leaveRoom()
    }


    //================== RTC Operation ===================

    private fun initRtcEngine() {
        mRtcEngine.addHandler(object : IRtcEngineEventHandler() {

            override fun onError(err: Int) {
                super.onError(err)
                ToastUtils.showToast(RtcEngine.getErrorDescription(err))
            }

            override fun onLocalVideoStats(
                source: Constants.VideoSourceType?,
                stats: LocalVideoStats?
            ) {
                super.onLocalVideoStats(source, stats)
                refreshStatisticInfo(
                    bitrate = stats?.encodedBitrate,
                    fps = stats?.encoderOutputFrameRate,
                    lossPackage = stats?.txPacketLossRate
                )
            }

            override fun onUplinkNetworkInfoUpdated(info: UplinkNetworkInfo?) {
                super.onUplinkNetworkInfoUpdated(info)
                refreshStatisticInfo(
                    upLinkBps = info?.video_encoder_target_bitrate_bps
                )
            }

            override fun onDownlinkNetworkInfoUpdated(info: DownlinkNetworkInfo?) {
                super.onDownlinkNetworkInfoUpdated(info)
                refreshStatisticInfo(
                    downLinkBps = info?.bandwidth_estimation_bps,
                    delay = info?.lastmile_buffer_delay_time_ms
                )
            }

        }.apply {
            mRtcEngineHandler = this
        })

        checkRequirePerms {
            joinChannel()
        }
    }

    private fun destroyRtcEngine() {
        mRtcEngine.removeHandler(mRtcEngineHandler)
        mRtcEngine.stopPreview()
        mRtcEngine.leaveChannel()
    }

    private fun joinChannel() {
        val uid = UserManager.getInstance().user.id
        val channelName = mRoomInfo.roomId
        TokenGenerator.generateTokens(
            channelName,
            uid.toString(),
            TokenGenerator.TokenGeneratorType.token006,
            arrayOf(TokenGenerator.AgoraTokenType.rtc),
            {
                val channelMediaOptions = ChannelMediaOptions()
                channelMediaOptions.clientRoleType =
                    if (isRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
                mRtcEngine.joinChannel(
                    it[TokenGenerator.AgoraTokenType.rtc],
                    channelName,
                    uid.toInt(),
                    channelMediaOptions
                )

                mService.getAllInterationList ({
                    val interactionInfo = it.getOrNull(0)
                    if (interactionInfo != null) {
                        if (interactionInfo.interactStatus == ShowInteractionStatus.onSeat.value && !isRoomOwner) {
                            val boardcasterVideoView = TextureView(this)
                            val audienceVideoView = TextureView(this)
                            mBinding.videoSinglehostLayout.root.isVisible = false
                            mBinding.videoLinkingLayout.root.isVisible = true
                            mBinding.videoLinkingAudienceLayout.root.isVisible = true
                            mBinding.videoLinkingAudienceLayout.root.bringToFront()
                            mBinding.videoLinkingLayout.videoContainer.addView(boardcasterVideoView)
                            mBinding.videoLinkingAudienceLayout.videoContainer.addView(audienceVideoView)
                            // 其他观众视角
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    audienceVideoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    interactionInfo.userId.toInt()
                                )
                            )
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    boardcasterVideoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    mRoomInfo.ownerId.toInt()
                                )
                            )
                        }
                    } else {
                        // Render host video
                        val videoView = SurfaceView(this)
                        mBinding.videoSinglehostLayout.videoContainer.addView(videoView)
                        if (isRoomOwner) {
                            mRtcEngine.setupLocalVideo(VideoCanvas(videoView))
                            mRtcEngine.startPreview()
                        } else {
                            mRtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    videoView,
                                    Constants.RENDER_MODE_HIDDEN,
                                    mRoomInfo.ownerId.toInt()
                                )
                            )
                        }
                    }
                })
            })
    }

    private fun checkRequirePerms(force: Boolean = false, granted: () -> Unit) {
        if (!isRoomOwner) {
            granted.invoke()
            return
        }
        mPermissionHelp.checkCameraAndMicPerms(
            {
                granted.invoke()
            },
            {
                showPermissionLeakDialog(granted)
            },
            force
        )
    }

}
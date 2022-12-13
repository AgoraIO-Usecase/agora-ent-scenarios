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
import io.agora.rtc2.*
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.ContentInspectConfig.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.show.databinding.ShowLiveDetailActivityBinding
import io.agora.scene.show.databinding.ShowLiveDetailMessageItemBinding
import io.agora.scene.show.databinding.ShowLivingEndDialogBinding
import io.agora.scene.show.service.*
import io.agora.scene.show.utils.PermissionHelp
import io.agora.scene.show.widget.*
import io.agora.scene.show.widget.link.LiveLinkAudienceSettingsDialog
import io.agora.scene.show.widget.link.LiveLinkDialog
import io.agora.scene.show.widget.link.OnLinkDialogActionListener
import io.agora.scene.show.widget.pk.LivePKDialog
import io.agora.scene.show.widget.pk.LivePKSettingsDialog
import io.agora.scene.show.widget.pk.OnPKDialogActionListener
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.StatusBarUtil
import org.json.JSONException
import org.json.JSONObject
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
    private val mPKSettingsDialog by lazy { LivePKSettingsDialog(this) }
    private val mLinkDialog by lazy { LiveLinkDialog() }
    private val mPKDialog by lazy { LivePKDialog() }
    private val mBeautyProcessor by lazy { RtcEngineInstance.beautyProcessor }
    private lateinit var mPermissionHelp: PermissionHelp
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }
    private var mRtcEngineHandler: IRtcEngineEventHandler? = null

    // 当前互动状态
    private var interactionInfo: ShowInteractionInfo? = null
    private var isPKCompetition: Boolean = false

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

    override fun onBackPressed() {
        if(isRoomOwner){
            showEndRoomDialog()
        }else{
            super.onBackPressed()
        }
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
            bottomLayout.vLinkingDot.isVisible = false
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

            if(isPKing()){
                // PK状态
                // 房主一定是PK的一方
                bottomLayout.flLinking.isVisible = false
                bottomLayout.flPK.isVisible = true
            }
            else if(isLinking()){
                // 连麦状态
                // 房主一定是连麦的一方
                bottomLayout.flPK.isVisible = false
                bottomLayout.flLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList = null
            }
            else{
                // 单主播状态
                // 房主是主播
                bottomLayout.flPK.isVisible = true
                bottomLayout.flLinking.isVisible = true
                bottomLayout.ivLinking.imageTintList = ColorStateList.valueOf(getColor(R.color.grey_7e))
            }

        } else {
            // 观众

            bottomLayout.ivSetting.isVisible = true
            // 观众没有PK权限
            bottomLayout.flPK.isVisible = false


            if(isPKing()){
                // PK状态
                // PK是房主和房主的事，和观众无关，观众只能看，同时无法再连麦
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false
                bottomLayout.flLinking.isVisible = false
            }
            else if (isLinking()){
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
            }
            else{
                // 单主播状态
                // 普通观众，只有发起连麦申请的按钮
                bottomLayout.ivMusic.isVisible = false
                bottomLayout.ivBeauty.isVisible = false

                bottomLayout.flLinking.isVisible = true
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

    private fun refreshViewDetailLayout(status: Int) {
        if (status == ShowInteractionStatus.idle.value) {
            mBinding.videoLinkingLayout.videoContainer.removeAllViews()
            mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
            mBinding.videoLinkingLayout.root.isVisible = false
            mBinding.videoLinkingAudienceLayout.root.isVisible = false
            mBinding.videoPKLayout.root.isVisible = false
            mBinding.videoSinglehostLayout.root.isVisible = true
        } else if (status == ShowInteractionStatus.onSeat.value) {
            mBinding.videoSinglehostLayout.videoContainer.removeAllViews()
            mBinding.videoSinglehostLayout.root.isVisible = false
            mBinding.videoPKLayout.root.isVisible = false
            mBinding.videoLinkingLayout.root.isVisible = true
            mBinding.videoLinkingAudienceLayout.root.isVisible = true
            mBinding.videoLinkingAudienceLayout.root.bringToFront()
        } else if (status == ShowInteractionStatus.pking.value) {
            mBinding.videoSinglehostLayout.videoContainer.removeAllViews()
            mBinding.videoLinkingLayout.videoContainer.removeAllViews()
            mBinding.videoLinkingAudienceLayout.videoContainer.removeAllViews()
            mBinding.videoLinkingLayout.root.isVisible = false
            mBinding.videoLinkingAudienceLayout.root.isVisible = false
            mBinding.videoSinglehostLayout.root.isVisible = false
            mBinding.videoPKLayout.root.isVisible = true
        }
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
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_QUALITY_ENHANCE, true)
            setItemShowTextOnly(AdvanceSettingDialog.ITEM_ID_SWITCH_BITRATE_SAVE, true)
            setItemInvisible(AdvanceSettingDialog.ITEM_ID_SEEKBAR_BITRATE, true)
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

    private fun showEndRoomDialog(){
        AlertDialog.Builder(this, R.style.show_alert_dialog)
            .setTitle(R.string.show_tip)
            .setMessage(R.string.show_live_end_room_or_not)
            .setPositiveButton(R.string.show_setting_confirm){ dialog, id ->
                finish()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.show_setting_cancel){dialog, id ->
                dialog.dismiss()
            }
            .show()
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
                    val list = it.filter { it.userId != UserManager.getInstance().user.id.toString() }
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

            // 观众发送连麦申请
            override fun onApplyOnSeat(dialog: LiveLinkDialog) {
                if (interactionInfo != null && interactionInfo!!.userId == UserManager.getInstance().user.id.toString()) {
                    ToastUtils.showToast(R.string.show_cannot_apply)
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

    private fun showInvitationDialog() {
        AlertDialog.Builder(this, R.style.show_alert_dialog).apply {
            setTitle(R.string.show_ask_for_link)
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                mService.acceptMicSeatInvitation()
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                mService.rejectMicSeatInvitation()
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun showPKDialog() {
        mPKDialog.setLinkDialogActionListener(object : OnPKDialogActionListener {
            override fun onRequestMessageRefreshing(dialog: LivePKDialog) {
                mService.getAllPKUserList({
                    mPKDialog.setOnlineBroadcasterList(it)
                })
            }

            override fun onInviteButtonChosen(dialog: LivePKDialog, roomItem: ShowRoomDetailModel) {
                if (isRoomOwner) {
                    mService.createPKInvitation(roomItem)
                }
            }
        })
        val ft = supportFragmentManager.beginTransaction()
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        mPKDialog.show(ft, "PKDialog")
    }

    private fun showPKInvitationDialog() {
        AlertDialog.Builder(this, R.style.show_alert_dialog).apply {
            setTitle(R.string.show_ask_for_pk)
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                mService.acceptPKInvitation {  }
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                mService.rejectPKInvitation {  }
                dialog.dismiss()
            }
        }.create().show()
    }

    private fun showPKSettingsDialog() {
        mPKSettingsDialog.apply {
            setOnItemActivateChangedListener { _, itemId, activated ->
                when (itemId) {
                    LivePKSettingsDialog.ITEM_ID_CAMERA -> mRtcEngine.enableLocalVideo(activated)
                    LivePKSettingsDialog.ITEM_ID_SWITCH_CAMERA -> mRtcEngine.switchCamera()
                    LivePKSettingsDialog.ITEM_ID_MIC -> mRtcEngine.enableLocalAudio(activated)
                    LivePKSettingsDialog.ITEM_ID_STOP_LINK -> {
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
                        showInvitationDialog()
                    }
                } else {
                    mLinkDialog.setSeatInvitationItemStatus(ShowUser(
                        user.userId,
                        user.avatar,
                        user.userName,
                        user.status
                    ))
                }
            }else if(status == ShowServiceProtocol.ShowSubscribeStatus.deleted && user?.userId == mRoomInfo.ownerId){
                showLivingEndDialog()
            }
        }
        mService.subscribeMessage { _, showMessage ->
            insertMessageItem(showMessage)
        }
        mService.subscribeMicSeatApply { _, _ ->
            mService.getAllMicSeatApplyList({ list->
                if (isRoomOwner) {
                    mBinding.bottomLayout.vLinkingDot.isVisible =
                        list.any { it.status == ShowRoomRequestStatus.waitting.value }
                }
                mLinkDialog.setSeatApplyList(interactionInfo, list)
            })
        }
        mService.subscribeInteractionChanged { status, info ->
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && info != null ) {
                // 开始互动
                interactionInfo = info
                // UI
                updateVideoSetting()
                refreshBottomLayout()
                refreshViewDetailLayout(info.interactStatus)
                mLinkDialog.setOnSeatStatus(info.userName, info.interactStatus)
                // RTC
                updateLinkingMode()
                updatePKingMode()
            } else {
                // 停止互动
                // UI
                refreshViewDetailLayout(ShowInteractionStatus.idle.value)
                updateVideoSetting()
                mLinkDialog.setOnSeatStatus("", null)
                // RTC
                updateIdleMode()
                interactionInfo = null
                refreshBottomLayout()
            }
        }

        mService.sendChatMessage(getString(R.string.show_live_chat_coming))
        mService.subscribePKInvitationChanged { status, info ->
            if (status == ShowServiceProtocol.ShowSubscribeStatus.updated && info != null) {
                if (info.status == ShowRoomRequestStatus.waitting.value && info.userId == UserManager.getInstance().user.id.toString()) {
                    isPKCompetition = true
                    showPKInvitationDialog()
                }
            } else {
                if (interactionInfo != null) {
                    mService.stopInteraction(interactionInfo!!, {
                        // success
                    })
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

    private fun showLivingEndDialog() {
        AlertDialog.Builder(this, R.style.show_alert_dialog)
            .setView(ShowLivingEndDialogBinding.inflate(LayoutInflater.from(this)).apply {
                Glide.with(this@LiveDetailActivity)
                    .load(mRoomInfo.ownerAvater)
                    .into(ivAvatar)
            }.root)
            .setPositiveButton(R.string.show_living_end_back_room_list){ dialog, _ ->
                finish()
                dialog.dismiss()
            }
            .show()
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

            override fun onContentInspectResult(result: Int) {
                super.onContentInspectResult(result)
                if (result > 1) {
                    ToastUtils.showToast(R.string.show_content)
                }
            }

        }.apply {
            mRtcEngineHandler = this
        })

        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            jsonObject.put("userId", UserManager.getInstance().user.id)
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
        } catch (_: JSONException) {

        }

        checkRequirePerms {
            joinChannel()
            if (isRoomOwner) {
                val cacheQualityResolution = PictureQualityDialog.getCacheQualityResolution()
                mRtcEngine.setCameraCapturerConfiguration(
                    CameraCapturerConfiguration(
                        CameraCapturerConfiguration.CaptureFormat(
                            cacheQualityResolution.width,
                            cacheQualityResolution.height,
                            15
                        )
                    )
                )
            }
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
                    this.interactionInfo = interactionInfo
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

    private fun updateVideoSetting(){
        VideoSetting.updateBroadcastSetting(
            when(interactionInfo?.interactStatus){
                ShowInteractionStatus.pking.value -> VideoSetting.LiveMode.PK
                else -> VideoSetting.LiveMode.OneVOne
            }
        )
    }

    private fun updateIdleMode() {
        if (interactionInfo?.interactStatus == ShowInteractionStatus.pking.value) {
            // 退出连麦多频道
            mRtcEngine.leaveChannelEx(RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()))
        }

        val broadcasterVideoView = SurfaceView(this)
        mBinding.videoSinglehostLayout.videoContainer.addView(broadcasterVideoView)
        if (isRoomOwner) {
            mRtcEngine.setupLocalVideo(VideoCanvas(broadcasterVideoView))
        } else {
            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            mRtcEngine.updateChannelMediaOptions(channelMediaOptions)
            mRtcEngine.setupRemoteVideo(
                VideoCanvas(
                    broadcasterVideoView,
                    Constants.RENDER_MODE_HIDDEN,
                    mRoomInfo.ownerId.toInt()
                )
            )
        }
    }

    private fun updateLinkingMode() {
        // 开始连麦
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.onSeat.value) return
        val boardcasterVideoView = TextureView(this)
        val audienceVideoView = TextureView(this)

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
                    interactionInfo?.userId!!.toInt()
                )
            )
        } else {
            // 连麦观众视角
            if (interactionInfo?.userId.equals(UserManager.getInstance().user.id.toString())) {
                val channelMediaOptions = ChannelMediaOptions()
                channelMediaOptions.publishCameraTrack = true
                channelMediaOptions.publishMicrophoneTrack = true
                channelMediaOptions.publishCustomAudioTrack = false
                channelMediaOptions.enableAudioRecordingOrPlayout = true
                channelMediaOptions.autoSubscribeVideo = true
                channelMediaOptions.autoSubscribeAudio = true
                channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
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
                        interactionInfo?.userId!!.toInt()
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
    }

    private fun updatePKingMode() {
        // 开始pk
        if (interactionInfo == null) return
        if (interactionInfo?.interactStatus != ShowInteractionStatus.pking.value) return
        if (isRoomOwner) {
            // pk 主播
            mBinding.videoPKLayout.iBroadcasterBView.setOnClickListener {
                showPKSettingsDialog()
            }

            val view = mBinding.videoPKLayout.iBroadcasterAView
            val competitorView = mBinding.videoPKLayout.iBroadcasterBView
            mRtcEngine.setupLocalVideo(VideoCanvas(view))

            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = true
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            TokenGenerator.generateTokens(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toString(),
                TokenGenerator.TokenGeneratorType.token006,
                arrayOf(TokenGenerator.AgoraTokenType.rtc),
                {
                    mRtcEngine.joinChannelEx(
                        it[TokenGenerator.AgoraTokenType.rtc],
                        RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()),
                        channelMediaOptions,
                        object : IRtcEngineEventHandler() {
                            override fun onJoinChannelSuccess(
                                channel: String?,
                                uid: Int,
                                elapsed: Int
                            ) {
                                super.onJoinChannelSuccess(channel, uid, elapsed)
                                mRtcEngine.setupRemoteVideoEx(
                                    VideoCanvas(
                                        competitorView,
                                        Constants.RENDER_MODE_HIDDEN,
                                        interactionInfo?.userId!!.toInt()
                                    ), RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt())
                                )
                            }
                        }
                    )
                })
        } else {
            // 观众
            val view = mBinding.videoPKLayout.iBroadcasterAView
            val competitorView = mBinding.videoPKLayout.iBroadcasterBView

            val channelMediaOptions = ChannelMediaOptions()
            channelMediaOptions.publishCameraTrack = false
            channelMediaOptions.publishMicrophoneTrack = false
            channelMediaOptions.publishCustomAudioTrack = false
            channelMediaOptions.enableAudioRecordingOrPlayout = true
            channelMediaOptions.autoSubscribeVideo = true
            channelMediaOptions.autoSubscribeAudio = true
            channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            TokenGenerator.generateTokens(
                interactionInfo!!.roomId,
                UserManager.getInstance().user.id.toString(),
                TokenGenerator.TokenGeneratorType.token006,
                arrayOf(TokenGenerator.AgoraTokenType.rtc),
                {
                    mRtcEngine.joinChannelEx(
                        it[TokenGenerator.AgoraTokenType.rtc],
                        RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt()),
                        channelMediaOptions,
                        object : IRtcEngineEventHandler() {
                            override fun onJoinChannelSuccess(
                                channel: String?,
                                uid: Int,
                                elapsed: Int
                            ) {
                                super.onJoinChannelSuccess(channel, uid, elapsed)
                                mRtcEngine.setupRemoteVideoEx(
                                    VideoCanvas(
                                        competitorView,
                                        Constants.RENDER_MODE_HIDDEN,
                                        interactionInfo?.userId!!.toInt()
                                    ), RtcConnection(interactionInfo!!.roomId, UserManager.getInstance().user.id.toInt())
                                )
                            }
                        }
                    )
                })

            mRtcEngine.setupRemoteVideo(
                VideoCanvas(
                    view,
                    Constants.RENDER_MODE_HIDDEN,
                    mRoomInfo.ownerId.toInt()
                )
            )
        }
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
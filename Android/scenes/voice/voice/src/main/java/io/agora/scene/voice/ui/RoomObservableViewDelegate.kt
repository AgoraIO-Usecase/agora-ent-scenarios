package io.agora.scene.voice.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import io.agora.scene.voice.R
import io.agora.scene.voice.bean.*
import io.agora.scene.voice.general.constructor.RoomInfoConstructor
import io.agora.scene.voice.general.constructor.RoomSoundAudioConstructor
import io.agora.scene.voice.model.VoiceRoomLivingViewModel
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.service.*
import io.agora.scene.voice.ui.ainoise.RoomAINSSheetDialog
import io.agora.scene.voice.ui.audiosettings.RoomAudioSettingsSheetDialog
import io.agora.scene.voice.ui.common.CommonFragmentAlertDialog
import io.agora.scene.voice.ui.common.CommonSheetAlertDialog
import io.agora.scene.voice.ui.dialog.RoomContributionAndAudienceSheetDialog
import io.agora.scene.voice.ui.dialog.RoomNoticeSheetDialog
import io.agora.scene.voice.ui.fragment.ChatroomHandsDialog
import io.agora.scene.voice.ui.mic.IRoomMicView
import io.agora.scene.voice.ui.micmanger.RoomMicManagerSheetDialog
import io.agora.scene.voice.ui.soundselection.RoomSocialChatSheetDialog
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionConstructor
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionSheetDialog
import io.agora.scene.voice.ui.spatialaudio.RoomSpatialAudioSheetDialog
import io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView
import io.agora.scene.voice.ui.widget.top.IRoomLiveTopView
import io.agora.scene.voice.annotation.MicClickAction
import io.agora.scene.voice.annotation.MicStatus
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.baseui.interfaces.IParserSource
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.buddy.tool.LogTools.logD
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.tool.ToastTools
import kotlin.random.Random

/**
 * @author create by zhangwei03
 *
 * 房间头部 && 麦位置数据变化代理
 */
class RoomObservableViewDelegate constructor(
    private val activity: FragmentActivity,
    private val roomLivingViewModel: VoiceRoomLivingViewModel,
    private val roomKitBean: RoomKitBean,
    private val iRoomTopView: IRoomLiveTopView, // 头部
    private val iRoomMicView: IRoomMicView, // 麦位
    private val chatPrimaryMenuView: ChatPrimaryMenuView, // 底部
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /**麦位信息，index,rtcUid*/
    private val micMap = mutableMapOf<Int, Int>()

    private var myselfMicInfo: VoiceMicInfoModel? = null

    /**举手dialog*/
    private var handsDialog: ChatroomHandsDialog? = null

    /**申请上麦标志*/
    private var isRequesting: Boolean = false

    fun isOnMic(): Boolean {
        return mySelfIndex() >= 0
    }

    private fun mySelfIndex(): Int {
        return myselfMicInfo?.micIndex ?: -1
    }

    fun mySelfMicStatus(): Int {
        return myselfMicInfo?.micStatus ?: MicStatus.Unknown
    }

    init {
        // 更新公告
        roomLivingViewModel.roomNoticeObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    if (data != true) return
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_notice_posted))
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_notice_posted_error))
                }
            })
        }
        // 打开机器人
        roomLivingViewModel.openBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "robot open：$data".logD()
                    if (data != true) return
                    iRoomMicView.activeBot(true)
                    VoiceBuddyFactory.get().rtcChannelTemp.isUseBot = true
                    roomAudioSettingDialog?.updateBoxCheckBoxView(true)
                    // 创建房间，第⼀次启动机器⼈后播放音效：
                    if (VoiceBuddyFactory.get().rtcChannelTemp.firstActiveBot) {
                        VoiceBuddyFactory.get().rtcChannelTemp.firstActiveBot = false
                        AgoraRtcEngineController.get()
                            .updateEffectVolume(VoiceBuddyFactory.get().rtcChannelTemp.botVolume)
                        RoomSoundAudioConstructor.createRoomSoundAudioMap[roomKitBean.roomType]?.let {
                            AgoraRtcEngineController.get().playMusic(it)
                        }
                    }
                }
            })
        }
        // 关闭机器人
        roomLivingViewModel.closeBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "robot close：$data".logD()
                    if (data != true) return
                    iRoomMicView.activeBot(false)
                    // 关闭机器人，暂停所有音效播放
                    VoiceBuddyFactory.get().rtcChannelTemp.isUseBot = false
                    AgoraRtcEngineController.get().resetMediaPlayer()
                }
            })
        }
        // 机器人音量
        roomLivingViewModel.robotVolumeObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "robotVolume update：$data".logD()
                    data?.let {
                        if (it.second) {
                            VoiceBuddyFactory.get().rtcChannelTemp.botVolume = it.first
                            AgoraRtcEngineController.get().updateEffectVolume(it.first)
                        }
                    }
                }
            })
        }
        // 麦位音量监听
        AgoraRtcEngineController.get().setMicVolumeListener(object : RtcMicVolumeListener() {
            // 更新机器人音量
            override fun onBotVolume(speaker: Int, finished: Boolean) {
                if (finished) {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_None)
                } else {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_Medium)
                }
            }

            override fun onUserVolume(rtcUid: Int, volume: Int) {
//                "onAudioVolumeIndication uid:${rtcUid},volume:${volume}".logD("onUserVolume")
                if (rtcUid == 0) {
                    // 自己,没有关麦
                    val myselfIndex = mySelfIndex()
                    if (myselfIndex >= 0 && !VoiceBuddyFactory.get().rtcChannelTemp.isLocalAudioMute) {
                        iRoomMicView.updateVolume(myselfIndex, volume)
                    }
                } else {
                    val micIndex = findIndexByRtcUid(rtcUid)
                    if (micIndex >= 0) {
                        iRoomMicView.updateVolume(micIndex, volume)
                    }
                }
            }
        })
        // 申请上麦
        roomLivingViewModel.startMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "start mic seat apply:$data".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_submit_sent))
                    chatPrimaryMenuView.setShowHandStatus(false, true)
                    isRequesting = true
                }
            })
        }
        // 取消申请
        roomLivingViewModel.cancelMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "cancel mic seat apply:$data".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_cancel_apply_success))
                    chatPrimaryMenuView.setShowHandStatus(false, false)
                    isRequesting = true
                }
            })
        }
        // 本地禁麦
        roomLivingViewModel.muteMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "mute mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 取消本地禁麦
        roomLivingViewModel.unMuteMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "cancel mute mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unmuted))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 下麦
        roomLivingViewModel.leaveMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "leave mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_off_stage))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 踢用户下麦
        roomLivingViewModel.kickMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "kick mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_kicked_off))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 禁言指定麦位
        roomLivingViewModel.forbidMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "force mute mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 取消禁言指定麦位
        roomLivingViewModel.cancelForbidMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "cancel force mute mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unmuted))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 锁麦
        roomLivingViewModel.lockMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "lock mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_blocked))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 取消锁麦
        roomLivingViewModel.cancelLockMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "cancel lock mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unblocked))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }
            })
        }
        // 用户拒绝申请上麦
        roomLivingViewModel.rejectMicInvitationObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "reject mic invitation：$data".logD()
                }
            })
        }
        // 接受邀请
        roomLivingViewModel.acceptMicSeatInvitationObservable()
            .observe(activity) { response: Resource<VoiceMicInfoModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                    override fun onSuccess(data: VoiceMicInfoModel?) {
                        data?.let {
                            iRoomMicView.onSeatUpdated(it)
                        }
                    }
                })
            }
        // 换麦
        roomLivingViewModel.changeMicObservable().observe(activity) { response: Resource<Map<Int, VoiceMicInfoModel>> ->
            parseResource(response, object : OnResourceParseCallback<Map<Int, VoiceMicInfoModel>>() {
                override fun onSuccess(data: Map<Int, VoiceMicInfoModel>?) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_exchange_mic_success))
                    data?.let {
                        iRoomMicView.onSeatUpdated(it)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_exchange_mic_failed))
                }
            })
        }
        // 榜单
        roomLivingViewModel.giftContributeObservable().observe(activity) { response ->
            parseResource(response, object : OnResourceParseCallback<List<VoiceRankUserModel>>() {
                override fun onSuccess(data: List<VoiceRankUserModel>?) {
                    data?.let {
                        if (activity.isFinishing) return
                        iRoomTopView.onRankMember(it)
                    }
                }
            })
        }
    }

    /**
     * 麦位index,rtcUid
     */
    fun onUpdateMicMap(updateMap: Map<Int, VoiceMicInfoModel>) {
        //临时变量，防止交换麦位时候被移除
        var kvLocalUser: VoiceMicInfoModel? = null
        updateMap.forEach { (index, micInfo) ->
            val rtcUid = micInfo.member?.rtcUid ?: -1
            if (rtcUid > 0) {
                micMap[index] = rtcUid
                // 当前用户在麦位上
                if (rtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                    kvLocalUser = micInfo
                }
            } else {
                val removeRtcUid = micMap.remove(index)
                // 当前用户从麦位移除
                if (removeRtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                    myselfMicInfo = null
                }
            }
        }
        kvLocalUser?.let {
            myselfMicInfo = it
        }
        AgoraRtcEngineController.get().switchRole(mySelfIndex() >= 0)

        if (mySelfMicStatus() == MicStatus.Normal) {
            // 状态正常
            AgoraRtcEngineController.get().enableLocalAudio(false)
        } else {
            // 其他状态
            AgoraRtcEngineController.get().enableLocalAudio(true)
        }
        // 机器人麦位
        updateMap[ConfigConstants.MicConstant.KeyIndex6]?.let {
            VoiceBuddyFactory.get().rtcChannelTemp.isUseBot = it.micStatus == MicStatus.BotActivated
        }
    }

    private fun findIndexByRtcUid(rtcUid: Int): Int {
        micMap.entries.forEach {
            if (it.value == rtcUid) {
                return it.key
            }
        }
        return -1
    }

    /**
     * 房间概要
     */
    fun onRoomModel(voiceRoomModel: VoiceRoomModel) {
        iRoomTopView.onChatroomInfo(voiceRoomModel)
    }

    /**
     * 房间详情
     */
    fun onRoomDetails(voiceRoomInfo: VoiceRoomInfo) {
        val isUseBot = voiceRoomInfo.roomInfo?.userRobot ?: false
        VoiceBuddyFactory.get().rtcChannelTemp.isUseBot = isUseBot
        VoiceBuddyFactory.get().rtcChannelTemp.botVolume =
            voiceRoomInfo.roomInfo?.robotVolume ?: ConfigConstants.RotDefaultVolume

        val ownerUid = voiceRoomInfo.roomInfo?.owner?.userId ?: ""
        voiceRoomInfo.roomInfo?.let { vRoomInfo ->
            iRoomTopView.onChatroomInfo(vRoomInfo)
        }
        voiceRoomInfo.micInfo?.let { micList ->
            val micInfoList: List<VoiceMicInfoModel> =
                RoomInfoConstructor.extendMicInfoList(micList, roomKitBean.roomType, ownerUid)
            micInfoList.forEach { micInfo ->
                micInfo.member?.let { userInfo ->
                    val rtcUid = userInfo.rtcUid
                    val micIndex = micInfo.micIndex
                    if (rtcUid > 0) {
                        // 自己
                        if (rtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                            myselfMicInfo = micInfo
                            VoiceBuddyFactory.get().rtcChannelTemp.isLocalAudioMute =
                                micInfo.micStatus != MicStatus.Normal
                        }
                        micMap[micIndex] = rtcUid
                    }
                }
            }
            iRoomMicView.onInitMic(micInfoList, isUseBot)
        }
        chatPrimaryMenuView.showMicVisible(VoiceBuddyFactory.get().rtcChannelTemp.isLocalAudioMute, isOnMic())
    }

    /**
     * 排行榜
     */
    fun onClickRank(currentItem: Int = 0) {
        val dialog = RoomContributionAndAudienceSheetDialog().apply {
            arguments = Bundle().apply {
                putSerializable(RoomContributionAndAudienceSheetDialog.KEY_ROOM_KIT_BEAN, roomKitBean)
                putInt(RoomContributionAndAudienceSheetDialog.KEY_CURRENT_ITEM, currentItem)
            }
        }
        dialog.show(
            activity.supportFragmentManager, "ContributionAndAudienceSheetDialog"
        )
    }

    /**
     * 公告
     */
    fun onClickNotice(announcement: String) {
        val roomNoticeDialog = RoomNoticeSheetDialog().contentText(announcement).apply {
            arguments = Bundle().apply {
                putSerializable(RoomNoticeSheetDialog.KEY_ROOM_KIT_BEAN, roomKitBean)
            }
        }
        roomNoticeDialog.confirmCallback = { newNotice ->
            roomLivingViewModel.updateAnnouncement(newNotice)
        }
        roomNoticeDialog.show(activity.supportFragmentManager, "roomNoticeSheetDialog")
    }

    /**
     * 音效
     */
    fun onClickSoundSocial(soundSelection: Int, finishBack: () -> Unit) {
        val curSoundSelection = RoomSoundSelectionConstructor.builderCurSoundSelection(activity, soundSelection)
        val socialDialog = RoomSocialChatSheetDialog().titleText(curSoundSelection.soundName)
            .contentText(curSoundSelection.soundIntroduce).customers(curSoundSelection.customer ?: mutableListOf())
        socialDialog.onClickSocialChatListener = object : RoomSocialChatSheetDialog.OnClickSocialChatListener {

            override fun onMoreSound() {
                onSoundSelectionDialog(roomKitBean.soundEffect, finishBack)
            }
        }
        socialDialog.show(activity.supportFragmentManager, "chatroomSocialChatSheetDialog")
    }

    var roomAudioSettingDialog: RoomAudioSettingsSheetDialog? = null

    /**
     * 音效设置
     */
    fun onAudioSettingsDialog(finishBack: () -> Unit) {
        roomAudioSettingDialog = RoomAudioSettingsSheetDialog().apply {
            arguments = Bundle().apply {
                val audioSettingsInfo = RoomAudioSettingsBean(
                    enable = roomKitBean.isOwner,
                    roomType = roomKitBean.roomType,
                    botOpen = VoiceBuddyFactory.get().rtcChannelTemp.isUseBot,
                    botVolume = VoiceBuddyFactory.get().rtcChannelTemp.botVolume,
                    soundSelection = roomKitBean.soundEffect,
                    anisMode = VoiceBuddyFactory.get().rtcChannelTemp.anisMode,
                    spatialOpen = false
                )
                putSerializable(RoomAudioSettingsSheetDialog.KEY_AUDIO_SETTINGS_INFO, audioSettingsInfo)
            }
        }

        roomAudioSettingDialog?.audioSettingsListener =
            object : RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener {

                override fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    roomLivingViewModel.enableRobot(isChecked)
                }

                override fun onBotVolumeChange(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    roomLivingViewModel.updateBotVolume(progress)
                }

                override fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean) {
                    onSoundSelectionDialog(soundSelectionType, finishBack)
                }

                override fun onNoiseSuppression(ainsMode: Int, isEnable: Boolean) {
                    onAINSDialog(ainsMode)
                }

                override fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean) {
                    onSpatialDialog()
                }

            }

        roomAudioSettingDialog?.show(activity.supportFragmentManager, "mtAudioSettings")
    }

    /**
     * 最佳音效选择
     */
    fun onSoundSelectionDialog(soundSelection: Int, finishBack: () -> Unit) {
        RoomSoundSelectionSheetDialog(
            roomKitBean.isOwner,
            object : RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener {
                override fun onSoundEffect(soundSelection: SoundSelectionBean, isCurrentUsing: Boolean) {
                    if (isCurrentUsing) {
                        // 试听音效需要开启机器人
                        if (VoiceBuddyFactory.get().rtcChannelTemp.isUseBot) {
                            RoomSoundAudioConstructor.soundSelectionAudioMap[soundSelection.soundSelectionType]?.let {
                                // 播放最佳音效说明
                                AgoraRtcEngineController.get().playMusic(it)
                            }
                        } else {
                            onBotMicClick(false, activity.getString(R.string.voice_chatroom_open_bot_to_sound_effect))
                        }
                    } else {
                        onExitRoom(
                            activity.getString(R.string.voice_chatroom_prompt),
                            activity.getString(R.string.voice_chatroom_exit_and_create_one),
                            finishBack
                        )
                    }
                }

            }).apply {
            arguments = Bundle().apply {
                putInt(RoomSoundSelectionSheetDialog.KEY_CURRENT_SELECTION, soundSelection)
            }
        }.show(activity.supportFragmentManager, "mtSoundSelection")
    }

    /**
     * AI降噪弹框
     */
    fun onAINSDialog(ainsMode: Int) {
        val ainsDialog = RoomAINSSheetDialog().apply {
            arguments = Bundle().apply {
                putInt(RoomAINSSheetDialog.KEY_AINS_MODE, ainsMode)
                putBoolean(RoomAINSSheetDialog.KEY_IS_ENABLE, roomKitBean.isOwner)
            }
        }
        ainsDialog.anisModeCallback = {
            VoiceBuddyFactory.get().rtcChannelTemp.anisMode = it.anisMode
            AgoraRtcEngineController.get().deNoise(it.anisMode)
            if (roomKitBean.isOwner && VoiceBuddyFactory.get().rtcChannelTemp.isUseBot && VoiceBuddyFactory.get().rtcChannelTemp.firstSwitchAnis) {
                VoiceBuddyFactory.get().rtcChannelTemp.firstSwitchAnis = false

                RoomSoundAudioConstructor.anisIntroduceAudioMap[it.anisMode]?.let { soundAudioList ->
                    // 播放AI 降噪介绍
                    AgoraRtcEngineController.get().playMusic(soundAudioList)
                }
            }
        }
        ainsDialog.anisSoundCallback = { position, ainsSoundBean ->
            "onAINSDialog anisSoundCallback：$ainsSoundBean".logD(TAG)
            if (VoiceBuddyFactory.get().rtcChannelTemp.isUseBot) {
                ainsDialog.updateAnisSoundsAdapter(position, true)
                RoomSoundAudioConstructor.AINSSoundMap[ainsSoundBean.soundType]?.let { soundAudioBean ->
                    val audioUrl =
                        if (ainsSoundBean.soundMode == ConfigConstants.AINSMode.AINS_High) soundAudioBean.audioUrlHigh else soundAudioBean.audioUrl
                    // 试听降噪音效
                    AgoraRtcEngineController.get()
                        .playMusic(soundAudioBean.soundId, audioUrl, soundAudioBean.speakerType)
                }
            } else {
                ainsDialog.updateAnisSoundsAdapter(position, false)
                onBotMicClick(false, activity.getString(R.string.voice_chatroom_open_bot_to_sound_effect))
            }
        }

        ainsDialog.show(activity.supportFragmentManager, "mtAnis")
    }

    /**
     * 空间音频弹框
     */
    fun onSpatialDialog() {
        val spatialAudioSheetDialog = RoomSpatialAudioSheetDialog().apply {
            arguments = Bundle().apply {
                putBoolean(RoomSpatialAudioSheetDialog.KEY_SPATIAL_OPEN, false)
                putBoolean(RoomSpatialAudioSheetDialog.KEY_IS_ENABLED, roomKitBean.isOwner)
            }
        }

        spatialAudioSheetDialog.show(activity.supportFragmentManager, "mtSpatialAudio")
    }

    /**
     * 退出房间
     */
    fun onExitRoom(title: String, content: String, finishBack: () -> Unit) {
        CommonFragmentAlertDialog().titleText(title).contentText(content)
            .leftText(activity.getString(R.string.voice_room_cancel))
            .rightText(activity.getString(R.string.voice_room_confirm))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    finishBack.invoke()
                }
            }).show(activity.supportFragmentManager, "mtCenterDialog")
    }

    /**
     * 点击麦位
     */
    fun onUserMicClick(micInfo: VoiceMicInfoModel) {
        val isMyself = TextUtils.equals(VoiceBuddyFactory.get().getVoiceBuddy().userId(), micInfo.member?.userId)
        if (roomKitBean.isOwner || isMyself) { // 房主或者自己
            val roomMicMangerDialog = RoomMicManagerSheetDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(RoomMicManagerSheetDialog.KEY_MIC_INFO, micInfo)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_OWNER, roomKitBean.isOwner)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_MYSELF, isMyself)
                }
            }
            roomMicMangerDialog.onItemClickListener = object : OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
                            // 房主邀请他人
                            if (data.enable) {
                                showOwnerHandsDialog()
                            } else {
                                ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
                            }
                        }
                        MicClickAction.ForbidMic -> {
                            // 房主禁言其他座位
                            roomLivingViewModel.forbidMic(micInfo.micIndex)
                        }
                        MicClickAction.UnForbidMic -> {
                            // 房主取消禁言其他座位
                            if (data.enable) {
                                roomLivingViewModel.cancelMuteMic(micInfo.micIndex)
                            } else {
                                ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
                            }
                        }
                        MicClickAction.Mute -> {
                            //自己禁言
                            muteLocalAudio(true, micInfo.micIndex)
                        }
                        MicClickAction.UnMute -> {
                            //取消自己禁言
                            muteLocalAudio(false, micInfo.micIndex)
                        }
                        MicClickAction.Lock -> {
                            //房主锁麦
                            roomLivingViewModel.lockMic(micInfo.micIndex)
                        }
                        MicClickAction.UnLock -> {
                            //房主取消锁麦
                            roomLivingViewModel.unLockMic(micInfo.micIndex)
                        }
                        MicClickAction.KickOff -> {
                            //房主踢用户下台
                            roomLivingViewModel.kickOff(micInfo.micIndex)
                        }
                        MicClickAction.OffStage -> {
                            //用户主动下台
                            roomLivingViewModel.leaveMicMic(micInfo.micIndex)
                        }
                    }
                }
            }
            roomMicMangerDialog.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        } else if (micInfo.micStatus == MicStatus.Lock || micInfo.micStatus == MicStatus.LockForceMute) {
            // 座位被锁麦
            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
        } else if ((micInfo.micStatus == MicStatus.Idle || micInfo.micStatus == MicStatus.ForceMute) && micInfo.member == null) {
            val mineMicIndex = iRoomMicView.findMicByUid(VoiceBuddyFactory.get().getVoiceBuddy().userId())
            if (mineMicIndex > 0) {
                // 在麦位上，换麦
                showAlertDialog(activity.getString(R.string.voice_chatroom_exchange_mic),
                    object : CommonSheetAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            roomLivingViewModel.changeMic(mineMicIndex, micInfo.micIndex)
                        }
                    })
            } else {
                if (isRequesting) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_submit_sent))
                } else {
                    showMemberHandsDialog(micInfo.micIndex)
                }
            }
        }
    }

    /**
     * 点击机器人
     */
    fun onBotMicClick(isUserBot: Boolean, content: String) {
        if (isUserBot) {
//            Toast.makeText(activity, "${data.userInfo?.username}", Toast.LENGTH_SHORT).show()
        } else {
            CommonFragmentAlertDialog().titleText(activity.getString(R.string.voice_chatroom_prompt))
                .contentText(content).leftText(activity.getString(R.string.voice_room_cancel))
                .rightText(activity.getString(R.string.voice_room_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        roomLivingViewModel.enableRobot(true)
                    }
                }).show(activity.supportFragmentManager, "botActivatedDialog")
        }
    }

    private fun showAlertDialog(content: String, onClickListener: CommonSheetAlertDialog.OnClickBottomListener) {
        CommonSheetAlertDialog().contentText(content).rightText(activity.getString(R.string.voice_room_confirm))
            .leftText(activity.getString(R.string.voice_room_cancel)).setOnClickListener(onClickListener)
            .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
    }

    /**
     * 自己关麦
     */
    fun muteLocalAudio(mute: Boolean, index: Int = -1) {
        AgoraRtcEngineController.get().enableLocalAudio(mute)
        val micIndex = if (index < 0) mySelfIndex() else index
        if (mute) {
            roomLivingViewModel.muteLocal(micIndex)
        } else {
            roomLivingViewModel.unMuteLocal(micIndex)
        }
    }

    private var updateRankRunnable: Runnable? = null

    // 收到礼物消息
    fun receiveGift(roomId: String) {
        if (updateRankRunnable != null) {
            ThreadManager.getInstance().removeCallbacks(updateRankRunnable)
        }
        val longDelay = Random.nextInt(1000, 10000)
        "receiveGift longDelay：$longDelay".logD(TAG)
        updateRankRunnable = Runnable {
            roomLivingViewModel.fetchGiftContribute()
        }
        ThreadManager.getInstance().runOnMainThreadDelay(updateRankRunnable, longDelay)
    }

    /**收到邀请上麦消息*/
    fun receiveInviteSite(roomId: String, micIndex: Int) {
        CommonFragmentAlertDialog().contentText(activity.getString(R.string.voice_chatroom_mic_anchor_invited_you_on_stage))
            .leftText(activity.getString(R.string.voice_room_decline))
            .rightText(activity.getString(R.string.voice_room_accept))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    roomLivingViewModel.acceptMicSeatInvitation()
                }

                override fun onCancelClick() {
                    roomLivingViewModel.refuseInvite()
                }
            }).show(activity.supportFragmentManager, "CommonFragmentAlertDialog")
    }

    fun subMemberCount() {
        ThreadManager.getInstance().runOnMainThread {
            iRoomTopView.subMemberCount()
        }
    }

    /**接受系统消息*/
    fun receiveSystem(ext: MutableMap<String, String>) {
        ThreadManager.getInstance().runOnMainThread {
            if (ext.containsKey("click_count")) {
                ext["click_count"]?.let {
                    iRoomTopView.onUpdateWatchCount(it.toIntOrNull() ?: -1)
                }
            }
            if (ext.containsKey("member_count")) {
                ext["member_count"]?.let {
                    iRoomTopView.onUpdateMemberCount(it.toIntOrNull() ?: -1)
                }
            }
            if (ext.containsKey("gift_amount")) {
                ext["gift_amount"]?.let {
                    iRoomTopView.onUpdateGiftCount(it.toIntOrNull() ?: -1)
                }
            }
        }
    }

    fun destroy() {
        AgoraRtcEngineController.get().destroy()
    }

    /**房主举手弹框*/
    fun showOwnerHandsDialog() {
        handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
        if (handsDialog == null) {
            handsDialog = ChatroomHandsDialog.newInstance
        }
        handsDialog?.setFragmentListener(object : ChatroomHandsDialog.OnFragmentListener{
            override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                iRoomMicView.onSeatUpdated(voiceMicInfoModel)
            }
        })
        handsDialog?.show(activity.supportFragmentManager, "room_hands")
        chatPrimaryMenuView.setShowHandStatus(true, false)
    }

    /**用户举手举手*/
    fun showMemberHandsDialog(micIndex: Int) {
        CommonSheetAlertDialog().contentText(
            if (isRequesting) activity.getString(R.string.voice_chatroom_cancel_request_speak)
            else activity.getString(R.string.voice_chatroom_request_speak)
        ).rightText(activity.getString(R.string.voice_room_confirm))
            .leftText(activity.getString(R.string.voice_room_cancel))
            .setOnClickListener(object : CommonSheetAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    if (isRequesting) {
                        roomLivingViewModel.cancelMicSeatApply(VoiceBuddyFactory.get().getVoiceBuddy().chatUserName())
                    } else {
                        roomLivingViewModel.startMicSeatApply(micIndex)
                    }
                }
            }).show(activity.supportFragmentManager, "room_hands_apply")
    }

    fun handsCheck(map: Map<String, String>) {
        handsDialog?.check(map)
    }

    fun handsUpdate(index: Int) {
        handsDialog?.update(index)
    }

    fun resetRequest() {
        isRequesting = false
    }
}
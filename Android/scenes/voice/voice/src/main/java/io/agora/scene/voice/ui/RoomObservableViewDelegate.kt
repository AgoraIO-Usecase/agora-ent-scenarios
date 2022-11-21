package io.agora.scene.voice.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.fragment.app.FragmentActivity
import io.agora.scene.voice.bean.*
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.general.constructor.RoomInfoConstructor
import io.agora.scene.voice.ui.ainoise.RoomAINSSheetDialog
import io.agora.scene.voice.ui.audiosettings.RoomAudioSettingsSheetDialog
import io.agora.scene.voice.ui.common.CommonFragmentAlertDialog
import io.agora.scene.voice.ui.common.CommonSheetAlertDialog
import io.agora.scene.voice.ui.dialog.RoomContributionAndAudienceSheetDialog
import io.agora.scene.voice.ui.dialog.RoomNoticeSheetDialog
import io.agora.scene.voice.ui.mic.IRoomMicView
import io.agora.scene.voice.ui.micmanger.RoomMicManagerSheetDialog
import io.agora.scene.voice.ui.soundselection.RoomSocialChatSheetDialog
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionConstructor
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionSheetDialog
import io.agora.scene.voice.ui.widget.top.IRoomLiveTopView
import io.agora.voice.baseui.adapter.OnItemClickListener
import io.agora.voice.baseui.general.callback.OnResourceParseCallback
import io.agora.voice.baseui.general.net.Resource
import io.agora.voice.baseui.interfaces.IParserSource
import io.agora.voice.buddy.tool.ThreadManager
import io.agora.voice.buddy.tool.ToastTools
import io.agora.scene.voice.R
import io.agora.scene.voice.model.VoiceRoomLivingViewModel
import io.agora.scene.voice.rtckit.RtcRoomController
import io.agora.scene.voice.service.VoiceBuddyFactory
import io.agora.scene.voice.service.VoiceRoomModel
import io.agora.voice.buddy.config.ConfigConstants
import io.agora.voice.buddy.tool.LogTools.logE
import io.agora.secnceui.annotation.MicClickAction
import io.agora.secnceui.annotation.MicStatus
import io.agora.scene.voice.ui.spatialaudio.RoomSpatialAudioSheetDialog
import io.agora.voice.network.tools.VRValueCallBack
import io.agora.voice.network.tools.bean.VRGiftBean
import io.agora.voice.network.tools.bean.VRoomInfoBean
import kotlin.random.Random

/**
 * @author create by zhangwei03
 *
 * 房间头部 && 麦位置数据变化代理
 */
class RoomObservableViewDelegate constructor(
    private val activity: FragmentActivity,
    private val roomLivingViewModel:VoiceRoomLivingViewModel,
    private val roomKitBean: RoomKitBean,
    private val iRoomTopView: IRoomLiveTopView, // 头部
    private val iRoomMicView: IRoomMicView, // 麦位
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /**麦位信息，index,rtcUid*/
    private val micMap = mutableMapOf<Int, Int>()

    private var myselfMicInfo: MicInfoBean? = null

    fun isOnMic(): Boolean {
        return mySelfIndex() >= 0
    }

    private fun mySelfIndex(): Int {
        return myselfMicInfo?.index ?: -1
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
                    if (data != true) return
                    RtcRoomController.get().rtcChannelTemp.isUseBot = true
                    roomAudioSettingDialog?.updateBoxCheckBoxView(true)
                    // 创建房间，第⼀次启动机器⼈后播放音效：
                    if (RtcRoomController.get().rtcChannelTemp.firstActiveBot) {
                        RtcRoomController.get().rtcChannelTemp.firstActiveBot = false
                        RtcRoomController.get().updateEffectVolume(RtcRoomController.get().rtcChannelTemp.botVolume)
                        RoomSoundAudioConstructor.createRoomSoundAudioMap[roomKitBean.roomType]?.let {
                            RtcRoomController.get().playMusic(it)
                        }
                    }
                }
            })
        }
        // 关闭机器人
        roomLivingViewModel.closeBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    if (data != true) return
                    // 关闭机器人，暂停所有音效播放
                    RtcRoomController.get().rtcChannelTemp.isUseBot = false
                    RtcRoomController.get().resetMediaPlayer()
                }
            })
        }
        // 机器人音量
        roomLivingViewModel.robotVolumeObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "robotVolume update：$data".logE()
                    data?.let {
                        if (it.second) {
                            RtcRoomController.get().rtcChannelTemp.botVolume = it.first
                            RtcRoomController.get().updateEffectVolume(it.first)
                        }
                    }
                }
            })
        }
        // 麦位音量监听
        RtcRoomController.get().setMicVolumeListener(object : RtcMicVolumeListener() {
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
                    if (myselfIndex >= 0 && !RtcRoomController.get().rtcChannelTemp.isLocalAudioMute) {
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
        // 关麦
        roomLivingViewModel.closeMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "close mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted))
                        }
                    }
                }
            })
        }
        // 取消关麦
        roomLivingViewModel.cancelCloseMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel close mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unmuted))
                        }
                    }
                }
            })
        }
        // 下麦
        roomLivingViewModel.leaveMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "leave mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            // 用户下麦
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_off_stage))
                        }
                    }
                }
            })
        }
        // 禁言指定麦位
        roomLivingViewModel.muteMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "force mute mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted))
                        }
                    }
                }
            })
        }
        // 取消禁言指定麦位
        roomLivingViewModel.cancelMuteMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel force mute mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unmuted))
                        }
                    }
                }
            })
        }
        // 踢用户下麦
        roomLivingViewModel.kickMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "kick mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_kicked_off))
                        }
                    }
                }
            })
        }
        // 用户拒绝申请上麦
        roomLivingViewModel.rejectMicInvitationObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "reject mic invitation：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "reject mic invitation:$data")
                }
            })
        }
        // 锁麦
        roomLivingViewModel.lockMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "lock mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_blocked))
                        }
                    }
                }
            })
        }
        // 取消锁麦
        roomLivingViewModel.cancelLockMicObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    "cancel lock mic：$data".logE()
                    data?.let {
                        if (it.second) {
                            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unblocked))
                        }
                    }
                }
            })
        }
        // 邀请上麦
        roomLivingViewModel.invitationMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "invitation mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "invitation mic:$data")
                }
            })
        }
        // 同意上麦申请
        roomLivingViewModel.applySubmitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "apply submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "apply submit mic:$data")
                }
            })
        }
        // 拒绝上麦申请
        roomLivingViewModel.rejectSubmitMicObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "reject submit mic：$data".logE()
                    if (data != true) return
                    ToastTools.show(activity, "reject submit mic:$data")
                }
            })
        }
    }

    /**
     * 麦位index,rtcUid
     */
    fun onUpdateMicMap(updateMap: Map<Int, MicInfoBean>) {
        //临时变量，防止交换麦位时候被移除
        var kvLocalUser: MicInfoBean? = null
        updateMap.forEach { (index, micInfo) ->
            val rtcUid = micInfo.userInfo?.rtcUid ?: -1
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
        RtcRoomController.get().switchRole(mySelfIndex() >= 0)

        if (mySelfMicStatus() == MicStatus.Normal) {
            // 状态正常
            RtcRoomController.get().enableLocalAudio(false)
        } else {
            // 其他状态
            RtcRoomController.get().enableLocalAudio(true)
        }
        // 机器人麦位
        updateMap[ConfigConstants.MicConstant.KeyIndex6]?.let {
            RtcRoomController.get().rtcChannelTemp.isUseBot = it.micStatus == MicStatus.BotActivated
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

    fun onRoomModel(voiceRoomModel: VoiceRoomModel) {
        val ownerUid = voiceRoomModel.owner?.uid ?: ""
        iRoomTopView.onChatroomInfo(RoomInfoConstructor.voiceRoomModel2UiRoomInfo(voiceRoomModel))
    }

    /**
     * 详情
     */
    fun onRoomDetails(vRoomInfoBean: VRoomInfoBean) {
        val isUseBot = vRoomInfoBean.room?.isUse_robot ?: false
        RtcRoomController.get().rtcChannelTemp.isUseBot = isUseBot
        RtcRoomController.get().rtcChannelTemp.botVolume =
            vRoomInfoBean.room?.robot_volume ?: ConfigConstants.RotDefaultVolume

        val ownerUid = vRoomInfoBean.room?.owner?.uid ?: ""
        vRoomInfoBean.room?.let { vRoomInfo ->
            iRoomTopView.onChatroomInfo(RoomInfoConstructor.serverRoomInfo2UiRoomInfo(vRoomInfo))
        }
        vRoomInfoBean.mic_info?.let { micList ->
            val micInfoList: List<MicInfoBean> =
                RoomInfoConstructor.convertMicUiBean(micList, roomKitBean.roomType, ownerUid)
            micInfoList.forEach { micInfo ->
                micInfo.userInfo?.let { userInfo ->
                    val rtcUid = userInfo.rtcUid
                    val micIndex = micInfo.index
                    if (rtcUid > 0) {
                        // 自己
                        if (rtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
                            myselfMicInfo = micInfo
                            RtcRoomController.get().rtcChannelTemp.isLocalAudioMute =
                                micInfo.micStatus != MicStatus.Normal
                        }
                        micMap[micIndex] = rtcUid
                    }
                }
            }
            iRoomMicView.onInitMic(micInfoList, vRoomInfoBean.room?.isUse_robot ?: false)
        }
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
        val roomNoticeDialog = RoomNoticeSheetDialog()
            .contentText(announcement)
            .apply {
                arguments = Bundle().apply {
                    putSerializable(RoomNoticeSheetDialog.KEY_ROOM_KIT_BEAN, roomKitBean)
                }
            }
        roomNoticeDialog.confirmCallback = { newNotice ->
            roomLivingViewModel.updateRoomNotice(activity, roomKitBean.roomId, newNotice)
        }
        roomNoticeDialog.show(activity.supportFragmentManager, "roomNoticeSheetDialog")
    }

    /**
     * 音效
     */
    fun onClickSoundSocial(soundSelection: Int, finishBack: () -> Unit) {
        val curSoundSelection = RoomSoundSelectionConstructor.builderCurSoundSelection(activity, soundSelection)
        val socialDialog = RoomSocialChatSheetDialog().titleText(curSoundSelection.soundName)
            .contentText(curSoundSelection.soundIntroduce)
            .customers(curSoundSelection.customer ?: mutableListOf())
        socialDialog.onClickSocialChatListener = object :
            RoomSocialChatSheetDialog.OnClickSocialChatListener {

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
                    botOpen = RtcRoomController.get().rtcChannelTemp.isUseBot,
                    botVolume = RtcRoomController.get().rtcChannelTemp.botVolume,
                    soundSelection = roomKitBean.soundEffect,
                    anisMode = RtcRoomController.get().rtcChannelTemp.anisMode,
                    spatialOpen = false
                )
                putSerializable(RoomAudioSettingsSheetDialog.KEY_AUDIO_SETTINGS_INFO, audioSettingsInfo)
            }
        }

        roomAudioSettingDialog?.audioSettingsListener = object :
            RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener {

            override fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                roomLivingViewModel.activeBot(activity, roomKitBean.roomId, isChecked)
            }

            override fun onBotVolumeChange(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                roomLivingViewModel.updateBotVolume(activity, roomKitBean.roomId, progress)
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
        RoomSoundSelectionSheetDialog(roomKitBean.isOwner,
            object : RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener {
                override fun onSoundEffect(soundSelection: SoundSelectionBean, isCurrentUsing: Boolean) {
                    if (isCurrentUsing) {
                        // 试听音效需要开启机器人
                        if (RtcRoomController.get().rtcChannelTemp.isUseBot) {
                            RoomSoundAudioConstructor.soundSelectionAudioMap[soundSelection.soundSelectionType]?.let {
                                // 播放最佳音效说明
                                RtcRoomController.get().playMusic(it)
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
        }
            .show(activity.supportFragmentManager, "mtSoundSelection")
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
            RtcRoomController.get().rtcChannelTemp.anisMode = it.anisMode
            RtcRoomController.get().deNoise(it.anisMode)
            if (roomKitBean.isOwner && RtcRoomController.get().rtcChannelTemp.isUseBot && RtcRoomController.get().rtcChannelTemp.firstSwitchAnis) {
                RtcRoomController.get().rtcChannelTemp.firstSwitchAnis = false

                RoomSoundAudioConstructor.anisIntroduceAudioMap[it.anisMode]?.let { soundAudioList ->
                    // 播放AI 降噪介绍
                    RtcRoomController.get().playMusic(soundAudioList)
                }
            }
        }
        ainsDialog.anisSoundCallback = { position, ainsSoundBean ->
            "onAINSDialog anisSoundCallback：$ainsSoundBean".logE(TAG)
            if (RtcRoomController.get().rtcChannelTemp.isUseBot) {
                ainsDialog.updateAnisSoundsAdapter(position, true)
                RoomSoundAudioConstructor.AINSSoundMap[ainsSoundBean.soundType]?.let { soundAudioBean ->
                    val audioUrl =
                        if (ainsSoundBean.soundMode == ConfigConstants.AINSMode.AINS_High) soundAudioBean.audioUrlHigh else soundAudioBean.audioUrl
                    // 试听降噪音效
                    RtcRoomController.get()
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
        CommonFragmentAlertDialog()
            .titleText(title)
            .contentText(content)
            .leftText(activity.getString(R.string.voice_room_cancel))
            .rightText(activity.getString(R.string.voice_room_confirm))
            .setOnClickListener(object :
                CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    finishBack.invoke()
                }
            })
            .show(activity.supportFragmentManager, "mtCenterDialog")
    }

    /**
     * 点击麦位
     */
    fun onUserMicClick(micInfo: MicInfoBean) {
        val isMyself = TextUtils.equals(VoiceBuddyFactory.get().getVoiceBuddy().userId(), micInfo.userInfo?.userId)
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
                                onRoomViewDelegateListener?.onInvitation(position)
                            } else {
                                ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
                            }
                        }
                        MicClickAction.ForceMute -> {
                            // 房主禁言其他座位
                            roomLivingViewModel.muteMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                        MicClickAction.ForceUnMute -> {
                            // 房主取消禁言其他座位
                            if (data.enable) {
                                roomLivingViewModel.cancelMuteMic(activity, roomKitBean.roomId, micInfo.index)
                            } else {
                                ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
                            }
                        }
                        MicClickAction.Mute -> {
                            //自己禁言
                            muteLocalAudio(true, micInfo.index)
                        }
                        MicClickAction.UnMute -> {
                            //取消自己禁言
                            muteLocalAudio(false, micInfo.index)
                        }
                        MicClickAction.Lock -> {
                            //房主锁麦
                            roomLivingViewModel.lockMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                        MicClickAction.UnLock -> {
                            //房主取消锁麦
                            roomLivingViewModel.cancelLockMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                        MicClickAction.KickOff -> {
                            //房主踢用户下台
                            roomLivingViewModel.kickMic(
                                activity, roomKitBean.roomId, micInfo.userInfo?.userId ?: "", micInfo.index
                            )
                        }
                        MicClickAction.OffStage -> {
                            //用户主动下台
                            roomLivingViewModel.leaveMicMic(activity, roomKitBean.roomId, micInfo.index)
                        }
                    }
                }
            }
            roomMicMangerDialog.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        } else if (micInfo.micStatus == MicStatus.Lock || micInfo.micStatus == MicStatus.LockForceMute) {
            // 座位被锁麦
            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_close_by_host))
        } else if ((micInfo.micStatus == MicStatus.Idle || micInfo.micStatus == MicStatus.ForceMute) && micInfo.userInfo == null) {
            val mineMicIndex = iRoomMicView.findMicByUid(VoiceBuddyFactory.get().getVoiceBuddy().userId())
            if (mineMicIndex > 0)
                showAlertDialog(activity.getString(R.string.voice_chatroom_exchange_mic),
                    object : CommonSheetAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(activity)
                                .exChangeMic(
                                    roomKitBean.roomId,
                                    mineMicIndex,
                                    micInfo.index,
                                    object : VRValueCallBack<Boolean?> {
                                        override fun onSuccess(var1: Boolean?) {
                                            ToastTools.show(
                                                activity,
                                                activity.getString(R.string.voice_chatroom_mic_exchange_mic_success),
                                            )
                                        }

                                        override fun onError(code: Int, desc: String) {
                                            ToastTools.show(
                                                activity,
                                                activity.getString(
                                                    R.string.voice_chatroom_mic_exchange_mic_failed,
                                                    desc
                                                ),
                                            )
                                        }
                                    })
                        }
                    })
            else
                onRoomViewDelegateListener?.onUserClickOnStage(micInfo.index)
        }
    }

    /**
     * 点击机器人
     */
    fun onBotMicClick(isUserBot: Boolean, content: String) {
        if (isUserBot) {
//            Toast.makeText(activity, "${data.userInfo?.username}", Toast.LENGTH_SHORT).show()
        } else {
            CommonFragmentAlertDialog()
                .titleText(activity.getString(R.string.voice_chatroom_prompt))
                .contentText(content)
                .leftText(activity.getString(R.string.voice_room_cancel))
                .rightText(activity.getString(R.string.voice_room_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        roomLivingViewModel.activeBot(activity, roomKitBean.roomId, true)
                    }
                })
                .show(activity.supportFragmentManager, "botActivatedDialog")
        }
    }

    private fun showAlertDialog(content: String, onClickListener: CommonSheetAlertDialog.OnClickBottomListener) {
        CommonSheetAlertDialog()
            .contentText(content)
            .rightText(activity.getString(R.string.voice_room_confirm))
            .leftText(activity.getString(R.string.voice_room_cancel))
            .setOnClickListener(onClickListener)
            .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
    }

    /**
     * 自己关麦
     */
    fun muteLocalAudio(mute: Boolean, index: Int = -1) {
        RtcRoomController.get().enableLocalAudio(mute)
        val micIndex = if (index < 0) mySelfIndex() else index
        if (mute) {
            roomLivingViewModel.closeMic(activity, roomKitBean.roomId, micIndex)
        } else {
            roomLivingViewModel.cancelCloseMic(activity, roomKitBean.roomId, micIndex)
        }
    }

    private var updateRankRunnable: Runnable? = null

    // 收到礼物消息
    fun receiveGift(roomId: String) {
        if (updateRankRunnable != null) {
            ThreadManager.getInstance().removeCallbacks(updateRankRunnable)
        }
        val longDelay = Random.nextInt(1000, 10000)
        "receiveGift longDelay：$longDelay".logE(TAG)
        updateRankRunnable = Runnable {
            io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(activity).getGiftList(roomId, object :
                VRValueCallBack<VRGiftBean> {
                override fun onSuccess(var1: VRGiftBean?) {
                    var1?.ranking_list?.let {
                        val rankList = RoomInfoConstructor.convertServerRankToUiRank(it)
                        if (activity.isFinishing) return
                        ThreadManager.getInstance().runOnMainThread {
                            iRoomTopView.onRankMember(rankList)
                        }
                    }
                }

                override fun onError(var1: Int, var2: String?) {

                }
            })
        }
        ThreadManager.getInstance().runOnMainThreadDelay(updateRankRunnable, longDelay)
    }

    /**收到邀请上麦消息*/
    fun receiveInviteSite(roomId: String, micIndex: Int) {
        CommonFragmentAlertDialog()
            .contentText(activity.getString(R.string.voice_chatroom_mic_anchor_invited_you_on_stage))
            .leftText(activity.getString(R.string.voice_room_decline))
            .rightText(activity.getString(R.string.voice_room_accept))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(activity)
                        .agreeMicInvitation(roomId, micIndex, object :
                            VRValueCallBack<Boolean> {
                            override fun onSuccess(var1: Boolean?) {

                            }

                            override fun onError(var1: Int, var2: String?) {

                            }
                        })
                }

                override fun onCancelClick() {
                    io.agora.scene.voice.general.net.ChatroomHttpManager.getInstance(activity)
                        .rejectMicInvitation(roomId, object :
                            VRValueCallBack<Boolean> {
                            override fun onSuccess(var1: Boolean?) {

                            }

                            override fun onError(var1: Int, var2: String?) {
                            }

                        })
                }
            })
            .show(activity.supportFragmentManager, "CommonFragmentAlertDialog")
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

    var onRoomViewDelegateListener: OnRoomViewDelegateListener? = null

    interface OnRoomViewDelegateListener {

        fun onInvitation(micIndex: Int)

        // 用户点击上台
        fun onUserClickOnStage(micIndex: Int)
    }
}
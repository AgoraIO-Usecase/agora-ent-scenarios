package io.agora.scene.voice.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import io.agora.CallBack
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.imkit.manager.ChatroomCacheManager
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.*
import io.agora.scene.voice.model.annotation.MicClickAction
import io.agora.scene.voice.model.annotation.MicStatus
import io.agora.scene.voice.model.constructor.RoomInfoConstructor
import io.agora.scene.voice.model.constructor.RoomSoundAudioConstructor
import io.agora.scene.voice.model.constructor.RoomSoundSelectionConstructor
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.ui.dialog.*
import io.agora.scene.voice.ui.dialog.common.CommonFragmentAlertDialog
import io.agora.scene.voice.ui.dialog.common.CommonFragmentContentDialog
import io.agora.scene.voice.ui.dialog.common.CommonSheetAlertDialog
import io.agora.scene.voice.ui.dialog.soundcard.SoundCardSettingDialog
import io.agora.scene.voice.ui.dialog.soundcard.SoundPresetTypeDialog
import io.agora.scene.voice.ui.widget.mic.IRoomMicView
import io.agora.scene.voice.ui.widget.primary.ChatPrimaryMenuView
import io.agora.scene.voice.ui.widget.top.IRoomLiveTopView
import io.agora.scene.voice.viewmodel.VoiceRoomLivingViewModel
import io.agora.scene.widget.toast.CustomToast
import io.agora.util.EMLog
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.netkit.OnResourceParseCallback
import io.agora.scene.voice.netkit.Resource
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.widget.utils.UiUtils

/**
 * @author create by zhangwei03
 *
 * Room header && mic location data change proxy
 */
class RoomObservableViewDelegate constructor(
    private val activity: FragmentActivity,
    private val roomLivingViewModel: VoiceRoomLivingViewModel,
    private var voiceRoomModel: VoiceRoomModel,
    private val iRoomTopView: IRoomLiveTopView, // Header
    private val iRoomMicView: IRoomMicView, // Mic
    private val chatPrimaryMenuView: ChatPrimaryMenuView, // Bottom
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /** Mic location data, index, rtcUid */
    private val micMap = mutableMapOf<Int, Int>()

    private var localUserMicInfo: VoiceMicInfoModel? = null

    /** Raise hand dialog */
    private var handsDialog: ChatroomHandsDialog? = null

    /** Mic application flag */
    private var isRequesting: Boolean = false

    private fun localUserIndex(): Int {
        return localUserMicInfo?.micIndex ?: -1
    }

    private var robotDialog: RoomRobotEnableDialog? = null

    init {
        // Update announcement
        roomLivingViewModel.roomNoticeObservable().observe(activity) { response: Resource<Pair<String, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<String, Boolean>>() {
                override fun onSuccess(data: Pair<String, Boolean>?) {
                    if (data?.second != true) return
                    voiceRoomModel.announcement = data.first
                    CustomToast.show(R.string.voice_chatroom_notice_posted)
                }

                override fun onError(code: Int, message: String?) {
                    CustomToast.show( R.string.voice_chatroom_notice_posted_error)
                }
            })
        }
        // Open robot
        roomLivingViewModel.openBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "robot open：$data")
                    if (data != true) return
                    iRoomMicView.activeBot(true)
                    voiceRoomModel.useRobot = true
                    roomAudioSettingDialog?.apply {
                        audioSettingsInfo.botOpen = true
                        updateBotStateView()
                    }
                    // When creating room, play sound effect after first robot activation
                    if (VoiceCenter.rtcChannelTemp.firstActiveBot) {
                        VoiceCenter.rtcChannelTemp.firstActiveBot = false
                        AgoraRtcEngineController.get()
                            .updateEffectVolume(voiceRoomModel.robotVolume)
                        RoomSoundAudioConstructor.createRoomSoundAudioMap[ConfigConstants.RoomType.Common_Chatroom]?.let {
                            AgoraRtcEngineController.get().playMusic(it)
                        }
                    }
                }
            })
        }
        // Close robot
        roomLivingViewModel.closeBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "robot close：$data")
                    if (data != true) return
                    iRoomMicView.activeBot(false)
                    // When closing robot, pause all sound effects
                    voiceRoomModel.useRobot = false
                    AgoraRtcEngineController.get().resetMediaPlayer()
                }
            })
        }
        // Robot volume
        roomLivingViewModel.robotVolumeObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    VoiceLogger.d(TAG, "robotVolume update：$data")
                    data?.let {
                        if (it.second) {
                            voiceRoomModel.robotVolume = it.first
                            AgoraRtcEngineController.get().updateEffectVolume(it.first)
                        }
                    }
                }
            })
        }
        // Mic volume listener
        AgoraRtcEngineController.get().setMicVolumeListener(object : RtcMicVolumeListener() {
            // Update robot volume
            override fun onBotVolume(speaker: Int, finished: Boolean) {
                if (finished) {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_None)
                } else {
                    iRoomMicView.updateBotVolume(speaker, ConfigConstants.VolumeType.Volume_Medium)
                }
            }

            override fun onUserVolume(rtcUid: Int, volume: Int) {
                if (rtcUid == 0) {
                    // Myself, not muted
                    val myselfIndex = localUserIndex()
                    if (myselfIndex >= 0) {
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
        // Apply for mic
        roomLivingViewModel.startMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "start mic seat apply:$data")
                    CustomToast.show( R.string.voice_chatroom_mic_submit_sent)
                    chatPrimaryMenuView.setShowHandStatus(false, true)
                    isRequesting = true
                }
            })
        }
        // Cancel application
        roomLivingViewModel.cancelMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "cancel mic seat apply:$data")
                    CustomToast.show(R.string.voice_chatroom_mic_cancel_apply_success)
                    chatPrimaryMenuView.setShowHandStatus(false, false)
                    isRequesting = false
                }
            })
        }
        // Local mute
        roomLivingViewModel.muteMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "mute mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_muted)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Cancel local mute
        roomLivingViewModel.unMuteMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "cancel mute mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_unmuted)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Leave mic
        roomLivingViewModel.leaveMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "leave mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_off_stage)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Kick user off mic
        roomLivingViewModel.kickMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "kick mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_kicked_off)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Mute specified mic
        roomLivingViewModel.forbidMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "force mute mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_muted)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Cancel mute specified mic
        roomLivingViewModel.cancelForbidMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "cancel force mute mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_unmuted)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Lock mic
        roomLivingViewModel.lockMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "lock mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_blocked)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // Cancel lock mic
        roomLivingViewModel.cancelLockMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    VoiceLogger.d(TAG, "cancel lock mic：${data?.micIndex}")
                    CustomToast.show(R.string.voice_chatroom_mic_unblocked)
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // User rejects mic application
        roomLivingViewModel.rejectMicInvitationObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "reject mic invitation：$data")
                }
            })
        }
        // Accept invitation
        roomLivingViewModel.acceptMicSeatInvitationObservable()
            .observe(activity) { response: Resource<VoiceMicInfoModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                    override fun onSuccess(data: VoiceMicInfoModel?) {
                        data?.let {
                            val newMicMap = mutableMapOf(it.micIndex to it)
                            dealMicDataMap(newMicMap)
                            updateViewByMicMap(newMicMap)
                        }
                    }
                })
            }
        // Change mic
        roomLivingViewModel.changeMicObservable().observe(activity) { response: Resource<Map<Int, VoiceMicInfoModel>> ->
            parseResource(response, object : OnResourceParseCallback<Map<Int, VoiceMicInfoModel>>() {
                override fun onSuccess(data: Map<Int, VoiceMicInfoModel>?) {
                    CustomToast.show(R.string.voice_chatroom_mic_exchange_mic_success)
                    data?.let {
                        // Update mic
                        dealMicDataMap(it)
                        updateViewByMicMap(it)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    CustomToast.show(R.string.voice_chatroom_mic_exchange_mic_failed)
                }
            })
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
     * Room details
     */
    fun onRoomDetails(voiceRoomInfo: VoiceRoomInfo) {
        voiceRoomInfo.roomInfo?.let { vRoomInfo ->
            this.voiceRoomModel = vRoomInfo
            iRoomTopView.onChatroomInfo(vRoomInfo)
        }
        if (!voiceRoomInfo.micInfo.isNullOrEmpty()) {
            // Mic data is not empty
            voiceRoomInfo.micInfo?.let { micList ->
                val micInfoList: List<VoiceMicInfoModel> =
                    RoomInfoConstructor.extendMicInfoList(micList, voiceRoomModel.owner?.userId ?: "")
                micInfoList.forEach { micInfo ->
                    micInfo.member?.let { userInfo ->
                        val rtcUid = userInfo.rtcUid
                        val micIndex = micInfo.micIndex
                        if (rtcUid > 0) {
                            // Self
                            if (rtcUid == VoiceCenter.rtcUid) {
                                localUserMicInfo = micInfo
                            }
                            micMap[micIndex] = rtcUid
                        }
                    }
                }
                iRoomMicView.onInitMic(micInfoList, voiceRoomModel.useRobot)
            }
        }
        val isOn = (localUserMicInfo?.member?.micStatus == 1 &&
                localUserMicInfo?.micStatus == MicStatus.Normal)
        val onStage = localUserIndex() >= 0
        chatPrimaryMenuView.showMicVisible(onStage, isOn)
        AgoraRtcEngineController.get().earBackManager()?.setForbidden(!onStage)
        AgoraRtcEngineController.get().soundCardManager()?.setForbidden(!onStage)
    }

    /**
     * Ranking
     */
    fun onClickRank(currentItem: Int = 0) {
        val dialog = RoomContributionAndAudienceSheetDialog().apply {
            arguments = Bundle().apply {
                putSerializable(RoomContributionAndAudienceSheetDialog.KEY_VOICE_ROOM_MODEL, voiceRoomModel)
                putInt(RoomContributionAndAudienceSheetDialog.KEY_CURRENT_ITEM, currentItem)
            }
        }
        dialog.show(
            activity.supportFragmentManager, "ContributionAndAudienceSheetDialog"
        )
    }

    /**
     * Announcement
     */
    fun onClickNotice() {
        var announcement = voiceRoomModel.announcement
        if (announcement.isEmpty()) {
            announcement = activity.getString(R.string.voice_voice_voice_chatroom_first_enter_room_notice_tips)
        }
        val roomNoticeDialog = RoomNoticeSheetDialog().contentText(announcement).apply {
            arguments = Bundle().apply {
                putSerializable(RoomNoticeSheetDialog.KEY_VOICE_ROOM_INFO, voiceRoomModel)
            }
        }
        roomNoticeDialog.confirmCallback = { newNotice ->
            roomLivingViewModel.updateAnnouncement(newNotice)
        }
        roomNoticeDialog.show(activity.supportFragmentManager, "roomNoticeSheetDialog")
    }

    /**
     * Sound effect
     */
    fun onClickSoundSocial(soundSelection: Int, finishBack: () -> Unit) {
        val curSoundSelection = RoomSoundSelectionConstructor.builderCurSoundSelection(activity, soundSelection)
        val socialDialog = RoomSocialChatSheetDialog().titleText(curSoundSelection.soundName)
            .contentText(curSoundSelection.soundIntroduce).customers(curSoundSelection.customer ?: mutableListOf())
        socialDialog.onClickSocialChatListener = object : RoomSocialChatSheetDialog.OnClickSocialChatListener {

            override fun onMoreSound() {
                onSoundSelectionDialog(voiceRoomModel.soundEffect, finishBack)
            }
        }
        socialDialog.show(activity.supportFragmentManager, "chatroomSocialChatSheetDialog")
    }

    var roomAudioSettingDialog: RoomAudioSettingsSheetDialog? = null

    /**
     * Sound effect settings
     */
    fun onAudioSettingsDialog(finishBack: () -> Unit) {
        roomAudioSettingDialog = RoomAudioSettingsSheetDialog().apply {
            arguments = Bundle().apply {
                val audioSettingsInfo = RoomAudioSettingsBean(
                    enable = voiceRoomModel.isOwner,
                    roomType = ConfigConstants.RoomType.Common_Chatroom,
                    botOpen = voiceRoomModel.useRobot,
                    botVolume = voiceRoomModel.robotVolume,
                    soundSelection = voiceRoomModel.soundEffect,
                    AINSMode = VoiceCenter.rtcChannelTemp.AINSMode,
                    AINSMusicMode = VoiceCenter.rtcChannelTemp.AINSMusicMode,
                    AINSMicMode = VoiceCenter.rtcChannelTemp.AINSMicMode,
                    isAIAECOn = VoiceCenter.rtcChannelTemp.isAIAECOn,
                    isAIAGCOn = VoiceCenter.rtcChannelTemp.isAIAGCOn,
                    spatialOpen = false
                )
                putSerializable(RoomAudioSettingsSheetDialog.KEY_AUDIO_SETTINGS_INFO, audioSettingsInfo)
            }
        }

        roomAudioSettingDialog?.audioSettingsListener =
            object : RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener {

                override fun onAINS(mode: Int, musicMode: Int, micMode: Int, isEnable: Boolean) {
                    onAINSDialog(mode, musicMode, micMode)
                }

                override fun onAIAEC(isOn: Boolean, isEnable: Boolean) {
                    onAIAECDialog(isOn)
                }

                override fun onAGC(isOn: Boolean, isEnable: Boolean) {
                    onAIAGCDialog(isOn)
                }

                override fun onEarBackSetting() {
                    onEarBackSettingDialog()
                }

                override fun onVirtualSoundCardSetting() {
                    onVirtualSoundCardSettingDialog()
                }

                override fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    roomLivingViewModel.enableRobot(isChecked)
                }

                override fun onBotVolumeChange(progress: Int) {
                    roomLivingViewModel.updateBotVolume(progress)
                }

                override fun onSoundEffect(soundSelectionType: Int, isEnable: Boolean) {
                    onSoundSelectionDialog(soundSelectionType, finishBack)
                }
            }
        roomAudioSettingDialog?.show(activity.supportFragmentManager, "mtAudioSettings")
    }

    /**
     * Best sound effect selection
     */
    fun onSoundSelectionDialog(soundSelection: Int, finishBack: () -> Unit) {
        RoomSoundSelectionSheetDialog(
            voiceRoomModel.isOwner,
            object : RoomSoundSelectionSheetDialog.OnClickSoundSelectionListener {
                override fun onSoundEffect(soundSelection: SoundSelectionBean, isCurrentUsing: Boolean) {
                    if (isCurrentUsing) {
                        // Sound effect needs to be enabled with robot
                        if (voiceRoomModel.useRobot) {
                            RoomSoundAudioConstructor.soundSelectionAudioMap[soundSelection.soundSelectionType]?.let {
                                // Play best sound effect introduction
                                AgoraRtcEngineController.get().playMusic(it)
                            }
                        } else {
                            onBotMicClick(
                                activity.getString(R.string.voice_chatroom_open_bot_to_sound_effect),
                                finishBack
                            )
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
     * AI noise reduction dialog
     */
    fun onAINSDialog(ainsMode: Int, musicMode: Int, micMode: Int) {
        val ainsDialog = RoomAINSSheetDialog().apply {
            arguments = Bundle().apply {
                putInt(RoomAINSSheetDialog.KEY_AINS_MODE, ainsMode)
                putInt(RoomAINSSheetDialog.KEY_AINS_MUSIC_MODE, musicMode)
                putInt(RoomAINSSheetDialog.KEY_AINS_MIC_MODE, micMode)
                putBoolean(RoomAINSSheetDialog.KEY_IS_ENABLE, voiceRoomModel.isOwner)
            }
        }
        ainsDialog.anisModeCallback = {
            when (it.type) {
                AINSType.AINS_Default -> {
                    VoiceCenter.rtcChannelTemp.AINSMode = it.anisMode
                    AgoraRtcEngineController.get().deDefaultNoise(it.anisMode)
                    roomAudioSettingDialog?.apply {
                        audioSettingsInfo.AINSMode = it.anisMode
                        updateAINSView()
                    }
                }

                AINSType.AINS_Music -> {
                    VoiceCenter.rtcChannelTemp.AINSMusicMode = it.anisMode
                    roomAudioSettingDialog?.apply {
                        audioSettingsInfo.AINSMusicMode = it.anisMode
                    }
                    AgoraRtcEngineController.get().deMusicNoise(it.anisMode)
                }

                AINSType.AINS_Mic -> {
                    VoiceCenter.rtcChannelTemp.AINSMicMode = it.anisMode
                    roomAudioSettingDialog?.apply {
                        audioSettingsInfo.AINSMicMode = it.anisMode
                    }
                    AgoraRtcEngineController.get().deMicNoise(it.anisMode)
                }
            }

            if (voiceRoomModel.isOwner && voiceRoomModel.useRobot && VoiceCenter.rtcChannelTemp.firstSwitchAnis) {
                VoiceCenter.rtcChannelTemp.firstSwitchAnis = false
                RoomSoundAudioConstructor.anisIntroduceAudioMap[it.anisMode]?.let { soundAudioList ->
                    // Play AI noise reduction introduction
                    AgoraRtcEngineController.get().playMusic(soundAudioList)
                }
            }
        }
        ainsDialog.anisSoundCallback = { position, ainsSoundBean ->
            VoiceLogger.d(TAG, "onAINSDialog anisSoundCallback：$ainsSoundBean")
            val playSound = {
                ainsDialog.updateAnisSoundsAdapter(position, true)
                RoomSoundAudioConstructor.AINSSoundMap[ainsSoundBean.soundType]?.let { soundAudioBean ->
                    val audioUrl =
                        if (ainsSoundBean.soundMode == ConfigConstants.AINSMode.AINS_High) soundAudioBean.audioUrlHigh else soundAudioBean.audioUrl
                    // Listen to noise reduction effect
                    AgoraRtcEngineController.get()
                        .playMusic(soundAudioBean.soundId, audioUrl, soundAudioBean.speakerType)
                }
            }
            if (voiceRoomModel.useRobot) {
                playSound.invoke()
            } else {
                CommonFragmentAlertDialog().titleText(activity.getString(R.string.voice_chatroom_prompt))
                    .contentText(activity.getString(R.string.voice_chatroom_open_bot_to_sound_effect))
                    .leftText(activity.getString(R.string.voice_room_cancel))
                    .rightText(activity.getString(R.string.voice_room_confirm))
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            VoiceCenter.rtcChannelTemp.firstActiveBot = false
                            roomLivingViewModel.enableRobot(true)
                            roomAudioSettingDialog?.apply {
                                audioSettingsInfo.botOpen = true
                                updateBotStateView()
                            }
                            playSound.invoke()
                        }
                    }).show(activity.supportFragmentManager, "botActivatedDialog")
            }
        }

        ainsDialog.show(activity.supportFragmentManager, "mtAnis")
    }

    /**
     * Echo cancellation dialog
     */
    fun onAIAECDialog(isOn: Boolean) {
        val dialog = RoomAIAECSheetDialog().apply {
            arguments = Bundle().apply {
                putBoolean(RoomAIAECSheetDialog.KEY_IS_ON, isOn)
            }
        }
        dialog.onClickCheckBox = { isOn ->
            AgoraRtcEngineController.get().setAIAECOn(isOn)
            VoiceCenter.rtcChannelTemp.isAIAECOn = isOn
            roomAudioSettingDialog?.apply {
                audioSettingsInfo.isAIAECOn = isOn
                updateAIAECView()
            }
        }
        dialog.show(activity.supportFragmentManager, "mtAIAEC")
    }

    /**
     * Voice enhancement dialog
     */
    fun onAIAGCDialog(isOn: Boolean) {
        val dialog = RoomAIAGCSheetDialog().apply {
            arguments = Bundle().apply {
                putBoolean(RoomAIAGCSheetDialog.KEY_IS_ON, isOn)
            }
        }
        dialog.onClickCheckBox = { isOn ->
            AgoraRtcEngineController.get().setAIAGCOn(isOn)
            VoiceCenter.rtcChannelTemp.isAIAGCOn = isOn
            roomAudioSettingDialog?.audioSettingsInfo?.isAIAGCOn = isOn
            roomAudioSettingDialog?.updateAIAGCView()
        }
        dialog.show(activity.supportFragmentManager, "mtAIAGC")
    }

    /** Earback settings dialog
     */
    fun onEarBackSettingDialog() {
        if (AgoraRtcEngineController.get().earBackManager()?.params?.isForbidden == true) {
            CustomToast.showTips(R.string.voice_chatroom_settings_earback_forbidden_toast)
            return
        }
        val dialog = RoomEarBackSettingSheetDialog()
        dialog.setFragmentManager(activity.supportFragmentManager)
        dialog.setOnEarBackStateChange {
            roomAudioSettingDialog?.updateEarBackState()
        }
        dialog.show(activity.supportFragmentManager, "mtBGMSetting")
    }

    /** Virtual sound card settings dialog
     */
    fun onVirtualSoundCardSettingDialog() {
        if (AgoraRtcEngineController.get().soundCardManager()?.isForbidden() == true) {
            CustomToast.showTips(R.string.voice_settings_sound_card_forbidden_toast)
            return
        }
        val dialog = SoundCardSettingDialog()
        dialog.onClickSoundCardType = {
            val preset = SoundPresetTypeDialog()
            preset.show(activity.supportFragmentManager, SoundPresetTypeDialog.TAG)
        }
        dialog.onSoundCardStateChange = {
            roomAudioSettingDialog?.updateSoundCardState()
        }
        dialog.show(activity.supportFragmentManager, SoundCardSettingDialog.TAG)
    }

    /**
     * Exit room
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
     * Timeout exit room
     */
    fun onTimeUpExitRoom(content: String, finishBack: () -> Unit) {
        if (activity.isFinishing) {
            return
        }
        CommonFragmentContentDialog().contentText(content)
            .setOnClickListener(object : CommonFragmentContentDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    finishBack.invoke()
                }
            }).show(activity.supportFragmentManager, "mtTimeOutDialog")
    }

    /**
     * Click mic
     */
    fun onUserMicClick(micInfo: VoiceMicInfoModel) {
        if (UiUtils.isFastClick(500)) {
            return
        }

        val isMyself = TextUtils.equals(VoiceCenter.userId, micInfo.member?.userId)
        if (voiceRoomModel.isOwner || isMyself) { // Host or myself
            val roomMicMangerDialog = RoomMicManagerSheetDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(RoomMicManagerSheetDialog.KEY_MIC_INFO, micInfo)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_OWNER, voiceRoomModel.isOwner)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_MYSELF, isMyself)
                }
            }
            roomMicMangerDialog.onItemClickListener = object :
                OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
                            // Host invites others
                            if (data.enable) {
                                showOwnerHandsDialog(micInfo.micIndex)
                            } else {
                                CustomToast.show(R.string.voice_chatroom_mic_close_by_host)
                            }
                        }

                        MicClickAction.ForbidMic -> {
                            // Host mute other seats
                            roomLivingViewModel.forbidMic(micInfo.micIndex)
                        }

                        MicClickAction.UnForbidMic -> {
                            // Host cancel mute other seats
                            if (data.enable) {
                                roomLivingViewModel.cancelMuteMic(micInfo.micIndex)
                            } else {
                                CustomToast.show(R.string.voice_chatroom_mic_close_by_host)
                            }
                        }

                        MicClickAction.Mute -> {
                            // Mute myself
                            muteLocalAudio(true, micInfo.micIndex)
                        }

                        MicClickAction.UnMute -> {
                            // Cancel mute myself
                            if (activity is ChatroomLiveActivity) {
                                activity.toggleSelfAudio(true, callback = {
                                    muteLocalAudio(false, micInfo.micIndex)
                                })
                            }
                        }

                        MicClickAction.Lock -> {
                            // Host lock mic
                            roomLivingViewModel.lockMic(micInfo.micIndex)
                        }

                        MicClickAction.UnLock -> {
                            // Host cancel lock mic
                            roomLivingViewModel.unLockMic(micInfo.micIndex)
                        }

                        MicClickAction.KickOff -> {
                            // Host kick user off stage
                            roomLivingViewModel.kickOff(micInfo.micIndex)
                        }

                        MicClickAction.OffStage -> {
                            // User voluntarily leave stage
                            roomLivingViewModel.leaveMic(micInfo.micIndex)
                        }
                    }
                }
            }
            roomMicMangerDialog.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        } else if (micInfo.micStatus == MicStatus.Lock || micInfo.micStatus == MicStatus.LockForceMute) {
            // Seat locked
            CustomToast.show(R.string.voice_chatroom_mic_close_by_host)
        } else if ((micInfo.micStatus == MicStatus.Idle || micInfo.micStatus == MicStatus.ForceMute) && micInfo.member == null) {
            val mineMicIndex = iRoomMicView.findMicByUid(VoiceCenter.userId)
            if (mineMicIndex > 0) {
                // On mic, change mic
                showAlertDialog(activity.getString(R.string.voice_chatroom_exchange_mic),
                    object : CommonSheetAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            roomLivingViewModel.changeMic(mineMicIndex, micInfo.micIndex)
                        }
                    })
            } else {
                if (isRequesting) {
                    CustomToast.show(R.string.voice_chatroom_mic_submit_sent)
                } else {
                    showMemberHandsDialog(micInfo.micIndex)
                }
            }
        }
    }

    /**
     * Click robot
     */
    fun onBotMicClick(content: String, finishBack: () -> Unit) {
        if (voiceRoomModel.isOwner) { // Host
            if (!voiceRoomModel.useRobot) {
                CommonFragmentAlertDialog().titleText(activity.getString(R.string.voice_chatroom_prompt))
                    .contentText(content).leftText(activity.getString(R.string.voice_room_cancel))
                    .rightText(activity.getString(R.string.voice_room_confirm))
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            roomLivingViewModel.enableRobot(true)
                        }
                    }).show(activity.supportFragmentManager, "botActivatedDialog")
            } else {
                // nothing
                RoomRobotEnableDialog(object : RoomRobotEnableDialog.OnClickBtnListener {
                    override fun onClickCloseBtn() {
                        roomLivingViewModel.enableRobot(false)
                    }

                    override fun onClickSettingBtn() {
                        onAudioSettingsDialog(finishBack)
                    }
                }).apply {
                    robotDialog = this
                }.show(activity.supportFragmentManager, "mtClickRobotDialog")
            }
        } else { // Member
            CustomToast.showTips(R.string.voice_chatroom_only_host_can_change_robot)
        }
    }

    private fun showAlertDialog(content: String, onClickListener: CommonSheetAlertDialog.OnClickBottomListener) {
        CommonSheetAlertDialog().contentText(content).rightText(activity.getString(R.string.voice_room_confirm))
            .leftText(activity.getString(R.string.voice_room_cancel)).setOnClickListener(onClickListener)
            .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
    }

    /**
     * Mute myself
     */
    fun muteLocalAudio(mute: Boolean, index: Int = -1) {
        AgoraRtcEngineController.get().enableLocalAudio(!mute)
        val micIndex = if (index < 0) localUserIndex() else index
        if (mute) {
            roomLivingViewModel.muteLocal(micIndex)
        } else {
            roomLivingViewModel.unMuteLocal(micIndex)
        }
    }

    // Send gift success callback
    fun onSendGiftSuccess(roomId: String, message: ChatMessageData?) {
        val voiceGiftModel = ChatroomIMManager.getInstance().getGiftModel(message)
        val count = voiceGiftModel.gift_count?.toIntOrNull() ?: 0
        val price = voiceGiftModel.gift_price?.toIntOrNull() ?: 0
        val amount = count * price
        ChatroomIMManager.getInstance()
            .updateRankList(VoiceCenter.chatUid, voiceGiftModel, object : CallBack {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread {
                        iRoomTopView.onRankMember(ChatroomIMManager.getInstance().rankList)
                    }
                    EMLog.d(TAG, "onSendGiftSuccess updateAmount success")
                }

                override fun onError(code: Int, error: String?) {
                    EMLog.d(TAG, "onSendGiftSuccess updateAmount error$code $error")
                }
            })
        ChatroomIMManager.getInstance()
            .updateAmount(VoiceCenter.chatUid, amount, object : CallBack {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread {
                        iRoomTopView.onUpdateGiftCount(ChatroomIMManager.getInstance().giftAmountCache)
                    }
                    EMLog.d(TAG, "onSendGiftSuccess updateAmount success")
                }

                override fun onError(code: Int, error: String) {
                    EMLog.d(TAG, "onSendGiftSuccess updateAmount error$code $error")
                }
            })
    }

    // User joins room, host increases watch count
    fun onUserJoinedRoom() {
        ChatroomIMManager.getInstance().increaseClickCount(VoiceCenter.chatUid,
            object : CallBack {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread {
                        iRoomTopView.onUpdateWatchCount(ChatroomIMManager.getInstance().clickCountCache)
                    }
                    EMLog.d(TAG, "increaseClickCount success")
                }

                override fun onError(code: Int, error: String) {
                    EMLog.d(TAG, "increaseClickCount error$code $error")
                }
            })
    }

    // Receive gift message callback
    fun receiveGift(roomId: String, message: ChatMessageData?) {
        val voiceGiftModel = ChatroomIMManager.getInstance().getGiftModel(message)
        val count = voiceGiftModel.gift_count?.toIntOrNull() ?: 0
        val price = voiceGiftModel.gift_price?.toIntOrNull() ?: 0
        val amount = count * price
        ChatroomIMManager.getInstance()
            .updateAmount(VoiceCenter.chatUid, amount, object : CallBack {
                override fun onSuccess() {
                    ThreadManager.getInstance().runOnMainThread {
                        iRoomTopView.onUpdateGiftCount(ChatroomIMManager.getInstance().giftAmountCache)
                    }
                    EMLog.d(TAG, "receiveGift updateAmount success")
                }

                override fun onError(code: Int, error: String) {
                    EMLog.d(TAG, "receiveGift updateAmount error$code $error")
                }
            })
    }

    /** Receive invite to speak message */
    fun receiveInviteSite(roomId: String, micIndex: Int) {
        CommonFragmentAlertDialog().contentText(activity.getString(R.string.voice_chatroom_mic_anchor_invited_you_on_stage))
            .leftText(activity.getString(R.string.voice_room_decline))
            .rightText(activity.getString(R.string.voice_room_accept))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {

                    if (activity is ChatroomLiveActivity) {
                        activity.toggleSelfAudio(true, callback = {
                            roomLivingViewModel.acceptMicSeatInvitation(micIndex)
                        })
                    }
                }

                override fun onCancelClick() {
                    roomLivingViewModel.refuseInvite()
                }
            }).show(activity.supportFragmentManager, "CommonFragmentAlertDialog")
    }

    fun destroy() {
        AgoraRtcEngineController.get().destroy()
    }

    /** Host hands dialog */
    fun showOwnerHandsDialog(inviteIndex: Int) {
        handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
        if (handsDialog == null) {
            handsDialog = ChatroomHandsDialog.newInstance
        }
        handsDialog?.setInviteMicIndex(inviteIndex)
        handsDialog?.setFragmentListener(object : ChatroomHandsDialog.OnFragmentListener {
            override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                val newMicMap = mutableMapOf(voiceMicInfoModel.micIndex to voiceMicInfoModel)
                dealMicDataMap(newMicMap)
                updateViewByMicMap(newMicMap)
            }
        })
        handsDialog?.show(activity.supportFragmentManager, "room_hands")
        chatPrimaryMenuView.setShowHandStatus(true, false)
    }

    /** User hands up */
    fun showMemberHandsDialog(micIndex: Int) {
        CommonSheetAlertDialog().contentText(
            if (isRequesting) activity.getString(R.string.voice_chatroom_cancel_request_speak)
            else activity.getString(R.string.voice_chatroom_request_speak)
        ).rightText(activity.getString(R.string.voice_room_confirm))
            .leftText(activity.getString(R.string.voice_room_cancel))
            .setOnClickListener(object : CommonSheetAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    if (isRequesting) {
                        roomLivingViewModel.cancelMicSeatApply(
                            voiceRoomModel.chatroomId,
                            VoiceCenter.chatUid
                        )
                    } else {
                        if (activity is ChatroomLiveActivity) {
                            activity.toggleSelfAudio(true, callback = {
                                roomLivingViewModel.startMicSeatApply(micIndex)
                            })
                        }
                    }
                }
            }).show(activity.supportFragmentManager, "room_hands_apply")
    }

    fun handsUpdate(index: Int) {
        handsDialog?.update(index)
    }

    // Click bottom mic icon
    fun onClickBottomMic() {
        if (localUserMicInfo?.micStatus == MicStatus.ForceMute) {
            CustomToast.show(R.string.voice_chatroom_mic_muted_by_host)
            return
        }
        val isOn = localUserMicInfo?.member?.micStatus == 1
        val toState = !isOn
        if (activity is ChatroomLiveActivity) {
            activity.toggleSelfAudio(toState, callback = {
                chatPrimaryMenuView.setEnableMic(toState)
                muteLocalAudio(!toState)
            })
        }
    }

    // Click bottom raise hand icon  
    fun onClickBottomHandUp() {
        if (voiceRoomModel.isOwner) {
            showOwnerHandsDialog(-1)
        } else {
            showMemberHandsDialog(-1)
        }
    }

    // Update announcement
    fun updateAnnouncement(announcement: String?) {
        if (voiceRoomModel.announcement != announcement) {
            voiceRoomModel.announcement = announcement ?: ""
            CustomToast.show(R.string.voice_chatroom_notice_changed)
        }
    }

    /**
     * IM custom field update
     *
     * @param attributeMap
     */
    fun onAttributeMapUpdated(attributeMap: Map<String, String>) {
        if (attributeMap.containsKey("gift_amount")) {
            attributeMap["gift_amount"]?.toIntOrNull()?.let {
                voiceRoomModel.giftAmount = it
                ChatroomIMManager.getInstance().giftAmountCache = it
                ThreadManager.getInstance().runOnMainThread {
                    iRoomTopView.onUpdateGiftCount(it)
                }
            }
        }
        if (attributeMap.containsKey("click_count")) {
            attributeMap["click_count"]?.toIntOrNull()?.let {
                voiceRoomModel.clickCount = it
                ChatroomIMManager.getInstance().setClickCountCache(it)
                ThreadManager.getInstance().runOnMainThread {
                    iRoomTopView.onUpdateWatchCount(it)
                }
            }
        }
        if (attributeMap.containsKey("robot_volume")) {
            attributeMap["robot_volume"]?.toIntOrNull()?.let {
                voiceRoomModel.robotVolume = it
            }
        }
        if (attributeMap.containsKey("use_robot")) {
            voiceRoomModel.useRobot = attributeMap["use_robot"] == "1"
            ThreadManager.getInstance().runOnMainThread {
                iRoomMicView.activeBot(voiceRoomModel.useRobot)
            }
        }
        if (attributeMap.containsKey("ranking_list")) {
            val rankList = GsonTools.toList(attributeMap["ranking_list"], VoiceRankUserModel::class.java)
            rankList?.let { rankUsers ->
                rankUsers.forEach { rank ->
                    ChatroomIMManager.getInstance().setRankList(rank)
                }
                ThreadManager.getInstance().runOnMainThread {
                    iRoomTopView.onRankMember(rankUsers)
                }
            }
        } else if (attributeMap.containsKey("member_list")) {
            val memberList = GsonTools.toList(attributeMap["member_list"], VoiceMemberModel::class.java)
            memberList?.let { members ->
                members.forEach { member ->
                    if (!member.chatUid.equals(voiceRoomModel.owner?.chatUid)) {
                        ChatroomCacheManager.cacheManager.setMemberList(member)
                    }
                }
            }
        } else {
            // mic
            val micInfoMap = mutableMapOf<String, VoiceMicInfoModel>()
            attributeMap
                .filter { it.key.startsWith("mic_") }
                .forEach { (key, value) ->
                    val micInfo =
                        GsonTools.toBean<VoiceMicInfoModel>(value, object : TypeToken<VoiceMicInfoModel>() {}.type)
                    micInfo?.let {
                        micInfoMap[key] = it
                        if (it.member?.rtcUid == VoiceCenter.rtcUid) {
                            localUserMicInfo = micInfo
                        }
                    }
                }
            val newMicMap = RoomInfoConstructor.extendMicInfoMap(micInfoMap, voiceRoomModel.owner?.userId ?: "")
            dealMicDataMap(newMicMap)
            ThreadManager.getInstance().runOnMainThread {
                updateViewByMicMap(newMicMap)
            }
        }
    }

    /**
     * Process mic data
     */
    private fun dealMicDataMap(updateMap: Map<Int, VoiceMicInfoModel>) {
        // Temporary variable, prevent removal when swapping mic positions
        var kvLocalUser: VoiceMicInfoModel? = null
        updateMap.forEach { (index, micInfo) ->
            val rtcUid = micInfo.member?.rtcUid ?: -1
            if (rtcUid > 0) {
                micMap[index] = rtcUid
                // Current user on mic
                if (rtcUid == VoiceCenter.rtcUid) kvLocalUser = micInfo
            } else {
                val removeRtcUid = micMap.remove(index)
                // Current user removed from mic
                if (removeRtcUid == VoiceCenter.rtcUid) localUserMicInfo = null
            }
        }
        kvLocalUser?.let { localUserMicInfo = it }
        AgoraRtcEngineController.get().switchRole(localUserIndex() >= 0)
        if (localUserMicInfo?.member?.micStatus == 1 &&
            localUserMicInfo?.micStatus == MicStatus.Normal
        ) {   // Normal state
            AgoraRtcEngineController.get().enableLocalAudio(true)
        } else {  // Other state
            AgoraRtcEngineController.get().enableLocalAudio(false)
        }
    }

    /**
     * Update UI based on mic data
     */
    private fun updateViewByMicMap(newMicMap: Map<Int, VoiceMicInfoModel>) {
        iRoomMicView.onSeatUpdated(newMicMap)
        val isOn = (localUserMicInfo?.member?.micStatus == 1 &&
                localUserMicInfo?.micStatus == MicStatus.Normal)
        val onStage = localUserIndex() >= 0
        chatPrimaryMenuView.showMicVisible(onStage, isOn)
        AgoraRtcEngineController.get().earBackManager()?.setForbidden(!onStage)
        AgoraRtcEngineController.get().soundCardManager()?.setForbidden(!onStage)
        if (voiceRoomModel.isOwner) {
            val handsCheckMap = mutableMapOf<Int, String>()
            newMicMap.forEach { (t, u) ->
                handsCheckMap[t] = u.member?.userId ?: ""
            }
            handsDialog?.check(handsCheckMap)
        } else {
            chatPrimaryMenuView.setEnableHand(localUserIndex() >= 0)
            isRequesting = false
        }
    }

    fun checkUserLeaveMic() {
        val localUserIndex = localUserIndex()
        // Normal user leaves
        if (localUserIndex > 0) {
            roomLivingViewModel.leaveMic(localUserIndex)
        }
    }

    fun checkUserLeaveMic(index: Int) {
        if (index > 0) {
            roomLivingViewModel.leaveMic(index)
        }
    }
}
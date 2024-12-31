package io.agora.scene.voice.spatial.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.VoiceSpatialLogger
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.global.IParserSource
import io.agora.scene.voice.spatial.global.VSpatialCenter
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.model.annotation.MicClickAction
import io.agora.scene.voice.spatial.model.annotation.MicStatus
import io.agora.scene.voice.spatial.model.constructor.RoomInfoConstructor
import io.agora.scene.voice.spatial.model.constructor.RoomSoundAudioConstructor
import io.agora.scene.voice.spatial.net.OnResourceParseCallback
import io.agora.scene.voice.spatial.net.Resource
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.spatial.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.spatial.rtckit.listener.RtcSpatialPositionListener
import io.agora.scene.voice.spatial.ui.activity.ChatroomLiveActivity
import io.agora.scene.voice.spatial.ui.dialog.*
import io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog
import io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentContentDialog
import io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetAlertDialog
import io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicView
import io.agora.scene.voice.spatial.ui.widget.primary.ChatPrimaryMenuView
import io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView
import io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.UiUtils

/**
 * @author create by zhangwei03
 *
 * Room header && mic position data change proxy
 */
class RoomObservableViewDelegate constructor(
    private val activity: FragmentActivity,
    private val roomLivingViewModel: VoiceRoomLivingViewModel,
    private val roomKitBean: RoomKitBean,
    private val iRoomTopView: IRoomLiveTopView, // header
    private val iRoomMicView: IRoomMicView, // mic
    private val chatPrimaryMenuView: ChatPrimaryMenuView, // bottom
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /**Mic location data, index, rtcUid*/
    private val micMap = mutableMapOf<Int, Int>()

    private var localUserMicInfo: VoiceMicInfoModel? = null

    /**Raise hand dialog*/
//    private var handsDialog: ChatroomHandsDialog? = null

    /**Apply for mic seat flag*/
    private var isRequesting: Boolean = false

    private var voiceRoomModel: VoiceRoomModel = VoiceRoomModel()
    private var robotInfo: RobotSpatialAudioModel = RobotSpatialAudioModel()

    private fun localUserIndex(): Int {
        return localUserMicInfo?.micIndex ?: -1
    }

    init {
        // Update announcement
        roomLivingViewModel.roomNoticeObservable().observe(activity) { response: Resource<Pair<String, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<String, Boolean>>() {
                override fun onSuccess(data: Pair<String, Boolean>?) {
                    if (data?.second != true) return
                    voiceRoomModel.announcement = data.first
                    CustomToast.show(activity.getString(R.string.voice_spatial_notice_posted))
                }

                override fun onError(code: Int, message: String?) {
                    CustomToast.show(activity.getString(R.string.voice_spatial_notice_posted_error))
                }
            })
        }
        // Open robot
        roomLivingViewModel.openBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "robot open：$data")
                    if (data != true) return
                    iRoomMicView.activeBot(true) { type, points ->
                        AgoraRtcEngineController.get().updatePlayerPosition(
                            floatArrayOf(points.first.x, points.first.y, 0f),
                            floatArrayOf(points.second.x, points.second.y, 0f),
                            type
                        )
                    }
                    robotInfo.useRobot = true
                    roomAudioSettingDialog?.updateBoxCheckBoxView(true)
                    activeRobotSound()
                }
            })
        }
        // Close robot
        roomLivingViewModel.closeBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "robot close：$data")
                    if (data != true) return
                    iRoomMicView.activeBot(false, null)
                    // Close robot, pause all sound effects
                    robotInfo.useRobot = false
                    AgoraRtcEngineController.get().resetMediaPlayer()
                }
            })
        }
        // Robot volume
        roomLivingViewModel.robotVolumeObservable().observe(activity) { response: Resource<Pair<Int, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<Int, Boolean>>() {
                override fun onSuccess(data: Pair<Int, Boolean>?) {
                    VoiceSpatialLogger.d(TAG, "robotVolume update：$data")
                    data?.let {
                        if (it.second) {
                            robotInfo.robotVolume = it.first
                            AgoraRtcEngineController.get().updateEffectVolume(it.first)
                        }
                    }
                }
            })
        }
        /**Open/close blue robot air absorption*/
        roomLivingViewModel.openBlueBotAirAbsorbObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableBlueAbsorb(data ?: false)
                }
            })
        }

        /**Open/close red robot air absorption*/
        roomLivingViewModel.openRedBotAirAbsorbObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableRedAbsorb(data ?: false)
                }
            })
        }

        /**Open/close blue robot blur*/
        roomLivingViewModel.openBlueBotBlurObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableBlueBlur(data ?: false)
                }
            })
        }

        /**Open/close red robot blur*/
        roomLivingViewModel.openRedBotBlurObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableRedBlur(data ?: false)
                }
            })
        }

        /**Blue robot attenuation coefficient*/
        roomLivingViewModel.blueRobotAttenuationObservable()
            .observe(activity) { response: Resource<Pair<Double, Boolean>> ->
                parseResource(response, object : OnResourceParseCallback<Pair<Double, Boolean>>() {
                    override fun onSuccess(data: Pair<Double, Boolean>?) {
                        data?.let {
                            if (it.second) {
                                AgoraRtcEngineController.get().adjustBlueAttenuation(it.first)
                            }
                        }
                    }
                })
            }

        /**Red robot attenuation coefficient*/
        roomLivingViewModel.redRobotAttenuationObservable()
            .observe(activity) { response: Resource<Pair<Double, Boolean>> ->
                parseResource(response, object : OnResourceParseCallback<Pair<Double, Boolean>>() {
                    override fun onSuccess(data: Pair<Double, Boolean>?) {
                        data?.let {
                            if (it.second) {
                                AgoraRtcEngineController.get().adjustRedAttenuation(it.first)
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
                if (rtcUid == 0) { // myself
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
        AgoraRtcEngineController.get().setSpatialListener(object : RtcSpatialPositionListener() {
            override fun onRemoteSpatialChanged(position: SeatPositionInfo) {
                iRoomMicView.updateSpatialPosition(position)
                AgoraRtcEngineController.get().updateRemotePosition(
                    position.uid,
                    floatArrayOf(position.x, position.y, 0f),
                    position.forward
                )
            }
        })
        // Apply for mic seat
        roomLivingViewModel.startMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "start mic seat apply:$data")
                    CustomToast.show(activity.getString(R.string.voice_spatial_mic_submit_sent))
                    chatPrimaryMenuView.setShowHandStatus(false, true)
                    isRequesting = true
                }
            })
        }
        // Cancel apply
        roomLivingViewModel.cancelMicSeatApplyObservable().observe(activity) { result: Resource<Boolean> ->
            parseResource(result, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "cancel mic seat apply:$data")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_cancel_apply_success))
                    chatPrimaryMenuView.setShowHandStatus(false, false)
                    isRequesting = false
                }
            })
        }
        // Local mute / cancel local mute
        roomLivingViewModel.muteMicObservable().observe(activity) { response: Resource<VoiceMemberModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMemberModel>() {
                override fun onSuccess(data: VoiceMemberModel?) {
                    if (data?.micStatus == MicStatus.Normal) {
                        CustomToast.show( activity.getString(R.string.voice_spatial_mic_unmuted))
                    } else {
                        CustomToast.show( activity.getString(R.string.voice_spatial_mic_muted))
                    }
                    localUserMicInfo?.let {
                        it.member = data
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
                    VoiceSpatialLogger.d(TAG, "leave mic：${data?.micIndex}")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_off_stage))
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
                    VoiceSpatialLogger.d(TAG, "kick mic：${data?.micIndex}")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_kicked_off))
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
                    VoiceSpatialLogger.d(TAG, "force mute mic：${data?.micIndex}")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_muted))
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
                    VoiceSpatialLogger.d(TAG, "cancel force mute mic：${data?.micIndex}")
                    CustomToast.show(activity.getString(R.string.voice_spatial_mic_unmuted))
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
                    VoiceSpatialLogger.d(TAG, "lock mic：${data?.micIndex}")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_blocked))
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
                    VoiceSpatialLogger.d(TAG, "cancel lock mic：${data?.micIndex}")
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_unblocked))
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
                    }
                }
            })
        }
        // User reject apply for mic
        roomLivingViewModel.rejectMicInvitationObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceSpatialLogger.d(TAG, "reject mic invitation：$data")
                }
            })
        }
        // Accept invitation
        roomLivingViewModel.acceptMicSeatInvitationObservable()
            .observe(activity) { response: Resource<VoiceMicInfoModel> ->
                parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                    override fun onSuccess(data: VoiceMicInfoModel?) {
                        data?.let {
                            // Update mic
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
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_exchange_mic_success))
                    data?.let {
                        // Update mic
                        dealMicDataMap(it)
                        updateViewByMicMap(it)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    CustomToast.show( activity.getString(R.string.voice_spatial_mic_exchange_mic_failed))
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
     * Room summary
     */
    fun onRoomModel(voiceRoomModel: VoiceRoomModel) {
        this.voiceRoomModel = voiceRoomModel
        iRoomTopView.onChatroomInfo(voiceRoomModel)
    }

    /**
     * Room details
     */
    fun onRoomDetails(voiceRoomInfo: VoiceRoomInfo) {
        voiceRoomInfo.roomInfo?.let { vRoomInfo ->
            this.voiceRoomModel = vRoomInfo
            iRoomTopView.onChatroomInfo(vRoomInfo)
        }
        voiceRoomInfo.robotInfo?.let { vRobotInfo ->
            this.robotInfo = vRobotInfo
        }
        if (!voiceRoomInfo.micInfo.isNullOrEmpty()) {
            // Mic data is not empty
            voiceRoomInfo.micInfo?.let { micList ->
//                val micInfoList: List<VoiceMicInfoModel> =
//                    RoomInfoConstructor.extendMicInfoList(micList, roomKitBean.roomType, roomKitBean.ownerId)
                micList.forEach { micInfo ->
                    micInfo.member?.let { userInfo ->
                        val rtcUid = userInfo.rtcUid
                        val micIndex = micInfo.micIndex
                        if (rtcUid > 0) {
                            // myself
                            if (rtcUid == VSpatialCenter.rtcUid) {
                                localUserMicInfo = micInfo
                            }
                            micMap[micIndex] = rtcUid
                        }
                    }
                }
                iRoomMicView.onInitMic(micList, robotInfo.useRobot) {
                    updateSpatialPosition(micList)
                }
            }
        }
        val micIsOn = (
                localUserMicInfo?.member?.micStatus == MicStatus.Normal &&
                        localUserMicInfo?.micStatus == MicStatus.Normal
                )
        chatPrimaryMenuView.showMicVisible(micIsOn, localUserIndex() >= 0)
    }

    /**
     * Rank
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
     * Show 3D spatial audio welcome page
     */
    fun showRoom3DWelcomeSheetDialog() {
        val room3DWelcomeSheetDialog = Room3DWelcomeSheetDialog()
        room3DWelcomeSheetDialog.show(activity.supportFragmentManager, "room3DWelcomeSheetDialog")
    }

    /**
     * Announcement
     */
    fun onClickNotice() {
        var announcement = voiceRoomModel.announcement
        if (announcement.isNullOrEmpty()) {
            announcement = activity.getString(R.string.voice_spatial_first_enter_room_notice_tips)
        }
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

    var roomAudioSettingDialog: RoomAudioSettingsSheetDialog? = null

    /**
     * Audio settings
     */
    fun onAudioSettingsDialog(finishBack: () -> Unit) {
        roomAudioSettingDialog = RoomAudioSettingsSheetDialog().apply {
            arguments = Bundle().apply {
                val audioSettingsInfo = RoomAudioSettingsBean(
                    enable = roomKitBean.isOwner,
                    roomType = ConfigConstants.RoomType.Spatial_Chatroom,
                    botOpen = robotInfo.useRobot,
                    botVolume = robotInfo.robotVolume,
                    soundSelection = roomKitBean.soundEffect,
                    AINSMode = VSpatialCenter.rtcChannelTemp.AINSMode,
                    isAIAECOn = VSpatialCenter.rtcChannelTemp.isAIAECOn,
                    isAIAGCOn = VSpatialCenter.rtcChannelTemp.isAIAGCOn,
                    spatialOpen = false
                )
                putSerializable(RoomAudioSettingsSheetDialog.KEY_AUDIO_SETTINGS_INFO, audioSettingsInfo)
            }
        }

        roomAudioSettingDialog?.audioSettingsListener =
            object : RoomAudioSettingsSheetDialog.OnClickAudioSettingsListener {

                override fun onVoiceChanger(mode: Int, isEnable: Boolean) {
                    onVoiceChangerDialog(mode)
                }

                override fun onBotCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
                    roomLivingViewModel.enableRobot(isChecked)
                }

                override fun onBotVolumeChange(progress: Int) {
                    roomLivingViewModel.updateBotVolume(progress)
                }

                override fun onSpatialAudio(isOpen: Boolean, isEnable: Boolean) {
                    onSpatialDialog()
                }

            }

        roomAudioSettingDialog?.show(activity.supportFragmentManager, "mtAudioSettings")
    }

    /**
     * Voice changer dialog
     */
    fun onVoiceChangerDialog(mode: Int) {

    }

    /**
     * Spatial audio dialog
     */
    fun onSpatialDialog() {
        val spatialAudioSheetDialog = RoomSpatialAudioSheetDialog().apply {
            arguments = Bundle().apply {
                putBoolean(RoomSpatialAudioSheetDialog.KEY_SPATIAL_OPEN, false)
                putBoolean(RoomSpatialAudioSheetDialog.KEY_IS_ENABLED, roomKitBean.isOwner)

                putBoolean(RoomSpatialAudioSheetDialog.KEY_BLUE_AIR_ABSORB_ENABLED, robotInfo.blueRobotAbsorb)
                putBoolean(RoomSpatialAudioSheetDialog.KEY_RED_AIR_ABSORB_ENABLED, robotInfo.redRobotAbsorb)
                putBoolean(RoomSpatialAudioSheetDialog.KEY_BLUE_BLUR_ENABLED, robotInfo.blueRobotBlur)
                putBoolean(RoomSpatialAudioSheetDialog.KEY_RED_BLUR_ENABLED, robotInfo.redRobotBlur)
                putInt(
                    RoomSpatialAudioSheetDialog.KEY_BLUE_ATTENUATION,
                    (robotInfo.blueRobotAttenuation * 100.0).toInt()
                )
                putInt(RoomSpatialAudioSheetDialog.KEY_RED_ATTENUATION, (robotInfo.redRobotAttenuation * 100.0).toInt())
            }
        }

        spatialAudioSheetDialog.audioSettingsListener = object :
            RoomSpatialAudioSheetDialog.OnClickSpatialAudioRobotsSettingsListener {
            override fun onBlueBotAttenuationChange(progress: Int) {
                val value = progress / 100.0
                roomLivingViewModel.updateBlueRoBotAttenuation(value)
            }

            override fun onBlueBotAirAbsorbCheckedChanged(
                buttonView: CompoundButton,
                isChecked: Boolean
            ) {
                roomLivingViewModel.enableBlueRobotAirAbsorb(isChecked)
            }

            override fun onBlueBotVoiceBlurCheckedChanged(
                buttonView: CompoundButton,
                isChecked: Boolean
            ) {
                roomLivingViewModel.enableBlueRobotBlur(isChecked)
            }

            override fun onRedBotAttenuationChange(progress: Int) {
                val value = progress / 100.0
                roomLivingViewModel.updateRedRoBotAttenuation(value)
            }

            override fun onRedBotAirAbsorbCheckedChanged(
                buttonView: CompoundButton,
                isChecked: Boolean
            ) {
                roomLivingViewModel.enableRedRobotAirAbsorb(isChecked)
            }

            override fun onRedBotVoiceBlurCheckedChanged(
                buttonView: CompoundButton,
                isChecked: Boolean
            ) {
                roomLivingViewModel.enableRedRobotBlur(isChecked)
            }

        }

        spatialAudioSheetDialog.show(activity.supportFragmentManager, "mtSpatialAudio")
    }

    /**
     * Exit room
     */
    fun onExitRoom(title: String, content: String, finishBack: () -> Unit) {
        CommonFragmentAlertDialog().titleText(title).contentText(content)
            .leftText(activity.getString(R.string.voice_spatial_room_cancel))
            .rightText(activity.getString(R.string.voice_spatial_room_confirm))
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

        val isMyself = TextUtils.equals(VSpatialCenter.userId, micInfo.member?.userId)
        if (roomKitBean.isOwner || isMyself) { // Host or myself
            val roomMicMangerDialog = RoomMicManagerSheetDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(RoomMicManagerSheetDialog.KEY_MIC_INFO, micInfo)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_OWNER, roomKitBean.isOwner)
                    putSerializable(RoomMicManagerSheetDialog.KEY_IS_MYSELF, isMyself)
                }
            }
            roomMicMangerDialog.onItemClickListener = object :
                OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
                            // Host invite others
                            if (data.enable) {
                                showOwnerHandsDialog(micInfo.micIndex)
                            } else {
                                CustomToast.show(activity.getString(R.string.voice_spatial_mic_close_by_host))
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
                                CustomToast.show( activity.getString(R.string.voice_spatial_mic_close_by_host))
                            }
                        }

                        MicClickAction.Mute -> {
                            // Mute myself
                            muteLocalAudio(true)
                        }

                        MicClickAction.UnMute -> {
                            // Cancel mute myself
                            if (activity is ChatroomLiveActivity) {
                                activity.toggleSelfAudio(true, callback = {
                                    muteLocalAudio(false)
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
                            // User leave stage
                            roomLivingViewModel.leaveMic(micInfo.micIndex)
                        }
                    }
                }
            }
            roomMicMangerDialog.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        } else if (micInfo.micStatus == MicStatus.Lock || micInfo.micStatus == MicStatus.LockForceMute) {
            // Mic is locked
            CustomToast.show( activity.getString(R.string.voice_spatial_mic_close_by_host))
        } else if ((micInfo.micStatus == MicStatus.Idle || micInfo.micStatus == MicStatus.ForceMute) && micInfo.member == null) {
            val mineMicIndex = iRoomMicView.findMicByUid(VSpatialCenter.userId)
            if (mineMicIndex >= 0) {
                // On mic, change mic
                showAlertDialog(activity.getString(R.string.voice_spatial_exchange_mic),
                    object : CommonSheetAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            roomLivingViewModel.changeMic(mineMicIndex, micInfo.micIndex)
                        }
                    })
            } else {
                if (isRequesting) {
                    CustomToast.show(activity.getString(R.string.voice_spatial_mic_submit_sent))
                } else {
                    showMemberHandsDialog(micInfo.micIndex)
                }
            }
        }
    }

    /**
     * Click robot
     */
    fun onBotMicClick(content: String) {
        if (roomKitBean.isOwner) { // Host
            if (!robotInfo.useRobot) {
                CommonFragmentAlertDialog().titleText(activity.getString(R.string.voice_spatial_prompt))
                    .contentText(content).leftText(activity.getString(R.string.voice_spatial_room_cancel))
                    .rightText(activity.getString(R.string.voice_spatial_room_confirm))
                    .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                        override fun onConfirmClick() {
                            roomLivingViewModel.enableRobot(true)
                        }
                    }).show(activity.supportFragmentManager, "botActivatedDialog")
            } else {
                // nothing
            }
        } else { // Member
            CustomToast.show( activity.getString(R.string.voice_spatial_only_host_can_change_robot))
        }
    }

    private fun showAlertDialog(content: String, onClickListener: CommonSheetAlertDialog.OnClickBottomListener) {
        CommonSheetAlertDialog().contentText(content).rightText(activity.getString(R.string.voice_spatial_room_confirm))
            .leftText(activity.getString(R.string.voice_spatial_room_cancel)).setOnClickListener(onClickListener)
            .show(activity.supportFragmentManager, "CommonSheetAlertDialog")
    }

    /**
     * Mute myself
     */
    fun muteLocalAudio(mute: Boolean) {
        AgoraRtcEngineController.get().enableLocalAudio(!mute)
        roomLivingViewModel.muteLocal(mute)
    }

    /**Receive invite to mic message*/
    fun receiveInviteSite(roomId: String, micIndex: Int) {
        CommonFragmentAlertDialog().contentText(activity.getString(R.string.voice_spatial_mic_anchor_invited_you_on_stage))
            .leftText(activity.getString(R.string.voice_spatial_room_decline))
            .rightText(activity.getString(R.string.voice_spatial_room_accept))
            .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    if (isRequesting) { // If I am applying for a mic, cancel the application
                        roomLivingViewModel.cancelMicSeatApply(VSpatialCenter.userId)
                    }
                    if (activity is ChatroomLiveActivity) {
                        activity.toggleSelfAudio(true, callback = {
                            roomLivingViewModel.acceptMicSeatInvitation()
                        })
                    }
                }

                override fun onCancelClick() {
                    roomLivingViewModel.refuseInvite()
                }
            }).show(activity.supportFragmentManager, "CommonFragmentAlertDialog")
    }

    /**Receive system message*/
    fun receiveSystem(ext: MutableMap<String, String>) {
        ThreadManager.getInstance().runOnMainThread {
            if (ext.containsKey("click_count")) {
                ext["click_count"]?.toIntOrNull()?.let {
                    iRoomTopView.onUpdateWatchCount(it)
                }
            }
            if (ext.containsKey("member_count")) {
                ext["member_count"]?.toIntOrNull()?.let {
                    iRoomTopView.onUpdateMemberCount(it)
                }
            }
            if (ext.containsKey("gift_amount")) {
                ext["gift_amount"]?.toIntOrNull()?.let {
                    iRoomTopView.onUpdateGiftCount(it)
                }
            }
        }
    }

    fun destroy() {
        AgoraRtcEngineController.get().destroy()
    }

    /**Host hands dialog*/
    fun showOwnerHandsDialog(micIndex: Int) {
        var handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
        if (handsDialog == null) {
            handsDialog = ChatroomHandsDialog()
            handsDialog.setFragmentListener(object : ChatroomHandsDialog.OnFragmentListener {
                override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                    // Update mic
                    val newMicMap = mutableMapOf(voiceMicInfoModel.micIndex to voiceMicInfoModel)
                    dealMicDataMap(newMicMap)
                    updateViewByMicMap(newMicMap)
                }
            })
            handsDialog.setMicIndex(micIndex)
            handsDialog.show(activity.supportFragmentManager, "room_hands")
        }
        chatPrimaryMenuView.setShowHandStatus(true, false)
    }

    /**Member hands dialog*/
    fun showMemberHandsDialog(micIndex: Int) {
        CommonSheetAlertDialog().contentText(
            if (isRequesting) activity.getString(R.string.voice_spatial_cancel_request_speak)
            else activity.getString(R.string.voice_spatial_request_speak)
        ).rightText(activity.getString(R.string.voice_spatial_room_confirm))
            .leftText(activity.getString(R.string.voice_spatial_room_cancel))
            .setOnClickListener(object : CommonSheetAlertDialog.OnClickBottomListener {
                override fun onConfirmClick() {
                    if (isRequesting) {
                        roomLivingViewModel.cancelMicSeatApply(VSpatialCenter.userId)
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
        val handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
        handsDialog?.update(index)
    }

    // Click bottom microphone icon
    fun onClickBottomMic() {
        if (localUserMicInfo?.micStatus == MicStatus.ForceMute) {
            // Mute
            CustomToast.show( activity.getString(R.string.voice_spatial_mic_muted_by_host))
            return
        }
        val openAudio = localUserMicInfo?.member?.micStatus != MicStatus.Normal
        if (activity is ChatroomLiveActivity) {
            activity.toggleSelfAudio(openAudio, callback = {
                chatPrimaryMenuView.setEnableMic(openAudio)
                muteLocalAudio(!openAudio)
            })
        }
    }

    // Click bottom hands icon
    fun onClickBottomHandUp() {
        if (roomKitBean.isOwner) {
            showOwnerHandsDialog(-1)
        } else {
            showMemberHandsDialog(-1)
        }
    }

    // Update announcement
    fun updateAnnouncement(announcement: String?) {
        voiceRoomModel.announcement = announcement ?: ""
    }

    fun onRobotUpdated(robotInfo: RobotSpatialAudioModel) {
        val oldValue = this.robotInfo.useRobot
        if (robotInfo.useRobot) {
            if (this.robotInfo.robotVolume != robotInfo.robotVolume) {
                AgoraRtcEngineController.get().updateEffectVolume(robotInfo.robotVolume)
            }
            if (this.robotInfo.redRobotAttenuation != robotInfo.redRobotAttenuation) {
                AgoraRtcEngineController.get().adjustRedAttenuation(robotInfo.redRobotAttenuation)
            }
            if (this.robotInfo.blueRobotAttenuation != robotInfo.blueRobotAttenuation) {
                AgoraRtcEngineController.get().adjustBlueAttenuation(robotInfo.blueRobotAttenuation)
            }
            if (this.robotInfo.blueRobotBlur != robotInfo.blueRobotBlur) {
                AgoraRtcEngineController.get().enableBlueBlur(robotInfo.blueRobotBlur)
            }
            if (this.robotInfo.redRobotBlur != robotInfo.redRobotBlur) {
                AgoraRtcEngineController.get().enableRedBlur(robotInfo.redRobotBlur)
            }
            if (this.robotInfo.redRobotAbsorb != robotInfo.redRobotAbsorb) {
                AgoraRtcEngineController.get().enableRedAbsorb(robotInfo.redRobotAbsorb)
            }
            if (this.robotInfo.blueRobotAbsorb != robotInfo.blueRobotAbsorb) {
                AgoraRtcEngineController.get().enableBlueAbsorb(robotInfo.blueRobotAbsorb)
            }
        }
        this.robotInfo = robotInfo
        ThreadManager.getInstance().runOnMainThread {
            if (robotInfo.useRobot != oldValue) {
                if (robotInfo.useRobot) {
                    iRoomMicView.activeBot(true) { type, points ->
                        AgoraRtcEngineController.get().updatePlayerPosition(
                            floatArrayOf(points.first.x, points.first.y, 0f),
                            floatArrayOf(points.second.x, points.second.y, 0f),
                            type
                        )
                    }
                    activeRobotSound()
                } else {
                    iRoomMicView.activeBot(false, null)
                    AgoraRtcEngineController.get().resetMediaPlayer()
                }
            }
        }
    }

    fun onSeatUpdated(attributeMap: Map<String, String>) {
        if (attributeMap.containsKey("gift_amount")) {
            attributeMap["gift_amount"]?.toIntOrNull()?.let {
                voiceRoomModel.giftAmount = it
                ThreadManager.getInstance().runOnMainThread {
                    iRoomTopView.onUpdateGiftCount(it)
                }
            }
        }
//        if (attributeMap.containsKey("robot_volume")) {
//            attributeMap["robot_volume"]?.toIntOrNull()?.let {
//                robotInfo.robotVolume = it
//            }
//        }
//        if (attributeMap.containsKey("use_robot")) {
//            // TODO: Magic value
//            robotInfo.useRobot = attributeMap["use_robot"] == "1"
//            ThreadManager.getInstance().runOnMainThread {
//                iRoomMicView.activeBot(robotInfo.useRobot)
//            }
//        }
        if (attributeMap.containsKey("ranking_list")) {
            val rankList = GsonTools.toList(attributeMap["ranking_list"], VoiceRankUserModel::class.java)
            rankList?.let { rankUsers ->
                ThreadManager.getInstance().runOnMainThread {
                    iRoomTopView.onRankMember(rankUsers)
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
                    micInfo?.let { micInfoMap[key] = it }
                }
            val newMicMap = RoomInfoConstructor.extendMicInfoMap(micInfoMap, roomKitBean.ownerId)
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
        // Temporary variable, prevent removal when exchanging mics
        var kvLocalUser: VoiceMicInfoModel? = null
        updateMap.forEach { (index, micInfo) ->
            val rtcUid = micInfo.member?.rtcUid ?: -1
            if (rtcUid > 0) {
                micMap[index] = rtcUid
                // Current user on mic
                if (rtcUid == VSpatialCenter.rtcUid) kvLocalUser = micInfo
            } else {
                val removeRtcUid = micMap.remove(index)
                // Current user removed from mic
                if (removeRtcUid == VSpatialCenter.rtcUid) localUserMicInfo = null
            }
        }
        kvLocalUser?.let { localUserMicInfo = it }
        AgoraRtcEngineController.get().switchRole(localUserIndex() >= 0)
        if (localUserMicInfo?.member?.micStatus == MicStatus.Normal &&
            localUserMicInfo?.micStatus == MicStatus.Normal
        ) {   // Status normal
            AgoraRtcEngineController.get().enableLocalAudio(true)
        } else {  // Other status
            AgoraRtcEngineController.get().enableLocalAudio(false)
        }
    }

    private fun activeRobotSound() {
        // Spatial audio robot plays sound effect every time
        VSpatialCenter.rtcChannelTemp.firstActiveBot = false
        AgoraRtcEngineController.get()
            .updateEffectVolume(robotInfo.robotVolume)
        RoomSoundAudioConstructor.createRoomSoundAudioMap[ConfigConstants.RoomType.Spatial_Chatroom]?.let {
            AgoraRtcEngineController.get().playMusic(it)
        }
    }

    /**
     * Update ui by mic data
     */
    private fun updateViewByMicMap(newMicMap: Map<Int, VoiceMicInfoModel>) {
        iRoomMicView.onSeatUpdated(newMicMap) {
            updateSpatialPosition(newMicMap.values)
        }
        val micIsOn = (
                localUserMicInfo?.member?.micStatus == MicStatus.Normal &&
                        localUserMicInfo?.micStatus == MicStatus.Normal)
        chatPrimaryMenuView.showMicVisible(micIsOn, localUserIndex() >= 0)
        if (roomKitBean.isOwner) {
            val handsCheckMap = mutableMapOf<Int, String>()
            newMicMap.forEach { (t, u) ->
                handsCheckMap[t] = u.member?.userId ?: ""
            }
            val handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
            handsDialog?.check(handsCheckMap)
        } else {
            chatPrimaryMenuView.setEnableHand(localUserIndex() >= 0)
            isRequesting = false
        }
    }

    private fun updateSpatialPosition(models: Collection<VoiceMicInfoModel>) {
        models.forEach { model ->
            model.member?.rtcUid?.let { uid ->
                if (uid == VSpatialCenter.rtcUid) {
                    AgoraRtcEngineController.get().updateSelfPosition(
                        floatArrayOf(model.position.x, model.position.y, 0.0f),
                        floatArrayOf(model.forward.x, model.forward.y, 0.0f),
                        floatArrayOf(-(model.forward.y), model.forward.x, 0.0f),
                    )
                } else {
                    if (!model.isSpatialSet) {
                        AgoraRtcEngineController.get().setupRemoteSpatialAudio(uid)
                        model.isSpatialSet = true
                    }
                    AgoraRtcEngineController.get().updateRemotePosition(
                        uid,
                        floatArrayOf(model.position.x, model.position.y, 0.0f),
                        floatArrayOf(model.forward.x, model.forward.y, 0.0f),
                    )
                }
            }
        }
    }

    fun handleBeforeExitRoom() {
        // Normal user leave
        val localUserIndex = localUserIndex()
        if (localUserIndex >= 0) {
            roomLivingViewModel.leaveMic(localUserIndex)
        }
        // Cancel own application for mic
        if (isRequesting) {
            roomLivingViewModel.cancelMicSeatApply(VSpatialCenter.userId)
        }
    }
}
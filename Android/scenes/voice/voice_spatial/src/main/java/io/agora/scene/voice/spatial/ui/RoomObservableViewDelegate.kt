package io.agora.scene.voice.spatial.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CompoundButton
import androidx.fragment.app.FragmentActivity
import com.google.gson.reflect.TypeToken
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.global.VoiceBuddyFactory
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.model.annotation.MicClickAction
import io.agora.scene.voice.spatial.model.annotation.MicStatus
import io.agora.scene.voice.spatial.model.constructor.RoomInfoConstructor
import io.agora.scene.voice.spatial.model.constructor.RoomSoundAudioConstructor
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.spatial.rtckit.listener.RtcMicVolumeListener
import io.agora.scene.voice.spatial.rtckit.listener.RtcSpatialPositionListener
import io.agora.scene.voice.spatial.ui.dialog.*
import io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentAlertDialog
import io.agora.scene.voice.spatial.ui.dialog.common.CommonFragmentContentDialog
import io.agora.scene.voice.spatial.ui.dialog.common.CommonSheetAlertDialog
import io.agora.scene.voice.spatial.ui.widget.mic.IRoomMicView
import io.agora.scene.voice.spatial.ui.widget.top.IRoomLiveTopView
import io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.IParserSource
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools

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
    private val chatPrimaryMenuView: io.agora.scene.voice.spatial.ui.widget.primary.ChatPrimaryMenuView, // 底部
) : IParserSource {
    companion object {
        private const val TAG = "RoomObservableDelegate"
    }

    /**麦位信息，index,rtcUid*/
    private val micMap = mutableMapOf<Int, Int>()

    private var localUserMicInfo: VoiceMicInfoModel? = null

    /**举手dialog*/
    private var handsDialog: ChatroomHandsDialog? = null

    /**申请上麦标志*/
    private var isRequesting: Boolean = false

    private var voiceRoomModel: VoiceRoomModel = VoiceRoomModel()
    private var robotInfo: RobotSpatialAudioModel = RobotSpatialAudioModel()

    private fun localUserIndex(): Int {
        return localUserMicInfo?.micIndex ?: -1
    }

    init {
        // 更新公告
        roomLivingViewModel.roomNoticeObservable().observe(activity) { response: Resource<Pair<String, Boolean>> ->
            parseResource(response, object : OnResourceParseCallback<Pair<String, Boolean>>() {
                override fun onSuccess(data: Pair<String, Boolean>?) {
                    if (data?.second != true) return
                    voiceRoomModel.announcement = data.first
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
        // 关闭机器人
        roomLivingViewModel.closeBotObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    "robot close：$data".logD()
                    if (data != true) return
                    iRoomMicView.activeBot(false, null)
                    // 关闭机器人，暂停所有音效播放
                    robotInfo.useRobot = false
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
                            robotInfo.robotVolume = it.first
                            AgoraRtcEngineController.get().updateEffectVolume(it.first)
                        }
                    }
                }
            })
        }
        /**打开/关闭 蓝色机器人空气衰减*/
        roomLivingViewModel.openBlueBotAirAbsorbObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableBlueAbsorb(data ?: false)
                }
            })
        }

        /**打开/关闭 红色机器人空气衰减*/
        roomLivingViewModel.openRedBotAirAbsorbObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableRedAbsorb(data ?: false)
                }
            })
        }

        /**打开/关闭 蓝色机器人模糊*/
        roomLivingViewModel.openBlueBotBlurObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableBlueBlur(data ?: false)
                }
            })
        }

        /**打开/关闭 红色机器人模糊*/
        roomLivingViewModel.openRedBotBlurObservable().observe(activity) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    AgoraRtcEngineController.get().enableRedBlur(data ?: false)
                }
            })
        }

        /**蓝色机器人衰减系数*/
        roomLivingViewModel.blueRobotAttenuationObservable().observe(activity) { response: Resource<Pair<Double, Boolean>> ->
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

        /**红色机器人衰减系数*/
        roomLivingViewModel.redRobotAttenuationObservable().observe(activity) { response: Resource<Pair<Double, Boolean>> ->
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
                if (rtcUid == 0) { // 自己
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
                    isRequesting = false
                }
            })
        }
        // 本地禁麦 / 取消本地禁麦
        roomLivingViewModel.muteMicObservable().observe(activity) { response: Resource<VoiceMemberModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMemberModel>() {
                override fun onSuccess(data: VoiceMemberModel?) {
                    if (data?.micStatus == MicStatus.Normal) {
                        ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_unmuted))
                    } else {
                        ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted))
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
        // 下麦
        roomLivingViewModel.leaveMicObservable().observe(activity) { response: Resource<VoiceMicInfoModel> ->
            parseResource(response, object : OnResourceParseCallback<VoiceMicInfoModel>() {
                override fun onSuccess(data: VoiceMicInfoModel?) {
                    "leave mic：${data?.micIndex}".logD()
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_off_stage))
                    data?.let {
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                        val newMicMap = mutableMapOf(it.micIndex to it)
                        dealMicDataMap(newMicMap)
                        updateViewByMicMap(newMicMap)
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
                            // 更新麦位
                            val newMicMap = mutableMapOf(it.micIndex to it)
                            dealMicDataMap(newMicMap)
                            updateViewByMicMap(newMicMap)
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
                        // 更新麦位
                        dealMicDataMap(it)
                        updateViewByMicMap(it)
                    }
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_exchange_mic_failed))
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
     * 房间概要
     */
    fun onRoomModel(voiceRoomModel: VoiceRoomModel) {
        this.voiceRoomModel = voiceRoomModel
        iRoomTopView.onChatroomInfo(voiceRoomModel)
    }

    /**
     * 房间详情
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
            // 麦位数据不为空
            voiceRoomInfo.micInfo?.let { micList ->
//                val micInfoList: List<VoiceMicInfoModel> =
//                    RoomInfoConstructor.extendMicInfoList(micList, roomKitBean.roomType, roomKitBean.ownerId)
                micList.forEach { micInfo ->
                    micInfo.member?.let { userInfo ->
                        val rtcUid = userInfo.rtcUid
                        val micIndex = micInfo.micIndex
                        if (rtcUid > 0) {
                            // 自己
                            if (rtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
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
        val isLocalAudioMute = (localUserMicInfo?.member?.micStatus != MicStatus.Normal)
        chatPrimaryMenuView.showMicVisible(isLocalAudioMute, localUserIndex() >= 0)
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
     * 展示3D空间音频欢迎页
     */
    fun showRoom3DWelcomeSheetDialog() {
        val room3DWelcomeSheetDialog = Room3DWelcomeSheetDialog()
        room3DWelcomeSheetDialog.show(activity.supportFragmentManager, "room3DWelcomeSheetDialog")
    }

    /**
     * 公告
     */
    fun onClickNotice() {
        var announcement = voiceRoomModel.announcement
        if (announcement.isNullOrEmpty()) {
            announcement = activity.getString(R.string.voice_voice_voice_chatroom_first_enter_room_notice_tips)
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
     * 音效设置
     */
    fun onAudioSettingsDialog(finishBack: () -> Unit) {
        roomAudioSettingDialog = RoomAudioSettingsSheetDialog().apply {
            arguments = Bundle().apply {
                val audioSettingsInfo = RoomAudioSettingsBean(
                    enable = roomKitBean.isOwner,
                    roomType = roomKitBean.roomType,
                    botOpen = robotInfo.useRobot,
                    botVolume = robotInfo.robotVolume,
                    soundSelection = roomKitBean.soundEffect,
                    AINSMode = VoiceBuddyFactory.get().rtcChannelTemp.AINSMode,
                    isAIAECOn = VoiceBuddyFactory.get().rtcChannelTemp.isAIAECOn,
                    isAIAGCOn = VoiceBuddyFactory.get().rtcChannelTemp.isAIAGCOn,
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
     * 变声器弹框
     */
    fun onVoiceChangerDialog(mode: Int) {

    }
    /**
     * 空间音频弹框
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
                putInt(RoomSpatialAudioSheetDialog.KEY_BLUE_ATTENUATION, (robotInfo.blueRobotAttenuation * 100.0).toInt())
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
     * 超时退出房间
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
            roomMicMangerDialog.onItemClickListener = object :
                OnItemClickListener<MicManagerBean> {
                override fun onItemClick(data: MicManagerBean, view: View, position: Int, viewType: Long) {
                    when (data.micClickAction) {
                        MicClickAction.Invite -> {
                            // 房主邀请他人
                            if (data.enable) {
                                showOwnerHandsDialog(micInfo.micIndex)
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
                            muteLocalAudio(true)
                        }
                        MicClickAction.UnMute -> {
                            //取消自己禁言
                            muteLocalAudio(false)
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
                            roomLivingViewModel.leaveMic(micInfo.micIndex)
                        }
                    }
                }
            }
            roomMicMangerDialog.show(activity.supportFragmentManager, "RoomMicManagerSheetDialog")
        } else if (micInfo.micStatus == MicStatus.Lock || micInfo.micStatus == MicStatus.LockForceMute) {
            // 座位被锁麦
            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted_by_host))
        } else if ((micInfo.micStatus == MicStatus.Idle || micInfo.micStatus == MicStatus.ForceMute) && micInfo.member == null) {
            val mineMicIndex = iRoomMicView.findMicByUid(VoiceBuddyFactory.get().getVoiceBuddy().userId())
            if (mineMicIndex >= 0) {
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
    fun onBotMicClick(content: String) {
        if (roomKitBean.isOwner) { // 房主
            if (!robotInfo.useRobot) {
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
            }
        } else { // 成员
            ToastTools.showTips(activity, activity.getString(R.string.voice_chatroom_only_host_can_change_robot))
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
    fun muteLocalAudio(mute: Boolean) {
        AgoraRtcEngineController.get().enableLocalAudio(!mute)
        roomLivingViewModel.muteLocal(mute)
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

    /**接受系统消息*/
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

    /**房主举手弹框*/
    fun showOwnerHandsDialog(micIndex: Int) {
        handsDialog = activity.supportFragmentManager.findFragmentByTag("room_hands") as ChatroomHandsDialog?
        if (handsDialog == null) {
            handsDialog = ChatroomHandsDialog.newInstance
        }
        handsDialog?.setFragmentListener(object : ChatroomHandsDialog.OnFragmentListener {
            override fun onAcceptMicSeatApply(voiceMicInfoModel: VoiceMicInfoModel) {
                // 更新麦位
                val newMicMap = mutableMapOf(voiceMicInfoModel.micIndex to voiceMicInfoModel)
                dealMicDataMap(newMicMap)
                updateViewByMicMap(newMicMap)
            }
        })
        handsDialog?.setMicIndex(micIndex)
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
                        roomLivingViewModel.cancelMicSeatApply(VoiceBuddyFactory.get().getVoiceBuddy().userId())
                    } else {
                        roomLivingViewModel.startMicSeatApply(micIndex)
                    }
                }
            }).show(activity.supportFragmentManager, "room_hands_apply")
    }

    fun handsUpdate(index: Int) {
        handsDialog?.update(index)
    }

    // 点击下方麦克风icon
    fun onClickBottomMic() {
        if (localUserMicInfo?.micStatus == MicStatus.ForceMute) {
            // 被禁言
            ToastTools.show(activity, activity.getString(R.string.voice_chatroom_mic_muted_by_host))
            return
        }
        if (localUserMicInfo?.member?.micStatus == MicStatus.Normal) {
            chatPrimaryMenuView.setEnableMic(false)
            muteLocalAudio(true)
        } else {
            chatPrimaryMenuView.setEnableMic(true)
            muteLocalAudio(false)
        }
    }

    // 点击下方举手icon
    fun onClickBottomHandUp() {
        if (roomKitBean.isOwner) {
            showOwnerHandsDialog(-1)
        } else {
            showMemberHandsDialog(-1)
        }
    }

    // 更新公告
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
//            // TODO: 魔法值
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
     * 处理麦位数据
     */
    private fun dealMicDataMap(updateMap: Map<Int, VoiceMicInfoModel>) {
        //临时变量，防止交换麦位时候被移除
        var kvLocalUser: VoiceMicInfoModel? = null
        updateMap.forEach { (index, micInfo) ->
            val rtcUid = micInfo.member?.rtcUid ?: -1
            if (rtcUid > 0) {
                micMap[index] = rtcUid
                // 当前用户在麦位上
                if (rtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) kvLocalUser = micInfo
            } else {
                val removeRtcUid = micMap.remove(index)
                // 当前用户从麦位移除
                if (removeRtcUid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) localUserMicInfo = null
            }
        }
        kvLocalUser?.let { localUserMicInfo = it }
        AgoraRtcEngineController.get().switchRole(localUserIndex() >= 0)
        if (localUserMicInfo?.member?.micStatus == MicStatus.Normal &&
            localUserMicInfo?.micStatus == MicStatus.Normal) {   // 状态正常
            AgoraRtcEngineController.get().enableLocalAudio(true)
        } else {  // 其他状态
            AgoraRtcEngineController.get().enableLocalAudio(false)
        }
    }

    private fun activeRobotSound() {
        // 创建房间，第⼀次启动机器⼈后播放音效：
        if (VoiceBuddyFactory.get().rtcChannelTemp.firstActiveBot) {
            VoiceBuddyFactory.get().rtcChannelTemp.firstActiveBot = false
            AgoraRtcEngineController.get()
                .updateEffectVolume(robotInfo.robotVolume)
            RoomSoundAudioConstructor.createRoomSoundAudioMap[roomKitBean.roomType]?.let {
                AgoraRtcEngineController.get().playMusic(it)
            }
        }
    }

    /**
     * 根据麦位数据更新ui
     */
    private fun updateViewByMicMap(newMicMap: Map<Int, VoiceMicInfoModel>) {
        iRoomMicView.onSeatUpdated(newMicMap) {
            updateSpatialPosition(newMicMap.values)
        }
        val isLocalAudioMute = (localUserMicInfo?.member?.micStatus != MicStatus.Normal)
        chatPrimaryMenuView.showMicVisible(isLocalAudioMute, localUserIndex() >= 0)
        if (roomKitBean.isOwner) {
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

    private fun updateSpatialPosition(models: Collection<VoiceMicInfoModel>) {
        models.forEach{ model ->
            model.member?.rtcUid?.let { uid ->
                if (uid == VoiceBuddyFactory.get().getVoiceBuddy().rtcUid()) {
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
        // 普通用户离开
        val localUserIndex = localUserIndex()
        if (localUserIndex >= 0) {
            roomLivingViewModel.leaveMic(localUserIndex)
        }
        // 取消自己的上麦申请
        if (isRequesting) {
            roomLivingViewModel.cancelMicSeatApply(VoiceBuddyFactory.get().getVoiceBuddy().userId())
        }
    }
}
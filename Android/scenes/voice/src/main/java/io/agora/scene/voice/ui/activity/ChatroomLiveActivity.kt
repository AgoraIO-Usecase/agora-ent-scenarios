package io.agora.scene.voice.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.reflect.TypeToken
import io.agora.CallBack
import io.agora.Error
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.LogUploader
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.voice.R
import io.agora.scene.voice.VoiceLogger
import io.agora.scene.voice.databinding.VoiceActivityChatroomBinding
import io.agora.scene.voice.imkit.bean.ChatMessageData
import io.agora.scene.voice.imkit.custorm.CustomMsgHelper
import io.agora.scene.voice.imkit.custorm.OnMsgCallBack
import io.agora.scene.voice.imkit.manager.ChatroomIMManager
import io.agora.scene.voice.model.VoiceMemberModel
import io.agora.scene.voice.model.VoiceMicInfoModel
import io.agora.scene.voice.model.VoiceRoomInfo
import io.agora.scene.voice.model.VoiceRoomModel
import io.agora.scene.voice.model.constructor.RoomInfoConstructor.convertByRoomInfo
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.service.VoiceRoomServiceKickedReason
import io.agora.scene.voice.service.VoiceServiceListenerProtocol
import io.agora.scene.voice.service.VoiceServiceProtocol
import io.agora.scene.voice.ui.RoomGiftViewDelegate
import io.agora.scene.voice.ui.RoomObservableViewDelegate
import io.agora.scene.voice.ui.debugSettings.OnDebugSettingCallback
import io.agora.scene.voice.ui.debugSettings.VoiceDebugSettingModel
import io.agora.scene.voice.ui.debugSettings.VoiceRoomDebugOptionsDialog
import io.agora.scene.voice.ui.widget.barrage.ChatroomMessagesView
import io.agora.scene.voice.ui.widget.primary.MenuItemClickListener
import io.agora.scene.voice.ui.widget.top.OnLiveTopClickListener
import io.agora.scene.voice.viewmodel.VoiceRoomLivingViewModel
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.dialog.showRoomDurationNotice
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.UiUtils
import io.agora.scene.voice.global.ConfigConstants
import io.agora.scene.voice.netkit.OnResourceParseCallback
import io.agora.scene.voice.netkit.Resource
import io.agora.scene.voice.ui.IParserSource
import io.agora.scene.voice.global.GsonTools
import io.agora.scene.voice.global.VoiceCenter
import io.agora.scene.widget.utils.StatusBarUtil

class ChatroomLiveActivity : BaseViewBindingActivity<VoiceActivityChatroomBinding>(), VoiceServiceListenerProtocol,
    IParserSource {

    companion object {
        const val KEY_VOICE_ROOM_MODEL = "voice_chat_room_model"
        const val TAG = "ChatroomLiveActivity"

        fun startActivity(activity: Activity, voiceRoomModel: AUIRoomInfo) {
            val intent = Intent(activity, ChatroomLiveActivity::class.java).apply {
                putExtra(KEY_VOICE_ROOM_MODEL, voiceRoomModel)
            }
            activity.startActivity(intent)
        }
    }

    /**room viewModel*/
    private lateinit var roomLivingViewModel: VoiceRoomLivingViewModel
    private lateinit var giftViewDelegate: RoomGiftViewDelegate
    private val voiceServiceProtocol = VoiceServiceProtocol.serviceProtocol
    private var isActivityStop = false

    /**
     * 代理头部view以及麦位view
     */
    private lateinit var roomObservableDelegate: RoomObservableViewDelegate


    /**房间基础*/
    private val voiceRoomModel = VoiceRoomModel()
    private val dialogFragments = mutableListOf<BottomSheetDialogFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showRoomDurationNotice(SceneConfigManager.chatExpireTime)
    }

    override fun getViewBinding(inflater: LayoutInflater): VoiceActivityChatroomBinding {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        return VoiceActivityChatroomBinding.inflate(inflater)
    }

    override fun onStop() {
        isActivityStop = true
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        isActivityStop = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (SceneConfigManager.logUpload) {
            LogUploader.uploadLog(LogUploader.SceneType.CHAT)
        }
    }

//    /** voice room info */
//    private val rtmRoomInfo: AUIRoomInfo by lazy {
//        intent.getSerializableExtra(KEY_VOICE_ROOM_MODEL) as AUIRoomInfo
//    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, true)
        roomLivingViewModel = ViewModelProvider(this)[VoiceRoomLivingViewModel::class.java]
        giftViewDelegate =
            RoomGiftViewDelegate.getInstance(this, roomLivingViewModel, binding.chatroomGiftView, binding.svgaView)

        val rtmRoomInfo: AUIRoomInfo = intent.getSerializableExtra(KEY_VOICE_ROOM_MODEL) as AUIRoomInfo

        voiceRoomModel.convertByRoomInfo(rtmRoomInfo)
        initView()
        giftViewDelegate.onRoomDetails(voiceRoomModel.roomId, voiceRoomModel.owner?.userId)
        ChatroomIMManager.getInstance().init(voiceRoomModel.chatroomId, voiceRoomModel.isOwner)
        ChatroomIMManager.getInstance().saveWelcomeMsg(
            getString(R.string.voice_room_welcome),
            VoiceCenter.nickname
        )

//        binding.messageView.refreshSelectLast()
        if (voiceRoomModel.isOwner) {
            toggleAudioRun = Runnable {
                VoiceLogger.d(TAG, "onPermissionGrant initSdkJoin")
                roomLivingViewModel.initSdkJoin(this, voiceRoomModel)
            }
            requestRecordPermission(true)
        } else {
            roomLivingViewModel.initSdkJoin(this, voiceRoomModel)
        }
    }

    private var toggleAudioRun: Runnable? = null

    fun toggleSelfAudio(isOpen: Boolean, callback: () -> Unit) {
        if (isOpen) {
            toggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission,
            { getPermissions() }
        ) { launchAppSetting(permission) }
    }

    override fun getPermissions() {
        if (toggleAudioRun != null) {
            toggleAudioRun?.run()
            toggleAudioRun = null
        }
    }

    override fun initListener() {
        // 房间详情
        roomLivingViewModel.roomDetailsObservable().observe(this) { response: Resource<VoiceRoomInfo> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomInfo>() {

                override fun onLoading(data: VoiceRoomInfo?) {
                    super.onLoading(data)
                    showLoadingView()
                }

                override fun onHideLoading() {
                    super.onHideLoading()
                    hideLoadingView()
                }

                override fun onSuccess(data: VoiceRoomInfo?) {
                    data?.let {
                        roomObservableDelegate.onRoomDetails(it)
                    }
                }
            })
        }
        roomLivingViewModel.joinObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {

                override fun onSuccess(data: Boolean?) {
                    CustomToast.show(R.string.voice_chatroom_join_room_success)
                    roomLivingViewModel.fetchRoomDetail(voiceRoomModel)
                    voiceServiceProtocol.fetchRoomMembers( completion = { error, result ->
                    })
                    CustomMsgHelper.getInstance().sendSystemMsg(
                        voiceRoomModel.owner?.chatUid, object : OnMsgCallBack() {
                            override fun onSuccess(message: ChatMessageData?) {
                                VoiceLogger.d(TAG, "sendSystemMsg onSuccess $message")
                                binding.messageView.refreshSelectLast()
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                VoiceLogger.e(TAG, "sendSystemMsg onFail $code $error")
                            }
                        }
                    )
                }

                override fun onError(code: Int, message: String?) {
                    voiceServiceProtocol.leaveRoom {  }
                    CustomToast.show(
                        message ?: getString(R.string.voice_chatroom_join_room_failed)
                    )
                    binding.root.postDelayed({
                        finish()
                    }, 1000)
                }
            })
        }
        roomLivingViewModel.updateRoomMemberObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
                override fun onSuccess(data: Boolean?) {
                    VoiceLogger.d(TAG, "ChatroomLiveActivity updateRoomMember onSuccess")
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    VoiceLogger.e(TAG, "ChatroomLiveActivity updateRoomMember onError $code $message")
                }
            })
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View?, insets: WindowInsetsCompat ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.clMain.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.clMain.setOnTouchListener { v, event ->
            resetUI()
            false
        }
        binding.messageView.setMessageViewListener(object : ChatroomMessagesView.MessageViewListener {
            override fun onItemClickListener(message: ChatMessageData?) {
            }

            override fun onListClickListener() {
                resetUI()
            }
        })
        voiceServiceProtocol.subscribeListener(object : VoiceServiceListenerProtocol {

            override fun onChatTokenWillExpire() {
                roomLivingViewModel.renewChatToken()
            }

            override fun onReceiveGift(roomId: String, message: ChatMessageData?) {
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                VoiceLogger.d(TAG, "onReceiveGift $roomId ${message?.content}")
                binding.chatroomGiftView.refresh()
                if (CustomMsgHelper.getInstance().getMsgGiftId(message).equals("VoiceRoomGift9")) {
                    giftViewDelegate.showGiftAction()
                    binding.subtitle.showSubtitleView(
                        resources.getString(
                            R.string.voice_chatroom_gift_notice,
                            ChatroomIMManager.getInstance().getUserName(message), voiceRoomModel.owner?.nickName
                        )
                    )
                }
                roomObservableDelegate.receiveGift(voiceRoomModel.roomId, message)
            }

            override fun onReceiveTextMsg(roomId: String, message: ChatMessageData?) {
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                VoiceLogger.d(TAG, "onReceiveTextMsg $roomId ${message?.content}")
                binding.messageView.refreshSelectLast()
            }

            override fun onReceiveSeatRequest(message: ChatMessageData) {
                VoiceLogger.d(TAG, "onReceiveSeatRequest ${voiceRoomModel.isOwner}")
                binding.chatBottom.setShowHandStatus(voiceRoomModel.isOwner, true)
            }

            override fun onReceiveSeatRequestRejected(chatUid: String) {
                VoiceLogger.d(TAG, "onReceiveSeatRequestRejected $chatUid")
                //刷新 owner 申请列表
                roomObservableDelegate.handsUpdate(0)
            }

            override fun onReceiveSeatInvitation(message: ChatMessageData) {
                VoiceLogger.d(TAG, "onReceiveSeatInvitation $message")
                if (message.customParams.containsKey("user")) {
                    val voiceRoomInvite = GsonTools.toBean(message.customParams["user"], VoiceMemberModel::class.java)
                    if (voiceRoomInvite != null) {
                        roomObservableDelegate.receiveInviteSite(voiceRoomModel.roomId, voiceRoomInvite.micIndex)
                    }
                }

            }

            override fun onReceiveSeatInvitationRejected(chatUid: String, message: ChatMessageData?) {
                VoiceLogger.d(TAG, "onReceiveSeatInvitationRejected $chatUid ${message?.content}")
            }

            override fun onAnnouncementChanged(roomId: String, content: String) {
                VoiceLogger.d(TAG, "onAnnouncementChanged $content")
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                roomObservableDelegate.updateAnnouncement(content)
            }

            override fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                VoiceLogger.d(TAG, "onUserJoinedRoom $roomId, ${voiceMember.chatUid}")
                if (voiceRoomModel.isOwner) {
                    ChatroomIMManager.getInstance().setMemberList(voiceMember)
                    roomLivingViewModel.updateRoomMember()
                    roomObservableDelegate.onUserJoinedRoom()
                }
                binding.messageView.refreshSelectLast()
            }

            override fun onUserLeftRoom(roomId: String, chatUid: String) {
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                VoiceLogger.d(TAG, "onUserLeftRoom $roomId, $chatUid")
                if (voiceRoomModel.isOwner) {
                    ChatroomIMManager.getInstance().removeMember(chatUid)
                    //当成员已申请上麦 未经过房主同意退出时 申请列表移除该成员
                    ChatroomIMManager.getInstance().removeSubmitMember(chatUid)
                    //刷新 owner 邀请列表
                    roomObservableDelegate.handsUpdate(1)
                    //刷新 owner 申请列表
                    roomObservableDelegate.handsUpdate(0)
                    roomLivingViewModel.updateRoomMember()
                    roomObservableDelegate.checkUserLeaveMic(
                        ChatroomIMManager.getInstance().getMicIndexByChatUid(chatUid)
                    )
                }
            }

            override fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {
                if (!TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                VoiceLogger.d(TAG, "userBeKicked $reason")
                if (reason == VoiceRoomServiceKickedReason.destroyed) {
                    CustomToast.show(R.string.voice_room_close)
                    leaveRoom()
                } else if (reason == VoiceRoomServiceKickedReason.removed) {
                    CustomToast.show(R.string.voice_room_kick_member)
                    leaveRoom()
                }
            }

            override fun onAttributeMapUpdated(roomId: String, attributeMap: Map<String, String>, fromId: String) {
                VoiceLogger.d(
                    TAG,
                    "roomAttributesDidUpdated ${Thread.currentThread()},roomId:$roomId,fromId:$fromId,map:$attributeMap"
                )
                if (isFinishing || !TextUtils.equals(voiceRoomModel.chatroomId, roomId)) return
                ChatroomIMManager.getInstance().updateMicInfoCache(attributeMap)
                roomObservableDelegate.onAttributeMapUpdated(attributeMap)
                attributeMap
                    .filter { it.key.startsWith("mic_") }
                    .forEach { (key, value) ->
                        val micInfo =
                            GsonTools.toBean<VoiceMicInfoModel>(value, object : TypeToken<VoiceMicInfoModel>() {}.type)
                        micInfo ?: return@forEach
                        if (micInfo.member?.chatUid != null) {
                            if (ChatroomIMManager.getInstance().checkMember(micInfo.member?.chatUid)) {
                                ChatroomIMManager.getInstance().removeSubmitMember(micInfo.member?.chatUid)
                                //刷新 owner 申请列表
                                roomObservableDelegate.handsUpdate(0)
                            }
                            if (ChatroomIMManager.getInstance().checkInvitationMember(micInfo.member?.chatUid)) {
                                //刷新 owner 邀请列表
                                roomObservableDelegate.handsUpdate(1)
                            }
                        }
                    }
            }

            override fun onRoomDestroyed(roomId: String) {
                roomObservableDelegate.onTimeUpExitRoom(
                    getString(R.string.voice_room_close), finishBack = {
                        if (voiceRoomModel.isOwner) {
                            leaveRoom()
                        } else {
                            roomObservableDelegate.checkUserLeaveMic()
                            leaveRoom()
                        }
                    })
            }

            override fun onRoomRoomExpire(roomId: String) {
                super.onRoomRoomExpire(roomId)
                roomObservableDelegate.onTimeUpExitRoom(
                    getString(R.string.voice_chatroom_time_up_tips), finishBack = {
                        if (voiceRoomModel.isOwner) {
                            leaveRoom()
                        } else {
                            roomObservableDelegate.checkUserLeaveMic()
                            leaveRoom()
                        }
                    })
            }

            override fun onSyncUserCountUpdate(userCount: Int) {
                VoiceLogger.d(TAG, "onSyncUserCountUpdate 1 ${voiceRoomModel.memberCount}")
                voiceRoomModel.memberCount = userCount
                VoiceLogger.d(TAG, "onSyncUserCountUpdate 2 ${voiceRoomModel.memberCount}")
                binding.cTopView.onUpdateMemberCount(voiceRoomModel.memberCount)
            }
        })
    }

    private fun initView() {
        binding.chatBottom.initMenu(ConfigConstants.RoomType.Common_Chatroom)
        binding.likeView.likeView.setOnClickListener { binding.likeView.addFavor() }
        binding.chatroomGiftView.init(voiceRoomModel.chatroomId)
        binding.messageView.init(voiceRoomModel.chatroomId, voiceRoomModel.owner?.chatUid)
        binding.rvChatroom2dMicLayout.isVisible = true
        roomObservableDelegate =
            RoomObservableViewDelegate(
                this,
                roomLivingViewModel,
                voiceRoomModel,
                binding.cTopView,
                binding.rvChatroom2dMicLayout,
                binding.chatBottom
            )
        binding.rvChatroom2dMicLayout.setMyRtcUid(VoiceCenter.rtcUid)
        binding.rvChatroom2dMicLayout.onItemClickListener(
            object :
                OnItemClickListener<VoiceMicInfoModel> {
                override fun onItemClick(
                    data: VoiceMicInfoModel,
                    view: View,
                    position: Int,
                    viewType: Long
                ) {
                    roomObservableDelegate.onUserMicClick(data)
                }
            },
            object :
                OnItemClickListener<VoiceMicInfoModel> {
                override fun onItemClick(
                    data: VoiceMicInfoModel,
                    view: View,
                    position: Int,
                    viewType: Long
                ) {
                    roomObservableDelegate.onBotMicClick(getString(R.string.voice_chatroom_open_bot_prompt)) {
                        finish()
                    }
                }
            }
        ).setUpInitAdapter()
        binding.cTopView.setOnLiveTopClickListener(object : OnLiveTopClickListener {
            override fun onClickBack(view: View) {
                onBackPressed()
            }

            override fun onClickRank(view: View, pageIndex: Int) {
                roomObservableDelegate.onClickRank(pageIndex)
            }

            override fun onClickNotice(view: View) {
                roomObservableDelegate.onClickNotice()
            }

            override fun onClickSoundSocial(view: View) {
                roomObservableDelegate.onClickSoundSocial(voiceRoomModel.soundEffect, finishBack = {
                    leaveRoom()
                })
            }

            override fun onClickMore(view: View) {
                TopFunctionDialog(this@ChatroomLiveActivity).show()
            }
        })
        binding.chatBottom.setMenuItemOnClickListener(object :
            MenuItemClickListener {
            override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                when (itemId) {
                    R.id.voice_extend_item_eq -> {
                        roomObservableDelegate.onAudioSettingsDialog(finishBack = {
                            leaveRoom()
                        })
                    }

                    R.id.voice_extend_item_mic -> {
                        roomObservableDelegate.onClickBottomMic()
                    }

                    R.id.voice_extend_item_hand_up -> {
                        roomObservableDelegate.onClickBottomHandUp()
                    }

                    R.id.voice_extend_item_gift -> {
                        giftViewDelegate.showGiftDialog(object : OnMsgCallBack() {
                            override fun onSuccess(message: ChatMessageData?) {
                                roomObservableDelegate.onSendGiftSuccess(voiceRoomModel.roomId, message)
                                if (CustomMsgHelper.getInstance().getMsgGiftId(message).equals("VoiceRoomGift9")) {
                                    binding.subtitle.showSubtitleView(
                                        resources.getString(
                                            R.string.voice_chatroom_gift_notice,
                                            ChatroomIMManager.getInstance().getUserName(message),
                                            voiceRoomModel.owner?.nickName
                                        )
                                    )
                                }
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                CustomToast.show(R.string.voice_chatroom_send_gift_fail)
                            }
                        })
                    }
                }
            }

            override fun onInputLayoutClick() {
                checkFocus(false)
            }

            override fun onSendMessage(content: String?) {
                if (!content.isNullOrEmpty())
                    ChatroomIMManager.getInstance().sendTxtMsg(content,
                        VoiceCenter.nickname, object : OnMsgCallBack() {
                            override fun onSuccess(message: ChatMessageData?) {
                                binding.root.post {
                                    binding.messageView.refreshSelectLast()
                                    binding.likeView.isVisible = true
                                }
                            }

                            override fun onError(code: Int, error: String?) {
                                VoiceLogger.e(TAG, "onSendMessage onError  $code $error")
                                binding.likeView.isVisible = true
                                if (code == Error.MODERATION_FAILED) {
                                    CustomToast.show(R.string.voice_room_content_prohibited)
                                }
                            }
                        })
            }
        })

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                if (f is BottomSheetDialogFragment) {
                    if (dialogFragments.contains(f)) {
                        return
                    }
                    val lastFragment = dialogFragments.lastOrNull()
                    dialogFragments.add(f)
                    lastFragment?.dismiss()
                }
                super.onFragmentStarted(fm, f)
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                super.onFragmentStopped(fm, f)
                if (isActivityStop) {
                    return
                }
                if (f is BottomSheetDialogFragment) {
                    val lastFragment = dialogFragments.lastOrNull()
                    if (lastFragment == f) {
                        dialogFragments.remove(f)
                        dialogFragments.lastOrNull()?.let {
                            it.show(fm, it.tag)
                        }
                    }
                }
            }
        }, true)
        // debug 模式
        if (AgoraApplication.the().isDebugModeOpen) {
            binding.btnDebug.isVisible = true
            VoiceRoomDebugOptionsDialog.debugMode()
        } else {
            binding.btnDebug.isVisible = false
        }
        binding.btnDebug.setOnClickListener {
            if (UiUtils.isFastClick()) return@setOnClickListener
            showDebugDialog()
        }
    }

    private fun showDebugDialog() {
        VoiceDebugSettingModel.callback = object : OnDebugSettingCallback {
            override fun onNsEnable(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsEnable()
                }
            }

            override fun onAinsToLoadFlag(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateAinsToLoadFlag()
                }
            }

            override fun onNsngAlgRoute(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngAlgRoute()
                }
            }

            override fun onNsngPredefAgg(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngPredefAgg()
                }
            }

            override fun onNsngMapInMaskMin(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngMapInMaskMin()
                }
            }

            override fun onNsngMapOutMaskMin(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngMapOutMaskMin()
                }
            }

            override fun onStatNsLowerBound(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateStatNsLowerBound()
                }
            }

            override fun onNsngFinalMaskLowerBound(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngFinalMaskLowerBound()
                }
            }

            override fun onStatNsEnhFactor(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateStatNsEnhFactor()
                }
            }

            override fun onStatNsFastNsSpeechTrigThreshold(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateStatNsFastNsSpeechTrigThreshold()
                }
            }

            override fun onAedEnable(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMusicMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateAedEnable()
                }
            }

            override fun onNsngMusicProbThr(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMusicMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateNsngMusicProbThr()
                }
            }

            override fun onStatNsMusicModeBackoffDB(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMusicMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateStatNsMusicModeBackoffDB()
                }
            }

            override fun onAinsMusicModeBackoffDB(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMusicMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateAinsMusicModeBackoffDB()
                }
            }

            override fun onAinsSpeechProtectThreshold(newValue: Int) {
                // 自定义
                if (VoiceCenter.rtcChannelTemp.AINSMicMode == ConfigConstants.AINSMode.AINS_Custom) {
                    AgoraRtcEngineController.get().updateAinsSpeechProtectThreshold()
                }
            }
        }
        VoiceRoomDebugOptionsDialog().show(supportFragmentManager, "mtDebug")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (binding.chatBottom.showNormalLayout()) {
            return
        }
        if (voiceRoomModel.isOwner) {
            roomObservableDelegate.onExitRoom(
                getString(R.string.voice_chatroom_end_live),
                getString(R.string.voice_chatroom_end_live_tips), finishBack = {
                    leaveRoom()
                })
        } else {
            roomObservableDelegate.checkUserLeaveMic()
            leaveRoom()
        }
    }

    private fun leaveRoom() {
        if (voiceRoomModel.isOwner) {
            ChatroomIMManager.getInstance().asyncDestroyChatRoom(voiceRoomModel.chatroomId, object :
                CallBack {
                override fun onSuccess() {}

                override fun onError(code: Int, error: String?) {}
            })
        }
        roomLivingViewModel.leaveSyncManagerRoom()
        finish()
    }

    override fun finish() {
        ChatroomIMManager.getInstance().leaveChatRoom(voiceRoomModel.chatroomId)
        ChatroomIMManager.getInstance().removeChatRoomChangeListener()
        ChatroomIMManager.getInstance().clearCache()
        binding.chatroomGiftView.clear()
        roomObservableDelegate.destroy()
        voiceServiceProtocol.unsubscribeListener()
        binding.subtitle.clearTask()
        dialogFragments.clear()
        super.finish()
    }

    private fun resetUI() {
        binding.chatBottom.hideExpressionView(false)
        hideInput()
        binding.chatBottom.showInput()
        binding.likeView.isVisible = true
        binding.chatBottom.hindViewChangeIcon()
    }

    private fun checkFocus(focus: Boolean) {
        binding.likeView.isVisible = focus
    }
}
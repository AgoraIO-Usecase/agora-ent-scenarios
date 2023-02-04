package io.agora.scene.voice.spatial.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
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
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialActivityChatroomBinding
import io.agora.scene.voice.spatial.global.VoiceBuddyFactory
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.model.constructor.RoomInfoConstructor.convertByVoiceRoomModel
import io.agora.scene.voice.spatial.service.VoiceRoomServiceKickedReason
import io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.widget.top.OnLiveTopClickListener
import io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.BaseUiActivity
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.StatusBarCompat
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

class ChatroomLiveActivity : BaseUiActivity<VoiceSpatialActivityChatroomBinding>(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks, VoiceRoomSubscribeDelegate {

    companion object {
        const val RC_PERMISSIONS = 101
        const val KEY_VOICE_ROOM_MODEL = "voice_chat_room_model"
        const val TAG = "ChatroomLiveActivity"

        fun startActivity(activity: Activity, voiceRoomModel: VoiceRoomModel) {
            val intent = Intent(activity, ChatroomLiveActivity::class.java).apply {
                putExtra(KEY_VOICE_ROOM_MODEL, voiceRoomModel)
            }
            activity.startActivity(intent)
        }
    }

    /**room viewModel*/
    private lateinit var roomLivingViewModel: VoiceRoomLivingViewModel
    private lateinit var giftViewDelegate: io.agora.scene.voice.spatial.ui.RoomGiftViewDelegate
    private val voiceServiceProtocol = VoiceServiceProtocol.getImplInstance()

    /**
     * 代理头部view以及麦位view
     */
    private lateinit var roomObservableDelegate: io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate

    /** voice room info */
    private val voiceRoomModel: VoiceRoomModel by lazy {
        intent.getSerializableExtra(KEY_VOICE_ROOM_MODEL) as VoiceRoomModel
    }

    /**房间基础*/
    private val roomKitBean = RoomKitBean()

    override fun getViewBinding(inflater: LayoutInflater): VoiceSpatialActivityChatroomBinding {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        return VoiceSpatialActivityChatroomBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarCompat.setLightStatusBar(this, false)
        roomLivingViewModel = ViewModelProvider(this)[VoiceRoomLivingViewModel::class.java]
        giftViewDelegate =
            io.agora.scene.voice.spatial.ui.RoomGiftViewDelegate.getInstance(this, roomLivingViewModel, binding.chatroomGiftView, binding.svgaView)
        initListeners()
        initData()
        initView()
        requestAudioPermission()
    }

    private fun initData() {
        roomKitBean.convertByVoiceRoomModel(voiceRoomModel)
        giftViewDelegate.onRoomDetails(roomKitBean.roomId, roomKitBean.ownerId)
        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().init(roomKitBean.chatroomId)
        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().saveWelcomeMsg(
            getString(R.string.voice_room_welcome),
            VoiceBuddyFactory.get().getVoiceBuddy().nickName()
        )
        binding.messageView.refreshSelectLast()
//        roomLivingViewModel.fetchRoomDetail(voiceRoomModel)
    }

    private fun initListeners() {
        // 房间详情
        roomLivingViewModel.roomDetailsObservable().observe(this) { response: Resource<VoiceRoomInfo> ->
            parseResource(response, object : OnResourceParseCallback<VoiceRoomInfo>() {

                override fun onLoading(data: VoiceRoomInfo?) {
                    super.onLoading(data)
                    showLoading(false)
                }

                override fun onHideLoading() {
                    super.onHideLoading()
                    dismissLoading()
                }

                override fun onSuccess(data: VoiceRoomInfo?) {
                    data?.let {
                        roomObservableDelegate.onRoomDetails(it)
                        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().setMemberList(
                            io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().mySelfModel)
                    }
                }
            })
        }
        roomLivingViewModel.joinObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {

                override fun onSuccess(data: Boolean?) {
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_chatroom_join_room_success))
                    roomLivingViewModel.fetchRoomDetail(voiceRoomModel)
                    io.agora.scene.voice.spatial.imkit.custorm.CustomMsgHelper.getInstance().sendSystemMsg(
                        roomKitBean.ownerChatUid, object : io.agora.scene.voice.spatial.imkit.custorm.OnMsgCallBack() {
                            override fun onSuccess(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
                                "sendSystemMsg onSuccess $message".logD()
                                binding.messageView.refreshSelectLast()
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                "sendSystemMsg onFail $code $error".logE()
                            }
                        }
                    )
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(
                        this@ChatroomLiveActivity,
                        message ?: getString(R.string.voice_chatroom_join_room_failed)
                    )
                    ThreadManager.getInstance().runOnMainThreadDelay({
                        finish()
                    }, 1000)
                }
            })
        }
        roomLivingViewModel.updateRoomMemberObservable().observe(this){ response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>(){
                override fun onSuccess(data: Boolean?) {
                    "ChatroomLiveActivity updateRoomMember onSuccess".logD()
                }

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    "ChatroomLiveActivity updateRoomMember onError $code $message".logE()
                }
            })
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View?, insets: WindowInsetsCompat ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            "systemInset:left:${systemInset.left},top:${systemInset.top},right:${systemInset.right},bottom:${systemInset.bottom}".logD(
                "insets=="
            )
            binding.clMain.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.clMain.setOnTouchListener { v, event ->
            reset()
            false
        }
        binding.messageView.setMessageViewListener(object : io.agora.scene.voice.spatial.ui.widget.barrage.ChatroomMessagesView.MessageViewListener {
            override fun onItemClickListener(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
            }

            override fun onListClickListener() {
                reset()
            }
        })
        voiceServiceProtocol.subscribeEvent(object : VoiceRoomSubscribeDelegate{
            override fun onReceiveGift(roomId: String, message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
                super.onReceiveGift(roomId, message)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "onReceiveGift $roomId ${message?.content}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    binding.chatroomGiftView.refresh()
                    if (io.agora.scene.voice.spatial.imkit.custorm.CustomMsgHelper.getInstance().getMsgGiftId(message).equals("VoiceRoomGift9")) {
                        giftViewDelegate.showGiftAction()
                    }
                    roomObservableDelegate.receiveGift(roomKitBean.roomId,message)
                }
            }

            override fun onReceiveTextMsg(roomId: String, message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
                super.onReceiveTextMsg(roomId, message)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "onReceiveTextMsg $roomId ${message?.content}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    binding.messageView.refreshSelectLast()
                }
            }

            override fun onReceiveSeatRequest(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData) {
                super.onReceiveSeatRequest(message)
                "onReceiveSeatRequest ${roomKitBean.isOwner}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    binding.chatBottom.setShowHandStatus(roomKitBean.isOwner, true)
                }
            }

            override fun onReceiveSeatRequestRejected(chatUid: String) {
                super.onReceiveSeatRequestRejected(chatUid)
                "onReceiveSeatRequestRejected $chatUid".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    //刷新 owner 申请列表
                    roomObservableDelegate.handsUpdate(0)
                }
            }

            override fun onReceiveSeatInvitation(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData) {
                super.onReceiveSeatInvitation(message)
                "onReceiveSeatInvitation $message".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    roomObservableDelegate.receiveInviteSite(roomKitBean.roomId, -1)
                }
            }

            override fun onReceiveSeatInvitationRejected(
                chatUid: String,
                message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?
            ) {
                super.onReceiveSeatInvitationRejected(chatUid, message)
                "onReceiveSeatInvitationRejected $chatUid ${message?.content}".logD(TAG)
            }

            override fun onAnnouncementChanged(roomId: String, content: String) {
                super.onAnnouncementChanged(roomId, content)
                "onAnnouncementChanged $content".logD(TAG)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                ThreadManager.getInstance().runOnMainThread {
                    roomObservableDelegate.updateAnnouncement(content)
                }
            }

            override fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {
                super.onUserJoinedRoom(roomId, voiceMember)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "onUserJoinedRoom $roomId, ${voiceMember.chatUid}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    voiceRoomModel.memberCount = voiceRoomModel.memberCount + 1
                    voiceRoomModel.clickCount = voiceRoomModel.clickCount + 1
                    binding.cTopView.onUpdateMemberCount(voiceRoomModel.memberCount)
                    binding.cTopView.onUpdateWatchCount(voiceRoomModel.clickCount)
                    voiceMember.let {
                        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().setMemberList(it)
                    }
                    binding.messageView.refreshSelectLast()
                }
            }

            override fun onUserLeftRoom(roomId: String, chatUid: String) {
                super.onUserLeftRoom(roomId, chatUid)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "onUserLeftRoom $roomId, $chatUid".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    chatUid.let {
                        if (roomKitBean.isOwner){
                            io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().removeMember(it)
                            //当成员已申请上麦 未经过房主同意退出时 申请列表移除该成员
                            io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().removeSubmitMember(it)
                            //刷新 owner 邀请列表
                            roomObservableDelegate.handsUpdate(1)
                            //刷新 owner 申请列表
                            roomObservableDelegate.handsUpdate(0)
                            roomLivingViewModel.updateRoomMember()
                            roomObservableDelegate.checkUserLeaveMic(io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().getMicIndexByChatUid(it))
                        }
                    }
                    voiceRoomModel.memberCount = voiceRoomModel.memberCount - 1
                    binding.cTopView.onUpdateMemberCount(voiceRoomModel.memberCount)
                }
            }

            override fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {
                super.onUserBeKicked(roomId, reason)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "userBeKicked $reason".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    if (reason == VoiceRoomServiceKickedReason.destroyed) {
                        ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_room_close))
                        finish()
                    } else if (reason == VoiceRoomServiceKickedReason.removed) {
                        ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_room_kick_member))
                        finish()
                    }
                }
            }

            override fun onSeatUpdated(
                roomId: String,
                attributeMap: Map<String, String>,
                fromId: String
            ) {
                super.onSeatUpdated(roomId, attributeMap, fromId)
                "roomAttributesDidUpdated ${Thread.currentThread()},roomId:$roomId,fromId:$fromId,map:$attributeMap".logD()
                if (isFinishing || !TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                attributeMap.let {
                    io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().updateMicInfoCache(it)
                    roomObservableDelegate.onSeatUpdated(it)
                }
                attributeMap
                    .filter { it.key.startsWith("mic_") }
                    .forEach { (key, value) ->
                        val micInfo =
                            GsonTools.toBean<VoiceMicInfoModel>(value, object : TypeToken<VoiceMicInfoModel>() {}.type)
                        micInfo?.let {
                            if(it.member?.chatUid != null){
                                if (io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().checkMember(it.member?.chatUid)){
                                    io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().removeSubmitMember(it.member?.chatUid)
                                    ThreadManager.getInstance().runOnMainThread {
                                        //刷新 owner 申请列表
                                        roomObservableDelegate.handsUpdate(0)
                                    }
                                }
                                if (io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().checkInvitationMember(it.member?.chatUid)){
                                    ThreadManager.getInstance().runOnMainThread {
                                        //刷新 owner 邀请列表
                                        roomObservableDelegate.handsUpdate(1)
                                    }
                                }
                            }
                        }
                    }
            }

            override fun onRoomDestroyed(roomId: String) {
                super.onRoomDestroyed(roomId)
                if (!TextUtils.equals(roomKitBean.chatroomId, roomId)) return
                "onRoomDestroyed $roomId".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_room_close))
                    finish()
                }
            }
        })
    }

    private fun initView() {
        binding.chatBottom.initMenu(roomKitBean.roomType)
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) { // 普通房间
            binding.likeView.likeView.setOnClickListener { binding.likeView.addFavor() }
            binding.chatroomGiftView.init(roomKitBean.chatroomId)
            binding.messageView.init(roomKitBean.chatroomId, roomKitBean.ownerChatUid)
            binding.rvChatroom2dMicLayout.isVisible = true
            binding.rvChatroom3dMicLayout.isVisible = false
            roomObservableDelegate =
                io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate(
                    this,
                    roomLivingViewModel,
                    roomKitBean,
                    binding.cTopView,
                    binding.rvChatroom2dMicLayout,
                    binding.chatBottom
                )
            binding.rvChatroom2dMicLayout.setMyRtcUid(VoiceBuddyFactory.get().getVoiceBuddy().rtcUid())
            binding.rvChatroom2dMicLayout.onItemClickListener(
                object :
                    OnItemClickListener<VoiceMicInfoModel> {
                    override fun onItemClick(data: VoiceMicInfoModel, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onUserMicClick(data)
                    }
                },
                object :
                    OnItemClickListener<VoiceMicInfoModel> {
                    override fun onItemClick(data: VoiceMicInfoModel, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onBotMicClick(getString(R.string.voice_chatroom_open_bot_prompt))
                    }
                }
            ).setUpInitAdapter()
        } else { // 空间音效房间
            binding.likeView.isVisible = false
            binding.rvChatroom2dMicLayout.isVisible = false
            binding.rvChatroom3dMicLayout.isVisible = true
            roomObservableDelegate =
                io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate(
                    this,
                    roomLivingViewModel,
                    roomKitBean,
                    binding.cTopView,
                    binding.rvChatroom3dMicLayout,
                    binding.chatBottom
                )
            binding.rvChatroom3dMicLayout.setMyRtcUid(VoiceBuddyFactory.get().getVoiceBuddy().rtcUid())
            binding.rvChatroom3dMicLayout.onItemClickListener(
                object :
                    OnItemClickListener<VoiceMicInfoModel> {
                    override fun onItemClick(data: VoiceMicInfoModel, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onUserMicClick(data)
                    }
                },
                object :
                    OnItemClickListener<VoiceMicInfoModel> {
                    override fun onItemClick(data: VoiceMicInfoModel, view: View, position: Int, viewType: Long) {
                        roomObservableDelegate.onBotMicClick(getString(R.string.voice_chatroom_open_bot_prompt))
                    }
                },
            ).setUpInitMicInfoMap()
        }
        binding.cTopView.setTitleMaxWidth()
//        roomObservableDelegate.onRoomModel(voiceRoomModel)
        binding.cTopView.hideRoomGifts()
        binding.cTopView.setOnLiveTopClickListener(object : OnLiveTopClickListener {
            override fun onClickBack(view: View) {
                onBackPressed()
            }

            override fun onClickRank(view: View) {
                roomObservableDelegate.onClickRank()
            }

            override fun onClickNotice(view: View) {
                roomObservableDelegate.onClickNotice()
            }

            override fun onClickSoundSocial(view: View) {
                roomObservableDelegate.onClickSoundSocial(roomKitBean.soundEffect, finishBack = {
                    finish()
                })
            }
        })
        binding.chatBottom.setMenuItemOnClickListener(object :
            io.agora.scene.voice.spatial.ui.widget.primary.MenuItemClickListener {
            override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
                when (itemId) {
                    R.id.voice_extend_item_eq -> {
                        roomObservableDelegate.onAudioSettingsDialog(finishBack = {
                            finish()
                        })
                    }
                    R.id.voice_extend_item_mic -> {
                        roomObservableDelegate.onClickBottomMic()
                    }
                    R.id.voice_extend_item_hand_up -> {
                        roomObservableDelegate.onClickBottomHandUp()
                    }
                    R.id.voice_extend_item_gift -> {
                        giftViewDelegate.showGiftDialog(object : io.agora.scene.voice.spatial.imkit.custorm.OnMsgCallBack() {
                            override fun onSuccess(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
                                roomObservableDelegate.onSendGiftSuccess(roomKitBean.roomId,message)
                            }

                            override fun onError(messageId: String?, code: Int, error: String?) {
                                ToastTools.show(
                                    this@ChatroomLiveActivity,
                                    getString(R.string.voice_chatroom_send_gift_fail)
                                )
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
                    io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().sendTxtMsg(content,
                        VoiceBuddyFactory.get().getVoiceBuddy().nickName(),
                        object : io.agora.scene.voice.spatial.imkit.custorm.OnMsgCallBack() {
                            override fun onSuccess(message: io.agora.scene.voice.spatial.imkit.bean.ChatMessageData?) {
                                ThreadManager.getInstance().runOnMainThread {
                                    binding.messageView.refreshSelectLast()
                                    binding.likeView.isVisible = true
                                }
                            }

                            override fun onError(code: Int, error: String?) {
                                "onSendMessage onError  $code $error".logE(TAG)
                                binding.likeView.isVisible = true
                                if (code == Error.MODERATION_FAILED){
                                    ToastTools.show(this@ChatroomLiveActivity,
                                        getString(R.string.voice_room_content_prohibited,Toast.LENGTH_SHORT)
                                    )
                                }
                            }
                        })
            }
        })

        supportFragmentManager.registerFragmentLifecycleCallbacks(object: FragmentManager.FragmentLifecycleCallbacks(){
            private val dialogFragments = mutableListOf<BottomSheetDialogFragment>()
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
                if(f is BottomSheetDialogFragment){
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
    }

    override fun onBackPressed() {
        if (binding.chatBottom.showNormalLayout()) {
            return
        }
        if (roomKitBean.isOwner) {
            roomObservableDelegate.onExitRoom(
                getString(R.string.voice_chatroom_end_live),
                getString(R.string.voice_chatroom_end_live_tips), finishBack = {
                    finish()
                })
        } else {
            roomObservableDelegate.checkUserLeaveMic()
            finish()
        }
    }

    override fun finish() {
        binding.chatroomGiftView.clear()
        roomObservableDelegate.destroy()
        voiceServiceProtocol.unsubscribeEvent()
        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().clearCache()
        if (roomKitBean.isOwner) {
            io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().asyncDestroyChatRoom(roomKitBean.chatroomId, object :
                CallBack {
                override fun onSuccess() {}

                override fun onError(code: Int, error: String?) {}
            })
        }
        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().leaveChatRoom(roomKitBean.chatroomId)
        roomLivingViewModel.leaveSyncManagerRoom(roomKitBean.roomId)
        io.agora.scene.voice.spatial.imkit.manager.ChatroomIMManager.getInstance().logout(false)
        super.finish()
    }

    private fun requestAudioPermission() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            onPermissionGrant()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_PERMISSIONS, *perms).build())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun onPermissionGrant() {
        "onPermissionGrant initSdkJoin".logD(TAG)
        roomLivingViewModel.initSdkJoin(roomKitBean)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        "onPermissionsGranted requestCode$requestCode $perms".logD(TAG)
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        "onPermissionsDenied $perms ".logD()
    }

    override fun onRationaleAccepted(requestCode: Int) {
        "onRationaleAccepted requestCode$requestCode ".logD(TAG)
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onRationaleDenied(requestCode: Int) {
        "onRationaleDenied requestCode$requestCode ".logD(TAG)
    }

    private fun reset() {
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) {
            binding.chatBottom.hideExpressionView(false)
            hideKeyboard()
            binding.chatBottom.showInput()
            binding.likeView.isVisible = true
            binding.chatBottom.hindViewChangeIcon()
        }
    }

    private fun checkFocus(focus: Boolean) {
        binding.likeView.isVisible = focus
    }
}
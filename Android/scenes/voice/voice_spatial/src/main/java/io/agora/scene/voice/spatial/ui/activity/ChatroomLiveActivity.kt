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
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.spatial.service.VoiceRoomServiceKickedReason
import io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.dialog.Room3DWelcomeSheetDialog
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
import java.util.*

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
    private var isRoomOwnerLeave = false
    /** 空间位置信息 */
    private var spatialSeatInfo: SeatPositionInfo? = null
    /** 空间位置同步timer */
    private var spatialTimer: Timer? = null

    override fun getViewBinding(inflater: LayoutInflater): VoiceSpatialActivityChatroomBinding {
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        return VoiceSpatialActivityChatroomBinding.inflate(inflater)
    }

    override fun onDestroy() {
        super.onDestroy()
        setSpatialSeatInfo(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarCompat.setLightStatusBar(this, false)
        roomLivingViewModel = ViewModelProvider(this)[VoiceRoomLivingViewModel::class.java]

        initListeners()
        initData()
        initView()
        requestAudioPermission()
    }

    private fun initData() {
        roomKitBean.convertByVoiceRoomModel(voiceRoomModel)
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
                    }
                }
            })
        }
        roomLivingViewModel.joinObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {

                override fun onSuccess(data: Boolean?) {
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_chatroom_join_room_success))
                    roomLivingViewModel.fetchRoomDetail(voiceRoomModel)
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
        voiceServiceProtocol.subscribeEvent(object : VoiceRoomSubscribeDelegate{

            override fun onReceiveSeatRequest() {
                super.onReceiveSeatRequest()
                "onReceiveSeatRequest ${roomKitBean.isOwner}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    binding.chatBottom.setShowHandStatus(roomKitBean.isOwner, true)
                }
            }

            override fun onReceiveSeatRequestRejected(userId: String) {
                super.onReceiveSeatRequestRejected(userId)
                "onReceiveSeatRequestRejected $userId".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    //刷新 owner 申请列表
                    roomObservableDelegate.handsUpdate(0)
                }
            }

            override fun onReceiveSeatInvitation() {
                super.onReceiveSeatInvitation()
                ThreadManager.getInstance().runOnMainThread {
                    roomObservableDelegate.receiveInviteSite(roomKitBean.roomId, -1)
                }
            }

            override fun onReceiveSeatInvitationRejected(userId: String) {
                super.onReceiveSeatInvitationRejected(userId)
            }

            override fun onAnnouncementChanged(roomId: String, content: String) {
                super.onAnnouncementChanged(roomId, content)
                "onAnnouncementChanged $content".logD(TAG)
                if (!TextUtils.equals(roomKitBean.roomId, roomId)) return
                ThreadManager.getInstance().runOnMainThread {
                    roomObservableDelegate.updateAnnouncement(content)
                }
            }

            override fun onUserJoinedRoom(roomId: String, voiceMember: VoiceMemberModel) {
                super.onUserJoinedRoom(roomId, voiceMember)
                if (!TextUtils.equals(roomKitBean.roomId, roomId)) return
                "onUserJoinedRoom $roomId, ${voiceMember.userId}".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    voiceRoomModel.memberCount = voiceRoomModel.memberCount + 1
                    voiceRoomModel.clickCount = voiceRoomModel.clickCount + 1
                    binding.cTopView.onUpdateMemberCount(voiceRoomModel.memberCount)
                    binding.cTopView.onUpdateWatchCount(voiceRoomModel.clickCount)
                }
            }

            override fun onUserLeftRoom(roomId: String, userId: String) {
                super.onUserLeftRoom(roomId, userId)
                if (!TextUtils.equals(roomKitBean.roomId, roomId)) return
                "onUserLeftRoom $roomId, $userId".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    userId.let {
                        if (roomKitBean.isOwner){
                            //刷新 owner 邀请列表
                            roomObservableDelegate.handsUpdate(1)
                            //刷新 owner 申请列表
                            roomObservableDelegate.handsUpdate(0)
                        }
                    }
                    voiceRoomModel.memberCount = voiceRoomModel.memberCount - 1
                    binding.cTopView.onUpdateMemberCount(voiceRoomModel.memberCount)
                }
            }

            override fun onUserBeKicked(roomId: String, reason: VoiceRoomServiceKickedReason) {
                super.onUserBeKicked(roomId, reason)
                if (!TextUtils.equals(roomKitBean.roomId, roomId)) return
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
                attributeMap: Map<String, String>
            ) {
                super.onSeatUpdated(roomId, attributeMap)
                "roomAttributesDidUpdated ${Thread.currentThread()},roomId:$roomId,map:$attributeMap".logD()
                if (isFinishing || !TextUtils.equals(roomKitBean.roomId, roomId)) return
                attributeMap.let {
                    roomObservableDelegate.onSeatUpdated(it)
                }
                attributeMap
                    .filter { it.key.startsWith("mic_") }
                    .forEach { (key, value) ->
                        val micInfo =
                            GsonTools.toBean<VoiceMicInfoModel>(value, object : TypeToken<VoiceMicInfoModel>() {}.type)
                        micInfo?.let {
                            ThreadManager.getInstance().runOnMainThread {
                                //刷新 owner 申请列表
                                roomObservableDelegate.handsUpdate(0)
                                //刷新 owner 邀请列表
                                roomObservableDelegate.handsUpdate(1)
                            }
                        }
                    }
            }

            override fun onRoomDestroyed(roomId: String) {
                super.onRoomDestroyed(roomId)
                if (!TextUtils.equals(roomKitBean.roomId, roomId)) return
                "onRoomDestroyed $roomId".logD(TAG)
                ThreadManager.getInstance().runOnMainThread {
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_room_close))
                    isRoomOwnerLeave = true
                    finish()
                }
            }

            override fun onRobotUpdate(roomId: String, robotInfo: RobotSpatialAudioModel) {
                super.onRobotUpdate(roomId, robotInfo)
                if (isFinishing || !TextUtils.equals(roomKitBean.roomId, roomId)) return
                roomObservableDelegate.onRobotUpdated(robotInfo)
            }
        })

        voiceServiceProtocol.subscribeRoomTimeUp {
            roomObservableDelegate.onTimeUpExitRoom(
                getString(R.string.voice_chatroom_time_up_tips), finishBack = {
                    if (roomKitBean.isOwner) {
                        finish()
                    } else {
                        roomObservableDelegate.checkUserLeaveMic()
                        finish()
                    }
                })
        }
    }

    private fun initView() {
        binding.chatBottom.initMenu(roomKitBean.roomType)
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) { // 普通房间
            binding.likeView.likeView.setOnClickListener { binding.likeView.addFavor() }
            binding.rvChatroom3dMicLayout.isVisible = false
        } else { // 空间音效房间
            binding.likeView.isVisible = false
            binding.rvChatroom3dMicLayout.isVisible = true
            roomObservableDelegate = io.agora.scene.voice.spatial.ui.RoomObservableViewDelegate(
                this,
                roomLivingViewModel,
                roomKitBean,
                binding.cTopView,
                binding.rvChatroom3dMicLayout,
                binding.chatBottom
            )
            roomObservableDelegate.showRoom3DWelcomeSheetDialog()
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
                object : OnItemMoveListener<VoiceMicInfoModel> {
                    override fun onItemMove(data: VoiceMicInfoModel, position: SeatPositionInfo, viewType: Long) {
                        super.onItemMove(data, position, viewType)
                        val right = floatArrayOf(-position.forward[1], -position.forward[0], 0f)
                        AgoraRtcEngineController.get().updateSelfPosition(
                            floatArrayOf(position.x, position.y, 0f),
                            position.forward,
                            right
                        )
                        setSpatialSeatInfo(position)
                    }
                }
            ).setUpInitMicInfoMap()
//            binding.rvChatroom3dMicLayout
        }
        binding.cTopView.setTitleMaxWidth()
//        roomObservableDelegate.onRoomModel(voiceRoomModel)
        binding.cTopView.setRoomType(roomKitBean.roomType)
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
                if (roomKitBean.roomType == ConfigConstants.RoomType.Spatial_Chatroom) {
                    Room3DWelcomeSheetDialog.needShow = true
                    roomObservableDelegate.showRoom3DWelcomeSheetDialog()
                    return
                }
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
                }
            }

            override fun onInputLayoutClick() {
                checkFocus(false)
            }

            override fun onSendMessage(content: String?) {
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

    fun setSpatialSeatInfo(info: SeatPositionInfo?) {
        val oldValue = spatialSeatInfo
        spatialSeatInfo = info
        if (oldValue == null && info != null) {
            spatialTimer = Timer().apply {
                schedule(object: TimerTask() {
                    override fun run() {
                        spatialSeatInfo?.let {
                            AgoraRtcEngineController.get().sendSelfPosition(it)
                        }
                    }
                }, 0, 100)
            }
        } else if (oldValue != null && info == null) {
            spatialTimer?.cancel()
            val defaultSeat = SeatPositionInfo(
                VoiceBuddyFactory.get().getVoiceBuddy().rtcUid(),
                floatArrayOf(0f, -1f, 0f),
                0f,
                0f,
                0f
            )
            AgoraRtcEngineController.get().sendSelfPosition(defaultSeat)
        }
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
        roomObservableDelegate.destroy()
        voiceServiceProtocol.unsubscribeEvent()
        roomLivingViewModel.leaveSyncManagerRoom(roomKitBean.roomId, isRoomOwnerLeave)
        isRoomOwnerLeave = false
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
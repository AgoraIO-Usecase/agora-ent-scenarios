package io.agora.scene.voice.spatial.ui.activity

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
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialActivityChatroomBinding
import io.agora.scene.voice.spatial.global.VoiceBuddyFactory
import io.agora.scene.voice.spatial.model.*
import io.agora.scene.voice.spatial.model.constructor.RoomInfoConstructor.convertByVoiceRoomModel
import io.agora.scene.voice.spatial.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.spatial.service.VoiceRoomServiceKickedReason
import io.agora.scene.voice.spatial.service.VoiceRoomSubscribeDelegate
import io.agora.scene.voice.spatial.service.VoiceServiceProtocol
import io.agora.scene.voice.spatial.ui.dialog.VoiceRoomDebugOptionsDialog
import io.agora.scene.voice.spatial.ui.widget.primary.MenuItemClickListener
import io.agora.scene.voice.spatial.ui.widget.top.OnLiveTopClickListener
import io.agora.scene.voice.spatial.viewmodel.VoiceRoomLivingViewModel
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.net.OnResourceParseCallback
import io.agora.voice.common.net.Resource
import io.agora.voice.common.ui.IParserSource
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.GsonTools
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.LogTools.logE
import io.agora.voice.common.utils.StatusBarCompat
import io.agora.voice.common.utils.ThreadManager
import io.agora.voice.common.utils.ToastTools
import java.util.*

class ChatroomLiveActivity : BaseViewBindingActivity<VoiceSpatialActivityChatroomBinding>(), VoiceRoomSubscribeDelegate,
    IParserSource {

    companion object {
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
    private var isActivityStop = false

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
        setSpatialSeatInfo(null)
    }

    private var toggleAudioRun: Runnable? = null

    fun toggleSelfAudio(isOpen: Boolean, callback : () -> Unit) {
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

                override fun onError(code: Int, message: String?) {
                    super.onError(code, message)
                    "roomDetailsObservable onError -- code=$code, message=$message".logD()
                }
            })
        }
        roomLivingViewModel.joinObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {

                override fun onSuccess(data: Boolean?) {
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_spatial_join_room_success))
                    roomLivingViewModel.fetchRoomDetail(voiceRoomModel)
                }

                override fun onError(code: Int, message: String?) {
                    ToastTools.show(
                        this@ChatroomLiveActivity,
                        message ?: getString(R.string.voice_spatial_join_room_failed)
                    )
                    ThreadManager.getInstance().runOnMainThreadDelay({
                        finish()
                    }, 1000)
                }
            })
        }
        roomLivingViewModel.updateRoomMemberObservable().observe(this) { response: Resource<Boolean> ->
            parseResource(response, object : OnResourceParseCallback<Boolean>() {
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
        voiceServiceProtocol.subscribeEvent(object : VoiceRoomSubscribeDelegate {

            override fun onReceiveSeatRequest() {
                super.onReceiveSeatRequest()
                "onReceiveSeatRequest ${roomKitBean.isOwner}".logD(TAG)
                if (roomKitBean.isOwner) {
                    ThreadManager.getInstance().runOnMainThread {
                        binding.chatBottom.setShowHandStatus(true, true)
                    }
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
                        if (roomKitBean.isOwner) {
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
                        ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_spatial_room_close))
                        finish()
                    } else if (reason == VoiceRoomServiceKickedReason.removed) {
                        ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_spatial_room_kick_member))
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
                    ToastTools.show(this@ChatroomLiveActivity, getString(R.string.voice_spatial_room_close))
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
                getString(R.string.voice_spatial_chatroom_time_up_tips), finishBack = {
                    if (roomKitBean.isOwner) {
                        finish()
                    } else {
                        roomObservableDelegate.handleBeforeExitRoom()
                        finish()
                    }
                })
        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarCompat.setLightStatusBar(this, false)
        roomLivingViewModel = ViewModelProvider(this)[VoiceRoomLivingViewModel::class.java]
        roomKitBean.convertByVoiceRoomModel(voiceRoomModel)

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
                        roomObservableDelegate.onBotMicClick(getString(R.string.voice_spatial_open_bot_prompt))
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
        }
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
                roomObservableDelegate.showRoom3DWelcomeSheetDialog()
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

        supportFragmentManager.registerFragmentLifecycleCallbacks(object :
            FragmentManager.FragmentLifecycleCallbacks() {
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
            VoiceRoomDebugOptionsDialog().show(supportFragmentManager, "mtDebug")
        }
        if (roomKitBean.isOwner) {
            toggleAudioRun =  Runnable {
                "onPermissionGrant initSdkJoin".logD(TAG)
                roomLivingViewModel.initSdkJoin(roomKitBean)
            }
            requestRecordPermission(true)
        } else {
            roomLivingViewModel.initSdkJoin(roomKitBean)
        }
    }

    fun setSpatialSeatInfo(info: SeatPositionInfo?) {
        val oldValue = spatialSeatInfo
        spatialSeatInfo = info
        if (oldValue == null && info != null) {
            spatialTimer = Timer().apply {
                schedule(object : TimerTask() {
                    override fun run() {
                        spatialSeatInfo?.let {
                            AgoraRtcEngineController.get().sendSelfPosition(it)
                        }
                    }
                }, 0, 50)
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
                getString(R.string.voice_spatial_end_live),
                getString(R.string.voice_spatial_end_live_tips), finishBack = {
                    finish()
                })
        } else {
            roomObservableDelegate.handleBeforeExitRoom()
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

    private fun reset() {
        if (roomKitBean.roomType == ConfigConstants.RoomType.Common_Chatroom) {
            binding.chatBottom.hideExpressionView(false)
            hideInput()
            binding.chatBottom.showInput()
            binding.likeView.isVisible = true
            binding.chatBottom.hindViewChangeIcon()
        }
    }

    private fun checkFocus(focus: Boolean) {
        binding.likeView.isVisible = focus
    }
}
package io.agora.scene.pure1v1.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.pure1v1.CallServiceManager
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.audio.AudioScenarioType
import io.agora.scene.pure1v1.audio.SceneType
import io.agora.scene.pure1v1.callapi.*
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListActivityBinding
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListItemLayoutBinding
import io.agora.scene.pure1v1.rtt.PureRttApiManager
import io.agora.scene.pure1v1.rtt.PureRttManager
import io.agora.scene.pure1v1.service.UserInfo
import io.agora.scene.pure1v1.ui.base.CallDialog
import io.agora.scene.pure1v1.ui.base.CallDialogState
import io.agora.scene.pure1v1.ui.calling.CallReceiveDialog
import io.agora.scene.pure1v1.ui.calling.CallSendDialog
import io.agora.scene.pure1v1.ui.debug.DebugSettingsDialog
import io.agora.scene.pure1v1.ui.living.CallDetailFragment
import io.agora.scene.pure1v1.utils.PermissionHelp
import io.agora.scene.widget.dialog.PermissionLeakDialog
import org.json.JSONException
import org.json.JSONObject

/*
 * 1v1 房间列表 activity
 */
class RoomListActivity : BaseViewBindingActivity<Pure1v1RoomListActivityBinding>(), ICallApiListener {

    private val tag = "RoomListActivity_LOG"

    private val kRoomListSwipeGuide = "io.agora.RoomListSwipeGuide"

    private var adapter: RoomListAdapter? = null

    private var dataList: List<UserInfo> = listOf()

    private var callState = CallStateType.Idle

    private var callDialog: CallDialog? = null

    private var callSendDialog: CallSendDialog? = null

    private var debugSettingsDialog: DebugSettingsDialog? = null

    private val permissionHelp = PermissionHelp(this)

    private var mCallDetailFragment: Fragment? = null

    private var isFirstEnterScene = true

    private var isOnline = true

    override fun getViewBinding(inflater: LayoutInflater): Pure1v1RoomListActivityBinding {
        return Pure1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onDestroy() {
        adapter = null
        callDialog = null
        mCallDetailFragment = null
        callSendDialog = null
        CallServiceManager.instance.cleanUp()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 准备通话中的Fragment
        binding.flCallContainer.isVisible = false
        val callDetailFragment = CallDetailFragment()
        supportFragmentManager.beginTransaction().add(R.id.flCallContainer, callDetailFragment, "CallDetailFragment").show(callDetailFragment).commit()
        mCallDetailFragment = callDetailFragment

        // 准备来电秀Fragment
        binding.flSendFragment.isVisible = false
        val callSendFragment = CallSendDialog(this)
        callSendFragment.setListener(object : CallSendDialog.CallSendDialogListener {
            override fun onSendViewDidClickHangup() {
                CallServiceManager.instance.callApi?.cancelCall {}
            }
        })
        supportFragmentManager.beginTransaction().add(R.id.flSendFragment, callSendFragment, "CallSendFragment").show(callSendFragment).commit()
        callSendDialog = callSendFragment

        setOnApplyWindowInsetsListener()
        setupView()

        CallServiceManager.instance.setup(this) {
            if (it) {
                CallServiceManager.instance.sceneService?.enterRoom { e ->
                    if (e == null) {
                        isOnline = true
                        fetchRoomList(false)
                    } else {
                        isOnline = false
                        Pure1v1Logger.e(tag, null, "enter room failed: ${e.message}")
                        Toast.makeText(this, getText(R.string.pure1v1_room_list_local_offline), Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                isOnline = false
                Toast.makeText(this, getText(R.string.pure1v1_room_list_local_offline), Toast.LENGTH_SHORT).show()
            }
        }
        CallServiceManager.instance.callApi?.addListener(this)
        CallServiceManager.instance.onUserChanged = {
            fetchRoomList(false)
        }
    }

    private fun setOnApplyWindowInsetsListener() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _: View, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPaddingRelative(0, 0, 0, inset.bottom)
            binding.titleView.setPaddingRelative(0, inset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
    }

    override fun onBackPressed() {
        CallServiceManager.instance.cleanUp()
        super.onBackPressed()
    }

    override fun onRestart() {
        super.onRestart()
        // 如果在房间列表页面锁屏停留超过20h，需要重新获取token
        if (CallServiceManager.instance.rtcToken != "" && TimeUtils.currentTimeMillis() - CallServiceManager.instance.lastTokenFetchTime >= CallServiceManager.instance.tokenExpireTime) {
            CallServiceManager.instance.rtcToken = ""
            CallServiceManager.instance.rtmToken = ""
            CallServiceManager.instance.fetchToken {
                CallServiceManager.instance.renewRtmToken()
            }
        }
        binding.smartRefreshLayout.autoRefresh()
    }

    private fun fetchRoomList(isAutoRefresh: Boolean) {
        CallServiceManager.instance.sceneService?.getUserList { msg, list ->
            // 用户是否在线
            if (!isOnline) {
                CallServiceManager.instance.setup(this) {
                    if (it) {
                        CallServiceManager.instance.sceneService?.enterRoom { e ->
                            if (e == null) {
                                isOnline = true
                                fetchRoomList(false)
                            } else {
                                isOnline = false
                                Pure1v1Logger.e(tag, null, "enter room failed: ${e.message}")
                                Toast.makeText(this, getText(R.string.pure1v1_room_list_local_offline), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        isOnline = false
                        Toast.makeText(this, getText(R.string.pure1v1_room_list_local_offline), Toast.LENGTH_SHORT).show()
                    }
                }
                binding.smartRefreshLayout.finishRefresh()
                return@getUserList
            }
            if (!binding.flCallContainer.isVisible) {
                if (msg != null) {
                    Toast.makeText(this, getString(R.string.pure1v1_room_list_refreshed, msg), Toast.LENGTH_SHORT).show()
                } else {
                    //Toast.makeText(this, getText(R.string.pure1v1_room_list_refresh), Toast.LENGTH_SHORT).show()
                }
            }
            dataList = list.filter { it.userId != UserManager.getInstance().user.id.toString() && it.userName != ""}
            adapter?.refresh(dataList)
            if (dataList.isNotEmpty()) {
                // 刷新后直接定位到首个
                binding.viewPager2.setCurrentItem(0, false)
            }
            mayShowGuideView()

            if (isAutoRefresh) {
                binding.smartRefreshLayout.finishRefresh()
            }
            isFirstEnterScene = false
        }
    }

    private var guided = SPUtil.getBoolean(kRoomListSwipeGuide, false)
    private fun mayShowGuideView() {
        if (dataList.isEmpty() || guided) {
            return
        }
        binding.vGuidance.visibility = View.VISIBLE
        binding.vGuidance.setOnClickListener {
            SPUtil.putBoolean(kRoomListSwipeGuide, true)
            guided = true
            binding.vGuidance.visibility = View.GONE
        }
    }

    private fun call(user: UserInfo) {
        if (callState == CallStateType.Failed) {
            CallServiceManager.instance.reInit()
        } else if (callState == CallStateType.Calling || callState == CallStateType.Connecting || callState == CallStateType.Connected) {
            return
        }
        permissionHelp.checkCameraAndMicPerms({
            // 准备工作
            CallServiceManager.instance.prepareForCall {
                // 拨打
                CallServiceManager.instance.callApi?.call(user.userId.toInt()) { error ->
                    if (error != null && callState == CallStateType.Calling) {
                        Toast.makeText(this, getString(R.string.pure1v1_call_failed, error.msg), Toast.LENGTH_SHORT).show()
                        // call 失败立刻挂断
                        CallServiceManager.instance.callApi?.cancelCall {  }
                    }
                }
            }
        }, {
            PermissionLeakDialog(this).show("", { getPermissions() }
            ) { launchAppSetting(Manifest.permission.CAMERA) }
        }, false)
    }

    private fun connectCallDetail() {
        binding.flCallContainer.isVisible = true

        val channelId =  CallServiceManager.instance.connectedChannelId ?: ""
        val localUid = CallServiceManager.instance.localUser?.userId?.toInt() ?: 0

        mCallDetailFragment?.let {
            (mCallDetailFragment as CallDetailFragment).start()
            (mCallDetailFragment as CallDetailFragment).updateTime()
            (mCallDetailFragment as CallDetailFragment).initDashBoard(channelId, localUid)
        }

        // 开启鉴黄鉴暴
        setupContentInspectConfig(true, RtcConnection(channelId, localUid))
        moderationAudio()
    }

    private fun showCallSendDialog(user: UserInfo) {
        binding.flSendFragment.isVisible = true
        callSendDialog?.initView(user)
    }

    private fun finishCallDialog() {
        binding.flSendFragment.isVisible = false
        callSendDialog?.hangUp()

        callDialog?.dismiss()
        callDialog = null
    }

    // ----------------------- ICallApiListener -----------------------
    override fun onCallError(
        errorEvent: CallErrorEvent,
        errorType: CallErrorCodeType,
        errorCode: Int,
        message: String?
    ) {
        super.onCallError(errorEvent, errorType, errorCode, message)
        Pure1v1Logger.d(tag, "onCallError: errorEvent$errorEvent, errorType:$errorType, errorCode:$errorCode, message:$message")
    }

    override fun canJoinRtcOnCalling(eventInfo: Map<String, Any>): Boolean {
        return true
    }

    // 监听 callapi 内的状态变化驱动业务行为
    override fun onCallStateChanged(
        state: CallStateType,
        stateReason: CallStateReason,
        eventReason: String,
        eventInfo: Map<String, Any>
    ) {
        val currentUid = CallServiceManager.instance.localUser?.userId ?: ""
        val publisher = eventInfo[CallApiImpl.kPublisher] as? String ?: currentUid
        if (publisher != currentUid) {return}
        callState = state
        when (state) {
            CallStateType.Calling -> {
                val fromUserId = eventInfo[CallApiImpl.kFromUserId] as? Int ?: 0
                val fromRoomId = eventInfo[CallApiImpl.kFromRoomId] as? String ?: ""
                val toUserId = eventInfo[CallApiImpl.kRemoteUserId] as? Int ?: 0
                val remoteUser = CallServiceManager.instance.remoteUser
                //Log.d("shsh", "toUserId=$toUserId, fromUserId=$fromUserId, currentUid=$currentUid eventInfo=$eventInfo")
                if (remoteUser != null && remoteUser.userId != fromUserId.toString())  {
                    CallServiceManager.instance.callApi?.reject(fromUserId, "already calling") { err ->
                        Pure1v1Logger.d(tag, "callApi reject failed: $err")
                    }
                    return
                }
                // 触发状态的用户是自己才处理
                if (currentUid == toUserId.toString()) {
                    CallServiceManager.instance.isCaller = false
                    CallServiceManager.instance.connectedChannelId = fromRoomId
                    var user = dataList.firstOrNull { it.userId == fromUserId.toString() }
                    if (user == null) {
                        val userMap = eventInfo[CallApiImpl.kFromUserExtension] as JSONObject
                        user = UserInfo()
                        user.userId = userMap.getString("userId")
                        user.userName = userMap.getString("userName")
                        user.avatar = userMap.getString("avatar")
                    }
                    if (user.userId.isEmpty()) { return } // 检验数据是否有效
                    CallServiceManager.instance.remoteUser = user

                    PureRttManager.resetRttSettings(false)
                    PureRttManager.targetUid = user.userId

                    val dialog = CallReceiveDialog(this, user)
                    dialog.setListener(object : CallReceiveDialog.CallReceiveDialogListener {
                        override fun onReceiveViewDidClickAccept() { // 点击接通
                            if (CallServiceManager.instance.rtcToken != "") {
                                CallServiceManager.instance.callApi?.accept(fromUserId) {
                                    if (it != null) {
                                        Toast.makeText(this@RoomListActivity, getString(R.string.pure1v1_accept_failed, it.msg), Toast.LENGTH_SHORT).show()
                                        // 如果接受消息出错，则发起拒绝，回到初始状态
                                        CallServiceManager.instance.callApi?.reject(fromUserId, it.msg) {}
                                    }
                                }
                            }
                        }
                        override fun onReceiveViewDidClickReject() {
                            CallServiceManager.instance.callApi?.reject(fromUserId, "reject by user") {
                            }
                        }
                    })
                    dialog.show()
                    callDialog = dialog

                    // 获取多媒体权限
                    permissionHelp.checkCameraAndMicPerms({}, {
                        PermissionLeakDialog(this@RoomListActivity).show("", { getPermissions() }) { launchAppSetting(Manifest.permission.CAMERA) }
                    }, false)

                    // 被叫播放来点音乐
                    // TODO bug CallServiceManager.instance.rtcEngine?.startAudioMixing(CallServiceManager.callMusic, true, -1, 0)
                    CallServiceManager.instance.playCallMusic(CallServiceManager.callMusic)
                } else if (currentUid == fromUserId.toString()) {
                    CallServiceManager.instance.isCaller = true
                    CallServiceManager.instance.connectedChannelId = fromRoomId
                    val user = dataList.firstOrNull { it.userId == toUserId.toString() } ?: return
                    CallServiceManager.instance.remoteUser = user

                    PureRttManager.resetRttSettings(true)
                    PureRttManager.targetUid = user.userId

                    // 主叫显示来电秀UI
                    showCallSendDialog(user)
                }

                // 设置视频最佳实践
                CallServiceManager.instance.rtcEngine?.setVideoEncoderConfigurationEx(
                    VideoEncoderConfiguration().apply {
                        dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                        frameRate = 24
                        degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                    },
                    RtcConnection(CallServiceManager.instance.connectedChannelId, currentUid.toInt())
                )
                CallServiceManager.instance.rtcEngine?.setParameters("{\"che.video.videoCodecIndex\": 2}")
            }
            CallStateType.Connecting -> {
                callSendDialog?.updateCallState(CallDialogState.Connecting)
                callDialog?.updateCallState(CallDialogState.Connecting)

                // 停止来点秀视频和铃声
                CallServiceManager.instance.stopCallShow()
                // TODO bug CallServiceManager.instance.rtcEngine?.stopAudioMixing()
                CallServiceManager.instance.stopCallMusic()
            }
            CallStateType.Connected -> {
                if (CallServiceManager.instance.remoteUser == null) { return }
                // 进入通话页面
                connectCallDetail()
                finishCallDialog()

                // 设置音频最佳实践（防止卡主线程，将大量sdk调用后置）
                binding.root.postDelayed( {
                    if (CallServiceManager.instance.isCaller) {
                        // 主叫
                        CallServiceManager.instance.scenarioApi?.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Caller)
                    } else {
                        // 被叫
                        CallServiceManager.instance.scenarioApi?.setAudioScenario(SceneType.Chat, AudioScenarioType.Chat_Callee)
                    }
                }, 500)
            }
            CallStateType.Prepared -> {
                when (stateReason) {
                    CallStateReason.RemoteHangup -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_hangup), Toast.LENGTH_SHORT).show()
                    }
                    CallStateReason.LocalRejected -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_local_rejected), Toast.LENGTH_SHORT).show()
                    }
                    CallStateReason.RemoteRejected -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_rejected), Toast.LENGTH_SHORT).show()
                    }
                    CallStateReason.CallingTimeout -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_no_answer), Toast.LENGTH_SHORT).show()
                    }
                    CallStateReason.RemoteCallBusy -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_remote_busy), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                CallServiceManager.instance.remoteUser = null
                CallServiceManager.instance.connectedChannelId = null
                finishCallDialog()
                (mCallDetailFragment as CallDetailFragment).reset()
                binding.flCallContainer.isVisible = false

                // 停止来点秀视频和铃声
                CallServiceManager.instance.stopCallShow()
                CallServiceManager.instance.stopCallMusic()
                // TODO bug CallServiceManager.instance.rtcEngine?.stopAudioMixing()
            }
            CallStateType.Failed -> {
                Toast.makeText(this, eventReason, Toast.LENGTH_SHORT).show()
                CallServiceManager.instance.remoteUser = null
                CallServiceManager.instance.connectedChannelId = null
                finishCallDialog()

                // 停止来点秀视频和铃声
                CallServiceManager.instance.stopCallShow()
                CallServiceManager.instance.stopCallMusic()
                // TODO bug CallServiceManager.instance.rtcEngine?.stopAudioMixing()

                // 自动刷新列表
                binding.smartRefreshLayout.autoRefresh()
            }
            else -> {
            }
        }
    }

    override fun callDebugInfo(message: String, logLevel: CallLogLevel) {
        val callTag = "CallAPI"
        Log.d(callTag, message)
        when (logLevel) {
            CallLogLevel.Normal -> Pure1v1Logger.d(callTag, message)
            CallLogLevel.Warning -> Pure1v1Logger.w(callTag, message)
            CallLogLevel.Error -> Pure1v1Logger.e(callTag, null, message)
        }
    }

    private fun setupContentInspectConfig(enable: Boolean, connection: RtcConnection) {
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "Pure1v1")
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectConfig.ContentInspectModule()
            module.interval = 30
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            val ret = CallServiceManager.instance.rtcEngine?.enableContentInspectEx(enable, contentInspectConfig, connection)
            Log.d(tag, "$ret")
        }
        catch (_: JSONException) {
        }
    }
    /// 语音审核
    private fun moderationAudio() {
        val channelName =  CallServiceManager.instance.connectedChannelId ?: return
        val uid = CallServiceManager.instance.localUser?.userId?.toLong() ?: 0
        AudioModeration.moderationAudio(channelName, uid, AudioModeration.AgoraChannelType.broadcast, "Pure1v1")
    }

    private fun setupView() {
        adapter = RoomListAdapter(this)
        adapter?.setItemActionHandler(object : RoomListAdapter.UserItemActionHandler {
            override fun onClickCall(user: UserInfo) {
                call(user)
            }
        })
        binding.viewPager2.offscreenPageLimit = 1
        binding.viewPager2.adapter = adapter

        binding.titleView.setLeftClick {
            CallServiceManager.instance.cleanUp()
            finish()
        }

        binding.smartRefreshLayout.setEnableLoadMore(false)
        binding.smartRefreshLayout.setEnableRefresh(true)
        binding.smartRefreshLayout.setOnRefreshListener {
            fetchRoomList(true)
        }
        binding.btnDebug.setOnClickListener {
            showDebugSettingsDialog()
        }
        binding.btnDebug.isVisible = AgoraApplication.the().isDebugModeOpen
    }

    private fun showDebugSettingsDialog() {
        if (debugSettingsDialog == null) {
            val dialog = DebugSettingsDialog(this)
            dialog.setListener(object : DebugSettingsDialog.DebugSettingsListener {
                override fun onAudioDumpEnable(enable: Boolean) {
                    Pure1v1Logger.d(tag, "onAudioDumpEnable: $enable")
                    if (enable) {
                        CallServiceManager.instance.rtcEngine?.setParameters("{\"rtc.debug.enable\": true}")
                        CallServiceManager.instance.rtcEngine?.setParameters("{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}")
                    } else {
                        CallServiceManager.instance.rtcEngine?.setParameters("{\"rtc.debug.enable\": false}")
                    }
                }
            })
            debugSettingsDialog = dialog
        }

        debugSettingsDialog?.let {
            if (!it.isShowing) {
                it.show()
            }
        }
    }

    // MARK: - CycleView Adapter
    private class RoomListAdapter constructor(
        private val context: Context
    ): RecyclerView.Adapter<UserItemViewHolder>()   {

        interface UserItemActionHandler {
            fun onClickCall(user: UserInfo)
        }

        private var dataList: List<UserInfo> = listOf()

        private var handler: UserItemActionHandler? = null

        private var lastClickTime: Long = 0
        private val clickInterval = 1000

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
            val binding = Pure1v1RoomListItemLayoutBinding.inflate(LayoutInflater.from(context))
            val view = binding.root
            view.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            return UserItemViewHolder(binding, view)
        }

        override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
            val userInfo = dataList[position % dataList.size]
            holder.binding.ivConnect.setOnClickListener {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastClickTime >= clickInterval) {
                    handler?.onClickCall(userInfo)
                    lastClickTime = currentTime
                }
            }

            var resourceId: Int
            try {
                val resourceName = "pure1v1_user_bg${userInfo.userId.toInt() % 9 + 1}"
                resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            } catch (e: Exception) {
                resourceId = R.drawable.pure1v1_user_bg1
            }
            val drawable = ContextCompat.getDrawable(context, resourceId)
            Glide.with(context).load(drawable).into(holder.binding.ivRoomCover)
            Glide.with(context)
                .asGif()
                .load(R.drawable.pure1v1_wave_living)
                .into(holder.binding.ivLiving)
            Glide.with(context)
                .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                .into(holder.binding.ivRemoteAvatar)
            holder.binding.tvRemoteName.text = userInfo.userName
            CallServiceManager.instance.localUser?.let { localUserInfo ->
                Glide.with(context)
                    .load(localUserInfo.avatar).apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.ivLocalAvatar)
                holder.binding.tvLocalName.text = localUserInfo.userName
            }

            holder.binding.ivConnectBG.breathAnim()
        }

        override fun getItemCount(): Int {
            return dataList.size
        }

        fun refresh(list: List<UserInfo>){
            // 使用 DiffUtil 计算差异
            val diffResult = DiffUtil.calculateDiff(DiffCallback(dataList, list))
            dataList = list
            // 通知 Adapter 应用差异
            diffResult.dispatchUpdatesTo(this)
        }

        fun setItemActionHandler(handler: UserItemActionHandler){
            this.handler = handler
        }

        private fun View.breathAnim() {
            val animator = ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat("scaleX", 0.8f, 1f, 0.8f),
                PropertyValuesHolder.ofFloat("scaleY", 0.8f, 1f, 0.8f)
            )
            animator.repeatCount = ObjectAnimator.INFINITE
            animator.repeatMode = ObjectAnimator.REVERSE
            animator.duration = 1600
            animator.start()
        }
    }

    // 创建一个 Callback 类来计算两个数据列表之间的差异
    class DiffCallback(private val oldList: List<UserInfo>, private val newList: List<UserInfo>) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    private class UserItemViewHolder(val binding: Pure1v1RoomListItemLayoutBinding, itemView: View) : RecyclerView.ViewHolder(itemView)
}


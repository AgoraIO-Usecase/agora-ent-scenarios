package io.agora.scene.pure1v1.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideOptions
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.callAPI.*
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListActivityBinding
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListItemLayoutBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import io.agora.scene.pure1v1.service.PermissionHelp
import io.agora.scene.pure1v1.service.UserInfo
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.utils.BlurTransformation
import org.json.JSONException
import org.json.JSONObject

class RoomListActivity : BaseViewBindingActivity<Pure1v1RoomListActivityBinding>(), ICallApiListener {

    private val tag = "RoomListActivity_LOG"

    private val kRoomListSwipeGuide = "io.agora.RoomListSwipeGuide"

    private var adapter: RoomListAdapter? = null

    private var dataList: List<UserInfo> = listOf()

    private var callState = CallStateType.Idle

    private var callDialog: CallDialog? = null

    private val permissionHelp = PermissionHelp(this)

    override fun getViewBinding(inflater: LayoutInflater): Pure1v1RoomListActivityBinding {
       return Pure1v1RoomListActivityBinding.inflate(inflater)
    }

    override fun onDestroy() {
        CallServiceManager.instance.cleanUp()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupView()

        CallServiceManager.instance.setup(this)
        CallServiceManager.instance.sceneService?.enterRoom { e ->
            fetchRoomList()
        }
        CallServiceManager.instance.startupCallApiIfNeed()
        CallServiceManager.instance.callApi?.addListener(this)
    }

    override fun onBackPressed() {
        CallServiceManager.instance.cleanUp()
        super.onBackPressed()
    }

    override fun onRestart() {
        super.onRestart()
        fetchRoomLiving()
    }

    private fun fetchRoomList() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.pure1v1_center_rotation)
        binding.ivRefresh.startAnimation(anim)
        binding.ivRefresh.isEnabled = false
        CallServiceManager.instance.sceneService?.getUserList { msg, list ->
            binding.ivRefresh.postDelayed({
                binding.ivRefresh.clearAnimation()
                binding.ivRefresh.isEnabled = true
            },1000)
            // 用户是否在线
            val living = list.any { it.userId == CallServiceManager.instance.localUser?.userId }
            if (!living) {
                CallServiceManager.instance.sceneService?.enterRoom { e ->
                    if (e != null) {
                        Toast.makeText(this, getText(R.string.pure1v1_room_list_local_offline), Toast.LENGTH_SHORT).show()
                    } else {
                        fetchRoomList()
                    }
                }
                return@getUserList
            }
            if (msg != null ) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getText(R.string.pure1v1_room_list_refresh), Toast.LENGTH_SHORT).show()
            }
            dataList = list.filter { it.userId != CallServiceManager.instance.localUser?.userId}
            adapter?.refresh(dataList)
            if (dataList.size > 1) {
                // 设置无限轮播中间位置
                binding.viewPager2.setCurrentItem(
                    ((Int.MAX_VALUE / 2) / (dataList.size)) * dataList.size,
                    false
                )
            }
            updateView()
            mayShowGuideView()
        }
    }

    private fun fetchRoomLiving() {
        CallServiceManager.instance.sceneService?.getUserList { msg, list ->
            val living = list.any { it.userId == CallServiceManager.instance.localUser?.userId }
            if (!living) {
                CallServiceManager.instance.sceneService?.enterRoom { e ->
                    fetchRoomList()
                }
            }
        }
    }

    private fun updateView() {
        if (dataList.isEmpty()) {
            binding.tvRoomTitle.setTextColor(Color.BLACK)
            binding.ivBack.setImageResource(R.drawable.pure1v1_room_list_back_dark)
            binding.ivRefresh.setImageResource(R.drawable.pure1v1_room_list_refresh_dark)
        } else {
            binding.tvRoomTitle.setTextColor(Color.WHITE)
            binding.ivBack.setImageResource(R.drawable.pure1v1_room_list_back_light)
            binding.ivRefresh.setImageResource(R.drawable.pure1v1_room_list_refresh_light)
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
        permissionHelp.checkCameraAndMicPerms({
            showCallSendDialog(user)
            CallServiceManager.instance.startupCallApiIfNeed()
            CallServiceManager.instance.callApi?.call(user.getRoomId(), user.userId.toInt()) { error ->
                if (error != null) {
                    finishCallDialog()
                }
            }
        }, {
            PermissionLeakDialog(this).show("", { getPermissions() }
            ) { launchAppSetting(Manifest.permission.CAMERA) }
        }, false)
    }

    private fun connectCallDetail() {
        val intent = Intent(this, CallDetailActivity::class.java)
        startActivity(intent)
        // 开启鉴黄鉴暴
        val channelId = CallServiceManager.instance.remoteUser?.getRoomId() ?: ""
        val localUid = CallServiceManager.instance.localUser?.userId?.toInt() ?: 0
        setupContentInspectConfig(true, RtcConnection(channelId, localUid))
        moderationAudio()
    }

    private fun showCallSendDialog(user: UserInfo) {
        if (callDialog != null) { return }
        val dialog = CallSendDialog(this, user)
        dialog.setListener(object : CallSendDialog.CallSendDialogListener {
            override fun onSendViewDidClickHangup() {
                CallServiceManager.instance.callApi?.cancelCall {
                }
            }
        })
        dialog.show()
        callDialog = dialog
    }

    private fun finishCallDialog() {
        callDialog?.dismiss()
        callDialog = null
    }

    override fun onCallStateChanged(
        state: CallStateType,
        stateReason: CallReason,
        eventReason: String,
        elapsed: Long,
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
                if (remoteUser != null && remoteUser.userId != fromUserId.toString())  {
                    CallServiceManager.instance.callApi?.reject(fromUserId, "already calling") { err ->
                    }
                    return
                }
                // 触发状态的用户是自己才处理
                if (currentUid == toUserId.toString()) {
                    CallServiceManager.instance.connectedChannelId = fromRoomId
                    var user = dataList.firstOrNull { it.userId == fromUserId.toString() }
                    if (user == null) {
                        val userMap = eventInfo[CallApiImpl.kFromUserExtension] as Map<String, Any>
                        user = UserInfo(userMap)
                    }
                    if (user.userId.isEmpty()) { return } // 检验数据是否有效
                    CallServiceManager.instance.fetchAcceptCallToken(fromRoomId, null)
                    CallServiceManager.instance.remoteUser = user
                    val dialog = CallReceiveDialog(this, user)
                    dialog.setListener(object : CallReceiveDialog.CallReceiveDialogListener {
                        override fun onReceiveViewDidClickAccept() { // 点击接通
                            // 获取多媒体权限
                            permissionHelp.checkCameraAndMicPerms({
                                // 获取token
                                CallServiceManager.instance.fetchAcceptCallToken(fromRoomId) { rtcToken ->
                                    if (rtcToken != null) {
                                        CallServiceManager.instance.callApi?.accept(fromRoomId, fromUserId, rtcToken) {
                                        }
                                    } else {
                                        Toast.makeText(this@RoomListActivity, "Fetch RTC token failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }, {
                                PermissionLeakDialog(this@RoomListActivity).show("", { getPermissions() }
                                ) { launchAppSetting(Manifest.permission.CAMERA) }
                            }, false)
                        }
                        override fun onReceiveViewDidClickReject() {
                            CallServiceManager.instance.callApi?.reject(fromUserId, "reject by user") {
                            }
                        }
                    })
                    dialog.show()
                    callDialog = dialog
                } else if (currentUid == fromUserId.toString()) {
                    CallServiceManager.instance.connectedChannelId = fromRoomId
                    val user = dataList.firstOrNull { it.userId == toUserId.toString() } ?: return
                    CallServiceManager.instance.remoteUser = user
                    showCallSendDialog(user)
                }
            }
            CallStateType.Connecting -> {
                callDialog?.updateCallState(CallDialogState.Connecting)
            }
            CallStateType.Connected -> {
                if (CallServiceManager.instance.remoteUser == null) { return }
                // 进入通话页面
                connectCallDetail()
                finishCallDialog()
            }
            CallStateType.Prepared -> {
                when(stateReason) {
                    CallReason.RemoteHangup -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_hangup), Toast.LENGTH_SHORT).show()
                    }
                    CallReason.LocalRejected -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_local_rejected), Toast.LENGTH_SHORT).show()
                    }
                    CallReason.RemoteRejected -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_rejected), Toast.LENGTH_SHORT).show()
                    }
                    CallReason.CallingTimeout -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_no_answer), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                CallServiceManager.instance.remoteUser = null
                CallServiceManager.instance.connectedChannelId = null
                CallServiceManager.instance.resetAcceptCallToken()
                finishCallDialog()
            }
            CallStateType.Failed -> {
                Toast.makeText(this, eventReason, Toast.LENGTH_SHORT).show()
                CallServiceManager.instance.remoteUser = null
                CallServiceManager.instance.connectedChannelId = null
                CallServiceManager.instance.resetAcceptCallToken()
                finishCallDialog()
            }
            else -> {
            }
        }
    }

    fun onCallLogger(tag: String, level: Int, message: String) {
        when (level) {
            Log.ERROR -> Pure1v1Logger.e(tag, message)
            Log.WARN -> Pure1v1Logger.w(tag, message)
            else -> Pure1v1Logger.d(tag, message)
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
        val channelName = CallServiceManager.instance.connectedChannelId ?: return
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
        binding.ivRefresh.setOnClickListener {
            fetchRoomList()
        }
        binding.ivBack.setOnClickListener {
            CallServiceManager.instance.cleanUp()
            finish()
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
            val resourceName = "pure1v1_user_bg${userInfo.userId.toInt() % 9 + 1}"
            val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)
            val drawable = ContextCompat.getDrawable(context, resourceId)
            Glide.with(context).load(drawable).into(holder.binding.ivRoomCover)
            Glide.with(context)
                .load(drawable).apply(
                    GlideOptions.bitmapTransform(
                        BlurTransformation(context)
                    ))
                .into(holder.binding.ivBackground)
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
            return if (dataList.size <= 1) dataList.size else Int.MAX_VALUE
        }

        fun refresh(list: List<UserInfo>){
            dataList = list
            notifyDataSetChanged()
        }

        fun setItemActionHandler(handler: UserItemActionHandler){
            this.handler = handler
        }

        private fun View.breathAnim() {
            val scaleAnima = ScaleAnimation(
                0.8f, 1f, 0.8f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f)
            scaleAnima.duration = 800
            scaleAnima.repeatCount = Animation.INFINITE
            scaleAnima.repeatMode = Animation.REVERSE
            this.startAnimation(scaleAnima)
        }
    }

    private class UserItemViewHolder(
        val binding: Pure1v1RoomListItemLayoutBinding,
        itemView: View) : RecyclerView.ViewHolder(itemView) {
        }
}


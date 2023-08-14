package io.agora.scene.pure1v1.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideOptions
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.callAPI.*
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListActivityBinding
import io.agora.scene.pure1v1.databinding.Pure1v1RoomListItemLayoutBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import io.agora.scene.pure1v1.service.PermissionHelp
import io.agora.scene.pure1v1.service.UserInfo
import io.agora.scene.widget.utils.BlurTransformation
import org.json.JSONException
import org.json.JSONObject

class RoomListActivity : AppCompatActivity(), ICallApiListener {

    private val tag = "RoomListActivity_LOG"

    private val kRoomListSwipeGuide = "io.agora.RoomListSwipeGuide"

    private lateinit var binding: Pure1v1RoomListActivityBinding

    private var adapter: RoomListAdapter? = null

    private var dataList: List<UserInfo> = listOf()

    private var callState = CallStateType.Idle

    private var callDialog: CallDialog? = null

    override fun onDestroy() {
        CallServiceManager.instance.cleanUp()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Pure1v1RoomListActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()

        CallServiceManager.instance.setup(this)
        CallServiceManager.instance.sceneService?.enterRoom { e ->
            fetchRoomList()
        }
        CallServiceManager.instance.startupCallApiIfNeed()
        CallServiceManager.instance.callApi?.addListener(this)
        mediaPermission()
    }

    override fun onBackPressed() {
        CallServiceManager.instance.cleanUp()
        super.onBackPressed()
    }

    private fun fetchRoomList() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.pure1v1_center_rotation)
        binding.ivRefresh.startAnimation(anim)
        binding.ivRefresh.isEnabled = false
        CallServiceManager.instance.sceneService?.getUserList { list ->
            Toast.makeText(this, getText(R.string.pure1v1_room_list_refresh), Toast.LENGTH_SHORT).show()
            Handler().postDelayed({
                binding.ivRefresh.clearAnimation()
                binding.ivRefresh.isEnabled = true
            }, 1000)
            dataList = list.filter { it.userId != CallServiceManager.instance.localUser?.userId}
            adapter?.refresh(dataList)
            if (dataList.size > 1) {
                // 设置无限轮播中间位置
                binding.viewPager2.setCurrentItem(
                    ((Int.MAX_VALUE / 2) / list.size) * list.size,
                    false
                )
            }
            updateView()
            mayShowGuideView()
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
            binding.vGuidance.visibility = View.GONE
        }
    }

    private fun call(user: UserInfo) {
        CallServiceManager.instance.startupCallApiIfNeed()
        CallServiceManager.instance.callApi?.call(user.userId, user.userId.toInt()) { er ->
        }
    }

    private fun onCallSend(user: UserInfo) {
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
                    CallServiceManager.instance.callApi?.reject(fromRoomId, fromUserId, "already calling") { err ->
                    }
                    return
                }
                // 触发状态的用户是自己才处理
                if (currentUid == toUserId.toString()) {
                    var user = dataList.firstOrNull { it.userId == fromUserId.toString() }
                    if (user == null) {
                        val userMap = eventInfo[CallApiImpl.kFromUserExtension] as Map<String, Any>
                        user = UserInfo(userMap)
                    }
                    if (user.userId.isEmpty()) { return } // 检验数据是否有效
                    CallServiceManager.instance.remoteUser = user
                    val dialog = CallReceiveDialog(this, user)
                    dialog.setListener(object : CallReceiveDialog.CallReceiveDialogListener {
                        override fun onReceiveViewDidClickAccept() {
                            TokenGenerator.generateTokens(
                                fromRoomId, toUserId.toString(),
                                TokenGenerator.TokenGeneratorType.token007,
                                arrayOf(TokenGenerator.AgoraTokenType.rtc), { ret ->
                                    val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc] ?: return@generateTokens
                                    CallServiceManager.instance.callApi?.accept(fromRoomId, fromUserId, rtcToken) {
                                    }
                                })
                        }
                        override fun onReceiveViewDidClickReject() {
                            CallServiceManager.instance.callApi?.reject(fromRoomId, fromUserId, "reject by user") {
                            }
                        }
                    })
                    dialog.show()
                    callDialog = dialog
                } else if (currentUid == fromUserId.toString()) {
                    val user = dataList.firstOrNull { it.userId == toUserId.toString() } ?: return
                    CallServiceManager.instance.remoteUser = user
                    onCallSend(user)
                }
            }
            CallStateType.Connecting -> {
                callDialog?.updateCallState(CallDialogState.Connecting)
            }
            CallStateType.Connected -> {
                if (CallServiceManager.instance.remoteUser == null) { return }
                // 进入通话页面
                callDialog?.dismiss()
                val intent = Intent(this, CallDetailActivity::class.java)
                startActivity(intent)
                // 开启鉴黄鉴暴
                val channelId = CallServiceManager.instance.remoteUser?.getRoomId() ?: ""
                val localUid = CallServiceManager.instance.localUser?.userId?.toInt() ?: 0
                setupContentInspectConfig(true, RtcConnection(channelId, localUid))
                moderationAudio()
            }
            CallStateType.Prepared -> {
                when(stateReason) {
                    CallReason.RemoteHangup -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_hangup), Toast.LENGTH_SHORT).show()
                    }
                    CallReason.LocalRejected,
                    CallReason.RemoteRejected -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_rejected), Toast.LENGTH_SHORT).show()
                    }
                    CallReason.CallingTimeout -> {
                        Toast.makeText(this, getText(R.string.pure1v1_call_toast_no_answer), Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
                CallServiceManager.instance.remoteUser = null
                callDialog?.dismiss()
            }
            CallStateType.Failed -> {
                CallServiceManager.instance.remoteUser = null
                callDialog?.dismiss()
            }
            else -> {
            }
        }
    }

    private fun mediaPermission() {
        PermissionHelp(this).checkCameraAndMicPerms(
            {
            },
            { finish() },
            true
        )
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
        val channelName = CallServiceManager.instance.remoteUser?.userId ?: ""
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
                handler?.onClickCall(userInfo)
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
                .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                .into(holder.binding.ivUserAvatar)
            holder.binding.tvUserName.text = userInfo.userName
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


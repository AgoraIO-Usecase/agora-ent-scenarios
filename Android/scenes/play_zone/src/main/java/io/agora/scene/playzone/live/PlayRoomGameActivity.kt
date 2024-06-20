package io.agora.scene.playzone.live

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.DataStreamConfig
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.playzone.PlayRtcEngineInstance
import io.agora.scene.playzone.PlayZoneCenter
import io.agora.scene.playzone.PlayZoneLogger
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityRoomGameLayoutBinding
import io.agora.scene.playzone.databinding.PlayZoneItemLiveDetailMessageBinding
import io.agora.scene.playzone.live.sub.QuickStartGameViewModel
import io.agora.scene.playzone.service.PlayStartGameInfo
import io.agora.scene.playzone.service.PlayZoneParameters
import io.agora.scene.playzone.service.PlayZoneServiceListenerProtocol
import io.agora.scene.playzone.service.PlayZoneServiceProtocol
import io.agora.scene.playzone.service.api.PlayGameInfoModel
import io.agora.scene.playzone.service.api.PlayZoneMessage
import io.agora.scene.playzone.widget.KeyboardStatusWatcher
import io.agora.scene.playzone.widget.statusBarHeight
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import org.json.JSONException
import org.json.JSONObject
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel.GameViewRectModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class PlayRoomGameActivity : BaseViewBindingActivity<PlayZoneActivityRoomGameLayoutBinding>() {

    companion object {
        private const val TAG = "Play_RoomLivingActivity"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context, roomInfo: AUIRoomInfo) {
            val intent = Intent(context, PlayRoomGameActivity::class.java)
            intent.putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
            context.startActivity(intent)
        }
    }

    private val mMessageAdapter: RoomMessageAdapter by lazy {
        RoomMessageAdapter(mutableListOf())
    }

    private val gameViewModel = QuickStartGameViewModel()

    private val mRoomInfo by lazy { (intent?.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as? AUIRoomInfo)!! }

    private val mUser: User get() = UserManager.getInstance().user

    private val mMainRtcConnection by lazy {
        RtcConnection(mRoomInfo.roomId, mUser.id.toInt())
    }
    private val mIsRoomOwner by lazy { mRoomInfo.roomOwner?.userId == mUser.id.toString() }

    private val mPlayZoneService by lazy { PlayZoneServiceProtocol.serviceProtocol }
    private val mRtcEngine by lazy { PlayRtcEngineInstance.rtcEngine }

    private var mStreamId = -1

    private var mToggleAudioRun: Runnable? = null

    override fun getPermissions() {
        mToggleAudioRun?.let {
            it.run()
            mToggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun getViewBinding(inflater: LayoutInflater): PlayZoneActivityRoomGameLayoutBinding {
        return PlayZoneActivityRoomGameLayoutBinding.inflate(inflater)
    }

    private lateinit var mRootInset: Insets

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        binding.root.post {
            // 设置游戏安全操作区域
            val gameViewRectModel = GameViewRectModel()
            gameViewRectModel.left = 0
            gameViewRectModel.top = binding.layoutTop.height + statusBarHeight
            gameViewRectModel.right = 0
            gameViewRectModel.bottom = binding.layoutBottom.height
            gameViewModel.gameViewRectModel = gameViewRectModel
            PlayZoneLogger.d(TAG, "gameViewRectModel: $gameViewRectModel")

            // 游戏配置
            val gameConfigModel = gameViewModel.getGameConfigModel()
            gameConfigModel.ui.ping.hide = false // 配置不隐藏ping值 English: Configuration to not hide ping value

        }
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v: View?, insets: WindowInsetsCompat ->
            val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            mRootInset = inset
            binding.root.setPaddingRelative(inset.left, 0, inset.right, inset.bottom)
            Log.d(TAG, "getInsets ${inset.left},${inset.top},${inset.right},${inset.bottom}")
            WindowInsetsCompat.CONSUMED
        }
        Log.d(TAG, "status height:$statusBarHeight")
        val titleParams: MarginLayoutParams = binding.layoutTop.layoutParams as MarginLayoutParams
        titleParams.topMargin = statusBarHeight
        binding.layoutTop.layoutParams = titleParams
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvRoomId.text = mRoomInfo.roomId
        GlideApp.with(this)
            .load(mRoomInfo.roomOwner?.userAvatar ?: "")
            .placeholder(R.mipmap.default_user_avatar)
            .error(R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)

        binding.ivAddBot.isVisible = mIsRoomOwner

        // 消息
        (binding.rvMessage.layoutManager as LinearLayoutManager).stackFromEnd = true
        binding.rvMessage.adapter = mMessageAdapter

        binding.ivClose.setOnClickListener {
            showEndRoomDialog()
        }
        binding.ivMore.setOnClickListener {
            TopFunctionDialog(this).show()
        }
        binding.tvRules.setOnClickListener {
            showRulesDialog()
        }
        binding.tvInput.setOnClickListener {
            showKeyboardInputLayout()
        }
        binding.etMessage.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    val content = v.text.toString()
                    Log.d(TAG, "action send：${v.text}")
                    showNormalInputLayout()
                    if (content.isNotEmpty()) {
                        ToastUtils.showToast("发送弹幕：$content")
                    }
                }
            }
            true
        }
        binding.ivAddBot.setOnClickListener {
            ToastUtils.showToast("点击添加机器人")
        }

        KeyboardStatusWatcher(this, this) { isKeyboardShowed: Boolean, keyboardHeight: Int ->
            Log.d(TAG, " isKeyboardShowed: $isKeyboardShowed keyboardHeight: $keyboardHeight")
            val lp: ViewGroup.LayoutParams = binding.vKeyboardBg.layoutParams
            if (isKeyboardShowed) {
                lp.height = keyboardHeight
            } else {
                lp.height = 55.dp.toInt()
                showNormalInputLayout()
            }
            binding.vKeyboardBg.layoutParams = lp
        }
    }

    private fun showNormalInputLayout(): Boolean {
        if (!binding.layoutBottom.isVisible) {
            hideInput()
            binding.etMessage.setText("")
            binding.layoutEtMessage.isVisible = false
            binding.tvInput.isEnabled = true

            binding.layoutBottom.isVisible = true
            return true
        }
        return false
    }

    private fun showKeyboardInputLayout() {
        binding.layoutEtMessage.isVisible = true
        binding.tvInput.isEnabled = false

        // 隐藏
        binding.layoutBottom.isVisible = false
        showInput(binding.etMessage)
    }

    private fun toggleSelfAudio(callback: () -> Unit) {
        if (mIsRoomOwner) {
            mToggleAudioRun = Runnable {
                callback.invoke()
            }
            requestRecordPermission(true)
        } else {
            callback.invoke()
        }
    }

    override fun requestData() {
        super.requestData()
        toggleSelfAudio {
            initRtcEngine()
        }
        startTopLayoutTimer()
        mPlayZoneService.subscribeListener(object : PlayZoneServiceListenerProtocol {
            override fun onUserCountUpdate(userCount: Int) {
                super.onUserCountUpdate(userCount)
            }


            override fun onStartGameInfoDidChanged(gameInfo: PlayStartGameInfo) {
                super.onStartGameInfoDidChanged(gameInfo)
            }

            override fun onRoomDestroy() {
                innerRelease()
                showLivingEndLayout(false)
            }

            override fun onRoomExpire() {
                innerRelease()
                showLivingEndLayout(false)
            }
        })
        val gameId = mRoomInfo.customPayload[PlayZoneParameters.GAME_ID] as? Long ?: 0L
        gameViewModel.switchGame(this, mRoomInfo.roomId, gameId)

        gameViewModel.gameViewLiveData.observe(this) { view ->
            if (view == null) { // 在关闭游戏时，把游戏View给移除
                binding.gameContainer.removeAllViews()
            } else { // 把游戏View添加到容器内
                binding.gameContainer.addView(
                    view,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        }
    }

    private fun initRtcEngine() {
        val eventListener = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)
                Log.d(TAG, "rtc onJoinChannelSuccess channel:$channel ,uid:$uid }")
                val config = DataStreamConfig()
                config.ordered = true
                config.syncWithAudio = true
                mStreamId = mRtcEngine.createDataStreamEx(config, mMainRtcConnection)
            }

            override fun onUserJoined(uid: Int, elapsed: Int) {
                super.onUserJoined(uid, elapsed)
                Log.d(TAG, "rtc onUserJoined uid:$uid }")
            }

            override fun onUserOffline(uid: Int, reason: Int) {
                super.onUserOffline(uid, reason)
                Log.d(TAG, "rtc onUserOffline uid:$uid }")
            }

            override fun onVideoSizeChanged(
                source: Constants.VideoSourceType?,
                uid: Int,
                width: Int,
                height: Int,
                rotation: Int
            ) {
                super.onVideoSizeChanged(source, uid, width, height, rotation)
                Log.i(TAG, "onVideoSizeChanged->uid:$uid,width:$width,height:$height,rotation:$rotation")
            }

            override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                Log.d(TAG, "rtc onRemoteVideoStateChanged uid:$uid,state:$state,reason:$reason}")
            }

            override fun onFirstRemoteVideoFrame(uid: Int, width: Int, height: Int, elapsed: Int) {
                super.onFirstRemoteVideoFrame(uid, width, height, elapsed)
                Log.d(TAG, "rtc onFirstRemoteVideoFrame uid:$uid")
            }

            override fun onError(err: Int) {
                super.onError(err)
                PlayZoneLogger.e(TAG, "rtc onError:$err ${RtcEngine.getErrorDescription(err)} ")
            }
        }

        joinChannel(eventListener)
    }

    private fun joinChannel(eventListener: IRtcEngineEventHandler) {
        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType =
            if (mIsRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = false
        channelMediaOptions.autoSubscribeAudio = true
        channelMediaOptions.publishCameraTrack = false
        channelMediaOptions.publishMicrophoneTrack = mIsRoomOwner
        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
        if (!mIsRoomOwner) {
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }

        mRtcEngine.joinChannelEx(
            PlayZoneCenter.mRtcToken,
            mMainRtcConnection,
            channelMediaOptions,
            eventListener
        )
        if (mIsRoomOwner) {
            enableContentInspectEx()
        }
        AudioModeration.moderationAudio(
            mMainRtcConnection.channelId,
            mMainRtcConnection.localUid.toLong(),
            AudioModeration.AgoraChannelType.broadcast,
            "Play_Zone"
        )
    }

    private fun enableContentInspectEx() {
        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "show")
            jsonObject.put("id", mUser.id)
            jsonObject.put("userNo", mUser.userNo)
            contentInspectConfig.extraInfo = jsonObject.toString()
            val module = ContentInspectConfig.ContentInspectModule()
            module.interval = 10
            module.type = ContentInspectConfig.CONTENT_INSPECT_TYPE_IMAGE_MODERATION
            contentInspectConfig.modules = arrayOf(module)
            contentInspectConfig.moduleCount = 1
            mRtcEngine.enableContentInspectEx(true, contentInspectConfig, mMainRtcConnection)
        } catch (_: JSONException) {

        }
    }

    private fun startTopLayoutTimer() {
        val dataFormat = SimpleDateFormat("HH:mm:ss").apply { timeZone = TimeZone.getTimeZone("GMT") }
        binding.tvTimer.post(object : Runnable {
            override fun run() {
                val currentTime = mPlayZoneService.getCurrentDuration(mRoomInfo.roomId)
                if (currentTime > 0) {
                    binding.tvTimer.text = dataFormat.format(Date(currentTime))
                }
                binding.tvTimer.postDelayed(this, 1000)
                binding.tvTimer.tag = this
            }
        })
    }

    /**
     *  游戏规则
     *
     */
    private fun showRulesDialog() {
        // TODO:
        val gameInfo = PlayGameInfoModel()
        val bundle = Bundle().apply {
            putSerializable(PlayGameRulesDialog.Key_Game, gameInfo)
            putBoolean(PlayGameRulesDialog.Key_IsOwner, mIsRoomOwner)
        }
        val dialog = PlayGameRulesDialog().apply {
            setBundleArgs(bundle)
        }
        dialog.show(supportFragmentManager, "rulesDialog")
    }

    private fun showLivingEndLayout(abnormal: Boolean) {
        val title = if (abnormal) R.string.play_zone_living_abnormal_title else R.string.play_zone_living_timeout_title
        val message =
            if (mIsRoomOwner) R.string.play_zone_living_host_timeout else R.string.play_zone_living_user_timeout
        AlertDialog.Builder(this, R.style.play_zone_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    /**
     * 退出房间
     *
     */
    private fun showEndRoomDialog() {
        val title =
            if (mIsRoomOwner) R.string.play_zone_living_host_end_title else R.string.play_zone_living_user_end_title
        val message =
            if (mIsRoomOwner) R.string.play_zone_living_host_end_content else R.string.play_zone_living_user_end_content
        AlertDialog.Builder(this, R.style.play_zone_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.confirm) { dialog, id ->
                dialog.dismiss()
                exitRoom()
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    private fun exitRoom() {
        mPlayZoneService.leaveRoom { e: Exception? ->
            if (e == null) { // success
                PlayZoneLogger.d(TAG, "RoomLivingViewModel.exitRoom() success")
            } else { // failure
                PlayZoneLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: " + e.message)
            }
            e?.message?.let { error ->
                ToastUtils.showToast(error)
            }
        }
        innerRelease()
    }

    override fun onResume() {
        super.onResume()
        gameViewModel.onResume()
    }

    override fun onBackPressed() {
        if (showNormalInputLayout()) return

        // 注意：需要保证页面销毁之前，先调用游戏的销毁方法
        // 如果有其他地方调用finish()，那么也要在finish()之前，先调用游戏的销毁方法
        gameViewModel.destroyMG()
        showEndRoomDialog()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        gameViewModel.destroyMG()
        // TODO: 日志上传
//        if (SceneConfigManager.logUpload) {
//            LogUploader.uploadLog(LogUploader.SceneType.JOY)
//        }
    }

    private fun innerRelease() {
        (binding.tvTimer.tag as? Runnable)?.let {
            it.run()
            binding.tvTimer.removeCallbacks(it)
            binding.tvTimer.tag = null
        }
        mRtcEngine.leaveChannelEx(mMainRtcConnection)
    }


    private class RoomMessageAdapter constructor(
        private var mList: MutableList<PlayZoneMessage>,
    ) : RecyclerView.Adapter<RoomMessageAdapter.ViewHolder?>() {

        inner class ViewHolder(val binding: PlayZoneItemLiveDetailMessageBinding) :
            RecyclerView.ViewHolder(binding.root)

        fun insertLast(item: PlayZoneMessage) {
            insert(itemCount, item)
        }

        fun insert(position: Int, item: PlayZoneMessage) {
            var index = position
            val itemCount = itemCount
            if (index < 0) {
                index = 0
            }
            if (index > itemCount) {
                index = itemCount
            }
            mList.add(index, item)
            notifyItemInserted(index)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            PlayZoneItemLiveDetailMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
            val playMessage: PlayZoneMessage = mList[position]
            holder.binding.text.text = SpannableStringBuilder().append(
                "${playMessage.userName}: ",
                ForegroundColorSpan(Color.parseColor("#A6C4FF")),
                SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
            ).append(
                playMessage.message,
                ForegroundColorSpan(Color.WHITE),
                SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
            )
        }

        override fun getItemCount(): Int = mList.size
    }
}
package io.agora.scene.joy.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.TextureView
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.base.DataState
import io.agora.scene.joy.databinding.JoyActivityLiveDetailBinding
import io.agora.scene.joy.databinding.JoyItemLiveDetailMessageBinding
import io.agora.scene.joy.network.JoyGameListResult
import io.agora.scene.joy.network.JoyGameStatus
import io.agora.scene.joy.service.JoyMessage
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceListenerProtocol
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.service.JoyStartGameInfo
import io.agora.scene.joy.service.JoyUserInfo
import io.agora.scene.joy.ui.widget.JoyChooseGameDialog
import io.agora.scene.joy.ui.widget.JoyGameRulesDialog
import io.agora.scene.joy.ui.widget.JoyGiftDialog
import io.agora.scene.joy.ui.widget.KeyboardStatusWatcher
import io.agora.scene.joy.utils.CustomToast
import io.agora.scene.joy.utils.JoyLogger
import io.agora.scene.joy.utils.dp
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.utils.StatusBarUtil
import io.agora.syncmanager.rtm.Sync
import org.json.JSONException
import org.json.JSONObject
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class RoomLivingActivity : BaseViewBindingActivity<JoyActivityLiveDetailBinding>() {

    companion object {
        private const val TAG = "Joy_RoomLivingActivity"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"
        private const val EXTRA_GAME_LIST = "gameList"

        fun launch(context: Context, roomInfo: JoyRoomInfo, gameList: List<JoyGameListResult>? = null) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
            gameList?.let {
                intent.putExtra(EXTRA_GAME_LIST, it as Serializable)
            }
            context.startActivity(intent)
        }
    }

    private val mMessageAdapter: RoomMessageAdapter by lazy {
        RoomMessageAdapter(mutableListOf())
    }

    private val mJoyViewModel: JoyViewModel by lazy {
        ViewModelProvider(this)[JoyViewModel::class.java]
    }

    private val mRoomInfo by lazy { (intent?.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as? JoyRoomInfo)!! }

    private var mStartGameInfo: JoyStartGameInfo? = null

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }
    private val mIsRoomOwner by lazy { mRoomInfo.ownerId.toLong() == UserManager.getInstance().user.id }

    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var mGameChooseGameDialog: JoyChooseGameDialog? = null

    private var mToggleVideoRun: Runnable? = null

    override fun getPermissions() {
        mToggleVideoRun?.let {
            it.run()
            mToggleVideoRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityLiveDetailBinding {
        return JoyActivityLiveDetailBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        StatusBarUtil.hideStatusBar(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvRoomId.text = mRoomInfo.roomId
        GlideApp.with(this)
            .load(mRoomInfo.ownerAvatar)
            .placeholder(R.mipmap.userimage)
            .error(R.mipmap.userimage)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)

        binding.tvEmptyGame.isVisible = !mIsRoomOwner
        // 消息
        val messageLayout = binding.messageLayout
        (messageLayout.rvMessage.layoutManager as LinearLayoutManager).let {
            it.stackFromEnd = true
        }
        messageLayout.rvMessage.adapter = mMessageAdapter

        binding.ivClose.setOnClickListener {
            showEndRoomDialog()
        }
        binding.ivMore.setOnClickListener {
            TopFunctionDialog(this).show()
        }
        binding.tvRules.setOnClickListener {
            showRulesDialog()
        }
        binding.ivGift.setOnClickListener {
            mJoyViewModel.mGameDetail?.gifts?.let { gifts ->
                val bundle = Bundle().apply {
                    putSerializable(JoyGiftDialog.Key_Gifts, gifts as Serializable)
                }
                val dialog = JoyGiftDialog().apply {
                    setBundleArgs(bundle)
                    mSelectedCompletion = { giftEntity, count ->
                        mJoyViewModel.sendGift(
                            mJoyViewModel.mGamId, mRoomInfo.roomId, giftEntity.id ?: "", count, giftEntity.price
                        )
                    }
                }
                dialog.show(supportFragmentManager, "giftDialog")
            }
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
                        mJoyViewModel.sendComment(mJoyViewModel.mGamId, mRoomInfo.roomId, content)
                        mJoyService.sendChatMessage(mRoomInfo.roomId, content, completion = {
                            it?.message?.let { errorMsg ->
                                CustomToast.showError(errorMsg)
                            }
                            if (it == null) {
                                CustomToast.show(getString(R.string.joy_send_message_success))
                            }
                        })
                    }
                }
            }
            true
        }
        binding.likeView.likeView.setOnClickListener {
            binding.likeView.addFavor()
            mJoyViewModel.sendLike(mJoyViewModel.mGamId, mRoomInfo.roomId, 1)
        }
        binding.root.setOnTouchListener { v, event ->
            showNormalInputLayout()
            true
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
            null
        }
    }

    private fun showNormalInputLayout(): Boolean {
        if (!binding.groupBottom.isVisible) {
            hideInput()
            binding.etMessage.setText("")
            binding.layoutEtMessage.isVisible = false
            binding.groupBottom.isVisible = true
            binding.tvInput.isEnabled = true
            return true
        }
        return false
    }

    private fun showKeyboardInputLayout() {
        binding.layoutEtMessage.isVisible = true
        binding.groupBottom.isVisible = false
        binding.tvInput.isEnabled = false
        showInput(binding.etMessage)
    }

    override fun requestData() {
        super.requestData()
        val roomLeftTime =
            JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt)
        if (roomLeftTime > 0) {
            mToggleVideoRun = Runnable {
                initRtcEngine()
                initServiceWithJoinRoom()
            }
            requestCameraPermission(true)
            startTopLayoutTimer()
        } else {
            ToastUtils.showToast(getString(R.string.joy_living_end))
            finish()
            return
        }
        mJoyService.subscribeListener(object : JoyServiceListenerProtocol {
            override fun onNetworkStatusChanged(status: Sync.ConnectionState) {

            }

            override fun onUserListDidChanged(userList: List<JoyUserInfo>) {
                mRoomInfo.roomUserCount = userList.size
            }

            override fun onMessageDidAdded(message: JoyMessage) {
                mMessageAdapter.insertLast(message)
                binding.messageLayout.rvMessage.scrollToPosition(mMessageAdapter.itemCount - 1)
            }

            override fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo) {
                mStartGameInfo = startGameInfo
                val gameId = mStartGameInfo?.gameId ?: ""
                if (!mIsRoomOwner && gameId.isNotEmpty()) {
                    // 观众收到房间开始游戏
                    mJoyViewModel.getGameDetail(gameId)
                }
            }

            override fun onRoomDidDestroy(roomInfo: JoyRoomInfo) {
                showLivingEndLayout() // 房间到了限制时间
                JoyLogger.d("showLivingEndLayout", "timer end!")
            }

            override fun onRoomDidChanged(roomInfo: JoyRoomInfo) {


            }
        })

        mJoyViewModel.mGameDetailLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    binding.tvRules.isVisible = true
                    binding.groupBottom.isVisible = true
                    if (mIsRoomOwner) {
                        showRulesDialog()
                        mRoomInfo.badgeTitle = mJoyViewModel.mGameDetail?.name ?: ""
                        mJoyService.updateRoom(mRoomInfo, completion = {

                        })
                    }
                }
            }
        }
        mJoyViewModel.mStartGameLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    val mTaskId = it.data?.taskId ?: return@observe
                    val gameSelect = mGameChooseGameDialog?.mSelectGame ?: return@observe
                    mGameChooseGameDialog?.let { dialog ->
                        dialog.dismiss()
                        mGameChooseGameDialog = null
                    }
                    mStartGameInfo = JoyStartGameInfo(
                        gameId = gameSelect.gameId ?: "",
                        taskId = mTaskId,
                        assistantUid = 1000000000 + mRoomInfo.ownerId,
                        gameName = gameSelect.name ?: ""
                    )
                    // 获取游戏状态
//                    mJoyViewModel.gameState(gameSelect.gameId ?: "", mTaskId)

                    mJoyService.updateStartGame(mRoomInfo.roomId, mStartGameInfo!!, completion = { error ->
                        if (error == null) { //启动游戏成功
                            setupAssistantVideoView()
                        }
                    })
                }
            }
        }
        mJoyViewModel.mStopGameLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_EMPTY,
                DataState.STATE_SUCCESS -> {
                    ToastUtils.showToast("停止游戏")
                }
            }
        }
        mJoyViewModel.mSendGiftLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_EMPTY,
                DataState.STATE_SUCCESS -> {
                    CustomToast.show("送礼物成功")
                }
            }
        }
        mJoyViewModel.mSendCommentLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_EMPTY,
                DataState.STATE_SUCCESS -> {
//                    CustomToast.show("发送弹幕成功")
                }
            }
        }
        mJoyViewModel.mSendLikeLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_EMPTY,
                DataState.STATE_SUCCESS -> {
                    CustomToast.show("点赞成功")
                }
            }
        }
        mJoyViewModel.mGameStatusLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    // TODO:
                    if (it.data?.status == JoyGameStatus.stopped.name) {
                        // 游戏暂停，需要重启
                        val gameId = mStartGameInfo?.gameId ?: return@observe
                        val assistantUid = mStartGameInfo?.assistantUid ?: return@observe
                        mJoyViewModel.startGame(mRoomInfo.roomId, gameId, assistantUid)
                    }
                }
            }
        }
    }

    private fun initRtcEngine() {
        val eventListener = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)

            }

            override fun onError(err: Int) {
                super.onError(err)
                JoyLogger.e(TAG, "rtc onError:${RtcEngine.getErrorDescription(err)}")
            }
        }

        joinChannel(eventListener)
        setupVideoView()
    }

    private fun setupVideoView() {
        val textureView = TextureView(this)
        binding.flVideoContainer.removeAllViews()
        binding.flVideoContainer.addView(textureView)
        if (mIsRoomOwner) {
            mRtcEngine.setupLocalVideo(VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        } else {
            mRtcEngine.setupRemoteVideoEx(
                VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, mRoomInfo.ownerId),
                mMainRtcConnection
            )
        }
    }


    private fun setupAssistantVideoView() {
        val assistantUid = mStartGameInfo?.assistantUid ?: return
        val textureView = TextureView(this)
        binding.flAssistantContainer.removeAllViews()
        binding.flAssistantContainer.addView(textureView)
        mRtcEngine.setupRemoteVideoEx(
            VideoCanvas(textureView, VideoCanvas.RENDER_MODE_HIDDEN, assistantUid),
            mMainRtcConnection
        )
    }

    private fun initServiceWithJoinRoom() {
        mJoyService.joinRoom(mRoomInfo, completion = {
            if (it == null) { //success
                getStartGameInfo()
            } else {
                ToastUtils.showToast("加入房间失败，请重试")
                destroy()
                finish()
            }
        })
    }

    private fun getStartGameInfo() {
        mJoyService.getStartGame(mRoomInfo.roomId, completion = { error, startGameInfo ->
            if (error == null) { //success
                mStartGameInfo = startGameInfo
                val gameId = mStartGameInfo?.gameId ?: ""
                binding.tvRules.isVisible = gameId.isNotEmpty()
                if (gameId.isNotEmpty()) {
                    // 获取游戏详情
                    mJoyViewModel.getGameDetail(gameId)
                    // 加载游戏画面
                    setupAssistantVideoView()
                    val taskId = mStartGameInfo?.taskId ?: return@getStartGame
                    // 获取游戏状态
                    mJoyViewModel.gameState(gameId, taskId)
                } else {
                    // 房主未开启游戏，选择游戏
                    if (mIsRoomOwner) {
                        showGameChooseDialog()
                    }
                }
            } else {
                ToastUtils.showToast("获取进行中的游戏失败")
            }
        })
    }

    private fun joinChannel(eventListener: IRtcEngineEventHandler) {
        val channelMediaOptions = ChannelMediaOptions()
        channelMediaOptions.clientRoleType =
            if (mIsRoomOwner) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        channelMediaOptions.autoSubscribeVideo = true
        channelMediaOptions.autoSubscribeAudio = true
        channelMediaOptions.publishCameraTrack = mIsRoomOwner
        channelMediaOptions.publishMicrophoneTrack = mIsRoomOwner
        // 如果是观众 把 ChannelMediaOptions 的 audienceLatencyLevel 设置为 AUDIENCE_LATENCY_LEVEL_LOW_LATENCY（超低延时）
        if (!mIsRoomOwner) {
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }

        mRtcEngine.joinChannelEx(
            RtcEngineInstance.generalToken(),
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
            "Joy"
        )
    }

    private fun enableContentInspectEx() {
        // ------------------ 开启鉴黄服务 ------------------
        val contentInspectConfig = ContentInspectConfig()
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sceneName", "show")
            jsonObject.put("id", UserManager.getInstance().user.id)
            jsonObject.put("userNo", UserManager.getInstance().user.userNo)
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
                binding.tvTimer.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt))
                binding.tvTimer.postDelayed(this, 1000)
                binding.tvTimer.tag = this
            }
        })
    }

    private fun showGameChooseDialog() {
        val gameList = intent?.getSerializableExtra(EXTRA_GAME_LIST) as? List<JoyGameListResult>
        if (!gameList.isNullOrEmpty()) {
            if (mGameChooseGameDialog == null) {
                val bundle = Bundle().apply {
                    putSerializable(JoyChooseGameDialog.Key_Games, gameList as Serializable)
                }
                mGameChooseGameDialog = JoyChooseGameDialog().apply {
                    setBundleArgs(bundle)
                    mSelectedCompletion = {
                        // 开始游戏
                        val assistantUid = 1000000000 + (UserManager.getInstance().user.id).toInt()
                        mJoyViewModel.startGame(mRoomInfo.roomId, it.gameId ?: "", assistantUid)
                        // 获取游戏详情
                        mJoyViewModel.getGameDetail(it.gameId!!)
                        // 加载游戏画面
                        setupAssistantVideoView()
                    }
                }
            }
            mGameChooseGameDialog?.show(supportFragmentManager, "chooseGameDialog")
        }
    }

    private fun showRulesDialog() {
        val gameDetail = mJoyViewModel.mGameDetailLiveData.value?.data ?: return
        val bundle = Bundle().apply {
            putSerializable(JoyGameRulesDialog.Key_Game, gameDetail)
            putBoolean(JoyGameRulesDialog.Key_IsOwner, mIsRoomOwner)
        }
        val dialog = JoyGameRulesDialog().apply {
            setBundleArgs(bundle)
        }
        dialog.show(supportFragmentManager, "rulesDialog")
    }

    private fun showLivingEndLayout() {
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(R.string.joy_living_timeout_title)
            .setMessage(R.string.joy_living_timeout_content)
            .setCancelable(false)
            .setPositiveButton(R.string.i_know) { dialog, _ ->
                destroy()
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun showEndRoomDialog() {
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(R.string.joy_living_end_title)
            .setMessage(R.string.joy_living_end_content)
            .setPositiveButton(R.string.confirm) { dialog, id ->
                destroy()
                dialog.dismiss()
                finish()
            }
            .setNegativeButton(R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (showNormalInputLayout()) return
        showEndRoomDialog()
    }

    private fun destroy() {
        (binding.tvTimer.tag as? Runnable)?.let {
            it.run()
            binding.tvTimer.removeCallbacks(it)
            binding.tvTimer.tag = null
        }
        mJoyService.leaveRoom(mRoomInfo, {})
        if (mIsRoomOwner) {
            mStartGameInfo?.taskId?.let { taskId ->
                mJoyViewModel.stopGame(mJoyViewModel.mGamId, taskId)
            }
            mRtcEngine.stopPreview()
        }
        mRtcEngine.leaveChannelEx(mMainRtcConnection)
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
//        if (keyCode == KeyEvent.KEYCODE_BACK && event.repeatCount == 0) {
//            return false;
//        }
//        return super.onKeyDown(keyCode, event)
//    }
}

private class RoomMessageAdapter constructor(
    private var mList: MutableList<JoyMessage>,
) : RecyclerView.Adapter<RoomMessageAdapter.ViewHolder?>() {

    inner class ViewHolder(val binding: JoyItemLiveDetailMessageBinding) : RecyclerView.ViewHolder(binding.root)

    fun insertLast(item: JoyMessage) {
        insert(itemCount, item)
    }

    fun insert(position: Int, item: JoyMessage) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemLiveDetailMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val joyMessage: JoyMessage = mList[position]
        holder.binding.text.text = SpannableStringBuilder().append(
            "${joyMessage.userName}: ",
            ForegroundColorSpan(Color.parseColor("#A6C4FF")),
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
        ).append(
            joyMessage.message,
            ForegroundColorSpan(Color.WHITE),
            SpannableStringBuilder.SPAN_INCLUSIVE_INCLUSIVE
        )
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}
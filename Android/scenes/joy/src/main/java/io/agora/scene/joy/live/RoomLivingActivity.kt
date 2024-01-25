package io.agora.scene.joy.live

import agora.pb.rctrl.RemoteCtrlMsg
import agora.pb.rctrl.RemoteCtrlMsg.KeyboardEventMsg
import agora.pb.rctrl.RemoteCtrlMsg.KeyboardEventType
import agora.pb.rctrl.RemoteCtrlMsg.RctrlMsg
import agora.pb.rctrl.RemoteCtrlMsg.RctrlMsges
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
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
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.service.base.DataState
import io.agora.scene.joy.databinding.JoyActivityLiveDetailBinding
import io.agora.scene.joy.databinding.JoyItemLiveDetailMessageBinding
import io.agora.scene.joy.service.api.JoyGameListResult
import io.agora.scene.joy.service.api.JoyGameRepo
import io.agora.scene.joy.service.api.JoyGameStatus
import io.agora.scene.joy.service.JoyMessage
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceListenerProtocol
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.service.JoyStartGameInfo
import io.agora.scene.joy.service.JoyUserInfo
import io.agora.scene.joy.widget.KeyboardStatusWatcher
import io.agora.scene.joy.widget.toast.CustomToast
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.live.fragmentdialog.JoyChooseGameDialog
import io.agora.scene.joy.live.fragmentdialog.JoyGameRulesDialog
import io.agora.scene.joy.live.fragmentdialog.JoyGiftDialog
import io.agora.scene.joy.widget.dp
import io.agora.scene.joy.widget.navBarHeight
import io.agora.scene.joy.widget.statusBarHeight
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
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

        fun launch(context: Context, roomInfo: JoyRoomInfo, gameList: List<JoyGameListResult>? = null) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
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

    private val mGameList = mutableListOf<JoyGameListResult>()

    private var mStartGameInfo: JoyStartGameInfo? = null

    private val mUser: User
        get() = UserManager.getInstance().user

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            mUser.id.toInt()
        )
    }
    private val mIsRoomOwner by lazy { mRoomInfo.ownerId.toLong() == mUser.id }

    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private var mStreamId = -1

    private var mGameChooseGameDialog: JoyChooseGameDialog? = null

    private var mToggleVideoRun: Runnable? = null
    private var mToggleAudioRun: Runnable? = null

    // 保存视频宽高
    private var mVideoSizes = mutableMapOf<Int, Size>()

    override fun getPermissions() {
        mToggleVideoRun?.let {
            it.run()
            mToggleVideoRun = null
        }
        mToggleAudioRun?.let {
            it.run()
            mToggleAudioRun = null
        }
    }

    override fun onPermissionDined(permission: String?) {
        PermissionLeakDialog(this).show(permission, { getPermissions() }) { launchAppSetting(permission) }
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityLiveDetailBinding {
        return JoyActivityLiveDetailBinding.inflate(inflater)
    }

    private lateinit var mRootInset: Insets

    // 关闭按钮屏幕位置
    private lateinit var mCloseRect: Rect

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
        val titleParams: MarginLayoutParams = binding.clRoomTitle.layoutParams as MarginLayoutParams
        titleParams.topMargin = statusBarHeight
        binding.clRoomTitle.layoutParams = titleParams
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
        if (mIsRoomOwner) {
            binding.ivGift.isVisible = false
        } else {
            binding.ivDeployTroops.isVisible = false
        }

        // 消息
        (binding.rvMessage.layoutManager as LinearLayoutManager).let {
            it.stackFromEnd = true
        }
        binding.rvMessage.adapter = mMessageAdapter

        binding.ivClose.setOnClickListener {
            showEndRoomDialog()
        }
        binding.ivMore.setOnClickListener {
            TopFunctionDialog(this).show()
        }
        binding.tvRules.setOnClickListener {
            if (mJoyViewModel.mGameDetail == null) {
                mStartGameInfo?.gameId?.let { gameId ->
                    mJoyViewModel.getGameDetail(gameId)
                }
                JoyLogger.d(TAG, "click rules mGameDetail is null gameId:${mStartGameInfo?.gameId}")
            } else {
                showRulesDialog()
            }
        }
        binding.ivGift.setOnClickListener {
            if (mJoyViewModel.mGameDetail == null) {
                mStartGameInfo?.gameId?.let { gameId ->
                    mJoyViewModel.getGameDetail(gameId)
                }
                JoyLogger.d(TAG, "click gift mGameDetail is null gameId:${mStartGameInfo?.gameId}")
            } else {
                mJoyViewModel.mGameDetail?.gifts?.let { gifts ->
                    val bundle = Bundle().apply {
                        putSerializable(JoyGiftDialog.Key_Gifts, gifts as Serializable)
                    }
                    val dialog = JoyGiftDialog().apply {
                        setBundleArgs(bundle)
                        mSelectedCompletion = { giftEntity, count ->
                            mJoyViewModel.sendGift(
                                mJoyViewModel.mGamId, mRoomInfo.roomId, giftEntity.vendorGiftId ?: "", count,
                                giftEntity.price * count
                            )
                        }
                    }
                    dialog.show(supportFragmentManager, "giftDialog")
                }
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
        binding.ivDeployTroops.setOnTouchListener { v, event ->
            if (!mIsRoomOwner) {
                return@setOnTouchListener false
            }
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    setControllerView(binding.ivDeployTroops, false)
                    sendKeyboardMessage(KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN, 'Z')
                }

                MotionEvent.ACTION_UP -> {
                    setControllerView(binding.ivDeployTroops, true)
                    sendKeyboardMessage(KeyboardEventType.KEYBOARD_EVENT_KEY_UP, 'Z')
                }
            }
            return@setOnTouchListener true
        }
        if (mIsRoomOwner) {
            binding.flAssistantContainer.setOnTouchListener { view, event ->
                if (!mIsRoomOwner) return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> sendMouseMessage(
                        event,
                        RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_DOWN.getNumber()
                    )

                    MotionEvent.ACTION_UP -> sendMouseMessage(
                        event,
                        RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_UP.getNumber()
                    )
                }
                return@setOnTouchListener true
            }
        } else {
            binding.root.setOnTouchListener { v, event ->
                if (mIsRoomOwner) return@setOnTouchListener false
                showNormalInputLayout()
                return@setOnTouchListener true
            }
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

        binding.root.post {
            val exitClose = binding.ivClose
            val location = IntArray(2)
            exitClose.getLocationOnScreen(location)
            val exitCloseLeft = location[0]
            val exitCloseTop = location[1]
            val exitCloseRight = exitCloseLeft + exitClose.width
            val exitCloseBottom = exitCloseTop + exitClose.height
            mCloseRect = Rect(exitCloseLeft, exitCloseTop, exitCloseRight, exitCloseBottom)
            Log.d("Joy_JoyChooseGameDialog", "$exitCloseLeft,$exitCloseTop,$exitCloseRight,$exitCloseBottom")
        }
    }

    private fun setControllerView(view: ImageView, isClick: Boolean) {
        view.alpha = if (isClick) 1.0f else 0.5f
        view.isFocusable = isClick
        view.isClickable = isClick
    }

    private fun showNormalInputLayout(): Boolean {
        if (!binding.tvInput.isVisible) {
            hideInput()
            binding.etMessage.setText("")
            binding.layoutEtMessage.isVisible = false
            binding.tvInput.isEnabled = true

            showBottomView(true)
            return true
        }
        return false
    }

    private fun showBottomView(show: Boolean) {
        if (show) {
            binding.tvInput.isVisible = true
            binding.likeView.isVisible = true
            binding.ivGift.isVisible = !mIsRoomOwner
            binding.ivDeployTroops.isVisible = mIsRoomOwner
        } else {
            binding.tvInput.isVisible = false
            binding.likeView.isVisible = false
            binding.ivGift.isVisible = false
            binding.ivDeployTroops.isVisible = false
        }
    }

    private fun showKeyboardInputLayout() {
        binding.layoutEtMessage.isVisible = true
        binding.tvInput.isEnabled = false

        // 隐藏
        showBottomView(false)
        showInput(binding.etMessage)
    }

    private fun toggleSelfVideo(callback: () -> Unit) {
        if (mIsRoomOwner) {
            mToggleVideoRun = Runnable {
                callback.invoke()
            }
            requestCameraPermission(true)
        } else {
            callback.invoke()
        }
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
        val roomLeftTime =
            JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt)
        if (roomLeftTime > 0) {
            toggleSelfVideo {
                initRtcEngine()
                initServiceWithJoinRoom()
            }
            toggleSelfAudio { }
            startTopLayoutTimer()
        } else {
            CustomToast.show(getString(R.string.joy_living_end))
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
                binding.rvMessage.scrollToPosition(mMessageAdapter.itemCount - 1)
            }

            override fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo) {
                mStartGameInfo = startGameInfo
                val gameId = mStartGameInfo?.gameId ?: ""
                if (!mIsRoomOwner && gameId.isNotEmpty()) {
                    // 观众收到房间开始游戏
                    mJoyViewModel.getGameDetail(gameId)
                    // 加载游戏画面
                    setupAssistantVideoView()
                }
            }

            override fun onRoomDidDestroy(roomInfo: JoyRoomInfo, abnormal: Boolean) {
                destroy()
                showLivingEndLayout(abnormal)
                JoyLogger.d("showLivingEndLayout", "timer end! abnormal:$abnormal")
            }

            override fun onRoomDidChanged(roomInfo: JoyRoomInfo) {


            }
        })

        mJoyViewModel.mGameDetailLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    binding.tvRules.isVisible = true
                    showBottomView(true)
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
                DataState.STATE_LOADING -> {
                    showLoadingView()
                }

                DataState.STATE_SUCCESS -> {
                    hideLoadingView()
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
                    // 获取游戏详情
                    mJoyViewModel.getGameDetail(gameSelect.gameId!!)
                    // 加载游戏画面
                    setupAssistantVideoView()
                    mJoyService.updateStartGame(mRoomInfo.roomId, mStartGameInfo!!, completion = { error ->
                        if (error == null) { //启动游戏成功
                        }
                    })
                }

                else -> {
                    mGameChooseGameDialog?.setEnableConfirm(true)
                    hideLoadingView()

                    if (it.errorMessage?.code == JoyGameRepo.CODE_NO_CLOUD_HOST) {
                        CustomToast.showError(getString(R.string.joy_request_no_cloud_host))
                    } else {
                        CustomToast.showError(getString(R.string.joy_request_failed))
                    }
                }
            }
        }
        mJoyViewModel.mStopGameLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                }
            }
        }
        mJoyViewModel.mSendGiftLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    CustomToast.showTips(getString(R.string.joy_send_gift_success))
                }

                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_send_gift_failed))
                }
            }
        }
        mJoyViewModel.mSendCommentLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    CustomToast.showTips(getString(R.string.joy_send_message_success))
                }

                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_instruction_error))
                }
            }
        }
        mJoyViewModel.mSendLikeLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_request_failed))
                }
            }
        }
        mJoyViewModel.mGameStatusLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    if (it.data?.status == JoyGameStatus.started.name) {
                        val gameId = mStartGameInfo?.gameId ?: ""
                        mJoyViewModel.getGameDetail(gameId)
                    } else if (it.data?.status == JoyGameStatus.stopped.name) {
                        // 游戏暂停，直接重新选择游戏
                        if (mIsRoomOwner) {
                            mJoyViewModel.getGames()
                        }
                    }
                }
            }
        }
        mJoyViewModel.mGameListLiveData.observe(this) {
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    it.data?.list?.apply {
                        mGameList.clear()
                        mGameList.addAll(this)
                    }
                    showGameChooseDialog()
                }
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
                binding.root.post {
                    if (uid == mStartGameInfo?.assistantUid) {
                        // todo 远端游戏退出
                        destroy()
                        showLivingEndLayout(true)
                    }
                }
            }

            override fun onVideoSizeChanged(
                source: Constants.VideoSourceType?,
                uid: Int,
                width: Int,
                height: Int,
                rotation: Int
            ) {
                super.onVideoSizeChanged(source, uid, width, height, rotation)
                mVideoSizes[uid] = Size(width, height)
                Log.i(TAG, "onVideoSizeChanged->uid:$uid,width:$width,height:$height,rotation:$rotation")
                binding.root.post {
                    adjustAssistantVideoSize(uid)
                }
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
                JoyLogger.e(TAG, "rtc onError:$err ${RtcEngine.getErrorDescription(err)} ")
            }
        }

        joinChannel(eventListener)
        setupVideoView()
    }

    private fun adjustAssistantVideoSize(uid: Int) {
        if (mVideoTextureView == null) return
        val assistantUid = mStartGameInfo?.assistantUid ?: return
        if (uid != assistantUid) return
        val width: Int = mVideoSizes[assistantUid]?.width ?: return
        val height = mVideoSizes[assistantUid]?.height ?: return
        val rootViewWidth: Int = binding.root.measuredWidth
        val rootViewHeight: Int = binding.root.measuredHeight

        Log.i(TAG, "onVideoSizeChanged->rootViewWidth:$rootViewWidth,rootViewHeight:$rootViewHeight")
        val targetWidth: Int
        val targetHeight: Int
        if (rootViewHeight.toFloat() / rootViewWidth > height.toFloat() / width) {
            targetHeight = rootViewHeight
            val scale = height.toFloat() / width
            targetWidth = (rootViewHeight / scale).toInt()
        } else {
            targetWidth = rootViewWidth
            val scale = height.toFloat() / width
            targetHeight = (rootViewWidth * scale).toInt()
        }
        Log.i(TAG, "onVideoSizeChanged->targetWidth:$targetWidth,targetHeight:$targetHeight,navBarHeight:$navBarHeight")
        mVideoTextureView?.post {
            val navHeight = if (::mRootInset.isInitialized) mRootInset.bottom else navBarHeight
            if (targetWidth == rootViewWidth) {
                // 宽度对齐
                mVideoTextureView?.layout(
                    0,
                    rootViewHeight - targetHeight - navHeight,
                    rootViewWidth,
                    rootViewHeight
                )
                Log.i(TAG, "onVideoSizeChanged->layout:111")
            } else {
                mVideoTextureView?.layout(
                    (rootViewWidth - targetWidth) / 2, 0,
                    (rootViewWidth + targetWidth) / 2, rootViewHeight - navHeight
                )
                Log.i(TAG, "onVideoSizeChanged->layout:222")
            }

        }
        mVideoSizes.remove(assistantUid)
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


    private var mVideoTextureView: TextureView? = null
    private fun setupAssistantVideoView() {
        val assistantUid = mStartGameInfo?.assistantUid ?: return
        if (mVideoTextureView == null) {
            mVideoTextureView = TextureView(this)
        }
        binding.flAssistantContainer.removeAllViews()
        binding.flAssistantContainer.addView(mVideoTextureView)
        mRtcEngine.setupRemoteVideoEx(
            VideoCanvas(mVideoTextureView, VideoCanvas.RENDER_MODE_FIT, assistantUid),
            mMainRtcConnection
        )
        Log.d(TAG, "setupAssistantVideoView $assistantUid")
        adjustAssistantVideoSize(assistantUid)
    }

    private fun initServiceWithJoinRoom() {
        mJoyService.joinRoom(mRoomInfo, completion = {
            if (it == null) { //success
                getStartGameInfo()
            } else {
                CustomToast.showError(getString(R.string.joy_join_room_error))
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
                    // 加载游戏画面
                    setupAssistantVideoView()
                    val taskId = mStartGameInfo?.taskId ?: return@getStartGame
                    // 获取获取游戏状态
                    mJoyViewModel.gameState(gameId, taskId)
                } else {
                    // 房主未开启游戏，选择游戏
                    if (mIsRoomOwner) {
                        mJoyViewModel.getGames()
                    }
                }
            } else {
                CustomToast.showError(getString(R.string.joy_get_start_game_error))
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
                binding.tvTimer.text = dataFormat.format(Date(TimeUtils.currentTimeMillis() - mRoomInfo.createdAt))
                binding.tvTimer.postDelayed(this, 1000)
                binding.tvTimer.tag = this
            }
        })
    }

    private fun showGameChooseDialog() {
        if (!mIsRoomOwner) return
        if (mGameList.isNotEmpty()) {
            if (mGameChooseGameDialog == null) {
                val bundle = Bundle().apply {
                    putSerializable(JoyChooseGameDialog.Key_Games, mGameList as Serializable)
                }
                mGameChooseGameDialog = JoyChooseGameDialog().apply {
                    setBundleArgs(bundle)
                    mSelectedCompletion = {
                        // 开始游戏
                        val assistantUid = 1000000000 + (mUser.id).toInt()
                        mJoyViewModel.startGame(mRoomInfo.roomId, it.gameId ?: "", assistantUid)
                    }
                    mTouchEventCompletion = { x, y ->
                        checkCloseByEvent(x, y)
                    }
                }
            }
            mGameChooseGameDialog?.show(supportFragmentManager, "chooseGameDialog")
        }
    }

    private fun checkCloseByEvent(x: Int, y: Int) {
        if (::mCloseRect.isInitialized) {
            if (x >= mCloseRect.left && x <= mCloseRect.right && y >= mCloseRect.top && y <= mCloseRect.bottom) {
                showEndRoomDialog()
            }
        }
    }

    private fun showRulesDialog() {
        val gameDetail = mJoyViewModel.mGameDetail ?: return
        val bundle = Bundle().apply {
            putSerializable(JoyGameRulesDialog.Key_Game, gameDetail)
            putBoolean(JoyGameRulesDialog.Key_IsOwner, mIsRoomOwner)
        }
        val dialog = JoyGameRulesDialog().apply {
            setBundleArgs(bundle)
        }
        dialog.show(supportFragmentManager, "rulesDialog")
    }

    private fun showLivingEndLayout(abnormal: Boolean) {
        val title = if (abnormal) R.string.joy_living_abnormal_title else R.string.joy_living_timeout_title
        val message = if (mIsRoomOwner) R.string.joy_living_host_timeout else R.string.joy_living_user_timeout
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun showEndRoomDialog() {
        val title = if (mIsRoomOwner) R.string.joy_living_host_end_title else R.string.joy_living_user_end_title
        val message = if (mIsRoomOwner) R.string.joy_living_host_end_content else R.string.joy_living_user_end_content
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(title)
            .setMessage(message)
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
                mJoyViewModel.stopGame(mRoomInfo.roomId, mJoyViewModel.mGamId, taskId)
            }
            mRtcEngine.stopPreview()
        }
        mRtcEngine.leaveChannelEx(mMainRtcConnection)
        binding.flVideoContainer.removeAllViews()
        binding.flAssistantContainer.removeAllViews()
        binding.flVideoContainer.isVisible = false
        binding.flAssistantContainer.isVisible = false
        mGameChooseGameDialog?.let { dialog ->
            dialog.dismiss()
            mGameChooseGameDialog = null
        }
    }

    private fun sendMouseMessage(event: MotionEvent, value: Int) {
        if (!mIsRoomOwner) return
        Log.i(TAG, "sendMouseMessage:event x:${event.x},event y:${event.y},value:$value")
        Log.i(
            TAG,
            "sendMouseMessage:event x/width: ${event.x / binding.flAssistantContainer.measuredWidth} ,event y/height:${event.y / binding.flAssistantContainer.measuredHeight}"
        )
        val x: Int = (event.x.toInt() shl 16) / binding.flAssistantContainer.measuredWidth
        val y: Int = (event.y.toInt() shl 16) / binding.flAssistantContainer.measuredHeight
        val eventMsg: RemoteCtrlMsg.MouseEventMsg = RemoteCtrlMsg.MouseEventMsg.newBuilder()
            .setMouseEvent(value)
            .setX(x)
            .setY(y)
            .build()
        val rctrlMsg: RemoteCtrlMsg.RctrlMsg = RemoteCtrlMsg.RctrlMsg.newBuilder()
            .setType(RemoteCtrlMsg.MsgType.MOUSE_EVENT_TYPE)
            .setTimestamp(System.currentTimeMillis())
            .setPayload(eventMsg.toByteString())
            .build()
        val rctrlMsges: RemoteCtrlMsg.RctrlMsges = RemoteCtrlMsg.RctrlMsges.newBuilder()
            .addMsges(rctrlMsg)
            .build()
        mRtcEngine.sendStreamMessageEx(mStreamId, rctrlMsges.toByteArray(), mMainRtcConnection)
    }

    private fun sendKeyboardMessage(eventType: KeyboardEventType, key: Char) {
        if (!mIsRoomOwner) return
        val eventMsg = KeyboardEventMsg.newBuilder()
            .setVkey(key.code)
            .setState(if (eventType == KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN) 1 else -0x3fffffff)
            .setKeyboardEvent(eventType.number)
            .build()
        val rctrlMsg = RctrlMsg.newBuilder()
            .setType(RemoteCtrlMsg.MsgType.KEYBOARD_EVENT_TYPE)
            .setTimestamp(System.currentTimeMillis())
            .setPayload(eventMsg.toByteString())
            .build()
        val rctrlMsges = RctrlMsges.newBuilder()
            .addMsges(rctrlMsg)
            .build()
        mRtcEngine.sendStreamMessageEx(mStreamId, rctrlMsges.toByteArray(), mMainRtcConnection)
        Log.i(TAG, "sendKeyboardMessage:$eventType,key:$key")
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
}
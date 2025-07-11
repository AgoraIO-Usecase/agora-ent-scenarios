package io.agora.scene.joy.live

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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.rtmsyncmanager.model.AUIUserInfo
import io.agora.scene.base.AgoraScenes
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.LogUploader
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.dp
import io.agora.scene.base.utils.navBarHeight
import io.agora.scene.base.utils.statusBarHeight
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.JoyServiceManager
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyActivityLiveDetailBinding
import io.agora.scene.joy.databinding.JoyItemLiveDetailMessageBinding
import io.agora.scene.joy.live.fragmentdialog.JoyChooseGameDialog
import io.agora.scene.joy.live.fragmentdialog.JoyGameRulesDialog
import io.agora.scene.joy.live.fragmentdialog.JoyGiftDialog
import io.agora.scene.joy.service.JoyMessage
import io.agora.scene.joy.service.JoyServiceListenerProtocol
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.service.JoyStartGameInfo
import io.agora.scene.joy.service.RemoteCtrlMsg
import io.agora.scene.joy.service.RemoteCtrlMsg.KeyboardEventMsg
import io.agora.scene.joy.service.RemoteCtrlMsg.KeyboardEventType
import io.agora.scene.joy.service.RemoteCtrlMsg.RctrlMsg
import io.agora.scene.joy.service.RemoteCtrlMsg.RctrlMsges
import io.agora.scene.joy.service.api.JoyAction
import io.agora.scene.joy.service.api.JoyApiService
import io.agora.scene.joy.service.api.JoyGameListResult
import io.agora.scene.joy.service.api.JoyGameRepo
import io.agora.scene.joy.service.api.JoyGameStatus
import io.agora.scene.joy.service.base.DataState
import io.agora.scene.widget.clearScreen.ClearScreenLayout
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.dialog.showRoomDurationNotice
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.widget.utils.KeyboardStatusWatcher
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

        fun launch(context: Context, roomInfo: AUIRoomInfo, gameList: List<JoyGameListResult>? = null) {
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

    private val mRoomInfo by lazy { (intent?.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as? AUIRoomInfo)!! }

    private val mGameList = mutableListOf<JoyGameListResult>()

    private var mStartGameInfo: JoyStartGameInfo? = null

    private val mUser: User get() = UserManager.getInstance().user

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            mUser.id.toInt()
        )
    }
    private val mIsRoomOwner by lazy { mRoomInfo.roomOwner?.userId == mUser.id.toString() }

    private val mJoyService by lazy { JoyServiceProtocol.serviceProtocol }
    private val mRtcEngine by lazy { JoyServiceManager.rtcEngine }

    private var mStreamId = -1

    private var mGameChooseGameDialog: JoyChooseGameDialog? = null

    private var isShownRoomDuration = false

    private var mToggleVideoRun: Runnable? = null
    private var mToggleAudioRun: Runnable? = null

    // Save video width and height
    private var mVideoSizes = mutableMapOf<Int, Size>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!mIsRoomOwner){
            showRoomDurationNotice(SceneConfigManager.joyExpireTime)
        }
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (SceneConfigManager.logUpload) {
            LogUploader.uploadLog(AgoraScenes.Play_Joy)
        }
    }

    override fun finish() {
        onBackPressedCallback.remove()
        super.finish()
    }

    override fun getViewBinding(inflater: LayoutInflater): JoyActivityLiveDetailBinding {
        return JoyActivityLiveDetailBinding.inflate(inflater)
    }

    private lateinit var mRootInset: Insets

    private lateinit var mCloseRect: Rect

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (showNormalInputLayout()) return
            showExitRoomDialog()
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
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        val titleParams: MarginLayoutParams = binding.clRoomTitle.layoutParams as MarginLayoutParams
        titleParams.topMargin = statusBarHeight
        binding.clRoomTitle.layoutParams = titleParams
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvRoomId.text = mRoomInfo.roomId
        GlideApp.with(this)
            .load(mRoomInfo.roomOwner?.userAvatar ?: "")
            .placeholder(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)

        binding.tvEmptyGame.isVisible = !mIsRoomOwner
        if (mIsRoomOwner) {
            binding.ivGift.isVisible = false
        } else {
            binding.layoutBottomAction.isVisible = false
        }

        (binding.rvMessage.layoutManager as LinearLayoutManager).let {
            it.stackFromEnd = true
        }
        binding.rvMessage.adapter = mMessageAdapter

        binding.ivClose.setOnClickListener {
            showExitRoomDialog()
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
        if (mIsRoomOwner) {
            binding.flAssistantContainer.setOnTouchListener { view, event ->
                if (!mIsRoomOwner) return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> sendMouseMessage(
                        event,
                        RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_DOWN.number
                    )

                    MotionEvent.ACTION_UP -> sendMouseMessage(
                        event,
                        RemoteCtrlMsg.MouseEventType.MOUSE_EVENT_LBUTTON_UP.number
                    )
                }
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

            binding.clearScreenLayout.open()
        }
        binding.clearScreenLayout.addDragListener(object : ClearScreenLayout.DragListener {
            override fun onDragging(dragView: View, slideOffset: Float) {
                //正在拖动中
                dragView.alpha = slideOffset
                Log.d("clearScreenLayout", "onDragging $slideOffset")
            }

            override fun onDragToOut(dragView: View) {
                // When the mask is dragged out
                Log.d("clearScreenLayout", "onDragToOut $dragView")
            }

            override fun onDragToIn(dragView: View) {
                // When the mask is dragged in
                Log.d("clearScreenLayout", "onDragToIn $dragView")
            }

            override fun onDragStateChanged(newState: Int) {
                // When the drag state changes
                Log.d("clearScreenLayout", "newState $newState")
            }
        })
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
            binding.layoutBottomAction.isVisible = mIsRoomOwner
        } else {
            binding.tvInput.isVisible = false
            binding.likeView.isVisible = false
            binding.ivGift.isVisible = false
            binding.layoutBottomAction.isVisible = false
        }
    }

    private fun showKeyboardInputLayout() {
        binding.layoutEtMessage.isVisible = true
        binding.tvInput.isEnabled = false

        // Hide
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
            JoyServiceProtocol.ROOM_AVAILABLE_DURATION - mJoyService.getCurrentRoomDuration(mRoomInfo.roomId)
        if (roomLeftTime > 0) {
            toggleSelfVideo {
                initRtcEngine()
                getStartGameInfo()
            }
            toggleSelfAudio { }
            startTopLayoutTimer()
        } else {
            CustomToast.show(getString(R.string.joy_living_end))
            innerRleasee()
            finish()
            return
        }
        mJoyService.subscribeListener(object : JoyServiceListenerProtocol {
            override fun onUserListDidChanged(userList: List<AUIUserInfo>) {
            }

            override fun onMessageDidAdded(message: JoyMessage) {
                mMessageAdapter.insertLast(message)
                binding.rvMessage.scrollToPosition(mMessageAdapter.itemCount - 1)
            }

            override fun onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo) {
                mStartGameInfo = startGameInfo
                val gameId = mStartGameInfo?.gameId ?: ""
                if (!mIsRoomOwner && gameId.isNotEmpty()) {
                    // Audience received room game start
                    mJoyViewModel.getGameDetail(gameId)
                    // Load game screen
                    setupAssistantVideoView()
                }
            }

            override fun onRoomDestroy() {
                innerRleasee()
                showCreatorExitDialog()
            }

            override fun onRoomExpire() {
                innerRleasee()
                showTimeUpExitDialog()
            }
        })

        mJoyViewModel.mGameDetailLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "GameDetail：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    binding.tvRules.isVisible = true
                    showBottomView(true)
                    if (mIsRoomOwner) {
                        showRulesDialog()
                        setupActionView(it.data?.actions)
                    }
                }

                else -> {}
            }
        }
        mJoyViewModel.mStartGameLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "StartGame：$it")
            when (it.dataState) {
                DataState.STATE_LOADING -> {
                    showLoadingView()
                }

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
                        assistantUid = 1000000000 + mUser.id.toInt(),
                        gameName = gameSelect.name ?: ""
                    )
                    // Get game details
                    mJoyViewModel.getGameDetail(gameSelect.gameId!!)
                    // Host priority loading game screen
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
            JoyLogger.d(JoyApiService.TAG, "StopGame：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                }

                else -> {}
            }
        }
        mJoyViewModel.mSendGiftLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "SendGift：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    CustomToast.showTips(getString(R.string.joy_send_gift_success))
                }

                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_send_gift_failed))
                }

                else -> {}
            }
        }
        mJoyViewModel.mSendCommentLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "SendComment：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    CustomToast.showTips(getString(R.string.joy_send_message_success))
                }

                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_instruction_error))
                }

                else -> {}
            }
        }
        mJoyViewModel.mSendLikeLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "SendLike：$it")
            when (it.dataState) {
                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_request_failed))
                }

                else -> {}
            }
        }
        mJoyViewModel.mGameStatusLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "getGameStatus：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    if (it.data?.status == JoyGameStatus.started.name) {
                        val gameId = mStartGameInfo?.gameId ?: ""
                        mJoyViewModel.getGameDetail(gameId)
                    } else if (it.data?.status == JoyGameStatus.stopped.name) {
                        // Game paused, directly select game
                        if (mIsRoomOwner) {
                            mJoyViewModel.getGames()
                        }
                    }
                }

                else -> {}
            }
        }
        mJoyViewModel.mGameListLiveData.observe(this) {
            JoyLogger.d(JoyApiService.TAG, "getGames：$it")
            when (it.dataState) {
                DataState.STATE_SUCCESS -> {
                    it.data?.list?.apply {
                        mGameList.clear()
                        mGameList.addAll(this)
                    }
                    showGameChooseDialog()
                }

                DataState.STATE_FAILED,
                DataState.STATE_ERROR -> {
                    CustomToast.showError(getString(R.string.joy_request_failed))
                }

                else -> {}
            }
        }
    }

    private fun setupActionView(actions: List<JoyAction>?) {
        binding.layoutBottomAction.removeAllViews()
        actions?.forEach { action ->
            val actionImage = ImageView(this)
            actionImage.tag = action
            val imageParams = LinearLayout.LayoutParams(
                36.dp.toInt(),
                36.dp.toInt()
            ).apply {
                gravity = Gravity.BOTTOM
            }
            actionImage.layoutParams = imageParams

            binding.layoutBottomAction.addView(actionImage)

            GlideApp.with(binding.root)
                .load(action.icon ?: "")
                .error(R.drawable.joy_icon_deploy_troops)
                .apply(RequestOptions.circleCropTransform())
                .into(actionImage)
            actionImage.setOnTouchListener { v, event ->
                if (!mIsRoomOwner) return@setOnTouchListener false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        setControllerView(actionImage, false)
                        action.command?.forEach { command ->
                            sendKeyboardMessage(KeyboardEventType.KEYBOARD_EVENT_KEY_DOWN, command[0])
                        }

                    }

                    MotionEvent.ACTION_UP -> {
                        setControllerView(actionImage, true)
                        action.command?.forEach { command ->
                            sendKeyboardMessage(KeyboardEventType.KEYBOARD_EVENT_KEY_UP, command[0])
                        }
                    }
                }
                return@setOnTouchListener true
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
                        // todo Remote game exit
                        innerRleasee()
                        showAssistantUidOffline()
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
                if (uid == mStartGameInfo?.assistantUid) {
                    // Hide loading when first frame of cloud machine is received
                    hideLoadingView()
                }
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
        mVideoTextureView?.post {
            val navHeight = if (::mRootInset.isInitialized) mRootInset.bottom else navBarHeight
            if (targetWidth == rootViewWidth) {
                // Align width
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
                VideoCanvas(
                    textureView,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    mRoomInfo.roomOwner?.userId?.toIntOrNull() ?: 0
                ),
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

    private fun getStartGameInfo() {
        mJoyService.getStartGame(mRoomInfo.roomId, completion = { error, startGameInfo ->
            if (error == null) { //success
                mStartGameInfo = startGameInfo
                val gameId = mStartGameInfo?.gameId ?: ""
                binding.tvRules.isVisible = gameId.isNotEmpty()
                if (gameId.isNotEmpty()) {
                    // Load game screen
                    setupAssistantVideoView()
                    val taskId = mStartGameInfo?.taskId ?: return@getStartGame
                    // Get game status
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
        // If it is an audience, set the audienceLatencyLevel of ChannelMediaOptions to AUDIENCE_LATENCY_LEVEL_LOW_LATENCY (ultra-low latency)
        if (!mIsRoomOwner) {
            channelMediaOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
        }

        mRtcEngine.joinChannelEx(
            JoyServiceManager.mRtcToken,
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
            AudioModeration.AgoraChannelType.Broadcast,
            "Joy"
        )
    }

    private fun enableContentInspectEx() {
        // ------------------ Enable content inspection service ------------------
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
                val currentTime = mJoyService.getCurrentRoomDuration(mRoomInfo.roomId)
                if (currentTime > 0) {
                    binding.tvTimer.text = dataFormat.format(Date(currentTime))
                }
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
                        // Start game
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
        if (!isShownRoomDuration){
            showRoomDurationNotice(SceneConfigManager.joyExpireTime)
            isShownRoomDuration = true
        }
    }

    private fun checkCloseByEvent(x: Int, y: Int) {
        if (::mCloseRect.isInitialized) {
            if (x >= mCloseRect.left && x <= mCloseRect.right && y >= mCloseRect.top && y <= mCloseRect.bottom) {
                showExitRoomDialog()
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

    // Host destroys room
    private fun showCreatorExitDialog() {
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle("")
            .setMessage(R.string.joy_living_destroy_content)
            .setCancelable(false)
            .setPositiveButton(io.agora.scene.widget.R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    // Room timeout
    private fun showTimeUpExitDialog() {
        val message = if (mIsRoomOwner) getString(R.string.joy_living_host_timeout, SceneConfigManager.joyExpireTime) else getString(R.string.joy_living_user_timeout)
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(R.string.joy_living_timeout_title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(io.agora.scene.widget.R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    // Remote robot exits
    private fun showAssistantUidOffline() {
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(R.string.joy_living_abnormal_title)
            .setMessage(R.string.joy_living_assistantUid_offline)
            .setCancelable(false)
            .setPositiveButton(io.agora.scene.widget.R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    // Exit room prompt
    private fun showExitRoomDialog() {
        val title =
            if (mIsRoomOwner) io.agora.scene.widget.R.string.dismiss_room else io.agora.scene.widget.R.string.exit_room
        val message =
            if (mIsRoomOwner) io.agora.scene.widget.R.string.confirm_to_dismiss_room else io.agora.scene.widget.R.string.confirm_to_exit_room
        AlertDialog.Builder(this, R.style.joy_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(io.agora.scene.widget.R.string.confirm) { dialog, id ->
                dialog.dismiss()
                exitRoom()
                finish()
            }
            .setNegativeButton(io.agora.scene.widget.R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
            .show()
    }

    private fun exitRoom() {
        mJoyService.leaveRoom { e: Exception? ->
            if (e == null) { // success
                JoyLogger.d(TAG, "RoomLivingViewModel.exitRoom() success")
            } else { // failure
                JoyLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: " + e.message)
            }
            e?.message?.let { error ->
                io.agora.scene.widget.toast.CustomToast.show(error, Toast.LENGTH_SHORT)
            }
        }
        innerRleasee()
    }

    private fun innerRleasee() {
        (binding.tvTimer.tag as? Runnable)?.let {
            it.run()
            binding.tvTimer.removeCallbacks(it)
            binding.tvTimer.tag = null
        }
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
        JoyLogger.d(TAG, "sendKeyboardMessage:$eventType,key:$key")
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
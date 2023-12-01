package io.agora.scene.joy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.ContentInspectConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.scene.base.AudioModeration
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.databinding.JoyLiveDetailActivityBinding
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.ui.widget.JoyGameRulesDialog
import io.agora.scene.joy.ui.widget.JoyGiftDialog
import io.agora.scene.joy.ui.widget.KeyboardStatusWatcher
import io.agora.scene.joy.utils.JoyLogger
import io.agora.scene.joy.utils.dp
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class RoomLivingActivity : BaseViewBindingActivity<JoyLiveDetailActivityBinding>() {

    companion object {
        private const val TAG = "Joy_RoomLivingActivity"
        private const val EXTRA_ROOM_DETAIL_INFO = "roomDetailInfo"

        fun launch(context: Context, roomInfo: JoyRoomInfo) {
            val intent = Intent(context, RoomLivingActivity::class.java)
            intent.putExtra(EXTRA_ROOM_DETAIL_INFO, roomInfo)
            context.startActivity(intent)
        }
    }

    val mRoomInfo by lazy { (intent?.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as? JoyRoomInfo)!! }

    private val mMainRtcConnection by lazy {
        RtcConnection(
            mRoomInfo.roomId,
            UserManager.getInstance().user.id.toInt()
        )
    }
    private val mIsRoomOwner by lazy { mRoomInfo.ownerId.toLong() == UserManager.getInstance().user.id }


    private val mJoyService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }

    private val mTimerRoomEndRun = Runnable {
        destroy() // 房间到了限制时间
        showLivingEndLayout() // 房间到了限制时间
        JoyLogger.d("showLivingEndLayout", "timer end!")
    }

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

    override fun getViewBinding(inflater: LayoutInflater): JoyLiveDetailActivityBinding {
        return JoyLiveDetailActivityBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding.tvRoomName.text = mRoomInfo.roomName
        binding.tvRoomId.text = mRoomInfo.roomId
        GlideApp.with(this)
            .load(mRoomInfo.ownerAvatar)
            .placeholder(R.mipmap.userimage)
            .error(R.mipmap.userimage)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)
        binding.ivClose.setOnClickListener {
            showEndRoomDialog()
        }
        binding.ivMore.setOnClickListener {
            TopFunctionDialog(this).show()
        }
        binding.chooseGameLayout.btnConfirm.setOnClickListener {
            binding.chooseGameLayout.root.isVisible = false
        }
        binding.tvRules.setOnClickListener {
            val bundle = Bundle().apply {
                putString(JoyGameRulesDialog.Key_Content, "test")
            }
            val dialog = JoyGameRulesDialog().apply {
                setBundleArgs(bundle)
            }
            dialog.show(supportFragmentManager, "rulesDialog")
        }
        binding.ivGift.setOnClickListener {
            JoyGiftDialog().show(supportFragmentManager, "giftDialog")
        }
        binding.tvInput.setOnClickListener {
           showKeyboardInputLayout()
        }
        binding.etMessage.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    Log.d(TAG, "action send：${v.text}")
                    showNormalInputLayout()
                }
            }
            true
        }
        binding.likeView.likeView.setOnClickListener { binding.likeView.addFavor() }
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

    fun setViewLayoutParams(view: View, width: Int, height: Int) {
        val lp = view.layoutParams
        lp.width = width
        lp.height = height
        view.layoutParams = lp
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

    private fun showKeyboardInputLayout(){
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
            binding.root.postDelayed(mTimerRoomEndRun, JoyServiceProtocol.ROOM_AVAILABLE_DURATION)
            mToggleVideoRun = Runnable {
                initRtcEngine()
                initServiceWithJoinRoom()
            }
            requestCameraPermission(true)
            startTopLayoutTimer()
        }
    }

    private fun initRtcEngine() {
        val eventListener = object : IRtcEngineEventHandler() {
            override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                super.onJoinChannelSuccess(channel, uid, elapsed)

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

    private fun initServiceWithJoinRoom() {
        mJoyService.joinRoom(mRoomInfo, completion = {
            if (it == null) { //success

            } else {

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
        binding.root.removeCallbacks(mTimerRoomEndRun)
        (binding.tvTimer.tag as? Runnable)?.let {
            it.run()
            binding.tvTimer.removeCallbacks(it)
            binding.tvTimer.tag = null
        }
        mJoyService.leaveRoom(mRoomInfo, {})
        if (mIsRoomOwner) {
            mRtcEngine.stopPreview()
        }
        mRtcEngine.leaveChannelEx(mMainRtcConnection)
    }
}
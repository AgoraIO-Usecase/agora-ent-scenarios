package io.agora.scene.playzone.live

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.Constants
import io.agora.rtmsyncmanager.model.AUIRoomInfo
import io.agora.scene.base.GlideApp
import io.agora.scene.base.SceneConfigManager
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.event.NetWorkEvent
import io.agora.scene.base.utils.dp
import io.agora.scene.base.utils.statusBarHeight
import io.agora.scene.playzone.PlayCenter
import io.agora.scene.playzone.PlayLogger
import io.agora.scene.playzone.R
import io.agora.scene.playzone.databinding.PlayZoneActivityRoomGameLayoutBinding
import io.agora.scene.playzone.live.sub.QuickStartGameViewModel
import io.agora.scene.playzone.service.PlayZoneParameters
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.dialog.showRoomDurationNotice
import io.agora.scene.widget.utils.KeyboardStatusWatcher
import tech.sud.mgp.SudMGPWrapper.model.GameViewInfoModel.GameViewRectModel

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

    private val gameViewModel = QuickStartGameViewModel()

    private val roomGameViewModel: PlayGameViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val roomInfo = (intent.getSerializableExtra(EXTRA_ROOM_DETAIL_INFO) as AUIRoomInfo?)!!
                return PlayGameViewModel(roomInfo) as T
            }
        })[PlayGameViewModel::class.java]
    }


    private var mToggleAudioRun: Runnable? = null

    override fun getPermissions() {
        mToggleAudioRun?.let {
            it.run()
            mToggleAudioRun = null
        }
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (showNormalInputLayout()) return
            showEndRoomDialog()
        }
    }

    override fun finish() {
        onBackPressedCallback.remove()
        super.finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        showRoomDurationNotice(SceneConfigManager.joyExpireTime)
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
            // Set game safe operation area
            val gameViewRectModel = GameViewRectModel()
            gameViewRectModel.left = 0
            gameViewRectModel.top = binding.layoutTop.height + statusBarHeight
            gameViewRectModel.right = 0
            gameViewRectModel.bottom = binding.layoutBottom.height
            gameViewModel.gameViewRectModel = gameViewRectModel
            PlayLogger.d(TAG, "gameViewRectModel: $gameViewRectModel")

            // Game configuration
            val gameConfigModel = gameViewModel.getGameConfigModel()
            gameConfigModel.ui.ping.hide = false // Configuration to not hide ping value
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

        binding.tvRoomName.text = roomGameViewModel.mRoomInfo.roomName
        binding.tvRoomId.text = roomGameViewModel.mRoomInfo.roomId
        GlideApp.with(this)
            .load(roomGameViewModel.mRoomInfo.roomOwner?.userAvatar ?: "")
            .placeholder(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
            .apply(RequestOptions.circleCropTransform())
            .into(binding.ivOwnerAvatar)


        binding.ivClose.setOnClickListener {
            showEndRoomDialog()
        }
        binding.ivMore.setOnClickListener {
            TopFunctionDialog(this).show()
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
                        roomGameViewModel.sendMessage(content)
                    }
                }
            }
            true
        }
        binding.ivAddBot.setOnClickListener {
            if (gameViewModel.sudFSMMGCache.captainUserId == PlayCenter.mUser.id.toString()) {
                gameViewModel.addRobot()
            }
        }

        binding.cbMic.isChecked = roomGameViewModel.isRoomOwner
        binding.cbMic.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (!compoundButton.isPressed) return@setOnCheckedChangeListener
            if (isChecked) {
                mToggleAudioRun = Runnable {
                    roomGameViewModel.muteMic(false)
                }
                requestRecordPermission(true)
            } else {
                roomGameViewModel.muteMic(true)
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
        // Hide
        binding.layoutBottom.isVisible = false
        showInput(binding.etMessage)
    }

    override fun requestData() {
        super.requestData()

        roomGameViewModel.initChatRoom(binding.chatListView)
        mToggleAudioRun = Runnable {
            roomGameViewModel.initData()
        }
        requestRecordPermission()
        val gameId = roomGameViewModel.mRoomInfo.customPayload[PlayZoneParameters.GAME_ID] as? Long ?: 0L
        gameViewModel.switchGame(this, roomGameViewModel.mRoomInfo.roomId, gameId)

        gameViewModel.gameViewLiveData.observe(this) { view ->
            if (view == null) { // When closing the game, remove the gameView
                binding.gameContainer.removeAllViews()
            } else { // Add the gameView to the container
                binding.gameContainer.addView(
                    view,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
        }
        gameViewModel.gameStartedLiveData.observe(this) {
            if (it) {
                if (roomGameViewModel.isRoomOwner) {
                    gameViewModel.notifyAPPCommonSelfIn(true, 0, false, 1)
                }
            }
        }
        gameViewModel.captainIdLiveData.observe(this) {
            val supportRobots = gameViewModel.supportRobots(gameId)
            binding.ivAddBot.isVisible = supportRobots && it.first == PlayCenter.mUser.id.toString() && it.second
        }
        gameViewModel.gameMessageLiveData.observe(this) {
            roomGameViewModel.insertLocalMessage(it)
        }

        roomGameViewModel.mRoomTimeLiveData.observe(this) {
            binding.tvTimer.text = it
        }
        roomGameViewModel.networkStatusLiveData.observe(this) { netWorkStatus: NetWorkEvent ->
            setNetWorkStatus(netWorkStatus.txQuality, netWorkStatus.rxQuality)
        }
        roomGameViewModel.roomExpireLiveData.observe(this) { roomExpire ->
            if (roomExpire) {
                gameViewModel.destroyMG()
                showTimeUpExitDialog()
            }
        }
        roomGameViewModel.roomDestroyLiveData.observe(this) { roomDestroy ->
            if (roomDestroy) {
                gameViewModel.destroyMG()
                showCreatorExitDialog()
            }
        }
        roomGameViewModel.mRobotListLiveData.observe(this) { robotList ->
            if (robotList.isNotEmpty()) {
                gameViewModel.robotInfoList.clear()
                gameViewModel.robotInfoList.addAll(robotList)
            }
        }
    }

    private fun setNetWorkStatus(txQuality: Int, rxQuality: Int) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            binding.ivNetStatus.setImageResource(io.agora.scene.widget.R.drawable.bg_round_yellow)
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_DOWN) {
            binding.ivNetStatus.setImageResource(io.agora.scene.widget.R.drawable.bg_round_red)
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            binding.ivNetStatus.setImageResource(io.agora.scene.widget.R.drawable.bg_round_green)
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            binding.ivNetStatus.setImageResource(io.agora.scene.widget.R.drawable.bg_round_red)
        } else {
            binding.ivNetStatus.setImageResource(io.agora.scene.widget.R.drawable.bg_round_green)
        }
    }

    /**
     * Game rules
     *
     */
//    private fun showRulesDialog() {
//        // TODO:
//        val gameInfo = PlayGameInfoModel()
//        val bundle = Bundle().apply {
//            putSerializable(PlayGameRulesDialog.Key_Game, gameInfo)
//            putBoolean(PlayGameRulesDialog.Key_IsOwner, roomGameViewModel.isRoomOwner)
//        }
//        val dialog = PlayGameRulesDialog().apply {
//            setBundleArgs(bundle)
//        }
//        dialog.show(supportFragmentManager, "rulesDialog")
//    }

    private fun showCreatorExitDialog() {
        val title = R.string.play_zone_living_destroy_title
        val message = R.string.play_zone_living_destroy_content
        AlertDialog.Builder(this, R.style.play_zone_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(io.agora.scene.widget.R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }


    private fun showTimeUpExitDialog() {
        val title = R.string.play_zone_living_timeout_title
        val message =
            if (roomGameViewModel.isRoomOwner) getString(R.string.play_zone_living_host_timeout, SceneConfigManager.joyExpireTime/ 60 ) else getString(R.string.play_zone_living_user_timeout)
        AlertDialog.Builder(this, R.style.play_zone_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(io.agora.scene.widget.R.string.i_know) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    /**
     * Exit room
     *
     */
    private fun showEndRoomDialog() {
        val title =
            if (roomGameViewModel.isRoomOwner) R.string.play_zone_living_host_end_title else R.string.play_zone_living_user_end_title
        val message =
            if (roomGameViewModel.isRoomOwner) R.string.play_zone_living_host_end_content else R.string.play_zone_living_user_end_content
        AlertDialog.Builder(this, R.style.play_zone_alert_dialog)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(io.agora.scene.widget.R.string.confirm) { dialog, id ->
                dialog.dismiss()
                setDarkStatusIcon(isBlackDarkStatus)
                roomGameViewModel.exitRoom()
                finish()
            }
            .setNegativeButton(io.agora.scene.widget.R.string.cancel) { dialog, id ->
                setDarkStatusIcon(isBlackDarkStatus)
                dialog.dismiss()
            }
            .show()
    }


    override fun onResume() {
        super.onResume()
        gameViewModel.onResume()
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.onPause()
    }

    override fun onDestroy() {
        gameViewModel.destroyMG()
        super.onDestroy()
        // TODO: Log upload
//        if (SceneConfigManager.logUpload) {
//            LogUploader.uploadLog(LogUploader.SceneType.JOY)
//        }
    }
}
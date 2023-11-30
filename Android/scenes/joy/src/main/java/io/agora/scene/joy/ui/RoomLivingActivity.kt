package io.agora.scene.joy.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.component.BaseViewBindingActivity
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.JoyLogger
import io.agora.scene.joy.R
import io.agora.scene.joy.RtcEngineInstance
import io.agora.scene.joy.databinding.JoyLiveDetailActivityBinding
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.service.JoyServiceProtocol
import io.agora.scene.joy.ui.widget.JoyGameRulesDialog
import io.agora.scene.joy.ui.widget.JoyGiftDialog
import io.agora.scene.joy.videoLoaderAPI.VideoLoader
import io.agora.scene.widget.dialog.PermissionLeakDialog
import io.agora.scene.widget.dialog.TopFunctionDialog
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
    private val isRoomOwner by lazy { mRoomInfo.ownerId.toLong() == UserManager.getInstance().user.id }


    private val mService by lazy { JoyServiceProtocol.getImplInstance() }
    private val mRtcEngine by lazy { RtcEngineInstance.rtcEngine }
    private val mRtcVideoLoaderApi by lazy { VideoLoader.getImplInstance(mRtcEngine) }

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
        binding.ivLike.setOnClickListener {
            ToastUtils.showToast("click like")
        }
        binding.tvSendMessage.setOnClickListener {
            binding.layoutEtMessage.isVisible = true
            showInput(binding.etMessage)
        }
        binding.etMessage.setOnEditorActionListener { v, actionId, event ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    Log.d(TAG, "action send：${v.text}")
                }
            }
            true
        }
    }

    override fun requestData() {
        super.requestData()
        val roomLeftTime =
            JoyServiceProtocol.ROOM_AVAILABLE_DURATION - (TimeUtils.currentTimeMillis() - mRoomInfo.createdAt)
        if (roomLeftTime > 0) {
            binding.root.postDelayed(mTimerRoomEndRun, JoyServiceProtocol.ROOM_AVAILABLE_DURATION)
            mToggleVideoRun = Runnable {

//                initRtcEngine()
//            initServiceWithJoinRoom()
            }
            requestCameraPermission(true)

        }
        startTopLayoutTimer()
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

    private fun destroy() {
        binding.root.removeCallbacks(mTimerRoomEndRun)
        (binding.tvTimer.tag as? Runnable)?.let {
            it.run()
            binding.tvTimer.removeCallbacks(it)
            binding.tvTimer.tag = null
        }
        mService.leaveRoom(mRoomInfo, {})
        if (isRoomOwner) {
            mRtcEngine.stopPreview()
            mRtcEngine.leaveChannelEx(mMainRtcConnection)
        }
        if (isRoomOwner) {
            mRtcEngine.stopPreview()
            mRtcEngine.leaveChannelEx(mMainRtcConnection)
        }
    }

}
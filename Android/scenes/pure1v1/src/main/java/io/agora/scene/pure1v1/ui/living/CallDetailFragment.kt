package io.agora.scene.pure1v1.ui.living

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcConnection
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailFragmentBinding
import io.agora.scene.pure1v1.CallServiceManager
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.scene.pure1v1.R
import io.agora.onetoone.*
import io.agora.scene.pure1v1.rtt.PureRttDialog
import io.agora.scene.pure1v1.rtt.PureRttManager
import io.agora.scene.pure1v1.rtt.RttEventListener
import io.agora.scene.widget.dialog.TopFunctionDialog
import io.agora.scene.widget.toast.CustomToast
import java.util.concurrent.TimeUnit

/*
 * 1v1 Interactive page
 */
class CallDetailFragment : Fragment(), ICallApiListener, RttEventListener {

    private lateinit var binding: Pure1v1CallDetailFragmentBinding

    private val TAG = "CallDetail"

    private var startTime = System.currentTimeMillis()
    private var timerHandler: Handler? = null
    private var dashboard: DashboardFragment? = null

    private var cameraOn = true
    private var micOn = true

    private var settingDialog: CallDetailSettingDialog? = null
    private var rttDialog: PureRttDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = Pure1v1CallDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        CallServiceManager.instance.callApi?.addListener(this)
        PureRttManager.addListener(this)

        timerHandler = Handler(Looper.getMainLooper())

        CallServiceManager.instance.prepareForCall { }
    }

    fun start() {
        startTime = System.currentTimeMillis()
        cameraOn = true
        micOn = true

        runOnUiThread {
            CallServiceManager.instance.remoteUser?.let { userInfo ->
                Glide.with(this)
                    .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserAvatar)
                binding.tvRoomTitle.text = userInfo.userName
                binding.tvRoomNum.text = userInfo.userId
            }
            CallServiceManager.instance.remoteUser?.let { userInfo ->
                binding.vDragWindow1.setUserName(userInfo.userName)
                binding.vDragWindow1.showComeBackSoonView(false)
            }
            CallServiceManager.instance.localUser?.let { userInfo ->
                binding.vDragWindow2.setUserName(userInfo.userName)
                binding.vDragWindow2.showComeBackSoonView(false)
            }
        }

        // Monitor video stream status callback after call starts, used to display corresponding UI when video stream status changes
        // Since CallAPI uses joinChannelEx to join the channel, addHandlerEx needs to be used here to register the listener
        CallServiceManager.instance.rtcEngine?.addHandlerEx(
            object : IRtcEngineEventHandler() {
                override fun onRemoteVideoStateChanged(
                    uid: Int,
                    state: Int,
                    reason: Int,
                    elapsed: Int
                ) {
                    super.onRemoteVideoStateChanged(uid, state, reason, elapsed)
                    if (state == Constants.REMOTE_VIDEO_STATE_STOPPED || state == Constants.REMOTE_VIDEO_STATE_FAILED) {
                        // Remote video stopped receiving
                        runOnUiThread {
                            binding.vDragWindow1.showComeBackSoonView(true)
                        }
                    } else if (state == Constants.REMOTE_VIDEO_STATE_STARTING || state == Constants.REMOTE_VIDEO_STATE_DECODING) {
                        // Remote video playing normally
                        runOnUiThread {
                            binding.vDragWindow1.showComeBackSoonView(false)
                        }
                    }
                }

                override fun onStreamMessage(uid: Int, streamId: Int, data: ByteArray?) {
                    super.onStreamMessage(uid, streamId, data)
                    if (uid.toString() != PureRttManager.pubBotUid) return
                    // Set data and uid values obtained from rtc's onStreamMessage
                    runOnUiThread {
                        binding.transcriptSubtitleView.pushMessageData(data, uid)
//                        // Get all transcription content
//                        binding.transcriptSubtitleView.getAllTranscriptText()
//                        // Get all translation content
//                        binding.transcriptSubtitleView.getAllTranslateText()
//                        // Clear all transcription and translation content
//                        binding.transcriptSubtitleView.clear()
                    }
                }
            },
            RtcConnection(
                CallServiceManager.instance.connectedChannelId,
                CallServiceManager.instance.localUser?.userId!!.toInt()
            )
        )
    }

    fun updateTime() {
        if (!isAdded || parentFragmentManager.isDestroyed) return
        val millis = System.currentTimeMillis() - startTime
        if (millis > (20 * 60 * 1000)) {
            onHangup()
            return
        }
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.tvTime.text = formattedTime
        timerHandler?.postDelayed({
            updateTime()
        }, 1000)
    }

    fun initDashBoard(channelId: String, localUid: Int) {
        dashboard?.setupRTCListener(channelId, localUid)
    }

    fun reset() {
        settingDialog?.dismiss()
        rttDialog?.dismiss()
        timerHandler?.removeCallbacksAndMessages(null)
        runOnUiThread {
            binding.transcriptSubtitleView.clear()
            binding.vDragWindow1.showComeBackSoonView(false)
            binding.vDragWindow2.showComeBackSoonView(false)
        }
        PureRttManager.disableRtt(true) {}
    }

    private fun setupView() {
        binding.vDragWindow1.setComeBackSoonViewStyle(false)
        binding.vDragWindow2.setComeBackSoonViewStyle(true)

        // Set view container into manager
        CallServiceManager.instance.localCanvas = binding.vDragWindow2.canvasContainer
        CallServiceManager.instance.remoteCanvas = binding.vDragWindow1.canvasContainer

        binding.vDragWindow1.setSmallType(false)
        binding.vDragWindow2.setSmallType(true)

        binding.vDragWindow2.setOnViewClick {
            exchangeDragWindow()
        }
        binding.ivHangup.setOnClickListener {
            onHangup()
        }
        binding.ivSetting.setOnClickListener {
            onClickSetting()
        }
        binding.ivRtt.setOnClickListener {
            onClickRtt()
        }
        binding.ivMore.setOnClickListener {
            this.activity?.let {
                TopFunctionDialog(it).show()
            }
        }
        binding.ivClose.setOnClickListener {
            binding.ivClose.visibility = View.INVISIBLE
            binding.flDashboard.visibility = View.INVISIBLE
            dashboard?.updateVisible(false)
        }
        val fragment = DashboardFragment()
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        dashboard = fragment
    }

    private fun exchangeDragWindow() {
        val params1 = LayoutParams(binding.vDragWindow1.width, binding.vDragWindow1.height)
        params1.topMargin = binding.vDragWindow1.top
        params1.leftMargin = binding.vDragWindow1.left
        val params2 = LayoutParams(binding.vDragWindow2.width, binding.vDragWindow2.height)
        params2.topMargin = binding.vDragWindow2.top
        params2.leftMargin = binding.vDragWindow2.left
        binding.vDragWindow1.layoutParams = params2
        binding.vDragWindow2.layoutParams = params1
        if (binding.vDragWindow1.layoutParams.height > binding.vDragWindow2.layoutParams.height) {
            binding.vDragWindow2.bringToFront()
            binding.vDragWindow2.setSmallType(true)
            binding.vDragWindow2.setOnViewClick {
                exchangeDragWindow()
            }
            binding.vDragWindow1.setOnViewClick(null)
            binding.vDragWindow1.setSmallType(false)
            CallServiceManager.instance.remoteUser?.let { userInfo ->
                Glide.with(this)
                    .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserAvatar)
                binding.tvRoomTitle.text = userInfo.userName
                binding.tvRoomNum.text = userInfo.userId
            }
        } else {
            binding.vDragWindow1.bringToFront()
            binding.vDragWindow1.setSmallType(true)
            binding.vDragWindow1.setOnViewClick {
                exchangeDragWindow()
            }
            binding.vDragWindow2.setOnViewClick(null)
            binding.vDragWindow2.setSmallType(false)
            CallServiceManager.instance.localUser?.let { userInfo ->
                Glide.with(this)
                    .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserAvatar)
                binding.tvRoomTitle.text = userInfo.userName
                binding.tvRoomNum.text = userInfo.userId
            }
        }
    }

    private fun onClickSetting() {
        val context = context ?: return
        val dialog = CallDetailSettingDialog(context, cameraOn, micOn)
        dialog.setListener(object : CallDetailSettingDialog.CallDetailSettingItemListener {
            override fun onClickDashboard() {
                binding.flDashboard.visibility = View.VISIBLE
                binding.ivClose.visibility = View.VISIBLE
                dashboard?.updateVisible(true)
            }

            override fun onCameraSwitch(isCameraOn: Boolean) {
                cameraOn = isCameraOn
                if (cameraOn) {
                    binding.vDragWindow2.showComeBackSoonView(false)
                } else {
                    binding.vDragWindow2.showComeBackSoonView(true)
                }
                CallServiceManager.instance.switchCamera(isCameraOn)
            }

            override fun onMicSwitch(isMicOn: Boolean) {
                micOn = isMicOn
                CallServiceManager.instance.switchMic(isMicOn)
            }
        })
        settingDialog = dialog
        dialog.show()
    }

    private fun onClickRtt() {
        val context = context ?: return
        val channelName = CallServiceManager.instance.connectedChannelId ?: return
        val dialog = PureRttDialog(context, channelName)
        rttDialog = dialog
        dialog.show()
    }

    override fun onRttStart() {
        binding.ivRtt.setImageResource(R.drawable.pure1v1_icon_rtt_enable)
    }

    override fun onRttStop() {
        binding.ivRtt.setImageResource(R.drawable.pure1v1_icon_rtt_disable)
    }

    private fun onHangup() {
        CallServiceManager.instance.remoteUser?.let { userInfo ->
            CallServiceManager.instance.callApi?.hangup(userInfo.userId.toInt(), reason = "hangup by user") {
            }
        }
        binding.transcriptSubtitleView.clear()
        PureRttManager.disableRtt(true) {}
        timerHandler?.removeCallbacksAndMessages(null)
    }

    // ----------------------- ICallApiListener -----------------------

    override fun onCallStateChanged(
        state: CallStateType,
        stateReason: CallStateReason,
        eventReason: String,
        eventInfo: Map<String, Any>
    ) {
    }

    override fun onCallEventChanged(event: CallEvent, eventReason: String?) {
        when (event) {
            CallEvent.RemoteLeft -> {
                eventReason?.let {
                    if (it.toInt() == Constants.USER_OFFLINE_DROPPED) {
                        CustomToast.show(getString(R.string.pure1v1_call_toast_hangup2))
                    }
                }
                onHangup()
            }

            else -> {}
        }
    }

    override fun onCallError(
        errorEvent: CallErrorEvent,
        errorType: CallErrorCodeType,
        errorCode: Int,
        message: String?
    ) {
        super.onCallError(errorEvent, errorType, errorCode, message)
        Pure1v1Logger.d(
            TAG,
            "onCallError: errorEvent$errorEvent, errorType:$errorType, errorCode:$errorCode, message:$message"
        )
    }

    override fun canJoinRtcOnCalling(eventInfo: Map<String, Any>): Boolean {
        return true
    }

    // ------------------------ inner private --------------------------
    private val mHandler = Handler(Looper.getMainLooper())
    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}
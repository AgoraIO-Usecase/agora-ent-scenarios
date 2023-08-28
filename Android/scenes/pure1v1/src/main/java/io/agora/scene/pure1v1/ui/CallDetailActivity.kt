package io.agora.scene.pure1v1.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.scene.base.component.BaseBindingActivity
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.callAPI.CallEvent
import io.agora.scene.pure1v1.callAPI.CallReason
import io.agora.scene.pure1v1.callAPI.CallStateType
import io.agora.scene.pure1v1.callAPI.ICallApiListener
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailActivityBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import java.util.concurrent.TimeUnit

class CallDetailActivity : BaseBindingActivity<Pure1v1CallDetailActivityBinding>(), ICallApiListener {

    private val tag = "CallDetailActivity_LOG"

    private val startTime = System.currentTimeMillis()
    private var timerHandler: Handler? = null
    private var dashboard: DashboardFragment? = null
    private var rtcEventHandler: IRtcEngineEventHandler? = null

    override fun getViewBinding(inflater: LayoutInflater): Pure1v1CallDetailActivityBinding {
        return Pure1v1CallDetailActivityBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupView()
        CallServiceManager.instance.callApi?.addListener(this)
        setupRTCListener()

        timerHandler = Handler(Looper.getMainLooper())
        updateTime()
    }

    override fun onBackPressed() {
        onHangup()
        super.onBackPressed()
    }

    private fun setupRTCListener() {
        val handler = object: IRtcEngineEventHandler() {
            override fun onContentInspectResult(result: Int) {
                Log.d(tag, "onContentInspectResult = $result")
                if (result > 1) {
                    Toast.makeText(this@CallDetailActivity, getText(R.string.pure1v1_call_content_inspect), Toast.LENGTH_SHORT).show()
                }
            }
        }
        CallServiceManager.instance.callApi?.addRTCListener(handler)
        rtcEventHandler = handler
    }

    private fun updateTime() {
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

    private fun setupView() {
        binding.ivHangup.setOnClickListener {
            onHangup()
        }
        binding.ivSetting.setOnClickListener {
            onClickSetting()
        }
        binding.ivClose.setOnClickListener {
            binding.ivClose.visibility = View.INVISIBLE
            binding.flDashboard.visibility = View.INVISIBLE
            dashboard?.updateVisible(false)
        }
        CallServiceManager.instance.remoteUser?.let { userInfo ->
            Glide.with(this)
                .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                .into(binding.ivUserAvatar)
            binding.tvRoomName.text = userInfo.userName
            binding.tvRoomNum.text = userInfo.getRoomId()
        }
        CallServiceManager.instance.remoteCanvas?.let { canvas ->
            binding.llContainer.removeAllViews()
            binding.llContainer.addView(canvas)
        }
        CallServiceManager.instance.localUser?.let { userInfo ->
            binding.vDragWindow.setUserName(userInfo.userName)
        }
        CallServiceManager.instance.localCanvas?.let { canvas ->
            binding.vDragWindow.canvasContainer.addView(canvas)
        }
        val fragment = DashboardFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(binding.flDashboard.id, fragment)
        fragmentTransaction.commit()
        dashboard = fragment
    }

    private fun onClickSetting() {
        val dialog = CallDetailSettingDialog(this)
        dialog.setListener(object: CallDetailSettingDialog.CallDetailSettingItemListener {
            override fun onClickDashboard() {
                binding.flDashboard.visibility = View.VISIBLE
                binding.ivClose.visibility = View.VISIBLE
                dashboard?.updateVisible(true)
            }
        })
        dialog.show()
    }

    private fun onHangup() {
        CallServiceManager.instance.remoteUser?.let { userInfo ->
            CallServiceManager.instance.callApi?.hangup(userInfo.getRoomId()) {
            }
        }
        binding.llContainer.removeAllViews()
        binding.vDragWindow.canvasContainer.removeAllViews()
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
        finish()
    }

    override fun onCallStateChanged(
        state: CallStateType,
        stateReason: CallReason,
        eventReason: String,
        elapsed: Long,
        eventInfo: Map<String, Any>
    ) {
    }

    override fun onCallEventChanged(event: CallEvent, elapsed: Long) {
        when(event) {
            CallEvent.LocalLeave,
            CallEvent.RemoteLeave -> {
                onHangup()
            }
            else -> {}
        }
    }
}
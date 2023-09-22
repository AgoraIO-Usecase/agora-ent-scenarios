package io.agora.scene.pure1v1.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams
import android.widget.Toast
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

    override fun onResume() {
        super.onResume()
        Log.d(tag,"onResume")
    }

    override fun onStop() {
        super.onStop()
        Log.d(tag,"onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d(tag,"onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag,"onDestroy")
    }

    override fun onBackPressed() {
        Log.d(tag,"onBackPressed")
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
        binding.vDragWindow1.setSmallType(false)
        binding.vDragWindow2.setSmallType(true)
        CallServiceManager.instance.remoteCanvas?.let { canvas ->
            binding.vDragWindow1.canvasContainer.addView(canvas)
        }
        CallServiceManager.instance.remoteUser?.let { userInfo ->
            binding.vDragWindow1.setUserName(userInfo.userName)
        }
        CallServiceManager.instance.localCanvas?.let { canvas ->
            binding.vDragWindow2.canvasContainer.addView(canvas)
        }
        CallServiceManager.instance.localUser?.let { userInfo ->
            binding.vDragWindow2.setUserName(userInfo.userName)
        }
        binding.vDragWindow2.setOnViewClick {
            exchangeDragWindow()
        }
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
            binding.tvRoomNum.text = userInfo.userId
        }
        val fragment = DashboardFragment()
        val fragmentTransaction = supportFragmentManager.beginTransaction()
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
        } else {
            binding.vDragWindow1.bringToFront()
            binding.vDragWindow1.setSmallType(true)
            binding.vDragWindow1.setOnViewClick {
                exchangeDragWindow()
            }
            binding.vDragWindow2.setOnViewClick(null)
            binding.vDragWindow2.setSmallType(false)
        }
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
        binding.vDragWindow1.canvasContainer.removeAllViews()
        binding.vDragWindow2.canvasContainer.removeAllViews()
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
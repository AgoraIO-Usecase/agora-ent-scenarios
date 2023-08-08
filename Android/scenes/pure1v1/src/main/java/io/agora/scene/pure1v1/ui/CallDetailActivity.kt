package io.agora.scene.pure1v1.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.pure1v1.callAPI.CallEvent
import io.agora.scene.pure1v1.callAPI.CallReason
import io.agora.scene.pure1v1.callAPI.CallStateType
import io.agora.scene.pure1v1.callAPI.ICallApiListener
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailActivityBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import io.agora.scene.pure1v1.service.UserInfo
import java.util.concurrent.TimeUnit

class CallDetailActivity : AppCompatActivity(), ICallApiListener {

    private lateinit var binding: Pure1v1CallDetailActivityBinding

    private val startTime = System.currentTimeMillis()
    private val timerHandler = Handler(Looper.getMainLooper())
    private var dashboard: DashboardFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Pure1v1CallDetailActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        CallServiceManager.instance.callApi?.addListener(this)
        updateTime()
    }

    private fun updateTime() {
        val millis = System.currentTimeMillis() - startTime
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        binding.tvTime.text = formattedTime
        timerHandler.postDelayed({
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
        CallServiceManager.instance.localUser?.let { userInfo ->
            binding.vDragWindow.setUserName(userInfo.userName)
        }
        CallServiceManager.instance.localCanvas?.let { canvas ->
            binding.llContainer.addView(canvas)
        }
        CallServiceManager.instance.remoteCanvas?.let { canvas ->
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
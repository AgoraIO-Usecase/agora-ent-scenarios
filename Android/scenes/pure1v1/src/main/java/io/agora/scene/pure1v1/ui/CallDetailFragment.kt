package io.agora.scene.pure1v1.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.callAPI.CallEvent
import io.agora.scene.pure1v1.callAPI.CallReason
import io.agora.scene.pure1v1.callAPI.CallStateType
import io.agora.scene.pure1v1.callAPI.ICallApiListener
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailFragmentBinding
import io.agora.scene.pure1v1.service.CallServiceManager
import io.agora.scene.widget.dialog.TopFunctionDialog
import java.util.concurrent.TimeUnit

class CallDetailFragment : Fragment(), ICallApiListener {

    private lateinit var binding: Pure1v1CallDetailFragmentBinding

    private val tag = "CallDetailActivity_LOG"

    private val startTime = System.currentTimeMillis()
    private var timerHandler: Handler? = null
    private var dashboard: DashboardFragment? = null
    private var rtcEventHandler: IRtcEngineEventHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = Pure1v1CallDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        CallServiceManager.instance.callApi?.addListener(this)
        setupRTCListener()

        timerHandler = Handler(Looper.getMainLooper())
        updateTime()
    }

    private fun setupRTCListener() {
        val handler = object: IRtcEngineEventHandler() {
            override fun onContentInspectResult(result: Int) {
                Log.d(tag, "onContentInspectResult = $result")
                if (result > 1) {
                    Toast.makeText(context, getText(R.string.pure1v1_call_content_inspect), Toast.LENGTH_SHORT).show()
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
        CallServiceManager.instance.remoteUser?.let { userInfo ->
            Glide.with(this)
                .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                .into(binding.ivUserAvatar)
            binding.tvRoomTitle.text = userInfo.userName
            binding.tvRoomNum.text = userInfo.userId
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
        val dialog = CallDetailSettingDialog(context)
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
            CallServiceManager.instance.callApi?.hangup(userInfo.userId.toInt()) {
            }
        }
        binding.vDragWindow1.canvasContainer.removeAllViews()
        binding.vDragWindow2.canvasContainer.removeAllViews()
        timerHandler?.removeCallbacksAndMessages(null)
        timerHandler = null
        if (isAdded && !parentFragmentManager.isDestroyed) {
            val fragment = parentFragmentManager.findFragmentByTag("CallDetailFragment")
            fragment?.let {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.remove(it)
                transaction.commit()
            }
        }
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
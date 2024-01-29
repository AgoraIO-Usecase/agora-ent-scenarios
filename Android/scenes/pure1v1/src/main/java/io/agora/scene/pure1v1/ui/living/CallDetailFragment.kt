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
import io.agora.scene.pure1v1.databinding.Pure1v1CallDetailFragmentBinding
import io.agora.scene.pure1v1.CallServiceManager
import io.agora.scene.pure1v1.Pure1v1Logger
import io.agora.scene.pure1v1.callapi.*
import io.agora.scene.widget.dialog.TopFunctionDialog
import java.util.concurrent.TimeUnit

/*
 * 1v1 互动中页面
 */
class CallDetailFragment : Fragment(), ICallApiListener {

    private lateinit var binding: Pure1v1CallDetailFragmentBinding

    private val tag = "CallDetailActivity_LOG"

    private var startTime = System.currentTimeMillis()
    private var timerHandler: Handler? = null
    private var dashboard: DashboardFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = Pure1v1CallDetailFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Pure1v1Logger.d(tag, "local pic debug onHiddenChanged: $hidden")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        CallServiceManager.instance.callApi?.addListener(this)

        timerHandler = Handler(Looper.getMainLooper())

        CallServiceManager.instance.prepareForCall {  }
    }

    fun start() {
        startTime = System.currentTimeMillis()

        view?.post {
            CallServiceManager.instance.remoteUser?.let { userInfo ->
                Glide.with(this)
                    .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                    .into(binding.ivUserAvatar)
                binding.tvRoomTitle.text = userInfo.userName
                binding.tvRoomNum.text = userInfo.userId
            }
            CallServiceManager.instance.remoteUser?.let { userInfo ->
                binding.vDragWindow1.setUserName(userInfo.userName)
            }
            CallServiceManager.instance.localUser?.let { userInfo ->
                binding.vDragWindow2.setUserName(userInfo.userName)
            }
        }
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

    private fun setupView() {
        // 将试图容器设置进manager
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
            CallServiceManager.instance.callApi?.hangup(userInfo.userId.toInt(), reason = "hangup by user") {
            }
        }
        timerHandler?.removeCallbacksAndMessages(null)
        if (isAdded && !parentFragmentManager.isDestroyed) {
            val fragment = parentFragmentManager.findFragmentByTag("CallDetailFragment")
            fragment?.let {
                val transaction = parentFragmentManager.beginTransaction()
                transaction.hide(it)
                transaction.commit()
            }
        }
    }

    override fun onCallStateChanged(
        state: CallStateType,
        stateReason: CallStateReason,
        eventReason: String,
        eventInfo: Map<String, Any>
    ) {
    }

    override fun onCallEventChanged(event: CallEvent) {
        when(event) {
            CallEvent.RemoteLeave -> {
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
        Pure1v1Logger.d(tag, "onCallError: errorEvent$errorEvent, errorType:$errorType, errorCode:$errorCode, message:$message")
    }
}
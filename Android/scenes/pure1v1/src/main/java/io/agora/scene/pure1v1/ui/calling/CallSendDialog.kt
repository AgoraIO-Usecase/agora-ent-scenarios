package io.agora.scene.pure1v1.ui.calling

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.pure1v1.CallServiceManager
import io.agora.scene.pure1v1.databinding.Pure1v1CallSendDialogBinding
import io.agora.scene.pure1v1.service.UserInfo
import io.agora.scene.pure1v1.ui.base.CallDialogState
import io.agora.scene.pure1v1.ui.base.DebouncedOnClickListener

/*
 * 1v1 拨打页面
 */
class CallSendDialog(
    private val context: Context
) : Fragment() {

    interface CallSendDialogListener {
        // 点击了挂断的回调
        fun onSendViewDidClickHangup()
    }

    private lateinit var binding: Pure1v1CallSendDialogBinding

    private var listener: CallSendDialogListener? = null

    private var callState = CallDialogState.None

    private val showView = SurfaceView(context)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = Pure1v1CallSendDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivHangup.setOnClickListener(DebouncedOnClickListener {
            listener?.onSendViewDidClickHangup()
            hangUp()
        })
//        val showView = SurfaceView(context)
    }

    fun initView(userInfo: UserInfo) {
        binding.root.post {
            CallServiceManager.instance.renderCallShow(showView)
            binding.tvShow.removeAllViews()
            binding.tvShow.addView(showView)

            binding.tvUserName.text = userInfo.userName
            Glide.with(context)
                .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
                .into(binding.ivUserAvatar)

            updateCallState(CallDialogState.Calling)
        }
    }

    fun setListener(l: CallSendDialogListener) {
        listener = l
    }

    fun updateCallState(state: CallDialogState) {
        if (callState != state) {
            callState = state
            textAnimation()
        }
    }

    fun hangUp() {
        if (isAdded && !parentFragmentManager.isDestroyed) {
            callState = CallDialogState.None
            val fragment = parentFragmentManager.findFragmentByTag("CallSendFragment")
            fragment?.let {
                binding.tvShow.removeAllViews()
                val transaction = parentFragmentManager.beginTransaction()
                transaction.hide(it)
                transaction.commit()
            }
        }
    }

    private var textCount: Int = 0
    private fun textAnimation() {
        if (callState == CallDialogState.Calling) {
            Log.d("hugo", "textAnimation: ${binding.tvDot.text}")
            when (textCount) {
                0 -> {binding.tvDot.text = ""}
                1 -> {binding.tvDot.text = "."}
                2 -> {binding.tvDot.text = ".."}
                3 -> {binding.tvDot.text = "..."}
                else -> {}
            }
            Handler().postDelayed({
                textAnimation()
            }, 500)
            if (textCount >= 3) {
                textCount = 0
            } else {
                textCount += 1
            }
        } else {
            textCount = 0
            binding.tvDot.text = ""
        }
    }
}
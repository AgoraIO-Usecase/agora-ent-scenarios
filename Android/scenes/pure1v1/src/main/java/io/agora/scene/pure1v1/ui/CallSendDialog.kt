package io.agora.scene.pure1v1.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.pure1v1.CallServiceManager
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1CallSendDialogBinding
import io.agora.scene.pure1v1.service.UserInfo

class CallSendDialog(
    private val context: Context,
    private val userInfo: UserInfo
) : Fragment() {

    interface CallSendDialogListener {
        // 点击了挂断的回调
        fun onSendViewDidClickHangup()
    }

    private lateinit var binding: Pure1v1CallSendDialogBinding

    private var listener: CallSendDialogListener? = null

    private var callState = CallDialogState.None

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = Pure1v1CallSendDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivHangup.setOnClickListener(DebouncedOnClickListener {
            onClickHangup()
        })
        binding.tvUserName.text = userInfo.userName
        Glide.with(context)
            .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)

        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)


        val showView = SurfaceView(context)
        binding.tvShow.removeAllViews()
        binding.tvShow.addView(showView)
        CallServiceManager.instance.renderCallShow(showView)
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

    private fun onClickHangup() {
        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_to_bottom)
        anim.setAnimationListener(object: AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                listener?.onSendViewDidClickHangup()
                hangUp()
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        binding.clContent.startAnimation(anim)
    }
}
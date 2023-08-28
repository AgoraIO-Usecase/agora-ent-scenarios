package io.agora.scene.showTo1v1.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.showTo1v1.R
import io.agora.scene.showTo1v1.databinding.ShowTo1v1CallSendDialogBinding
import io.agora.scene.showTo1v1.service.UserInfo

class CallSendDialog constructor(
    private val context: Context,
    private val userInfo: UserInfo
) : CallDialog(context, userInfo) {

    interface CallSendDialogListener {
        // 点击了挂断的回调
        fun onSendViewDidClickHangup()
    }

    private val binding = ShowTo1v1CallSendDialogBinding.inflate(LayoutInflater.from(context))

    private var listener: CallSendDialogListener? = null

    private var callState = CallDialogState.None

    init {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.ivHangup.setOnClickListener {
            onClickHangup()
        }
        binding.tvUserName.text = userInfo.userName
        Glide.with(context)
            .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)

        val anim = AnimationUtils.loadAnimation(context, R.anim.show_to1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)
    }

    fun setListener(l: CallSendDialogListener) {
        listener = l
    }

    override fun updateCallState(state: CallDialogState) {
        if (callState != state) {
            callState = state
        }
    }

    private fun onClickHangup() {
        val anim = AnimationUtils.loadAnimation(context, R.anim.show_to1v1_slide_to_bottom)
        anim.setAnimationListener(object: AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                listener?.onSendViewDidClickHangup()
                dismiss()
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        binding.clContent.startAnimation(anim)
    }
}
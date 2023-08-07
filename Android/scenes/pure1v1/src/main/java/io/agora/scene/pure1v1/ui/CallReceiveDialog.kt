package io.agora.scene.pure1v1.ui

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.pure1v1.R
import io.agora.scene.pure1v1.databinding.Pure1v1CallReceiveDialogBinding
import io.agora.scene.pure1v1.service.UserInfo

class CallReceiveDialog(
    private val context: Context,
    private val userInfo: UserInfo
) : CallDialog(context, userInfo) {

    interface CallReceiveDialogListener {
        // 点击了接受的回调
        fun onReceiveViewDidClickAccept()
        // 点击了拒绝的回调
        fun onReceiveViewDidClickReject()
    }

    private val binding: Pure1v1CallReceiveDialogBinding = Pure1v1CallReceiveDialogBinding.inflate(LayoutInflater.from(context))

    private var listener: CallReceiveDialogListener? = null

    private var callState = CallDialogState.None

    init {
        setContentView(binding.root)
    }

    override fun dismiss() {
        binding.ivCircle1.clearAnimation()
        binding.ivCircle2.clearAnimation()
        binding.ivUserAvatar.clearAnimation()
        super.dismiss()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.ivAccept.setOnClickListener {
            listener?.onReceiveViewDidClickAccept()
            dismiss()
        }
        binding.ivReject.setOnClickListener {
            onClickReject()
        }
        binding.tvUserName.text = userInfo.userName
        Glide.with(context)
            .load(userInfo.avatar).apply(RequestOptions.circleCropTransform())
            .into(binding.ivUserAvatar)

        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_from_bottom)
        binding.clContent.startAnimation(anim)
        setupAvatarAnimation()
        updateCallState(CallDialogState.Calling)
    }

    fun setListener(l: CallReceiveDialogListener) {
        listener = l
    }

    override fun updateCallState(state: CallDialogState) {
        if (callState != state) {
            callState = state
            textAnimation()
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

    private fun onClickReject() {
        val anim = AnimationUtils.loadAnimation(context, R.anim.pure1v1_slide_to_bottom)
        anim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }
            override fun onAnimationEnd(animation: Animation?) {
                listener?.onReceiveViewDidClickReject()
                dismiss()
            }
            override fun onAnimationRepeat(animation: Animation?) {
            }
        })
        binding.clContent.startAnimation(anim)
    }

    private fun setupAvatarAnimation() {
        // 创建两个呼吸动画
        val alphaAnima = AlphaAnimation(0f, 1.0f)
        alphaAnima.duration = 2000
        alphaAnima.interpolator = LinearInterpolator()
        alphaAnima.repeatCount = Animation.INFINITE
        alphaAnima.repeatMode = Animation.REVERSE
        binding.ivCircle1.startAnimation(alphaAnima)
        binding.ivCircle2.startAnimation(alphaAnima)

        val scaleAnima = ScaleAnimation(
            0.8f,  // 起始X缩放比例
            0.9f,  // 结束X缩放比例
            0.8f,  // 起始Y缩放比例
            0.9f,  // 结束Y缩放比例
            Animation.RELATIVE_TO_SELF,  // X轴相对于自身
            0.5f,  // X轴缩放的中心点位置，这里是相对于自身的中心位置
            Animation.RELATIVE_TO_SELF,  // Y轴相对于自身
            0.5f   // Y轴缩放的中心点位置，这里是相对于自身的中心位置
        )
        scaleAnima.duration = 2000
        scaleAnima.repeatCount = Animation.INFINITE
        scaleAnima.repeatMode = Animation.REVERSE
        binding.ivUserAvatar.startAnimation(scaleAnima)
    }

}
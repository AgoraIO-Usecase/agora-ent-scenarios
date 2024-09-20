package io.agora.scene.aichat.chat

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.databinding.AichatFragmentVoiceCallBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AiChatVoiceCallFragment : BaseViewBindingFragment<AichatFragmentVoiceCallBinding>() {

    companion object {

        private val TAG = AiChatVoiceCallFragment::class.java.simpleName
    }

    private val mAIChatViewModel: AIChatViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiChatActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIChatViewModel(conversationId) as T
            }
        }
    }

//    private val mAIChatViewModel: AIChatViewModel by activityViewModels()

    private var hideLayoutTips: Job? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentVoiceCallBinding {
        return AichatFragmentVoiceCallBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()

        Glide.with(this)
            .load(mAIChatViewModel.getAgentBgUrlByAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存原始图片和变换后的图片
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // 使用 Drawable，例如设置到 ImageView
                    binding.rootView.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 清理资源或处理占位符
                }
            })

        binding.tvAgentName.text = mAIChatViewModel.getChatName()
        binding.ivAgentAvatar.loadCircleImage(mAIChatViewModel.getChatAvatar())

        binding.cbMicMute.isChecked = mAIChatViewModel.mMicOn
        binding.cbVoiceInterruption.isChecked = mAIChatViewModel.mFlushAllowed

        showInterruptTipWithAnimation(binding.layoutInterruptTips)
    }

    private fun showInterruptTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = 0f

        layout.visibility = View.VISIBLE
        // 放大动画
        ObjectAnimator.ofFloat(layout, "scaleX", 0f, 1f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 0f, 1f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 取消之前的协程任务
        hideLayoutTips?.cancel()

        hideLayoutTips = CoroutineScope(Dispatchers.Main).launch {
            delay(10000)
            hideInterruptWithAnimation(layout) // 隐藏并缩小 TextView
        }
    }

    private fun hideInterruptWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = 0f
        // 缩小动画
        ObjectAnimator.ofFloat(layout, "scaleX", 1f, 0f).apply {
            duration = 300 // 动画时长
            interpolator = DecelerateInterpolator()
            start()
        }
        ObjectAnimator.ofFloat(layout, "scaleY", 1f, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            start()
        }

        // 动画结束后隐藏 TextView
        CoroutineScope(Dispatchers.Main).launch {
            delay(300) // 等待动画结束
            layout.visibility = View.GONE
        }
    }


    override fun initListener() {
        super.initListener()
        binding.cbVoiceInterruption.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            mAIChatViewModel.updateInterruptConfig(ischecked)
        }
        binding.cbMicMute.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            if (activity is AiChatActivity) {
                (activity as AiChatActivity).toggleSelfAudio(ischecked, callback = {
                    mAIChatViewModel.micMute(!ischecked)
                })
            }
        }
        binding.btnVoiceCallInterrupt.setOnClickListener {
            mAIChatViewModel.interruptionVoiceCall()
        }
        binding.btnVoiceCallHangup.setOnClickListener {
            mAIChatViewModel.voiceCallHangup()
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        hideLayoutTips?.cancel()
        super.onDestroyView()
    }
}
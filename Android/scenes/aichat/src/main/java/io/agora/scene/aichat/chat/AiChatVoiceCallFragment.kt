package io.agora.scene.aichat.chat

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.logic.AIChatViewModel
import io.agora.scene.aichat.databinding.AichatFragmentVoiceCallBinding
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.base.component.AgoraApplication
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

    // 隐藏打断按钮 tips
    private var mHideInterruptTips: Job? = null

    // 语音通话动画
    private var mAudioDrawable: APNGDrawable? = null

    private var mIsAudioAnimate = false

    // 智能体回答中动画
    private var mAgentDrawable: APNGDrawable? = null

    private var mIsAgentAnimate = false

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentVoiceCallBinding {
        return AichatFragmentVoiceCallBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAIChatViewModel.voiceCallStart()
    }

    override fun initView() {
        super.initView()

        Glide.with(this)
            .load(mAIChatViewModel.getAgentBgUrlByAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存原始图片和变换后的图片
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    // 使用 Drawable，例如设置到 ImageView
                    binding.ivVoiceCallBg.setImageDrawable(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 清理资源或处理占位符
                }
            })

        binding.tvAgentName.text = mAIChatViewModel.getChatName()
        binding.ivAgentAvatar.loadCircleImage(mAIChatViewModel.getChatAvatar())

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
        mHideInterruptTips?.cancel()

        mHideInterruptTips = CoroutineScope(Dispatchers.Main).launch {
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

    private fun startAudioAnimate() {
        if (mIsAudioAnimate) return
        if (mAudioDrawable == null) {
            mAudioDrawable =
                APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_audio_with_sound.png")
        } else {
            mAudioDrawable?.resume()
        }
        mAudioDrawable?.setLoopLimit(-1)
        binding.ivAudioNoSound.visibility = View.INVISIBLE
        binding.ivAudioSound.visibility = View.VISIBLE
        binding.ivAudioSound.setImageDrawable(mAudioDrawable)
        mIsAudioAnimate = true
        // 设计要求 在我讲话时，应为置灰状态，因为没必要打断自己讲话
        binding.btnVoiceCallInterrupt.isEnabled = false
    }

    private fun stopAudioAnimate() {
        if (!mIsAudioAnimate) return
        mAudioDrawable?.pause()
        binding.ivAudioSound.visibility = View.GONE
        binding.ivAudioNoSound.visibility = View.VISIBLE
        mIsAudioAnimate = false
        binding.btnVoiceCallInterrupt.isEnabled = true
    }

    private fun startAgentAnimate() {
        if (mIsAgentAnimate) return
        if (mAgentDrawable == null) {
            mAgentDrawable =
                APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_agent_call_wave.png")
        } else {
            mAgentDrawable?.resume()
        }
        mAgentDrawable?.setLoopLimit(-1)
        binding.ivAgentWave.visibility = View.VISIBLE
        binding.ivAgentWave.setImageDrawable(mAgentDrawable)
        mIsAgentAnimate = true
    }

    private fun stopAgentAnimate() {
        if (!mIsAgentAnimate) return
        mAgentDrawable?.pause()
        binding.ivAgentWave.visibility = View.GONE
        mIsAgentAnimate = false
    }

    override fun initListener() {
        super.initListener()
        // 语音打断开关
        // checked = true 代表麦允许语音打断，checked = false 代表不允许语音打断
        binding.cbVoiceInterruption.isChecked = mAIChatViewModel.mFlushAllowed
        binding.cbVoiceInterruption.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            mAIChatViewModel.updateInterruptConfig(ischecked)
        }
        // 麦克风开关
        // checked = true 代表麦克风打开，checked = false 代表麦克风关闭
        binding.cbMicUnMute.isChecked = mAIChatViewModel.mMicOn
        binding.cbMicUnMute.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            if (activity is AiChatActivity) {
                (activity as AiChatActivity).toggleSelfAudio(ischecked, callback = {
                    mAIChatViewModel.micUnMute(ischecked)
                })
            }
        }

        // 点击打断按钮
        binding.btnVoiceCallInterrupt.setOnClickListener {
            mAIChatViewModel.interruptionVoiceCall()
        }
        // 点击挂断按钮
        binding.btnVoiceCallHangup.setOnClickListener {
            mAIChatViewModel.voiceCallHangup()
            findNavController().popBackStack()
        }

        mAIChatViewModel.startVoiceCallAgentLivedata.observe(viewLifecycleOwner) {
            if (it) {
                // startAudioAnimate()
            }
        }

        mAIChatViewModel.stopVoiceCallAgentLivedata.observe(viewLifecycleOwner) {
            if (it) {
                mIsAudioAnimate = false
            }
        }

        mAIChatViewModel.openInterruptCallAgentLivedata.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvAudioSoundTips.isVisible = true
            } else {
                binding.cbVoiceInterruption.isChecked = false
            }
        }
        mAIChatViewModel.closeInterruptCallAgentLivedata.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvAudioSoundTips.isVisible = false
            } else {
                binding.cbVoiceInterruption.isChecked = true
            }
        }
        mAIChatViewModel.localVolumeLivedata.observe(viewLifecycleOwner) {
            if (it > 30) {
                startAudioAnimate()
            } else if (it < 20) {
                stopAudioAnimate()
            }
        }
        mAIChatViewModel.remoteVolumeLivedata.observe(viewLifecycleOwner) {
            if (it > 30) {
                startAgentAnimate()
            } else if (it < 20) {
                stopAgentAnimate()
            }
        }
    }

    override fun onDestroyView() {
        mHideInterruptTips?.cancel()
        super.onDestroyView()
    }
}
package io.agora.scene.aichat.chat

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.aichat.R
import io.agora.scene.aichat.chat.logic.AIVoiceCallViewModel
import io.agora.scene.aichat.databinding.AichatFragmentVoiceCallBinding
import io.agora.scene.aichat.ext.copyTextToClipboard
import io.agora.scene.aichat.ext.loadCircleImage
import io.agora.scene.aichat.ext.vibrate
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseViewBindingFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AiChatVoiceCallFragment : BaseViewBindingFragment<AichatFragmentVoiceCallBinding>() {

    companion object {

        private val TAG = AiChatVoiceCallFragment::class.java.simpleName
    }

    private val mAIChatViewModel: AIVoiceCallViewModel by activityViewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(aClass: Class<T>): T {
                val conversationId = arguments?.getString(AiVoiceCallActivity.EXTRA_CONVERSATION_ID) ?: ""
                return AIVoiceCallViewModel(conversationId) as T
            }
        }
    }

    // 隐藏打断按钮 tips
    private var mHideInterruptTips: Job? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): AichatFragmentVoiceCallBinding {
        return AichatFragmentVoiceCallBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        mAIChatViewModel.initCurrentRoom()

        mAIChatViewModel.currentRoomLiveData.observe(viewLifecycleOwner) { currentUser ->
            if (currentUser != null) {
                mAIChatViewModel.voiceCallStart()
            } else {
                activity?.finish()
            }
        }
    }

    private fun showInterruptTipWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = 0f

        layout.visibility = View.VISIBLE

        playScaleAnimation(layout, 0f, 1f)

        // 取消之前的协程任务
        mHideInterruptTips?.cancel()

        mHideInterruptTips = viewLifecycleOwner.lifecycleScope.launch {
            delay(10000)
            if (isActive) {
                hideInterruptWithAnimation(layout) // 隐藏并缩小 TextView
            }
        }
    }

    private fun hideInterruptWithAnimation(layout: FrameLayout) {
        layout.pivotX = layout.width / 3f * 2
        layout.pivotY = 0f

        playScaleAnimation(layout, 1f, 0f)

        // 动画结束后隐藏 TextView
        viewLifecycleOwner.lifecycleScope.launch {
            delay(300) // 等待动画结束
            if (isActive && layout.isShown) {
                layout.visibility = View.GONE
            }
        }
    }

    private fun playScaleAnimation(layout: FrameLayout, startScale: Float, endScale: Float) {
        val scaleXAnimator = ObjectAnimator.ofFloat(layout, "scaleX", startScale, endScale).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        val scaleYAnimator = ObjectAnimator.ofFloat(layout, "scaleY", startScale, endScale).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
        }

        // 并行播放 scaleX 和 scaleY 动画
        AnimatorSet().apply {
            playTogether(scaleXAnimator, scaleYAnimator)
            start()
        }
    }


    // 语音通话动画
    private var mStopUserAudioAnimateJob: Job? = null
    private var mUserAudioDrawable: APNGDrawable? = null
    private var mIsUserTalking = false
    private var mIsUserAudioAnimate = false
    private fun startUserAudioAnimate() {
        mIsUserTalking = true
        if (mIsUserAudioAnimate) return
        if (mUserAudioDrawable == null) {
            mUserAudioDrawable =
                APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_audio_with_sound.png")
        } else {
            mUserAudioDrawable?.resume()
        }
        mUserAudioDrawable?.setLoopLimit(-1)
        binding.ivAudioNoSound.visibility = View.INVISIBLE
        binding.ivAudioSound.visibility = View.VISIBLE
        binding.ivAudioSound.setImageDrawable(mUserAudioDrawable)
        mIsUserAudioAnimate = true
        mIsUserTalking = true
    }

    // 延迟停止动画
    private fun stopUserAudioAnimate(force: Boolean = false) {
        if (!mIsUserAudioAnimate) return
        mIsUserTalking = false
        // 取消之前的协程任务
        mStopUserAudioAnimateJob?.cancel()
        // 如果 0.5 秒内没有重新开始说话，则停止动画
        mStopUserAudioAnimateJob = viewLifecycleOwner.lifecycleScope.launch {
            if (!force) {
                delay(500)
            }
            // 如果在延迟期间用户没有重新开始说话
            if (isActive) {
                if (!mIsUserTalking && mIsUserAudioAnimate) {
                    mUserAudioDrawable?.pause()
                    binding.ivAudioSound.visibility = View.GONE
                    binding.ivAudioNoSound.visibility = View.VISIBLE
                    mIsUserAudioAnimate = false
                }
            }
        }
    }


    // 智能体回答中动画
    private var mAgentDAudioDrawable: APNGDrawable? = null
    private var mIsAgentAudioAnimate = false
    private var mIsAgentTalking = false
    private var mStopAgentAudioAnimateJob: Job? = null
    private fun startAgentAudioAnimate() {
        mIsAgentTalking = true
        if (mIsAgentAudioAnimate) return
        if (mAgentDAudioDrawable == null) {
            mAgentDAudioDrawable =
                APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_agent_call_wave.png")
        } else {
            mAgentDAudioDrawable?.resume()
        }
        mAgentDAudioDrawable?.setLoopLimit(-1)
        binding.ivAgentWave.visibility = View.VISIBLE
        binding.ivAgentWave.setImageDrawable(mAgentDAudioDrawable)
        mIsAgentAudioAnimate = true
        mIsAgentTalking = true
        // 智能体回答时候可以打断
        binding.btnVoiceCallInterrupt.setBackgroundResource(R.drawable.aichat_icon_calling_interrupt_selector)
        binding.btnVoiceCallInterrupt.isEnabled = true
        binding.btnVoiceCallInterrupt.isActivated = true
    }

    // 延迟停止动画
    private fun stopAgentAudioAnimate() {
        if (!mIsAgentAudioAnimate) return
        mIsAgentTalking = false
        // 取消任何延迟停止的任务
        mStopAgentAudioAnimateJob?.cancel()
        // 如果 0.5 秒内没有重新开始说话，则停止动画
        mStopUserAudioAnimateJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(500)
            // 如果在延迟期间用户没有重新开始说话
            if (isActive) {
                if (!mIsAgentTalking && mIsAgentAudioAnimate) {
                    mAgentDAudioDrawable?.pause()
                    binding.ivAgentWave.visibility = View.GONE
                    mIsAgentAudioAnimate = false
                    // 智能体不回答时候候置灰
                    binding.btnVoiceCallInterrupt.setBackgroundResource(R.drawable.aichat_icon_calling_interrupt_banned)
                    binding.btnVoiceCallInterrupt.isEnabled = false
                    binding.btnVoiceCallInterrupt.isActivated = false
                }
            }
        }
    }

    private fun clearAllAnimate() {
        mHideInterruptTips?.let {
            it.cancel()
            mHideInterruptTips = null
        }
        mStopUserAudioAnimateJob?.let {
            it.cancel()
            mStopUserAudioAnimateJob = null
        }
        mIsUserTalking = false
        mIsUserAudioAnimate = false
        mStopAgentAudioAnimateJob?.let {
            it.cancel()
            mStopAgentAudioAnimateJob = null
        }
        mIsAgentTalking = false
        mIsAgentAudioAnimate = false
    }

    override fun initListener() {
        super.initListener()

        // 语音打断开关
        // checked = true 代表麦允许语音打断，checked = false 代表不允许语音打断
        binding.cbVoiceInterruption.isChecked = mAIChatViewModel.mFlushAllowed
        binding.cbVoiceInterruption.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            // 防止重复点击
            binding.cbVoiceInterruption.isEnabled = false
            mAIChatViewModel.updateInterruptConfig(ischecked)
        }
        // 麦克风开关
        // checked = true 代表麦克风打开，checked = false 代表麦克风关闭
        binding.cbMicUnMute.isChecked = mAIChatViewModel.mMicOn
        binding.cbMicUnMute.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            context?.vibrate()
            if (activity is AiVoiceCallActivity) {
                (activity as AiVoiceCallActivity).toggleSelfAudio(ischecked, callback = {
                    mAIChatViewModel.micUnMute(ischecked)
                    if (!ischecked) {
                        stopUserAudioAnimate(true)
                    }
                })
            }
        }

        // 点击打断按钮
        binding.btnVoiceCallInterrupt.setOnClickListener {
            context?.vibrate()
            mAIChatViewModel.interruptionVoiceCall()
        }
        // 点击挂断按钮
        binding.btnVoiceCallHangup.setOnClickListener {
            context?.vibrate()
            mAIChatViewModel.voiceCallHangup()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(300)
                clearAllAnimate()
                activity?.finish()
            }
        }

        mAIChatViewModel.startVoiceCallAgentLivedata.observe(viewLifecycleOwner) {
            it?.taskId?.let { taskId ->
                context?.copyTextToClipboard("voice chat taskId:$taskId")
            }
        }

        mAIChatViewModel.openInterruptCallAgentLivedata.observe(viewLifecycleOwner) {
            binding.cbVoiceInterruption.isEnabled = true
            it?:return@observe
            if (it) {
                binding.tvAudioSoundTips.isVisible = true
            } else {
                binding.cbVoiceInterruption.isChecked = false
            }
        }
        mAIChatViewModel.closeInterruptCallAgentLivedata.observe(viewLifecycleOwner) {
            binding.cbVoiceInterruption.isEnabled = true
            it?:return@observe
            if (it) {
                binding.tvAudioSoundTips.isVisible = false
            } else {
                binding.cbVoiceInterruption.isChecked = true
            }
        }
        mAIChatViewModel.localVolumeLivedata.observe(viewLifecycleOwner) {
            it?:return@observe
            if (it > 50) {
                startUserAudioAnimate()
            } else if (it < 30) {
                stopUserAudioAnimate()
            }
        }
        mAIChatViewModel.remoteVolumeLivedata.observe(viewLifecycleOwner) {
            it?:return@observe
            if (it > 50) {
                startAgentAudioAnimate()
            } else if (it < 30) {
                stopAgentAudioAnimate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 拦截返回键，屏蔽默认返回行为
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // 屏蔽返回按钮的功能，什么也不做
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
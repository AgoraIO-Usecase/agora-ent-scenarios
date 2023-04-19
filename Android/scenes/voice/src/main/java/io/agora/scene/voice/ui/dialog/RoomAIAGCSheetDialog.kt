package io.agora.scene.voice.ui.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiagcBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import java.util.*

class RoomAIAGCSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiagcBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val beforeSoundId = 201001
    private val afterSoundId = 201002

    private val isOn by lazy {
        arguments?.getBoolean(RoomAIAECSheetDialog.KEY_IS_ON, true) ?: true
    }

    var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    private var beforeDrawable: APNGDrawable? = null
    private var beforeTimer: Timer? = null

    private var afterDrawable: APNGDrawable? = null
    private var afterTimer: Timer? = null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiagcBinding? {
        return VoiceDialogChatroomAiagcBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        resetTimer()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        setupOnClickPlayButton()
        beforeDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_agc_sample_before.png")
        beforeDrawable?.registerAnimationCallback(object: Animatable2Compat.AnimationCallback(){
            var firstStart = true
            override fun onAnimationStart(drawable: Drawable?) {
                super.onAnimationStart(drawable)
                if(firstStart){
                    beforeDrawable?.pause()
                    firstStart = false
                }
            }
        })
        binding?.ivBefore?.setImageDrawable(beforeDrawable)

        afterDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_agc_sample_after.png")
        afterDrawable?.registerAnimationCallback(object: Animatable2Compat.AnimationCallback(){
            var firstStart = true
            override fun onAnimationStart(drawable: Drawable?) {
                super.onAnimationStart(drawable)
                if(firstStart){
                    afterDrawable?.pause()
                    firstStart = false
                }
            }
        })
        binding?.ivAfter?.setImageDrawable(afterDrawable)

        binding?.accbAGC?.isChecked = isOn
        binding?.accbAGC?.setOnCheckedChangeListener { _, b ->
            onClickCheckBox?.invoke(b)
        }
    }

    fun resetTimer() {
        if (beforeTimer != null) {
            beforeTimer?.cancel()
            beforeTimer = null
        }
        if (afterTimer != null) {
            afterTimer?.cancel()
            afterTimer = null
        }
    }

    fun setupOnClickPlayButton() {
        binding?.btnBefore?.setOnClickListener {
            resetTimer()
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "sounds/voice_sample_agc_before.WAV"
                io.agora.scene.base.utils.FileUtils.copyFileFromAssets(this.context!!, file, context?.externalCacheDir!!.absolutePath).apply {
                    AgoraRtcEngineController.get().playMusic(beforeSoundId, this, 0)
                }
                val timer = Timer()
                beforeTimer = timer
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        if (beforeDrawable?.isRunning == true) {
                            beforeDrawable?.stop()
                            binding?.btnBefore?.isSelected = false
                        }
                    }
                }, 15 * 1000)
                it.isSelected = true
                binding?.btnAfter?.isSelected = false
                beforeDrawable?.start()
                beforeDrawable?.resume()
                afterDrawable?.stop()
            }
        }
        binding?.btnAfter?.setOnClickListener {
            resetTimer()
            if (it.isSelected) { // stop play
                AgoraRtcEngineController.get().resetMediaPlayer()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "sounds/voice_sample_agc_after.WAV"
                io.agora.scene.base.utils.FileUtils.copyFileFromAssets(this.context!!, file, context?.externalCacheDir!!.absolutePath).apply {
                    AgoraRtcEngineController.get().playMusic(afterSoundId, this, 0)
                }
                val timer = Timer()
                afterTimer = timer
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        if (afterDrawable?.isRunning == true) {
                            afterDrawable?.stop()
                            binding?.btnAfter?.isSelected = false
                        }
                    }
                }, 15 * 1000)
                it.isSelected = true
                binding?.btnBefore?.isSelected = false
                afterDrawable?.start()
                afterDrawable?.resume()
                beforeDrawable?.stop()
            }
        }
    }
}

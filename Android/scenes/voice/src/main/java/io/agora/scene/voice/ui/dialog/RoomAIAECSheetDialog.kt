package io.agora.scene.voice.ui.dialog

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomAiaecBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.listener.MediaPlayerObserver
import io.agora.voice.common.ui.dialog.BaseSheetDialog
import java.util.*

class RoomAIAECSheetDialog: BaseSheetDialog<VoiceDialogChatroomAiaecBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val isOn by lazy {
        arguments?.getBoolean(KEY_IS_ON, true) ?: true
    }

    var onClickCheckBox: ((isOn: Boolean) -> Unit)? = null

    private var beforeDrawable: APNGDrawable? = null
    private var beforeTimer: Timer? = null

    private var afterDrawable: APNGDrawable? = null
    private var afterTimer: Timer? = null

    private val mediaPlayer: IMediaPlayer? by lazy {
        AgoraRtcEngineController.get().createLocalMediaPlayer()?.apply {
            this.registerPlayerObserver(mediaPlayerObserver)
        }
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomAiaecBinding? {
        return VoiceDialogChatroomAiaecBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        resetTimer()
        mediaPlayer?.stop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation

        setupOnClickPlayButton()
        beforeDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_aec_sample_before.png")
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

        afterDrawable = APNGDrawable.fromAsset(activity?.applicationContext, "voice_aec_sample_after.png")
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

        binding?.accbAEC?.isChecked = isOn
        binding?.accbAEC?.setOnCheckedChangeListener { _, isChecked ->
            onClickCheckBox?.invoke(isChecked)
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
                mediaPlayer?.stop()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/voice_sample_aec_before.m4a"
                mediaPlayer?.stop()
                mediaPlayer?.open(file, 0)
                val timer = Timer()
                beforeTimer = timer
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        if (beforeDrawable?.isRunning == true) {
                            beforeDrawable?.stop()
                            binding?.btnBefore?.isSelected = false
                        }
                    }
                }, 9500)
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
                mediaPlayer?.stop()
                beforeDrawable?.stop()
                afterDrawable?.stop()
                it.isSelected = false
            } else { // start play
                val file = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/ent/music/voice_sample_aec_after.m4a"
                mediaPlayer?.stop()
                mediaPlayer?.open(file, 0)
                val timer = Timer()
                afterTimer = timer
                timer.schedule(object: TimerTask() {
                    override fun run() {
                        if (afterDrawable?.isRunning == true) {
                            afterDrawable?.stop()
                            binding?.btnAfter?.isSelected = false
                        }
                    }
                }, 9500)
                it.isSelected = true
                binding?.btnBefore?.isSelected = false
                afterDrawable?.start()
                afterDrawable?.resume()
                beforeDrawable?.stop()
            }
        }
    }

    private val mediaPlayerObserver = object : MediaPlayerObserver() {
        override fun onPlayerStateChanged(state: Constants.MediaPlayerState?, error: Constants.MediaPlayerError?) {
            when (state) {
                Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED -> {
                    mediaPlayer?.play()
                }
                Constants.MediaPlayerState.PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED -> {
                }
                Constants.MediaPlayerState.PLAYER_STATE_PLAYING -> {
                }
                else -> {}
            }
        }

        override fun onPositionChanged(position_ms: Long, timestamp_ms: Long) {
        }
    }
}

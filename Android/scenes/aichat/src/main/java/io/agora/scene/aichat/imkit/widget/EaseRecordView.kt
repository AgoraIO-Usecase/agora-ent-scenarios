package io.agora.scene.aichat.imkit.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.github.penfeizhou.animation.apng.APNGDrawable
import io.agora.scene.aichat.R
import io.agora.scene.aichat.databinding.EaseWidgetRecordBinding
import io.agora.scene.base.component.AgoraApplication

interface EaseRecordViewListener {

    /**
     * On start recording action
     *
     */
    fun onStartRecordingAction()

    /**
     * On send recording action
     *
     */
    fun onSendRecordingAction()

    /**
     * On cancel recording action
     *
     */
    fun onCancelRecordingAction()
}

class EaseRecordView @kotlin.jvm.JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var recordDrawable: APNGDrawable? = null

    private var isRecording = false
    private var cancelRecording = false
    private val offsetThreshold = 50 // 设置滑动偏移量阈值

    private var recordViewListener: EaseRecordViewListener? = null

    private val binding: EaseWidgetRecordBinding by lazy {
        EaseWidgetRecordBinding.inflate(LayoutInflater.from(context))
    }

    init {
        addView(binding.root)
        setOnTouchListener { view, event ->
//            Log.d("EaseRecordView", "onTouch: ${event.x}")
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start recording
                    isRecording = true
                    cancelRecording = false
                    binding.tvRecordAction.setText(R.string.aichat_release_to_send_swipe_left_to_cancel)
                    startRecordingAnimation()
                    recordViewListener?.onStartRecordingAction()
                }

                MotionEvent.ACTION_MOVE -> {
                    // 左滑时增加偏移阈值，防止在临界值处反复切换
                    if (event.x < binding.cardRecording.width / 3 * 2 - offsetThreshold) {
                        if (!cancelRecording) {
                            binding.tvRecordAction.setText(R.string.aichat_release_to_cancel)
                            binding.ivAudioBg.setBackgroundResource(R.drawable.aichat_recording_bg_waring)
                            cancelRecording = true
                        }
                    } else if (event.x > binding.cardRecording.width / 3 * 2 + offsetThreshold) {
                        if (cancelRecording) {
                            cancelRecording = false
                            binding.tvRecordAction.setText(R.string.aichat_release_to_send_swipe_left_to_cancel)
                            binding.ivAudioBg.setBackgroundResource(R.drawable.aichat_recording_bg)
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    if (isRecording) {
                        stopRecordingAnimation()
                        if (cancelRecording) {
                            // Cancel recording
                            recordViewListener?.onCancelRecordingAction()
                        } else {
                            // Send recording
                            recordViewListener?.onSendRecordingAction()
                        }
                    }
                    isRecording = false
                }
            }
            true
        }
    }

    fun setRecordViewListener(listener: EaseRecordViewListener?) {
        this.recordViewListener = listener
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == VISIBLE) {
            binding.tvRecordAction.setText(R.string.aichat_speaker_tips)
            binding.ivAudioSound.setImageResource(R.drawable.aichat_audio_no_sound)
            binding.ivAudioBg.setBackgroundResource(R.drawable.aichat_recording_bg)
        }
    }

    private fun startRecordingAnimation() {
        // Show recording animation (e.g., mic or sound waves)
        setupRecordDrawable()
    }


    private fun stopRecordingAnimation() {
        isVisible = false
    }

    private fun setupRecordDrawable() {
        if (recordDrawable == null) {
            recordDrawable =
                APNGDrawable.fromAsset(AgoraApplication.the().applicationContext, "aichat_audio_with_sound.png")
        }
        recordDrawable?.setLoopLimit(-1)
        binding.ivAudioSound.setImageDrawable(recordDrawable)
    }
}
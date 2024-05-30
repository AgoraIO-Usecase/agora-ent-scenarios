package io.agora.scene.ktv.widget

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvLayoutMicWaveBinding

/**
 * Mic seat wave view
 *
 * @constructor Create empty Mic seat wave view
 */
class MicSeatWaveView : ConstraintLayout {

    private val animatorSet = AnimatorSet()

    private val binding: KtvLayoutMicWaveBinding

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val root = View.inflate(context, R.layout.ktv_layout_mic_wave, this)
        binding = KtvLayoutMicWaveBinding.bind(root)
        setupView()
    }

    /**
     * Start wave
     *
     */
    fun startWave() {
        if (!animatorSet.isRunning) {
            Log.d("micWaveLog", "volume start")
            animatorSet.start()
        }
    }

    /**
     * End wave
     *
     */
    fun endWave() {
        if (animatorSet.isRunning) {
            Log.d("micWaveLog", "volume end")
            animatorSet.end()
        }
    }

    private fun setupView() {
        val animator1 = ObjectAnimator.ofPropertyValuesHolder(
            binding.vWave1,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.1f, 1f),
            PropertyValuesHolder.ofFloat("alpha", 1f, 0.5f, 0.3f)
        )
        animator1.repeatCount = ObjectAnimator.INFINITE
        animator1.repeatMode = ObjectAnimator.RESTART
        animator1.interpolator = DecelerateInterpolator()
        animator1.duration = 1400

        val animator2 = ObjectAnimator.ofPropertyValuesHolder(
            binding.vWave2,
            PropertyValuesHolder.ofFloat("scaleX", 1f, 1.4f),
            PropertyValuesHolder.ofFloat("scaleY", 1f, 1.4f),
            PropertyValuesHolder.ofFloat("alpha", 0.6f, 0.3f, 0f)
        )
        animator2.repeatCount = ObjectAnimator.INFINITE
        animator2.repeatMode = ObjectAnimator.RESTART
        animator2.interpolator = DecelerateInterpolator()
        animator2.duration = 1400
        animatorSet.playTogether(animator1, animator2)
    }
}
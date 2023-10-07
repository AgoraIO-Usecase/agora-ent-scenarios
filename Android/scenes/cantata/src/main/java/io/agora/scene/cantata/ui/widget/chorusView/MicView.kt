package io.agora.scene.cantata.ui.widget.chorusView

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import io.agora.scene.base.GlideApp
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataViewMicBinding
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
import kotlin.math.pow

class MicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val mBinding: CantataViewMicBinding by lazy {
        CantataViewMicBinding.inflate(LayoutInflater.from(context), this, true)
    }

    var clickBlock: ((Int) -> Unit)? = null

    init {
        mBinding.root.setOnClickListener {
            clickBlock?.invoke(tag as Int)
        }
    }

    private var mComboControl: ComboControl? = null

    fun updateMicImage(url: String) {
        if (url.isEmpty()) {
            mBinding.ivUserAvatar.setImageResource(R.drawable.cantata_ic_seat)
            return
        }
        GlideApp.with(this)
            .load(url)
            .error(R.mipmap.userimage)
            .transform(CenterCropRoundCornerTransform(100))
            .into(mBinding.ivUserAvatar)
    }

    fun getMicTextView(): TextView {
        return mBinding.tvUserName
    }

    fun reset() {
        if (mComboControl == null) {
            mComboControl = ComboControl()
        }
        mComboControl?.reset(mBinding.ivGradeIcon)
    }

    fun updateScore(score: Double) {
        if (mComboControl == null) {
            mComboControl = ComboControl()
        }
        mComboControl?.checkAndShowCombos(mBinding.ivGradeIcon, score.toInt())
    }
}

class ComboControl {
    private var mComboIconDrawable: GifDrawable? = null
    fun reset(comboView: ImageView) {
        comboView.visibility = FrameLayout.INVISIBLE
    }

    fun checkAndShowCombos(comboView: ImageView, score: Int) {
        comboView.visibility = FrameLayout.VISIBLE
        showComboAnimation(comboView, score)
    }

    private var mComboOfLastTime = 0 // Only for showComboAnimation
    private fun showComboAnimation(comboView: ImageView, score: Int) {
        var comboIconRes = 0
        if (score >= 90) {
            comboIconRes = R.drawable.cantata_combo_excellent
        } else if (score >= 75) {
            comboIconRes = R.drawable.cantata_combo_good
        } else if (score >= 60) {
            comboIconRes = R.drawable.cantata_combo_fair
        }
        mComboOfLastTime = comboIconRes
        if (comboIconRes > 0) {
            val options = RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            Glide.with(comboView.context).asGif().load(comboIconRes).apply(options)
                .addListener(object : RequestListener<GifDrawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any,
                        target: Target<GifDrawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        return true
                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        model: Any,
                        target: Target<GifDrawable>,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        mComboIconDrawable = resource
                        resource.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                            override fun onAnimationStart(drawable: Drawable) {
                                super.onAnimationStart(drawable)
                            }

                            override fun onAnimationEnd(drawable: Drawable) {
                                super.onAnimationEnd(drawable)
                                comboView.alpha = 0f
                                comboView.visibility = FrameLayout.INVISIBLE
                                mComboIconDrawable?.unregisterAnimationCallback(this)
                            }
                        })
                        resource.setLoopCount(1)
                        comboView.visibility = FrameLayout.VISIBLE
                        return false
                    }
                }).into(comboView)
        } else {
            comboView.visibility = FrameLayout.INVISIBLE
        }
    }
}

fun View.addFloatingAnimation(randomX: Float, randomY: Float) {
    val controlPoint1 = Pair(x + randomX, y + randomY)
    val controlPoint2 = Pair(x - randomX, y - randomY)
    val toPoint = Pair(x, y)

    val path = Path()
    path.moveTo(x, y)

    val numIntermediatePoints = 50
    for (i in 0 until numIntermediatePoints) {
        val t = i.toFloat() / numIntermediatePoints
        val x = bezierPoint(t, x, controlPoint1.first, controlPoint2.first, toPoint.first)
        val y = bezierPoint(t, y, controlPoint1.second, controlPoint2.second, toPoint.second)
        path.lineTo(x, y)
    }
    path.lineTo(toPoint.first, toPoint.second)

    val animator = ObjectAnimator.ofFloat(this, View.X, View.Y, path)
    animator.duration = 5000
    animator.repeatCount = Animation.INFINITE
    animator.setAutoCancel(true)
    animator.interpolator = PathInterpolator(0.0f, 0.0f, 1.0f, 1.0f)
    animator.start()
}

private fun bezierPoint(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
    val u = 1f - t
    val tt = t.pow(2f)
    val uu = u.pow(2f)
    val uuu = uu * u
    val ttt = tt * t
    return uuu * p0 + 3 * uu * t * p1 + 3 * u * tt * p2 + ttt * p3
}
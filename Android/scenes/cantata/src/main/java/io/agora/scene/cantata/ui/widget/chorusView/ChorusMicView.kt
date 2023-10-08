package io.agora.scene.cantata.ui.widget.chorusView

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.agora.scene.cantata.R
import io.agora.scene.cantata.service.RoomSeatModel
import java.lang.Math.PI
import java.lang.Math.cos
import java.lang.Math.sin
import java.util.Random
import kotlin.math.min

interface ChorusMicViewDelegate {
    fun didChorusMicViewClicked(index: Int)
}

val Number.dp
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

class ChorusMicView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ViewGroup(context, attrs, defStyle) {

    private val topMicCount: Int = 9 // 默认多少个 mic
    private val centralMicSize: Float = 96.0f.dp // 中间大麦位的大小
    private val sideMicSize: Float = 48.0f.dp // 周边麦位的大小
    private val floatingAnimationDuration: Long = 1500 // 麦位浮动动画的持续时间

    private var bgView: ImageView? = null
    private var centralMicView: MicView? = null // 中间大麦位视图
    private val sideMicViews: MutableList<MicView> = mutableListOf() // 周边麦位视图数组
    private val boundaryInset: Float = 20.0f.dp // 边界缩进值

    private val random by lazy {
        Random(System.nanoTime())
    }

    var seatArray: List<RoomSeatModel>? = null
        set(value) {
            field = value
            value?.let { updateAllMics(it) }
        }

    var delegate: ChorusMicViewDelegate? = null

    init {
        addBGView()
        setupMicViews()
    }

    private fun addBGView() {
        bgView = ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.cantata_seat_bg))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        bgView?.let {
            val bgSize = Resources.getSystem().displayMetrics.widthPixels
            val layoutParams = LayoutParams(bgSize, bgSize)
            addView(it, layoutParams)
        }
    }

    private fun setupMicViews() {
        // 添加中间大麦位视图
        centralMicView = MicView(context).apply {
            clickBlock = { index ->
                delegate?.didChorusMicViewClicked(index)
            }
            getMicTextView().apply {
                text = "admin"
                setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            }
            tag = 1000
        }
        centralMicView?.let {
            val layoutParams = LayoutParams(centralMicSize.toInt(), LayoutParams.WRAP_CONTENT)
            addView(it, layoutParams)
        }

        // 添加周边麦位视图
        for (i in 0 until topMicCount - 1) {
            val micView = MicView(context).apply {
                clickBlock = { index ->
                    delegate?.didChorusMicViewClicked(index)
                }
                getMicTextView().apply {
                    text = context.getString(R.string.cantata_seat_index, i + 1)
                    setTextColor(ResourcesCompat.getColor(resources, R.color.white_80_percent, null))
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 8f)
                }
            }
            val layoutParams = LayoutParams(sideMicSize.toInt(), LayoutParams.WRAP_CONTENT)
            sideMicViews.add(micView)
            addView(micView, layoutParams)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChildren(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        layoutChildren()
    }

    private fun layoutChildren() {
        val centerPoint = PointF(measuredWidth.toFloat() / 2f, measuredHeight.toFloat() / 2f)
        bgView?.let {
            val bgViewWidth = it.measuredWidth
            val bgViewHeight = it.measuredHeight
            val left = (centerPoint.x - bgViewWidth / 2).toInt()
            val top = (centerPoint.y - bgViewHeight / 2 - 30.0f.dp).toInt()
            val right = (centerPoint.x + bgViewWidth / 2).toInt()
            val bottom = (centerPoint.y + bgViewHeight / 2 + 30.0f.dp).toInt()
            it.layout(left, top, right, bottom)
        }

        // 布局中间大麦位视图
        centralMicView?.let {
            val seatTextViewHeight = it.getMicTextView().measuredHeight
            val left = (centerPoint.x - centralMicSize / 2).toInt()
            val top = (centerPoint.y - centralMicSize / 2).toInt()
            val right = (centerPoint.x + centralMicSize / 2).toInt()
            val bottom = ((centerPoint.y + centralMicSize / 2) + seatTextViewHeight).toInt()
            it.layout(left, top, right, bottom)
        }

        // 布局周边麦位视图
        val maxRadius =
            min(measuredWidth, measuredHeight) / 2 - centralMicSize - sideMicSize - boundaryInset * 2 // 考虑到边界缩进值
        val minRadius = centralMicSize + sideMicSize + boundaryInset * 2 // 考虑到边界缩进值
        val radiusRange = minOf(minRadius, maxRadius)..maxOf(minRadius, maxRadius)

        for (i in 0 until sideMicViews.size) {
            val angle = i.toFloat() * 2 * PI.toFloat() / sideMicViews.size // 计算每个麦位的角度

            var isValidPosition = false

            while (!isValidPosition) {
                val radius = randomFloatInRange(radiusRange) // 在最小半径和最大半径范围内随机生成麦位的半径

                // 计算麦位视图的位置
                val x = centerPoint.x + radius * cos(angle.toDouble()).toFloat()
                val y = centerPoint.y + radius * sin(angle.toDouble()).toFloat()

                val seatTextViewHeight = sideMicViews[i].getMicTextView().measuredHeight
                val micFrame =
                    RectF(
                        x - sideMicSize / 2,
                        y - sideMicSize / 2,
                        x + sideMicSize / 2,
                        y + sideMicSize / 2 + seatTextViewHeight
                    )

                if (isMicRectFValid(micFrame)) {
                    isValidPosition = true
                    sideMicViews[i].let {
                        it.layout(
                            micFrame.left.toInt(),
                            micFrame.top.toInt(),
                            micFrame.right.toInt(),
                            micFrame.bottom.toInt()
                        )
                        // 添加浮动效果
                        val randomX = nonzeroRandom((-20f).dp..20f.dp)
                        val randomY = nonzeroRandom((-20f).dp..20f.dp)
                        it.addFloatingAnimation(randomX, randomY)
                        it.tag = 1001 + i
                    }
                }
            }
        }
    }


    private fun updateAllMics(seatArray: List<RoomSeatModel>) {
        for (roomSeat in seatArray) {
            val headUrl = roomSeat.headUrl
            val index = roomSeat.seatIndex
            val micView = findMicViewWithTag(1000 + index) ?: return
            micView.getMicTextView().apply {
                text = roomSeat.name.ifEmpty { context.getString(R.string.cantata_seat_index, roomSeat.seatIndex) }
            }
            micView.updateMicImage(headUrl)
        }
    }

    fun updateMics(roomSeat: RoomSeatModel) {
        val micView = findMicViewWithTag(1000 + roomSeat.seatIndex) ?: return
        micView.getMicTextView().apply {
            text = roomSeat.name.ifEmpty { context.getString(R.string.cantata_seat_index, roomSeat.seatIndex) }
        }
        micView.updateMicImage(roomSeat.headUrl)
    }


    private fun nonzeroRandom(range: ClosedFloatingPointRange<Float>): Float {
        var randomValue: Float = 0F
        do {
            randomValue = randomFloatInRange(range)
        } while (randomValue == 0F)
        return randomValue
    }

    private fun findMicViewWithTag(tag: Int): MicView? {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is MicView && child.tag == tag) {
                return child
            }
        }
        return null
    }

    private fun randomFloatInRange(range: ClosedFloatingPointRange<Float>): Float {
        return range.start + random.nextFloat() * (range.endInclusive - range.start)
    }

    private fun isMicRectFValid(rectF: RectF): Boolean {
        for (existingMicView in sideMicViews) {
            val existingViewRect = RectF(
                existingMicView.left.toFloat(),
                existingMicView.top.toFloat(),
                existingMicView.right.toFloat(),
                existingMicView.bottom.toFloat()
            )
            if (rectF.intersect(existingViewRect)) {
                return false
            }
        }

        centralMicView?.let { centralMicView ->
            val centralViewRect = RectF(
                centralMicView.left.toFloat(),
                centralMicView.top.toFloat(),
                centralMicView.right.toFloat(),
                centralMicView.bottom.toFloat()
            )
            if (rectF.intersect(centralViewRect)) {
                return false
            }
        }

        if (rectF.left < boundaryInset || rectF.top < boundaryInset ||
            rectF.right > width - boundaryInset || rectF.bottom > height - boundaryInset
        ) {
            return false
        }
        return true
    }

}
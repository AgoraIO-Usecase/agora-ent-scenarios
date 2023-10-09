package io.agora.scene.cantata.ui.widget.chorusView

import android.content.Context
import android.content.res.Resources
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.agora.scene.cantata.R
import io.agora.scene.cantata.service.RoomSeatModel
import java.lang.Math.PI
import kotlin.math.min
import kotlin.random.Random

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

    private val TAG = "ChorusMicView"

    private val topMicCount: Int = 9 // 默认多少个 mic
    private val centralMicWidth: Float = 96.0f.dp // 中间大麦位宽度
    private val centralMicHeight: Float = 120.0f.dp // 中间大麦位高度
    private val sideMicWidth: Float = 48.0f.dp // 周边麦位的宽度
    private val sideMicHeight: Float = 68.0f.dp // 周边麦位的高度
    private val floatingAnimationDuration: Long = 1500 // 麦位浮动动画的持续时间

    private var bgView: ImageView? = null
    private var centralMicView: MicView? = null // 中间大麦位视图
    private val sideMicViews: MutableList<MicView> = mutableListOf() // 周边麦位视图数组
    private val boundaryInset: Float = 20.0f.dp // 边界缩进值

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
            val layoutParams = LayoutParams(centralMicWidth.toInt(), centralMicHeight.toInt())
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
            val layoutParams = LayoutParams(sideMicWidth.toInt(), sideMicHeight.toInt())
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
        Log.d(TAG, "onLayout layoutChildren")
    }

    private var onLayoutComplete = false

    private fun layoutChildren() {
        if (onLayoutComplete) return
        onLayoutComplete = true
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
            val micRectF = RectF(
                centerPoint.x - centralMicWidth / 2,
                centerPoint.y - centralMicWidth / 2,
                centerPoint.x + centralMicWidth / 2,
                centerPoint.y - centralMicWidth / 2 + centralMicHeight
            )
            it.layout(micRectF.left.toInt(), micRectF.top.toInt(), micRectF.right.toInt(), micRectF.bottom.toInt())
        }

        // 布局周边麦位视图
        val maxRadius =
            min(measuredWidth, measuredHeight) / 2 - centralMicWidth - sideMicWidth - boundaryInset * 2 // 考虑到边界缩进值
        val minRadius = centralMicWidth + sideMicWidth + boundaryInset * 2 // 考虑到边界缩进值
        val radiusRange = minOf(minRadius, maxRadius)..maxOf(minRadius, maxRadius)

        for (i in 0 until sideMicViews.size) {
            val angle = i.toFloat() * 2 * PI.toFloat() / sideMicViews.size // 计算每个麦位的角度

            var isValidPosition = false
            val micView = sideMicViews[i]

            while (!isValidPosition) {
                val radius = randomFloatInRange(radiusRange) // 在最小半径和最大半径范围内随机生成麦位的半径

                // 计算麦位视图的位置
                val x = centerPoint.x + radius * kotlin.math.cos(angle.toDouble()).toFloat()
                val y = centerPoint.y + radius * kotlin.math.sin(angle.toDouble()).toFloat()

                val micRectF = RectF(
                    x - sideMicWidth / 2,
                    y - sideMicWidth / 2,
                    x + sideMicWidth / 2,
                    y - sideMicWidth / 2 + sideMicHeight
                )

                if (isMicRectFValid(micRectF)) {
                    isValidPosition = true
                    micView.layout(
                        micRectF.left.toInt(),
                        micRectF.top.toInt(),
                        micRectF.right.toInt(),
                        micRectF.bottom.toInt()
                    )
                }
            }

            // 添加浮动效果
            val randomX = nonzeroRandom((-20f).dp..20f.dp)
            val randomY = nonzeroRandom((-20f).dp..20f.dp)
            micView.addFloatingAnimation(randomX, randomY)
            micView.tag = 1001 + i

            Log.d(TAG, "addFloatingAnimation")
        }
    }


     fun updateAllMics(leadSingerModel: RoomSeatModel, seatArray: List<RoomSeatModel>) {
        Log.d(TAG, "updateAllMics ${seatArray.size}")
        val micView = findMicViewWithTag(1000) ?: return
        micView.getMicTextView().apply {
            text = leadSingerModel.name.ifEmpty { context.getString(R.string.cantata_seat_index, leadSingerModel.seatIndex) }
        }
        micView.updateMicImage(leadSingerModel.headUrl)

        for (i in 0 until childCount) {
            if (seatArray.size > i) {
                val micView1 = findMicViewWithTag(1000 + i + 1) ?: return
                micView1.getMicTextView().apply {
                    text = seatArray[i].name.ifEmpty { context.getString(R.string.cantata_seat_index, i) }
                }
                micView1.updateMicImage(seatArray[i].headUrl)
            } else {
                val micView1 = findMicViewWithTag(1000 + i + 1) ?: return
                micView1.getMicTextView().apply {
                    text = context.getString(R.string.cantata_seat_index, (i + 1))
                }
                micView1.updateMicImage("")
            }
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
        val randomRadius = Random.nextDouble(range.start.toDouble(), range.endInclusive.toDouble())
        return randomRadius.toFloat()
    }

    private fun isMicRectFValid(rectF: RectF): Boolean {
        var isValid = true
        val tempRectF = RectF(rectF.left, rectF.top, rectF.right, rectF.bottom)
        for (existingMicView in sideMicViews) {
            val existingViewRect = RectF(
                existingMicView.left.toFloat(),
                existingMicView.top.toFloat(),
                existingMicView.right.toFloat(),
                existingMicView.bottom.toFloat()
            )
            if (tempRectF.intersect(existingViewRect)) {
                Log.d(TAG, "rectF.isMicRectFValid false1 $existingMicView")
                return false
            }
            Log.d(TAG, "rectF.isMicRectFValid true1 $existingMicView")
        }

        centralMicView?.let { centralMicView ->
            val centralViewRect = RectF(
                centralMicView.left.toFloat(),
                centralMicView.top.toFloat(),
                centralMicView.right.toFloat(),
                centralMicView.bottom.toFloat()
            )
            if (tempRectF.intersect(centralViewRect)) {
                Log.d(TAG, "rectF.isMicRectFValid false2 $centralMicView")
                return false
            }
            Log.d(TAG, "rectF.isMicRectFValid true2 $centralMicView")
        }

        if (tempRectF.left < boundaryInset || tempRectF.top < boundaryInset ||
            tempRectF.right > width - boundaryInset || tempRectF.bottom > height - boundaryInset
        ) {
            Log.d(TAG, "rectF.isMicRectFValid false3")
            return false
        }
        Log.d(TAG, "rectF.isMicRectFValid true3")
        return true
    }

}
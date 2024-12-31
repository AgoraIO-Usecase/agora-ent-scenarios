package io.agora.scene.voice.spatial.ui.widget.mic

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.ThreadManager
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoom3dMicLayoutBinding
import io.agora.scene.voice.spatial.global.ConfigConstants
import io.agora.scene.voice.spatial.model.OnItemMoveListener
import io.agora.scene.voice.spatial.model.SeatPositionInfo
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.annotation.MicStatus
import io.agora.scene.voice.spatial.model.constructor.RoomMicConstructor
import kotlin.math.*

class Room3DMicLayout : ConstraintLayout, View.OnClickListener, IRoomMicView {

    companion object {
        const val TAG = "Room3DMicLayout"

        // Angle deviation error, left and right 10 degrees
        const val OFFSET_ANGLE = 10

        // Time interval
        const val TIME_INTERVAL = 100

        // Number of normal users
        const val USER_SIZE = 5
    }

    private lateinit var binding: VoiceSpatialViewRoom3dMicLayoutBinding

    private val constraintSet = ConstraintSet()

    private var lastX = -1
    private var lastY = -1

    // Last move coordinates (center circle)
    private val preMovePoint = Point(0, 0)

    // spatialView size
    private val micViewSize by lazy {
        Size(binding.micV0Center.width, binding.micV0Center.height)
    }

    // rootView size
    private val rootSize by lazy {
        Size(binding.root.width, binding.root.height)
    }

    // 3d seat maximum moving distance
    private val maxTranslationScope by lazy {
        Size(
            binding.root.width / 2 - binding.micV0Center.width / 2,
            binding.root.height / 2 - binding.micV0Center.height / 2
        )
    }

    // Last angle
    private var preAngle: Double = 0.0
    // Last move time
    private var preTime: Long = 0
    private var onItemClickListener: OnItemClickListener<VoiceMicInfoModel>? = null
    private var onBotClickListener: OnItemClickListener<VoiceMicInfoModel>? = null
    private var onSpatialMoveListener: OnItemMoveListener<VoiceMicInfoModel>? = null

    /**Mic data information*/
    private val micInfoMap = mutableMapOf<Int, VoiceMicInfoModel>()

    /**Mic view information*/
    private val micViewMap = mutableMapOf<Int, IRoomMicBinding>()

    fun onItemClickListener(
        onItemClickListener: OnItemClickListener<VoiceMicInfoModel>,
        onBotClickListener: OnItemClickListener<VoiceMicInfoModel>,
        onSpatialMoveListener: OnItemMoveListener<VoiceMicInfoModel>
    ) = apply {
        this.onItemClickListener = onItemClickListener
        this.onBotClickListener = onBotClickListener
        this.onSpatialMoveListener = onSpatialMoveListener
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context, attrs, defStyleAttr, defStyleRes
    ) {
        init(context)
    }

    private fun init(context: Context) {
        val root = View.inflate(context, R.layout.voice_spatial_view_room_3d_mic_layout, this)
        binding = VoiceSpatialViewRoom3dMicLayoutBinding.bind(root)
        constraintSet.clone(binding.root)
        initListeners()
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex0] = binding.micV0Center
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex1] = binding.micV1
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex2] = binding.micV2
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex3] = binding.micV3Blue
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex4] = binding.micV4
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex5] = binding.micV5
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex6] = binding.micV6Red
        post {
            // Current moving coordinates
            preMovePoint.x = binding.micV0Center.left + micViewSize.width / 2
            preMovePoint.y = binding.micV0Center.top + micViewSize.height / 2
            binding.micV0Center.changeAngle(180.0f)
            binding.micV1.changeAngle(180.0f)
            binding.micV2.changeAngle(135.0f)
            binding.micV3Blue.changeAngle(45.0f)
            binding.micV4.changeAngle(0.0f)
            binding.micV5.changeAngle(315.0f)
            binding.micV6Red.changeAngle(225.0f)
        }
    }

    fun setUpInitMicInfoMap() {
        micInfoMap.putAll(RoomMicConstructor.builderDefault3dMicMap(context, false))
        micInfoMap.entries.forEach { entry ->
            micViewMap[entry.key]?.apply {
                binding(entry.value)
            }
        }
    }

    private fun initListeners() {
        binding.micV0Center.setOnClickListener(this)
        binding.micV1.setOnClickListener(this)
        binding.micV2.setOnClickListener(this)
        binding.micV3Blue.setOnClickListener(this)
        binding.micV4.setOnClickListener(this)
        binding.micV5.setOnClickListener(this)
        binding.micV6Red.setOnClickListener(this)
    }

    private fun getRect(view: View): RectF {
        return RectF(
            view.x,
            view.y + rootView.y,
            view.x + view.width,
            view.y + rootView.y + view.height
        )
    }

    /**
     * Get the position of the view in the Cartesian coordinate system
     */
    private fun getPosition(view: View): PointF {
        val axisLength = 20f
        val fullWidth = binding.cl3DMicLayoutRoot.width * 1.0f
        val fullHeight = binding.cl3DMicLayoutRoot.height * 1.0f
        val oPoint = PointF(fullWidth * 0.5f, fullHeight * 0.5f)
        val vPoint = PointF(view.x + view.width * 0.5f, view.y + view.height * 0.5f)
        // Relative coordinates and flip Y axis
        val relativePoint = PointF(vPoint.x - oPoint.x, oPoint.y - vPoint.y)
        // Screen relative coordinates to coordinate system coordinates
        return PointF(
            relativePoint.x / fullWidth * axisLength,
            relativePoint.y / fullHeight * axisLength
        )
    }
    /**
     * Convert Cartesian coordinates to view coordinates
     */
    private fun convertPoint(point: PointF): PointF {
        val axisLength = 20f
        val fullWidth = binding.cl3DMicLayoutRoot.width * 1.0f
        val fullHeight = binding.cl3DMicLayoutRoot.height * 1.0f
        val oPoint = PointF(fullWidth * 0.5f, fullHeight * 0.5f)
        // Cartesian screen coordinates
        val vPoint = PointF(point.x / axisLength * fullWidth, point.y / axisLength * fullHeight)
        val x = oPoint.x + vPoint.x
        val y = oPoint.y - vPoint.y
        return PointF(x, y)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.micV0Center -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex0]?.let {
                    onItemClickListener?.onItemClick(it, v, 0, -1)
                }
            }
            R.id.micV1 -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex1]?.let {
                    onItemClickListener?.onItemClick(it, v, 1, -1)
                }
            }

            R.id.micV2 -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex2]?.let {
                    onItemClickListener?.onItemClick(it, v, 2, -1)
                }
            }
            R.id.micV3Blue -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex3]?.let {
                    onBotClickListener?.onItemClick(it, v, 3, -1)
                }
            }

            R.id.micV4 -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex4]?.let {
                    onItemClickListener?.onItemClick(it, v, 4, -1)
                }
            }
            R.id.micV5 -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex5]?.let {
                    onItemClickListener?.onItemClick(it, v, 5, -1)
                }
            }

            R.id.micV6Red -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex6]?.let {
                    onBotClickListener?.onItemClick(it, v, 6, -1)
                }
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!canMove()) return super.onInterceptTouchEvent(event)
        val x = event.x.toInt()
        val y = event.y.toInt()
        // Intercept 3d seat
        if (check3DMicChildView(x, y)) {
            return if (event.action == MotionEvent.ACTION_MOVE) {
                true
            } else {
                super.onInterceptTouchEvent(event)
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    private fun canMove(): Boolean {
        val centerRtcUid = micInfoMap[ConfigConstants.MicConstant.KeyIndex0]?.member?.rtcUid ?: -1
        return centerRtcUid == myRtcUid()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!canMove()) return super.onInterceptTouchEvent(event)
        // Get the horizontal and vertical coordinates of the finger
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (!getRect(binding.micV0Center).contains(event.x, event.y)) {
                    return false
                }
                if (lastX < 0 || lastY < 0) {
                    lastX = x
                    lastY = y
                    return false
                }
                val dx = (x - lastX)
                val dy = (y - lastY)
                lastX = x
                lastY = y

                val nextTransitionX = binding.micV0Center.translationX + dx
                val nextTransitionY = binding.micV0Center.translationY + dy
                if (abs(nextTransitionX) > maxTranslationScope.width || abs(nextTransitionY) > maxTranslationScope.height) {
                    return false
                }
                binding.micV0Center.translationX = nextTransitionX
                binding.micV0Center.translationY = nextTransitionY
                // Calculate arrow angle
                val curTime = SystemClock.elapsedRealtime()
                if (curTime - preTime >= TIME_INTERVAL) {
                    preTime = curTime
                    // Current coordinates to move to the circle point
                    val curPoint = Point(
                        binding.micV0Center.left + nextTransitionX.toInt() + micViewSize.width / 2,
                        binding.micV0Center.top + nextTransitionY.toInt() + micViewSize.height / 2
                    )
                    // Moving angle
                    val generalAngle = getAngle(curPoint, preMovePoint)
                    val angle = generalAngle + 90f
                    if (abs(angle - preAngle) > OFFSET_ANGLE) {
                        binding.micV0Center.changeAngle(angle.toFloat())
                        preMovePoint.x = curPoint.x
                        preMovePoint.y = curPoint.y
                        preAngle = angle
                    }
                    Log.d(TAG,"onTouchEvent ACTION_MOVE x:${x} y:${y} dx:${dx} dy:${dy} angle:${angle}")
                    micInfoMap[ConfigConstants.MicConstant.KeyIndex0]?.let {
                        val point = getPosition(binding.micV0Center)
                        val p = SeatPositionInfo(
                            it.member?.rtcUid ?: -1,
                            floatArrayOf(cos(angle).toFloat(), sin(angle).toFloat(), 0f),
                            point.x,
                            point.y,
                            generalAngle.toFloat()
                        )
                        onSpatialMoveListener?.onItemMove(it, p, -1)
                    }
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                lastX = -1
                lastY = -1
            }
            MotionEvent.ACTION_CANCEL -> {
                lastX = -1
                lastY = -1
            }
        }
        return super.onTouchEvent(event)
    }

    // Correct x deviation
    private fun correctMotionEventX(x: Int): Int {
        if (x < micViewSize.width / 2) return micViewSize.width / 2
        if (x > rootSize.width - micViewSize.width / 2) return rootSize.width - micViewSize.width / 2
        return x
    }

    // Correct y deviation
    private fun correctMotionEventY(y: Int): Int {
        if (y < micViewSize.height / 2) return micViewSize.height / 2
        if (y > rootSize.height - micViewSize.height / 2) return rootSize.height - micViewSize.height / 2
        return y
    }

    private fun getAngle(curP: Point, preP: Point): Double {
        val changeX = curP.x - preP.x
        val changeY = curP.y - preP.y
        // Use inverse trigonometric functions to find the angle
        val radina = atan2(changeY.toDouble(), changeX.toDouble())
        return 180.0 / Math.PI * radina
    }

    private fun ignoreSmallOffsets(dx: Float, dy: Float): Boolean {
        return abs(dx) < 10f && abs(dy) < 10f
    }

    /**
     * Whether it is a 3d seat movement
     */
    private fun check3DMicChildView(x: Int, y: Int): Boolean {
        if (getRect(binding.micV0Center).contains(x.toFloat(), y.toFloat())) {
            Log.d(TAG,"onTouchEvent ACTION_DOWN checkChildView:${x} ${y}")
            return true
        }
        return false
    }

    private fun setChildView(childView: View, isClickable: Boolean) {
        childView.isClickable = isClickable
    }

    override fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean, complete: (() -> Unit)?) {
        micInfoList.forEach { micInfo ->
            if (micInfo.micIndex != 3 && micInfo.micIndex != 6) {
                val index = micInfo.micIndex
                micInfoMap[index] = micInfo
                micViewMap[index]?.apply {
                    binding(micInfo)
                    micInfo.position = getPosition(this as View)
                    micInfo.forward = getForward(index)
                }
            }
        }
        activeBot(isBotActive, null)
        complete?.invoke()
    }

    override fun activeBot(active: Boolean, each: ((Int, Pair<PointF, PointF>) -> Unit)?) {
        if (active) {
            micInfoMap[ConfigConstants.MicConstant.KeyIndex3]?.apply {
                this.micStatus = MicStatus.BotActivated
                binding.micV3Blue.binding(this)
                micViewMap[ConfigConstants.MicConstant.KeyIndex3]?.binding(this)
                // BotSpeaker Type Position
                each?.invoke(0, Pair(getPosition(binding.micV3Blue), PointF(1f, 1f)))
            }
            micInfoMap[ConfigConstants.MicConstant.KeyIndex6]?.apply {
                this.micStatus = MicStatus.BotActivated
                micViewMap[ConfigConstants.MicConstant.KeyIndex6]?.binding(this)
                each?.invoke(1, Pair(getPosition(binding.micV6Red), PointF(-1f, -1f)))
            }
        } else {
            micInfoMap[ConfigConstants.MicConstant.KeyIndex3]?.apply {
                this.micStatus = MicStatus.BotInactive
                binding.micV3Blue.binding(this)
                micViewMap[ConfigConstants.MicConstant.KeyIndex3]?.binding(this)
            }
            micInfoMap[ConfigConstants.MicConstant.KeyIndex6]?.apply {
                this.micStatus = MicStatus.BotInactive
                micViewMap[ConfigConstants.MicConstant.KeyIndex6]?.binding(this)
            }
        }
    }

    override fun updateVolume(index: Int, volume: Int) {
        if (index >= 0 && index < micInfoMap.size) {
            micInfoMap[index]?.apply {
                this.audioVolumeType = volume
                micViewMap[index]?.binding(this)
            }
        }
    }
    /**Update robot prompt volume*/
    override fun updateBotVolume(speakerType: Int, volume: Int) {
        when (speakerType) {
            ConfigConstants.BotSpeaker.BotBlue -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex3]?.apply {
                    this.audioVolumeType = volume
                    binding.micV3Blue.binding(this)
                }
            }
            ConfigConstants.BotSpeaker.BotRed -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex6]?.apply {
                    this.audioVolumeType = volume
                    binding.micV6Red.binding(this)
                }
            }
            else -> {
                micInfoMap[ConfigConstants.MicConstant.KeyIndex3]?.apply {
                    this.audioVolumeType = volume
                    binding.micV3Blue.binding(this)
                }
                micInfoMap[ConfigConstants.MicConstant.KeyIndex6]?.apply {
                    this.audioVolumeType = volume
                    binding.micV6Red.binding(this)
                }
            }
        }
    }

    override fun findMicByUid(uid: String): Int {
        micInfoMap.entries.forEach { entry ->
            val index = entry.key
            val micInfo = entry.value
            if (TextUtils.equals(micInfo.member?.userId, uid)) {
                return index
            }
        }
        return -1
    }

    override fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>, complete: (() -> Unit)?) {
        ThreadManager.getInstance().runOnMainThread {
            newMicMap.entries.forEach { entry ->
                val index = entry.key
                val micInfo = entry.value
                // Normal user
                if (index != 3 && index != 6) {
                    micInfoMap[index] = micInfo
                    micViewMap[index]?.apply {
                        binding(micInfo)
                        micInfo.position = getPosition(this as View)
                        micInfo.forward = getForward(index)
                    }
                }
                complete?.invoke()
            }
            // Robot
            if (newMicMap.containsKey(ConfigConstants.MicConstant.KeyIndex3)) {
                val value = newMicMap[ConfigConstants.MicConstant.KeyIndex3]
                activeBot(value?.micStatus == MicStatus.BotActivated, null)
            }
        }
    }

    private var myRtcUid: Int = -1

    fun setMyRtcUid(rtcUid: Int) {
        this.myRtcUid = rtcUid
    }

    override fun myRtcUid(): Int {
        return myRtcUid
    }

    override fun updateSpatialPosition(info: SeatPositionInfo) {
        post {
            val view = binding.micV0Center
            val p = convertPoint(PointF(info.x, info.y))
            view.x = p.x - view.width * 0.5f
            view.y = p.y - view.height * 0.5f
            binding.micV0Center.changeAngle(info.angle + 90)
        }
    }

    private fun getForward(index: Int): PointF {
        return when (index) {
            0 -> PointF(1f, 0f)
            1 -> PointF(0f, -1f)
            2 -> PointF(1f, -1f)
            4 -> PointF(0f, 1f)
            5 -> PointF(1f, -1f)
            else -> PointF(0f, 0f)
        }
    }
}
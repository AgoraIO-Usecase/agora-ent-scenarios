package io.agora.scene.voice.spatial.ui.widget.mic

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.RectF
import android.os.SystemClock
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Size
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialViewRoom3dMicLayoutBinding
import io.agora.scene.voice.spatial.model.OnItemMoveListener
import io.agora.scene.voice.spatial.model.SeatPositionInfo
import io.agora.scene.voice.spatial.model.VoiceMicInfoModel
import io.agora.scene.voice.spatial.model.annotation.MicStatus
import io.agora.scene.voice.spatial.model.constructor.RoomMicConstructor
import io.agora.voice.common.constant.ConfigConstants
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.utils.LogTools.logD
import io.agora.voice.common.utils.ThreadManager
import kotlin.math.*

class Room3DMicLayout : ConstraintLayout, View.OnClickListener, IRoomMicView {

    companion object {
        const val TAG = "Room3DMicLayout"

        // 偏移角度误差,左右10度
        const val OFFSET_ANGLE = 10

        // 时间间隔
        const val TIME_INTERVAL = 100

        // 普通用户个数
        const val USER_SIZE = 5
    }

    private lateinit var binding: VoiceSpatialViewRoom3dMicLayoutBinding

    private val constraintSet = ConstraintSet()

    private var lastX = 0
    private var lastY = 0

    // 上一次移动坐标(中心圆点)
    private val preMovePoint = Point(0, 0)

    // 是否触摸移动
    private var isMove = false

    // spatialView 尺寸
    private val micViewSize by lazy {
        Size(binding.micV0Center.width, binding.micV0Center.height)
    }

    // rootView尺寸
    private val rootSize by lazy {
        Size(binding.root.width, binding.root.height)
    }

    // 3d 座位最大移动距离
    private val maxTranslationScope by lazy {
        Size(
            binding.root.width / 2 - binding.micV0Center.width / 2,
            binding.root.height / 2 - binding.micV0Center.height / 2
        )
    }

    // 上一次角度
    private var preAngle: Double = 0.0
    // 上一次移动的时间
    private var preTime: Long = 0
    private var onItemClickListener: OnItemClickListener<VoiceMicInfoModel>? = null
    private var onBotClickListener: OnItemClickListener<VoiceMicInfoModel>? = null
    private var onSpatialMoveListener: OnItemMoveListener<VoiceMicInfoModel>? = null

    /**麦位数据信息*/
    private val micInfoMap = mutableMapOf<Int, VoiceMicInfoModel>()

    /**麦位view信息*/
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
        post {
            // 当前移动的坐标圆点
            preMovePoint.x = binding.micV0Center.left + micViewSize.width / 2
            preMovePoint.y = binding.micV0Center.top + micViewSize.height / 2
        }
        binding.micV1.changeAngle(180.0f)
        binding.micV2.changeAngle(135.0f)
        binding.micV3Blue.changeAngle(45.0f)
        binding.micV4.changeAngle(0.0f)
        binding.micV5.changeAngle(315.0f)
        binding.micV6Red.changeAngle(225.0f)
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex0] = binding.micV0Center
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex1] = binding.micV1
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex2] = binding.micV2
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex3] = binding.micV3Blue
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex4] = binding.micV4
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex5] = binding.micV5
        this.micViewMap[ConfigConstants.MicConstant.KeyIndex6] = binding.micV6Red
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
     * 获取视图在笛卡尔坐标系中的位置
     */
    private fun getPosition(view: View): PointF {
        val axisLength = 20f
        val fullWidth = binding.cl3DMicLayoutRoot.width * 1.0f
        val fullHeight = binding.cl3DMicLayoutRoot.height * 1.0f
        val oPoint = PointF(fullWidth * 0.5f, fullHeight * 0.5f)
        // 翻转Y轴
        val turnY = fullHeight - view.y
        val vPoint = PointF(view.width * 0.5f + view.x, turnY - (view.height * 0.5f))
        // 相对坐标
        val relativePoint = PointF(vPoint.x - oPoint.x, vPoint.y - oPoint.y)
        // 屏幕相对坐标转化为坐标系坐标
        return PointF(
            relativePoint.x / fullWidth * axisLength,
            relativePoint.y / fullWidth * axisLength
        )
    }
    /**
     * 将笛卡尔坐标转换成视图中的坐标
     */
    private fun convertPoint(point: PointF): PointF {
        val axisLength = 20f
        val fullWidth = binding.cl3DMicLayoutRoot.width * 1.0f
        val fullHeight = binding.cl3DMicLayoutRoot.height * 1.0f
        val oPoint = PointF(fullWidth * 0.5f, fullHeight * 0.5f)
        // 笛卡尔屏幕坐标
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
        //拦截3d 座位
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
        //获取到手指处的横坐标和纵坐标
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                isMove = false
                "onTouchEvent ACTION_DOWN x:${x} y:${y}".logD(TAG)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
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
                isMove = true
                // 计算箭头角度
                val curTime = SystemClock.elapsedRealtime()
                if (curTime - preTime >= TIME_INTERVAL) {
                    preTime = curTime
                    // 当前准备移动到的坐标圆点
                    val curPoint = Point(
                        binding.micV0Center.left + nextTransitionX.toInt() + micViewSize.width / 2,
                        binding.micV0Center.top + nextTransitionY.toInt() + micViewSize.height / 2
                    )
                    // 移动的角度
                    val generalAngle = getAngle(curPoint, preMovePoint)
                    val angle = generalAngle + 90f
                    if (abs(angle - preAngle) > OFFSET_ANGLE) {
                        binding.micV0Center.changeAngle(angle.toFloat())
                        preMovePoint.x = curPoint.x
                        preMovePoint.y = curPoint.y
                        preAngle = angle
                    }
                    "onTouchEvent ACTION_MOVE x:${x} y:${y} dx:${dx} dy:${dy} angle:${angle}".logD(TAG)
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
            }
            MotionEvent.ACTION_CANCEL -> {}
        }
        return super.onTouchEvent(event)
    }

    // 纠正x偏差
    private fun correctMotionEventX(x: Int): Int {
        if (x < micViewSize.width / 2) return micViewSize.width / 2
        if (x > rootSize.width - micViewSize.width / 2) return rootSize.width - micViewSize.width / 2
        return x
    }

    // 纠正y偏差
    private fun correctMotionEventY(y: Int): Int {
        if (y < micViewSize.height / 2) return micViewSize.height / 2
        if (y > rootSize.height - micViewSize.height / 2) return rootSize.height - micViewSize.height / 2
        return y
    }

    private fun getAngle(curP: Point, preP: Point): Double {
        val changeX = curP.x - preP.x
        val changeY = curP.y - preP.y
        // 用反三角函数求弧度
        val radina = atan2(changeY.toDouble(), changeX.toDouble())
        return 180.0 / Math.PI * radina
    }

    private fun ignoreSmallOffsets(dx: Float, dy: Float): Boolean {
        return abs(dx) < 10f && abs(dy) < 10f
    }

    /**
     * 是否有是3d 座位移动
     */
    private fun check3DMicChildView(x: Int, y: Int): Boolean {
        if (getRect(binding.micV0Center).contains(x.toFloat(), y.toFloat())) {
            "onTouchEvent ACTION_DOWN checkChildView:${x} ${y}".logD(TAG)
            return true
        }
        return false
    }

    private fun setChildView(childView: View, isClickable: Boolean) {
        childView.isClickable = isClickable
    }

    override fun onInitMic(micInfoList: List<VoiceMicInfoModel>, isBotActive: Boolean) {
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
    /**更新机器人提示音量*/
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

    override fun onSeatUpdated(newMicMap: Map<Int, VoiceMicInfoModel>) {
        ThreadManager.getInstance().runOnMainThread {
            newMicMap.entries.forEach { entry ->
                val index = entry.key
                val micInfo = entry.value
                // 普通用户
                if (index != 3 && index != 6) {
                    micInfoMap[index] = micInfo
                    micViewMap[index]?.apply {
                        binding(micInfo)
                        micInfo.position = getPosition(this as View)
                        micInfo.forward = getForward(index)
                    }
                }
            }
            // 机器人
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
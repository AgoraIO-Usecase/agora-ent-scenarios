package io.agora.videoloaderapi

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.View
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngineEx

/**
 * 直播间item触摸事件
 * @param mRtcEngine RtcEngineEx对象
 * @param roomInfo 房间信息
 * @param localUid 观众uid
 */
abstract class OnLiveRoomItemTouchEventHandler constructor(
    private val mRtcEngine: RtcEngineEx,
    private val roomInfo: VideoLoader.RoomInfo,
    private val localUid: Int
): View.OnTouchListener {
    private val tag = "[VideoLoader]Touch"
    private val videoLoader by lazy { VideoLoader.getImplInstance(mRtcEngine) }
    private val clickInternal = 500L
    private var lastClickTime = 0L

    // View.OnTouchListener
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // 处理频繁点击事件
        if (System.currentTimeMillis() - lastClickTime <= clickInternal) return true
        val motionEvent = event ?: return true
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                VideoLoader.videoLoaderApiLog(tag, "click down, roomInfo:${roomInfo}")
                roomInfo.anchorList.forEach { anchorInfo->
                    // 加入频道并将远端音量调为0
                    videoLoader.switchAnchorState(AnchorState.JOINED_WITHOUT_AUDIO, anchorInfo, localUid)
                    // 触发视频渲染最佳时机
                    onRequireRenderVideo(anchorInfo)?.let { canvas ->
                        videoLoader.renderVideo(anchorInfo, localUid, canvas)
                    }
                    (videoLoader as VideoLoaderImpl).getProfiler(anchorInfo.channelId, anchorInfo.anchorUid).perceivedStartTime = System.currentTimeMillis()
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                VideoLoader.videoLoaderApiLog(tag, "click cancel, roomInfo:${roomInfo}")
                roomInfo.anchorList.forEach {
                    videoLoader.switchAnchorState(AnchorState.IDLE, it, localUid)
                }
            }
            MotionEvent.ACTION_UP -> {
                VideoLoader.videoLoaderApiLog(tag, "click up, roomInfo:${roomInfo}")
                lastClickTime = System.currentTimeMillis()
                roomInfo.anchorList.forEach { anchorInfo ->
                    videoLoader.switchAnchorState(AnchorState.JOINED, anchorInfo, localUid)
                    // 打点
                    mRtcEngine.startMediaRenderingTracingEx(RtcConnection(anchorInfo.channelId, localUid))
                    (videoLoader as VideoLoaderImpl).getProfiler(anchorInfo.channelId, anchorInfo.anchorUid).perceivedStartTime = System.currentTimeMillis()
                }
            }
        }
        return true
    }

    // 推荐设置视图的最佳时机
    abstract fun onRequireRenderVideo(info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer?
}

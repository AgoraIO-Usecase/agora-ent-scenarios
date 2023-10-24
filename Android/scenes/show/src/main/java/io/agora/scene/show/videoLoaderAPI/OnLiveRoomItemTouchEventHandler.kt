package io.agora.scene.show.videoLoaderAPI

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.RtcEngineEx

/**
 * 直播间item触摸事件
 * @param context 直播间item处在的上下文
 * @param mRtcEngine RtcEngineEx对象
 * @param roomInfo 房间信息
 * @param localUid 观众uid
 */
abstract class OnLiveRoomItemTouchEventHandler constructor(
    private val context: Context,
    private val mRtcEngine: RtcEngineEx,
    private val roomInfo: VideoLoader.RoomInfo,
    private val localUid: Int
): View.OnTouchListener {
    private val tag = "OnTouchEventHandler"
    private val videoSwitcher by lazy { VideoLoader.getImplInstance(mRtcEngine) }
    private val CLICK_INTERNAL = 500L
    private var lastClickTime = 0L

    // View.OnTouchListener
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        // 处理频繁点击事件
        if (System.currentTimeMillis() - lastClickTime <= CLICK_INTERNAL) return true
        val motionEvent = event ?: return true
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(tag, "click down")
                roomInfo.anchorList.forEach { anchorInfo->
                    // 加入频道并将远端音量调为0
                    videoSwitcher.switchAnchorState(AnchorState.JOINED, anchorInfo, localUid, null)
                    mRtcEngine.adjustUserPlaybackSignalVolumeEx(anchorInfo.anchorUid, 0, RtcConnection(anchorInfo.channelId, localUid))
                    // 触发视频渲染最佳时机
                    onRequireRenderVideo(anchorInfo)?.let { canvas ->
                        videoSwitcher.renderVideo(anchorInfo, localUid, canvas)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                Log.d(tag, "click cancel")
                roomInfo.anchorList.forEach {
                    videoSwitcher.switchAnchorState(AnchorState.IDLE, it, localUid, null)
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.d(tag, "click up join channel, roomInfo:${roomInfo}")
                lastClickTime = System.currentTimeMillis()
                roomInfo.anchorList.forEach { anchorInfo->
                    // 打点
                    mRtcEngine.startMediaRenderingTracingEx(RtcConnection(anchorInfo.channelId, localUid))
                }
            }
        }
        return true
    }

    // 推荐设置视图的最佳时机
    abstract fun onRequireRenderVideo(info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer?
}

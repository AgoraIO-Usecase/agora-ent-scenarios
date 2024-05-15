package io.agora.scene.showTo1v1.videoloaderapi

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.showTo1v1.videoloaderapi.report.APIReporter
import io.agora.scene.showTo1v1.videoloaderapi.report.APIType

/**
 * 房间状态
 * @param IDLE 默认状态
 * @param PRE_JOINED 预加入房间状态
 * @param JOINED 已进入房间状态
 * @param JOINED_WITHOUT_AUDIO 不播放音频
 */
enum class AnchorState {
    IDLE,
    PRE_JOINED,
    JOINED,
    JOINED_WITHOUT_AUDIO,
}

/**
 * 视频流管理模块
 */
interface VideoLoader {

    companion object {
        const val version = "1.0.0"
        private var rtcEngine: RtcEngineEx? = null
        private var instance: VideoLoader? = null
        var reporter: APIReporter? = null

        fun getImplInstance(engine: RtcEngineEx): VideoLoader {
            rtcEngine = engine
            if (instance == null) {
                instance = VideoLoaderImpl(engine)
                reporter = APIReporter(APIType.VIDEO_LOADER, version, engine)
                engine.enableInstantMediaRendering()
            }
            return instance as VideoLoader
        }

        // 日志输出
        fun videoLoaderApiLog(tag: String, msg: String) {
            reporter?.writeLog("[$tag] $msg", Constants.LOG_LEVEL_INFO)
        }

        // 日志输出
        fun videoLoaderApiLogWarning(tag: String, msg: String) {
            reporter?.writeLog("[$tag] $msg", Constants.LOG_LEVEL_WARNING)
        }

        fun release() {
            instance = null
            rtcEngine = null
            reporter = null
        }
    }

    /**
     * 视频容器
     * @param lifecycleOwner 视频容器所在的生命周期, 推荐为Fragment的viewLifecycleOwner
     * @param container 视频容器
     * @param uid 需要渲染对象视频流的uid
     * @param viewIndex 视频view在container上的区域index
     * @param renderMode 需要渲染对象视频流方式
     */
    data class VideoCanvasContainer(
        val lifecycleOwner: LifecycleOwner,
        val container: ViewGroup,
        val uid: Int,
        val viewIndex: Int = 0,
        val renderMode: Int = Constants.RENDER_MODE_HIDDEN,
    )

    /**
     * 房间内单个主播用户信息
     * @param channelId 频道名
     * @param anchorUid 主播uid
     * @param token 加入channel需要的token（建议使用万能token）
     */
    data class AnchorInfo constructor(
        val channelId: String = "",
        val anchorUid: Int = 0,
        val token: String = ""
    ) {
        override fun toString(): String {
            return "channelId:$channelId, anchorUid:$anchorUid"
        }
    }

    /**
     * 房间信息
     * @param roomId 房主频道
     * @param anchorList 主播列表
     */
    data class RoomInfo(
        val roomId: String,
        val anchorList: ArrayList<AnchorInfo>
    )

    /**
     * 清除缓存、离开所有已加入的频道连接
     */
    fun cleanCache()

    /**
     * 预加载主播频道
     * @param anchorList 主播列表
     * @param uid 用户uid
     */
    fun preloadAnchor(anchorList: List<AnchorInfo>, uid: Int)

    /**
     * 切换指定主播的状态
     * @param newState 目标状态
     * @param anchorInfo 主播信息
     * @param localUid 本地用户 uid
     * @param mediaOptions 自定义的 ChannelMediaOptions
     */
    fun switchAnchorState(
        newState: AnchorState,
        anchorInfo: AnchorInfo,
        localUid: Int,
        mediaOptions: ChannelMediaOptions? = null
    )

    /**
     * 获取指定房间的状态
     * @param channelId 频道名
     * @param localUid 用户id
     */
    fun getAnchorState(channelId: String, localUid: Int): AnchorState?


    /**
     * 渲染远端视频，相比于RtcEngineEx.setupRemoteVideoEx，这里会缓存渲染视图，减少渲染时不断重复创建渲染视图，提高渲染速度
     * @param anchorInfo 主播信息
     * @param localUid 用户id
     * @param container 视频渲染的容器，内部会把view显示在容器的指定区域
     */
    fun renderVideo(anchorInfo: AnchorInfo, localUid: Int, container: VideoCanvasContainer)
}
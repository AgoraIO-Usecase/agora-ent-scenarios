package io.agora.scene.show.videoLoaderAPI

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngineEx

/**
 * 视频流管理模块
 */
interface VideoLoader {

    companion object {
        private var rtcEngine: RtcEngineEx? = null
        private var instance: VideoLoader? = null

        fun getImplInstance(engine: RtcEngineEx): VideoLoader {
            rtcEngine = engine
            if (instance == null) {
                instance = VideoLoaderImpl(rtcEngine!!)
                engine.enableInstantMediaRendering()
            }
            return instance as VideoLoader
        }

        fun release() {
            instance = null
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
            return "{channelId=$channelId, anchorUid=$anchorUid"
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
     * 切换指定主播的状态
     * @param anchorList 主播列表
     * @param uid 用户uid
     */
    fun preloadAnchor(anchorList: List<AnchorInfo>, uid: Int)

    /**
     * 切换指定主播的状态
     * @param newState 目标状态
     * @param anchorInfo 主播信息
     * @param uid 用户uid
     */
    fun switchAnchorState(
        newState: AnchorState,
        anchorInfo: AnchorInfo,
        uid: Int
    )

    /**
     * 切换指定房间的状态
     * @param channelId 频道名
     * @param localUid 用户id
     */
    fun getRoomState(channelId: String, localUid: Int): AnchorState?


    /**
     * 渲染远端视频，相比于RtcEngineEx.setupRemoteVideoEx，这里会缓存渲染视图，减少渲染时不断重复创建渲染视图，提高渲染速度
     * @param anchorInfo 主播信息
     * @param localUid 用户id
     * @param container 视频渲染的容器，内部会把view显示在容器的指定区域
     */
    fun renderVideo(anchorInfo: AnchorInfo, localUid: Int, container: VideoCanvasContainer)
}
package io.agora.scene.show.videoLoaderAPI

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import androidx.viewpager2.widget.ViewPager2
import io.agora.rtc2.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 直播间出图模式
 * @param VISIABLE 立即出图
 * @param END_DRAG 松手出图
 * @param END_SCROLL 停下出图
 */
enum class AGSlicingType(val value :Int) {
    VISIABLE(0),  //立即出图
    END_DRAG(1),  //松手出图
    END_SCROLL(2),  //停下出图
    NEVER(3)
}

/**
 * 直播间 item 触摸事件
 * @param mRtcEngine RtcEngineEx 对象
 * @param localUid 观众 uid
 * @param needPreJoin 房间是否需要 preJoin
 * @param videoScrollMode 视频出图模式
 */
abstract class OnPageScrollEventHandler constructor(
    private val mRtcEngine: RtcEngineEx,
    private val localUid: Int,
    private val needPreJoin: Boolean,
    private val videoScrollMode: AGSlicingType
) : ViewPager2.OnPageChangeCallback() {
    private val tag = "OnPageScrollHandler"
    private val videoSwitcher by lazy { VideoLoader.getImplInstance(mRtcEngine) }
    private val roomList = SparseArray<VideoLoader.RoomInfo>()

    fun cleanCache() {
        mainHandler.removeCallbacksAndMessages(null)
        roomsForPreloading.clear()
        roomsJoined.clear()
    }

    // ViewPager2.OnPageChangeCallback()
    private val POSITION_NONE = -1
    private var currLoadPosition = POSITION_NONE
    private val PRE_LOAD_OFFSET = 0.01f
    private var preLoadPosition = POSITION_NONE
    private var lastOffset = 0f
    private var scrollStatus: Int = ViewPager2.SCROLL_STATE_IDLE

    private val roomsForPreloading = Collections.synchronizedList(mutableListOf<VideoLoader.RoomInfo>())
    private val roomsJoined = Collections.synchronizedList(mutableListOf<VideoLoader.RoomInfo>())

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    fun onRoomCreated(position: Int, info: VideoLoader.RoomInfo, isCurrentItem: Boolean) {
        roomList.put(position, info)
        if (isCurrentItem) {
            joinChannel(position, info, localUid, true)
            info.anchorList.forEach {
                mRtcEngine.adjustUserPlaybackSignalVolumeEx(it.anchorUid, 100, RtcConnection(it.channelId, localUid))
            }
            mainHandler.postDelayed({
                roomsJoined.add(info)
                preJoinChannels()
            }, 200)
            onPageStartLoading(position)
        }
    }

    fun updateRoomList(list: ArrayList<VideoLoader.RoomInfo>) {
        roomsForPreloading.addAll(list)
    }

    fun updateRoomInfo(position: Int, info: VideoLoader.RoomInfo) {
        if (info.roomId != roomList[position].roomId) return
        val oldAnchorList = roomList[position].anchorList
        val newAnchorList = info.anchorList
        newAnchorList.forEach { newInfo ->
            videoSwitcher.switchAnchorState(AnchorState.JOINED, newInfo, localUid)
            onRequireRenderVideo(position, newInfo)?.let { canvas ->
                videoSwitcher.renderVideo(
                    newInfo,
                    localUid,
                    canvas
                )
            }
        }

        oldAnchorList.forEach { oldInfo ->
            if (newAnchorList.none { new -> new.channelId == oldInfo.channelId }) {
                videoSwitcher.switchAnchorState(AnchorState.IDLE, oldInfo, localUid)
            }
        }

        val roomInfo = roomsForPreloading.filter { it.roomId == info.roomId }.getOrNull(0) ?: return
        val index = roomsForPreloading.indexOf(roomInfo)
        roomsForPreloading[index] = info
        roomList[position] = info
    }

    fun getCurrentRoomPosition(): Int {
        return currLoadPosition
    }

    // ViewPager2
    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)
        Log.d(tag, "PageChange onPageScrollStateChanged state=$state")
        when (state) {
            ViewPager2.SCROLL_STATE_IDLE -> {
                if(preLoadPosition != POSITION_NONE){
                    // TODO preLoadPosition 页面消失
                    hideChannel(roomList[preLoadPosition] ?: return)
                    onPageLeft(preLoadPosition)
                }
                // TODO currLoadPosition 页面显示完成
                startAudio(roomList[currLoadPosition] ?: return)
                roomsJoined.add(roomList[currLoadPosition] ?: return)
                preJoinChannels()
                pageLoaded(currLoadPosition, roomList[currLoadPosition])
                preLoadPosition = POSITION_NONE
                lastOffset = 0f
            }
            ViewPager2.SCROLL_STATE_SETTLING -> {
                // 松手事件
            }
            ViewPager2.SCROLL_STATE_DRAGGING -> {

            }
        }
        scrollStatus = state
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
        Log.d(tag, "PageChange onPageScrolled positionOffset=$positionOffset")
        if (scrollStatus == ViewPager2.SCROLL_STATE_DRAGGING) {
            if (lastOffset > 0f) {
                val isMoveUp = (positionOffset - lastOffset) > 0
                if (isMoveUp && positionOffset >= PRE_LOAD_OFFSET && preLoadPosition == POSITION_NONE) {
                    preLoadPosition = currLoadPosition + 1
                    // TODO preLoadPosition 页面开始显示
                    joinChannel(preLoadPosition, roomList[preLoadPosition] ?: return, localUid, false)
                    onPageStartLoading(preLoadPosition)
                } else if (!isMoveUp && positionOffset <= (1 - PRE_LOAD_OFFSET) && preLoadPosition == POSITION_NONE) {
                    preLoadPosition = currLoadPosition - 1
                    // TODO preLoadPosition 页面开始显示
                    joinChannel(preLoadPosition, roomList[preLoadPosition] ?: return, localUid, false)
                    onPageStartLoading(preLoadPosition)
                }
            }
            lastOffset = positionOffset
        }
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        Log.d(tag, "PageChange onPageSelected position=$position, currLoadPosition=$currLoadPosition, preLoadPosition=$preLoadPosition")

        if (currLoadPosition != POSITION_NONE) {
            if (preLoadPosition != POSITION_NONE) {
                if (position == preLoadPosition) {
                    // TODO currLoadPosition 页面消失
                    hideChannel(roomList[currLoadPosition] ?: return)
                    onPageLeft(currLoadPosition)
                } else {
                    // TODO preLoadPosition 页面消失
                    hideChannel(roomList[preLoadPosition] ?: return)
                    onPageLeft(preLoadPosition)

                    // TODO currLoadPosition 页面显示完成
                    roomsJoined.add(roomList[currLoadPosition] ?: return)
                    preJoinChannels()
                    startAudio(roomList[currLoadPosition] ?: return)
                    pageLoaded(currLoadPosition, roomList[currLoadPosition])
                }
            }

            if (currLoadPosition != position) {
                // TODO currLoadPosition 页面消失
                hideChannel(roomList[currLoadPosition] ?: return)
                onPageLeft(currLoadPosition)

                joinChannel(position, roomList[position] ?: return, localUid, false)
                onPageStartLoading(position)
            }
        }
        currLoadPosition = position
        preLoadPosition = POSITION_NONE
        lastOffset = 0f
    }

    // OnPageStateEventHandler
    abstract fun onPageStartLoading(position: Int)

    abstract fun onPageLoaded(position: Int)

    abstract fun onPageLeft(position: Int)

    abstract fun onRequireRenderVideo(position: Int, info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer?

    // ------------------------ inner ---------------------------
    private fun joinChannel(position: Int, roomInfo: VideoLoader.RoomInfo, uid: Int, isCurrentItem: Boolean) {
        Log.d(tag, "joinChannel roomInfo=$roomInfo")

        roomInfo.anchorList.forEach { anchorInfo ->
            videoSwitcher.switchAnchorState(AnchorState.JOINED_WITHOUT_AUDIO, anchorInfo, uid)
            if (videoScrollMode == AGSlicingType.VISIABLE || isCurrentItem) {
                onRequireRenderVideo(position, anchorInfo)?.let {
                    videoSwitcher.renderVideo(
                        anchorInfo,
                        localUid,
                        it
                    )
                }
            }

            // 打点
            mRtcEngine.startMediaRenderingTracingEx(RtcConnection(anchorInfo.channelId, localUid))
        }
    }

    private fun hideChannel(roomInfo: VideoLoader.RoomInfo) {
        Log.d(tag, "switchRoomState, hideChannel: $roomInfo")
        roomsJoined.removeIf { it.roomId == roomInfo.roomId }
        val currentRoom = roomsJoined.firstOrNull() ?: return
        roomInfo.anchorList.forEach {
            if (needPreJoin && currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                videoSwitcher.switchAnchorState(AnchorState.PRE_JOINED, it, localUid)
            }
        }
    }

    private fun startAudio(roomInfo: VideoLoader.RoomInfo) {
        roomInfo.anchorList.forEach {
            videoSwitcher.switchAnchorState(AnchorState.JOINED, it, localUid)
        }
    }

    private fun preJoinChannels() {
        val size = roomsForPreloading.size
        val currentRoom = roomsJoined.firstOrNull() ?: return
        val index =
            roomsForPreloading.indexOfFirst { it.roomId == currentRoom.roomId }
        Log.d(tag, "switchRoomState, index: $index, connectionsJoined:$roomsJoined")
        Log.d(tag, "switchRoomState, roomsForPreloading: $roomsForPreloading")

        // joined房间的上下两个房间
        val connPreLoaded = mutableListOf<VideoLoader.RoomInfo>()
        for (i in (index - 1)..(index + 3 / 2)) {
            if (i == index) {
                continue
            }
            // workaround
            if (size == 0) {
                return
            }
            val realIndex = (if (i < 0) size + i else i) % size
            if (realIndex < 0 || realIndex >= size) {
                continue
            }
            val conn = roomsForPreloading[realIndex]
            if (roomsJoined.any { it.roomId == conn.roomId }) {
                continue
            }
            if (videoSwitcher.getRoomState(conn.roomId, localUid) != AnchorState.PRE_JOINED) {
                Log.d(tag, "switchRoomState, getRoomState: $roomsForPreloading")
                videoSwitcher.preloadAnchor(conn.anchorList, localUid)
                conn.anchorList.forEach {
                    if (needPreJoin && currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                        videoSwitcher.switchAnchorState(AnchorState.PRE_JOINED, it, localUid)
                    }
                }
            }
            connPreLoaded.add(conn)
        }

        Log.d(tag, "switchRoomState, connPreLoaded: $connPreLoaded ")
        // 非preJoin房间需要退出频道
        roomsForPreloading.forEach { room ->
            if (needPreJoin && videoSwitcher.getRoomState(room.roomId, localUid) == AnchorState.PRE_JOINED && connPreLoaded.none {room.roomId == it.roomId}) {
                Log.d(tag, "switchRoomState, remove: $room ")
                room.anchorList.forEach {
                    if (currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                        videoSwitcher.switchAnchorState(AnchorState.IDLE, it, localUid)
                    }
                }
            } else if (!needPreJoin && videoSwitcher.getRoomState(room.roomId, localUid) != AnchorState.IDLE && roomsJoined.none {room.roomId == it.roomId}) {
                Log.d(tag, "switchRoomState, remove: $room ")
                room.anchorList.forEach {
                    if (currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                        videoSwitcher.switchAnchorState(AnchorState.IDLE, it, localUid)
                    }
                }
            }
        }
    }

    private fun pageLoaded(position: Int, roomInfo: VideoLoader.RoomInfo) {
        onPageLoaded(position)
        if (videoScrollMode == AGSlicingType.END_SCROLL) {
            roomInfo.anchorList.forEach { anchorInfo ->
                onRequireRenderVideo(position, anchorInfo)?.let {
                    videoSwitcher.renderVideo(
                        anchorInfo,
                        localUid,
                        it
                    )
                }
            }
        }
    }
}
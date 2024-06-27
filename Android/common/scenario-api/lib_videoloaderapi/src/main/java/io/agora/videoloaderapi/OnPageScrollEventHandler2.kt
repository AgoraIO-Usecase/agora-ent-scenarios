package io.agora.videoloaderapi

import android.os.Handler
import android.os.Looper
import android.util.SparseArray
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtc2.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * 直播间 item 触摸事件
 * @param mRtcEngine RtcEngineEx 对象
 * @param localUid 观众 uid
 * @param needPreJoin 房间是否需要 preJoin
 * @param videoScrollMode 视频出图模式
 */
abstract class OnPageScrollEventHandler2 constructor(
    private val layoutManager: LinearLayoutManager,
    private val mRtcEngine: RtcEngineEx,
    private val localUid: Int,
    private val needPreJoin: Boolean,
    private val videoScrollMode: AGSlicingType
) : RecyclerView.OnScrollListener() {
    private val tag = "[VideoLoader]Scroll2"
    private val videoLoader by lazy { VideoLoader.getImplInstance(mRtcEngine) }
    private val roomList = SparseArray<VideoLoader.RoomInfo>()

    fun cleanCache() {
        mainHandler.removeCallbacksAndMessages(null)
        roomsForPreloading.clear()
        roomsJoined.clear()
    }

    // ViewPager2.OnPageChangeCallback()
    private val POSITION_NONE = -1
    private var currLoadPosition = POSITION_NONE
    private var preLoadPosition = POSITION_NONE
    private var lastVisibleItemCount = 0

    private val roomsForPreloading = Collections.synchronizedList(mutableListOf<VideoLoader.RoomInfo>())
    private val roomsJoined = Collections.synchronizedList(mutableListOf<VideoLoader.RoomInfo>())

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private var isFirst = true

    fun onRoomCreated(position: Int, info: VideoLoader.RoomInfo) {
        VideoLoader.videoLoaderApiLog(tag, "onRoomCreated, position: $position, info:$info")
        roomList.put(position, info)
        if (isFirst) {
            isFirst = false
            info.anchorList.forEach {
                videoLoader.switchAnchorState(AnchorState.JOINED, it, localUid)
            }
            mainHandler.postDelayed({
                roomsJoined.add(info)
                pageLoaded(position, info)
                preJoinRooms()
                preLoadPosition = POSITION_NONE
                currLoadPosition = layoutManager.findFirstVisibleItemPosition()
                lastVisibleItemCount = layoutManager.childCount
            }, 200)
        }
    }

    fun updateRoomList(list: ArrayList<VideoLoader.RoomInfo>) {
        roomsForPreloading.addAll(list)
    }

    fun updateRoomInfo(position: Int, info: VideoLoader.RoomInfo) {
        VideoLoader.videoLoaderApiLog(tag, "updateRoomInfo, position: $position, info:$info")
        if (info.roomId != roomList[position].roomId) return
        val oldAnchorList = roomList[position].anchorList
        val newAnchorList = info.anchorList
        newAnchorList.forEach { newInfo ->
            videoLoader.switchAnchorState(AnchorState.JOINED, newInfo, localUid)
            onRequireRenderVideo(position, newInfo)?.let { canvas ->
                videoLoader.renderVideo(
                    newInfo,
                    localUid,
                    canvas
                )
            }
        }

        oldAnchorList.forEach { oldInfo ->
            if (newAnchorList.none { new -> new.channelId == oldInfo.channelId }) {
                videoLoader.switchAnchorState(AnchorState.IDLE, oldInfo, localUid)
            }
        }

        val roomInfo = roomsForPreloading.filter { it.roomId == info.roomId }.getOrNull(0) ?: return
        val index = roomsForPreloading.indexOf(roomInfo)
        roomsForPreloading[index] = info
        roomList[position] = info
        // 这里需要更新curr position
        currLoadPosition = layoutManager.findFirstVisibleItemPosition()
    }

    fun getCurrentRoomPosition(): Int {
        return currLoadPosition
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)

        if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 滚动停止时
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount

            // 检查哪些item离开了视图
            for (i in currLoadPosition until currLoadPosition + lastVisibleItemCount) {
                if (i < firstVisibleItemPosition || i >= firstVisibleItemPosition + visibleItemCount) {
                    leaveRoom(roomList[i] ?: return)
                    onPageLeft(i)
                }
            }

            // 当前停留的页面
            if (currLoadPosition != firstVisibleItemPosition) {
                joinRoomAndStartAudio(roomList[firstVisibleItemPosition] ?: return)
                roomsJoined.add(roomList[firstVisibleItemPosition] ?: return)
                preJoinRooms()
                pageLoaded(firstVisibleItemPosition, roomList[firstVisibleItemPosition])
            }

            currLoadPosition = firstVisibleItemPosition
            lastVisibleItemCount = visibleItemCount
            preLoadPosition = POSITION_NONE
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        // 检查新的页面是否开始出现
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
        // 和上次第一个可见的item不同，认为是新的页面开始加载
        if (firstVisibleItemPosition != currLoadPosition && preLoadPosition != firstVisibleItemPosition) {
            // 下滑
            preLoadPosition = firstVisibleItemPosition
        } else if (lastVisibleItemPosition != currLoadPosition && preLoadPosition != lastVisibleItemPosition) {
            // 上滑
            preLoadPosition = lastVisibleItemPosition
        } else {
            return
        }

        joinRoomWithoutAudio(preLoadPosition, roomList[preLoadPosition] ?: return, localUid)
        onPageStartLoading(preLoadPosition)
    }

    // OnPageStateEventHandler
    abstract fun onPageStartLoading(position: Int)

    abstract fun onPageLoaded(position: Int)

    abstract fun onPageLeft(position: Int)

    abstract fun onRequireRenderVideo(position: Int, info: VideoLoader.AnchorInfo): VideoLoader.VideoCanvasContainer?

    // ------------------------ inner ---------------------------
    private fun joinRoomWithoutAudio(position: Int, roomInfo: VideoLoader.RoomInfo, uid: Int) {
        VideoLoader.videoLoaderApiLog(tag, "joinChannel roomInfo=$roomInfo")

        roomInfo.anchorList.forEach { anchorInfo ->
            videoLoader.switchAnchorState(AnchorState.JOINED_WITHOUT_AUDIO, anchorInfo, uid)
            if (videoScrollMode == AGSlicingType.VISIBLE) {
                onRequireRenderVideo(position, anchorInfo)?.let {
                    videoLoader.renderVideo(
                        anchorInfo,
                        localUid,
                        it
                    )
                }
            }

            // 打点
            mRtcEngine.startMediaRenderingTracingEx(RtcConnection(anchorInfo.channelId, localUid))
            (videoLoader as VideoLoaderImpl).getProfiler(anchorInfo.channelId, anchorInfo.anchorUid).perceivedStartTime = System.currentTimeMillis()
            (videoLoader as VideoLoaderImpl).getProfiler(anchorInfo.channelId, anchorInfo.anchorUid).reportExt = mutableMapOf("videoScrollMode" to videoScrollMode.value, "needPreJoin" to needPreJoin)
        }
    }

    private fun joinRoomAndStartAudio(roomInfo: VideoLoader.RoomInfo) {
        roomInfo.anchorList.forEach {
            videoLoader.switchAnchorState(AnchorState.JOINED, it, localUid)
        }
    }

    private fun leaveRoom(roomInfo: VideoLoader.RoomInfo) {
        VideoLoader.videoLoaderApiLog(tag, "switchRoomState, hideChannel: $roomInfo")
        roomsJoined.removeIf { it.roomId == roomInfo.roomId }
        val currentRoom = roomsJoined.firstOrNull() ?: return
        roomInfo.anchorList.forEach {
            if (needPreJoin && currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                videoLoader.switchAnchorState(AnchorState.PRE_JOINED, it, localUid)
            } else if (currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                videoLoader.switchAnchorState(AnchorState.IDLE, it, localUid)
            }
        }
    }

    private fun preJoinRooms() {
        val size = roomsForPreloading.size
        val currentRoom = roomsJoined.firstOrNull() ?: return
        val index =
            roomsForPreloading.indexOfFirst { it.roomId == currentRoom.roomId }
        VideoLoader.videoLoaderApiLog(tag, "switchRoomState, index: $index, connectionsJoined:$roomsJoined")
        VideoLoader.videoLoaderApiLog(tag, "switchRoomState, roomsForPreloading: $roomsForPreloading")

        // joined房间的上下两个房间
        val roomPreLoaded = mutableListOf<VideoLoader.RoomInfo>()
        val anchorPreJoined = mutableListOf<VideoLoader.AnchorInfo>()
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
            val roomInfo = roomsForPreloading[realIndex]
            if (roomsJoined.any { it.roomId == roomInfo.roomId }) {
                continue
            }
            if (videoLoader.getAnchorState(roomInfo.roomId, localUid) != AnchorState.PRE_JOINED) {
                VideoLoader.videoLoaderApiLog(tag, "getAnchorState $roomsForPreloading")
                videoLoader.preloadAnchor(roomInfo.anchorList, localUid)
                roomInfo.anchorList.forEach {
                    if (needPreJoin && currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                        videoLoader.switchAnchorState(AnchorState.PRE_JOINED, it, localUid)
                        anchorPreJoined.add(it)
                    }
                }
            }
            roomPreLoaded.add(roomInfo)
        }

        VideoLoader.videoLoaderApiLog(tag, "switchRoomState, connPreLoaded: $roomPreLoaded")
        // 非preJoin房间需要退出频道
        roomsForPreloading.forEach { room ->
            if (needPreJoin && videoLoader.getAnchorState(room.roomId, localUid) == AnchorState.PRE_JOINED && roomPreLoaded.none {room.roomId == it.roomId}) {
                VideoLoader.videoLoaderApiLog(tag, "switchRoomState, remove: $room")
                room.anchorList.forEach {
                    if (currentRoom.anchorList.none { joined -> joined.channelId == it.channelId } && anchorPreJoined.none { preJoined -> preJoined.channelId == it.channelId }) {
                        videoLoader.switchAnchorState(AnchorState.IDLE, it, localUid)
                    }
                }
            } else if (!needPreJoin && videoLoader.getAnchorState(room.roomId, localUid) != AnchorState.IDLE && roomsJoined.none {room.roomId == it.roomId}) {
                VideoLoader.videoLoaderApiLog(tag, "switchRoomState, remove: $room")
                room.anchorList.forEach {
                    if (currentRoom.anchorList.none { joined -> joined.channelId == it.channelId }) {
                        videoLoader.switchAnchorState(AnchorState.IDLE, it, localUid)
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
                    videoLoader.renderVideo(
                        anchorInfo,
                        localUid,
                        it
                    )
                }
            }
        }
    }
}
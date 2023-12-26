package io.agora.scene.show.videoLoaderAPI

import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.rtc2.RtcEngineEx

/**
 * 直播间列表滚动事件
 * @param mRtcEngine RtcEngineEx对象
 * @param localUid 观众uid
 */
abstract class OnRoomListScrollEventHandler constructor(
    private val mRtcEngine: RtcEngineEx,
    private val localUid: Int
): RecyclerView.OnScrollListener() {
    private val tag = "OnRoomListScrollEventHandler"
    private val roomList = ArrayList<VideoLoader.RoomInfo>()

    fun updateRoomList(list: ArrayList<VideoLoader.RoomInfo>) {
        roomList.addAll(list)
        preloadChannels()
    }

    // RecyclerView.OnScrollListener
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (newState == RecyclerView.SCROLL_STATE_IDLE) { // 停止状态
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItem = layoutManager.findFirstVisibleItemPosition() // 第一个可见 item
            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()  // 最后一个可见 item
            Log.d("RoomListActivity", "firstVisible $firstVisibleItem, lastVisible $lastVisibleItem")
            val firstPreloadPosition = if (firstVisibleItem - 7 < 0) 0 else firstVisibleItem - 7
            val lastPreloadPosition = if (firstPreloadPosition + 19 >= roomList.size)
                roomList.size - 1 else firstPreloadPosition + 19
            preloadChannels(firstPreloadPosition, lastPreloadPosition)
        }
    }

    // ------------------------ inner ------------------------
    // preload房间列表内前20个房间
    private fun preloadChannels() {
        if (roomList.isNotEmpty()) {
            // sdk 最多 preload 20个频道，超过 20 个，sdk 内部维护最新的 20 个频道预加载
            roomList.take(20).forEach { room ->
                val info = room.anchorList[0]
                val ret = mRtcEngine.preloadChannel(info.token, info.channelId, localUid)
                Log.d(tag, "call rtc sdk preloadChannel ${info.channelId} ret:$ret")
            }
        }
    }

    // preload房间列表内指定位置区域的房间
    private fun preloadChannels(from: Int, to: Int) {
        if (roomList.isNotEmpty()) {
            val size = roomList.size
            for (i in from until to + 1) {
                if (i >= size) return
                val info = roomList[i].anchorList[0]
                val ret = mRtcEngine.preloadChannel(info.token, info.channelId, localUid)
                Log.d(tag, "call rtc sdk preloadChannel ${info.channelId} ret:$ret")
            }
        }
    }
}
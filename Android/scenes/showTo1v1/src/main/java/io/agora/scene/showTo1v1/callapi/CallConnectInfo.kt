package io.agora.scene.showTo1v1.callapi

import android.os.Handler
import android.os.Looper

enum class CallConnectCostType(val value: String) {
    RemoteUserRecvCall("remoteUserRecvCall"),       //主叫呼叫成功，收到呼叫成功表示已经送达对端(被叫)
    AcceptCall("acceptCall"),                       //主叫收到被叫接受呼叫(onAccept)/被叫点击接受(accept)
    LocalUserJoinChannel("localUserJoinChannel"),   //本地用户加入频道
    RemoteUserJoinChannel("remoteUserJoinChannel"), //远端用户加入频道
    RecvFirstFrame("recvFirstFrame")                //收到对端首帧
}

class CallConnectInfo {
    // 开始获取视频流的时间
    var startRetrieveFirstFrame: Long? = null
        private set

    // 是否获取到对端视频首帧
    var isRetrieveFirstFrame: Boolean = false

    // 呼叫的session id
    var callId: String = ""

    // 呼叫中的频道名
    var callingRoomId: String? = null

    // 呼叫中的远端用户
    var callingUserId: Int? = null

    /// 本地是否已经同意
    var isLocalAccepted: Boolean = false

    // 呼叫开始的时间
    private var _callTs: Long? = null
    var callTs: Long?
        get() = _callTs
        set(value) {
            _callTs = value
            callCostMap.clear()
        }

    val callCostMap = mutableMapOf<String, Long>()

    // 发起呼叫的定时器，用来处理超时
    private val mHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
        set(value) {
            val oldVlaue = field
            field = value
            oldVlaue?.let { mHandler.removeCallbacks(it) }
        }

    fun scheduledTimer(runnable: Runnable?, time: Long = 0) {
        val oldRunnable = timerRunnable
        if (oldRunnable != null) {
            mHandler.removeCallbacks(oldRunnable)
            timerRunnable = null
        }
        if (runnable != null) {
            timerRunnable = runnable
            mHandler.postDelayed(runnable, time)
        }
    }

    fun clean() {
        scheduledTimer(null)
        callingRoomId = null
        callingUserId = null
        callTs = null
        callId = ""
        isRetrieveFirstFrame = false
        startRetrieveFirstFrame = null
        isLocalAccepted = false
    }

    fun set(userId: Int, roomId: String, callId: String? = null, isLocalAccepted: Boolean = false) {
        this.callingUserId = userId
        this.callingRoomId = roomId
        this.isLocalAccepted = isLocalAccepted
        if (callId != null) {
            this.callId = callId
        }
        if (callTs == null) {
            callTs = System.currentTimeMillis()
        }
        if (startRetrieveFirstFrame == null) {
            startRetrieveFirstFrame = System.currentTimeMillis()
        }
    }
}
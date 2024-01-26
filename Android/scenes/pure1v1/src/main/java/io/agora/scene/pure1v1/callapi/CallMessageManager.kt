package io.agora.scene.pure1v1.callapi

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.agora.rtm.*
import io.agora.rtm.RtmConstants.RtmChannelType
import io.agora.scene.pure1v1.callapi.extension.getCostMilliseconds
import org.json.JSONObject

/// 回执的消息队列对象
private class CallQueueInfo {
    val TAG = "CALL_QUEUE_LOG"
    var toUserId: String = ""
    var messageId: Int = 0
    var messageInfo: Map<String, Any>? = null

    var checkReceiptsFail: ((CallQueueInfo) -> Unit)? = null

    private val createDate = System.currentTimeMillis()
    var retryTimes: Int = 3
    private val mHandler = Handler(Looper.getMainLooper())
    private var retryRunnable: Runnable? = null
        set(value) {
            field?.let { mHandler.removeCallbacks(it) }
            field = value
            field?.let { mHandler.postDelayed(it, 3000) }
        }

    fun checkReceipt() {
        retryRunnable = Runnable {
            retryTimes -= 1
            Log.d(TAG, "receipt timeout retry $retryTimes , message id: $messageId")
            checkReceiptsFail?.invoke(this@CallQueueInfo)
        }
    }
    init {
        Log.d(TAG, "CallQueueInfo init $messageId")
    }
    fun finish() {
        retryRunnable?.let { mHandler.removeCallbacks(it) }
        Log.d(TAG, "CallQueueInfo deinit $messageId cost: $createDate ms")
    }
}

interface CallMessageListener: RtmEventListener {
    fun onConnectionFail()
    fun debugInfo(message: String, logLevel: Int)
}

class CallMessageManager(
    private val config: CallConfig,
    private var listener: CallMessageListener? = null,
): RtmEventListener {

    private val TAG = "CALL_MSG_MANAGER"
    /** 回执的消息id */
    private val kReceiptsKey = "receipts"
    /** 回执到哪个房间，因为没有点对点，所以单点消息通过不同房间发送消息 */
    private val kReceiptsRoomIdKey = "receiptsRoomId"
    /** 发送的消息id */
    private val kMessageId = "messageId"

    private var rtmClient: RtmClient
    /** RTM是否已经登录 */
    private var isLoginedRtm = false

    /** 是否外部传入的rtm，如果是则不需要手动logout */
    private var isExternalRtmClient = false

    private var prepareConfig: PrepareConfig? = null
    /** 消息id */
    private var messageId: Int = 0
    /** 待接收回执队列，保存没有接收到回执或者等待未超时的消息 */
//    private var receiptsQueue: MutableList<CallQueueInfo>? = mutableListOf()

    private val mHandler = Handler(Looper.getMainLooper())
    init {
        val rtm = config.rtmClient
        if (rtm != null) {
            //如果外部传入rtmclient，默认登陆成功
            isLoginedRtm = true
            isExternalRtmClient = true
            rtmClient = rtm
        } else {
            rtmClient = _createRtmClient()
        }
        rtmClient.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        callMessagePrint("init-- CallMessageManager ")
    }

    private fun _createRtmClient(): RtmClient {
        val rtmConfig = RtmConfig.Builder(config.appId, config.userId.toString()).build()
        if (rtmConfig.userId.isEmpty()) {
            callMessagePrint("userId is empty", 2)
        }
        if (rtmConfig.appId.isEmpty()) {
            callMessagePrint("appId is empty", 2)
        }
        var rtmClient: RtmClient? = null
        try {
            rtmClient = RtmClient.create(rtmConfig)
        } catch (e: Exception) {
            callMessagePrint("create rtm client fail: ${e.message}", 2)
        }
        return rtmClient!!
    }

    private fun _sendMessage(userId: String, message: Map<String, Any>, completion:((AGError?)->Unit)?) {
        if (userId.isEmpty()) {
            completion?.invoke(AGError("send message fail! roomId is empty", -1))
            return
        }
        val msgId = message[kMessageId] as? Int ?: 0
        val json = Gson().toJson(message)
        val options = PublishOptions()
        options.setChannelType(RtmChannelType.USER)
        val startTime = System.currentTimeMillis()
        callMessagePrint("_sendMessage[$msgId] to '$userId', message: $message")
        rtmClient.publish(userId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                callMessagePrint("_sendMessage[$msgId] publish cost ${startTime.getCostMilliseconds()} ms")
                runOnUiThread { completion?.invoke(null) }
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                val msg = errorInfo.errorReason
                val code = RtmConstants.RtmErrorCode.getValue(errorInfo.errorCode)
                callMessagePrint("_sendMessage[$msgId]: fail: $msg cost: ${startTime.getCostMilliseconds()} ms", 1)
                runOnUiThread { completion?.invoke(AGError(msg, code)) }
            }
        })
    }

    private fun loginRTM(rtmClient: RtmClient, token: String, completion: (ErrorInfo?) -> Unit) {
        if (isLoginedRtm) {
            completion(null)
            return
        }
        rtmClient.logout(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
        callMessagePrint("will login")
        rtmClient.login(token, object : ResultCallback<Void?> {
            override fun onSuccess(p0: Void?) {
                callMessagePrint("login completion")
                isLoginedRtm = true
                runOnUiThread { completion(null) }
            }

            override fun onFailure(p0: ErrorInfo?) {
                callMessagePrint("login completion: ${p0?.errorCode}")
                isLoginedRtm = false
                runOnUiThread { completion(p0) }
            }
        })
    }
    // MARK: - Public
    fun deinitialize() {
        rtmClient.removeEventListener(this)
        if (!isExternalRtmClient) {
            rtmClient.logout(object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {}
                override fun onFailure(errorInfo: ErrorInfo?) {}
            })
            RtmClient.release()
        }
//        receiptsQueue?.forEach { it.finish() }
//        receiptsQueue = null
    }
    /** 根据配置初始化RTM
     * @param prepareConfig: <#prepareConfig description#>
     * @param completion: <#completion description#>
     */
    fun initialize(prepareConfig: PrepareConfig, completion: (AGError?) -> Unit) {
        callMessagePrint("initialize")
        this.prepareConfig = prepareConfig
        rtmClient.addEventListener(this)
        val rtmToken = prepareConfig.rtmToken
        if (rtmToken.isEmpty()) {
            val reason = "RTM Token is Empty"
            completion(AGError(reason, -1))
            return
        }
        val rtmClient = this.rtmClient
        if (!isLoginedRtm) {
            loginRTM(rtmClient, rtmToken) { err ->
                if (err != null) {
                    val errorCode = RtmConstants.RtmErrorCode.getValue(err.errorCode)
                    completion.invoke(AGError(err.errorReason, errorCode))
                    return@loginRTM
                }
                completion.invoke(null)
            }
        } else {
            completion.invoke(null)
        }
    }

    /// 更新RTM token
    /// - Parameter rtmToken: <#rtmToken description#>
    fun renewToken(rtcToken: String, rtmToken: String) {
        if (!isLoginedRtm) {
            val prepareCfg = prepareConfig
            if (prepareCfg != null) {
                //没有登陆成功，但是需要自动登陆，可能是初始token问题，这里重新initialize
                callMessagePrint("renewToken need to reinit")
                rtmClient.logout(object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {}
                    override fun onFailure(errorInfo: ErrorInfo?) {}
                })
                initialize(prepareCfg) { _ ->
                }
            }
            return
        }
        this.prepareConfig?.rtcToken = rtcToken
        this.prepareConfig?.rtmToken = rtmToken
        rtmClient.renewToken(rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                callMessagePrint("rtm renewToken")
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
            }
        })
    }

    /** 发送频道消息
     * @param userId: 往哪个用户发送消息
     * @param fromUserId: 哪个频道发送的，用来给对端发送回执
     * @param message: 发送的消息字典
     * @param completion: <#completion description#>
     */
    fun sendMessage(userId: String, message: Map<String, Any>, completion: ((AGError?)-> Unit)?) {
        if (userId.isEmpty() || userId == "0") {
            val errorStr = "sendMessage fail, invalid userId[$userId]"
            callMessagePrint(errorStr)
            completion?.invoke(AGError(errorStr, -1))
            return
        }
        messageId += 1
        messageId %= Int.MAX_VALUE
        val map = message.toMutableMap()
        map[kMessageId] = messageId
//        map[kReceiptsRoomIdKey] = fromUserId
//        require(fromUserId.isNotEmpty()) { "kReceiptsRoomIdKey is empty" }
        _sendMessage(userId, map, completion)
    }

    //MARK: AgoraRtmClientDelegate
    override fun onConnectionStateChanged(
        channelName: String?,
        state: RtmConstants.RtmConnectionState?,
        reason: RtmConstants.RtmConnectionChangeReason?
    ) {
        callMessagePrint("rtm connectionStateChanged: $state reason: $reason")
        runOnUiThread {
            if (reason == RtmConstants.RtmConnectionChangeReason.TOKEN_EXPIRED) {
                listener?.onTokenPrivilegeWillExpire(channelName)
            } else if (reason == RtmConstants.RtmConnectionChangeReason.LOST) {
                listener?.onConnectionFail()
            }
        }
    }
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        callMessagePrint("rtm onTokenPrivilegeWillExpire[${channelName ?: "nil"}]")
        runOnUiThread {
            listener?.onTokenPrivilegeWillExpire(channelName)
        }
    }

    override fun onMessageEvent(event: MessageEvent?) {
//        val message = event?.message?.data as? ByteArray ?: return
//        val jsonString = String(message, Charsets.UTF_8)
//        callMessagePrint("on event message: $jsonString")
//        val map = jsonStringToMap(jsonString)
//        val messageId = map[kMessageId] as? Int
//        val receiptsRoomId = map[kReceiptsRoomIdKey] as? String
//        val receiptsId = map[kReceiptsKey] as? Int
//        if (receiptsRoomId != null && messageId != null) {
//            _sendReceipts(receiptsRoomId, messageId)
//        } else if (receiptsId != null) {
//            receiptsQueue?.let { queue ->
//                queue.filter {it.messageId == receiptsId}.forEach { it.finish() }
//                queue.removeAll { it.messageId == receiptsId }
//            }
//            callMessagePrint("recv receipts: $receiptsId msgIds${receiptsQueue?.map { it.messageId }}")
//        } else {
//            callMessagePrint("on event message parse fail, ${event.message.type} ${event.message.data}", 1)
//        }
        runOnUiThread {
            listener?.onMessageEvent(event)
        }
    }
    override fun onPresenceEvent(event: PresenceEvent?) {}
    override fun onTopicEvent(event: TopicEvent?) {}
    override fun onLockEvent(event: LockEvent?) {}
    override fun onStorageEvent(event: StorageEvent?) {}
    private fun jsonStringToMap(jsonString: String): Map<String, Any> {
        val json = JSONObject(jsonString)
        val map = mutableMapOf<String, Any>()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = json.get(key)
        }
        return map
    }

    private fun callMessagePrint(message: String, logLevel: Int = 0) {
        val tag = "[MessageManager]"
        listener?.debugInfo("$tag$message)", logLevel)
        if (listener == null) {
            Log.d(TAG, "[CallApi]$tag $message)")
        }
    }
    private fun runOnUiThread(runnable: Runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            runnable.run()
        } else {
            mHandler.post(runnable)
        }
    }
}



package io.agora.scene.showTo1v1.callAPI

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.agora.rtm.*
import org.json.JSONObject

/// 回执的消息队列对象
private class CallQueueInfo {

    val TAG = "CALL_QUEUE_LOG"
    var messageId: Int = 0
    var messageInfo: Map<String, Any>? = null
    var callback: ((AGError?) -> Unit)? = null

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
    /** 回执没有收到*/
    fun onMissReceipts(message: Map<String, Any>)
    fun onConnectionFail()
    fun debugInfo(message: String)
    fun debugWarning(message: String)
}

class CallMessageManager(
    private val config: CallConfig,
    private val rtmClient: RtmClient,
    private var listener: CallMessageListener? = null,
): RtmEventListener {

    private val TAG = "CALL_MSG_MANAGER"

    /** 回执的消息id */
    private val kReceiptsKey = "receipts"

    /** 回执到哪个房间，因为没有点对点，所以单点消息通过不同房间发送消息 */
    private val kReceiptsRoomIdKey = "receiptsRoomId"

    /** 发送的消息id */
    private val kMessageId = "messageId"
    // RTM是否已经登录
    private var isLoginedRTM = false
    // RTM 是否已经订阅频道
    var isSubscribedRTM = false
        private set

    private var prepareConfig: PrepareConfig? = null
    private var tokenConfig: CallTokenConfig? = null
    // 消息id
    private var messageId: Int = 0
    // 待接收回执队列，保存没有接收到回执或者等待未超时的消息
    private var receiptsQueue: MutableList<CallQueueInfo>? = mutableListOf()

    private val mHandler = Handler(Looper.getMainLooper())
    init {
        if (rtmClient == config.rtmClient) {
            //如果外部传入rtmclient，默认登陆成功
            isLoginedRTM = true
        }
        callMessagePrint("init-- CallMessageManager ")
    }

    fun rtmDeinitialize() {
        rtmClient.removeEventListener(this)
        callMessagePrint("unsubscribe[${config.userId}]")
        receiptsQueue?.forEach { it.finish() }
        receiptsQueue = null
        rtmClient.unsubscribe(config.userId.toString(), object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
    }

    fun rtmInitialize(prepareConfig: PrepareConfig, tokenConfig: CallTokenConfig?, completion: (AGError?) -> Unit) {
        callMessagePrint("_rtmInitialize")
        this.prepareConfig = prepareConfig
        this.tokenConfig = tokenConfig
        rtmClient.addEventListener(this)
        val rtmToken = tokenConfig?.rtmToken ?: run {
            val reason = "RTM Token is Empty"
            completion(AGError(reason, -1))
            return
        }
        val rtmClient = this.rtmClient
        if (prepareConfig.autoLoginRTM && !isLoginedRTM) {
            loginRTM(rtmClient, rtmToken) { err ->
                if (err != null) {
                    val errorCode = RtmConstants.RtmErrorCode.getValue(err.errorCode)
                    completion.invoke(AGError(err.errorReason, errorCode))
                }
                rtmInitialize(prepareConfig, tokenConfig, completion)
            }
        } else if (isLoginedRTM && prepareConfig.autoSubscribeRTM) {
            _subscribeRTM(config.userId.toString()) { error ->
                if (error != null) {
                    completion.invoke(error)
                } else {
                    isSubscribedRTM = true
                    completion.invoke(null)
                }
            }
        } else {
            completion.invoke(null)
        }
    }

    /// 更新RTM token
    /// - Parameter rtmToken: <#rtmToken description#>
    fun renewToken(tokenConfig: CallTokenConfig) {
        if (!isLoginedRTM) {
            val prepareConfig = prepareConfig
            if (prepareConfig != null && prepareConfig.autoLoginRTM) {
                //没有登陆成功，但是需要自动登陆，可能是初始token问题，这里重新initialize
                callMessagePrint("renewToken need to reinit")
                rtmClient.logout(object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {}
                    override fun onFailure(errorInfo: ErrorInfo?) {}
                })
                rtmInitialize(prepareConfig, tokenConfig) { _ ->
                }
            }
            return
        }
        this.tokenConfig = tokenConfig
        rtmClient.renewToken(tokenConfig.rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                callMessagePrint("rtm renewToken")
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
            }
        })
    }

    /** 发送频道消息
     * @param userId: 往哪个用户发送消息
     * @param fromRoomId: 哪个频道发送的，用来给对端发送回执
     * @param message: 发送的消息字典
     * @param completion: <#completion description#>
     */
    fun sendMessage(userId: String, fromUserId: String, message: Map<String, Any>, completion: ((AGError?)-> Unit)?) {
        messageId += 1
        messageId %= Int.MAX_VALUE
        val map = message.toMutableMap()
        val msgId = messageId
        map[kMessageId] = msgId
        map[kReceiptsRoomIdKey] = fromUserId
        require(fromUserId.isNotEmpty()) { "kReceiptsRoomIdKey is empty" }
        _sendMessage(userId, map, completion)
    }

    /** 根据策略订阅频道消息
     *  @param userId: 频道号
     *  @param completion: <#completion description#>
     */
    private fun _subscribeRTM(userId: String, completion: ((AGError?) -> Unit)?) {
        val options = SubscribeOptions()
        options.withMessage = true
        options.withMetadata = false
        options.withPresence = false
        callMessagePrint("will subscribe[$userId] message: ${options.withMessage} presence: ${options.withPresence}")
        rtmClient.unsubscribe(userId, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
        rtmClient.subscribe(userId, options, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                callMessagePrint("subscribe[$userId] finished = success")
                runOnUiThread { completion?.invoke(null) }
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                runOnUiThread { completion?.invoke(AGError(msg, errorCode)) }
            }
        })
    }

    /** 发送回执消息
     * @param roomId: 回执消息发往的频道
     * @param messageId: 回执的消息id
     * @param completion: <#completion description#>
     */
    private fun _sendReceipts(roomId: String, messageId: Int, completion: ((AGError?)-> Unit)? = null) {
        val message = mutableMapOf<String, Any>()
        message[kReceiptsKey] = messageId
        callMessagePrint("_sendReceipts to $roomId, message: $message")
        val json = Gson().toJson(message)
        val options = PublishOptions()
        rtmClient.publish(roomId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                callMessagePrint("_sendReceipts cost ${System.currentTimeMillis()} ms")
                runOnUiThread { completion?.invoke(null) }
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                runOnUiThread { completion?.invoke(AGError(msg, errorCode)) }
            }
        })
    }

    private fun _sendMessage(userId: String, message: Map<String, Any>, completion:((AGError?)->Unit)?) {
        if (userId.isEmpty()) {
            completion?.invoke(AGError("send message fail! roomId is empty", -1))
            return
        }
        callMessagePrint("_sendMessage to '$userId', message: $message")
        val msgId = message[kMessageId] as? Int ?: 0
        val json = Gson().toJson(message)
        val options = PublishOptions()
        val startTime = System.currentTimeMillis()
        rtmClient.publish(userId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                callMessagePrint("publish cost ${System.currentTimeMillis() - startTime} ms")
                runOnUiThread { completion?.invoke(null) }
                //发送成功检查回执，没收到则重试
                receiptsQueue?.let { queue ->
                    queue.firstOrNull { it.messageId == msgId }?.let {
                        it.checkReceipt()
                        return
                    }
                    val receiptInfo = CallQueueInfo()
                    receiptInfo.messageId = msgId
                    receiptInfo.messageInfo = message
                    receiptInfo.callback = completion
                    receiptInfo.checkReceiptsFail = { info ->
                        if (info.retryTimes > 0) {
                            _sendMessage(userId, message, completion = completion)
                        } else {
                            val messageInfo = info.messageInfo ?: emptyMap()
                            queue.filter {it.messageId == msgId}.forEach { it.finish() }
                            queue.removeAll { it.messageId == msgId }
                            listener?.onMissReceipts(messageInfo)
                        }
                    }
                    queue.add(receiptInfo)
                    receiptInfo.checkReceipt()
                }
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val code = errorInfo?.errorCode?.ordinal ?: -1
                callWarningPrint("_sendMessage: fail: $msg")
                runOnUiThread { completion?.invoke(AGError(msg, code)) }
            }
        })
    }
    private fun loginRTM(rtmClient: RtmClient, token: String, completion: (ErrorInfo?) -> Unit) {
        if (isLoginedRTM) {
            completion(null)
            return
        }
        callMessagePrint("will login")
        rtmClient.logout(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
        rtmClient.login(token, object : ResultCallback<Void?> {
            override fun onSuccess(p0: Void?) {
                callMessagePrint("login completion")
                isLoginedRTM = true
                runOnUiThread { completion(null) }
            }

            override fun onFailure(p0: ErrorInfo?) {
                callMessagePrint("login completion: ${p0?.errorCode}")
                isLoginedRTM = false
                runOnUiThread { completion(p0) }
            }
        })
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
        val message = event?.message?.data as? ByteArray ?: return
        val jsonString = String(message, Charsets.UTF_8)
        callMessagePrint("on event message: $jsonString")
        val map = jsonStringToMap(jsonString)
        val messageId = map[kMessageId] as? Int
        val receiptsRoomId = map[kReceiptsRoomIdKey] as? String
        val receiptsId = map[kReceiptsKey] as? Int
        if (receiptsRoomId != null && messageId != null) {
            _sendReceipts(receiptsRoomId, messageId)
        } else if (receiptsId != null) {
            callMessagePrint("recv receipts $receiptsId")
            receiptsQueue?.let { queue ->
                queue.filter {it.messageId == receiptsId}.forEach { it.finish() }
                queue.removeAll { it.messageId == receiptsId }
            }
        } else {
            callWarningPrint("on event message parse fail, ${event.message.type} ${event.message.data}")
        }
        runOnUiThread {
            listener?.onMessageEvent(event)
        }
    }

    override fun onPresenceEvent(event: PresenceEvent?) {
        runOnUiThread {
            listener?.onPresenceEvent(event)
        }
    }

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

    private fun callMessagePrint(message: String) {
        val tag = "[MessageManager]"
        listener?.debugInfo("$tag$message)")
        if (listener == null) {
            Log.d(TAG, "[CallApi]$tag $message)")
        }
    }

    private fun callWarningPrint(message: String) {
        val tag = "[MessageManager]"
        listener?.debugWarning("$tag$message")
        if (listener == null) {
            Log.d(TAG, "[CallApi][Warning]$tag$message")
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



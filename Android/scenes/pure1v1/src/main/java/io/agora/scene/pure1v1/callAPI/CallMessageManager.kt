package io.agora.scene.pure1v1.callAPI

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.agora.rtm2.*
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

interface CallMessageListener {
    /** 回执没有收到
     */
    fun onMissReceipts(message: Map<String, Any>)
}

class CallMessageManager(
    private val mContext: Context,
    private val config: CallConfig,
    private var listener: CallMessageListener? = null,
    private var rtmListener: RtmEventListener? = null,
): RtmEventListener {

    private val TAG = "CALL_MSG_MANAGER"

    /** 回执的消息id */
    private val kReceiptsKey = "receipts"

    /** 回执到哪个房间，因为没有点对点，所以单点消息通过不同房间发送消息 */
    private val kReceiptsRoomIdKey = "receiptsRoomId"

    /** 发送的消息id */
    private val kMessageId = "messageId"

    private var rtmClient = _createRtmClient()

    private var loginSuccess: ((ErrorInfo?)-> Unit)? = null
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
    private var receiptsQueue: MutableList<CallQueueInfo> = mutableListOf()

    private val mHandler = Handler(Looper.getMainLooper())

    fun logout() {
        Log.d(TAG, "logout")
        rtmClient.logout(object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
        RtmClient.release()
    }

    fun rtmInitialize(prepareConfig: PrepareConfig, tokenConfig: CallTokenConfig?, completion: (AGError?) -> Unit) {
        this.prepareConfig = prepareConfig
        this.tokenConfig = tokenConfig
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
    fun renewToken(rtmToken: String) {
        if (!isLoginedRTM) {
            val prepareConfig = prepareConfig
            if (prepareConfig != null && prepareConfig.autoJoinRTC) {
                Log.e(TAG, "renewToken need to reinit")
                rtmClient.logout(object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {}
                    override fun onFailure(errorInfo: ErrorInfo?) {}
                })
                rtmInitialize(prepareConfig, tokenConfig) { _ ->
                }
            }
            return
        }
        rtmClient.renewToken(rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
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

    private fun _createRtmClient(): RtmClient {
        val rtmConfig = RtmConfig()
        rtmConfig.userId = config.userId.toString()
        rtmConfig.appId = config.appId
        if (rtmConfig.userId.isEmpty()) {
            Log.e(TAG, "userId is empty")
        }
        if (rtmConfig.appId.isEmpty()) {
            Log.e(TAG, "appId is empty")
        }
        rtmConfig.eventListener = this
        return RtmClient.create(rtmConfig)
    }
    /** 根据策略订阅频道消息
     *  @param userId: 频道号
     *  @param completion: <#completion description#>
     */
    private fun _subscribeRTM(userId: String, completion: ((AGError?) -> Unit)?) {
        /*
         移除所有的presence，所有缓存由调用的业务服务器去控制
         订阅自己频道的message，用来收消息
         */
        val options = SubscribeOptions()
        options.withMessage = true
        options.withMetadata = false
        options.withPresence = false
        _subscribe(userId, options, completion)
    }

    /** 发送回执消息
     * @param roomId: 回执消息发往的频道
     * @param messageId: 回执的消息id
     * @param completion: <#completion description#>
     */
    private fun _sendReceipts(roomId: String, messageId: Int, completion: ((AGError?)-> Unit)? = null) {
        val message = mutableMapOf<String, Any>()
        message[kReceiptsKey] = messageId
        Log.d(TAG, "_sendReceipts to $roomId, message: $message")
        val json = Gson().toJson(message)
        val options = PublishOptions()
        rtmClient.publish(roomId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d(TAG, "_sendReceipts cost ${System.currentTimeMillis()} ms")
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                completion?.invoke(AGError(msg, errorCode))
            }
        })
    }

    private fun _sendMessage(userId: String, message: Map<String, Any>, completion:((AGError?)->Unit)?) {
        if (userId.isEmpty()) {
            completion?.invoke(AGError("send message fail! roomId is empty", -1))
            return
        }
        Log.d(TAG, "_sendMessage to '$userId', message: $message")
        val msgId = message[kMessageId] as? Int ?: 0
        val json = Gson().toJson(message)
        val options = PublishOptions()
        rtmClient.publish(userId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                completion?.invoke(null)
                receiptsQueue.firstOrNull { it.messageId == msgId }?.let {
                    it.checkReceipt()
                    return
                }
                val receiptInfo = CallQueueInfo()
                receiptInfo.messageId = msgId
                receiptInfo.messageInfo = message
                receiptInfo.callback = completion
                receiptInfo.checkReceiptsFail = { info ->
                    if (info.retryTimes > 0) {
                        Log.d(TAG, "retry send message ------------")
                        _sendMessage(userId, message, completion = completion)
                    } else {
                        val messageInfo = info.messageInfo ?: emptyMap()
                        Log.d(TAG, "get receipts fail, msg: $messageInfo")
                        receiptsQueue.filter {it.messageId == msgId}.forEach { it.finish() }
                        receiptsQueue.removeAll { it.messageId == msgId }
                        listener?.onMissReceipts(messageInfo)
                    }
                }
                receiptsQueue.add(receiptInfo)
                receiptInfo.checkReceipt()
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                completion?.invoke(AGError(msg, -1))
            }
        })
    }

    private fun _subscribe(channelName: String, option: SubscribeOptions, completion: ((AGError?) -> Unit)?) {
        Log.d(TAG, "will subscribe[$channelName] message: ${option.withMessage} presence: ${option.withPresence}")
        rtmClient.unsubscribe(channelName, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
            }
        })
        rtmClient.subscribe(channelName, option, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d(TAG, "subscribe[$channelName] finished = success")
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                Log.d(TAG, "subscribe[$channelName] finished = failed: $msg")
                completion?.invoke(AGError(msg, errorCode))
            }
        })
    }

    private fun loginRTM(rtmClient: RtmClient, token: String, completion: (ErrorInfo?) -> Unit) {
        if (isLoginedRTM) {
            completion(null)
            return
        }
        Log.d(TAG, "will login")
        loginSuccess = completion
        rtmClient.logout(object : ResultCallback<Void?> {
            override fun onSuccess(responseInfo: Void?) {}
            override fun onFailure(errorInfo: ErrorInfo?) {}
        })
        val ret = rtmClient.login(token, object : ResultCallback<Void?> {
            override fun onSuccess(p0: Void?) {
                Log.d(TAG, "login success")
                isLoginedRTM = true
                completion(null)
            }

            override fun onFailure(p0: ErrorInfo?) {
                Log.d(TAG, "login failed: ${p0?.errorCode}")
                isLoginedRTM = false
                completion(p0)
            }
        })

        Log.d(TAG, "login ret: $ret")
    }
    //MARK: AgoraRtmClientDelegate
    override fun onConnectionStateChange(channelName: String?,
                                         state: RtmConstants.RtmConnectionState?,
                                         reason: RtmConstants.RtmConnectionChangeReason?) {
        Log.d(TAG, "rtm connectionStateChanged: $state reason: $reason")
        if (reason == RtmConstants.RtmConnectionChangeReason.TOKEN_EXPIRED) {
            rtmListener?.onTokenPrivilegeWillExpire(channelName)
        }
    }
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        Log.d(TAG, "rtm onTokenPrivilegeWillExpire[${channelName ?: "nil"}]")
        rtmListener?.onTokenPrivilegeWillExpire(channelName)
    }

    override fun onMessageEvent(event: MessageEvent?) {
        val message = event?.message?.data as? ByteArray ?: return
        val jsonString = String(message, Charsets.UTF_8)
        Log.d(TAG, "on event message: $jsonString")
        val map = jsonStringToMap(jsonString)
        val messageId = map[kMessageId] as? Int
        val receiptsRoomId = map[kReceiptsRoomIdKey] as? String
        val receiptsId = map[kReceiptsKey] as? Int
        if (receiptsRoomId != null && messageId != null) {
            _sendReceipts(receiptsRoomId, messageId)
        } else if (receiptsId != null) {
            Log.d(TAG, "recv receipts $receiptsId")
            receiptsQueue.filter {it.messageId == receiptsId}.forEach { it.finish() }
            receiptsQueue.removeAll { it.messageId == receiptsId }
            Log.d(TAG, "receiptsQueue.removeAll $receiptsId ${receiptsQueue.count()}")
        }
        rtmListener?.onMessageEvent(event)
    }

    override fun onPresenceEvent(event: PresenceEvent?) {
        rtmListener?.onPresenceEvent(event)
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
}



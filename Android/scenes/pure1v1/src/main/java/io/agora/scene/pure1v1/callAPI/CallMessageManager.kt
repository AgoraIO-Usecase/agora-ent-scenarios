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

    private var snapshotDidRecv: (() -> Unit)? = null
    // RTM是否已经登录
    private var isLoginedRTM = false
    // RTM 是否已经订阅频道
    var isSubscribedRTM = false
        private set
    // 消息id
    private var messageId: Int = 0
    // 待接收回执队列，保存没有接收到回执或者等待未超时的消息
    private var receiptsQueue: MutableList<CallQueueInfo> = mutableListOf()

    private val mHandler = Handler(Looper.getMainLooper())

    fun rtmInitialize(prepareConfig: PrepareConfig, tokenConfig: CallTokenConfig?, completion: (AGError?) -> Unit) {
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
                mHandler.postDelayed({
                    rtmInitialize(prepareConfig, tokenConfig, completion)
                }, 200)
            }
        } else if (isLoginedRTM && prepareConfig.autoSubscribeRTM) {
            _subscribeRTM(tokenConfig) { error ->
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
        if (!isLoginedRTM) { return }
        rtmClient.renewToken(rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
            }
        })
    }

    /// 发送频道消息
    /// - Parameters:
    ///   - roomId: 往哪个频道发送消息
    ///   - fromRoomId: 哪个频道发送的，用来给对端发送回执
    ///   - message: 发送的消息字典
    ///   - retryCount: 重试次数
    ///   - completion: <#completion description#>
    fun sendMessage(roomId: String, fromRoomId: String, message: Map<String, Any>, retryCount: Int = 3,completion: ((AGError?)-> Unit)?) {
        messageId += 1
        messageId %= Int.MAX_VALUE
        val map = message.toMutableMap()
        val msgId = messageId
        map[kMessageId] = msgId
        map[kReceiptsRoomIdKey] = fromRoomId
        require(fromRoomId.isNotEmpty()) { "kReceiptsRoomIdKey is empty" }
        _sendMessage(roomId, map, retryCount, completion)
    }

    fun setPresenceState(attr:Map<String, Any>, retryCount: Int = 3, completion: ((AGError?) -> Unit)?) {
        if (config.mode != CallMode.ShowTo1v1) {
            completion?.invoke(AGError("can not be set presence in 'pure 1v1' mode", -1))
            return
        }
        val presence = rtmClient.presence ?: run {
            completion?.invoke(AGError("not presence", -1))
            return
        }
        val roomId = config.ownerRoomId ?: run  {
            completion?.invoke(AGError("ownerRoomId isEmpty", -1))
            return
        }
        fun _retry(): Boolean {
            if (retryCount <= 1) {
                return false
            }
            Handler(Looper.getMainLooper()).postDelayed({
                setPresenceState(attr, retryCount - 1, completion)
            }, 1000)
            return true
        }
        Log.d(TAG, "_setPresenceState: to $roomId, attr: $attr")
        val items = ArrayList<StateItem>()
        attr.forEach { e ->
            items.add(StateItem(e.key, e.value.toString()))
        }

        presence.setState(roomId, RtmConstants.RtmChannelType.MESSAGE, items, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                if (!_retry()) {
                    val msg = errorInfo?.errorReason ?: "error"
                    val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                    completion?.invoke(AGError(msg, errorCode))
                }
            }
        })
    }

    /// 清理presence信息
    /// - Parameters:
    ///   - keys: 需要清理的presence的key 数组
    ///   - completion: <#completion description#>
    fun removePresenceState(keys: ArrayList<String>, completion: ((AGError?) -> Unit)?) {
        val presence = rtmClient.presence ?: run {
            completion?.invoke(AGError("not presence", -1))
            return
        }
        val roomId = config.ownerRoomId ?: run  {
            completion?.invoke(AGError("ownerRoomId isEmpty", -1))
            return
        }
        Log.d(TAG, "_removePresenceState: to $roomId, attr: $keys")

        presence.removeState(roomId, RtmConstants.RtmChannelType.MESSAGE, keys, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                Log.e(TAG, "presence removeState $roomId finished: ${errorInfo?.errorCode}")
                val e = AGError(errorInfo?.errorReason ?: "error", RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode))
                completion?.invoke(e)
            }
        })
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
    /// 根据策略订阅频道消息
    /// - Parameters:
    ///   - roomId: 频道号
    ///   - completion: <#completion description#>
    private fun _subscribeRTM(tokenConfig: CallTokenConfig?, completion: ((AGError?) -> Unit)?) {
        val roomId = if (config.mode == CallMode.ShowTo1v1) {
            if (config.role == CallRole.CALLER) tokenConfig?.roomId else config.ownerRoomId
        } else {
            tokenConfig?.roomId
        }
        roomId ?: run {
            completion?.invoke(AGError("channelName is Empty", -1))
            return
        }
        /*
         纯1v1
         订阅自己频道的presence和消息

         秀场转1v1
         1.主叫
            a.订阅被叫频道的presence，用来写入presence
            b.订阅自己频道的message, 用来收消息
         2.被叫
            a.订阅自己频道的presence和消息
         */
        if (config.role == CallRole.CALLER && config.mode == CallMode.ShowTo1v1) {
            val ownerRoomId = config.ownerRoomId ?: run {
                completion?.invoke(AGError("ownerRoomId is nil, please invoke 'initialize' to setup config", -1))
                return
            }
            var error1: AGError? = null
            var error2: AGError? = null
            var tryCount = 2
            val tryToInvoke = {
                if (tryCount == 0) {
                    completion?.invoke(error1 ?: error2)
                }
            }
            val options1 = SubscribeOptions()
            options1.withMessage = true
            options1.withMetadata = false
            options1.withPresence = false
            _subscribe(roomId, options1) { error ->
                error1 = error
                tryCount -= 1
                tryToInvoke.invoke()
            }

            val options2 = SubscribeOptions()
            options2.withMessage = false
            options2.withMetadata = false
            options2.withPresence = true
            _subscribe(ownerRoomId, options2) { error ->
                error2 = error
                tryCount -= 1
                tryToInvoke.invoke()
            }
        } else {
            val options = SubscribeOptions()
            options.withMessage = true
            options.withMetadata = false
            options.withPresence = true
            _subscribe(roomId, options, completion)
        }
    }

    /// 发送回执消息
    /// - Parameters:
    ///   - roomId: 回执消息发往的频道
    ///   - messageId: 回执的消息id
    ///   - retryCount: 重试次数
    ///   - completion: <#completion description#>
    private fun _sendReceipts(roomId: String, messageId: Int, retryCount: Int = 3, completion: ((AGError?)-> Unit)? = null) {
        val message = mutableMapOf<String, Any>()
        message[kReceiptsKey] = messageId
        Log.d(TAG, "_sendReceipts to $roomId, message: $message, retryCount: $retryCount")
        val json = Gson().toJson(message)
        val options = PublishOptions()
        rtmClient.publish(roomId, json.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                Log.d(TAG, "_sendReceipts cost ${System.currentTimeMillis()} ms")
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                if (retryCount <= 1) {
                    val msg = errorInfo?.errorReason ?: "error"
                    val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                    completion?.invoke(AGError(msg, errorCode))
                } else {
                    _sendReceipts(roomId, messageId, retryCount - 1, completion)
                }
            }
        })
    }

    private fun _sendMessage(roomId: String, message: Map<String, Any>, retryCount: Int = 3, completion:((AGError?)->Unit)?) {
        Log.d(TAG, "_sendMessage to '$roomId', message: $message, retryCount: $retryCount")
        val msgId = message[kMessageId] as? Int ?: 0
        val json = Gson().toJson(message)
        val options = PublishOptions()
        rtmClient.publish(roomId, json.toByteArray(), options, object : ResultCallback<Void> {
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
                        _sendMessage(roomId, message, completion = completion)
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
                if (retryCount <= 1) {
                    val msg = errorInfo?.errorReason ?: "error"
                    completion?.invoke(AGError(msg, -1))
                } else {
                    _sendMessage(roomId, message, retryCount - 1, completion)
                }
            }
        })
    }

    private fun _subscribe(channelName: String, option: SubscribeOptions, completion: ((AGError?) -> Unit)?) {
        rtmClient.subscribe(channelName, option, object: ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                completion?.invoke(null)
            }
            override fun onFailure(errorInfo: ErrorInfo?) {
                val msg = errorInfo?.errorReason ?: "error"
                val errorCode = RtmConstants.RtmErrorCode.getValue(errorInfo?.errorCode)
                completion?.invoke(AGError(msg, errorCode))
            }
        })
    }

    private fun loginRTM(rtmClient: RtmClient, token: String, completion: (ErrorInfo?) -> Unit) {
        if (isLoginedRTM) {
            completion(null)
            return
        }
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
    override fun onTokenPrivilegeWillExpire(channelName: String?) {
        Log.d(TAG, "rtm onTokenPrivilegeWillExpire[${channelName ?: "nil"}]")
        rtmListener?.onTokenPrivilegeWillExpire(channelName)
    }

    override fun onMessageEvent(event: MessageEvent?) {
        val message = event?.message?.data as? ByteArray ?: return
        val jsonString = String(message)
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
        event ?: return
        if (event.type == RtmConstants.RtmPresenceEventType.SNAPSHOT) {
            snapshotDidRecv?.invoke()
            snapshotDidRecv = null
        }
        rtmListener?.onPresenceEvent(event)
    }

    override fun onTopicEvent(event: TopicEvent?) {}
    override fun onLockEvent(event: LockEvent?) {}
    override fun onStorageEvent(event: StorageEvent?) {}
    override fun onConnectionStateChange(
        channelName: String?,
        state: RtmConstants.RtmConnectionState?,
        reason: RtmConstants.RtmConnectionChangeReason?
    ) {}
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



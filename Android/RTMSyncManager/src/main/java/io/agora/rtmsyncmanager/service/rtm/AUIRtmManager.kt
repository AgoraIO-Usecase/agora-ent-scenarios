package io.agora.rtmsyncmanager.service.rtm

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import io.agora.rtm.ErrorInfo
import io.agora.rtm.JoinChannelOptions
import io.agora.rtm.MetadataItem
import io.agora.rtm.MetadataOptions
import io.agora.rtm.PresenceOptions
import io.agora.rtm.PublishOptions
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmConstants.RtmChannelType
import io.agora.rtm.RtmConstants.RtmErrorCode
import io.agora.rtm.StateItem
import io.agora.rtm.StreamChannel
import io.agora.rtm.SubscribeOptions
import io.agora.rtm.WhoNowResult
import io.agora.rtmsyncmanager.utils.AUILogger
import io.agora.rtmsyncmanager.utils.GsonTools

class AUIRtmManager constructor(
    context: Context,
    private val rtmClient: RtmClient,
    @Volatile var isLogin: Boolean = false
) {

    val tag = "AUIRtmManager"
    val kRTM_Referee_LockName = "rtm_referee_lock"
    val proxy = AUIRtmMsgProxy()

    private val rtmStreamChannelMap = mutableMapOf<String, StreamChannel>()
    private val logger = AUILogger(AUILogger.Config(context, "AUIRtmManager"))
    private val throttlerUpdateMetaDataModel = AUIThrottlerUpdateMetaDataModel()
    private val throttlerRemoveMetaDataModel = AUIThrottlerRemoveMetaDataModel()

    init {
        rtmClient.addEventListener(proxy)
    }

    fun deInit(){
        cleanReceipts()
        throttlerUpdateMetaDataModel.reset()
        throttlerRemoveMetaDataModel.reset()
        proxy.unRegisterAllObservers()
        rtmClient.removeEventListener(proxy)
    }

    fun renew(token: String) {
        rtmClient.renewToken(token, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                AUILogger.logger().i("AUIRtmManager", "renew success")
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                AUILogger.logger().e("AUIRtmManager", "renew failed -- $errorInfo")
            }
        })
    }

    fun renewStreamChannelToken(channelName: String, token: String) {
        if (rtmStreamChannelMap[channelName] != null) {
            val streamChannel = rtmStreamChannelMap[channelName]
            streamChannel?.renewToken(token, object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    AUILogger.logger()
                        .i("AUIRtmManager", "renew $channelName channel token success.")
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    AUILogger.logger()
                        .e("AUIRtmManager", "renew $channelName channel token failed -- $errorInfo")
                }
            })
        }
    }


    fun login(token: String, completion: (AUIRtmException?) -> Unit) {
        if (isLogin) {
            completion.invoke(null)
            return
        }
        rtmClient.login(token, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                logger.d("AUIRtmManager", "login success")
                isLogin = true
                completion.invoke(null)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                logger.d("AUIRtmManager", "login error: ${errorInfo?.errorReason}")
                if (errorInfo?.errorCode != RtmErrorCode.OK && errorInfo?.errorCode != RtmErrorCode.DUPLICATE_OPERATION) {
                    completion.invoke(
                        AUIRtmException(
                            RtmErrorCode.getValue(errorInfo?.errorCode),
                            errorInfo?.errorReason ?: "UnKnow",
                            errorInfo?.operation ?: "UnKnow",
                        )
                    )
                } else {
                    isLogin = true
                    completion.invoke(null)
                }
            }
        })
        logger.d("AUIRtmManager", "login ...")
    }

    fun logout() {
        rtmClient.logout(object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
        isLogin = false
    }

    fun subscribeAttribute(channelName: String, itemKey: String, handler: AUIRtmAttributeRespObserver) {
        proxy.registerAttributeRespObserver(channelName, itemKey, handler)
    }

    fun unsubscribeAttribute(channelName: String, itemKey: String, handler: AUIRtmAttributeRespObserver) {
        proxy.unRegisterAttributeRespObserver(channelName, itemKey, handler)
    }

    fun subscribeMessage(handler: AUIRtmMessageRespObserver) {
        proxy.registerMessageRespObserver(handler)
    }

    fun unsubscribeMessage(handler: AUIRtmMessageRespObserver) {
        proxy.unRegisterMessageRespObserver(handler)
    }

    fun subscribeUser(delegate: AUIRtmUserRespObserver) {
        proxy.registerUserRespObserver(delegate)
    }

    fun unsubscribeUser(delegate: AUIRtmUserRespObserver) {
        proxy.unRegisterUserRespObserver(delegate)
    }

    fun subscribe(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        token: String? = null,
        completion: (AUIRtmException?) -> Unit
    ) {
        when (channelType) {
            RtmChannelType.MESSAGE -> {
                val option = SubscribeOptions()
                option.withMetadata = true
                option.withPresence = true
                option.withLock = true
                option.withMessage = true
                logger.d("AUIRtmManager", "subscribe join message channel ...")
                logger.d("MessageChannel", "joining... channelName=$channelName")
                rtmClient.subscribe(channelName, option, object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {
                        logger.d("MessageChannel", "subscribe RtmChannelType.MESSAGE  onSuccess")
                        completion.invoke(null)
                    }

                    override fun onFailure(errorInfo: ErrorInfo?) {
                        if (errorInfo != null) {
                            logger.d(
                                "MessageChannel",
                                "subscribe RtmChannelType.MESSAGE onFailure $errorInfo"
                            )
                            completion.invoke(
                                AUIRtmException(
                                    RtmErrorCode.getValue(errorInfo.errorCode),
                                    errorInfo.errorReason,
                                    errorInfo.operation
                                )
                            )
                        } else {
                            logger.d("MessageChannel", "subscribe RtmChannelType.MESSAGE onFailure")
                            completion.invoke(AUIRtmException(-1, "error", ""))
                        }
                    }
                })
            }

            RtmChannelType.STREAM -> {
                val option = JoinChannelOptions()
                option.token = token
                option.withMetadata = true
                option.withPresence = true
                if (rtmStreamChannelMap[channelName] == null) {
                    logger.d("AUIRtmManager", "create and join stream channel ...")
                    val streamChannel = rtmClient.createStreamChannel(channelName)
                    logger.d("StreamChannel", "joining... channelName=$channelName, token=$token")
                    streamChannel.join(option, object : ResultCallback<Void> {
                        override fun onSuccess(responseInfo: Void?) {
                            logger.d(
                                "StreamChannel",
                                "create and join the stream channel successfully channelName=$channelName"
                            )
                            completion.invoke(null)
                        }

                        override fun onFailure(errorInfo: ErrorInfo?) {
                            logger.d(
                                "StreamChannel",
                                "create and join the stream channel failed for $errorInfo"
                            )
                            if (errorInfo != null) {
                                completion.invoke(
                                    AUIRtmException(
                                        RtmErrorCode.getValue(errorInfo.errorCode),
                                        errorInfo.errorReason,
                                        errorInfo.operation
                                    )
                                )
                            } else {
                                completion.invoke(AUIRtmException(-1, "error", ""))
                            }
                        }
                    })
                    rtmStreamChannelMap[channelName] = streamChannel
                } else {
                    logger.d(
                        "StreamChannel",
                        "create and join the stream channel failed for existing"
                    )
                    completion.invoke(
                        AUIRtmException(
                            -999,
                            "error for streamChannel existing",
                            "subscribe join stream channel"
                        )
                    )
                }
            }

            else -> {
                logger.d("AUIRtmManager", "RtmChannelType mismatching")
                completion.invoke(AUIRtmException(-1, "error", ""))
            }
        }
    }

    fun unSubscribe(
        channelName: String,
        channelType: RtmChannelType = RtmConstants.RtmChannelType.MESSAGE
    ) {
        proxy.cleanCache(channelName)
        when (channelType) {
            RtmChannelType.MESSAGE -> {
                rtmClient.unsubscribe(channelName, object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {
                        AUILogger.logger().i(
                            "AUIRtmManager",
                            "rtmClient unsubscribe $channelName channel success."
                        )
                    }

                    override fun onFailure(errorInfo: ErrorInfo?) {
                        AUILogger.logger().e(
                            "AUIRtmManager",
                            "rtmClient unsubscribe $channelName channel failed -- $errorInfo"
                        )
                    }
                })
            }

            RtmChannelType.STREAM -> {
                val streamChannel = rtmStreamChannelMap[channelName] ?: return
                streamChannel.leave(object : ResultCallback<Void> {
                    override fun onSuccess(responseInfo: Void?) {

                    }

                    override fun onFailure(errorInfo: ErrorInfo?) {

                    }
                })
                rtmStreamChannelMap.remove(channelName)
            }

            else -> {}
        }
    }

    fun fetchMetaDataSnapshot(channelName: String, completion: (AUIRtmException?) -> Unit){
        getMetadata(channelName){ error, metadata ->
            proxy.processMetaData(channelName, metadata)
            completion.invoke(error)
        }
    }

    public fun cleanAllMedadata(channelName: String, lockName: String, completion: (AUIRtmException?) -> Unit) {
        val removeKeys = proxy.keys(channelName) ?: emptyList()
        cleanMetadata(
            channelName = channelName,
            removeKeys = removeKeys,
            lockName = lockName,
            completion = completion
        )
    }

    fun cleanMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        removeKeys: List<String>,
        lockName: String = kRTM_Referee_LockName,
        completion: (AUIRtmException?) -> Unit
    ) {
        val storage = rtmClient.storage
        logger.d("AUIRTM_TAG","cleanMetadata $this, $rtmClient")
        val data = storage.createMetadata()

        removeKeys.forEach {
            val item = MetadataItem()
            item.key = it
            data.setMetadataItem(item)
        }

        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.removeChannelMetadata(channelName, channelType, data, options,
            lockName, object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            RtmErrorCode.getValue(errorInfo.errorCode),
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun setMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        metadata: Map<String, String>,
        completion: (AUIRtmException?) -> Unit
    ) {
        logger.d("AUIRTM_TAG","setMetadata $this, $rtmClient")
        val storage = rtmClient.storage ?: return
        val data = storage.createMetadata()
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        val options = MetadataOptions(true, true)
        storage.setChannelMetadata(
            channelName,
            channelType,
            data,
            options,
            lockName,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            RtmErrorCode.getValue(errorInfo.errorCode),
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun updateMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        metadata: Map<String, String>,
        completion: (AUIRtmException?) -> Unit
    ) {
        val storage = rtmClient.storage
        logger.d("AUIRTM_TAG","updateMetadata $this, $rtmClient")
        val data = storage.createMetadata()
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }
        val options = MetadataOptions()
        storage.updateChannelMetadata(
            channelName,
            channelType,
            data,
            options,
            lockName,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            RtmErrorCode.getValue(errorInfo.errorCode),
                            errorInfo.errorReason,
                            errorInfo.operation
                        )
                    )
                }
            })
    }

    fun getMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        completion: (AUIRtmException?, io.agora.rtm.Metadata?) -> Unit
    ) {
        val storage = rtmClient.storage
        logger.d("AUIRTM_TAG","getMetadata $this, $rtmClient")
        storage.getChannelMetadata(
            channelName,
            channelType,
            object : ResultCallback<io.agora.rtm.Metadata> {
                override fun onSuccess(responseInfo: io.agora.rtm.Metadata?) {
                    completion.invoke(null, responseInfo)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    errorInfo ?: return
                    completion.invoke(
                        AUIRtmException(
                            RtmErrorCode.getValue(errorInfo.errorCode),
                            errorInfo.errorReason,
                            errorInfo.operation
                        ), null
                    )
                }
            })
    }


    fun whoNow(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        completion: (AUIRtmException?, List<Map<String, String>>?) -> Unit
    ) {
        val presence = rtmClient.presence
        logger.d("AUIRTM_TAG","whoNow $this, $rtmClient")
        val options = PresenceOptions()
        options.includeUserId = true
        options.includeState = true
        presence.whoNow(channelName, channelType, options, object : ResultCallback<WhoNowResult> {
            override fun onSuccess(responseInfo: WhoNowResult?) {
                responseInfo ?: return
                var userList = arrayListOf<Map<String, String>>()
                responseInfo.userStateList.forEach { user ->
                    val userMap = mutableMapOf<String, String>()
                    userMap["userId"] = user.userId
                    user.states.forEach { item ->
                        userMap[item.key] = item.value
                    }
                    userList.add(userMap)
                }
                completion.invoke(null, userList)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                errorInfo ?: return
                completion.invoke(
                    AUIRtmException(
                        RtmErrorCode.getValue(errorInfo.errorCode),
                        errorInfo.errorReason,
                        errorInfo.operation
                    ), null
                )
            }
        })
    }

    fun setPresenceState(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        attr: Map<String, Any>,
        completion: (AUIRtmException?) -> Unit
    ) {
        val presence = rtmClient.presence
        logger.d("AUIRTM_TAG","setPresenceState $this, $rtmClient")
        val items = ArrayList<StateItem>()
        attr.forEach { entry ->
            val item = StateItem()
            item.key = entry.key
            item.value = entry.value.toString()
            items.add(item)
        }
        logger.d(
            "PresenceState",
            "Setting channelName=$channelName, channelType=$channelType, items=$items"
        )
        presence.setState(channelName, channelType, items, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                logger.d("PresenceState", "Setting successfully")
                completion.invoke(null)
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                logger.d("PresenceState", "Setting failure : $errorInfo")
                completion.invoke(
                    AUIRtmException(
                        RtmErrorCode.getValue(errorInfo?.errorCode),
                        errorInfo?.errorReason ?: "UnKnow Error",
                        errorInfo?.operation ?: "UnKnow Error"
                    )
                )
            }
        })
    }

    fun setBatchMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        metadata: Map<String, String>,
        fetchImmediately: Boolean = false,
        completion: (AUIRtmException?) -> Unit
    ) {
        AUILogger.logger().d(
            tag = "AUIRtmManager",
            message = "setBatchMetadata1[$channelName)] metadata keys: ${metadata.keys}"
        )
        throttlerUpdateMetaDataModel.appendMetaDataInfo(metadata, completion)
        throttlerUpdateMetaDataModel.throttler.triggerLastEvent(10) {
            val callbacks = throttlerUpdateMetaDataModel.callbacks
            setMetadata(
                channelName,
                channelType,
                lockName,
                throttlerUpdateMetaDataModel.metadata
            ) { ex ->
                callbacks.forEach {
                    it.invoke(ex)
                }
            }
            throttlerUpdateMetaDataModel.reset()
        }
        if (fetchImmediately) {
            throttlerUpdateMetaDataModel.throttler.triggerNow()
        }
    }

    fun cleanBatchMetadata(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        remoteKeys: List<String>,
        fetchImmediately: Boolean = false,
        completion: (AUIRtmException?) -> Unit
    ) {
        throttlerRemoveMetaDataModel.appendMetaDataInfo(remoteKeys, completion)
        throttlerRemoveMetaDataModel.throttler.triggerLastEvent(10) {
            val callbacks = throttlerRemoveMetaDataModel.callbacks
            cleanMetadata(
                channelName,
                channelType,
                throttlerRemoveMetaDataModel.keys,
                lockName
            ) { error ->
                callbacks.forEach {
                    it.invoke(error)
                }
            }
            throttlerRemoveMetaDataModel.reset()
        }
        if (fetchImmediately) {
            throttlerRemoveMetaDataModel.throttler.triggerNow()
        }
    }

    // lock
    fun subscribeLock(
        channelName: String,
        lockName: String = kRTM_Referee_LockName,
        observer: AUIRtmLockRespObserver
    ) {
        proxy.registerLockRespObserver(channelName, lockName, observer)
    }

    fun unsubscribeLock(observer: AUIRtmLockRespObserver) {
        proxy.unRegisterLockRespObserver(observer)
    }

    fun setLock(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        ttl: Long = 10,
        completion: (AUIRtmException?) -> Unit
    ) {
        val lock = rtmClient.lock
        if (lock == null) {
            completion.invoke(AUIRtmException(-1, "get lock error", ""))
            return
        }
        logger.d(tag,"setLock[$channelName][$lockName] start")
        lock.setLock(
            channelName,
            channelType,
            lockName,
            ttl,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    completion.invoke(
                        AUIRtmException(
                            errorInfo?.errorCode?.ordinal ?: -1,
                            errorInfo?.errorReason ?: "",
                            errorInfo?.operation ?: ""
                        )
                    )
                }
            })
    }

    fun acquireLock(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        retry: Boolean = true,
        completion: (AUIRtmException?) -> Unit
    ) {
        val lock = rtmClient.lock
        if (lock == null) {
            completion.invoke(AUIRtmException(-1, "get lock error", ""))
            return
        }
        logger.d(tag,"acquireLock[$channelName][$lockName] start")
        lock.acquireLock(
            channelName,
            channelType,
            lockName,
            retry,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    completion.invoke(
                        AUIRtmException(
                            errorInfo?.errorCode?.ordinal ?: -1,
                            errorInfo?.errorReason ?: "",
                            errorInfo?.operation ?: ""
                        )
                    )
                }
            })
    }

    fun releaseLock(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        completion: (AUIRtmException?) -> Unit
    ) {
        val lock = rtmClient.lock
        if (lock == null) {
            completion.invoke(AUIRtmException(-1, "get lock error", ""))
            return
        }
        logger.d(tag,"releaseLock[$channelName][$lockName] start")
        lock.releaseLock(
            channelName,
            channelType,
            lockName,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    completion.invoke(
                        AUIRtmException(
                            errorInfo?.errorCode?.ordinal ?: -1,
                            errorInfo?.errorReason ?: "",
                            errorInfo?.operation ?: ""
                        )
                    )
                }
            })
    }

    fun removeLock(
        channelName: String,
        channelType: RtmChannelType = RtmChannelType.MESSAGE,
        lockName: String = kRTM_Referee_LockName,
        completion: (AUIRtmException?) -> Unit
    ) {
        val lock = rtmClient.lock
        if (lock == null) {
            completion.invoke(AUIRtmException(-1, "get lock error", ""))
            return
        }
        logger.d(tag,"removeLock[$channelName][$lockName] start")
        lock.removeLock(
            channelName,
            channelType,
            lockName,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    completion.invoke(
                        AUIRtmException(
                            errorInfo?.errorCode?.ordinal ?: -1,
                            errorInfo?.errorReason ?: "",
                            errorInfo?.operation ?: ""
                        )
                    )
                }
            })
    }

    // message
    private val receiptTimeoutRun = mutableMapOf<String, AUIRtmReceiptHandler>()
    private val receiptHandler = Handler(Looper.getMainLooper())

    fun sendReceipt(channelName: String, userId: String, receipt: AUIRtmReceiptModel) {
        publish(channelName, userId, GsonTools.beanToString(receipt) ?: "") {}
    }

    fun cleanReceipts() {
        receiptTimeoutRun.clear()
        receiptHandler.removeCallbacksAndMessages(null)
    }

    fun markReceiptFinished(uniqueId: String, error: AUIRtmException? ) {
        receiptTimeoutRun.remove(uniqueId)?.let {
            it.closure.invoke(error)
            receiptHandler.removeCallbacks(it.runnable)
        }
    }

    fun <Model> publishAndWaitReceipt(
        channelName: String,
        userId: String,
        publishModel: AUIRtmPublishModel<Model>,
        completion: (AUIRtmException?) -> Unit
    ){
        publishAndWaitReceipt(
            channelName,
            userId,
            GsonTools.beanToString(publishModel) ?: "",
            publishModel.uniqueId ?: "",
            completion = completion
        )
    }

    fun publishAndWaitReceipt(
        channelName: String,
        userId: String,
        message: String,
        uniqueId: String,
        timeout: Long = 1000,
        completion: (AUIRtmException?) -> Unit
    ) {
        publish(channelName, userId, message) { error ->
            if (error != null) {
                completion.invoke(error)
                return@publish
            }
            val receipt = AUIRtmReceiptHandler(
                uniqueId,
                completion
            ) {
                receiptTimeoutRun.remove(uniqueId)
                completion.invoke(
                    AUIRtmException(
                        -1,
                        "timeout. uniqueId=$uniqueId",
                        "publishAndWaitReceipt"
                    )
                )
            }
            receiptTimeoutRun[uniqueId] = receipt
            receiptHandler.postDelayed(receipt.runnable, timeout)
        }
    }

    fun publish(
        channelName: String,
        userId: String,
        message: String,
        completion: (AUIRtmException?) -> Unit
    ) {
        val options = PublishOptions()
        var target = channelName
        if(userId.isNotEmpty()){
            options.setChannelType(RtmChannelType.USER)
            target = userId
        }
        rtmClient.publish(
            target,
            message,
            options,
            object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {
                    completion.invoke(null)
                }

                override fun onFailure(errorInfo: ErrorInfo?) {
                    completion.invoke(
                        AUIRtmException(
                            errorInfo?.errorCode?.ordinal ?: -1,
                            errorInfo?.errorReason ?: "",
                            errorInfo?.operation ?: ""
                        )
                    )
                }
            }
        )
    }

    // user metadata
    fun subscribeUser(userId: String) {
        val storage = rtmClient.storage
        storage.subscribeUserMetadata(userId, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun unSubscribeUser(userId: String) {
        val storage = rtmClient.storage
        storage.unsubscribeUserMetadata(userId, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                AUILogger.logger().i("AUIRtmManager", "unsubscribeUserMetadata $userId success.")
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                AUILogger.logger()
                    .e("AUIRtmManager", "unsubscribeUserMetadata $userId failed -- $errorInfo")
            }
        })
    }

    fun removeUserMetadata(userId: String) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        storage.removeUserMetadata(userId, data, options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun setUserMetadata(userId: String, metadata: Map<String, String>) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        storage.setUserMetadata(userId, data, options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })

    }

    fun updateUserMetadata(userId: String, metadata: Map<String, String>) {
        val storage = rtmClient.storage
        val data = storage.createMetadata()
        val options = MetadataOptions()
        options.recordTs = true
        options.recordUserId = true
        metadata.forEach { entry ->
            val item = MetadataItem()
            item.key = entry.key
            item.value = entry.value
            data.setMetadataItem(item)
        }

        storage.updateUserMetadata(userId, data, options, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    fun getUserMetadata(userId: String) {
        val storage = rtmClient.storage
        storage.getUserMetadata(userId, object : ResultCallback<io.agora.rtm.Metadata> {
            override fun onSuccess(responseInfo: io.agora.rtm.Metadata?) {

            }

            override fun onFailure(errorInfo: ErrorInfo?) {

            }
        })
    }

    // error

    fun subscribeError(observer: AUIRtmErrorRespObserver){
        proxy.registerErrorRespObserver(observer)
    }

    fun unSubscribeError(observer: AUIRtmErrorRespObserver){
        proxy.unRegisterErrorRespObserver(observer)
    }
}
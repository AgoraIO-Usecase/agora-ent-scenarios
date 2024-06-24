package io.agora.onetoone.signalClient

import android.os.Handler
import android.os.Looper
import io.agora.onetoone.AGError
import io.agora.onetoone.extension.getCostMilliseconds
import io.agora.rtm.*

fun createRtmSignalClient(client: RtmClient) = CallRtmSignalClient(client)

class CallRtmSignalClient(
    client: RtmClient
): ISignalClient, CallBaseSignalClient(), RtmEventListener {

    companion object {
        private const val TAG = "CALL_RTM_MSG_MANAGER"
    }

    private val mHandler = Handler(Looper.getMainLooper())

    private var rtmClient: RtmClient

    init {
        rtmClient = client
        rtmClient.addEventListener(this)

        // disable retry message
        rtmClient.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        callMessagePrint("init-- CallMessageManager ")
    }

    override fun sendMessage(
        userId: String,
        message: String,
        completion: ((AGError?) -> Unit)?
    ) {
        if (userId.isEmpty() || userId == "0") {
            val errorStr = "sendMessage fail, invalid userId[$userId]"
            callMessagePrint(errorStr)
            completion?.invoke(AGError(errorStr, -1))
            return
        }
        innerSendMessage(userId, message, completion)
    }

    // --------------- MARK: AgoraRtmClientDelegate -------------
    override fun onMessageEvent(event: MessageEvent?) {
        runOnUiThread {
            val message = event?.message?.data as? ByteArray ?: return@runOnUiThread
            val jsonString = String(message, Charsets.UTF_8)
            listeners.forEach {
                it.onMessageReceive(jsonString)
            }
        }
    }

    // --------------- inner private ---------------
    private fun innerSendMessage(userId: String, message: String, completion:((AGError?)->Unit)?) {
        if (userId.isEmpty()) {
            completion?.invoke(AGError("send message fail! userId is empty", -1))
            return
        }
        val options = PublishOptions()
        options.setChannelType(RtmConstants.RtmChannelType.USER)
        val startTime = System.currentTimeMillis()
        callMessagePrint("_sendMessage to '$userId', message: $message")
        rtmClient.publish(userId, message.toByteArray(), options, object : ResultCallback<Void> {
            override fun onSuccess(p0: Void?) {
                callMessagePrint("_sendMessage publish cost ${startTime.getCostMilliseconds()} ms")
                runOnUiThread { completion?.invoke(null) }
            }
            override fun onFailure(errorInfo: ErrorInfo) {
                val msg = errorInfo.errorReason
                val code = RtmConstants.RtmErrorCode.getValue(errorInfo.errorCode)
                callMessagePrint("_sendMessage fail: $msg cost: ${startTime.getCostMilliseconds()} ms", 1)
                runOnUiThread { completion?.invoke(AGError(msg, code)) }
            }
        })
    }

    private fun callMessagePrint(message: String, logLevel: Int = 0) {
        val tag = "[MessageManager]"
        listeners.forEach {
            it.debugInfo("$tag$message)", logLevel)
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
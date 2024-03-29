package io.agora.scene.joy

import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.ErrorInfo
import io.agora.rtm.ResultCallback
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.joy.service.PrepareConfig
import java.util.concurrent.Executors

interface ICallRtmManagerListener {
    /**
     * rtm连接成功
     */
    fun onConnected()

    /**
     * rtm连接断开
     */
    fun onDisconnected()

    /**
     * rtm中断，需要重新login
     */
    fun onConnectionLost()

    /**
     * token即将过期，需要renew token
     */
    fun onTokenPrivilegeWillExpire(channelName: String)
}

object JoyServiceManager {

    private const val TAG = "JoyServiceManager"

    private val mWorkingExecutor = Executors.newSingleThreadExecutor()

    val mPrepareConfig: PrepareConfig = PrepareConfig()

    private val mUser: User
        get() = UserManager.getInstance().user

    var mConnected: Boolean = false

    // RTM是否已经登录
    private var mLoginedRtm = false

    private val mRtmListeners = mutableListOf<ICallRtmManagerListener>()

    private var innerRtcEngine: RtcEngineEx? = null
    val rtcEngine: RtcEngineEx
        get() {
            if (innerRtcEngine == null) {
                val config = RtcEngineConfig()
                config.mContext = AgoraApplication.the()
                config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
                config.mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onError(err: Int) {
                        super.onError(err)
                        JoyLogger.d(TAG, "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    private val rtmEventListener = object :RtmEventListener{
        override fun onConnectionStateChanged(
            channelName: String?,
            state: RtmConstants.RtmConnectionState?,
            reason: RtmConstants.RtmConnectionChangeReason?
        ) {
            super.onConnectionStateChanged(channelName, state, reason)
            JoyLogger.d(TAG,"rtm connectionStateChanged, channelName: $channelName, state: $state reason: $reason")
            channelName ?: return
            if (reason == RtmConstants.RtmConnectionChangeReason.TOKEN_EXPIRED) {
                mRtmListeners.forEach { it.onTokenPrivilegeWillExpire(channelName) }
            } else if (reason == RtmConstants.RtmConnectionChangeReason.LOST) {
                mConnected = false
                mRtmListeners.forEach { it.onConnectionLost() }
            } else if (state == RtmConstants.RtmConnectionState.CONNECTED) {
                if (mConnected) return
                mConnected = true
                mRtmListeners.forEach { it.onConnected() }
            } else {
                if (!mConnected) return
                mConnected = false
                mRtmListeners.forEach { it.onDisconnected() }
            }
        }
    }

    private var innerRtmClient: RtmClient? = null
    val rtmClient: RtmClient
        get() {
            if (innerRtmClient == null) {
                val rtmConfig = RtmConfig.Builder(BuildConfig.AGORA_APP_ID, mUser.id.toString()).build()
                innerRtmClient = RtmClient.create(rtmConfig)
            }
            return innerRtmClient!!
        }

    fun initRtm(){
        rtmClient.addEventListener(rtmEventListener)
        rtmClient.setParameters("{\"rtm.msg.tx_timeout\": 3000}")
        JoyLogger.d(TAG,"init rtm")
    }

    fun loginRtm(rtmToken: String, completion: (error: Exception?, ret: Int) -> Unit) {
        JoyLogger.d(TAG, "login rtm start")
        if (rtmToken.isEmpty()) {
            val reason = "RTM Token is Empty"
            completion(Exception(reason), -1)
            return
        }
        val rtmClient = this.rtmClient
        if (!mLoginedRtm) {
            rtmClient.logout(object : ResultCallback<Void?> {
                override fun onSuccess(responseInfo: Void?) {}
                override fun onFailure(errorInfo: ErrorInfo?) {}
            })
            rtmClient.login(rtmToken, object : ResultCallback<Void?> {
                override fun onSuccess(p0: Void?) {
                    JoyLogger.d(TAG, "login rtm success")
                    mLoginedRtm = true
                    completion(null, 0)
                }

                override fun onFailure(p0: ErrorInfo?) {
                    JoyLogger.d(TAG, "login rtm failure:$p0")
                    mLoginedRtm = false
                    completion(Exception(p0.toString()), -1)
                }
            })
        } else {
            completion.invoke(null, 0)
        }
    }

    fun renewTokens(callback: ((Boolean)) -> Unit) {
        TokenGenerator.generateTokens(
            "", // 万能 token
            UserManager.getInstance().user.id.toString(),
            TokenGenerator.TokenGeneratorType.token007,
            arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
                TokenGenerator.AgoraTokenType.rtm
            ),
            success = { ret ->
                val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc]
                val rtmToken = ret[TokenGenerator.AgoraTokenType.rtm]
                if (rtcToken == null || rtmToken == null) {
                    callback.invoke(false)
                    return@generateTokens
                }
                mPrepareConfig.rtcToken = rtcToken
                mPrepareConfig.rtmToken = rtmToken
                renewRtmToken(rtmToken)
                callback.invoke(true)
            },
            failure = {
                callback.invoke(false)
            })
    }

    fun renewRtmToken(rtmToken: String) {
        val rtmClient = this.rtmClient
        if (!mLoginedRtm) {
            //没有登陆成功，但是需要自动登陆，可能是初始token问题，这里重新initialize
            JoyLogger.d(TAG, "renewToken need to reinit")
            rtmClient.logout(object : ResultCallback<Void?> {
                override fun onSuccess(responseInfo: Void?) {}
                override fun onFailure(errorInfo: ErrorInfo?) {}
            })
            loginRtm(rtmToken, completion = { error, ret ->

            })
            return
        }

        rtmClient.renewToken(rtmToken, object : ResultCallback<Void> {
            override fun onSuccess(responseInfo: Void?) {
                JoyLogger.d(TAG, "rtm renewToken success")
            }

            override fun onFailure(errorInfo: ErrorInfo?) {
                JoyLogger.d(TAG, "rtm renewToken failure:$errorInfo")
            }
        })
    }

    fun destroy() {
        innerRtcEngine?.let {
            mWorkingExecutor.execute { RtcEngineEx.destroy() }
            innerRtcEngine = null
        }
        innerRtmClient?.let {
            it.logout(object : ResultCallback<Void> {
                override fun onSuccess(responseInfo: Void?) {}
                override fun onFailure(errorInfo: ErrorInfo?) {}
            })
            mConnected = false
            innerRtmClient =null
        }
        RtmClient.release()
        mPrepareConfig.rtcToken = ""
        mPrepareConfig.rtmToken = ""
    }
}
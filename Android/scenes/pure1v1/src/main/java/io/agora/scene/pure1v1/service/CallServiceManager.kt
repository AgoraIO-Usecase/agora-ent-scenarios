package io.agora.scene.pure1v1.service

import android.content.Context
import android.util.Log
import android.view.TextureView
import io.agora.rtc2.*
import io.agora.rtm.RtmClient
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.pure1v1.callAPI.*

class CallServiceManager {
    companion object {
        val instance: CallServiceManager by lazy {
            CallServiceManager()
        }
    }

    private val TAG = "CallServiceManager_LOG"

    var rtcEngine: RtcEngineEx? = null

    var callApi: ICallApi? = null

    var sceneService: Pure1v1ServiceImp? = null

    var localUser: UserInfo? = null

    var remoteUser: UserInfo? = null

    var connectedChannelId: String? = null

    var localCanvas: TextureView? = null

    var remoteCanvas: TextureView? = null

    private var tokenConfig: CallTokenConfig? = null
    // 接通使用的rtc token
    private var acceptToken: String? = null

    private var mContext: Context? = null

    fun setup(context: Context) {
        tokenConfig = CallTokenConfig()
        mContext = context
        // 获取用户信息
        val user = UserInfo()
        user.userId = UserManager.getInstance().user.id.toString()
        user.userName = UserManager.getInstance().user.name
        user.avatar = UserManager.getInstance().user.headUrl
        localUser = user
        // 初始化场景service
        sceneService = Pure1v1ServiceImp(user)
        // 初始化call api
        callApi = CallApiImpl(context)
        // 创建canvas
        localCanvas = TextureView(context)
        remoteCanvas = TextureView(context)
    }

    fun cleanUp() {
        rtcEngine = null
        tokenConfig = null
        localUser = null
        remoteUser = null
        mContext = null
        localCanvas = null
        remoteCanvas = null
        callApi?.deinitialize {
            RtcEngine.destroy()

            RtmClient.release()
        }
        callApi = null
        sceneService?.leaveRoom {
        }
        sceneService = null
    }

    fun startupCallApiIfNeed() {
        val user = localUser ?: return
        val tokenConfig = tokenConfig ?: return
        if (tokenConfig.rtcToken.isNotEmpty() && tokenConfig.rtmToken.isNotEmpty()) {
            return
        }
        tokenConfig.roomId = user.getRoomId()
        TokenGenerator.generateTokens(
            tokenConfig.roomId,
            user.userId,
            TokenGenerator.TokenGeneratorType.token007,
            arrayOf(
                TokenGenerator.AgoraTokenType.rtc,
                TokenGenerator.AgoraTokenType.rtm
            ), { ret ->
                val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc]
                val rtmToken = ret[TokenGenerator.AgoraTokenType.rtm]
                if (rtcToken == null || rtmToken == null) {
                    return@generateTokens
                }
                tokenConfig.rtcToken = rtcToken
                tokenConfig.rtmToken = rtmToken
                initialize(tokenConfig)
            })
    }

    fun fetchAcceptCallToken(fromRoomId: String, complete: ((String?) -> Unit)?) {
        val user = localUser ?: run {
            complete?.invoke(null)
            return
        }
        val token = acceptToken
        if (token != null) {
            complete?.invoke(token)
        } else {
            TokenGenerator.generateTokens(
                fromRoomId, user.userId,
                TokenGenerator.TokenGeneratorType.token007,
                arrayOf(TokenGenerator.AgoraTokenType.rtc), { ret ->
                    val rtcToken = ret[TokenGenerator.AgoraTokenType.rtc]
                    if (rtcToken != null) {
                        acceptToken = rtcToken
                        complete?.invoke(rtcToken)
                    } else {
                        complete?.invoke(null)
                    }
                })
        }
    }

    fun resetAcceptCallToken() {
        acceptToken = null
    }

    private fun initialize(tokenConfig: CallTokenConfig) {
        val api = callApi ?: return
        val user = localUser ?: return
        val localView = localCanvas ?: return
        val remoteView = remoteCanvas ?: return
        val engine = createRtcEngine()
        rtcEngine = engine
        val config = CallConfig(
            BuildConfig.AGORA_APP_ID,
            user.userId.toInt(),
            user.toMap(),
            engine,
            null,
            CallMode.Pure1v1,
            CallRole.CALLER,
            localView,
            remoteView,
            false
        )
        api.initialize(config, tokenConfig) {
            val prepareConfig = PrepareConfig.calleeConfig()
            api.prepareForCall(prepareConfig) { err ->
            }
        }
    }

    private fun createRtcEngine(): RtcEngineEx {
        val context = mContext ?: throw RuntimeException("RtcEngine create failed!")
        var rtcEngine: RtcEngineEx? = null
        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
                Log.e(TAG, "IRtcEngineEventHandler onError:$err")
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
        try {
            rtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "RtcEngine.create() called error: $e")
        }
        return rtcEngine ?: throw RuntimeException("RtcEngine create failed!")
    }
}
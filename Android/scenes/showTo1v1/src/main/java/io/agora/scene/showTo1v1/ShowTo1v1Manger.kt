package io.agora.scene.showTo1v1

import android.content.Context
import android.util.Log
import android.view.TextureView
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtm.RtmClient
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.scene.showTo1v1.audio.AudioScenarioApi
import io.agora.scene.showTo1v1.callapi.CallApiImpl
import io.agora.scene.showTo1v1.callapi.CallConfig
import io.agora.scene.showTo1v1.callapi.PrepareConfig
import io.agora.scene.showTo1v1.callapi.signalClient.CallRtmManager
import io.agora.scene.showTo1v1.callapi.signalClient.ICallRtmManagerListener
import io.agora.scene.showTo1v1.callapi.signalClient.createRtmManager
import io.agora.scene.showTo1v1.callapi.signalClient.createRtmSignalClient
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceImpl
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CallRole(val value: Int) {
    CALLEE(0),
    CALLER(1)
}

/*
 * 业务逻辑管理模块
 */
class ShowTo1v1Manger constructor() {

    val scenarioApi by lazy { AudioScenarioApi(mRtcEngine) }

    companion object {

        private val instance by lazy {
            ShowTo1v1Manger()
        }

        fun getImpl(): ShowTo1v1Manger {
            return instance
        }

        val scheduledThreadPool: ExecutorService = Executors.newSingleThreadExecutor()
    }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    val mCallApi by lazy { CallApiImpl(AgoraApplication.the()) }

    // 远端用户
    var mRemoteUser: ShowTo1v1UserInfo? = null

    // 连接的用户
    var mConnectedChannelId: String? = null

    private var innerCurrentUser: ShowTo1v1UserInfo? = null

    private var rtmManager: CallRtmManager? = null

    var mService: ShowTo1v1ServiceImpl? = null

    var isCaller = false

    // 本地用户
    val mCurrentUser: ShowTo1v1UserInfo
        get() {
            if (innerCurrentUser == null) {
                innerCurrentUser = ShowTo1v1UserInfo(
                    userId = UserManager.getInstance().user.id.toString(),
                    userName = UserManager.getInstance().user.name,
                    avatar = UserManager.getInstance().user.headUrl,
                    createdAt = TimeUtils.currentTimeMillis()
                )
            }
            return innerCurrentUser!!
        }

    private var innerPrepareConfig: PrepareConfig? = null

    // 1v1 tokenConfig
    val mPrepareConfig: PrepareConfig
        get() {
            if (innerPrepareConfig == null) {
                innerPrepareConfig = PrepareConfig()
            }
            return innerPrepareConfig!!
        }

    val mLocalVideoView: TextureView
        get() {
            return mCallApi.tempLocalCanvasView
        }

    val mRemoteVideoView: TextureView
        get() {
            return mCallApi.tempRemoteCanvasView
        }

    @Volatile
    private var isCallApiInit = false

    fun setup(context: Context) {
        initRtm()
        mService = ShowTo1v1ServiceImpl(context, rtmManager!!.getRtmClient(), mCurrentUser)
    }

    private fun initRtm() {
        if (rtmManager == null) {
            // 使用RtmManager管理RTM
            rtmManager = createRtmManager(BuildConfig.AGORA_APP_ID, mCurrentUser.getIntUserId())
            // 监听 rtm manager 事件
            rtmManager?.addListener(object : ICallRtmManagerListener {
                override fun onConnected() {

                }

                override fun onDisconnected() {

                }

                override fun onConnectionLost() {
                    // 表示rtm超时断连了，需要重新登录，这里模拟了3s重新登录
                }

                override fun onTokenPrivilegeWillExpire(channelName: String) {
                    // 重新获取token
                    renewTokens {  }
                }
            })
        }

        // rtm login
        rtmManager?.login(mPrepareConfig.rtmToken) {
            if (it == null) {
                // login 成功后初始化 call api
                initCallAPi()
            }
        }
    }

    private fun initCallAPi() {
        val config = CallConfig(
            appId = BuildConfig.AGORA_APP_ID,
            userId = mCurrentUser.getIntUserId(),
            rtcEngine = mRtcEngine,
            createRtmSignalClient(rtmManager!!.getRtmClient())
        )
        mCallApi.initialize(config)
    }

    /**
     * @param role 呼叫/被叫
     * @param ownerRoomId 呼叫/被叫房间 id
     */
    fun prepareCall(role: CallRole, ownerRoomId: String, callback: (success: Boolean) -> Unit) {
        initCallAPi()
        if (role == CallRole.CALLER) {
            mPrepareConfig.roomId = mCurrentUser.get1v1ChannelId()
        } else {
            isCallApiInit = false
            mPrepareConfig.roomId = ownerRoomId
        }
        mPrepareConfig.userExtension = mCurrentUser.toMap()
        checkCallTokenConfig { renewToken ->
            if (renewToken) {
                mCallApi.prepareForCall(mPrepareConfig) {
                    if (it == null) {
                        isCallApiInit = true
                        callback.invoke(true)
                    } else {
                        callback.invoke(false)
                    }
                }
            }
        }
    }

    fun deInitialize() {
        // 防止 deinitialize 失败，提前置 false
        isCallApiInit = false
        mCallApi.deinitialize {

        }
    }

    fun renewTokens(callback: ((Boolean)) -> Unit) {
        if (generalToken() != "") {
            callback.invoke(true)
            return
        }
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
                setupGeneralToken(rtcToken)
                mCallApi.renewToken(rtcToken)
                rtmManager?.renewToken(rtmToken)
                callback.invoke(true)
            },
            failure = {
                callback.invoke(false)
            })
    }


    // call api tokenConfig
    private fun checkCallTokenConfig(callback: (Boolean) -> Unit) {
        if (mPrepareConfig.rtcToken.isNotEmpty() && mPrepareConfig.rtmToken.isNotEmpty()) {
            callback.invoke(true)
            return
        }
        renewTokens {
            callback.invoke(it)
        }
    }


    // 万能通用 token ,进入房间列表默认获取万能 token
    private var mGeneralToken: String = ""

    fun setupGeneralToken(generalToken: String) {
        mGeneralToken = generalToken
    }

    fun generalToken(): String = mGeneralToken

    private var innerRtcEngine: RtcEngineEx? = null
    val mRtcEngine: RtcEngineEx
        get() {
            if (innerRtcEngine == null) {
                val config = RtcEngineConfig()
                config.mContext = AgoraApplication.the()
                config.mAppId = BuildConfig.AGORA_APP_ID
                config.addExtension("agora_ai_echo_cancellation_extension")
                config.addExtension("agora_ai_noise_suppression_extension")
                config.mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onError(err: Int) {
                        super.onError(err)
                        Log.d("RtcEngine", "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngineEx.create(config) as RtcEngineEx).apply {
                    enableVideo()
                    // 设置视频最佳配置
                    setCameraCapturerConfiguration(CameraCapturerConfiguration(
                        CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT,
                        CameraCapturerConfiguration.CaptureFormat(720, 1280, 24)
                    ).apply {
                        followEncodeDimensionRatio = true
                    })
                    setVideoEncoderConfiguration(
                        VideoEncoderConfiguration().apply {
                            dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                            frameRate = 24
                            degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                        }
                    )
                    setParameters("{\"che.video.videoCodecIndex\": 2}")
                    enableInstantMediaRendering()
                    setParameters("{\"rtc.video.quickIntraHighFec\": true}")
                    setParameters("{\"rtc.network.e2e_cc_mode\": 3}")
                }
                scenarioApi.initialize()
            }
            return innerRtcEngine!!
        }

    fun destroy() {
        mGeneralToken = ""
        mRemoteUser = null
        mConnectedChannelId = null
        isCallApiInit = false
        mCallApi.deinitialize {}
        innerCurrentUser = null
        innerPrepareConfig = null
        rtmManager?.let {
            it.logout()
            RtmClient.release()
            rtmManager = null
        }
        innerRtcEngine?.let {
            workingExecutor.execute { RtcEngine.destroy() }
            innerRtcEngine = null
        }
    }
}
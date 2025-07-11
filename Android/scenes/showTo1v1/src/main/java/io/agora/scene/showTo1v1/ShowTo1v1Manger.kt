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
import io.agora.audioscenarioapi.AudioScenarioApi
import io.agora.onetoone.AGError
import io.agora.onetoone.CallApiImpl
import io.agora.onetoone.CallConfig
import io.agora.onetoone.PrepareConfig
import io.agora.onetoone.signalClient.CallRtmManager
import io.agora.onetoone.signalClient.ICallRtmManagerListener
import io.agora.onetoone.signalClient.createRtmManager
import io.agora.onetoone.signalClient.createRtmSignalClient
import io.agora.rtc2.RtcConnection
import io.agora.scene.base.AgoraTokenType
import io.agora.scene.base.TokenGeneratorType
import io.agora.scene.showTo1v1.service.ShowTo1v1ServiceImpl
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class CallRole(val value: Int) {
    CALLEE(0),
    CALLER(1)
}

/*
 * Business logic management module
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

    // Remote user
    var mRemoteUser: ShowTo1v1UserInfo? = null

    // Connected user
    var mConnectedChannelId: String? = null

    private var innerCurrentUser: ShowTo1v1UserInfo? = null

    private var rtmManager: CallRtmManager? = null

    var mService: ShowTo1v1ServiceImpl? = null

    var isCaller = false

    // Local user
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

    private var isLogined = false

    fun setup(context: Context, callback: (e: AGError?) -> Unit) {
        if (rtmManager == null) {
            // Use RtmManager to manage RTM
            rtmManager = createRtmManager(BuildConfig.AGORA_APP_ID, mCurrentUser.getIntUserId())
            // Listen to rtm manager events
            rtmManager?.addListener(object : ICallRtmManagerListener {
                override fun onConnected() {

                }

                override fun onDisconnected() {

                }

                override fun onTokenPrivilegeWillExpire(channelName: String) {
                    // Re-acquire token
                    renewTokens {  }
                }
            })
            mService = ShowTo1v1ServiceImpl(context, rtmManager!!.getRtmClient(), mCurrentUser)
            initCallAPi()
        }

        // rtm login
        if (!isLogined) {
            rtmManager?.login(mPrepareConfig.rtmToken) {
                if (it == null) {
                    isLogined = true
                }
                callback.invoke(it)
            }
        } else {
            callback.invoke(null)
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
     * @param role Caller/Called
     * @param ownerRoomId Caller/Called room id
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
        // Prevent deinitialize from failing, set to false early
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
            "", // Universal token
            UserManager.getInstance().user.id.toString(),
            TokenGeneratorType.Token007,
            arrayOf(
                AgoraTokenType.Rtc,
                AgoraTokenType.Rtm
            ),
            success = { ret ->
                mPrepareConfig.rtcToken = ret
                mPrepareConfig.rtmToken = ret
                setupGeneralToken(ret)
                mCallApi.renewToken(ret)
                rtmManager?.renewToken(ret)
                callback.invoke(true)
            },
            failure = {
                callback.invoke(false)
            })
    }

    // Switch camera status
    fun switchCamera(cameraOn: Boolean) {
        val channelId = mConnectedChannelId ?: return
        val uid = innerCurrentUser?.userId ?: return
        if (cameraOn) {
            mRtcEngine.startPreview()
            mRtcEngine.muteLocalVideoStreamEx(false, RtcConnection(channelId, uid.toInt()))
        } else {
            mRtcEngine.stopPreview()
            mRtcEngine.muteLocalVideoStreamEx(true, RtcConnection(channelId, uid.toInt()))
        }
    }

    // Switch microphone status
    fun switchMic(micOn: Boolean) {
        val channelId = mConnectedChannelId ?: return
        val uid = innerCurrentUser?.userId ?: return
        if (micOn) {
            mRtcEngine.muteLocalAudioStreamEx(false, RtcConnection(channelId, uid.toInt()))
        } else {
            mRtcEngine.muteLocalAudioStreamEx(true, RtcConnection(channelId, uid.toInt()))
        }
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


    // Universal general token, default to get universal token when entering room list
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
                    // Set video best configuration
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
                }
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
            isLogined = false
        }
        innerRtcEngine?.let {
            workingExecutor.execute { RtcEngine.destroy() }
            innerRtcEngine = null
        }
    }
}
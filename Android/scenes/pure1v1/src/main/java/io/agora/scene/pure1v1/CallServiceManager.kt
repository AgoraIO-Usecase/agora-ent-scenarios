package io.agora.scene.pure1v1

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.agora.onetoone.CallApiImpl
import io.agora.onetoone.CallConfig
import io.agora.onetoone.ICallApi
import io.agora.onetoone.PrepareConfig
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.data.MediaPlayerSource
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtm.RtmClient
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.TimeUtils
import io.agora.audioscenarioapi.AudioScenarioApi
import io.agora.onetoone.signalClient.CallRtmManager
import io.agora.onetoone.signalClient.ICallRtmManagerListener
import io.agora.onetoone.signalClient.createRtmSignalClient
import io.agora.scene.base.AgoraTokenType
import io.agora.scene.base.TokenGeneratorType
import io.agora.scene.pure1v1.rtt.PureRttApiManager
import io.agora.scene.pure1v1.service.Pure1v1ServiceImp
import io.agora.scene.pure1v1.service.UserInfo

/*
 * Business logic management module
 */
class CallServiceManager {
    companion object {
        val instance: CallServiceManager by lazy {
            CallServiceManager()
        }

        val urls = arrayOf(
            "https://download.agora.io/demo/test/calling_show_1.mp4",
            "https://download.agora.io/demo/test/calling_show_2.mp4",
            "https://download.agora.io/demo/test/calling_show_4.mp4"
        )

        const val callMusic = "https://download.agora.io/demo/test/1v1_bgm1.wav"
    }

    private val tag = "CallServiceManager_LOG"

    val tokenExpireTime = 20 * 60 * 60 * 1000 // 20h

    var rtcEngine: RtcEngineEx? = null

    var callApi: ICallApi? = null

    var sceneService: Pure1v1ServiceImp? = null

    var localUser: UserInfo? = null

    var remoteUser: UserInfo? = null

    var isCaller: Boolean = false

    var connectedChannelId: String? = null

    var mPrepareConfig: PrepareConfig? = null

    var localCanvas: ViewGroup? = null

    var remoteCanvas: ViewGroup? = null

    // rtc channel universal token
    var rtcToken: String = ""

    // rtm token
    var rtmToken: String = ""

    var lastTokenFetchTime: Long = 0L

    var onUserChanged: (() -> Unit)? = null

    private var mContext: Context? = null

    private var mMediaPlayer: IMediaPlayer? = null

    private var mMediaPlayer2: IMediaPlayer? = null

    private var callRtmManager: CallRtmManager? = null

    var scenarioApi: AudioScenarioApi? = null

    fun setup(context: Context, completion: (success: Boolean)-> Unit) {
        // Initialize rtm manager
        if (callRtmManager == null) {
            val rtmManager = CallRtmManager(BuildConfig.AGORA_APP_ID, UserManager.getInstance().user.id.toInt())
            callRtmManager = rtmManager
            rtmManager.addListener(object : ICallRtmManagerListener {
                override fun onConnected() {
                    // RTM connected
                }

                override fun onDisconnected() {
                    // RTM disconnected
                }

                override fun onTokenPrivilegeWillExpire(channelName: String) {
                    // RTM Token expired, need to re-acquire token
                }
            })

            mPrepareConfig = PrepareConfig()
            mContext = context
            // Get user information
            val user = UserInfo()
            user.userId = UserManager.getInstance().user.id.toString()
            user.userName = UserManager.getInstance().user.name
            user.avatar = UserManager.getInstance().user.headUrl
            localUser = user
            // Create rtc engine instance
            val engine = createRtcEngine()
            rtcEngine = engine
            // Initialize mpk, used to play incoming call video
            mMediaPlayer = engine.createMediaPlayer()
            // Initialize mpk2, used to play incoming call music
            mMediaPlayer2 = engine.createMediaPlayer()
            // Initialize scene service
            sceneService = Pure1v1ServiceImp(context, rtmManager.getRtmClient(), user) {
                onUserChanged?.invoke()
            }

            // Create and initialize CallAPI
            val callApi = CallApiImpl(context)
            this.callApi = callApi
            callApi.initialize(CallConfig(
                BuildConfig.AGORA_APP_ID,
                user.userId.toInt(),
                engine,
                createRtmSignalClient(rtmManager.getRtmClient())
            ))

            // Initialize audio scenario API
            val scenarioApi = AudioScenarioApi(engine)
            this.scenarioApi = scenarioApi
        }

        // Get universal token
        fetchToken { success ->
            // External creation needs to manage login
            if (success) {
                callRtmManager?.login(rtmToken) {
                    if (it == null) {
                        completion.invoke(true)
                    } else {
                        Pure1v1Logger.e(tag, null,"login error = ${it.msg}")
                        completion.invoke(false)
                    }
                }
            } else {
                completion.invoke(false)
            }
        }
    }

    fun cleanUp() {
        rtcEngine = null
        mPrepareConfig = null
        localUser = null
        remoteUser = null
        mContext = null
        localCanvas = null
        remoteCanvas = null
        callApi?.deinitialize {
            mMediaPlayer?.destroy()
            mMediaPlayer = null
            mMediaPlayer2?.destroy()
            mMediaPlayer2 = null
            RtcEngine.destroy()
        }
        callApi = null
        sceneService?.leaveRoom {
        }
        sceneService?.reset()
        sceneService = null

        callRtmManager?.logout()
        callRtmManager = null

        RtmClient.release()
    }

    fun reInit() {
        val uid = localUser?.userId ?: return
        val manager = callRtmManager ?: return
        callApi?.initialize(CallConfig(
            BuildConfig.AGORA_APP_ID,
            uid.toInt(),
            rtcEngine!!,
            createRtmSignalClient(manager.getRtmClient())
        ))
    }

    /*
     * Get universal token and initialize CallAPI
     */
    fun fetchToken(completion: (success: Boolean) -> Unit) {
        val user = localUser ?: return
        //Pure1v1Logger.d(tag, "generateTokens")
        TokenGenerator.generateTokens(
            "",
            user.userId,
            TokenGeneratorType.Token007,
            arrayOf(
                AgoraTokenType.Rtc,
                AgoraTokenType.Rtm
            ), { ret ->
                this.rtcToken = ret
                this.rtmToken = ret
                this.lastTokenFetchTime = TimeUtils.currentTimeMillis()

                PureRttApiManager.setBasicAuth(rtcToken)
                //Pure1v1Logger.d(tag, "generateTokens success")
                completion.invoke(true)
            }, {
                //Pure1v1Logger.e(tag, null,"generateTokens failed: $it")
                completion.invoke(false)
            })
    }

    fun renewRtmToken() {
        if (rtmToken != "") {
            callRtmManager?.renewToken(rtmToken)
        }
    }

    // Prepare call environment
    fun prepareForCall(success: () -> Unit) {
        val api = callApi ?: return
        val user = localUser ?: return
        val localView = localCanvas ?: return
        val remoteView = remoteCanvas ?: return
        val prepareConfig = mPrepareConfig ?: return

        if (rtcToken == "" || rtmToken == "") {
            fetchToken {
                prepareConfig.roomId = user.getCallChannelId()
                prepareConfig.rtcToken = rtcToken
                prepareConfig.rtmToken = rtmToken
                prepareConfig.localView = localView
                prepareConfig.remoteView = remoteView
                prepareConfig.userExtension = user.toMap()
                api.prepareForCall(prepareConfig) {
                    if (it == null) {
                        success.invoke()
                    }
                }
            }
        } else {
            prepareConfig.roomId = user.getCallChannelId()
            prepareConfig.rtcToken = rtcToken
            prepareConfig.rtmToken = rtmToken
            prepareConfig.localView = localView
            prepareConfig.remoteView = remoteView
            prepareConfig.userExtension = user.toMap()
            api.prepareForCall(prepareConfig) {
                if (it == null) {
                    success.invoke()
                }
            }
        }
    }

    // Play incoming call video
    fun playCallShow(url: String) {
        val ret = mMediaPlayer?.openWithMediaSource(MediaPlayerSource().apply {
            setUrl(url)
            isAutoPlay = true
            isEnableCache = true
        })
        mMediaPlayer?.setLoopCount(-1)
        mMediaPlayer?.adjustPlayoutVolume(0)
        Pure1v1Logger.d(tag, "playCallShow: $ret")
    }

    // Stop incoming call video
    fun stopCallShow() {
        val player = mMediaPlayer ?: return
        val canvas = VideoCanvas(null)
        canvas.renderMode = RENDER_MODE_HIDDEN
        canvas.sourceType = VIDEO_SOURCE_MEDIA_PLAYER
        canvas.mediaPlayerId = player.mediaPlayerId
        rtcEngine?.setupLocalVideo(canvas)

        val ret = player.stop()
        Pure1v1Logger.d(tag, "stopCallShow：$ret")
    }

    // Render incoming call video
    fun renderCallShow(view: View) {
        val player = mMediaPlayer ?: return
        val canvas = VideoCanvas(view)
        canvas.renderMode = RENDER_MODE_HIDDEN
        canvas.sourceType = VIDEO_SOURCE_MEDIA_PLAYER
        canvas.mediaPlayerId = player.mediaPlayerId
        rtcEngine?.setupLocalVideo(canvas)
    }

    // Play incoming call music
    fun playCallMusic(url: String) {
        mMediaPlayer2?.openWithMediaSource(MediaPlayerSource().apply {
            setUrl(url)
            isAutoPlay = true
            isEnableCache = true
        })
        mMediaPlayer2?.setLoopCount(-1)
    }

    // Stop playing incoming call music
    fun stopCallMusic() {
        mMediaPlayer2?.stop()
    }

    // Switch camera switch status
    fun switchCamera(cameraOn: Boolean) {
        val channelId = connectedChannelId ?: return
        val uid = localUser?.userId ?: return
        if (cameraOn) {
            rtcEngine?.startPreview()
            rtcEngine?.muteLocalVideoStreamEx(false, RtcConnection(channelId, uid.toInt()))
        } else {
            rtcEngine?.stopPreview()
            rtcEngine?.muteLocalVideoStreamEx(true, RtcConnection(channelId, uid.toInt()))
        }
    }

    // Switch microphone switch status
    fun switchMic(micOn: Boolean) {
        val channelId = connectedChannelId ?: return
        val uid = localUser?.userId ?: return
        if (micOn) {
            rtcEngine?.muteLocalAudioStreamEx(false, RtcConnection(channelId, uid.toInt()))
        } else {
            rtcEngine?.muteLocalAudioStreamEx(true, RtcConnection(channelId, uid.toInt()))
        }
    }

    // -------------------------- inner private --------------------------
    // Create RtcEngine instance
    private fun createRtcEngine(): RtcEngineEx {
        val context = mContext ?: throw RuntimeException("RtcEngine create failed!")
        var rtcEngine: RtcEngineEx? = null
        val config = RtcEngineConfig()
        config.mContext = context
        config.mAppId = BuildConfig.AGORA_APP_ID
        config.mEventHandler = object : IRtcEngineEventHandler() {
            override fun onError(err: Int) {
                super.onError(err)
            }
        }
        config.mChannelProfile = CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = AUDIO_SCENARIO_GAME_STREAMING
        config.addExtension("agora_ai_echo_cancellation_extension")
        config.addExtension("agora_ai_noise_suppression_extension")
        try {
            rtcEngine = RtcEngine.create(config) as RtcEngineEx

            // Set video best configuration
            rtcEngine.setCameraCapturerConfiguration(CameraCapturerConfiguration(
                CameraCapturerConfiguration.CAMERA_DIRECTION.CAMERA_FRONT,
                CameraCapturerConfiguration.CaptureFormat(720, 1280, 24)
            ).apply {
                followEncodeDimensionRatio = true
            })
            rtcEngine.setVideoEncoderConfiguration(
                VideoEncoderConfiguration().apply {
                    dimensions = VideoEncoderConfiguration.VideoDimensions(720, 1280)
                    frameRate = 24
                    degradationPrefer = VideoEncoderConfiguration.DEGRADATION_PREFERENCE.MAINTAIN_BALANCED
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Pure1v1Logger.e(tag, null,"RtcEngine.create() called error: $e")
        }
        return rtcEngine ?: throw RuntimeException("RtcEngine create failed!")
    }
}
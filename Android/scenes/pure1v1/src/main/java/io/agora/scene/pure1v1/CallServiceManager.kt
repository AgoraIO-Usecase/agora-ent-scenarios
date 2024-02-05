package io.agora.scene.pure1v1

import android.content.Context
import android.view.View
import android.view.ViewGroup
import io.agora.scene.pure1v1.callapi.CallApiImpl
import io.agora.scene.pure1v1.callapi.CallConfig
import io.agora.scene.pure1v1.callapi.ICallApi
import io.agora.scene.pure1v1.callapi.PrepareConfig
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.data.MediaPlayerSource
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm.RtmClient
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.pure1v1.service.Pure1v1ServiceImp
import io.agora.scene.pure1v1.service.UserInfo

/*
 * 业务逻辑管理模块
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

    var rtcEngine: RtcEngineEx? = null

    var callApi: ICallApi? = null

    var sceneService: Pure1v1ServiceImp? = null

    var localUser: UserInfo? = null

    var remoteUser: UserInfo? = null

    var connectedChannelId: String? = null

    var mPrepareConfig: PrepareConfig? = null

    var localCanvas: ViewGroup? = null

    var remoteCanvas: ViewGroup? = null

    // rtc 频道万能 token
    var rtcToken: String = ""

    // rtm token
    var rtmToken: String = ""

    private var mContext: Context? = null

    private var mMediaPlayer: IMediaPlayer? = null

    private var mMediaPlayer2: IMediaPlayer? = null

    fun setup(context: Context) {
        mPrepareConfig = PrepareConfig()
        mContext = context
        // 获取用户信息
        val user = UserInfo()
        user.userId = UserManager.getInstance().user.id.toString()
        user.userName = UserManager.getInstance().user.name
        user.avatar = UserManager.getInstance().user.headUrl
        localUser = user
        // 创建 rtc引擎实例
        val engine = createRtcEngine()
        rtcEngine = engine
        // 初始化mpk，用于播放来电秀视频
        mMediaPlayer = engine.createMediaPlayer()
        // 初始化mpk2，用于播放来电铃声
        mMediaPlayer2 = engine.createMediaPlayer()
        // 初始化场景service
        sceneService = Pure1v1ServiceImp(user)
        // 创建并初始化CallAPI
        val callApi = CallApiImpl(context)
        this.callApi = callApi
        callApi.initialize(CallConfig(
            BuildConfig.AGORA_APP_ID,
            user.userId.toInt(),
            engine,
            null
        ))
        // 获取万能Token
        fetchToken {}
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
            RtmClient.release()
        }
        callApi = null
        sceneService?.leaveRoom {
        }
        sceneService?.reset()
        sceneService = null
    }

    fun reInit() {
        val uid = localUser?.userId ?: return
        callApi?.initialize(CallConfig(
            BuildConfig.AGORA_APP_ID,
            uid.toInt(),
            rtcEngine,
            null
        ))
    }

    /*
     * 获取万能Token并初始化CallAPI
     */
    private fun fetchToken(success: () -> Unit) {
        val user = localUser ?: return
        TokenGenerator.generateTokens(
            "",
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
                this.rtcToken = rtcToken
                this.rtmToken = rtmToken
                success.invoke()
            }, {
                Pure1v1Logger.e(tag, "generateTokens failed: $it")
            })
    }

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
                prepareConfig.autoJoinRTC = false
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
            prepareConfig.autoJoinRTC = false
            prepareConfig.userExtension = user.toMap()
            api.prepareForCall(prepareConfig) {
                if (it == null) {
                    success.invoke()
                }
            }
        }
    }

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

    fun renderCallShow(view: View) {
        val player = mMediaPlayer ?: return
        val canvas = VideoCanvas(view)
        canvas.renderMode = RENDER_MODE_HIDDEN
        canvas.sourceType = VIDEO_SOURCE_MEDIA_PLAYER
        canvas.mediaPlayerId = player.mediaPlayerId
        rtcEngine?.setupLocalVideo(canvas)
    }

    fun playCallMusic(url: String) {
        val ret = mMediaPlayer2?.openWithMediaSource(MediaPlayerSource().apply {
            setUrl(url)
            isAutoPlay = true
            isEnableCache = true
        })
        mMediaPlayer2?.setLoopCount(-1)
        Pure1v1Logger.d(tag, "playCallMusic：$ret")
    }

    fun stopCallMusic() {
        val ret = mMediaPlayer2?.stop()
        Pure1v1Logger.d(tag, "stopCallMusic：$ret")
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
                Pure1v1Logger.e(tag, "IRtcEngineEventHandler onError:$err")
            }
        }
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING
        try {
            rtcEngine = RtcEngine.create(config) as RtcEngineEx
        } catch (e: Exception) {
            e.printStackTrace()
            Pure1v1Logger.e(tag, "RtcEngine.create() called error: $e")
        }
        return rtcEngine ?: throw RuntimeException("RtcEngine create failed!")
    }
}
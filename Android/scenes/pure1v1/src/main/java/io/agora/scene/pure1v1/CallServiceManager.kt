package io.agora.scene.pure1v1

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.rtc2.*
import io.agora.rtc2.Constants.*
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtm.RtmClient
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.manager.UserManager
import io.agora.scene.pure1v1.callapi.*
import io.agora.scene.pure1v1.service.Pure1v1ServiceImp
import io.agora.scene.pure1v1.service.UserInfo

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

    var mPrepareConfig: PrepareConfig? = null

    var localCanvas: ViewGroup? = null

    var remoteCanvas: ViewGroup? = null
    // 接通使用的rtc token
    private var acceptToken: String? = null

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
        // 初始化场景service
        sceneService = Pure1v1ServiceImp(user)
        // 初始化call api
        callApi = CallApiImpl(context)
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
        sceneService = null
    }

    fun startupCallApiIfNeed() {
        val user = localUser ?: return
        val prepareConfig = mPrepareConfig ?: return
        if (prepareConfig.rtcToken.isNotEmpty() && prepareConfig.rtmToken.isNotEmpty()) {
            return
        }
        prepareConfig.roomId = user.getRoomId()
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
                prepareConfig.rtcToken = rtcToken
                prepareConfig.rtmToken = rtmToken
                initialize(prepareConfig)
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

    fun playCallShow(url: String) {
        val ret = mMediaPlayer?.open(url, 0)
        mMediaPlayer?.adjustPlayoutVolume(0)
        Log.d("hugo", "$ret")
    }

    fun stopCallShow() {
        val ret = mMediaPlayer?.stop()
        Log.d("hugo", "$ret")
    }

    fun renderCallShow(view: View) {
        val player = mMediaPlayer ?: return
        val canvas = VideoCanvas(view)
        canvas.renderMode = RENDER_MODE_FIT
        canvas.sourceType = VIDEO_SOURCE_MEDIA_PLAYER
        canvas.mediaPlayerId = player.mediaPlayerId
        rtcEngine?.setupLocalVideo(canvas)
    }

    fun playCallMusic(url: String) {
        val ret = mMediaPlayer2?.open(url, 0)
        Log.d("hugo", "$ret")
    }

    fun stopCallMusic() {
        val ret = mMediaPlayer2?.stop()
        Log.d("hugo", "$ret")
    }

    private fun initialize(prepareConfig: PrepareConfig) {
        val api = callApi ?: return
        val user = localUser ?: return
        val localView = localCanvas ?: return
        val remoteView = remoteCanvas ?: return
        val engine = createRtcEngine()
        rtcEngine = engine
        val config = CallConfig(
            BuildConfig.AGORA_APP_ID,
            user.userId.toInt(),
            engine,
            null
        )
        api.initialize(config)
        prepareConfig.localView = localView
        prepareConfig.remoteView = remoteView
        prepareConfig.autoAccept = false
        prepareConfig.autoJoinRTC = false
        prepareConfig.userExtension = user.toMap()
        api.prepareForCall(prepareConfig) { }

        // 初始化mpk，用于播放来电秀视频
        mMediaPlayer = engine.createMediaPlayer()
        mMediaPlayer?.registerPlayerObserver(object : IMediaPlayerObserver {
            override fun onPlayerStateChanged(
                state: io.agora.mediaplayer.Constants.MediaPlayerState?,
                error: io.agora.mediaplayer.Constants.MediaPlayerError?
            ) {
                Log.d("hugo", "onPlayerStateChanged: $state, error: $error")
                if (state == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                    mMediaPlayer?.setLoopCount(-1)
                    mMediaPlayer?.play()
                }
            }

            override fun onPositionChanged(positionMs: Long, timestampMs: Long) {}

            override fun onPlayerEvent(
                eventCode: io.agora.mediaplayer.Constants.MediaPlayerEvent?,
                elapsedTime: Long,
                message: String?
            ) {}

            override fun onMetaData(
                type: io.agora.mediaplayer.Constants.MediaPlayerMetadataType?,
                data: ByteArray?
            ) {}

            override fun onPlayBufferUpdated(playCachedBuffer: Long) {}

            override fun onPreloadEvent(
                src: String?,
                event: io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent?
            ) {}

            override fun onAgoraCDNTokenWillExpire() {}

            override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {}

            override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {}

            override fun onAudioVolumeIndication(volume: Int) {}
        })

        mMediaPlayer2 = engine.createMediaPlayer()
        mMediaPlayer2?.registerPlayerObserver(object : IMediaPlayerObserver {
            override fun onPlayerStateChanged(
                state: io.agora.mediaplayer.Constants.MediaPlayerState?,
                error: io.agora.mediaplayer.Constants.MediaPlayerError?
            ) {
                Log.d("hugo", "onPlayerStateChanged: $state, error: $error")
                if (state == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                    mMediaPlayer2?.setLoopCount(-1)
                    mMediaPlayer2?.play()
                }
            }

            override fun onPositionChanged(positionMs: Long, timestampMs: Long) {}

            override fun onPlayerEvent(
                eventCode: io.agora.mediaplayer.Constants.MediaPlayerEvent?,
                elapsedTime: Long,
                message: String?
            ) {}

            override fun onMetaData(
                type: io.agora.mediaplayer.Constants.MediaPlayerMetadataType?,
                data: ByteArray?
            ) {}

            override fun onPlayBufferUpdated(playCachedBuffer: Long) {}

            override fun onPreloadEvent(
                src: String?,
                event: io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent?
            ) {}

            override fun onAgoraCDNTokenWillExpire() {}

            override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {}

            override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {}

            override fun onAudioVolumeIndication(volume: Int) {}
        })
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
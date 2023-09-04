package io.agora.scene.showTo1v1

import android.util.Log
import android.view.TextureView
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.RtcEngineEx
import io.agora.rtc2.video.CameraCapturerConfiguration
import io.agora.rtc2.video.VideoEncoderConfiguration
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.BuildConfig
import io.agora.scene.base.TokenGenerator
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.showTo1v1.callAPI.CallConfig
import io.agora.scene.showTo1v1.callAPI.CallMode
import io.agora.scene.showTo1v1.callAPI.CallRole
import io.agora.scene.showTo1v1.callAPI.CallTokenConfig
import io.agora.scene.showTo1v1.callAPI.ICallApi
import io.agora.scene.showTo1v1.service.ShowTo1v1RoomInfo
import io.agora.scene.showTo1v1.service.ShowTo1v1UserInfo
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcher
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherAPIImpl
import io.agora.scene.showTo1v1.videoSwitchApi.VideoSwitcherImpl
import java.util.concurrent.Executors

class ShowTo1v1Manger constructor() {

    val videoEncoderConfiguration = VideoEncoderConfiguration().apply {
        orientationMode = VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
    }
    val virtualBackgroundSource = VirtualBackgroundSource().apply {
        backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR
    }
    val videoCaptureConfiguration = CameraCapturerConfiguration(CameraCapturerConfiguration.CaptureFormat()).apply {
        followEncodeDimensionRatio = false
    }

    companion object {

        private val instance by lazy {
            ShowTo1v1Manger()
        }

        fun getImpl(): ShowTo1v1Manger {
            return instance
        }
    }

    private val workingExecutor = Executors.newSingleThreadExecutor()

    val mCallApi by lazy { ICallApi.getImplInstance() }

    // 远端用户
    var mRemoteUser: ShowTo1v1UserInfo? = null

    // 连接的用户
    var mConnectedChannelId: String? = null

    private var innerCurrentUser: ShowTo1v1UserInfo? = null

    // 本地用户
    val mCurrentUser: ShowTo1v1UserInfo
        get() {
            if (innerCurrentUser == null) {
                innerCurrentUser = ShowTo1v1UserInfo(
                    userId = UserManager.getInstance().user.id.toString(),
                    userName = UserManager.getInstance().user.name,
                    avatar = UserManager.getInstance().user.headUrl
                )
            }
            return innerCurrentUser!!
        }

    private var innerCallTokenConfig: CallTokenConfig? = null

    // 1v1 tokenConfig
    val mCallTokenConfig: CallTokenConfig
        get() {
            if (innerCallTokenConfig == null) {
                innerCallTokenConfig = CallTokenConfig()
            }
            return innerCallTokenConfig!!
        }

    private var innerLocalVideoView: TextureView? = null

    val mLocalVideoView: TextureView
        get() {
            if (innerLocalVideoView == null) {
                innerLocalVideoView = TextureView(AgoraApplication.the())
            }
            return innerLocalVideoView!!
        }

    private var innerRemoteVideoView: TextureView? = null

    val mRemoteVideoView: TextureView
        get() {
            if (innerRemoteVideoView == null) {
                innerRemoteVideoView = TextureView(AgoraApplication.the())
            }
            return innerRemoteVideoView!!
        }

    /**
     * @param role 呼叫/被叫
     * @param ownerRoomId 呼叫/被叫房间 id
     */
    fun reInitCallApi(role: CallRole, ownerRoomId: String, callback: () -> Unit) {
        if (role == CallRole.CALLER) {
            mCallTokenConfig.roomId = mCurrentUser.get1v1ChannelId()
        } else {
            mCallTokenConfig.roomId = ownerRoomId
        }
        checkCallTokenConfig(role) {
            mCallApi.deinitialize {
                val config = CallConfig(
                    appId = BuildConfig.AGORA_APP_ID,
                    userId = mCurrentUser.getIntUserId(),
                    userExtension = mCurrentUser.toMap(),
                    ownerRoomId = ownerRoomId,
                    rtcEngine = mRtcEngine,
                    mode = CallMode.ShowTo1v1,
                    role = role,
                    localView = mLocalVideoView,
                    remoteView = mRemoteVideoView,
                    autoAccept = true
                )
                mCallApi.initialize(config, mCallTokenConfig) {
                    callback.invoke()
                }
            }
        }
    }


    // call api tokenConfig
    private fun checkCallTokenConfig(role: CallRole, callback: () -> Unit) {
        if (mCallTokenConfig.rtcToken.isNotEmpty() && mCallTokenConfig.rtmToken.isNotEmpty()) {
            callback.invoke()
            return
        }
        TokenGenerator.generateTokens(
            if (role == CallRole.CALLEE) "" else mCallTokenConfig.roomId, // 被叫万能 token
            mCurrentUser.userId,
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
                mCallTokenConfig.rtcToken = rtcToken
                mCallTokenConfig.rtmToken = rtmToken
                callback.invoke()
            })
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
                config.mAppId = io.agora.scene.base.BuildConfig.AGORA_APP_ID
                config.mEventHandler = object : IRtcEngineEventHandler() {
                    override fun onError(err: Int) {
                        super.onError(err)
                        Log.d("RtcEngine", "Rtc Error code:$err, msg:" + RtcEngine.getErrorDescription(err))
                    }
                }
                innerRtcEngine = (RtcEngine.create(config) as RtcEngineEx).apply {
                    enableVideo()
                }
            }
            return innerRtcEngine!!
        }

    private var innerVideoSwitcher: VideoSwitcher? = null
    val mVideoSwitcher: VideoSwitcher
        get() {
            if (innerVideoSwitcher == null) {
                innerVideoSwitcher = VideoSwitcherImpl(mRtcEngine, VideoSwitcherAPIImpl(mRtcEngine))
            }
            return innerVideoSwitcher!!
        }

    fun cleanCache() {
        innerVideoSwitcher?.let {
            it.unloadConnections()
        }
    }

    fun destroy() {
        innerVideoSwitcher?.let {
            it.unloadConnections()
            innerVideoSwitcher = null
        }
        innerRtcEngine?.let {
            workingExecutor.execute { RtcEngine.destroy() }
            innerRtcEngine = null
        }
    }
}
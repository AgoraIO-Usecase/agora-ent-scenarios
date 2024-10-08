package io.agora.scene.show

import io.agora.rtc2.Constants
import io.agora.rtc2.RtcConnection
import io.agora.rtc2.SimulcastStreamConfig
import io.agora.rtc2.video.*
import io.agora.scene.base.Constant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.GsonUtil
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.base.utils.ToastUtils

/*
 * 高清设置模块
 */
object VideoSetting {

    enum class BitRate constructor(val value: Int) {
        BR_Low_1V1(1461),
        BR_Medium_1V1(1461),
        BR_High_1V1(2099),
        BR_Low_PK(700),
        BR_Medium_PK(800),
        BR_High_PK(800),
        BR_STANDRAD(0)
    }

    enum class SuperResolution constructor(val value: Int) {
        //1倍：     n=6
        //1.33倍:  n=7
        //1.5倍：  n=8
        //2倍：     n=3
        //锐化：    n=10(android是10，iOS是11)
        //超级画质  n=20
        SR_1(6),
        SR_1_33(7),
        SR_1_5(8),
        SR_2(3),
        SR_SHARP(10),
        SR_NONE(0),
        SR_SUPER(20),
        SR_AUTO(-1)
    }

    enum class Resolution constructor(val width: Int, val height: Int) {
        V_1080P(1080, 1920),
        V_720P(720, 1280),
        V_540P(540, 960),
        V_480P(480, 856),
        V_360P(360, 640),
        V_180P(180, 360),
    }

    fun Resolution.toIndex() = ResolutionList.indexOf(this)

    val ResolutionList = listOf(
        Resolution.V_360P,
        Resolution.V_480P,
        Resolution.V_540P,
        Resolution.V_720P,
        Resolution.V_1080P
    )

    enum class FrameRate constructor(val fps: Int) {
        FPS_1(1),
        FPS_7(7),
        FPS_10(10),
        FPS_15(15),
        FPS_24(24),
        FPS_30(30),
        FPS_60(60),
    }

    fun FrameRate.toIndex() = FrameRateList.indexOf(this)

    val FrameRateList = listOf(
        FrameRate.FPS_1,
        FrameRate.FPS_7,
        FrameRate.FPS_10,
        FrameRate.FPS_15,
        FrameRate.FPS_24
    )

    enum class DeviceLevel constructor(val value: Int) {
        Low(0),
        Medium(1),
        High(2)
    }

    enum class NetworkLevel constructor(val value: Int) {
        Good(0),
        Normal(1)
    }

    enum class BroadcastStrategy constructor(val value: Int) {
        Smooth(0),
        Clear(1)
    }

    // 观众端 ---- 看播设置
    class AudiencePlaySetting {

        companion object {
            // 画质增强、低端机
            val ENHANCE_LOW = 0

            // 画质增强、中端机
            val ENHANCE_MEDIUM = 1

            // 画质增强、高端机
            val ENHANCE_HIGH = 2

            // 基础模式、低端机
            val BASE_LOW = 3

            // 基础模式、中端机
            val BASE_MEDIUM = 4

            // 基础模式、高端机
            val BASE_HIGH = 5
        }

    }

    enum class LiveMode constructor(val value: Int) {
        OneVOne(0),
        PK(1)
    }

    /**
     * 观众设置
     */
    data class AudienceSetting constructor(val video: Video) {
        data class Video constructor(
            val SR: SuperResolution // 超分
        )
    }

    /**
     * 主播设置
     */
    data class BroadcastSetting constructor(
        val video: Video,
        val audio: Audio
    ) {
        data class Video constructor(
            val H265: Boolean, // 画质增强
            val colorEnhance: Boolean, // 色彩增强
            val lowLightEnhance: Boolean, // 暗光增强
            val videoDenoiser: Boolean, // 视频降噪
            val PVC: Boolean, // 码率节省
            val captureResolution: Resolution, // 采集分辨率
            val encodeResolution: Resolution, // 编码分辨率
            val frameRate: FrameRate, // 帧率
            val bitRate: Int, // 码率
            val bitRateRecommand: Int,
            val bitRateStandard: Boolean, // 码率自适应
            val hardwareVideoEncoder: Boolean
        )

        data class Audio constructor(
            val inEarMonitoring: Boolean, // 耳返
            val recordingSignalVolume: Int, // 人声音量
            val audioMixingVolume: Int, // 音乐音量
        )
    }

    data class LowStreamVideoSetting constructor(
        val encodeResolution: Resolution, // 编码分辨率
        val frameRate: FrameRate, // 帧率
        val bitRate: Int, // 码率(0为自适应)
        val SVC: Boolean,
        val enableHardwareEncoder: Boolean
    )

    /**
     * 推荐设置
     */
    object RecommendBroadcastSetting {

        val LowDevice1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_15,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_Low_1V1.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val MediumDevice1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_24,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_Medium_1V1.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val HighDevice1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_1080P,
                encodeResolution = Resolution.V_1080P,
                frameRate = FrameRate.FPS_24,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_High_1V1.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val Audience1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = true,
                captureResolution = Resolution.V_180P,
                encodeResolution = Resolution.V_180P,
                frameRate = FrameRate.FPS_15,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_STANDRAD.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val LowDevicePK = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_540P,
                encodeResolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_Low_PK.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val MediumDevicePK = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_540P,
                encodeResolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_Medium_PK.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val HighDevicePK = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_15,
                bitRate = BitRate.BR_STANDRAD.value,
                bitRateRecommand = BitRate.BR_High_PK.value,
                bitRateStandard = true,
                hardwareVideoEncoder = true
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

    }

    /**
     * 视频小流设置
     */
    object RecommendLowStreamVideoSetting {
        val LowDeviceGoodNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 680,
            SVC = false,
            enableHardwareEncoder = true,
        )

        val LowDeviceNormalNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 748,
            SVC = true,
            enableHardwareEncoder = false,
        )

        val MiddleDeviceGoodNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 680,
            SVC = false,
            enableHardwareEncoder = true,
        )

        val MiddleDeviceNormalNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 748,
            SVC = true,
            enableHardwareEncoder = false,
        )

        val HighDeviceGoodNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_540P,
            frameRate = FrameRate.FPS_15,
            bitRate = 1100,
            SVC = false,
            enableHardwareEncoder = true,
        )

        val HighDeviceNormalNetwork1v1 = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 748,
            SVC = true,
            enableHardwareEncoder = false,
        )

        val PK = LowStreamVideoSetting(
            encodeResolution = Resolution.V_360P,
            frameRate = FrameRate.FPS_15,
            bitRate = 680,
            SVC = false,
            enableHardwareEncoder = true,
        )
    }

    private var currAudienceSetting: AudienceSetting = getCurrAudienceSetting()
    private var currBroadcastSetting: BroadcastSetting = getCurrBroadcastSetting()
    private var currLowStreamSetting: LowStreamVideoSetting? = getCurrLowStreamSetting()

    // 当前观众设备等级（高、中、低）
    private var currAudienceDeviceLevel: DeviceLevel = DeviceLevel.valueOf(
        SPUtil.getString(Constant.CURR_AUDIENCE_DEVICE_LEVEL, DeviceLevel.Low.toString())
    )

    // 当前主播网络设置
    private var currNetworkLevel: NetworkLevel = NetworkLevel.valueOf(
        SPUtil.getString(Constant.CURR_BROADCAST_NETWORK_LEVEL, NetworkLevel.Good.toString())
    )

    // 观众看播设置
    private var currAudiencePlaySetting: Int =
        SPUtil.getInt(Constant.CURR_AUDIENCE_PLAY_SETTING, AudiencePlaySetting.BASE_LOW)

    // 超分开关
    private var currAudienceEnhanceSwitch =
        SPUtil.getBoolean(Constant.CURR_AUDIENCE_ENHANCE_SWITCH, true)

    // 是否在pk 模式中，pk 中观众不开启超分
    @Volatile
    private var isPkMode: Boolean = false

    fun setIsPkMode(isPkMode: Boolean) {
        this.isPkMode = isPkMode
    }

    fun getCurrAudienceSetting(): AudienceSetting {
        //
        val jsonStr = SPUtil.getString(Constant.CURR_AUDIENCE_SETTING, "")
        try {
            return GsonUtil.getInstance().fromJson(jsonStr, AudienceSetting::class.java)
        } catch (e: java.lang.Exception) {
            val result = AudienceSetting(AudienceSetting.Video(SuperResolution.SR_NONE))
            setCurrAudienceSetting(result)
            return result
        }
    }

    fun getCurrBroadcastSetting(): BroadcastSetting {
        val jsonStr = SPUtil.getString(Constant.CURR_BROADCAST_SETTING, "")
        try {
            return GsonUtil.getInstance().fromJson(jsonStr, BroadcastSetting::class.java)
        } catch (e: java.lang.Exception) {
            val result = RecommendBroadcastSetting.LowDevice1v1
            setCurrBroadcastSetting(result)
            return result
        }
    }

    fun getCurrLowStreamSetting(): LowStreamVideoSetting? {
        val jsonStr = SPUtil.getString(Constant.CURR_LOW_STREAM_SETTING, "")
        if (jsonStr == "") return null
        try {
            return GsonUtil.getInstance().fromJson(jsonStr, LowStreamVideoSetting::class.java)
        } catch (e: java.lang.Exception) {
            val result = RecommendLowStreamVideoSetting.LowDeviceGoodNetwork1v1
            setCurrLowStreamSetting(result)
            return result
        }
    }

    fun getCurrAudiencePlaySetting() = currAudiencePlaySetting

    fun getCurrAudienceEnhanceSwitch() = currAudienceEnhanceSwitch

    fun setCurrAudienceSetting(audienceSetting: AudienceSetting) {
        SPUtil.putString(Constant.CURR_AUDIENCE_SETTING, GsonUtil.instance.toJson(audienceSetting))
        currAudienceSetting = audienceSetting
    }

    fun setCurrBroadcastSetting(broadcastSetting: BroadcastSetting) {
        SPUtil.putString(
            Constant.CURR_BROADCAST_SETTING,
            GsonUtil.instance.toJson(broadcastSetting)
        )
        currBroadcastSetting = broadcastSetting
    }

    fun setCurrLowStreamSetting(lowStreamSetting: LowStreamVideoSetting?) {
        if (lowStreamSetting == null) {
            SPUtil.putString(
                Constant.CURR_LOW_STREAM_SETTING,
                ""
            )
        } else {
            SPUtil.putString(
                Constant.CURR_LOW_STREAM_SETTING,
                GsonUtil.instance.toJson(lowStreamSetting)
            )
        }
    }

    fun setCurrAudienceDeviceLevel(deviceLevel: DeviceLevel) {
        currAudienceDeviceLevel = deviceLevel
        SPUtil.putString(Constant.CURR_AUDIENCE_DEVICE_LEVEL, deviceLevel.toString())
    }

    fun setCurrNetworkLevel(networkLevel: NetworkLevel) {
        currNetworkLevel = networkLevel
        SPUtil.putString(Constant.CURR_BROADCAST_NETWORK_LEVEL, networkLevel.toString())
    }

    fun setCurrAudiencePlaySetting(currAudiencePlaySetting: Int) {
        this.currAudiencePlaySetting = currAudiencePlaySetting
        SPUtil.putInt(Constant.CURR_AUDIENCE_PLAY_SETTING, currAudiencePlaySetting)
    }

    fun setCurrAudienceEnhanceSwitch(currAudienceEnhanceSwitch: Boolean) {
        this.currAudienceEnhanceSwitch = currAudienceEnhanceSwitch
        SPUtil.putBoolean(Constant.CURR_AUDIENCE_ENHANCE_SWITCH, currAudienceEnhanceSwitch)
    }

    fun resetAudienceSetting() {
        isPkMode = false
        val result = AudienceSetting(AudienceSetting.Video(SuperResolution.SR_NONE))
        setCurrAudienceSetting(result)
    }

    fun resetBroadcastSetting() {
        setCurrBroadcastSetting(
            when (currAudienceDeviceLevel) {
                DeviceLevel.Low -> RecommendBroadcastSetting.LowDevice1v1
                DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevice1v1
                DeviceLevel.High -> RecommendBroadcastSetting.HighDevice1v1
            }
        )
    }

    fun updateAudienceSetting() {
        if (currAudienceDeviceLevel != DeviceLevel.Low) {
            setCurrAudienceEnhanceSwitch(true)
            updateAudioSetting(SR = SuperResolution.SR_AUTO)
        } else {
            setCurrAudienceEnhanceSwitch(false)
            updateAudioSetting(SR = SuperResolution.SR_NONE)
        }
    }

    fun updateAudioSetting(SR: SuperResolution? = null) {
        setCurrAudienceSetting(
            AudienceSetting(AudienceSetting.Video(SR ?: currAudienceSetting.video.SR))
        )
        updateRTCAudioSetting(SR)
    }

    /**
     * 更新开播设置
     * @param deviceLevel 设备等级：高、中、低
     * @param networkLevel 区域网络状况：好、一般
     * @param broadcastStrategy 开播策略：清晰优先、流畅优先
     * @param isJoinedRoom 是否已经加入频道（部分设置必须在加入频道前设置）
     * @param isByAudience
     */
    fun updateBroadcastSetting(
        deviceLevel: DeviceLevel,
        networkLevel: NetworkLevel = NetworkLevel.Good,
        broadcastStrategy: BroadcastStrategy = BroadcastStrategy.Smooth,
        isJoinedRoom: Boolean = false,
        isByAudience: Boolean = false,
        rtcConnection: RtcConnection? = null
    ) {
        ShowLogger.d("VideoSettings", "updateBroadcastSetting, deviceLevel:$deviceLevel networkLevel:$networkLevel broadcastStrategy:$broadcastStrategy")
        var liveMode = LiveMode.OneVOne
        if (isByAudience) {
            setCurrAudienceDeviceLevel(deviceLevel)
            return
        } else {
            setCurrAudienceDeviceLevel(deviceLevel)
            setCurrNetworkLevel(networkLevel)
            liveMode = when (currBroadcastSetting) {
                RecommendBroadcastSetting.LowDevice1v1, RecommendBroadcastSetting.MediumDevice1v1, RecommendBroadcastSetting.HighDevice1v1 -> LiveMode.OneVOne
                RecommendBroadcastSetting.LowDevicePK, RecommendBroadcastSetting.MediumDevicePK, RecommendBroadcastSetting.HighDevicePK -> LiveMode.PK
                else -> LiveMode.OneVOne
            }
        }

        updateBroadcastSetting(
            when (liveMode) {
                LiveMode.OneVOne -> when (deviceLevel) {
                    DeviceLevel.Low -> RecommendBroadcastSetting.LowDevice1v1
                    DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevice1v1
                    DeviceLevel.High -> RecommendBroadcastSetting.HighDevice1v1
                }

                LiveMode.PK -> when (deviceLevel) {
                    DeviceLevel.Low -> RecommendBroadcastSetting.LowDevicePK
                    DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevicePK
                    DeviceLevel.High -> RecommendBroadcastSetting.HighDevicePK
                }
            },
            if (broadcastStrategy == BroadcastStrategy.Smooth) when (liveMode) {
                LiveMode.OneVOne -> when (deviceLevel) {
                    DeviceLevel.Low -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.LowDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.LowDeviceNormalNetwork1v1
                    DeviceLevel.Medium -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.MiddleDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.MiddleDeviceNormalNetwork1v1
                    DeviceLevel.High -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.HighDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.HighDeviceNormalNetwork1v1
                }

                LiveMode.PK -> when (deviceLevel) {
                    DeviceLevel.Low -> RecommendLowStreamVideoSetting.PK
                    DeviceLevel.Medium -> RecommendLowStreamVideoSetting.PK
                    DeviceLevel.High -> RecommendLowStreamVideoSetting.PK
                }
            } else null,
            isJoinedRoom,
            rtcConnection
        )
    }

    fun updateBroadcastSetting(
        liveMode: LiveMode,
        isLinkAudience: Boolean = false,
        isJoinedRoom: Boolean = true,
        rtcConnection: RtcConnection? = null
    ) {
        ShowLogger.d("VideoSettings", "updateBroadcastSetting2, liveMode: $liveMode, isLinkAudience: $isLinkAudience, isPKMode: $isPkMode")

        val deviceLevel = when (currBroadcastSetting) {
            RecommendBroadcastSetting.LowDevice1v1, RecommendBroadcastSetting.LowDevicePK -> DeviceLevel.Low
            RecommendBroadcastSetting.MediumDevice1v1, RecommendBroadcastSetting.MediumDevicePK -> DeviceLevel.Medium
            RecommendBroadcastSetting.HighDevice1v1, RecommendBroadcastSetting.HighDevicePK -> DeviceLevel.High
            RecommendBroadcastSetting.Audience1v1 -> DeviceLevel.High
            else -> return
        }

        val networkLevel = currNetworkLevel

        updateBroadcastSetting(
            if (isLinkAudience) RecommendBroadcastSetting.Audience1v1 else {
                when (liveMode) {
                    LiveMode.OneVOne -> when (deviceLevel) {
                        DeviceLevel.Low -> RecommendBroadcastSetting.LowDevice1v1
                        DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevice1v1
                        DeviceLevel.High -> RecommendBroadcastSetting.HighDevice1v1
                    }

                    LiveMode.PK -> when (deviceLevel) {
                        DeviceLevel.Low -> RecommendBroadcastSetting.LowDevicePK
                        DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevicePK
                        DeviceLevel.High -> RecommendBroadcastSetting.HighDevicePK
                    }
                }
            },
            if (getCurrLowStreamSetting() != null)
                if (liveMode == LiveMode.PK) RecommendLowStreamVideoSetting.PK
                else when (deviceLevel) {
                    DeviceLevel.Low -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.LowDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.LowDeviceNormalNetwork1v1
                    DeviceLevel.Medium -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.MiddleDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.MiddleDeviceNormalNetwork1v1
                    DeviceLevel.High -> if (networkLevel == NetworkLevel.Good) RecommendLowStreamVideoSetting.HighDeviceGoodNetwork1v1 else RecommendLowStreamVideoSetting.HighDeviceNormalNetwork1v1
                }
            else null,
            isJoinedRoom,
            rtcConnection
        )

        if (isLinkAudience && isPkMode) {
            setCurrAudienceEnhanceSwitch(false)
            updateAudioSetting(SR = SuperResolution.SR_NONE)
        }
    }

    private fun updateBroadcastSetting(
        recommendSetting: BroadcastSetting,
        lowStreamSetting: LowStreamVideoSetting?,
        isJoinedRoom: Boolean,
        rtcConnection: RtcConnection? = null
    ) {
        ShowLogger.d("VideoSettings", "updateBroadcastSetting3, lowStreamSetting: $lowStreamSetting")
        setCurrBroadcastSetting(recommendSetting)
        setCurrLowStreamSetting(lowStreamSetting)
        updateRTCBroadcastSetting(
            rtcConnection,
            isJoinedRoom,
            currBroadcastSetting.video.H265,
            currBroadcastSetting.video.colorEnhance,
            currBroadcastSetting.video.lowLightEnhance,
            currBroadcastSetting.video.videoDenoiser,
            currBroadcastSetting.video.PVC,
            currBroadcastSetting.video.captureResolution,
            currBroadcastSetting.video.encodeResolution,
            currBroadcastSetting.video.frameRate,
            currBroadcastSetting.video.bitRate,
            currBroadcastSetting.video.hardwareVideoEncoder,

            currBroadcastSetting.audio.inEarMonitoring,
            currBroadcastSetting.audio.recordingSignalVolume,
            currBroadcastSetting.audio.audioMixingVolume
        )

        if (isJoinedRoom) {
            if (lowStreamSetting == null) {
                updateRTCLowStreamSetting(
                    rtcConnection,
                    false,
                    isJoinedRoom = isJoinedRoom)
            } else {
                updateRTCLowStreamSetting(
                    rtcConnection,
                    true,
                    lowStreamSetting.encodeResolution,
                    lowStreamSetting.frameRate,
                    lowStreamSetting.bitRate,
                    lowStreamSetting.SVC,
                    lowStreamSetting.enableHardwareEncoder,
                    isJoinedRoom = isJoinedRoom)
            }
        }
    }

    fun updateBroadcastSetting(
        rtcConnection: RtcConnection? = null,
        isJoinedRoom: Boolean = true,

        h265: Boolean? = null,
        colorEnhance: Boolean? = null,
        lowLightEnhance: Boolean? = null,
        videoDenoiser: Boolean? = null,
        PVC: Boolean? = null,
        captureResolution: Resolution? = null,
        encoderResolution: Resolution? = null,
        frameRate: FrameRate? = null,
        bitRate: Int? = null,
        bitRateRecommand: Int? = null,
        bitRateStandard: Boolean? = null,

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {

        setCurrBroadcastSetting(
            BroadcastSetting(
                BroadcastSetting.Video(
                    h265 ?: currBroadcastSetting.video.H265,
                    colorEnhance ?: currBroadcastSetting.video.colorEnhance,
                    lowLightEnhance ?: currBroadcastSetting.video.lowLightEnhance,
                    videoDenoiser ?: currBroadcastSetting.video.videoDenoiser,
                    PVC ?: currBroadcastSetting.video.PVC,
                    captureResolution ?: currBroadcastSetting.video.captureResolution,
                    encoderResolution ?: currBroadcastSetting.video.encodeResolution,
                    frameRate ?: currBroadcastSetting.video.frameRate,
                     bitRate ?: currBroadcastSetting.video.bitRate,
                    bitRateRecommand ?: currBroadcastSetting.video.bitRateRecommand,
                    bitRateStandard ?: currBroadcastSetting.video.bitRateStandard,
                    true
                ),
                BroadcastSetting.Audio(
                    inEarMonitoring ?: currBroadcastSetting.audio.inEarMonitoring,
                    recordingSignalVolume ?: currBroadcastSetting.audio.recordingSignalVolume,
                    audioMixingVolume ?: currBroadcastSetting.audio.audioMixingVolume
                )
            )
        )


        var newBitRate = bitRate
        bitRateStandard?.let {
            newBitRate = if (it) {  // 自适应打开设置码率为 0，sdk 算法处理
                0
            } else {
                // 自适应关闭时候码率为推荐码率
                getRecommendBroadcastSetting().video.bitRateRecommand
            }
        }
        updateRTCBroadcastSetting(
            rtcConnection,
            isJoinedRoom,
            h265,
            colorEnhance,
            lowLightEnhance,
            videoDenoiser,
            PVC,
            captureResolution,
            encoderResolution,
            frameRate,
            newBitRate,
            true,

            inEarMonitoring,
            recordingSignalVolume,
            audioMixingVolume
        )

    }

    fun isCurrBroadcastSettingRecommend(): Boolean {
        return currBroadcastSetting == RecommendBroadcastSetting.LowDevice1v1
                || currBroadcastSetting == RecommendBroadcastSetting.MediumDevice1v1
                || currBroadcastSetting == RecommendBroadcastSetting.HighDevice1v1
                || currBroadcastSetting == RecommendBroadcastSetting.LowDevicePK
                || currBroadcastSetting == RecommendBroadcastSetting.MediumDevicePK
                || currBroadcastSetting == RecommendBroadcastSetting.HighDevicePK
    }

    // 推荐配置
    fun getRecommendBroadcastSetting(): BroadcastSetting {
        return if (isPkMode) {
            when (currAudienceDeviceLevel) {
                DeviceLevel.Low -> RecommendBroadcastSetting.LowDevicePK
                DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevicePK
                else -> RecommendBroadcastSetting.HighDevicePK
            }
        } else {
            when (currAudienceDeviceLevel) {
                DeviceLevel.Low -> RecommendBroadcastSetting.LowDevice1v1
                DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevice1v1
                else -> RecommendBroadcastSetting.HighDevice1v1
            }
        }
    }


    private fun updateRTCAudioSetting(SR: SuperResolution? = null) {
        val rtcEngine = RtcEngineInstance.rtcEngine
        if (rtcEngine.queryDeviceScore() < 75) {
            ToastUtils.showToast(R.string.show_audience_sr_tips)
        }
        SR?.let {
            val enableSR = currAudienceEnhanceSwitch && SR != SuperResolution.SR_NONE
            val autoSR = currAudienceEnhanceSwitch && SR == SuperResolution.SR_AUTO
            ShowLogger.d(
                "VideoSetting",
                "SR_Config -- enable=$enableSR sr_type=$SR currAudienceEnhanceSwitch=$currAudienceEnhanceSwitch"
            )

            if (enableSR) {
                if (autoSR) {
                    // 设置最大分辨率
                    rtcEngine.setParameters("{\"rtc.video.sr_max_wh\":921598}")
                    // 超分开关
                    rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":$enableSR, \"mode\": 2}}")
                    return
                }

                // 设置最大分辨率
                rtcEngine.setParameters("{\"rtc.video.sr_max_wh\":921598}")
                // 在切换时必须先关闭sr再设置倍数再打开,，即
                //i.   "rtc.video.enable_sr":("enabled": false, "mode" :2)
                //ii.  "rtc.video.sr_type"：（超分倍数类型 推荐3、7、20）
                //iii. "rtc.video.enable_sr": {"enabled": true, "mode":2)
                rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":false, \"mode\": 2}}")
                /**
                 * 超分倍数选项
                 * 1倍：      n=6
                 * 1.33倍:   n=7
                 * 1.5倍：   n=8
                 * 2倍：     n=3
                 * 锐化：    n=10(android是10，iOS是11)Å
                 * 超级画质： n=20
                 */
                rtcEngine.setParameters("{\"rtc.video.sr_type\":${SR.value}}")
            }
            // 超分开关
            rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":$enableSR, \"mode\": 2}}")
        }
    }


    private fun updateRTCBroadcastSetting(
        rtcConnection: RtcConnection? = null,
        isJoinedRoom: Boolean,

        h265: Boolean? = null,
        colorEnhance: Boolean? = null,
        lowLightEnhance: Boolean? = null,
        videoDenoiser: Boolean? = null,
        PVC: Boolean? = null,
        captureResolution: Resolution? = null,
        encoderResolution: Resolution? = null,
        frameRate: FrameRate? = null,
        bitRate: Int? = null,
        hardwareVideoEncoder: Boolean? = null,

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {
        ShowLogger.d("VideoSettings", "updateRTCBroadcastSetting, frameRate:$frameRate")
        val rtcEngine = RtcEngineInstance.rtcEngine
        val videoEncoderConfiguration = RtcEngineInstance.videoEncoderConfiguration
        h265?.let {
            if (!isJoinedRoom) {
                // 只能在加入房间前设置，否则rtc sdk会崩溃
                rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":${it}}")
                rtcEngine.setParameters("{\"che.video.videoCodecIndex\":${if(it) 2 else 1}}")
            }
        }
        colorEnhance?.let {
            rtcEngine.setColorEnhanceOptions(it, ColorEnhanceOptions())
        }
        lowLightEnhance?.let {
            rtcEngine.setLowlightEnhanceOptions(it, LowLightEnhanceOptions())
        }
        videoDenoiser?.let {
            when (currAudienceDeviceLevel) {
                DeviceLevel.High -> rtcEngine.setVideoDenoiserOptions(it, VideoDenoiserOptions(1, 2))
                DeviceLevel.Medium -> rtcEngine.setVideoDenoiserOptions(it, VideoDenoiserOptions(1, 0))
                DeviceLevel.Low -> rtcEngine.setVideoDenoiserOptions(it, VideoDenoiserOptions(1, 1))
            }
        }
        PVC?.let {
            // 1080p 有可能pvc自动关闭，设置私参提高pvc最大支持分辨率限制
            //rtcEngine.setParameters("{\"rtc.video.pvc_max_support_resolution\": 2073600}")
            // pvc 单帧耗时超过一定时间限制会自动关闭， 设置私参提高pvc最大支持单帧耗时
            //rtcEngine.setParameters("{\"rtc.video.maxCosttime4AIExt\": {\"pvc_max\": 20}}")
            rtcEngine.setParameters("{\"rtc.video.enable_pvc\":${it}}")
        }
        // 开发者模式下采集分辨率由开发者模式高级设置决定
        if (!AgoraApplication.the().isDebugModeOpen) {
            captureResolution?.let {
                val fps: Int = frameRate?.fps.let { getCurrBroadcastSetting().video.frameRate.fps }
                rtcEngine.setCameraCapturerConfiguration(CameraCapturerConfiguration(
                    CameraCapturerConfiguration.CaptureFormat(it.width, it.height, fps)
                ).apply {
                    followEncodeDimensionRatio = true
                })
            }
        }
        encoderResolution?.let {
            videoEncoderConfiguration.dimensions =
                VideoEncoderConfiguration.VideoDimensions(it.width, it.height)
            if (rtcConnection != null) {
                rtcEngine.setVideoEncoderConfigurationEx(videoEncoderConfiguration, rtcConnection)
            } else {
                rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
            }
        }
        frameRate?.let {
            videoEncoderConfiguration.frameRate = it.fps
            rtcEngine.setCameraCapturerConfiguration(
                CameraCapturerConfiguration(
                    CameraCapturerConfiguration.CaptureFormat(videoEncoderConfiguration.dimensions.width, videoEncoderConfiguration.dimensions.height, it.fps)
                ).apply {
                    followEncodeDimensionRatio = true
                })
            if (rtcConnection != null) {
                rtcEngine.setVideoEncoderConfigurationEx(videoEncoderConfiguration, rtcConnection)
            } else {
                rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
            }
        }
        bitRate?.let {
            videoEncoderConfiguration.bitrate = it
            if (rtcConnection != null) {
                rtcEngine.setVideoEncoderConfigurationEx(
                    videoEncoderConfiguration, rtcConnection
                )
            } else {
                rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
            }
        }
        hardwareVideoEncoder?.let {
            rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":\"$hardwareVideoEncoder\"}")
        }

        inEarMonitoring?.let {
            rtcEngine.enableInEarMonitoring(it)
        }
        recordingSignalVolume?.let {
            rtcEngine.adjustRecordingSignalVolume(it)
        }
        audioMixingVolume?.let {
            if (rtcConnection != null) {
                //videoLoaderApi.adjustAudioMixingVolume(rtcConnection, it)
            } else {
                rtcEngine.adjustAudioMixingVolume(it)
            }
        }
        // 默认开启人脸自动对焦
        rtcEngine.setCameraAutoFocusFaceModeEnabled(true)
    }

    private fun updateRTCLowStreamSetting(
        rtcConnection: RtcConnection? = null,
        enableLowStream: Boolean,

        encoderResolution: Resolution? = null,
        frameRate: FrameRate? = null,
        bitRate: Int? = null,
        svc: Boolean? = null,
        enableHardwareEncoder: Boolean? = null,
        isJoinedRoom: Boolean = false
    ) {
        ShowLogger.d("VideoSettings", "updateRTCLowStreamSetting, enableLowStream:$enableLowStream, svc:$svc")
        val rtcEngine = RtcEngineInstance.rtcEngine

        val connection = rtcConnection ?: return
        if (enableLowStream) {
            val resolution = encoderResolution ?: return
            val br = bitRate ?: return
            val fps = frameRate ?: return
            val enableSVC = svc ?: return

            // 1、小流开SVC需要软编码，关SVC改为硬编码
            // 2、小流开启的私有参数需要在setDualStreamModeEx前设置
            if (enableSVC) {
                rtcEngine.setParameters("{\"che.video.minor_stream_num_temporal_layers\": 2}")
                rtcEngine.setParameters("{\"rtc.video.high_low_video_ratio_enabled\": true}")
                rtcEngine.setParameters("{\"che.video.enable_264_fix_svc_nego\": false}")
                rtcEngine.setParameters("{\"rtc.video.low_stream_enable_hw_encoder\": false}")
            } else {
                rtcEngine.setParameters("{\"rtc.video.high_low_video_ratio_enabled\": false}")
                rtcEngine.setParameters("{\"rtc.video.low_stream_enable_hw_encoder\": true}")
            }

            rtcEngine.setDualStreamModeEx(
                Constants.SimulcastStreamMode.ENABLE_SIMULCAST_STREAM, SimulcastStreamConfig(
                    VideoEncoderConfiguration.VideoDimensions(
                        resolution.width, resolution.height
                    ), br, fps.fps), connection)
        } else {
            rtcEngine.setDualStreamModeEx(Constants.SimulcastStreamMode.DISABLE_SIMULCAST_STREAM,SimulcastStreamConfig(), connection)
        }
    }
}
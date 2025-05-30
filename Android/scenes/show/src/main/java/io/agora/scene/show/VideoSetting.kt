package io.agora.scene.show

import io.agora.rtc2.RtcConnection
import io.agora.rtc2.video.*
import io.agora.scene.base.Constant
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.utils.GsonTools
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.widget.toast.CustomToast

/*
 * HD Settings Module
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
        // 1x:      n=6
        // 1.33x:   n=7
        // 1.5x:    n=8
        // 2x:      n=3
        // Sharpen: n=10 (Android is 10, iOS is 11)
        // Super Quality: n=20
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

    // Audience side ---- Playback settings
    class AudiencePlaySetting {
        companion object {
            // Image enhancement, low-end device
            val ENHANCE_LOW = 0

            // Image enhancement, medium-end device
            val ENHANCE_MEDIUM = 1

            // Image enhancement, high-end device
            val ENHANCE_HIGH = 2

            // Basic mode, low-end device
            val BASE_LOW = 3

            // Basic mode, medium-end device
            val BASE_MEDIUM = 4

            // Basic mode, high-end device
            val BASE_HIGH = 5
        }
    }

    enum class LiveMode constructor(val value: Int) {
        OneVOne(0),
        PK(1)
    }

    /**
     * Audience settings
     */
    data class AudienceSetting constructor(val video: Video) {
        data class Video constructor(
            val SR: SuperResolution // Super Resolution
        )
    }

    /**
     * Broadcaster settings
     */
    data class BroadcastSetting constructor(
        val video: Video,
        val audio: Audio
    ) {
        data class Video constructor(
            val H265: Boolean, // Image enhancement
            val colorEnhance: Boolean, // Color enhancement
            val lowLightEnhance: Boolean, // Low light enhancement
            val videoDenoiser: Boolean, // Video noise reduction
            val PVC: Boolean, // Bitrate saving
            val captureResolution: Resolution, // Capture resolution
            val encodeResolution: Resolution, // Encoding resolution
            val frameRate: FrameRate, // Frame rate
            val bitRate: Int, // Bitrate
            val bitRateRecommand: Int,
            val bitRateStandard: Boolean, // Adaptive bitrate
            val hardwareVideoEncoder: Boolean
        )

        data class Audio constructor(
            val inEarMonitoring: Boolean, // In-ear monitoring
            val recordingSignalVolume: Int, // Voice volume
            val audioMixingVolume: Int, // Music volume
        )
    }

    /**
     * Recommended settings
     */
    object RecommendBroadcastSetting {

        val LowDevice1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = true,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = true,
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
                PVC = true,
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
                PVC = true,
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
                PVC = true,
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
                PVC = true,
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
                PVC = true,
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

    private var currAudienceSetting: AudienceSetting = getCurrAudienceSetting()
    private var currBroadcastSetting: BroadcastSetting = getCurrBroadcastSetting()

    // Current audience device level (high, medium, low)
    private var currAudienceDeviceLevel: DeviceLevel = DeviceLevel.valueOf(
        SPUtil.getString(Constant.CURR_AUDIENCE_DEVICE_LEVEL, DeviceLevel.Low.toString())
    )

    // Audience playback settings
    private var currAudiencePlaySetting: Int =
        SPUtil.getInt(Constant.CURR_AUDIENCE_PLAY_SETTING, AudiencePlaySetting.BASE_LOW)

    // Super resolution switch
    private var currAudienceEnhanceSwitch =
        SPUtil.getBoolean(Constant.CURR_AUDIENCE_ENHANCE_SWITCH, true)

    // Whether in pk mode, super resolution is not enabled for viewers in pk mode
    @Volatile
    private var isPkMode: Boolean = false

    fun setIsPkMode(isPkMode: Boolean) {
        this.isPkMode = isPkMode
    }

    fun getCurrAudienceSetting(): AudienceSetting {
        //
        val jsonStr = SPUtil.getString(Constant.CURR_AUDIENCE_SETTING, "")
        try {
            return GsonTools.toBean(jsonStr, AudienceSetting::class.java)!!
        } catch (e: java.lang.Exception) {
            val result = AudienceSetting(AudienceSetting.Video(SuperResolution.SR_NONE))
            setCurrAudienceSetting(result)
            return result
        }
    }

    fun getCurrBroadcastSetting(): BroadcastSetting {
        val jsonStr = SPUtil.getString(Constant.CURR_BROADCAST_SETTING, "")
        try {
            return GsonTools.toBean(jsonStr, BroadcastSetting::class.java)!!
        } catch (e: java.lang.Exception) {
            val result = RecommendBroadcastSetting.LowDevice1v1
            setCurrBroadcastSetting(result)
            return result
        }
    }

    fun getCurrAudiencePlaySetting() = currAudiencePlaySetting

    fun getCurrAudienceEnhanceSwitch() = currAudienceEnhanceSwitch

    fun setCurrAudienceSetting(audienceSetting: AudienceSetting) {
        SPUtil.putString(Constant.CURR_AUDIENCE_SETTING, GsonTools.beanToString(audienceSetting))
        currAudienceSetting = audienceSetting
    }

    fun setCurrBroadcastSetting(broadcastSetting: BroadcastSetting) {
        SPUtil.putString(Constant.CURR_BROADCAST_SETTING, GsonTools.beanToString(broadcastSetting))
        currBroadcastSetting = broadcastSetting
    }

    fun setCurrAudienceDeviceLevel(deviceLevel: DeviceLevel) {
        currAudienceDeviceLevel = deviceLevel
        SPUtil.putString(Constant.CURR_AUDIENCE_DEVICE_LEVEL, deviceLevel.toString())
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
     * Update broadcast settings
     * @param deviceLevel Device level: high, medium, low
     * @param networkLevel Network status: good, general
     * @param broadcastStrategy Broadcast strategy: clear priority, smooth priority
     * @param isJoinedRoom Whether the channel has been joined (some settings must be set before joining the channel)
     * @param isByAudience
     */
    fun updateBroadcastSetting(
        deviceLevel: DeviceLevel,
        isJoinedRoom: Boolean = false,
        isByAudience: Boolean = false,
        rtcConnection: RtcConnection? = null
    ) {
        ShowLogger.d("VideoSettings", "updateBroadcastSetting, deviceLevel:$deviceLevel")
        var liveMode = LiveMode.OneVOne
        if (isByAudience) {
            setCurrAudienceDeviceLevel(deviceLevel)
            return
        } else {
            setCurrAudienceDeviceLevel(deviceLevel)
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
        isJoinedRoom: Boolean,
        rtcConnection: RtcConnection? = null
    ) {
        ShowLogger.d("VideoSettings", "updateBroadcastSetting3")
        setCurrBroadcastSetting(recommendSetting)
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
            newBitRate = if (it) {  // Adaptive bitrate is set to 0, sdk algorithm handles
                0
            } else {
                // When adaptive bitrate is turned off, the bitrate is the recommended bitrate
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

    // Recommended configuration
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
            CustomToast.show(R.string.show_audience_sr_tips)
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
                    // Set maximum resolution
                    rtcEngine.setParameters("{\"rtc.video.sr_max_wh\":921598}")
                    // Super resolution switch
                    rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":$enableSR, \"mode\": 2}}")
                    return
                }

                // Set maximum resolution
                rtcEngine.setParameters("{\"rtc.video.sr_max_wh\":921598}")
                // When switching, must first turn off SR, then set multiplier, then turn on, i.e.
                //i.   "rtc.video.enable_sr":("enabled": false, "mode" :2)
                //ii.  "rtc.video.sr_type"：(SR multiplier type, recommended 3, 7, 20)
                //iii. "rtc.video.enable_sr": {"enabled": true, "mode":2)
                rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":false, \"mode\": 2}}")
                /**
                 * Super resolution multiplier options
                 * 1x:      n=6
                 * 1.33x:   n=7
                 * 1.5x:    n=8
                 * 2x:      n=3
                 * Sharpen: n=10 (Android is 10, iOS is 11)
                 * Super Quality: n=20
                 */
                rtcEngine.setParameters("{\"rtc.video.sr_type\":${SR.value}}")
            }
            // Super resolution switch
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
                // Can only be set before joining room, otherwise rtc sdk will crash
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
            // For 1080p, PVC may automatically turn off. Set private parameters to increase PVC maximum supported resolution limit
            //rtcEngine.setParameters("{\"rtc.video.pvc_max_support_resolution\": 2073600}")
            // PVC will automatically turn off if single frame processing time exceeds limit. Set private parameters to increase PVC maximum supported frame processing time
            //rtcEngine.setParameters("{\"rtc.video.maxCosttime4AIExt\": {\"pvc_max\": 20}}")
            // rtcEngine.setParameters("{\"rtc.video.enable_pvc\":${it}}")
        }
        // In developer mode, capture resolution is determined by advanced settings in developer mode
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
        // Default enable face auto focus
        rtcEngine.setCameraAutoFocusFaceModeEnabled(true)
    }
}
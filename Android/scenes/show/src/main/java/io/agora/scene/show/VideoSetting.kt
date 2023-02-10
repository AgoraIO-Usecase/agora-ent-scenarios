package io.agora.scene.show

import io.agora.rtc2.video.*
import io.agora.scene.base.Constant
import io.agora.scene.base.utils.GsonUtil
import io.agora.scene.base.utils.SPUtil

object VideoSetting {

    enum class SuperResolution(val value: Int) {
        //1倍：     n=6
        //1.33倍:  n=7
        //1.5倍：  n=8
        //2倍：     n=3
        //锐化：    n=10(android是10，iOS是11)
        SR_1(6),
        SR_1_33(7),
        SR_1_5(8),
        SR_2(3),
        SR_SHARP(10),
        SR_NONE(0)
    }

    enum class Resolution(val width: Int, val height: Int) {
        V_1080P(1920, 1080),
        V_720P(1280, 720),
        V_540P(480, 540),
        V_480P(854, 480),
        V_360P(640, 360),
        V_240P(360, 240)
    }

    fun Resolution.toIndex() = ResolutionList.indexOf(this)

    val ResolutionList = listOf(
        Resolution.V_240P,
        Resolution.V_360P,
        Resolution.V_480P,
        Resolution.V_540P,
        Resolution.V_720P
    )

    enum class FrameRate(val fps: Int) {
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
        FrameRate.FPS_24,
        FrameRate.FPS_30,
        FrameRate.FPS_60,
    )

    enum class DeviceLevel(val value: Int) {
        Low(0),
        Medium(1),
        High(2)
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

    enum class LiveMode(val value: Int) {
        OneVOne(0),
        PK(1)
    }

    /**
     * 观众设置
     */
    data class AudienceSetting(
        val video: Video
    ) {
        data class Video(
            val SR: SuperResolution // 超分
        )
    }

    /**
     * 主播设置
     */
    data class BroadcastSetting(
        val video: Video,
        val audio: Audio
    ) {
        data class Video(
            val H265: Boolean, // 画质增强
            val colorEnhance: Boolean, // 色彩增强
            val lowLightEnhance: Boolean, // 暗光增强
            val videoDenoiser: Boolean, // 视频降噪
            val PVC: Boolean, // 码率节省
            val captureResolution: Resolution, // 采集分辨率
            val encodeResolution: Resolution, // 编码分辨率
            val frameRate: FrameRate, // 帧率
            val bitRate: Int // 码率
        )

        data class Audio(
            val inEarMonitoring: Boolean, // 耳返
            val recordingSignalVolume: Int, // 人声音量
            val audioMixingVolume: Int, // 音乐音量
        )
    }

    /**
     * 推荐设置
     */
    object RecommendBroadcastSetting {

        val LowDevice1v1 = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = false,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_1080P,
                encodeResolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 1461
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
                frameRate = FrameRate.FPS_15,
                bitRate = 1461
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
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_24,
                bitRate = 2099
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val LowDevicePK = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = false,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_360P,
                frameRate = FrameRate.FPS_15,
                bitRate = 700
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
                captureResolution = Resolution.V_720P,
                encodeResolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 800
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
                encodeResolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 800
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

    }

    private var currAudienceSetting = AudienceSetting(AudienceSetting.Video(SuperResolution.SR_NONE))
    private var currBroadcastSetting: BroadcastSetting = getCurrBroadcastSetting()

    // 当前观众设备等级（高、中、低）
    private var currAudienceDeviceLevel: DeviceLevel = DeviceLevel.valueOf(SPUtil.getString(Constant.CURR_AUDIENCE_DEVICE_LEVEL, DeviceLevel.Low.toString()))

    // 观众看播设置
    private var currAudiencePlaySetting: Int = SPUtil.getInt(Constant.CURR_AUDIENCE_PLAY_SETTING, AudiencePlaySetting.BASE_LOW)

    // 超分开关
    private var currAudienceEnhanceSwitch = SPUtil.getBoolean(Constant.CURR_AUDIENCE_ENHANCE_SWITCH, true)

    fun getCurrAudienceSetting() = currAudienceSetting
    fun getCurrBroadcastSetting(): BroadcastSetting {
        val jsonStr = SPUtil.getString(Constant.CURR_BROADCAST_SETTING, "")
        try {
            return GsonUtil.getInstance().fromJson(jsonStr, BroadcastSetting::class.java)
        }
        catch (e: java.lang.Exception) {
            val result = RecommendBroadcastSetting.LowDevice1v1;
            setCurrBroadcastSetting(result)
            return result
        }
    }

    fun getCurrAudiencePlaySetting() = currAudiencePlaySetting

    fun getCurrAudienceEnhanceSwitch() = currAudienceEnhanceSwitch

    fun setCurrBroadcastSetting(broadcastSetting: BroadcastSetting) {
        SPUtil.putString(Constant.CURR_BROADCAST_SETTING, GsonUtil.instance.toJson(broadcastSetting))
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

    fun resetBroadcastSetting() {
        setCurrBroadcastSetting(when (currAudienceDeviceLevel) {
            DeviceLevel.Low -> RecommendBroadcastSetting.LowDevice1v1
            DeviceLevel.Medium -> RecommendBroadcastSetting.MediumDevice1v1
            DeviceLevel.High -> RecommendBroadcastSetting.HighDevice1v1
        })
    }

    fun updateAudienceSetting(
        isJoinedRoom: Boolean = true,
    ) {
        updateRTCAudioSetting(
            isJoinedRoom
        )
    }

    fun updateAudioSetting(isJoinedRoom: Boolean = false, SR: SuperResolution? = null) {
        currAudienceSetting = AudienceSetting(AudienceSetting.Video(SR ?: currAudienceSetting.video.SR))
        updateRTCAudioSetting(isJoinedRoom, SR)
    }

    fun updateBroadcastSetting(deviceLevel: DeviceLevel, isJoinedRoom: Boolean = false, isByAudience: Boolean = false) {
        var liveMode = LiveMode.OneVOne
        if (isByAudience) {
            setCurrAudienceDeviceLevel(deviceLevel)
        } else {
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
            isJoinedRoom
        )
    }

    fun updateBroadcastSetting(liveMode: LiveMode, isJoinedRoom: Boolean = true) {
        val deviceLevel = when (currBroadcastSetting) {
            RecommendBroadcastSetting.LowDevice1v1, RecommendBroadcastSetting.LowDevicePK -> DeviceLevel.Low
            RecommendBroadcastSetting.MediumDevice1v1, RecommendBroadcastSetting.MediumDevicePK -> DeviceLevel.Medium
            RecommendBroadcastSetting.HighDevice1v1, RecommendBroadcastSetting.HighDevicePK -> DeviceLevel.High
            else -> return
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
            isJoinedRoom
        )
    }

    private fun updateBroadcastSetting(recommendSetting: BroadcastSetting, isJoinedRoom: Boolean) {
        setCurrBroadcastSetting(recommendSetting)
        updateRTCBroadcastSetting(
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

            currBroadcastSetting.audio.inEarMonitoring,
            currBroadcastSetting.audio.recordingSignalVolume,
            currBroadcastSetting.audio.audioMixingVolume
        )
    }

    fun updateBroadcastSetting(
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

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {
        setCurrBroadcastSetting(BroadcastSetting(
            BroadcastSetting.Video(
                h265 ?: currBroadcastSetting.video.H265,
                colorEnhance ?: currBroadcastSetting.video.colorEnhance,
                lowLightEnhance ?: currBroadcastSetting.video.lowLightEnhance,
                videoDenoiser ?: currBroadcastSetting.video.videoDenoiser,
                PVC ?: currBroadcastSetting.video.PVC,
                captureResolution ?: currBroadcastSetting.video.captureResolution,
                encoderResolution ?: currBroadcastSetting.video.encodeResolution,
                frameRate ?: currBroadcastSetting.video.frameRate,
                bitRate ?: currBroadcastSetting.video.bitRate
            ),
            BroadcastSetting.Audio(
                inEarMonitoring ?: currBroadcastSetting.audio.inEarMonitoring,
                recordingSignalVolume ?: currBroadcastSetting.audio.recordingSignalVolume,
                audioMixingVolume ?: currBroadcastSetting.audio.audioMixingVolume
            )
        ))

        updateRTCBroadcastSetting(
            isJoinedRoom,
            h265,
            colorEnhance,
            lowLightEnhance,
            videoDenoiser,
            PVC,
            captureResolution,
            encoderResolution,
            frameRate,
            bitRate,
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


    private fun updateRTCAudioSetting(
        isJoinedRoom: Boolean, SR: SuperResolution? = null) {
        val rtcEngine = RtcEngineInstance.rtcEngine
        SR?.let {
            // 超分开关
            rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":${currAudienceEnhanceSwitch}, \"mode\": 2}}")
            if (!currAudienceEnhanceSwitch) {
                return
            }
            // 设置最大分辨率
            rtcEngine.setParameters("{\"rtc.video.sr_max_wh\":921600}")
            /**
             * 超分倍数选项
             * 1倍：      n=6
             * 1.33倍:   n=7
             * 1.5倍：   n=8
             * 2倍：     n=3
             * 锐化：    n=10(android是10，iOS是11)
             */
            rtcEngine.setParameters("{\"rtc.video.sr_type\":${SR.value}}")
        }
    }


    private fun updateRTCBroadcastSetting(
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

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {
        val rtcEngine = RtcEngineInstance.rtcEngine
        val videoEncoderConfiguration = RtcEngineInstance.videoEncoderConfiguration
        h265?.let {
            if (!isJoinedRoom) {
                // 只能在加入房间前设置，否则rtc sdk会崩溃
                rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":${it}}")
                rtcEngine.setParameters("{\"engine.video.codec_type\":\"${if (it) 3 else 2}\"}")
            }
        }
        colorEnhance?.let {
            rtcEngine.setColorEnhanceOptions(it, ColorEnhanceOptions())
        }
        lowLightEnhance?.let {
            rtcEngine.setLowlightEnhanceOptions(it, LowLightEnhanceOptions())
        }
        videoDenoiser?.let {
            rtcEngine.setVideoDenoiserOptions(it, VideoDenoiserOptions())
        }
        PVC?.let {
            // RTC 4.0.0.9版本 不支持，强行设置rtc sdk会崩溃
            // rtcEngine.setParameters("{\"rtc.video.enable_pvc\":${it}}")
        }
        captureResolution?.let {
            rtcEngine.setCameraCapturerConfiguration(CameraCapturerConfiguration(
                CameraCapturerConfiguration.CaptureFormat(it.width, it.height, 15)
            ).apply {
                followEncodeDimensionRatio = false
            })
        }
        encoderResolution?.let {
            videoEncoderConfiguration.dimensions =
                VideoEncoderConfiguration.VideoDimensions(it.width, it.height)
            rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
        }
        frameRate?.let {
            videoEncoderConfiguration.frameRate = it.fps
            rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
        }
        bitRate?.let {
            videoEncoderConfiguration.bitrate = it
            rtcEngine.setVideoEncoderConfiguration(videoEncoderConfiguration)
        }

        inEarMonitoring?.let {
            rtcEngine.enableInEarMonitoring(it)
        }
        recordingSignalVolume?.let {
            rtcEngine.adjustRecordingSignalVolume(it)
        }
        audioMixingVolume?.let {
            rtcEngine.adjustAudioMixingVolume(it)
        }
    }

}
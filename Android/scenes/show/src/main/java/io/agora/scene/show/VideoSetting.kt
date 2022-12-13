package io.agora.scene.show

import io.agora.rtc2.video.ColorEnhanceOptions
import io.agora.rtc2.video.LowLightEnhanceOptions
import io.agora.rtc2.video.VideoDenoiserOptions
import io.agora.rtc2.video.VideoEncoderConfiguration

object VideoSetting {

    enum class Resolution(val width: Int, val height: Int) {
        V_1080P(1920, 1080),
        V_720P(1080, 720),
        V_540P(960, 540),
        V_360P(640, 360),
        V_270P(480, 270),
        V_180P(360, 180),
    }

    fun Resolution.toIndex() = ResolutionList.indexOf(this)


    val ResolutionList = listOf(
        Resolution.V_180P,
        Resolution.V_270P,
        Resolution.V_360P,
        Resolution.V_540P,
        Resolution.V_720P,
        Resolution.V_1080P
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
            val SR: Boolean // 超分
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
            val resolution: Resolution, // 分辨率
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
                resolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 1500
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
                resolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_15,
                bitRate = 1800
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
                resolution = Resolution.V_720P,
                frameRate = FrameRate.FPS_15,
                bitRate = 1800
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
                resolution = Resolution.V_360P,
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
                resolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 800
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

        val HighDevicePK = BroadcastSetting(
            BroadcastSetting.Video(
                H265 = false,
                colorEnhance = false,
                lowLightEnhance = false,
                videoDenoiser = false,
                PVC = false,
                resolution = Resolution.V_540P,
                frameRate = FrameRate.FPS_15,
                bitRate = 800
            ),
            BroadcastSetting.Audio(false, 80, 30)
        )

    }

    private var currAudienceSetting = AudienceSetting(AudienceSetting.Video(false))
    private var currBroadcastSetting = RecommendBroadcastSetting.LowDevice1v1

    fun getCurrAudienceSetting() = currAudienceSetting
    fun getCurrBroadcastSetting() = currBroadcastSetting

    fun updateAudienceSetting(){
        updateRTCAudioSetting(currAudienceSetting.video.SR)
    }

    fun updateAudioSetting(
        SR: Boolean? = null
    ) {
        currAudienceSetting = AudienceSetting(
            AudienceSetting.Video(
                SR ?: currAudienceSetting.video.SR
            )
        )

        updateRTCAudioSetting(SR)
    }

    fun updateBroadcastSetting(deviceLevel: DeviceLevel) {
        val liveMode = when (currBroadcastSetting) {
            RecommendBroadcastSetting.LowDevice1v1, RecommendBroadcastSetting.MediumDevice1v1, RecommendBroadcastSetting.HighDevice1v1 -> LiveMode.OneVOne
            RecommendBroadcastSetting.LowDevicePK, RecommendBroadcastSetting.MediumDevicePK, RecommendBroadcastSetting.HighDevicePK -> LiveMode.PK
            else -> LiveMode.OneVOne
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
            }
        )
    }

    fun updateBroadcastSetting(liveMode: LiveMode) {
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
            }
        )
    }

    private fun updateBroadcastSetting(recommendSetting: BroadcastSetting) {
        currBroadcastSetting = recommendSetting
        updateRTCBroadcastSetting(
            currBroadcastSetting.video.H265,
            currBroadcastSetting.video.colorEnhance,
            currBroadcastSetting.video.lowLightEnhance,
            currBroadcastSetting.video.videoDenoiser,
            currBroadcastSetting.video.PVC,
            currBroadcastSetting.video.resolution,
            currBroadcastSetting.video.frameRate,
            currBroadcastSetting.video.bitRate,

            currBroadcastSetting.audio.inEarMonitoring,
            currBroadcastSetting.audio.recordingSignalVolume,
            currBroadcastSetting.audio.audioMixingVolume
        )
    }

    fun updateBroadcastSetting(
        h265: Boolean? = null,
        colorEnhance: Boolean? = null,
        lowLightEnhance: Boolean? = null,
        videoDenoiser: Boolean? = null,
        PVC: Boolean? = null,
        resolution: Resolution? = null,
        frameRate: FrameRate? = null,
        bitRate: Int? = null,

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {
        currBroadcastSetting = BroadcastSetting(
            BroadcastSetting.Video(
                h265 ?: currBroadcastSetting.video.H265,
                colorEnhance ?: currBroadcastSetting.video.colorEnhance,
                lowLightEnhance ?: currBroadcastSetting.video.lowLightEnhance,
                videoDenoiser ?: currBroadcastSetting.video.videoDenoiser,
                PVC ?: currBroadcastSetting.video.PVC,
                resolution ?: currBroadcastSetting.video.resolution,
                frameRate ?: currBroadcastSetting.video.frameRate,
                bitRate ?: currBroadcastSetting.video.bitRate
            ),
            BroadcastSetting.Audio(
                inEarMonitoring ?: currBroadcastSetting.audio.inEarMonitoring,
                recordingSignalVolume ?: currBroadcastSetting.audio.recordingSignalVolume,
                audioMixingVolume ?: currBroadcastSetting.audio.audioMixingVolume
            )
        )

        updateRTCBroadcastSetting(
            h265,
            colorEnhance,
            lowLightEnhance,
            videoDenoiser,
            PVC,
            resolution,
            frameRate,
            bitRate,
            inEarMonitoring
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
        SR: Boolean? = null
    ) {
        val rtcEngine = RtcEngineInstance.rtcEngine
        SR?.let { rtcEngine.setParameters("{\"rtc.video.enable_sr\":{\"enabled\":${it}, \"mode\": 2}}") }
    }


    private fun updateRTCBroadcastSetting(
        h265: Boolean? = null,
        colorEnhance: Boolean? = null,
        lowLightEnhance: Boolean? = null,
        videoDenoiser: Boolean? = null,
        PVC: Boolean? = null,
        resolution: Resolution? = null,
        frameRate: FrameRate? = null,
        bitRate: Int? = null,

        inEarMonitoring: Boolean? = null,
        recordingSignalVolume: Int? = null,
        audioMixingVolume: Int? = null
    ) {
        val rtcEngine = RtcEngineInstance.rtcEngine
        val videoEncoderConfiguration = RtcEngineInstance.videoEncoderConfiguration
        h265?.let {
            rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":${it}}")
            rtcEngine.setParameters("{\"engine.video.codec_type\":\"${if (it) 3 else 2}\"}")
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
            // RTC 4.0.0.9版本 不支持
            // rtcEngine.setParameters("{\"rtc.video.enable_pvc\":${it}}")
        }
        resolution?.let {
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
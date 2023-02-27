package io.agora.scene.show.debugSettings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.switchmaterial.SwitchMaterial
import io.agora.rtc2.Constants.*
import io.agora.rtc2.video.ColorEnhanceOptions
import io.agora.rtc2.video.LowLightEnhanceOptions
import io.agora.rtc2.video.VideoDenoiserOptions
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.databinding.ShowWidgetDebugSettingDialogBinding
import io.agora.scene.show.widget.BottomFullDialog

class DebugSettingDialog(context: Context) : BottomFullDialog(context) {
    private val TAG = "DebugSettings"
    private val mBinding by lazy {
        ShowWidgetDebugSettingDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    init {
        setContentView(mBinding.root)

        mBinding.ivBack.setOnClickListener {
            dismiss()
        }
        // 采集帧率
        setText(mBinding.etFpsCapture, RtcEngineInstance.videoCaptureConfiguration.captureFormat.fps.toString())
        // 采集分辨率
        setText(mBinding.etResolutionWidthCapture, RtcEngineInstance.videoCaptureConfiguration.captureFormat.width.toString())
        setText(mBinding.etResolutionHeightCapture, RtcEngineInstance.videoCaptureConfiguration.captureFormat.height.toString())

        // 帧率
        setText(mBinding.etFps, RtcEngineInstance.videoEncoderConfiguration.frameRate.toString())
        // 分辨率
        setText(mBinding.etResolutionWidth, RtcEngineInstance.videoEncoderConfiguration.dimensions.width.toString())
        setText(mBinding.etResolutionHeight, RtcEngineInstance.videoEncoderConfiguration.dimensions.height.toString())
        // 码率
        setText(mBinding.etBitrate, RtcEngineInstance.videoEncoderConfiguration.bitrate.toString())

        // pvc
        setEnable(mBinding.pvcSwitchCompat, RtcEngineInstance.debugSettingModel.pvcEnabled)
        // 人脸对焦
        setEnable(mBinding.focusFaceSwitchCompat, RtcEngineInstance.debugSettingModel.autoFocusFaceModeEnabled)
        // 曝光区域
        setText(mBinding.etExposureX, RtcEngineInstance.debugSettingModel.exposurePositionX.toString())
        setText(mBinding.etExposureY, RtcEngineInstance.debugSettingModel.exposurePositionY.toString())
        // camera 切换
        setText(mBinding.etSwitchCamera, RtcEngineInstance.debugSettingModel.cameraSelect.toString())
        // 颜色空间
        setText(mBinding.etvideoFullrangeExt, RtcEngineInstance.debugSettingModel.videoFullrangeExt.toString())
        setText(mBinding.etmatrixCoefficientsExt, RtcEngineInstance.debugSettingModel.matrixCoefficientsExt.toString())
        // 硬编/软编
        if (RtcEngineInstance.debugSettingModel.enableHWEncoder) {
            setSelect(mBinding.encoderRadioBox, 0)
        } else {
            setSelect(mBinding.encoderRadioBox, 1)
        }
        // 编码器
        if (RtcEngineInstance.debugSettingModel.codecType == 3) {
            setSelect(mBinding.codecRadioBox, 0)
        } else {
            setSelect(mBinding.codecRadioBox, 1)
        }
        // 镜像
        setEnable(mBinding.mirrorSwitchCompat, RtcEngineInstance.debugSettingModel.mirrorMode)
        // hidden / fix
        if (RtcEngineInstance.debugSettingModel.renderMode == 1) {
            setSelect(mBinding.fixModeRadioBox, 0)
        } else {
            setSelect(mBinding.fixModeRadioBox, 1)
        }
        // 色彩增强
        setEnable(mBinding.colorSwitchCompat, RtcEngineInstance.debugSettingModel.colorEnhance)
        // 暗光增强
        setEnable(mBinding.darkSwitchCompat, RtcEngineInstance.debugSettingModel.dark)
        // 视频降噪
        setEnable(mBinding.noiseSwitchCompat, RtcEngineInstance.debugSettingModel.noise)

        mBinding.tvSure.visibility = View.GONE

        // 采集帧率
        mBinding.etFpsCapture.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etFpsCapture = mBinding.etFpsCapture.text.toString().toIntOrNull()?: 30
                RtcEngineInstance.videoCaptureConfiguration.captureFormat.fps = etFpsCapture
                RtcEngineInstance.rtcEngine.setCameraCapturerConfiguration(RtcEngineInstance.videoCaptureConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.captureFormat.fps: $etFpsCapture")
            }
        }

        // 采集分辨率
        mBinding.etResolutionWidthCapture.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etResolutionWidthCapture = mBinding.etResolutionWidthCapture.text.toString().toIntOrNull()?: 720
                val etResolutionHeightCapture = mBinding.etResolutionHeightCapture.text.toString().toIntOrNull()?: 1080
                RtcEngineInstance.videoCaptureConfiguration.captureFormat.width = etResolutionWidthCapture
                RtcEngineInstance.rtcEngine.setCameraCapturerConfiguration(RtcEngineInstance.videoCaptureConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.captureFormat: $etResolutionWidthCapture, $etResolutionHeightCapture")
            }
        }

        mBinding.etResolutionHeightCapture.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etResolutionWidthCapture = mBinding.etResolutionWidthCapture.text.toString().toIntOrNull()?: 720
                val etResolutionHeightCapture = mBinding.etResolutionHeightCapture.text.toString().toIntOrNull()?: 1080
                RtcEngineInstance.videoCaptureConfiguration.captureFormat.height = etResolutionHeightCapture
                RtcEngineInstance.rtcEngine.setCameraCapturerConfiguration(RtcEngineInstance.videoCaptureConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.captureFormat: $etResolutionWidthCapture, $etResolutionHeightCapture")
            }
        }

        // 编码帧率
        mBinding.etFps.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etFps = mBinding.etFps.text.toString().toIntOrNull()?: 30
                RtcEngineInstance.videoEncoderConfiguration.frameRate = etFps
                RtcEngineInstance.rtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.frameRate: $etFps")
            }
        }

        // 编码分辨率
        mBinding.etResolutionWidth.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etResolutionWidth = mBinding.etResolutionWidth.text.toString().toIntOrNull()?: 720
                val etResolutionHeight = mBinding.etResolutionHeight.text.toString().toIntOrNull()?: 1080
                RtcEngineInstance.videoEncoderConfiguration.dimensions.width = etResolutionWidth
                RtcEngineInstance.videoEncoderConfiguration.dimensions.height = etResolutionHeight
                RtcEngineInstance.rtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.dimensions: $etResolutionWidth, $etResolutionHeight")
            }
        }

        mBinding.etResolutionHeight.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val etResolutionWidth = mBinding.etResolutionWidth.text.toString().toIntOrNull()?: 720
                val etResolutionHeight = mBinding.etResolutionHeight.text.toString().toIntOrNull()?: 1080
                RtcEngineInstance.videoEncoderConfiguration.dimensions.width = etResolutionWidth
                RtcEngineInstance.videoEncoderConfiguration.dimensions.height = etResolutionHeight
                RtcEngineInstance.rtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.dimensions: $etResolutionWidth, $etResolutionHeight")
            }
        }

        // 码率
        mBinding.etBitrate.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val bitrate = mBinding.etBitrate.text.toString().toIntOrNull()?: 720
                RtcEngineInstance.videoEncoderConfiguration.bitrate = bitrate
                RtcEngineInstance.rtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)
                ShowLogger.d(TAG, "videoCaptureConfiguration.bitrate: $bitrate")
            }
        }

        // PVC
        mBinding.pvcSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.pvcEnabled = isOpen
            RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_pvc\":$isOpen}")
            ShowLogger.d(TAG, "rtc.video.enable_pvc: $isOpen")
        }

        // 人脸对焦
        mBinding.focusFaceSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.autoFocusFaceModeEnabled = isOpen
            RtcEngineInstance.rtcEngine.setCameraAutoFocusFaceModeEnabled(isOpen)
            ShowLogger.d(TAG, "focusFace: $isOpen")
        }

        // 曝光区域
        mBinding.etExposureX.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val exposureX = mBinding.etExposureX.text.toString().toFloatOrNull()
                val exposureY = mBinding.etExposureY.text.toString().toFloatOrNull()
                RtcEngineInstance.debugSettingModel.exposurePositionX = exposureX
                if (exposureX != null && exposureY != null) {
                    RtcEngineInstance.rtcEngine.setCameraExposurePosition(exposureX, exposureY)
                    ShowLogger.d(TAG, "setCameraExposurePosition: $exposureX,$exposureY")
                }
            }
        }

        mBinding.etExposureY.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val exposureX = mBinding.etExposureX.text.toString().toFloatOrNull()
                val exposureY = mBinding.etExposureY.text.toString().toFloatOrNull()
                RtcEngineInstance.debugSettingModel.exposurePositionY = exposureY
                if (exposureX != null && exposureY != null) {
                    RtcEngineInstance.rtcEngine.setCameraExposurePosition(exposureX, exposureY)
                    ShowLogger.d(TAG, "setCameraExposurePosition: $exposureX,$exposureY")
                }
            }
        }

        // 相机切换
        mBinding.etSwitchCamera.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val cameraNum = mBinding.etSwitchCamera.text.toString().toIntOrNull()
                RtcEngineInstance.debugSettingModel.cameraSelect = cameraNum
                if (cameraNum != null && (cameraNum == 0 || cameraNum == 1)) {
                    RtcEngineInstance.rtcEngine.setParameters("{\"che.video.android_camera_select\":$cameraNum}")
                    ShowLogger.d(TAG, "che.video.android_camera_select: $cameraNum")
                }
            }
        }

        // 颜色空间
        mBinding.etvideoFullrangeExt.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val videoFullRangeExt = mBinding.etvideoFullrangeExt.text.toString().toIntOrNull()
                RtcEngineInstance.debugSettingModel.videoFullrangeExt = videoFullRangeExt
                if (videoFullRangeExt != null && (videoFullRangeExt == 0 || videoFullRangeExt == 1)) {
                    RtcEngineInstance.rtcEngine.setParameters("{\"che.video.videoFullrangeExt\":$videoFullRangeExt}")
                    ShowLogger.d(TAG, "che.video.videoFullrangeExt: $videoFullRangeExt")
                }
            }
        }

        mBinding.etmatrixCoefficientsExt.onFocusChangeListener = object : View.OnFocusChangeListener {
            override fun onFocusChange(p0: View?, isFocus: Boolean) {
                if (isFocus) return
                val matrixCoefficientsExt = mBinding.etmatrixCoefficientsExt.text.toString().toIntOrNull()
                RtcEngineInstance.debugSettingModel.matrixCoefficientsExt = matrixCoefficientsExt
                if (matrixCoefficientsExt != null && (matrixCoefficientsExt == 0 || matrixCoefficientsExt == 1)) {
                    RtcEngineInstance.rtcEngine.setParameters("{\"che.video.matrixCoefficientsExt\":$matrixCoefficientsExt}")
                    ShowLogger.d(TAG, "che.video.matrixCoefficientsExt: $matrixCoefficientsExt")
                }
            }
        }

        // 硬编/软编
        mBinding.encoderRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    // 硬编
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":\"true\"}")
                    RtcEngineInstance.debugSettingModel.enableHWEncoder = true
                    ShowLogger.d(TAG, "engine.video.enable_hw_encoder: true")
                } else if (p2 == 1) {
                    // 软编
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":\"false\"}")
                    RtcEngineInstance.debugSettingModel.enableHWEncoder = false
                    ShowLogger.d(TAG, "engine.video.enable_hw_encoder: false")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 编码器
        mBinding.codecRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    // h265
                    RtcEngineInstance.debugSettingModel.codecType = 3
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.codec_type\":\"3\"}")
                    ShowLogger.d(TAG, "engine.video.codec_type: 3(h265)")
                } else if (p2 == 1) {
                    // h264
                    RtcEngineInstance.debugSettingModel.codecType = 2
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.codec_type\":\"2\"}")
                    ShowLogger.d(TAG, "engine.video.codec_type: 2(h264)")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 镜像
        mBinding.mirrorSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            // setUpLocalVideo videoCanvas mirrorMode
            RtcEngineInstance.debugSettingModel.mirrorMode = isOpen
            val mirrorMode = if (isOpen)  VIDEO_MIRROR_MODE_ENABLED else VIDEO_MIRROR_MODE_DISABLED
            val renderMode = if (RtcEngineInstance.debugSettingModel.renderMode == 1)  RENDER_MODE_HIDDEN else RENDER_MODE_FIT
            RtcEngineInstance.rtcEngine.setLocalRenderMode(renderMode, mirrorMode)
            ShowLogger.d(TAG, "setLocalRenderMode mirrorMode: $mirrorMode, renderMode: $renderMode")
        }

        // 渲染模式
        mBinding.fixModeRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                // setUpLocalVideo videoCanvas renderMode
                if (p2 == 0) {
                    // hidden
                    RtcEngineInstance.debugSettingModel.renderMode = 1
                    val mirrorMode = if (RtcEngineInstance.debugSettingModel.mirrorMode)  VIDEO_MIRROR_MODE_ENABLED else VIDEO_MIRROR_MODE_DISABLED
                    RtcEngineInstance.rtcEngine.setLocalRenderMode(RENDER_MODE_HIDDEN, mirrorMode)
                    ShowLogger.d(TAG, "setLocalRenderMode mirrorMode: $mirrorMode, renderMode: 1(hidden)")
                } else if (p2 == 1) {
                    // fit
                    RtcEngineInstance.debugSettingModel.renderMode = 2
                    val mirrorMode = if (RtcEngineInstance.debugSettingModel.mirrorMode)  VIDEO_MIRROR_MODE_ENABLED else VIDEO_MIRROR_MODE_DISABLED
                    RtcEngineInstance.rtcEngine.setLocalRenderMode(RENDER_MODE_FIT, mirrorMode)
                    ShowLogger.d(TAG, "setLocalRenderMode mirrorMode: $mirrorMode, renderMode: 2(fit)")
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 色彩增强
        mBinding.colorSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.colorEnhance = isOpen
            RtcEngineInstance.rtcEngine.setColorEnhanceOptions(isOpen, ColorEnhanceOptions())
            ShowLogger.d(TAG, "colorEnhance: $isOpen")
        }

        // 暗光增强
        mBinding.darkSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.dark = isOpen
            RtcEngineInstance.rtcEngine.setLowlightEnhanceOptions(isOpen, LowLightEnhanceOptions())
            ShowLogger.d(TAG, "dark: $isOpen")
        }

        // 视频降噪
        mBinding.noiseSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.debugSettingModel.noise = isOpen
            RtcEngineInstance.rtcEngine.setVideoDenoiserOptions(isOpen, VideoDenoiserOptions())
            ShowLogger.d(TAG, "noise: $isOpen")
        }
    }

    private fun setText(editText: EditText, content: String) {
        editText.setText(content)
        editText.setSelection(content.length)
    }

    private fun setEnable(switch: SwitchMaterial, enabled: Boolean) {
        switch.isChecked = enabled
    }

    private fun setSelect(select: Spinner, num: Int) {
        select.setSelection(num)
    }
}
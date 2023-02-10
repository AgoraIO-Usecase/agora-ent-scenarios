package io.agora.scene.show.debugSettings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import com.google.android.material.switchmaterial.SwitchMaterial
import io.agora.rtc2.video.ColorEnhanceOptions
import io.agora.rtc2.video.LowLightEnhanceOptions
import io.agora.rtc2.video.VideoDenoiserOptions
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.databinding.ShowWidgetDebugSettingDialogBinding
import io.agora.scene.show.widget.BottomFullDialog

class DebugSettingDialog(context: Context) : BottomFullDialog(context) {

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
        // hit / hidden
        if (RtcEngineInstance.debugSettingModel.fitMode == 0) {
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

        mBinding.tvSure.setOnClickListener {
            RtcEngineInstance.videoCaptureConfiguration.captureFormat.fps = mBinding.etFpsCapture.text.toString().toIntOrNull()?: 30
            RtcEngineInstance.videoCaptureConfiguration.captureFormat.width = mBinding.etResolutionWidthCapture.text.toString().toIntOrNull()?: 720
            RtcEngineInstance.videoCaptureConfiguration.captureFormat.height = mBinding.etResolutionHeightCapture.text.toString().toIntOrNull()?: 1080
            RtcEngineInstance.rtcEngine.setCameraCapturerConfiguration(RtcEngineInstance.videoCaptureConfiguration)

            RtcEngineInstance.videoEncoderConfiguration.frameRate = mBinding.etFps.text.toString().toIntOrNull()?: 30
            RtcEngineInstance.videoEncoderConfiguration.dimensions.width = mBinding.etResolutionWidth.text.toString().toIntOrNull()?: 720
            RtcEngineInstance.videoEncoderConfiguration.dimensions.height = mBinding.etResolutionHeight.text.toString().toIntOrNull()?: 1080
            RtcEngineInstance.videoEncoderConfiguration.bitrate = mBinding.etBitrate.text.toString().toIntOrNull()?: 720
            RtcEngineInstance.rtcEngine.setVideoEncoderConfiguration(RtcEngineInstance.videoEncoderConfiguration)

            RtcEngineInstance.debugSettingModel.pvcEnabled = mBinding.pvcSwitchCompat.isChecked
            RtcEngineInstance.debugSettingModel.autoFocusFaceModeEnabled = mBinding.focusFaceSwitchCompat.isChecked
            RtcEngineInstance.debugSettingModel.mirrorMode = mBinding.mirrorSwitchCompat.isChecked
            RtcEngineInstance.debugSettingModel.colorEnhance = mBinding.colorSwitchCompat.isChecked
            RtcEngineInstance.debugSettingModel.dark = mBinding.darkSwitchCompat.isChecked
            RtcEngineInstance.debugSettingModel.noise = mBinding.noiseSwitchCompat.isChecked

            // 曝光区域 TODO
            val exposureX = mBinding.etExposureX.text.toString().toFloatOrNull()
            val exposureY = mBinding.etExposureY.text.toString().toFloatOrNull()
            RtcEngineInstance.debugSettingModel.exposurePositionX = exposureX
            RtcEngineInstance.debugSettingModel.exposurePositionY = exposureY
            if (exposureX != null && exposureY != null) {
                RtcEngineInstance.rtcEngine.setCameraExposurePosition(exposureX, exposureY)
            }

            // camera 切换
            val cameraNum = mBinding.etSwitchCamera.text.toString().toIntOrNull()
            RtcEngineInstance.debugSettingModel.cameraSelect = cameraNum
            if (cameraNum != null && (cameraNum == 1 || cameraNum == 2)) {
                RtcEngineInstance.rtcEngine.setParameters("{\"che.video.android_camera_select\":$cameraNum}")
            }

            // 颜色空间
            val videoFullrangeExt = mBinding.etvideoFullrangeExt.text.toString().toIntOrNull()
            RtcEngineInstance.debugSettingModel.videoFullrangeExt = videoFullrangeExt
            if (videoFullrangeExt != null && (videoFullrangeExt == 0 || videoFullrangeExt == 1)) {
                RtcEngineInstance.rtcEngine.setParameters("{\"che.video.videoFullrangeExt\":$videoFullrangeExt}")
            }
            val matrixCoefficientsExt = mBinding.etmatrixCoefficientsExt.text.toString().toIntOrNull()
            RtcEngineInstance.debugSettingModel.matrixCoefficientsExt = matrixCoefficientsExt
            if (matrixCoefficientsExt != null && (matrixCoefficientsExt == 0 || matrixCoefficientsExt == 1)) {
                RtcEngineInstance.rtcEngine.setParameters("{\"che.video.matrixCoefficientsExt\":$matrixCoefficientsExt}")
            }

            dismiss()
        }

        // PVC
        mBinding.pvcSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.rtcEngine.setParameters("{\"rtc.video.enable_pvc\":$isOpen}")
        }

        // 人脸对焦
        mBinding.focusFaceSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.rtcEngine.setCameraAutoFocusFaceModeEnabled(isOpen)
        }

        // 硬编/软编
        mBinding.encoderRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    // 硬编
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":true}")
                    RtcEngineInstance.debugSettingModel.enableHWEncoder = true
                } else if (p2 == 1) {
                    // 软编
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.enable_hw_encoder\":false}")
                    RtcEngineInstance.debugSettingModel.enableHWEncoder = false
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
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.codec_type\":3}")
                    RtcEngineInstance.debugSettingModel.codecType = 3
                } else if (p2 == 1) {
                    // h264
                    RtcEngineInstance.rtcEngine.setParameters("{\"engine.video.codec_type\":2}")
                    RtcEngineInstance.debugSettingModel.codecType = 2
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 镜像
        mBinding.mirrorSwitchCompat.setOnCheckedChangeListener { _, isOpen ->

        }


        // 渲染模式
        mBinding.fixModeRadioBox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == 0) {
                    // hidden
                    RtcEngineInstance.debugSettingModel.fitMode = 0
                } else if (p2 == 1) {
                    // fit
                    RtcEngineInstance.debugSettingModel.fitMode = 1
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        // 色彩增强
        mBinding.colorSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.rtcEngine.setColorEnhanceOptions(isOpen, ColorEnhanceOptions())
        }

        // 暗光增强
        mBinding.darkSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.rtcEngine.setLowlightEnhanceOptions(isOpen, LowLightEnhanceOptions())
        }

        // 视频降噪
        mBinding.noiseSwitchCompat.setOnCheckedChangeListener { _, isOpen ->
            RtcEngineInstance.rtcEngine.setVideoDenoiserOptions(isOpen, VideoDenoiserOptions())
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
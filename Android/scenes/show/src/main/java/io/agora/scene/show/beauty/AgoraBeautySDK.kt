package io.agora.scene.show.beauty

import android.content.Context
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.IVideoEffectObject
import io.agora.rtc2.video.FaceShapeAreaOptions
import io.agora.scene.base.utils.SPUtil
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.utils.FileUtils

object AgoraBeautySDK {
    private const val TAG = "AgoraBeautySDK"
    private var rtcEngine: RtcEngine? = null
    private var beautyEffect: IVideoEffectObject? = null

    private var beautyEnable = false
    private var filterEnable = false
    private var makeupEnable = false
    private var stickerEnable = false

    private var useLocalBeautyResource = true
    private const val assetsPath = "beauty_agora"

    private var materialPath = ""
    private var materialCopied = false

    // Model type constants for beauty algorithm selection
    private const val KEY_MODEL_TYPE = "show_model_type"
    const val MODEL_TYPE_ADAPTIVE = 0
    const val MODEL_TYPE_LARGE = 1
    const val MODEL_TYPE_SMALL = 2

    // 美颜配置
    val beautyConfig = BeautyConfig()

    fun initBeautySDK(context: Context, rtcEngine: RtcEngine, useLocalBeautyResource: Boolean): Boolean {
        val storagePath = context.getExternalFilesDir("")?.absolutePath ?: return false
        if (!materialCopied) {
            val destPath = "$storagePath/beauty_agora"
            FileUtils.copyAssets(context, assetsPath, destPath)
            materialCopied = true
        }
        materialPath = "$storagePath/beauty_agora/beauty_material_functional"

        this.rtcEngine = rtcEngine

        // Set model type private parameter before createVideoEffectObject
        setModelTypeParameter(rtcEngine, context)

        // Enable extension (may already be enabled, but it's safe to call again)
        val ret = rtcEngine.enableExtension(
            "agora_video_filters_clear_vision",
            "clear_vision",
            true,
            Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
        )
        if (ret != Constants.ERR_OK) {
            ShowLogger.e(TAG, "enableExtension failed: errorMsg:${RtcEngine.getErrorDescription(ret)},errorCode:$ret")
            this.rtcEngine = null
            return false
        }


        // Create VideoEffectObject
        beautyEffect = rtcEngine.createVideoEffectObject(materialPath, Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE)
        if (beautyEffect == null) {
            ShowLogger.e(TAG, "Failed to create VideoEffectObject")
            this.rtcEngine = null
            return false
        }

        ShowLogger.d(TAG, "Beauty SDK initialized successfully")
        return true
    }

    /**
     * Get current model type from SharedPreferences
     * @return Model type (MODEL_TYPE_ADAPTIVE, MODEL_TYPE_LARGE, or MODEL_TYPE_SMALL)
     */
    fun getCurrentModelType(): Int {
        return SPUtil.getInt(KEY_MODEL_TYPE, MODEL_TYPE_ADAPTIVE)
    }

    /**
     * Save model type to SharedPreferences
     * @param modelType Model type to save (MODEL_TYPE_ADAPTIVE, MODEL_TYPE_LARGE, or MODEL_TYPE_SMALL)
     */
    fun saveModelType(modelType: Int) {
        SPUtil.putInt(KEY_MODEL_TYPE, modelType)
    }

    /**
     * Set model type private parameter based on user selection
     * - Not set (adaptive): Default behavior, SDK automatically selects model based on device score
     * - MODEL_TYPE_LARGE: Force all devices to use large model (score = 0)
     * - MODEL_TYPE_SMALL: Force all devices to use small model (score = 100)
     */
    private fun setModelTypeParameter(rtcEngine: RtcEngine, context: Context) {
        val modelType = getCurrentModelType()
        when (modelType) {
            MODEL_TYPE_LARGE -> {
                // All devices use large model
                val ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":0}")
                ShowLogger.d(TAG, "Set model type to LARGE, result: $ret")
            }

            MODEL_TYPE_SMALL -> {
                // All devices use small model
                val ret = rtcEngine.setParameters("{\"che.video.low_alg_score_4_beauty\":100}")
                ShowLogger.d(TAG, "Set model type to SMALL, result: $ret")
            }

            MODEL_TYPE_ADAPTIVE -> {
                // Adaptive mode: do not set parameter, SDK will automatically select based on device score
                ShowLogger.d(TAG, "Model type is ADAPTIVE, using default behavior")
            }
        }
    }

    fun unInitBeautySDK() {
        try {
            enable(false)
            // Destroy beautyEffect before disabling extension
            beautyEffect?.let { effect ->
                rtcEngine?.destroyVideoEffectObject(effect)
            }
            rtcEngine?.enableExtension(
                "agora_video_filters_clear_vision",
                "clear_vision",
                false,
                Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE
            )
        } catch (e: Exception) {
            ShowLogger.e(TAG, e, "Error during unInitBeautySDK")
        } finally {
            rtcEngine = null
            // Clear beautyEffect reference
            beautyEffect = null
            // Note: rtcEngine is a singleton, we keep the reference because:
            // 1. BeautyConfig uses rtcEngine?.setFaceShapeAreaOptions (safe call, won't crash if null)
            // 2. But keeping reference allows BeautyConfig to work if beautyEnable is true later
            // 3. The beautyEnable flag controls whether operations execute, not rtcEngine being null
            // 4. If we set rtcEngine to null, re-initialization would need to set it again anyway
            beautyEnable = false
            filterEnable = false
            makeupEnable = false
            stickerEnable = false
            beautyConfig.reset()
            ShowLogger.d(TAG, "unInitBeautySDK")
        }
    }

    fun enable(enable: Boolean) {
        if (enable) {
            enableBeauty(true)
            enableFilter(true)
            enableMakeup(true)
            enableSticker(true)
            beautyConfig.resume()
        } else {
            enableBeauty(false)
            enableFilter(false)
            enableMakeup(false)
            enableSticker(false)
        }
    }

    private fun enableBeauty(enable: Boolean) {
        val effect = beautyEffect ?: return
        if (enable == beautyEnable) return
        if (enable) {
            effect.addOrUpdateVideoEffect(
                IVideoEffectObject.VIDEO_EFFECT_NODE_ID.BEAUTY.value,
                beautyConfig.beautyName
            )
        } else {
            effect.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.BEAUTY.value)
        }
        this.beautyEnable = enable
    }

    private fun enableFilter(enable: Boolean) {
        val effect = beautyEffect ?: return
        if (enable == filterEnable) return
        if (enable) {
            if (beautyConfig.filterName != null) {
                effect.addOrUpdateVideoEffect(
                    IVideoEffectObject.VIDEO_EFFECT_NODE_ID.FILTER.value,
                    beautyConfig.filterName
                )
            }
        } else {
            effect.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.FILTER.value)
        }
        this.filterEnable = enable
    }

    private fun enableMakeup(enable: Boolean) {
        val effect = beautyEffect ?: return
        if (enable == makeupEnable) return
        if (enable) {
            if (beautyConfig.makeupName != null) {
                effect.addOrUpdateVideoEffect(
                    IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STYLE_MAKEUP.value,
                    beautyConfig.makeupName
                )
            }
        } else {
            effect.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STYLE_MAKEUP.value)
        }
        this.makeupEnable = enable
    }

    private fun enableSticker(enable: Boolean) {
        val effect = beautyEffect ?: return
        if (enable == stickerEnable) return
        if (enable) {
            if (beautyConfig.stickerName != null) {
                effect.addOrUpdateVideoEffect(
                    IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STICKER.value,
                    beautyConfig.stickerName
                )
            }
        } else {
            effect.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STICKER.value)
        }
        this.stickerEnable = enable
    }

    class BeautyConfig {

        // =================================== 美颜 start ==========================
        // 美颜开关
        var beauty: Boolean = false
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                // Just set the parameter, don't call addOrUpdateVideoEffect to avoid overriding beauty effect
                beautyEffect?.setVideoEffectBoolParam("beauty_effect_option", "enable", value)
            }

        var faceShape: Boolean = false
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                // Just set the parameter, don't call addOrUpdateVideoEffect to avoid overriding beauty effect
                beautyEffect?.setVideoEffectBoolParam("face_shape_beauty_option", "enable", value)
            }

        // 美颜模板，空字符串表示素材默认 beauty_normal_ordinary
        var beautyName: String = ""
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                beautyEffect?.addOrUpdateVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.BEAUTY.value, value)
            }

        // 磨皮程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始磨皮程度。取值越大，磨皮程度越大。
        var smoothness: Float = 0.5f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "smoothness", value)
            }

        // 美白自然白，取值范围为 [0.0,1.0]，其中 0.0 表示原始亮度。取值越大，美白程度越大。
        var whitenNatural: Float = 0.4f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectStringParam("beauty_effect_option", "whiten_lut_path", "")
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "lightness", value)
            }

        // 红润度，取值范围为 [0.0,1.0]，其中 0.0 表示原始红润度。取值越大，红润程度越大。
        var redness: Float = 0.3f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "redness", value)
            }

        // 锐化程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始锐度。取值越大，锐化程度越大。
        var sharpen: Float = 0.6f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "sharpness", value)
            }

        // 清晰度，取值范围为 [-1.0,1.0]，其中 0.0 表示原始清晰度。取值越大，清晰度越大。
        var clarity: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "contrast_strength", value)
            }

        // 瘦脸 对应修饰力度范围为 [0,100]，值越大瘦脸效果越强。
        var faceContour = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACECONTOUR, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 小头 对应修饰力度范围为 [0,100]，值越大小头效果越强。
        var headScale = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_HEADSCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 窄脸 对应修饰力度范围为 [0,100]，值越大窄脸效果越强。
        var faceWidth = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACEWIDTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 长脸 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短。
        var faceLength = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACELENGTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦颧骨 对应修饰力度范围为 [0,100]，值越大颧骨越窄。
        var cheekbone = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEKBONE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 下颌线 对应修饰力度范围为 [0,100]，值越大脸颊越窄。
        var shrinkCheek = 10
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEK, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // v脸 对应修饰力度范围为 [0,100]，值越大v脸效果越强。
        var mandible = 50
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MANDIBLE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦下巴 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强。
        var chinLength = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHIN, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 发际线 对应修饰力度范围为 [-100,100]，正值为调高，负值为调低，绝对值越大修饰效果越强，预设值为 50。
        var hairlineHeight = 50
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FOREHEAD, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 去法令纹 取值范围为 [0.0,1.0]，其中 0.0 表示原始程度。取值越大，去除效果越强。
        var nasolabialFolds: Float = 0.8f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("face_buffing_option", "nasolabial_fold", value)
            }

        // 大眼 对应修饰力度范围为 [0,100]，值越大，眼睛越大。
        var enlargeEye = 40
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYESCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 亮眼 取值范围为 [0.0,1.0]，其中 0.0 表示原始程度。取值越大，亮眼效果越强。
        var brightenEye: Float = 0.3f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("face_buffing_option", "brighten_eye", value)
            }

        // 去黑眼圈 取值范围为 [0.0,1.0]，其中 0.0 表示原始程度。取值越大，去除效果越强。
        var darkCircle: Float = 0.8f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("face_buffing_option", "eye_pouch", value)
            }

        // 眼移动 对应修饰力度范围为 [-100,100]，正值为向上，负值为向下，预设值为 0。
        var eyePosition = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEPOSITION, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 眼距 对应修饰力度范围为 [-100,100]，正值为增大，负值为减小。
        var eyeDistance = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEDISTANCE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 眼瞳放大效果 [0,100]
        var eyePupil = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEPUPILS, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 眼睑下至 下眼皮向外突出效果 [0,100]
        var eyeLid = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYELID, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 内眼角 对应修饰力度范围为 [-100,100]，正值为开大，负值为缩小，预设值为 0。
        var eyeInnercorner = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEINNERCORNER, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 外眼角 对应修饰力度范围为 [-100,100]，正值为开大，负值为缩小。
        var eyeOutercorner = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEOUTERCORNER, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦鼻 对应修饰力度范围为 [0,100]。
        var narrowNose = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEWIDTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 长鼻 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强。
        var noseLength = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSELENGTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 鼻翼 对应修饰力度范围为 [0,100]，鼻翼收缩效果。
        var noseWing = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEWING, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 鼻梁 对应修饰力度范围为 [0,100]，鼻梁收缩效果。
        var noseBridge = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEBRIDGE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 山根 对应修饰力度范围为 [0,100]，正山根收缩效果（山根为鼻梁顶端，双眼中点位置。
        var noseRoot = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEROOT, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 鼻尖 对应修饰力度范围为 [0,100]，鼻尖收缩效果。
        var noseTip = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSETIP, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 鼻综合 对应修饰力度范围为 [-100,100]，鼻整体收缩效果。正值为变小，负值为变大。
        var noseGeneral = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEGENERAL, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 嘴型 对应修饰力度范围为 [-100,100]，正值为变大，负值为变小，绝对值越大修饰效果越强。
        var mouthSize = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHSCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 缩人中 对应修饰力度范围为 [0,100]，“人中”是嘴的位置。正值为上移。
        var mouthPosition = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHPOSITION, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 微笑唇 对应修饰力度范围为 [0,100]，嘴角微笑强度。
        var mouthSmile = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHSMILE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 丰唇 对应修饰力度范围为 [0,100]，丰唇效果。
        var mouthLip = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHLIP, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 白牙 取值范围为 [0.0,1.0]，其中 0.0 表示原始程度。取值越大，美白效果越强。
        var whitenTeeth: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("face_buffing_option", "whiten_teeth", value)
            }

        // 眉上下 对应修饰力度范围为 [-100,100]，正值为上移，负值为下移。
        var eyebrowPosition = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEBROWPOSITION, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 眉粗细 对应修饰力度范围为 [-100,100]，正值为增粗，负值为变细。
        var eyebrowThickness = 0
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYEBROWTHICKNESS, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }
        // =================================== 美颜 end ==========================

        // =================================== 画质 start ==========================
        // 色调 取值范围为 [-1.0,1.0]，其中 0 表示原始色调。正值为偏红，负值为偏绿。
        var hue: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "hue", value)
            }

        // 色温 取值范围为 [-1.0,1.0]，其中 0 表示原始色温。正值为偏暖，负值为偏冷。
        var temperature: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "temperature", value)
            }

        // 饱和度 取值范围为 [-1.0,1.0]，其中 0 表示原始饱和度。正值为增加，负值为减少。
        var saturation: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "saturation", value)
            }

        // 亮度 取值范围为 [-1.0,1.0]，其中 0 表示原始亮度。正值为增加，负值为减少。
        var brightness: Float = 0f
            set(value) {
                field = value
                // Only set parameters if beauty effect is enabled (总开关已开启)
                if (!beautyEnable) return
                beauty = true
                beautyEffect?.setVideoEffectFloatParam("beauty_effect_option", "brightness", value)
            }
        // =================================== 画质 end ==========================

        // =================================== 美妆 start ==========================
        // 美妆素材
        var makeupName: String? = null
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                if (value == null) {
                    beautyEffect?.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STYLE_MAKEUP.value)
                }
                if (makeupEnable && value != null) {
                    beautyEffect?.addOrUpdateVideoEffect(
                        IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STYLE_MAKEUP.value,
                        value
                    )
                }
            }

        // 美妆强度
        var makeupStrength: Float = 0.6f
            set(value) {
                field = value
                beautyEffect?.setVideoEffectFloatParam("style_makeup_option", "styleIntensity", value)
            }
        // =================================== 美妆 end ==========================

        // =================================== 滤镜 start ==========================
        // 滤镜模板
        var filterName: String? = null
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                if (value == null) {
                    beautyEffect?.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.FILTER.value)
                }
                if (filterEnable && value != null) {
                    beautyEffect?.addOrUpdateVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.FILTER.value, value)
                }
            }

        var filterStrength: Float = 0.4f
            set(value) {
                field = value
                beautyEffect?.setVideoEffectFloatParam("filter_effect_option", "strength", value)
            }
        // =================================== 滤镜 end ==========================

        // =================================== 贴纸 start ==========================
        // 贴纸素材
        var stickerName: String? = null
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                if (value == null) {
                    beautyEffect?.removeVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STICKER.value)
                }
                if (stickerEnable && value != null) {
                    beautyEffect?.addOrUpdateVideoEffect(IVideoEffectObject.VIDEO_EFFECT_NODE_ID.STICKER.value, value)
                }
            }
        // =================================== 贴纸 end ==========================

        internal fun resetBeauty() {
            // Beauty parameters
            smoothness = 0.5f
            whitenNatural = 0.4f
            redness = 0.3f
            sharpen = 0.6f
            clarity = 0f

            // Face shape parameters
            faceContour = 0
            headScale = 0
            faceWidth = 0
            faceLength = 0
            cheekbone = 0
            shrinkCheek = 10
            mandible = 50
            chinLength = 0
            hairlineHeight = 50
            nasolabialFolds = 0.8f

            // Eye parameters
            enlargeEye = 40
            brightenEye = 0.3f
            darkCircle = 0.8f
            eyePosition = 0
            eyeDistance = 0
            eyePupil = 0
            eyeLid = 0
            eyeInnercorner = 0
            eyeOutercorner = 0

            // Nose parameters
            narrowNose = 0
            noseLength = 0
            noseWing = 0
            noseBridge = 0
            noseRoot = 0
            noseTip = 0
            noseGeneral = 0

            // Mouth parameters
            mouthSize = 0
            mouthPosition = 0
            mouthSmile = 0
            mouthLip = 0
            whitenTeeth = 0f

            // Eyebrow parameters
            eyebrowPosition = 0
            eyebrowThickness = 0
        }

        internal fun reset() {
            resetBeauty()
            // Image quality parameters
            hue = 0f
            temperature = 0f
            saturation = 0f
            brightness = 0f

            filterName = null
            filterStrength = 0.4f
            makeupName = null
            makeupStrength = 0.6f
            stickerName = null
        }

        internal fun resume() {
            beauty = beauty
            beautyName = beautyName
            // Beauty parameters
            smoothness = smoothness
            whitenNatural = whitenNatural
            redness = redness
            sharpen = sharpen
            clarity = clarity

            // Face shape parameters
            faceContour = faceContour
            headScale = headScale
            faceWidth = faceWidth
            faceLength = faceLength
            cheekbone = cheekbone
            shrinkCheek = shrinkCheek
            mandible = mandible
            chinLength = chinLength
            hairlineHeight = hairlineHeight
            nasolabialFolds = nasolabialFolds

            // Eye parameters
            enlargeEye = enlargeEye
            brightenEye = brightenEye
            darkCircle = darkCircle
            eyePosition = eyePosition
            eyeDistance = eyeDistance
            eyePupil = eyePupil
            eyeLid = eyeLid
            eyeInnercorner = eyeInnercorner
            eyeOutercorner = eyeOutercorner

            // Nose parameters
            narrowNose = narrowNose
            noseLength = noseLength
            noseWing = noseWing
            noseBridge = noseBridge
            noseRoot = noseRoot
            noseTip = noseTip
            noseGeneral = noseGeneral

            // Mouth parameters
            mouthSize = mouthSize
            mouthPosition = mouthPosition
            mouthSmile = mouthSmile
            mouthLip = mouthLip
            whitenTeeth = whitenTeeth

            // Eyebrow parameters
            eyebrowPosition = eyebrowPosition
            eyebrowThickness = eyebrowThickness

            // Image quality parameters
            hue = hue
            temperature = temperature
            saturation = saturation
            brightness = brightness

            // Filter parameters
            filterName = filterName
            filterStrength = filterStrength
            // Makeup parameters
            makeupName = makeupName
            makeupStrength = makeupStrength
            stickerName = stickerName
        }
    }


    private var savedBeautyState = false
    private var savedFaceShapeState = false
    private var savedMakeupName: String? = null
    private var savedFilterName: String? = null
    private var savedStickerName: String? = null

    fun actionBareFace() {
        savedBeautyState = beautyConfig.beauty
        savedFaceShapeState = beautyConfig.faceShape
        savedMakeupName = beautyConfig.makeupName
        savedFilterName = beautyConfig.filterName
        savedStickerName = beautyConfig.stickerName
        // Temporarily disable beauty to show original face
        beautyConfig.beauty = false
        beautyConfig.faceShape = false
        beautyConfig.makeupName = null
        beautyConfig.filterName = null
        beautyConfig.stickerName = null
    }

    fun actionBeauty() {
        beautyConfig.beauty = savedBeautyState
        beautyConfig.faceShape = savedFaceShapeState
        beautyConfig.makeupName = savedMakeupName
        beautyConfig.filterName = savedFilterName
        beautyConfig.stickerName = savedStickerName
    }
}
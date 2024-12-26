package io.agora.scene.show.beauty

import android.content.Context
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.BeautyOptions
import io.agora.rtc2.video.FaceShapeAreaOptions
import io.agora.rtc2.video.FaceShapeBeautyOptions
import io.agora.rtc2.video.FilterEffectOptions
import io.agora.rtc2.video.MakeUpOptions
import io.agora.scene.show.ShowLogger
import io.agora.scene.show.utils.FileUtils
import org.json.JSONException
import org.json.JSONObject

object AgoraBeautySDK {
    private const val TAG = "AgoraBeautySDK"
    private var rtcEngine: RtcEngine? = null
    private var basicEnable = false
    private var filterEnable = false
    private var faceShapeEnable = false
    private var makeupEnable = false

    private var useLocalBeautyResource = true
    private var storagePath = ""
    private const val assetsPath = "beauty_agora"
    private var filterPortraitPath = ""

    // 美颜配置
    val beautyConfig = BeautyConfig()

    fun initBeautySDK(context: Context, rtcEngine: RtcEngine, useLocalBeautyResource: Boolean): Boolean {
        this.useLocalBeautyResource = useLocalBeautyResource
        storagePath = context.getExternalFilesDir("")?.absolutePath ?: return false
        if (useLocalBeautyResource) {
            // copy filter_portrait
            filterPortraitPath = "$storagePath/beauty_agora/filter_portrait"
            FileUtils.copyAssets(context, "${assetsPath}/beauty_agora/filter_portrait", filterPortraitPath)
        } else {
            filterPortraitPath = "$storagePath/assets/beauty_agora/filter_portrait"
        }
        this.rtcEngine = rtcEngine
        val ret = rtcEngine.enableExtension("agora_video_filters_clear_vision", "clear_vision", true)
        if (ret != Constants.ERR_OK) {
            ShowLogger.d(TAG, "enableExtension failed: errorMsg:${RtcEngine.getErrorDescription(ret)},errorCode:$ret")
            return false
        }
        // The private parameter is not supported, use VideoFrameObserver#getMirrorApplied instead
        // rtcEngine.setParameters("{\"rtc.camera_capture_mirror_mode\":0}")
        beautyConfig.resume()
        return true
    }

    fun unInitBeautySDK() {
        rtcEngine?.setBeautyEffectOptions(false, beautyConfig.beautyOption)
        rtcEngine?.setFilterEffectOptions(false, beautyConfig.filterOption)
        rtcEngine?.setFaceShapeBeautyOptions(false, beautyConfig.faceShapeOption)
        val makeupObj = JSONObject()
        try {
            makeupObj.put("enable_mu", false)
            rtcEngine?.setExtensionProperty(
                "agora_video_filters_clear_vision",
                "clear_vision",
                "makeup_options",
                makeupObj.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        rtcEngine?.enableExtension("agora_video_filters_clear_vision", "clear_vision", false)
        // The private parameter is not supported, use VideoFrameObserver#getMirrorApplied instead
        // rtcEngine?.setParameters("{\"rtc.camera_capture_mirror_mode\":2}")
        rtcEngine = null
        basicEnable = false
        filterEnable = false
        faceShapeEnable = false
        makeupEnable = false
        beautyConfig.reset()
    }

    fun enable(enable: Boolean) {
        if (enable) {
            enableBasic(true)
            enableFilter(true)
            enableFaceShape(true)
        } else {
            enableBasic(false)
            enableFilter(false)
            enableFaceShape(false)
            enableMakeup(false)
        }
    }

    private fun enableBasic(enable: Boolean) {
        val rtc = rtcEngine ?: return
        rtc.setBeautyEffectOptions(enable, beautyConfig.beautyOption)
        this.basicEnable = enable
    }

    private fun enableFaceShape(enable: Boolean, force: Boolean = false) {
        val rtc = rtcEngine ?: return
        if (this.faceShapeEnable == enable && !force) return
        rtc.setFaceShapeBeautyOptions(enable, beautyConfig.faceShapeOption)
        this.faceShapeEnable = enable
    }

    private fun enableFilter(enable: Boolean) {
        val rtc = rtcEngine ?: return
        rtc.setFilterEffectOptions(enable, beautyConfig.filterOption)
        this.filterEnable = enable
    }

    private fun enableMakeup(enable: Boolean) {
        val rtc = rtcEngine ?: return
        val makeupObj = JSONObject()
        try {
            if (!beautyConfig.makeupOption.mMakeUpEnable) {
                makeupObj.put("enable_mu", false);
            } else {
                makeupObj.put("enable_mu", beautyConfig.makeupOption.mMakeUpEnable);
                makeupObj.put("browStyle", beautyConfig.makeupOption.mBrowType);
                makeupObj.put("browColor", beautyConfig.makeupOption.mBrowColor);
                makeupObj.put("browStrength", beautyConfig.makeupOption.mBrowStrength);
                makeupObj.put("lashStyle", beautyConfig.makeupOption.mLashType);
                makeupObj.put("lashColor", beautyConfig.makeupOption.mLashColor);
                makeupObj.put("lashStrength", beautyConfig.makeupOption.mLashStrength);
                makeupObj.put("shadowStyle", beautyConfig.makeupOption.mShadowType);
                makeupObj.put("shadowStrength", beautyConfig.makeupOption.mShadowStrength);
                makeupObj.put("pupilStyle", beautyConfig.makeupOption.mPupilType);
                makeupObj.put("pupilStrength", beautyConfig.makeupOption.mPupilStrength);
                makeupObj.put("blushStyle", beautyConfig.makeupOption.mBlushType);
                makeupObj.put("blushColor", beautyConfig.makeupOption.mBlushColor);
                makeupObj.put("blushStrength", beautyConfig.makeupOption.mBlushStrength);
                makeupObj.put("lipStyle", beautyConfig.makeupOption.mLipType);
                makeupObj.put("lipColor", beautyConfig.makeupOption.mLipColor);
                makeupObj.put("lipStrength", beautyConfig.makeupOption.mLipStrength);
            }
            rtc.setExtensionProperty(
                "agora_video_filters_clear_vision",
                "clear_vision",
                "makeup_options",
                makeupObj.toString()
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        this.makeupEnable = enable
    }

    enum class FilterStyle constructor(val value: Int) {
        None(0),
        YuanSheng(1),
        NenBai(2),
        LengBai(3),
    }

    class BeautyConfig {

        // 基础美颜配置
        internal val beautyOption = BeautyOptions()

        // 滤镜配置
        internal val filterOption = FilterEffectOptions()

        // 美型配置
        internal val faceShapeOption = FaceShapeBeautyOptions()

        // 美妆配置
        internal val makeupOption = MakeUpOptions()

        // 基础美颜
        var basicBeauty = false
            set(value) {
                field = value
                enableBasic(value)
            }

        // 滤镜
        var filter = false
            set(value) {
                field = value
                enableFilter(value)
            }

        // 磨皮程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始磨皮程度，默认值为 0.5。取值越大，磨皮程度越大。
        var smooth: Float = 0.5f
            set(value) {
                field = value
                beautyOption.smoothnessLevel = value
                basicBeauty = true
            }

        // 美白程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始亮度，默认值为 0.6。取值越大，美白程度越大。
        var whiten: Float = 0.6f
            set(value) {
                field = value
                beautyOption.lighteningLevel = value
                basicBeauty = true
            }

        // 红润度，取值范围为 [0.0,1.0]，其中 0.0 表示原始红润度，默认值为 0.1。取值越大，红润程度越大。
        var redden = 0.1f
            set(value) {
                field = value
                beautyOption.rednessLevel = value
                basicBeauty = true
            }

        // 滤镜
        var filterType = FilterStyle.None
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                when (value) {
                    FilterStyle.YuanSheng -> { // 原生
                        filterOption.path = "$filterPortraitPath/yuansheng32.cube"
                        filter = true
                    }

                    FilterStyle.NenBai -> {   // 嫩白
                        filterOption.path = "$filterPortraitPath/nenbai32.cube"
                        filter = true
                    }

                    FilterStyle.LengBai -> {  // 冷白
                        filterOption.path = "$filterPortraitPath/lengbai32.cube"
                        filter = true
                    }

                    else -> {
                        filter = false
                    }
                }
            }

        var filterStrength = 0.5f
            set(value) {
                field = value
                filterOption.strength = value
                val rtc = rtcEngine ?: return
                rtc.setFilterEffectOptions(filterEnable, beautyConfig.filterOption)
            }

        // 锐化程度，取值范围为 [0.0,1.0]，其中 0.0 表示原始锐度，默认值为 0.3。取值越大，锐化程度越大。
        var sharpen = 0.3f
            set(value) {
                field = value
                beautyOption.sharpnessLevel = value
                basicBeauty = true
            }

        // 美型
        var faceShape = false
            set(value) {
                field = value
                enableFaceShape(value)
            }

        // 大眼 对应修饰力度范围为 [0,100]，值越大，眼睛越大，预设值为 53。
        var enlargeEye = 53
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYESCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 下巴 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强，预设值为 -20。
        var chinLength = -20
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHIN, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦脸 对应修饰力度范围为 [0,100]，值越大瘦脸效果越强，预设值为 10。
        var thinFace = 10
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACECONTOUR, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦颧骨 对应修饰力度范围为 [0,100]，值越大颧骨越窄，预设值为 43。
        var shrinkCheekbone = 43
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEKBONE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        //长鼻 对应修饰力度范围为 [-100,100]，正值为拉长，负值为变短，绝对值越大修饰效果越强，预设值为 -10。
        var longNose = -10
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSELENGTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 瘦鼻 对应修饰力度范围为 [-100,100]，正值为变宽，负值为变窄，绝对值越大修饰效果越强，预设值为 72。
        var narrowNose = 72
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEWIDTH, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 嘴型 对应修饰力度范围为 [-100,100]，正值为变大，负值为变小，绝对值越大修饰效果越强，预设值为 20。
        var mouthSize = 20
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHSCALE, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 下颌骨 对应修饰力度范围为 [0,100]，值越大脸颊越窄，预设值为 50。
        var shrinkJawbone = 50
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEK, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 发际线 对应修饰力度范围为 [-100,100]，正值为调高，负值为调低，绝对值越大修饰效果越强，预设值为 50。
        var hairlineHeight = 50
            set(value) {
                field = value
                faceShape = true
                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FOREHEAD, value);
                rtcEngine?.setFaceShapeAreaOptions(areaOption)
            }

        // 绅士脸
        var gentlemanFace = 50
            set(value) {
                field = value
                faceShapeOption.shapeStyle = FaceShapeBeautyOptions.FACE_SHAPE_BEAUTY_STYLE_MALE
                faceShapeOption.styleIntensity = value
                enableFaceShape(enable = true, force = true)
            }

        // 淑女脸
        var ladyFace = 80
            set(value) {
                field = value
                faceShapeOption.shapeStyle = FaceShapeBeautyOptions.FACE_SHAPE_BEAUTY_STYLE_FEMALE
                faceShapeOption.styleIntensity = value
                enableFaceShape(enable = true, force = true)
            }

//        // 小头
//        var headScale = 100
//            set(value) {
//                field = value
//                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_HEADSCALE, value);
//                rtcEngine?.setFaceShapeAreaOptions(areaOption)
//                faceShape = true
//            }
//
//        // 长脸
//        var longFace = 0
//            set(value) {
//                field = value
//                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACELENGTH, value);
//                rtcEngine?.setFaceShapeAreaOptions(areaOption)
//                faceShape = true
//            }
//
//        // 窄脸
//        var narrowFace = 10
//            set(value) {
//                field = value
//                val areaOption = FaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACEWIDTH, value);
//                rtcEngine?.setFaceShapeAreaOptions(areaOption)
//                faceShape = true
//            }

        // 美妆素材
        var makeupType = 0
            set(value) {
                field = value
                when (value) {
                    1 -> { // 第1套美妆
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 1
                        beautyConfig.makeupOption.mBrowColor = 1
                        beautyConfig.makeupOption.mLashType = 1
                        beautyConfig.makeupOption.mLashColor = 1
                        beautyConfig.makeupOption.mShadowType = 1
                        beautyConfig.makeupOption.mPupilType = 1
                        beautyConfig.makeupOption.mBlushType = 1
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 1
                        beautyConfig.makeupOption.mLipColor = 1
                        enableMakeup(true)
                    }

                    2 -> {  // 第2套美妆
                        beautyConfig.makeupOption.mMakeUpEnable = true
                        beautyConfig.makeupOption.mBrowType = 2
                        beautyConfig.makeupOption.mBrowColor = 1
                        beautyConfig.makeupOption.mLashType = 2
                        beautyConfig.makeupOption.mLashColor = 1
                        beautyConfig.makeupOption.mShadowType = 2
                        beautyConfig.makeupOption.mPupilType = 2
                        beautyConfig.makeupOption.mBlushType = 2
                        beautyConfig.makeupOption.mBlushColor = 1
                        beautyConfig.makeupOption.mLipType = 2
                        beautyConfig.makeupOption.mLipColor = 1
                        enableMakeup(true)
                    }

                    else -> {
                        beautyConfig.makeupOption.mMakeUpEnable = false
                        enableMakeup(false)
                    }
                }
            }

        // 美妆强度
        var makeupStrength = 0.5f
            set(value) {
                field = value
                val makeupObj = JSONObject()
                beautyConfig.makeupOption.mBrowStrength = value
                beautyConfig.makeupOption.mLashStrength = value
                beautyConfig.makeupOption.mShadowStrength = value
                beautyConfig.makeupOption.mPupilStrength = value
                beautyConfig.makeupOption.mBlushStrength = value
                beautyConfig.makeupOption.mLipStrength = value
                try {
                    makeupObj.put("enable_mu", beautyConfig.makeupOption.mMakeUpEnable);
                    makeupObj.put("browStrength", beautyConfig.makeupOption.mBrowStrength);
                    makeupObj.put("lashStrength", beautyConfig.makeupOption.mLashStrength);
                    makeupObj.put("shadowStrength", beautyConfig.makeupOption.mShadowStrength);
                    makeupObj.put("pupilStrength", beautyConfig.makeupOption.mPupilStrength);
                    makeupObj.put("blushStrength", beautyConfig.makeupOption.mBlushStrength);
                    makeupObj.put("lipStrength", beautyConfig.makeupOption.mLipStrength);
                    rtcEngine?.setExtensionProperty(
                        "agora_video_filters_clear_vision",
                        "clear_vision",
                        "makeup_options",
                        makeupObj.toString()
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }


        internal fun reset() {
            smooth = 0.5f
            whiten = 0.6f
            redden = 0.1f
            sharpen = 0.3f
            filterType = FilterStyle.None
            filterStrength = 0.5f
            makeupType = 0
            makeupStrength = 0.5f

            enlargeEye = 53
            chinLength = -20
            thinFace = 10
            shrinkCheekbone = 43
            longNose = -10
            narrowNose = 72
            mouthSize = 20
            shrinkJawbone = 50
            hairlineHeight = 50
            ladyFace = 80

//            headScale = 0
//            longFace = 0
//            narrowFace = 0
        }

        internal fun resume() {
            smooth = smooth
            whiten = whiten
            redden = redden
            sharpen = sharpen
            filterType = filterType
            filterStrength = filterStrength
            makeupType = makeupType
            makeupStrength = makeupStrength

            enlargeEye =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_EYESCALE)?.shapeIntensity ?: 53
            chinLength =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHIN)?.shapeIntensity ?: -20
            thinFace =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACECONTOUR)?.shapeIntensity
                    ?: 10
            shrinkCheekbone =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEKBONE)?.shapeIntensity ?: 43
            longNose =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSELENGTH)?.shapeIntensity
                    ?: -10
            narrowNose =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_NOSEWIDTH)?.shapeIntensity ?: 72
            mouthSize =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_MOUTHSCALE)?.shapeIntensity
                    ?: 20
            shrinkJawbone =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_CHEEK)?.shapeIntensity ?: 50
            hairlineHeight =
                rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FOREHEAD)?.shapeIntensity ?: 50

//            headScale = rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_HEADSCALE)?.shapeIntensity ?: 0
//            longFace = rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACELENGTH)?.shapeIntensity ?: 0
//            narrowFace = rtcEngine?.getFaceShapeAreaOptions(FaceShapeAreaOptions.FACE_SHAPE_AREA_FACEWIDTH)?.shapeIntensity ?: 0

        }
    }
}
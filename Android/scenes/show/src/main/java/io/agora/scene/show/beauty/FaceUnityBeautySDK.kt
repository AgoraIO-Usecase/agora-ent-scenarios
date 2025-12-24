package io.agora.scene.show.beauty

import android.content.Context
import android.util.Log
import com.faceunity.core.callback.OperateCallback
import com.faceunity.core.entity.FUBundleData
import com.faceunity.core.enumeration.FUAITypeEnum
import com.faceunity.core.faceunity.FUAIKit
import com.faceunity.core.faceunity.FURenderConfig.OPERATE_SUCCESS_AUTH
import com.faceunity.core.faceunity.FURenderKit
import com.faceunity.core.faceunity.FURenderManager
import com.faceunity.core.model.facebeauty.FaceBeauty
import com.faceunity.core.model.makeup.SimpleMakeup
import com.faceunity.core.model.prop.sticker.Sticker
import com.faceunity.core.utils.FULogger
import com.faceunity.wrapper.faceunity
import io.agora.beautyapi.faceunity.FaceUnityBeautyAPI
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.show.BuildConfig
import java.io.File

object FaceUnityBeautySDK {

    private const val TAG = "FaceUnityBeautySDK"

    /* AI道具*/
    private const val BUNDLE_AI_FACE = "model/ai_face_processor.bundle"
    private const val BUNDLE_AI_HUMAN = "model/ai_human_processor.bundle"

    // 美颜配置
    val beautyConfig = BeautyConfig()

    private var beautyAPI: FaceUnityBeautyAPI? = null

    private var useLocalBeautyResource = true

    fun initBeauty(context: Context, useLocalBeautyResource: Boolean): Boolean {
        this.useLocalBeautyResource = useLocalBeautyResource

        val auth = if (useLocalBeautyResource) {
            try {
                getAuth()
            } catch (e: Exception) {
                Log.w(TAG, e)
                return false
            } ?: return false
        } else {
            readFromFileToByteArray(context.getExternalFilesDir("")?.absolutePath + "/assets/beauty_faceunity/license/fu.txt")
        }

        FURenderManager.setKitDebug(FULogger.LogLevel.TRACE)
        FURenderManager.setCoreDebug(FULogger.LogLevel.ERROR)
        FURenderManager.registerFURender(context, auth, object : OperateCallback {
            override fun onSuccess(code: Int, msg: String) {
                Log.i(TAG, "FURenderManager onSuccess -- code=$code, msg=$msg")
                if (code == OPERATE_SUCCESS_AUTH) {
                    faceunity.fuSetUseTexAsync(1)
                    if (useLocalBeautyResource) {
                        FUAIKit.getInstance()
                            .loadAIProcessor(BUNDLE_AI_FACE, FUAITypeEnum.FUAITYPE_FACEPROCESSOR)
                        FUAIKit.getInstance().loadAIProcessor(
                            BUNDLE_AI_HUMAN,
                            FUAITypeEnum.FUAITYPE_HUMAN_PROCESSOR
                        )
                    } else {
                        FUAIKit.getInstance().loadAIProcessor(
                            context.getExternalFilesDir("")?.absolutePath + "/assets/beauty_faceunity/$BUNDLE_AI_FACE",
                            FUAITypeEnum.FUAITYPE_FACEPROCESSOR
                        )
                        FUAIKit.getInstance().loadAIProcessor(
                            context.getExternalFilesDir("")?.absolutePath + "/assets/beauty_faceunity/$BUNDLE_AI_HUMAN",
                            FUAITypeEnum.FUAITYPE_HUMAN_PROCESSOR
                        )
                    }
                    beautyConfig.reset()
                }
            }

            override fun onFail(errCode: Int, errMsg: String) {
                Log.e(TAG, "FURenderManager onFail -- code=$errCode, msg=$errMsg")
            }
        })
        return true
    }

    fun unInitBeauty() {
        beautyAPI = null
        beautyConfig.reset()
        FUAIKit.getInstance().releaseAllAIProcessor()
        FURenderKit.getInstance().release()
    }

    // 读取文件内容并写入字节数组
    private fun readFromFileToByteArray(filePath: String): ByteArray {
        val file = File(filePath)
        val inputStream = file.inputStream()
        val bytes = inputStream.readBytes()
        inputStream.close()
        return bytes
    }

    private fun getAuth(): ByteArray? {
        val authpack = Class.forName("io.agora.scene.show.beauty.authpack")
        val aMethod = authpack.getDeclaredMethod("A")
        aMethod.isAccessible = true
        return aMethod.invoke(null) as? ByteArray
    }

    internal fun setBeautyAPI(beautyAPI: FaceUnityBeautyAPI) {
        this.beautyAPI = beautyAPI
        beautyConfig.resume()
    }

    private fun runOnBeautyThread(run: () -> Unit) {
        beautyAPI?.runOnProcessThread(run) ?: run.invoke()
    }


    class BeautyConfig {

        private val fuRenderKit = FURenderKit.getInstance()

        // 美颜配置
        private val faceBeauty =
            if (BuildConfig.BEAUTY_RESOURCE.isEmpty())
                FaceBeauty(FUBundleData("graphics" + File.separator + "face_beautification.bundle"))
            else
                FaceBeauty(FUBundleData(AgoraApplication.the().externalFilesDir.absolutePath + "/assets/beauty_faceunity/graphics" + File.separator + "face_beautification.bundle"))

        // 资源基础路径
        private val resourceBase = "beauty_faceunity"

        // 美颜开关
        var beauty: Boolean = faceBeauty.enable
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.enable = value
                }
            }

        // 磨皮
        var smooth = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.blurIntensity = value * 6.0
                }
            }

        // 美白
        var whiten = 0.4f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.colorIntensity = value * 2.0
                }
            }

        // 红润度
        var redness: Float = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.redIntensity = value * 2.0
                }
            }

        // 锐化
        var sharpen = 0.6f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.sharpenIntensity = value.toDouble()
                }
            }

        // 清晰度
        var clarity = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.clarityIntensity = value.toDouble()
                }
            }

        // 瘦脸
        var faceContour = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekThinningIntensity = value.toDouble()
                }
            }

        // 小脸
        var faceSmall = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekSmallIntensity = value.toDouble()
                }
            }

        // 窄脸
        var faceWidth = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekNarrowIntensity = value.toDouble()
                }
            }

        // 长脸
        var faceLength = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekLongIntensity = value.toDouble()
                }
            }

        // 瘦颧骨
        var shrinkCheekbone = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekBonesIntensity = value.toDouble()
                }
            }

        // 下颌骨
        var shrinkJawbone = 0.1f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.lowerJawIntensity = value.toDouble()
                }
            }

        // v脸
        var mandible = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.cheekVIntensity = value.toDouble()
                }
            }

        // 下巴
        var chinLength = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.chinIntensity = value.toDouble()
                }
            }

        // 额头
        var hairlineHeight = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.forHeadIntensity = value.toDouble()
                }
            }

        // 祛法令纹
        var nasolabialFolds = 0.8f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.removeLawPatternIntensity = value.toDouble()
                }
            }

        // 大眼
        var enlargeEye = 0.4f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeEnlargingIntensity = value.toDouble()
                }
            }

        // 亮眼
        var brightEye = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeBrightIntensity = value.toDouble()
                }
            }

        // 祛黑眼圈
        var darkCircles = 0.8f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.removePouchIntensity = value.toDouble()
                }
            }

        // 眼移动
        var eyePosition = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeHeightIntensity = value.toDouble()
                }
            }

        // 眼距
        var eyeDistance = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeSpaceIntensity = value.toDouble()
                }
            }

        // 眼瞳放大效果
        var eyePupil = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 眼睑下至
        var eyeLid = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeLidIntensity = value.toDouble()
                }
            }

        // 眼角
        var eyecorner = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.eyeRotateIntensity = value.toDouble()
                }
            }

        // 瘦鼻
        var narrowNose = 0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.noseIntensity = value.toDouble()
                }
            }

        // 长鼻
        var noseLength = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.longNoseIntensity = value.toDouble()
                }
            }

        // 鼻翼
        var noseWing = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 鼻梁
        var noseBridge = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 山根
        var noseRoot = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 鼻尖
        var noseTip = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 鼻综合
        var noseGeneral = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    // TODO:
                }
            }

        // 嘴型
        var mouthSize = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.mouthIntensity = value.toDouble()
                }
            }

        // 缩人中
        var mouthPosition = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.philtrumIntensity = value.toDouble()
                }
            }

        // 微笑唇
        var mouthSmile = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.smileIntensity = value.toDouble()
                }
            }

        // 丰唇
        var mouthLip = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.lipThickIntensity = value.toDouble()
                }
            }

        // 白牙
        var whiteTeeth = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.toothIntensity = value.toDouble()
                }
            }

        // 眉上下
        var eyebrowPosition = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.browHeightIntensity = value.toDouble()
                }
            }

        // 眉粗细
        var eyebrowThickness = 0.5f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.browThickIntensity = value.toDouble()
                }
            }

        // 五官立体
        var faceThree = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    beauty = true
                    faceBeauty.faceThreeIntensity = value.toDouble()
                }
            }

        // 贴纸
        var sticker: StickerItem? = null
            set(value) {
                field = value
                runOnBeautyThread {
                    fuRenderKit.propContainer.removeAllProp()
                    if (value != null) {
                        val path =
                            value.context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourceBase/${value.path}"
                        val prop =
                            if (useLocalBeautyResource) Sticker(FUBundleData("$resourceBase/${value.path}")) else Sticker(
                                FUBundleData(path)
                            )
                        fuRenderKit.propContainer.addProp(prop)
                    }
                }
            }

        // 美妆
        var makeUp: MakeUpItem? = null
            set(value) {
                field = value
                runOnBeautyThread {
                    if (value == null) {
                        fuRenderKit.makeup = null
                    } else {
                        if (useLocalBeautyResource) {
                            val makeup =
                                SimpleMakeup(FUBundleData("graphics" + File.separator + "face_makeup.bundle"))
                            makeup.setCombinedConfig(FUBundleData("$resourceBase/${value.path}"))
                            makeup.makeupIntensity = value.intensity.toDouble()
                            fuRenderKit.makeup = makeup
                        } else {
                            val path =
                                value.context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourceBase/${value.path}"
                            val makeup =
                                SimpleMakeup(FUBundleData(value.context.getExternalFilesDir(null)?.absolutePath + "/assets/beauty_faceunity/graphics" + File.separator + "face_makeup.bundle"))
                            makeup.setCombinedConfig(FUBundleData(path))
                            makeup.makeupIntensity = value.intensity.toDouble()
                            fuRenderKit.makeup = makeup
                        }
                    }
                }
            }


        internal fun resetBeauty() {
            // Beauty parameters
            smooth = 0.5f
            whiten = 0.4f
            redness = 0.3f
            sharpen = 0.6f
            clarity = 0.0f

            // Face shape parameters
            faceContour = 0.0f
            faceSmall = 0.0f
            faceWidth = 0.0f
            faceLength = 0.0f
            shrinkCheekbone = 0.0f
            shrinkJawbone = 0.1f
            mandible = 0.5f
            chinLength = 0.5f
            hairlineHeight = 0.5f
            nasolabialFolds = 0.8f

            // Eye parameters
            enlargeEye = 0.4f
            brightEye = 0.3f
            darkCircles = 0.8f
            eyePosition = 0.5f
            eyeDistance = 0.5f
            eyePupil = 0.0f
            eyeLid = 0.0f
            eyecorner = 0.5f

            // Nose parameters
            narrowNose = 0f
            noseLength = 0.5f
            noseWing = 0.0f
            noseBridge = 0.0f
            noseRoot = 0.0f
            noseTip = 0.0f
            noseGeneral = 0.5f

            // Mouth parameters
            mouthSize = 0.5f
            mouthPosition = 0.5f
            mouthSmile = 0.5f
            mouthLip = 0.5f
            whiteTeeth = 0f

            eyebrowPosition = 0.5f
            eyebrowThickness = 0.5f
            faceThree = 0f

        }

        internal fun reset() {
            resetBeauty()

            makeUp = null
            sticker = null
        }

        internal fun resume() {
            runOnBeautyThread {
                fuRenderKit.faceBeauty = faceBeauty
            }
        }
    }

    data class MakeUpItem(
        val context: Context,
        val path: String,
        val intensity: Float
    )

    data class StickerItem(
        val context: Context,
        val path: String
    )

    private var savedBeautyState = false
    private var savedMakeup: MakeUpItem? = null
    private var savedSticker: StickerItem? = null

    fun actionBareFace() {
        savedBeautyState = beautyConfig.beauty
        savedMakeup = beautyConfig.makeUp
        savedSticker = beautyConfig.sticker
        // Temporarily disable beauty to show original face
        beautyConfig.beauty = false
        beautyConfig.sticker = null
        beautyConfig.makeUp = null
    }

    fun actionBeauty() {
       beautyConfig.beauty = savedBeautyState
       beautyConfig.sticker = savedSticker
       beautyConfig.makeUp = savedMakeup
    }
}
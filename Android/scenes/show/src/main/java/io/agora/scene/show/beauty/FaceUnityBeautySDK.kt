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
import java.io.FileOutputStream

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
                            FUAITypeEnum.FUAITYPE_FACEPROCESSOR)
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

        // 磨皮
        var smooth = 0.65f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.blurIntensity = value * 6.0
                }
            }

        // 美白
        var whiten = 0.65f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.colorIntensity = value * 2.0
                }
            }

        // 瘦脸
        var thinFace = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.cheekThinningIntensity = value.toDouble()
                }
            }

        // 大眼
        var enlargeEye = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.eyeEnlargingIntensity = value.toDouble()
                }
            }

        // 红润
        var redden = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.redIntensity = value * 2.0
                }
            }

        // 五官立体
        var faceThree = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.faceThreeIntensity = value.toDouble()
                }
            }

        // 瘦颧骨
        var shrinkCheekbone = 0.3f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.cheekBonesIntensity = value.toDouble()
                }
            }

        // 下颌骨
        var shrinkJawbone = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.lowerJawIntensity = value.toDouble()
                }
            }

        // 美牙
        var whiteTeeth = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.toothIntensity = value.toDouble()
                }
            }

        // 额头
        var hairlineHeight = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.forHeadIntensity = value.toDouble()
                }
            }

        // 瘦鼻
        var narrowNose = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.noseIntensity = value.toDouble()
                }
            }

        // 嘴形
        var mouthSize = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.mouthIntensity = value.toDouble()
                }
            }

        // 下巴
        var chinLength = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.chinIntensity = value.toDouble()
                }
            }

        // 亮眼
        var brightEye = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.eyeBrightIntensity = value.toDouble()
                }
            }

        // 祛黑眼圈
        var darkCircles = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.removePouchIntensity = value.toDouble()
                }
            }

        // 祛法令纹
        var nasolabialFolds = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.removeLawPatternIntensity = value.toDouble()
                }
            }

        // 锐化
        var sharpen = 0.0f
            set(value) {
                field = value
                runOnBeautyThread {
                    faceBeauty.sharpenIntensity = value.toDouble()
                }
            }

        // 贴纸
        var sticker: StickerItem? = null
            set(value) {
                field = value
                runOnBeautyThread {
                    fuRenderKit.propContainer.removeAllProp()
                    if (value != null) {
                        val path = value.context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourceBase/${value.path}"
                        val prop = if (useLocalBeautyResource) Sticker(FUBundleData("$resourceBase/${value.path}")) else Sticker(FUBundleData(path))
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
                            val path = value.context.getExternalFilesDir(null)?.absolutePath + "/assets/$resourceBase/${value.path}"
                            val makeup =
                                SimpleMakeup(FUBundleData(value.context.getExternalFilesDir(null)?.absolutePath + "/assets/beauty_faceunity/graphics" + File.separator + "face_makeup.bundle"))
                            makeup.setCombinedConfig(FUBundleData(path))
                            makeup.makeupIntensity = value.intensity.toDouble()
                            fuRenderKit.makeup = makeup
                        }
                    }
                }
            }


        internal fun reset() {
            smooth = 0.65f
            whiten = 0.65f
            thinFace = 0.3f
            enlargeEye = 0.0f
            redden = 0.0f
            shrinkCheekbone = 0.3f
            shrinkJawbone = 0.0f
            whiteTeeth = 0.0f
            hairlineHeight = 0.0f
            narrowNose = 0.0f
            mouthSize = 0.0f
            chinLength = 0.0f
            brightEye = 0.0f
            darkCircles = 0.0f
            nasolabialFolds = 0.0f
            faceThree = 0.0f

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
}
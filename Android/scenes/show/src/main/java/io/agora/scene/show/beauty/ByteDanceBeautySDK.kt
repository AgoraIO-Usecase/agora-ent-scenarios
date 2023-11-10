package io.agora.scene.show.beauty

import android.content.Context
import android.util.Log
import com.effectsar.labcv.effectsdk.RenderManager
import io.agora.scene.show.utils.FileUtils
import java.io.File

object ByteDanceBeautySDK {

    private const val TAG = "ByteDanceBeautySDK"

    private val LICENSE_NAME = "Agora_test_20230815_20231115_io.agora.test.entfull_4.5.0_599.licbag"
    private var storagePath = ""
    private var assetsPath = ""
    private var licensePath = ""
    private var modelsPath = ""
    private var beautyNodePath = ""
    private var beauty4ItemsNodePath = ""
    private var reSharpNodePath = ""
    private var stickerPath = ""

    // 特效句柄
    val renderManager = RenderManager()

    // 美颜配置
    val beautyConfig = BeautyConfig()



    fun initBeautySDK(context: Context): Boolean {
        storagePath = context.getExternalFilesDir("")?.absolutePath ?: return false
        assetsPath = "beauty_bytedance"

        // copy license
        licensePath = "$storagePath/beauty_bytedance/LicenseBag.bundle/$LICENSE_NAME"
        FileUtils.copyAssets(context, "$assetsPath/LicenseBag.bundle/$LICENSE_NAME", licensePath)
        if (!File(licensePath).exists()) {
            return false
        }

        // copy models
        modelsPath = "$storagePath/beauty_bytedance/ModelResource.bundle"
        FileUtils.copyAssets(context, "$assetsPath/ModelResource.bundle", modelsPath)

        // copy beauty node
        beautyNodePath =
            "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/beauty_Android_lite"
        FileUtils.copyAssets(
            context,
            "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/beauty_Android_lite",
            beautyNodePath
        )

        // copy beauty 4items node
        beauty4ItemsNodePath =
            "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/beauty_4Items"
        FileUtils.copyAssets(
            context,
            "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/beauty_4Items",
            beauty4ItemsNodePath
        )

        // copy resharp node
        reSharpNodePath =
            "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/reshape_lite"
        FileUtils.copyAssets(
            context,
            "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/reshape_lite",
            reSharpNodePath
        )

        // copy stickers
        stickerPath = "$storagePath/beauty_bytedance/StickerResource.bundle/stickers"
        FileUtils.copyAssets(context, "$assetsPath/StickerResource.bundle/stickers", stickerPath)

        return true
    }

    // GL Thread
    fun initEffect(context: Context) {
        val ret = renderManager.init(
            context,
            modelsPath, licensePath, false, false, 0
        )
        if (!checkResult("RenderManager init ", ret)) {
            return
        }
        renderManager.useBuiltinSensor(true)
        renderManager.set3Buffer(false)
        renderManager.appendComposerNodes(
            arrayOf(
                beautyNodePath,
                beauty4ItemsNodePath,
                reSharpNodePath
            )
        )
        renderManager.loadResourceWithTimeout(-1)
        beautyConfig.resume()
    }

    // GL Thread
    fun unInitEffect() {
        beautyConfig.reset()
        renderManager.release()
    }

    private fun checkResult(msg: String, ret: Int): Boolean {
        if (ret != 0 && ret != -11 && ret != 1) {
            val log = "$msg error: $ret"
            Log.e(TAG, log)
            return false
        }
        return true
    }


    class BeautyConfig {

        // 磨皮
        var smooth = 0.3f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beautyNodePath, "smooth", value)
            }

        // 美白
        var whiten = 0.5f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beautyNodePath, "whiten", value)
            }

        // 红润
        var redden = 0.0f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beautyNodePath, "sharp", value)
            }

        // 瘦脸
        var thinFace = 0.15f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Overall", value)
            }

        // 大眼
        var enlargeEye = 0.15f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Eye", value)
            }

        // 瘦颧骨
        var shrinkCheekbone = 0.3f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Zoom_Cheekbone", value)
            }

        // 下颌骨
        var shrinkJawbone = 0.46f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Zoom_Jawbone", value)
            }
        // 美牙
        var whiteTeeth = 0.2f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "BEF_BEAUTY_WHITEN_TEETH", value)
            }

        // 额头
        var hairlineHeight = 0.4f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Forehead", value)
            }

        // 瘦鼻
        var narrowNose = 0.15f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Nose", value)
            }

        // 嘴形
        var mouthSize = 0.16f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_ZoomMouth", value)
            }

        // 下巴
        var chinLength = 0.46f
            set(value) {
                field = value
                renderManager.updateComposerNodes(reSharpNodePath, "Internal_Deform_Chin", value)
            }

        // 亮眼
        var brightEye = 0.0f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beauty4ItemsNodePath, "BEF_BEAUTY_BRIGHTEN_EYE", value)
            }

        // 祛黑眼圈
        var darkCircles = 0.0f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beauty4ItemsNodePath, "BEF_BEAUTY_REMOVE_POUCH", value)
            }

        // 祛法令纹
        var nasolabialFolds = 0.0f
            set(value) {
                field = value
                renderManager.updateComposerNodes(beauty4ItemsNodePath, "BEF_BEAUTY_SMILES_FOLDS", value)
            }

        // 美妆
        var makeUp: MakeUpItem? = null
            set(value) {
                if (field == value) {
                    return
                }
                val oMakeUp = field
                field = value
                if (oMakeUp?.style != value?.style) {
                    if (oMakeUp != null) {
                        val oNodePath =
                            "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/style_makeup/${oMakeUp.style}"
                        renderManager.removeComposerNodes(arrayOf(oNodePath))
                    }

                    if (value != null) {
                        val nodePath =
                            "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/style_makeup/${value.style}"
                        FileUtils.copyAssets(
                            value.context,
                            "$assetsPath/ComposeMakeup.bundle/ComposeMakeup/style_makeup/${value.style}",
                            nodePath
                        )
                        renderManager.appendComposerNodes(arrayOf(nodePath))
                        renderManager.loadResourceWithTimeout(-1)
                    }
                }

                if (value != null) {
                    val nodePath =
                        "$storagePath/beauty_bytedance/ComposeMakeup.bundle/ComposeMakeup/style_makeup/${value.style}"
                    renderManager.updateComposerNodes(
                        nodePath,
                        "Filter_ALL",
                        value.identity
                    )
                    renderManager.updateComposerNodes(
                        nodePath,
                        "Makeup_ALL",
                        value.identity
                    )
                }
            }


        // 贴纸
        var sticker: String? = null
            set(value) {
                if (field == value) {
                    return
                }
                field = value
                if (value != null) {
                    renderManager.setSticker("$stickerPath/$value")
                } else {
                    renderManager.setSticker(null)
                }
            }

        internal fun reset(){
            smooth = 0.3f
            whiten = 0.5f
            thinFace = 0.15f
            enlargeEye = 0.15f
            redden = 0.0f
            shrinkCheekbone = 0.3f
            shrinkJawbone = 0.46f
            whiteTeeth = 0.2f
            hairlineHeight = 0.4f
            narrowNose = 0.15f
            mouthSize = 0.16f
            chinLength = 0.46f
            brightEye = 0.0f
            darkCircles = 0.0f
            nasolabialFolds = 0.0f

            makeUp = null
            sticker = null
        }

        internal fun resume() {
            smooth = smooth
            whiten = whiten
            thinFace = thinFace
            enlargeEye = enlargeEye
            redden = redden
            shrinkCheekbone = shrinkCheekbone
            shrinkJawbone = shrinkJawbone
            whiteTeeth = whiteTeeth
            hairlineHeight = hairlineHeight
            narrowNose = narrowNose
            mouthSize = mouthSize
            chinLength = chinLength
            brightEye = brightEye
            darkCircles = darkCircles
            nasolabialFolds = nasolabialFolds

            makeUp = makeUp
            sticker = sticker
        }
    }

    data class MakeUpItem(
        val context: Context,
        val style: String,
        val identity: Float
    )
}
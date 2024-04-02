package io.agora.scene.show.photographer

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.TextureView
import io.agora.meta.renderer.unity.AgoraAvatarView
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.api.model.User
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.manager.UserManager
import io.agora.scene.base.utils.SPUtil
import org.json.JSONException
import org.json.JSONObject


object AiPhotographerType {
    const val GROUP_ID_AI_PHOTOGRAPHER = 0x00000001 // AI 摄影师
    const val ITEM_ID_AI_PHOTOGRAPHER_NONE = GROUP_ID_AI_PHOTOGRAPHER
    const val ITEM_ID_AI_RHYTHM = GROUP_ID_AI_PHOTOGRAPHER + 1 // AI 律动
    const val ITEM_ID_AI_EDGE_LIGHT = GROUP_ID_AI_PHOTOGRAPHER + 2 // 人物边缘光
    const val ITEM_ID_AI_SHADOW = GROUP_ID_AI_PHOTOGRAPHER + 3 // AI 光影跟随, 属于律动的一种
    const val ITEM_ID_AI_LIGHTING_AD = GROUP_ID_AI_PHOTOGRAPHER + 4 // 广告灯
    const val ITEM_ID_AI_LIGHTING_3D = GROUP_ID_AI_PHOTOGRAPHER + 5 // AI 3D 打光
    const val ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG = GROUP_ID_AI_PHOTOGRAPHER + 6 // 3D 打光+虚拟背景
    const val ITEM_ID_AI_AURORA = GROUP_ID_AI_PHOTOGRAPHER + 7 // 极光特效
}

object IMetaRunningState {
    const val idle = 0
    const val initialized = 1
    const val unityLoaded = 2
    const val sceneLoaded = 3
    const val requestTextureResp = 4
    const val sceneUnloaded = 5
    const val uninitializeFinish = 6
}


object SpecialEffectType {
    const val SETypeNone = 0 // mone
    const val SEType3DLight = 1001 // 3D 打光
    const val SETypeRipple = 1002 //屏幕波纹
    const val SETypeAurora = 1003 // 极光
    const val SETypeFlame = 2001 // 人物边缘火焰
    const val SETypeAmbLight = 3001 // 氛围灯光组
    const val SETypeAdvLight = 3002 // 广告灯
}


object BackgroundType {
    const val BGTypePano = 0
    const val BGType2D = 1
    const val BGType3D = 2
    const val BGTypeNull = 3
}
;


interface OnMetaSceneLoadedListener {
    fun onInitializeFinish()
    fun onUnityLoadFinish()
    fun onLoadSceneResp()
    fun onRequestTextureResp()
    fun onUnloadSceneResp()
    fun onUninitializeFinish()
}

class MetaEngineHandler : AGExtensionHandler {

    companion object {
        private const val TAG = "MetaEngineHandler"
        private const val initial_flag = -1
        private const val metaValue = "{\"company_id\":\"agoraDemo\"," +
                "\"license\":\"" +
                "PFeVRXMZ1hJd075NSZtrZD3tYYl154QTs1Ui2i5ztvOcgOhklZcZdl2f5dYmf6GuLUebbBs1xp5I" +
                "crywJ+NaZ8ncYAfekfg3oR34cYJ8Pe3z4EVzw+CshGOfL0hcSYmtXjkXxG298c64+SGZdP6/UBVJ" +
                "/wIOYJRMgEUTBTqewcQ=\"}"

        const val KEY_META_ASSETS_RESOURCE = "meta_assets_resource"

        var isMetaAssetsResourceReady: Boolean = false
            get() {
                return SPUtil.getBoolean(KEY_META_ASSETS_RESOURCE, false)
            }
            set(newValue) {
                SPUtil.putBoolean(KEY_META_ASSETS_RESOURCE, newValue)
                field = newValue
            }
    }

    private val mMainHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val mCurrentAssetPath by lazy {
        AgoraApplication.the().getExternalFilesDir("assets/metaAssets").toString()
    }
    private val mCurrentMetaFilesPath by lazy {
        AgoraApplication.the().getExternalFilesDir("assets/metaFiles").toString()
    }

    var mRunningState: Int = IMetaRunningState.idle

//    var mEffectId: Int = SpecialEffectType.SETypeNone

    val isEffectModeAvailable: Boolean
        get() {
            return isMetaInit && (mRunningState >= IMetaRunningState.sceneLoaded)
        }

    init {
        System.loadLibrary("apm-plugin-video")
    }

    override fun onStart(provider: String, ext: String) {
        if (provider != "agora_video_filters_metakit" || ext != "metakit") return
        Log.d(TAG, "onStart provider:$provider, ext:$ext")
    }

    override fun onStop(provider: String, ext: String) {
        if (provider != "agora_video_filters_metakit" || ext != "metakit") return
        Log.d(TAG, "onStop provider:$provider, ext:$ext")
    }

    override fun onEvent(provider: String, ext: String, key: String, msg: String) {
        if (provider != "agora_video_filters_metakit" || ext != "metakit") return
        Log.d(TAG, "onEvent provider:$provider, onEvent: $key, msg: $msg")
        when (key) {
            "initializeFinish" -> {
                mRunningState = IMetaRunningState.initialized
                mOnMetaSceneLoadedListener?.onInitializeFinish()
            }

            "unityLoadFinish" -> {
                mRunningState = IMetaRunningState.unityLoaded
                mOnMetaSceneLoadedListener?.onUnityLoadFinish()
            }

            "loadSceneResp" -> {
                mRunningState = IMetaRunningState.sceneLoaded
                mOnMetaSceneLoadedListener?.onLoadSceneResp()
            }

            "setAvatarResp" -> {}
            "requestTextureResp" -> {
                mRunningState = IMetaRunningState.requestTextureResp
                mOnMetaSceneLoadedListener?.onRequestTextureResp()
            }

            "unloadSceneResp" -> {
                mRunningState = IMetaRunningState.sceneUnloaded
                mOnMetaSceneLoadedListener?.onUnloadSceneResp()
            }

            "uninitializeFinish" -> {
                mRunningState = IMetaRunningState.uninitializeFinish
                mOnMetaSceneLoadedListener?.onUninitializeFinish()
            }
        }
    }

    override fun onError(provider: String, ext: String, key: Int, msg: String) {
        if (provider != "agora_video_filters_metakit" || ext != "metakit") return
        Log.d(TAG, "onError: provide:$provider, ext:$ext, key:$key, msg: $msg")
    }

    var mOnMetaSceneLoadedListener: OnMetaSceneLoadedListener? = null

    private val mUser: User
        get() {
            return UserManager.getInstance().user
        }

    private var mLastAiPhotographerId = AiPhotographerType.ITEM_ID_AI_PHOTOGRAPHER_NONE

    // AI 摄影师是否开启
    val isAiPhotographerEnable: Boolean
        get() {
            return mLastAiPhotographerId != AiPhotographerType.ITEM_ID_AI_PHOTOGRAPHER_NONE
        }

    fun updateAiPhotographerId(id: Int) {
        mLastAiPhotographerId = id
    }

    var isMetaInit = false

    external fun getTextureViewHandler(view: AgoraAvatarView): Long

    external fun getTextureViewHandler(view: TextureView): Long

    external fun getContextHandler(context: Activity): Long

    external fun destroyHandles()

    private var mRtcEngine: RtcEngine? = null

    fun initializeRtc(rtcEngine: RtcEngine) {
        this.mRtcEngine = rtcEngine
    }

    fun initializeMeta(activity: Activity) {
        if (isMetaInit) return
        val rtcEngine = mRtcEngine ?: return
        val valueObj = JSONObject()
        try {
            val address = getContextHandler(activity)
            valueObj.put("activityContext", address.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "setActivityContext", valueObj.toString()
        )
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "initialize", "{}"
        )
        isMetaInit = true
    }

    fun enableSegmentation() {
        Log.i(TAG, "metakitx enableSegmentation")
        val rtcEngine = mRtcEngine ?: return
        val source = VirtualBackgroundSource()
        source.backgroundSourceType = 0 //0 for background none; 1 for background color
        source.color = 0xFFFFFF //0xffffff;
        source.source = ""
        source.blurDegree = 1
        val param = SegmentationProperty()
        param.modelType = 1 //1 for AI, 2 for green screen
        param.greenCapacity = 0.5f
        val ret: Int =
            rtcEngine.enableVirtualBackground(true, source, param, Constants.MediaSourceType.PRIMARY_CAMERA_SOURCE)
        Log.i(TAG, "metakitx enable seg ret: $ret")
    }

    fun loadScene() {
        val rtcEngine = mRtcEngine ?: return
        val valueObj = JSONObject()
        try {
            val sceneObj = JSONObject()
            sceneObj.put("scenePath", mCurrentAssetPath)
            val customObj = JSONObject()
            customObj.put("sceneIndex", 0)
            valueObj.put("sceneInfo", sceneObj)
            valueObj.put("assetManifest", "")
            valueObj.put("userId", mUser.id.toString())
            valueObj.put("extraCustomInfo", customObj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.d(TAG, "enterScene:$valueObj")
        Log.d(TAG, "runningState:$mRunningState")
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit",
            "loadScene",
            valueObj.toString()
        )
    }

    fun leaveScene() {
        Log.d(TAG, " removeMainSceneView")
        Log.d(TAG, "setExtensionProperty unloadScene")
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "unloadScene", "{}"
        )
        isMetaInit = false
        Log.d(TAG, "setExtensionProperty unloadScene exit")
    }

    fun destroy() {
        destroyHandles()
        Log.d(TAG, "setExtensionProperty destroy")
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "destroy", "{}"
        )
        isMetaInit = false
        mRunningState = IMetaRunningState.idle
    }

    /**
     * 律动
     */
    fun configAiRhythm(enable: Boolean, aiPhotographerType: Int = AiPhotographerType.ITEM_ID_AI_PHOTOGRAPHER_NONE) {
        val rhythmMode = when (aiPhotographerType) {
            AiPhotographerType.ITEM_ID_AI_RHYTHM -> "6"
            AiPhotographerType.ITEM_ID_AI_SHADOW -> "2"
            else -> ""
        }
        Log.d(TAG, "---configAiRhythm--:$enable rhythmMode:$rhythmMode")
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.enableExtension("agora_video_filters_portrait_rhythm", "portrait_rhythm", enable)
        if (enable) {
            rtcEngine.setExtensionProperty(
                "agora_video_filters_portrait_rhythm",
                "portrait_rhythm", "mode", rhythmMode
            )
        }
    }

    // 停止律动
    fun stopAiRhythm() {
        if (mLastAiPhotographerId == AiPhotographerType.ITEM_ID_AI_RHYTHM
            || mLastAiPhotographerId == AiPhotographerType.ITEM_ID_AI_SHADOW
        ) {
            configAiRhythm(false)
        }
    }

    // 灯光特效
    fun configEffect3D(
        enable: Boolean,
        aiPhotographerType: Int = AiPhotographerType.ITEM_ID_AI_PHOTOGRAPHER_NONE
    ) {
        if (enable) {
            enableSegmentation()
            val effect3DId: Int = when (aiPhotographerType) {
                AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT -> SpecialEffectType.SETypeFlame
                AiPhotographerType.ITEM_ID_AI_LIGHTING_AD -> SpecialEffectType.SETypeAdvLight
                AiPhotographerType.ITEM_ID_AI_LIGHTING_3D -> SpecialEffectType.SEType3DLight
                AiPhotographerType.ITEM_ID_AI_AURORA -> SpecialEffectType.SETypeAurora
                AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> SpecialEffectType.SETypeAurora
                else -> 0
            }
            if (effect3DId > 0) {
                configEffectLight(true, effect3DId)
            }
            if (aiPhotographerType == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
                setMetaBGMode(BackgroundType.BGTypePano)
            }
        } else {
            stopEffect3D()
        }
    }

    // 停止灯光特效
    fun stopEffect3D() {
        val effect3DId: Int = when (mLastAiPhotographerId) {
            AiPhotographerType.ITEM_ID_AI_EDGE_LIGHT -> SpecialEffectType.SETypeFlame
            AiPhotographerType.ITEM_ID_AI_LIGHTING_AD -> SpecialEffectType.SETypeAdvLight
            AiPhotographerType.ITEM_ID_AI_LIGHTING_3D -> SpecialEffectType.SEType3DLight
            AiPhotographerType.ITEM_ID_AI_AURORA -> SpecialEffectType.SETypeAurora
            AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG -> SpecialEffectType.SETypeAurora
            else -> 0
        }
        if (effect3DId > 0) {
            configEffectLight(false, effect3DId)
        }
        if (mLastAiPhotographerId == AiPhotographerType.ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG) {
            setMetaBGMode(BackgroundType.BGTypeNull)
        }
    }

    // 灯光特效
    private fun configEffectLight(enable: Boolean, id: Int) {
        Log.d(TAG, "metakitx configEffect light, mode: $enable, id: $id")
        val configObj = JSONObject()
        try {
            configObj.put("id", id)
            configObj.put("enable", enable)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val rtcEngine = mRtcEngine ?: return
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "setEffectVideo", configObj.toString()
        )
    }

    fun requestTextureVB() {
        Log.d(TAG, "metakitx configEffectTextureVB, mRunningState: $mRunningState")
        if (mRunningState < IMetaRunningState.unityLoaded) return
        val rtcEngine = mRtcEngine ?: return
        val resolutionW: Int = 720
        val resolutionH: Int = 1280
        val index = 0
        val valueObj = JSONObject()
        try {
            valueObj.put("index", index)
            valueObj.put("enable", true)
            val configObj = JSONObject()
            configObj.put("width", resolutionW)
            configObj.put("height", resolutionH)
            val extraObj = JSONObject()
            extraObj.put("sceneIndex", 0)
            extraObj.put("avatarMode", 2)
            extraObj.put("backgroundEffect", true)
            extraObj.put("userId", mUser.id.toString())
            configObj.put("extraInfo", extraObj.toString())
            valueObj.put("config", configObj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "requestTexture", valueObj.toString()
        )
    }

    fun setMetaBGMode(bgMode: Int) {
        Log.d(TAG, "metakitx setMetaBGMode: $bgMode")
        val rtcEngine = mRtcEngine ?: return
        var filePath = ""
        var mode = ""
        var gyroState = "off"
        when (bgMode) {
            BackgroundType.BGTypePano -> {
                mode = "tex360"
                filePath = "$mCurrentMetaFilesPath/metaFiles/pano.jpg"
                gyroState = "on"
            }
            BackgroundType.BGTypeNull -> {
                mode = "off"
                filePath = ""
                gyroState = "off"
            }
            else -> {}
        }
        val picObj = JSONObject()
        try {
            picObj.put("mode", mode)
            val configObj = JSONObject()
            configObj.put("path", filePath)
            picObj.put("param", configObj)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "setBGVideo", picObj.toString()
        )
        val gyroObj = JSONObject()
        try {
            gyroObj.put("state", gyroState)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        rtcEngine.setExtensionProperty(
            "agora_video_filters_metakit",
            "metakit", "setCameraGyro", gyroObj.toString()
        )
    }

    fun resetAiPhotographer() {
        stopAiRhythm()
        stopEffect3D()
    }
}
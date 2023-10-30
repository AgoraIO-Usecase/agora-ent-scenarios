package io.agora.scene.show.beauty.sensetime

import android.content.Context
import android.util.Log
import com.softsugar.stmobile.STCommonNative
import com.softsugar.stmobile.STMobileAuthentificationNative
import com.softsugar.stmobile.STMobileEffectNative
import com.softsugar.stmobile.STMobileEffectParams
import com.softsugar.stmobile.STMobileHumanActionNative
import com.softsugar.stmobile.params.STEffectBeautyGroup
import com.softsugar.stmobile.params.STEffectBeautyType
import com.softsugar.stmobile.params.STHumanActionParamsType
import io.agora.scene.show.utils.FileUtils

object SenseTimeBeautySDK {
    private const val TAG = "SenseTimeBeautySDK"

    private const val resourcePath = "beauty_sensetime"
    private const val humanActionCreateConfig = 0

    private const val MODEL_106 = "models/M_SenseME_Face_Video_Template_p_3.9.0.3.model" // 106
    private const val MODEL_FACE_EXTRA = "models/M_SenseME_Face_Extra_Advanced_Template_p_2.0.0.model" // 282
    private const val MODEL_AVATAR_HELP = "models/M_SenseME_Avatar_Help_p_2.3.7.model" // avatar人脸驱动
    private const val MODEL_LIPS_PARSING = "models/M_SenseME_MouthOcclusion_p_1.3.0.1.model" // 嘴唇分割
    private const val MODEL_HAND = "models/M_SenseME_Hand_p_6.0.8.1.model" // 手势
    private const val MODEL_SEGMENT = "models/M_SenseME_Segment_Figure_p_4.14.1.1.model" // 前后背景分割
    private const val MODEL_SEGMENT_HAIR = "models/M_SenseME_Segment_Hair_p_4.4.0.model" // 头发分割
    private const val MODEL_FACE_OCCLUSION = "models/M_SenseME_FaceOcclusion_p_1.0.7.1.model" // 妆容遮挡
    private const val MODEL_SEGMENT_SKY = "models/M_SenseME_Segment_Sky_p_1.1.0.1.model" // 天空分割
    private const val MODEL_SEGMENT_SKIN = "models/M_SenseME_Segment_Skin_p_1.0.1.1.model" // 皮肤分割
    private const val MODEL_3DMESH = "models/M_SenseME_3DMesh_Face2396pt_280kpts_Ear_p_1.1.0v2.model" // 3DMesh
    private const val MODEL_HEAD_P_EAR = "models/M_SenseME_Ear_p_1.0.1.1.model" // 搭配 mesh 耳朵模型
    private const val MODEL_360HEAD_INSTANCE = "models/M_SenseME_3Dmesh_360Head2396pt_p_1.0.0.1.model" // 360度人头mesh
    private const val MODEL_FOOT = "models/M_SenseME_Foot_p_2.10.7.model" // 鞋子检测模型
    private const val MODEL_PANT = "models/M_SenseME_Segment_Trousers_p_1.1.10.model" // 裤腿的检测
    private const val MODEL_WRIST = "models/M_SenseME_Wrist_p_1.4.0.model" // 试表
    private const val MODEL_CLOTH = "models/M_SenseME_Segment_Clothes_p_1.0.2.2.model" // 衣服分割
    private const val MODEL_HEAD_INSTANCE = "models/M_SenseME_Segment_Head_Instance_p_1.1.0.1.model" // 实例分割版本
    private const val MODEL_HEAD_P_INSTANCE = "models/M_SenseME_Head_p_1.3.0.1.model" // 360度人头-头部模型
    private const val MODEL_NAIL = "models/M_SenseME_Nail_p_2.4.0.model" // 指甲检测



    private var stickerPackagePair : Pair<String, Int>? = null
    private var stylePackagePair : Pair<String, Int>? = null

    var isLicenseCheckSuccess = false
        @JvmStatic get
    val mobileEffectNative = STMobileEffectNative()
        @JvmStatic get
    val humanActionNative = STMobileHumanActionNative()
        @JvmStatic get


    @JvmStatic
    fun initBeautySDK(context: Context){
        checkLicense(context)
        initHumanAction(context)
    }

    @JvmStatic
    fun initMobileEffect(context: Context){
        val result =
            mobileEffectNative.createInstance(context, STMobileEffectNative.EFFECT_CONFIG_NONE)
        mobileEffectNative.setParam(STMobileEffectParams.EFFECT_PARAM_QUATERNION_SMOOTH_FRAME, 5f)
        Log.d(TAG, "SenseTime >> STMobileEffectNative create result : $result")
    }

    @JvmStatic
    fun unInitMobileEffect(){
        mobileEffectNative.destroyInstance()
        stylePackagePair = null
        stickerPackagePair = null
    }

    private fun checkLicense(context: Context) {
        val license = FileUtils.getAssetsString(
            context,
            "$resourcePath/license/SenseME.lic"
        )
        val activeCode = STMobileAuthentificationNative.generateActiveCodeFromBuffer(
            context,
            license,
            license.length
        )
        val success = activeCode.isNotEmpty()
        if (success) {
            isLicenseCheckSuccess = true
            Log.d(TAG, "SenseTime >> checkLicense successfully!")
        } else {
            Log.e(TAG, "SenseTime >> checkLicense failed!")
        }
    }

    private fun initHumanAction(context: Context){
        val assets = context.assets
        val result = humanActionNative.createInstanceFromAssetFile(
            "$resourcePath/$MODEL_106",
            humanActionCreateConfig,
            assets
        )
        Log.d(TAG, "SenseTime >> STMobileHumanActionNative create result : $result")

        if(result != 0){
            return
        }

        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_HAND", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FACE_EXTRA", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT_HAIR", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_LIPS_PARSING", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FACE_OCCLUSION", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_SEGMENT_SKY", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_AVATAR_HELP", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_FOOT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_PANT", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_3DMESH", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_WRIST", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_CLOTH", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_HEAD_INSTANCE", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_360HEAD_INSTANCE", assets)
        humanActionNative.addSubModelFromAssetFile("$resourcePath/$MODEL_NAIL", assets)

        // 背景分割羽化程度[0,1](默认值0.35),0 完全不羽化,1羽化程度最高,在strenth较小时,羽化程度基本不变.值越大,前景与背景之间的过度边缘部分越宽.
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_BACKGROUND_BLUR_STRENGTH, 0.35f)
        // 设置face mesh结果输出坐标系,(0: 屏幕坐标系， 1：3d世界坐标系， 2:3d摄像机坐标系,是摄像头透视投影坐标系, 原点在摄像机 默认是0）
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_FACE_MESH_OUTPUT_FORMAT, 1.0f)
        // 设置mesh渲染模式
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_MESH_MODE, STCommonNative.MESH_CONFIG.toFloat())
        // 设置人头实例分割
        humanActionNative.setParam(STHumanActionParamsType.ST_HUMAN_ACTION_PARAM_HEAD_SEGMENT_INSTANCE, 1.0f)
    }

    @JvmStatic
    fun setSticker(context: Context, path: String) {
        if(path.isNotEmpty()){
            if(stickerPackagePair?.first == path){
                return
            }
            val assets = context.assets
            stickerPackagePair?.let { mobileEffectNative.removeEffect(it.second) }
            stickerPackagePair = Pair(path, mobileEffectNative.addPackageFromAssetsFile("$resourcePath/$path", assets))
        }else{
            stickerPackagePair?.let { mobileEffectNative.removeEffect(it.second) }
            stickerPackagePair = null
        }
    }

    @JvmStatic
    fun setStyle(context: Context, path: String, strength: Float){
        if(path.isNotEmpty()){
            if(stylePackagePair?.first != path){
                val assets = context.assets
                stylePackagePair?.let { mobileEffectNative.removeEffect(it.second) }
                stylePackagePair = Pair(path, mobileEffectNative.addPackageFromAssetsFile("$resourcePath/$path", assets))
            }
            val packageId = stylePackagePair?.second ?: return
            mobileEffectNative.setPackageBeautyGroupStrength(packageId, STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER, strength)
            mobileEffectNative.setPackageBeautyGroupStrength(packageId, STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP, strength)
        }else{
            stylePackagePair?.let { mobileEffectNative.removeEffect(it.second) }
            stylePackagePair = null
        }
    }

    @JvmStatic
    fun setFilter(context: Context, path: String, strength: Float){
        val assets = context.assets
        mobileEffectNative.setBeautyFromAssetsFile(STEffectBeautyType.EFFECT_BEAUTY_FILTER, "$resourcePath/$path", assets)
        mobileEffectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_FILTER, strength)
    }

}
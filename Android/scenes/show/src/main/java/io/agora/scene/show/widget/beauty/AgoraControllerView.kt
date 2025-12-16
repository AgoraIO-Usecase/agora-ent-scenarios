package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.beauty.AgoraBeautySDK

class AgoraControllerView : BaseControllerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPageListCreate(): List<PageInfo> {
        val beautyConfig = AgoraBeautySDK.beautyConfig
        return listOf(
            PageInfo(
                R.string.show_beauty_group_beauty,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = !beautyConfig.beauty,
                        onValueChanged = { _ ->
                            beautyConfig.beauty = false
                            beautyConfig.faceShape = false
                        }
                    ),
                    // 重置
                    ItemInfo(
                        R.string.show_beauty_item_beauty_reset,
                        R.mipmap.show_beauty_ic_adjust_contrast,
                        onValueChanged = { value ->
                            beautyConfig.beauty = true
                            beautyConfig.faceShape = true
                            beautyConfig.resetBeauty()
                        }
                    ),
                    // 1. 磨皮
                    ItemInfo(
                        R.string.show_beauty_item_beauty_smooth,
                        R.mipmap.show_beauty_ic_face_mopi,
                        beautyConfig.smoothness * 100,
                        isSelected = beautyConfig.beauty,
                        onValueChanged = { value ->
                            beautyConfig.smoothness = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 2. 美白自然白
                    ItemInfo(
                        R.string.show_beauty_item_beauty_whitenNatural,
                        R.mipmap.show_beauty_ic_face_meibai,
                        beautyConfig.whitenNatural * 100,
                        onValueChanged = { value ->
                            beautyConfig.whitenNatural = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 3. 红润
                    ItemInfo(
                        R.string.show_beauty_item_beauty_redden,
                        R.mipmap.show_beauty_ic_face_redden,
                        beautyConfig.redness * 100,
                        onValueChanged = { value ->
                            beautyConfig.redness = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 4. 锐化
                    ItemInfo(
                        R.string.show_beauty_item_adjust_sharpen,
                        R.mipmap.show_beauty_ic_adjust_sharp,
                        beautyConfig.sharpen * 100,
                        onValueChanged = { value ->
                            beautyConfig.sharpen = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 5. 清晰度
                    ItemInfo(
                        R.string.show_beauty_item_adjust_clarity,
                        R.mipmap.show_beauty_ic_adjust_clear,
                        beautyConfig.clarity * 50,
                        onValueChanged = { value ->
                            beautyConfig.clarity = value / 50
                        },
                        valueRange = -50f..50f
                    ),
                    // 6. 瘦脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_overall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceContour.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.faceContour = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 7. 小头
                    ItemInfo(
                        R.string.show_beauty_item_beauty_headScale,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.headScale.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.headScale = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 8. 窄脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_narrowFace,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceWidth.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.faceWidth = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 9. 长脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_longFace,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceLength.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.faceLength = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 10. 瘦颧骨
                    ItemInfo(
                        R.string.show_beauty_item_beauty_cheekbone,
                        R.mipmap.show_beauty_ic_face_shouquangu,
                        beautyConfig.cheekbone.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.cheekbone = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 11. 下颌线
                    ItemInfo(
                        R.string.show_beauty_item_beauty_xiahexian,
                        R.mipmap.show_beauty_ic_face_xiahexian,
                        beautyConfig.shrinkCheek.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheek = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 12. v脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mandible,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.mandible.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mandible = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 13. 瘦下巴
                    ItemInfo(
                        R.string.show_beauty_item_beauty_chin_length,
                        R.mipmap.show_beauty_ic_face_xiaba,
                        beautyConfig.chinLength.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.chinLength = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 14. 发际线
                    ItemInfo(
                        R.string.show_beauty_item_beauty_fajixian,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.hairlineHeight.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = value.toInt()* 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 15. 去法令纹
                    ItemInfo(
                        R.string.show_beauty_item_beauty_remove_nasolabial_folds,
                        R.mipmap.show_beauty_ic_face_remove_nasolabial_folds,
                        beautyConfig.nasolabialFolds * 100,
                        onValueChanged = { value ->
                            beautyConfig.nasolabialFolds = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 16. 大眼
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_big,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.enlargeEye.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 17. 亮眼
                    ItemInfo(
                        R.string.show_beauty_item_beauty_bright_eye,
                        R.mipmap.show_beauty_ic_face_bright_eye,
                        beautyConfig.brightenEye * 100,
                        onValueChanged = { value ->
                            beautyConfig.brightenEye = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 18. 去眼袋
                    // 19. 去黑眼圈
                    ItemInfo(
                        R.string.show_beauty_item_beauty_remove_dark_circles,
                        R.mipmap.show_beauty_ic_face_remove_dark_circles,
                        beautyConfig.darkCircle * 100,
                        onValueChanged = { value ->
                            beautyConfig.darkCircle = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 20. 眼移动
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_position,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyePosition.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyePosition = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 21. 眼距
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_distance,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeDistance.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyeDistance = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 22. 瞳孔
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_pupil,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyePupil.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.eyePupil = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 23. 眼睑下至
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyelid,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeLid.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.eyeLid = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 24. 内眼角
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_innercorner,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeInnercorner.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyeInnercorner = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 25. 外眼角
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_outercorner,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeOutercorner.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyeOutercorner = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 26. 瘦鼻
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_width,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.narrowNose.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 27. 长鼻
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_long,
                        R.mipmap.show_beauty_ic_face_changbi,
                        beautyConfig.noseLength.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.noseLength = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 28. 鼻翼
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_wing,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.noseWing.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.noseWing = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 29. 鼻梁
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_bridge,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.noseBridge.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.noseBridge = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 30. 山根
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_root,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.noseRoot.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.noseRoot = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 31. 鼻尖
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_tip,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.noseTip.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.noseTip = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 32. 鼻综合
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_general,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.noseGeneral.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.noseGeneral = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 33. 嘴型
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSize.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 34. 缩人中
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_position,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthPosition.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mouthPosition = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 35. 微笑唇
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_smile,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSmile.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mouthSmile = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 36. 丰唇
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_lip,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthLip.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mouthLip = value.toInt()
                        },
                        valueRange = 0f..100f
                    ),
                    // 37. 白牙
                    ItemInfo(
                        R.string.show_beauty_item_beauty_white_teeth,
                        R.mipmap.show_beauty_ic_face_meiya,
                        beautyConfig.whitenTeeth * 100,
                        onValueChanged = { value ->
                            beautyConfig.whitenTeeth = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 38. 眉上下
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyebrow_position,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyebrowPosition.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyebrowPosition = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                    // 39. 眉粗细
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyebrow_thickness,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyebrowThickness.toFloat() / 2,
                        onValueChanged = { value ->
                            beautyConfig.eyebrowThickness = value.toInt() * 2
                        },
                        valueRange = -50f..50f
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_adjust1,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = true,
                        onValueChanged = { _ ->
                            beautyConfig.hue = 0.0f
                            beautyConfig.temperature = 0.0f
                            beautyConfig.saturation = 0.0f
                            beautyConfig.brightness = 0.0f
                        }
                    ),
                    // 色调
                    ItemInfo(
                        R.string.show_beauty_item_beauty_hue,
                        R.mipmap.show_beauty_ic_adjust_clear,
                        beautyConfig.hue * 50,
                        onValueChanged = { value ->
                            beautyConfig.hue = value / 50
                        },
                        valueRange = -50f..50f
                    ),
                    // 色温
                    ItemInfo(
                        R.string.show_beauty_item_beauty_temp,
                        R.mipmap.show_beauty_ic_adjust_clear,
                        beautyConfig.temperature * 50,
                        onValueChanged = { value ->
                            beautyConfig.temperature = value / 50
                        },
                        valueRange = -50f..50f
                    ),
                    // 饱和度
                    ItemInfo(
                        R.string.show_beauty_item_adjust_saturation,
                        R.mipmap.show_beauty_ic_adjust_saturation,
                        beautyConfig.saturation * 50,
                        onValueChanged = { value ->
                            beautyConfig.saturation = value / 50
                        },
                        valueRange = -50f..50f
                    ),
                    // 亮度
                    ItemInfo(
                        R.string.show_beauty_item_beauty_brightness,
                        R.mipmap.show_beauty_ic_adjust_clear,
                        beautyConfig.brightness * 50,
                        onValueChanged = { value ->
                            beautyConfig.brightness = value / 50
                        },
                        valueRange = -50f..50f
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_makeup,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.makeupName == null,
                        onValueChanged = { _ ->
                            beautyConfig.makeupName = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_xuemei,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "学妹妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "学妹妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_xuejie,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "学姐妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "学姐妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_qizhi,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "气质妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "气质妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_baixi,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "白皙妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "白皙妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_youya,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "优雅妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "优雅妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_fenyun,
                        R.mipmap.show_beauty_ic_effect_hunxue,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "粉晕妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "粉晕妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_qiaopi,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "俏皮妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "俏皮妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_shaonv,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "少女妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "少女妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_shenshui,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "深邃妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "深邃妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_yinyun,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupName == "氤氲妆",
                        value = beautyConfig.makeupStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.makeupName = "氤氲妆"
                            beautyConfig.makeupStrength = value / 100
                        },
                        valueRange = 0f..100f
                    )
                )
            ),
            PageInfo(
                R.string.show_beauty_group_filter,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.filterName == null,
                        onValueChanged = { _ ->
                            beautyConfig.filterName = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_chenwen,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "沉稳",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "沉稳"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_dushi,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "都市",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "都市"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_liuguang,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "流光",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "流光"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_liujin,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "流金",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "流金"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_naiyou,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "奶油",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "奶油"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_natie,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "拿铁",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "拿铁"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_ningxia,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "柠夏",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "柠夏"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_richang,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "日常",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "日常"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_shenshi,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "绅士",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "绅士"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_xiangcao,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "香草",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "香草"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_baici,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "白瓷",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "白瓷"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_baitao,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "白桃",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "白桃"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_cangmo,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "苍墨",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "苍墨"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_jiaopian,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "胶片",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "胶片"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_jiqin,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "霁晴",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "霁晴"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_manhua,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "漫画",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "漫画"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_menghuan,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "梦幻",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "梦幻"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_mianrong,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "棉绒",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "棉绒"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_suda,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "苏打",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "苏打"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_yuebai,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "月白",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "月白"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_baicha,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "白茶",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "白茶"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_chenmi,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "沉谧",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "沉谧"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_ins,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "ins风",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "ins风"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_laojie,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "老街",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "老街"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_paofu,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "泡芙",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "泡芙"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_sicang,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "私藏",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "私藏"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_yanqishui,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "盐汽水",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "盐汽水"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_zhigan,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "质感",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "质感"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_qise,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "气色",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "气色"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_chuxue,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "初雪",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "初雪"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_fenxia,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "粉霞",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "粉霞"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_huaijiu,
                        R.mipmap.show_beauty_ic_filter_yuansheng,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "怀旧",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "怀旧"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_jiaotang,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "焦糖",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "焦糖"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_weixun,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "微醺",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "微醺"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_xunyicao,
                        R.mipmap.show_beauty_ic_filter_lengbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "薰衣草",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "薰衣草"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_yanzhi,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "胭脂",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "胭脂"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_yinyun,
                        R.mipmap.show_beauty_ic_filter_nenbai,
                        withPadding = false,
                        isSelected = beautyConfig.filterName == "氤氲",
                        value = beautyConfig.filterStrength * 100,
                        onValueChanged = { value ->
                            beautyConfig.filterName = "氤氲"
                            beautyConfig.filterStrength = value / 100
                        },
                        valueRange = 0f..100f
                    )
                )
            ),
            PageInfo(
                R.string.show_beauty_group_sticker,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.stickerName == null,
                        onValueChanged = { _ ->
                            beautyConfig.stickerName = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_christmas,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "圣诞节",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "圣诞节"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_squid,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "章鱼",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "章鱼"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_piggy,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "猪可爱",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "猪可爱"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_long_cat,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "辫子猫",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "辫子猫"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_hairhoop,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "粉色发箍",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "粉色发箍"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_relax_time,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "没有烦恼",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "没有烦恼"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_cat,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "卡通猫",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "卡通猫"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_butterfly,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "蝴蝶",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "蝴蝶"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_brush,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "粉刷时光",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "粉刷时光"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_cyber_glass,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "赛博眼镜",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "赛博眼镜"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_cyber_neon_tiara,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "霓虹皇冠",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "霓虹皇冠"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_cartoon_cyber_neon_love_glass,
                        R.mipmap.show_beauty_ic_sticer_zhaocaimao,
                        withPadding = false,
                        isSelected = beautyConfig.stickerName == "爱心眼镜",
                        onValueChanged = { value ->
                            beautyConfig.stickerName = "爱心眼镜"
                        }
                    )
                )
            )

        )
    }

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.name == R.string.show_beauty_item_beauty_reset) {
            // Call reset callback to close dialog
            onResetClickListener?.invoke()
        }
        if (itemInfo.name == R.string.show_beauty_item_none ||
            itemInfo.name == R.string.show_beauty_item_beauty_reset ||
            pageInfo.name == R.string.show_beauty_group_sticker

        ) {
            viewBinding.slider.visibility = View.INVISIBLE
            // TODO:  hide Beauty switch
//            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_filter
            || pageInfo.name == R.string.show_beauty_group_adjust1
            || pageInfo.name == R.string.show_beauty_group_makeup
        ) {
            viewBinding.slider.visibility = View.VISIBLE
            // TODO:  hide Beauty switch
//            viewBinding.ivCompare.isVisible = true
        }
    }
}
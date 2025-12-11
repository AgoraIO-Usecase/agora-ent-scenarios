package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.beauty.FaceUnityBeautySDK

class FaceUnityControllerView : BaseControllerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPageListCreate(): List<PageInfo> {
        val beautyConfig = FaceUnityBeautySDK.beautyConfig
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
//                            beautyConfig.smooth = 0.0f
//                            beautyConfig.whiten = 0.0f
//                            beautyConfig.redness = 0.0f
//                            beautyConfig.sharpen = 0.0f
//                            beautyConfig.clarity = 0.0f
//                            beautyConfig.faceContour = 0.0f
//                            beautyConfig.faceSmall = 0.0f
//                            beautyConfig.faceWidth = 0.0f
//                            beautyConfig.faceLength = 0.0f
//                            beautyConfig.shrinkCheekbone = 0.0f
//                            beautyConfig.shrinkJawbone = 0.0f
//                            beautyConfig.mandible = 0.0f
//                            beautyConfig.chinLength = 0.5f
//                            beautyConfig.hairlineHeight = 0.5f
//                            beautyConfig.nasolabialFolds = 0.0f
//                            beautyConfig.enlargeEye = 0.0f
//                            beautyConfig.brightEye = 0.0f
//                            beautyConfig.darkCircles = 0.0f
//                            beautyConfig.eyePosition = 0.5f
//                            beautyConfig.eyeDistance = 0.5f
//                            beautyConfig.eyeLid = 0.0f
//                            beautyConfig.eyecorner = 0.5f
//                            beautyConfig.narrowNose = 0.0f
//                            beautyConfig.noseLength = 0.5f
//                            beautyConfig.mouthSize = 0.5f
//                            beautyConfig.mouthPosition = 0.5f
//                            beautyConfig.mouthSmile = 0.5f
//                            beautyConfig.mouthLip = 0.5f
//                            beautyConfig.whiteTeeth = 0.0f
//                            beautyConfig.eyebrowPosition = 0.5f
//                            beautyConfig.eyebrowThickness = 0.0f
//                            beautyConfig.faceThree = 0.0f
                        }
                    ),
                    // 重置
                    ItemInfo(
                        R.string.show_beauty_item_beauty_reset,
                        R.mipmap.show_beauty_ic_adjust_contrast,
                        onValueChanged = { value ->
                            beautyConfig.beauty = true
                            beautyConfig.resetBeauty()
                        }
                    ),
                    // 1. 磨皮
                    ItemInfo(
                        R.string.show_beauty_item_beauty_smooth,
                        R.mipmap.show_beauty_ic_face_mopi,
                        beautyConfig.smooth * 100,
                        isSelected = beautyConfig.beauty,
                        onValueChanged = { value ->
                            beautyConfig.smooth = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 2. 美白
                    ItemInfo(
                        R.string.show_beauty_item_beauty_whiten,
                        R.mipmap.show_beauty_ic_face_meibai,
                        beautyConfig.whiten * 100,
                        onValueChanged = { value ->
                            beautyConfig.whiten = value / 100
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
                        beautyConfig.clarity * 100,
                        onValueChanged = { value ->
                            beautyConfig.clarity = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 6. 瘦脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_overall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceContour * 100,
                        onValueChanged = { value ->
                            beautyConfig.faceContour = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 7. 小脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_faceSmall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceSmall * 100,
                        onValueChanged = { value ->
                            beautyConfig.faceSmall = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 8. 窄脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_narrowFace,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceWidth * 100,
                        onValueChanged = { value ->
                            beautyConfig.faceWidth = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 9. 长脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_longFace,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.faceLength * 100,
                        onValueChanged = { value ->
                            beautyConfig.faceLength = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 10. 瘦颧骨
                    ItemInfo(
                        R.string.show_beauty_item_beauty_cheekbone,
                        R.mipmap.show_beauty_ic_face_shouquangu,
                        beautyConfig.shrinkCheekbone * 100,
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheekbone = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 11. 下颌线
                    ItemInfo(
                        R.string.show_beauty_item_beauty_jawbone,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.shrinkJawbone * 100,
                        onValueChanged = { value ->
                            beautyConfig.shrinkJawbone = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 12. v脸
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mandible,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.mandible * 100,
                        onValueChanged = { value ->
                            beautyConfig.mandible = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 13. 瘦下巴
                    ItemInfo(
                        R.string.show_beauty_item_beauty_chin_length,
                        R.mipmap.show_beauty_ic_face_xiaba,
                        beautyConfig.chinLength * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.chinLength = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 14. 发际线
                    ItemInfo(
                        R.string.show_beauty_item_beauty_forehead,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.hairlineHeight * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = (value + 50) / 100
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
                        beautyConfig.enlargeEye * 100,
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 17. 亮眼
                    ItemInfo(
                        R.string.show_beauty_item_beauty_bright_eye,
                        R.mipmap.show_beauty_ic_face_bright_eye,
                        beautyConfig.brightEye * 100,
                        onValueChanged = { value ->
                            beautyConfig.brightEye = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 18. 去眼袋
                    // 19. 去黑眼圈
                    ItemInfo(
                        R.string.show_beauty_item_beauty_remove_dark_circles,
                        R.mipmap.show_beauty_ic_face_remove_dark_circles,
                        beautyConfig.darkCircles * 100,
                        onValueChanged = { value ->
                            beautyConfig.darkCircles = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 20. 眼移动
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_position,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyePosition * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.eyePosition = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 21. 眼距
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_distance,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeDistance * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.eyeDistance = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 22. 瞳孔
                    // 23. 眼睑
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyelid,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyeLid * 100,
                        onValueChanged = { value ->
                            beautyConfig.eyeLid = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 24. 眼角
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye_corner,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyecorner * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.eyecorner = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 26. 瘦鼻
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_width,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.narrowNose * 100,
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 27. 长鼻
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose_long,
                        R.mipmap.show_beauty_ic_face_changbi,
                        beautyConfig.noseLength * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.noseLength = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 28. 鼻翼
                    // 29. 鼻梁
                    // 30. 山根
                    // 31. 鼻尖
                    // 32. 鼻综合
                    // 33. 嘴型
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSize * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 34. 缩人中
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_position,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthPosition * 100,
                        onValueChanged = { value ->
                            beautyConfig.mouthPosition = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 35. 微笑唇
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_smile,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSmile * 100,
                        onValueChanged = { value ->
                            beautyConfig.mouthSmile = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 36. 丰唇
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth_lip,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthLip * 100,
                        onValueChanged = { value ->
                            beautyConfig.mouthLip = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 37. 白牙
                    ItemInfo(
                        R.string.show_beauty_item_beauty_teeth2,
                        R.mipmap.show_beauty_ic_face_meiya,
                        beautyConfig.whiteTeeth * 100,
                        onValueChanged = { value ->
                            beautyConfig.whiteTeeth = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                    // 38. 眉上下
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyebrow_position,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyebrowPosition * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.eyebrowPosition = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 39. 眉粗细
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eyebrow_thickness,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.eyebrowThickness * 100 - 50,
                        onValueChanged = { value ->
                            beautyConfig.eyebrowThickness = (value + 50) / 100
                        },
                        valueRange = -50f..50f
                    ),
                    // 五官立体
                    ItemInfo(
                        R.string.show_beauty_item_beauty_face_three,
                        R.mipmap.show_beauty_ic_face_liti,
                        beautyConfig.faceThree * 100,
                        onValueChanged = { value ->
                            beautyConfig.faceThree = value / 100
                        },
                        valueRange = 0f..100f
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_effect,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.makeUp == null,
                        onValueChanged = { _ ->
                            beautyConfig.makeUp = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_diadiatu,
                        R.mipmap.show_beauty_ic_effect_diadiatu,
                        withPadding = false,
                        isSelected = beautyConfig.makeUp?.path == "makeup/diadiatu.bundle",
                        value = if (beautyConfig.makeUp?.path == "makeup/diadiatu.bundle") (beautyConfig.makeUp?.intensity
                            ?: 0.6f) * 100 else 60f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = FaceUnityBeautySDK.MakeUpItem(
                                context,
                                "makeup/diadiatu.bundle",
                                value / 100
                            )
                        },
                        valueRange = 0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_hunxue,
                        R.mipmap.show_beauty_ic_effect_fu_hunxue,
                        withPadding = false,
                        isSelected = beautyConfig.makeUp?.path == "makeup/hunxue.bundle",
                        value = if (beautyConfig.makeUp?.path == "makeup/hunxue.bundle") (beautyConfig.makeUp?.intensity
                            ?: 0.6f) * 100 else 60f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = FaceUnityBeautySDK.MakeUpItem(
                                context,
                                "makeup/hunxue.bundle",
                                value / 100
                            )
                        },
                        valueRange = 0f..100f
                    )
                )
            ),
//            PageInfo(
//                R.string.show_beauty_group_adjust,
//                listOf(
//                    ItemInfo(
//                        R.string.show_beauty_item_none,
//                        R.mipmap.show_beauty_ic_none,
//                        0.0f,
//                        isSelected = true,
//                        onValueChanged = { _ ->
////                            beautyConfig.sharpen = 0.0f
//                        },
//                    ),
//                )
//            ),
            PageInfo(
                R.string.show_beauty_group_sticker,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.sticker == null,
                        onValueChanged = { _ ->
                            beautyConfig.sticker = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_sticker_sdlu,
                        R.mipmap.show_beauty_ic_sticker_elk,
                        isSelected = beautyConfig.sticker?.path == "sticker/sdlu.bundle",
                        onValueChanged = { _ ->
                            beautyConfig.sticker = FaceUnityBeautySDK.StickerItem(context, "sticker/sdlu.bundle")
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
        if (itemInfo.name == R.string.show_beauty_item_none
            || itemInfo.name == R.string.show_beauty_item_beauty_reset
            || pageInfo.name == R.string.show_beauty_group_sticker
        ) {
            viewBinding.slider.visibility = View.INVISIBLE
            // TODO:  hide Beauty switch
//            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_effect
            || pageInfo.name == R.string.show_beauty_group_adjust
        ) {
            viewBinding.slider.visibility = View.VISIBLE
            // TODO:  hide Beauty switch
//            viewBinding.ivCompare.isVisible = true
        }
    }
}
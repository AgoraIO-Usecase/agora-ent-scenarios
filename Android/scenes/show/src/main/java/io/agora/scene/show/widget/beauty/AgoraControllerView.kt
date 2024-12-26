package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.beauty.AgoraBeautySDK
import kotlin.math.roundToInt

val Int.castFromPositive100: Float
    get() = 0.01f * this


val Float.castToPositive100: Int
    get() = (100 * this).roundToInt()

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
                        onValueChanged = { _ ->
                            beautyConfig.smooth = 0.0f
                            beautyConfig.whiten = 0.0f
                            beautyConfig.redden = 0.0f
                            beautyConfig.basicBeauty = false
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_smooth,
                        R.mipmap.show_beauty_ic_smooth,
                        beautyConfig.smooth,
                        isSelected = true,
                        onValueChanged = { value ->
                            beautyConfig.smooth = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_whiten,
                        R.mipmap.show_beauty_ic_face_meibai,
                        beautyConfig.whiten,
                        onValueChanged = { value ->
                            beautyConfig.whiten = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_redden,
                        R.mipmap.show_beauty_ic_face_redden,
                        beautyConfig.redden,
                        onValueChanged = { value ->
                            beautyConfig.redden = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_face_shape,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        onValueChanged = { _ ->
                            beautyConfig.faceShape = false
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_eye,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.enlargeEye.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_chin,
                        R.mipmap.show_beauty_ic_face_xiaba,
                        beautyConfig.chinLength.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.chinLength = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_overall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.thinFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.thinFace = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_cheekbone,
                        R.mipmap.show_beauty_ic_face_shouquangu,
                        beautyConfig.shrinkCheekbone.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheekbone = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_long_nose,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.longNose.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.longNose = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_nose,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.narrowNose.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_mouth,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSize.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
//                    ItemInfo(
//                        R.string.show_beauty_item_beauty_xiahexian,
//                        R.mipmap.show_beauty_ic_face_xiahegu,
//                        beautyConfig.mouthSize.toFloat(),
//                        onValueChanged = { value ->
//                            // TODO:
//                        }
//                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_jawbone,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.shrinkJawbone.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.shrinkJawbone = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_fajixian,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.hairlineHeight.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = value.toInt()
                        },
                        valueRange = -100f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_gentleman_face,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.gentlemanFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.gentlemanFace = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_lady_face,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.ladyFace.toFloat(),
                        onValueChanged = { value ->
                            beautyConfig.ladyFace = value.toInt()
                        },
                        valueRange =  0f..100f
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_effect,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.makeupType == 0,
                        onValueChanged = { _ ->
                            beautyConfig.makeupType = 0
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_hunxue,
                        R.mipmap.show_beauty_ic_effect_fu_hunxue,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 1,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 1
                            beautyConfig.makeupStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_oumei,
                        R.mipmap.show_beauty_ic_effect_oumei,
                        withPadding = false,
                        isSelected = beautyConfig.makeupType == 2,
                        value = beautyConfig.makeupStrength,
                        onValueChanged = { value ->
                            beautyConfig.makeupType = 2
                            beautyConfig.makeupStrength = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_filter,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.None,
                        onValueChanged = { _ ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.None
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_original,
                        R.mipmap.show_beauty_ic_face_etou,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.YuanSheng,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.YuanSheng
                            beautyConfig.filterStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_whitening,
                        R.mipmap.show_beauty_ic_face_etou,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.NenBai,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.NenBai
                            beautyConfig.filterStrength = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_filter_cool_white,
                        R.mipmap.show_beauty_ic_face_etou,
                        isSelected = beautyConfig.filterType == AgoraBeautySDK.FilterStyle.LengBai,
                        value = beautyConfig.filterStrength,
                        onValueChanged = { value ->
                            beautyConfig.filterType = AgoraBeautySDK.FilterStyle.LengBai
                            beautyConfig.filterStrength = value
                        }
                    ),
                )
            ),
            PageInfo(
                R.string.show_beauty_group_adjust,
                listOf(
                    ItemInfo(
                        R.string.show_beauty_item_none,
                        R.mipmap.show_beauty_ic_none,
                        0.0f,
                        isSelected = true,
                        onValueChanged = { _ ->
                            beautyConfig.sharpen = 0.0f
                        },
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_adjust_sharpen,
                        R.mipmap.show_beauty_ic_adjust_sharp,
                        beautyConfig.sharpen,
                        onValueChanged = { value ->
                            beautyConfig.sharpen = value
                        }
                    ),
                )
            )
        )
    }

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.name == R.string.show_beauty_item_none) {
            viewBinding.slider.visibility = View.INVISIBLE
            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_face_shape
            || pageInfo.name == R.string.show_beauty_group_effect
            || pageInfo.name == R.string.show_beauty_group_filter
            || pageInfo.name == R.string.show_beauty_group_adjust
        ) {
            viewBinding.slider.visibility = View.VISIBLE
            viewBinding.ivCompare.isVisible = true
        }
    }

}
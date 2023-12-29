package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.beauty.SenseTimeBeautySDK

class SenseTimeControllerView : BaseControllerView {


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPageListCreate() = listOf(
        PageInfo(
            R.string.show_beauty_group_beauty,
            listOf(
                ItemInfo(
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none,
                    onValueChanged = { _ ->
                        SenseTimeBeautySDK.beautyConfig.smooth = 0.0f
                        SenseTimeBeautySDK.beautyConfig.whiten = 0.0f
                        SenseTimeBeautySDK.beautyConfig.thinFace = 0.0f
                        SenseTimeBeautySDK.beautyConfig.enlargeEye = 0.0f
                        SenseTimeBeautySDK.beautyConfig.redden = 0.0f
                        SenseTimeBeautySDK.beautyConfig.shrinkCheekbone = 0.0f
                        SenseTimeBeautySDK.beautyConfig.shrinkJawbone = 0.0f
                        SenseTimeBeautySDK.beautyConfig.whiteTeeth = 0.0f
                        SenseTimeBeautySDK.beautyConfig.hairlineHeight = 0.0f
                        SenseTimeBeautySDK.beautyConfig.narrowNose = 0.0f
                        SenseTimeBeautySDK.beautyConfig.mouthSize = 0.0f
                        SenseTimeBeautySDK.beautyConfig.chinLength = 0.0f
                        SenseTimeBeautySDK.beautyConfig.brightEye = 0.0f
                        SenseTimeBeautySDK.beautyConfig.darkCircles = 0.0f
                        SenseTimeBeautySDK.beautyConfig.nasolabialFolds = 0.0f
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_face_mopi,
                    SenseTimeBeautySDK.beautyConfig.smooth,
                    isSelected = true,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.smooth = value
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_beauty_whiten,
                    R.mipmap.show_beauty_ic_face_meibai,
                    SenseTimeBeautySDK.beautyConfig.whiten,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.whiten = value
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_beauty_overall,
                    R.mipmap.show_beauty_ic_face_shoulian,
                    SenseTimeBeautySDK.beautyConfig.thinFace,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.thinFace = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_cheekbone,
                    R.mipmap.show_beauty_ic_face_shouquangu,
                    SenseTimeBeautySDK.beautyConfig.shrinkCheekbone,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.shrinkCheekbone = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_eye,
                    R.mipmap.show_beauty_ic_face_eye,
                    SenseTimeBeautySDK.beautyConfig.enlargeEye,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.enlargeEye = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_nose,
                    R.mipmap.show_beauty_ic_face_shoubi,
                    SenseTimeBeautySDK.beautyConfig.narrowNose,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.narrowNose = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_chin,
                    R.mipmap.show_beauty_ic_face_xiaba,
                    SenseTimeBeautySDK.beautyConfig.chinLength,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.chinLength = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_jawbone,
                    R.mipmap.show_beauty_ic_face_xiahegu,
                    SenseTimeBeautySDK.beautyConfig.shrinkJawbone,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.shrinkJawbone = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_forehead,
                    R.mipmap.show_beauty_ic_face_etou,
                    SenseTimeBeautySDK.beautyConfig.hairlineHeight,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.hairlineHeight = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_mouth,
                    R.mipmap.show_beauty_ic_face_zuixing,
                    SenseTimeBeautySDK.beautyConfig.mouthSize,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.mouthSize = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_teeth,
                    R.mipmap.show_beauty_ic_face_meiya,
                    SenseTimeBeautySDK.beautyConfig.whiteTeeth,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.whiteTeeth = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_bright_eye,
                    R.mipmap.show_beauty_ic_face_bright_eye,
                    SenseTimeBeautySDK.beautyConfig.brightEye,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.brightEye = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_remove_nasolabial_folds,
                    R.mipmap.show_beauty_ic_face_remove_nasolabial_folds,
                    SenseTimeBeautySDK.beautyConfig.nasolabialFolds,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.nasolabialFolds = value
                    }
                ), ItemInfo(
                    R.string.show_beauty_item_beauty_remove_dark_circles,
                    R.mipmap.show_beauty_ic_face_remove_dark_circles,
                    SenseTimeBeautySDK.beautyConfig.darkCircles,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.darkCircles = value
                    }
                )
            )
        ),
        PageInfo(
            R.string.show_beauty_group_effect,
            listOf(
                ItemInfo(
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none,
                    isSelected = SenseTimeBeautySDK.beautyConfig.makeUp == null,
                    onValueChanged = { _ ->
                        SenseTimeBeautySDK.beautyConfig.makeUp = null
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_effect_oumei,
                    R.mipmap.show_beauty_ic_effect_oumei,
                    withPadding = false,
                    isSelected = SenseTimeBeautySDK.beautyConfig.makeUp?.path == "style_lightly/oumei.zip",
                    value = if (SenseTimeBeautySDK.beautyConfig.makeUp?.path == "style_lightly/oumei.zip")
                        SenseTimeBeautySDK.beautyConfig.makeUp?.strength
                            ?: 0.8f else 0.8f,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.makeUp = SenseTimeBeautySDK.MakeUpItem(
                            context,
                            "style_lightly/oumei.zip",
                            value
                        )
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_effect_hunxue,
                    R.mipmap.show_beauty_ic_effect_hunxue,
                    withPadding = false,
                    isSelected = SenseTimeBeautySDK.beautyConfig.makeUp?.path == "style_lightly/hunxue.zip",
                    value = if (SenseTimeBeautySDK.beautyConfig.makeUp?.path == "style_lightly/hunxue.zip")
                        SenseTimeBeautySDK.beautyConfig.makeUp?.strength
                            ?: 0.8f else 0.8f,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.makeUp = SenseTimeBeautySDK.MakeUpItem(
                            context,
                            "style_lightly/hunxue.zip",
                            value
                        )
                    }
                )
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
                        SenseTimeBeautySDK.beautyConfig.sharpen = 0.0f
                        SenseTimeBeautySDK.beautyConfig.clear = 0.0f
                        SenseTimeBeautySDK.beautyConfig.saturation = 0.0f
                        SenseTimeBeautySDK.beautyConfig.contrast = 0.0f
                    },
                ),
                ItemInfo(
                    R.string.show_beauty_item_adjust_contrast,
                    R.mipmap.show_beauty_ic_adjust_contrast,
                    SenseTimeBeautySDK.beautyConfig.contrast,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.contrast = value
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_adjust_saturation,
                    R.mipmap.show_beauty_ic_adjust_saturation,
                    SenseTimeBeautySDK.beautyConfig.saturation,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.saturation = value
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_adjust_sharpen,
                    R.mipmap.show_beauty_ic_adjust_sharp,
                    SenseTimeBeautySDK.beautyConfig.sharpen,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.sharpen = value
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_adjust_clarity,
                    R.mipmap.show_beauty_ic_adjust_clear,
                    SenseTimeBeautySDK.beautyConfig.clear,
                    onValueChanged = { value ->
                        SenseTimeBeautySDK.beautyConfig.clear = value
                    }
                ),
            )
        ),
        PageInfo(
            R.string.show_beauty_group_sticker,
            listOf(
                ItemInfo(
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none,
                    isSelected = SenseTimeBeautySDK.beautyConfig.sticker == null,
                    onValueChanged = { _ ->
                        SenseTimeBeautySDK.beautyConfig.sticker = null
                    }
                ),
                ItemInfo(
                    R.string.show_beauty_item_sticker_shangbanle,
                    R.mipmap.show_beauty_ic_sticker_shangbanle,
                    withPadding = false,
                    isSelected = SenseTimeBeautySDK.beautyConfig.sticker?.path == "stick er_face_shape/ShangBanLe.zip",
                    onValueChanged = { _ ->
                        SenseTimeBeautySDK.beautyConfig.sticker = SenseTimeBeautySDK.StickerItem(
                            context,
                            "sticker_face_shape/ShangBanLe.zip"
                        )
                    }
                )
            )
        )
    )

    override fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        super.onSelectedChanged(pageIndex, itemIndex)
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]
        if (itemInfo.name == R.string.show_beauty_item_none
            || pageInfo.name == R.string.show_beauty_group_sticker
        ) {
            viewBinding.slider.visibility = View.INVISIBLE
            viewBinding.ivCompare.isVisible = false
        } else if (pageInfo.name == R.string.show_beauty_group_beauty
            || pageInfo.name == R.string.show_beauty_group_effect
            || pageInfo.name == R.string.show_beauty_group_adjust
        ) {
            viewBinding.slider.visibility = View.VISIBLE
            viewBinding.ivCompare.isVisible = true
        }
    }

}
package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.View
import io.agora.scene.show.R
import io.agora.scene.show.beauty.ByteDanceBeautySDK

class ByteDanceControllerView : BaseControllerView {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onPageListCreate(): List<PageInfo> {
        val beautyConfig = ByteDanceBeautySDK.beautyConfig
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
                            beautyConfig.thinFace = 0.0f
                            beautyConfig.enlargeEye = 0.0f
                            beautyConfig.redden = 0.0f
                            beautyConfig.shrinkCheekbone = 0.0f
                            beautyConfig.shrinkJawbone = 0.0f
                            beautyConfig.whiteTeeth = 0.0f
                            beautyConfig.hairlineHeight = 0.0f
                            beautyConfig.narrowNose = 0.0f
                            beautyConfig.mouthSize = 0.0f
                            beautyConfig.chinLength = 0.0f
                            beautyConfig.brightEye = 0.0f
                            beautyConfig.darkCircles = 0.0f
                            beautyConfig.nasolabialFolds = 0.0f
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_smooth,
                        R.mipmap.show_beauty_ic_smooth,
                        beautyConfig.smooth,
                        onValueChanged = { value ->
                            beautyConfig.smooth = value
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_beauty_whiten,
                        R.mipmap.show_beauty_ic_face_meibai,
                        beautyConfig.whiten,
                        isSelected = true,
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
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_overall,
                        R.mipmap.show_beauty_ic_face_shoulian,
                        beautyConfig.thinFace,
                        onValueChanged = { value ->
                            beautyConfig.thinFace = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_cheekbone,
                        R.mipmap.show_beauty_ic_face_shouquangu,
                        beautyConfig.shrinkCheekbone,
                        onValueChanged = { value ->
                            beautyConfig.shrinkCheekbone = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_eye,
                        R.mipmap.show_beauty_ic_face_eye,
                        beautyConfig.enlargeEye,
                        onValueChanged = { value ->
                            beautyConfig.enlargeEye = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_nose,
                        R.mipmap.show_beauty_ic_face_shoubi,
                        beautyConfig.narrowNose,
                        onValueChanged = { value ->
                            beautyConfig.narrowNose = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_chin,
                        R.mipmap.show_beauty_ic_face_xiaba,
                        beautyConfig.chinLength,
                        onValueChanged = { value ->
                            beautyConfig.chinLength = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_jawbone,
                        R.mipmap.show_beauty_ic_face_xiahegu,
                        beautyConfig.shrinkJawbone,
                        onValueChanged = { value ->
                            beautyConfig.shrinkJawbone = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_forehead,
                        R.mipmap.show_beauty_ic_face_etou,
                        beautyConfig.hairlineHeight,
                        onValueChanged = { value ->
                            beautyConfig.hairlineHeight = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_mouth,
                        R.mipmap.show_beauty_ic_face_zuixing,
                        beautyConfig.mouthSize,
                        onValueChanged = { value ->
                            beautyConfig.mouthSize = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_teeth,
                        R.mipmap.show_beauty_ic_face_meiya,
                        beautyConfig.whiteTeeth,
                        onValueChanged = { value ->
                            beautyConfig.whiteTeeth = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_bright_eye,
                        R.mipmap.show_beauty_ic_face_bright_eye,
                        beautyConfig.brightEye,
                        onValueChanged = { value ->
                            beautyConfig.brightEye = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_remove_dark_circles,
                        R.mipmap.show_beauty_ic_face_remove_dark_circles,
                        beautyConfig.darkCircles,
                        onValueChanged = { value ->
                            beautyConfig.darkCircles = value
                        }
                    ), ItemInfo(
                        R.string.show_beauty_item_beauty_remove_nasolabial_folds,
                        R.mipmap.show_beauty_ic_face_remove_nasolabial_folds,
                        beautyConfig.nasolabialFolds,
                        onValueChanged = { value ->
                            beautyConfig.nasolabialFolds = value
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
                        isSelected = beautyConfig.makeUp == null,
                        onValueChanged = { _ ->
                            beautyConfig.makeUp = null
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_cwei,
                        R.mipmap.show_beauty_ic_effect_cwei,
                        isSelected = beautyConfig.makeUp?.style == "cwei",
                        value = if (beautyConfig.makeUp?.style == "cwei") beautyConfig.makeUp?.identity
                            ?: 0.5f else 0.5f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = ByteDanceBeautySDK.MakeUpItem(
                                context,
                                "cwei",
                                value
                            )
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_effect_yuanqi,
                        R.mipmap.show_beauty_ic_effect_yuanqi,
                        isSelected = beautyConfig.makeUp?.style == "yuanqi",
                        value = if (beautyConfig.makeUp?.style == "yuanqi") beautyConfig.makeUp?.identity
                            ?: 0.5f else 0.5f,
                        onValueChanged = { value ->
                            beautyConfig.makeUp = ByteDanceBeautySDK.MakeUpItem(
                                context,
                                "yuanqi",
                                value
                            )
                        }
                    )
                )
            ),
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
                        R.string.show_beauty_item_sticker_huahua,
                        R.mipmap.show_beauty_ic_filter_naiyou,
                        isSelected = beautyConfig.sticker == "huahua",
                        onValueChanged = { _ ->
                            beautyConfig.sticker = "huahua"
                        }
                    ),
                    ItemInfo(
                        R.string.show_beauty_item_sticker_wochaotian,
                        R.mipmap.show_beauty_ic_filter_naiyou,
                        isSelected = beautyConfig.sticker == "wochaotian",
                        onValueChanged = { _ ->
                            beautyConfig.sticker = "wochaotian"
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
        if (itemInfo.name == R.string.show_beauty_item_none
            || pageInfo.name == R.string.show_beauty_group_sticker
        ) {
            viewBinding.slider.visibility = View.INVISIBLE
        } else {
            viewBinding.slider.visibility = View.VISIBLE
        }
    }
}
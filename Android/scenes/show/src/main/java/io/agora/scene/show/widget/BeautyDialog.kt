package io.agora.scene.show.widget

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.sensetime.effects.STRenderKit
import io.agora.beauty.sensetime.*
import io.agora.rtc2.video.SegmentationProperty
import io.agora.rtc2.video.VirtualBackgroundSource
import io.agora.scene.base.utils.FileUtils
import io.agora.scene.show.R
import io.agora.scene.show.RtcEngineInstance
import io.agora.scene.show.beauty.*
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogBottomBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogItemBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogPageBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogTopBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class BeautyDialog constructor(context: Context) : BottomDarkDialog(context) {

    private data class ItemInfo(val id: Int, @StringRes val name: Int, @DrawableRes val icon: Int)
    private data class GroupInfo(
        val id: Int,
        @StringRes val name: Int,
        val itemList: List<ItemInfo>,
        var selectedIndex: Int = itemList.indexOfFirst {
            it.id == BeautyCache.getLastOperationItemId(
                id
            )
        }
    )

    private val mGroupList = arrayListOf(
        GroupInfo(
            GROUP_ID_BEAUTY, R.string.show_beauty_group_beauty, arrayListOf(
                ItemInfo(
                    ITEM_ID_BEAUTY_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_SMOOTH,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_face_mopi
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_WHITEN,
                    R.string.show_beauty_item_beauty_whiten,
                    R.mipmap.show_beauty_ic_face_meibai
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_REDDEN,
                    R.string.show_beauty_item_beauty_redden,
                    R.mipmap.show_beauty_ic_face_redden
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_OVERALL,
                    R.string.show_beauty_item_beauty_overall,
                    R.mipmap.show_beauty_ic_face_shoulian
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_CHEEKBONE,
                    R.string.show_beauty_item_beauty_cheekbone,
                    R.mipmap.show_beauty_ic_face_shouquangu
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_EYE,
                    R.string.show_beauty_item_beauty_eye,
                    R.mipmap.show_beauty_ic_face_eye
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_NOSE,
                    R.string.show_beauty_item_beauty_nose,
                    R.mipmap.show_beauty_ic_face_shoubi
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_CHIN,
                    R.string.show_beauty_item_beauty_chin,
                    R.mipmap.show_beauty_ic_face_xiaba
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_JAWBONE,
                    R.string.show_beauty_item_beauty_jawbone,
                    R.mipmap.show_beauty_ic_face_xiahegu
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_FOREHEAD,
                    R.string.show_beauty_item_beauty_forehead,
                    R.mipmap.show_beauty_ic_face_etou
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_MOUTH,
                    R.string.show_beauty_item_beauty_mouth,
                    R.mipmap.show_beauty_ic_face_zuixing
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_TEETH,
                    R.string.show_beauty_item_beauty_teeth,
                    R.mipmap.show_beauty_ic_face_meiya
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_BRIGHT_EYE,
                    R.string.show_beauty_item_beauty_bright_eye,
                    R.mipmap.show_beauty_ic_face_bright_eye
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_REMOVE_DARK_CIRCLES,
                    R.string.show_beauty_item_beauty_remove_dark_circles,
                    R.mipmap.show_beauty_ic_face_remove_dark_circles
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_REMOVE_NASOLABIAL_FOLDS,
                    R.string.show_beauty_item_beauty_remove_nasolabial_folds,
                    R.mipmap.show_beauty_ic_face_remove_nasolabial_folds
                )
            )
        ),
        GroupInfo(
            GROUP_ID_ADJUST, R.string.show_beauty_group_adjust, arrayListOf(
                ItemInfo(
                    ITEM_ID_ADJUST_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_ADJUST_CONTRAST,
                    R.string.show_beauty_item_adjust_contrast,
                    R.mipmap.show_beauty_ic_adjust_contrast
                ),
                ItemInfo(
                    ITEM_ID_ADJUST_SATURATION,
                    R.string.show_beauty_item_adjust_saturation,
                    R.mipmap.show_beauty_ic_adjust_saturation
                ),
                ItemInfo(
                    ITEM_ID_ADJUST_SHARPEN,
                    R.string.show_beauty_item_adjust_sharpen,
                    R.mipmap.show_beauty_ic_adjust_sharp
                ),
                ItemInfo(
                    ITEM_ID_ADJUST_CLARITY,
                    R.string.show_beauty_item_adjust_clarity,
                    R.mipmap.show_beauty_ic_adjust_clear
                ),
            )
        ),
        GroupInfo(
            GROUP_ID_EFFECT, R.string.show_beauty_group_effect, arrayListOf(
                ItemInfo(
                    ITEM_ID_EFFECT_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_EFFECT_CWEI,
                    R.string.show_beauty_item_effect_cwei,
                    R.mipmap.show_beauty_ic_effect_cwei
                ),
                ItemInfo(
                    ITEM_ID_EFFECT_YUANQI,
                    R.string.show_beauty_item_effect_yuanqi,
                    R.mipmap.show_beauty_ic_effect_yuanqi
                ),
            )
        ),
        GroupInfo(
            GROUP_ID_STICKER, R.string.show_beauty_group_sticker, arrayListOf(
                ItemInfo(
                    ITEM_ID_STICKER_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_STICKER_HUAHUA,
                    R.string.show_beauty_item_sticker_huahua,
                    R.mipmap.show_beauty_ic_filter_naiyou
                ),
            )
        ),
        GroupInfo(
            GROUP_ID_VIRTUAL_BG, R.string.show_beauty_group_virtual_bg, arrayListOf(
                ItemInfo(
                    ITEM_ID_VIRTUAL_BG_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_VIRTUAL_BG_BLUR,
                    R.string.show_beauty_item_virtual_bg_blur,
                    R.mipmap.show_beauty_ic_virtual_bg_blur
                ),
                ItemInfo(
                    ITEM_ID_VIRTUAL_BG_MITAO,
                    R.string.show_beauty_item_virtual_bg_mitao,
                    R.mipmap.show_beauty_ic_virtual_bg_mitao
                ),
            ),
            when (RtcEngineInstance.virtualBackgroundSource.backgroundSourceType) {
                VirtualBackgroundSource.BACKGROUND_BLUR -> 1
                VirtualBackgroundSource.BACKGROUND_IMG -> 2
                else -> 0
            }
        )
    )

    private val mTopBinding by lazy {
        ShowWidgetBeautyDialogTopBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }
    private val mBottomBinding by lazy {
        ShowWidgetBeautyDialogBottomBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private var beautyProcessor: IBeautyProcessor? = null

    init {
        setTopView(mTopBinding.root)
        setBottomView(mBottomBinding.root)

        mBottomBinding.tabLayout.apply {
            mGroupList.forEach {
                addTab(newTab().setText(it.name))
            }
        }

        val groupAdapter =
            object : BindingSingleAdapter<GroupInfo, ShowWidgetBeautyDialogPageBinding>() {

                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): BindingViewHolder<ShowWidgetBeautyDialogPageBinding> {
                    val viewHolder = super.onCreateViewHolder(parent, viewType)
                    viewHolder.itemView.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    return viewHolder
                }

                override fun onBindViewHolder(
                    holder: BindingViewHolder<ShowWidgetBeautyDialogPageBinding>,
                    tabPosition: Int
                ) {
                    val groupItem = getItem(tabPosition) ?: return
                    val groupPosition = tabPosition

                    val itemAdapter = object :
                        BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>() {

                        override fun onBindViewHolder(
                            holder: BindingViewHolder<ShowWidgetBeautyDialogItemBinding>,
                            position: Int
                        ) {
                            val itemInfo = getItem(position) ?: return

                            holder.binding.ivIcon.isActivated = position == groupItem.selectedIndex
                            holder.binding.ivIcon.setImageResource(itemInfo.icon)
                            if (groupItem.selectedIndex == position && mBottomBinding.tabLayout.selectedTabPosition == groupPosition) {
                                refreshTopLayout(groupItem.id, itemInfo.id)
                            }
                            holder.binding.ivIcon.setOnClickListener {
                                if (position == groupItem.selectedIndex) {
                                    return@setOnClickListener
                                }
                                val activate = !it.isActivated
                                it.isActivated = activate

                                val oSelectedIndex = groupItem.selectedIndex
                                groupItem.selectedIndex = position
                                notifyItemChanged(oSelectedIndex)
                                notifyItemChanged(groupItem.selectedIndex)

                                onItemSelected(groupItem.id, itemInfo.id)
                            }
                            holder.binding.tvName.setText(itemInfo.name)
                        }
                    }
                    itemAdapter.resetAll(groupItem.itemList)
                    holder.binding.recycleView.adapter = itemAdapter
                }
            }
        mBottomBinding.viewPager.isUserInputEnabled = false
        mBottomBinding.viewPager.offscreenPageLimit = 1
        mBottomBinding.viewPager.adapter = groupAdapter
        groupAdapter.resetAll(mGroupList)

        mBottomBinding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                mGroupList[mBottomBinding.tabLayout.selectedTabPosition].apply {
                    onItemSelected(id, itemList[selectedIndex].id)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        TabLayoutMediator(
            mBottomBinding.tabLayout,
            mBottomBinding.viewPager
        ) { tab, position ->
            tab.text = context.getString(mGroupList[position].name)
        }.attach()

        mTopBinding.root.isVisible = false
        mTopBinding.ivCompare.setOnClickListener {
            beautyProcessor?.apply {
                setBeautyEnable(!isBeautyEnable())
            }
        }
    }

    // 修改绿幕开关
    private fun changeGreenScreenSwitch(isChecked: Boolean) {
        beautyProcessor?.setGreenScreen(isChecked)
        mGroupList[mBottomBinding.tabLayout.selectedTabPosition].apply {
            onItemSelected(GROUP_ID_VIRTUAL_BG, itemList[selectedIndex].id)
        }
    }

    override fun onStop() {
        super.onStop()
    }

    fun setBeautyProcessor(processor: IBeautyProcessor) {
        this.beautyProcessor = processor
    }

    private fun refreshTopLayout(groupId: Int, itemId: Int) {
        mTopBinding.slider.clearOnChangeListeners()
        mTopBinding.slider.clearOnSliderTouchListeners()
        mTopBinding.mSwitchMaterial.setOnCheckedChangeListener(null)
        Log.e("liu0208", "refreshTopLayout    groupId = $groupId    itemId = $itemId")

        mTopBinding.ivCompare.visibility = if (groupId != GROUP_ID_VIRTUAL_BG) View.VISIBLE else View.GONE
        mTopBinding.llGreenScreen.visibility = if (groupId == GROUP_ID_VIRTUAL_BG) View.VISIBLE else View.GONE
        mTopBinding.tvStrength.visibility = if (groupId == GROUP_ID_VIRTUAL_BG) View.VISIBLE else View.GONE

        when (groupId) {
            // 虚拟背景
            GROUP_ID_VIRTUAL_BG -> {
                mTopBinding.root.isVisible = true
                mTopBinding.slider.value = beautyProcessor?.let { it.getGreenScreenStrength() } ?: 0.5f
                mTopBinding.mSwitchMaterial.isChecked = beautyProcessor?.let { it.greenScreen() } ?: false
                mTopBinding.mSwitchMaterial.setOnCheckedChangeListener { _, isChecked ->
                    Log.e("liu0208", "OnCheckedChange    $isChecked")
                    if (isChecked) {
                        AlertDialog.Builder(context, R.style.show_alert_dialog).apply {
                            setCancelable(false)
                            setTitle(R.string.show_tip)
                            setMessage(R.string.show_beauty_green_screen_tip)
                            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                                changeGreenScreenSwitch(true)
                                dialog.dismiss()
                            }
                            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                                mTopBinding.mSwitchMaterial.isChecked = false
                                dialog.dismiss()
                            }
                        }.create().show()
                        return@setOnCheckedChangeListener
                    }
                    changeGreenScreenSwitch(isChecked);
                }
                mTopBinding.slider.addOnChangeListener { slider, sValure, fromUser ->
                    beautyProcessor?.setBg(sValure)
                    mGroupList[mBottomBinding.tabLayout.selectedTabPosition].apply {
                        onItemSelected(GROUP_ID_VIRTUAL_BG, itemList[selectedIndex].id)
                    }
                }
            }
            // 美颜
            GROUP_ID_BEAUTY -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_BEAUTY_NONE
                if (itemId != ITEM_ID_BEAUTY_NONE) {
                    mTopBinding.slider.value = BeautyCache.getItemValueWithDefault(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, sValure, fromUser ->
                        beautyProcessor?.setFaceBeautify(itemId, sValure)
                    }
                }
            }
            // 滤镜
            GROUP_ID_FILTER -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_FILTER_NONE
                if (itemId != ITEM_ID_FILTER_NONE) {
                    mTopBinding.slider.value = BeautyCache.getItemValueWithDefault(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, value, fromUser ->
                        beautyProcessor?.setFilter(itemId, value)
                    }
                }
            }
            // 特效
            GROUP_ID_EFFECT -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_EFFECT_NONE
                if (itemId != ITEM_ID_EFFECT_NONE) {
                    mTopBinding.slider.value = BeautyCache.getItemValueWithDefault(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, value, fromUser ->
                        beautyProcessor?.setEffect(itemId, value)
                    }
                }
            }
            // 调整
            GROUP_ID_ADJUST -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_ADJUST_NONE
                if (itemId != ITEM_ID_ADJUST_NONE) {
                    mTopBinding.slider.value = BeautyCache.getItemValueWithDefault(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, value, fromUser ->
                        beautyProcessor?.setAdjust(itemId, value)
                    }
                }
            }
            // 贴纸
            GROUP_ID_STICKER -> {
                mTopBinding.root.isVisible = false
            }
        }
    }


    private fun onItemSelected(groupId: Int, itemId: Int) {
        refreshTopLayout(groupId, itemId)
        val greenScreenStrength = beautyProcessor?.let { it.getGreenScreenStrength() } ?: 0.5f
        val greenScreen = beautyProcessor?.let { it.greenScreen() } ?: false
        Log.e("liu0208", "greenScreenStrength = $greenScreenStrength")
        when (groupId) {
            GROUP_ID_VIRTUAL_BG -> {
                when (itemId) {
                    // 无
                    ITEM_ID_VIRTUAL_BG_NONE -> {
                        RtcEngineInstance.rtcEngine.enableVirtualBackground(
                            false,
                            RtcEngineInstance.virtualBackgroundSource.apply { backgroundSourceType = VirtualBackgroundSource.BACKGROUND_COLOR },
                            SegmentationProperty(if (greenScreen) SegmentationProperty.SEG_MODEL_GREEN else SegmentationProperty.SEG_MODEL_AI, greenScreenStrength)
                        )
                    }
                    // 模糊
                    ITEM_ID_VIRTUAL_BG_BLUR -> {
                        RtcEngineInstance.rtcEngine.enableVirtualBackground(
                            true,
                            RtcEngineInstance.virtualBackgroundSource.apply { backgroundSourceType = VirtualBackgroundSource.BACKGROUND_BLUR },
                            SegmentationProperty(if (greenScreen) SegmentationProperty.SEG_MODEL_GREEN else SegmentationProperty.SEG_MODEL_AI, greenScreenStrength)
                        )
                    }
                    // 蜜桃
                    ITEM_ID_VIRTUAL_BG_MITAO -> {
                        RtcEngineInstance.rtcEngine.enableVirtualBackground(
                            true,
                            RtcEngineInstance.virtualBackgroundSource.apply {
                                backgroundSourceType = VirtualBackgroundSource.BACKGROUND_IMG
                                source = FileUtils.copyFileFromAssets(context, "virtualbackgroud_mitao.jpg", context.externalCacheDir!!.absolutePath)
                            },
                            SegmentationProperty(if (greenScreen) SegmentationProperty.SEG_MODEL_GREEN else SegmentationProperty.SEG_MODEL_AI, greenScreenStrength)
                        )
                    }
                }
            }
            GROUP_ID_BEAUTY -> {
                if (itemId == ITEM_ID_BEAUTY_NONE) {
                    beautyProcessor?.setFaceBeautify(itemId, 0.0f)
                } else {
                    beautyProcessor?.setFaceBeautify(itemId, mTopBinding.slider.value)
                }
            }
            GROUP_ID_FILTER -> {
                if (itemId == ITEM_ID_FILTER_NONE) {
                    beautyProcessor?.setFilter(itemId, 0.0f)
                } else {
                    beautyProcessor?.setFilter(itemId, mTopBinding.slider.value)
                }

            }
            GROUP_ID_EFFECT -> {
                if (itemId == ITEM_ID_EFFECT_NONE) {
                    beautyProcessor?.setEffect(itemId, 0.0f)
                } else {
                    beautyProcessor?.setEffect(itemId, mTopBinding.slider.value)
                }
            }
            GROUP_ID_ADJUST -> {
                if (itemId == ITEM_ID_ADJUST_NONE) {
                    beautyProcessor?.setAdjust(itemId, 0.0f)
                } else {
                    beautyProcessor?.setAdjust(itemId, mTopBinding.slider.value)
                }

            }
            GROUP_ID_STICKER -> {
                beautyProcessor?.setSticker(itemId)
            }
        }
    }

}
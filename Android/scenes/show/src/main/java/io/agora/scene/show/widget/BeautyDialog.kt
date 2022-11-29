package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.show.R
import io.agora.scene.show.beauty.*
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogBottomBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogItemBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogPageBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogTopBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class BeautyDialog(context: Context) : BottomDarkDialog(context) {

    private data class ItemInfo(val id: Int, @StringRes val name: Int, @DrawableRes val icon: Int)
    private data class GroupInfo(
        val id: Int,
        @StringRes val name: Int,
        val itemList: List<ItemInfo>,
        var selectedIndex: Int = itemList.indexOfFirst { it.id == BeautyCache.getLastOperationItemId(id) }
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
                )
            )
        ),
        GroupInfo(
            GROUP_ID_FILTER, R.string.show_beauty_group_filter, arrayListOf(
                ItemInfo(
                    ITEM_ID_FILTER_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_FILTER_CREAM,
                    R.string.show_beauty_item_filter_cream,
                    R.mipmap.show_beauty_ic_filter_naiyou
                ),
                ItemInfo(
                    ITEM_ID_FILTER_MAKALONG,
                    R.string.show_beauty_item_filter_mokalong,
                    R.mipmap.show_beauty_ic_filter_makalong
                ),
                ItemInfo(
                    ITEM_ID_FILTER_OXGEN,
                    R.string.show_beauty_item_filter_oxgen,
                    R.mipmap.show_beauty_ic_filter_yangqi
                ),
                ItemInfo(
                    ITEM_ID_FILTER_WUYU,
                    R.string.show_beauty_item_filter_wuyu,
                    R.mipmap.show_beauty_ic_filter_wuyu
                ),
                ItemInfo(
                    ITEM_ID_FILTER_Po9,
                    R.string.show_beauty_item_filter_po9,
                    R.mipmap.show_beauty_ic_filter_haibian
                ),
                ItemInfo(
                    ITEM_ID_FILTER_LOLITA,
                    R.string.show_beauty_item_filter_lolita,
                    R.mipmap.show_beauty_ic_filter_luolita
                ),
                ItemInfo(
                    ITEM_ID_FILTER_MITAO,
                    R.string.show_beauty_item_filter_mitao,
                    R.mipmap.show_beauty_ic_filter_mitao
                ),
                ItemInfo(
                    ITEM_ID_FILTER_YINHUA,
                    R.string.show_beauty_item_filter_yinhua,
                    R.mipmap.show_beauty_ic_filter_yinghua
                ),
                ItemInfo(
                    ITEM_ID_FILTER_BEIHAIDAO,
                    R.string.show_beauty_item_filter_beihaidao,
                    R.mipmap.show_beauty_ic_filter_beihaidao
                ),
                ItemInfo(
                    ITEM_ID_FILTER_S3,
                    R.string.show_beauty_item_filter_s3,
                    R.mipmap.show_beauty_ic_filter_lvtu
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
                    ITEM_ID_EFFECT_BAIXI,
                    R.string.show_beauty_item_effect_baixi,
                    R.mipmap.show_beauty_ic_effect_baixi
                ),
                ItemInfo(
                    ITEM_ID_EFFECT_TIANMEI,
                    R.string.show_beauty_item_effect_tianmei,
                    R.mipmap.show_beauty_ic_effect_tianmei
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
                    ITEM_ID_STICKER_BITI,
                    R.string.show_beauty_item_sticker_biti,
                    R.mipmap.show_beauty_ic_filter_naiyou
                ),
                ItemInfo(
                    ITEM_ID_STICKER_ZHOUNIAN,
                    R.string.show_beauty_item_sticker_zhounian,
                    R.mipmap.show_beauty_ic_filter_makalong
                ),
            )
        ),
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
                    position: Int
                ) {
                    val groupItem = getItem(position) ?: return

                    val itemAdapter = object :
                        BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>() {

                        override fun onBindViewHolder(
                            holder: BindingViewHolder<ShowWidgetBeautyDialogItemBinding>,
                            position: Int
                        ) {
                            val itemInfo = getItem(position) ?: return

                            holder.binding.ivIcon.isActivated = position == groupItem.selectedIndex
                            holder.binding.ivIcon.setImageResource(itemInfo.icon)
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
                setEnable(!isEnable())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        beautyProcessor?.setEnable(true)
    }

    fun setBeautyProcessor(processor: IBeautyProcessor) {
        this.beautyProcessor = processor
    }

    private fun onItemSelected(groupId: Int, itemId: Int) {

        when (groupId) {
            GROUP_ID_BEAUTY -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_BEAUTY_NONE
                mTopBinding.slider.clearOnChangeListeners()
                if (itemId == ITEM_ID_BEAUTY_NONE) {
                    beautyProcessor?.setFaceBeautify(itemId, 0.0f)
                } else {
                    mTopBinding.slider.value = BeautyCache.getItemValue(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, sValure, fromUser ->
                        beautyProcessor?.setFaceBeautify(itemId, sValure)
                    }
                }

            }
            GROUP_ID_FILTER -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_FILTER_NONE
                mTopBinding.slider.clearOnChangeListeners()
                if (itemId == ITEM_ID_FILTER_NONE) {
                    beautyProcessor?.setFilter(itemId, 0.0f)
                } else {
                    mTopBinding.slider.value = BeautyCache.getItemValue(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, value, fromUser ->
                        beautyProcessor?.setFilter(itemId, value)
                    }
                }

            }
            GROUP_ID_EFFECT -> {
                mTopBinding.root.isVisible = itemId != ITEM_ID_EFFECT_NONE
                mTopBinding.slider.clearOnChangeListeners()
                if (itemId == ITEM_ID_EFFECT_NONE) {
                    beautyProcessor?.setEffect(itemId, 0.0f)
                } else {
                    mTopBinding.slider.value = BeautyCache.getItemValue(itemId)
                    mTopBinding.slider.addOnChangeListener { slider, value, fromUser ->
                        beautyProcessor?.setEffect(itemId, value)
                    }
                }
            }
            GROUP_ID_STICKER -> {
                mTopBinding.root.isVisible = false
                beautyProcessor?.setSticker(itemId)
            }
        }
    }

}
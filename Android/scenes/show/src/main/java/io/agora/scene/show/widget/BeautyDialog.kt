package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogBottomBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogItemBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogPageBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogTopBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class BeautyDialog(context: Context) : BottomDarkDialog(context) {

    companion object {

        const val GROUP_ID_BEAUTY = 0x00000001 // 美颜
        const val ITEM_ID_BEAUTY_NONE = GROUP_ID_BEAUTY
        const val ITEM_ID_BEAUTY_SMOOTH = GROUP_ID_BEAUTY + 1 // 磨皮
        const val ITEM_ID_BEAUTY_WHITEN = GROUP_ID_BEAUTY + 2 // 美白
        const val ITEM_ID_BEAUTY_OVERALL = GROUP_ID_BEAUTY + 3 // 瘦脸
        const val ITEM_ID_BEAUTY_CHEEKBONE = GROUP_ID_BEAUTY + 4 // 瘦颧骨
        const val ITEM_ID_BEAUTY_JAWBONE = GROUP_ID_BEAUTY + 5 // 下颌骨
        const val ITEM_ID_BEAUTY_EYE = GROUP_ID_BEAUTY + 6 // 大眼
        const val ITEM_ID_BEAUTY_TEETH = GROUP_ID_BEAUTY + 7 // 美牙
        const val ITEM_ID_BEAUTY_FOREHEAD = GROUP_ID_BEAUTY + 8 // 额头
        const val ITEM_ID_BEAUTY_NOSE = GROUP_ID_BEAUTY + 9 // 瘦鼻
        const val ITEM_ID_BEAUTY_MOUTH = GROUP_ID_BEAUTY + 10 // 嘴形
        const val ITEM_ID_BEAUTY_CHIN = GROUP_ID_BEAUTY + 11 // 下巴


        const val GROUP_ID_STYLE = GROUP_ID_BEAUTY shl 4 // 风格
        const val ITEM_ID_STYLE_NONE = GROUP_ID_STYLE
        const val ITEM_ID_STYLE_BAIXI = GROUP_ID_STYLE + 1 //白皙
        const val ITEM_ID_STYLE_TIANMEI = GROUP_ID_STYLE + 2 // 甜美
        const val ITEM_ID_STYLE_CWEI = GROUP_ID_STYLE + 3 // C位
        const val ITEM_ID_STYLE_YUANQI = GROUP_ID_STYLE + 4 // 元气

        const val GROUP_ID_FILTER = GROUP_ID_STYLE shl 4 // 滤镜
        const val ITEM_ID_FILTER_NONE = GROUP_ID_FILTER
        const val ITEM_ID_FILTER_CREAM = GROUP_ID_FILTER + 1 // 奶油
        const val ITEM_ID_FILTER_MAKALONG = GROUP_ID_FILTER + 2 // 马卡龙
        const val ITEM_ID_FILTER_OXGEN = GROUP_ID_FILTER + 3 // 氧气
        const val ITEM_ID_FILTER_WUYU = GROUP_ID_FILTER + 4 // 物语
        const val ITEM_ID_FILTER_Po9 = GROUP_ID_FILTER + 5 // 海边人物
        const val ITEM_ID_FILTER_LOLITA = GROUP_ID_FILTER + 6 // 洛丽塔
        const val ITEM_ID_FILTER_MITAO = GROUP_ID_FILTER + 7 // 蜜桃
        const val ITEM_ID_FILTER_YINHUA = GROUP_ID_FILTER + 8 // 樱花
        const val ITEM_ID_FILTER_BEIHAIDAO = GROUP_ID_FILTER + 9 // 北海道
        const val ITEM_ID_FILTER_S3 = GROUP_ID_FILTER + 10 // 旅途

        const val GROUP_ID_TOOL = GROUP_ID_FILTER shl 4 // 道具
        const val ITEM_ID_TOOL_NONE = GROUP_ID_TOOL
        const val ITEM_ID_TOOL_DIANZIERDUO = GROUP_ID_TOOL + 1 // 电子耳朵
        const val ITEM_ID_TOOL_TUER = GROUP_ID_TOOL + 2 // 兔耳
    }

    private data class ItemInfo(val id: Int, @StringRes val name: Int, @DrawableRes val icon: Int)
    private data class GroupInfo(
        val id: Int,
        @StringRes val name: Int,
        val itemList: List<ItemInfo>,
        var selectedIndex: Int = 0
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
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_WHITEN,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_OVERALL,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_CHEEKBONE,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_JAWBONE,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_EYE,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_TEETH,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_FOREHEAD,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_NOSE,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_MOUTH,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_BEAUTY_CHIN,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                )
            )
        ),
        GroupInfo(
            GROUP_ID_STYLE, R.string.show_beauty_group_beauty, arrayListOf(
                ItemInfo(
                    ITEM_ID_STYLE_NONE,
                    R.string.show_beauty_item_none,
                    R.mipmap.show_beauty_ic_none
                ),
                ItemInfo(
                    ITEM_ID_STYLE_BAIXI,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                ),
                ItemInfo(
                    ITEM_ID_STYLE_TIANMEI,
                    R.string.show_beauty_item_beauty_smooth,
                    R.mipmap.show_beauty_ic_smooth
                )
            )
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

    init {
        setTopView(mTopBinding.root)
        setBottomView(mBottomBinding.root)

        mBottomBinding.tabLayout.apply {
            mGroupList.forEach {
                addTab(newTab().setText(it.name))
            }
        }

        val groupAdapter = object : BindingSingleAdapter<GroupInfo, ShowWidgetBeautyDialogPageBinding>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BindingViewHolder<ShowWidgetBeautyDialogPageBinding> {
                val viewHolder = super.onCreateViewHolder(parent, viewType)
                viewHolder.itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                return viewHolder
            }

            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetBeautyDialogPageBinding>,
                position: Int
            ) {
                val groupItem = getItem(position) ?: return

                val itemAdapter = object: BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>(){

                    override fun onBindViewHolder(
                        holder: BindingViewHolder<ShowWidgetBeautyDialogItemBinding>,
                        position: Int
                    ) {
                        val itemInfo = getItem(position) ?: return

                        holder.binding.ivIcon.isActivated = position == groupItem.selectedIndex
                        holder.binding.ivIcon.setImageResource(itemInfo.icon)
                        holder.binding.ivIcon.setOnClickListener {
                            if(position == groupItem.selectedIndex){
                                return@setOnClickListener
                            }
                            val activate = !it.isActivated
                            it.isActivated = activate

                            val oSelectedIndex = groupItem.selectedIndex
                            groupItem.selectedIndex = position
                            notifyItemChanged(oSelectedIndex)
                            notifyItemChanged(groupItem.selectedIndex)
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

        TabLayoutMediator(
            mBottomBinding.tabLayout,
            mBottomBinding.viewPager
        ) { tab, position ->
            tab.text = context.getString(mGroupList[position].name)
        }.attach()

    }

}
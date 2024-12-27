package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.StringRes
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.base.GlideApp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetBeautyBaseLayoutBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogItemBinding
import io.agora.scene.show.databinding.ShowWidgetBeautyDialogPageBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import java.time.temporal.ValueRange

open class BaseControllerView : FrameLayout {


    private val itemAdapterList =
        mutableListOf<BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>>()

    private val pageAdapter by lazy {
        object : BindingSingleAdapter<PageInfo, ShowWidgetBeautyDialogPageBinding>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BindingViewHolder<ShowWidgetBeautyDialogPageBinding> {
                val holder = super.onCreateViewHolder(parent, viewType)
                holder.itemView.layoutParams = LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
                )
                return holder
            }

            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetBeautyDialogPageBinding>,
                tabPosition: Int
            ) {
                val pageInfo = getItem(tabPosition) ?: return
                val itemAdapter = createItemAdapter(tabPosition)
                itemAdapter.resetAll(pageInfo.itemList)
                itemAdapterList.add(tabPosition, itemAdapter)

                (context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager)?.defaultDisplay?.width?.let {
                    holder.binding.recycleView.layoutParams =
                        holder.binding.recycleView.layoutParams.apply {
                            width = it
                        }
                }
                holder.binding.recycleView.adapter = itemAdapter
            }
        }
    }

    val viewBinding by lazy {
        ShowWidgetBeautyBaseLayoutBinding.inflate(LayoutInflater.from(context))
    }

    var pageList = listOf<PageInfo>()
        set(value) {
            field = value
            pageAdapter.resetAll(value)
            itemAdapterList.clear()
            viewBinding.tabLayout.removeAllTabs()
            value.forEach {
                val tab = viewBinding.tabLayout.newTab().setText(it.name)
                viewBinding.tabLayout.addTab(tab)
                if (it.isSelected) {
                    tab.select()
                }
            }
        }

    var beautyOpenClickListener: OnClickListener? = null
        set(value) {
            field = value
            viewBinding.ivCompare.setOnClickListener((value))
        }

    var beautyOpenIsActivated: Boolean? = null
        set(value) {
            field = value
            viewBinding.ivCompare.isActivated = value ?: false
        }

    var onSelectedChangeListener: ((pageIndex: Int, itemIndex: Int) -> Unit)? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        addView(viewBinding.root)
        viewBinding.viewPager.isUserInputEnabled = false
        pageList = onPageListCreate()
        viewBinding.viewPager.adapter = pageAdapter


        viewBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedPosition = viewBinding.tabLayout.selectedTabPosition
                pageList.forEachIndexed { index, pageInfo ->
                    pageInfo.isSelected = index == selectedPosition
                }
                var itemIndex = pageList[selectedPosition].itemList.indexOfFirst { it.isSelected }
                if (itemIndex < 0) {
                    itemIndex = 0
                }
                onSelectedChanged(
                    selectedPosition,
                    itemIndex
                )
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        TabLayoutMediator(
            viewBinding.tabLayout,
            viewBinding.viewPager
        ) { tab, position ->
            tab.text = context.getString(pageList[position].name)
        }.attach()
    }


    private fun createItemAdapter(pageIndex: Int) =
        object : BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>() {

            private var selectedHolder: BindingViewHolder<ShowWidgetBeautyDialogItemBinding>? = null

            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetBeautyDialogItemBinding>,
                position: Int
            ) {
                val itemInfo = getItem(position) ?: return
                holder.binding.ivIcon.setImageDrawable(null)

                holder.binding.ivIcon.isActivated = itemInfo.isSelected
                GlideApp.with(holder.binding.ivIcon)
                    .load(itemInfo.icon)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.binding.ivIcon)
                if (itemInfo.withPadding) {
                    val padding =
                        holder.binding.ivIcon.context.resources.getDimensionPixelSize(R.dimen.show_beauty_item_padding)
                    holder.binding.ivIcon.setPadding(padding, padding, padding, padding)
                } else {
                    val padding =
                        holder.binding.ivIcon.context.resources.getDimensionPixelSize(R.dimen.show_beauty_item_padding_background)
                    holder.binding.ivIcon.setPadding(padding, padding, padding, padding)
                }
                holder.binding.tvName.setText(itemInfo.name)
                if (itemInfo.isSelected) {
                    selectedHolder = holder
                    if (pageList[pageIndex].isSelected) {
                        onSelectedChanged(pageIndex, position)
                    }
                }
                holder.binding.ivIcon.setOnClickListener { _ ->
                    if (itemInfo.isSelected) {
                        return@setOnClickListener
                    }
                    val oSelectedItem = pageList[pageIndex].itemList.firstOrNull { it.isSelected }
                    oSelectedItem?.isSelected = false
                    itemInfo.isSelected = true
                    selectedHolder?.binding?.ivIcon?.isActivated = false
                    holder.binding.ivIcon.isActivated = true
                    selectedHolder = holder
                    onSelectedChanged(pageIndex, position)
                }
            }
        }

    protected open fun onPageListCreate() = listOf<PageInfo>()

    protected open fun onSelectedChanged(pageIndex: Int, itemIndex: Int) {
        val pageInfo = pageList[pageIndex]
        val itemInfo = pageInfo.itemList[itemIndex]

        itemInfo.onValueChanged.invoke(itemInfo.value)
        viewBinding.slider.clearOnChangeListeners()
        viewBinding.slider.clearOnSliderTouchListeners()
        viewBinding.slider.valueFrom = itemInfo.valueRange.start
        viewBinding.slider.valueTo = itemInfo.valueRange.endInclusive

        if (itemInfo.valueRange.endInclusive > 1) {
            viewBinding.slider.value = itemInfo.value.toInt().toFloat()
        } else {
            viewBinding.slider.value = itemInfo.value
        }
        // 设置自定义的整数格式化器
        viewBinding.slider.setLabelFormatter { value ->
            if (itemInfo.valueRange.endInclusive > 1) {
                value.toInt().toString() // 显示为整数
            } else {
                String.format("%.1f", value) // 显示一位小数
            }
        }
        viewBinding.slider.addOnChangeListener { _, value, _ ->
            if (itemInfo.valueRange.endInclusive > 1) {
                val intValue = value.toInt()
                itemInfo.value = intValue.toFloat()
                itemInfo.onValueChanged.invoke(intValue.toFloat())
            } else {
                itemInfo.value = value
                itemInfo.onValueChanged.invoke(value)
            }
        }
        onSelectedChangeListener?.invoke(pageIndex, itemIndex)
    }

    fun updateItemInfo(updater: (itemInfo: ItemInfo) -> Boolean) {
        pageList.forEach { pageInfo ->
            pageInfo.itemList.forEach { itemInfo ->
                if (updater(itemInfo)) {
                    itemInfo.onValueChanged.invoke(itemInfo.value)
                    return
                }
            }
        }
    }

    data class PageInfo(
        @StringRes val name: Int,
        val itemList: List<ItemInfo>,
        var isSelected: Boolean = false
    )

    data class ItemInfo constructor(
        @StringRes val name: Int,
        @DrawableRes val icon: Int,
        var value: Float = 0.0f,
        val onValueChanged: (value: Float) -> Unit,
        var isSelected: Boolean = false,
        var withPadding: Boolean = true,
        val valueRange: ClosedFloatingPointRange<Float> = 0.0f..1.0f
    )
}
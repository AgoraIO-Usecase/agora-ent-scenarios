package io.agora.scene.show.widget.beauty

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
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

open class BaseControllerView : FrameLayout {


    protected val itemAdapterList =
        mutableListOf<BindingSingleAdapter<ItemInfo, ShowWidgetBeautyDialogItemBinding>?>()

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
                // Ensure the list is large enough before adding at the specified position
                while (itemAdapterList.size <= tabPosition) {
                    itemAdapterList.add(null)
                }
                itemAdapterList[tabPosition] = itemAdapter

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

    // TODO:  hide Beauty switch
    var beautyOpenClickListener: OnClickListener? = null
        set(value) {
            field = value
//            viewBinding.ivCompare.setOnClickListener((value))
        }

    // TODO:  hide Beauty switch
    var beautyOpenIsActivated: Boolean? = null
        set(value) {
            field = value
//            viewBinding.ivCompare.isActivated = value ?: false
        }

    var beautyCompareListener: OnTouchListener?=null
        set(value) {
            field = value
            viewBinding.ivCompareBeauty.setOnTouchListener((value))
        }

    var onSelectedChangeListener: ((pageIndex: Int, itemIndex: Int) -> Unit)? = null

    var onResetClickListener: (() -> Unit)? = null

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


        // More accurately determine the value type
        // If the max value of the range is greater than 1 and the range span is an integer, treat it as integer type
        val isIntegerType = itemInfo.valueRange.endInclusive > 1.0f &&
                (itemInfo.valueRange.endInclusive - itemInfo.valueRange.start) % 1.0f == 0.0f &&
                itemInfo.valueRange.start % 1.0f == 0.0f

        // Set value (no listeners attached at this point, so no callback will be triggered)
        // Note: since the callback was already invoked earlier in onSelectedChanged, setting the value here won't cause a loop
        if (isIntegerType) {
            viewBinding.slider.value = itemInfo.value.toInt().toFloat()
        } else {
            viewBinding.slider.value = itemInfo.value
        }

        viewBinding.slider.setLabelFormatter { value ->
            if (itemInfo.valueRange.endInclusive > 1) {
                value.toInt().toString()
            } else {
                String.format("%.1f", value) 
            }
        }
        // Add listeners after setting the value to avoid triggering callbacks during initialization
        var lastUpdateTime = 0L
        val throttleInterval = 50L // Throttle interval: 50ms, balancing smoothness and performance

        viewBinding.slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= throttleInterval) {
                    lastUpdateTime = currentTime

                    // Convert to integer type if needed
                    val finalValue = if (isIntegerType) {
                        value.toInt().toFloat()
                    } else {
                        value
                    }
                    itemInfo.value = finalValue
                    itemInfo.onValueChanged?.invoke(finalValue)
                }
            }
        }
        viewBinding.slider.addOnSliderTouchListener(object : com.google.android.material.slider.Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) {
                // Reset throttle timer when touch starts
                lastUpdateTime = 0L
            }

            override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                // Ensure the final value callback is invoked when touch ends
                val finalValue = if (isIntegerType) {
                    slider.value.toInt().toFloat()
                } else {
                    slider.value
                }
                itemInfo.value = finalValue
                itemInfo.onValueChanged?.invoke(finalValue)
            }
        })
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
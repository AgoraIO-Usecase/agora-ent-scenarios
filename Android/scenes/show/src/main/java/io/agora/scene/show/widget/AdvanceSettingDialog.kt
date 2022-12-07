package io.agora.scene.show.widget

import android.content.Context
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.show.R
import io.agora.scene.show.databinding.*
import io.agora.scene.widget.basic.BindingViewHolder
import java.util.*

/**
 * 高级设置弹窗
 */
class AdvanceSettingDialog(context: Context) : BottomFullDialog(context) {

    companion object {
        val ITEM_ID_SWITCH_BASE = 0x00000001
        val ITEM_ID_SWITCH_QUALITY_ENHANCE = ITEM_ID_SWITCH_BASE + 1
        val ITEM_ID_SWITCH_COLOR_ENHANCE = ITEM_ID_SWITCH_BASE + 2
        val ITEM_ID_SWITCH_DARK_ENHANCE = ITEM_ID_SWITCH_BASE + 3
        val ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE = ITEM_ID_SWITCH_BASE + 4
        val ITEM_ID_SWITCH_BITRATE_SAVE = ITEM_ID_SWITCH_BASE + 5
        val ITEM_ID_SWITCH_EAR_BACK = ITEM_ID_SWITCH_BASE + 6

        val ITEM_ID_SEEKBAR_BASE = ITEM_ID_SWITCH_BASE shl 8
        val ITEM_ID_SEEKBAR_BITRATE = ITEM_ID_SEEKBAR_BASE + 1
        val ITEM_ID_SEEKBAR_VOCAL_VOLUME = ITEM_ID_SEEKBAR_BASE + 2
        val ITEM_ID_SEEKBAR_MUSIC_VOLUME = ITEM_ID_SEEKBAR_BASE + 3

        val ITEM_ID_SELECTOR_BASE = ITEM_ID_SEEKBAR_BASE shl 8
        val ITEM_ID_SELECTOR_RESOLUTION = ITEM_ID_SELECTOR_BASE + 1
        val ITEM_ID_SELECTOR_FRAMERATE = ITEM_ID_SELECTOR_BASE + 2

        private val resolutionList =
            listOf("320x240", "480x360", "360x640", "960x540", "960x720", "1280x720")
        private val frameRateList =
            listOf("1 fps", "7 fps", "10 fps", "15 fps", "24 fps", "30 fps", "60 fps")
        private val cacheItemValues = mutableMapOf<Int, Int>()
    }

    private val mBinding by lazy {
        ShowSettingAdvanceDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private data class TabPage(
        val viewType: Int,
        @StringRes val title: Int
    )

    private val VIEW_TYPE_VIDEO_SETTING = 0
    private val VIEW_TYPE_AUDIO_SETTING = 1
    private val tagPageList = arrayListOf(
        TabPage(
            VIEW_TYPE_VIDEO_SETTING,
            R.string.show_setting_advance_video_setting,
        ),
        TabPage(
            VIEW_TYPE_AUDIO_SETTING,
            R.string.show_setting_advance_audio_setting,
        )
    )

    private val itemInVisibleMap = mutableMapOf<Int, Boolean>()
    private var presetMode = false

    private val presetChangeTipDialog by lazy {
        AlertDialog.Builder(context, R.style.show_alert_dialog).apply {
            setTitle(R.string.show_tip)
            setMessage(R.string.show_setting_advance_preset_mode)
            setPositiveButton(R.string.show_setting_confirm) { dialog, _ ->
                presetMode = false
                dialog.dismiss()
            }
            setNegativeButton(R.string.show_setting_cancel) { dialog, _ ->
                dialog.dismiss()
            }
        }.create()
    }

    private var onSwitchChangeListener: ((AdvanceSettingDialog, Int, Boolean) -> Unit)? = null
    private var onSelectorChangeListener: ((AdvanceSettingDialog, Int, Int) -> Unit)? = null
    private var onSeekbarChangeListener: ((AdvanceSettingDialog, Int, Int) -> Unit)? = null

    private var isDismissWhenPresetDone = false
    private var isShowPreset = true

    init {
        setContentView(mBinding.root)

        mBinding.ivBack.setOnClickListener {
            dismiss()
        }
        mBinding.tvPreset.setOnClickListener {
            showPresetDialog()
        }

        mBinding.viewPager2.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ) = when (viewType) {
                VIEW_TYPE_VIDEO_SETTING -> BindingViewHolder(
                    ShowSettingAdvanceVideoBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
                VIEW_TYPE_AUDIO_SETTING -> BindingViewHolder(
                    ShowSettingAdvanceAudioBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                )
                else -> throw RuntimeException("Not support viewType: $viewType")
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                when (getItemViewType(position)) {
                    VIEW_TYPE_VIDEO_SETTING -> (holder as? BindingViewHolder<*>)?.binding?.let {
                        updateVideoSettingView(it as ShowSettingAdvanceVideoBinding)
                    }
                    VIEW_TYPE_AUDIO_SETTING -> (holder as? BindingViewHolder<*>)?.binding?.let {
                        updateAudioSettingView(it as ShowSettingAdvanceAudioBinding)
                    }
                    else -> throw RuntimeException("Can not find position support viewType. position: $position")
                }
            }

            override fun getItemCount() = tagPageList.size

            override fun getItemViewType(position: Int): Int {
                return tagPageList[position].viewType
            }
        }

        TabLayoutMediator(
            mBinding.tabLayout, mBinding.viewPager2
        ) { tab, position ->
            tab.text = context.getString(tagPageList[position].title)
        }.attach()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if(isShowPreset){
            showPresetDialog()
        }
    }

    private fun showPresetDialog() {
        PresetDialog(getParentContext()).apply {
            show()
        }
    }

    fun hideAudioSetting(){
        if(tagPageList.size > 1){
            tagPageList.removeAt(1)
            mBinding.tabLayout.isVisible = false
            mBinding.viewPager2.adapter?.notifyDataSetChanged()
        }
    }

    fun setItemInvisible(itemId: Int, invisible: Boolean) {
        itemInVisibleMap[itemId] = invisible
    }

    fun setOnSwitchChangeListener(listener: (AdvanceSettingDialog, Int, Boolean) -> Unit) {
        onSwitchChangeListener = listener
    }

    fun setOnSeekbarChangeListener(listener: (AdvanceSettingDialog, Int, Int) -> Unit) {
        onSeekbarChangeListener = listener
    }

    fun setOnSelectorChangeListener(listener: (AdvanceSettingDialog, Int, Int) -> Unit) {
        onSelectorChangeListener = listener
    }

    fun setDismissWhenPresetDone(dismiss: Boolean) {
        isDismissWhenPresetDone = dismiss
    }

    fun setShowPreset(isShow: Boolean){
        isShowPreset = isShow
        mBinding.tvPreset.isVisible = isShowPreset
    }

    fun getResolution(selected: Int): Size {
        resolutionList.getOrNull(selected)?.let {
            val split = it.split("x")
            return Size(split[0].toInt(), split[1].toInt())
        }
        return Size(640, 480)
    }

    fun getFrameRate(selected: Int): Int {
        frameRateList.getOrNull(selected)?.let {
            val split = it.split(" ")
            return split[0].toInt()
        }
        return 15
    }

    private fun updateAudioSettingView(binding: ShowSettingAdvanceAudioBinding) {
        setupSwitchItem(
            ITEM_ID_SWITCH_EAR_BACK,
            binding.earBack,
            R.string.show_setting_advance_ear_back,
            R.string.show_setting_advance_ear_back_tip
        )
        setupSeekbarItem(
            ITEM_ID_SEEKBAR_VOCAL_VOLUME,
            binding.vocalVolume,
            R.string.show_setting_advance_vocal_volume,
            "%d",
            0, 100
        )
        setupSeekbarItem(
            ITEM_ID_SEEKBAR_MUSIC_VOLUME,
            binding.musicVolume,
            R.string.show_setting_advance_music_volume,
            "%d",
            0, 100
        )
    }

    private fun updateVideoSettingView(binding: ShowSettingAdvanceVideoBinding) {
        setupSwitchItem(
            ITEM_ID_SWITCH_QUALITY_ENHANCE,
            binding.qualityEnhance,
            R.string.show_setting_advance_quality_enhance,
            R.string.show_setting_advance_quality_enhance_tip
        )
        setupSwitchItem(
            ITEM_ID_SWITCH_COLOR_ENHANCE,
            binding.colorEnhance,
            R.string.show_setting_advance_color_enhance,
            R.string.show_setting_advance_color_enhance_tip
        )
        setupSwitchItem(
            ITEM_ID_SWITCH_DARK_ENHANCE,
            binding.darkEnhance,
            R.string.show_setting_advance_dark_enhance,
            R.string.show_setting_advance_dark_enhance_tip
        )
        setupSwitchItem(
            ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE,
            binding.videoNoiseReduction,
            R.string.show_setting_advance_video_noise_reduce,
            R.string.show_setting_advance_video_noise_reduce_tip
        )
        setupSwitchItem(
            ITEM_ID_SWITCH_BITRATE_SAVE,
            binding.bitrateSave,
            R.string.show_setting_advance_bitrate_save,
            R.string.show_setting_advance_bitrate_save_tip
        )
        setupSelectorItem(
            ITEM_ID_SELECTOR_RESOLUTION,
            binding.resolution,
            R.string.show_setting_advance_resolution,
            resolutionList
        )
        setupSelectorItem(
            ITEM_ID_SELECTOR_FRAMERATE,
            binding.frameRate,
            R.string.show_setting_advance_framerate,
            frameRateList
        )
        setupSeekbarItem(
            ITEM_ID_SEEKBAR_BITRATE,
            binding.bitrate,
            R.string.show_setting_advance_bitrate,
            "%d kbps",
            200, 2000
        )
    }

    private fun setupSwitchItem(
        itemId: Int,
        binding: ShowSettingAdvanceItemSwitchBinding,
        @StringRes title: Int,
        @StringRes tip: Int
    ) {
        binding.root.isVisible = itemInVisibleMap[itemId]?.not() ?: true
        binding.tvTitle.text = context.getString(title)
        binding.ivTip.setOnClickListener {
            ToastDialog(context).showTip(context.getString(tip))
        }
        binding.switchCompat.setOnCheckedChangeListener(null)
        binding.switchCompat.isChecked = (cacheItemValues[itemId] ?: 0) > 0
        onSwitchChangeListener?.invoke(this, itemId, binding.switchCompat.isChecked)
        binding.switchCompat.setOnCheckedChangeListener { btn, isChecked ->
            if (checkPresetMode()) {
                btn.isChecked = !isChecked
            } else {
                cacheItemValues[itemId] = if (isChecked) 1 else 0
                onSwitchChangeListener?.invoke(this, itemId, isChecked)
            }
        }
    }

    private fun setupSelectorItem(
        itemId: Int,
        binding: ShowSettingAdvanceItemSelectorBinding,
        @StringRes title: Int,
        selectList: List<String>
    ) {
        binding.root.isVisible = itemInVisibleMap[itemId]?.not() ?: true
        binding.tvTitle.text = context.getString(title)
        val selectPosition = cacheItemValues[itemId] ?: 0
        binding.tvValue.text = selectList.getOrNull(selectPosition)
        onSelectorChangeListener?.invoke(this, itemId, selectPosition)
        binding.root.setOnClickListener {
            if (!checkPresetMode()) {
                BottomLightListDialog(context).apply {
                    setTitle(title)
                    setListData(selectList)
                    setSelectedPosition(cacheItemValues[itemId] ?: 0)
                    setOnSelectedChangedListener { dialog, index ->
                        cacheItemValues[itemId] = index
                        binding.tvValue.text = selectList.getOrNull(index)
                        onSelectorChangeListener?.invoke(this@AdvanceSettingDialog, itemId, index)
                        dialog.dismiss()
                    }
                    show()
                }
            }

        }
    }

    private fun setupSeekbarItem(
        itemId: Int,
        binding: ShowSettingAdvanceItemSeekbarBinding,
        @StringRes title: Int,
        valueFormat: String,
        fromValue: Int,
        toValue: Int
    ) {
        binding.root.isVisible = itemInVisibleMap[itemId]?.not() ?: true
        binding.tvTitle.text = context.getString(title)
        binding.slider.valueFrom = fromValue.toFloat()
        binding.slider.valueTo = toValue.toFloat()
        val defaultValue = cacheItemValues[itemId]?.toFloat() ?: fromValue.toFloat()
        binding.slider.value = defaultValue
        binding.tvValue.text = String.format(Locale.US, valueFormat, binding.slider.value.toInt())
        binding.slider.clearOnChangeListeners()
        onSeekbarChangeListener?.invoke(this, itemId, defaultValue.toInt())
        binding.slider.addOnChangeListener { _, nValue, fromUser ->
            if (fromUser) {
                if (checkPresetMode()) {
                    binding.slider.value = defaultValue
                } else {
                    binding.tvValue.text = String.format(Locale.US, valueFormat, nValue.toInt())
                    cacheItemValues[itemId] = nValue.toInt()
                    onSeekbarChangeListener?.invoke(this, itemId, nValue.toInt())
                }
            }
        }
    }

    private fun checkPresetMode(): Boolean {
        if (!presetMode) {
            return false
        }
        if (!presetChangeTipDialog.isShowing) {
            presetChangeTipDialog.show()
        }
        return true
    }

    private fun onPresetShowModeSelected(level: Int) {
        if (level < 0) {
            if (isDismissWhenPresetDone) {
                dismiss()
            }
            return
        }
        presetMode = true
        when (level) {
            // 低端机
            0 -> {
                cacheItemValues[ITEM_ID_SWITCH_QUALITY_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_COLOR_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_DARK_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_BITRATE_SAVE] = 0
                cacheItemValues[ITEM_ID_SELECTOR_RESOLUTION] = 3
                cacheItemValues[ITEM_ID_SELECTOR_FRAMERATE] = 3
                cacheItemValues[ITEM_ID_SEEKBAR_BITRATE] = 1500
                cacheItemValues[ITEM_ID_SWITCH_EAR_BACK] = 0
                cacheItemValues[ITEM_ID_SEEKBAR_VOCAL_VOLUME] = 80
                cacheItemValues[ITEM_ID_SEEKBAR_MUSIC_VOLUME] = 30
            }
            // 中端机
            1 -> {
                cacheItemValues[ITEM_ID_SWITCH_QUALITY_ENHANCE] = 1
                cacheItemValues[ITEM_ID_SWITCH_COLOR_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_DARK_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_BITRATE_SAVE] = 0
                cacheItemValues[ITEM_ID_SELECTOR_RESOLUTION] = 5
                cacheItemValues[ITEM_ID_SELECTOR_FRAMERATE] = 4
                cacheItemValues[ITEM_ID_SEEKBAR_BITRATE] = 1800
                cacheItemValues[ITEM_ID_SWITCH_EAR_BACK] = 0
                cacheItemValues[ITEM_ID_SEEKBAR_VOCAL_VOLUME] = 80
                cacheItemValues[ITEM_ID_SEEKBAR_MUSIC_VOLUME] = 30
            }
            // 高端机
            2 -> {
                cacheItemValues[ITEM_ID_SWITCH_QUALITY_ENHANCE] = 1
                cacheItemValues[ITEM_ID_SWITCH_COLOR_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_DARK_ENHANCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE] = 0
                cacheItemValues[ITEM_ID_SWITCH_BITRATE_SAVE] = 0
                cacheItemValues[ITEM_ID_SELECTOR_RESOLUTION] = 5
                cacheItemValues[ITEM_ID_SELECTOR_FRAMERATE] = 4
                cacheItemValues[ITEM_ID_SEEKBAR_BITRATE] = 1800
                cacheItemValues[ITEM_ID_SWITCH_EAR_BACK] = 0
                cacheItemValues[ITEM_ID_SEEKBAR_VOCAL_VOLUME] = 80
                cacheItemValues[ITEM_ID_SEEKBAR_MUSIC_VOLUME] = 30
            }
        }
        mBinding.viewPager2.adapter?.notifyDataSetChanged()
        ToastDialog(context).apply {
            dismissDelayShort()
            showMessage(context.getString(R.string.show_setting_preset_done))
        }
        if (isDismissWhenPresetDone) {
            dismiss()
        }
    }


    /**
     * 预设设置
     */
    inner class PresetDialog(context: Context) : BottomFullDialog(context) {
        private val mBinding by lazy {
            ShowSettingPresetDialogBinding.inflate(
                LayoutInflater.from(
                    context
                )
            )
        }

        init {
            setChangeStatusBar(false)
            setContentView(mBinding.root)
            mBinding.ivClose.setOnClickListener {
                onPresetShowModeSelected(-1)
                dismiss()
            }
            mBinding.tvConfirm.setOnClickListener {
                val showSelectPosition = getGroupSelectedItem(
                    mBinding.showChooseItemLowDevice,
                    mBinding.showChooseItemMediumDevice,
                    mBinding.showChooseItemHighDevice
                )
                if (showSelectPosition < 0) {
                    ToastDialog(context).apply {
                        dismissDelayShort()
                        showMessage(context.getString(R.string.show_setting_preset_no_choise_tip))
                    }
                    return@setOnClickListener
                }
                onPresetShowModeSelected(showSelectPosition)
                dismiss()
            }
            groupItems(
                {},
                mBinding.showChooseItemLowDevice,
                mBinding.showChooseItemMediumDevice,
                mBinding.showChooseItemHighDevice
            )
        }

        private fun getGroupSelectedItem(vararg itemViews: View): Int {
            itemViews.forEachIndexed { index, view ->
                if (view.isActivated) {
                    return index
                }
            }
            return -1;
        }

        private fun groupItems(
            onSelectChanged: (Int) -> Unit,
            vararg itemViews: View
        ) {
            itemViews.forEachIndexed { index, view ->
                view.setOnClickListener {
                    if (view.isActivated) {
                        return@setOnClickListener
                    }
                    itemViews.forEach { it.isActivated = it == view }
                    onSelectChanged.invoke(index)
                }
            }
        }
    }
}


package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.scene.show.R
import io.agora.scene.show.VideoSetting
import io.agora.scene.show.VideoSetting.toIndex
import io.agora.scene.show.databinding.*
import io.agora.scene.show.utils.toInt
import io.agora.scene.widget.basic.BindingViewHolder
import java.util.*

/**
 * 高级设置弹窗
 */
class AdvanceSettingDialog(context: Context) : BottomFullDialog(context) {

    companion object {
        private const val ITEM_ID_SWITCH_BASE = 0x00000001
        const val ITEM_ID_SWITCH_QUALITY_ENHANCE = ITEM_ID_SWITCH_BASE + 1
        const val ITEM_ID_SWITCH_COLOR_ENHANCE = ITEM_ID_SWITCH_BASE + 2
        const val ITEM_ID_SWITCH_DARK_ENHANCE = ITEM_ID_SWITCH_BASE + 3
        const val ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE = ITEM_ID_SWITCH_BASE + 4
        const val ITEM_ID_SWITCH_BITRATE_SAVE = ITEM_ID_SWITCH_BASE + 5
        const val ITEM_ID_SWITCH_EAR_BACK = ITEM_ID_SWITCH_BASE + 6

        private const val ITEM_ID_SEEKBAR_BASE = ITEM_ID_SWITCH_BASE shl 8
        const val ITEM_ID_SEEKBAR_BITRATE = ITEM_ID_SEEKBAR_BASE + 1
        const val ITEM_ID_SEEKBAR_VOCAL_VOLUME = ITEM_ID_SEEKBAR_BASE + 2
        const val ITEM_ID_SEEKBAR_MUSIC_VOLUME = ITEM_ID_SEEKBAR_BASE + 3

        private const val ITEM_ID_SELECTOR_BASE = ITEM_ID_SEEKBAR_BASE shl 8
        const val ITEM_ID_SELECTOR_RESOLUTION = ITEM_ID_SELECTOR_BASE + 1
        const val ITEM_ID_SELECTOR_FRAME_RATE = ITEM_ID_SELECTOR_BASE + 2

        private const val VIEW_TYPE_VIDEO_SETTING = 0
        private const val VIEW_TYPE_AUDIO_SETTING = 1
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

    private val defaultItemValues = mutableMapOf<Int, Int>().apply {
        put(
            ITEM_ID_SWITCH_QUALITY_ENHANCE,
            VideoSetting.getCurrBroadcastSetting().video.H265.toInt()
        )
        put(
            ITEM_ID_SWITCH_COLOR_ENHANCE,
            VideoSetting.getCurrBroadcastSetting().video.colorEnhance.toInt()
        )
        put(
            ITEM_ID_SWITCH_DARK_ENHANCE,
            VideoSetting.getCurrBroadcastSetting().video.lowLightEnhance.toInt()
        )
        put(
            ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE,
            VideoSetting.getCurrBroadcastSetting().video.videoDenoiser.toInt()
        )
        put(ITEM_ID_SWITCH_BITRATE_SAVE, VideoSetting.getCurrBroadcastSetting().video.PVC.toInt())
        put(
            ITEM_ID_SWITCH_EAR_BACK,
            VideoSetting.getCurrBroadcastSetting().audio.inEarMonitoring.toInt()
        )
        put(ITEM_ID_SEEKBAR_BITRATE, VideoSetting.getCurrBroadcastSetting().video.bitRate)
        put(
            ITEM_ID_SEEKBAR_VOCAL_VOLUME,
            VideoSetting.getCurrBroadcastSetting().audio.recordingSignalVolume
        )
        put(
            ITEM_ID_SEEKBAR_MUSIC_VOLUME,
            VideoSetting.getCurrBroadcastSetting().audio.audioMixingVolume
        )
        put(
            ITEM_ID_SELECTOR_RESOLUTION,
            VideoSetting.getCurrBroadcastSetting().video.encodeResolution.toIndex()
        )
        put(
            ITEM_ID_SELECTOR_FRAME_RATE,
            VideoSetting.getCurrBroadcastSetting().video.frameRate.toIndex()
        )
    }

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
    private val itemShowTextMap = mutableMapOf<Int, Boolean>()
    private var presetMode = VideoSetting.isCurrBroadcastSettingRecommend()

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

    init {
        setContentView(mBinding.root)

        mBinding.ivBack.setOnClickListener {
            dismiss()
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

    fun setItemInvisible(itemId: Int, invisible: Boolean) {
        itemInVisibleMap[itemId] = invisible
    }

    fun setItemShowTextOnly(itemId: Int, showText: Boolean) {
        itemShowTextMap[itemId] = showText
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
            R.string.show_setting_advance_quality_h265,
            R.string.show_setting_advance_quality_h265_tip
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
        // 码率节省
        setupSwitchItem(
            ITEM_ID_SWITCH_BITRATE_SAVE,
            binding.bitrateSave,
            R.string.show_setting_advance_bitrate_save,
            R.string.show_setting_advance_bitrate_save_tip
        )
        // 编码分辨率
        setupSelectorItem(
            ITEM_ID_SELECTOR_RESOLUTION,
            binding.resolution,
            R.string.show_setting_advance_encode_resolution,
            R.string.show_setting_advance_encode_resolution_tip,
            VideoSetting.ResolutionList.map { "${it.width}x${it.height}" }
        )
        // 编码帧率
        setupSelectorItem(
            ITEM_ID_SELECTOR_FRAME_RATE,
            binding.frameRate,
            R.string.show_setting_advance_encode_framerate,
            R.string.show_setting_advance_encode_framerate_tip,
            VideoSetting.FrameRateList.map { "${it.fps} fps" }
        )
        setupSeekbarItem(
            ITEM_ID_SEEKBAR_BITRATE,
            binding.bitrate,
            R.string.show_setting_advance_bitrate,
            "%d kbps",
            200, 4000
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
        if (itemShowTextMap[itemId] == true) {
            binding.switchCompat.isVisible = false
            binding.tvSwitch.isVisible = true
            val isChecked = (defaultItemValues[itemId] ?: 0) > 0
            binding.tvSwitch.text =
                context.getString(if (isChecked) R.string.show_setting_opened else R.string.show_setting_closed)
        } else {
            binding.switchCompat.isVisible = true
            binding.tvSwitch.isVisible = false
            binding.switchCompat.setOnCheckedChangeListener(null)
            binding.switchCompat.isChecked = (defaultItemValues[itemId] ?: 0) > 0
            onSwitchChanged(itemId, binding.switchCompat.isChecked)
            binding.switchCompat.setOnCheckedChangeListener { btn, isChecked ->
                if (checkPresetMode()) {
                    btn.isChecked = !isChecked
                } else {
                    defaultItemValues[itemId] = if (isChecked) 1 else 0
                    onSwitchChanged(itemId, isChecked)
                }
            }
        }
    }

    private fun setupSelectorItem(
        itemId: Int,
        binding: ShowSettingAdvanceItemSelectorBinding,
        @StringRes title: Int,
        @StringRes tip: Int = -1,
        selectList: List<String>
    ) {
        binding.root.isVisible = itemInVisibleMap[itemId]?.not() ?: true
        binding.tvTitle.text = context.getString(title)
        binding.ivTip.visibility = if (tip == -1) View.GONE else View.VISIBLE
        binding.ivTip.setOnClickListener {
            ToastDialog(context).showTip(context.getString(tip))
        }
        val selectPosition = defaultItemValues[itemId] ?: 0
        binding.tvValue.text = selectList.getOrNull(selectPosition)
        onSelectorChanged(itemId, selectPosition)
        binding.root.setOnClickListener {
            if (!checkPresetMode()) {
                BottomLightListDialog(context).apply {
                    setTitle(title)
                    setListData(selectList)
                    setSelectedPosition(defaultItemValues[itemId] ?: 0)
                    setOnSelectedChangedListener { dialog, index ->
                        defaultItemValues[itemId] = index
                        binding.tvValue.text = selectList.getOrNull(index)
                        onSelectorChanged(itemId, index)
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
        val defaultValue = defaultItemValues[itemId]?.toFloat() ?: fromValue.toFloat()
        binding.slider.value = defaultValue
        binding.tvValue.text = String.format(Locale.US, valueFormat, binding.slider.value.toInt())
        binding.slider.clearOnChangeListeners()
        onSeekbarChanged(itemId, defaultValue.toInt())
        binding.slider.addOnChangeListener { _, nValue, fromUser ->
            if (fromUser) {
                if (checkPresetMode()) {
                    binding.slider.value = defaultValue
                } else {
                    binding.tvValue.text = String.format(Locale.US, valueFormat, nValue.toInt())
                    defaultItemValues[itemId] = nValue.toInt()
                    onSeekbarChanged(itemId, nValue.toInt())
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

    private fun onSwitchChanged(itemId: Int, isChecked: Boolean) {
        when (itemId) {
            ITEM_ID_SWITCH_QUALITY_ENHANCE -> VideoSetting.updateBroadcastSetting(h265 = isChecked)
            ITEM_ID_SWITCH_COLOR_ENHANCE -> VideoSetting.updateBroadcastSetting(colorEnhance = isChecked)
            ITEM_ID_SWITCH_DARK_ENHANCE -> VideoSetting.updateBroadcastSetting(lowLightEnhance = isChecked)
            ITEM_ID_SWITCH_VIDEO_NOISE_REDUCE -> VideoSetting.updateBroadcastSetting(videoDenoiser = isChecked)
            ITEM_ID_SWITCH_BITRATE_SAVE -> VideoSetting.updateBroadcastSetting(PVC = isChecked)
            ITEM_ID_SWITCH_EAR_BACK -> VideoSetting.updateBroadcastSetting(inEarMonitoring = isChecked)
        }
    }

    private fun onSeekbarChanged(itemId: Int, value: Int) {
        when (itemId) {
            ITEM_ID_SEEKBAR_BITRATE -> VideoSetting.updateBroadcastSetting(bitRate = value)
            ITEM_ID_SEEKBAR_VOCAL_VOLUME -> VideoSetting.updateBroadcastSetting(
                recordingSignalVolume = value
            )
            ITEM_ID_SEEKBAR_MUSIC_VOLUME -> VideoSetting.updateBroadcastSetting(audioMixingVolume = value)
        }
    }

    private fun onSelectorChanged(itemId: Int, index: Int) {
        when (itemId) {
            ITEM_ID_SELECTOR_RESOLUTION -> VideoSetting.updateBroadcastSetting(encoderResolution = VideoSetting.ResolutionList[index], captureResolution = VideoSetting.ResolutionList[index])
            ITEM_ID_SELECTOR_FRAME_RATE -> VideoSetting.updateBroadcastSetting(frameRate = VideoSetting.FrameRateList[index])
        }
    }
}


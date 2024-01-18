package io.agora.scene.ktv.live.fragmentdialog

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseRecyclerViewAdapter.BaseViewHolder
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.dp
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogMusicSettingBinding
import io.agora.scene.ktv.databinding.KtvItemEffectvoiceBinding
import io.agora.scene.ktv.live.RoomLivingActivity
import io.agora.scene.ktv.live.bean.AECLevel
import io.agora.scene.ktv.live.bean.AINSMode
import io.agora.scene.ktv.live.bean.EffectVoiceBean
import io.agora.scene.ktv.live.bean.MusicSettingBean
import io.agora.scene.ktv.live.bean.ScoringDifficultyMode
import io.agora.scene.ktv.live.bean.SoundCardSettingBean
import io.agora.scene.ktv.service.RoomSelSongModel
import io.agora.scene.widget.doOnProgressChanged
import io.agora.scene.widget.toast.CustomToast


class MusicSettingDialog constructor(
    var mSetting: MusicSettingBean,
    var mSoundCardSetting: SoundCardSettingBean,
    var isListener: Boolean, // 是否是观众
    var currentSong: RoomSelSongModel?, // 当前歌曲
) :
    BaseBottomSheetDialogFragment<KtvDialogMusicSettingBinding>() {

    companion object {
        const val TAG = "KtvMusicProfileDialog"
    }

    private var mVoiceEffectAdapter: BaseRecyclerViewAdapter<KtvItemEffectvoiceBinding, EffectVoiceBean, EffectVoiceHolder>? =
        null

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
            val bottomSheet =
                (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//                behavior.state = BottomSheetBehavior.STATE_EXPANDED
//                behavior.skipCollapsed = true
                behavior.isHideable = false
//                behavior.setDraggable(false)
            }
        }
    }

    //防止多次回调
    private var lastKeyBoard = false
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.let { window ->
            // 获取根布局可见区域的高度
            val initialWindowHeight = Rect().apply { window.decorView.getWindowVisibleDisplayFrame(this) }.height()
            view.viewTreeObserver.addOnGlobalLayoutListener {
                val tempWindow = dialog?.window ?: return@addOnGlobalLayoutListener
                val currentWindowHeight =
                    Rect().apply { tempWindow.decorView.getWindowVisibleDisplayFrame(this) }.height()
                // 判断键盘高度来确定键盘的显示状态
                if (currentWindowHeight < initialWindowHeight) {
                    if (lastKeyBoard) return@addOnGlobalLayoutListener
                    lastKeyBoard = true
                    val length = mBinding.AIAECInput.text?.length ?: 0
                    mBinding.AIAECInput.setSelection(length)

                    // 软键盘可见
                    Log.d("zhangw", "current: $currentWindowHeight, initial: $initialWindowHeight, show: true")
                } else {
                    if (!lastKeyBoard) return@addOnGlobalLayoutListener
                    lastKeyBoard = false
                    // 软键盘已收起
                    Log.d("zhangw", "current: $currentWindowHeight, initial: $initialWindowHeight, show: false")
                    mBinding.AIAECInput.clearFocus()
                    val AIAECStrength = mBinding.AIAECInput.text.toString().toIntOrNull() ?: 0
                    if (IntRange(0, 4).contains(AIAECStrength)) {
                        if (mSetting.mAIAECStrength != AIAECStrength) {
                            mSetting.mAIAECStrength = AIAECStrength
                        }
                    } else {
                        mBinding.AIAECInput.setText(mSetting.mAIAECStrength.toString())
                        CustomToast.show(R.string.ktv_AIAEC_input_hint)
                    }
                }
            }
        }

        mBinding.ivBackIcon.setOnClickListener { view -> (requireActivity() as RoomLivingActivity).closeMenuDialog() }
        // 耳返
        if (mSetting.mEarBackEnable) {
            mBinding.switchEar.text = getString(R.string.ktv_open)
        } else {
            mBinding.switchEar.text = getString(R.string.ktv_close)
        }
        mBinding.switchEar.setOnClickListener { v: View -> this.showEarBackPage(v) }

        // 虚拟声卡
        if (mSoundCardSetting.isEnable()) {
            mBinding.switchSoundCard.text = getString(R.string.ktv_open)
        } else {
            mBinding.switchSoundCard.text = getString(R.string.ktv_close)
        }
        mBinding.switchSoundCard.setOnClickListener { v: View -> this.showSoundCardPage(v) }

        // 人声音量
        mBinding.sbMicVol.progress = mSetting.mMicVolume
        mBinding.btMicVolDown.setOnClickListener { v -> tuningMicVolume(false) }
        mBinding.btnMicVolUp.setOnClickListener { v -> tuningMicVolume(true) }
        mBinding.sbMicVol.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mMicVolume = progress
            }
        }

        // 伴奏音量
        mBinding.sbAccVol.progress = mSetting.mAccVolume
        mBinding.btAccVolDown.setOnClickListener { v -> tuningMusicVolume(false) }
        mBinding.btAccVolUp.setOnClickListener { v -> tuningMusicVolume(true) }
        mBinding.sbAccVol.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mAccVolume = progress
            }
        }

        // 远端音量
        mBinding.sbRemoteVol.progress = mSetting.mRemoteVolume
        mBinding.btRemoteVolDown.setOnClickListener { v -> tuningRemoteVolume(false) }
        mBinding.btRemoteVolUp.setOnClickListener { v -> tuningRemoteVolume(true) }
        mBinding.sbRemoteVol.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mRemoteVolume = progress
            }
        }

        if (isListener) {
            enableDisableView(mBinding.layoutAccVol, false)
            mBinding.layoutAccVol.alpha = 0.3f
            enableDisableView(mBinding.layoutAccVol, false)
            mBinding.layoutRemoteVol.alpha = 0.3f
        } else {
            enableDisableView(mBinding.layoutAccVol, true)
            mBinding.layoutAccVol.alpha = 1.0f
            enableDisableView(mBinding.layoutRemoteVol, true)
            mBinding.layoutRemoteVol.alpha = 1.0f
        }

        // 音效
        setupVoiceEffectAdapter()

        // 打分难度设置
        when (mSetting.mScoringDifficultyMode) {
            ScoringDifficultyMode.Low -> mBinding.rgVoiceScoringDifficulty.check(R.id.tvScoringDifficultyLow)
            ScoringDifficultyMode.High -> mBinding.rgVoiceScoringDifficulty.check(R.id.tvScoringDifficultyHigh)
            else -> mBinding.rgVoiceScoringDifficulty.check(R.id.tvScoringDifficultyRecommend)
        }
        mBinding.rgVoiceScoringDifficulty.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.tvScoringDifficultyLow -> mSetting.mScoringDifficultyMode = ScoringDifficultyMode.Low
                R.id.tvScoringDifficultyHigh -> mSetting.mScoringDifficultyMode = ScoringDifficultyMode.High
                else -> mSetting.mScoringDifficultyMode = ScoringDifficultyMode.Recommend
            }
        }

        if (currentSong == null) {
            enableDisableView(mBinding.layoutVoiceScoringDifficulty, false)
            mBinding.layoutVoiceScoringDifficulty.alpha = 0.3f
        } else {
            enableDisableView(mBinding.layoutVoiceScoringDifficulty, true)
            mBinding.layoutVoiceScoringDifficulty.alpha = 1.0f
        }

        // 专业模式
        mBinding.cbStartProfessionalMode.setOnCheckedChangeListener { buttonView, isChecked ->
            mSetting.mProfessionalModeEnable = isChecked
        }
        mBinding.cbStartProfessionalMode.isChecked = mSetting.mProfessionalModeEnable

        // Multi Path
        mBinding.cbMultipath.setOnCheckedChangeListener { buttonView, isChecked ->
            mSetting.mMultiPathEnable = isChecked
        }
        mBinding.cbMultipath.isChecked = mSetting.mMultiPathEnable

        // 音质
        when (mSetting.mAecLevel) {
            AECLevel.High -> mBinding.rgVoiceMode.check(R.id.tvVoiceHigh)
            else -> mBinding.rgVoiceMode.check(R.id.tvVoiceStandard)
        }
        mBinding.rgVoiceMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.tvVoiceStandard -> mSetting.mAecLevel = AECLevel.Standard
                R.id.tvVoiceHigh -> mSetting.mAecLevel = AECLevel.High
            }
        }

        // 降低背景噪音
        when (mSetting.mAinsMode) {
            AINSMode.Medium -> mBinding.rgAINSMode.check(R.id.tvAINSMiddle)
            AINSMode.High -> mBinding.rgAINSMode.check(R.id.tvAINSMiddle)
            else -> mBinding.rgAINSMode.check(R.id.tvAINSClose)
        }
        mBinding.rgAINSMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.tvAINSClose -> mSetting.mAinsMode = AINSMode.Close
                R.id.tvAINSMiddle -> {
                    if (mSetting.mLowLatencyMode) {
                        mBinding.cbLowLatency.isChecked = false
                    }
                    mSetting.mAinsMode = AINSMode.Medium
                }

                R.id.tvAINSHigh -> {
                    if (mSetting.mLowLatencyMode) {
                        mBinding.cbLowLatency.isChecked = false
                    }
                    mSetting.mAinsMode = AINSMode.High
                }
            }
        }

        // 低延迟模式
        mBinding.cbLowLatency.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked && mSetting.mAinsMode != AINSMode.Close) {
                mBinding.rgAINSMode.check(R.id.tvAINSClose)
            }
            mSetting.mLowLatencyMode = isChecked
        }
        mBinding.cbLowLatency.isChecked = mSetting.mLowLatencyMode

        // AIAEC 开关
        mBinding.cbAIAECSwitcher.setOnCheckedChangeListener { buttonView, isChecked ->
            mBinding.groupAIAECStrength.isVisible = isChecked
            mSetting.mAIAECEnable = isChecked
            mSetting.mAIAECStrength = mSetting.mAIAECStrength
        }
        mBinding.cbAIAECSwitcher.isChecked = mSetting.mAIAECEnable

        // AIAEC 强度选择
        mBinding.AIAECInput.doAfterTextChanged {

        }
        mBinding.AIAECInput.setOnTouchListener { v, event ->
            mBinding.AIAECInput.requestFocus()
            showKeyboard(mBinding.AIAECInput)
            true
        }
        mBinding.groupAIAECStrength.isVisible = mSetting.mAIAECEnable
        mBinding.AIAECInput.setText(mSetting.mAIAECStrength.toString())
    }

    private fun enableDisableView(viewGroup: ViewGroup, enable: Boolean) {
        for (idx in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(idx).isEnabled = enable
        }
    }

    /**
     * 耳返设置
     */
    private fun showEarBackPage(v: View) {
        mBinding.root.removeAllViews()
        val earBackFragment = EarBackFragment(mSetting)
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, earBackFragment, EarBackFragment.TAG)
        ft.commit()
    }

    /**
     * 虚拟声卡
     */
    private fun showSoundCardPage(v: View) {
        mBinding.root.removeAllViews()
        val soundCardFragment = SoundCardFragment(mSoundCardSetting)
        soundCardFragment.onClickSoundCardType = {
            showSoundTypeSelectPage()
        }
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, soundCardFragment, SoundCardFragment.TAG)
        ft.commit()
    }

    /**
     * 预设音效
     */
    private fun showSoundTypeSelectPage() {
        mBinding.root.removeAllViews()
        val soundTypeFragment: BaseViewBindingFragment<*> = SoundTypeFragment(mSoundCardSetting)
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, soundTypeFragment, SoundCardFragment.TAG)
        ft.commit()
    }

    // 人声音量
    private fun tuningMicVolume(volumeUp: Boolean) {
        var newVocalVolume: Int = this.mSetting.mMicVolume
        if (volumeUp) {
            newVocalVolume += 1
        } else {
            newVocalVolume -= 1
        }
        if (newVocalVolume > 100) newVocalVolume = 100
        if (newVocalVolume < 0) newVocalVolume = 0
        if (newVocalVolume != this.mSetting.mMicVolume) {
            this.mSetting.mMicVolume = newVocalVolume
        }
        mBinding.sbMicVol.progress = newVocalVolume
    }

    // 伴奏音量
    private fun tuningMusicVolume(volumeUp: Boolean) {
        var newMusicVolume: Int = this.mSetting.mAccVolume
        if (volumeUp) {
            newMusicVolume += 1
        } else {
            newMusicVolume -= 1
        }
        if (newMusicVolume > 100) newMusicVolume = 100
        if (newMusicVolume < 0) newMusicVolume = 0
        if (newMusicVolume != this.mSetting.mAccVolume) {
            this.mSetting.mAccVolume = newMusicVolume
        }
        mBinding.sbAccVol.progress = newMusicVolume
    }

    // 远端音量
    private fun tuningRemoteVolume(volumeUp: Boolean) {
        var newRemoteVolume: Int = this.mSetting.mRemoteVolume
        if (volumeUp) {
            newRemoteVolume += 1
        } else {
            newRemoteVolume -= 1
        }
        if (newRemoteVolume > 100) newRemoteVolume = 100
        if (newRemoteVolume < 0) newRemoteVolume = 0
        if (newRemoteVolume != this.mSetting.mRemoteVolume) {
            this.mSetting.mRemoteVolume = newRemoteVolume
        }
        mBinding.sbRemoteVol.progress = newRemoteVolume
    }

    // 音效
    private fun setupVoiceEffectAdapter() {
        val stringArray = resources.getStringArray(R.array.ktv_audioPreset)
        val list: MutableList<EffectVoiceBean> = ArrayList()
        for (i in stringArray.indices) {
            val drawable: Int = if (i % 4 == 0) {
                R.mipmap.bg_sound_mode_4
            } else if (i % 3 == 0) {
                R.mipmap.bg_sound_mode_3
            } else if (i % 2 == 0) {
                R.mipmap.bg_sound_mode_2
            } else {
                R.mipmap.bg_sound_mode_1
            }
            val audioEffect = mSetting.getEffectIndex(i)
            list.add(EffectVoiceBean(i, audioEffect, drawable, stringArray[i]))
        }
        for (item in list) {
            item.isSelect = (mSetting.mAudioEffect == item.audioEffect)
        }

        mVoiceEffectAdapter =
            BaseRecyclerViewAdapter(
                list, object : OnItemClickListener<EffectVoiceBean> {
                    override fun onItemClick(data: EffectVoiceBean, view: View?, position: Int, viewType: Long) {
                        super.onItemClick(data, view, position, viewType)
                        Log.d(TAG, "onItemClick audio effect  $position")
                        mVoiceEffectAdapter?.apply {
                            for (i in dataList.indices) {
                                dataList[i].isSelect = i == position
                                notifyItemChanged(i)
                            }
                            mSetting.mAudioEffect = data.audioEffect
                        }
                    }
                },
                EffectVoiceHolder::class.java
            )

        mBinding.rvVoiceEffectList.adapter = mVoiceEffectAdapter
        val context = context ?: return
        val itemDecoration = object : DividerItemDecoration(context, HORIZONTAL) {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemCount = state.itemCount
                when (parent.getChildAdapterPosition(view)) {
                    0 -> { // first
                        outRect.left = 20.dp.toInt()
                        outRect.right = 10.dp.toInt()
                    }

                    itemCount - 1 -> { // last
                        outRect.right = 20.dp.toInt()
                    }

                    else -> {
                        outRect.right = 10.dp.toInt()
                    }
                }
            }
        }
        mBinding.rvVoiceEffectList.addItemDecoration(itemDecoration)
    }
}


class EffectVoiceHolder constructor(mBinding: KtvItemEffectvoiceBinding) :
    BaseViewHolder<KtvItemEffectvoiceBinding, EffectVoiceBean>(mBinding) {
    override fun binding(data: EffectVoiceBean?, selectedIndex: Int) {
        data ?: return
        mBinding.ivBg.setImageResource(data.resId)
        mBinding.tvTitle.text = data.title
        mBinding.select.isVisible = data.isSelect
    }
}

interface MusicSettingCallback {
    /**
     * 耳返开关
     */
    fun onEarChanged(isEar: Boolean)

    /**
     * 耳返音量
     */
    fun onEarBackVolumeChanged(volume: Int)

    /**
     * 耳返模式
     */
    fun onEarBackModeChanged(mode: Int)

    /**
     * 人声音量
     */
    fun onMicVolChanged(vol: Int)

    /**
     * 伴奏音量
     */
    fun onAccVolChanged(vol: Int)

    /**
     * 远端音量
     */
    fun onRemoteVolChanged(volume: Int)

    /**
     * 音效
     */
    fun onAudioEffectChanged(audioEffect: Int)

    /**
     * 打分难度
     */
    fun onScoringDifficultyChanged(difficulty: Int)

    /**
     * 专业模式
     */
    fun onProfessionalModeChanged(enable: Boolean)

    /**
     * MultiPath 开关
     */
    fun onMultiPathChanged(enable: Boolean)

    /**
     * 音质
     */
    fun onAECLevelChanged(level: Int)

    /**
     * 低延迟模式
     */
    fun onLowLatencyModeChanged(enable: Boolean)

    /**
     * 降低背景噪音
     */
    fun onAINSModeChanged(mode: Int)

    /**
     * AIAEC 开关
     */
    fun onAIAECChanged(enable: Boolean)

    /**
     * AIAEC 强度
     */
    fun onAIAECStrengthSelect(strength: Int)
}


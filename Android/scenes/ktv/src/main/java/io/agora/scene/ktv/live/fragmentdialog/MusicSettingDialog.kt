package io.agora.scene.ktv.live.fragmentdialog

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseRecyclerViewAdapter.BaseViewHolder
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
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
import io.agora.scene.ktv.service.ChosenSongInfo
import io.agora.scene.widget.toast.CustomToast
import io.agora.scene.base.utils.dp
import io.agora.scene.widget.doOnProgressChanged


/**
 * Music setting dialog
 *
 * @property mSetting
 * @property mSoundCardSetting
 * @property isListener
 * @property currentSong
 * @constructor Create empty Music setting dialog
 */
class MusicSettingDialog constructor(
    private var mSetting: MusicSettingBean,
    private var mSoundCardSetting: SoundCardSettingBean,
    private var isListener: Boolean, // Whether it is a viewer
    private var currentSong: ChosenSongInfo?, // Current song
) :
    BaseBottomSheetDialogFragment<KtvDialogMusicSettingBinding>() {

    companion object {
        const val TAG = "KtvMusicSettingDialog"
    }

    private var mVoiceEffectAdapter: BaseRecyclerViewAdapter<KtvItemEffectvoiceBinding, EffectVoiceBean, EffectVoiceHolder>? =
        null

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setCancelable(false)
            it.setCanceledOnTouchOutside(false)
            val h = (0.7 * resources.displayMetrics.heightPixels).toInt()
            val bottomSheet =
                (it as BottomSheetDialog).findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { rootLayout ->
                rootLayout.layoutParams.width = -1
                rootLayout.layoutParams.height = h
                val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(rootLayout)
                behavior.isHideable = false
                behavior.peekHeight = h
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.ivBackIcon.setOnClickListener { view -> (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog() }
        // Earback
        if (mSetting.mEarBackEnable) {
            mBinding.switchEar.text = getString(R.string.ktv_open)
        } else {
            mBinding.switchEar.text = getString(R.string.ktv_close)
        }
        mBinding.switchEar.setOnClickListener { v: View -> this.showEarBackPage(v) }

        // Virtual sound card
        if (mSoundCardSetting.isEnable()) {
            mBinding.switchSoundCard.text = getString(R.string.ktv_open)
        } else {
            mBinding.switchSoundCard.text = getString(R.string.ktv_close)
        }
        mBinding.switchSoundCard.setOnClickListener { v: View -> this.showSoundCardPage(v) }

        // Vocal volume
        mBinding.sbMicVol.progress = mSetting.mMicVolume
        mBinding.btMicVolDown.setOnClickListener { v -> tuningMicVolume(false) }
        mBinding.btnMicVolUp.setOnClickListener { v -> tuningMicVolume(true) }
        mBinding.sbMicVol.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mMicVolume = progress
            }
        }

        // Accompaniment volume
        mBinding.sbAccVol.progress = mSetting.mAccVolume
        mBinding.btAccVolDown.setOnClickListener { v -> tuningMusicVolume(false) }
        mBinding.btAccVolUp.setOnClickListener { v -> tuningMusicVolume(true) }
        mBinding.sbAccVol.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mAccVolume = progress
            }
        }

        // Remote volume
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
            enableDisableView(mBinding.layoutRemoteVol, false)
            mBinding.layoutRemoteVol.alpha = 0.3f
        } else {
            enableDisableView(mBinding.layoutAccVol, true)
            mBinding.layoutAccVol.alpha = 1.0f
            enableDisableView(mBinding.layoutRemoteVol, true)
            mBinding.layoutRemoteVol.alpha = 1.0f
        }

        // Sound effect
        setupVoiceEffectAdapter()

        // Scoring difficulty setting
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
            enableDisableView(mBinding.rgVoiceScoringDifficulty, true)
            mBinding.layoutVoiceScoringDifficulty.alpha = 1.0f
        } else {
            enableDisableView(mBinding.rgVoiceScoringDifficulty, false)
            mBinding.layoutVoiceScoringDifficulty.alpha = 0.3f
        }

        // Professional mode
        mBinding.cbStartProfessionalMode.setOnCheckedChangeListener { buttonView, isChecked ->
            mSetting.mProfessionalModeEnable = isChecked
        }
        mBinding.cbStartProfessionalMode.isChecked = mSetting.mProfessionalModeEnable

        // Multi Path
        mBinding.cbMultipath.setOnCheckedChangeListener { buttonView, isChecked ->
            mSetting.mMultiPathEnable = isChecked
        }
        mBinding.cbMultipath.isChecked = mSetting.mMultiPathEnable

        // Audio quality
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

        // Reduce background noise
        when (mSetting.mAinsMode) {
            AINSMode.Medium -> mBinding.rgAINSMode.check(R.id.tvAINSMiddle)
            AINSMode.High -> mBinding.rgAINSMode.check(R.id.tvAINSHigh)
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

        // Low latency mode
        mBinding.cbLowLatency.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked && mSetting.mAinsMode != AINSMode.Close) {
                mBinding.rgAINSMode.check(R.id.tvAINSClose)
            }
            mSetting.mLowLatencyMode = isChecked
        }
        mBinding.cbLowLatency.isChecked = mSetting.mLowLatencyMode

        // AIAEC switch
        mBinding.cbAIAECSwitcher.setOnCheckedChangeListener { buttonView, isChecked ->
            mBinding.layoutAIACStrength.isVisible = isChecked
            mSetting.mAIAECEnable = isChecked
//            if (isChecked) {
//                mBinding.root.postDelayed({
//                    mBinding.scrollView.smoothScrollTo(0, mBinding.layoutContent.measuredHeight)
//                }, 100)
//            }
        }
        mBinding.cbAIAECSwitcher.isChecked = mSetting.mAIAECEnable
        mBinding.layoutAIACStrength.isVisible = mSetting.mAIAECEnable

        // AIAEC
        mBinding.sbAIAEStrength.progress = mSetting.mAIAECStrength
        mBinding.btAIAECDown.setOnClickListener { v -> tuningAIAECStrength(false) }
        mBinding.btAIAECUp.setOnClickListener { v -> tuningAIAECStrength(true) }
        mBinding.sbAIAEStrength.doOnProgressChanged { seekBar, progress, fromUser ->
            if (fromUser) {
                mSetting.mRemoteVolume = progress
            }
        }
    }

    private fun enableDisableView(viewGroup: ViewGroup, enable: Boolean) {
        for (idx in 0 until viewGroup.childCount) {
            viewGroup.getChildAt(idx).isEnabled = enable
        }
    }

    /**
     * Earback setting
     */
    private fun showEarBackPage(v: View) {
        mBinding.root.removeAllViews()
        val earBackFragment = EarBackFragment(mSetting)
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, earBackFragment, EarBackFragment.TAG)
        ft.commit()
    }

    /**
     * Virtual sound card
     */
    private fun showSoundCardPage(v: View) {
        mBinding.root.removeAllViews()
        mBinding.root.setBackgroundResource(R.drawable.ktv_rect_white_top_r20)
        val soundCardFragment = SoundCardFragment(mSoundCardSetting)
        soundCardFragment.onClickSoundCardType = {
            showSoundTypeSelectPage()
        }
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, soundCardFragment, SoundCardFragment.TAG)
        ft.commit()
    }

    /**
     * Preset sound effect
     */
    private fun showSoundTypeSelectPage() {
        mBinding.root.removeAllViews()
        val soundTypeFragment: BaseViewBindingFragment<*> = SoundTypeFragment(mSoundCardSetting)
        val ft = childFragmentManager.beginTransaction()
        ft.add(mBinding.root.id, soundTypeFragment, SoundCardFragment.TAG)
        ft.commit()
    }

    // Vocal volume
    private fun tuningMicVolume(volumeUp: Boolean) {
        var newVocalVolume: Int = this.mSetting.mMicVolume
        if (volumeUp) {
            newVocalVolume += 1
        } else {
            newVocalVolume -= 1
        }
        if (newVocalVolume > 100) newVocalVolume = 100
        if (newVocalVolume < 0) newVocalVolume = 0
        this.mSetting.mMicVolume = newVocalVolume
        mBinding.sbMicVol.progress = newVocalVolume
    }

    // Accompaniment volume
    private fun tuningMusicVolume(volumeUp: Boolean) {
        var newMusicVolume: Int = this.mSetting.mAccVolume
        if (volumeUp) {
            newMusicVolume += 1
        } else {
            newMusicVolume -= 1
        }
        if (newMusicVolume > 100) newMusicVolume = 100
        if (newMusicVolume < 0) newMusicVolume = 0
        this.mSetting.mAccVolume = newMusicVolume
        mBinding.sbAccVol.progress = newMusicVolume
    }

    // Remote volume
    private fun tuningRemoteVolume(volumeUp: Boolean) {
        var newRemoteVolume: Int = this.mSetting.mRemoteVolume
        if (volumeUp) {
            newRemoteVolume += 1
        } else {
            newRemoteVolume -= 1
        }
        if (newRemoteVolume > 100) newRemoteVolume = 100
        if (newRemoteVolume < 0) newRemoteVolume = 0
        this.mSetting.mRemoteVolume = newRemoteVolume
        mBinding.sbRemoteVol.progress = newRemoteVolume
    }

    // AIAEC strength
    private fun tuningAIAECStrength(strengthUp: Boolean) {
        var newAIAECStrength: Int = this.mSetting.mAIAECStrength
        if (strengthUp) {
            newAIAECStrength += 1
        } else {
            newAIAECStrength -= 1
        }
        if (newAIAECStrength > 4) newAIAECStrength = 4
        if (newAIAECStrength < 0) newAIAECStrength = 0
        this.mSetting.mAIAECStrength = newAIAECStrength
        mBinding.sbAIAEStrength.progress = newAIAECStrength
    }

    // Sound effect
    private fun setupVoiceEffectAdapter() {
        val stringArray = resources.getStringArray(R.array.ktv_audioPreset)
        val list: MutableList<EffectVoiceBean> = ArrayList()
        for (i in stringArray.indices) {
            val drawable: Int = if (i % 4 == 0) {
                io.agora.scene.widget.R.mipmap.bg_sound_mode_4
            } else if (i % 3 == 0) {
                io.agora.scene.widget.R.mipmap.bg_sound_mode_3
            } else if (i % 2 == 0) {
                io.agora.scene.widget.R.mipmap.bg_sound_mode_2
            } else {
                io.agora.scene.widget.R.mipmap.bg_sound_mode_1
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
                            for (i in list.indices) {
                                list[i].isSelect = i == position
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


/**
 * Effect voice holder
 *
 * @constructor
 *
 * @param mBinding
 */
class EffectVoiceHolder constructor(mBinding: KtvItemEffectvoiceBinding) :
    BaseViewHolder<KtvItemEffectvoiceBinding, EffectVoiceBean>(mBinding) {
    override fun binding(data: EffectVoiceBean?, selectedIndex: Int) {
        data ?: return
        mBinding.ivBg.setImageResource(data.resId)
        mBinding.tvTitle.text = data.title
        mBinding.select.isVisible = data.isSelect
    }
}

/**
 * Music setting callback
 *
 * @constructor Create empty Music setting callback
 */
interface MusicSettingCallback {
    /**
     * On ear changed
     *
     * @param isEar
     */
    fun onEarChanged(isEar: Boolean)

    /**
     * On ear back volume changed
     *
     * @param volume
     */
    fun onEarBackVolumeChanged(volume: Int)

    /**
     * On ear back mode changed
     *
     * @param mode
     */
    fun onEarBackModeChanged(mode: Int)

    /**
     * On mic vol changed
     *
     * @param vol
     */
    fun onMicVolChanged(vol: Int)

    /**
     * On acc vol changed
     *
     * @param vol
     */
    fun onAccVolChanged(vol: Int)

    /**
     * On remote vol changed
     *
     * @param volume
     */
    fun onRemoteVolChanged(volume: Int)

    /**
     * On audio effect changed
     *
     * @param audioEffect
     */
    fun onAudioEffectChanged(audioEffect: Int)

    /**
     * On scoring difficulty changed
     *
     * @param difficulty
     */
    fun onScoringDifficultyChanged(difficulty: Int)

    /**
     * On professional mode changed
     *
     * @param enable
     */
    fun onProfessionalModeChanged(enable: Boolean)

    /**
     * On multi path changed
     *
     * @param enable
     */
    fun onMultiPathChanged(enable: Boolean)

    /**
     * On aec level changed
     *
     * @param level
     */
    fun onAECLevelChanged(level: Int)

    /**
     * On low latency mode changed
     *
     * @param enable
     */
    fun onLowLatencyModeChanged(enable: Boolean)

    /**
     * On ains mode changed
     *
     * @param mode
     */
    fun onAINSModeChanged(mode: Int)

    /**
     * On aiaec changed
     *
     * @param enable
     */
    fun onAIAECChanged(enable: Boolean)

    /**
     * On aiaec strength select
     *
     * @param strength
     */
    fun onAIAECStrengthSelect(strength: Int)
}
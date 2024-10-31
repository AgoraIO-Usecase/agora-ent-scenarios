package io.agora.scene.cantata.live.fragmentdialog

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.component.BaseRecyclerViewAdapter
import io.agora.scene.base.component.BaseRecyclerViewAdapter.BaseViewHolder
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.base.component.OnItemClickListener
import io.agora.scene.base.utils.dp
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogMusicSettingBinding
import io.agora.scene.cantata.databinding.CantataItemEffectvoiceBinding
import io.agora.scene.cantata.live.bean.EffectVoiceBean
import io.agora.scene.cantata.live.bean.MusicSettingBean
import io.agora.scene.widget.doOnProgressChanged

/**
 * 控制台
 */
class MusicSettingDialog constructor(private val mSetting: MusicSettingBean, private val isPause: Boolean) :
    BaseBottomSheetDialogFragment<CantataDialogMusicSettingBinding>() {

    companion object {
        const val TAG = "MusicSettingDialog"
    }

    private var mEffectAdapter:
            BaseRecyclerViewAdapter<CantataItemEffectvoiceBinding, EffectVoiceBean, EffectVoiceHolder>? = null

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let { window ->
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View?, insets: WindowInsetsCompat ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                mBinding.root.apply {
                    setPadding(inset.left, 0, inset.right, inset.bottom)
                }
                WindowInsetsCompat.CONSUMED
            }
        }
        // 耳返
        if (mSetting.mEarBackEnable) {
            mBinding.switchEar.text = getString(R.string.cantata_switch_open)
        } else {
            mBinding.switchEar.text = getString(R.string.cantata_switch_close)
        }

        mBinding.switchEar.setOnClickListener { v: View -> this.showEarBackPage(v) }

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
                mBinding.cbSwitch.isChecked = progress == 0
            }
        }

        if (isPause) {
            enableDisableView(mBinding.layoutAccVol, false)
            mBinding.layoutAccVol.alpha = 0.3f
            enableDisableView(mBinding.layoutRemoteVol, false)
            mBinding.layoutRemoteVol.alpha  = 0.3f
        } else {
            enableDisableView(mBinding.layoutAccVol, true)
            mBinding.layoutAccVol.alpha = 1.0f
            enableDisableView(mBinding.layoutRemoteVol, true)
            mBinding.layoutRemoteVol.alpha  = 1.0f
        }

        // 音效
        setupVoiceEffectAdapter()

        // 沉浸模式
        mBinding.cbSwitch.isChecked = mSetting.mRemoteVolume == 0
        mBinding.cbSwitch.setOnCheckedChangeListener { buttonView, ischecked ->
            if (!buttonView.isPressed) return@setOnCheckedChangeListener
            if (ischecked) {
                mBinding.sbRemoteVol.progress = 0
                mSetting.mRemoteVolume = 0
            } else {
                mBinding.sbRemoteVol.progress = MusicSettingBean.DEFAULT_REMOTE_VOL
                mSetting.mRemoteVolume = MusicSettingBean.DEFAULT_REMOTE_VOL
            }
        }
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

    // 人声音量
    private fun tuningMicVolume(volumeUp: Boolean) {
        var newVocalVolume = mSetting.mMicVolume
        if (volumeUp) {
            newVocalVolume += 1
        } else {
            newVocalVolume -= 1
        }
        if (newVocalVolume > 100) newVocalVolume = 100
        if (newVocalVolume < 0) newVocalVolume = 0
        mSetting.mMicVolume = newVocalVolume
        mBinding.sbMicVol.progress = newVocalVolume
    }

    // 伴奏音量
    private fun tuningMusicVolume(volumeUp: Boolean) {
        var newMusicVolume = mSetting.mAccVolume
        if (volumeUp) {
            newMusicVolume += 1
        } else {
            newMusicVolume -= 1
        }
        if (newMusicVolume > 100) newMusicVolume = 100
        if (newMusicVolume < 0) newMusicVolume = 0
        mSetting.mAccVolume = newMusicVolume
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
        this.mSetting.mRemoteVolume = newRemoteVolume
        mBinding.sbRemoteVol.progress = newRemoteVolume
    }

    private fun setupVoiceEffectAdapter(){
        val stringArray = resources.getStringArray(R.array.cantata_audioPreset)
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
            val audioEffect: Int = mSetting.getEffectIndex(i)
            list.add(EffectVoiceBean(i, audioEffect, drawable, stringArray[i]))
        }
        for (item in list) {
            item.setSelect(mSetting.mAudioEffect == item.audioEffect)
        }
        mEffectAdapter = BaseRecyclerViewAdapter(
            list, object : OnItemClickListener<EffectVoiceBean> {

                override fun onItemClick(data: EffectVoiceBean, view: View?, position: Int, viewType: Long) {
                    super.onItemClick(data, view, position, viewType)
                    mEffectAdapter?.let {
                        for (i in it.dataList.indices) {
                            it.dataList[i].setSelect(i == position)
                            it.notifyItemChanged(i)
                        }
                    }

                    mSetting.mAudioEffect = data.audioEffect
                }
            },
            EffectVoiceHolder::class.java
        )

        mBinding.rvVoiceEffectList.adapter = mEffectAdapter
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

class EffectVoiceHolder constructor(mBinding: CantataItemEffectvoiceBinding) :
    BaseViewHolder<CantataItemEffectvoiceBinding, EffectVoiceBean>(mBinding) {
    override fun binding(data: EffectVoiceBean?, selectedIndex: Int) {
        data ?: return
        mBinding.ivBg.setImageResource(data.resId)
        mBinding.tvTitle.text = data.title
        mBinding.select.isVisible = data.isSelect
    }
}

/**
 * 控制台设置 callback
 */
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
    fun onRemoteVolumeChanged(volume: Int)

    /**
     * 音效
     */
    fun onAudioEffectChanged(effect: Int)
}
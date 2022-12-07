package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowWidgetMusicEffectDialogBinding
import io.agora.scene.show.databinding.ShowWidgetMusicEffectItem01Binding
import io.agora.scene.show.databinding.ShowWidgetMusicEffectItem02Binding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class MusicEffectDialog(context: Context) : BottomDarkDialog(context) {

    companion object {

        const val GROUP_ID_BACK_MUSIC = 0x00000001 // 背景音乐
        const val ITEM_ID_BACK_MUSIC_NONE = GROUP_ID_BACK_MUSIC
        const val ITEM_ID_BACK_MUSIC_JOY = GROUP_ID_BACK_MUSIC + 1 // 欢乐
        const val ITEM_ID_BACK_MUSIC_ROMANTIC = GROUP_ID_BACK_MUSIC + 2 // 浪漫
        const val ITEM_ID_BACK_MUSIC_JOY2 = GROUP_ID_BACK_MUSIC + 3 // 欢乐2


        const val GROUP_ID_BEAUTY_VOICE = GROUP_ID_BACK_MUSIC shl 8 // 美声
        const val ITEM_ID_BEAUTY_VOICE_ORIGINAL = GROUP_ID_BEAUTY_VOICE + 1 // 原声
        const val ITEM_ID_BEAUTY_VOICE_SWEET = GROUP_ID_BEAUTY_VOICE + 2 // 甜美
        const val ITEM_ID_BEAUTY_VOICE_ZHONGXIN = GROUP_ID_BEAUTY_VOICE + 3 // 中性
        const val ITEM_ID_BEAUTY_VOICE_WENZHONG = GROUP_ID_BEAUTY_VOICE + 4 // 稳重
        const val ITEM_ID_BEAUTY_VOICE_MOHUAN = GROUP_ID_BEAUTY_VOICE + 5 // 魔幻

        const val GROUND_ID_MIXING = GROUP_ID_BEAUTY_VOICE shl 8 // 混响
        const val ITEM_ID_MIXING_NONE = GROUND_ID_MIXING
        const val ITEM_ID_MIXING_KTV = GROUND_ID_MIXING + 1 // KTV
        const val ITEM_ID_MIXING_CONCERT = GROUND_ID_MIXING + 2 // 演唱会
        const val ITEM_ID_MIXING_LUYINPEN = GROUND_ID_MIXING + 3 // 录音棚
        const val ITEM_ID_MIXING_KONGKUANG = GROUND_ID_MIXING + 4 // 空旷
    }

    private data class ItemInfo(val itemId: Int,
                                @StringRes val name: Int,
                                @DrawableRes val icon: Int)

    private var mBackMusicSelectedIndex = -1
    private val mBackMusicItemList = arrayListOf(
        ItemInfo(ITEM_ID_BACK_MUSIC_JOY, R.string.show_music_effect_back_music_joy, R.mipmap.show_music_effect_ic_music),
        ItemInfo(ITEM_ID_BACK_MUSIC_ROMANTIC, R.string.show_music_effect_back_music_romantic, R.mipmap.show_music_effect_ic_music),
        ItemInfo(ITEM_ID_BACK_MUSIC_JOY2, R.string.show_music_effect_back_music_joy2, R.mipmap.show_music_effect_ic_music)
    )

    private var mBeautyVoiceSelectedIndex = 0
    private val mBeautyVoiceItemList = arrayListOf(
        ItemInfo(ITEM_ID_BEAUTY_VOICE_ORIGINAL, R.string.show_music_effect_beauty_voice_original, R.mipmap.show_music_effect_ic_bg_01),
        ItemInfo(ITEM_ID_BEAUTY_VOICE_SWEET, R.string.show_music_effect_beauty_voice_sweet, R.mipmap.show_music_effect_ic_bg_02),
        ItemInfo(ITEM_ID_BEAUTY_VOICE_ZHONGXIN, R.string.show_music_effect_beauty_voice_zhongxin, R.mipmap.show_music_effect_ic_bg_03),
        ItemInfo(ITEM_ID_BEAUTY_VOICE_WENZHONG, R.string.show_music_effect_beauty_voice_wenzhong, R.mipmap.show_music_effect_ic_bg_04),
        ItemInfo(ITEM_ID_BEAUTY_VOICE_MOHUAN, R.string.show_music_effect_beauty_voice_mohuan, R.mipmap.show_music_effect_ic_bg_05)
    )

    private var mMixingSelectedIndex = 0
    private val mMixingItemList = arrayListOf(
        ItemInfo(ITEM_ID_MIXING_NONE, 0, 0),
        ItemInfo(ITEM_ID_MIXING_KTV, R.string.show_music_effect_mixing_ktv, R.mipmap.show_music_effect_ic_bg_06),
        ItemInfo(ITEM_ID_MIXING_CONCERT, R.string.show_music_effect_mixing_concert, R.mipmap.show_music_effect_ic_bg_07),
        ItemInfo(ITEM_ID_MIXING_LUYINPEN, R.string.show_music_effect_mixing_luyinpen, R.mipmap.show_music_effect_ic_bg_08),
        ItemInfo(ITEM_ID_MIXING_KONGKUANG, R.string.show_music_effect_mixing_kongkuang, R.mipmap.show_music_effect_ic_bg_09),
    )

    private val mBinding by lazy {
        ShowWidgetMusicEffectDialogBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }

    private val mBackMusicAdapter: BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem01Binding>
    private val mBeautyVoiceAdapter: BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem02Binding>
    private val mMixingAdapter: BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem02Binding>
    private var onItemSelectedListener: ((MusicEffectDialog, itemId:Int)->Unit)? = null



    init {
        setBottomView(mBinding.root)

        mBackMusicAdapter = object : BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem01Binding>(){
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetMusicEffectItem01Binding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                holder.binding.ivNone.isVisible = item.itemId == ITEM_ID_BACK_MUSIC_NONE
                holder.binding.llOverlay.isActivated = mBackMusicSelectedIndex == position
                holder.binding.tvName.isActivated = mBackMusicSelectedIndex == position
                holder.binding.ivIcon.setImageResource(item.icon)
                holder.binding.tvName.text = if(item.name > 0) context.getString(item.name) else ""
                holder.binding.root.setOnClickListener {
                    if(mBackMusicSelectedIndex == holder.adapterPosition){
                        val oSelectedIndex = mBackMusicSelectedIndex
                        mBackMusicSelectedIndex = -1
                        notifyItemChanged(oSelectedIndex)
                        onItemSelectedListener?.invoke(this@MusicEffectDialog, ITEM_ID_BACK_MUSIC_NONE)
                        return@setOnClickListener
                    }
                    val activate = !holder.binding.llOverlay.isActivated
                    holder.binding.llOverlay.isActivated = activate

                    updateBackMusicPosition(holder.adapterPosition)
                }
            }
        }.apply {
            mBinding.rvBgMusic.adapter = this
            resetAll(mBackMusicItemList)
        }

        mBeautyVoiceAdapter = object : BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem02Binding>(){
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetMusicEffectItem02Binding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                holder.binding.ivOverlay.isActivated = mBeautyVoiceSelectedIndex == position
                holder.binding.tvName.isActivated = mBeautyVoiceSelectedIndex == position
                holder.binding.ivIcon.setImageResource(item.icon)
                holder.binding.tvName.text = if(item.name > 0) context.getString(item.name) else ""
                holder.binding.root.setOnClickListener {
                    if(mBeautyVoiceSelectedIndex == holder.adapterPosition){
                        return@setOnClickListener
                    }
                    val activate = !holder.binding.ivOverlay.isActivated
                    holder.binding.ivOverlay.isActivated = activate

                    updateBeautyVoicePosition(holder.adapterPosition)
                }
            }
        }.apply {
            mBinding.rvVoice.adapter = this
            resetAll(mBeautyVoiceItemList)
        }

        mMixingAdapter = object : BindingSingleAdapter<ItemInfo, ShowWidgetMusicEffectItem02Binding>(){
            override fun onBindViewHolder(
                holder: BindingViewHolder<ShowWidgetMusicEffectItem02Binding>,
                position: Int
            ) {
                val item = getItem(position) ?: return
                holder.binding.ivNone.isVisible = item.itemId == ITEM_ID_MIXING_NONE
                holder.binding.ivOverlay.isActivated = mMixingSelectedIndex == position
                holder.binding.tvName.isActivated = mMixingSelectedIndex == position
                holder.binding.ivIcon.setImageResource(item.icon)
                holder.binding.tvName.text = if(item.name > 0) context.getString(item.name) else ""
                holder.binding.root.setOnClickListener {
                    if(mMixingSelectedIndex == holder.adapterPosition){
                        return@setOnClickListener
                    }
                    val activate = !holder.binding.ivOverlay.isActivated
                    holder.binding.ivOverlay.isActivated = activate

                    updateMixingPosition(holder.adapterPosition)
                }
            }
        }.apply {
            mBinding.rvMixing.adapter = this
            resetAll(mMixingItemList)
        }
    }

    private fun updateBackMusicPosition(position: Int){
        val item = mBackMusicItemList.getOrNull(position) ?: return
        val oSelectedIndex = mBackMusicSelectedIndex
        mBackMusicSelectedIndex = position

        mBackMusicAdapter.notifyItemChanged(oSelectedIndex)
        mBackMusicAdapter.notifyItemChanged(mBackMusicSelectedIndex)
        onItemSelectedListener?.invoke(this@MusicEffectDialog, item.itemId)
    }

    private fun updateBeautyVoicePosition(position: Int){
        val item = mBeautyVoiceItemList.getOrNull(position) ?: return
        val oSelectedIndex = mBeautyVoiceSelectedIndex
        mBeautyVoiceSelectedIndex = position

        mBeautyVoiceAdapter.notifyItemChanged(oSelectedIndex)
        mBeautyVoiceAdapter.notifyItemChanged(mBeautyVoiceSelectedIndex)

        onItemSelectedListener?.invoke(this@MusicEffectDialog, item.itemId)
    }

    private fun updateMixingPosition(position: Int){
        val item = mMixingItemList.getOrNull(position) ?: return
        val oSelectedIndex = mMixingSelectedIndex
        mMixingSelectedIndex = position

        mMixingAdapter.notifyItemChanged(oSelectedIndex)
        mMixingAdapter.notifyItemChanged(mMixingSelectedIndex)

        onItemSelectedListener?.invoke(this@MusicEffectDialog, item.itemId)
    }


    fun setOnItemSelectedListener(listener: (MusicEffectDialog, itemId:Int)->Unit){
        onItemSelectedListener = listener
    }
}
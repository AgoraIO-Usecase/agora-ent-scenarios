package io.agora.scene.show.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.base.utils.dp
import io.agora.scene.show.R
import io.agora.scene.show.databinding.ShowAiPhotographerItemBinding
import io.agora.scene.show.databinding.ShowWidgetAiPhotographerDialogBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class AiPhotographerDialog constructor(context: Context) : BottomDarkDialog(context) {

    private data class ItemInfo constructor(
        val itemId: Int,
        @DrawableRes val icon: Int,
        var status: DownloadStatus = DownloadStatus.None
    )

    private enum class DownloadStatus {
        None,
        Loading,
        Downloaded
    }

    companion object {

        const val GROUP_ID_AI_PHOTOGRAPHER = 0x00000001 // AI 摄影师
        const val ITEM_ID_AI_PHOTOGRAPHER_NONE = GROUP_ID_AI_PHOTOGRAPHER
        const val ITEM_ID_AI_AVATAR = GROUP_ID_AI_PHOTOGRAPHER + 1 // AI 人物形象
        const val ITEM_ID_AI_LIGHTING_AD = GROUP_ID_AI_PHOTOGRAPHER + 2 // 广告灯
        const val ITEM_ID_AI_EDGE_LIGHT = GROUP_ID_AI_PHOTOGRAPHER + 3 // 人物边缘光
        const val ITEM_ID_AI_AURORA = GROUP_ID_AI_PHOTOGRAPHER + 4 // 极光背景
        const val ITEM_ID_AI_SHADOW = GROUP_ID_AI_PHOTOGRAPHER + 5 // AI 光影跟随
        const val ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG = GROUP_ID_AI_PHOTOGRAPHER + 6 // 3D 打光+虚拟背景
        const val ITEM_ID_AI_LIGHTING_3D = GROUP_ID_AI_PHOTOGRAPHER + 7 // AI 3D 打光
        const val ITEM_ID_AI_RHYTHM = GROUP_ID_AI_PHOTOGRAPHER + 8 // AI 律动
    }

    private val mAiPhotographerItemList = arrayListOf(
        ItemInfo(ITEM_ID_AI_AVATAR, R.mipmap.show_ai_avatar,),
        ItemInfo(ITEM_ID_AI_LIGHTING_AD, R.mipmap.show_ai_lighting_ad),
        ItemInfo(ITEM_ID_AI_EDGE_LIGHT, R.mipmap.show_ai_edge_light),
        ItemInfo(ITEM_ID_AI_AURORA, R.mipmap.show_ai_aurora),
        ItemInfo(ITEM_ID_AI_SHADOW,R.mipmap.show_ai_shadow),
        ItemInfo(ITEM_ID_AI_LIGHTING_3D_VIRTUAL_BG,R.mipmap.show_ai_lighting_3d_virtul_bg),
        ItemInfo(ITEM_ID_AI_LIGHTING_3D,R.mipmap.show_ai_lighting_3d),
        ItemInfo(ITEM_ID_AI_RHYTHM,R.mipmap.show_ai_rhythm),
    )

    private var mAiSelectedIndex = -1
    private val mBinding by lazy {
        ShowWidgetAiPhotographerDialogBinding.inflate(LayoutInflater.from(context))
    }

    private val mAiPhotographerAdapter: BindingSingleAdapter<ItemInfo, ShowAiPhotographerItemBinding>

    var onItemSelectedListener: ((AiPhotographerDialog, itemId: Int) -> Unit)? = null

    init {
        setBottomView(mBinding.root)
        mAiPhotographerAdapter = object : BindingSingleAdapter<ItemInfo, ShowAiPhotographerItemBinding>() {
            override fun onBindViewHolder(holder: BindingViewHolder<ShowAiPhotographerItemBinding>, position: Int) {
                val item = getItem(position) ?: return
                holder.binding.ivOverlay.isActivated = position == mAiSelectedIndex
                holder.binding.ivIcon.setImageResource(item.icon)
                when (item.status) {
                    DownloadStatus.Downloaded -> {
                        holder.binding.ivDownload.visibility = View.INVISIBLE
                    }

                    DownloadStatus.Loading -> {
                        holder.binding.ivDownload.setImageResource(R.mipmap.show_ai_assets_loading)
                        holder.binding.ivDownload.visibility = View.VISIBLE
                    }

                    else -> {
                        holder.binding.ivDownload.setImageResource(R.mipmap.show_ai_assets_download)
                        holder.binding.ivDownload.visibility = View.VISIBLE
                    }
                }
                holder.binding.ivDownload.setOnClickListener {
                    if (item.status != DownloadStatus.None) return@setOnClickListener
                    ToastUtils.showToast("开始下载资源 ${holder.adapterPosition}")
                }
                holder.binding.root.setOnClickListener {
                    if (item.status != DownloadStatus.Downloaded) return@setOnClickListener
                    if (mAiSelectedIndex == holder.adapterPosition) {
                        val oSelectedIndex = mAiSelectedIndex
                        mAiSelectedIndex = -1
                        notifyItemChanged(oSelectedIndex)
                        onItemSelectedListener?.invoke(
                            this@AiPhotographerDialog,
                            ITEM_ID_AI_PHOTOGRAPHER_NONE
                        )
                        return@setOnClickListener
                    }
                    val activate = !holder.binding.ivOverlay.isActivated
                    holder.binding.ivOverlay.isActivated = activate

                    updateAiPhotographerPosition(holder.adapterPosition)
                }
            }
        }.apply {
            mBinding.rvAiPhotographer.adapter = this
            mBinding.rvAiPhotographer.addItemDecoration(MaterialDividerItemDecoration(context,
                MaterialDividerItemDecoration.VERTICAL).apply {
                dividerThickness = 24.dp.toInt()
                dividerColor =  ResourcesCompat.getColor(context.resources, android.R.color.transparent, null)
            })
            resetAll(mAiPhotographerItemList)

        }
    }

    private fun updateAiPhotographerPosition(position: Int){
        val item = mAiPhotographerItemList.getOrNull(position) ?: return
        val oSelectedIndex = mAiSelectedIndex
        mAiSelectedIndex = position

        mAiPhotographerAdapter.notifyItemChanged(oSelectedIndex)
        mAiPhotographerAdapter.notifyItemChanged(mAiSelectedIndex)

        onItemSelectedListener?.invoke(this@AiPhotographerDialog, item.itemId)
    }
}
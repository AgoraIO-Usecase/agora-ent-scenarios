package io.agora.scene.voice.spatial.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.spatial.R
import io.agora.scene.voice.spatial.databinding.VoiceSpatialDialogRoomSoundSelectionBinding
import io.agora.scene.voice.spatial.databinding.VoiceSpatialItemRoomSoundSelectionBinding
import io.agora.scene.voice.spatial.model.SoundSelectionBean
import io.agora.scene.voice.spatial.model.constructor.RoomSoundSelectionConstructor
import io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomSoundSelectionFooterViewHolder
import io.agora.scene.voice.spatial.ui.adapter.viewholder.RoomSoundSelectionViewHolder
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.utils.ToastTools

class RoomSoundSelectionSheetDialog constructor(
    private val isEnable: Boolean = true,
    private val soundSelectionListener: OnClickSoundSelectionListener
) :
    BaseFixedHeightSheetDialog<VoiceSpatialDialogRoomSoundSelectionBinding>() {

    companion object {
        const val KEY_CURRENT_SELECTION = "current_selection"
        const val KEY_IS_ENABLE = "is_enable"
    }

    private var soundSelectionAdapter: BaseRecyclerViewAdapter<VoiceSpatialItemRoomSoundSelectionBinding, SoundSelectionBean, RoomSoundSelectionViewHolder>? =
        null

    private val soundSelectionList = mutableListOf<SoundSelectionBean>()


    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceSpatialDialogRoomSoundSelectionBinding {
        return VoiceSpatialDialogRoomSoundSelectionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        dialog?.setCanceledOnTouchOutside(false)
        arguments?.apply {
            val currentSelection: Int = getInt(KEY_CURRENT_SELECTION)
            soundSelectionList.addAll(
                RoomSoundSelectionConstructor.builderSoundSelectionList(view.context, currentSelection)
            )
        }

        binding?.apply {
            setOnApplyWindowInsets(root)
            ivBottomSheetBack.setOnClickListener {
                onHandleOnBackPressed()
            }
            initAdapter(rvBottomSheetSoundSelection)
        }
    }

    private fun initAdapter(recyclerView: RecyclerView) {
        soundSelectionAdapter =
            BaseRecyclerViewAdapter(
                soundSelectionList,
                object :
                    OnItemClickListener<SoundSelectionBean> {

                    override fun onItemClick(data: SoundSelectionBean, view: View, position: Int, viewType: Long) {
                        if (isEnable) {
                            soundSelectionListener.onSoundEffect(data, data.isCurrentUsing)
                        } else {
                            activity?.let {
                                ToastTools.showTips(
                                    it,
                                    getString(R.string.voice_chatroom_only_host_can_change_best_sound)
                                )
                            }
                        }
                    }
                },
                RoomSoundSelectionViewHolder::class.java
            )
        val footerList = mutableListOf(recyclerView.context.getString(R.string.voice_chatroom_sound_selection_more))
        val footerAdapter = BaseRecyclerViewAdapter(
            footerList,
            null,
            object : OnItemChildClickListener<String> {
                override fun onItemChildClick(
                    data: String?,
                    extData: Any?,
                    view: View,
                    position: Int,
                    itemViewType: Long
                ) {
//                if (extData is String) {
//                    val intent = Intent()
//                        .setAction("android.intent.action.VIEW")
//                        .setData(Uri.parse(extData))
//                    startActivity(intent)
//                }
                }
            },
            RoomSoundSelectionFooterViewHolder::class.java
        )
        val config = ConcatAdapter.Config.Builder().setIsolateViewTypes(true).build()
        val concatAdapter = ConcatAdapter(config, soundSelectionAdapter, footerAdapter)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = concatAdapter
    }

    interface OnClickSoundSelectionListener {
        fun onSoundEffect(soundSelection: SoundSelectionBean, isCurrentUsing: Boolean)
    }
}
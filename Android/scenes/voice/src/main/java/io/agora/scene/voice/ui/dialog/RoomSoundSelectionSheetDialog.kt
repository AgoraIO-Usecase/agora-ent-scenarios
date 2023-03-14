package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.model.SoundSelectionBean
import io.agora.voice.common.ui.adapter.BaseRecyclerViewAdapter
import io.agora.voice.common.ui.adapter.listener.OnItemChildClickListener
import io.agora.voice.common.ui.adapter.listener.OnItemClickListener
import io.agora.voice.common.ui.dialog.BaseFixedHeightSheetDialog
import io.agora.voice.common.utils.ToastTools
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogRoomSoundSelectionBinding
import io.agora.scene.voice.databinding.VoiceItemRoomSoundSelectionBinding
import io.agora.scene.voice.model.constructor.RoomSoundSelectionConstructor
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionFooterViewHolder
import io.agora.scene.voice.ui.soundselection.RoomSoundSelectionViewHolder

class RoomSoundSelectionSheetDialog constructor(
    private val isEnable: Boolean = true,
    private val soundSelectionListener: OnClickSoundSelectionListener
) :
    BaseFixedHeightSheetDialog<VoiceDialogRoomSoundSelectionBinding>() {

    companion object {
        const val KEY_CURRENT_SELECTION = "current_selection"
        const val KEY_IS_ENABLE = "is_enable"
    }

    private var soundSelectionAdapter: BaseRecyclerViewAdapter<VoiceItemRoomSoundSelectionBinding, SoundSelectionBean, RoomSoundSelectionViewHolder>? =
        null

    private val soundSelectionList = mutableListOf<SoundSelectionBean>()


    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogRoomSoundSelectionBinding {
        return VoiceDialogRoomSoundSelectionBinding.inflate(inflater, container, false)
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
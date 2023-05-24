package io.agora.scene.voice.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogChatroomBgmSettingBinding
import io.agora.scene.voice.databinding.VoiceRoomMusicItemLayoutBinding
import io.agora.scene.voice.model.SoundSelectionBean
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class RoomBGMSettingSheetDialog: BaseSheetDialog<VoiceDialogChatroomBgmSettingBinding>() {

    companion object {
        const val KEY_IS_ON = "isOn"
    }

    private val isOn by lazy {
        arguments?.getBoolean(RoomAIAECSheetDialog.KEY_IS_ON, true) ?: true
    }

    private lateinit var listAdapter: ListAdapter<SoundSelectionBean, BindingViewHolder<VoiceRoomMusicItemLayoutBinding>>

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceDialogChatroomBgmSettingBinding {
        return VoiceDialogChatroomBgmSettingBinding.inflate(inflater, container, false)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.attributes?.windowAnimations = R.style.voice_BottomSheetDialogAnimation
        setupListAdapter()
        fetchData()
    }

    private fun fetchData() {
        AgoraRtcEngineController.get().bgmManager.fetchBGMList {

        }
    }

    private fun setupListAdapter() {
        listAdapter = object : ListAdapter<SoundSelectionBean, BindingViewHolder<VoiceRoomMusicItemLayoutBinding>>(object : DiffUtil.ItemCallback<SoundSelectionBean>() {
            override fun areItemsTheSame(
                oldItem: SoundSelectionBean,
                newItem: SoundSelectionBean
            ): Boolean {
                return false
            }

            override fun areContentsTheSame(
                oldItem: SoundSelectionBean,
                newItem: SoundSelectionBean
            ): Boolean {
                return false
            }
        }) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BindingViewHolder<VoiceRoomMusicItemLayoutBinding> {
                val binding = VoiceRoomMusicItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context)
                )
                return BindingViewHolder(binding)
            }

            override fun onBindViewHolder(
                holder: BindingViewHolder<VoiceRoomMusicItemLayoutBinding>,
                position: Int
            ) {

            }
        }
        binding?.rvMusicList?.adapter = listAdapter
    }
}

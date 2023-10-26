package io.agora.scene.voice.ui.dialog.soundcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.rtckit.AgoraPresetSound
import io.agora.scene.voice.rtckit.PresetSoundModel
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogSoundPresetTypeBinding
import io.agora.scene.voice.rtckit.AgoraRtcEngineController
import io.agora.scene.voice.rtckit.AgoraSoundCardManager
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class SoundPresetTypeDialog: BaseSheetDialog<VoiceDialogSoundPresetTypeBinding>() {

    companion object {
        const val TAG: String = "SoundTypeFragment"
    }

    private var adapter: SoundPresetsAdapter? = null

    private lateinit var mManager: AgoraSoundCardManager

    private lateinit var soundTypes: List<PresetSoundModel>

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogSoundPresetTypeBinding {
        return VoiceDialogSoundPresetTypeBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mManager = AgoraRtcEngineController.get().soundCardManager() ?: run {
            dismiss()
            return
        }
        setupSoundTypes()
        binding?.let { binding ->
            binding.rvSoundType.layoutManager = LinearLayoutManager(this.context)
            var index = -1
            for ((i, item) in soundTypes.withIndex()) {
                if (item.type == mManager.presetSound()) {
                    index = i
                }
            }
            adapter = SoundPresetsAdapter(soundTypes, index) { index ->
                val soundType = soundTypes[index].type
                mManager.setPresetSound(soundType) {}
                adapter?.notifyDataSetChanged()
            }
            binding.rvSoundType.adapter = adapter
            binding.btnClose.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun setupSoundTypes() {
        soundTypes = listOf(
            PresetSoundModel(
                AgoraPresetSound.Sound2001,
                getString(R.string.voice_preset_sound_dashu),
                getString(R.string.voice_preset_sound_dashu_tips),
                R.drawable.voice_ic_sound_card_2001
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2002,
                getString(R.string.voice_preset_sound_mum),
                getString(R.string.voice_preset_sound_mum_tips),
                R.drawable.voice_ic_sound_card_2002
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2003,
                getString(R.string.voice_preset_sound_qingshu),
                getString(R.string.voice_preset_sound_qingshu_tips),
                R.drawable.voice_ic_sound_card_2003
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2004,
                getString(R.string.voice_preset_sound_yuma),
                getString(R.string.voice_preset_sound_yuma_tips),
                R.drawable.voice_ic_sound_card_2004
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2005,
                getString(R.string.voice_preset_sound_qingnian),
                getString(R.string.voice_preset_sound_qingnian_tips),
                R.drawable.voice_ic_sound_card_2005
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2006,
                getString(R.string.voice_preset_sound_shaoyu),
                getString(R.string.voice_preset_sound_shaoyu_tips),
                R.drawable.voice_ic_sound_card_2006
            )
        )
    }
}

private class SoundPresetsAdapter(
    private var list: List<PresetSoundModel>,
    private var selectedIndex: Int,
    private var onDidSelectIndex: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<SoundPresetsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
        val tvPresetName: TextView = itemView.findViewById(R.id.tvPresetName)
        val tvPresetInfo: TextView = itemView.findViewById(R.id.tvPresetInfo)
        val checkBox: ImageView = itemView.findViewById(R.id.ivCheckBox)
        val borderView: View = itemView.findViewById(R.id.cvCornerRatio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.voice_dialog_sound_type_item_layout, parent, false)
        return ViewHolder(view)
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.tvPresetName.text = data.name
        holder.tvPresetInfo.text = data.tips
        holder.checkBox.visibility = if (selectedIndex == position) View.VISIBLE else View.INVISIBLE
        holder.borderView.visibility = if (selectedIndex == position) View.VISIBLE else View.INVISIBLE
        holder.ivAvatar.setImageResource(data.resId)
        holder.itemView.setOnClickListener {
            selectedIndex = position
            onDidSelectIndex?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
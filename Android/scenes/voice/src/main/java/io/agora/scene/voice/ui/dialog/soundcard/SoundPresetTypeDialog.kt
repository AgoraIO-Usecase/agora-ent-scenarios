package io.agora.scene.voice.ui.dialog.soundcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.voice.R
import io.agora.scene.voice.databinding.VoiceDialogSoundPresetTypeBinding
import io.agora.scene.voice.rtckit.*
import io.agora.voice.common.ui.dialog.BaseSheetDialog

class SoundPresetTypeDialog: BaseSheetDialog<VoiceDialogSoundPresetTypeBinding>() {

    companion object {
        const val TAG: String = "SoundTypeFragment"
    }

    private var adapter: SoundPresetsAdapter? = null

    private var mOnSoundTypeChange: (() -> Unit)? = null

    private lateinit var mManager: AgoraSoundCardManager

    private val soundTypes = listOf(
        AgoraPresetSound.Sound1001,
        AgoraPresetSound.Sound1002,
        AgoraPresetSound.Sound1003,
        AgoraPresetSound.Sound1004,
        AgoraPresetSound.Sound2001,
        AgoraPresetSound.Sound2002,
        AgoraPresetSound.Sound2003,
        AgoraPresetSound.Sound2004,
        AgoraPresetSound.Sound2005,
        AgoraPresetSound.Sound2006,
    )

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VoiceDialogSoundPresetTypeBinding {
        return VoiceDialogSoundPresetTypeBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mManager = AgoraRtcEngineController.get().soundCardManager() ?: run {
            dismiss()
            return
        }
        binding?.let { binding ->
            binding.rvSoundType.layoutManager = LinearLayoutManager(this.context)
            var index = -1
            for ((i, item) in soundTypes.withIndex()) {
                if (item == mManager.presetSound()) {
                    index = i
                }
            }
            adapter = SoundPresetsAdapter(soundTypes, index) { index ->
                val soundType = soundTypes[index]
                mManager.setPresetSound(soundType) {}
                adapter?.notifyDataSetChanged()
            }
            binding.rvSoundType.adapter = adapter
            binding.btnClose.setOnClickListener {
                dismiss()
            }
        }
    }
    fun setOnSoundTypeChange(action: (() -> Unit)?) {
        mOnSoundTypeChange = action
    }
}
private class SoundPresetsAdapter(
    private var list: List<AgoraPresetSound>,
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
        holder.tvPresetName.text = holder.itemView.context.getString(data.titleStringID)
        holder.tvPresetInfo.text = holder.itemView.context.getString(data.infoStringID)
        holder.checkBox.visibility = if (selectedIndex == position) View.VISIBLE else View.INVISIBLE
        holder.borderView.visibility = if (selectedIndex == position) View.VISIBLE else View.INVISIBLE
        holder.ivAvatar.setImageResource(data.resID)
        holder.itemView.setOnClickListener {
            selectedIndex = position
            onDidSelectIndex?.invoke(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
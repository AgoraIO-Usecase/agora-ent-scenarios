package io.agora.scene.ktv.live.fragmentdialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogSoundTypeBinding
import io.agora.scene.ktv.live.RoomLivingActivity
import io.agora.scene.ktv.live.bean.SoundCardSettingBean

/**
 * Sound type fragment
 *
 * @property soundCardSetting
 * @constructor Create empty Sound type fragment
 */
class SoundTypeFragment constructor(private val soundCardSetting: SoundCardSettingBean) :
    BaseViewBindingFragment<KtvDialogSoundTypeBinding>() {

    companion object {
        const val TAG: String = "SoundTypeFragment"
    }

    private var adapter: SoundPresetsAdapter? = null

    private lateinit var soundTypes: List<PresetSoundModel>

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): KtvDialogSoundTypeBinding {
        return KtvDialogSoundTypeBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

        setupSoundTypes()
        binding.rvSoundType.layoutManager = LinearLayoutManager(this.context)

        var index = -1
        for ((i, item) in soundTypes.withIndex()) {
            if (item.type == soundCardSetting.presetSound()) {
                index = i
            }
        }
        adapter = SoundPresetsAdapter(soundTypes, index) { index ->
            val soundType = soundTypes[index].type
            soundCardSetting.setPresetSound(soundType) {}
            adapter?.notifyDataSetChanged()
        }
        binding.rvSoundType.adapter = adapter
        binding.btnClose.setOnClickListener {
            (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog()
        }
    }

    private fun setupSoundTypes() {
        soundTypes = listOf(
            PresetSoundModel(
                AgoraPresetSound.Sound2001,
                getString(R.string.ktv_preset_sound_dashu),
                getString(R.string.ktv_preset_sound_dashu_tips),
                R.mipmap.ktv_ic_sound_card_2001
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2002,
                getString(R.string.ktv_preset_sound_mum),
                getString(R.string.ktv_preset_sound_mum_tips),
                R.mipmap.ktv_ic_sound_card_2002
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2003,
                getString(R.string.ktv_preset_sound_qingshu),
                getString(R.string.ktv_preset_sound_qingshu_tips),
                R.mipmap.ktv_ic_sound_card_2003
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2004,
                getString(R.string.ktv_preset_sound_yuma),
                getString(R.string.ktv_preset_sound_yuma_tips),
                R.mipmap.ktv_ic_sound_card_2004
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2005,
                getString(R.string.ktv_preset_sound_qingnian),
                getString(R.string.ktv_preset_sound_qingnian_tips),
                R.mipmap.ktv_ic_sound_card_2005
            ),
            PresetSoundModel(
                AgoraPresetSound.Sound2006,
                getString(R.string.ktv_preset_sound_shaoyu),
                getString(R.string.ktv_preset_sound_shaoyu_tips),
                R.mipmap.ktv_ic_sound_card_2006
            )
        )
    }

    private class SoundPresetsAdapter(
        private var list: List<PresetSoundModel>,
        private var selectedIndex: Int,
        private var onDidSelectIndex: ((Int) -> Unit)? = null
    ) : RecyclerView.Adapter<SoundPresetsAdapter.ViewHolder>() {

        /**
         * View holder
         *
         * @constructor
         *
         * @param itemView
         */
        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivAvatar: ImageView = itemView.findViewById(R.id.ivAvatar)
            val tvPresetName: TextView = itemView.findViewById(R.id.tvPresetName)
            val tvPresetInfo: TextView = itemView.findViewById(R.id.tvPresetInfo)
            val checkBox: ImageView = itemView.findViewById(R.id.ivCheckBox)
            val borderView: View = itemView.findViewById(R.id.cvCornerRatio)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.ktv_dialog_sound_type_layout, parent, false)
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
}
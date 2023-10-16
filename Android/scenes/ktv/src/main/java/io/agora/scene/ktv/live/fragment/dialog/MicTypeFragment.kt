package io.agora.scene.ktv.live.fragment.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.BaseViewBindingFragment
import io.agora.scene.ktv.R
import io.agora.scene.ktv.databinding.KtvDialogMicTypeBinding
import io.agora.scene.ktv.live.RoomLivingActivity

class MicTypeFragment constructor(private val soundCardSetting: SoundCardSettingBean) :
    BaseViewBindingFragment<KtvDialogMicTypeBinding>() {

    companion object {
        const val TAG: String = "MicTypeFragment"
    }

    private var adapter: MicTypesAdapter? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): KtvDialogMicTypeBinding {
        return KtvDialogMicTypeBinding.inflate(inflater)
    }

    override fun initView() {
        super.initView()

        binding.rvMicType.layoutManager = LinearLayoutManager(this.context)
        adapter = MicTypesAdapter(soundCardSetting.presetValue()) { value ->
            soundCardSetting.setPresetValue(value)
            adapter?.notifyDataSetChanged()
        }
        binding.rvMicType.adapter = adapter
        binding.btnClose.setOnClickListener {
            (requireActivity() as RoomLivingActivity).closeMusicSettingsDialog()
        }
    }
}

private class MicTypesAdapter(
    private var selectedValue: Int,
    private var onDidSelectIndex: ((Int) -> Unit)? = null
    ) : RecyclerView.Adapter<MicTypesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tvMicType)
        val divider: View = itemView.findViewById(R.id.dvBottom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ktv_dialog_mic_type_layout, parent, false)
        return ViewHolder(view)
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val value = position + 1
        if (position == 4) {
            holder.divider.visibility = View.GONE
        }
        holder.textView.text = holder.itemView.context.getString(R.string.ktv_sound_card_mic_type, value)
        holder.itemView.setOnClickListener {
            selectedValue = value
            onDidSelectIndex?.invoke(value)
        }
        if (value == selectedValue) {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.bg_color_blue_31))
        } else {
            holder.textView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }
    }

    // 返回数据源的项数
    override fun getItemCount(): Int {
        return 5
    }
}
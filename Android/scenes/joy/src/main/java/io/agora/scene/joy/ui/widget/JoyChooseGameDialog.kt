package io.agora.scene.joy.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyDialogChooseGameLayoutBinding
import io.agora.scene.joy.databinding.JoyDialogGiftLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGameChooseLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGiftLayoutBinding
import io.agora.scene.joy.network.JoyGameEntity
import io.agora.scene.joy.service.JoyRoomInfo
import io.agora.scene.joy.ui.JoyViewModel

class JoyChooseGameDialog constructor(val mJoyViewModel: JoyViewModel):
    BaseBottomSheetDialogFragment<JoyDialogChooseGameLayoutBinding>() {

    companion object {
        const val Key_Content = "key_content"
    }

    private val mContent by lazy {
        arguments?.getString(Key_Content) ?: ""
    }

    private var mChooseGameAdapter: JoyChooseGameAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.btnConfirm.setOnClickListener {
            dismiss()
            ToastUtils.showToast("confirm")
        }
        mChooseGameAdapter = JoyChooseGameAdapter(emptyList(), 0, onDidSelectIndex = {
            ToastUtils.showToast("select $it")
        })
        mBinding.rvGame.adapter = mChooseGameAdapter

        mJoyViewModel.getGames()
        mJoyViewModel.mGameEntityList.observe(this) {
            val en = mutableListOf<JoyGameEntity>()
            en.addAll(it)
            en.addAll(it)
            en.addAll(it)
            en.addAll(it)
            en.addAll(it)
            mChooseGameAdapter?.setDataList(en)
        }
    }
}

private class JoyChooseGameAdapter constructor(
    private var mList: List<JoyGameEntity>,
    private var selectedIndex: Int,
    private var onDidSelectIndex: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<JoyChooseGameAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: JoyItemGameChooseLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemGameChooseLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    fun setDataList(list: List<JoyGameEntity>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = mList[position]
        holder.binding.ivGame.setImageResource(R.mipmap.joy_img_room_item_bg_1)
        holder.binding.tvGameName.text = data.name
        val context = holder.binding.root.context
        if (selectedIndex == position) {
            holder.binding.tvGameName.setBackgroundColor(
                ResourcesCompat.getColor(context.resources, R.color.joy_color_2F7, null)
            )
            holder.binding.cvGame.strokeColor = ResourcesCompat.getColor(context.resources, R.color.joy_color_2F7, null)
        } else {
            holder.binding.tvGameName.setBackgroundColor(
                ResourcesCompat.getColor(context.resources, android.R.color.transparent, null)
            )
            holder.binding.cvGame.strokeColor = ResourcesCompat.getColor(context.resources, R.color.white, null)
        }
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onDidSelectIndex?.invoke(position)
        }
    }
}
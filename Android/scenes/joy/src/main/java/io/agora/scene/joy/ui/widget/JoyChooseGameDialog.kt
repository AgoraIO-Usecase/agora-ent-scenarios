package io.agora.scene.joy.ui.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyDialogChooseGameLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGameChooseLayoutBinding
import io.agora.scene.joy.network.JoyGameListResult


class JoyChooseGameDialog constructor(var gamList: List<JoyGameListResult>, var completion: (game: JoyGameListResult) -> Unit) :
    BaseBottomSheetDialogFragment<JoyDialogChooseGameLayoutBinding>() {

    companion object {
        const val Key_Content = "key_content"
    }

    private val mContent by lazy {
        arguments?.getString(Key_Content) ?: ""
    }

    private var mChooseGameAdapter: JoyChooseGameAdapter? = null

    override fun onStart() {
        super.onStart()
        dialog?.let {
            it.setCanceledOnTouchOutside(false)
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(it.findViewById(R.id.design_bottom_sheet))
            behavior.setHideable(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEnableConfirm(true)
        mBinding.btnConfirm.setOnClickListener {
            setEnableConfirm(false)
            ToastUtils.showToast("confirm")
            mChooseGameAdapter?.apply {
                val game = gamList[selectedIndex]
                completion.invoke(game)
            }

        }
        mChooseGameAdapter = JoyChooseGameAdapter(gamList, 0)
        mBinding.rvGame.adapter = mChooseGameAdapter
    }

    fun setEnableConfirm(isEnable: Boolean) {
        mBinding.btnConfirm.isEnabled = isEnable
    }
}

private class JoyChooseGameAdapter constructor(
    var mList: List<JoyGameListResult>,
    var selectedIndex: Int,
) : RecyclerView.Adapter<JoyChooseGameAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: JoyItemGameChooseLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemGameChooseLayoutBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    fun setDataList(list: List<JoyGameListResult>) {
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
            holder.binding.tvGameName.setTextColor(
                ResourcesCompat.getColor(context.resources, R.color.white, null)
            )
            holder.binding.layoutGame.setBackgroundResource(R.drawable.joy_bg_item_choose_game)
        } else {
            holder.binding.tvGameName.setTextColor(
                ResourcesCompat.getColor(context.resources, R.color.def_text_color_040, null)
            )
            holder.binding.layoutGame.setBackgroundResource(R.color.white)
        }
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
        }
    }
}
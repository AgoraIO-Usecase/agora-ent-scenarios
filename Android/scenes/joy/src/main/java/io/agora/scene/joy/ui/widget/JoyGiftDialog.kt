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
import io.agora.scene.joy.databinding.JoyDialogGiftLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGiftLayoutBinding
import io.agora.scene.joy.network.JoyGiftEntity

class JoyGiftDialog : BaseBottomSheetDialogFragment<JoyDialogGiftLayoutBinding>() {

    companion object {
        const val Key_Gifts = "key_gifts"
    }

    private val mGiftList by lazy {
        arguments?.getSerializable(Key_Gifts) as List<JoyGiftEntity>
    }

    private val mGiftAdapter: JoyGiftAdapter by lazy {
        JoyGiftAdapter(mGiftList, 0, onDidSelectIndex = {
        })
    }

    private var mGiftCount = 1

    var mSelectedCompletion: ((game: JoyGiftEntity, count: Int) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvSend.setOnClickListener {
            dismiss()
            mSelectedCompletion?.invoke(mGiftAdapter.selectGift, mGiftCount)
        }

        mBinding.rvGift.adapter = mGiftAdapter
        setupGiftCountView()
        mBinding.ivSubCount.setOnClickListener {
            if (mGiftCount > 1) {
                mGiftCount--
            }
            setupGiftCountView()
        }
        mBinding.ivPlusCount.setOnClickListener {
            mGiftCount++
            setupGiftCountView()
        }
    }

    private fun setupGiftCountView() {
        mBinding.tvGiftCount.text = "$mGiftCount"
        if (mGiftCount > 1) {
            mBinding.ivSubCount.isEnabled = true
            mBinding.ivSubCount.setImageResource(R.drawable.joy_icon_sub)
        } else {
            mBinding.ivSubCount.isEnabled = false
            mBinding.ivSubCount.setImageResource(R.drawable.joy_icon_sub_grey)
        }
        if (mGiftCount >= 20) {
            mBinding.ivPlusCount.isEnabled = false
            mBinding.ivPlusCount.setImageResource(R.drawable.joy_icon_plus_grey)
        } else {
            mBinding.ivPlusCount.isEnabled = true
            mBinding.ivPlusCount.setImageResource(R.drawable.joy_icon_plus)
        }
    }
}

private class JoyGiftAdapter constructor(
    private var list: List<JoyGiftEntity>,
    var selectedIndex: Int,
    private var onDidSelectIndex: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<JoyGiftAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: JoyItemGiftLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemGiftLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    val selectGift: JoyGiftEntity
        get() = list[selectedIndex]

    override fun getItemCount(): Int {
        return list.size
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        val context = AgoraApplication.the()
        var resourceId: Int
        try {
            resourceId = context.resources.getIdentifier(
                "joy_icon_gift${position + 1}", "drawable", context.packageName
            )
        } catch (e: Exception) {
            resourceId = R.drawable.joy_icon_gift1
        }

        holder.binding.ivGift.setImageResource(resourceId)
        holder.binding.tvGiftName.text = data.name
        if (selectedIndex == position) {
            holder.binding.itemLayout.setBackgroundResource(R.drawable.joy_bg_gift_selected)
        } else {
            holder.binding.itemLayout.setBackgroundColor(
                ResourcesCompat.getColor(AgoraApplication.the().resources, android.R.color.transparent, null)
            )
        }
        holder.itemView.setOnClickListener {
            selectedIndex = position
            notifyDataSetChanged()
            onDidSelectIndex?.invoke(position)
        }
    }
}
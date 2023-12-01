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

class JoyGiftDialog : BaseBottomSheetDialogFragment<JoyDialogGiftLayoutBinding>() {

    companion object {
        const val Key_Content = "key_content"
    }

    private val mContent by lazy {
        arguments?.getString(Key_Content) ?: ""
    }

    private val mGiftList: List<JoyGift> by lazy {
        mutableListOf(
            JoyGift(R.drawable.joy_icon_gift1, "爱心"),
            JoyGift(R.drawable.joy_icon_gift2, "鲜花"),
            JoyGift(R.drawable.joy_icon_gift3, "小兔子"),
            JoyGift(R.drawable.joy_icon_gift4, "金拱门"),
            JoyGift(R.drawable.joy_icon_gift5, "钻戒"),
            JoyGift(R.drawable.joy_icon_gift6, "火箭"),
        )
    }

    private var mGiftAdapter: JoyGiftAdapter? = null

    private var mGiftCount = 1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvSend.setOnClickListener {
            dismiss()
            ToastUtils.showToast("send")
        }
        mGiftAdapter = JoyGiftAdapter(mGiftList, 0, onDidSelectIndex = {
            ToastUtils.showToast("select $it")
        })
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
    }
}

private class JoyGiftAdapter constructor(
    private var list: List<JoyGift>,
    private var selectedIndex: Int,
    private var onDidSelectIndex: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<JoyGiftAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: JoyItemGiftLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            JoyItemGiftLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    // 将数据绑定到视图项
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.ivGift.setImageResource(data.drawableRes)
        holder.binding.tvGiftName.text = data.giftName
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

data class JoyGift constructor(
    val drawableRes: Int,
    val giftName: String
)
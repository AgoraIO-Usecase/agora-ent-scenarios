package io.agora.scene.joy.ui.widget

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import io.agora.scene.base.component.AgoraApplication
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.base.utils.ToastUtils
import io.agora.scene.joy.R
import io.agora.scene.joy.databinding.JoyDialogGiftLayoutBinding
import io.agora.scene.joy.databinding.JoyItemGiftLayoutBinding
import io.agora.scene.joy.dp

class JoyGiftDialog : BaseBottomSheetDialogFragment<JoyDialogGiftLayoutBinding>() {

    companion object {
        const val Key_Content = "key_content"
    }

    private val mContent by lazy {
        arguments?.getString(Key_Content) ?: ""
    }

    private val mGiftList: List<JoyGift> by lazy {
        mutableListOf(
            JoyGift(R.drawable.joy_icon_gift1,"爱心"),
            JoyGift(R.drawable.joy_icon_gift1,"鲜花"),
            JoyGift(R.drawable.joy_icon_gift1,"小兔子"),
            JoyGift(R.drawable.joy_icon_gift1,"金拱门"),
            JoyGift(R.drawable.joy_icon_gift1,"钻戒"),
            JoyGift(R.drawable.joy_icon_gift1,"火箭"),
        )
    }

    private var mGiftAdapter: JoyGiftAdapter?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.tvSend.setOnClickListener {
            dismiss()
            ToastUtils.showToast("send")
        }
        mGiftAdapter = JoyGiftAdapter(mGiftList,0, onDidSelectIndex = {
            ToastUtils.showToast("select $it")
        })
        mBinding.rvGift.adapter = mGiftAdapter

        //设置item 间距
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable()
        drawable.setSize(0, 8.dp.toInt())
        mBinding.rvGift.addItemDecoration(itemDecoration)
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
            holder.binding.root.setBackgroundResource(R.drawable.joy_bg_gift_selected)
        } else {
            holder.binding.root.setBackgroundColor(
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

data class JoyGift(
    val drawableRes: Int,
    val giftName: String
)
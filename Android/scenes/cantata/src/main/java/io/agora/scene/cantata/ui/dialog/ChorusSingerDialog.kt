package io.agora.scene.cantata.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import io.agora.scene.base.GlideApp
import io.agora.scene.base.component.BaseBottomSheetDialogFragment
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataDialogSingingUserBinding
import io.agora.scene.cantata.databinding.CantataItemSingingUserBinding
import io.agora.scene.cantata.service.RoomSeatModel
import io.agora.scene.cantata.service.RoomSelSongModel
import io.agora.scene.cantata.ui.widget.OnClickJackingListener
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform
import java.text.DecimalFormat
import java.text.NumberFormat

class ChorusSingerDialog constructor(
    private val selSongModel: RoomSelSongModel?,
    private val seatModelList: List<RoomSeatModel>
) :
    BaseBottomSheetDialogFragment<CantataDialogSingingUserBinding>() {

    companion object {
        const val TAG = "ChorusSingerDialog"
    }

    private val numberFormat: NumberFormat by lazy {
        DecimalFormat("#,###")
    }

    var onKickingCallback: ((RoomSeatModel) -> Unit)? = null

    private val mAdapter: BindingSingleAdapter<RoomSeatModel, CantataItemSingingUserBinding> =

        object : BindingSingleAdapter<RoomSeatModel, CantataItemSingingUserBinding>() {
            override fun onBindViewHolder(holder: BindingViewHolder<CantataItemSingingUserBinding>, position: Int) {
                val item: RoomSeatModel = getItem(position) ?: return
                val binding: CantataItemSingingUserBinding = holder.binding
                when (position) {
                    0 -> {
                        binding.ivChorusNum.setBackgroundResource(R.drawable.cantata_rank_one)
                        binding.ivChorusNum.visibility = View.VISIBLE
                        binding.tvChorusNum.visibility = View.INVISIBLE
                    }

                    1 -> {
                        binding.ivChorusNum.setBackgroundResource(R.drawable.cantata_rank_two)
                        binding.ivChorusNum.visibility = View.VISIBLE
                        binding.tvChorusNum.visibility = View.INVISIBLE
                    }

                    2 -> {
                        binding.ivChorusNum.setBackgroundResource(R.drawable.cantata_rank_three)
                        binding.ivChorusNum.visibility = View.VISIBLE
                        binding.tvChorusNum.visibility = View.INVISIBLE
                    }

                    else -> {
                        binding.tvChorusNum.text = "${(position + 1)}"
                        binding.ivChorusNum.visibility = View.INVISIBLE
                        binding.tvChorusNum.visibility = View.VISIBLE
                    }
                }
                binding.tvUserName.text = item.name
                if (item.score == -1) {
                    binding.tvChorusScore.text = "-"
                } else {
                    val scoreStr = numberFormat.format(item.score)
                    binding.tvChorusScore.text =
                        binding.root.context.getString(R.string.cantata_current_score, scoreStr)
                }
                if (item.headUrl == "") {
                    binding.ivUserAvatar.setImageResource(R.mipmap.userimage)
                } else {
                    GlideApp.with(mBinding.root)
                        .load(item.headUrl)
                        .error(R.mipmap.userimage)
                        .transform(CenterCropRoundCornerTransform(100))
                        .into(binding.ivChorusNum)
                }
                binding.btnKicking.isGone = selSongModel?.userNo == item.userNo
                binding.btnKicking.setOnClickListener(object : OnClickJackingListener {
                    override fun onClickJacking(view: View) {
                        onKickingCallback?.invoke(item)
                    }
                })
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.let { window ->
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v: View?, insets: WindowInsetsCompat ->
                val inset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                mBinding?.root?.apply {
                    setPadding(inset.left, 0, inset.right, inset.bottom)
                }
                WindowInsetsCompat.CONSUMED
            }
        }

        mBinding.recyclerSinging.adapter = mAdapter

        mBinding.tvTitle.text = mBinding.root.context.getString(R.string.cantata_singing_user, seatModelList.size)
    }

    fun updateAllData(){
        mAdapter.resetAll(seatModelList)
    }
}
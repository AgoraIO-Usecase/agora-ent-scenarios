package io.agora.scene.cantata.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private val isRoomOwner: Boolean,
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
                        binding.ivSingerNum.setImageResource(R.drawable.cantata_rank_one)
                        binding.ivSingerNum.visibility = View.VISIBLE
                        binding.tvSingerNum.visibility = View.INVISIBLE
                    }

                    1 -> {
                        binding.ivSingerNum.setImageResource(R.drawable.cantata_rank_two)
                        binding.ivSingerNum.visibility = View.VISIBLE
                        binding.tvSingerNum.visibility = View.INVISIBLE
                    }

                    2 -> {
                        binding.ivSingerNum.setImageResource(R.drawable.cantata_rank_three)
                        binding.ivSingerNum.visibility = View.VISIBLE
                        binding.tvSingerNum.visibility = View.INVISIBLE
                    }

                    else -> {
                        binding.tvSingerNum.text = "${(position + 1)}"
                        binding.ivSingerNum.visibility = View.INVISIBLE
                        binding.tvSingerNum.visibility = View.VISIBLE
                    }
                }
                binding.tvSingerName.text = item.name
                if (item.score == -1) {
                    binding.tvSingerScore.text = "-"
                } else {
                    val scoreStr = numberFormat.format(item.score)
                    binding.tvSingerScore.text =
                        binding.root.context.getString(R.string.cantata_current_score, scoreStr)
                }
                context?.let {
                    if (selSongModel?.userNo == item.userNo) {
                        binding.tvSingerName.setCompoundDrawablesRelative(
                            it.getDrawable(R.drawable.cantata_main_singer_ic)?.apply {
                                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                            },
                            null, null, null
                        )
                    } else {
                        binding.tvSingerName.setCompoundDrawablesRelative(null, null, null, null)
                    }
                }

                if (item.headUrl == "") {
                    binding.ivSingerAvatar.setImageResource(R.mipmap.userimage)
                } else {
                    GlideApp.with(mBinding.root)
                        .load(item.headUrl)
                        .error(R.mipmap.userimage)
                        .transform(CenterCropRoundCornerTransform(100))
                        .into(binding.ivSingerAvatar)
                }
                binding.btnKicking.isGone = (!isRoomOwner || selSongModel?.userNo == item.userNo)
                binding.btnKicking.setOnClickListener(object : OnClickJackingListener {
                    override fun onClickJacking(view: View) {
                        onKickingCallback?.invoke(item)
                    }
                })
            }

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): BindingViewHolder<CantataItemSingingUserBinding> {
                return BindingViewHolder(
                    CantataItemSingingUserBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent, false
                    )
                )
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

    fun updateAllData() {
        mAdapter.resetAll(seatModelList)
    }
}
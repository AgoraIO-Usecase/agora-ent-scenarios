package io.agora.scene.cantata.ui.widget.rankList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.base.utils.dp
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataItemRankListBinding
import io.agora.scene.cantata.databinding.CantataItemSingingUserBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder

class RankListAdapter : BindingSingleAdapter<RankItem, CantataItemRankListBinding>() {
    override fun onBindViewHolder(holder: BindingViewHolder<CantataItemRankListBinding>, position: Int) {
        val item = getItem(position) ?: return
        val mBinding = holder.binding
        when (position) {
            0 -> {
                mBinding.itemRoot.setBackgroundResource(R.drawable.cantata_game_rank_list_1_background)
                mBinding.ivRoundRank.setImageResource(R.drawable.cantata_rank_one)
                mBinding.ivRoundRank.visibility = View.VISIBLE
                mBinding.tvRoundRank.visibility = View.INVISIBLE
            }

            1 -> {
                mBinding.itemRoot.setBackgroundResource(R.drawable.cantata_game_rank_list_2_background)
                mBinding.ivRoundRank.setImageResource(R.drawable.cantata_rank_two)
                mBinding.ivRoundRank.visibility = View.VISIBLE
                mBinding.tvRoundRank.visibility = View.INVISIBLE
            }

            2 -> {
                mBinding.itemRoot.setBackgroundResource(R.drawable.cantata_game_rank_list_3_background)
                mBinding.ivRoundRank.setImageResource(R.drawable.cantata_rank_three)
                mBinding.ivRoundRank.visibility = View.VISIBLE
                mBinding.tvRoundRank.visibility = View.INVISIBLE
            }

            else -> {
                mBinding.itemRoot.setBackgroundResource(R.drawable.cantata_game_rank_list_default_background)
                mBinding.tvRoundRank.text = "${(position + 1)}"
                mBinding.ivRoundRank.visibility = View.INVISIBLE
                mBinding.tvRoundRank.visibility = View.VISIBLE
            }
        }
        mBinding.tvPlayer.text = item.userName
        if (item.score == -1) {
            mBinding.tvScore.text = "-"
        } else {
            mBinding.tvScore.text = mBinding.tvScore.context.getString(R.string.cantata_score1, item.score)
        }
        if (item.avatar == "") {
            mBinding.ivHeader.visibility = View.INVISIBLE
        } else {
            GlideApp.with(mBinding.root)
                .load(item.avatar)
                .error(R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(mBinding.ivHeader)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindingViewHolder<CantataItemRankListBinding> {
        return BindingViewHolder(
            CantataItemRankListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }
}
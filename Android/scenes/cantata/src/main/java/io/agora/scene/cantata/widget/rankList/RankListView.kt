package io.agora.scene.cantata.widget.rankList

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.bumptech.glide.request.RequestOptions
import io.agora.scene.base.GlideApp
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataItemRankListBinding
import io.agora.scene.cantata.databinding.CantataLayoutGameRankListViewBinding
import io.agora.scene.widget.basic.BindingSingleAdapter
import io.agora.scene.widget.basic.BindingViewHolder
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * 本轮总分展示
 */
class RankListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {

    private val mAdapter = RankListAdapter()

    private val mBinding: CantataLayoutGameRankListViewBinding by lazy {
        CantataLayoutGameRankListViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    private var mCountDownLatch: CountDownTimer? = null
    var onNextSongClickCallback: (() -> Unit)? = null

    private val  numberFormat: NumberFormat by lazy {
        DecimalFormat("#,###")
    }

    init {
        init(context)
    }

    private fun init(context: Context) {
        mBinding.rvRankList.adapter = mAdapter
        mBinding.tvNextSongCountdown.setOnClickListener {
            mCountDownLatch?.cancel()
            onNextSongClickCallback?.invoke()
            isVisible = false
        }
    }

    fun resetRankList(list: List<RankItem>, nextSongName: String?) {
        val newList: MutableList<RankItem> = ArrayList()
        newList.addAll(list)
        if (list.size < 3) {
            for (i in 0 until 3 - list.size) {
                val emptyItem = RankItem()
                emptyItem.score = -1
                emptyItem.userName = context.getString(R.string.cantata_not_on_the_list)
                emptyItem.avatar = ""
                newList.add(emptyItem)
            }
        }
        mAdapter.resetAll(newList)
        var totalScore = 0
        list.forEach { rankItem ->
            if (rankItem.score >= 0) {
                totalScore += rankItem.score
            }
        }

        val formattedNumber = numberFormat.format(totalScore)
        mBinding.tvRoundScore.text = formattedNumber

        updateNextSong(nextSongName)
    }

    private fun updateNextSong(songName: String?) {
        if (songName == null) {
            mBinding.tvNextSong.visibility = GONE
            mBinding.tvNextSongCountdown.visibility = GONE
        } else if (songName == "") {
            mBinding.tvNextSong.visibility = GONE
            mBinding.tvNextSongCountdown.visibility = VISIBLE
        } else {
            mBinding.tvNextSong.visibility = VISIBLE
            mBinding.tvNextSong.text = context.getString(R.string.cantata_next_song, songName)
            mBinding.tvNextSongCountdown.visibility = VISIBLE
        }
        startTimer()
    }

    private fun startTimer(){
        mCountDownLatch?.cancel()
        mCountDownLatch = object : CountDownTimer((10 * 1000).toLong(), 999) {
            override fun onTick(millisUntilFinished: Long) {
                val second = (millisUntilFinished / 1000).toInt()
                if (second <= 9) {
                    mBinding.tvNextSongCountdown.text = resources.getString(R.string.cantata_next_countdown,second)
                }
            }

            override fun onFinish() {
                onNextSongClickCallback?.invoke()
                isVisible = false
            }
        }.start()
    }
}

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
                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
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

data class RankItem constructor(
    var rank: Int = 0,
    var userName: String? = null,
    var avatar: String? = null,
    var score: Int = 0
)
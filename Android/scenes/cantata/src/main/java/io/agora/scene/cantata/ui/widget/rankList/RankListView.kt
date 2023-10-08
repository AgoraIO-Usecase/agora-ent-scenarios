package io.agora.scene.cantata.ui.widget.rankList

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import io.agora.scene.cantata.R
import io.agora.scene.cantata.databinding.CantataLayoutGameRankListViewBinding
import java.text.DecimalFormat
import java.text.NumberFormat

class RankListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    FrameLayout(context, attrs, defStyleAttr) {
    private val mAdapter = RankListAdapter()

    private val mBinding: CantataLayoutGameRankListViewBinding by lazy {
        CantataLayoutGameRankListViewBinding.inflate(LayoutInflater.from(context), this, true)
    }

    init {
        init(context)
    }

    private fun init(context: Context) {
        mBinding.rvRankList.adapter = mAdapter
    }

    fun resetRankList(list: List<RankItem>) {
        val newList: MutableList<RankItem> = ArrayList()
        newList.addAll(list)
        if (list.size < 3) {
            for (i in 0 until 3 - list.size) {
                val emptyItem = RankItem()
                emptyItem.score = -1
                emptyItem.songNum = -1
                emptyItem.userName = context.getString(R.string.cantata_not_on_the_list)
                emptyItem.poster = ""
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
        val numberFormat: NumberFormat = DecimalFormat("#,###")
        val formattedNumber = numberFormat.format(totalScore)
        mBinding.tvRoundScore.text = formattedNumber
    }

    fun updateNextSong(songName: String?) {
        mBinding.tvNextSong.isGone = songName.isNullOrEmpty()
        songName?.let {
            mBinding.tvNextSong.text = context.getString(R.string.cantata_next_song, it)
        }
    }
}
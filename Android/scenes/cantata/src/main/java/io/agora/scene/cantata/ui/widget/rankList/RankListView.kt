package io.agora.scene.cantata.ui.widget.rankList

import android.content.Context
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
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
    }

    fun resetRankList(list: List<RankItem>, nextSongName: String?) {
        val newList: MutableList<RankItem> = ArrayList()
        newList.addAll(list)
        if (list.size < 3) {
            for (i in 0 until 3 - list.size) {
                val emptyItem = RankItem()
                emptyItem.score = -1
                //emptyItem.songNum = -1
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
        } else {
            mBinding.tvNextSong.visibility = VISIBLE
            mBinding.tvNextSong.text = context.getString(R.string.cantata_next_song, songName)
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
                isVisible = false
                onNextSongClickCallback?.invoke()
            }
        }.start()
    }
}
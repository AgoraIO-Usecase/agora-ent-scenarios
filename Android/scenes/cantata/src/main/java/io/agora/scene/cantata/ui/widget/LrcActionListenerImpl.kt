package io.agora.scene.cantata.ui.widget

import android.content.Context
import io.agora.karaoke_view.v11.model.LyricsLineModel
import io.agora.scene.cantata.ui.viewmodel.RoomLivingViewModel
import io.agora.scene.cantata.ui.widget.lrcView.LrcControlView

open class LrcActionListenerImpl constructor(
    private val mContext: Context,
    private val mViewModel: RoomLivingViewModel,
    private val mLrcControlView: LrcControlView
) : LrcControlView.OnKaraokeEventListener {
    override fun onSwitchOriginalClick() {
        super.onSwitchOriginalClick()
        mViewModel.musicToggleOriginal()
        mLrcControlView.setSwitchOriginalChecked(mViewModel.isOriginalMode())
    }

    override fun onPlayClick() {
        mViewModel.musicToggleStart()
    }

    override fun onStartSing() {
        mViewModel.leaveChorus()
    }

    override fun onJoinChorus() {
        mViewModel.joinChorus()
    }

    override fun onLeaveChorus() {
        mViewModel.leaveChorus()
    }

    override fun onDragTo(time: Long) {
        mViewModel.musicSeek(time)
    }

    override fun onLineFinished(line: LyricsLineModel?, score: Int, cumulativeScore: Int, index: Int, total: Int) {
        mLrcControlView.updateScore(
            score.toDouble(), cumulativeScore.toDouble(),
            /** Workaround(Hai_Guo) */
            (total * 100).toDouble()
        )
        mViewModel.syncSingleLineScore(score, cumulativeScore, index, total * 100)
    }

    override fun onSkipPreludeClick() {
        val lyrics = mLrcControlView.karaokeView!!.lyricsData ?: return
        // Experience will be better when seeking 2000 milliseconds ahead
        val seekPosition = lyrics.startOfVerse - 2000
        mViewModel.musicSeek(if (seekPosition > 0) seekPosition else 0)
    }

    override fun onSkipPostludeClick() {
        val lyrics = mLrcControlView.karaokeView!!.lyricsData ?: return
        mViewModel.musicSeek(mViewModel.getSongDuration()!! - 500)
    }

    override fun onReGetLrcUrl() {
        mViewModel.reGetLrcUrl()
    }
}
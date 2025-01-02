package io.agora.scene.cantata.live.listener

import android.content.Context
import io.agora.karaoke_view_ex.internal.model.LyricsLineModel
import io.agora.scene.cantata.live.RoomLivingActivity
import io.agora.scene.cantata.live.RoomLivingViewModel
import io.agora.scene.cantata.widget.lrcView.LrcControlView

/**
 * Mixer Control
 */
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
        if (mContext is RoomLivingActivity) {
            mContext.toggleSelfAudio(true, callback = {
                mViewModel.joinChorus()
            })
        }
    }

    override fun onLeaveChorus() {
        mViewModel.leaveChorus()
    }

    override fun onDragTo(time: Long) {
        mViewModel.musicSeek(time)
    }

    override fun onLineFinished(line: LyricsLineModel?, score: Int, cumulativeScore: Int, index: Int, total: Int) {
        mViewModel.updateSeatScoreStatus(score, cumulativeScore)
    }

    override fun onSkipPreludeClick() {
        val lyrics = mLrcControlView.karaokeView?.lyricData ?: return
        // Experience will be better when seeking 2000 milliseconds ahead
        val seekPosition = lyrics.preludeEndPosition - 2000
        mViewModel.musicSeek(if (seekPosition > 0) seekPosition else 0)
    }

    override fun onSkipPostludeClick() {
        val lyrics = mLrcControlView.karaokeView?.lyricData ?: return
        mViewModel.musicSeek(mViewModel.getSongDuration()!! - 500)
    }

    override fun onReGetLrcUrl() {
        mViewModel.reGetLrcUrl()
    }
}
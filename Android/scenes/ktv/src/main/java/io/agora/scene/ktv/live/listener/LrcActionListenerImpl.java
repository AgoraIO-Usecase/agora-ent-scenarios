package io.agora.scene.ktv.live.listener;

import android.content.Context;

import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.widget.lrcView.LrcControlView;

/**
 * 调音台 listener
 */
public class LrcActionListenerImpl implements LrcControlView.OnKaraokeEventListener {

    private final Context mContext;
    private final RoomLivingViewModel mViewModel;
    private final LrcControlView mLrcControlView;

    /**
     * Instantiates a new Lrc action listener.
     *
     * @param context        the context
     * @param viewModel      the view model
     * @param lrcControlView the lrc control view
     */
    public LrcActionListenerImpl(Context context, RoomLivingViewModel viewModel, LrcControlView lrcControlView) {
        mContext = context;
        mViewModel = viewModel;
        mLrcControlView = lrcControlView;
    }


    @Override
    public void onSwitchOriginalClick(LrcControlView.AudioTrack audioTrack) {
        LrcControlView.OnKaraokeEventListener.super.onSwitchOriginalClick(audioTrack);
        mViewModel.musicToggleOriginal(audioTrack);

        if (audioTrack == LrcControlView.AudioTrack.Origin ||
                audioTrack == LrcControlView.AudioTrack.DaoChang) {
            mLrcControlView.setSwitchOriginalChecked(true);
        } else {
            mLrcControlView.setSwitchOriginalChecked(false);
        }
    }

    @Override
    public void onPlayClick() {
        LrcControlView.OnKaraokeEventListener.super.onPlayClick();

        mViewModel.musicToggleStart();
    }


    @Override
    public void onStartSing() {
        mViewModel.leaveChorus();
    }

    @Override
    public void onJoinChorus() {
        mViewModel.joinChorus();
    }

    @Override
    public void onLeaveChorus() {
        mViewModel.leaveChorus();
    }

    @Override
    public void onDragTo(long time) {
        mViewModel.musicSeek(time);
    }

    @Override
    public void onLineFinished(LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
        mLrcControlView.updateScore(score, cumulativeScore, /** Workaround(Hai_Guo)*/total * 100);
        mViewModel.syncSingleLineScore(score, cumulativeScore, index, total * 100);
    }

    @Override
    public void onSkipPreludeClick() {
        LyricsModel lyrics = mLrcControlView.getKaraokeView().getLyricsData();
        if (lyrics == null) {
            return;
        }
        // Experience will be better when seeking 2000 milliseconds ahead
        long seekPosition = lyrics.startOfVerse - 2000;
        mViewModel.musicSeek(seekPosition > 0 ? seekPosition : 0);
    }

    @Override
    public void onSkipPostludeClick() {
        LyricsModel lyrics = mLrcControlView.getKaraokeView().getLyricsData();
        if (lyrics == null) {
            return;
        }
        mViewModel.musicSeek(mViewModel.getSongDuration() - 500);
    }

    @Override
    public void onReGetLrcUrl() {
        mViewModel.reGetLrcUrl();
    }
}

package io.agora.scene.ktv.live.listener;

import android.content.Context;

import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.widget.LrcControlView;

public class LrcActionListenerImpl implements LrcControlView.OnKaraokeEventListener {

    private final Context mContext;
    private final RoomLivingViewModel mViewModel;
    private final LrcControlView mLrcControlView;

    public LrcActionListenerImpl(Context context, RoomLivingViewModel viewModel, LrcControlView lrcControlView) {
        mContext = context;
        mViewModel = viewModel;
        mLrcControlView = lrcControlView;
    }


    @Override
    public void onSwitchOriginalClick() {
        LrcControlView.OnKaraokeEventListener.super.onSwitchOriginalClick();
        if (mViewModel.musicToggleOriginal()) {
            mLrcControlView.setSwitchOriginalChecked(true);
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
    public void onWaitTimeOut() {
        mViewModel.leaveChorus();
    }

    @Override
    public void onCountTime(int time) {
        mViewModel.musicCountDown(time);
    }

    @Override
    public void onDragTo(long time) {
        mViewModel.musicSeek(time);
    }

    @Override
    public void onLineFinished(LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
        mLrcControlView.updateScore(score, cumulativeScore);
    }

    @Override
    public void onSkipPostludeClick() {
        mViewModel.changeMusic();
    }

    @Override
    public void onSkipPreludeClick() {

    }
}

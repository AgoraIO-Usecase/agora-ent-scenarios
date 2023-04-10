package io.agora.scene.ktv.live.listener;

import android.content.Context;

import io.agora.lyrics_view.PitchView;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.widget.LrcControlView;

public class LrcActionListenerImpl implements LrcControlView.OnLrcActionListener, PitchView.OnSingScoreListener {

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
        LrcControlView.OnLrcActionListener.super.onSwitchOriginalClick();
        if (mViewModel.musicToggleOriginal()) {
            mLrcControlView.setSwitchOriginalChecked(true);
        }
    }

    @Override
    public void onPlayClick() {
        LrcControlView.OnLrcActionListener.super.onPlayClick();
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

    // The Impl of LrcControlView.OnLrcActionListener

    @Override
    public void onProgressChanged(long time) {
        mViewModel.musicSeek(time);
    }

    @Override
    public void onStartTrackingTouch() {

    }

    @Override
    public void onStopTrackingTouch() {

    }


    // The Impl of PitchView.OnSingScoreListener

    @Override
    public void onOriginalPitch(float pitch, int totalCount) {

    }

    @Override
    public void onScore(double score, double cumulativeScore, double totalScore) {
        mLrcControlView.updateScore(score);
    }


}

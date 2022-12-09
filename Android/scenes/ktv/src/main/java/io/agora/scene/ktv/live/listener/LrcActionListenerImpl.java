package io.agora.scene.ktv.live.listener;

import android.content.Context;

import io.agora.lrcview.PitchView;
import io.agora.scene.ktv.live.RoomLivingViewModel;
import io.agora.scene.ktv.widget.LrcControlView;

public class LrcActionListenerImpl implements LrcControlView.OnLrcActionListener, PitchView.OnActionListener {

    private final Context mContext;
    private final RoomLivingViewModel mViewModel;
    private final LrcControlView mLrcControlView;

    public LrcActionListenerImpl(Context context, RoomLivingViewModel viewModel, LrcControlView lrcControlView) {
        mContext = context;
        mViewModel = viewModel;
        mLrcControlView = lrcControlView;
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
        mViewModel.leaveChorus(mContext);
    }

    @Override
    public void onJoinChorus() {
        mViewModel.joinChorus();
    }

    @Override
    public void onWaitTimeOut() {
        mViewModel.leaveChorus(mContext);
    }

    @Override
    public void onCountTime(int time) {
        mViewModel.musicCountDown(time);

    }


    // The Impl of PitchView.OnActionListener

    @Override
    public void onOriginalPitch(float pitch, int totalCount) {

    }

    @Override
    public void onScore(double score, double cumulativeScore, double totalScore) {
        mLrcControlView.updateScore(score);
    }


}

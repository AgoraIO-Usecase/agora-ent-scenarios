package io.agora.scene.ktv.singbattle.live.listener;

import android.content.Context;

import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;
import io.agora.scene.ktv.singbattle.live.RoomLivingViewModel;
import io.agora.scene.ktv.singbattle.widget.lrcView.LrcControlView;

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
        mViewModel.musicToggleOriginal();

        mLrcControlView.setSwitchOriginalChecked(mViewModel.isOriginalMode());
    }

    @Override
    public void onPlayClick() {
        LrcControlView.OnKaraokeEventListener.super.onPlayClick();

        mViewModel.musicToggleStart();
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
    public void onReGetLrcUrl() {
        mViewModel.reGetLrcUrl();
    }

    @Override
    public void onGraspSongClick() {
        mViewModel.graspSong();
    }
}

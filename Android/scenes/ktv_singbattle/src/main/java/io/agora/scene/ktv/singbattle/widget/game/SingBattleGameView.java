package io.agora.scene.ktv.singbattle.widget.game;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvLayoutGameViewBinding;

public class SingBattleGameView extends FrameLayout {

    protected KtvLayoutGameViewBinding mBinding;
    private boolean isRoomOwner = false;
    private OnSingBattleGameEventListener mSingBattleGameEventListener;

    public SingBattleGameView(@NonNull Context context) {
        this(context, null);
    }

    public SingBattleGameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingBattleGameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBinding = null;
    }

    private void init(Context context) {
        mBinding = KtvLayoutGameViewBinding.inflate(LayoutInflater.from(context), this, true);
        initListener();
    }

    private void initListener() {
        mBinding.ilIDLE.btAutoSelectSong.setOnClickListener(view -> mSingBattleGameEventListener.onAutoSelectSongClick());
        mBinding.ilIDLE.btChooseSong.setOnClickListener(View -> mSingBattleGameEventListener.onChooseSongClick());
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(3 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
//
//                if (mOnLrcActionListener != null) {
//                    mOnLrcActionListener.onCountTime(second);
//                }
//
                setCountDown(second);
            }

            @Override
            public void onFinish() {
                //mOnLrcActionListener.onWaitTimeOut();
                onCountFinished();
            }
        }.start();
    }

    private void stopTimer() {
        if (mCountDownLatch != null) {
            mCountDownLatch.cancel();
            mCountDownLatch = null;
        }
    }

    private void setCountDown(int second) {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText("" + second);
    }

    private void onCountFinished() {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
    }

    public void setSingBattleGameEventListener(OnSingBattleGameEventListener listener) {
        this.mSingBattleGameEventListener = listener;
    }

    public void setIsRoomOwner(boolean isRoomOwner) {
        this.isRoomOwner = isRoomOwner;
    }

    public void onGameWaittingStatus() {
        if (mBinding == null) return;
        if (isRoomOwner) {
            mBinding.ilIDLE.messageText.setText(R.string.ktv_game_room_owner_idle);
            mBinding.ilIDLE.btChooseSong.setVisibility(View.VISIBLE);
            mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.VISIBLE);
        } else {
            mBinding.ilIDLE.messageText.setText(R.string.ktv_game_room_owner_choosing_song);
            mBinding.ilIDLE.btChooseSong.setVisibility(View.GONE);
            mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.GONE);
        }
    }

    public void onGameStartStatus() {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_start);
        mBinding.ilIDLE.btChooseSong.setVisibility(View.GONE);
        mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.GONE);
        startTimer();
    }

    public interface OnSingBattleGameEventListener {
        default void onChooseSongClick() {}
        default void onAutoSelectSongClick() {}
    }
}

package io.agora.scene.ktv.singbattle.widget.game;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvLayoutGameViewBinding;
import io.agora.scene.ktv.singbattle.widget.rankList.RankItem;

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
        //onGameWaitingStatus();
        initListener();
    }

    private void initListener() {
        mBinding.ilIDLE.btAutoSelectSong.setOnClickListener(view -> mSingBattleGameEventListener.onAutoSelectSongClick());
        mBinding.ilIDLE.btChooseSong.setOnClickListener(View -> mSingBattleGameEventListener.onChooseSongClick());
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(4 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);

                if (second <= 2) {
                    if (mBinding == null) return;
                    mBinding.ilIDLE.messageText.setText("" + (second + 1));
                }
            }

            @Override
            public void onFinish() {
                if (mBinding == null || mSingBattleGameEventListener == null) return;
                mSingBattleGameEventListener.onGameStart();
                onBattleGamePrepare();
            }
        }.start();
    }

    private void stopTimer() {
        if (mCountDownLatch != null) {
            mCountDownLatch.cancel();
            mCountDownLatch = null;
        }
    }

    public void setSingBattleGameEventListener(OnSingBattleGameEventListener listener) {
        this.mSingBattleGameEventListener = listener;
    }

    public void setIsRoomOwner(boolean isRoomOwner) {
        this.isRoomOwner = isRoomOwner;
        //onGameWaitingStatus();
    }

    // 游戏等待
    public void onGameWaitingStatus() {
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

    // 游戏开始
    public void onGameStartStatus(int songNum) {
        this.songNum = songNum;
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_start);
        mBinding.ilIDLE.btChooseSong.setVisibility(View.GONE);
        mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.GONE);
        startTimer();
    }

    // 预播放歌曲
    private int songNum = 0;
    private int nowNum = 0;
    public void onBattleGamePrepare() {
        if (mBinding == null) return;
        if (nowNum >= songNum) return;
        nowNum ++;
        mBinding.ilActive.tvSongTab.setText(nowNum + "/" + songNum);
        mBinding.ilIDLE.messageText.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
        mBinding.ilActive.lrcControlView.startTimerCount();
    }

    // 抢唱成功
    public void onGraspSongSuccess(String userName) {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText("本轮由 " + userName + " 抢到麦");
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.getRoot().postDelayed(() -> {
            if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onStartSing();
            mBinding.ilIDLE.messageText.setVisibility(View.GONE);
            mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
        }, 5000);
    }

    // 无人抢唱
    public void onNobodyGraspSong() {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_nobody_grasp);
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.getRoot().postDelayed(() -> {
            if (nowNum >= songNum) {
                if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onGameEnd();
                return;
            }
            if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onNextSong();
        }, 5000);
    }

    // 挑战结束
    public void onSongFinish(int score) {
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        if (score <= 60) {
            mBinding.ilIDLE.messageText.setText("挑战失败：" + score);
            mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_game_defeat_text_background);
        } else {
            mBinding.ilIDLE.messageText.setText("挑战成功：" + score);
            mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_game_win_text_background);
        }
        mBinding.getRoot().postDelayed(this::onNextSong, 5000);
    }

    // 下一首
    public void onNextSong() {
        if (nowNum < songNum) {
            mBinding.ilIDLE.messageText.setText("下一首");
            mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_game_idle_text_background);
            mBinding.getRoot().postDelayed(() -> {
                if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onNextSong();
            }, 3000);
        } else {
            // TODO UI
            //onGameEnd();
        }
    }

    // 游戏结束
    public void onGameEnd(List<RankItem> list) {
        nowNum = 0;
        songNum = 0;
        mBinding.ilRank.setVisibility(View.VISIBLE);
        mBinding.ilRank.resetRankList(list);
    }

    public interface OnSingBattleGameEventListener {
        /**
         * 抢唱-点击"点歌"按钮回调
         */
        default void onChooseSongClick() {}

        /**
         * 抢唱-点击"随机选歌开始"按钮回调
         */
        default void onAutoSelectSongClick() {}

        /**
         * 抢唱-游戏开始
         */
        default void onGameStart() {}

        /**
         * 抢唱-开始演唱
         */
        default void onStartSing() {}

        /**
         * 抢唱-播放下一首歌
         */
        default void onNextSong() {}

        /**
         * 抢唱-游戏结束
         */
        default void onGameEnd() {}
    }
}

package io.agora.scene.ktv.singbattle.widget.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.singbattle.KTVLogger;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleLayoutGameViewBinding;
import io.agora.scene.ktv.singbattle.widget.rankList.RankItem;

public class SingBattleGameView extends FrameLayout {

    private final String TAG = "SingBattleGameView_LOG";
    protected KtvSingbattleLayoutGameViewBinding mBinding;
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
        mBinding = KtvSingbattleLayoutGameViewBinding.inflate(LayoutInflater.from(context), this, true);
        //onGameWaitingStatus();
        initListener();
    }

    private void initListener() {
        mBinding.ilIDLE.btAutoSelectSong.setOnClickListener(view -> {
            mBinding.ilIDLE.btAutoSelectSong.setEnabled(false);
            mSingBattleGameEventListener.onAutoSelectSongClick();
        });
        mBinding.ilIDLE.btChooseSong.setOnClickListener(View -> mSingBattleGameEventListener.onChooseSongClick());
        mBinding.btGameAgain.setOnClickListener(View -> mSingBattleGameEventListener.onGameAgainClick());
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(5 * 1000, 999) {
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                if (mBinding == null) return;
                KTVLogger.d("hugo", "pig" + second);
                if (second <= 3 && second >= 1) {
                    mBinding.ilIDLE.messageText.setText(String.valueOf(second));
                } else if (second < 1) {
                    mBinding.ilIDLE.messageText.setText("Go");
                }
            }

            @Override
            public void onFinish() {
                if (mBinding == null || mSingBattleGameEventListener == null) return;
                mSingBattleGameEventListener.onGameStart();
                onBattleGamePrepare(-1);
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

    // Game waiting
    public void onGameWaitingStatus() {
        KTVLogger.d(TAG, "onGameWaitingStatus");
        if (mBinding == null) return;
        mBinding.ilRank.setVisibility(GONE);
        if (isRoomOwner) {
            mBinding.btGameAgain.setVisibility(GONE);
            mBinding.ilIDLE.messageText.setText(R.string.ktv_singbattle_game_room_owner_idle);
            mBinding.ilIDLE.btChooseSong.setVisibility(View.VISIBLE);
            //mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.VISIBLE);
            //mBinding.ilIDLE.btAutoSelectSong.setEnabled(true);
        } else {
            mBinding.ilIDLE.messageText.setText(R.string.ktv_singbattle_game_room_owner_choosing_song);
            mBinding.ilIDLE.btChooseSong.setVisibility(View.GONE);
            //mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.GONE);
        }
    }

    // Game start
    public void onGameStartStatus() {
        KTVLogger.d(TAG, "onGameStartStatus");
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_singbattle_game_start);
        mBinding.ilIDLE.btChooseSong.setVisibility(View.GONE);
        //mBinding.ilIDLE.btAutoSelectSong.setVisibility(View.GONE);
        startTimer();
    }

    public void setSongTotalNum(int songNum) {
        KTVLogger.d(TAG, "setSongTotalNum, songNum:" + songNum);
        this.songNum = songNum;
    }

    // Preview song
    private int songNum = 0;
    private int nowNum = 0;
    @SuppressLint("SetTextI18n")
    public void onBattleGamePrepare(int leftSongNum) {
        KTVLogger.d(TAG, "onBattleGamePrepare");
        if (mBinding == null) return;
        if (leftSongNum == -1) {
            nowNum = 1;
        } else {
            nowNum = songNum - leftSongNum + 1;
        }
        mBinding.ilActive.tvSongTab.setText(nowNum + "/" + songNum);
        mBinding.ilIDLE.messageText.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
    }

    // Grasp song success
    public void onGraspSongSuccess(String userName, String headUrl) {
        KTVLogger.d(TAG, "onGraspSongSuccess， headUrl：" + headUrl);
        if (mBinding == null) return;
        mBinding.ilIDLE.tvBattleResultView.setVisibility(View.VISIBLE);
        GlideApp.with(mBinding.getRoot())
                .load(headUrl)
                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(mBinding.ilIDLE.ivHeader);
        mBinding.ilIDLE.tvBattleResultName.setText(" " + userName + getContext().getString(R.string.ktv_singbattle_mic_grabbed));
        mBinding.ilIDLE.messageText.setText("");
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.getRoot().postDelayed(() -> {
            if (mBinding == null) return;
            if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onStartSing();
            mBinding.ilIDLE.messageText.setVisibility(View.GONE);
            mBinding.ilIDLE.tvBattleResultView.setVisibility(View.GONE);
            mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
        }, 5000);
    }

    // No one grabs the song
    public void onNobodyGraspSong() {
        KTVLogger.d(TAG, "onNobodyGraspSong");
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_singbattle_game_nobody_grasp);
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.getRoot().postDelayed(() -> {
            if (nowNum < songNum) {
                onNextSong();
            } else {
                // Already the last song
                if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onGameEnd();
            }
        }, 5000);
    }

    // Song finish
    public void onSongFinish(int score) {
        KTVLogger.d(TAG, "onSongFinish");
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        if (score < 50) {
            mBinding.ilIDLE.messageText.setText("");
            mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_singbattle_game_defeat_text_background);
            mBinding.ilIDLE.scoreFailText.setVisibility(View.VISIBLE);
            mBinding.ilIDLE.scoreFailText.setText(String.valueOf(score));
        } else {
            mBinding.ilIDLE.messageText.setText("");
            mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_singbattle_game_win_text_background);
            mBinding.ilIDLE.scoreSuccessText.setVisibility(View.VISIBLE);
            mBinding.ilIDLE.scoreSuccessText.setText(String.valueOf(score));
        }

        mBinding.getRoot().postDelayed(() -> {
            if (nowNum < songNum) {
                onNextSong();
            } else {
                // Already the last song
                if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onGameEnd();
            }
        }, 5000);
    }

    // Next song
    public void onNextSong() {
        KTVLogger.d(TAG, "onNextSong");
        if (mBinding == null) return;
        mBinding.ilIDLE.scoreFailText.setVisibility(View.GONE);
        mBinding.ilIDLE.scoreSuccessText.setVisibility(View.GONE);
        mBinding.ilIDLE.messageText.setText(R.string.ktv_singbattle_next);
        mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_singbattle_game_idle_text_background);
        mBinding.getRoot().postDelayed(() -> {
            if (mSingBattleGameEventListener != null) mSingBattleGameEventListener.onNextSong();
        }, 3000);
    }

    // Game end
    public void onGameEnd(List<RankItem> list) {
        KTVLogger.d(TAG, "onGameEnd");
        if (mBinding == null) return;
        nowNum = 0;
        songNum = 0;
        mBinding.ilIDLE.scoreFailText.setVisibility(View.GONE);
        mBinding.ilIDLE.scoreSuccessText.setVisibility(View.GONE);
        mBinding.ilRank.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_singbattle_game_idle_text_background);

        if (isRoomOwner) {
            mBinding.btGameAgain.setVisibility(View.VISIBLE);
        }
        mBinding.ilRank.resetRankList(list);
    }

    public interface OnSingBattleGameEventListener {
        /**
         * Click "choose song" button callback
         */
        default void onChooseSongClick() {}

        /**
         * Click "random select song" button callback
         */
        default void onAutoSelectSongClick() {}

        /**
         * Grasp song - game start
         */
        default void onGameStart() {}

        /**
         * Grasp song - start singing
         */
        default void onStartSing() {}

        /**
         * Grasp song - play next song
         */
        default void onNextSong() {}

        /**
         * Grasp song - game end
         */
        default void onGameEnd() {}

        /**
         * Grasp song - again
         */
        default void onGameAgainClick() {}
    }
}

package io.agora.scene.ktv.singrelay.widget.game;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import io.agora.scene.base.GlideApp;
import io.agora.scene.ktv.singrelay.KTVLogger;
import io.agora.scene.ktv.singrelay.R;
import io.agora.scene.ktv.singrelay.databinding.KtvLayoutGameViewBinding;
import io.agora.scene.ktv.singrelay.widget.lrcView.LrcControlView;
import io.agora.scene.ktv.singrelay.widget.rankList.RankItem;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;

public class SingRelayGameView extends FrameLayout {

    private final String TAG = "SingRelayGameView_LOG";
    protected KtvLayoutGameViewBinding mBinding;
    private boolean isRoomOwner = false;
    private OnSingRelayGameEventListener mSingRelayGameEventListener;
    private int partNum = 1;

    public SingRelayGameView(@NonNull Context context) {
        this(context, null);
    }

    public SingRelayGameView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingRelayGameView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mBinding.ilIDLE.btGameStart.setOnClickListener(View -> mSingRelayGameEventListener.onGameStartBtnClick());
        mBinding.btGameAgain.setOnClickListener(View -> mSingRelayGameEventListener.onGameAgainClick());
        mBinding.ilIDLE.tvSongTab.setOnClickListener(view -> {
            mBinding.ilIDLE.tvSongTab.setVisibility(View.GONE);
            mBinding.ilIDLE.ivGameTips.setVisibility(View.VISIBLE);
            mBinding.ilIDLE.ivGameTips.bringToFront();
        });
        mBinding.ilIDLE.ivGameTips.setOnClickListener(view -> {
            mBinding.ilIDLE.ivGameTips.setVisibility(View.GONE);
            mBinding.ilIDLE.tvSongTab.setVisibility(View.VISIBLE);
        });
        mBinding.ilActive.tvSongTab.setOnClickListener(view -> {
            mBinding.ilActive.tvSongTab.setVisibility(View.GONE);
            mBinding.ilActive.ivGameTips.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivGameTips.bringToFront();
        });
        mBinding.ilActive.ivGameTips.setOnClickListener(view -> {
            mBinding.ilActive.ivGameTips.setVisibility(View.GONE);
            mBinding.ilActive.tvSongTab.setVisibility(View.VISIBLE);
        });
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(5 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                if (mBinding == null) return;
                KTVLogger.d("hugo", "pig" + second);
                if (second <= 3 && second >= 1) {
                    mBinding.ilIDLE.messageText.setText("" + second);
                } else if (second < 1) {
                    mBinding.ilIDLE.messageText.setText("Go");
                }
            }

            @Override
            public void onFinish() {
                if (mBinding == null || mSingRelayGameEventListener == null) return;
                mSingRelayGameEventListener.onGameStart();
                mBinding.ilIDLE.messageText.setVisibility(View.GONE);
                mBinding.ilIDLE.tvSongTab.setVisibility(View.GONE);
                mBinding.ilIDLE.ivGameTips.setVisibility(View.GONE);
                mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void stopTimer() {
        if (mCountDownLatch != null) {
            mCountDownLatch.cancel();
            mCountDownLatch = null;
        }
    }

    public void setSingRelayGameEventListener(OnSingRelayGameEventListener listener) {
        this.mSingRelayGameEventListener = listener;
    }

    public void setIsRoomOwner(boolean isRoomOwner) {
        this.isRoomOwner = isRoomOwner;
        //onGameWaitingStatus();
    }

    // 游戏等待
    public void onGameWaitingStatus() {
        KTVLogger.d(TAG, "onGameWaitingStatus");
        if (mBinding == null) return;
        mBinding.ilRank.setVisibility(GONE);
        if (isRoomOwner) {
            mBinding.btGameAgain.setVisibility(GONE);
            mBinding.ilIDLE.messageText.setVisibility(GONE);
            mBinding.ilIDLE.btGameStart.setVisibility(View.VISIBLE);
        } else {
            mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
            mBinding.ilIDLE.messageText.setText(R.string.ktv_game_room_owner_choosing_song);
            mBinding.ilIDLE.btGameStart.setVisibility(View.GONE);
        }
    }

    // 游戏开始
    public void onGameStartStatus() {
        KTVLogger.d(TAG, "onGameStartStatus");
        if (mBinding == null) return;
        mBinding.ilIDLE.btGameStart.setVisibility(View.GONE);
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_start);
        startTimer();
    }

    // 下一段前提示
    public void onBattleGamePrepare(boolean isWinner) {
        KTVLogger.d(TAG, "onBattleGamePrepare");
        if (mBinding == null) return;
        if (isWinner) {
            mBinding.ilActive.messageText.setText("下段演唱即将开始，准备演唱");
        } else {
            mBinding.ilActive.messageText.setText("下段演唱即将开始，准备抢唱");
        }
        mBinding.ilActive.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.messageText.bringToFront();
        mBinding.getRoot().postDelayed(() -> {
            if (mBinding == null) return;
            mBinding.ilActive.messageText.setVisibility(View.GONE);
        }, 3000);
    }

    public void onGraspSongBegin() {
        if (mBinding == null) return;
        partNum ++;
        mBinding.ilActive.tvSongNumTab.setText( partNum + "/5");
        if (partNum != 5) {
            mBinding.ilActive.lrcControlView.onGraspEnable();
        }
    }

    // 抢唱成功
    public void onGraspSongSuccess(String userName, String headUrl) {
        KTVLogger.d(TAG, "onGraspSongSuccess， headUrl：" + headUrl);
        if (mBinding == null) return;
        mBinding.ilActive.lrcControlView.onGraspDisable();
        GlideApp.with(mBinding.getRoot())
                .load(headUrl)
                .error(R.mipmap.userimage)
                .transform(new CenterCropRoundCornerTransform(100))
                .into(mBinding.ilActive.ivHeader);
        mBinding.ilActive.messageText.setText("");
        mBinding.ilActive.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.messageText.bringToFront();
        mBinding.ilActive.tvBattleResultName.setText(" " + userName + " 抢到麦");
        mBinding.ilActive.tvBattleResultView.setVisibility(View.VISIBLE);
        mBinding.ilActive.tvBattleResultView.bringToFront();

        GlideApp.with(mBinding.getRoot())
                .load(headUrl)
                .error(R.mipmap.userimage)
                .transform(new CenterCropRoundCornerTransform(100))
                .into(mBinding.ilActive.ivWinnerHeader);
        mBinding.ilActive.ivWinnerName.setText(userName + " 演唱");
        mBinding.ilActive.winnerTips.setVisibility(VISIBLE);
        mBinding.getRoot().postDelayed(() -> {
            if (mBinding == null) return;
            mBinding.ilActive.messageText.setVisibility(View.GONE);
            mBinding.ilActive.tvBattleResultView.setVisibility(View.GONE);
        }, 5000);
    }

    // 无人抢唱
    public void onNobodyGraspSong() {
        KTVLogger.d(TAG, "onNobodyGraspSong");
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_nobody_grasp);
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.getRoot().postDelayed(() -> {
            if (mSingRelayGameEventListener != null) mSingRelayGameEventListener.onGameEnd();
        }, 5000);
    }

    public void onNextPart(boolean isWinner) {
        KTVLogger.d(TAG, "onNextPart");
        if (mBinding == null) return;
        mBinding.ilActive.winnerTips.setVisibility(GONE);
        if (isWinner) {
            mBinding.ilActive.lrcControlView.changeMusicController(LrcControlView.Role.Singer);
        } else {
            mBinding.ilActive.lrcControlView.changeMusicController(LrcControlView.Role.Listener);
        }
    }

    // 歌曲演唱结束
    public void onSongFinish(int score) {
        KTVLogger.d(TAG, "onSongFinish");
        if (mBinding == null) return;
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        if (mSingRelayGameEventListener != null) mSingRelayGameEventListener.onGameEnd();
    }

    // 游戏结束
    public void onGameEnd(List<RankItem> list) {
        KTVLogger.d(TAG, "onGameEnd");
        if (mBinding == null) return;
        mBinding.ilIDLE.tvSongTab.setVisibility(View.VISIBLE);
        mBinding.ilRank.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_game_idle_text_background);

        if (isRoomOwner) {
            mBinding.btGameAgain.setVisibility(View.VISIBLE);
        }
        mBinding.ilRank.resetRankList(list);
        partNum = 1;
    }

    public interface OnSingRelayGameEventListener {
        /**
         * 接唱-点击"开始"按钮回调
         */
        default void onGameStartBtnClick() {}

        /**
         * 抢唱-游戏开始
         */
        default void onGameStart() {}

        /**
         * 抢唱-开始演唱
         */
        default void onStartSing() {}

        /**
         * 抢唱-游戏结束
         */
        default void onGameEnd() {}

        /**
         * 抢唱-再来一轮
         */
        default void onGameAgainClick() {}
    }
}

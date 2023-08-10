package io.agora.scene.ktv.singrelay.widget.game;

import android.annotation.SuppressLint;
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

/**
 * 游戲View
 */
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);
                if (mBinding == null) return;
                if (second <= 3 && second >= 1) {
                    mBinding.ilIDLE.messageText.setText(String.valueOf(second));
                } else if (second < 1) {
                    mBinding.ilIDLE.messageText.setText("Go");
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                if (mBinding == null || mSingRelayGameEventListener == null) return;
                mSingRelayGameEventListener.onGameStart();
                mBinding.ilIDLE.messageText.setVisibility(View.GONE);
                mBinding.ilIDLE.tvSongTab.setVisibility(View.GONE);
                mBinding.ilIDLE.ivGameTips.setVisibility(View.GONE);
                mBinding.ilActive.tvSongNumTab.setText(partNum + "/5");
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

    // 注册游戏事件
    public void setSingRelayGameEventListener(OnSingRelayGameEventListener listener) {
        this.mSingRelayGameEventListener = listener;
    }

    // 设置房主
    public void setIsRoomOwner(boolean isRoomOwner) {
        this.isRoomOwner = isRoomOwner;
    }

    // 游戏等待
    public void onGameWaitingStatus() {
        KTVLogger.d(TAG, "onGameWaitingStatus");
        if (mBinding == null) return;
        mBinding.ilRank.setVisibility(GONE);
        mBinding.ilIDLE.ivGameTips.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.tvSongTab.setVisibility(View.GONE);
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
    private boolean isGamer = false;
    public void onGameStartStatus(boolean isGamer) {
        KTVLogger.d(TAG, "onGameStartStatus");
        if (mBinding == null) return;
        this.isGamer = isGamer;
        mBinding.ilIDLE.ivGameTips.setVisibility(View.GONE);
        mBinding.ilIDLE.tvSongTab.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.btGameStart.setVisibility(View.GONE);
        mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.messageText.setText(R.string.ktv_game_start);
        startTimer();
    }

    // 下一段前提示
    public void onBattleGamePrepare(boolean isWinner) {
        KTVLogger.d(TAG, "onBattleGamePrepare， isWinner: " + isWinner);
        if (mBinding == null || !isGamer) return;
        mBinding.ilActive.lrcControlView.onGraspDisable();
        if (isWinner) {
            mBinding.ilActive.messageText.setText(R.string.ktv_next_round_singer_tips);
        } else {
            mBinding.ilActive.messageText.setText(R.string.ktv_next_round_listener_tips);
        }
        mBinding.ilActive.messageText.setVisibility(View.VISIBLE);
        mBinding.ilActive.messageText.bringToFront();
        mBinding.getRoot().postDelayed(() -> {
            if (mBinding == null) return;
            mBinding.ilActive.messageText.setVisibility(View.GONE);
        }, 3000);
    }

    @SuppressLint("SetTextI18n")
    public void onGraspSongBegin() {
        if (mBinding == null) return;
        KTVLogger.d(TAG, "onGraspSongBegin: " + partNum);
        this.partNum = partNum + 1;
        mBinding.ilActive.tvSongNumTab.setText(partNum + "/5");
        if (partNum < 5 && isGamer) {
            mBinding.ilActive.lrcControlView.onGraspEnable();
        } else {
            mBinding.ilActive.lrcControlView.onGraspDisable();
        }
    }

    // 抢唱成功
    public void onGraspSongSuccess(String userName, String headUrl) {
        KTVLogger.d(TAG, "onGraspSongSuccess， userName：" + userName);
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
        mBinding.ilActive.tvBattleResultName.setText(getResources().getString(R.string.ktv_winner_tip, userName));
        mBinding.ilActive.tvBattleResultView.setVisibility(View.VISIBLE);
        mBinding.ilActive.tvBattleResultView.bringToFront();

        GlideApp.with(mBinding.getRoot())
                .load(headUrl)
                .error(R.mipmap.userimage)
                .transform(new CenterCropRoundCornerTransform(100))
                .into(mBinding.ilActive.ivWinnerHeader);
        mBinding.ilActive.ivWinnerName.setText(getResources().getString(R.string.ktv_next_round_singer, userName));
        mBinding.ilActive.winnerTips.setVisibility(VISIBLE);
        mBinding.getRoot().postDelayed(() -> {
            if (mBinding == null) return;
            mBinding.ilActive.messageText.setVisibility(View.GONE);
            mBinding.ilActive.tvBattleResultView.setVisibility(View.GONE);
        }, 3000);
    }

    // 下一段歌曲开始播放
    public void onNextPart(boolean isWinner) {
        KTVLogger.d(TAG, "onNextPart, isWinner:" + isWinner);
        if (mBinding == null) return;
        mBinding.ilActive.winnerTips.setVisibility(GONE);
        if (isWinner) {
            mBinding.ilActive.lrcControlView.changeMusicController(LrcControlView.Role.Singer);
        } else {
            mBinding.ilActive.lrcControlView.changeMusicController(LrcControlView.Role.Listener);
        }
    }

    // 歌曲演唱结束
    public void onSongFinish() {
        KTVLogger.d(TAG, "onSongFinish");
        if (mBinding == null) return;
        if (mSingRelayGameEventListener != null) mSingRelayGameEventListener.onGameEnd();
    }

    // 游戏结束
    public void onGameEnd(List<RankItem> list) {
        KTVLogger.d(TAG, "onGameEnd");
        if (mBinding == null) return;
        //mBinding.ilIDLE.messageText.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.ivGameTips.setVisibility(View.GONE);
        mBinding.ilActive.winnerTips.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        mBinding.ilRank.setVisibility(View.VISIBLE);
        mBinding.ilIDLE.messageText.setBackgroundResource(R.mipmap.ktv_game_idle_text_background);

        if (isRoomOwner) {
            mBinding.btGameAgain.setVisibility(View.VISIBLE);
        } else {
            mBinding.ilIDLE.messageText.setVisibility(View.GONE);
        }
        mBinding.ilRank.resetRankList(list);
        partNum = 1;
        isGamer = false;
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
         * 抢唱-游戏结束
         */
        default void onGameEnd() {}

        /**
         * 抢唱-再来一轮
         */
        default void onGameAgainClick() {}
    }
}

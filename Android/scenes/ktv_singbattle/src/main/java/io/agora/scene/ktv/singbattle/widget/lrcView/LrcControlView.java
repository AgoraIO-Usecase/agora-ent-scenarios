package io.agora.scene.ktv.singbattle.widget.lrcView;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.karaoke_view_ex.KaraokeEvent;
import io.agora.karaoke_view_ex.KaraokeView;
import io.agora.karaoke_view_ex.LyricsView;
import io.agora.karaoke_view_ex.ScoringView;
import io.agora.karaoke_view_ex.constants.DownloadError;
import io.agora.karaoke_view_ex.downloader.LyricsFileDownloader;
import io.agora.karaoke_view_ex.downloader.LyricsFileDownloaderCallback;
import io.agora.karaoke_view_ex.internal.model.LyricsLineModel;
import io.agora.karaoke_view_ex.model.LyricModel;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.ktv.singbattle.KTVLogger;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleLayoutLrcControlViewBinding;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleLayoutLrcPrepareBinding;
import io.agora.ktvapi.ILrcView;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.widget.basic.OutlineSpan;
import io.agora.scene.widget.toast.CustomToast;
import io.agora.scene.widget.utils.UiUtils;
import kotlin.jvm.Volatile;

/**
 * Lyrics control view
 */
public class LrcControlView extends FrameLayout implements View.OnClickListener, ILrcView {

    private final String tag = "LrcControlView";

    protected KtvSingbattleLayoutLrcControlViewBinding mBinding;
    protected KtvSingbattleLayoutLrcPrepareBinding mPrepareBinding;

    protected KaraokeView mKaraokeView;

    protected int mCumulativeScoreInPercentage;

    public int getCumulativeScoreInPercentage() {
        return mCumulativeScoreInPercentage;
    }

    protected ComboControl mComboControl;

    public LyricsView getLyricsView() {
        if (mBinding != null && mBinding.ilActive != null) {
            return mBinding.ilActive.lyricsView;
        } else {
            return null;
        }
    }

    public ScoringView getScoringView() {
        return mBinding.ilActive.scoringView;
    }

    public KaraokeView getKaraokeView() {
        return mKaraokeView;
    }

    public enum Role {
        Singer, Listener
    }

    public Role mRole = Role.Listener;
    private OnKaraokeEventListener mOnKaraokeActionListener;

    private boolean isOnSeat = false;

    public void onSeat(boolean isOnSeat) {
        this.isOnSeat = isOnSeat;
    }

    public LrcControlView(@NonNull Context context) {
        this(context, null);
    }

    public LrcControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        mBinding = KtvSingbattleLayoutLrcControlViewBinding.inflate(LayoutInflater.from(context), this, true);

        mPrepareBinding = KtvSingbattleLayoutLrcPrepareBinding.bind(mBinding.getRoot());

        mBinding.ilIDLE.getRoot().setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);

        mKaraokeView = new KaraokeView(mBinding.ilActive.lyricsView, mBinding.ilActive.scoringView);

        initListener();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBinding = null;
        mPrepareBinding = null;
    }

    private void initListener() {
        mBinding.ilActive.switchOriginal.setOnClickListener(this);
        mBinding.ilActive.ivMusicMenu.setOnClickListener(this);
        mBinding.ilActive.ivMusicStart.setOnClickListener(this);
        mBinding.ilActive.ivChangeSong.setOnClickListener(this);
        mBinding.ilActive.downloadLrcFailedBtn.setOnClickListener(this);
        mBinding.ilActive.singBattle.setOnClickListener(this);
        mBinding.ilActive.singBattle.setEnabled(false);

        mKaraokeView.setKaraokeEvent(new KaraokeEvent() {
            @Override
            public void onDragTo(KaraokeView view, long position) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onDragTo(position);
                }
            }

            @Override
            public void onLineFinished(KaraokeView view, LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
//                if (mRole == Role.Singer && mOnKaraokeActionListener != null) {
//                    mOnKaraokeActionListener.onLineFinished(line, score, cumulativeScore, index, total);
//                }
            }
        });
    }

    public void setOnLrcClickListener(OnKaraokeEventListener karaokeActionListener) {
        this.mOnKaraokeActionListener = karaokeActionListener;
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(3 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);

                if (second <= 2) {
                    setCountDown(second);
                }
            }

            @Override
            public void onFinish() {
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

    @SuppressLint("SetTextI18n")
    private void setCountDown(int second) {
        if (mBinding == null) return;
        mBinding.ilActive.singBattle.setText("" + (second + 1));
    }

    private void onCountFinished() {
        if (mBinding == null) return;
        mBinding.ilActive.singBattle.setEnabled(true);
        mBinding.ilActive.singBattle.setText("");
        mBinding.ilActive.singBattle.setBackgroundResource(R.mipmap.ktv_singbattle_start_grasp);
    }

    public void startTimerCount() {
        startTimer();
    }

    private boolean isMineOwner = false;

    public void onPrepareStatus(boolean isMineOwner) {
        this.isMineOwner = isMineOwner;
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);

        changeViewByRole();
    }

    private RoomSelSongModel songPlaying;

    public void onPlayStatus(RoomSelSongModel songPlaying) {
        //if (isPrepareSong) startTimerCount();
        this.songPlaying = songPlaying;

        if (mBinding == null) return;
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);

        mBinding.ilActive.ivMusicStart.setIconResource(R.mipmap.ktv_ic_pause);
        mBinding.ilActive.ivMusicStart.setText(R.string.ktv_singbattle_room_player_pause);
    }

    public void startSingBattlePrepareTimeCount() {
        if (isPrepareSong) startTimerCount();
    }

    private void changeViewByRole() {
        KTVLogger.d("LrcView", "changeViewByRole, role: " + mRole);
        if (mBinding == null) return;
        mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
        mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.INVISIBLE);
        if (this.mRole == Role.Singer) {
            mBinding.ilActive.lyricsView.enableDragging(false);
            mBinding.ilActive.switchOriginal.setChecked(false); // reset ui icon for mAudioTrackMode
            mBinding.ilActive.switchOriginal.setIconResource(io.agora.scene.widget.R.mipmap.ic_play_original_off);
            if (isPrepareSong) {
                mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
            } else {
                mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            }
        } else if (this.mRole == Role.Listener) {
            mBinding.ilActive.lyricsView.enableDragging(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        }
    }

    public void onPauseStatus() {
        if (mBinding == null) return;
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);

        mBinding.ilActive.ivMusicStart.setIconResource(R.drawable.ktv_ic_play);
        mBinding.ilActive.ivMusicStart.setText(R.string.ktv_singbattle_room_player_play);
    }

    public void onIdleStatus() {
        if (mBinding == null) return;
        mBinding.ilIDLE.getRoot().setVisibility(View.VISIBLE);
        mBinding.clActive.setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
    }

    private boolean isPrepareSong = true;

    public void onGameBattlePrepareStatus() {
        if (mBinding == null) return;
        isPrepareSong = true;
        mBinding.ilActive.scoringView.setVisibility(View.GONE);
        mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        mBinding.tvMusicName.setVisibility(View.GONE);
        mBinding.tvCumulativeScore.setVisibility(View.GONE);
        mBinding.gradeView.setVisibility(View.GONE);
        mBinding.comboView.getRoot().setVisibility(View.GONE);
        mBinding.lineScore.setVisibility(View.GONE);
        mBinding.ilActive.tvMusicName2.setVisibility(View.VISIBLE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.lyricsView).getLayoutParams();
        params.topToBottom = R.id.tvMusicName2;
        params.bottomToTop = R.id.singBattle;
        findViewById(R.id.lyricsView).requestLayout();

        if (isOnSeat) {
            mBinding.ilActive.singBattle.setVisibility(View.VISIBLE);
            mBinding.ilActive.singBattle.setEnabled(false);
            mBinding.ilActive.singBattle.setBackgroundResource(R.mipmap.ktv_singbattle_start_grasp_waiting);
        } else {
            mBinding.ilActive.singBattle.setVisibility(View.GONE);
        }
    }

    public void onGamingStatus() {
        if (mBinding == null) return;
        isPrepareSong = false;
        mBinding.ilActive.scoringView.setVisibility(View.VISIBLE);
        if (mRole == Role.Singer) {
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
        } else {
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        }
        mBinding.tvMusicName.setVisibility(View.VISIBLE);
        mBinding.tvCumulativeScore.setVisibility(View.VISIBLE);
        mBinding.gradeView.setVisibility(View.VISIBLE);
        mBinding.comboView.getRoot().setVisibility(View.VISIBLE);
        mBinding.lineScore.setVisibility(View.VISIBLE);
        mBinding.ilActive.tvMusicName2.setVisibility(View.GONE);
        mBinding.ilActive.singBattle.setVisibility(View.GONE);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) findViewById(R.id.lyricsView).getLayoutParams();
        params.topToBottom = R.id.scoringView;
        params.bottomToTop = R.id.bgd_control_layout_lrc;
        findViewById(R.id.lyricsView).requestLayout();
    }

    public void onGraspDiasble() {
        if (mBinding == null) return;
        mBinding.ilActive.singBattle.setVisibility(View.INVISIBLE);
    }

    public void setRole(@NonNull Role mRole) {
        KTVLogger.d("LrcView", "setRole: " + mRole);
        this.mRole = mRole;
        lrcUrl = null;
        changeViewByRole();
    }

    public Role getRole() {
        return this.mRole;
    }

    public void setMusic(RoomSelSongModel mMusic) {
        if (mBinding == null) return;
        mKaraokeView.reset();
        if (mComboControl != null) {
            mComboControl.reset(mBinding);
        }
        mBinding.ivCumulativeScoreGrade.setVisibility(INVISIBLE);
        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_singbattle_score_formatter), "0"));
        mBinding.gradeView.setScore(0, 0, 0);

        if (mMusic == null) return;

        if (mMusic.getWinnerNo() == null || !mMusic.getWinnerNo().equals("")) {
            onGamingStatus();
        }
        mBinding.tvMusicName.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
        mBinding.ilActive.tvMusicName2.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
    }

    private int backgroundResId = io.agora.scene.widget.R.mipmap.mvbg4;

    public void setLrcViewBackground(@DrawableRes int resId) {
        backgroundResId = resId;
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Palette.from(mBitmap).generate(palette -> {
            if (palette == null) {
                return;
            }
            int defaultColor = ContextCompat.getColor(getContext(), io.agora.scene.widget.R.color.pink_b4);
            mBinding.ilActive.lyricsView.setCurrentLineHighlightedTextColor(defaultColor);

            defaultColor = ContextCompat.getColor(getContext(), io.agora.scene.widget.R.color.white);
            mBinding.ilActive.lyricsView.setCurrentLineTextColor(defaultColor);
        });
        mBinding.clActive.setBackgroundResource(resId);
    }

    public void updateScore(double score, double cumulativeScore, double perfectScore) {
        if (isPrepareSong) return;
        KTVLogger.d("hugo", "updateScore, score: " + score + " cumulativeScore: " + cumulativeScore + " perfectScore: " + totalScore);
        mCumulativeScoreInPercentage = (int) ((cumulativeScore / totalScore) * 100);

        mBinding.gradeView.setScore((int) score, (int) cumulativeScore, (int) totalScore);

        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_singbattle_score_formatter), "" + (int) cumulativeScore));
        int gradeDrawable = mBinding.gradeView.getCumulativeDrawable();
        if (gradeDrawable == 0) {
            mBinding.ivCumulativeScoreGrade.setVisibility(INVISIBLE);
        } else {
            mBinding.ivCumulativeScoreGrade.setImageResource(gradeDrawable);
            mBinding.ivCumulativeScoreGrade.setVisibility(VISIBLE);
        }

        if (mComboControl == null) {
            mComboControl = new ComboControl();
        }
        mComboControl.checkAndShowCombos(mBinding, (int) score, (int) cumulativeScore);
    }

    private static class ComboControl {
        private GifDrawable mComboIconDrawable;

        private int mNumberOfCombos;

        private void reset(KtvSingbattleLayoutLrcControlViewBinding binding) {
            mNumberOfCombos = 0;
            binding.comboView.getRoot().setVisibility(INVISIBLE);
        }

        private void checkAndShowCombos(KtvSingbattleLayoutLrcControlViewBinding binding, int score, int cumulativeScore) {
            binding.comboView.getRoot().setVisibility(VISIBLE);

            showComboAnimation(binding.comboView.getRoot(), score);
            showScoreAnimation((View) binding.comboView.getRoot().getParent(), score);
        }

        private int mComboOfLastTime; // Only for showComboAnimation

        private void showComboAnimation(View comboView, int score) {
            int comboIconRes = 0;

            if (score >= 90) {
                comboIconRes = R.drawable.combo_excellent_3x;
            } else if (score >= 75) {
                comboIconRes = R.drawable.combo_good_3x;
            } else if (score >= 60) {
                comboIconRes = R.drawable.combo_fair_3x;
            }

            ImageView comboIcon = comboView.findViewById(R.id.combo_icon);
            TextView comboText = comboView.findViewById(R.id.combo_text);

            boolean sameWithLastTime = (comboIconRes == mComboOfLastTime);
            mComboOfLastTime = comboIconRes;

            if (comboIconRes > 0) {
                if (sameWithLastTime) {
                    mNumberOfCombos++;
                } else {
                    mNumberOfCombos = 1;
                }

                RequestOptions options = new RequestOptions().diskCacheStrategy(DiskCacheStrategy.RESOURCE);
                OutlineSpan outlineSpan = new OutlineSpan(Color.parseColor("#368CFF"), 10F
                );
                Glide.with(comboView.getContext()).asGif().load(comboIconRes).apply(options).addListener(new RequestListener<GifDrawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                        return true;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                        mComboIconDrawable = resource;

                        resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
                            @Override
                            public void onAnimationStart(Drawable drawable) {
                                super.onAnimationStart(drawable);
                            }

                            @Override
                            public void onAnimationEnd(Drawable drawable) {
                                super.onAnimationEnd(drawable);

                                comboText.setAlpha(0f);
                                comboIcon.setVisibility(INVISIBLE);
                                comboText.setVisibility(INVISIBLE);

                                mComboIconDrawable.unregisterAnimationCallback(this);
                            }
                        });

                        resource.setLoopCount(1);

                        comboIcon.setVisibility(VISIBLE);

                        comboText.setAlpha(0f);
                        comboText.setVisibility(mNumberOfCombos == 1 ? INVISIBLE : VISIBLE); // Per request from product team, do not show `+X` view for first one
                        if (mNumberOfCombos != 1) {
                            String text = "x" + mNumberOfCombos;
                            SpannableString spannable = new SpannableString(text);
                            spannable.setSpan(outlineSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            comboText.setText(spannable);
                            comboText.animate().alpha(1f).setDuration(500).setStartDelay(0).start();
                        }

                        return false;
                    }
                }).into(comboIcon);
            } else {
                mNumberOfCombos = 0;
                comboIcon.setVisibility(INVISIBLE);
                comboText.setVisibility(INVISIBLE);
            }
        }

        private float mInitialYOfScoreView; // Only for showScoreAnimation

        @SuppressLint("SetTextI18n")
        private void showScoreAnimation(View lyricsControlView, double score) {
            TextView lineScore = lyricsControlView.findViewById(R.id.line_score);
            int widthOfParent = ((View) (lineScore.getParent())).getWidth();
            int marginLeft = (int) (widthOfParent * 0.4);
            ((MarginLayoutParams) (lineScore.getLayoutParams())).leftMargin = marginLeft;
            ((MarginLayoutParams) (lineScore.getLayoutParams())).setMarginStart(marginLeft);

            lineScore.setText("+" + (int) score);
            lineScore.setAlpha(1.0f);
            lineScore.setVisibility(VISIBLE);
            if (mInitialYOfScoreView == 0) {
                mInitialYOfScoreView = lineScore.getY();
            }

            float movingPixels = 200;
            lineScore.animate().translationY(-movingPixels).setDuration(1000).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    lineScore.animate().alpha(0).setDuration(100).setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            lineScore.setY(mInitialYOfScoreView);
                            lineScore.setVisibility(INVISIBLE);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    }).start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            }).start();

        }

    }

    @Override
    public void onClick(View v) {
        if (v == mBinding.ilActive.switchOriginal) {
            mOnKaraokeActionListener.onSwitchOriginalClick();

            boolean withOriginal = mBinding.ilActive.switchOriginal.isChecked();
            mBinding.ilActive.switchOriginal.setIconResource(withOriginal ? io.agora.scene.widget.R.mipmap.ic_play_original_on : io.agora.scene.widget.R.mipmap.ic_play_original_off);
        } else if (v == mBinding.ilActive.ivMusicMenu) {
            mOnKaraokeActionListener.onMenuClick();
        } else if (v == mBinding.ilActive.ivMusicStart) {
            mOnKaraokeActionListener.onPlayClick();
        } else if (v == mBinding.ilActive.ivChangeSong) {
            mOnKaraokeActionListener.onChangeMusicClick();
        } else if (v == mBinding.ilActive.downloadLrcFailedBtn) {
            mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
            mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.INVISIBLE);
            if (lrcUrl == null) {
                mOnKaraokeActionListener.onReGetLrcUrl();
            } else {
                downloadAndSetLrcData();
            }
        } else if (v == mBinding.ilActive.singBattle) {
            if (UiUtils.isFastClick(2000)) {
                return;
            }
            mOnKaraokeActionListener.onGraspSongClick();
        }
    }

    public void setSwitchOriginalChecked(boolean checked) {
        mBinding.ilActive.switchOriginal.setChecked(checked);
    }

    // ------------------ ILrcView ------------------
    @Override
    public void onUpdatePitch(Float pitch) {
        if (mKaraokeView == null) return;
        mKaraokeView.setPitch(pitch, -1);
    }

    @Override
    public void onUpdateProgress(Long progress) {
        if (mKaraokeView.getLyricData() == null) return;
        mKaraokeView.setProgress(progress);
    }

    private String lrcUrl;
    @Volatile
    private int highStartTime;
    @Volatile
    private int highEndTime;

    @Volatile
    private LyricModel mLyricsModel = null;

    @Override
    public void onDownloadLrcData(String url) {
        this.lrcUrl = url;
        downloadAndSetLrcData();
    }

    @Override
    public void onHighPartTime(long highStartTime, long highEndTime) {
        this.highStartTime = (int) highStartTime;
        this.highEndTime = (int) highEndTime;
        if (mLyricsModel != null) {
            // Lyrics download success
            LyricModel cutLyricsModel = dealWithBattleSong(mLyricsModel);
            mKaraokeView.setLyricData(cutLyricsModel, false);
        }
    }

    private void downloadAndSetLrcData() {
        Context context = AgoraApplication.the();
        LyricsFileDownloader.getInstance(context).setLyricsFileDownloaderCallback(new LyricsFileDownloaderCallback() {
            @Override
            public void onLyricsFileDownloadProgress(int requestId, float progress) {

            }

            @Override
            public void onLyricsFileDownloadCompleted(int requestId, byte[] fileData, DownloadError error) {
                if (error == null) {
                    LyricModel lyricsModel = KaraokeView.parseLyricData(fileData, null);
                    if (lyricsModel == null) {
                        CustomToast.show("Unexpected parseLyricsData", Toast.LENGTH_SHORT);
                        if (mBinding != null) {
                            mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                            mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                        }
                        return;
                    }
                    if (mKaraokeView != null) {
                        if (mBinding != null) {
                            mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
                        }
                        if (highStartTime != 0) { // onHighPartTime callback
                            LyricModel cutLyricsModel = dealWithBattleSong(lyricsModel);
                            mKaraokeView.setLyricData(cutLyricsModel, false);
                        } else {
                            mLyricsModel = lyricsModel;
                            // TODO: 2024/12/2 mock
                            mKaraokeView.setLyricData(mLyricsModel, false);
                        }
                    }
                } else {
                    if (error.getMessage() != null) {
                        CustomToast.show(error.getMessage(),Toast.LENGTH_SHORT);
                    }
                    if (mBinding != null) {
                        mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                        mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        LyricsFileDownloader.getInstance(context).download(lrcUrl);
    }

    public void onNoLrc() {
        lrcUrl = null;
        mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
        mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
    }

    private int totalScore = 0;

    private LyricModel dealWithBattleSong(LyricModel lyricsModel) {
        KTVLogger.d(tag, "dealWithBattleSong");
        List<LyricsCutter.Line> cutterLines = new ArrayList<>();
        for (int i = 0; i < lyricsModel.lines.size(); i++) {
            LyricsLineModel lyricsLine = lyricsModel.lines.get(i);
            long durationOfCurrentLine = lyricsLine.getEndTime() - lyricsLine.getStartTime();
            cutterLines.add(new LyricsCutter.Line(lyricsLine.getStartTime(), durationOfCurrentLine));
        }
        KTVLogger.d(tag, "handleFixTime1 highStartTime " + highStartTime + songPlaying);
        Pair<Integer, Integer> res = LyricsCutter.handleFixTime((int) highStartTime, (int) highEndTime, cutterLines);
        if (res != null) {
            highStartTime = res.first;
            highEndTime = res.second;
        }
        KTVLogger.d(tag, "handleFixTime2 highStartTime " + highStartTime);
        LyricModel cutLyricsModel = LyricsCutter.cut(lyricsModel, (int) highStartTime, (int) highEndTime);
        AtomicInteger lineCount = new AtomicInteger();
        cutLyricsModel.lines.forEach(line -> {
            if (line.getStartTime() >= highStartTime && line.getEndTime() <= highEndTime) {
                lineCount.getAndIncrement();
            }
        });
        totalScore = lineCount.get() * 100;
        KTVLogger.d(tag, "totalScore: " + totalScore);
        highStartTime = 0;
        highEndTime = 0;
        mLyricsModel = null;
        return cutLyricsModel;
    }

    public void onReceiveSingleLineScore(int score, int index, int cumulativeScore, int total) {
        if (mRole == Role.Listener) {
            updateScore(score, cumulativeScore, /** Workaround(Hai_Guo)*/total);
        }
    }

    public interface OnKaraokeEventListener {
        default void onSwitchOriginalClick() {
        }

        default void onMenuClick() {
        }

        default void onPlayClick() {
        }

        default void onChangeMusicClick() {
        }

        default void onDragTo(long position) {
        }

        default void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
        }

        default void onLineFinished(LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
        }

        default void onReGetLrcUrl() {
        }

        default void onGraspSongClick() {
        }
    }
}

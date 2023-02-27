package io.agora.scene.ktv.widget;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.File;
import java.util.List;

import io.agora.karaoke_view.DownloadManager;
import io.agora.karaoke_view.v11.KaraokeEvent;
import io.agora.karaoke_view.v11.KaraokeView;
import io.agora.karaoke_view.v11.LyricsView;
import io.agora.karaoke_view.v11.ScoringView;
import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.ZipUtils;
import io.agora.scene.ktv.KTVLogger;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvLayoutLrcControlViewBinding;
import io.agora.scene.ktv.databinding.KtvLayoutLrcPrepareBinding;
import io.agora.scene.ktv.service.RoomSelSongModel;
import io.agora.scene.widget.basic.OutlineSpan;

/**
 * 歌词控制View
 */
public class LrcControlView extends FrameLayout implements View.OnClickListener {

    protected KtvLayoutLrcControlViewBinding mBinding;
    protected KtvLayoutLrcPrepareBinding mPrepareBinding;

    protected KaraokeView mKaraokeView;

    protected int mCumulativeScore;

    public int getCumulativeScore() {
        return mCumulativeScore;
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
        Singer, Listener, Partner
    }

    private Role mRole = Role.Listener;
    private OnKaraokeEventListener mOnKaraokeActionListener;

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
        mBinding = KtvLayoutLrcControlViewBinding.inflate(LayoutInflater.from(context), this, true);

        mPrepareBinding = KtvLayoutLrcPrepareBinding.bind(mBinding.getRoot());

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
        mBinding.ilChorus.btChorus.setOnClickListener(this);
        mBinding.ilActive.switchOriginal.setOnClickListener(this);
        mBinding.ilActive.switchOriginal.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                mBinding.ilActive.switchOriginal.setText(R.string.ktv_room_original);
            } else {
                mBinding.ilActive.switchOriginal.setText(R.string.ktv_room_accompany);
            }
        });
        mBinding.ilActive.ivMusicMenu.setOnClickListener(this);
        mBinding.ilActive.ivMusicStart.setOnClickListener(this);
        mBinding.ilActive.ivChangeSong.setOnClickListener(this);
        mBinding.ilActive.ivSkipPostludeSkip.setOnClickListener(this);
        mBinding.ilActive.ivSkipPreludeSkip.setOnClickListener(this);
        mBinding.ilActive.ivSkipPostludeCancel.setOnClickListener(this);
        mBinding.ilActive.ivSkipPreludeCancel.setOnClickListener(this);

        mKaraokeView.setKaraokeEvent(new KaraokeEvent() {
            @Override
            public void onDragTo(KaraokeView view, long position) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onDragTo(position);
                }
            }

            @Override
            public void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onRefPitchUpdate(refPitch, numberOfRefPitches);
                }
            }

            @Override
            public void onLineFinished(KaraokeView view, LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onLineFinished(line, score, cumulativeScore, index, total);
                }
            }
        });
    }

    public void setOnLrcClickListener(OnKaraokeEventListener karaokeActionListener) {
        this.mOnKaraokeActionListener = karaokeActionListener;
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(20 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);

                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onCountTime(second);
                }

                setCountDown(second);
            }

            @Override
            public void onFinish() {
                mOnKaraokeActionListener.onWaitTimeOut();
            }
        }.start();
    }

    private void stopTimer() {
        if (mCountDownLatch != null) {
            mCountDownLatch.cancel();
            mCountDownLatch = null;
        }
    }

    public void onWaitChorusStatus() {
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.ilChorus.getRoot().setVisibility(View.VISIBLE);
        mBinding.clActive.setBackgroundResource(R.mipmap.ktv_mv_default);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);

        if (mRole == Role.Singer) {
            mBinding.ilChorus.tvWaitingTime.setText(getContext().getString(R.string.ktv_room_time_wait_join_chorus));
            mBinding.ilChorus.btChorus.setText(R.string.ktv_music_chorus_start_now);

        } else if (mRole == Role.Listener) {
            mBinding.ilChorus.tvWaitingTime.setText(getContext().getString(R.string.ktv_room_time_join_chorus_));
            mBinding.ilChorus.btChorus.setText(R.string.ktv_music_join_chorus);
        }

        if (mRole == Role.Singer) {
            startTimer();
        }
    }

    public void onMemberJoinedChorus() {
        mBinding.ilActive.lyricsView.enableDragging(false);
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.ilChorus.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        stopTimer();
    }

    public void onPrepareStatus(boolean isMineOwner) {
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.ilChorus.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.VISIBLE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
        if (this.mRole == Role.Singer) {
            mBinding.ilActive.lyricsView.enableDragging(true);
            mBinding.ilActive.ivMusicStart.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
            mBinding.ilActive.switchOriginal.setChecked(false);
        } else if (this.mRole == Role.Listener) {
            mBinding.ilActive.lyricsView.enableDragging(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        }
        if (isMineOwner) {
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicStart.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
            if (this.mRole == Role.Listener) {
                mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
                mBinding.ilActive.switchOriginal.setVisibility(View.INVISIBLE);
                mBinding.ilActive.ivMusicMenu.setVisibility(View.INVISIBLE);
                mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
                mBinding.ilActive.ivSkipPrelude.setVisibility(View.INVISIBLE);
            } else if (this.mRole == Role.Partner) {
                mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
                mBinding.ilActive.ivSkipPrelude.setVisibility(View.INVISIBLE);
            }
        }
        stopTimer();
    }

    public void onPlayStatus(RoomSelSongModel songPlaying) {
        stopTimer();
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        setScoreControlView(songPlaying);
        mBinding.ilChorus.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);
        mBinding.ilActive.ivMusicStart.setImageResource(R.mipmap.ktv_ic_pause);
    }

    private boolean mNeedToShowComboView;

    public void setScoreControlView(RoomSelSongModel songPlaying) {
        if (songPlaying != null && songPlaying.isChorus()) {
            mNeedToShowComboView = UserManager.getInstance().getUser().id.toString().equals(songPlaying.getUserNo());
        } else if (songPlaying != null && !songPlaying.isChorus()) {
            mNeedToShowComboView = true;
        } else {
            mNeedToShowComboView = false;
        }
    }

    public void onPauseStatus() {
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.ilChorus.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);

        mBinding.ilActive.ivMusicStart.setImageResource(R.drawable.ktv_ic_play);
    }

    public void onIdleStatus() {
        mBinding.ilIDLE.getRoot().setVisibility(View.VISIBLE);
        mBinding.clActive.setVisibility(View.GONE);
        mBinding.ilChorus.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);

        stopTimer();
    }

    public void setRole(@NonNull Role mRole) {
        this.mRole = mRole;
        if (this.mRole == Role.Singer) {
            mBinding.ilActive.lyricsView.enableDragging(true);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setChecked(false);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
        } else if (this.mRole == Role.Partner) {
            mBinding.ilActive.switchOriginal.setChecked(false);
            mBinding.ilActive.lyricsView.enableDragging(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
            mBinding.ilActive.ivChangeSong.setVisibility(View.INVISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivSkipPrelude.setVisibility(View.INVISIBLE);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
        } else if (this.mRole == Role.Listener) {
            mBinding.ilActive.lyricsView.enableDragging(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.INVISIBLE);
        }
    }

    public void setMusic(@NonNull RoomSelSongModel mMusic) {
        mKaraokeView.reset();
        if (mComboControl != null) {
            mComboControl.reset(mBinding);
        }

        mBinding.tvMusicName.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
        mBinding.ilChorus.tvMusicName2.setText(mMusic.getSongName() + "-" + mMusic.getSinger());

        mBinding.ivCumulativeScoreGrade.setVisibility(INVISIBLE);
        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_score_formatter), "0"));
        mBinding.gradeView.setScore(0, 0, 0);
    }

    public void setCountDown(int time) {
        if (mBinding == null || mBinding.ilChorus == null) return;
        if (mRole == Role.Singer) {
            mBinding.ilChorus.tvWaitingTime.setText(getContext().getString(R.string.ktv_room_time_wait_join_chorus));
        } else if (mRole == Role.Listener) {
            mBinding.ilChorus.tvWaitingTime.setText(getContext().getString(R.string.ktv_room_time_join_chorus_));
        }
        mBinding.ilChorus.tvWaitingTimeCount.setText(getContext().getString(R.string.ktv_room_time_wait, 0, time));
    }

    private int backgroundResId = R.mipmap.ktv_mv_default;

    public void setLrcViewBackground(@DrawableRes int resId) {
        backgroundResId = resId;
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Palette.from(mBitmap).generate(palette -> {
            if (palette == null) {
                return;
            }
            int defaultColor = ContextCompat.getColor(getContext(), R.color.yellow_60);
            mBinding.ilActive.lyricsView.setCurrentHighlightedTextColor(defaultColor);

            defaultColor = ContextCompat.getColor(getContext(), R.color.white);
            mBinding.ilActive.lyricsView.setCurrentTextColor(defaultColor);
        });
        mBinding.clActive.setBackgroundResource(resId);
    }

    public void updateScore(double score, double cumulativeScore, double perfectScore) {
        mCumulativeScore = (int) cumulativeScore;

        mBinding.gradeView.setScore((int) score, (int) cumulativeScore, (int) perfectScore);

        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_score_formatter), "" + mCumulativeScore));
        int gradeDrawable = mBinding.gradeView.getCumulativeDrawable();
        if (gradeDrawable == 0) {
            mBinding.ivCumulativeScoreGrade.setVisibility(INVISIBLE);
        } else {
            mBinding.ivCumulativeScoreGrade.setImageResource(gradeDrawable);
            mBinding.ivCumulativeScoreGrade.setVisibility(VISIBLE);
        }

        if (!mNeedToShowComboView) {
            return;
        }

        if (mComboControl == null) {
            mComboControl = new ComboControl();
        }
        mComboControl.checkAndShowCombos(mBinding, (int) score, (int) cumulativeScore);
    }

    private static class ComboControl {
        private GifDrawable mComboIconDrawable;

        private int mNumberOfCombos;

        private void reset(KtvLayoutLrcControlViewBinding binding) {
            mNumberOfCombos = 0;
            binding.comboView.getRoot().setVisibility(INVISIBLE);
        }

        private void checkAndShowCombos(KtvLayoutLrcControlViewBinding binding, int score, int cumulativeScore) {
            binding.comboView.getRoot().setVisibility(VISIBLE);

            showComboAnimation(binding.comboView.getRoot(), score);
            showScoreAnimation((View) binding.comboView.getRoot().getParent(), score);
        }

        private int mComboOfLastTime;

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

                        comboText.setAlpha(0f);
                        comboIcon.setVisibility(VISIBLE);
                        comboText.setVisibility(VISIBLE);

                        String text = "x" + mNumberOfCombos;
                        SpannableString spannable = new SpannableString(text);
                        spannable.setSpan(outlineSpan, 0, text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        comboText.setText(spannable);
                        comboText.animate().alpha(1f).setDuration(500).setStartDelay(0).start();

                        return false;
                    }
                }).into(comboIcon);
            } else {
                mNumberOfCombos = 0;
                comboIcon.setVisibility(INVISIBLE);
                comboText.setVisibility(INVISIBLE);
            }
        }

        private void showScoreAnimation(View lyricsControlView, double score) {
            if (score == 0) {
                return;
            }
            TextView lineScore = lyricsControlView.findViewById(R.id.line_score);
            int widthOfParent = ((View) (lineScore.getParent())).getWidth();
            int marginLeft = (int) (widthOfParent * 0.4);
            ((MarginLayoutParams) (lineScore.getLayoutParams())).leftMargin = marginLeft;
            ((MarginLayoutParams) (lineScore.getLayoutParams())).setMarginStart(marginLeft);

            lineScore.setText("+" + (int) score);
            lineScore.setAlpha(1.0f);
            lineScore.setVisibility(VISIBLE);
            float yOfScore = lineScore.getY();

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
                            lineScore.setY(yOfScore);
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
        } else if (v == mBinding.ilActive.ivMusicMenu) {
            mOnKaraokeActionListener.onMenuClick();
        } else if (v == mBinding.ilActive.ivMusicStart) {
            mOnKaraokeActionListener.onPlayClick();
        } else if (v == mBinding.ilActive.ivChangeSong) {
            mOnKaraokeActionListener.onChangeMusicClick();
        } else if (v == mBinding.ilChorus.btChorus) {
            if (mRole == Role.Singer) {
                mOnKaraokeActionListener.onStartSing();
            } else if (mRole == Role.Listener) {
                mOnKaraokeActionListener.onJoinChorus();
            }
        } else if (v == mBinding.ilActive.ivSkipPreludeSkip) {
            mOnKaraokeActionListener.onSkipPreludeClick();
            mBinding.ilActive.ivSkipPrelude.setVisibility(INVISIBLE);
        } else if (v == mBinding.ilActive.ivSkipPostludeSkip) {
            mOnKaraokeActionListener.onSkipPostludeClick();
        } else if (v == mBinding.ilActive.ivSkipPreludeCancel) {
            mBinding.ilActive.ivSkipPrelude.setVisibility(INVISIBLE);
        } else if (v == mBinding.ilActive.ivSkipPostludeCancel) {
            mBinding.ilActive.ivSkipPostlude.setVisibility(INVISIBLE);
        }
    }

    public void setSwitchOriginalChecked(boolean checked) {
        mBinding.ilActive.switchOriginal.setChecked(checked);
    }

    public void setProgress(Long progress) {
        if (mKaraokeView.getLyricsData() == null) return;
        if (mRole == Role.Singer) {
            if (progress >= mKaraokeView.getLyricsData().startOfVerse) {
                mBinding.ilActive.ivSkipPrelude.setVisibility(INVISIBLE);
            }

            if (progress >= mKaraokeView.getLyricsData().duration) {
                mBinding.ilActive.ivSkipPostlude.setVisibility(VISIBLE);
            } else {
                mBinding.ilActive.ivSkipPostlude.setVisibility(INVISIBLE);
            }
        }
        mKaraokeView.setProgress(progress);
    }

    public int retryTime = 0;

    public void downloadLrcData(String url) {
        retryTime++;
        DownloadManager.getInstance().download(getContext(), url, file -> {
            if (file.getName().endsWith(".zip")) {
                ZipUtils.unzipOnlyPlainXmlFilesAsync(file.getAbsolutePath(),
                        file.getAbsolutePath().replace(".zip", ""),
                        new ZipUtils.UnZipCallback() {
                            @Override
                            public void onFileUnZipped(List<String> unZipFilePaths) {
                                String xmlPath = "";
                                for (String path : unZipFilePaths) {
                                    if (path.endsWith(".xml")) {
                                        xmlPath = path;
                                        break;
                                    }
                                }
                                if (TextUtils.isEmpty(xmlPath)) {
                                    ToastUtils.showToast("The xml file not exist!");
                                    return;
                                }
                                File xmlFile = new File(xmlPath);

                                LyricsModel lyricsModel = KaraokeView.parseLyricsData(xmlFile);
                                if (mKaraokeView != null) {
                                    mKaraokeView.setLyricsData(lyricsModel);
                                }

                                retryTime = 0;
                            }

                            @Override
                            public void onError(Exception e) {
                                ToastUtils.showToast(e.getMessage());
                            }
                        });
            } else {
                LyricsModel lyricsModel = KaraokeView.parseLyricsData(file);
                if (mKaraokeView != null) {
                    mKaraokeView.setLyricsData(lyricsModel);
                }

                retryTime = 0;
            }
        }, exception -> {
            if (retryTime < 3) {
                downloadLrcData(url);
            } else {
                retryTime = 0;
                ToastUtils.showToast(exception.getMessage());
            }
        });
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

        default void onStartSing() {
        }

        default void onJoinChorus() {
        }

        default void onWaitTimeOut() {
        }

        default void onCountTime(int time) {
        }

        default void onDragTo(long position) {
        }

        default void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
        }

        default void onLineFinished(LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
        }

        default void onSkipPreludeClick() {
        }

        default void onSkipPostludeClick() {
        }
    }
}

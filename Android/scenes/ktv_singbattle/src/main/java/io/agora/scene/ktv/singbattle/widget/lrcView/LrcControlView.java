package io.agora.scene.ktv.singbattle.widget.lrcView;

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

import io.agora.karaoke_view.v11.KaraokeEvent;
import io.agora.karaoke_view.v11.KaraokeView;
import io.agora.karaoke_view.v11.LyricsView;
import io.agora.karaoke_view.v11.ScoringView;
import io.agora.karaoke_view.v11.model.LyricsLineModel;
import io.agora.karaoke_view.v11.model.LyricsModel;
import io.agora.scene.base.utils.DownloadUtils;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.ZipUtils;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvLayoutLrcControlViewBinding;
import io.agora.scene.ktv.singbattle.databinding.KtvLayoutLrcPrepareBinding;
import io.agora.scene.ktv.singbattle.live.ILrcView;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.widget.basic.OutlineSpan;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 歌词控制View
 */
public class LrcControlView extends FrameLayout implements View.OnClickListener, ILrcView {

    protected KtvLayoutLrcControlViewBinding mBinding;
    protected KtvLayoutLrcPrepareBinding mPrepareBinding;

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
            public void onRefPitchUpdate(float refPitch, int numberOfRefPitches) {
                if (mOnKaraokeActionListener != null) {
                    mOnKaraokeActionListener.onRefPitchUpdate(refPitch, numberOfRefPitches);
                }
            }

            @Override
            public void onLineFinished(KaraokeView view, LyricsLineModel line, int score, int cumulativeScore, int index, int total) {
                if (mRole == Role.Singer && mOnKaraokeActionListener != null) {
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

        mCountDownLatch = new CountDownTimer(3 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);

                setCountDown(second);
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

    private void setCountDown(int second) {
        if (mBinding == null) return;
        mBinding.ilActive.singBattle.setText("" + second);
    }

    private void onCountFinished() {
        if (mBinding == null) return;
        mBinding.ilActive.singBattle.setEnabled(true);
        mBinding.ilActive.singBattle.setText("抢唱");
        //mBinding.ilActive.singBattle.setBackgroundResource(R.mipmap.ktv_start_grasp);
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
        this.songPlaying = songPlaying;

        if (mBinding == null) return;
        mBinding.ilIDLE.getRoot().setVisibility(View.GONE);
        mBinding.clActive.setVisibility(View.VISIBLE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.VISIBLE);

        mBinding.ilActive.ivMusicStart.setIconResource(R.mipmap.ktv_ic_pause);
        mBinding.ilActive.ivMusicStart.setText(R.string.ktv_room_player_pause);
    }

    private void changeViewByRole() {
        if (mBinding == null) return;
        mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
        mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.INVISIBLE);
        if (this.mRole == Role.Singer) {
            mBinding.ilActive.lyricsView.enableDragging(false);
            //mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setChecked(false); // reset ui icon for mAudioTrackMode
            mBinding.ilActive.switchOriginal.setIconResource(R.mipmap.ic_play_original_off);
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
        mBinding.ilActive.ivMusicStart.setText(R.string.ktv_room_player_play);
    }

    public void onIdleStatus() {
        if (mBinding == null) return;
        mBinding.ilIDLE.getRoot().setVisibility(View.VISIBLE);
        mBinding.clActive.setVisibility(View.GONE);
        mBinding.clActive.setBackgroundResource(backgroundResId);
        mPrepareBinding.statusPrepareViewLrc.setVisibility(View.GONE);
        mBinding.ilActive.getRoot().setVisibility(View.GONE);
    }

    public void onGameBattlePrepareStatus() {
        if (mBinding == null) return;
        mBinding.ilActive.scoringView.setVisibility(View.GONE);
        mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        mBinding.tvMusicName.setVisibility(View.GONE);
        mBinding.tvCumulativeScore.setVisibility(View.GONE);
        mBinding.gradeView.setVisibility(View.GONE);

        mBinding.ilActive.tvMusicName2.setVisibility(View.VISIBLE);
        mBinding.ilActive.singBattle.setVisibility(View.VISIBLE);
        mBinding.ilActive.singBattle.setEnabled(false);
        mBinding.ilActive.singBattle.setBackgroundResource(R.mipmap.ktv_start_grasp_waiting);
    }

    public void onGamingStatus() {
        if (mBinding == null) return;
        mBinding.ilActive.scoringView.setVisibility(View.VISIBLE);
        mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
        mBinding.tvMusicName.setVisibility(View.VISIBLE);
        mBinding.tvCumulativeScore.setVisibility(View.VISIBLE);
        mBinding.gradeView.setVisibility(View.VISIBLE);
        mBinding.ilActive.tvMusicName2.setVisibility(View.GONE);
        mBinding.ilActive.singBattle.setVisibility(View.GONE);
    }

    public void setRole(@NonNull Role mRole) {
        this.mRole = mRole;
        lrcUrl = null;
        changeViewByRole();
    }

    public Role getRole() {
        return this.mRole;
    }

    public void setMusic(@NonNull RoomSelSongModel mMusic) {
        mKaraokeView.reset();
        if (mComboControl != null) {
            mComboControl.reset(mBinding);
        }

        mBinding.tvMusicName.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
        mBinding.ilActive.tvMusicName2.setText(mMusic.getSongName() + "-" + mMusic.getSinger());

        mBinding.ivCumulativeScoreGrade.setVisibility(INVISIBLE);
        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_score_formatter), "0"));
        mBinding.gradeView.setScore(0, 0, 0);
    }

    private int backgroundResId = R.mipmap.ktv_mv_default;

    public void setLrcViewBackground(@DrawableRes int resId) {
        backgroundResId = resId;
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Palette.from(mBitmap).generate(palette -> {
            if (palette == null) {
                return;
            }
            int defaultColor = ContextCompat.getColor(getContext(), R.color.pink_b4);
            mBinding.ilActive.lyricsView.setCurrentLineHighlightedTextColor(defaultColor);

            defaultColor = ContextCompat.getColor(getContext(), R.color.white);
            mBinding.ilActive.lyricsView.setCurrentLineTextColor(defaultColor);
        });
        mBinding.clActive.setBackgroundResource(resId);
    }

    public void updateScore(double score, double cumulativeScore, double perfectScore) {
        mCumulativeScoreInPercentage = (int) ((cumulativeScore / perfectScore) * 100);

        mBinding.gradeView.setScore((int) score, (int) cumulativeScore, (int) perfectScore);

        mBinding.tvCumulativeScore.setText(String.format(getResources().getString(R.string.ktv_score_formatter), "" + (int) cumulativeScore));
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

        private void reset(KtvLayoutLrcControlViewBinding binding) {
            mNumberOfCombos = 0;
            binding.comboView.getRoot().setVisibility(INVISIBLE);
        }

        private void checkAndShowCombos(KtvLayoutLrcControlViewBinding binding, int score, int cumulativeScore) {
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
            mBinding.ilActive.switchOriginal.setIconResource(withOriginal ? R.mipmap.ic_play_original_on : R.mipmap.ic_play_original_off);
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
        mKaraokeView.setPitch(pitch);
    }

    @Override
    public void onUpdateProgress(Long progress) {
        if (mKaraokeView.getLyricsData() == null) return;
        mKaraokeView.setProgress(progress);
    }

    private String lrcUrl;

    @Override
    public void onDownloadLrcData(String url) {
        this.lrcUrl = url;
        downloadAndSetLrcData();
    }

    private void downloadAndSetLrcData() {
        DownloadUtils.getInstance().download(getContext(), lrcUrl, file -> {
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
                                    mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                                    mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                                    return;
                                }

                                File xmlFile = new File(xmlPath);
                                LyricsModel lyricsModel = KaraokeView.parseLyricsData(xmlFile);

                                if (lyricsModel == null) {
                                    ToastUtils.showToast("Unexpected content from " + xmlPath);
                                    mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                                    mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                                    return;
                                }

                                if (mKaraokeView != null) {
                                    mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
                                    mKaraokeView.setLyricsData(lyricsModel);
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                                mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                                ToastUtils.showToast(e.getMessage());
                            }
                        });
            } else {
                LyricsModel lyricsModel = KaraokeView.parseLyricsData(file);

                if (lyricsModel == null) {
                    ToastUtils.showToast("Unexpected content from " + file);
                    mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
                    mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
                    return;
                }

                if (mKaraokeView != null) {
                    mBinding.ilActive.downloadLrcFailedView.setVisibility(View.INVISIBLE);
                    mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.INVISIBLE);
                    mKaraokeView.setLyricsData(lyricsModel);
                }
            }
        }, exception -> {
            ToastUtils.showToast(exception.getMessage());
            mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
            mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
        });
    }

    public void onNoLrc() {
        lrcUrl = null;
        mBinding.ilActive.downloadLrcFailedView.setVisibility(View.VISIBLE);
        mBinding.ilActive.downloadLrcFailedBtn.setVisibility(View.VISIBLE);
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

package io.agora.scene.ktv.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import java.io.File;
import java.util.List;

import io.agora.lyrics_view.DownloadManager;
import io.agora.lyrics_view.LrcLoadUtils;
import io.agora.lyrics_view.LrcView;
import io.agora.lyrics_view.PitchView;
import io.agora.lyrics_view.bean.LrcData;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.ZipUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvLayoutLrcControlViewBinding;
import io.agora.scene.ktv.databinding.KtvLayoutLrcPrepareBinding;
import io.agora.scene.ktv.service.RoomSelSongModel;

/**
 * 歌词控制View
 */
public class LrcControlView extends FrameLayout implements View.OnClickListener {

    protected KtvLayoutLrcControlViewBinding mBinding;
    protected KtvLayoutLrcPrepareBinding mPrepareBinding;

    public LrcView getLrcView() {
        if (mBinding != null && mBinding.ilActive != null) {
            return mBinding.ilActive.lrcView;
        } else {
            return null;
        }
    }

    public PitchView getPitchView() {
        return mBinding.ilActive.pitchView;
    }


    public enum Role {
        Singer, Listener, Partner
    }

    private Role mRole = Role.Listener;
    private OnLrcActionListener mOnLrcActionListener;

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

        initListener();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getLayoutParams();

        float heightPixels = Resources.getSystem().getDisplayMetrics().heightPixels;
        float density = Resources.getSystem().getDisplayMetrics().density;
        if (heightPixels > 1280 * 2) { // 2K/Slim screen
            params.bottomMargin = (int) (120 * density);
            setLayoutParams(params);
        }
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
        mBinding.ilActive.ivSkipPostlude.setOnClickListener(this);
        mBinding.ilActive.ivSkipPrelude.setOnClickListener(this);
    }

    public void setOnLrcClickListener(OnLrcActionListener mOnLrcActionListener) {
        this.mOnLrcActionListener = mOnLrcActionListener;
        mBinding.ilActive.lrcView.setSeekListener(mOnLrcActionListener);
    }

    public void setPitchViewOnActionListener(PitchView.OnSingScoreListener onActionListener) {
        mBinding.ilActive.pitchView.setSingScoreListener(onActionListener);
    }

    private CountDownTimer mCountDownLatch;

    private void startTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();

        mCountDownLatch = new CountDownTimer(20 * 1000, 999) {
            @Override
            public void onTick(long millisUntilFinished) {
                int second = (int) (millisUntilFinished / 1000);

                if (mOnLrcActionListener != null) {
                    mOnLrcActionListener.onCountTime(second);
                }

                setCountDown(second);
            }

            @Override
            public void onFinish() {
                mOnLrcActionListener.onWaitTimeOut();
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
        mBinding.clActive.setBackgroundResource(R.mipmap.bg_player_default);
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
            mBinding.ilActive.lrcView.setEnableDrag(true);
            mBinding.ilActive.ivMusicStart.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
            mBinding.ilActive.switchOriginal.setChecked(true);
        } else if (this.mRole == Role.Listener) {
            mBinding.ilActive.lrcView.setEnableDrag(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.GONE);
        }
        if (isMineOwner) {
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicStart.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicMenu.setVisibility(View.VISIBLE);
            if (this.mRole == Role.Listener) {
                mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
                mBinding.ilActive.switchOriginal.setVisibility(View.INVISIBLE);
                mBinding.ilActive.ivMusicMenu.setVisibility(View.INVISIBLE);
            } else if (this.mRole == Role.Partner) {
                mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
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

    public void setScoreControlView(RoomSelSongModel songPlaying) {
        if (songPlaying != null && songPlaying.isChorus()) {
            if (UserManager.getInstance().getUser().id.toString().equals(songPlaying.getUserNo())) {
                mBinding.scoreControlView.setVisibility(VISIBLE);
                mBinding.scoreControlView.setText(getContext().getString(R.string.ktv_score_formatter, 0.00));
            } else {
                mBinding.scoreControlView.setVisibility(GONE);
            }
        } else if (songPlaying != null && !songPlaying.isChorus()) {
            mBinding.scoreControlView.setVisibility(VISIBLE);
            mBinding.scoreControlView.setText(getContext().getString(R.string.ktv_score_formatter, 0.00));
        } else {
            mBinding.scoreControlView.setVisibility(GONE);
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
            mBinding.ilActive.lrcView.setEnableDrag(true);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.switchOriginal.setChecked(true);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
        } else if (this.mRole == Role.Partner) {
            mBinding.ilActive.lrcView.setEnableDrag(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.VISIBLE);
            mBinding.ilActive.ivMusicStart.setVisibility(View.INVISIBLE);
            mBinding.ilActive.ivChangeSong.setVisibility(View.INVISIBLE);
            mBinding.ilActive.switchOriginal.setVisibility(View.INVISIBLE);
            mBinding.ilActive.ivSkipPrelude.setVisibility(View.INVISIBLE);
            mBinding.ilActive.ivSkipPostlude.setVisibility(View.INVISIBLE);
        } else if (this.mRole == Role.Listener) {
            mBinding.ilActive.lrcView.setEnableDrag(false);
            mBinding.ilActive.rlMusicControlMenu.setVisibility(View.INVISIBLE);
        }
    }

    public void setMusic(@NonNull RoomSelSongModel mMusic) {
        mBinding.ilActive.lrcView.reset();
        mBinding.ilActive.pitchView.setLrcData(null);

        mBinding.tvMusicName.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
        mBinding.ilChorus.tvMusicName2.setText(mMusic.getSongName() + "-" + mMusic.getSinger());
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

    private int backgroundResId = R.mipmap.bg_player_default;

    public void setLrcViewBackground(@DrawableRes int resId) {
        backgroundResId = resId;
        Bitmap mBitmap = BitmapFactory.decodeResource(getResources(), resId);
        Palette.from(mBitmap).generate(palette -> {
            if (palette == null) {
                return;
            }
            int defaultColor = ContextCompat.getColor(getContext(), R.color.yellow_60);
            mBinding.ilActive.lrcView.setCurrentColor(defaultColor);

            defaultColor = ContextCompat.getColor(getContext(), R.color.white);
            mBinding.ilActive.lrcView.setNormalColor(defaultColor);
        });
        mBinding.clActive.setBackgroundResource(resId);
    }

    public void updateScore(double score) {
        mBinding.scoreControlView.setText(getContext().getString(R.string.ktv_score_formatter, score));
    }

    @Override
    public void onClick(View v) {
        if (v == mBinding.ilActive.switchOriginal) {
            mOnLrcActionListener.onSwitchOriginalClick();
        } else if (v == mBinding.ilActive.ivMusicMenu) {
            mOnLrcActionListener.onMenuClick();
        } else if (v == mBinding.ilActive.ivMusicStart) {
            mOnLrcActionListener.onPlayClick();
        } else if (v == mBinding.ilActive.ivChangeSong) {
            mOnLrcActionListener.onChangeMusicClick();
        } else if (v == mBinding.ilChorus.btChorus) {
            if (mRole == Role.Singer) {
                mOnLrcActionListener.onStartSing();
            } else if (mRole == Role.Listener) {
                mOnLrcActionListener.onJoinChorus();
            }
        } else if (v == mBinding.ilActive.ivSkipPrelude) {
            mOnLrcActionListener.onSkipPreludeClick();
        } else if (v == mBinding.ilActive.ivSkipPostlude) {
            mOnLrcActionListener.onSkipPostludeClick();
        }
    }

    public void setSwitchOriginalChecked(boolean checked) {
        mBinding.ilActive.switchOriginal.setChecked(checked);
    }

    public void downloadLrcData(String url) {
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

                                LrcData data = LrcLoadUtils.parse(xmlFile);
                                getLrcView().setLrcData(data);
                                getPitchView().setLrcData(data);
                            }

                            @Override
                            public void onError(Exception e) {
                                ToastUtils.showToast(e.getMessage());
                            }
                        });
            } else {
                LrcData data = LrcLoadUtils.parse(file);
                getLrcView().setLrcData(data);
                getPitchView().setLrcData(data);
            }
        }, exception -> {
            ToastUtils.showToast(exception.getMessage());
        });
    }

    public interface OnLrcActionListener extends LrcView.OnLyricsSeekListener {
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

        default void onSkipPreludeClick() {
        }

        default void onSkipPostludeClick() {
        }
    }
}

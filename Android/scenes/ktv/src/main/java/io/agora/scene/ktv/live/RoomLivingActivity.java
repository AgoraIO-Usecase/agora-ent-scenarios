package io.agora.scene.ktv.live;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.rtc2.Constants;
import io.agora.scene.base.GlideApp;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.KTVLogger;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.databinding.KtvActivityRoomLivingBinding;
import io.agora.scene.ktv.databinding.KtvItemRoomSpeakerBinding;
import io.agora.scene.ktv.debugSettings.KTVDebugSettingsDialog;
import io.agora.scene.ktv.live.fragment.dialog.MVFragment;
import io.agora.scene.ktv.live.listener.LrcActionListenerImpl;
import io.agora.scene.ktv.live.listener.SongActionListenerImpl;
import io.agora.scene.ktv.service.JoinRoomOutputModel;
import io.agora.scene.ktv.service.RoomSeatModel;
import io.agora.scene.ktv.service.RoomSelSongModel;
import io.agora.scene.ktv.widget.KtvCommonDialog;
import io.agora.scene.ktv.widget.lrcView.LrcControlView;
import io.agora.scene.ktv.widget.MoreDialog;
import io.agora.scene.ktv.widget.MusicSettingDialog;
import io.agora.scene.ktv.widget.UserLeaveSeatMenuDialog;
import io.agora.scene.ktv.widget.song.SongDialog;
import io.agora.scene.ktv.widget.voiceHighight.OnVoiceHighlightDialogListener;
import io.agora.scene.ktv.widget.voiceHighight.VoiceHighlightBean;
import io.agora.scene.ktv.widget.voiceHighight.VoiceHighlightDialog;
import io.agora.scene.widget.DividerDecoration;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;
import io.agora.scene.widget.dialog.CommonDialog;
import io.agora.scene.widget.dialog.PermissionLeakDialog;
import io.agora.scene.widget.dialog.TopFunctionDialog;
import io.agora.scene.widget.utils.CenterCropRoundCornerTransform;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 房间主页
 */
public class RoomLivingActivity extends BaseViewBindingActivity<KtvActivityRoomLivingBinding> {
    private static final String EXTRA_ROOM_INFO = "roomInfo";

    private RoomLivingViewModel roomLivingViewModel;
    private MoreDialog moreDialog;
    private VoiceHighlightDialog voiceHighlightDialog;
    private MusicSettingDialog musicSettingDialog;
    private BindingSingleAdapter<RoomSeatModel, KtvItemRoomSpeakerBinding> mRoomSpeakerAdapter;
    private KtvCommonDialog creatorExitDialog;

    private CommonDialog exitDialog;
    private UserLeaveSeatMenuDialog mUserLeaveSeatMenuDialog;
    private SongDialog mChooseSongDialog;

    // 房间存活时间，单位ms
    private KtvCommonDialog timeUpExitDialog;


    public static void launch(Context context, JoinRoomOutputModel roomInfo) {
        Intent intent = new Intent(context, RoomLivingActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        context.startActivity(intent);
    }

    @Override
    protected KtvActivityRoomLivingBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return KtvActivityRoomLivingBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            finish();
            return;
        }
        getWindow().getDecorView().setKeepScreenOn(true);
        setOnApplyWindowInsetsListener(getBinding().superLayout);
        roomLivingViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                return (T) new RoomLivingViewModel((JoinRoomOutputModel) getIntent().getSerializableExtra(EXTRA_ROOM_INFO));
            }
        }).get(RoomLivingViewModel.class);
        roomLivingViewModel.setLrcView(getBinding().lrcControlView);

        mRoomSpeakerAdapter = new BindingSingleAdapter<RoomSeatModel, KtvItemRoomSpeakerBinding>() {
            @Override
            public void onBindViewHolder(@NonNull BindingViewHolder<KtvItemRoomSpeakerBinding> holder, int position) {
                RoomSeatModel item = getItem(position);
                KtvItemRoomSpeakerBinding binding = holder.binding;

                boolean isOutSeat = item == null || TextUtils.isEmpty(item.getUserNo());
                binding.getRoot().setOnClickListener(v -> {
                    if (!isOutSeat) {
                        // 下麦
                        if (roomLivingViewModel.isRoomOwner()) {
                            if (!item.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                                showUserLeaveSeatMenuDialog(item);
                            }
                        } else if (item.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                            showUserLeaveSeatMenuDialog(item);
                        }
                    } else {
                        // 上麦
                        RoomSeatModel seatLocal = roomLivingViewModel.seatLocalLiveData.getValue();
                        if (seatLocal == null || seatLocal.getSeatIndex() < 0) {
                            toggleAudioRun = () -> {
                                roomLivingViewModel.haveSeat(position);
                                getBinding().cbMic.setChecked(false);
                                getBinding().cbVideo.setChecked(false);
                            };
                            requestRecordPermission();
                        }
                    }
                });

                if (isOutSeat) {
                    binding.avatarItemRoomSpeaker.setImageResource(R.mipmap.ktv_ic_seat);
                    binding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE);
                    binding.tvZC.setVisibility(View.GONE);
                    binding.tvHC.setVisibility(View.GONE);
                    binding.tvRoomOwner.setVisibility(View.GONE);
                    binding.ivMute.setVisibility(View.GONE);
                    binding.tvUserName.setText(String.valueOf(position + 1));
                    binding.flVideoContainer.removeAllViews();
                } else {
                    binding.tvUserName.setText(item.getName());

                    if (item.isMaster() && position == 0) {
                        binding.tvRoomOwner.setVisibility(View.VISIBLE);
                    } else {
                        binding.tvRoomOwner.setVisibility(View.GONE);
                    }

                    // microphone
                    if (item.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
                        binding.ivMute.setVisibility(View.VISIBLE);
                    } else {
                        binding.ivMute.setVisibility(View.GONE);
                    }

                    // video
                    if (item.isVideoMuted() == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
                        binding.avatarItemRoomSpeaker.setVisibility(View.VISIBLE);
                        binding.flVideoContainer.removeAllViews();
                        GlideApp.with(binding.getRoot())
                                .load(item.getHeadUrl())
                                .error(R.mipmap.userimage)
                                .transform(new CenterCropRoundCornerTransform(100))
                                .into(binding.avatarItemRoomSpeaker);
                    } else {
                        binding.avatarItemRoomSpeaker.setVisibility(View.INVISIBLE);
                        binding.flVideoContainer.removeAllViews();
                        SurfaceView surfaceView = fillWithRenderView(binding.flVideoContainer);
                        if (item.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) { // 是本人
                            roomLivingViewModel.renderLocalCameraVideo(surfaceView);
                        } else {
                            int uid = Integer.parseInt(item.getRtcUid());
                            roomLivingViewModel.renderRemoteCameraVideo(surfaceView, uid);
                        }
                    }


                    RoomSelSongModel songModel = roomLivingViewModel.songPlayingLiveData.getValue();
                    if (songModel != null) {
                        if (item.getUserNo().equals(songModel.getUserNo())) {
                            binding.tvZC.setText("主唱");
                            binding.tvHC.setVisibility(View.GONE);
                            binding.tvZC.setVisibility(View.VISIBLE);
                        } else if (item.getChorusSongCode() != null && !item.getUserNo().equals(songModel.getUserNo()) && item.getChorusSongCode().equals(songModel.getSongNo() + songModel.getCreateAt())) {
                            binding.tvHC.setText("合唱");
                            binding.tvZC.setVisibility(View.GONE);
                            binding.tvHC.setVisibility(View.VISIBLE);
                        } else {
                            binding.tvZC.setVisibility(View.GONE);
                            binding.tvHC.setVisibility(View.GONE);
                        }
                    } else {
                        binding.tvZC.setVisibility(View.GONE);
                        binding.tvHC.setVisibility(View.GONE);
                    }
                }
            }
        };
        getBinding().rvUserMember.addItemDecoration(new DividerDecoration(4, 24, 8));
        getBinding().rvUserMember.setAdapter(mRoomSpeakerAdapter);
        mRoomSpeakerAdapter.resetAll(Arrays.asList(null, null, null, null, null, null, null, null));
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            getBinding().rvUserMember.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
        getBinding().lrcControlView.post(() -> {
            // TODO workaround 先强制申请权限， 避免首次安装无声
            if(roomLivingViewModel.isRoomOwner()){
                toggleAudioRun = () -> roomLivingViewModel.init();
                requestRecordPermission();
            }
            else{
                roomLivingViewModel.init();
            }
        });

        if (!TextUtils.isEmpty(roomLivingViewModel.roomInfoLiveData.getValue().getBgOption())) {
            setPlayerBgFromMsg(Integer.parseInt(roomLivingViewModel.roomInfoLiveData.getValue().getBgOption()));
        } else {
            setPlayerBgFromMsg(0);
        }
        getBinding().tvRoomName.setText(roomLivingViewModel.roomInfoLiveData.getValue().getRoomName());

        if (AgoraApplication.the().isDebugModeOpen()) {
            getBinding().btnDebug.setVisibility(View.VISIBLE);
        } else {
            getBinding().btnDebug.setVisibility(View.INVISIBLE);
        }
        getBinding().btnDebug.setOnClickListener(v -> {
            KTVDebugSettingsDialog dialog = new KTVDebugSettingsDialog(roomLivingViewModel.mDebugSetting);
            dialog.show(getSupportFragmentManager(), "debugSettings");
        });
        getBinding().ivMore.setOnClickListener(v -> {
            new TopFunctionDialog(RoomLivingActivity.this).show();
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) getBinding().rvUserMember.getLayoutParams();

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int heightPixels; // current window
        int widthPixels; // current window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Rect rect = windowManager.getCurrentWindowMetrics().getBounds();
            heightPixels = rect.height();
            widthPixels = rect.width();
        } else {
            Point point = new Point();
            windowManager.getDefaultDisplay().getSize(point);
            heightPixels = point.y;
            widthPixels = point.x;
        }

        float density = Resources.getSystem().getDisplayMetrics().density;
        if (heightPixels * 1.0 / widthPixels > 16.0 / 9) { // 2K/Slim/> 16:9 screens
            // TODO(HAI_GUO) Flip/Fold/Split screens and One-handed mode may not supported well
            params.bottomMargin = (int) (heightPixels * (1.0 - ((16.0 * widthPixels) / (9 * heightPixels))));
            getBinding().rvUserMember.setLayoutParams(params);
        }

        // density 4.0 densityDpi 640 system resources 2560 1440 display real 2560 1440 current window 2560 1440 HUAWEI V9
        // density 3.0 densityDpi 480 system resources 2297 1080 display real 2400 1080 current window 2400 1080 1+ 9R
    }

    @Override
    public void initListener() {
        getBinding().ivExit.setOnClickListener(view -> showExitDialog());
        getBinding().superLayout.setOnClickListener(view -> setDarkStatusIcon(isBlackDarkStatus()));
        getBinding().cbMic.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!compoundButton.isPressed()) return;
            RoomSeatModel seatLocal = roomLivingViewModel.seatLocalLiveData.getValue();
            if (seatLocal == null || mRoomSpeakerAdapter.getItem(seatLocal.getSeatIndex()) == null) {
                return;
            }
            if (b) {
                toggleAudioRun = () -> roomLivingViewModel.toggleMic(true);
                requestRecordPermission(true);
            } else {
                roomLivingViewModel.toggleMic(false);
            }
        });
        getBinding().iBtnChooseSong.setOnClickListener(v -> showChooseSongDialog());
        getBinding().btnMenu.setOnClickListener(this::showMoreDialog);
        //getBinding().btnVocal.setOnClickListener(this::showVoiceHighlightDialog);
        getBinding().btnOK.setOnClickListener(view -> getBinding().groupResult.setVisibility(View.GONE));
        LrcActionListenerImpl lrcActionListenerImpl = new LrcActionListenerImpl(this, roomLivingViewModel, getBinding().lrcControlView) {
            @Override
            public void onMenuClick() {
                super.onMenuClick();
                showMusicSettingDialog();
            }

            @Override
            public void onChangeMusicClick() {
                super.onChangeMusicClick();
                showChangeMusicDialog();
            }

            @Override
            public void onVocalHighlightClick() {
                super.onVocalHighlightClick();
                showVoiceHighlightDialog();
            }
        };
        getBinding().lrcControlView.setOnLrcClickListener(lrcActionListenerImpl);
        getBinding().cbVideo.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!compoundButton.isPressed()) return;
            toggleSelfVideo(b);
        });

        roomLivingViewModel.loadingDialogVisible.observe(this, visible -> {
            if (visible) {
                showLoadingView();
            } else {
                hideLoadingView();
            }
        });

        // 房间相关
        roomLivingViewModel.roomInfoLiveData.observe(this, joinRoomOutputModel -> {
            //修改背景
            if (!TextUtils.isEmpty(joinRoomOutputModel.getBgOption())) {
                setPlayerBgFromMsg(Integer.parseInt(joinRoomOutputModel.getBgOption()));
            }
        });
        roomLivingViewModel.roomDeleteLiveData.observe(this, deletedByCreator -> {
            if (deletedByCreator) {
                showCreatorExitDialog();
            } else {
                finish();
            }
        });
        roomLivingViewModel.roomUserCountLiveData.observe(this, count ->
                getBinding().tvRoomMCount.setText(getString(R.string.ktv_room_count, String.valueOf(count))));
        roomLivingViewModel.roomTimeUpLiveData.observe(this, isTimeUp -> {
            if (roomLivingViewModel.release() && isTimeUp) {
                showTimeUpExitDialog();
            }
        });

        // 麦位相关
        roomLivingViewModel.seatLocalLiveData.observe(this, seatModel -> {
            boolean isOnSeat = seatModel != null && seatModel.getSeatIndex() >= 0;
            getBinding().groupBottomView.setVisibility(isOnSeat ? View.VISIBLE : View.GONE);
            getBinding().groupEmptyPrompt.setVisibility(isOnSeat ? View.GONE : View.VISIBLE);

            boolean isVideoChecked = seatModel != null && seatModel.isVideoMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE();
            getBinding().cbVideo.setChecked(isVideoChecked);
            boolean isAudioChecked = seatModel != null && seatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE();
            getBinding().cbMic.setChecked(isAudioChecked);

            getBinding().lrcControlView.onSeat(seatModel != null);
        });
        roomLivingViewModel.seatListLiveData.observe(this, seatModels -> {
            if (seatModels == null) {
                return;
            }
            int chorusNowNum = 0;
            for (RoomSeatModel seatModel : seatModels) {
                RoomSeatModel oSeatModel = mRoomSpeakerAdapter.getItem(seatModel.getSeatIndex());
                if (oSeatModel == null
                        || oSeatModel.isAudioMuted() != seatModel.isAudioMuted()
                        || oSeatModel.isVideoMuted() != seatModel.isVideoMuted()
                        || (oSeatModel.getChorusSongCode() != null && !oSeatModel.getChorusSongCode().equals(seatModel.getChorusSongCode()))) {
                    mRoomSpeakerAdapter.replace(seatModel.getSeatIndex(), seatModel);
                }

                if (seatModel.getChorusSongCode() != null && roomLivingViewModel.songPlayingLiveData.getValue() != null && seatModel.getChorusSongCode().equals(roomLivingViewModel.songPlayingLiveData.getValue().getSongNo() + roomLivingViewModel.songPlayingLiveData.getValue().getCreateAt())) {
                    chorusNowNum ++;
                }
            }

            if (roomLivingViewModel.chorusNum == 0 && chorusNowNum > 0) {
                // 有人加入合唱
                roomLivingViewModel.soloSingerJoinChorusMode(true);
            } else if (roomLivingViewModel.chorusNum > 0 && chorusNowNum == 0) {
                // 最后一人退出合唱
                roomLivingViewModel.soloSingerJoinChorusMode(false);
            }
            roomLivingViewModel.chorusNum = chorusNowNum;


            for (int i = 0; i < mRoomSpeakerAdapter.getItemCount(); i++) {
                RoomSeatModel seatModel = mRoomSpeakerAdapter.getItem(i);
                if (seatModel == null) {
                    continue;
                }
                boolean exist = false;
                for (RoomSeatModel model : seatModels) {
                    if (seatModel.getSeatIndex() == model.getSeatIndex()) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    onMemberLeave(seatModel);
                }
            }
            if (seatModels.size() == 8) {
                getBinding().lrcControlView.onSeatFull(true);
            } else if ((seatModels.size() < 8)) {
                getBinding().lrcControlView.onSeatFull(false);
            }
        });


        // 歌词相关
        roomLivingViewModel.mainSingerScoreLiveData.observe(this, score -> {
            getBinding().lrcControlView.onReceiveSingleLineScore(score.score, score.index, score.cumulativeScore, score.total);
        });
        roomLivingViewModel.songsOrderedLiveData.observe(this, models -> {
            if (models == null || models.isEmpty()) {
                // songs empty
                getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
                getBinding().lrcControlView.onIdleStatus();
                mRoomSpeakerAdapter.notifyDataSetChanged();
            }
            if (mChooseSongDialog != null) {
                mChooseSongDialog.resetChosenSongList(SongActionListenerImpl.transSongModel(models));
            }
        });
        roomLivingViewModel.songPlayingLiveData.observe(this, model -> {
            if (model == null) {
                roomLivingViewModel.musicStop();
                return;
            }
            onMusicChanged(model);
        });
        roomLivingViewModel.scoringAlgoControlLiveData.observe(this, model -> {
            if (model == null) {
                return;
            }
            getBinding().lrcControlView.getKaraokeView().setScoringLevel(model.getLevel());
            getBinding().lrcControlView.getKaraokeView().setScoringCompensationOffset(model.getOffset());
        });
        roomLivingViewModel.noLrcLiveData.observe(this, isNoLrc -> {
            if (isNoLrc) {
                getBinding().lrcControlView.onNoLrc();
            }
        });
        roomLivingViewModel.playerMusicStatusLiveData.observe(this, status -> {
            if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PREPARE) {
                getBinding().lrcControlView.onPrepareStatus(roomLivingViewModel.isRoomOwner());
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PLAYING) {
                getBinding().lrcControlView.onPlayStatus(roomLivingViewModel.songPlayingLiveData.getValue());
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PAUSE) {
                getBinding().lrcControlView.onPauseStatus();
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_LRC_RESET) {
                getBinding().lrcControlView.getLyricsView().reset();
                if (getBinding().lrcControlView.getRole() == LrcControlView.Role.Singer) {
                    roomLivingViewModel.changeMusic();
                }
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_CHANGING_START) {
                getBinding().lrcControlView.setEnabled(false);
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_CHANGING_END) {
                getBinding().lrcControlView.setEnabled(true);
            }
        });
        roomLivingViewModel.joinchorusStatusLiveData.observe(this, status -> {
            if (status == RoomLivingViewModel.JoinChorusStatus.ON_JOIN_CHORUS) {
                getBinding().cbMic.setChecked(true);
                getBinding().lrcControlView.onSelfJoinedChorus();
            } else if (status == RoomLivingViewModel.JoinChorusStatus.ON_JOIN_FAILED) {
                getBinding().lrcControlView.onSelfJoinedChorusFailed();
            } else if (status == RoomLivingViewModel.JoinChorusStatus.ON_LEAVE_CHORUS) {
                getBinding().cbMic.setChecked(false);
                getBinding().lrcControlView.onSelfLeavedChorus();
            }
        });
        roomLivingViewModel.playerMusicOpenDurationLiveData.observe(this, duration -> {
            getBinding().lrcControlView.getLyricsView().setDuration(duration);
        });
        roomLivingViewModel.playerMusicPlayCompleteLiveData.observe(this, score -> {
            if (score.isLocal()) {
                int sc = getBinding().lrcControlView.getCumulativeScoreInPercentage();
                getBinding().tvResultScore.setText(String.valueOf(sc));
                if (sc >= 90) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_s);
                } else if (sc >= 80) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_a);
                } else if (sc >= 70) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_b);
                } else {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_c);
                }
                getBinding().groupResult.setVisibility(View.VISIBLE);

                if (getBinding().lrcControlView.getRole() == LrcControlView.Role.Singer) {
                    roomLivingViewModel.syncSingingAverageScore(sc);
                }
            } else {
                if (getBinding().lrcControlView.getRole() != LrcControlView.Role.Listener) return;
                getBinding().tvResultScore.setText(String.valueOf(score.getScore()));
                if (score.getScore() >= 90) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_s);
                } else if (score.getScore() >= 80) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_a);
                } else if (score.getScore() >= 70) {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_b);
                } else {
                    getBinding().ivResultLevel.setImageResource(R.mipmap.ic_c);
                }
                getBinding().groupResult.setVisibility(View.VISIBLE);
            }
        });
        roomLivingViewModel.playerMusicCountDownLiveData.observe(this, time ->
                getBinding().lrcControlView.setCountDown(time));
        roomLivingViewModel.networkStatusLiveData.observe(this, netWorkStatus ->
                setNetWorkStatus(netWorkStatus.txQuality, netWorkStatus.rxQuality));
    }


    private void setNetWorkStatus(int txQuality, int rxQuality) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR
                || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_yellow);
            getBinding().tvNetStatus.setText(R.string.ktv_net_status_m);
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN
                || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_DOWN) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.ktv_net_status_low);
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD
                || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.ktv_net_status_good);
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.ktv_net_status_un_know);
        } else {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.ktv_net_status_good);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        KTVLogger.d("ktv", "onResume() " + isBlackDarkStatus());
        setDarkStatusIcon(isBlackDarkStatus());
    }

    /**
     * 下麦提示
     */
    private void showUserLeaveSeatMenuDialog(RoomSeatModel seatModel) {
        if (mUserLeaveSeatMenuDialog == null) {
            mUserLeaveSeatMenuDialog = new UserLeaveSeatMenuDialog(this);
        }
        mUserLeaveSeatMenuDialog.setOnButtonClickListener(new OnButtonClickListener() {
            @Override
            public void onLeftButtonClick() {
                setDarkStatusIcon(isBlackDarkStatus());
                roomLivingViewModel.leaveChorus();
            }

            @Override
            public void onRightButtonClick() {
                setDarkStatusIcon(isBlackDarkStatus());
                roomLivingViewModel.leaveSeat(seatModel);
            }
        });
        mUserLeaveSeatMenuDialog.setAgoraMember(seatModel.getName(), seatModel.getHeadUrl());
        mUserLeaveSeatMenuDialog.show();
    }

    private static SurfaceView fillWithRenderView(@NonNull ViewGroup container) {
        Context context = container.getContext();
        MaterialCardView cardView = new MaterialCardView(context, null, R.attr.materialCardViewStyle);
        cardView.setCardElevation(0);
        cardView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> cardView.setRadius((right - left) / 2f));

        SurfaceView surfaceView = new SurfaceView(context);
        surfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        cardView.addView(surfaceView);
        container.addView(cardView);
        return surfaceView;
    }

    private void showTimeUpExitDialog() {
        if (timeUpExitDialog == null) {
            timeUpExitDialog = new KtvCommonDialog(this);

            if (roomLivingViewModel.isRoomOwner()) {
                timeUpExitDialog.setDescText(getString(R.string.time_up_exit_room));
            } else {
                timeUpExitDialog.setDescText(getString(R.string.expire_exit_room));
            }
            timeUpExitDialog.setDialogBtnText("", getString(R.string.ktv_confirm));
            timeUpExitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                }

                @Override
                public void onRightButtonClick() {
                    roomLivingViewModel.exitRoom();
                }
            });
        }
        timeUpExitDialog.show();
    }

    private void showExitDialog() {
        if (exitDialog == null) {
            exitDialog = new CommonDialog(this);

            if (roomLivingViewModel.isRoomOwner()) {
                exitDialog.setDialogTitle(getString(R.string.dismiss_room));
                exitDialog.setDescText(getString(R.string.confirm_to_dismiss_room));
            } else {
                exitDialog.setDialogTitle(getString(R.string.exit_room));
                exitDialog.setDescText(getString(R.string.confirm_to_exit_room));
            }
            exitDialog.setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm));
            exitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.exitRoom();
                    finish();
                }
            });
        }
        exitDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void onMusicChanged(@NonNull RoomSelSongModel music) {
        getBinding().lrcControlView.setMusic(music);
        if (UserManager.getInstance().getUser().id.toString().equals(music.getUserNo())) {
            getBinding().lrcControlView.setRole(LrcControlView.Role.Singer);
        } else {
            getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
        }
        roomLivingViewModel.resetMusicStatus();
        roomLivingViewModel.musicStartPlay(music);
        mRoomSpeakerAdapter.notifyDataSetChanged();
    }


    public void closeMenuDialog() {
        setDarkStatusIcon(isBlackDarkStatus());
        moreDialog.dismiss();
    }

    private LinkedHashMap<Integer, String> filterSongTypeMap(LinkedHashMap<Integer, String> typeMap) {
        // 0 -> "项目热歌榜单"
        // 1 -> "声网热歌榜"
        // 2 -> "新歌榜" ("热门新歌")
        // 3 -> "嗨唱推荐"
        // 4 -> "抖音热歌"
        // 5 -> "古风热歌"
        // 6 -> "KTV必唱"
        LinkedHashMap<Integer, String> ret = new LinkedHashMap<>();
        for (Map.Entry<Integer, String> entry : typeMap.entrySet()) {
            int key = entry.getKey();
            String value = entry.getValue();
            if (key == 2) {
                value = getString(R.string.ktv_song_rank_7);
                ret.put(key, value);
            } else if (key == 3 || key == 4 || key == 6) {
                ret.put(key, value);
            }
        }
        return ret;
    }

    private void onClickJoinChorus() {
        roomLivingViewModel.joinChorus();
    }

    private boolean showChooseSongDialogTag = false;
    private void showChooseSongDialog() {
        if (showChooseSongDialogTag) {
            return;
        }
        showChooseSongDialogTag = true;

        if (mChooseSongDialog == null) {
            mChooseSongDialog = new SongDialog();
            mChooseSongDialog.setChosenControllable(roomLivingViewModel.isRoomOwner());
            showLoadingView();
            LiveDataUtils.observerThenRemove(this, roomLivingViewModel.getSongTypes(), typeMap -> {

                SongActionListenerImpl chooseSongListener = new SongActionListenerImpl(this, roomLivingViewModel, filterSongTypeMap(typeMap), false);
                mChooseSongDialog.setChooseSongTabsTitle(chooseSongListener.getSongTypeTitles(this), chooseSongListener.getSongTypeList(), 0);
                mChooseSongDialog.setChooseSongListener(chooseSongListener);
                hideLoadingView();

                if (!mChooseSongDialog.isAdded()) {
                    roomLivingViewModel.getSongChosenList();
                    mChooseSongDialog.show(getSupportFragmentManager(), "ChooseSongDialog");
                }

                getBinding().getRoot().post(() -> showChooseSongDialogTag = false);
            });
            return;
        }

        if (!mChooseSongDialog.isAdded()) {
            roomLivingViewModel.getSongChosenList();
            mChooseSongDialog.show(getSupportFragmentManager(), "ChooseSongDialog");
        }

        getBinding().getRoot().post(() -> {
            showChooseSongDialogTag = false;
        });
    }

    private boolean showMoreDialogTag = false;
    private void showMoreDialog(View view) {
        if (showMoreDialogTag) {
            return;
        }
        showMoreDialogTag = true;

        if (moreDialog == null) {
            moreDialog = new MoreDialog(roomLivingViewModel.mSetting);
        }

        if (!moreDialog.isAdded()) {
            moreDialog.show(getSupportFragmentManager(), MoreDialog.TAG);
        }

        getBinding().getRoot().post(() -> {
            showMoreDialogTag = false;
        });
    }

    private boolean showVoiceHighlightDialogTag = false;
    private void showVoiceHighlightDialog() {
        if (showVoiceHighlightDialogTag) {
            return;
        }
        showVoiceHighlightDialogTag = true;

        if (voiceHighlightDialog == null) {
            voiceHighlightDialog = new VoiceHighlightDialog(new OnVoiceHighlightDialogListener() {
                @Override
                public void onUserListLoad() {
                    List<VoiceHighlightBean> list = new ArrayList<>();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (roomLivingViewModel.seatListLiveData.getValue() == null || roomLivingViewModel.songPlayingLiveData.getValue() == null) return;
                        AtomicBoolean hasChorus = new AtomicBoolean(false);
                        roomLivingViewModel.seatListLiveData.getValue().forEach(seat -> {
                            if (seat.getChorusSongCode().equals(roomLivingViewModel.songPlayingLiveData.getValue().getSongNo() + roomLivingViewModel.songPlayingLiveData.getValue().getCreateAt())) {
                                VoiceHighlightBean bean = new VoiceHighlightBean();
                                bean.user = seat;
                                list.add(bean);
                                hasChorus.set(true);
                            } else if (seat.getUserNo().equals(roomLivingViewModel.songPlayingLiveData.getValue().getUserNo())) {
                                VoiceHighlightBean bean = new VoiceHighlightBean();
                                bean.user = seat;
                                list.add(bean);
                            }
                        });
                        if (!hasChorus.get()) return;
                    }
                    voiceHighlightDialog.setUserList(list);
                }

                @Override
                public void onUserItemChosen(VoiceHighlightBean user) {
                    roomLivingViewModel.syncVoiceHighlightResult(user.user.getRtcUid(), user.user.getChorusSongCode());
                }
            });
        }

        if (!voiceHighlightDialog.isAdded()) {
            voiceHighlightDialog.show(getSupportFragmentManager(), MoreDialog.TAG);
        }

        getBinding().getRoot().post(() -> {
            showVoiceHighlightDialogTag = false;
        });
    }

    private void showMusicSettingDialog() {
        //if (musicSettingDialog == null) {
            musicSettingDialog = new MusicSettingDialog(roomLivingViewModel.mSetting, roomLivingViewModel.playerMusicStatusLiveData.getValue() == RoomLivingViewModel.PlayerMusicStatus.ON_PAUSE);
        //}
        musicSettingDialog.show(getSupportFragmentManager(), MusicSettingDialog.TAG);
    }

    private CommonDialog changeMusicDialog;

    private void showChangeMusicDialog() {
        if (UiUtils.isFastClick(2000)) {
            ToastUtils.showToast("操作太频繁");
            return;
        }
        if (changeMusicDialog == null) {
            changeMusicDialog = new CommonDialog(this);
            changeMusicDialog.setDialogTitle(getString(R.string.ktv_room_change_music_title));
            changeMusicDialog.setDescText(getString(R.string.ktv_room_change_music_msg));
            changeMusicDialog.setDialogBtnText(getString(R.string.ktv_cancel), getString(R.string.ktv_confirm));
            changeMusicDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.changeMusic();
                }
            });
        }
        changeMusicDialog.show();
    }

    public void setPlayerBgFromMsg(int position) {
        getBinding().lrcControlView.setLrcViewBackground(MVFragment.exampleBackgrounds.get(position));
    }

    public void setPlayerBg(int position) {
        roomLivingViewModel.setMV_BG(position);
        getBinding().lrcControlView.setLrcViewBackground(MVFragment.exampleBackgrounds.get(position));
    }

    @Override
    protected void onStart() {
        super.onStart();
        roomLivingViewModel.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        roomLivingViewModel.onStop();
    }

    @Override
    public boolean isBlackDarkStatus() {
        return false;
    }

    private Runnable toggleVideoRun;
    private Runnable toggleAudioRun;

    //开启 关闭摄像头
    private void toggleSelfVideo(boolean isOpen) {
        if (isOpen) {
            toggleVideoRun = () -> {
                roomLivingViewModel.toggleSelfVideo(true);
            };
            requestCameraPermission(true);
        } else {
            roomLivingViewModel.toggleSelfVideo(false);
        }
    }

    @Override
    public void getPermissions() {
        if (toggleVideoRun != null) {
            toggleVideoRun.run();
            toggleVideoRun = null;
        }
        if (toggleAudioRun != null) {
            toggleAudioRun.run();
            toggleAudioRun = null;
        }
    }

    @Override
    protected void onPermissionDined(String permission) {
        new PermissionLeakDialog(this).show(permission,
                this::getPermissions,
                () -> launchAppSetting(permission));
    }

    private void onMemberLeave(@NonNull RoomSeatModel member) {
        if (member.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
            getBinding().groupBottomView.setVisibility(View.GONE);
            getBinding().groupEmptyPrompt.setVisibility(View.VISIBLE);
        }
        RoomSeatModel temp = mRoomSpeakerAdapter.getItem(member.getSeatIndex());
        if (temp != null) {
            mRoomSpeakerAdapter.replace(member.getSeatIndex(), null);
        }
    }

    private void showCreatorExitDialog() {
        if (creatorExitDialog == null) {
            creatorExitDialog = new KtvCommonDialog(this);
            creatorExitDialog.setDescText(getString(R.string.room_has_close));
            creatorExitDialog.setDialogBtnText("", getString(R.string.ktv_iknow));
            creatorExitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    finish();
                }
            });
        }
        creatorExitDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        roomLivingViewModel.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showExitDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

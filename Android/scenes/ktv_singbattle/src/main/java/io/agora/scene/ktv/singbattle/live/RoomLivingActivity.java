package io.agora.scene.ktv.singbattle.live;

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

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.card.MaterialCardView;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import io.agora.rtc2.Constants;
import io.agora.scene.base.GlideApp;
import io.agora.scene.base.LogUploader;
import io.agora.scene.base.SceneConfigManager;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.component.BaseViewBindingActivity;
import io.agora.scene.base.component.OnButtonClickListener;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.LiveDataUtils;
import io.agora.scene.ktv.singbattle.KTVLogger;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleActivityRoomLivingBinding;
import io.agora.scene.ktv.singbattle.databinding.KtvSingbattleItemRoomSpeakerBinding;
import io.agora.scene.ktv.singbattle.debugSettings.KTVDebugSettingsDialog;
import io.agora.scene.ktv.singbattle.live.listener.LrcActionListenerImpl;
import io.agora.scene.ktv.singbattle.live.listener.SongActionListenerImpl;
import io.agora.scene.ktv.singbattle.service.JoinRoomOutputModel;
import io.agora.scene.ktv.singbattle.service.RoomSeatModel;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.ktv.singbattle.widget.KtvCommonDialog;
import io.agora.scene.ktv.singbattle.widget.game.SingBattleGameView;
import io.agora.scene.ktv.singbattle.widget.lrcView.LrcControlView;
import io.agora.scene.ktv.singbattle.widget.MusicSettingDialog;
import io.agora.scene.ktv.singbattle.widget.UserLeaveSeatMenuDialog;
import io.agora.scene.ktv.singbattle.widget.song.SongDialog;
import io.agora.scene.widget.DividerDecoration;
import io.agora.scene.widget.basic.BindingSingleAdapter;
import io.agora.scene.widget.basic.BindingViewHolder;
import io.agora.scene.widget.dialog.CommonDialog;
import io.agora.scene.widget.dialog.PermissionLeakDialog;
import io.agora.scene.widget.dialog.TopFunctionDialog;
import io.agora.scene.widget.utils.UiUtils;

/**
 * 房间主页
 */
public class RoomLivingActivity extends BaseViewBindingActivity<KtvSingbattleActivityRoomLivingBinding> {
    private static final String EXTRA_ROOM_INFO = "roomInfo";

    private RoomLivingViewModel roomLivingViewModel;
    private MusicSettingDialog musicSettingDialog;
    private BindingSingleAdapter<RoomSeatModel, KtvSingbattleItemRoomSpeakerBinding> mRoomSpeakerAdapter;
    private KtvCommonDialog creatorExitDialog;

    private CommonDialog exitDialog;
    private UserLeaveSeatMenuDialog mUserLeaveSeatMenuDialog;
    private SongDialog mChooseSongDialog;

    // 房间存活时间，单位ms
    private KtvCommonDialog timeUpExitDialog;
    private KtvCommonDialog gameExitDialog;


    public static void launch(Context context, JoinRoomOutputModel roomInfo) {
        Intent intent = new Intent(context, RoomLivingActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        context.startActivity(intent);
    }

    @Override
    protected KtvSingbattleActivityRoomLivingBinding getViewBinding(@NonNull LayoutInflater inflater) {
        return KtvSingbattleActivityRoomLivingBinding.inflate(inflater);
    }

    @Override
    public void initView(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            finish();
            return;
        }
        getWindow().getDecorView().setKeepScreenOn(true);
        roomLivingViewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> aClass) {
                return (T) new RoomLivingViewModel((JoinRoomOutputModel) getIntent().getSerializableExtra(EXTRA_ROOM_INFO));
            }
        }).get(RoomLivingViewModel.class);

        mRoomSpeakerAdapter = new BindingSingleAdapter<RoomSeatModel, KtvSingbattleItemRoomSpeakerBinding>() {
            @Override
            public void onBindViewHolder(@NonNull BindingViewHolder<KtvSingbattleItemRoomSpeakerBinding> holder, int position) {
                RoomSeatModel item = getItem(position);
                KtvSingbattleItemRoomSpeakerBinding binding = holder.binding;

                boolean isOutSeat = item == null || TextUtils.isEmpty(item.getUserNo());
                binding.getRoot().setOnClickListener(v -> {
                    if (!isOutSeat) {
                        // 下麦
                        if (roomLivingViewModel.isRoomOwner()) {
                            if (!item.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                                if (roomLivingViewModel.singBattleGameStatusMutableLiveData.getValue() == RoomLivingViewModel.GameStatus.ON_START) {
                                    ToastUtils.showToast(R.string.ktv_singbattle_gaming_seat_tip1);
                                    return;
                                }
                                showUserLeaveSeatMenuDialog(item);
                            }
                        } else if (item.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                            if (roomLivingViewModel.singBattleGameStatusMutableLiveData.getValue() == RoomLivingViewModel.GameStatus.ON_START) {
                                ToastUtils.showToast(R.string.ktv_singbattle_gaming_seat_tip2);
                                return;
                            }
                            showUserLeaveSeatMenuDialog(item);
                        }
                    } else {
                        // 上麦
                        RoomSeatModel seatLocal = roomLivingViewModel.seatLocalLiveData.getValue();
                        if (seatLocal == null || seatLocal.getSeatIndex() < 0) {
                            if (roomLivingViewModel.singBattleGameStatusMutableLiveData.getValue() == RoomLivingViewModel.GameStatus.ON_START) {
                                ToastUtils.showToast(R.string.ktv_singbattle_gaming_seat_tip3);
                                return;
                            }
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
                    binding.tvUserName.setText(getString(R.string.ktv_singbattle_seat_num, String.valueOf(position + 1)));
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
                                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                                .apply(RequestOptions.circleCropTransform())
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
                    if (songModel != null && !songModel.getWinnerNo().equals("")) {
                        if (item.getUserNo().equals(songModel.getWinnerNo())) {
                            binding.tvZC.setText("主唱");
                            binding.tvHC.setVisibility(View.GONE);
                            binding.tvZC.setVisibility(View.VISIBLE);
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
            if (roomLivingViewModel.isRoomOwner()) {
                toggleAudioRun = () -> {
                    roomLivingViewModel.init();
                    roomLivingViewModel.setLrcView(getBinding().lrcControlView);
                };
                requestRecordPermission();
            } else {
                roomLivingViewModel.init();
                roomLivingViewModel.setLrcView(getBinding().lrcControlView);
            }
        });
        getBinding().singBattleGameView.setIsRoomOwner(roomLivingViewModel.isRoomOwner());

        if (!TextUtils.isEmpty(roomLivingViewModel.roomInfoLiveData.getValue().getBgOption())) {
            setPlayerBgFromMsg(Integer.parseInt(roomLivingViewModel.roomInfoLiveData.getValue().getBgOption()));
        } else {
            setPlayerBgFromMsg(0);
        }
        getBinding().tvRoomName.setText(roomLivingViewModel.roomInfoLiveData.getValue().getRoomName());
        GlideApp.with(getBinding().getRoot())
                .load(roomLivingViewModel.roomInfoLiveData.getValue().getCreatorAvatar())
                .error(io.agora.scene.widget.R.mipmap.default_user_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(getBinding().ivOwnerAvatar);

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
            new TopFunctionDialog(RoomLivingActivity.this,false).show();
        });

        setOnApplyWindowInsetsListener(getBinding().superLayout);
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

    private final Runnable onGraspDisableTask = new Runnable() {
        @Override
        public void run() {
            if (getBinding() != null) {
                getBinding().lrcControlView.onGraspDiasble();
            }
        }
    };

    private final Runnable onGraspFinshTask = new Runnable() {
        @Override
        public void run() {
            roomLivingViewModel.onGraspFinish();
        }
    };

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
        getBinding().singBattleGameView.setSingBattleGameEventListener(new SingBattleGameView.OnSingBattleGameEventListener() {
            @Override
            public void onChooseSongClick() {
                showChooseSongDialog();
            }

            @Override
            public void onAutoSelectSongClick() {
                //roomLivingViewModel.autoSelectMusic();
            }

            @Override
            public void onGameStart() {
                roomLivingViewModel.onSongPlaying();
                getBinding().singBattleGameView.setSongTotalNum(roomLivingViewModel.songsOrderedLiveData.getValue().size());
            }

            @Override
            public void onStartSing() {
                getBinding().lrcControlView.setMusic(null);
                roomLivingViewModel.onSongPlaying();
                getBinding().lrcControlView.onGamingStatus();
                mRoomSpeakerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onNextSong() {
                roomLivingViewModel.onSongPlaying();
                if (roomLivingViewModel.songsOrderedLiveData.getValue() != null) {
                    getBinding().singBattleGameView.onBattleGamePrepare(roomLivingViewModel.songsOrderedLiveData.getValue().size());
                }
                getBinding().lrcControlView.onGameBattlePrepareStatus();
                mRoomSpeakerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onGameEnd() {
                getBinding().singBattleGameView.onGameEnd(roomLivingViewModel.getRankList());
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.finishSingBattleGame();
                }
            }

            @Override
            public void onGameAgainClick() {
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.changeMusic();
                    roomLivingViewModel.prepareSingBattleGame();
                }
            }
        });
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
        roomLivingViewModel.roomDeleteLiveData.observe(this, deletedByCreator -> {
            if (deletedByCreator) {
                showCreatorExitDialog();
            } else {
                finish();
            }
        });
        roomLivingViewModel.roomUserCountLiveData.observe(this, count ->
                getBinding().tvRoomMCount.setText(getString(R.string.ktv_singbattle_room_count, String.valueOf(count))));
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
            for (RoomSeatModel seatModel : seatModels) {
                RoomSeatModel oSeatModel = mRoomSpeakerAdapter.getItem(seatModel.getSeatIndex());
                if (oSeatModel == null
                        || oSeatModel.isAudioMuted() != seatModel.isAudioMuted()
                        || oSeatModel.isVideoMuted() != seatModel.isVideoMuted()) {
                    mRoomSpeakerAdapter.replace(seatModel.getSeatIndex(), seatModel);
                }
            }

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
            KTVLogger.d("KTV_Scene_LOG", "playerMusicStatusLiveData: " + status);
            if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PREPARE) {
                getBinding().lrcControlView.onPrepareStatus(roomLivingViewModel.isRoomOwner());
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PLAYING) {
                getBinding().lrcControlView.onPlayStatus(roomLivingViewModel.songPlayingLiveData.getValue());
                if (roomLivingViewModel.isRoomOwner() && roomLivingViewModel.songPlayingLiveData.getValue() != null && roomLivingViewModel.songPlayingLiveData.getValue().getWinnerNo().equals("")) {
                    getBinding().lrcControlView.startSingBattlePrepareTimeCount();

                    getBinding().getRoot().removeCallbacks(onGraspDisableTask);
                    getBinding().getRoot().postDelayed(() -> {
                        onGraspDisableTask.run();
                    }, 12000);
                    getBinding().getRoot().removeCallbacks(onGraspFinshTask);
                    getBinding().getRoot().postDelayed(() -> {
                       onGraspFinshTask.run();
                    }, 13000);
                }
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_BATTLE) {
                getBinding().lrcControlView.onPlayStatus(roomLivingViewModel.songPlayingLiveData.getValue());
                if (roomLivingViewModel.songPlayingLiveData.getValue() != null && roomLivingViewModel.songPlayingLiveData.getValue().getWinnerNo().equals("")) {
                    getBinding().lrcControlView.startSingBattlePrepareTimeCount();
                    getBinding().getRoot().removeCallbacks(onGraspDisableTask);
                    getBinding().getRoot().postDelayed(() -> {
                        onGraspDisableTask.run();
                    }, 12000);
                    getBinding().getRoot().removeCallbacks(onGraspFinshTask);
                    getBinding().getRoot().postDelayed(() -> {
                        onGraspFinshTask.run();
                    }, 13000);
                }
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_PAUSE) {
                getBinding().lrcControlView.onPauseStatus();
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_LRC_RESET) {
                getBinding().lrcControlView.getLyricsView().reset();
                //TODO
                //getBinding().singBattleGameView.onSongFinish(0);
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_CHANGING_START) {
                getBinding().lrcControlView.setEnabled(false);
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_CHANGING_END) {
                getBinding().lrcControlView.setEnabled(true);
            } else if (status == RoomLivingViewModel.PlayerMusicStatus.ON_LEAVE) {
                getBinding().singBattleGameView.onSongFinish(0);
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.changeMusic();
                }
            }
        });

        roomLivingViewModel.singBattleGameStatusMutableLiveData.observe(this, status -> {
            if (status == RoomLivingViewModel.GameStatus.ON_START) {
                if (roomLivingViewModel.songsOrderedLiveData.getValue() != null) {
                    getBinding().singBattleGameView.onGameStartStatus();
                }
                getBinding().lrcControlView.onGameBattlePrepareStatus();
                roomLivingViewModel.toggleMic(false);
                getBinding().cbMic.setEnabled(false);
                getBinding().groupEmptyPrompt.setVisibility(View.GONE);
            } else if (status == RoomLivingViewModel.GameStatus.ON_WAITING) {
                getBinding().singBattleGameView.onGameWaitingStatus();
            } else if (status == RoomLivingViewModel.GameStatus.ON_END) {
                getBinding().singBattleGameView.onGameEnd(roomLivingViewModel.getRankList());
                getBinding().cbMic.setEnabled(true);
                if ((!roomLivingViewModel.isRoomOwner()) && (!roomLivingViewModel.isOnSeat)) {
                    getBinding().groupEmptyPrompt.setVisibility(View.VISIBLE);
                }
            } else if (status == RoomLivingViewModel.GameStatus.ON_ERROR) {
                showGameExitDialog();
            }
        });
        roomLivingViewModel.graspStatusMutableLiveData.observe(this, model -> {
            if (model.status == RoomLivingViewModel.GraspStatus.SUCCESS) {
                roomLivingViewModel.musicStop();
                getBinding().singBattleGameView.onGraspSongSuccess(model.userName, model.headUrl);

                // 抢唱成功， 更新麦克风状态
                if (model.userId.equals(UserManager.getInstance().getUser().id.toString())) {
                    roomLivingViewModel.toggleMic(true);
                } else {
                    roomLivingViewModel.toggleMic(false);
                }
            } else if (model.status == RoomLivingViewModel.GraspStatus.EMPTY) {
                getBinding().singBattleGameView.onNobodyGraspSong();
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.musicStop();
                    roomLivingViewModel.changeMusic();
                }
            }
        });
        roomLivingViewModel.playerMusicOpenDurationLiveData.observe(this, duration -> {
            getBinding().lrcControlView.getLyricsView().setDuration(duration);
        });
        roomLivingViewModel.playerMusicPlayCompleteLiveData.observe(this, score -> {
            mRoomSpeakerAdapter.notifyDataSetChanged();
            roomLivingViewModel.toggleMic(false);
            if (score.isLocal()) {
                int sc = getBinding().lrcControlView.getCumulativeScoreInPercentage();
                if (getBinding().lrcControlView.getRole() == LrcControlView.Role.Singer) {
                    roomLivingViewModel.syncSingingAverageScore(sc);
                }

                getBinding().singBattleGameView.onSongFinish(sc);
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.changeMusic();
                }
            } else {
                //if (getBinding().lrcControlView.getRole() != LrcControlView.Role.Listener) return;
                getBinding().singBattleGameView.onSongFinish(score.getScore());
                if (roomLivingViewModel.isRoomOwner()) {
                    roomLivingViewModel.changeMusic();
                }
            }
        });
        roomLivingViewModel.networkStatusLiveData.observe(this, netWorkStatus ->
                setNetWorkStatus(netWorkStatus.txQuality, netWorkStatus.rxQuality));
    }


    private void setNetWorkStatus(int txQuality, int rxQuality) {
        if (txQuality == Constants.QUALITY_BAD || txQuality == Constants.QUALITY_POOR
                || rxQuality == Constants.QUALITY_BAD || rxQuality == Constants.QUALITY_POOR) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_yellow);
            getBinding().tvNetStatus.setText(R.string.ktv_singbattle_net_status_m);
        } else if (txQuality == Constants.QUALITY_VBAD || txQuality == Constants.QUALITY_DOWN
                || rxQuality == Constants.QUALITY_VBAD || rxQuality == Constants.QUALITY_DOWN) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.ktv_singbattle_net_status_low);
        } else if (txQuality == Constants.QUALITY_EXCELLENT || txQuality == Constants.QUALITY_GOOD
                || rxQuality == Constants.QUALITY_EXCELLENT || rxQuality == Constants.QUALITY_GOOD) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.ktv_singbattle_net_status_good);
        } else if (txQuality == Constants.QUALITY_UNKNOWN || rxQuality == Constants.QUALITY_UNKNOWN) {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_red);
            getBinding().tvNetStatus.setText(R.string.ktv_singbattle_net_status_un_know);
        } else {
            getBinding().ivNetStatus.setImageResource(R.drawable.bg_round_green);
            getBinding().tvNetStatus.setText(R.string.ktv_singbattle_net_status_good);
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
            timeUpExitDialog.setDialogBtnText("", getString(R.string.ktv_singbattle_confirm));
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

    private void showGameExitDialog() {
        if (gameExitDialog == null) {
            gameExitDialog = new KtvCommonDialog(this);

            gameExitDialog.setDescText("当前房间正在游戏中，请退出");
            gameExitDialog.setDialogBtnText("", getString(R.string.ktv_singbattle_confirm));
            gameExitDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                }

                @Override
                public void onRightButtonClick() {
                    roomLivingViewModel.exitRoom();
                }
            });
        }
        gameExitDialog.show();
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
            exitDialog.setDialogBtnText(getString(R.string.ktv_singbattle_cancel), getString(R.string.ktv_singbattle_confirm));
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
        if (!music.getWinnerNo().equals("")) {
            if (UserManager.getInstance().getUser().id.toString().equals(music.getWinnerNo())) {
                getBinding().lrcControlView.setRole(LrcControlView.Role.Singer);
            } else {
                getBinding().lrcControlView.setRole(LrcControlView.Role.Listener);
            }
        }
        roomLivingViewModel.resetMusicStatus();
        roomLivingViewModel.musicStartPlay(music);
        mRoomSpeakerAdapter.notifyDataSetChanged();
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
                value = getString(R.string.ktv_singbattle_song_rank_7);
                ret.put(key, value);
            } else if (key == 3 || key == 4 || key == 6) {
                ret.put(key, value);
            }
        }
        return ret;
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

                SongActionListenerImpl chooseSongListener = new SongActionListenerImpl(this, roomLivingViewModel, filterSongTypeMap(typeMap));
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

    private void showMusicSettingDialog() {
        //if (musicSettingDialog == null) {
        musicSettingDialog = new MusicSettingDialog(roomLivingViewModel.mSetting, roomLivingViewModel.playerMusicStatusLiveData.getValue() == RoomLivingViewModel.PlayerMusicStatus.ON_PAUSE);
        //}
        musicSettingDialog.show(getSupportFragmentManager(), MusicSettingDialog.TAG);
    }

    private CommonDialog changeMusicDialog;

    private void showChangeMusicDialog() {
        if (UiUtils.isFastClick(2000)) {
            ToastUtils.showToast(R.string.ktv_singbattle_too_fast);
            return;
        }
        if (changeMusicDialog == null) {
            changeMusicDialog = new CommonDialog(this);
            changeMusicDialog.setDialogTitle(getString(R.string.ktv_singbattle_room_change_music_title));
            changeMusicDialog.setDescText(getString(R.string.ktv_singbattle_room_change_music_msg));
            changeMusicDialog.setDialogBtnText(getString(R.string.ktv_singbattle_cancel), getString(R.string.ktv_singbattle_confirm));
            changeMusicDialog.setOnButtonClickListener(new OnButtonClickListener() {
                @Override
                public void onLeftButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                }

                @Override
                public void onRightButtonClick() {
                    setDarkStatusIcon(isBlackDarkStatus());
                    roomLivingViewModel.toggleMic(false);
                    getBinding().singBattleGameView.onSongFinish(0);
                    roomLivingViewModel.syncSingingAverageScore(0);
                    roomLivingViewModel.resetMusicStatus();
                    if (roomLivingViewModel.isRoomOwner()) {
                        roomLivingViewModel.changeMusic();
                    }
                }
            });
        }
        changeMusicDialog.show();
    }

    public void setPlayerBgFromMsg(int position) {
        getBinding().lrcControlView.setLrcViewBackground(R.mipmap.mvbg4);
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
            creatorExitDialog.setDialogBtnText("", getString(R.string.ktv_singbattle_iknow));
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
        getBinding().getRoot().removeCallbacks(onGraspDisableTask);
        getBinding().getRoot().removeCallbacks(onGraspFinshTask);
        roomLivingViewModel.release();
        if (SceneConfigManager.INSTANCE.getLogUpload()) {
            LogUploader.INSTANCE.uploadLog(LogUploader.SceneType.CHAT_SPATIAL);
        }
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

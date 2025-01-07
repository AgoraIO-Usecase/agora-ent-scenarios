package io.agora.scene.ktv.singbattle.live;

import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION;
import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_SUPERVISE;
import static io.agora.ktvapi.KTVApiKt.createKTVApi;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.video.ContentInspectConfig;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.event.NetWorkEvent;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.resourceManager.DownloadManager;
import io.agora.scene.ktv.singbattle.KTVLogger;
import io.agora.scene.ktv.singbattle.R;
import io.agora.scene.ktv.singbattle.debugSettings.KTVDebugSettingBean;
import io.agora.scene.ktv.singbattle.debugSettings.KTVDebugSettingsDialog;
import io.agora.ktvapi.*;
import io.agora.scene.ktv.singbattle.live.listener.SongLoadFailReason;
import io.agora.scene.ktv.singbattle.live.listener.SongLoadStateListener;
import io.agora.scene.ktv.singbattle.service.ChangeMVCoverInputModel;
import io.agora.scene.ktv.singbattle.service.ChooseSongInputModel;
import io.agora.scene.ktv.singbattle.service.JoinRoomOutputModel;
import io.agora.scene.ktv.singbattle.service.KTVServiceProtocol;
import io.agora.scene.ktv.singbattle.service.KTVSingBattleGameService;
import io.agora.scene.ktv.singbattle.service.KTVSyncManagerServiceImp;
import io.agora.scene.ktv.singbattle.service.MakeSongTopInputModel;
import io.agora.scene.ktv.singbattle.service.OnSeatInputModel;
import io.agora.scene.ktv.singbattle.service.OutSeatInputModel;
import io.agora.scene.ktv.singbattle.service.RankModel;
import io.agora.scene.ktv.singbattle.service.RemoveSongInputModel;
import io.agora.scene.ktv.singbattle.service.RoomSeatModel;
import io.agora.scene.ktv.singbattle.service.RoomSelSongModel;
import io.agora.scene.ktv.singbattle.service.ScoringAlgoControlModel;
import io.agora.scene.ktv.singbattle.service.ScoringAverageModel;
import io.agora.scene.ktv.singbattle.service.SingBattleGameStatus;
import io.agora.scene.ktv.singbattle.service.api.KtvApiManager;
import io.agora.scene.ktv.singbattle.service.api.KtvSongApiModel;
import io.agora.scene.ktv.singbattle.widget.MusicSettingBean;
import io.agora.scene.ktv.singbattle.widget.MusicSettingDialog;
import io.agora.scene.ktv.singbattle.widget.rankList.RankItem;
import io.agora.scene.widget.toast.CustomToast;

public class RoomLivingViewModel extends ViewModel {

    private final String TAG = "KTV_Scene_LOG";
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();
    private KTVApi ktvApiProtocol;
    private KtvApiManager ktvApiManager = new KtvApiManager();

    // loading dialog
    private final MutableLiveData<Boolean> _loadingDialogVisible = new MutableLiveData<>(false);
    final LiveData<Boolean> loadingDialogVisible = _loadingDialogVisible;

    /**
     * Room information
     */
    final MutableLiveData<JoinRoomOutputModel> roomInfoLiveData;
    final MutableLiveData<Boolean> roomDeleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Boolean> roomTimeUpLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> roomUserCountLiveData = new MutableLiveData<>(0);

    /**
     * Seat information
     */
    boolean isOnSeat = false;
    final MutableLiveData<List<RoomSeatModel>> seatListLiveData = new MutableLiveData<>(new ArrayList<>());
    final MutableLiveData<RoomSeatModel> seatLocalLiveData = new MutableLiveData<>();

    /**
     * Lyrics information
     */
    final MutableLiveData<List<RoomSelSongModel>> songsOrderedLiveData = new MutableLiveData<>();
    final MutableLiveData<RoomSelSongModel> songPlayingLiveData = new MutableLiveData<>();

    class LineScore {
        int score;
        int index;
        int cumulativeScore;
        int total;
    }

    final MutableLiveData<LineScore> mainSingerScoreLiveData = new MutableLiveData<>();

    /**
     * Player/RTC information
     */
    int streamId = 0;

    enum PlayerMusicStatus {
        ON_PREPARE,
        ON_PLAYING,
        ON_BATTLE,
        ON_PAUSE,
        ON_STOP,
        ON_LRC_RESET,
        ON_CHANGING_START,
        ON_CHANGING_END,
        ON_LEAVE
    }

    final MutableLiveData<PlayerMusicStatus> playerMusicStatusLiveData = new MutableLiveData<>();

    final MutableLiveData<Boolean> noLrcLiveData = new MutableLiveData<>();


    enum GameStatus {
        ON_WAITING,
        ON_START,
        ON_END,
        ON_ERROR
    }

    final MutableLiveData<GameStatus> singBattleGameStatusMutableLiveData = new MutableLiveData<>();

    enum GraspStatus {
        IDLE,
        SUCCESS,
        FAILED,
        EMPTY
    }

    class GraspModel {
        GraspStatus status;
        String userId;
        String userName;
        String headUrl;
    }

    final MutableLiveData<GraspModel> graspStatusMutableLiveData = new MutableLiveData<>();

    final MutableLiveData<Long> playerMusicOpenDurationLiveData = new MutableLiveData<>();
    final MutableLiveData<ScoringAverageModel> playerMusicPlayCompleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> playerMusicCountDownLiveData = new MutableLiveData<>();
    final MutableLiveData<NetWorkEvent> networkStatusLiveData = new MutableLiveData<>();

    final MutableLiveData<ScoringAlgoControlModel> scoringAlgoControlLiveData = new MutableLiveData<>();

    private final Map<String, RankModel> rankMap = new HashMap<>();

    /**
     * Rtc engine
     */
    private RtcEngineEx mRtcEngine;

    /**
     * Main version audio settings
     */
    private final ChannelMediaOptions mainChannelMediaOption = new ChannelMediaOptions();

    /**
     * Player configuration
     */
    MusicSettingBean mSetting;

    /**
     * Whether to enable background playback
     */
    KTVDebugSettingBean mDebugSetting;

    /**
     * Whether to enable background playback
     */
    private boolean isBackPlay = false;

    /**
     * Whether to enable earback
     */
    private boolean isOpnEar = false;

    public RoomLivingViewModel(JoinRoomOutputModel roomInfo) {
        this.roomInfoLiveData = new MutableLiveData<>(roomInfo);
    }

    public boolean isRoomOwner() {
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().id.toString());
    }

    public void init() {
        initRTCPlayer();
        initRoom();
        initSeats();
        initSongs();
        initSingBattleGame();
        initReConnectEvent();
    }

    public boolean release() {
        KTVLogger.d(TAG, "release called");
        streamId = 0;
        if (mRtcEngine != null) {
            ktvApiProtocol.release();
        }

        if (mRtcEngine != null) {
            mRtcEngine.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
            mRtcEngine.leaveChannel();
            RtcEngineEx.destroy();
            mRtcEngine = null;
            return true;
        }
        return false;
    }

    // ======================= Disconnection reconnection related =======================
    public void initReConnectEvent() {
        ktvServiceProtocol.subscribeReConnectEvent(() -> {
            reFetchUserNum();
            reFetchSeatStatus();
            reFetchSongStatus();
            return null;
        });
    }

    private void reFetchUserNum() {
        KTVLogger.d(TAG, "reFetchUserNum: call");
        ktvServiceProtocol.getAllUserList(num -> {
            roomUserCountLiveData.postValue(num);
            return null;
        }, null);
    }

    private void reFetchSeatStatus() {
        KTVLogger.d(TAG, "reFetchSeatStatus: call");
        ktvServiceProtocol.getSeatStatusList((e, data) -> {
            if (e == null && data != null) {
                KTVLogger.d(TAG, "getSeatStatusList: return" + data);
                seatListLiveData.setValue(data);
            }
            return null;
        });
    }

    private void reFetchSongStatus() {
        KTVLogger.d(TAG, "reFetchSongStatus: call");
        onSongChanged();
    }

    // ======================= Room related =======================
    public void initRoom() {
        JoinRoomOutputModel _roomInfo = roomInfoLiveData.getValue();
        if (_roomInfo == null) {
            throw new RuntimeException("The roomInfo must be not null before initSeats method calling!");
        }

        roomUserCountLiveData.postValue(_roomInfo.getRoomPeopleNum());

        ktvServiceProtocol.subscribeRoomStatus((ktvSubscribe, vlRoomListModel) -> {
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted");
                roomDeleteLiveData.postValue(true);
            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                // Triggered when the room status changes
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated");
                if (!vlRoomListModel.getBgOption().equals(_roomInfo.getBgOption())) {
                    roomInfoLiveData.postValue(new JoinRoomOutputModel(
                            _roomInfo.getRoomName(),
                            _roomInfo.getRoomNo(),
                            _roomInfo.getCreatorNo(),
                            _roomInfo.getCreatorAvatar(),
                            vlRoomListModel.getBgOption(),
                            _roomInfo.getSeatsArray(),
                            _roomInfo.getRoomPeopleNum(),
                            _roomInfo.getAgoraRTMToken(),
                            _roomInfo.getAgoraRTCToken(),
                            _roomInfo.getAgoraChorusToken(),
                            _roomInfo.getCreatedAt()
                    ));
                }
            }
            return null;
        });

        ktvServiceProtocol.subscribeUserListCount(count -> {
            roomUserCountLiveData.postValue(count);
            return null;
        });

        ktvServiceProtocol.subscribeRoomTimeUp(() -> {
            roomTimeUpLiveData.postValue(true);
            return null;
        });
    }

    /**
     * Exit room
     */
    public void exitRoom() {
        KTVLogger.d(TAG, "RoomLivingViewModel.exitRoom() called");
        ktvServiceProtocol.leaveRoom(e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.exitRoom() success");
                roomDeleteLiveData.postValue(false);
                roomTimeUpLiveData.postValue(false);
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(), Toast.LENGTH_SHORT);
            }
            return null;
        });
    }

    // ======================= Seat related =======================
    public void initSeats() {
        JoinRoomOutputModel _roomInfo = roomInfoLiveData.getValue();
        if (_roomInfo == null) {
            throw new RuntimeException("The roomInfo must be not null before initSeats method calling!");
        }
        List<RoomSeatModel> seatsArray = _roomInfo.getSeatsArray();
        seatListLiveData.postValue(seatsArray);

        if (seatsArray != null) {
            for (RoomSeatModel roomSeatModel : seatsArray) {
                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    seatLocalLiveData.setValue(roomSeatModel);
                    isOnSeat = true;
                    if (mRtcEngine != null) {
                        mainChannelMediaOption.publishCameraTrack = roomSeatModel.isVideoMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE();
                        mainChannelMediaOption.publishMicrophoneTrack = true;
                        mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                        mainChannelMediaOption.autoSubscribeVideo = true;
                        mainChannelMediaOption.autoSubscribeAudio = true;
                        mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                        mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);

                        updateVolumeStatus(roomSeatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE());
                    }
                    break;
                }
            }
        }
        if (seatLocalLiveData.getValue() == null) {
            seatLocalLiveData.setValue(null);
        }

        ktvServiceProtocol.subscribeSeatList((ktvSubscribe, roomSeatModel) -> {
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated) {
                KTVLogger.d(TAG, "subscribeSeatList KTVSubscribeCreated");
                List<RoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<RoomSeatModel> value = new ArrayList<>(oValue);
                value.add(roomSeatModel);
                seatListLiveData.postValue(value);

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    seatLocalLiveData.setValue(roomSeatModel);
                    updateVolumeStatus(roomSeatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE());
                }

            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                KTVLogger.d(TAG, "subscribeSeatList KTVSubscribeUpdated");
                List<RoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<RoomSeatModel> value = new ArrayList<>(oValue);
                int index = -1;
                for (int i = 0; i < value.size(); i++) {
                    if (value.get(i).getSeatIndex() == roomSeatModel.getSeatIndex()) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    value.remove(index);
                    value.add(index, roomSeatModel);
                    seatListLiveData.postValue(value);

                    if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                        seatLocalLiveData.setValue(roomSeatModel);
                        updateVolumeStatus(roomSeatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_FALSE());
                    }
                }

            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                KTVLogger.d(TAG, "subscribeSeatList KTVSubscribeDeleted");
                List<RoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<RoomSeatModel> value = new ArrayList<>(oValue);
                Iterator<RoomSeatModel> iterator = value.iterator();
                while (iterator.hasNext()) {
                    RoomSeatModel next = iterator.next();
                    if (next.getUserNo().equals(roomSeatModel.getUserNo())) {
                        iterator.remove();
                    }
                }
                seatListLiveData.postValue(value);

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    seatLocalLiveData.postValue(null);
                }


                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    isOnSeat = false;
                    if (mRtcEngine != null) {
                        mainChannelMediaOption.publishCameraTrack = false;
                        mainChannelMediaOption.publishMicrophoneTrack = false;
                        mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                        mainChannelMediaOption.autoSubscribeVideo = true;
                        mainChannelMediaOption.autoSubscribeAudio = true;
                        mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                        mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                    }
                    updateVolumeStatus(false);
                }
            }
            return null;
        });
    }

    /**
     * Seat
     */
    public void haveSeat(int onSeatIndex) {
        KTVLogger.d(TAG, "RoomLivingViewModel.haveSeat() called: " + onSeatIndex);
        ktvServiceProtocol.onSeat(new OnSeatInputModel(onSeatIndex), e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.haveSeat() success");
                isOnSeat = true;
                if (mRtcEngine != null) {
                    mainChannelMediaOption.publishCameraTrack = false;
                    mainChannelMediaOption.publishMicrophoneTrack = true;
                    mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                    mainChannelMediaOption.autoSubscribeVideo = true;
                    mainChannelMediaOption.autoSubscribeAudio = true;
                    mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                    mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                }
                toggleMic(false);
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.haveSeat() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
            }
            return null;
        });
    }

    /**
     * Leave seat
     */
    public void leaveSeat(RoomSeatModel seatModel) {
        KTVLogger.d(TAG, "RoomLivingViewModel.leaveSeat() called");
        ktvServiceProtocol.outSeat(
                new OutSeatInputModel(
                        seatModel.getUserNo(),
                        seatModel.getRtcUid(),
                        seatModel.getName(),
                        seatModel.getHeadUrl(),
                        seatModel.getSeatIndex()
                ),
                e -> {
                    if (e == null) {
                        // success
                        KTVLogger.d(TAG, "RoomLivingViewModel.leaveSeat() success");
                        if (seatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                            isOnSeat = false;
                            if (seatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
                                if (mRtcEngine != null) {
                                    mainChannelMediaOption.publishCameraTrack = false;
                                    mainChannelMediaOption.publishMicrophoneTrack = false;
                                    mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                                    mainChannelMediaOption.autoSubscribeVideo = true;
                                    mainChannelMediaOption.autoSubscribeAudio = true;
                                    mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                                    mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                                }
                                updateVolumeStatus(false);
                            }
                        }
                    } else {
                        // failure
                        KTVLogger.e(TAG, "RoomLivingViewModel.leaveSeat() failed: " + e.getMessage());
                        CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                    }
                    return null;
                });
    }

    /**
     * Toggle camera
     */
    boolean isCameraOpened = false;

    public void toggleSelfVideo(boolean isOpen) {
        KTVLogger.d(TAG, "RoomLivingViewModel.toggleSelfVideo() called：" + isOpen);
        ktvServiceProtocol.updateSeatVideoMuteStatus(!isOpen, e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.toggleSelfVideo() success");
                isCameraOpened = isOpen;
                mRtcEngine.enableLocalVideo(isOpen);
                ChannelMediaOptions channelMediaOption = new ChannelMediaOptions();
                channelMediaOption.publishCameraTrack = isOpen;
                mRtcEngine.updateChannelMediaOptions(channelMediaOption);
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.toggleSelfVideo() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
            }
            return null;
        });
    }

    /**
     * Mute
     */
    public void toggleMic(boolean isUnMute) {
        KTVLogger.d(TAG, "RoomLivingViewModel.toggleMic() called：" + isUnMute);
        updateVolumeStatus(isUnMute);
        ktvServiceProtocol.updateSeatAudioMuteStatus(!isUnMute, e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.toggleMic() success");
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.toggleMic() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
            }
            return null;
        });
    }

    private void updateVolumeStatus(boolean isUnMute) {
        ktvApiProtocol.muteMic(!isUnMute);

        // Adjust earback
        if (!isUnMute && mSetting.isEar()) {
            if (mRtcEngine != null) {
                mRtcEngine.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
            }
        } else if (isUnMute && mSetting.isEar()) {
            if (mRtcEngine != null) {
                mRtcEngine.enableInEarMonitoring(true, Constants.EAR_MONITORING_FILTER_NONE);
            }
        }

        if (isUnMute) {
            KTVLogger.d(TAG, "unmute! setMicVolume: " + micOldVolume);
            if (mRtcEngine != null) {
                mRtcEngine.adjustRecordingSignalVolume(micOldVolume);
            }
        }
    }


    // ======================= Song related =======================
    public void initSongs() {
        ktvServiceProtocol.subscribeChooseSong((ktvSubscribe, songModel) -> {
            // When the song information changes, re-obtain the song list action
            KTVLogger.d(TAG, "subscribeChooseSong updateSongs");
            onSongChanged();
            return null;
        });

        // Get initial song list
        //onSongChanged();
        // TODO
        ktvServiceProtocol.getChoosedSongsList((e, data) -> {
            if (e == null && data != null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success");
                songNum = data.size();
                songsOrderedLiveData.postValue(data);
            } else {
                // failed
                if (e != null) {
                    KTVLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed: " + e.getMessage());
                    CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
            return null;
        });
        songsOrderedLiveData.postValue(new ArrayList<>());
    }

    public int songNum = 0;

    public void onSongChanged() {
        ktvServiceProtocol.getChoosedSongsList((e, data) -> {
            if (e == null && data != null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success" + data);
                songNum = data.size();
                songsOrderedLiveData.postValue(data);

                if (singBattleGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
                    if (!data.isEmpty()) {
                        RoomSelSongModel value = songPlayingLiveData.getValue();
                        RoomSelSongModel songPlaying = data.get(0);

                        if (value != null && value.getWinnerNo() != null && value.getWinnerNo().equals("") && !songPlaying.getWinnerNo().equals("")) {
                            // Everyone updates the grab singing result UI
                            hasReceiveStartSingBattle = false;
                            GraspModel model = new GraspModel();
                            model.status = GraspStatus.SUCCESS;
                            model.userId = songPlaying.getWinnerNo();
                            model.userName = songPlaying.getName();
                            model.headUrl = songPlaying.getImageUrl();
                            graspStatusMutableLiveData.postValue(model);
                        }
                    } else {
                        KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList");
                        songPlayingLiveData.postValue(null);
                    }
                }
            } else {
                // failed
                if (e != null) {
                    KTVLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed: " + e.getMessage());
                    CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
            return null;
        });
    }

    public void onSongPlaying() {
        KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying()");
        if (singBattleGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
            if (songsOrderedLiveData.getValue() != null && songsOrderedLiveData.getValue().size() > 0) {
                RoomSelSongModel value = songPlayingLiveData.getValue();
                RoomSelSongModel songPlaying = songsOrderedLiveData.getValue().get(0);

                if (value == null || !value.getSongNo().equals(songPlaying.getSongNo())) {
                    // No chosen song, directly set the first list as the current playing song
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying() chosen song list is empty");
                    songPlayingLiveData.postValue(songPlaying);
                } else if (!value.getWinnerNo().equals(songPlaying.getWinnerNo())) {
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying() has winner");
                    songPlayingLiveData.postValue(songPlaying);
                }
            } else {
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList");
                //gameSong = null;
                songPlayingLiveData.postValue(null);
            }
        }
    }

    public void getSongChosenList() {
        ktvServiceProtocol.getChoosedSongsList((e, data) -> {
            if (e == null && data != null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() success");
                songsOrderedLiveData.postValue(data);
            } else {
                // failed
                if (e != null) {
                    KTVLogger.e(TAG, "RoomLivingViewModel.getSongChosenList() failed: " + e.getMessage());
                    CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                }
            }
            return null;
        });
    }

    public interface RestfulSongCallback {
        void completion(Exception error);
    }

    private List<KtvSongApiModel> songList = new ArrayList<>();

    private void getRestfulSongList(RestfulSongCallback callback) {
        if (!songList.isEmpty()) {
            callback.completion(null);
            return;
        }
        ktvApiManager.getSongList((error, ktvSongApiModels) -> {
            KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() return error:");
            if (error != null) {
                CustomToast.show(R.string.ktv_singbattle_get_songs_failed, error.getMessage() != null ? error.getMessage() : "");
                callback.completion(error);
            } else {
                songList.clear();
                songList.addAll(ktvSongApiModels);
                callback.completion(null);
            }
            return null;
        });
    }


    /**
     * Get song list
     */
    public LiveData<List<RoomSelSongModel>> getSongList() {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() called");
        MutableLiveData<List<RoomSelSongModel>> liveData = new MutableLiveData<>();
        getRestfulSongList((error) -> {
            KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() return");
            List<RoomSelSongModel> songs = new ArrayList<>();
            // Need to call another interface to get the current song list to supplement the list information >_<
            ktvServiceProtocol.getChoosedSongsList((e, songsChosen) -> {
                if (e == null && songsChosen != null) {
                    // success
                    for (KtvSongApiModel music : songList) {
                        RoomSelSongModel songItem = null;
                        for (RoomSelSongModel chosen : songsChosen) {
                            if (chosen.getSongNo().equals(music.getSongCode())) {
                                songItem = chosen;
                                break;
                            }
                        }
                        if (songItem == null) {
                            songItem = new RoomSelSongModel(
                                    music.getName(),
                                    music.getSongCode(),
                                    music.getSinger(),
                                    "", // imageUrl
                                    "",
                                    "",
                                    0,
                                    "",
                                    0,
                                    0,
                                    0
                            );
                        }
                        songs.add(songItem);
                    }
                    liveData.postValue(songs);
                } else {
                    if (e != null) {
                        CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                    }
                    liveData.postValue(new ArrayList<>());
                }
                return null;
            });
        });

        return liveData;
    }

    /**
     * Choose song
     */
    public LiveData<Boolean> chooseSong(RoomSelSongModel songModel) {
        KTVLogger.d(TAG, "RoomLivingViewModel.chooseSong() called, name:" + songModel.getName());
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        ktvServiceProtocol.chooseSong(
                new ChooseSongInputModel(
                        songModel.getSongName(),
                        songModel.getSongNo(),
                        songModel.getSinger(),
                        songModel.getImageUrl()),
                e -> {
                    if (e == null) {
                        // success
                        KTVLogger.d(TAG, "RoomLivingViewModel.chooseSong() success");
                        liveData.postValue(true);
                    } else {
                        // failure
                        KTVLogger.e(TAG, "RoomLivingViewModel.chooseSong() failed: " + e.getMessage());
                        CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                        liveData.postValue(false);
                    }
                    return null;
                }
        );
        return liveData;
    }

    /**
     * Delete song
     */
    public void deleteSong(RoomSelSongModel songModel) {
        KTVLogger.d(TAG, "RoomLivingViewModel.deleteSong() called, name:" + songModel.getName());
        ktvServiceProtocol.removeSong(false,
                new RemoveSongInputModel(songModel.getSongNo()),
                e -> {
                    if (e == null) {
                        // success: do nothing for subscriber dealing with the event already
                        KTVLogger.d(TAG, "RoomLivingViewModel.deleteSong() success");
                    } else {
                        // failure
                        KTVLogger.e(TAG, "RoomLivingViewModel.deleteSong() failed: " + e.getMessage());
                        CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                    }
                    return null;
                }
        );
    }

    /**
     * Top up song
     */
    public void topUpSong(RoomSelSongModel songModel) {
        KTVLogger.d(TAG, "RoomLivingViewModel.topUpSong() called, name:" + songModel.getName());
        ktvServiceProtocol.makeSongTop(new MakeSongTopInputModel(
                songModel.getSongNo()
        ), e -> {
            if (e == null) {
                // success: do nothing for subscriber dealing with the event already
                KTVLogger.d(TAG, "RoomLivingViewModel.topUpSong() success");
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.topUpSong() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
            }
            return null;
        });
    }

    /**
     * Start song switching
     */
    public void changeMusic() {
        KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() called");
        //gameSong = null;
        RoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            KTVLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed, no song is playing now!");
            return;
        }

        //ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, "", null);

        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_START);
        ktvServiceProtocol.removeSong(true, new RemoveSongInputModel(
                musicModel.getSongNo()
        ), e -> {
            if (e == null) {
                // success do nothing for dealing in song subscriber
                KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() success");
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END);
            } else {
                // failed
                KTVLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed: " + e.getMessage());
                CustomToast.show(e.getMessage(),Toast.LENGTH_SHORT);
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END);
            }
            return null;
        });
    }

    private WeakReference<ILrcView> lrcControlView = null;

    /**
     * Set lyrics view
     */
    public void setLrcView(ILrcView view) {
        lrcControlView = new WeakReference(view);
        ktvApiProtocol.setLrcView(view);
    }

    // ======================= Player/RTC/MPK related =======================
    private void initRTCPlayer() {
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw new NullPointerException("please check \"strings_config.xml\"");
        }
        if (mRtcEngine != null) return;

        // ------------------ Initialize RTC ------------------
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = AgoraApplication.the();
        config.mAppId = BuildConfig.AGORA_APP_ID;
        config.mEventHandler = new IRtcEngineEventHandler() {
            @Override
            public void onUserOffline(int uid, int reason) {
                if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals(String.valueOf(uid))) {
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LEAVE);
                }
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                // Network status callback, local user uid = 0
                if (uid == 0) {
                    networkStatusLiveData.postValue(new NetWorkEvent(txQuality, rxQuality));
                }
            }

            @Override
            public void onContentInspectResult(int result) {
                super.onContentInspectResult(result);
                if (result > 1) {
                    CustomToast.show(R.string.ktv_singbattle_content);
                }
            }

            @Override
            public void onStreamMessage(int uid, int streamId, byte[] data) {
                JSONObject jsonMsg;
                try {
                    String strMsg = new String(data);
                    jsonMsg = new JSONObject(strMsg);
                    if (jsonMsg.getString("cmd").equals("singleLineScore")) {
                        int score = jsonMsg.getInt("score");
                        int index = jsonMsg.getInt("index");
                        int cumulativeScore = jsonMsg.getInt("cumulativeScore");
                        int total = jsonMsg.getInt("total");

                        LineScore lineScore = new LineScore();
                        lineScore.score = score;
                        lineScore.index = index;
                        lineScore.cumulativeScore = cumulativeScore;
                        lineScore.total = total;
                        mainSingerScoreLiveData.postValue(lineScore);
                    } else if (jsonMsg.getString("cmd").equals("SingingScore")) {
                        KTVLogger.d("hugo", "onMessage/SingingScore: " + jsonMsg);
                        float score = (float) jsonMsg.getDouble("score");
                        String userId = (String) jsonMsg.getString("userId");
                        String userName = (String) jsonMsg.getString("userName");
                        String poster = (String) jsonMsg.getString("poster");
                        playerMusicPlayCompleteLiveData.postValue(new ScoringAverageModel(false, (int) score));

                        // Local singing counts into rank
                        if (rankMap.containsKey(userId)) {
                            RankModel oldModel = rankMap.get(userId);
                            RankModel model = new RankModel(
                                    oldModel.getUserName(),
                                    oldModel.getSongNum() + 1,
                                    (int) (oldModel.getScore() * oldModel.getSongNum() + score),
                                    poster
                            );
                            rankMap.put(userId, model);
                        } else {
                            RankModel model = new RankModel(
                                    userName,
                                    1,
                                    (int) score,
                                    poster
                            );
                            rankMap.put(userId, model);
                        }
                    } else if (jsonMsg.getString("cmd").equals("StartSingBattleCountDown")) {
                        KTVLogger.d(TAG, "StartSingBattleCountDown");
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_BATTLE);
                        hasReceiveStartSingBattle = true;
                    }
                } catch (JSONException exp) {
                    KTVLogger.e(TAG, "onStreamMessage:" + exp);
                }
            }
        };
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING;
        try {
            mRtcEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            KTVLogger.e(TAG, "RtcEngine.create() called error: " + e);
        }
        mRtcEngine.loadExtensionProvider("agora_drm_loader");


        // ------------------Scenario api initialization ------------------
        KTVApi.Companion.setDebugMode(AgoraApplication.the().isDebugModeOpen());
        if (AgoraApplication.the().isDebugModeOpen()) {
            KTVApi.Companion.setMccDomain("api-test.agora.io");
        }
        ktvApiProtocol = createKTVApi(new KTVApiConfig(
                BuildConfig.AGORA_APP_ID,
                roomInfoLiveData.getValue().getAgoraRTMToken(),
                mRtcEngine,
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id.intValue(),
                roomInfoLiveData.getValue().getRoomNo() + "_ex",
                // mock
                roomInfoLiveData.getValue().getAgoraChorusToken(), 10, KTVType.Normal, KTVMusicType.SONG_URL) //
        );

        ktvApiProtocol.addEventHandler(new IKTVApiEventHandler() {
                                           @Override
                                           public void onMusicPlayerStateChanged(@NonNull io.agora.mediaplayer.Constants.MediaPlayerState state, @NonNull io.agora.mediaplayer.Constants.MediaPlayerReason reason, boolean isLocal) {
                                               switch (state) {
                                                   case PLAYER_STATE_OPEN_COMPLETED:
                                                       playerMusicOpenDurationLiveData.postValue(ktvApiProtocol.getMediaPlayer().getDuration());
                                                       break;
                                                   case PLAYER_STATE_PLAYING:
                                                       //playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                                                       if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals("") && isLocal) {
                                                           ktvApiProtocol.switchAudioTrack(AudioTrackMode.YUAN_CHANG);
                                                           //SyncStartSing();
                                                       }
                                                       break;
                                                   case PLAYER_STATE_PAUSED:
                                                       playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
                                                       break;
                                                   case PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED:
                                                       if (isLocal) {
                                                           playerMusicPlayCompleteLiveData.postValue(new ScoringAverageModel(true, 0));
                                                           playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
                                                       }
                                                       break;
                                                   default:
                                               }
                                           }
                                       }
        );

        if (isRoomOwner()) {
            ktvApiProtocol.muteMic(false);
            isOnSeat = true;
        }

        // ------------------Join channel ------------------
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.enableVideo();
        mRtcEngine.enableLocalVideo(false);
        mRtcEngine.enableAudio();
        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_GAME_STREAMING);
        mRtcEngine.enableAudioVolumeIndication(50, 10, true);
        mRtcEngine.setClientRole(isOnSeat ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);
        int ret = mRtcEngine.joinChannel(
                roomInfoLiveData.getValue().getAgoraRTCToken(),
                roomInfoLiveData.getValue().getRoomNo(),
                null,
                UserManager.getInstance().getUser().id.intValue()
        );
        KTVLogger.d(TAG, "joinRTC() cname : " + roomInfoLiveData.getValue().getRoomNo() + " uid: " + UserManager.getInstance().getUser().id.intValue());
        if (ret != Constants.ERR_OK) {
            KTVLogger.e(TAG, "joinRTC() called error: " + ret);
        }

        // ------------------Start pornographic detection service ------------------
        ContentInspectConfig contentInspectConfig = new ContentInspectConfig();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sceneName", "ktv");
            jsonObject.put("id", UserManager.getInstance().getUser().id.toString());
            contentInspectConfig.extraInfo = jsonObject.toString();
            ContentInspectConfig.ContentInspectModule module1 = new ContentInspectConfig.ContentInspectModule();
            module1.interval = 30;
            module1.type = CONTENT_INSPECT_TYPE_SUPERVISE;
            ContentInspectConfig.ContentInspectModule module2 = new ContentInspectConfig.ContentInspectModule();
            module2.interval = 30;
            module2.type = CONTENT_INSPECT_TYPE_MODERATION;
            contentInspectConfig.modules = new ContentInspectConfig.ContentInspectModule[]{module1, module2};
            contentInspectConfig.moduleCount = 2;
            mRtcEngine.enableContentInspect(true, contentInspectConfig);
        } catch (JSONException e) {
            KTVLogger.e(TAG, e.toString());
        }

        // ------------------   Initialize music playback settings panel ------------------
        mDebugSetting = new KTVDebugSettingBean(new KTVDebugSettingsDialog.Callback() {
            @Override
            public void onAudioDumpEnable(boolean enable) {
                if (enable) {
                    mRtcEngine.setParameters("{\"rtc.debug.enable\": true}");
                    mRtcEngine.setParameters("{\"che.audio.frame_dump\":{\"location\":\"all\",\"action\":\"start\",\"max_size_bytes\":\"120000000\",\"uuid\":\"123456789\",\"duration\":\"1200000\"}}");
                } else {
                    mRtcEngine.setParameters("{\"rtc.debug.enable\": false}");
                }
            }

            @Override
            public void onScoringControl(int level, int offset) {
                scoringAlgoControlLiveData.postValue(new ScoringAlgoControlModel(level, offset));
            }
        });

        mSetting = new MusicSettingBean(false, 100, 50, 0, new MusicSettingDialog.Callback() {
            @Override
            public void onEarChanged(boolean isEar) {
                if (seatLocalLiveData.getValue() == null) return;
                int isMuted = seatLocalLiveData.getValue().isAudioMuted();
                if (isMuted == 1) {
                    isOpnEar = isEar;
                    return;
                }
                if (mRtcEngine != null) {
                    mRtcEngine.enableInEarMonitoring(isEar, Constants.EAR_MONITORING_FILTER_NONE);
                }
            }

            @Override
            public void onMicVolChanged(int vol) {
                setMicVolume(vol);
            }

            @Override
            public void onMusicVolChanged(int vol) {
                setMusicVolume(vol);
            }

            @Override
            public void onEffectChanged(int effect) {
                setAudioEffectPreset(getEffectIndex(effect));
            }

            @Override
            public void onBeautifierPresetChanged(int effect) {
                if (mRtcEngine != null) {
                    switch (effect) {
                        case 0:
                            mRtcEngine.setVoiceBeautifierParameters(Constants.VOICE_BEAUTIFIER_OFF, 0, 0);
                        case 1:
                            mRtcEngine.setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 1, 2);
                        case 2:
                            mRtcEngine.setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 1, 1);
                        case 3:
                            mRtcEngine.setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 2, 2);
                        case 4:
                            mRtcEngine.setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 2, 1);
                    }
                }
            }

            @Override
            public void setAudioEffectParameters(int param1, int param2) {
                if (mRtcEngine != null) {
                    if (param1 == 0) {
                        mRtcEngine.setAudioEffectParameters(Constants.VOICE_CONVERSION_OFF, param1, param2);
                    } else {
                        mRtcEngine.setAudioEffectParameters(Constants.PITCH_CORRECTION, param1, param2);
                    }
                }
            }

            @Override
            public void onToneChanged(int newToneValue) {
                ktvApiProtocol.getMediaPlayer().setAudioPitch(newToneValue);
            }

            @Override
            public void onRemoteVolumeChanged(int volume) {
                KTVApi.Companion.setRemoteVolume(volume);
                mRtcEngine.adjustPlaybackSignalVolume(volume);
            }
        });

        // StreamId used externally
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = false;
            cfg.ordered = false;
            streamId = mRtcEngine.createDataStream(cfg);
        }
    }

    private void setAudioEffectPreset(int effect) {
        if (mRtcEngine == null) {
            return;
        }
        mRtcEngine.setAudioEffectPreset(effect);
    }

    // ======================= Grasping logic =======================
    private void initSingBattleGame() {
        ktvServiceProtocol.subscribeSingBattleGame((ktvSubscribe, gameModel) -> {
            KTVLogger.d(TAG, "subscribeSingBattleGame: " + ktvSubscribe + " " + gameModel);
            if (gameModel.getStatus() == SingBattleGameStatus.waitting.getValue()) {
                singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_WAITING);
            } else if (gameModel.getStatus() == SingBattleGameStatus.started.getValue()) {
                singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_START);
                rankMap.clear();
            } else if (gameModel.getStatus() == SingBattleGameStatus.ended.getValue()) {
                singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_END);
            }
            return null;
        });
        ktvServiceProtocol.getSingBattleGameInfo((e, info) -> {
            if (e == null && info != null) {
                if (info.getStatus() == SingBattleGameStatus.waitting.getValue()) {
                    singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_WAITING);
                } else if (info.getStatus() == SingBattleGameStatus.started.getValue()) {
                    singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_ERROR);
                } else if (info.getStatus() == SingBattleGameStatus.ended.getValue()) {
                    singBattleGameStatusMutableLiveData.postValue(GameStatus.ON_END);
                    KTVLogger.d(TAG, "rank: " + info.getRank());
                    if (info.getRank() != null) {
                        rankMap.putAll(info.getRank());
                    }
                }
            } else {
                ktvServiceProtocol.prepareSingBattleGame(error -> null);
            }
            return null;
        });
    }

    public void startSingBattleGame() {
        KTVLogger.d(TAG, "startSingBattleGame called");
        ktvServiceProtocol.startSingBattleGame(e -> {
            KTVLogger.d(TAG, "startSingBattleGame success");
            return null;
        });
    }

    public void prepareSingBattleGame() {
        KTVLogger.d(TAG, "startPrepareSingBattleGame called");
        ktvServiceProtocol.prepareSingBattleGame(e -> {
            KTVLogger.d(TAG, "startPrepareSingBattleGame success");
            return null;
        });
    }

    public void finishSingBattleGame() {
        KTVLogger.d(TAG, "finishSingBattleGame called");
        ktvServiceProtocol.finishSingBattleGame(rankMap, e -> {
            KTVLogger.d(TAG, "finishSingBattleGame success");
            return null;
        });
    }

    // ------------------ Start grasping ------------------
    public void graspSong() {
        KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() called");
        KTVSingBattleGameService.INSTANCE.graspSong(
                KTVSyncManagerServiceImp.kSceneId,
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id.toString(),
                UserManager.getInstance().getUser().name,
                songPlayingLiveData.getValue().getSongNo(),
                UserManager.getInstance().getUser().headUrl,
                (userId) -> {
                    KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() success " + userId);
                    // Update Service grasp result
                    ktvServiceProtocol.updateSongModel(songPlayingLiveData.getValue().getSongNo(), userId, UserManager.getInstance().getUser().name, UserManager.getInstance().getUser().headUrl, e -> {
                        if (e == null) {
                            KTVLogger.d(TAG, "RoomLivingViewModel.updateSongModel() success " + userId);
                        }
                        return null;
                    });
                    return null;
                },
                null
        );
    }

    public void onGraspFinish() {
        KTVLogger.d(TAG, "RoomLivingViewModel.onGraspFinish() called");
        if (songPlayingLiveData.getValue() == null) return;
        KTVSingBattleGameService.INSTANCE.getWinnerInfo(
                KTVSyncManagerServiceImp.kSceneId,
                roomInfoLiveData.getValue().getRoomNo(),
                songPlayingLiveData.getValue().getSongNo(),
                (userId, userName) -> {
                    KTVLogger.d(TAG, "RoomLivingViewModel.getWinnerInfo() called：" + userId + " success");
                    return null;
                },
                e -> {
                    if (e.getMessage().equals("961")) {
                        KTVLogger.d(TAG, "RoomLivingViewModel.getWinnerInfo() nobody grasp");
                        hasReceiveStartSingBattle = false;
                        GraspModel model = new GraspModel();
                        model.status = GraspStatus.EMPTY;
                        graspStatusMutableLiveData.postValue(model);
                    }
                    return null;
                }
        );
    }

    // ======================= settings =======================
    // ------------------ Audio effect adjustment ------------------
    private int getEffectIndex(int index) {
        switch (index) {
            // Original
            case 0:
                return Constants.AUDIO_EFFECT_OFF;
            // KTV
            case 1:
                return Constants.ROOM_ACOUSTICS_KTV;
            // Concert
            case 2:
                return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT;
            // Studio
            case 3:
                return Constants.ROOM_ACOUSTICS_STUDIO;
            // Phonograph
            case 4:
                return Constants.ROOM_ACOUSTICS_PHONOGRAPH;
            // Spacious
            case 5:
                return Constants.ROOM_ACOUSTICS_SPACIAL;
            // Ethereal
            case 6:
                return Constants.ROOM_ACOUSTICS_ETHEREAL;
            // Popular
            case 7:
                return Constants.STYLE_TRANSFORMATION_POPULAR;
            // R&B
            case 8:
                return Constants.STYLE_TRANSFORMATION_RNB;
        }
        // Original
        return Constants.AUDIO_EFFECT_OFF;
    }

    // ------------------ Volume adjustment ------------------
    private int micOldVolume = 100;

    private void setMusicVolume(int v) {
        ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(v);
        ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(v);
    }

    private void setMicVolume(int v) {
        RoomSeatModel value = seatLocalLiveData.getValue();
        int isMuted = value == null ? RoomSeatModel.Companion.getMUTED_VALUE_TRUE() : value.isAudioMuted();
        if (isMuted == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
            micOldVolume = v;
            KTVLogger.d(TAG, "muted! setMicVolume: " + v);
            return;
        }
        KTVLogger.d(TAG, "unmute! setMicVolume: " + v);
        if (mRtcEngine != null) {
            mRtcEngine.adjustRecordingSignalVolume(v);
        }
    }

    // ------------------ Original/Accompaniment ------------------
    private enum KTVPlayerTrackMode {
        Origin,
        Acc
    }

    protected KTVPlayerTrackMode mAudioTrackMode = KTVPlayerTrackMode.Acc;

    public void musicToggleOriginal() {
        if (mAudioTrackMode == KTVPlayerTrackMode.Origin) {
            ktvApiProtocol.switchAudioTrack(AudioTrackMode.BAN_ZOU);
            mAudioTrackMode = KTVPlayerTrackMode.Acc;
        } else {
            ktvApiProtocol.switchAudioTrack(AudioTrackMode.YUAN_CHANG);
            mAudioTrackMode = KTVPlayerTrackMode.Origin;
        }
    }

    public boolean isOriginalMode() {
        return mAudioTrackMode == KTVPlayerTrackMode.Origin;
    }

    // ------------------ Pause/Play ------------------
    public void musicToggleStart() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            ktvApiProtocol.pauseSing();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            ktvApiProtocol.resumeSing();
        }
    }

    // ------------------ Local video rendering ------------------
    public void renderLocalCameraVideo(SurfaceView surfaceView) {
        if (mRtcEngine == null) return;
        mRtcEngine.startPreview();
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, 0));
    }

    // ------------------ Remote video rendering ------------------
    public void renderRemoteCameraVideo(SurfaceView surfaceView, int uid) {
        if (mRtcEngine == null) return;
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, uid));
    }

    // ------------------ Reset song status (when song switching) ------------------
    public void resetMusicStatus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called");
        retryTimes = 0;
        mAudioTrackMode = KTVPlayerTrackMode.Acc;
        ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null);

        // Reset earback
        mSetting.setEar(false);
        if (mRtcEngine != null) {
            mRtcEngine.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
        }
    }


    private Boolean needPrelude;
    // ------------------ Song start playback ------------------
    private int retryTimes = 0;

    public void musicStartPlay(@NonNull RoomSelSongModel music) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called");
        if (music.getUserNo() == null) return;
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);

        boolean isOwnSong;
//        String jsonOption;
        int mainSingerUid;
        if (music.getWinnerNo().equals("")) {
            isOwnSong = Objects.equals(music.getUserNo(), UserManager.getInstance().getUser().id.toString());
//            jsonOption = "{\"format\":{\"highPart\":0}}";
            needPrelude = false;
            mainSingerUid = Integer.parseInt(music.getUserNo());
        } else {
            isOwnSong = Objects.equals(music.getWinnerNo(), UserManager.getInstance().getUser().id.toString());
            // After grabbing the singing right, the selected segment is with buffer, the original segment is +5s before and +3s after. For lyrics, only the lyrics without buffer are displayed
//            jsonOption = "{\"format\":{\"highPartIndex\":0}}";
            needPrelude = true;
            mainSingerUid = Integer.parseInt(music.getWinnerNo());
        }

        long songCode = Long.parseLong(music.getSongNo());
//        Long newSongCode = ktvApiProtocol.getMusicContentCenter().getInternalSongCode(songCode, jsonOption);

        getRestfulSongList(error -> {
            if (isOwnSong) {
                // Main singer load song
                loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), mainSingerUid, KTVLoadMusicMode.LOAD_MUSIC_AND_LRC,
                        needPrelude), music, true);
            } else {
                // Audience
                loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY,
                        needPrelude), music, false);
            }
        });
    }

    private boolean hasReceiveStartSingBattle = false;

    private AtomicBoolean loadingMusic = new AtomicBoolean(false);

    private void loadMusic(KTVLoadMusicConfiguration config, RoomSelSongModel songInfo, Boolean isOwnSong) {
        loadingMusic.set(true);

        innerLoadMusic(config, songInfo, new SongLoadStateListener() {
            @Override
            public void onMusicLoadSuccess(@NonNull String songCode, @NonNull String musicUri, @NonNull String lyricUrl) {
                loadingMusic.set(false);

                // Current song has been cut
                if (songPlayingLiveData.getValue() == null) return;

                // Reset settings
                retryTimes = 0;
                mSetting.setVolMic(100);
                mSetting.setVolMusic(50);
                ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
                ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);

//                if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals("") && isRoomOwner()) {
//                    SyncStartSing();
//                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
//                } else if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals("") && !isRoomOwner() && hasReceiveStartSingBattle) {
//                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
//                } else if (songPlayingLiveData.getValue() != null && !songPlayingLiveData.getValue().getWinnerNo().equals("")) {
//                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
//                }

                if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals("") && isRoomOwner()) {
                    SyncStartSing();
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                } else if (songPlayingLiveData.getValue() != null && songPlayingLiveData.getValue().getWinnerNo().equals("") && !isRoomOwner()) {
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                } else if (songPlayingLiveData.getValue() != null && !songPlayingLiveData.getValue().getWinnerNo().equals("")) {
                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                }

                ktvApiProtocol.loadMusic(musicUri, config);
                if (isOwnSong) {
                    ktvApiProtocol.switchSingerRole(KTVSingRole.SoloSinger, null);
                    ktvApiProtocol.startSing(musicUri, 0);
                }
            }

            @Override
            public void onMusicLoadFail(@NonNull String songCode, @NonNull SongLoadFailReason reason) {
                loadingMusic.set(false);

                // Current song has been cut
                if (songPlayingLiveData.getValue() == null) {
                    return;
                }

                KTVLogger.e(TAG, "onMusicLoadFail， reason: " + reason);
               /* if (reason == KTVLoadMusicFailReason.NO_LYRIC_URL) {
                    // No lyrics obtained, normal playback
                    retryTimes = 0;
                    mSetting.setVolMic(100);
                    mSetting.setVolMusic(50);
                    ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
                    ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);

                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    noLrcLiveData.postValue(true);
                } else*/
                if (reason == SongLoadFailReason.MUSIC_DOWNLOAD_FAIL) {
                    // Song load failed, retry 3 times
                    retryTimes = retryTimes + 1;
                    if (retryTimes < 3) {
                        loadMusic(config, songInfo, isOwnSong);
                    } else {
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        CustomToast.show(R.string.ktv_singbattle_try,Toast.LENGTH_LONG);
                    }
                }
            }

            @Override
            public void onMusicLoadProgress(@NonNull String songCode, int percent, @NonNull MusicLoadStatus status, @Nullable String lyricUrl) {

            }
        });
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String getMusicFolder() {
        File folder = AgoraApplication.the().getExternalFilesDir("musics");
        return folder != null ? folder.getAbsolutePath() : null;
    }

    public void innerLoadMusic(
            KTVLoadMusicConfiguration config,
            RoomSelSongModel songInfo,
            SongLoadStateListener songLoadStateListener) {
        if (config.getMode() == KTVLoadMusicMode.LOAD_NONE) {
            return;
        }

        KtvSongApiModel song = songList.stream()
                .filter(s -> s.getSongCode().equals(songInfo.getSongNo()))
                .findFirst()
                .orElse(null);
        if (song == null) {
            return;
        }

        if (config.getMode() == KTVLoadMusicMode.LOAD_LRC_ONLY) {
            if (!songInfo.getSongNo().equals(songPlayingLiveData.getValue().getSongNo())) {
                songLoadStateListener.onMusicLoadFail(songInfo.getSongNo(), SongLoadFailReason.CANCELED);
                return;
            }

            if (lrcControlView != null && lrcControlView.get() != null) {
                lrcControlView.get().onDownloadLrcData(song.getLyric());
            }
            songLoadStateListener.onMusicLoadSuccess(songInfo.getSongNo(), "", song.getLyric());
            return;
        }

        String path = getMusicFolder();
        if (path == null) {
            songLoadStateListener.onMusicLoadFail(songInfo.getSongNo(), SongLoadFailReason.UNKNOW);
            return;
        }

        executorService.execute(() -> {
            DownloadManager.getInstance().downloadForJava(song.getMusic(), path, new DownloadManager.FileDownloadCallback() {
                @Override
                public void onProgress(File file, int progress) {
                    mainHandler.post(() -> songLoadStateListener.onMusicLoadProgress(
                            songInfo.getSongNo(),
                            progress,
                            MusicLoadStatus.INPROGRESS,
                            song.getLyric()
                    ));
                }

                @Override
                public void onSuccess(File file) {
                    if (!songInfo.getSongNo().equals(songPlayingLiveData.getValue().getSongNo())) {
                        mainHandler.post(() -> songLoadStateListener.onMusicLoadFail(
                                songInfo.getSongNo(),
                                SongLoadFailReason.CANCELED
                        ));
                        return;
                    }

                    String musicUri = path + File.separator + song.getMusic().substring(song.getMusic().lastIndexOf("/") + 1);

                    mainHandler.post(() -> {
                        if (config.getMode() == KTVLoadMusicMode.LOAD_MUSIC_AND_LRC) {
                            if (lrcControlView != null && lrcControlView.get() != null) {
                                lrcControlView.get().onDownloadLrcData(song.getLyric());
                            }
                            songLoadStateListener.onMusicLoadProgress(
                                    songInfo.getSongNo(),
                                    100,
                                    MusicLoadStatus.INPROGRESS,
                                    song.getLyric()
                            );
                            songLoadStateListener.onMusicLoadSuccess(
                                    songInfo.getSongNo(),
                                    musicUri,
                                    song.getLyric()
                            );
                        } else if (config.getMode() == KTVLoadMusicMode.LOAD_MUSIC_ONLY) {
                            songLoadStateListener.onMusicLoadProgress(
                                    songInfo.getSongNo(),
                                    100,
                                    MusicLoadStatus.INPROGRESS,
                                    song.getLyric()
                            );
                            songLoadStateListener.onMusicLoadSuccess(
                                    songInfo.getSongNo(),
                                    musicUri,
                                    song.getLyric()
                            );
                        }
                    });
                }

                @Override
                public void onFailed(Exception exception) {
                    mainHandler.post(() -> songLoadStateListener.onMusicLoadFail(
                            songInfo.getSongNo(),
                            SongLoadFailReason.MUSIC_DOWNLOAD_FAIL
                    ));
                }
            });
        });
    }

    // ------------------ Re-obtain lyrics url ------------------
    public void reGetLrcUrl() {
        RoomSelSongModel songModel = songPlayingLiveData.getValue();
        if (songModel == null || songModel.getUserNo() == null) return;
        boolean isOwnSong;
        if (songModel.getWinnerNo().equals("")) {
            isOwnSong = Objects.equals(songModel.getUserNo(), UserManager.getInstance().getUser().id.toString());
        } else {
            isOwnSong = Objects.equals(songModel, UserManager.getInstance().getUser().id.toString());
        }
        try {
            int mainSingerUid = Integer.parseInt(Objects.requireNonNull(songPlayingLiveData.getValue().getUserNo()));
            loadMusic(new KTVLoadMusicConfiguration(songPlayingLiveData.getValue().getSongNo(), mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY, needPrelude), songModel, isOwnSong);
        } catch (RuntimeException e) {
            KTVLogger.d(TAG, "RoomLivingViewModel.reGetLrcUrl() error:" + e);
        }
    }

    // ------------------ Song seek ------------------
    public void musicSeek(long time) {
        ktvApiProtocol.seekSing(time);
    }

    public Long getSongDuration() {
        return ktvApiProtocol.getMediaPlayer().getDuration();
    }

    // ------------------ Song end playback ------------------
    public void musicStop() {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStop() called");
        // No song in list, restore status
        resetMusicStatus();
    }

    public void onStart() {
        if (isBackPlay) {
            ktvApiProtocol.getMediaPlayer().mute(false);
        }
    }

    public void onStop() {
        if (isBackPlay) {
            ktvApiProtocol.getMediaPlayer().mute(true);
        }
    }

    // ------------------ Lyrics component related ------------------
    public void syncSingleLineScore(int score, int cumulativeScore, int index, int total) {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "singleLineScore");
        msg.put("score", score);
        msg.put("index", index);
        msg.put("cumulativeScore", cumulativeScore);
        msg.put("total", total);
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "syncSingleLineScore() sendStreamMessage called returned: " + ret);
        }
    }

    public void syncSingingAverageScore(double score) {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "SingingScore");
        msg.put("score", score);
        msg.put("userName", UserManager.getInstance().getUser().name);
        msg.put("userId", UserManager.getInstance().getUser().id.toString());
        msg.put("poster", UserManager.getInstance().getUser().headUrl);
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "syncSingingAverageScore() sendStreamMessage called returned: " + ret);
        }

        // Local singing counts into rank
        if (rankMap.containsKey(UserManager.getInstance().getUser().id.toString())) {
            RankModel oldModel = rankMap.get(UserManager.getInstance().getUser().id.toString());
            RankModel model = new RankModel(
                    oldModel.getUserName(),
                    oldModel.getSongNum() + 1,
                    (int) (oldModel.getScore() * oldModel.getSongNum() + score),
                    UserManager.getInstance().getUser().headUrl
            );
            rankMap.put(UserManager.getInstance().getUser().id.toString(), model);
        } else {
            RankModel model = new RankModel(
                    UserManager.getInstance().getUser().name,
                    1,
                    (int) score,
                    UserManager.getInstance().getUser().headUrl
            );
            rankMap.put(UserManager.getInstance().getUser().id.toString(), model);
        }
    }

    private void SyncStartSing() {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "StartSingBattleCountDown");
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "SyncStartSing() sendStreamMessage called returned: " + ret);
        }
    }

    public List<RankItem> getRankList() {
        List<RankItem> rankItemList = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        rankMap.forEach((uid, model) -> {
            RankItem item = new RankItem();
            item.rank = i.get();
            item.userName = model.getUserName();
            item.songNum = model.getSongNum();
            item.score = model.getScore();
            item.poster = model.getPoster();
            rankItemList.add(item);
            i.getAndIncrement();
        });
        sort(rankItemList);
        return rankItemList;
    }

    public void sort(List<RankItem> list) {
        list.sort((o1, o2) -> {
            if (o1.score != o2.score) {
                return o2.score - o1.score; //score larger in front
            } else {
                return o2.songNum - o1.songNum; //score same songNum larger in front
            }
        });
    }
}

package io.agora.scene.ktv.singrelay.live;

import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION;
import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_SUPERVISE;

import android.os.Build;
import android.text.TextUtils;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import io.agora.musiccontentcenter.Music;
import io.agora.musiccontentcenter.MusicChartInfo;
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
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.singrelay.KTVLogger;
import io.agora.scene.ktv.singrelay.R;
import io.agora.scene.ktv.singrelay.debugSettings.KTVDebugSettingBean;
import io.agora.scene.ktv.singrelay.debugSettings.KTVDebugSettingsDialog;
import io.agora.scene.ktv.singrelay.ktvapi.IKTVApiEventHandler;
import io.agora.scene.ktv.singrelay.ktvapi.ILrcView;
import io.agora.scene.ktv.singrelay.ktvapi.IMusicLoadStateListener;
import io.agora.scene.ktv.singrelay.ktvapi.ISwitchRoleStateListener;
import io.agora.scene.ktv.singrelay.ktvapi.KTVApi;
import io.agora.scene.ktv.singrelay.ktvapi.KTVApiConfig;
import io.agora.scene.ktv.singrelay.ktvapi.KTVApiImpl;
import io.agora.scene.ktv.singrelay.ktvapi.KTVLoadMusicConfiguration;
import io.agora.scene.ktv.singrelay.ktvapi.KTVLoadMusicMode;
import io.agora.scene.ktv.singrelay.ktvapi.KTVLoadSongFailReason;
import io.agora.scene.ktv.singrelay.ktvapi.KTVSingRole;
import io.agora.scene.ktv.singrelay.ktvapi.KTVType;
import io.agora.scene.ktv.singrelay.ktvapi.MusicLoadStatus;
import io.agora.scene.ktv.singrelay.ktvapi.SwitchRoleFailReason;
import io.agora.scene.ktv.singrelay.live.song.SongModel;
import io.agora.scene.ktv.singrelay.service.ChangeMVCoverInputModel;
import io.agora.scene.ktv.singrelay.service.ChooseSongInputModel;
import io.agora.scene.ktv.singrelay.service.JoinRoomOutputModel;
import io.agora.scene.ktv.singrelay.service.KTVServiceProtocol;
import io.agora.scene.ktv.singrelay.service.KTVSingRelayGameService;
import io.agora.scene.ktv.singrelay.service.MakeSongTopInputModel;
import io.agora.scene.ktv.singrelay.service.OnSeatInputModel;
import io.agora.scene.ktv.singrelay.service.OutSeatInputModel;
import io.agora.scene.ktv.singrelay.service.RankModel;
import io.agora.scene.ktv.singrelay.service.RemoveSongInputModel;
import io.agora.scene.ktv.singrelay.service.RoomSeatModel;
import io.agora.scene.ktv.singrelay.service.RoomSelSongModel;
import io.agora.scene.ktv.singrelay.service.ScoringAlgoControlModel;
import io.agora.scene.ktv.singrelay.service.ScoringAverageModel;
import io.agora.scene.ktv.singrelay.service.SingRelayGameStatus;
import io.agora.scene.ktv.singrelay.widget.MusicSettingBean;
import io.agora.scene.ktv.singrelay.widget.MusicSettingDialog;
import io.agora.scene.ktv.singrelay.widget.rankList.RankItem;

public class RoomLivingViewModel extends ViewModel {

    private final String TAG = "KTV_Scene_LOG";
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();
    private final KTVApi ktvApiProtocol = new KTVApiImpl();

    // loading dialog
    private final MutableLiveData<Boolean> _loadingDialogVisible = new MutableLiveData<>(false);
    final LiveData<Boolean> loadingDialogVisible = _loadingDialogVisible;

    /**
     * 房间信息
     */
    final MutableLiveData<JoinRoomOutputModel> roomInfoLiveData;
    final MutableLiveData<Boolean> roomDeleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Boolean> roomTimeUpLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> roomUserCountLiveData = new MutableLiveData<>(0);

    /**
     * 麦位信息
     */
    boolean isOnSeat = false;
    final MutableLiveData<List<RoomSeatModel>> seatListLiveData = new MutableLiveData<>(new ArrayList<>());
    final MutableLiveData<RoomSeatModel> seatLocalLiveData = new MutableLiveData<>();

    /**
     * 歌曲信息
     */
    final MutableLiveData<List<RoomSelSongModel>> songsOrderedLiveData = new MutableLiveData<>();
    final MutableLiveData<RoomSelSongModel> songPlayingLiveData = new MutableLiveData<>();
    private List<Long> relayList = new ArrayList<>();
    private int partNum = 0;

    class LineScore {
        int score;
        int index;
        int cumulativeScore;
        int total;
    }

    final MutableLiveData<LineScore> mainSingerScoreLiveData = new MutableLiveData<>();

    /**
     * Player/RTC信息
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
    final MutableLiveData<GameStatus> singRelayGameStatusMutableLiveData = new MutableLiveData<>();

    enum GraspStatus {
        IDLE,
        SUCCESS,
        Mention,
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
    final MutableLiveData<Boolean> playerMusicPlayCompleteLiveData = new MutableLiveData<>();
    final MutableLiveData<NetWorkEvent> networkStatusLiveData = new MutableLiveData<>();

    final MutableLiveData<ScoringAlgoControlModel> scoringAlgoControlLiveData = new MutableLiveData<>();

    private final Map<String, RankModel> rankMap = new HashMap<>();

    /**
     * Rtc引擎
     */
    private RtcEngineEx mRtcEngine;

    /**
     * 主版本的音频设置
     */
    private final ChannelMediaOptions mainChannelMediaOption = new ChannelMediaOptions();

    /**
     * 播放器配置
     */
    MusicSettingBean mSetting;

    /**
     * 是否开启后台播放
     */
    KTVDebugSettingBean mDebugSetting;

    /**
     * 是否开启后台播放
     */
    private boolean isBackPlay = false;

    /**
     * 是否开启耳返
     */
    private boolean isOpnEar = false;

    public RoomLivingViewModel(JoinRoomOutputModel roomInfo) {
        this.roomInfoLiveData = new MutableLiveData<>(roomInfo);
    }

    public boolean isRoomOwner() {
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().id.toString());
    }

    public boolean isOnSeat() {
        return seatLocalLiveData.getValue() != null;
    }

    public void init() {
        if (isRoomOwner()) {
            ktvApiProtocol.setMicStatus(true);
            isOnSeat = true;
        }
        initRTCPlayer();
        initRoom();
        initSeats();
        initSongs();
        initSingRelayGame();
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

    // ======================= 断网重连相关 =======================

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

    // ======================= 房间相关 =======================

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
                // 当房间内状态发生改变时触发
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated");
                if (!vlRoomListModel.getBgOption().equals(_roomInfo.getBgOption())) {
                    roomInfoLiveData.postValue(new JoinRoomOutputModel(
                            _roomInfo.getRoomName(),
                            _roomInfo.getRoomNo(),
                            _roomInfo.getCreatorNo(),
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
     * 退出房间
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
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 设置背景
     */
    public void setMV_BG(int bgPosition) {
        KTVLogger.d(TAG, "RoomLivingViewModel.setMV_BG() called: " + bgPosition);
        ktvServiceProtocol.changeMVCover(new ChangeMVCoverInputModel(bgPosition), e -> {
            if (e == null) {
                // success
                // do nothing for the subscriber will callback the new room info.
                KTVLogger.d(TAG, "RoomLivingViewModel.setMV_BG() success");
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.setMV_BG() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    // ======================= 麦位相关 =======================

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
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeCreated");
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
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated");
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
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted");
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
     * 上麦
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
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 离开麦位
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
                        ToastUtils.showToast(e.getMessage());
                    }
                    return null;
                });
    }

    /**
     * 开关摄像头
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
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 静音
     */
    public void toggleMic(boolean isUnMute) {
        KTVLogger.d(TAG, "RoomLivingViewModel.toggleMic() called：" + isUnMute);
        ktvServiceProtocol.updateSeatAudioMuteStatus(!isUnMute, e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.toggleMic() success");
                updateVolumeStatus(isUnMute);
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.toggleMic() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    private void updateVolumeStatus(boolean isUnMute) {
        ktvApiProtocol.setMicStatus(isUnMute);
        if (!isUnMute) {
            if (mSetting.isEar()) {
                isOpnEar = true;
                mSetting.setEar(false);
            } else {
                isOpnEar = false;
            }
        } else {
            mSetting.setEar(isOpnEar);
        }

        // 静音时将本地采集音量改为0
        if (!isUnMute && mRtcEngine != null) mRtcEngine.adjustRecordingSignalVolume(0);
        setMicVolume(micOldVolume);
    }


    // ======================= 歌曲相关 =======================

    public void initSongs() {
        ktvServiceProtocol.subscribeChooseSong((ktvSubscribe, songModel) -> {
            // 歌曲信息发生变化时，重新获取歌曲列表动作
            KTVLogger.d(TAG, "subscribeChooseSong updateSongs");
            onSongChanged();
            return null;
        });

        // 获取初始歌曲列表
        onSongChanged();
    }

    public int songNum = 0;
    public void onSongChanged() {
        ktvServiceProtocol.getChoosedSongsList((e, data) -> {
            if (e == null && data != null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() success" + data);
                songNum = data.size();
                songsOrderedLiveData.postValue(data);

                if (singRelayGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
                    if (data.size() > 0){
                        RoomSelSongModel value = songPlayingLiveData.getValue();
                        RoomSelSongModel songPlaying = data.get(0);

                        if (value != null && value.getWinnerNo() != null && value.getWinnerNo().equals("") && !songPlaying.getWinnerNo().equals("")) {
                            // 所有人更新抢唱结果UI
                            hasReceiveStartSingRelay = false;
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
                    ToastUtils.showToast(e.getMessage());
                }
            }
            return null;
        });
    }

    public void onSongPlaying() {
        KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying()");
        if (singRelayGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
            if (songsOrderedLiveData.getValue() != null && songsOrderedLiveData.getValue().size() > 0){
                RoomSelSongModel value = songPlayingLiveData.getValue();
                RoomSelSongModel songPlaying = songsOrderedLiveData.getValue().get(0);

                if (value == null || !value.getSongNo().equals(songPlaying.getSongNo())) {
                    // 无已点歌曲， 直接将列表第一个设置为当前播放歌曲
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying() chosen song list is empty");
                    songPlayingLiveData.postValue(songPlaying);
                    relayList = SongModel.INSTANCE.getSongPartListWithSongCode(songPlaying.getSongNo());
                } else if (!value.getWinnerNo().equals(songPlaying.getWinnerNo())) {
                    KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying() has winner");
                    songPlayingLiveData.postValue(songPlaying);
                    relayList = SongModel.INSTANCE.getSongPartListWithSongCode(songPlaying.getSongNo());
                }
            } else {
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList");
                //gameSong = null;
                songPlayingLiveData.postValue(null);
            }
        }
    }

    /**
     * 开始切歌
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
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END);
            }
            return null;
        });
    }

    /**
     * 设置歌词view
     */
    public void setLrcView(ILrcView view) {
        ktvApiProtocol.setLrcView(view);
    }

    // ======================= Player/RTC/MPK相关 =======================
    private void initRTCPlayer() {
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw new NullPointerException("please check \"strings_config.xml\"");
        }
        if (mRtcEngine != null) return;

        // ------------------ 初始化RTC ------------------
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
                // 网络状态回调, 本地user uid = 0
                if (uid == 0) {
                    networkStatusLiveData.postValue(new NetWorkEvent(txQuality, rxQuality));
                }
            }

            @Override
            public void onContentInspectResult(int result) {
                super.onContentInspectResult(result);
                if (result > 1) {
                    ToastUtils.showToast(R.string.ktv_content);
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
                        KTVLogger.d(TAG, "onMessage/SingingScore: " + jsonMsg);
                        float score = (float) jsonMsg.getDouble("score");
                        int songNum = (int) jsonMsg.getDouble("num");
                        String userId = jsonMsg.getString("userId");
                        String userName = jsonMsg.getString("userName");
                        String poster = jsonMsg.getString("poster");

                        // 本地演唱 计入rank
                        RankModel model = new RankModel(
                                userName,
                                songNum,
                                (int)score,
                                poster
                        );
                        rankMap.put(userId, model);
                    } else if (jsonMsg.getString("cmd").equals("StartSingRelayCountDown")) {
                        KTVLogger.d(TAG, "StartSingRelayCountDown");
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_BATTLE);
                        hasReceiveStartSingRelay = true;
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


        // ------------------ 场景化api初始化 ------------------
        ktvApiProtocol.initialize(new KTVApiConfig(
                BuildConfig.AGORA_APP_ID,
                roomInfoLiveData.getValue().getAgoraRTMToken(),
                mRtcEngine,
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id.intValue(),
                roomInfoLiveData.getValue().getRoomNo() + "_ex",
                roomInfoLiveData.getValue().getAgoraChorusToken(), 10, KTVType.SingRelay)
        );

        ktvApiProtocol.addEventHandler(new IKTVApiEventHandler() {
               @Override
               public void onMusicPlayerStateChanged(@NonNull io.agora.mediaplayer.Constants.MediaPlayerState state, io.agora.mediaplayer.Constants.MediaPlayerError error,  boolean isLocal) {
                   switch (state) {
                       case PLAYER_STATE_OPEN_COMPLETED:
                           playerMusicOpenDurationLiveData.postValue(ktvApiProtocol.getMediaPlayer().getDuration());
                           break;
                       case PLAYER_STATE_PLAYING:
                           playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                           break;
                       case PLAYER_STATE_PAUSED:
                           playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
                           break;
                       case PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED:
                           if (isLocal) {
                               playerMusicPlayCompleteLiveData.postValue(true);
                               playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
                           }
                           break;
                       default:
                   }
               }

               @Override
               public void onMusicPlayerPositionChanged(long position_ms, long timestamp_ms) {
                   super.onMusicPlayerPositionChanged(position_ms, timestamp_ms);
                   onNextPart(position_ms);
                   onNextPartMention(position_ms);
               }
           }
        );

        ktvApiProtocol.renewInnerDataStreamId();

        // ------------------ 加入频道 ------------------
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
        if (ret != Constants.ERR_OK) {
            KTVLogger.e(TAG, "joinRTC() called error: " + ret);
        }

        // ------------------ 开启鉴黄服务 ------------------
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
            contentInspectConfig.modules = new ContentInspectConfig.ContentInspectModule[] { module1, module2 };
            contentInspectConfig.moduleCount = 2;
            mRtcEngine.enableContentInspect(true, contentInspectConfig);
        } catch (JSONException e) {
            KTVLogger.e(TAG, e.toString());
        }

        // ------------------ 初始化音乐播放设置面版 ------------------
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
                KTVApiImpl ktvApiImpl = (KTVApiImpl) ktvApiProtocol;
                ktvApiImpl.setRemoteVolume(volume);
                mRtcEngine.adjustPlaybackSignalVolume(volume);
            }
        });

        // 外部使用的StreamId
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

    // ======================= 抢唱逻辑 =======================
    private void initSingRelayGame() {
        ktvServiceProtocol.subscribeSingRelayGame((ktvSubscribe, gameModel) -> {
            KTVLogger.d(TAG, "subscribeSingRelayGame: " + ktvSubscribe + " " + gameModel);
            if (gameModel.getStatus() == SingRelayGameStatus.waitting.getValue()) {
                singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_WAITING);
            } else if (gameModel.getStatus() == SingRelayGameStatus.started.getValue()) {
                singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_START);
                rankMap.clear();
            } else if (gameModel.getStatus() == SingRelayGameStatus.ended.getValue()) {
                singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_END);
            }
            return null;
        });
        ktvServiceProtocol.getSingRelayGameInfo((e, info) -> {
            if (e == null && info != null) {
                if (info.getStatus() == SingRelayGameStatus.waitting.getValue()) {
                    singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_WAITING);
                } else if (info.getStatus() == SingRelayGameStatus.started.getValue()) {
                    singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_ERROR);
                } else if (info.getStatus() == SingRelayGameStatus.ended.getValue()) {
                    singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_END);
                    KTVLogger.d(TAG, "rank: " + info.getRank());
                    if (info.getRank() != null) {
                        rankMap.putAll(info.getRank());
                    }
                }
            } else {
                ktvServiceProtocol.prepareSingRelayGame(error -> null);
            }
            return null;
        });
    }

    public void startSingRelayGame() {
        KTVLogger.d(TAG, "startSingRelayGame called");
        ktvServiceProtocol.startSingRelayGame(e -> {
            KTVLogger.d(TAG, "startSingRelayGame success");
            return null;
        });
    }

    public void prepareSingRelayGame() {
        KTVLogger.d(TAG, "startPrepareSingRelayGame called");
        ktvServiceProtocol.prepareSingRelayGame(e -> {
            KTVLogger.d(TAG, "startPrepareSingRelayGame success");
            return null;
        });
    }

    public void finishSingRelayGame() {
        KTVLogger.d(TAG, "finishSingRelayGame called");
        ktvServiceProtocol.finishSingRelayGame(rankMap, e -> {
            KTVLogger.d(TAG, "finishSingRelayGame success");
            return null;
        });
    }

    // ------------------ 开始抢唱 ------------------
    public void graspSong() {
        KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() called");
        KTVSingRelayGameService.INSTANCE.graspSong(
                "scene_singrelay_3.5.0",
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id.toString(),
                UserManager.getInstance().getUser().name,
                songPlayingLiveData.getValue().getSongNo() + "_" + (partNum + 1),
                UserManager.getInstance().getUser().headUrl,
                (userId) -> {
                    KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() success " + userId);
                    // 更新Service抢唱结果
                    ktvServiceProtocol.updateSongModel(songPlayingLiveData.getValue().getSongNo(), userId + "_" + (partNum + 1), UserManager.getInstance().getUser().name, UserManager.getInstance().getUser().headUrl, e -> {
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
        KTVSingRelayGameService.INSTANCE.getWinnerInfo(
                "scene_singrelay_3.4.0",
                roomInfoLiveData.getValue().getRoomNo(),
                songPlayingLiveData.getValue().getSongNo(),
                (userId, userName) -> {
                    KTVLogger.d(TAG, "RoomLivingViewModel.getWinnerInfo() called：" + userId + " success");
                    return null;
                },
                e -> {
                    if (e.getMessage().equals("961")) {
                        KTVLogger.d(TAG, "RoomLivingViewModel.getWinnerInfo() nobody grasp");
                        hasReceiveStartSingRelay = false;
                        GraspModel model = new GraspModel();
                        model.status = GraspStatus.EMPTY;
                        graspStatusMutableLiveData.postValue(model);
                    }
                    return null;
                }
        );
    }

    // ======================= settings =======================
    // ------------------ 音效调整 ------------------
    private int getEffectIndex(int index) {
        switch (index) {
            // 原声
            case 0:
                return Constants.AUDIO_EFFECT_OFF;
            // KTV
            case 1:
                return Constants.ROOM_ACOUSTICS_KTV;
            // 演唱会
            case 2:
                return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT;
            // 录音棚
            case 3:
                return Constants.ROOM_ACOUSTICS_STUDIO;
            // 留声机
            case 4:
                return Constants.ROOM_ACOUSTICS_PHONOGRAPH;
            // 空旷
            case 5:
                return Constants.ROOM_ACOUSTICS_SPACIAL;
            // 空灵
            case 6:
                return Constants.ROOM_ACOUSTICS_ETHEREAL;
            // 流行
            case 7:
                return Constants.STYLE_TRANSFORMATION_POPULAR;
            // R&B
            case 8:
                return Constants.STYLE_TRANSFORMATION_RNB;
        }
        // 原声
        return Constants.AUDIO_EFFECT_OFF;
    }

    // ------------------ 音量调整 ------------------
    private int micVolume = 100;
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
        micVolume = v;
        if (mRtcEngine != null) {
            mRtcEngine.adjustRecordingSignalVolume(v);
        }
    }

    // ------------------ 原唱/伴奏 ------------------
    private enum KTVPlayerTrackMode {
        Origin,
        Acc
    }
    protected KTVPlayerTrackMode mAudioTrackMode = KTVPlayerTrackMode.Acc;
    public void musicToggleOriginal() {
        if (mAudioTrackMode == KTVPlayerTrackMode.Origin) {
            ktvApiProtocol.getMediaPlayer().selectAudioTrack(1);
            mAudioTrackMode = KTVPlayerTrackMode.Acc;
        } else {
            ktvApiProtocol.getMediaPlayer().selectAudioTrack(0);
            mAudioTrackMode = KTVPlayerTrackMode.Origin;
        }
    }

    public boolean isOriginalMode() {
        return mAudioTrackMode == KTVPlayerTrackMode.Origin;
    }

    // ------------------ 暂停/播放 ------------------
    public void musicToggleStart() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            ktvApiProtocol.pauseSing();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            ktvApiProtocol.resumeSing();
        }
    }

    // ------------------ 本地视频渲染 ------------------
    public void renderLocalCameraVideo(SurfaceView surfaceView) {
        if (mRtcEngine == null) return;
        mRtcEngine.startPreview();
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, 0));
    }

    // ------------------ 远端视频渲染 ------------------
    public void renderRemoteCameraVideo(SurfaceView surfaceView, int uid) {
        if (mRtcEngine == null) return;
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, Constants.RENDER_MODE_HIDDEN, uid));
    }

    // ------------------ 重置歌曲状态(歌曲切换时) ------------------
    public void resetMusicStatus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.resetMusicStatus() called");
        retryTimes = 0;
        mAudioTrackMode = KTVPlayerTrackMode.Acc;
        ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null);
    }

    // ------------------ 歌曲开始播放 ------------------
    private int retryTimes = 0;
    public void musicStartPlay(@NonNull RoomSelSongModel music) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called");
        if (music.getUserNo() == null) return;
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);

        long songCode = Long.parseLong(music.getSongNo());
        int mainSingerUid = Integer.parseInt(music.getUserNo());
        if (isOnSeat()) {
            // 麦上玩家加载音乐
            loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), false, mainSingerUid, KTVLoadMusicMode.LOAD_MUSIC_AND_LRC), songCode);
        } else {
            // 观众
            loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), false, mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY), songCode);
        }
    }

    private boolean hasReceiveStartSingRelay = false;
    private void loadMusic(KTVLoadMusicConfiguration config, Long songCode) {

        ktvApiProtocol.loadMusic(songCode, config, new IMusicLoadStateListener() {
            @Override
            public void onMusicLoadProgress(long songCode, int percent, @NonNull MusicLoadStatus status, @Nullable String msg, @Nullable String lyricUrl) {
                KTVLogger.d(TAG, "onMusicLoadProgress, songCode: " + songCode + " percent: " + percent + " lyricUrl: " + lyricUrl);
            }

            @Override
            public void onMusicLoadSuccess(long songCode, @NonNull String lyricUrl) {
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    ToastUtils.showToastLong("load失败，当前已无歌曲");
                    return;
                }

                // 重置settings
                retryTimes = 0;
                mSetting.setVolMic(100);
                mSetting.setVolMusic(50);
                ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
                ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);

                if (isRoomOwner()) {
                    ktvApiProtocol.switchSingerRole(KTVSingRole.LeadSinger, new ISwitchRoleStateListener() {
                        @Override
                        public void onSwitchRoleSuccess() {
                            ktvApiProtocol.startSing(songCode, 0);
                        }

                        @Override
                        public void onSwitchRoleFail(@NonNull SwitchRoleFailReason reason) {

                        }
                    });
                } else if (isOnSeat()) {
                    ktvApiProtocol.switchSingerRole(KTVSingRole.CoSinger, null);
                }

                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
            }

            @Override
            public void onMusicLoadFail(long songCode, @NonNull KTVLoadSongFailReason reason) {
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    ToastUtils.showToastLong("load失败，当前已无歌曲");
                    return;
                }

                KTVLogger.e(TAG, "onMusicLoadFail， reason: " + reason);
                if (reason == KTVLoadSongFailReason.NO_LYRIC_URL) {
                    // 未获取到歌词 正常播放
                    retryTimes = 0;
                    mSetting.setVolMic(100);
                    mSetting.setVolMusic(50);
                    ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
                    ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);

                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                    noLrcLiveData.postValue(true);
                } else if (reason == KTVLoadSongFailReason.MUSIC_PRELOAD_FAIL) {
                    // 歌曲加载失败 ，重试3次
                    ToastUtils.showToastLong("歌曲加载失败");
                    retryTimes = retryTimes + 1;
                    if (retryTimes < 3) {
                        loadMusic(config, songCode);
                    } else {
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        ToastUtils.showToastLong("已尝试三次，请自动切歌");
                    }
                } else if (reason == KTVLoadSongFailReason.CANCELED) {
                    // 当前已被切歌
                    ToastUtils.showToastLong("load失败，当前已切换到另一首歌");
                }
            }
        });
    }

    // ------------------ 重新获取歌词url ------------------
    public void reGetLrcUrl() {
        if (songPlayingLiveData.getValue() == null) return;
        loadMusic(new KTVLoadMusicConfiguration(songPlayingLiveData.getValue().getSongNo(), true, Integer.parseInt(songPlayingLiveData.getValue().getUserNo()), KTVLoadMusicMode.LOAD_LRC_ONLY), Long.parseLong(songPlayingLiveData.getValue().getSongNo()));
    }

    // ------------------ 歌曲seek ------------------
    public void musicSeek(long time) {
        ktvApiProtocol.seekSing(time);
    }

    public Long getSongDuration() {
        return ktvApiProtocol.getMediaPlayer().getDuration();
    }

    // ------------------ 歌曲结束播放 ------------------
    public void musicStop() {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStop() called");
        // 列表中无歌曲， 还原状态
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

    // ------------------ 歌词组件相关 ------------------
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

    public void syncSingingAverageScore(double score, int singedNum) {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "SingingScore");
        msg.put("score", score);
        msg.put("num", singedNum);
        msg.put("userName", UserManager.getInstance().getUser().name);
        msg.put("userId", UserManager.getInstance().getUser().id.toString());
        msg.put("poster", UserManager.getInstance().getUser().headUrl);
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "syncSingingAverageScore() sendStreamMessage called returned: " + ret);
        }

        // 本地演唱 计入rank
        RankModel model = new RankModel(
                UserManager.getInstance().getUser().name,
                singedNum,
                (int)score,
                UserManager.getInstance().getUser().headUrl
        );
        rankMap.put(UserManager.getInstance().getUser().id.toString(), model);
    }

    private void SyncStartSing() {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "StartSingRelayCountDown");
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "SyncStartSing() sendStreamMessage called returned: " + ret);
        }
    }

    public List<RankItem> getRankList() {
        List<RankItem> rankItemList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
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
        }
        sort(rankItemList);
        return rankItemList;
    }

    public void sort(List<RankItem> list) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.sort((o1, o2) -> {
                if (o1.score != o2.score) {
                    return o2.score - o1.score; //score大的在前面
                } else {
                    return o2.songNum - o1.songNum; //score相同 songNum大的在前面
                }
            });
        }
    }

    public void autoSelectMusic() {
        List<ChooseSongInputModel> musicList = new ArrayList<>();
        musicList.add(SongModel.INSTANCE.getRandomGameSong());
        ktvServiceProtocol.autoChooseSongAndStartGame(musicList, e -> null);
    }

    private void onNextPart(long position_ms) {
        if ((position_ms - relayList.get(1)) > 0 && (position_ms - relayList.get(1) < 1000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.IDLE;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 1;
        } else if ((position_ms - relayList.get(2)) > 0 && (position_ms - relayList.get(2) < 1000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.IDLE;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 2;
        } else if ((position_ms - relayList.get(3)) > 0 && (position_ms - relayList.get(3) < 1000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.IDLE;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 3;
        } else if ((position_ms - relayList.get(4)) > 0 && (position_ms - relayList.get(4) < 1000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.IDLE;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 4;
        }
    }

    private void onNextPartMention(long position_ms) {
        if ((position_ms - relayList.get(1)) > -4000 && (position_ms - relayList.get(1) < -3000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.Mention;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 1;
        } else if ((position_ms - relayList.get(2)) > -4000 && (position_ms - relayList.get(2) < -3000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.Mention;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 2;
        } else if ((position_ms - relayList.get(3)) > -4000 && (position_ms - relayList.get(3) < -3000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.Mention;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 3;
        } else if ((position_ms - relayList.get(4)) > -4000 && (position_ms - relayList.get(4) < -3000)) {
            GraspModel graspModel = new GraspModel();
            graspModel.status = GraspStatus.Mention;
            graspStatusMutableLiveData.postValue(graspModel);
            partNum = 4;
        }
    }

    public boolean isNextRoundSinger() {
        if (songsOrderedLiveData.getValue().get(0).getWinnerNo().split("_")[0].equals(UserManager.getInstance().getUser().id.toString())) {
            return true;
        } else {
            if (isRoomOwner() && (songsOrderedLiveData.getValue().get(0).getWinnerNo().equals("") || !songsOrderedLiveData.getValue().get(0).getWinnerNo().split("_")[1].equals(String.valueOf(partNum)))) {
                return true;
            } else {
                return false;
            }
        }
    }
}

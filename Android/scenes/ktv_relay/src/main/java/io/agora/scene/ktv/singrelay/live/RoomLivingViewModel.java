package io.agora.scene.ktv.singrelay.live;

import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION;
import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_SUPERVISE;
import static io.agora.ktvapi.KTVApiKt.createKTVApi;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.base.utils.resourceManager.DownloadManager;
import io.agora.scene.ktv.singrelay.KTVLogger;
import io.agora.scene.ktv.singrelay.R;
import io.agora.scene.ktv.singrelay.debugSettings.KTVDebugSettingBean;
import io.agora.scene.ktv.singrelay.debugSettings.KTVDebugSettingsDialog;
import io.agora.ktvapi.AudioTrackMode;
import io.agora.ktvapi.IKTVApiEventHandler;
import io.agora.ktvapi.ILrcView;
import io.agora.ktvapi.ISwitchRoleStateListener;
import io.agora.ktvapi.KTVApi;
import io.agora.ktvapi.KTVApiConfig;
import io.agora.ktvapi.KTVLoadMusicConfiguration;
import io.agora.ktvapi.KTVLoadMusicMode;
import io.agora.ktvapi.KTVMusicType;
import io.agora.ktvapi.KTVSingRole;
import io.agora.ktvapi.KTVType;
import io.agora.ktvapi.MusicLoadStatus;
import io.agora.ktvapi.SwitchRoleFailReason;
import io.agora.scene.ktv.singrelay.live.listener.SongLoadFailReason;
import io.agora.scene.ktv.singrelay.live.listener.SongLoadStateListener;
import io.agora.scene.ktv.singrelay.live.song.SongModel;
import io.agora.scene.ktv.singrelay.service.JoinRoomOutputModel;
import io.agora.scene.ktv.singrelay.service.KTVServiceProtocol;
import io.agora.scene.ktv.singrelay.service.KTVSingRelayGameService;
import io.agora.scene.ktv.singrelay.service.KTVSyncManagerServiceImp;
import io.agora.scene.ktv.singrelay.service.OnSeatInputModel;
import io.agora.scene.ktv.singrelay.service.OutSeatInputModel;
import io.agora.scene.ktv.singrelay.service.RankModel;
import io.agora.scene.ktv.singrelay.service.RemoveSongInputModel;
import io.agora.scene.ktv.singrelay.service.RoomSeatModel;
import io.agora.scene.ktv.singrelay.service.RoomSelSongModel;
import io.agora.scene.ktv.singrelay.service.ScoringAlgoControlModel;
import io.agora.scene.ktv.singrelay.service.SingRelayGameStatus;
import io.agora.scene.ktv.singrelay.service.api.KtvApiManager;
import io.agora.scene.ktv.singrelay.service.api.KtvSongApiModel;
import io.agora.scene.ktv.singrelay.widget.MusicSettingBean;
import io.agora.scene.ktv.singrelay.widget.MusicSettingDialog;
import io.agora.scene.ktv.singrelay.widget.rankList.RankItem;
import io.agora.scene.widget.toast.CustomToast;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class RoomLivingViewModel extends ViewModel {

    private final String TAG = "KTV_Scene_LOG";
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();
    private KTVApi ktvApiProtocol;

    private KtvApiManager ktvApiManager = new KtvApiManager();

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
    private int partNum = 1;

    static class LineScore {
        int score;
        int index;
        int cumulativeScore;
        int total;
        String userName;
        String poster;
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

    final MutableLiveData<GameStatus> singRelayGameStatusMutableLiveData = new MutableLiveData<>(GameStatus.ON_WAITING);

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
        int partNum;
    }

    final MutableLiveData<GraspModel> graspStatusMutableLiveData = new MutableLiveData<>();
    final MutableLiveData<Long> playerMusicOpenDurationLiveData = new MutableLiveData<>();
    final MutableLiveData<Boolean> playerMusicPlayCompleteLiveData = new MutableLiveData<>();
    final MutableLiveData<NetWorkEvent> networkStatusLiveData = new MutableLiveData<>();
    final MutableLiveData<ScoringAlgoControlModel> scoringAlgoControlLiveData = new MutableLiveData<>();

    private final Map<String, RankModel> rankMap = new HashMap<>();
    private final Map<String, Integer> singerMap = new HashMap<>();

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

    private int prepareNum = 0;
    private boolean hasRecievedFirstPosition = false;
    private Long mLastPostSongPartChangeStatusTime = 0L;

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
            onMicSeatChange();
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated) {
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeCreated");
                List<RoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<RoomSeatModel> value = new ArrayList<>(oValue);
                value.add(roomSeatModel);
                //seatListLiveData.postValue(value);

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
                    //seatListLiveData.setValue(value);

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
                //seatListLiveData.setValue(value);

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    seatLocalLiveData.setValue(null);
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

    private void onMicSeatChange() {
        ktvServiceProtocol.getSeatStatusList((e, list) -> {
            if (e == null) {
                seatListLiveData.setValue(list);
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
        updateVolumeStatus(isUnMute);
        ktvServiceProtocol.updateSeatAudioMuteStatus(!isUnMute, e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.toggleMic() success");
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.toggleMic() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    private void updateVolumeStatus(boolean isUnMute) {
        ktvApiProtocol.muteMic(!isUnMute);

        // 调整耳返
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
                songsOrderedLiveData.setValue(data);

                if (singRelayGameStatusMutableLiveData.getValue() == GameStatus.ON_WAITING && data.size() > 0 && isRoomOwner()) {
                    // 歌曲选择成功后，开始游戏
                    KTVLogger.d(TAG, "RoomLivingViewModel.startSingRelayGame");
                    ktvServiceProtocol.startSingRelayGame(err -> {
                        if (err != null) {
                            KTVLogger.e(TAG, "RoomLivingViewModel.startSingRelayGame() failed: " + err.getMessage());
                            ToastUtils.showToast(err.getMessage());
                        }
                        return null;
                    });
                } else if (singRelayGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
                    if (data.size() > 0) {
                        RoomSelSongModel value = songPlayingLiveData.getValue();
                        RoomSelSongModel songPlaying = data.get(0);

                        // TODO ios端 winnerNo 有概率为 null
                        if (value != null && value.getWinnerNo() != null && !songPlaying.getWinnerNo().equals("") && seatLocalLiveData.getValue() != null) {
                            // 所有人更新抢唱结果UI
                            String userId = songPlaying.getWinnerNo().split("_")[0];
                            int num = Integer.parseInt(songPlaying.getWinnerNo().split("_")[1]);

                            if (graspStatusMutableLiveData.getValue() == null || (graspStatusMutableLiveData.getValue() != null && graspStatusMutableLiveData.getValue().partNum != num)) {
                                KTVLogger.d("pigpig", "44444444");
                                if (isRoomOwner() && !userId.equals(UserManager.getInstance().getUser().id.toString())) {
                                    if (singerMap.containsKey(userId)) {
                                        int songNum = singerMap.get(userId);
                                        singerMap.put(userId, songNum + 1);
                                    } else {
                                        singerMap.put(userId, 1);
                                    }
                                }
                            }

                            GraspModel model = new GraspModel();
                            model.status = GraspStatus.SUCCESS;
                            model.userId = songPlaying.getWinnerNo();
                            model.userName = songPlaying.getName();
                            model.headUrl = songPlaying.getImageUrl();
                            model.partNum = num;
                            graspStatusMutableLiveData.setValue(model);
                        }

                        if (isGaming && value == null) {
                            RoomSelSongModel song = data.get(0);
                            songPlayingLiveData.setValue(song);
                            relayList = SongModel.INSTANCE.getSongPartListWithSongCode(song.getSongNo());
                        }
                    } else {
                        KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList");
                        songPlayingLiveData.setValue(null);
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

    /**
     * 开始播放歌曲
     */
    boolean isGaming = false;

    public void onSongPlaying() {
        KTVLogger.d(TAG, "RoomLivingViewModel.onSongPlaying()");
        isGaming = true;
        if (singRelayGameStatusMutableLiveData.getValue() == GameStatus.ON_START) {
            if (songsOrderedLiveData.getValue() != null && songsOrderedLiveData.getValue().size() > 0) {
                RoomSelSongModel songPlaying = songsOrderedLiveData.getValue().get(0);

                songPlayingLiveData.postValue(songPlaying);
                relayList = SongModel.INSTANCE.getSongPartListWithSongCode(songPlaying.getSongNo());
            } else {
                KTVLogger.d(TAG, "RoomLivingViewModel.onSongChanged() return is emptyList");
                songPlayingLiveData.postValue(null);
            }
        }
    }

    /**
     * 自动点歌
     */
    public void autoSelectMusic() {
        ktvServiceProtocol.chooseSong(SongModel.INSTANCE.getRandomGameSong(), e -> null);
    }

    /**
     * 开始切歌
     */
    public void changeMusic() {
        KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() called");
        RoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            KTVLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed, no song is playing now!");
            return;
        }

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

    private WeakReference<ILrcView> lrcControlView = null;

    /**
     * 设置歌词view
     */
    public void setLrcView(ILrcView view) {
        lrcControlView = new WeakReference<>(view);
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
                    ToastUtils.showToast(R.string.ktv_relay_content);
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

                        // 本地演唱 计入rank
                        String senderUid = String.valueOf(uid);
                        if (rankMap.containsKey(senderUid)) {
                            RankModel oldModel = rankMap.get(senderUid);
                            if (oldModel != null) {
                                RankModel model = new RankModel(
                                        oldModel.getUserName(),
                                        0,
                                        oldModel.getScore() + score,
                                        oldModel.getPoster(),
                                        oldModel.getLines() + 1
                                );
                                rankMap.remove(senderUid);
                                rankMap.put(senderUid, model);
                            }
                        } else {
                            if (seatListLiveData.getValue() == null) return;
                            for (int i = 0; i < seatListLiveData.getValue().size(); i++) {
                                if (seatListLiveData.getValue().get(i).getRtcUid().equals(senderUid)) {
                                    rankMap.put(senderUid, new RankModel(
                                            seatListLiveData.getValue().get(i).getName(),
                                            0,
                                            score,
                                            seatListLiveData.getValue().get(i).getHeadUrl(),
                                            1
                                    ));
                                    break;
                                }
                            }
                        }
                    } else if (jsonMsg.getString("cmd").equals("CoSingerLoadSuccess") && isRoomOwner()) {
                        prepareNum = prepareNum + 1;
                        if (seatListLiveData.getValue() != null && prepareNum == seatListLiveData.getValue().size() && songPlayingLiveData.getValue() != null) {
                            ktvApiProtocol.switchSingerRole(KTVSingRole.LeadSinger, new ISwitchRoleStateListener() {
                                @Override
                                public void onSwitchRoleSuccess() {
                                    ktvApiProtocol.startSing(currentLoadMusicUri, 0);
                                }

                                @Override
                                public void onSwitchRoleFail(@NonNull SwitchRoleFailReason reason) {

                                }
                            });
                        }
                    }
                } catch (JSONException exp) {
                    KTVLogger.e(TAG, "onStreamMessage:" + exp);
                }
            }
        };
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AUDIO_SCENARIO_GAME_STREAMING;
        config.addExtension("agora_ai_echo_cancellation_extension");
        try {
            mRtcEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            KTVLogger.e(TAG, "RtcEngine.create() called error: " + e);
        }
        mRtcEngine.loadExtensionProvider("agora_drm_loader");


        // ------------------ 场景化api初始化 ------------------
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
                roomInfoLiveData.getValue().getAgoraChorusToken(), 10, KTVType.SingRelay, KTVMusicType.SONG_URL)
        );

        ktvApiProtocol.addEventHandler(new IKTVApiEventHandler() {
                                           @Override
                                           public void onMusicPlayerStateChanged(@NonNull io.agora.mediaplayer.Constants.MediaPlayerState state, @NonNull io.agora.mediaplayer.Constants.MediaPlayerReason reason, boolean isLocal) {
                                               switch (state) {
                                                   case PLAYER_STATE_OPEN_COMPLETED:
                                                       playerMusicOpenDurationLiveData.postValue(ktvApiProtocol.getMediaPlayer().getDuration());
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
                                               Log.d("hugo", "onMusicPlayerPositionChanged: " + position_ms);
                                               if (!hasRecievedFirstPosition && isOnSeat) {
                                                   hasRecievedFirstPosition = true;
                                                   playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_BATTLE);
                                               }
                                               for (int i = 0; i < relayList.size() - 1; i++) {
                                                   if (Math.abs(position_ms - relayList.get(i)) < 500) {
                                                       // 下一段
                                                       // workaround：防止因为mpk position回调时间不准造成的段时间内重复上报段落切换事件的bug
                                                       if (System.currentTimeMillis() - mLastPostSongPartChangeStatusTime < 5000) break;
                                                       GraspModel graspModel = new GraspModel();
                                                       graspModel.status = GraspStatus.IDLE;
                                                       if (graspStatusMutableLiveData.getValue() != null) {
                                                           graspModel.userId = graspStatusMutableLiveData.getValue().userId;
                                                           graspModel.userName = graspStatusMutableLiveData.getValue().userName;
                                                           graspModel.headUrl = graspStatusMutableLiveData.getValue().headUrl;
                                                           graspModel.partNum = graspStatusMutableLiveData.getValue().partNum;
                                                       }
                                                       mLastPostSongPartChangeStatusTime = System.currentTimeMillis();
                                                       partNum = i + 2;
                                                       graspStatusMutableLiveData.postValue(graspModel);
                                                       break;
                                                   } else if ((position_ms - relayList.get(i)) > -3000 && (position_ms - relayList.get(i) < -2000)) {
                                                       // 提前3s下一段提示
                                                       GraspModel graspModel = new GraspModel();
                                                       graspModel.status = GraspStatus.Mention;
                                                       if (graspStatusMutableLiveData.getValue() != null) {
                                                           graspModel.userId = graspStatusMutableLiveData.getValue().userId;
                                                           graspModel.userName = graspStatusMutableLiveData.getValue().userName;
                                                           graspModel.headUrl = graspStatusMutableLiveData.getValue().headUrl;
                                                           graspModel.partNum = graspStatusMutableLiveData.getValue().partNum;
                                                       }
                                                       graspStatusMutableLiveData.postValue(graspModel);
                                                       break;
                                                   }
                                               }
                                           }
                                       }
        );

        ktvApiProtocol.getMediaPlayer().setPlayerOption("play_pos_change_callback", 500);
        if (isRoomOwner()) {
            ktvApiProtocol.muteMic(false);
            isOnSeat = true;
        }

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
            contentInspectConfig.modules = new ContentInspectConfig.ContentInspectModule[]{module1, module2};
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
                isGaming = false;
                resetMusicStatus();
            } else if (gameModel.getStatus() == SingRelayGameStatus.started.getValue()) {
                singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_START);
                rankMap.clear();
            } else if (gameModel.getStatus() == SingRelayGameStatus.ended.getValue()) {
                songPlayingLiveData.postValue(null);
                if (gameModel.getRank() != null) {
                    rankMap.putAll(gameModel.getRank());
                }
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
                    KTVLogger.d(TAG, "rank: " + info.getRank());
                    if (info.getRank() != null) {
                        rankMap.putAll(info.getRank());
                    }
                    singRelayGameStatusMutableLiveData.postValue(GameStatus.ON_END);
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
        Map<String, RankModel> newRankMap = new HashMap<>();
        for (Map.Entry<String, RankModel> entry : rankMap.entrySet()) {
            String key = entry.getKey();
            RankModel value = entry.getValue();
            int songNum = 0;
            if (singerMap.containsKey(key)) {
                songNum = singerMap.get(key).intValue();
            } else if (key.equals(UserManager.getInstance().getUser().id.toString())) {
                songNum = 5;
                for (Map.Entry<String, Integer> entry_ : singerMap.entrySet()) {
                    songNum = songNum - entry_.getValue();
                }
            }
            RankModel newModel = new RankModel(
                    value.getUserName(),
                    songNum,
                    (int) (value.getScore() / value.getLines()),
                    value.getPoster(),
                    value.getLines()
            );
            newRankMap.put(key, newModel);
        }

        ktvServiceProtocol.finishSingRelayGame(newRankMap, e -> {
            KTVLogger.d(TAG, "finishSingRelayGame success");
            return null;
        });
    }

    // ------------------ 开始抢唱 ------------------
    public void graspSong() {
        KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() called");
        if (roomInfoLiveData.getValue() == null || songPlayingLiveData.getValue() == null) return;
        KTVSingRelayGameService.INSTANCE.graspSong(
                KTVSyncManagerServiceImp.getKSceneId(),
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id.toString(),
                UserManager.getInstance().getUser().name,
                songPlayingLiveData.getValue().getSongNo() + "_" + partNum,
                UserManager.getInstance().getUser().headUrl,
                (userId) -> {
                    KTVLogger.d(TAG, "RoomLivingViewModel.graspSong() success " + userId);
                    // 更新Service抢唱结果
                    ktvServiceProtocol.updateSongModel(songPlayingLiveData.getValue().getSongNo(), userId + "_" + partNum, UserManager.getInstance().getUser().name, UserManager.getInstance().getUser().headUrl, e -> {
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
    private int micOldVolume = 100;

    private void setMusicVolume(int v) {
        ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(v);
        ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(v);
    }

    private void setMicVolume(int v) {
        RoomSeatModel value = seatLocalLiveData.getValue();
        int isMuted = value == null ? RoomSeatModel.Companion.getMUTED_VALUE_TRUE() : value.isAudioMuted();
        if (isMuted == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
            KTVLogger.d(TAG, "muted! setMicVolume: " + v);
            micOldVolume = v;
            return;
        }
        KTVLogger.d(TAG, "unmute! setMicVolume: " + v);
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
            ktvApiProtocol.switchAudioTrack(AudioTrackMode.BAN_ZOU);
            mAudioTrackMode = KTVPlayerTrackMode.Acc;
        } else {
            if (isRoomOwner()) {
                // 主唱（房主）开导唱
                ktvApiProtocol.switchAudioTrack(AudioTrackMode.DAO_CHANG);
            } else {
                // 其他游戏者开原唱
                ktvApiProtocol.switchAudioTrack(AudioTrackMode.YUAN_CHANG);
            }
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
        hasRecievedFirstPosition = false;
        retryTimes = 0;
        prepareNum = 0;
        singerMap.clear();
        mLastPostSongPartChangeStatusTime = 0L;
        mAudioTrackMode = KTVPlayerTrackMode.Acc;
        ktvApiProtocol.switchSingerRole(KTVSingRole.Audience, null);

        // 重置耳返
        mSetting.setEar(false);
        if (mRtcEngine != null) {
            mRtcEngine.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
        }

        graspStatusMutableLiveData.postValue(null);
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
                CustomToast.show(R.string.ktv_relay_lrc_load_fail);
                callback.completion(error);
            } else {
                songList.clear();
                songList.addAll(ktvSongApiModels);
                callback.completion(null);
            }
            return null;
        });
    }


    // ------------------ 歌曲开始播放 ------------------
    private int retryTimes = 0;

    public void musicStartPlay(@NonNull RoomSelSongModel music) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called");
        if (music.getUserNo() == null || roomInfoLiveData.getValue() == null) return;
        partNum = 1;
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);

        long songCode = Long.parseLong(music.getSongNo());
        int mainSingerUid = Integer.parseInt(music.getUserNo());

        getRestfulSongList((error) -> {
            if (isOnSeat()) {
                // 麦上玩家加载音乐
                loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), mainSingerUid,
                        KTVLoadMusicMode.LOAD_MUSIC_AND_LRC, false), music);
            } else {
                // 观众
                loadMusic(new KTVLoadMusicConfiguration(music.getSongNo(), mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY, false
                ), music);
            }
        });
    }

    private AtomicBoolean loadingMusic = new AtomicBoolean(false);

    private String currentLoadMusicUri = "";

    private void loadMusic(KTVLoadMusicConfiguration config, RoomSelSongModel songInfo) {
        loadingMusic.set(true);

        innerLoadMusic(config, songInfo, new SongLoadStateListener() {
            @Override
            public void onMusicLoadSuccess(@NonNull String songCode, @NonNull String musicUri, @NonNull String lyricUrl) {
                loadingMusic.set(false);
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    return;
                }

                // 重置settings
                retryTimes = 0;
                mSetting.setVolMic(100);
                mSetting.setVolMusic(50);
                ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
                ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);

                if (isRoomOwner()) {
                    prepareNum = prepareNum + 1;
                    if (seatListLiveData.getValue() != null && prepareNum == seatListLiveData.getValue().size()) {
                        ktvApiProtocol.loadMusic(musicUri, config);
                        currentLoadMusicUri = musicUri;
                        ktvApiProtocol.switchSingerRole(KTVSingRole.LeadSinger, new ISwitchRoleStateListener() {
                            @Override
                            public void onSwitchRoleSuccess() {
                                ktvApiProtocol.startSing(musicUri, 0);
                            }

                            @Override
                            public void onSwitchRoleFail(@NonNull SwitchRoleFailReason reason) {

                            }
                        });
                    }
                } else if (isOnSeat()) {
                    SyncPrepareReady();
                    ktvApiProtocol.loadMusic(musicUri, config);
                    currentLoadMusicUri = musicUri;
                    ktvApiProtocol.switchSingerRole(KTVSingRole.CoSinger, null);
                } else {
                    ktvApiProtocol.loadMusic(musicUri, config);
                    currentLoadMusicUri = musicUri;
                }
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
            }

            @Override
            public void onMusicLoadFail(@NonNull String songCode, @NonNull SongLoadFailReason reason) {
                loadingMusic.set(false);
                // 当前已被切歌
                if (songPlayingLiveData.getValue() == null) {
                    return;
                }

                KTVLogger.e(TAG, "onMusicLoadFail， reason: " + reason);
//                if (reason == SongLoadFailReason.MUSIC_DOWNLOAD_FAIL) {
//                    // 未获取到歌词 正常播放
//                    retryTimes = 0;
//                    mSetting.setVolMic(100);
//                    mSetting.setVolMusic(50);
//                    ktvApiProtocol.getMediaPlayer().adjustPlayoutVolume(50);
//                    ktvApiProtocol.getMediaPlayer().adjustPublishSignalVolume(50);
//
//                    playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
//                    noLrcLiveData.postValue(true);
//                }

                if (reason == SongLoadFailReason.MUSIC_DOWNLOAD_FAIL) {
                    // 歌曲加载失败 ，重试3次
                    retryTimes = retryTimes + 1;
                    if (retryTimes < 3) {
                        loadMusic(config, songInfo);
                    } else {
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        ToastUtils.showToastLong(R.string.ktv_relay_try);
                    }
                } else {
                    CustomToast.show(R.string.ktv_relay_load_failed, Toast.LENGTH_LONG);
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

    // ------------------ 重新获取歌词url ------------------
    public void reGetLrcUrl() {
        RoomSelSongModel songModel = songPlayingLiveData.getValue();
        if (songModel == null || songModel.getUserNo() == null) return;
        String songIdentifier = songPlayingLiveData.getValue().getSongNo();
        try {
            int mainSingerUid = Integer.parseInt(songPlayingLiveData.getValue().getUserNo());
//            long songCode = Long.parseLong(songPlayingLiveData.getValue().getSongNo());
            loadMusic(new KTVLoadMusicConfiguration(songIdentifier, mainSingerUid, KTVLoadMusicMode.LOAD_LRC_ONLY, false), songModel);
        } catch (RuntimeException e) {
            KTVLogger.d(TAG, "RoomLivingViewModel.reGetLrcUrl() error:" + e);
        }
    }

    // ------------------ 歌曲seek ------------------
    public void musicSeek(long time) {
        ktvApiProtocol.seekSing(time);
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

        // 本地演唱 计入rank
        if (rankMap.containsKey(UserManager.getInstance().getUser().id.toString())) {
            RankModel oldModel = rankMap.get(UserManager.getInstance().getUser().id.toString());
            if (oldModel != null) {
                RankModel model = new RankModel(
                        UserManager.getInstance().getUser().name,
                        0,
                        oldModel.getScore() + score,
                        UserManager.getInstance().getUser().headUrl,
                        oldModel.getLines() + 1
                );
                rankMap.remove(UserManager.getInstance().getUser().id.toString());
                rankMap.put(UserManager.getInstance().getUser().id.toString(), model);
            }
        } else {
            rankMap.put(UserManager.getInstance().getUser().id.toString(), new RankModel(
                    UserManager.getInstance().getUser().name,
                    0,
                    (int) score,
                    UserManager.getInstance().getUser().headUrl,
                    1
            ));
        }
    }

    private void SyncPrepareReady() {
        if (mRtcEngine == null) return;
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "CoSingerLoadSuccess");
        JSONObject jsonMsg = new JSONObject(msg);
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "SyncPrepareReady() sendStreamMessage called returned: " + ret);
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
        list.sort((o1, o2) -> {
            if (o1.score != o2.score) {
                return o2.score - o1.score; //score大的在前面
            } else {
                return o2.songNum - o1.songNum; //score相同 songNum大的在前面
            }
        });
    }

    public boolean isNextRoundSinger() {
        KTVLogger.d(TAG, "RoomLivingViewModel.isNextRoundSinger() called");
        // 当前无歌曲
        if (songsOrderedLiveData.getValue() == null || songsOrderedLiveData.getValue().size() == 0) return false;
        // 无抢唱记录
        if (graspStatusMutableLiveData.getValue() == null || graspStatusMutableLiveData.getValue().userId == null) {
            KTVLogger.d(TAG, "RoomLivingViewModel.isNextRoundSinger() no grasp record1");
            return isRoomOwner();
        }
        // 有抢唱记录
        // 本段有人抢
        KTVLogger.d(TAG, "RoomLivingViewModel.isNextRoundSinger() debug， graspStatusMutableLiveData.getValue().partNum：" + graspStatusMutableLiveData.getValue().partNum + " partNum：" + partNum);
        if (graspStatusMutableLiveData.getValue().partNum == (partNum - 1)) {
            KTVLogger.d(TAG, "RoomLivingViewModel.isNextRoundSinger() has grasp record， graspStatusMutableLiveData.getValue().userId：" + graspStatusMutableLiveData.getValue().userId);
            return graspStatusMutableLiveData.getValue().userId.split("_")[0].equals(UserManager.getInstance().getUser().id.toString());
        } else {
            // 本段无人抢
            KTVLogger.d(TAG, "RoomLivingViewModel.isNextRoundSinger() no grasp record2");
            return isRoomOwner();
        }
    }
}

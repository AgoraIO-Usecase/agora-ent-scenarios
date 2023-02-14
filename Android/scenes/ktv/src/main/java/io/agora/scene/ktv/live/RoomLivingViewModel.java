package io.agora.scene.ktv.live;

import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_MODERATION;
import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_SUPERVISE;

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

import io.agora.musiccontentcenter.IAgoraMusicContentCenter;
import io.agora.musiccontentcenter.IAgoraMusicPlayer;
import io.agora.musiccontentcenter.IMusicContentCenterEventHandler;
import io.agora.musiccontentcenter.Music;
import io.agora.musiccontentcenter.MusicChartInfo;
import io.agora.musiccontentcenter.MusicContentCenterConfiguration;
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
import io.agora.scene.ktv.KTVLogger;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.service.ChangeMVCoverInputModel;
import io.agora.scene.ktv.service.ChooseSongInputModel;
import io.agora.scene.ktv.service.JoinChorusInputModel;
import io.agora.scene.ktv.service.JoinRoomOutputModel;
import io.agora.scene.ktv.service.KTVServiceProtocol;
import io.agora.scene.ktv.service.MakeSongTopInputModel;
import io.agora.scene.ktv.service.OnSeatInputModel;
import io.agora.scene.ktv.service.OutSeatInputModel;
import io.agora.scene.ktv.service.RemoveSongInputModel;
import io.agora.scene.ktv.service.RoomSeatModel;
import io.agora.scene.ktv.service.RoomSelSongModel;
import io.agora.scene.ktv.widget.LrcControlView;
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.ktv.widget.MusicSettingDialog;

public class RoomLivingViewModel extends ViewModel implements KTVApi.KTVApiEventHandler {

    private final String TAG = "KTV Scene LOG";
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
     * 歌词信息
     */
    final MutableLiveData<List<RoomSelSongModel>> songsOrderedLiveData = new MutableLiveData<>();
    final MutableLiveData<RoomSelSongModel> songPlayingLiveData = new MutableLiveData<>();
    final MutableLiveData<RoomSelSongModel> chorusPlayingLiveData = new MutableLiveData<>();

    /**
     * Player/RTC信息
     */
    int streamId = 0;

    enum PlayerMusicStatus {
        ON_PREPARE,
        ON_WAIT_CHORUS,
        ON_CHORUS_JOINED,
        ON_PLAYING,
        ON_PAUSE,
        ON_LRC_RESET,
        ON_CHANGING_START,
        ON_CHANGING_END
    }
    final MutableLiveData<PlayerMusicStatus> playerMusicStatusLiveData = new MutableLiveData<>();
    final MutableLiveData<Long> playerMusicOpenDurationLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> playerMusicPlayCompleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> playerMusicCountDownLiveData = new MutableLiveData<>();
    final MutableLiveData<NetWorkEvent> networkStatusLiveData = new MutableLiveData<>();

    /**
     * Rtc引擎
     */
    private RtcEngineEx mRtcEngine;
    /**
     * 主版本的音频设置
     */
    private final ChannelMediaOptions mainChannelMediaOption = new ChannelMediaOptions();
    /**
     * 歌曲内容中心
     */
    public IAgoraMusicContentCenter iAgoraMusicContentCenter;
    /**
     * 播放器
     */
    protected IAgoraMusicPlayer mPlayer;
    /**
     * 播放器配置
     */
    MusicSettingBean mSetting;
    /**
     * 是否开启后台播放
     */
    private boolean isBackPlay = false;
    private boolean isOpnEar = false;

    /**
     * RTC歌词内容回调
     */
    private final Map<String, IMusicContentCenterEventHandler> rtcMusicHandlerMap = new HashMap<>();

    public RoomLivingViewModel(JoinRoomOutputModel roomInfo) {
        this.roomInfoLiveData = new MutableLiveData<>(roomInfo);
    }

    public boolean isRoomOwner() {
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().id.toString());
    }

    public void init() {
        if (isRoomOwner()) {
            isOnSeat = true;
        }
        initRTCPlayer();
        initRoom();
        initSeats();
        initSongs();
        initReConnectEvent();
    }

    public boolean release() {
        KTVLogger.d(TAG, "release called");
        streamId = 0;
        if (mPlayer != null && iAgoraMusicContentCenter != null && mRtcEngine != null) {
            ktvApiProtocol.release();
        }

        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.destroy();
            mPlayer = null;
        }

        if (iAgoraMusicContentCenter != null) {
            IAgoraMusicContentCenter.destroy();
            iAgoraMusicContentCenter = null;
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
        getSongChosenList();
    }

    // ======================= 房间相关 =======================

    public void initRoom() {
        JoinRoomOutputModel _roomInfo = roomInfoLiveData.getValue();
        if (_roomInfo == null) {
            throw new RuntimeException("The roomInfo muse be not null before initSeats method calling!");
        }

        roomUserCountLiveData.postValue(_roomInfo.getRoomPeopleNum());

        ktvServiceProtocol.subscribeRoomStatus((ktvSubscribe, vlRoomListModel) -> {
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeDeleted");
                roomDeleteLiveData.postValue(true);
            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                // 当房间内状态发生改变时触发
                KTVLogger.d(TAG, "subscribeRoomStatus KTVSubscribeUpdated");
                JoinRoomOutputModel _rroomInfo = roomInfoLiveData.getValue();
                if (!vlRoomListModel.getBgOption().equals(_rroomInfo.getBgOption())) {
                    roomInfoLiveData.postValue(new JoinRoomOutputModel(
                            _rroomInfo.getRoomName(),
                            _rroomInfo.getRoomNo(),
                            _rroomInfo.getCreatorNo(),
                            vlRoomListModel.getBgOption(),
                            _rroomInfo.getSeatsArray(),
                            _rroomInfo.getRoomPeopleNum(),
                            _rroomInfo.getAgoraRTMToken(),
                            _rroomInfo.getAgoraRTCToken(),
                            _rroomInfo.getAgoraPlayerRTCToken()
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
                KTVLogger.e(TAG, "RoomLivingViewModel.setMV_BG() success");
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
            throw new RuntimeException("The roomInfo muse be not null before initSeats method calling!");
        }
        List<RoomSeatModel> seatsArray = _roomInfo.getSeatsArray();
        seatListLiveData.postValue(seatsArray);

        if (seatsArray != null) {
            for (RoomSeatModel roomSeatModel : seatsArray) {
                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                    seatLocalLiveData.setValue(roomSeatModel);
                    isOnSeat = true;
                    if (mRtcEngine != null) {
                        mainChannelMediaOption.publishCameraTrack = false;
                        mainChannelMediaOption.publishMicrophoneTrack = true;
                        mainChannelMediaOption.publishCustomAudioTrack = false;
                        mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                        mainChannelMediaOption.autoSubscribeVideo = true;
                        mainChannelMediaOption.autoSubscribeAudio = true;
                        mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                        mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
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
                        mainChannelMediaOption.publishCustomAudioTrack = false;
                        mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                        mainChannelMediaOption.autoSubscribeVideo = true;
                        mainChannelMediaOption.autoSubscribeAudio = true;
                        mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
                        mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                    }
                    updateVolumeStatus(false);

                    RoomSelSongModel songPlayingData = songPlayingLiveData.getValue();
                    if(songPlayingData == null){
                        return null;
                    }

                    // 合唱相关逻辑
                    if (UserManager.getInstance().getUser().id.toString().equals(songPlayingLiveData.getValue().getChorusNo())) {
                        //我是合唱
                        getSongChosenList();
                        ktvServiceProtocol.leaveChorus();
                    }
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
                    mainChannelMediaOption.publishCustomAudioTrack = false;
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
                        if (seatModel.isAudioMuted() == RoomSeatModel.Companion.getMUTED_VALUE_TRUE()) {
                            if (seatModel.getUserNo().equals(UserManager.getInstance().getUser().id.toString())) {
                                isOnSeat = false;
                                if (mRtcEngine != null) {
                                    mainChannelMediaOption.publishCameraTrack = false;
                                    mainChannelMediaOption.publishMicrophoneTrack = false;
                                    mainChannelMediaOption.publishCustomAudioTrack = false;
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
                mainChannelMediaOption.publishCameraTrack = isOpen;
                mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
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
            getSongChosenList();
            return null;
        });

        // 获取初始歌曲列表
        getSongChosenList();
    }

    /**
     * 获取歌曲类型
     * @return map key: 类型名称，value: 类型值
     */
    public LiveData<LinkedHashMap<Integer, String>> getSongTypes() {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongTypes() called");
        MutableLiveData<LinkedHashMap<Integer, String>> liveData = new MutableLiveData<>();

        String requestId = iAgoraMusicContentCenter.getMusicCharts();
        rtcMusicHandlerMap.put(requestId, new IMusicContentCenterEventHandler() {
            @Override
            public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
                // do nothing
            }

            @Override
            public void onMusicCollectionResult(String requestId, int status, int page, int pageSize, int total, Music[] list) {
                // do nothing
            }

            @Override
            public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {
                LinkedHashMap<Integer, String> types = new LinkedHashMap<>();
                for (MusicChartInfo musicChartInfo : list) {
                    types.put(musicChartInfo.type, musicChartInfo.name);
                }
                liveData.postValue(types);
            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {
                // do nothing
            }
        });

        return liveData;
    }

    /**
     * 获取歌曲列表
     */
    public LiveData<List<RoomSelSongModel>> getSongList(int type, int page) {
        // 从RTC中获取歌曲列表
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongList() called, type:" + type + " page:" + page);
        MutableLiveData<List<RoomSelSongModel>> liveData = new MutableLiveData<>();
        String jsonOption = "{\"pitchType\":1,\"needLyric\":true}";
        String requestId = iAgoraMusicContentCenter.getMusicCollectionByMusicChartId(type, page, 30, jsonOption);
        rtcMusicHandlerMap.put(requestId, new IMusicContentCenterEventHandler() {
            @Override
            public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
                // do nothing
            }

            @Override
            public void onMusicCollectionResult(String requestId, int status, int page, int pageSize,
                                                int total, Music[] list) {
                List<Music> musicList = new ArrayList<>(Arrays.asList(list));
                List<RoomSelSongModel> songs = new ArrayList<>();

                // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
                ktvServiceProtocol.getChoosedSongsList((e, songsChosen) -> {
                    if(e == null && songsChosen != null){
                        // success
                        for (Music music : musicList) {
                            RoomSelSongModel songItem = null;
                            for (RoomSelSongModel roomSelSongModel : songsChosen) {
                                if(roomSelSongModel.getSongNo().equals(String.valueOf(music.songCode))){
                                    songItem = roomSelSongModel;
                                    break;
                                }
                            }

                            if(songItem == null){
                                songItem = new RoomSelSongModel(
                                        music.name,
                                        String.valueOf(music.songCode),
                                        music.singer,
                                        music.poster,

                                        "", "", "", false, 0, 0, 0, 0
                                );
                            }
                            songs.add(songItem);
                        }
                        liveData.postValue(songs);
                    }else{
                        if(e != null){
                            ToastUtils.showToast(e.getMessage());
                        }
                    }
                    return null;
                });
            }

            @Override
            public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {
                // do nothing
            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {
                // do nothing
            }
        });
        return liveData;
    }

    /**
     * 搜索歌曲
     */
    public LiveData<List<RoomSelSongModel>> searchSong(String condition) {
        // 从RTC中搜索歌曲
        KTVLogger.d(TAG, "RoomLivingViewModel.searchSong() called, condition:" + condition);
        MutableLiveData<List<RoomSelSongModel>> liveData = new MutableLiveData<>();

        String jsonOption = "{\"pitchType\":1,\"needLyric\":true}";
        String requestId = iAgoraMusicContentCenter.searchMusic(condition, 0, 50, jsonOption);
        rtcMusicHandlerMap.put(requestId, new IMusicContentCenterEventHandler() {
            @Override
            public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
                // do nothing
            }

            @Override
            public void onMusicCollectionResult(String requestId, int status, int page, int pageSize, int total, Music[] list) {
                List<Music> musicList = new ArrayList<>(Arrays.asList(list));
                List<RoomSelSongModel> songs = new ArrayList<>();

                // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
                ktvServiceProtocol.getChoosedSongsList((e, songsChosen) -> {
                    if(e == null && songsChosen != null){
                        // success
                        for (Music music : musicList) {
                            RoomSelSongModel songItem = null;
                            for (RoomSelSongModel roomSelSongModel : songsChosen) {
                                if(roomSelSongModel.getSongNo().equals(String.valueOf(music.songCode))){
                                    songItem = roomSelSongModel;
                                    break;
                                }
                            }

                            if(songItem == null){
                                songItem = new RoomSelSongModel(
                                        music.name,
                                        String.valueOf(music.songCode),
                                        music.singer,
                                        music.poster,

                                        "", "", "", false, 0, 0, 0, 0
                                );
                            }

                            songs.add(songItem);
                        }
                        liveData.postValue(songs);
                    }else{
                        if(e != null){
                            ToastUtils.showToast(e.getMessage());
                        }
                    }
                    return null;
                });
            }

            @Override
            public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {
                // do nothing
            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {
                // do nothing
            }
        });
        return liveData;
    }

    /**
     * 点歌
     */
    public LiveData<Boolean> chooseSong(RoomSelSongModel songModel, boolean isChorus) {
        KTVLogger.d(TAG, "RoomLivingViewModel.chooseSong() called, name:" + songModel.getName() + " isChorus:" + isChorus);
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        ktvServiceProtocol.chooseSong(
                new ChooseSongInputModel(isChorus ? 1 : 0,
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
                        ToastUtils.showToast(e.getMessage());
                        liveData.postValue(false);
                    }
                    return null;
                }
        );
        return liveData;
    }

    /**
     * 删歌
     */
    public void deleteSong(RoomSelSongModel songModel) {
        KTVLogger.d(TAG, "RoomLivingViewModel.deleteSong() called, name:" + songModel.getName());
        ktvServiceProtocol.removeSong(
                new RemoveSongInputModel(songModel.getSongNo()),
                e -> {
                    if (e == null) {
                        // success: do nothing for subscriber dealing with the event already
                        KTVLogger.d(TAG, "RoomLivingViewModel.deleteSong() success");
                    } else {
                        // failure
                        KTVLogger.e(TAG, "RoomLivingViewModel.deleteSong() failed: " + e.getMessage());
                        ToastUtils.showToast(e.getMessage());
                    }
                    return null;
                }
        );
    }

    /**
     * 置顶歌曲
     */
    public void topUpSong(RoomSelSongModel songModel){
        KTVLogger.d(TAG, "RoomLivingViewModel.topUpSong() called, name:" + songModel.getName());
        ktvServiceProtocol.makeSongTop(new MakeSongTopInputModel(
                songModel.getSongNo()
        ), e -> {
            if(e == null){
                // success: do nothing for subscriber dealing with the event already
                KTVLogger.d(TAG, "RoomLivingViewModel.topUpSong() success");
            }else{
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.topUpSong() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 获取已点列表
     */
    public void getSongChosenList() {
        KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() called");
        ktvServiceProtocol.getChoosedSongsList((e, data) -> {
            if (e == null && data != null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() success");
                songsOrderedLiveData.postValue(data);

                if (data.size() > 0){
                    RoomSelSongModel value = songPlayingLiveData.getValue();
                    RoomSelSongModel songPlaying = data.get(0);

                    if (value == null) {
                        // 无已点歌曲， 直接将列表第一个设置为当前播放歌曲
                        KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() chosen song list is empty");
                        songPlayingLiveData.postValue(songPlaying);
                    } else {
                        // 当前有已点歌曲, 且更新歌曲和之前歌曲非同一首
                        if (!value.getSongNo().equals(songPlaying.getSongNo())) {
                            KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() single or first chorus");
                            songPlayingLiveData.postValue(songPlaying);
                        } else {
                            if ((value.isChorus() && !songPlaying.isChorus())) {
                                // 取消合唱
                                KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() become solo");
                                songPlayingLiveData.postValue(songPlaying);
                            } else if (value.isChorus() && value.getChorusNo() == null && songPlaying.getChorusNo() != null) {
                                // 加入合唱
                                KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() partner joined");
                                songPlayingLiveData.postValue(songPlaying);
                            } else if (value.isChorus() && value.getChorusNo() != null && songPlaying.getChorusNo() != null && songPlaying.getChorusNo().equals("0")) {
                                // 伴唱退出合唱
                                KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() partner exited");
                                if (value.getChorusNo().equals(UserManager.getInstance().getUser().id.toString())) {
                                    ktvApiProtocol.stopSong();
                                    ktvApiProtocol.loadSong(Long.parseLong(songPlaying.getSongNo()), new KTVSongConfiguration(KTVSongType.KTVSongTypeChorus, KTVSingRole.KTVSingRoleAudience, Long.parseLong(songPlaying.getSongNo()), Integer.parseInt(songPlaying.getUserNo()), 0),
                                            (song, lyricUrl, singRole, singState) -> {
                                                if (singState == KTVLoadSongState.KTVLoadSongStateOK) {
                                                    if (singRole == KTVSingRole.KTVSingRoleAudience) {
                                                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                                                    }
                                                    ktvApiProtocol.playSong(song);
                                                }
                                                return null;
                                            }
                                    );
                                }
                                chorusPlayingLiveData.setValue(songPlaying);
                            }
                        }
                    }
                } else {
                    KTVLogger.d(TAG, "RoomLivingViewModel.getSongChosenList() return is emptyList");
                    songPlayingLiveData.postValue(null);
                }
                _loadingDialogVisible.postValue(false);

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
     * 点击加入合唱
     */
    public void joinChorus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.joinChorus() called");
        if (!isOnSeat) {
            ToastUtils.showToast(R.string.ktv_onseat_toast);
            return;
        }
        RoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }
        if (!TextUtils.isEmpty(musicModel.getChorusNo())) {
            return;
        }

        ktvServiceProtocol.joinChorus(new JoinChorusInputModel(musicModel.getSongNo()), e -> {
            if (e == null) {
                // success
                KTVLogger.d(TAG, "RoomLivingViewModel.joinChorus() success");
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.joinChorus() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 退出合唱
     */
    public void leaveChorus() {
        KTVLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called");
        ktvServiceProtocol.becomeSolo();
    }

    /**
     * 开始切歌
     */
    public void changeMusic() {
        KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() called");
        RoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) return;

        ktvApiProtocol.stopSong();

        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_START);
        _loadingDialogVisible.postValue(true);
        ktvServiceProtocol.removeSong(new RemoveSongInputModel(
                musicModel.getSongNo()
        ), e -> {
            if (e == null) {
                // success do nothing for dealing in song subscriber
                KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() success");
            } else {
                // failed
                KTVLogger.e(TAG, "RoomLivingViewModel.changeMusic() failed: " + e.getMessage());
                _loadingDialogVisible.postValue(false);
                ToastUtils.showToast(e.getMessage());
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END);
            }
            return null;
        });
    }

    /**
     * 设置歌词view
     */
    public void setLryView(LrcControlView view) {
        ktvApiProtocol.setLycView(view);
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
            public void onStreamMessage(int uid, int streamId, byte[] data) {
                JSONObject jsonMsg;
                try {
                    String strMsg = new String(data);
                    jsonMsg = new JSONObject(strMsg);
                    if (jsonMsg.getString("cmd").equals("countdown")) {
                        // 各端收到合唱倒计时
                        if (mPlayer == null) return;
                        int time = jsonMsg.getInt("time");
                        playerMusicCountDownLiveData.postValue(time);
                    }
                } catch (JSONException exp) {
                    KTVLogger.e(TAG, "onStreamMessage:" + exp);
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
        };
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS;
        try {
            mRtcEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            KTVLogger.e(TAG, "RtcEngine.create() called error: " + e);
        }
        mRtcEngine.loadExtensionProvider("agora_drm_loader");

        // ------------------ 加入频道 ------------------
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.enableVideo();
        mRtcEngine.enableAudio();
        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_GAME_STREAMING);
        mRtcEngine.enableAudioVolumeIndication(30, 10, true);

        mRtcEngine.setParameters("{\"rtc.ntp_delay_drop_threshold\":1000}");
        mRtcEngine.setParameters("{\"che.audio.agc.enable\": true}");
        mRtcEngine.setParameters("{\"rtc.video.enable_sync_render_ntp\": true}");
        mRtcEngine.setParameters("{\"rtc.net.maxS2LDelay\": 800}");
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

        // ------------------ 初始化内容中心 ------------------
        MusicContentCenterConfiguration contentCenterConfiguration
                = new MusicContentCenterConfiguration();
        contentCenterConfiguration.appId = BuildConfig.AGORA_APP_ID;
        contentCenterConfiguration.mccUid = UserManager.getInstance().getUser().id;
        contentCenterConfiguration.token = roomInfoLiveData.getValue().getAgoraRTMToken();
        iAgoraMusicContentCenter = IAgoraMusicContentCenter.create(mRtcEngine);
        iAgoraMusicContentCenter.initialize(contentCenterConfiguration);

        // ------------------ 初始化音乐播放器实例 ------------------
        mPlayer = iAgoraMusicContentCenter.createMusicPlayer();

        // ------------------ 初始化音乐播放设置面版 ------------------
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
                if (mRtcEngine != null) {
                    mRtcEngine.setAudioEffectPreset(getEffectIndex(effect));
                }
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
                if (mPlayer != null) {
                    mPlayer.setAudioPitch(newToneValue);
                }
            }

            @Override
            public void onRemoteVolumeChanged(int volume) {
                ktvApiProtocol.adjustRemoteVolume(volume);
            }
        });

        // ------------------ 初始化音量 ------------------
        mPlayer.adjustPlayoutVolume(50);
        mPlayer.adjustPublishSignalVolume(50);

        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }

        ktvApiProtocol.initWithRtcEngine(mRtcEngine, roomInfoLiveData.getValue().getRoomNo(), iAgoraMusicContentCenter, mPlayer, streamId, this);
    }

    // ======================= settings =======================
    // ------------------ 音效调整 ------------------
    private int getEffectIndex(int index) {
        switch (index) {
            case 0:
                return Constants.AUDIO_EFFECT_OFF;
            case 1:
                return Constants.ROOM_ACOUSTICS_KTV;
            case 2:
                return Constants.ROOM_ACOUSTICS_VOCAL_CONCERT;
            case 3:
                return Constants.ROOM_ACOUSTICS_STUDIO;
            case 4:
                return Constants.ROOM_ACOUSTICS_PHONOGRAPH;
            case 5:
                return Constants.ROOM_ACOUSTICS_SPACIAL;
            case 6:
                return Constants.ROOM_ACOUSTICS_ETHEREAL;
            case 7:
                return Constants.STYLE_TRANSFORMATION_POPULAR;
            case 8:
                return Constants.STYLE_TRANSFORMATION_RNB;
        }
        return Constants.AUDIO_EFFECT_OFF;
    }

    // ------------------ 音量调整 ------------------
    private int micVolume = 100;
    private int micOldVolume = 100;

    private void setMusicVolume(int v) {
        mPlayer.adjustPlayoutVolume(v);
        mPlayer.adjustPublishSignalVolume(v);
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
    protected KTVPlayerTrackMode mAudioTrackMode = KTVPlayerTrackMode.KTVPlayerTrackAcc;
    public boolean musicToggleOriginal() {
        if (mAudioTrackMode == KTVPlayerTrackMode.KTVPlayerTrackOrigin) {
            ktvApiProtocol.selectTrackMode(KTVPlayerTrackMode.KTVPlayerTrackAcc);
            mAudioTrackMode = KTVPlayerTrackMode.KTVPlayerTrackAcc;
        } else {
            ktvApiProtocol.selectTrackMode(KTVPlayerTrackMode.KTVPlayerTrackOrigin);
            mAudioTrackMode = KTVPlayerTrackMode.KTVPlayerTrackOrigin;
        }
        return false;
    }

    // ------------------ 暂停/播放 ------------------
    public void musicToggleStart() {
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            ktvApiProtocol.pausePlay();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            ktvApiProtocol.resumePlay();
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

    // ------------------ 倒计时 ------------------
    public void musicCountDown(int time) {
        playerMusicCountDownLiveData.postValue(time);
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "countdown");
        msg.put("time", time);
        JSONObject jsonMsg = new JSONObject(msg);
        mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
    }

    // ------------------ 歌曲开始播放 ------------------
    public void musicStartPlay(@NonNull RoomSelSongModel music) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called");
        chorusPlayingLiveData.setValue(null);
        ktvApiProtocol.stopSong();
        mAudioTrackMode = KTVPlayerTrackMode.KTVPlayerTrackAcc;

        boolean isOwnSong = Objects.equals(music.getUserNo(), UserManager.getInstance().getUser().id.toString());
        boolean isChorus = music.isChorus();
        boolean isChorusMem = false;
        if (isChorus && music.getChorusNo() != null) {
            isChorusMem = music.getChorusNo().equals(UserManager.getInstance().getUser().id.toString());
        }
        Long songCode = Long.parseLong(music.getSongNo());
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);

        KTVSongType type = isChorus ? KTVSongType.KTVSongTypeChorus : KTVSongType.KTVSongTypeSolo;
        KTVSingRole role =  isOwnSong ? KTVSingRole.KTVSingRoleMainSinger : (isChorusMem ? KTVSingRole.KTVSingRoleCoSinger : KTVSingRole.KTVSingRoleAudience);
        int mainSingerUid = music.getUserNo() == null ? 0 : Integer.parseInt(music.getUserNo());
        int coSingerUid = music.getChorusNo() == null ? 0 : Integer.parseInt(music.getChorusNo());

        if (isChorus) {
            if (music.getChorusNo() == null) {
                // 没有合唱者加入时， 播放等待动画
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_WAIT_CHORUS);
                return;
            } else if (isOwnSong) {
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHORUS_JOINED);
            }
        }
        ktvApiProtocol.loadSong(songCode, new KTVSongConfiguration(type, role, songCode, mainSingerUid, coSingerUid),
                (song, lyricUrl, singRole, singState) -> {
                    if (singState == KTVLoadSongState.KTVLoadSongStateOK) {
                        if (singRole == KTVSingRole.KTVSingRoleAudience) {
                            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        } else if (singRole == KTVSingRole.KTVSingRoleCoSinger) {
                            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        }
                        ktvApiProtocol.playSong(song);
                    }
                    return null;
                }
        );
        ktvServiceProtocol.makeSongDidPlay(music, e -> {
            if (e != null) {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    // ------------------ 歌曲seek ------------------
    public void musicSeek(long time) {
        ktvApiProtocol.seek(time);
    }

    public Long getSongDuration() {
        if (mPlayer == null) return 0L;
        return mPlayer.getDuration();
    }

    // ------------------ 歌曲结束播放 ------------------
    public void musicStop() {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStop() called");
        // 列表中无歌曲， 还原状态
        chorusPlayingLiveData.setValue(null);
        ktvApiProtocol.stopSong();
        mAudioTrackMode = KTVPlayerTrackMode.KTVPlayerTrackAcc;

        if (isOnSeat) {
            mainChannelMediaOption.publishMicrophoneTrack = true;
            mainChannelMediaOption.publishCameraTrack = isCameraOpened;
            mainChannelMediaOption.publishCustomAudioTrack = false;
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
            mainChannelMediaOption.autoSubscribeVideo = true;
            mainChannelMediaOption.autoSubscribeAudio = true;
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            mainChannelMediaOption.publishMediaPlayerId = mPlayer.getMediaPlayerId();
            mainChannelMediaOption.publishMediaPlayerAudioTrack = false;
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
        } else {
            mainChannelMediaOption.publishCameraTrack = false;
            mainChannelMediaOption.publishMicrophoneTrack = false;
            mainChannelMediaOption.publishCustomAudioTrack = false;
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
            mainChannelMediaOption.autoSubscribeVideo = true;
            mainChannelMediaOption.autoSubscribeAudio = true;
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE;
            mainChannelMediaOption.publishMediaPlayerId = mPlayer.getMediaPlayerId();
            mainChannelMediaOption.publishMediaPlayerAudioTrack = false;
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
        }
    }

    public void onStart() {
        if (mPlayer != null && isBackPlay) {
            mPlayer.mute(false);
        }
    }

    public void onStop() {
        if (mPlayer != null && isBackPlay) {
            mPlayer.mute(true);
        }
    }

    // ------------------ KTVApiEvent ------------------

    @Override
    public void onPlayerStateChanged(@NonNull KTVApi controller, long songCode, @NonNull io.agora.mediaplayer.Constants.MediaPlayerState state, boolean isLocal) {
        switch (state) {
            case PLAYER_STATE_OPEN_COMPLETED:
                playerMusicOpenDurationLiveData.postValue(mPlayer.getDuration());
                break;
            case PLAYER_STATE_PLAYING:
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                break;
            case PLAYER_STATE_PAUSED:
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
                break;
            case PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED:
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
                changeMusic();
                break;
            default:
        }
    }

    @Override
    public void onSingingScoreResult(float score) {
        playerMusicPlayCompleteLiveData.postValue((int)score);
    }

    @Override
    public void onMusicCollectionResult(@Nullable String requestId, int status, int page, int pageSize, int total, @Nullable Music[] list) {
        IMusicContentCenterEventHandler handler = rtcMusicHandlerMap.get(requestId);
        if(handler != null){
            handler.onMusicCollectionResult(requestId, status, page, pageSize, total, list);
            rtcMusicHandlerMap.remove(requestId);
        }
    }

    @Override
    public void onMusicChartsResult(@Nullable String requestId, int status, @Nullable MusicChartInfo[] list) {
        IMusicContentCenterEventHandler handler = rtcMusicHandlerMap.get(requestId);
        if(handler != null){
            handler.onMusicChartsResult(requestId, status, list);
            rtcMusicHandlerMap.remove(requestId);
        }
    }
}

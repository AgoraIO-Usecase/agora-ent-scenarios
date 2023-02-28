package io.agora.scene.ktv.live;

import static io.agora.rtc2.video.ContentInspectConfig.CONTENT_INSPECT_TYPE_SUPERVISE;

import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.agora.lyrics_view.DownloadManager;
import io.agora.lyrics_view.LrcLoadUtils;
import io.agora.lyrics_view.bean.LrcData;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
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
import io.agora.rtc2.RtcConnection;
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
import io.agora.scene.base.utils.ZipUtils;
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
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.ktv.widget.MusicSettingDialog;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RoomLivingViewModel extends ViewModel {

    private final String TAG = "KTV Scene LOG";
    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();

    // loading dialog
    private final MutableLiveData<Boolean> _loadingDialogVisible = new MutableLiveData<>(false);
    final LiveData<Boolean> loadingDialogVisible = _loadingDialogVisible;

    /**
     * 房间信息
     */
    final MutableLiveData<JoinRoomOutputModel> roomInfoLiveData;
    final MutableLiveData<Boolean> roomDeleteLiveData = new MutableLiveData<>();
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

    /**
     * Player/RTC信息
     */
    int streamId = 0;
    boolean mpkNeedStopped = false;
    boolean mccNeedPreload = false;
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
    final MutableLiveData<LrcData> playerMusicLrcDataLiveData = new MutableLiveData<>();
    final MutableLiveData<Long> playerMusicOpenDurationLiveData = new MutableLiveData<>();
    final MutableLiveData<String> playerMusicPlayCompleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Long> playerMusicPlayPositionChangeLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> playerMusicCountDownLiveData = new MutableLiveData<>();
    final MutableLiveData<Double> playerPitchLiveData = new MutableLiveData<>();
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
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().userNo);
    }

    public void init() {
        if (isRoomOwner()) {
            // 房主开启倒计时，默认为在麦上状态
            startExitRoomTimer();
            isOnSeat = true;
        }
        initRTCPlayer();
        initRoom();
        initSeats();
        initSongs();
    }

    public void release() {
        streamId = 0;
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
        }
        mCountDownLatch = null;
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
            } else {
                // failure
                KTVLogger.e(TAG, "RoomLivingViewModel.exitRoom() failed: " + e.getMessage());
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 房主退出房间倒计时（20分钟）
     */
    private CountDownTimer mCountDownLatch;
    public void startExitRoomTimer() {
        if (mCountDownLatch != null) mCountDownLatch.cancel();
        mCountDownLatch = new CountDownTimer(20 * 60 * 1000, 20 * 60 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                ToastUtils.showToast("体验时间已耗尽，自动离开房间");
                exitRoom();
            }
        }.start();
    }

    /**
     * 设置背景
     */
    public void setMV_BG(int bgPosition) {
        KTVLogger.d(TAG, "RoomLivingViewModel.setMV_BG() called: " + bgPosition);
        ktvServiceProtocol.changeMVCover(new ChangeMVCoverInputModel(bgPosition), new Function1<Exception, Unit>() {
            @Override
            public Unit invoke(Exception e) {
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
            }
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
                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    seatLocalLiveData.setValue(roomSeatModel);
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

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
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

                    if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
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

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    seatLocalLiveData.postValue(null);
                }

                if (roomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
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
                    if (UserManager.getInstance().getUser().userNo.equals(songPlayingData.getChorusNo())) {
                        //我是合唱
                        getSongChosenList();
                    } else if (UserManager.getInstance().getUser().userNo.equals(songPlayingData.getUserNo())) {
                        //推送切歌逻辑
                    }
                }
            }
            return null;
        });
    }

    private Thread mReLinkThread;
    public void getSeatStatus() {
        mReLinkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //TODO: workaround 网络重连后等待3s刷新麦位状态， SYNC中添加回调后修改
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                KTVLogger.d(TAG, "getSeatStatusList: call");
                ktvServiceProtocol.getSeatStatusList((e, data) -> {
                    if (e == null && data != null) {
                        KTVLogger.d(TAG, "getSeatStatusList: return" + data);
                        seatListLiveData.setValue(data);
                        try {
                            mReLinkThread.join();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return null;
                });
            }
        });
        mReLinkThread.start();
    }

    /**
     * 上麦
     */
    public void haveSeat(int onSeatIndex) {
        KTVLogger.d(TAG, "RoomLivingViewModel.haveSeat() called: " + onSeatIndex);
        ktvServiceProtocol.onSeat(new OnSeatInputModel(onSeatIndex), new Function1<Exception, Unit>() {
            @Override
            public Unit invoke(Exception e) {
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
            }
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
                            if (seatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
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
        String requestId = iAgoraMusicContentCenter.getMusicCollectionByMusicChartId(type, page, 30);
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

        String requestId = iAgoraMusicContentCenter.searchMusic(condition, 0, 100);
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
        if(songModel == null){
            return liveData;
        }
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
        if(songModel == null){
            return;
        }
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
        if(songModel == null){
            return;
        }
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
    public void leaveChorus(Context context) {
        KTVLogger.d(TAG, "RoomLivingViewModel.leaveChorus() called");
        ktvServiceProtocol.becomeSolo();
    }

    /**
     * 开始切歌
     */
    public void changeMusic() {
        KTVLogger.d(TAG, "RoomLivingViewModel.changeMusic() called");
        RoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }
        if (mPlayer != null) {
            if (musicModel.isChorus() && musicModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                // 合唱主唱切歌， mpk流离开频道
                mRtcEngine.muteAllRemoteAudioStreams(false);
                mRtcEngine.leaveChannelEx(new RtcConnection(roomInfoLiveData.getValue().getRoomNo(), (int) (UserManager.getInstance().getUser().id * 10 + 1)));
            }
            mPlayer.stop();
        }
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

    // ======================= 分数相关 =======================
    private void initSingScore(){
        ktvServiceProtocol.subscribeSingingScoreChange((ktvSubscribe, aDouble) -> {
            if (mPlayer == null || mPlayer.getState() != io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_PLAYING) {
                return null;
            }

            playerPitchLiveData.postValue(aDouble);
            return null;
        });
    }

    // ======================= Player/RTC/MPK相关 =======================
    private void initRTCPlayer(){
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
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                KTVLogger.d(TAG, "onJoinChannelSuccess() called, channel: " + channel + " uid: " + uid);
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                KTVLogger.d(TAG, "onLeaveChannel() called");
            }

            @Override
            public void onStreamMessage(int uid, int streamId, byte[] data) {
                JSONObject jsonMsg;
                try {
                    String strMsg = new String(data);
                    jsonMsg = new JSONObject(strMsg);
                    if (jsonMsg.getString("cmd").equals("setLrcTime")) {
                        long position = jsonMsg.getLong("time");
                        if (position == 0) {
                            // 伴唱收到歌曲播放指令
                            mPlayer.play();
                        } else if (position == -1) {
                            // 伴唱收到歌曲暂停指令
                            mPlayer.pause();
                        } else {
                            // 观众收到歌词播放状态同步信息
                            mRecvedPlayPosition = position;
                            mLastRecvPlayPosTime = System.currentTimeMillis();
                        }
                    } else if (jsonMsg.getString("cmd").equals("countdown")) {
                        // 各端收到合唱倒计时
                        if (mPlayer == null) return;
                        int time = jsonMsg.getInt("time");
                        playerMusicCountDownLiveData.postValue(time);
                    } else if (jsonMsg.getString("cmd").equals("TrackMode")) {
                        // 伴唱收到原唱伴唱调整指令
                        if (mPlayer == null) return;
                        int TrackMode = jsonMsg.getInt("mode");
                        mPlayer.selectAudioTrack(TrackMode);
                    } else if (jsonMsg.getString("cmd").equals("Seek")) {
                        // 伴唱收到原唱seek指令
                        if (mPlayer == null) return;
                        long position = jsonMsg.getLong("position");
                        mPlayer.seek(position);
                    } else if (jsonMsg.getString("cmd").equals("setVoicePitch")) {
                        // 伴唱收到原唱seek指令
                        if (mPlayer == null) return;
                        double pitch = jsonMsg.getDouble("pitch");
                        if (mPlayer != null && playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
                            playerPitchLiveData.postValue(pitch);
                        }
                    }
                } catch (JSONException exp) {
                    KTVLogger.e(TAG, "onStreamMessage:" + exp.toString());
                }
            }

            @Override
            public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
                // VideoPitch回调, 用于同步各端音准
                RoomSelSongModel songPlaying = songPlayingLiveData.getValue();
                if (songPlaying == null) {
                    return;
                }
                if (Objects.equals(songPlaying.getUserNo(), UserManager.getInstance().getUser().userNo)
                        || Objects.equals(songPlaying.getChorusNo(), UserManager.getInstance().getUser().userNo)) {
                    for (AudioVolumeInfo info : speakers) {
                        if (info.uid == 0 && playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
                            if (mPlayer != null && mPlayer.getState() == io.agora.mediaplayer.Constants.MediaPlayerState.PLAYER_STATE_PLAYING) {
                                playerPitchLiveData.postValue(info.voicePitch);
                                pitch = info.voicePitch;
                            }
                        }
                    }
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
            public void onRejoinChannelSuccess(String channel, int uid, int elapsed) {
                // 断网重连操作，断网重连后刷新麦位状态
                getSeatStatus();
            }

            @Override
            public void onContentInspectResult(int result) {

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
        mRtcEngine.setParameters("{\"rtc.audio.opensl.mode\":0}");
        mRtcEngine.setParameters("{\"rtc.audio_fec\":[3,2]}");
        mRtcEngine.setParameters("{\"rtc.audio_resend\":false}");
        mRtcEngine.setParameters("{\"che.audio.custom_bitrate\":128000}");
        mRtcEngine.setParameters("{\"che.audio.custom_payload_type\":78}");
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
            jsonObject.put("userNo", UserManager.getInstance().getUser().userNo);
            contentInspectConfig.extraInfo = jsonObject.toString();
            ContentInspectConfig.ContentInspectModule module = new ContentInspectConfig.ContentInspectModule();
            module.interval = 30;
            module.type = CONTENT_INSPECT_TYPE_SUPERVISE;
            contentInspectConfig.modules = new ContentInspectConfig.ContentInspectModule[] { module };
            contentInspectConfig.moduleCount = 1;
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
        iAgoraMusicContentCenter.registerEventHandler(new IMusicContentCenterEventHandler() {
            @Override
            public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
                KTVLogger.d(TAG, "onPreLoadEvent percent = " + percent + " status = " + status);
                if (percent == 100) {
                    if (mccNeedPreload && mPlayer != null) {
                        mccNeedPreload = false;
                        mPlayer.open(songCode, 0);
                    }
                }
            }

            @Override
            public void onMusicCollectionResult(String requestId, int status, int page, int pageSize, int total, Music[] list) {
                IMusicContentCenterEventHandler handler = rtcMusicHandlerMap.get(requestId);
                if(handler != null){
                   handler.onMusicCollectionResult(requestId, status, page, pageSize, total, list);
                    rtcMusicHandlerMap.remove(requestId);
                }
            }

            @Override
            public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {
                IMusicContentCenterEventHandler handler = rtcMusicHandlerMap.get(requestId);
                if(handler != null){
                    handler.onMusicChartsResult(requestId, status, list);
                    rtcMusicHandlerMap.remove(requestId);
                }
            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {
                IMusicContentCenterEventHandler handler = rtcMusicHandlerMap.get(requestId);
                if(handler != null){
                    handler.onLyricResult(requestId, lyricUrl);
                    rtcMusicHandlerMap.remove(requestId);
                }
            }
        });

        // ------------------ 初始化音乐播放器实例 ------------------
        mPlayer = iAgoraMusicContentCenter.createMusicPlayer();
        mPlayer.registerPlayerObserver(new IMediaPlayerObserver() {

            private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

            @Override
            public void onPlayerStateChanged(io.agora.mediaplayer.Constants.MediaPlayerState state, io.agora.mediaplayer.Constants.MediaPlayerError error) {
                switch (state) {
                    case PLAYER_STATE_OPEN_COMPLETED:
                        KTVLogger.d(TAG, "musicPlayer PLAYER_STATE_OPEN_COMPLETED");
                        playerMusicOpenDurationLiveData.postValue(mPlayer.getDuration());
                        mPlayer.play();
                        startDisplayLrc();
                        break;
                    case PLAYER_STATE_PLAYING:
                        KTVLogger.d(TAG, "musicPlayer PLAYER_STATE_PLAYING");
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        if (!songPlayingLiveData.getValue().isChorus()) {
                            startSyncLrc(songPlayingLiveData.getValue().getSongNo(), mPlayer.getDuration());
                            startSyncPitch();
                        }
                        break;
                    case PLAYER_STATE_PAUSED:
                        KTVLogger.d(TAG, "musicPlayer PLAYER_STATE_PAUSED");
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
                        break;
                    case PLAYER_STATE_STOPPED:
                        KTVLogger.d(TAG, "musicPlayer PLAYER_STATE_STOPPED");
                        if (mpkNeedStopped && mPlayer != null) {
                            mpkNeedStopped = false;
                            String songNo = songPlayingLiveData.getValue().getSongNo();
                            mPlayer.open(Long.parseLong(songNo), 0);
                        }
                        break;
                    case PLAYER_STATE_FAILED:
                        KTVLogger.e(TAG, "onPlayerStateChanged: failed to play:" + error.toString());
                        break;
                    case PLAYER_STATE_PLAYBACK_COMPLETED:
                    case PLAYER_STATE_PLAYBACK_ALL_LOOPS_COMPLETED:
                        KTVLogger.d(TAG, "onMusicCompleted");
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
                        playerMusicPlayCompleteLiveData.postValue(songPlayingLiveData.getValue().getUserNo());
                        changeMusic();
                        break;
                    default:
                }
            }

            @Override
            public void onPositionChanged(long position_ms) {
                // 本端获取播放位置，用于歌词播放
                // Workaround, delay emit for 350ms
                mExecutor.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
                            mLastRecvPlayPosTime = System.currentTimeMillis();
                            mRecvedPlayPosition = position_ms;
                        }
                    }
                }, 350, TimeUnit.MILLISECONDS);
            }

            @Override
            public void onPlayerEvent(io.agora.mediaplayer.Constants.MediaPlayerEvent eventCode, long elapsedTime, String message) {

            }

            @Override
            public void onMetaData(io.agora.mediaplayer.Constants.MediaPlayerMetadataType type, byte[] data) {

            }

            @Override
            public void onPlayBufferUpdated(long playCachedBuffer) {

            }

            @Override
            public void onPreloadEvent(String src, io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent event) {

            }

            @Override
            public void onAgoraCDNTokenWillExpire() {

            }

            @Override
            public void onPlayerSrcInfoChanged(SrcInfo from, SrcInfo to) {

            }

            @Override
            public void onPlayerInfoUpdated(PlayerUpdatedInfo info) {

            }

            @Override
            public void onAudioVolumeIndication(int volume) {

            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                mExecutor.shutdown();
            }
        });

        // ------------------ 初始化音乐播放设置面版 ------------------
        mSetting = new MusicSettingBean(false, 40, 40, 0, new MusicSettingDialog.Callback() {
            @Override
            public void onEarChanged(boolean isEar) {
                int isMuted = seatLocalLiveData.getValue().isAudioMuted();
                if (isMuted == 1) {
                    isOpnEar = isEar;
                    return;
                }
                mRtcEngine.enableInEarMonitoring(isEar, Constants.EAR_MONITORING_FILTER_NONE);
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
                mRtcEngine.setAudioEffectPreset(getEffectIndex(effect));
            }

            @Override
            public void onBeautifierPresetChanged(int effect) {
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

            @Override
            public void setAudioEffectParameters(int param1, int param2) {
                if (param1 == 0) {
                    mRtcEngine.setAudioEffectParameters(Constants.VOICE_CONVERSION_OFF, param1, param2);
                } else {
                    mRtcEngine.setAudioEffectParameters(Constants.PITCH_CORRECTION, param1, param2);
                }
            }

            @Override
            public void onToneChanged(int newToneValue) {
                mPlayer.setAudioPitch(newToneValue);
            }
        });

        // ------------------ 初始化音量 ------------------
        mPlayer.adjustPlayoutVolume(40);
        mPlayer.adjustPublishSignalVolume(40);
        updateVolumeStatus(false);

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
    private int micVolume = 40;
    private int micOldVolume = 40;

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
        mRtcEngine.adjustRecordingSignalVolume(v);
    }

    // ------------------ 原唱/伴奏 ------------------
    protected int mAudioTrackIndex = 1;
    public boolean musicToggleOriginal() {
        if (mPlayer == null) {
            return false;
        }
        boolean needSendStatus = songPlayingLiveData.getValue().isChorus()
                && songPlayingLiveData.getValue().getUserNo().equals(UserManager.getInstance().getUser().userNo);

        if (true) { // 因为咪咕音乐没有音轨，只有左右声道，所以暂定如此
            if (mAudioTrackIndex == 0) {
                mAudioTrackIndex = 1;
                if (needSendStatus) {
                    // 合唱时 发送状态
                    sendTrackMode(0);
                }
                mPlayer.selectAudioTrack(0);
            } else {
                mAudioTrackIndex = 0;
                if (needSendStatus) {
                    // 合唱时 发送状态
                    sendTrackMode(1);
                }
                mPlayer.selectAudioTrack(1);
            }
            return false;
        } else {
            ToastUtils.showToast(R.string.ktv_error_cut);
            return true;
        }
    }

    // ------------------ 暂停/播放 ------------------
    public void musicToggleStart() {
        if (mPlayer == null) {
            return;
        }
        boolean needSendStatus = songPlayingLiveData.getValue().isChorus()
                && songPlayingLiveData.getValue().getUserNo().equals(UserManager.getInstance().getUser().userNo);
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            if (needSendStatus) {
                // 合唱时 发送状态
                sendPause();
            }
            mPlayer.pause();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            if (needSendStatus) {
                // 合唱时 发送状态
                sendPlay();
            }
            mPlayer.resume();
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
        if (mPlayer != null) {
            playerMusicCountDownLiveData.postValue(time);
            Map<String, Object> msg = new HashMap<>();
            msg.put("cmd", "countdown");
            msg.put("time", time);
            JSONObject jsonMsg = new JSONObject(msg);

            if (streamId == 0) {
                DataStreamConfig cfg = new DataStreamConfig();
                cfg.syncWithAudio = true;
                cfg.ordered = true;
                streamId = mRtcEngine.createDataStream(cfg);
            }
            mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        }
    }

    // ------------------ 歌曲开始播放 ------------------
    public void musicStartPlay(Context context, @NonNull RoomSelSongModel music) {
        KTVLogger.d(TAG, "RoomLivingViewModel.musicStartPlay() called");
        mPlayer.stop();
        stopSyncPitch();
        stopSyncLrc();
        stopDisplayLrc();
        mRecvedPlayPosition = 0;
        mLastRecvPlayPosTime = null;
        mAudioTrackIndex = 1;

        boolean isOwnSong = Objects.equals(music.getUserNo(), UserManager.getInstance().getUser().userNo);
        boolean isChorus = music.isChorus();
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
        if (isChorus) {
            //合唱
            mRtcEngine.muteAllRemoteAudioStreams(false);
            if (music.getChorusNo() == null) {
                // 没有合唱者加入时， 播放等待动画
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_WAIT_CHORUS);

                // 合唱准备
                mainChannelMediaOption.publishCameraTrack = isCameraOpened;
                mainChannelMediaOption.publishMicrophoneTrack = true;
                mainChannelMediaOption.publishCustomAudioTrack = false;
                mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                mainChannelMediaOption.autoSubscribeAudio = true;
                mainChannelMediaOption.autoSubscribeVideo = true;
                mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                mainChannelMediaOption.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                mainChannelMediaOption.publishMediaPlayerAudioTrack = false;
                int ret = mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                KTVLogger.d(TAG, "RoomLivingViewModel.updateChannelMediaOptions() stop publish mpk called: " + ret);
                return;
            } else if (isOwnSong) {
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHORUS_JOINED);
                // 合唱者加入后，点歌者推mpk流
                ChannelMediaOptions options = new ChannelMediaOptions();
                options.publishCameraTrack = false;
                options.publishCustomAudioTrack = false;
                options.enableAudioRecordingOrPlayout = false;
                options.publishMicrophoneTrack = false;
                options.autoSubscribeVideo = false;
                options.autoSubscribeAudio = false;
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                options.publishMediaPlayerAudioTrack = true;

                int ret = mRtcEngine.joinChannelEx(
                        roomInfoLiveData.getValue().getAgoraRTCToken(),
                        new RtcConnection(roomInfoLiveData.getValue().getRoomNo(), (int) (UserManager.getInstance().getUser().id * 10 + 1)),
                        options,
                        new IRtcEngineEventHandler() {
                        }
                );
                KTVLogger.d(TAG, "RoomLivingViewModel.joinChannelEx() called, ret = " + ret);
                // 合唱状态下mute推的mpk流
                mRtcEngine.muteRemoteAudioStream((int) (UserManager.getInstance().getUser().id * 10 + 1), true);
            } else if (music.getChorusNo().equals(UserManager.getInstance().getUser().userNo)) {
                // 合唱者加入后，合唱者mute 点歌者mpk流
                List<RoomSeatModel> seatList = seatListLiveData.getValue();
                RoomSeatModel mainSinger = null;
                if (seatList != null) {
                    for (RoomSeatModel model : seatList) {
                        if (model.getUserNo().equals(songPlayingLiveData.getValue().getUserNo())) {
                            mainSinger = model;
                        }
                    }
                }

                if (mainSinger != null) {
                    int ret = mRtcEngine.muteRemoteAudioStream(Integer.parseInt(mainSinger.getRtcUid()) * 10 + 1, true);
                    KTVLogger.d(TAG, "RoomLivingViewModel.muteRemoteAudioStream() called: " + Integer.parseInt(mainSinger.getRtcUid()) * 10 + 1);
                }
                // 合唱者开始网络测试
                // startNetTestTask();
            }
        } else {
            // 独唱状态
            if (isOwnSong) {
                // 点歌者(演唱者)同时推人声、播放器混流, 停止订阅远端音频流
                mainChannelMediaOption.publishCameraTrack = isCameraOpened;
                mainChannelMediaOption.publishMicrophoneTrack = true;
                mainChannelMediaOption.publishCustomAudioTrack = false;
                mainChannelMediaOption.enableAudioRecordingOrPlayout = true;
                mainChannelMediaOption.autoSubscribeAudio = true;
                mainChannelMediaOption.autoSubscribeVideo = true;
                mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                mainChannelMediaOption.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                mainChannelMediaOption.publishMediaPlayerAudioTrack = true;
                int ret = mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption);
                KTVLogger.d(TAG, "RoomLivingViewModel.updateChannelMediaOptions() publish microphone and mpk mixing called: " + ret);
            }
        }

        ktvServiceProtocol.makeSongDidPlay(music, e -> {
            if (e == null) {
                // success

            } else {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });

        // 准备歌词
        prepareLrc(context, music, isChorus, isOwnSong);
    }

    // ------------------ 歌曲seek ------------------
    public void musicSeek(long time) {
        if (mPlayer != null) {
            if ( songPlayingLiveData.getValue().isChorus()
                    && songPlayingLiveData.getValue().getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    sendMusicPlayerPosition(time);
            }
            mPlayer.seek(time);
        }
    }

    // ------------------ 歌曲结束播放 ------------------
    public void musicStop() {
         KTVLogger.d(TAG, "RoomLivingViewModel.musicStop() called");
        // 列表中无歌曲， 还原状态
        if (mPlayer != null) {
            mPlayer.stop();
            stopSyncPitch();
            stopSyncLrc();
            stopDisplayLrc();
            mRecvedPlayPosition = 0;
            mLastRecvPlayPosTime = null;
            mAudioTrackIndex = 1;
        }

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

    // ------------------ 歌词播放、同步 ------------------
    //主唱同步歌词给其他人
    private boolean mStopSyncLrc = true;
    private Thread mSyncLrcThread;

    //歌词实时刷新
    protected boolean mStopDisplayLrc = true;
    private Thread mDisplayThread;

    private static volatile long mRecvedPlayPosition = 0;//播放器播放position，ms
    private static volatile Long mLastRecvPlayPosTime = null;

    // 歌词播放准备
    private void prepareLrc(Context mContext, @NonNull RoomSelSongModel music, boolean isChorus, boolean isOwnSong) {
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);

        // lyricType -- 0: xml; 1: lrc
        String requestId = iAgoraMusicContentCenter.getLyric(Long.parseLong(music.getSongNo()), 0);
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
                // do nothing
            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {
                if (lyricUrl == null) {
                    KTVLogger.e(TAG, "iAgoraMusicContentCenter.onLyricResult lyricUrl is null");
                    ToastUtils.showToast("lyricUrl is null");
                    playerMusicLrcDataLiveData.postValue(null);
                    preloadMusic(isOwnSong, isChorus, music);
                    return;
                }
                DownloadManager.getInstance().download(mContext, lyricUrl, file -> {
                    if (file.getName().endsWith(".zip")) {
                        ZipUtils.unZipAsync(file.getAbsolutePath(),
                                file.getAbsolutePath().replace(".zip", ""),
                                new ZipUtils.UnZipCallback() {
                                    @Override
                                    public void onFileUnZipped(List<String> unZipFilePaths) {
                                        String xmlPath = "";
                                        for (String path : unZipFilePaths) {
                                            if(path.endsWith(".xml")){
                                                xmlPath = path;
                                                break;
                                            }
                                        }
                                        if(TextUtils.isEmpty(xmlPath)){
                                            ToastUtils.showToast("The xml file not exist!");
                                            return;
                                        }
                                        File xmlFile = new File(xmlPath);

                                        LrcData data = LrcLoadUtils.parse(xmlFile);
                                        playerMusicLrcDataLiveData.postValue(data);
                                        preloadMusic(isOwnSong, isChorus, music);
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        ToastUtils.showToast(e.getMessage());
                                    }
                                });
                    } else {
                        LrcData data = LrcLoadUtils.parse(file);
                        playerMusicLrcDataLiveData.postValue(data);
                        // 歌词加载成功后加载音频资源
                        preloadMusic(isOwnSong, isChorus, music);
                    }
                }, exception -> {
                    ToastUtils.showToast(exception.getMessage());
                });
            }
        });
    }

    private void preloadMusic(boolean isOwnSong, boolean isChorus, @NonNull RoomSelSongModel music) {
        if (isOwnSong || isChorus){
            // 点歌者视角、合唱者视角

            // 加载歌曲
            if (iAgoraMusicContentCenter.isPreloaded(Long.parseLong(music.getSongNo())) != 0) {
                mccNeedPreload = true;
                iAgoraMusicContentCenter.preload(Long.parseLong(music.getSongNo()), null);
            } else {
                mccNeedPreload = false;
                int ret = mPlayer.open(Long.parseLong(music.getSongNo()), 0);
                mpkNeedStopped = ret != 0;
            }
        } else {
            // 听众视角
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
            startDisplayLrc();
        }
    }

    // 开始播放歌词
    private void startDisplayLrc() {
        KTVLogger.d("KTVLiveRoomLog:", "startDisplayLrc");
        mStopDisplayLrc = false;
        mDisplayThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long curTs = 0;
                long curTime;
                long offset;
                while (!mStopDisplayLrc) {
                    if (mLastRecvPlayPosTime != null) {
                        curTime = System.currentTimeMillis();
                        offset = curTime - mLastRecvPlayPosTime;
                        if (offset <= 1000) {
                            curTs = mRecvedPlayPosition + offset;
                            playerMusicPlayPositionChangeLiveData.postValue(curTs);
                        }
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException exp) {
                        break;
                    }
                }
            }
        });
        mDisplayThread.setName("Thread-Display");
        mDisplayThread.start();
    }

    // 停止播放歌词
    private void stopDisplayLrc() {
        mStopDisplayLrc = true;
        if (mDisplayThread != null) {
            try {
                mDisplayThread.join();
            } catch (InterruptedException exp) {
                KTVLogger.d(TAG, "stopDisplayLrc: " + exp.toString());
            }
        }
    }

    // 开始同步歌词
    private void startSyncLrc(String lrcId, long duration) {
        mSyncLrcThread = new Thread(new Runnable() {

            @Override
            public void run() {
                mStopSyncLrc = false;
                while (!mStopSyncLrc /*&& playerMusicStatusLiveData.getValue() >= PlayerMusicStatus.ON_PLAYING*/) {
                    if (mPlayer == null) {
                        break;
                    }
                    if (mLastRecvPlayPosTime != null && playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
                        sendSyncLrc(lrcId, duration, mRecvedPlayPosition);
                    }

                    try {
                        Thread.sleep(999L);
                    } catch (InterruptedException exp) {
                        break;
                    }
                }
            }

            private void sendSyncLrc(String lrcId, long duration, long time) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("cmd", "setLrcTime");
                msg.put("lrcId", lrcId);
                msg.put("duration", duration);
                msg.put("time", time);//ms
                JSONObject jsonMsg = new JSONObject(msg);

                if (streamId == 0) {
                    DataStreamConfig cfg = new DataStreamConfig();
                    cfg.syncWithAudio = true;
                    cfg.ordered = true;
                    streamId = mRtcEngine.createDataStream(cfg);
                }

                int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
                if (ret < 0) {
                    KTVLogger.e(TAG, "sendSyncLrc() sendStreamMessage called returned: " + ret);
                }
            }
        });
        mSyncLrcThread.setName("Thread-SyncLrc");
        mSyncLrcThread.start();
    }

    // 停止同步歌词
    private void stopSyncLrc() {
        mStopSyncLrc = true;
        if (mSyncLrcThread != null) {
            try {
                mSyncLrcThread.join();
            } catch (InterruptedException exp) {
                KTVLogger.e(TAG, "stopSyncLrc: " + exp.toString());
            }
        }
    }

    // ------------------ 音高pitch同步 ------------------
    private Thread mSyncPitchThread;
    private boolean mStopSyncPitch = true;
    private double pitch = 0;
    // 开始同步音高
    private void startSyncPitch() {
        mSyncPitchThread = new Thread(new Runnable() {

            @Override
            public void run() {
                mStopSyncPitch = false;
                while (!mStopSyncPitch) {
                    if (mPlayer == null) {
                        break;
                    }
                    if (mLastRecvPlayPosTime != null && playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
                        sendSyncPitch(pitch);
                    }

                    try {
                        Thread.sleep(999L);
                    } catch (InterruptedException exp) {
                        break;
                    }
                }
            }

            private void sendSyncPitch(double pitch) {
                Map<String, Object> msg = new HashMap<>();
                msg.put("cmd", "setVoicePitch");
                msg.put("pitch", pitch);
                JSONObject jsonMsg = new JSONObject(msg);

                if (streamId == 0) {
                    DataStreamConfig cfg = new DataStreamConfig();
                    cfg.syncWithAudio = true;
                    cfg.ordered = true;
                    streamId = mRtcEngine.createDataStream(cfg);
                }

                int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
                if (ret < 0) {
                    KTVLogger.e(TAG, "sendPitch() sendStreamMessage called returned: " + ret);
                }
            }
        });
        mSyncPitchThread.setName("Thread-SyncPitch");
        mSyncPitchThread.start();
    }

    // 停止同步歌词
    private void stopSyncPitch() {
        mStopSyncPitch = true;
        pitch = 0;
        if (mSyncPitchThread != null) {
            try {
                mSyncPitchThread.join();
            } catch (InterruptedException exp) {
                KTVLogger.e(TAG, "stopSyncPitch: " + exp.toString());
            }
        }
    }

    // ------------------ 合唱网络时间延迟测试(暂未使用) ------------------
    private long offsetTS = 0;
    private long netRtt = 0;
    private long delayWithBrod = 0;
    private boolean mRunNetTask = false;
    private Thread mNetTestThread;

    private void startNetTestTask() {
        mRunNetTask = true;
        mNetTestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunNetTask) {
                    sendTestDelay();
                    try {
                        Thread.sleep(10 * 1000L);
                    } catch (InterruptedException exp) {
                        break;
                    }
                }
            }
        });
        mNetTestThread.setName("Thread-NetTest");
        mNetTestThread.start();
    }

    private void sendReplyTestDelay(long receiveTime) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "replyTestDelay");
        msg.put("testDelayTime", String.valueOf(receiveTime));
        msg.put("time", String.valueOf(System.currentTimeMillis()));
        msg.put("position", mPlayer.getPlayPosition());
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendReplyTestDelay() sendStreamMessage called returned: " + ret);
        }
    }

    private void sendTestDelay() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "testDelay");
        msg.put("time", String.valueOf(System.currentTimeMillis()));
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendTestDelay() sendStreamMessage called returned: " + ret);
        }
    }

    // ------------------ 合唱状态同步 ------------------
    private void sendPause() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "setLrcTime");
        msg.put("time", -1);
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        KTVLogger.d(TAG, "发送多人暂停消息");
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendPause() sendStreamMessage called returned: " + ret);
        }
    }

    private void sendPlay() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "setLrcTime");
        msg.put("time", 0);
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        KTVLogger.d(TAG, "发送多人恢复");
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendPlay() sendStreamMessage called returned: " + ret);
        }
    }

    private void sendTrackMode(int mode) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "TrackMode");
        msg.put("mode", mode);
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendTrackMode() sendStreamMessage called returned: " + ret);
        }
    }

    private void sendMusicPlayerPosition(long position) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "Seek");
        msg.put("position", position);
        JSONObject jsonMsg = new JSONObject(msg);
        if (streamId == 0) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            streamId = mRtcEngine.createDataStream(cfg);
        }
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            KTVLogger.e(TAG, "sendMusicPlayerPosition() sendStreamMessage called returned: " + ret);
        }
    }
}

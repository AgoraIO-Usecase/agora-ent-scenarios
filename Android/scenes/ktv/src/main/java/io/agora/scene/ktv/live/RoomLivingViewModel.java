package io.agora.scene.ktv.live;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.agora.lrcview.LrcLoadUtils;
import io.agora.lrcview.bean.LrcData;
import io.agora.musiccontentcenter.IAgoraMusicPlayer;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.scene.base.api.ApiException;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.ApiSubscriber;
import io.agora.scene.base.api.apiutils.SchedulersUtil;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.bean.Page;
import io.agora.scene.base.bean.PageModel;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.data.model.BaseMusicModel;
import io.agora.scene.base.data.model.MusicModelNew;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.manager.BaseMusicPlayer;
import io.agora.scene.ktv.manager.MultipleMusicPlayer;
import io.agora.scene.ktv.manager.RTCManager;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.ktv.manager.SingleMusicPlayer;
import io.agora.scene.ktv.service.KTVChangeMVCoverInputModel;
import io.agora.scene.ktv.service.KTVChooseSongInputModel;
import io.agora.scene.ktv.service.KTVJoinChorusInputModel;
import io.agora.scene.ktv.service.KTVJoinRoomOutputModel;
import io.agora.scene.ktv.service.KTVMakeSongTopInputModel;
import io.agora.scene.ktv.service.KTVOnSeatInputModel;
import io.agora.scene.ktv.service.KTVOutSeatInputModel;
import io.agora.scene.ktv.service.KTVRemoveSongInputModel;
import io.agora.scene.ktv.service.KTVServiceProtocol;
import io.agora.scene.ktv.service.KTVSwitchSongInputModel;
import io.agora.scene.ktv.service.VLRoomSeatModel;
import io.agora.scene.ktv.service.VLRoomSelSongModel;
import io.agora.scene.ktv.widget.MusicSettingBean;
import io.agora.scene.ktv.widget.MusicSettingDialog;
import io.reactivex.disposables.Disposable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RoomLivingViewModel extends ViewModel {

    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();

    /**
     * 音乐播放器
     */
    private BaseMusicPlayer mMusicPlayer;

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
     * 房间信息
     */
    final MutableLiveData<KTVJoinRoomOutputModel> roomInfoLiveData;
    final MutableLiveData<Boolean> roomDeleteLiveData = new MutableLiveData<>();
    final MutableLiveData<Integer> roomUserCountLiveData = new MutableLiveData<>(0);

    /**
     * 麦位信息
     */
    final MutableLiveData<List<VLRoomSeatModel>> seatListLiveData = new MutableLiveData<>(new ArrayList<>());
    final MutableLiveData<VLRoomSeatModel> seatLocalLiveData = new MutableLiveData<>();

    /**
     * 歌词信息
     */
    public enum SongType {
        HI_SONG(1),
        TICKTOK(2),
        CLASSICAL(3),
        KTV(4);

        final int value;

        SongType(int value) {
            this.value = value;
        }
    }

    final MutableLiveData<List<VLRoomSelSongModel>> songsOrderedLiveData = new MutableLiveData<>();
    final MutableLiveData<VLRoomSelSongModel> songPlayingLiveData = new MutableLiveData<>();

    /**
     * 播放器信息
     */
    enum PlayerMusicStatus {
        ON_PREPARE,
        ON_WAIT_CHORUS,
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


    private final BaseMusicPlayer.Callback playerMusicCallback = new BaseMusicPlayer.Callback() {

        @Override
        public void onPrepareResource() {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
        }

        @Override
        public void onResourceReady(@NonNull MemberMusicModel music) {
            File lrcFile = music.fileLrc;
            LrcData data = LrcLoadUtils.parse(lrcFile);
            playerMusicLrcDataLiveData.postValue(data);
        }

        @Override
        public void onMusicOpening() {
        }

        @Override
        public void onMusicOpenCompleted(long duration) {
            playerMusicOpenDurationLiveData.postValue(duration);
        }

        @Override
        public void onMusicOpenError(int error) {

        }

        @Override
        public void onMusicPlaying() {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
        }

        @Override
        public void onMusicPause() {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
        }

        @Override
        public void onMusicStop() {

        }

        @Override
        public void onMusicCompleted() {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
            playerMusicPlayCompleteLiveData.postValue(RoomManager.getInstance().mMusicModel.userNo);
            Log.d("cwtsw", "onMusicCompleted");
            changeMusic();
        }

        @Override
        public void onMusicPositionChanged(long position) {
            playerMusicPlayPositionChangeLiveData.postValue(position);
        }

        @Override
        public void onReceivedCountdown(int time) {
            playerMusicCountDownLiveData.postValue(time);
        }
    };

    public RoomLivingViewModel(KTVJoinRoomOutputModel roomInfo) {
        this.roomInfoLiveData = new MutableLiveData<>(roomInfo);
    }

    public boolean isRoomOwner() {
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().userNo);
    }

    public void init() {
        RTCManager.getInstance().initRTC();
        RTCManager.getInstance().setRTCEvent((type, data) -> {

        });
        RTCManager.getInstance().joinRTC(
                roomInfoLiveData.getValue().getAgoraRTCToken(),
                roomInfoLiveData.getValue().getRoomNo(),
                UserManager.getInstance().getUser().id,
                isRoomOwner() ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE
        );
        RTCManager.getInstance().initMcc(
                UserManager.getInstance().getUser().id,
                roomInfoLiveData.getValue().getAgoraRTMToken()
        );
        mPlayer = RTCManager.getInstance().createMediaPlayer();

        initRoom();
        initSeats();
        initSongs();
    }

    public void release() {
        RTCManager.getInstance().leaveRTCRoom();
        musicStop();
        if (mPlayer != null) {
            mPlayer.destroy();
            mPlayer = null;
        }
        if (mMusicPlayer != null) {
            mMusicPlayer.unregisterPlayerObserver();
            mMusicPlayer.destroy();
            mMusicPlayer = null;
        }
    }

    // =============== 房间相关 ==========================

    public void initRoom() {
        // RoomManager.getInstance().setAgoraRoom(ktvJoinRoomOutputModel.getData());
        if (isRoomOwner()) {
            RTCManager.getInstance().getRtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        }

        ktvServiceProtocol.subscribeRoomStatusWithChanged((ktvSubscribe, vlRoomListModel) -> {
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {
                roomDeleteLiveData.postValue(true);
            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {
                KTVJoinRoomOutputModel _roomInfo = roomInfoLiveData.getValue();
                roomInfoLiveData.postValue(new KTVJoinRoomOutputModel(
                        _roomInfo.getRoomName(),
                        _roomInfo.getRoomNo(),
                        _roomInfo.getCreatorNo(),
                        vlRoomListModel.getBgOption(),
                        _roomInfo.getSeatsArray(),
                        _roomInfo.getAgoraRTMToken(),
                        _roomInfo.getAgoraRTCToken(),
                        _roomInfo.getAgoraPlayerRTCToken()
                ));
            }
            return null;
        });

        ktvServiceProtocol.subscribeUserListCountWithChanged(count -> {
            roomUserCountLiveData.postValue(count);
            return null;
        });

    }

    /**
     * 退出房间
     */
    public void exitRoom() {
        ktvServiceProtocol.leaveRoomWithCompletion(e -> {
            if (e == null) {
                roomDeleteLiveData.postValue(false);
            } else {
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 设置背景
     */
    public void setMV_BG(int bgPosition) {
        ktvServiceProtocol.changeMVCoverWithInput(new KTVChangeMVCoverInputModel(bgPosition), new Function1<Exception, Unit>() {
            @Override
            public Unit invoke(Exception e) {
                if (e == null) {
                    // success
                    // do nothing for the subscriber will callback the new room info.
                } else {
                    // failure
                    ToastUtils.showToast(e.getMessage());
                }
                return null;
            }
        });
    }

    // ================= 麦位相关 ===========================


    public void initSeats() {
        KTVJoinRoomOutputModel _roomInfo = roomInfoLiveData.getValue();
        if (_roomInfo == null) {
            throw new RuntimeException("The roomInfo muse be not null before initSeats method calling!");
        }
        List<VLRoomSeatModel> seatsArray = _roomInfo.getSeatsArray();
        seatListLiveData.postValue(seatsArray);

        if (seatsArray != null) {
            for (VLRoomSeatModel vlRoomSeatModel : seatsArray) {
                if (vlRoomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    seatLocalLiveData.setValue(vlRoomSeatModel);
                    break;
                }
            }
        }
        if (seatLocalLiveData.getValue() == null) {
            seatLocalLiveData.setValue(null);
        }

        ktvServiceProtocol.subscribeSeatListWithChanged((ktvSubscribe, vlRoomSeatModel) -> {
            if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeCreated) {

                List<VLRoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<VLRoomSeatModel> value = new ArrayList<>(oValue);
                value.add(vlRoomSeatModel);
                seatListLiveData.postValue(value);

                if (vlRoomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    seatLocalLiveData.postValue(vlRoomSeatModel);
                }

            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeUpdated) {

                List<VLRoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<VLRoomSeatModel> value = new ArrayList<>(oValue);
                int index = -1;
                for (VLRoomSeatModel seat : value) {
                    index++;
                    if (seat.getOnSeat() == vlRoomSeatModel.getOnSeat()) {
                        break;
                    }
                }
                if (index != -1) {
                    value.remove(index);
                    value.add(index, vlRoomSeatModel);
                    seatListLiveData.postValue(value);

                    if (vlRoomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                        seatLocalLiveData.postValue(vlRoomSeatModel);
                    }
                }

            } else if (ktvSubscribe == KTVServiceProtocol.KTVSubscribe.KTVSubscribeDeleted) {

                List<VLRoomSeatModel> oValue = seatListLiveData.getValue();
                if (oValue == null) {
                    return null;
                }
                List<VLRoomSeatModel> value = new ArrayList<>(oValue);
                Iterator<VLRoomSeatModel> iterator = value.iterator();
                while (iterator.hasNext()) {
                    VLRoomSeatModel next = iterator.next();
                    if (next.getUserNo().equals(vlRoomSeatModel.getUserNo())) {
                        iterator.remove();
                    }
                }
                seatListLiveData.postValue(value);

                if (vlRoomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                    seatLocalLiveData.postValue(null);
                }


                if (vlRoomSeatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {

                    if (mMusicPlayer != null) {
                        mMusicPlayer.switchRole(Constants.CLIENT_ROLE_AUDIENCE);
                    }
                    RTCManager.getInstance().getRtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);

                    // 合唱相关逻辑
                    if (RoomManager.getInstance().mMusicModel != null
                            && UserManager.getInstance().getUser().userNo.equals(RoomManager.getInstance().mMusicModel.userId)) {
                        //我是合唱
                        RoomManager.getInstance().mMusicModel.isChorus = false;
                        RoomManager.getInstance().mMusicModel.userId = "";
                        RoomManager.getInstance().mMusicModel.setType(MemberMusicModel.SingType.Single);
                        getSongChosenList();
                    } else if (UserManager.getInstance().getUser().userNo.equals(RoomManager.getInstance().mMusicModel.userNo)) {
                        //推送切歌逻辑
                        // val bean2 = RTMMessageBean()
                        // bean2.headUrl = UserManager.getInstance().user.headUrl
                        // bean2.messageType = KtvConstant.MESSAGE_ROOM_TYPE_SWITCH_SONGS
                        // bean2.roomNo = agoraRoom.roomNo
                        // bean2.userNo = UserManager.getInstance().user.userNo
                        // RTMManager.getInstance().sendMessage(gson.toJson(bean2))
                        // getSongOrdersList(true)
                    }
                } else if (RoomManager.getInstance().mMusicModel != null
                        && vlRoomSeatModel.getUserNo().equals(RoomManager.getInstance().mMusicModel.userId)) {
                    // 被房主下麦克的合唱者
                    // RoomManager.getInstance().mMusicModel.isChorus = false;
                    // RoomManager.getInstance().mMusicModel.user1Id = ""
                    // RoomManager.getInstance().mMusicModel.type =
                    //         MemberMusicModel.SingType.Single
                    // getISingleCallback().onSingleCallback(
                    //         KtvConstant.CALLBACK_TYPE_ROOM_SEAT_CHANGE,
                    //         null
                    // );
                }
            }
            return null;
        });
    }

    /**
     * 上麦
     */
    public void haveSeat(int onSeatIndex) {
        ktvServiceProtocol.onSeatWithInput(new KTVOnSeatInputModel(onSeatIndex), new Function1<Exception, Unit>() {
            @Override
            public Unit invoke(Exception e) {
                if (e == null) {
                    // success
                    RTCManager.getInstance().getRtcEngine()
                            .setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
                    if (mMusicPlayer != null) {
                        mMusicPlayer.switchRole(Constants.CLIENT_ROLE_BROADCASTER);
                    }
                } else {
                    // failure
                    ToastUtils.showToast(e.getMessage());
                }
                return null;
            }
        });
    }

    /**
     * 离开麦位
     */
    public void leaveSeat(VLRoomSeatModel seatModel) {
        ktvServiceProtocol.outSeatWithInput(
                new KTVOutSeatInputModel(
                        seatModel.getUserNo(),
                        seatModel.getId(),
                        seatModel.getName(),
                        seatModel.getHeadUrl(),
                        seatModel.getOnSeat()
                ),
                e -> {
                    if (e == null) {
                        // success
                        if (seatModel.isSelfMuted() == 1) {
                            if (seatModel.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
                                toggleMic(0);
                            }
                        }
                    } else {
                        // failure
                        ToastUtils.showToast(e.getMessage());
                    }
                    return null;
                });
    }

    public void toggleSelfVideo(int isVideoMuted) {
        ktvServiceProtocol.openVideoStatusWithStatus(isVideoMuted, e -> {
            if (e == null) {
                // success
                RTCManager.getInstance().getRtcEngine().enableLocalVideo(isVideoMuted == 1);
            } else {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 静音
     *
     * @param isSelfMuted 1 为静音
     */
    public void toggleMic(int isSelfMuted) {
        boolean isUnMute = isSelfMuted == 0;
        ktvServiceProtocol.muteWithMuteStatus(isSelfMuted, e -> {
            if (e == null) {
                // success
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
                ChannelMediaOptions options = new ChannelMediaOptions();
                options.publishMicrophoneTrack = isUnMute;
                RTCManager.getInstance().getRtcEngine().updateChannelMediaOptions(options);
                if (mMusicPlayer != null) {
                    if (isUnMute) {
                        mMusicPlayer.setOldMicVolume();
                    } else {
                        mMusicPlayer.resetVolume();
                    }
                }
            } else {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }


    // ======== 歌曲相关 ============

    public void initSongs() {
        mSetting = new MusicSettingBean(false, 40, 40, 0, new MusicSettingDialog.Callback() {
            @Override
            public void onEarChanged(boolean isEar) {
                if (seatLocalLiveData.getValue().isSelfMuted() == 1) {
                    isOpnEar = isEar;
                    return;
                }
                RTCManager.getInstance().getRtcEngine().enableInEarMonitoring(isEar, Constants.EAR_MONITORING_FILTER_NONE);
            }

            @Override
            public void onMicVolChanged(int vol) {
                mMusicPlayer.setMicVolume(vol);
            }

            @Override
            public void onMusicVolChanged(int vol) {
                mMusicPlayer.setMusicVolume(vol);
            }

            @Override
            public void onEffectChanged(int effect) {
                RTCManager.getInstance().getRtcEngine().setAudioEffectPreset(getEffectIndex(effect));
            }

            @Override
            public void onBeautifierPresetChanged(int effect) {
                switch (effect) {
                    case 0:
                        RTCManager.getInstance().getRtcEngine().setVoiceBeautifierParameters(Constants.VOICE_BEAUTIFIER_OFF, 0, 0);
                    case 1:
                        RTCManager.getInstance().getRtcEngine().setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 1, 2);
                    case 2:
                        RTCManager.getInstance().getRtcEngine().setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 1, 1);
                    case 3:
                        RTCManager.getInstance().getRtcEngine().setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 2, 2);
                    case 4:
                        RTCManager.getInstance().getRtcEngine().setVoiceBeautifierParameters(Constants.SINGING_BEAUTIFIER, 2, 1);
                }
            }

            @Override
            public void setAudioEffectParameters(int param1, int param2) {
                if (param1 == 0) {
                    RTCManager.getInstance().getRtcEngine().setAudioEffectParameters(Constants.VOICE_CONVERSION_OFF, param1, param2);
                } else {
                    RTCManager.getInstance().getRtcEngine().setAudioEffectParameters(Constants.PITCH_CORRECTION, param1, param2);
                }
            }

            @Override
            public void onToneChanged(int newToneValue) {
                mMusicPlayer.setAudioMixingPitch(newToneValue);
            }
        });

        ktvServiceProtocol.subscribeChooseSongWithChanged((ktvSubscribe, songModel) -> {
            getSongChosenList();
            return null;
        });
    }

    /**
     * 获取歌曲列表
     */
    public LiveData<List<VLRoomSelSongModel>> getSongList(SongType type, int page) {
        MutableLiveData<List<VLRoomSelSongModel>> liveData = new MutableLiveData<>();
        ApiManager.getInstance().requestGetSongsList(page, type.value)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<BaseMusicModel>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(BaseResponse<BaseMusicModel> data) {
                                BaseMusicModel musicModel = data.getData();
                                if (musicModel == null) {
                                    return;
                                }
                                List<VLRoomSelSongModel> songs = new ArrayList<>();

                                // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
                                ktvServiceProtocol.getChoosedSongsListWithCompletion((e, songsChosen) -> {
                                    if(e == null && songsChosen != null){
                                        // success
                                        for (MusicModelNew record : musicModel.records) {
                                            VLRoomSelSongModel songItem = null;
                                            for (VLRoomSelSongModel vlRoomSelSongModel : songsChosen) {
                                                if(vlRoomSelSongModel.getSongNo().equals(record.songNo)){
                                                    songItem = vlRoomSelSongModel;
                                                    break;
                                                }
                                            }

                                            if(songItem == null){
                                                songItem = new VLRoomSelSongModel(
                                                        record.songName,
                                                        record.songNo,
                                                        record.songUrl,
                                                        record.singer,
                                                        record.lyric,
                                                        record.status,
                                                        record.imageUrl,

                                                        "", "", "", "", false, 0, 0, 0.0, false, ""
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
                            public void onFailure(@Nullable ApiException t) {
                                if(t != null){
                                    ToastUtils.showToast(t.getMessage());
                                }
                            }
                        }
                );
        return liveData;
    }

    /**
     * 搜索歌曲
     */
    public LiveData<List<VLRoomSelSongModel>> searchSong(String condition) {
        MutableLiveData<List<VLRoomSelSongModel>> liveData = new MutableLiveData<>();
        PageModel pageModel = new PageModel();
        pageModel.page = new Page();
        pageModel.page.current = 1;
        pageModel.page.size = 100;
        pageModel.name = condition;
        ApiManager.getInstance().requestSearchSong(pageModel)
                .compose(SchedulersUtil.INSTANCE.applyApiSchedulers()).subscribe(
                        new ApiSubscriber<BaseResponse<BaseMusicModel>>() {
                            @Override
                            public void onSubscribe(@NonNull Disposable d) {

                            }

                            @Override
                            public void onSuccess(BaseResponse<BaseMusicModel> data) {
                                BaseMusicModel musicModel = data.getData();
                                if (musicModel == null) {
                                    return;
                                }
                                List<VLRoomSelSongModel> songs = new ArrayList<>();

                                // 需要再调一个接口获取当前已点的歌单来补充列表信息 >_<
                                ktvServiceProtocol.getChoosedSongsListWithCompletion((e, songsChosen) -> {
                                    if(e == null && songsChosen != null){
                                        // success
                                        for (MusicModelNew record : musicModel.records) {
                                            VLRoomSelSongModel songItem = null;
                                            for (VLRoomSelSongModel vlRoomSelSongModel : songsChosen) {
                                                if(vlRoomSelSongModel.getSongNo().equals(record.songNo)){
                                                    songItem = vlRoomSelSongModel;
                                                    break;
                                                }
                                            }

                                            if(songItem == null){
                                                songItem = new VLRoomSelSongModel(
                                                        record.songName,
                                                        record.songNo,
                                                        record.songUrl,
                                                        record.singer,
                                                        record.lyric,
                                                        record.status,
                                                        record.imageUrl,

                                                        "", "", "", "", false, 0, 0, 0.0, false, ""
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
                            public void onFailure(@Nullable ApiException t) {
                                if(t != null){
                                    ToastUtils.showToast(t.getMessage());
                                }
                            }
                        }
                );
        return liveData;
    }

    /**
     * 点歌
     */
    public LiveData<Boolean> chooseSong(VLRoomSelSongModel songModel, boolean isChorus) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        if(songModel == null){
            return liveData;
        }
        ktvServiceProtocol.chooseSongWithInput(
                new KTVChooseSongInputModel(isChorus ? 1 : 0,
                        songModel.getSongName(),
                        songModel.getSongNo(),
                        songModel.getSongUrl(),
                        songModel.getSinger(),
                        songModel.getImageUrl()),
                e -> {
                    if (e == null) {
                        // success
                        liveData.postValue(true);
                    } else {
                        // failure
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
    public void deleteSong(VLRoomSelSongModel songModel) {
        if(songModel == null){
            return;
        }
        ktvServiceProtocol.removeSongWithInput(
                new KTVRemoveSongInputModel(songModel.getSongNo(), songModel.getSort(), ""),
                e -> {
                    if (e == null) {
                        // success: do nothing for subscriber dealing with the event already

                    } else {
                        // failure
                        ToastUtils.showToast(e.getMessage());
                    }
                    return null;
                }
        );
    }

    /**
     * 置顶歌曲
     */
    public void topUpSong(VLRoomSelSongModel songModel){
        if(songModel == null){
            return;
        }
        ktvServiceProtocol.makeSongTopWithInput(new KTVMakeSongTopInputModel(
                songModel.getSongNo(),
                songModel.getSort(),
                ""
        ), e -> {
            if(e == null){
                // success: do nothing for subscriber dealing with the event already
            }else{
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 获取已点列表
     */
    public void getSongChosenList() {
        ktvServiceProtocol.getChoosedSongsListWithCompletion((e, data) -> {
            if (e == null && data != null) {
                // success
                songsOrderedLiveData.postValue(data);

            } else {
                // failed
                if (e != null) {
                    ToastUtils.showToast(e.getMessage());
                }
            }
            return null;

        });
    }

    /**
     * 开始切歌
     */
    public void changeMusic() {
        Log.d("cwtsw", "changeMusic 切歌");
        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        if (mRoom == null) {
            return;
        }

        VLRoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }

        if (mMusicPlayer != null) {
            mMusicPlayer.selectAudioTrack(1);
            mMusicPlayer.stop();
        }
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_START);

        ktvServiceProtocol.switchSongWithInput(new KTVSwitchSongInputModel(
                UserManager.getInstance().getUser().userNo,
                musicModel.getSongNo(),
                mRoom.roomNo
        ), e -> {
            if (e == null) {
                // success
                VLRoomSelSongModel songPlaying = songPlayingLiveData.getValue();
                List<VLRoomSelSongModel> orderedSongs = songsOrderedLiveData.getValue();

                if (orderedSongs == null) {
                    return null;
                }

                if (songPlaying != null) {
                    Iterator<VLRoomSelSongModel> iterator = orderedSongs.iterator();
                    while (iterator.hasNext()) {
                        VLRoomSelSongModel next = iterator.next();
                        if (next.getSongNo().equals(songPlaying.getSongNo())) {
                            iterator.remove();
                            break;
                        }
                    }
                }

                songsOrderedLiveData.postValue(new ArrayList<>(orderedSongs));
                songPlayingLiveData.postValue(orderedSongs.size() > 0 ? orderedSongs.get(0) : null);
            } else {
                // failed
                ToastUtils.showToast(e.getMessage());
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_END);
            }
            return null;
        });
    }


    public void musicStartPlay(Context context, VLRoomSelSongModel music) {
        musicStop();
        int role;
        if (music.getUserNo().equals(UserManager.getInstance().getUser().userNo)) {
            role = Constants.CLIENT_ROLE_BROADCASTER;
        } else {
            role = Constants.CLIENT_ROLE_AUDIENCE;
        }
        if (music.isChorus() || music.getStatus() == 2) {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
            mMusicPlayer = new SingleMusicPlayer(context, role, mPlayer);
        } else {
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_WAIT_CHORUS);
            mMusicPlayer = new MultipleMusicPlayer(context, role, mPlayer);
        }
        mMusicPlayer.switchRole(Constants.CLIENT_ROLE_BROADCASTER);
        mMusicPlayer.registerPlayerObserver(playerMusicCallback);
        mMusicPlayer.prepare(music.toMemberMusicModel());
    }


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

    /**
     * 音乐停止
     */
    public void musicStop() {
        if (mMusicPlayer != null) {
            mMusicPlayer.stop();
            mMusicPlayer.destroy();
        }
    }

    public void musicCountDown(int time) {
        if (mMusicPlayer != null) {
            mMusicPlayer.sendCountdown(time);
        }
    }

    public void musicSeek(long time) {
        if (mMusicPlayer != null) {
            mMusicPlayer.seek(time);
        }
    }

    public boolean musicToggleOriginal() {
        if (mMusicPlayer == null) {
            return false;
        }
        if (mMusicPlayer.hasAccompaniment()) {
            mMusicPlayer.toggleOrigle();
            return false;
        } else {
            ToastUtils.showToast(R.string.ktv_error_cut);
            return true;
        }
    }

    public void musicToggleStart() {
        if (mMusicPlayer == null) {
            return;
        }
        mMusicPlayer.togglePlay();
    }

    /**
     * 点击加入合唱
     */
    public void joinChorus() {
        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        if (mRoom == null) {
            return;
        }
        VLRoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }
//        User mUser = UserManager.getInstance().getUser();
//        if (roomInfoLiveData.getValue().getCreatorNo().equals(RoomManager.mMine.userNo)) {
//            RoomManager.mMine.role = AgoraMember.Role.Owner;
//        }
//        if (RoomManager.mMine.role == AgoraMember.Role.Listener) {
//            ToastUtils.showToast(R.string.ktv_need_up);
//            return;
//        }

        ktvServiceProtocol.joinChorusWithInput(new KTVJoinChorusInputModel(musicModel.getSongNo()), e -> {
            if (e == null) {
                // success
                mMusicPlayer.switchRole(Constants.CLIENT_ROLE_BROADCASTER);
            } else {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    public void leaveChorus(Context context) {
        VLRoomSelSongModel music = songPlayingLiveData.getValue();
        if (music == null) {
            return;
        }
        musicStartPlay(context, music);
        ktvServiceProtocol.becomeSolo();
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
}

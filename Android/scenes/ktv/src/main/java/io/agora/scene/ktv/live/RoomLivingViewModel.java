package io.agora.scene.ktv.live;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.agora.lrcview.LrcLoadUtils;
import io.agora.lrcview.bean.LrcData;
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
import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.api.ApiException;
import io.agora.scene.base.api.ApiManager;
import io.agora.scene.base.api.ApiSubscriber;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.apiutils.SchedulersUtil;
import io.agora.scene.base.api.base.BaseResponse;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.bean.Page;
import io.agora.scene.base.bean.PageModel;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.data.model.AgoraMember;
import io.agora.scene.base.data.model.BaseMusicModel;
import io.agora.scene.base.data.model.MusicModelNew;
import io.agora.scene.base.event.NetWorkEvent;
import io.agora.scene.base.event.PreLoadEvent;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.R;
import io.agora.scene.ktv.manager.BaseMusicPlayer;
import io.agora.scene.ktv.manager.MultipleMusicPlayer;
import io.agora.scene.ktv.manager.RTCManager;
import io.agora.scene.ktv.manager.RTMManager;
import io.agora.scene.ktv.manager.ResourceManager;
import io.agora.scene.ktv.manager.RoomManager;
import io.agora.scene.ktv.manager.SingleMusicPlayer;
import io.agora.scene.ktv.manager.bean.RTCMessageBean;
import io.agora.scene.ktv.manager.bean.RTMMessageBean;
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
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RoomLivingViewModel extends ViewModel {

    private final KTVServiceProtocol ktvServiceProtocol = KTVServiceProtocol.Companion.getImplInstance();

    /**
     * 用户状态
     */
    public enum Role {
        Listener(0),
        Owner(1),
        Speaker(2);
        final int value;

        Role(int value) {
            this.value = value;
        }
    }
    private Role role = Role.Listener;

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
     * Player/RTC信息
     */
    int streamId;
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

    final MutableLiveData<Double> playerPitchLiveData = new MutableLiveData<>();
    final MutableLiveData<NetWorkEvent> networkStatusLiveData = new MutableLiveData<>();

    /**
     * Rtc引擎
     */
    private RtcEngineEx mRtcEngine;
    /**
     * 音乐播放器
     */
    private BaseMusicPlayer mMusicPlayer;
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

    public RoomLivingViewModel(KTVJoinRoomOutputModel roomInfo) {
        this.roomInfoLiveData = new MutableLiveData<>(roomInfo);
    }

    public boolean isRoomOwner() {
        return roomInfoLiveData.getValue().getCreatorNo().equals(UserManager.getInstance().getUser().userNo);
    }

    public void init() {
        if (isRoomOwner()) {
            role = Role.Owner;
        }
        initRTCPlayer();
        initRoom();
        initSeats();
        initSongs();
    }

    public void release() {
        streamId = 0;
        mRtcEngine.enableInEarMonitoring(false, Constants.EAR_MONITORING_FILTER_NONE);
        mRtcEngine.leaveChannel();
        mPlayer.stop();
        if (mPlayer != null) {
            mPlayer.destroy();
            mPlayer = null;
        }
        if (iAgoraMusicContentCenter != null) {
            IAgoraMusicContentCenter.destroy();
            iAgoraMusicContentCenter = null;
        }
        if (mRtcEngine != null) {
            RtcEngineEx.destroy();
            mRtcEngine = null;
        }
    }

    // =============== 房间相关 ==========================

    public void initRoom() {
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
                    mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);

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
                    role = Role.Speaker;
                    mRtcEngine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER);

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
                        role = Role.Listener;
                        mRtcEngine.setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
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
                mRtcEngine.enableLocalVideo(isVideoMuted == 1);
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
                mRtcEngine.updateChannelMediaOptions(options);
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
        ktvServiceProtocol.subscribeChooseSongWithChanged((ktvSubscribe, songModel) -> {
            getSongChosenList();
            return null;
        });

        getSongChosenList();
    }

    /**
     * 获取歌曲列表
     */
    public LiveData<List<VLRoomSelSongModel>> getSongList(SongType type, int page) {
        // TODO 改成从RTC中获取歌曲列表
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
        // TODO 改成从RTC中搜索歌曲
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

                if(data.size() > 0){
                    VLRoomSelSongModel value = songPlayingLiveData.getValue();
                    if(value == null){
                        songPlayingLiveData.postValue(data.get(0));
                    }
                }

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
     * 点击加入合唱
     */
    public void joinChorus() {
        VLRoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }
        if (!TextUtils.isEmpty(musicModel.getChorusNo())) {
            return;
        }

        ktvServiceProtocol.joinChorusWithInput(new KTVJoinChorusInputModel(musicModel.getSongNo()), e -> {
            if (e == null) {
                songPlayingLiveData.postValue(new VLRoomSelSongModel(
                        musicModel.getSongName(),
                        musicModel.getSongNo(),
                        musicModel.getSongUrl(),
                        musicModel.getSinger(),
                        musicModel.getLyric(),
                        musicModel.getStatus(),

                        musicModel.getImageUrl(),
                        musicModel.getUserNo(),
                        musicModel.getUserId(),
                        musicModel.getName(),
                        UserManager.getInstance().getUser().userNo,
                        true,
                        musicModel.isOriginal(),
                        musicModel.getSort(),

                        musicModel.getScore(),
                        musicModel.isOwnSong(),

                        musicModel.getObjectId()
                ));
                // success
                mMusicPlayer.switchRole(Constants.CLIENT_ROLE_BROADCASTER);
            } else {
                // failure
                ToastUtils.showToast(e.getMessage());
            }
            return null;
        });
    }

    /**
     * 退出合唱
     */
    public void leaveChorus(Context context) {
        VLRoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null || TextUtils.isEmpty(musicModel.getChorusNo())) {
            return;
        }
        musicStartPlay(context, musicModel);
        ktvServiceProtocol.becomeSolo();

        songPlayingLiveData.postValue(new VLRoomSelSongModel(
                musicModel.getSongName(),
                musicModel.getSongNo(),
                musicModel.getSongUrl(),
                musicModel.getSinger(),
                musicModel.getLyric(),
                musicModel.getStatus(),

                musicModel.getImageUrl(),
                musicModel.getUserNo(),
                musicModel.getUserId(),
                musicModel.getName(),
                "",
                false,
                musicModel.isOriginal(),
                musicModel.getSort(),

                musicModel.getScore(),
                musicModel.isOwnSong(),

                musicModel.getObjectId()
        ));
    }

    /**
     * 开始切歌
     */
    public void changeMusic() {
        Log.d("cwtsw", "changeMusic 切歌");
        VLRoomSelSongModel musicModel = songPlayingLiveData.getValue();
        if (musicModel == null) {
            return;
        }
        if (mPlayer != null) {
            mPlayer.setAudioDualMonoMode(2);
            mPlayer.stop();
        }
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_CHANGING_START);

        ktvServiceProtocol.switchSongWithInput(new KTVSwitchSongInputModel(
                UserManager.getInstance().getUser().userNo,
                musicModel.getSongNo(),
                roomInfoLiveData.getValue().getRoomNo()
        ), e -> {
            if (e == null) {
                // success
                List<VLRoomSelSongModel> orderedSongs = songsOrderedLiveData.getValue();

                if (orderedSongs == null) {
                    return null;
                }

                Iterator<VLRoomSelSongModel> iterator = orderedSongs.iterator();
                while (iterator.hasNext()) {
                    VLRoomSelSongModel next = iterator.next();
                    if (next.getSongNo().equals(musicModel.getSongNo())) {
                        iterator.remove();
                        break;
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

    // ============ Player/RTC/MPK相关 =====================

    private void initRTCPlayer(){
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw new NullPointerException("please check \"strings_config.xml\"");
        }
        if (mRtcEngine != null) {
            return;
        }
        // --------- 初始化RTC ---------
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = AgoraApplication.the();
        config.mAppId = BuildConfig.AGORA_APP_ID;
        config.mEventHandler = new IRtcEngineEventHandler() {

            @Override
            public void onConnectionStateChanged(int state, int reason) {
                super.onConnectionStateChanged(state, reason);
                //mLoggerRTC.d("onConnectionStateChanged() called with: state = [%s], reason = [%s]", state, reason);
                if (state == Constants.CONNECTION_STATE_FAILED) {

                }
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                //mLoggerRTC.i("onJoinChannelSuccess() called with: channel = [%s], uid = [%s], elapsed = [%s]", channel, uid, elapsed);
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                //mLoggerRTC.i("onLeaveChannel() called with: stats = [%s]", stats);
            }

//        private long receiveMessageTime = 0L;

            @Override
            public void onStreamMessage(int uid, int streamId, byte[] data) {
                JSONObject jsonMsg;
                try {
                    String strMsg = new String(data);
                    jsonMsg = new JSONObject(strMsg);
                    //Log.d("收到BaseMusicPlayer消息 = " + strMsg + " streamId = " + streamId);
                    if (jsonMsg.getString("cmd").equals("setLrcTime")) {
                        long position = jsonMsg.getLong("time");
                        if (position == 0) {
                            //mHandler.obtainMessage(ACTION_ON_RECEIVED_PLAY, uid).sendToTarget();
                        } else if (position == -1) {
                            //mHandler.obtainMessage(ACTION_ON_RECEIVED_PAUSE, uid).sendToTarget();
                        } else {
//                            Bundle bundle = new Bundle();
//                            bundle.putInt("uid", uid);
//                            bundle.putLong("time", position);
//                            Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_SYNC_TIME);
//                            message.setData(bundle);
//                            message.sendToTarget();
                            mRecvedPlayPosition = position;
                            mLastRecvPlayPosTime = System.currentTimeMillis();
                        }
                    } else if (jsonMsg.getString("cmd").equals("countdown")) {
//                        int time = jsonMsg.getInt("time");
//                        Bundle bundle = new Bundle();
//                        bundle.putInt("uid", uid);
//                        bundle.putInt("time", time);
//                        Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_COUNT_DOWN);
//                        message.setData(bundle);
//                        message.sendToTarget();
                    } else if (jsonMsg.getString("cmd").equals("testDelay")) {
                        // TODO 添加条件
                        long time = jsonMsg.getLong("time");
                        sendReplyTestDelay(time);
                    } else if (jsonMsg.getString("cmd").equals("replyTestDelay")) {
                        // TODO 添加条件
                        long testDelayTime = jsonMsg.getLong("testDelayTime");
                        long time = jsonMsg.getLong("time");
                        long position = jsonMsg.getLong("position");
                        long localTs = System.currentTimeMillis();
                        netRtt = (localTs - testDelayTime) / 2;
                        delayWithBrod = position + netRtt;

                        long localPos = mPlayer.getPlayPosition();
                        long diff = localPos - delayWithBrod;
                        if (Math.abs(diff) > 40) {
                            //mLogger.d("xn123 seek= [%s], remotePos = [%s], localPos = [%s]", delayWithBrod, position, localPos);
                            mPlayer.seek(delayWithBrod);
                        }
                    } else if (jsonMsg.getString("cmd").equals("TrackMode")) {
//                        int mode = jsonMsg.getInt("mode");
//                        Bundle bundle = new Bundle();
//                        bundle.putInt("uid", uid);
//                        bundle.putInt("mode", mode);
//                        Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_CHANGED_ORIGLE);
//                        message.setData(bundle);
//                        message.sendToTarget();
                    }
                } catch (JSONException exp) {
                    //mLogger.e("onStreamMessage: failed parse json, error: " + exp.toString());
                }
            }

            @Override
            public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
                for (AudioVolumeInfo info : speakers) {
                    if (info.uid == 0 && info.voicePitch > 0) {
                        //RoomManager.getInstance().mMainThreadDispatch.onLocalPitch(info.voicePitch);
                        playerPitchLiveData.postValue(info.voicePitch);

                        if (RoomManager.getInstance().mMusicModel != null && !RoomManager.getInstance().mMusicModel.isChorus) {
                            RTMMessageBean bean = new RTMMessageBean();
                            bean.headUrl = UserManager.getInstance().getUser().headUrl;
                            bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_SYNCHRO_PITCH;
                            bean.roomNo = RoomManager.mRoom.roomNo;
                            bean.pitch = info.voicePitch;
                            RTMManager.getInstance().sendMessage(GsonUtils.Companion.getGson().toJson(bean));
                        }
                    }
                }
            }

            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                super.onNetworkQuality(uid, txQuality, rxQuality);
                if(uid == UserManager.getInstance().getUser().id) {
                    networkStatusLiveData.postValue(new NetWorkEvent(txQuality, rxQuality));
                }
            }
        };
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS;
        try {
            mRtcEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            //mLoggerRTC.e("init error", e);
        }
        mRtcEngine.loadExtensionProvider("agora_drm_loader");
        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_GAME_STREAMING);

//        RTCManager.getInstance().setRTCEvent((type, data) -> {
//
//        });

        // --------- 加入频道 -----------
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        mRtcEngine.enableAudio();
        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO);
        mRtcEngine.enableAudioVolumeIndication(30, 10, true);
        mRtcEngine.setParameters("{\"rtc.audio.opensl.mode\":0}");
        mRtcEngine.setParameters("{\"rtc.audio_fec\":[3,2]}");
        mRtcEngine.setParameters("{\"rtc.audio_resend\":false}");
        mRtcEngine.setClientRole(role == Role.Owner ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE);
        int ret = mRtcEngine.joinChannel(
                roomInfoLiveData.getValue().getAgoraRTCToken(),
                roomInfoLiveData.getValue().getRoomNo(),
                null,
                UserManager.getInstance().getUser().id.intValue()
        );
        if (ret != Constants.ERR_OK) {
            //mLoggerRTC.e("joinRTC() called error " + ret);
        } else {
            //mRTCEvent.onSingleCallback(RTC_TYPE_INIT_SUCCESS, null);
        }

        // --------- 初始化内容中心 ---------
        MusicContentCenterConfiguration contentCenterConfiguration
                = new MusicContentCenterConfiguration();
        contentCenterConfiguration.appId = BuildConfig.AGORA_APP_ID;
        contentCenterConfiguration.mccUid = UserManager.getInstance().getUser().id;
        contentCenterConfiguration.rtmToken = roomInfoLiveData.getValue().getAgoraRTMToken();
        iAgoraMusicContentCenter = IAgoraMusicContentCenter.create(mRtcEngine);
        iAgoraMusicContentCenter.initialize(contentCenterConfiguration);
        iAgoraMusicContentCenter.registerEventHandler(new IMusicContentCenterEventHandler() {
            @Override
            public void onPreLoadEvent(long songCode, int percent, int status, String msg, String lyricUrl) {
                if (percent == 100) {
                    Log.d("cwtsw", "多人 percent = " + percent + " status = " + status);
                    EventBus.getDefault().post(new PreLoadEvent());
                }
            }

            @Override
            public void onMusicCollectionResult(String requestId, int status, int page, int pageSize, int total, Music[] list) {

            }

            @Override
            public void onMusicChartsResult(String requestId, int status, MusicChartInfo[] list) {

            }

            @Override
            public void onLyricResult(String requestId, String lyricUrl) {

            }
        });

        //获取音乐播放器实例
        mPlayer = iAgoraMusicContentCenter.createMusicPlayer();
        mPlayer.registerPlayerObserver(new IMediaPlayerObserver() {
            @Override
            public void onPlayerStateChanged(io.agora.mediaplayer.Constants.MediaPlayerState state, io.agora.mediaplayer.Constants.MediaPlayerError error) {
                switch (state) {
                    case PLAYER_STATE_OPENING:
                        //onMusicOpening();
                        break;
                    case PLAYER_STATE_OPEN_COMPLETED:
                        playerMusicOpenDurationLiveData.postValue(mPlayer.getDuration());
                        mPlayer.play();
                        startDisplayLrc();
                        break;
                    case PLAYER_STATE_PLAYING:
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                        startSyncLrc(songPlayingLiveData.getValue().getSongNo(), mPlayer.getDuration());
                        break;
                    case PLAYER_STATE_PAUSED:
                        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
                        //onMusicPause();
                        break;
                    case PLAYER_STATE_STOPPED:
                        stopSyncLrc();
                        stopDisplayLrc();
                        mRecvedPlayPosition = 0;
                        mLastRecvPlayPosTime = null;
//                        mMusicModel = null;
                        mAudioTrackIndex = 1;
//                        mStatus = BaseMusicPlayer.Status.IDLE;
                        break;
                    case PLAYER_STATE_FAILED:
                        //onMusicOpenError(io.agora.mediaplayer.Constants.MediaPlayerError.getValue(error));
                        //mLogger.e("onPlayerStateChanged: failed to play, error " + error);
                        break;
                    default:
                }
            }

            @Override
            public void onPositionChanged(long position_ms) {
                mLastRecvPlayPosTime = System.currentTimeMillis();
                mRecvedPlayPosition = position_ms;
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
            public void onCompleted() {
                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
                playerMusicPlayCompleteLiveData.postValue(RoomManager.getInstance().mMusicModel.userNo);
                Log.d("cwtsw", "onMusicCompleted");
                changeMusic();

                mPlayer.stop();
                stopDisplayLrc();
                stopSyncLrc();
                mRecvedPlayPosition = 0;
                mLastRecvPlayPosTime = null;
                mAudioTrackIndex = 1;
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
        });

        mSetting = new MusicSettingBean(false, 40, 40, 0, new MusicSettingDialog.Callback() {
            @Override
            public void onEarChanged(boolean isEar) {
                if (seatLocalLiveData.getValue().isSelfMuted() == 1) {
                    isOpnEar = isEar;
                    return;
                }
                mRtcEngine.enableInEarMonitoring(isEar, Constants.EAR_MONITORING_FILTER_NONE);
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
                mMusicPlayer.setAudioMixingPitch(newToneValue);
            }
        });
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

    public void musicStartPlay(Context context, VLRoomSelSongModel music) {
        //musicStop();
        boolean isOwnSong = music.getUserNo().equals(UserManager.getInstance().getUser().userNo);
        boolean isChorus = music.isChorus() || music.getStatus() == 2;
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
        if (isChorus) {
            //合唱
            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_WAIT_CHORUS);
            if (isOwnSong) {
                // 点歌者推mpk流
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

                mRtcEngine.joinChannelEx(
                        roomInfoLiveData.getValue().getAgoraRTCToken(),
                        new RtcConnection(roomInfoLiveData.getValue().getRoomNo(), 12345),
                        options,
                        null
                );
                mRtcEngine.muteRemoteAudioStream(12345, true);

            } else {
                // 合唱者mute 远端mpk流
                mRtcEngine.muteRemoteAudioStream(12345, true);
                // 合唱者开始网络测试
                startNetTestTask();
            }

        } else {
            // 独唱状态 同时推人声、播放器混流
            if (role == Role.Owner || role == Role.Speaker) {
                ChannelMediaOptions options = new ChannelMediaOptions();
                options.publishCameraTrack = false;
                options.publishCustomAudioTrack = false;
                options.enableAudioRecordingOrPlayout = false;
                options.autoSubscribeVideo = false;
                if (isOwnSong) {
                    options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                    options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                    options.publishMediaPlayerAudioTrack = true;
                    options.autoSubscribeAudio = false;
                } else {
                    options.autoSubscribeAudio = true;
                }
                mRtcEngine.updateChannelMediaOptions(options);
            }
        }

        // 准备歌词
        prepareLrc(context, music.toMemberMusicModel(), isChorus, isOwnSong);

//        mMusicPlayer.switchRole(Constants.CLIENT_ROLE_BROADCASTER);
//        mMusicPlayer.registerPlayerObserver(new BaseMusicPlayer.Callback() {
//
//            @Override
//            public void onPrepareResource() {
//                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
//            }
//
//            @Override
//            public void onResourceReady(@NonNull MemberMusicModel music) {
//                File lrcFile = music.fileLrc;
//                LrcData data = LrcLoadUtils.parse(lrcFile);
//                playerMusicLrcDataLiveData.postValue(data);
//            }
//
//            @Override
//            public void onMusicOpening() {
//            }
//
//            @Override
//            public void onMusicOpenCompleted(long duration) {
//                playerMusicOpenDurationLiveData.postValue(duration);
//            }
//
//            @Override
//            public void onMusicOpenError(int error) {
//
//            }
//
//            @Override
//            public void onMusicPlaying() {
//                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
//            }
//
//            @Override
//            public void onMusicPause() {
//                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PAUSE);
//            }
//
//            @Override
//            public void onMusicStop() {
//
//            }
//
//            @Override
//            public void onMusicCompleted() {
//                playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_LRC_RESET);
//                playerMusicPlayCompleteLiveData.postValue(RoomManager.getInstance().mMusicModel.userNo);
//                Log.d("cwtsw", "onMusicCompleted");
//                changeMusic();
//            }
//
//            @Override
//            public void onMusicPositionChanged(long position) {
//                playerMusicPlayPositionChangeLiveData.postValue(position);
//            }
//
//            @Override
//            public void onReceivedCountdown(int time) {
//                playerMusicCountDownLiveData.postValue(time);
//            }
//        });
        //mMusicPlayer.prepare(music.toMemberMusicModel());
    }

    // 倒计时
    public void musicCountDown(int time) {
        if (mPlayer != null) {
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

    public void musicSeek(long time) {
        if (mPlayer != null) {
            mPlayer.seek(time);
        }
    }

    // 原唱/伴奏
    protected int mAudioTrackIndex = 1;
    public boolean musicToggleOriginal() {
        if (mPlayer == null) {
            return false;
        }
        if (true) { // 因为咪咕音乐没有音轨，只有左右声道，所以暂定如此
            //mMusicPlayer.toggleOrigle();
            if (mAudioTrackIndex == 0) {
                mAudioTrackIndex = 1;
                mPlayer.setAudioDualMonoMode(2);
            } else {
                mAudioTrackIndex = 0;
                mPlayer.setAudioDualMonoMode(1);
            }
            return false;
        } else {
            ToastUtils.showToast(R.string.ktv_error_cut);
            return true;
        }
    }

    // 暂停/播放
    public void musicToggleStart() {
        if (mPlayer == null) {
            return;
        }
        //mMusicPlayer.togglePlay();
        if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PLAYING) {
            mPlayer.pause();
        } else if (playerMusicStatusLiveData.getValue() == PlayerMusicStatus.ON_PAUSE) {
            mPlayer.resume();
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

    //主唱同步歌词给其他人
    private boolean mStopSyncLrc = true;
    private Thread mSyncLrcThread;

    //歌词实时刷新
    protected boolean mStopDisplayLrc = true;
    private Thread mDisplayThread;

    private static volatile long mRecvedPlayPosition = 0;//播放器播放position，ms
    private static volatile Long mLastRecvPlayPosTime = null;

    // ----------------------- 歌词播放、同步 -----------------------
    // 歌词播放准备
    private void prepareLrc(Context mContext,  @NonNull MemberMusicModel music, boolean isChorus, boolean isOwnSong) {
        playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PREPARE);
        // TODO 判断条件
        if (isOwnSong || isChorus) {
            // 点歌者视角、合唱者视角
            ResourceManager.Instance(mContext)
                    .download(music, true)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<MemberMusicModel>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull MemberMusicModel musicModel) {
                            //onResourceReady(musicModel);
                            File lrcFile = music.fileLrc;
                            LrcData data = LrcLoadUtils.parse(lrcFile);
                            playerMusicLrcDataLiveData.postValue(data);
                            RoomManager.getInstance().mMusicModel.fileLrc = musicModel.fileLrc; //TODO ？
                            if (iAgoraMusicContentCenter.isPreloaded(Long.parseLong(musicModel.songNo)) != 0) {
                                iAgoraMusicContentCenter.preload(Long.parseLong(musicModel.songNo), null);
                            }
                            mPlayer.open(Long.parseLong(musicModel.songNo), 0);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(io.agora.scene.base.R.string.ktv_lrc_load_fail);
                        }
                    });
        } else {
            // 听众视角
            ResourceManager.Instance(mContext)
                    .download(music, true)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<MemberMusicModel>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull MemberMusicModel musicModel) {
//                            onResourceReady(musicModel);
//                            onMusicPlaingByListener();
//                            playByListener(musicModel);
                            File lrcFile = music.fileLrc;
                            LrcData data = LrcLoadUtils.parse(lrcFile);
                            playerMusicLrcDataLiveData.postValue(data);
                            playerMusicStatusLiveData.postValue(PlayerMusicStatus.ON_PLAYING);
                            startDisplayLrc(); // TODD 听众stop播放LRC的时机？？
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(io.agora.scene.base.R.string.ktv_lrc_load_fail);
                        }
                    });
        }
    }

    // 开始播放歌词
    private void startDisplayLrc() {
        if (mDisplayThread != null) return;
        Log.d("cwtsw", "startDisplayLrc");
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
                            //mHandler.obtainMessage(ACTION_UPDATE_TIME, curTs).sendToTarget();
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
                //mLogger.e("stopDisplayLrc: " + exp.getMessage());
            }
        }
    }

    // 开始同步歌词
    private void startSyncLrc(String lrcId, long duration) {
        mSyncLrcThread = new Thread(new Runnable() {

            @Override
            public void run() {
                //mLogger.i("startSyncLrc: " + lrcId);
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
                    //mLogger.e("sendSyncLrc() sendStreamMessage called returned: ret = [%s]", ret);
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
                //mLogger.e("stopSyncLrc: " + exp.getMessage());
            }
        }
    }

    // ----------------------- 合唱网络时间延迟测试 -----------------------
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

    public void sendReplyTestDelay(long receiveTime) {
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
            //mLogger.e("sendReplyTestDelay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendTestDelay() {
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
            //mLogger.e("sendTestDelay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendStartPlay() {
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
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            //mLogger.e("sendStartPlay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendPause() {
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
        Log.d("cwtsw", "发送多人暂停消息");
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            //mLogger.e("sendPause() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendPlay() {
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
        Log.d("cwtsw", "发送多人恢复");
        int ret = mRtcEngine.sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            //mLogger.e("sendPause() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }
}

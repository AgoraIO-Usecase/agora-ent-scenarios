package io.agora.scene.ktv.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.ObjectsCompat;

import io.agora.musiccontentcenter.IAgoraMusicPlayer;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.R;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.data.model.AgoraMember;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SingleMusicPlayer extends BaseMusicPlayer {

    public SingleMusicPlayer(Context mContext, int role, IAgoraMusicPlayer mPlayer) {
        super(mContext, role, mPlayer);
        RTCManager.getInstance().getRtcEngine().setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO);
    }

    @Override
    public void destroy() {
        super.destroy();
        leaveChannelEx();
    }

    private void leaveChannelEx() {
        if (mRtcConnection == null) {
            return;
        }
        mLogger.d("leaveChannelEX() called");
        RTCManager.getInstance().getRtcEngine().muteAllRemoteAudioStreams(false);
        if (!TextUtils.isEmpty(channelName)) {
            RTCManager.getInstance().getRtcEngine().leaveChannelEx(mRtcConnection);
            mRtcConnection = null;
        }
    }

    @Override
    public void switchRole(int role) {
        mLogger.d("switchRole() called with: role = [%s]", role);

        if(role == Constants.CLIENT_ROLE_BROADCASTER) {
            // Setting player connection.
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.publishCameraTrack = false;
            options.publishCustomAudioTrack = false;
            options.enableAudioRecordingOrPlayout = false;
            options.autoSubscribeAudio = false;
            options.autoSubscribeVideo = false;
            options.publishMicrophoneTrack = false;

            if (role == Constants.CLIENT_ROLE_BROADCASTER) {
                options.clientRoleType = role;
                options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                options.publishMediaPlayerAudioTrack = true;
            } else if (RoomManager.mMine.role != AgoraMember.Role.Listener) {
                options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
                options.publishMediaPlayerAudioTrack = true;
            } else {
                options.clientRoleType = role;
                options.publishMediaPlayerAudioTrack = false;
            }
            joinChannelEX(options);
//        RTCManager.getInstance().getRtcEngine().updateChannelMediaOptions(options);
            RTCManager.getInstance().getRtcEngine().muteRemoteAudioStream(RoomManager.mMine.id.intValue()*10 + 1, true);
        }

        // Set mic connection.
        ChannelMediaOptions micOptions = new ChannelMediaOptions();
        micOptions.publishMediaPlayerAudioTrack = false;
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            micOptions.clientRoleType = role;
            if (RoomManager.mMine.isSelfMuted == 0) {
                micOptions.publishMicrophoneTrack = true;
            }
        } else if (RoomManager.mMine.role != AgoraMember.Role.Listener) {
            micOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
            if (RoomManager.mMine.isSelfMuted == 0) {
                micOptions.publishMicrophoneTrack = true;
            }
        } else {
            micOptions.clientRoleType = role;
            micOptions.publishMicrophoneTrack = false;
        }
        RTCManager.getInstance().getRtcEngine().updateChannelMediaOptions(micOptions);

        mRole = micOptions.clientRoleType;
    }

    private RtcConnection mRtcConnection;
    private String channelName = null;

    private void joinChannelEX(ChannelMediaOptions options) {
        Log.d("cwtsw", "joinChannelEX");
        channelName = RoomManager.getInstance().getRoom().roomNo;
//        options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
        mRtcConnection = new RtcConnection();
        mRtcConnection.channelId = RoomManager.getInstance().getRoom().roomNo;
        mRtcConnection.localUid = RoomManager.mMine.id.intValue() * 10 + 1;
        mLogger.d("joinChannelEx with token: %s, for uid: %d, channelId: %s", KtvConstant.PLAYER_TOKEN, mRtcConnection.localUid, mRtcConnection.channelId);
        RTCManager.getInstance().getRtcEngine().joinChannelEx(KtvConstant.PLAYER_TOKEN, mRtcConnection, options, new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                mLogger.d("onJoinChannelSuccessEX() called with: channel = [%s], uid = [%s], elapsed = [%s]", channel, uid, elapsed);
//                RTCManager.getInstance().getRtcEngine().updateChannelMediaOptions(options);
            }

            @Override
            public void onError(int err) {
                mLogger.d("onJoinChannelSuccessEX() failed with err: %d", err);
            }


            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                mLogger.d("onLeaveChannelEX() called with: stats = [%s]", stats);
            }

        });
    }

    @Override
    public void prepare(@NonNull MemberMusicModel music) {
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }
        onPrepareResource();
        if (ObjectsCompat.equals(music.userNo, mUser.userNo)) {
            switchRole(Constants.CLIENT_ROLE_BROADCASTER);
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
                            onResourceReady(musicModel);
                            RoomManager.getInstance().mMusicModel.fileLrc = musicModel.fileLrc;
                            if (RTCManager.getInstance().preLoad(musicModel.songNo)) {
                                open(musicModel);
                            }
//
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(R.string.ktv_lrc_load_fail);
                        }
                    });
        } else {
            switchRole(Constants.CLIENT_ROLE_AUDIENCE);
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
                            onResourceReady(musicModel);
                            onMusicPlaingByListener();
                            playByListener(musicModel);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(R.string.ktv_lrc_load_fail);
                        }
                    });
        }
    }
}

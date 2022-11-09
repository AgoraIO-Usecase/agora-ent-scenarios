package io.agora.scene.ktv.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.event.PreLoadEvent;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 1.起始函数{@link MultipleMusicPlayer#prepare}。
 * 2.陪唱点击按钮"加入合唱"后，触发申请 ，然后触发{@link MultipleMusicPlayer#onMemberApplyJoinChorus}，主唱把第一个人设置成陪唱。
 * 3.有陪唱加入后，会收到回调{@link MultipleMusicPlayer#onMemberJoinedChorus}，开始下载资源。
 * 4.{@link MultipleMusicPlayer#joinChannelEX}之后，修改状态成Ready，当所有唱歌的人都Ready后，会触发{@link MultipleMusicPlayer#onMemberChorusReady}
 */
public class MultipleMusicPlayer extends BaseMusicPlayer {

    private static final long PLAY_WAIT = 1000L;

    private final SimpleRoomEventCallback mRoomEventCallback = new SimpleRoomEventCallback() {
        @Override
        public void onMemberApplyJoinChorus(@NonNull MemberMusicModel music) {
            super.onMemberApplyJoinChorus(music);
            MultipleMusicPlayer.this.onMemberApplyJoinChorus(music);
        }

        @Override
        public void onMemberJoinedChorus(@NonNull MemberMusicModel music) {
            super.onMemberJoinedChorus(music);
            MultipleMusicPlayer.this.onMemberJoinedChorus(music);
        }

        @Override
        public void onMemberChorusReady(@NonNull MemberMusicModel music) {
            super.onMemberChorusReady(music);
            MultipleMusicPlayer.this.onMemberChorusReady(music);
        }
    };

    public MultipleMusicPlayer(Context mContext, int role, IAgoraMusicPlayer mPlayer) {
        super(mContext, role, mPlayer);
        RTCManager.getInstance().getRtcEngine().setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_STANDARD);
        RoomManager.getInstance().mMainThreadDispatch.addRoomEventCallback(mRoomEventCallback);
        this.mPlayer.adjustPlayoutVolume(80);
        this.selectAudioTrack(1);
    }

    @Override
    public void destroy() {
        super.destroy();
        leaveChannelEX();
        RoomManager.getInstance().mMainThreadDispatch.removeRoomEventCallback(mRoomEventCallback);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        super.onJoinChannelSuccess(channel, uid, elapsed);
    }

    private MemberMusicModel musicModelReady;

    @Override
    public void prepare(@NonNull MemberMusicModel music) {
        Log.d("cwtsw", "多人 prepare");
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        if (mRoom == null) {
            return;
        }

        if (ObjectsCompat.equals(music.userNo, mUser.userNo)) {
            if (music.userStatus == MemberMusicModel.UserStatus.Ready) {
                onMemberJoinedChorus(music);
            }
        } else if (ObjectsCompat.equals(music.userId, mUser.userNo)) {
            onMemberJoinedChorus(music);
        } else {
            if (!TextUtils.isEmpty(music.userNo) && music.userStatus == MemberMusicModel.UserStatus.Ready
                    && !TextUtils.isEmpty(music.userId) && music.user1Status == MemberMusicModel.UserStatus.Ready) {
                onMemberChorusReady(music);
            } else if (!TextUtils.isEmpty(music.userId) && music.user1Status == MemberMusicModel.UserStatus.Idle) {
                onMemberJoinedChorus(music);
            } else if (!TextUtils.isEmpty(music.applyUser1Id)) {
                onMemberApplyJoinChorus(music);
            }
        }
    }

    private RtcConnection mRtcConnection;

    private String channelName = null;

    private void joinChannelEX() {
        Log.d("cwtsw", "多人 joinChannelEX");
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        assert mRoom != null;
        channelName = mRoom.roomNo;

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.clientRoleType = mRole;
        options.publishMicrophoneTrack = false;
        options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
        if (musicModelReady != null && ObjectsCompat.equals(musicModelReady.userNo, mUser.userNo)) {
            options.publishMediaPlayerAudioTrack = true;
            options.enableAudioRecordingOrPlayout = false;
        } else if (ObjectsCompat.equals(musicModelReady.userId, mUser.userNo)) {
            options.publishMediaPlayerAudioTrack = false;
            options.enableAudioRecordingOrPlayout = false;
        }

//        int uid = (int) (Math.random() * (Integer.MAX_VALUE / 2));
        if (musicModelReady != null && ObjectsCompat.equals(mUser.userNo, musicModelReady.userNo)) {
            if (musicModelReady.userbgId != null && musicModelReady.userbgId != 0) {
                uid = musicModelReady.userbgId.intValue();
            }
        } else if (musicModelReady != null && ObjectsCompat.equals(mUser.userNo, musicModelReady.userId)) {
            if (musicModelReady.user1bgId != null && musicModelReady.user1bgId != 0) {
                uid = musicModelReady.user1bgId.intValue();
            }
        }

        mRtcConnection = new RtcConnection();
        mRtcConnection.channelId = channelName;
        mRtcConnection.localUid = uid;
        RTCManager.getInstance().getRtcEngine().joinChannelEx(KtvConstant.PLAYER_TOKEN, mRtcConnection, options, new IRtcEngineEventHandler() {
            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                super.onJoinChannelSuccess(channel, uid, elapsed);
                mLogger.d("onJoinChannelSuccessEX() called with: channel = [%s], uid = [%s], elapsed = [%s]", channel, uid, elapsed);
                MultipleMusicPlayer.this.onJoinChannelExSuccess(uid);
            }

            @Override
            public void onLeaveChannel(RtcStats stats) {
                super.onLeaveChannel(stats);
                mLogger.d("onLeaveChannelEX() called with: stats = [%s]", stats);
            }

        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(@Nullable PreLoadEvent event) {
        open(RoomManager.getInstance().mMusicModel);
    }

    private void leaveChannelEX() {
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

    private int mUid;

    private void onJoinChannelExSuccess(int uid) {
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        if (mRoom == null) {
            return;
        }

        this.mUid = uid;
        Long streamId = uid & 0xffffffffL;
        if (ObjectsCompat.equals(musicModelReady.userNo, mUser.userNo)) {
            //修改准备好的 userbgId 为steaid
            //修改准备好的 userstatus 为Ready 无回调
            RoomManager.getInstance().mMusicModel.userbgId = streamId;
            RoomManager.getInstance().mMusicModel.userStatus = MemberMusicModel.UserStatus.Ready;

//            maps.put(MemberMusicModel.COLUMN_USERSTATUS, MemberMusicModel.UserStatus.Ready.value);
//            maps.put(MemberMusicModel.COLUMN_USERBGID, streamId);
        }
        onMemberChorusReady(RoomManager.getInstance().mMusicModel);
    }

    @Override
    protected void onMusicOpenCompleted() {
        mLogger.i("onMusicOpenCompleted() called");
        if (mStatus == Status.Opened) return;
        mStatus = Status.Opened;

        startDisplayLrc();
        mHandler.obtainMessage(ACTION_ON_MUSIC_OPENCOMPLETED, mPlayer.getDuration()).sendToTarget();
    }

    private volatile boolean isApplyJoinChorus = false;

    private void onMemberApplyJoinChorus(@NonNull MemberMusicModel music) {
        Log.d("cwtsw", "多人 onMemberApplyJoinChorus");
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (!ObjectsCompat.equals(mUser.userNo, music.userNo)) {
            return;
        }

        AgoraRoom mRoom = RoomManager.getInstance().getRoom();
        if (mRoom == null) {
            return;
        }

        if (isApplyJoinChorus) {
            return;
        }

        isApplyJoinChorus = true;

        //修改音乐的 user1id 为 music applyUser1Id 通过id查找
        //修改音乐的 applyUser1Id 为""
        //无回调

        RoomManager.getInstance().mMusicModel.userId = music.applyUser1Id;
        RoomManager.getInstance().mMusicModel.applyUser1Id = "";
        RoomManager.getInstance().mMusicModel.user1bgId = music.user1bgId;
        onMemberJoinedChorus(music);
//        maps.put(MemberMusicModel.COLUMN_USER1ID, music.applyUser1Id);
//        maps.put(MemberMusicModel.COLUMN_APPLYUSERID, "");
    }

    private void onMemberJoinedChorus(@NonNull MemberMusicModel music) {
        Log.d("cwtsw", "多人 onMemberJoinedChorus");
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }
        if (ObjectsCompat.equals(mUser.userNo, music.userNo)
                || ObjectsCompat.equals(mUser.userNo, music.userId)) {
            onPrepareResource();
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
                            musicModelReady = musicModel;
                            switchRole(Constants.CLIENT_ROLE_BROADCASTER);
                            if (RTCManager.getInstance().preLoad(musicModel.songNo)) {
                                Log.d("cwtsw", "多人 open");
                                open(musicModel);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(R.string.ktv_lrc_load_fail);
                        }
                    });
        }
    }

    @Override
    protected int open(@NonNull MemberMusicModel music) {
        int result = super.open(music);
        if (ObjectsCompat.equals(UserManager.getInstance().getUser().userNo, music.userNo)) {
            Log.d("cwtsw", "多人 open 我是主唱");
            joinChannelEX();
        } else if (UserManager.getInstance().getUser().userNo.equals(music.userId)) {
            startNetTestTask();
            joinChannelEX();
            musicModelReady.user1Status = MemberMusicModel.UserStatus.Ready;
            musicModelReady.user1bgId = music.user1bgId;
            Log.d("cwtsw", "多人 open 我是合唱");
            if (isStartPlay || mStatus != Status.Started) {
                onReceivedStatusPlay(uid);
                isStartPlay = false;
            }
        }
        return result;
    }

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

    /**
     * 主唱逻辑：
     * 1.{@link MultipleMusicPlayer#sendStartPlay}通知陪唱的人，要开始了。
     * 2.陪唱人会收到{@link MultipleMusicPlayer#onReceivedStatusPlay}。
     * 3.为了保证所有人同时播放音乐，做延迟wait。
     * <p>
     * 陪唱逻辑:{@link MultipleMusicPlayer#onReceivedStatusPlay}
     *
     * @param music
     */
    private void onMemberChorusReady(@NonNull MemberMusicModel music) {
        Log.d("cwtsw", "多人 onMemberChorusReady");
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        AgoraMember mMine = RoomManager.getInstance().getMine();
        if (mMine == null) {
            return;
        }

        if (music.userNo.equals(mUser.userNo)) {
            //唱歌人，主唱，joinChannel 需要屏蔽的uid
            RTCManager.getInstance().getRtcEngine().muteRemoteAudioStream(music.userbgId.intValue(), true);
        } else if (music.userId.equals(mUser.userNo)) {
            //唱歌人，陪唱人，joinChannel 需要屏蔽的uid
            RTCManager.getInstance().getRtcEngine().muteRemoteAudioStream(music.userbgId.intValue(), true);
        }
        Log.d("cwtsw", "多人 onMemberChorusReady");
        if (ObjectsCompat.equals(music.userNo, mUser.userNo)
                || ObjectsCompat.equals(music.userId, mUser.userNo)) {
            music.fileMusic = (musicModelReady.fileMusic);
            music.fileLrc = (musicModelReady.fileLrc);

//            if (ObjectsCompat.equals(music.userNo, mUser.userNo)) {
            Log.d("cwtsw", "多人 onMemberChorusReady 调play");
            sendStartPlay();
            try {
                synchronized (this) {
                    wait(PLAY_WAIT);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            play();
//            } else {
//                Log.d("cwtsw", "多人 onMemberChorusReady 我不是主唱");
//            }
        } else {
            onPrepareResource();

            ResourceManager.Instance(mContext)
                    .download(music, true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<MemberMusicModel>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onSuccess(@NonNull MemberMusicModel musicModel) {
                            onResourceReady(musicModel);

                            onMusicPlaingByListener();
                            playByListener(music);
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            ToastUtils.showToast(R.string.ktv_lrc_load_fail);
                        }
                    });
        }
    }

    @Override
    protected void onReceivedStatusPlay(int uid) {
        super.onReceivedStatusPlay(uid);
        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (mUser.userNo.equals(mMemberMusicModel.userId) || mUser.userNo.equals(mMemberMusicModel.userNo)) {
            //已经开始了 直接retrun;
            Log.d("cwtsw", "多人 相等");

            try {
                synchronized (this) {
                    long waitTime = PLAY_WAIT - netRtt;
                    mLogger.d("onReceivedStatusPlay() called with: waitTime = [%s]", waitTime);
                    if (waitTime > 0) {
                        wait(waitTime);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            play();
        } else {
            Log.d("cwtsw", "多人 不相等");
        }

    }

    @Override
    protected void onReceivedStatusPause(int uid) {
        super.onReceivedStatusPause(uid);

        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userNo, mUser.userNo)) {

        } else if (ObjectsCompat.equals(mMemberMusicModel.userId, mUser.userNo)) {
            pause();
        }
    }

    @Override
    protected void onReceivedSetLrcTime(int uid, long position) {
        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userNo, mUser.userNo)) {

        } else if (ObjectsCompat.equals(mMemberMusicModel.userId, mUser.userNo)) {
            //如果是暂停 则恢复
            if (mStatus == Status.Paused) {
                resume();
            } else {
                super.onReceivedSetLrcTime(uid, position);
            }
        } else {
            super.onReceivedSetLrcTime(uid, position);
        }
    }

    @Override
    protected void onReceivedTestDelay(int uid, long time) {
        super.onReceivedTestDelay(uid, time);

        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userNo, mUser.userNo)) {
            sendReplyTestDelay(time);
        }
    }

    private long offsetTS = 0;
    private long netRtt = 0;
    private long delayWithBrod = 0;

    @Override
    protected void onReceivedReplyTestDelay(int uid, long testDelayTime, long time, long position) {
        super.onReceivedReplyTestDelay(uid, testDelayTime, time, position);
        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userId, mUser.userNo)) {
            long localTs = System.currentTimeMillis();
            netRtt = (localTs - testDelayTime) / 2;
            delayWithBrod = position + netRtt;

            long localPos = mPlayer.getPlayPosition();
            long diff = localPos - delayWithBrod;
            if (Math.abs(diff) > 40) {
                mLogger.d("xn123 seek= [%s], remotePos = [%s], localPos = [%s]", delayWithBrod, position, localPos);
                seek(delayWithBrod);
            }
        }
    }

    @Override
    protected void onReceivedOrigleChanged(int uid, int mode) {
        super.onReceivedOrigleChanged(uid, mode);
        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userId, mUser.userNo)) {
//            selectAudioTrack(mode);
        }
    }

    @Override
    public void switchRole(int role) {
        mLogger.d("switchRole() called with: role = [%s]", role);
        mRole = role;

        ChannelMediaOptions options = new ChannelMediaOptions();
        options.publishMediaPlayerId = mPlayer.getMediaPlayerId();
        options.clientRoleType = role;
        options.publishMediaPlayerAudioTrack = false;
        if (role == Constants.CLIENT_ROLE_BROADCASTER) {
            if (RoomManager.mMine.isSelfMuted == 0) {
                options.publishMicrophoneTrack = true;
            }
        } else {
            options.publishMicrophoneTrack = false;
        }
        RTCManager.getInstance().getRtcEngine().updateChannelMediaOptions(options);
    }

    @Override
    protected void startPublish() {
        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userNo, mUser.userNo)) {
            super.startPublish();
        }
    }

    @Override
    public void togglePlay() {
        if (mStatus == Status.Started) {
            sendPause();
        } else if (mStatus == Status.Paused) {
            sendPlay();
        }

        super.togglePlay();
    }

    public void sendReplyTestDelay(long receiveTime) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "replyTestDelay");
        msg.put("testDelayTime", String.valueOf(receiveTime));
        msg.put("time", String.valueOf(System.currentTimeMillis()));
        msg.put("position", mPlayer.getPlayPosition());
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendReplyTestDelay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendTestDelay() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "testDelay");
        msg.put("time", String.valueOf(System.currentTimeMillis()));
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendTestDelay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendStartPlay() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "setLrcTime");
        msg.put("time", 0);
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendStartPlay() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendPause() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "setLrcTime");
        msg.put("time", -1);
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        Log.d("cwtsw", "发送多人暂停消息");
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendPause() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    public void sendPlay() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "setLrcTime");
        msg.put("time", 0);
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        Log.d("cwtsw", "发送多人恢复");
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendPause() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }

    @Override
    public void selectAudioTrack(int i) {
        super.selectAudioTrack(i);

        MemberMusicModel mMemberMusicModel = RoomManager.getInstance().getMusicModel();
        if (mMemberMusicModel == null) {
            return;
        }

        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return;
        }

        if (ObjectsCompat.equals(mMemberMusicModel.userNo, mUser.userNo)) {
            sendTrackMode(i);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (mRtcConnection == null) {
            return;
        }
        if (mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            RTCManager.getInstance().getRtcEngine().muteAllRemoteAudioStreams(false);
            if (!TextUtils.isEmpty(channelName)) {
                RTCManager.getInstance().getRtcEngine().leaveChannelEx(mRtcConnection);
                mRtcConnection = null;
            }
        }
    }

    public void sendTrackMode(int mode) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "TrackMode");
        msg.put("mode", mode);
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
        if (ret < 0) {
            mLogger.e("sendTrackMode() sendStreamMessage called returned: ret = [%s]", ret);
        }
    }
}

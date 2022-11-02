package io.agora.scene.base.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.agora.mediaplayer.IMediaPlayer;
import io.agora.mediaplayer.IMediaPlayerObserver;
import io.agora.mediaplayer.data.PlayerUpdatedInfo;
import io.agora.mediaplayer.data.SrcInfo;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.event.PlayerStatusEvent;

public abstract class BaseMusicPlayerOld extends IRtcEngineEventHandler implements IMediaPlayerObserver {
    protected final Logger.Builder mLogger = XLog.tag("MusicPlayer");

    protected final Context mContext;
    protected int mRole = Constants.CLIENT_ROLE_BROADCASTER;

    //主唱同步歌词给其他人
    private boolean mStopSyncLrc = true;
    private Thread mSyncLrcThread;

    //歌词实时刷新
    protected boolean mStopDisplayLrc = true;
    private Thread mDisplayThread;

    protected IMediaPlayer mPlayer;

    private static volatile long mRecvedPlayPosition = 0;//播放器播放position，ms
    private static volatile Long mLastRecvPlayPosTime = null;

    protected static volatile MemberMusicModel mMusicModel;

    private Callback mCallback;

    protected static final int ACTION_UPDATE_TIME = 100;
    protected static final int ACTION_ONMUSIC_OPENING = ACTION_UPDATE_TIME + 1;
    protected static final int ACTION_ON_MUSIC_OPENCOMPLETED = ACTION_ONMUSIC_OPENING + 1;
    protected static final int ACTION_ON_MUSIC_OPENERROR = ACTION_ON_MUSIC_OPENCOMPLETED + 1;
    protected static final int ACTION_ON_MUSIC_PLAING = ACTION_ON_MUSIC_OPENERROR + 1;
    protected static final int ACTION_ON_MUSIC_PAUSE = ACTION_ON_MUSIC_PLAING + 1;
    protected static final int ACTION_ON_MUSIC_STOP = ACTION_ON_MUSIC_PAUSE + 1;
    protected static final int ACTION_ON_MUSIC_COMPLETED = ACTION_ON_MUSIC_STOP + 1;
    protected static final int ACTION_ON_RECEIVED_COUNT_DOWN = ACTION_ON_MUSIC_COMPLETED + 1;
    protected static final int ACTION_ON_RECEIVED_PLAY = ACTION_ON_RECEIVED_COUNT_DOWN + 1;
    protected static final int ACTION_ON_RECEIVED_PAUSE = ACTION_ON_RECEIVED_PLAY + 1;
    protected static final int ACTION_ON_RECEIVED_SYNC_TIME = ACTION_ON_RECEIVED_PAUSE + 1;
    protected static final int ACTION_ON_RECEIVED_TEST_DELAY = ACTION_ON_RECEIVED_SYNC_TIME + 1;
    protected static final int ACTION_ON_RECEIVED_REPLAY_TEST_DELAY = ACTION_ON_RECEIVED_TEST_DELAY + 1;
    protected static final int ACTION_ON_RECEIVED_CHANGED_ORIGLE = ACTION_ON_RECEIVED_REPLAY_TEST_DELAY + 1;

    protected static volatile Status mStatus = Status.IDLE;

    enum Status {
        IDLE(0), Opened(1), Started(2), Paused(3), Stopped(4);

        int value;

        Status(int value) {
            this.value = value;
        }

        public boolean isAtLeast(@NonNull Status state) {
            return compareTo(state) >= 0;
        }
    }

    protected final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == ACTION_UPDATE_TIME) {
                if (mCallback != null) {
                    mCallback.onMusicPositionChanged((long) msg.obj);
                }
            } else if (msg.what == ACTION_ONMUSIC_OPENING) {
                if (mCallback != null) {
                    mCallback.onMusicOpening();
                }
            } else if (msg.what == ACTION_ON_MUSIC_OPENCOMPLETED) {
                if (mCallback != null) {
                    mCallback.onMusicOpenCompleted((long) msg.obj);
                }
            } else if (msg.what == ACTION_ON_MUSIC_OPENERROR) {
                if (mCallback != null) {
                    mCallback.onMusicOpenError((int) msg.obj);
                }
            } else if (msg.what == ACTION_ON_MUSIC_PLAING) {
                if (mCallback != null) {
                    mCallback.onMusicPlaying();
                }
            } else if (msg.what == ACTION_ON_MUSIC_PAUSE) {
                if (mCallback != null) {
                    mCallback.onMusicPause();
                }
            } else if (msg.what == ACTION_ON_MUSIC_STOP) {
                if (mCallback != null) {
                    mCallback.onMusicStop();
                }
            } else if (msg.what == ACTION_ON_MUSIC_COMPLETED) {
                if (mCallback != null) {
                    mCallback.onMusicCompleted();
                }
            } else if (msg.what == ACTION_ON_RECEIVED_COUNT_DOWN) {
                Bundle data = msg.getData();
                int uid = data.getInt("uid");
                int time = data.getInt("time");
                if (mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                    onReceivedCountdown(uid, time);
                }
            } else if (msg.what == ACTION_ON_RECEIVED_PLAY) {
                onReceivedStatusPlay((Integer) msg.obj);
            } else if (msg.what == ACTION_ON_RECEIVED_PAUSE) {
                onReceivedStatusPause((Integer) msg.obj);
            } else if (msg.what == ACTION_ON_RECEIVED_SYNC_TIME) {
                Bundle data = msg.getData();
                int uid = data.getInt("uid");
                long time = data.getLong("time");
                onReceivedSetLrcTime(uid, time);
            } else if (msg.what == ACTION_ON_RECEIVED_TEST_DELAY) {
                Bundle data = msg.getData();
                int uid = data.getInt("uid");
                long time = data.getLong("time");
                onReceivedTestDelay(uid, time);
            } else if (msg.what == ACTION_ON_RECEIVED_REPLAY_TEST_DELAY) {
                Bundle data = msg.getData();
                int uid = data.getInt("uid");
                long testDelayTime = data.getLong("testDelayTime");
                long time = data.getLong("time");
                long position = data.getLong("position");
                onReceivedReplyTestDelay(uid, testDelayTime, time, position);
            } else if (msg.what == ACTION_ON_RECEIVED_CHANGED_ORIGLE) {
                Bundle data = msg.getData();
                int uid = data.getInt("uid");
                int mode = data.getInt("mode");
                onReceivedOrigleChanged(uid, mode);
            }
        }
    };

    public BaseMusicPlayerOld(Context mContext, int role, IMediaPlayer mPlayer) {
        this.mContext = mContext;
        this.mPlayer = mPlayer;
        reset();

        this.mPlayer.registerPlayerObserver(this);

        RTCManager.getInstance().getRtcEngine().addHandler(this);
        switchRole(role);
    }

    private void reset() {
        mRecvedPlayPosition = 0;
        mLastRecvPlayPosTime = null;
        mMusicModel = null;
        mAudioTrackIndex = 1;
        mStatus = Status.IDLE;
    }

    public void registerPlayerObserver(Callback mCallback) {
        this.mCallback = mCallback;
    }

    public void unregisterPlayerObserver() {
        this.mCallback = null;
    }

    public abstract void switchRole(int role);

    public abstract void prepare(@NonNull MemberMusicModel music);

    public void playByListener(@NonNull MemberMusicModel mMusicModel) {
        BaseMusicPlayerOld.mMusicModel = mMusicModel;
        startDisplayLrc();
    }

    protected int open(@NonNull MemberMusicModel mMusicModel) {
        if (mRole != Constants.CLIENT_ROLE_BROADCASTER) {
            mLogger.e("open error: current role is not broadcaster, abort playing");
            return -1;
        }

        if (mStatus.isAtLeast(Status.Opened)) {
            mLogger.e("open error: current player is in playing state already, abort playing");
            return -2;
        }

        if (!mStopDisplayLrc) {
            mLogger.e("open error: current player is recving remote streams, abort playing");
            return -3;
        }

        File fileMusic = mMusicModel.fileMusic;
        if (fileMusic.exists() == false) {
            mLogger.e("open error: fileMusic is not exists");
            return -4;
        }

        File fileLrc = mMusicModel.fileLrc;
        if (fileLrc.exists() == false) {
            mLogger.e("open error: fileLrc is not exists");
            return -5;
        }

        if (mPlayer == null) {
            return -6;
        }

        stopDisplayLrc();

        mAudioTrackIndex = 1;
        BaseMusicPlayerOld.mMusicModel = mMusicModel;
        mLogger.i("open() called with: mMusicModel = [%s]", mMusicModel);
        mPlayer.open(fileMusic.getAbsolutePath(), 0);
        return 0;
    }

    protected void play() {
        mLogger.i("play() called");
//        if (!mStatus.isAtLeast(Status.Opened)) {
//            return;
//        }

        if (mStatus == Status.Started)
            return;

        mStatus = Status.Started;
        mPlayer.play();
        mLogger.i("play() called___");
        EventBus.getDefault().post(new PlayerStatusEvent(true));
    }

    public void stop() {
        mLogger.i("stop() called");
        if (!mStatus.isAtLeast(Status.Started)) {
            return;
        }

        mPlayer.stop();
    }

    protected void pause() {
        mLogger.i("pause() called");
        if (!mStatus.isAtLeast(Status.Opened)) {
            return;
        }

        if (mStatus == Status.Paused)
            return;

        mPlayer.pause();
    }

    protected void resume() {
        mLogger.i("resume() called");
        if (!mStatus.isAtLeast(Status.Opened)) {
            return;
        }

        if (mStatus == Status.Started)
            return;

        mPlayer.resume();
    }

    public void togglePlay() {
        if (!mStatus.isAtLeast(Status.Started)) {
            return;
        }

        if (mStatus == Status.Started) {
            pause();
        } else if (mStatus == Status.Paused) {
            resume();
        }
    }

    protected int mAudioTrackIndex = 1;

    public void selectAudioTrack(int i) {
        //因为咪咕音乐没有音轨，只有左右声道，所以暂定如此
        mAudioTrackIndex = i;

        if (mAudioTrackIndex == 0) {
            mPlayer.setAudioDualMonoMode(1);
        } else {
            mPlayer.setAudioDualMonoMode(2);
        }
    }

    public boolean hasAccompaniment() {
        //因为咪咕音乐没有音轨，只有左右声道，所以暂定如此
        return true;
    }

    public void toggleOrigle() {
        if (mAudioTrackIndex == 0) {
            selectAudioTrack(1);
        } else {
            selectAudioTrack(0);
        }
    }

    public void sendCountdown(int time) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("cmd", "countdown");
        msg.put("time", time);
        JSONObject jsonMsg = new JSONObject(msg);
        int streamId = RTCManager.getInstance().getStreamId();
        RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
    }

    public void setMusicVolume(int v) {
        mPlayer.adjustPlayoutVolume(v);
        mPlayer.adjustPublishSignalVolume(v);
    }

    public void setMicVolume(int v) {
        RTCManager.getInstance().getRtcEngine().adjustRecordingSignalVolume(v);
    }

    /**
     * Sets the pitch of the current media file.
     * PITCH Sets the pitch of the local music file by chromatic scale. The default value is 0,
     * which means keeping the original pitch. The value ranges from -12 to 12, and the pitch value
     * between consecutive values is a chromatic value. The greater the absolute value of this
     * parameter, the higher or lower the pitch of the local music file.
     *
     * @return - 0: Success.
     * - < 0: Failure.
     */
    public void setAudioMixingPitch(int newToneValue) {
        mPlayer.setAudioPitch(newToneValue);
    }

    public void seek(long d) {
        mPlayer.seek(d);
    }

    protected void startDisplayLrc() {
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
                            mHandler.obtainMessage(ACTION_UPDATE_TIME, curTs).sendToTarget();
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

    protected void stopDisplayLrc() {
        mStopDisplayLrc = true;
        if (mDisplayThread != null) {
            try {
                mDisplayThread.join();
            } catch (InterruptedException exp) {
                mLogger.e("stopDisplayLrc: " + exp.getMessage());
            }
        }
    }

    private void startSyncLrc(String lrcId, long duration) {
        mSyncLrcThread = new Thread(new Runnable() {

            @Override
            public void run() {
                mLogger.i("startSyncLrc: " + lrcId);
                mStopSyncLrc = false;
                while (!mStopSyncLrc && mStatus.isAtLeast(Status.Started)) {
                    if (mPlayer == null) {
                        break;
                    }

                    if (mLastRecvPlayPosTime != null && mStatus == Status.Started) {
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
                int streamId = RTCManager.getInstance().getStreamId();
                int ret = RTCManager.getInstance().getRtcEngine().sendStreamMessage(streamId, jsonMsg.toString().getBytes());
                if (ret < 0) {
                    mLogger.e("sendSyncLrc() sendStreamMessage called returned: ret = [%s]", ret);
                }
            }
        });
        mSyncLrcThread.setName("Thread-SyncLrc");
        mSyncLrcThread.start();
    }

    private void stopSyncLrc() {
        mStopSyncLrc = true;
        if (mSyncLrcThread != null) {
            try {
                mSyncLrcThread.join();
            } catch (InterruptedException exp) {
                mLogger.e("stopSyncLrc: " + exp.getMessage());
            }
        }
    }

    protected void startPublish() {
        startSyncLrc(String.valueOf(mMusicModel.songNo), mPlayer.getDuration());
    }

    private void stopPublish() {
        stopSyncLrc();
    }

    @Override
    public void onStreamMessageError(int uid, int streamId, int error, int missed, int cached) {
        super.onStreamMessageError(uid, streamId, error, missed, cached);
        mLogger.e("onStreamMessageError() called with: uid = [%s], streamId = [%s], error = [%s], missed = [%s], cached = [%s]", uid, streamId, error, missed, cached);
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
                    mHandler.obtainMessage(ACTION_ON_RECEIVED_PLAY, uid).sendToTarget();
                } else if (position == -1) {
                    mHandler.obtainMessage(ACTION_ON_RECEIVED_PAUSE, uid).sendToTarget();
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putInt("uid", uid);
                    bundle.putLong("time", position);
                    Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_SYNC_TIME);
                    message.setData(bundle);
                    message.sendToTarget();
                }
            } else if (jsonMsg.getString("cmd").equals("countdown")) {
                int time = jsonMsg.getInt("time");
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("time", time);
                Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_COUNT_DOWN);
                message.setData(bundle);
                message.sendToTarget();
            } else if (jsonMsg.getString("cmd").equals("testDelay")) {
                long time = jsonMsg.getLong("time");
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putLong("time", time);
                Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_TEST_DELAY);
                message.setData(bundle);
                message.sendToTarget();
            } else if (jsonMsg.getString("cmd").equals("replyTestDelay")) {
                long testDelayTime = jsonMsg.getLong("testDelayTime");
                long time = jsonMsg.getLong("time");
                long position = jsonMsg.getLong("position");
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putLong("time", time);
                bundle.putLong("position", position);
                bundle.putLong("testDelayTime", testDelayTime);
                Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_REPLAY_TEST_DELAY);
                message.setData(bundle);
                message.sendToTarget();
            } else if (jsonMsg.getString("cmd").equals("TrackMode")) {
                int mode = jsonMsg.getInt("mode");
                Bundle bundle = new Bundle();
                bundle.putInt("uid", uid);
                bundle.putInt("mode", mode);
                Message message = Message.obtain(mHandler, ACTION_ON_RECEIVED_CHANGED_ORIGLE);
                message.setData(bundle);
                message.sendToTarget();
            }
        } catch (JSONException exp) {
            mLogger.e("onStreamMessage: failed parse json, error: " + exp.toString());
        }
    }

    protected void onReceivedStatusPlay(int uid) {
    }

    protected void onReceivedStatusPause(int uid) {
    }

    protected void onReceivedSetLrcTime(int uid, long position) {
        mRecvedPlayPosition = position;
        mLastRecvPlayPosTime = System.currentTimeMillis();
    }

    protected void onReceivedCountdown(int uid, int time) {
        if (mCallback != null) {
            RoomManager.getInstance().mMusicModel.userbgId = (long) uid;
            mCallback.onReceivedCountdown(time);
        }
    }

    protected void onReceivedTestDelay(int uid, long time) {
    }

    protected void onReceivedReplyTestDelay(int uid, long testDelayTime, long time, long position) {
    }

    protected void onReceivedOrigleChanged(int uid, int mode) {
        mLogger.d("onReceivedOrigleChanged() called with: uid = [%s], mode = [%s]", uid, mode);
    }

    @Override
    public void onPlayerStateChanged(io.agora.mediaplayer.Constants.MediaPlayerState state, io.agora.mediaplayer.Constants.MediaPlayerError error) {
        mLogger.i("onPlayerStateChanged: " + state + ", error: " + error);
        switch (state) {
            case PLAYER_STATE_OPENING:
                onMusicOpening();
                break;
            case PLAYER_STATE_OPEN_COMPLETED:
                onMusicOpenCompleted();
                break;
            case PLAYER_STATE_PLAYING:
                onMusicPlaing();
                break;
            case PLAYER_STATE_PAUSED:
                onMusicPause();
                break;
            case PLAYER_STATE_STOPPED:
                onMusicStop();
                break;
            case PLAYER_STATE_FAILED:
                onMusicOpenError(io.agora.mediaplayer.Constants.MediaPlayerError.getValue(error));
                mLogger.e("onPlayerStateChanged: failed to play, error " + error);
                break;
            default:
        }
    }

    @Override
    public void onPositionChanged(long position) {
        mRecvedPlayPosition = position;
        mLastRecvPlayPosTime = System.currentTimeMillis();
    }

    @Override
    public void onPlayerEvent(io.agora.mediaplayer.Constants.MediaPlayerEvent eventCode, long elapsedTime, String message) {

    }

    @Override
    public void onPreloadEvent(String src, io.agora.mediaplayer.Constants.MediaPlayerPreloadEvent event) {

    }


    @Override
    public void onPlayerSrcInfoChanged(SrcInfo from, SrcInfo to) {

    }

    @Override
    public void onAgoraCDNTokenWillExpire() {

    }

    @Override
    public void onPlayerInfoUpdated(PlayerUpdatedInfo info) {

    }

    @Override
    public void onAudioVolumeIndication(int volume) {

    }

    @Override
    public void onMetaData(io.agora.mediaplayer.Constants.MediaPlayerMetadataType type, byte[] data) {

    }

    @Override
    public void onPlayBufferUpdated(long l) {

    }

    @Override
    public void onCompleted() {
        onMusicCompleted();
    }

    private void onMusicOpening() {
        mLogger.i("onMusicOpening() called");
        mHandler.obtainMessage(ACTION_ONMUSIC_OPENING).sendToTarget();
    }

    protected void onMusicOpenCompleted() {
        mLogger.i("onMusicOpenCompleted() called");
        mStatus = Status.Opened;

        play();
        startDisplayLrc();
        mHandler.obtainMessage(ACTION_ON_MUSIC_OPENCOMPLETED, mPlayer.getDuration()).sendToTarget();
    }

    private void onMusicOpenError(int error) {
        mLogger.i("onMusicOpenError() called with: error = [%s]", error);
        reset();

        mHandler.obtainMessage(ACTION_ON_MUSIC_OPENERROR, error).sendToTarget();
    }

    protected void onMusicPlaingByListener() {
        mLogger.i("onMusicPlaingByListener() called");
        mStatus = Status.Started;

        mHandler.obtainMessage(ACTION_ON_MUSIC_PLAING).sendToTarget();
    }

    protected void onMusicPlaing() {
        mLogger.i("onMusicPlaing() called");
        mStatus = Status.Started;

        if (mStopSyncLrc)
            startPublish();

        mHandler.obtainMessage(ACTION_ON_MUSIC_PLAING).sendToTarget();
    }

    private void onMusicPause() {
        mLogger.i("onMusicPause() called");
        mStatus = Status.Paused;

        mHandler.obtainMessage(ACTION_ON_MUSIC_PAUSE).sendToTarget();
    }

    private void onMusicStop() {
        mLogger.i("onMusicStop() called");
        mStatus = Status.Stopped;

        stopDisplayLrc();
        stopPublish();
        reset();

        mHandler.obtainMessage(ACTION_ON_MUSIC_STOP).sendToTarget();
    }

    private void onMusicCompleted() {
        mLogger.i("onMusicCompleted() called");
        mPlayer.stop();
        stopDisplayLrc();
        stopPublish();
        reset();
        mHandler.obtainMessage(ACTION_ON_MUSIC_COMPLETED).sendToTarget();
        EventBus.getDefault().post(new PlayerStatusEvent(false));
    }

    public void destroy() {
        mLogger.i("destory() called");
        mPlayer.unRegisterPlayerObserver(this);
        RTCManager.getInstance().getRtcEngine().removeHandler(this);
        mCallback = null;
    }

    protected void onPrepareResource() {
        if (mCallback != null) {
            mCallback.onPrepareResource();
        }
    }

    protected void onResourceReady(@NonNull MemberMusicModel music) {
        if (mCallback != null) {
            mCallback.onResourceReady(music);
        }
    }

    @MainThread
    public interface Callback {
        /**
         * 从云端下载资源
         */
        void onPrepareResource();

        /**
         * 资源下载结束
         *
         * @param music
         */
        void onResourceReady(@NonNull MemberMusicModel music);

        /**
         * 歌曲文件打开
         */
        void onMusicOpening();

        /**
         * 歌曲打开成功
         *
         * @param duration 总共时间，毫秒
         */
        void onMusicOpenCompleted(long duration);

        /**
         * 歌曲打开失败
         *
         * @param error 错误码
         */
        void onMusicOpenError(int error);

        /**
         * 正在播放
         */
        void onMusicPlaying();

        /**
         * 暂停
         */
        void onMusicPause();

        /**
         * 结束
         */
        void onMusicStop();

        /**
         * 播放完成
         */
        void onMusicCompleted();

        /**
         * 进度更新
         *
         * @param position
         */
        void onMusicPositionChanged(long position);

        /**
         * 合唱模式下，等待加入合唱倒计时
         *
         * @param time 秒
         */
        void onReceivedCountdown(int time);
    }
}

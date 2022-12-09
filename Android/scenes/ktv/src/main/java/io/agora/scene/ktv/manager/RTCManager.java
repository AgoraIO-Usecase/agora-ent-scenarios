package io.agora.scene.ktv.manager;

import android.text.TextUtils;
import android.util.Log;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import io.agora.musiccontentcenter.IAgoraMusicContentCenter;
import io.agora.musiccontentcenter.IAgoraMusicPlayer;
import io.agora.musiccontentcenter.IMusicContentCenterEventHandler;
import io.agora.musiccontentcenter.Music;
import io.agora.musiccontentcenter.MusicChartInfo;
import io.agora.musiccontentcenter.MusicContentCenterConfiguration;
import io.agora.rtc2.Constants;
import io.agora.rtc2.DataStreamConfig;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.RtcEngineConfig;
import io.agora.rtc2.RtcEngineEx;
import io.agora.scene.base.BuildConfig;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.component.ISingleCallback;
import io.agora.scene.base.event.NetWorkEvent;
import io.agora.scene.base.event.PreLoadEvent;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.ktv.manager.bean.RTCMessageBean;
import io.agora.scene.ktv.manager.bean.RTMMessageBean;

/**
 * RTC控制
 */
public final class RTCManager {
    private final Logger.Builder mLoggerRTC = XLog.tag("RTC");
    public static final int RTC_TYPE_INIT_SUCCESS = 1;

    private static class SingletonHolder {
        private static final RTCManager INSTANCE = new RTCManager();
    }

    public static RTCManager getInstance() {
        return RTCManager.SingletonHolder.INSTANCE;
    }

    private ISingleCallback<Integer, Object> mRTCEvent;

    public void setRTCEvent(ISingleCallback<Integer, Object> roomEvent) {
        this.mRTCEvent = roomEvent;
    }

    public void removeAllEvent() {
        this.mRTCEvent = null;
    }


    private RtcEngineEx mRtcEngine;

    public RtcEngineEx getRtcEngine() {
        return mRtcEngine;
    }

    public IAgoraMusicContentCenter iAgoraMusicContentCenter;

    public void initRTC() {
        if (TextUtils.isEmpty(BuildConfig.AGORA_APP_ID)) {
            throw new NullPointerException("please check \"strings_config.xml\"");
        }
        if (mRtcEngine != null) {
            return;
        }
        //初始化RTC
        RtcEngineConfig config = new RtcEngineConfig();
        config.mContext = AgoraApplication.the();
        config.mAppId = BuildConfig.AGORA_APP_ID;
        config.mEventHandler = mIRtcEngineEventHandler;
        config.mChannelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
        config.mAudioScenario = Constants.AUDIO_SCENARIO_CHORUS;
        try {
            mRtcEngine = (RtcEngineEx) RtcEngine.create(config);
        } catch (Exception e) {
            e.printStackTrace();
            mLoggerRTC.e("init error", e);
        }
        mRtcEngine.loadExtensionProvider("agora_drm_loader");
        mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY, Constants.AUDIO_SCENARIO_GAME_STREAMING);
        mRtcEngine.addHandler(new IRtcEngineEventHandler() {
            @Override
            public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
                super.onNetworkQuality(uid, txQuality, rxQuality);
                if(uid == UserManager.getInstance().getUser().id) {
                    EventBus.getDefault().post(new NetWorkEvent(txQuality, rxQuality));
                }
            }
        });
    }

    public void initMcc(long uid, String rtmToken) {
        MusicContentCenterConfiguration contentCenterConfiguration
                = new MusicContentCenterConfiguration();
        contentCenterConfiguration.appId = BuildConfig.AGORA_APP_ID;
        contentCenterConfiguration.mccUid = uid;
        contentCenterConfiguration.rtmToken = rtmToken;
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
    }

    public String getAgoraRTCSdkVersion() {
        return RtcEngine.getSdkVersion();
    }

    public IAgoraMusicPlayer createMediaPlayer() {
        return iAgoraMusicContentCenter.createMusicPlayer();
    }

    public boolean preLoad(String songNo) {
        if(iAgoraMusicContentCenter.isPreloaded(Long.parseLong(songNo)) != 0){
            iAgoraMusicContentCenter.preload(Long.parseLong(songNo), null);
            return false;
        }else {
            return true;
        }
//        if (iAgoraMusicContentCenter.isPreloaded(Long.parseLong(songNo), IAgoraMusicContentCenter.MusicMediaType.AGORA_MEDIA_TYPE_AUDIO, null) != 0) {
//            iAgoraMusicContentCenter.preload(Long.parseLong(songNo),
//                    IAgoraMusicContentCenter.MusicMediaType.AGORA_MEDIA_TYPE_AUDIO, null);
//            return false;
//        } else {
//            return true;
//        }
    }


    public void joinRTC(String token, String channelId, Long uid, int role) {
        getRtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        getRtcEngine().enableAudio();
        getRtcEngine().enableAudioVolumeIndication(30, 10, true);
        getRtcEngine().enableVideo();
        getRtcEngine().enableLocalVideo(false);
        getRtcEngine().setParameters("{\"rtc.audio.opensl.mode\":0}");
        getRtcEngine().setParameters("{\"rtc.audio_fec\":[3,2]}");
        getRtcEngine().setParameters("{\"rtc.audio_resend\":false}");
        getRtcEngine().setClientRole(role);
        int ret = getRtcEngine().joinChannel(token, channelId, null, uid.intValue());
        if (ret != Constants.ERR_OK) {
            mLoggerRTC.e("joinRTC() called error " + ret);
        } else {
            mRTCEvent.onSingleCallback(RTC_TYPE_INIT_SUCCESS, null);
        }
    }

    public void joinRTC(String roomId, String userNo, int role) {
        getRtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        getRtcEngine().enableAudio();
        getRtcEngine().enableAudioVolumeIndication(30, 10, true);
        getRtcEngine().enableVideo();
        getRtcEngine().enableLocalVideo(false);
        getRtcEngine().setParameters("{\"rtc.audio.opensl.mode\":0}");
        getRtcEngine().setParameters("{\"rtc.audio_fec\":[3,2]}");
        getRtcEngine().setParameters("{\"rtc.audio_resend\":false}");
        getRtcEngine().setClientRole(role);
        int ret = getRtcEngine().joinChannelWithUserAccount("", roomId, userNo);
        if (ret != Constants.ERR_OK) {
            mLoggerRTC.e("joinRTC() called error " + ret);
        } else {
            mRTCEvent.onSingleCallback(RTC_TYPE_INIT_SUCCESS, null);
        }
    }

    private Integer mStreamId;

    /**
     * joinChannel之后只能创建5个，leaveChannel之后重置。
     */
    public Integer getStreamId() {
        if (mStreamId == null) {
            DataStreamConfig cfg = new DataStreamConfig();
            cfg.syncWithAudio = true;
            cfg.ordered = true;
            mStreamId = getRtcEngine().createDataStream(cfg);
        }
        return mStreamId;
    }

    public void leaveRTCRoom() {
        getRtcEngine().leaveChannel();
        mStreamId = null;
    }


    private final IRtcEngineEventHandler mIRtcEngineEventHandler = new IRtcEngineEventHandler() {

        @Override
        public void onConnectionStateChanged(int state, int reason) {
            super.onConnectionStateChanged(state, reason);
            mLoggerRTC.d("onConnectionStateChanged() called with: state = [%s], reason = [%s]", state, reason);
            if (state == Constants.CONNECTION_STATE_FAILED) {

            }
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            mLoggerRTC.i("onJoinChannelSuccess() called with: channel = [%s], uid = [%s], elapsed = [%s]", channel, uid, elapsed);
        }

        @Override
        public void onLeaveChannel(RtcStats stats) {
            super.onLeaveChannel(stats);
            mLoggerRTC.i("onLeaveChannel() called with: stats = [%s]", stats);
        }

//        private long receiveMessageTime = 0L;

        @Override
        public void onStreamMessage(int uid, int streamId, byte[] data) {
//            if (System.currentTimeMillis() - receiveMessageTime < 800) {
//                return;
//            }
//            receiveMessageTime = System.currentTimeMillis();
            JSONObject jsonMsg;
            try {
                String strMsg = new String(data);
                if (!strMsg.startsWith("{")) {
                    return;
                }
                jsonMsg = new JSONObject(strMsg);
                if (RoomManager.getInstance().mMusicModel == null)
                    return;

                if (jsonMsg.getString("cmd").equals("setLrcTime")) {
                    if (jsonMsg.has("lrcId") && !jsonMsg.getString("lrcId")
                            .equals(RoomManager.getInstance().mMusicModel.songNo)) {
                        return;
                    }
                    if (!jsonMsg.has("duration")) {
                        return;
                    }
                    long total = jsonMsg.getLong("duration");
                    long cur = jsonMsg.getLong("time");
                    RTCMessageBean msg = new RTCMessageBean();
                    msg.total = total;
                    msg.cur = cur;
                    RoomManager.getInstance().mMainThreadDispatch.onMusicProgress(total, cur);
                }
            } catch (JSONException exp) {
                exp.printStackTrace();
            }
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            for (AudioVolumeInfo info : speakers) {
                if (info.uid == 0 && info.voicePitch > 0) {
                    RoomManager.getInstance().mMainThreadDispatch.onLocalPitch(info.voicePitch);


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
    };
}

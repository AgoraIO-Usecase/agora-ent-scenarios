package io.agora.scene.ktv.manager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.ObjectsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.elvishew.xlog.Logger;
import com.elvishew.xlog.XLog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.agora.rtc2.Constants;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.api.apiutils.GsonUtils;
import io.agora.scene.base.api.model.User;
import io.agora.scene.base.bean.MemberMusicModel;
import io.agora.scene.base.component.ISingleCallback;
import io.agora.scene.base.data.model.AgoraMember;
import io.agora.scene.base.data.model.AgoraRoom;
import io.agora.scene.base.data.model.MusicModelNew;
import io.agora.scene.base.data.sync.AgoraException;
import io.agora.scene.base.event.MusicListChangeEvent;
import io.agora.scene.base.listener.EventListener;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;
import io.agora.scene.ktv.manager.bean.RTMMessageBean;

/**
 * 房间控制
 */
public final class RoomManager {
    private final Logger.Builder mLogger = XLog.tag("RoomManager");

    public static final int ROOM_TYPE_ON_MUSIC_EMPTY = 1001;
    public static final int ROOM_TYPE_ON_MUSIC_CHANGED = 1002;
    public static final int ROOM_TYPE_ON_MEMBER_JOINED_CHORUS = 1003;
    public static final int ROOM_TYPE_ON_MEMBER_APPLY_JOIN_CHORUS = 1004;
    public static final int ROOM_TYPE_ON_RTM_COUNT_UPDATE = 1005;
    public static final int ROOM_TYPE_ON_RTM_JOIN_SUCCESS = 0;

    private static class SingletonHolder {
        private static final RoomManager INSTANCE = new RoomManager();
    }

    public static RoomManager getInstance() {
        return RoomManager.SingletonHolder.INSTANCE;
    }

    public final MainThreadDispatch mMainThreadDispatch = new MainThreadDispatch();

    private final Map<String, AgoraMember> memberHashMap = new ConcurrentHashMap<>();

    public volatile static AgoraRoom mRoom = new AgoraRoom();
    public volatile static AgoraMember owner = new AgoraMember();
    public volatile static AgoraMember mMine = new AgoraMember();

    public volatile MemberMusicModel mMusicModel = new MemberMusicModel();

    private boolean isRTMSuccess = false;
    private boolean isRTCSuccess = false;

    public void loginOut() {
        mMine = null;
    }

    /**
     * 唱歌人的UserId
     */
    private final List<String> singers = new ArrayList<>();

    private RoomManager() {
        RTCManager.getInstance().initRTC();
    }

    private ISingleCallback<Integer, Object> iSingleCallback;

    public void setAgoraRoom(AgoraRoom room) {
        mRoom = room;
        setUserStatus();
    }

    /**
     * 加入房间
     */
    public void joinRoom(ISingleCallback<Integer, Object> iSingleCallback) {
        this.iSingleCallback = iSingleCallback;
        setUserStatus();
        joinRTM();
        joinRTC();
    }

    private void setUserStatus() {
        if (mMine == null) {
            mMine = new AgoraMember();
            mMine.roomId = mRoom;
            mMine.userNo = UserManager.getInstance().getUser().userNo;
            mMine.id = (long) UserManager.getInstance().getUser().id;
        } else {
            mMine.isMaster = false;
        }
    }

    public boolean isMyMusic() {
        if (mMusicModel == null) return false;
        return mMine.userNo.equals(mMusicModel.userNo);
    }


    private void joinRTM() {
        RTMManager.getInstance().joinRTMRoom(mRoom.roomNo);
    }

    private void joinRTC() {
        int role;
        mMine.role = AgoraMember.Role.Listener;
        if (UserManager.getInstance().getUser().userNo.equals(mRoom.creatorNo)) {
            mMine.isMaster = true;
            mMine.role = AgoraMember.Role.Owner;
            role = Constants.CLIENT_ROLE_BROADCASTER;
        } else if (mMine.role == AgoraMember.Role.Speaker) {
            role = Constants.CLIENT_ROLE_BROADCASTER;
        } else {
            role = Constants.CLIENT_ROLE_AUDIENCE;
        }
        RTCManager.getInstance().joinRTC(KtvConstant.RTC_TOKEN, mRoom.roomNo, mMine.getStreamId(), role);
    }

    public void loadMemberStatus() {
        for (AgoraMember value : memberHashMap.values()) {
            if (value.role == AgoraMember.Role.Speaker) {
                onMemberRoleChanged(value);
            }
        }
    }

    public boolean isOwner() {
        return mMine.role == AgoraMember.Role.Owner;
    }

    @Nullable
    public AgoraRoom getRoom() {
        return mRoom;
    }

    @Nullable
    public AgoraMember getOwner() {
        return owner;
    }

    @Nullable
    public AgoraMember getMine() {
        return mMine;
    }

    //是否是主唱
    public boolean isSinger(String userId) {
        return singers.contains(userId);
    }

    public void addRoomEventCallback(@NonNull RoomEventCallback callback) {
        mMainThreadDispatch.addRoomEventCallback(callback);
        RTMManager.getInstance().setRTMEvent(mRTMEvent);
        RTCManager.getInstance().setRTCEvent(mRTCEvent);
    }

    public void removeRoomEventCallback(@NonNull RoomEventCallback callback) {
        mMainThreadDispatch.removeRoomEventCallback(callback);
        RTMManager.getInstance().removeAllEvent();
        RTCManager.getInstance().removeAllEvent();
    }

    private void onMemberRoleChanged(@NonNull AgoraMember member) {
        mLogger.i("onMemberRoleChanged() called with: member = [%s]", member);
        mMainThreadDispatch.onRoleChanged(member);
    }

    private void onAudioStatusChanged(@NonNull AgoraMember member) {
        mLogger.i("onAudioStatusChanged() called with: member = [%s]", member);
        mMainThreadDispatch.onAudioStatusChanged(member);
    }

    private void onVideoStatusChanged(@NonNull AgoraMember member) {
        mLogger.i("onVideoStatusChanged() called with: member = [%s]", member);
        mMainThreadDispatch.onVideoStatusChanged(member);
    }

    public void onMusicAdd(MemberMusicModel model) {
        mLogger.i("onMusicAdd() called with: model = [%s]", model);
        if (musics.contains(model)) {
            return;
        }
        musics.add(model);
        liveDataMusics.postValue(musics);
        mMainThreadDispatch.onMusicAdd(model);
        if (mMusicModel == null) {
            onMusicChanged(musics.get(0));
        }
        EventBus.getDefault().post(new MusicListChangeEvent());
    }

    public void onTopMusic(String musicNo) {
        for (int i = 0; i < musics.size(); i++) {
            if (musics.get(i).songNo.equals(musicNo)) {
                MemberMusicModel music = musics.remove(i);
                musics.add(1, music);
//                Collections.swap(musics, i, 1);
                break;
            }
        }
        liveDataMusics.postValue(musics);
        //交换 推送
        RTMMessageBean bean = new RTMMessageBean();
        bean.headUrl = UserManager.getInstance().getUser().headUrl;
        bean.messageType = KtvConstant.MESSAGE_ROOM_TYPE_ON_SEAT;
        bean.userNo = UserManager.getInstance().getUser().userNo;
        bean.name = UserManager.getInstance().getUser().name;

        RTMManager.getInstance().sendMessage(GsonUtils.Companion.getGson().toJson(bean));
    }

    public void onMusicDelete(String musicNo, int position) {
        if (musics.get(position).songNo.equals(musicNo)) {
            musics.remove(position);
        }
        liveDataMusics.postValue(musics);
//        mMainThreadDispatch.onMusicDelete(musicNo);
        if (musics.size() > 0) {
            onMusicChanged(musics.get(0));
        } else {
            onMusicEmpty(true);
        }
        EventBus.getDefault().post(new MusicListChangeEvent());

//        //删歌 推送
//        RTMMessageBean bean = new RTMMessageBean();
//        bean.headUrl = UserManager.getInstance().getUser().headUrl;
//        bean.messageType = "1";
//        bean.userNo = UserManager.getInstance().getUser().userNo;
//        bean.name = UserManager.getInstance().getUser().name;
//
//        RTMManager.getInstance().sendMessage(GsonUtils.Companion.getGson().toJson(bean));
    }

    public void onMusicEmpty(boolean isUpdateUI) {
        mMusicModel = null;
        singers.clear();
        musics.clear();
        liveDataMusics.postValue(musics);
        if (isUpdateUI) {
            iSingleCallback.onSingleCallback(ROOM_TYPE_ON_MUSIC_EMPTY, null);
        }
    }

    public void onMusicChanged(MemberMusicModel model) {
        int index = musics.indexOf(model);
        if (index >= 0) {
            musics.set(index, model);
        }
        mMusicModel = model;
        if (mMusicModel.getType() == MemberMusicModel.SingType.Single) {
            singers.add(model.userNo);
        } else if (mMusicModel.getType() == MemberMusicModel.SingType.Chorus) {
            singers.add(model.userNo);
            singers.add(model.userId);
        }
        iSingleCallback.onSingleCallback(ROOM_TYPE_ON_MUSIC_CHANGED, null);

        //唱 下一首歌
//        RTMMessageBean bean = new RTMMessageBean();
//        bean.messageType = "100";
//        String json = GsonUtils.Companion.getGson().toJson(bean);
//        RTMManager.getInstance().sendMessage(json);
    }

    /**
     * 同意加入合唱
     */
    public void onMemberApplyJoinChorus(MemberMusicModel model) {
        mLogger.i("onMemberApplyJoinChorus() called with: model = [%s]", model);
        int index = musics.indexOf(model);
        if (index >= 0) {
            musics.set(index, model);
        }
        mMusicModel = model;
        mMainThreadDispatch.onMemberApplyJoinChorus(model);


//        //同意加入合唱
//        RTMMessageBean bean = new RTMMessageBean();
//        bean.messageType = RoomLivingViewModel.MESSAGE_ROOM_TYPE_APPLY_JOIN_CHORUS;
//        bean.userNo = UserManager.getInstance().getUser().userNo;
//        bean.name = UserManager.getInstance().getUser().name;
//        bean.songNo = model.songNo;
//        bean.bgUid = mMusicModel.userbgId;
//
//        RTMManager.getInstance().sendMessage(GsonUtils.Companion.getGson().toJson(bean));

    }

    public void onMemberJoin(@NonNull AgoraMember member) {
        memberHashMap.put(member.userNo, member);
        mMainThreadDispatch.onMemberJoin(member);
    }

    public void onMemberLeave(@NonNull AgoraMember member) {
        mMainThreadDispatch.onMemberLeave(member);
        memberHashMap.remove(member.userNo);
    }

    public void onMemberJoinedChorus(MemberMusicModel model) {
        Log.d("cwtsw", "多人 room manager onMemberJoinedChorus");
        mLogger.i("onMemberJoinedChorus() called with: model = [%s]", model);
        int index = musics.indexOf(model);
        if (index >= 0) {
            musics.set(index, model);
        }
        mMusicModel = model;
        iSingleCallback.onSingleCallback(ROOM_TYPE_ON_MEMBER_JOINED_CHORUS, model);
        mMainThreadDispatch.onMemberJoinedChorus(model);
    }

    public void onMemberChorusReady(MemberMusicModel model) {
        mLogger.i("onMemberChorusReady() called with: model = [%s]", model);
        int index = musics.indexOf(model);
        if (index >= 0) {
            musics.set(index, model);
        }
        mMusicModel = model;
        mMainThreadDispatch.onMemberChorusReady(model);
    }

    private final EventListener mRTMEvent = new EventListener() {

        @Override
        public void onSuccess() {
            isRTMSuccess = true;
            iSingleCallback.onSingleCallback(ROOM_TYPE_ON_RTM_JOIN_SUCCESS, null);
        }

        @Override
        public void onReceive() {
            iSingleCallback.onSingleCallback(ROOM_TYPE_ON_RTM_COUNT_UPDATE, null);
        }

        @Override
        public void onError(String error) {
            ToastUtils.showToast(error);
        }

        @Override
        public void onSubscribeError(AgoraException ex) {
            mLogger.e("mRoomEvent() called with: ex = [%s]", String.valueOf(ex));
        }
    };
    private final ISingleCallback<Integer, Object> mRTCEvent = new ISingleCallback<Integer, Object>() {

        @Override
        public void onSingleCallback(Integer type, Object o) {
            if (type == RTCManager.RTC_TYPE_INIT_SUCCESS) {
                isRTCSuccess = true;
            }
            iSingleCallback.onSingleCallback(type, o);
        }
    };

    private final ArrayList<MemberMusicModel> musics = new ArrayList<>();

    private final MutableLiveData<ArrayList<MemberMusicModel>> liveDataMusics = new MutableLiveData<>();

    public LiveData<ArrayList<MemberMusicModel>> getLiveDataMusics() {
        return liveDataMusics;
    }

    public List<MemberMusicModel> getMusics() {
        return musics;
    }

    @Nullable
    public MemberMusicModel getMusicModel() {
        return mMusicModel;
    }

    public boolean isMainSinger() {
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return false;
        }

        if (mMusicModel == null) {
            return false;
        }

        return ObjectsCompat.equals(mMusicModel.userNo, mUser.userNo);
    }

    public boolean isFollowSinger() {
        User mUser = UserManager.getInstance().getUser();
        if (mUser == null) {
            return false;
        }

        if (mMusicModel == null) {
            return false;
        }

        return ObjectsCompat.equals(mMusicModel.userId, mUser.userNo);
    }

    public boolean isMainSinger(@NonNull AgoraMember member) {
        if (mMusicModel == null) {
            return false;
        }

        return ObjectsCompat.equals(mMusicModel.userNo, member.userNo);
    }

    public boolean isInMusicOrderList(MusicModelNew item) {
        for (MemberMusicModel music : musics) {
            if (ObjectsCompat.equals(music.songNo, String.valueOf(item.songNo))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 成功加入房间
     */
    private void onJoinRoom() {
        mMusicModel = null;
    }

    public void leaveRoom() {
        RTCManager.getInstance().leaveRTCRoom();
        RTMManager.getInstance().levelRTMRoom();
        memberHashMap.clear();
        singers.clear();
    }

    //同步用户状态
    public void memberEventUpData(AgoraMember memberRemote) {
        AgoraMember memberLocal = memberHashMap.get(memberRemote.userNo);
        if (memberLocal != null && memberLocal.role != memberRemote.role) {
            memberLocal.role = memberRemote.role;
            onMemberRoleChanged(memberLocal);
        }

        if (memberLocal != null && memberLocal.isSelfMuted != memberRemote.isSelfMuted) {
            memberLocal.isSelfMuted = memberRemote.isSelfMuted;
            onAudioStatusChanged(memberLocal);
        }

        if (memberLocal != null && memberLocal.isVideoMuted != memberRemote.isVideoMuted) {
            memberLocal.isVideoMuted = memberRemote.isVideoMuted;
            onVideoStatusChanged(memberLocal);
        }
    }

}

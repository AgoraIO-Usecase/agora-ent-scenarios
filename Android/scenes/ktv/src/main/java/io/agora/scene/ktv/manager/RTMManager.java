package io.agora.scene.ktv.manager;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.List;

import io.agora.rtm.ErrorInfo;
import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmFileMessage;
import io.agora.rtm.RtmImageMessage;
import io.agora.rtm.RtmMessage;
import io.agora.scene.base.KtvConstant;
import io.agora.scene.base.agora.ChatManager;
import io.agora.scene.base.component.AgoraApplication;
import io.agora.scene.base.event.ReceivedMessageEvent;
import io.agora.scene.base.listener.EventListener;
import io.agora.scene.base.manager.UserManager;
import io.agora.scene.base.utils.ToastUtils;

/**
 * RTM控制
 */
public final class RTMManager implements RtmChannelListener {
    private static final String TAG = RTMManager.class.getSimpleName();

    private static class SingletonHolder {
        private static final RTMManager INSTANCE = new RTMManager();
    }

    public static RTMManager getInstance() {
        return RTMManager.SingletonHolder.INSTANCE;
    }

    private ChatManager mChatManager;
    public RtmClient mRtmClient;

    private RTMManager() {
        mChatManager = new ChatManager(AgoraApplication.the());
        mChatManager.init();
        mRtmClient = mChatManager.getRtmClient();
    }

    private EventListener mRTMEvent;

    public void setRTMEvent(EventListener roomEvent) {
        this.mRTMEvent = roomEvent;
//        AgoraApplication.the().getChatManager().registerListener(this);
    }

    public void removeAllEvent() {
        this.mRTMEvent = null;
//        AgoraApplication.the().getChatManager().unregisterListener(this);
    }

    /**
     * rtm 房间信息
     */
    public RtmChannel mRtmChannel;

    /**
     * 通过后台返回的channel id来创建并加入频道
     * 此处调用后台接口来创建不再app端创建
     */
    public void createRTMRoom(String roomId) {
        mRtmChannel = mRtmClient.createChannel(roomId, this);
    }

    /**
     * 加入rtm 房间
     */
    public void joinRTMRoom(String roomId) {
        createRTMRoom(roomId);
        mRtmChannel.join(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                mRTMEvent.onSuccess();
                Log.d(TAG, "joinRTMRoom success roomId = " + roomId);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                if (errorInfo.getErrorCode() == 6) {
                    //已经加入了
                    mRTMEvent.onSuccess();
                } else {
                    mRTMEvent.onError(errorInfo.getErrorDescription() + " errorCode = " + errorInfo.getErrorCode());
                }
                Log.d(TAG, "joinRTMRoom onFailure errorInfo = " + errorInfo.getErrorDescription());
            }
        });
    }

    /**
     * 离开rtm 房间 暂不使用
     */
    public void levelRTMRoom() {
        mRtmChannel.leave(new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                ToastUtils.showToast(errorInfo.getErrorDescription());
            }
        });
    }

    /**
     * 推送消息
     */
    public void sendMessage(String json) {
        RtmMessage message = mRtmClient.createMessage();
        message.setRawMessage(json.getBytes(StandardCharsets.UTF_8));
        message.setText("send");
        mRtmChannel.sendMessage(message, new ResultCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d(TAG, "sendMessage success " + json);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                ToastUtils.showToast(errorInfo.getErrorDescription());
            }
        });

    }

    /**
     * 登录到RTM
     */
    public void doLoginRTM() {
        mRtmClient.login(
                KtvConstant.RTM_TOKEN,
                UserManager.getInstance().getUser().id.toString(), new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void responseInfo) {
                        //登录成功
                        Log.d(TAG, "doLoginRTM success");
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        ToastUtils.showToast(errorInfo.getErrorDescription());
                        //登录失败
                    }
                });
    }

    /**
     * 退出RTM
     */
    public void doLogoutRTM() {
        mRtmClient.logout(null);
    }

    private int memberCount;

    public int getMemberCount() {
        if (memberCount == 0) {
            memberCount = 1;
        }
        return memberCount;
    }

    /**
     * 频道成员人数更新回调。返回最新频道成员人数
     *
     * @param memberCount 房间人数
     */
    @Override
    public void onMemberCountUpdated(int memberCount) {
        this.memberCount = memberCount;
        mRTMEvent.onReceive();
        Log.d(TAG, "onMemberCountUpdated memberCount = " + memberCount);
    }

    /**
     * 当频道属性更新时返回当前频道的所有属性
     *
     * @param list 频道的所有属性
     */
    @Override
    public void onAttributesUpdated(List<RtmChannelAttribute> list) {
        Log.d(TAG, "onAttributesUpdated list = " + list);
    }

    /**
     * 收到频道消息回调 更新页面状态
     *
     * @param rtmMessage       消息内容`
     * @param rtmChannelMember 成员信息
     */
    @Override
    public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember rtmChannelMember) {
        EventBus.getDefault().post(new ReceivedMessageEvent(new String(rtmMessage.getRawMessage())));
    }

    /**
     * 收到频道图片消息回调
     * 该方法已废弃，Agora 建议你不要使用。
     */
    @Override
    public void onImageMessageReceived(RtmImageMessage rtmImageMessage, RtmChannelMember rtmChannelMember) {

    }

    /**
     * 收到频道文件消息回调。
     * 该方法已废弃，Agora 建议你不要使用。
     */
    @Override
    public void onFileMessageReceived(RtmFileMessage rtmFileMessage, RtmChannelMember rtmChannelMember) {

    }

    /**
     * 远端用户加入频道回调。
     *
     * @param rtmChannelMember 用户信息
     */
    @Override
    public void onMemberJoined(RtmChannelMember rtmChannelMember) {
        Log.d(TAG, "onMemberJoined rtmChannelMember = " + rtmChannelMember.getUserId());
    }

    /**
     * 频道成员离开频道回调。
     *
     * @param rtmChannelMember 用户信息
     */
    @Override
    public void onMemberLeft(RtmChannelMember rtmChannelMember) {
        Log.d(TAG, "onMemberLeft rtmChannelMember = " + rtmChannelMember.getUserId());
    }

//    /**
//     * SDK 与 Agora RTM 系统的连接状态发生改变回调
//     *
//     * @param state  int 	CONNECTION_STATE_DISCONNECTED = 1 初始状态。SDK 未连接到 Agora RTM 系统
//     *               int 	CONNECTION_STATE_CONNECTING = 2 SDK 正在登录 Agora RTM 系统。
//     *               int 	CONNECTION_STATE_CONNECTED = 3 SDK 已登录 Agora RTM 系统。
//     *               int 	CONNECTION_STATE_RECONNECTING = 4 SDK 与 Agora RTM 系统连接由于网络原因出现中断，SDK 正在尝试自动重连 Agora RTM 系统。
//     *               int 	CONNECTION_STATE_ABORTED = 5 SDK 停止登录 Agora RTM 系统。
//     *               <p>
//     * @param reason int 	CONNECTION_CHANGE_REASON_LOGIN = 1 SDK 正在登录 Agora RTM 系统
//     *               int 	CONNECTION_CHANGE_REASON_LOGIN_SUCCESS = 2 SDK 登录 Agora RTM 系统成功
//     *               int 	CONNECTION_CHANGE_REASON_LOGIN_FAILURE = 3 SDK 登录 Agora RTM 系统失败
//     *               <p>
//     *               int 	CONNECTION_CHANGE_REASON_LOGIN_TIMEOUT = 4 SDK 无法登录 Agora RTM 系统超过 12 秒，停止登录。
//     *               <t>    可能原因：用户正处于 CONNECTION_STATE_ABORTED 状态或 CONNECTION_STATE_RECONNECTING 状态。
//     *               <p>
//     *               int 	CONNECTION_CHANGE_REASON_INTERRUPTED = 5 SDK 与 Agora RTM 系统的连接被中断
//     *               int 	CONNECTION_CHANGE_REASON_LOGOUT = 6 用户已调用 logout() 方法登出 Agora RTM 系统
//     *               int 	CONNECTION_CHANGE_REASON_BANNED_BY_SERVER = 7 SDK 被服务器禁止登录 Agora RTM 系统
//     *               int 	CONNECTION_CHANGE_REASON_REMOTE_LOGIN = 8 另一个用户正以相同的用户 ID 登录 Agora RTM 系统
//     */
//    @Override
//    public void onConnectionStateChanged(int state, int reason) {
//        Log.d(TAG, "onConnectionStateChanged state = " + state);
//        Log.d(TAG, "onConnectionStateChanged reason = " + reason);
//    }
//
//    /**
//     * 收到点对点消息回调。
//     *
//     * @param rtmMessage 消息
//     * @param s          消息发送者的用户 ID
//     */
//    @Override
//    public void onMessageReceived(RtmMessage rtmMessage, String s) {
//        Log.d(TAG, "onMessageReceived state = " + rtmMessage);
//    }
//
//    @Deprecated
//    @Override
//    public void onImageMessageReceivedFromPeer(RtmImageMessage rtmImageMessage, String s) {
//        //弃用
//    }
//
//    @Deprecated
//    @Override
//    public void onFileMessageReceivedFromPeer(RtmFileMessage rtmFileMessage, String s) {
//        //弃用
//    }
//
//    @Deprecated
//    @Override
//    public void onMediaUploadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {
//        //弃用
//    }
//
//    @Deprecated
//    @Override
//    public void onMediaDownloadingProgress(RtmMediaOperationProgress rtmMediaOperationProgress, long l) {
//        //弃用
//    }
//
//    /**
//     * （SDK 断线重连时触发）当前使用的 RTM Token 已超过 24 小时的签发有效期。
//     */
//    @Override
//    public void onTokenExpired() {
//        Log.d(TAG, "onTokenExpired ");
//        //登录过期后重新登录
////        mIsInChat = false;
////        doLogout();
////        doLogin();
//    }
//
//    /**
//     * 被订阅用户在线状态改变回调。
//     * <p>
//     * 首次订阅在线状态成功时，SDK 也会返回本回调，显示所有被订阅用户的在线状态。
//     * 每当被订阅用户的在线状态发生改变，SDK 都会通过该回调通知订阅方。
//     * 如果 SDK 在断线重连过程中有被订阅用户的在线状态发生改变，SDK 会在重连成功时通过该回调通知订阅方。
//     *
//     * @param map
//     */
//    @Override
//    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {
//        Log.d(TAG, "onPeersOnlineStatusChanged map " + map);
//    }
}

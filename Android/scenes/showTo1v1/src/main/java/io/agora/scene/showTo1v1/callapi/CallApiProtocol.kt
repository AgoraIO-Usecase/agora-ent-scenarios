package io.agora.scene.showTo1v1.callapi

import android.view.ViewGroup
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

open class CallConfig(
    //声网App Id
    var appId: String = "",
    //用户id，通过该用户id来发送信令消息
    var userId: Int = 0,
    //rtc engine实例
    var rtcEngine: RtcEngineEx? = null,
    //[可选]rtm client实例，如果设置则需要负责rtmClient的login和logout，需要使用appId和userId创建
    var rtmClient: RtmClient? = null,
){}

open class PrepareConfig(
    var roomId: String = "",                      // 频道名(主叫需要设置为1v1的频道，被叫可设置为自己的广播频道)
    var rtcToken: String = "",                    // rtc token，需要使用万能token，token创建的时候channel name为空字符串
    var rtmToken: String = "",                    // rtm token
    var localView: ViewGroup? = null,             // 显示本地流的画布
    var remoteView: ViewGroup? = null,            // 显示远端流的画布
    var autoJoinRTC: Boolean = false,             // 是否自动登录RTC
    var callTimeoutMillisecond: Long = 15000L,    // 呼叫超时时间，单位毫秒，如果传0内部将不做超时逻辑
    var userExtension: Map<String, Any>? = null   // [可选]用户扩展字段，收到对端消息而改变状态(例如calling/connecting)时可以通过kFromUserExtension字段获取
) {}

/**
 * 呼叫状态类型
 */
enum class CallStateType(val value: Int) {
    Idle(0),            // 空闲
    Prepared(1),        // 创建1v1环境完成
    Calling(2),         // 呼叫中
    Connecting(3),      // 连接中
    Connected(4),       // 通话中
    Failed(10);         // 出现错误

    companion object {
        fun fromValue(value: Int): CallStateType {
            return values().find { it.value == value } ?: Idle
        }
    }
}

/*
 * 呼叫状态变迁原因
 */
enum class CallStateReason(val value: Int) {
    None(0),
    JoinRTCFailed(1),           // 加入RTC失败
    RtmSetupFailed(2),          // 设置RTM失败
    RtmSetupSuccessed(3),       // 设置RTM成功
    MessageFailed(4),           // 消息发送失败
    LocalRejected(5),           // 本地用户拒绝
    RemoteRejected(6),          // 远端用户拒绝
    RemoteAccepted(7),          // 远端用户接受
    LocalAccepted(8),           // 本地用户接受
    LocalHangup(9),             // 本地用户挂断
    RemoteHangup(10),           // 远端用户挂断
    LocalCancel(11),            // 本地用户取消呼叫
    RemoteCancel(12),           // 远端用户取消呼叫
    RecvRemoteFirstFrame(13),   // 收到远端首帧
    CallingTimeout (14),        // 呼叫超时
    CancelByCallerRecall(15),   // 同样的主叫呼叫不同频道导致取消
    RtmLost(16),                // rtm超时断连
    RemoteCallBusy(17)          // 远端用户忙
}

/*
 * 呼叫事件
 */
enum class CallEvent(val value: Int) {
    None(0),
    Deinitialize(1),                // 调用了deinitialize
    //MissingReceipts(2),             // 没有收到消息回执[已废弃]
    CallingTimeout(3),              // 呼叫超时
    //JoinRTCFailed(4),               // 加入RTC失败[已废弃，请使用onCallErrorOccur(state: rtcOccurError)]
    JoinRTCSuccessed(5),            // 加入RTC成功
    //RtmSetupFailed(6),                  // 设置RTM失败[已废弃，请使用onCallErrorOccur(state: rtmSetupFail)]
    RtmSetupSuccessed(7),           // 设置RTM成功
    //MessageFailed(8),                   // 消息发送失败[已废弃，请使用onCallErrorOccur(state: sendMessageFail)]
    StateMismatch(9),               // 状态流转异常
    //PreparedRoomIdChanged(10),      // prepared了另一个roomId导致[已废弃]
    RemoteUserRecvCall(99),         // 主叫呼叫成功
    LocalRejected(100),             // 本地用户拒绝
    RemoteRejected(101),            // 远端用户拒绝
    OnCalling(102),                 // 变成呼叫中
    RemoteAccepted(103),            // 远端用户接收
    LocalAccepted(104),             // 本地用户接收
    LocalHangup(105),               // 本地用户挂断
    RemoteHangup(106),              // 远端用户挂断
    RemoteJoin(107),                // 远端用户加入RTC频道
    RemoteLeave(108),               // 远端用户离开RTC频道
    LocalCancel(109),               // 本地用户取消呼叫
    RemoteCancel(110),              // 远端用户取消呼叫
    LocalJoin(111),                 // 本地用户加入RTC频道
    LocalLeave(112),                // 本地用户离开RTC频道
    RecvRemoteFirstFrame(113),      // 收到远端首帧
    //CancelByCallerRecall(114),      // 同样的主叫呼叫不同频道导致取消[已废弃]
    RtmLost(115),                   // rtm超时断连
    //RtcOccurError(116),             // rtc出现错误[已废弃，请使用onCallErrorOccur(state: rtcOccurError)]
    RemoteCallBusy(117),            // 远端用户忙
    //StartCaptureFail(118),          // 开启采集失败[已废弃，请使用onCallErrorOccur(state: startCaptureFail)]
    CaptureFirstLocalVideoFrame(119),       //采集到首帧视频帧
    PublishFirstLocalVideoFrame(120)        //推送首帧视频帧成功
}

/*
 * 呼叫错误事件
 */
enum class CallErrorEvent(val value: Int) {
    NormalError(0),         // 通用错误
    RtcOccurError(100),     // rtc出现错误
    StartCaptureFail(110),  // rtc开启采集失败
    RtmSetupFail(200),      // rtm初始化失败
    SendMessageFail(210)    // 消息发送失败
}

/*
 * 呼叫错误事件的错误码类型
 */
enum class CallErrorCodeType(val value: Int) {
    Normal(0),   // 业务类型的错误，暂无
    Rtc(1),      // rtc的错误，使用AgoraErrorCode
    Rtm(2)       // rtm的错误，使用AgoraRtmErrorCode
}

/*
 * 日志等级
 */
enum class CallLogLevel(val value: Int) {
    Normal(0),
    Warning(1),
    Error(2),
}

interface ICallApiListener {
    /**
     * 状态响应回调
     * @param state 状态类型
     * @param stateReason 状态变更的原因
     * @param eventReason 事件类型描述
     * @param eventInfo 扩展信息，不同事件类型参数不同，其中key为“publisher”为状态变更者id，空则表示是自己的状态变更
     */
    fun onCallStateChanged(state: CallStateType,
                           stateReason: CallStateReason,
                           eventReason: String,
                           eventInfo: Map<String, Any>)

    /**
     * 内部详细事件变更回调
     * @param event: 事件
     */
    fun onCallEventChanged(event: CallEvent) {}

    /**
     * 内部详细事件变更回调
     * @param errorEvent: 错误事件
     * @param errorType: 错误类型
     * @param errorCode: 错误码
     * @param message: 错误信息
     */
    fun onCallError(errorEvent: CallErrorEvent,
                    errorType: CallErrorCodeType,
                    errorCode: Int,
                    message: String?) {}

    /**
     * token快要过期了(需要外部获取新token调用renewToken更新)
     */
    fun tokenPrivilegeWillExpire() {}

    /** 日志回调
     *  @param message: 日志信息
     *  @param logLevel: 日志优先级: 0: 普通日志，1: 警告日志, 2: 错误日志
     */
    fun callDebugInfo(message: String, logLevel: CallLogLevel) {}
}

data class AGError(
    val msg: String,
    val code: Int
)

interface ICallApi {

    /**
     * 初始化配置
     * @param config
     */
    fun initialize(config: CallConfig)

    /**
     * 释放缓存
     */
    fun deinitialize(completion: (() -> Unit))

    /**
     * 更新自己的rtc/rtm的token
     */
    fun renewToken(rtcToken: String, rtmToken: String)

    /**
     * 准备通话环境，需要调用成功才可以进行呼叫，如需要更换通话的RTC 频道号可以重复调用，确保调用时必须是非通话状态(非calling、connecting、connected)才可调用成功
     * @param prepareConfig
     * @param completion
     */
    fun prepareForCall(prepareConfig: PrepareConfig, completion: ((AGError?) -> Unit)?)

    /**
     * 添加回调的listener
     * @param listener
     */
    fun addListener(listener: ICallApiListener)

    /**
     * 移除回调的listener
     * @param listener
     */
    fun removeListener(listener: ICallApiListener)

    /**
     * 发起通话，主叫调用，通过prepareForCall设置的RTC频道号和远端用户建立RTC通话连接
     * @param remoteUserId 呼叫的用户id
     * @param completion
     */
    fun call(remoteUserId: Int, completion: ((AGError?) -> Unit)?)

    /**
     * 取消正在发起的通话，主叫调用
     * @param completion
     */
    fun cancelCall(completion: ((AGError?) -> Unit)?)

    /** 接受通话，调用后主叫会收到onAccept
     *
     * @param remoteUserId: 呼叫的用户id
     * @param completion: <#completion description#>
     */
    fun accept(remoteUserId: Int, completion: ((AGError?) -> Unit)?)

    /**
     * 接受通话，被叫调用
     * @param remoteUserId 呼叫的用户id
     * @param reason 拒绝原因
     * @param completion
     */
    fun reject(remoteUserId: Int, reason: String?, completion: ((AGError?) -> Unit)?)

    /**
     * 结束通话，主叫和被叫均可调用
     * @param remoteUserId 挂断的用户id
     * @param reason 挂断原因
     * @param completion
     */
    fun hangup(remoteUserId: Int, reason: String?, completion: ((AGError?) -> Unit)?)

    /**
     * 获取当前通话的callId，callId为当次通话过程中唯一标识，通过该标识声网后台服务可以查询到当前通话的关键节点耗时和状态变迁的时间节点
     * @return callId，非呼叫到通话之外的消息为空
     */
    fun getCallId(): String
}
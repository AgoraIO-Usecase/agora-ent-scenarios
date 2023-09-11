package io.agora.scene.showTo1v1.callAPI

import android.view.TextureView
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngineEx
import io.agora.scene.base.component.AgoraApplication

enum class CallRole(val value: Int) {
    // 被叫
    CALLEE(0),
    // 主叫
    CALLER(1)
}

/// 模式
enum class CallMode(val value: Int) {
    ShowTo1v1(0),       //秀场转1v1
    Pure1v1(1)          //纯1v1
}

open class CallConfig(
    //声网App Id
    var appId: String = "",
    //用户id
    var userId: Int = 0,
    //[可选]用户扩展字段,用在呼叫上，对端收到calling时可以通过kFromUserExtension字段读到
    var userExtension: Map<String, Any>? = null,
    //rtc engine实例
    var rtcEngine: RtcEngineEx? = null,
    // 模式
    var mode: CallMode = CallMode.ShowTo1v1,
    //角色，纯1v1需要设置成caller
    var role: CallRole = CallRole.CALLEE,
    //显示本地流的画布
    var localView: TextureView,
    //显示远端流的画布
    var remoteView: TextureView,
    //是否收到被叫后自动接受，秀场转1v1可用
    var autoAccept: Boolean = true,
){}

open class PrepareConfig {
    var autoLoginRTM: Boolean = true        //是否自动登录RTM
    var autoSubscribeRTM: Boolean = true    //是否自动订阅RTM，如果为true，则autoLoginRTM必定为true
    var autoJoinRTC: Boolean = false        //是否自动登录RTC

    companion object {
        //主叫默认配置
        fun callerConfig(): PrepareConfig {
            val config = PrepareConfig()
            config.autoLoginRTM = false
            config.autoSubscribeRTM = false
            return config
        }
        /// 被叫默认配置
        /// - Returns: <#description#>
        fun calleeConfig(): PrepareConfig {
            return PrepareConfig()
        }

    }
}

/** token renew时的配置
 */
open class CallTokenConfig {
    var roomId: String = ""     //频道名(主叫需要设置为1v1的频道，被叫可设置为自己的广播频道)
    var rtcToken: String = ""   // rtc token，被叫需要使用万能token
    var rtmToken: String = ""   // rtm token
}
enum class CallReason(val value: Int) {
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
    CallingTimeout (14)         // 呼叫超时
}

enum class CallEvent(val value: Int) {
    None(0),
    Deinitialize(1),                // 调用了deinitialize
    MissingReceipts(2),             // 没有收到消息回执
    CallingTimeout(3),              // 呼叫超时
    JoinRTCFailed(4),               // 加入RTC失败
    JoinRTCSuccessed(5),            // 加入RTC成功
    RtmSetupFailed(6),              // 设置RTM失败
    RtmSetupSuccessed(7),           // 设置RTM成功
    MessageFailed(8),               // 消息发送失败
    StateMismatch(9),               // 状态流转异常
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
    RecvRemoteFirstFrame(113)       // 收到远端首帧
}

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

interface ICallApiListener {
    /**
     * 状态响应回调
     * @param state 状态类型
     * @param stateReason 状态原因
     * @param eventReason 事件类型描述
     * @param elapsed 从触发到回调的耗时(只有呼叫到通话中间事件可统计)
     * @param eventInfo 扩展信息，不同事件类型参数不同，其中key为“publisher”为状态变更者id，空则表示是自己的状态变更
     */
    fun onCallStateChanged(state: CallStateType,
                           stateReason: CallReason,
                           eventReason: String,
                           elapsed: Long,
                           eventInfo: Map<String, Any>)

    /**
     * 内部详细事件变更回调
     * @param event: 事件
     * @param elapsed: 耗时(只有呼叫到通话中间事件可统计)
     */
    fun onCallEventChanged(event: CallEvent, elapsed: Long) {}

    /** token快要过期了(需要外部获取新token调用renewToken更新)
     */
    fun tokenPrivilegeWillExpire() {}
}

data class AGError(
    val msg: String,
    val code: Int
)

interface ICallApi {

    companion object {
        fun getImplInstance(): ICallApi {
            return instance
        }


        private val instance by lazy {
            CallApiImpl(AgoraApplication.the())
        }

    }

    /** 初始化配置 */
    fun initialize(config: CallConfig, token: CallTokenConfig, completion: ((AGError?) -> Unit))

    /** 释放缓存 */
    fun deinitialize(completion: (() -> Unit))

    /** 更新 rtc/rtm 的token*
     */
    fun renewToken(config: CallTokenConfig)

    /** 更新呼叫token
     *
     * @param roomId
     * @param token
     */
    fun renewRemoteCallerChannelToken(roomId: String, token: String)

    /** 连接(对RTM进行login和subscribe)， 观众调用
     *
     * @param prepareConfig
     * @param completion
     */
    fun prepareForCall(prepareConfig: PrepareConfig, completion: ((AGError?) -> Unit)?)

    /** 监听远端处理的回调
     *
     * @param listener
     */
    fun addListener(listener: ICallApiListener)

    /** 取消监听远端回调
     * @param listener
     */
    fun removeListener(listener: ICallApiListener)

    /** 发起通话，加 RTC 频道并且发流，并且发 rtm 频道消息 申请链接，调用后被叫会收到onCall
     *
     * @param roomId 目标主播频道号
     * @param remoteUserId 呼叫的用户id
     * @param completion
     */
    fun call(roomId: String, remoteUserId: Int, completion: ((AGError?) -> Unit)?)

    /** 取消正在发起的通话，未接通的时候可用，调用后被叫会收到onCancel
     *
     * @param completion
     */
    fun cancelCall(completion: ((AGError?) -> Unit)?)

    /** 接受通话，调用后主叫会收到onAccept
     *
     * @param roomId: 频道号
     * @param remoteUserId: 呼叫的用户id
     * @param rtcToken: roomId对应的rtc token
     * @param completion: <#completion description#>
     */
    fun accept(roomId: String, remoteUserId: Int, rtcToken: String, completion: ((AGError?) -> Unit)?)

    /** 被叫拒绝通话，调用后主叫会收到onReject
     *
     * @param roomId 频道号
     * @param remoteUserId 呼叫的用户id
     * @param reason 拒绝原因
     * @param completion
     */
    fun reject(roomId: String, remoteUserId: Int, reason: String?, completion: ((AGError?) -> Unit)?)

    /** 结束通话，调用后被叫会收到onHangup
     * @param roomId 频道号
     * @param completion
     */
    fun hangup(roomId: String, completion: ((AGError?) -> Unit)?)

    /** 获取callId，callId为通话过程中消息的标识，通过argus可以查询到从呼叫到通话的耗时和状态变迁的时间戳
     * @return callId，非呼叫到通话之外的消息为空
     */
    fun getCallId(): String

    /** 添加RTC接口回调 */
    fun addRTCListener(listener: IRtcEngineEventHandler)
    /** 移除RTC接口回调 */
    fun removeRTCListener(listener: IRtcEngineEventHandler)
}
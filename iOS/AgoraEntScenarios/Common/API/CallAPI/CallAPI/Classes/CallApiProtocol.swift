//
//  CallApiProtocol.swift
//  CallAPI
//
//  Created by Agora on 2023/5/18.
//

import Foundation
import AgoraRtcKit
import AgoraRtmKit

/// 初始化配置信息
@objc public class CallConfig: NSObject {
    public var appId: String = ""               //声网App Id
    public var userId: UInt = 0                 //用户id
    public var rtcEngine: AgoraRtcEngineKit!    //rtc engine实例
    public var rtmClient: AgoraRtmClientKit?    //[可选]rtm client实例，如果设置则需要负责rtmClient的login和logout，需要使用appId和userId创建
}

//TODO: 如何不设置万能token
@objc public class PrepareConfig: NSObject {
    public var roomId: String = ""                      //自己的RTC频道名，用于呼叫对端用户时让对端用户进入加入这个RTC频道
    public var rtcToken: String = ""                    //rtc token，需要使用万能token，token创建的时候channel name为空字符串
    public var rtmToken: String = ""                    //rtm token
    public var localView: UIView!                       //显示本地流的画布
    public var remoteView: UIView!                      //显示远端流的画布
    public var autoJoinRTC: Bool = false                //是否在不呼叫的情况下提前加入自己的RTC频道，该设置可以加快呼叫的出图速度
    public var callTimeoutMillisecond: UInt64 = 15000   //呼叫超时时间，单位豪秒，0表示内部不处理超时
    public var userExtension: [String: Any]?            //[可选]用户扩展字段，收到对端消息而改变状态(例如calling/connecting)时可以通过kFromUserExtension字段获取
}

/// 呼叫状态
@objc public enum CallStateType: UInt {
    case idle = 0            //未知
    case prepared = 1        //空闲
    case calling = 2         //呼叫中
    case connecting = 3      //连接中
    case connected = 4       //通话中
    case failed = 10         //出现错误
}

/// 呼叫状态变迁原因
@objc public enum CallStateReason: UInt {
    case none = 0
    case joinRTCFailed = 1         //加入RTC失败
    case rtmSetupFailed = 2        //设置RTM失败
    case rtmSetupSuccessed = 3     //设置RTM成功
    case messageFailed = 4         //消息发送失败
    case localRejected = 5         //本地用户拒绝
    case remoteRejected = 6        //远端用户拒绝
    case remoteAccepted = 7        //远端用户接受
    case localAccepted = 8         //本地用户接受
    case localHangup = 9           //本地用户挂断
    case remoteHangup = 10         //远端用户挂断
    case localCancel = 11          //本地用户取消呼叫
    case remoteCancel = 12         //远端用户取消呼叫
    case recvRemoteFirstFrame = 13 //收到远端首帧
    case callingTimeout = 14       //呼叫超时
    case cancelByCallerRecall = 15 //同样的主叫呼叫不同频道导致取消
    case rtmLost = 16              //rtm超时断连
    case remoteCallBusy = 17       //远端用户忙
}

/// 呼叫事件
@objc public enum CallEvent: UInt {
    case none = 0
    case deinitialize = 1                         //调用了deinitialize
//    case missingReceipts = 2                      //没有收到消息回执[已废弃]
    case callingTimeout = 3                       //呼叫超时
//    case joinRTCFailed = 4                        //加入RTC失败[已废弃，请使用onCallErrorOccur(state: rtcOccurError)]
    case joinRTCSuccessed = 5                     //加入RTC成功
//    case rtmSetupFailed = 6                       //设置RTM失败[已废弃，请使用onCallErrorOccur(state: rtmSetupFail)]
    case rtmSetupSuccessed = 7                    //设置RTM成功
//    case messageFailed = 8                        //消息发送失败[已废弃，请使用onCallErrorOccur(state: sendMessageFail)]
    case stateMismatch = 9                        //状态流转异常
//    case preparedRoomIdChanged = 10               //prepared了另一个roomId[已废弃]
    case remoteUserRecvCall = 99                  //主叫呼叫成功
    case localRejected = 100                      //本地用户拒绝
    case remoteRejected = 101                     //远端用户拒绝
    case onCalling = 102                          //变成呼叫中
    case remoteAccepted = 103                     //远端用户接收
    case localAccepted = 104                      //本地用户接收
    case localHangup = 105                        //本地用户挂断
    case remoteHangup = 106                       //远端用户挂断
    case remoteJoin = 107                         //远端用户加入RTC频道
    case remoteLeave = 108                        //远端用户离开RTC频道
    case localCancel = 109                        //本地用户取消呼叫
    case remoteCancel = 110                       //远端用户取消呼叫
    case localJoin = 111                          //本地用户加入RTC频道
    case localLeave = 112                         //本地用户离开RTC频道
    case recvRemoteFirstFrame = 113               //收到远端首帧
//    case cancelByCallerRecall = 114               //同样的主叫呼叫不同频道导致取消[已废弃]
    case rtmLost = 115                            //rtm超时断连
//    case rtcOccurError = 116                      //rtc出现错误[已废弃，请使用onCallErrorOccur(state: rtcOccurError)]
    case remoteCallBusy = 117                     //远端用户忙
//    case startCaptureFail = 118                   //开启采集失败[已废弃，请使用onCallErrorOccur(state: startCaptureFail)]
    case captureFirstLocalVideoFrame = 119        //采集到首帧视频帧
    case publishFirstLocalVideoFrame = 120        //推送首帧视频帧成功
}


/// 呼叫错误事件
@objc public enum CallErrorEvent: UInt {
    case normalError = 0              //通用错误
    case rtcOccurError = 100          //rtc出现错误
    case startCaptureFail = 110       //rtc开启采集失败
    case rtmSetupFail = 200           //rtm初始化失败
    case sendMessageFail = 210        //消息发送失败
}

/// 呼叫错误事件的错误码类型
@objc public enum CallErrorCodeType: UInt {
    case normal = 0   //业务类型的错误，暂无
    case rtc          //rtc的错误，使用AgoraErrorCode
    case rtm          //rtm的错误，使用AgoraRtmErrorCode
}


/// 日志等级
@objc public enum CallLogLevel: Int {
    case normal = 0
    case warning = 1
    case error = 2
}

@objc public protocol CallApiListenerProtocol: NSObjectProtocol {
    /// 状态响应回调
    /// - Parameters:
    ///   - state: 状态类型
    ///   - stateReason: 状态变更的原因
    ///   - eventReason: 事件类型描述
    ///   - eventInfo: 扩展信息，不同事件类型参数不同
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallStateReason,
                            eventReason: String,
                            eventInfo: [String: Any])

    /// 内部详细事件变更回调
    /// - Parameters:
    ///   - event: 事件
    @objc optional func onCallEventChanged(with event: CallEvent)
    
    /// 发生错误的回调
    /// - Parameters:
    ///   - errorEvent: 错误事件
    ///   - errorType: 错误码类型
    ///   - errorCode: 错误码
    ///   - message: 错误信息
    @objc optional func onCallError(with errorEvent: CallErrorEvent,
                                    errorType: CallErrorCodeType,
                                    errorCode: Int,
                                    message: String?)
    
    /// token即将要过期(需要外部获取新token调用renewToken更新)
    @objc optional func tokenPrivilegeWillExpire()
    
    /// 打印日志
    /// - Parameters:
    ///   - message: 日志信息
    ///   - logLevel: 日志优先级: 0: 普通日志，1: 警告日志, 2: 错误日志
    @objc optional func callDebugInfo(message: String, logLevel: CallLogLevel)
}

@objc public protocol CallApiProtocol: NSObjectProtocol {
    /// 初始化配置
    /// - Parameters:
    ///   - config: <#config description#>
    func initialize(config: CallConfig)
    
    /// 释放缓存
    func deinitialize(completion: @escaping (()->()))
    
    /// 更新自己rtc/rtm的token
    /// - Parameter config: <#config description#>
    func renewToken(with rtcToken: String, rtmToken: String)
    
    /// 准备通话环境，需要调用成功才可以进行呼叫，如需要更换通话的RTC 频道号可以重复调用，确保调用时必须是非通话状态(非calling、connecting、connected)才可调用成功
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - completion: completion description
    func prepareForCall(prepareConfig: PrepareConfig, completion: ((NSError?)->())?)
    
    /// 添加回调的listener
    /// - Parameter listener: <#listener description#>
    func addListener(listener: CallApiListenerProtocol)
    
    /// 移除回调的listener
    /// - Parameter listener: <#listener description#>
    func removeListener(listener: CallApiListenerProtocol)
    
    /// 发起呼叫邀请，主叫调用，通过prepareForCall设置的RTC频道号和远端用户建立RTC通话连接
    /// - Parameters:
    ///   - remoteUserId: 呼叫的用户id
    ///   - completion: <#completion description#>
    func call(remoteUserId: UInt, completion: ((NSError?)->())?)
    
    /// 取消正在发起的呼叫邀请，主叫调用
    /// - Parameter completion: <#completion description#>
    func cancelCall(completion: ((NSError?)->())?)
    
    /// 接受呼叫邀请，被叫调用
    /// - Parameters:
    ///   - remoteUserId: 接受的用户id
    ///   - completion: <#completion description#>
    func accept(remoteUserId: UInt, completion: ((NSError?)->())?)
    
    /// 拒绝呼叫邀请，被叫调用
    /// - Parameters:
    ///   - remoteUserId: 拒绝的用户id
    ///   - reason: 拒绝原因
    ///   - completion: <#completion description#>
    func reject(remoteUserId: UInt, reason: String?, completion: ((NSError?)->())?)
    
    /// 挂断通话，主叫和被叫均可调用
    /// - Parameters:
    ///   - userId: 挂断的用户id
    ///   - reason: 挂断原因
    ///   - completion: <#completion description#>
    func hangup(remoteUserId: UInt, reason: String?, completion: ((NSError?)->())?)
    
    /// 获取当前通话的callId，callId为当次通话过程中唯一标识，通过该标识声网后台服务可以查询到当前通话的关键节点耗时和状态变迁的时间节点
    /// - Returns: callId，非呼叫到通话之外的消息为空
    func getCallId() -> String
}

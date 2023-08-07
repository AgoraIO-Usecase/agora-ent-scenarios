//
//  CallApiProtocol.swift
//  CallAPI
//
//  Created by Agora on 2023/5/18.
//

import Foundation
import AgoraRtcKit

/// 角色
public enum CallRole: Int {
    case callee = 0    //被叫
    case caller        //主叫
}


/// 模式
public enum CallMode: UInt {
    case showTo1v1 = 0      //秀场转1v1
    case pure1v1            //纯1v1
}

/// 初始化配置信息
public class CallConfig: NSObject {
    public var appId: String = ""               //声网App Id
    public var userId: UInt = 0                 //用户id
    public var userExtension: [String: Any]?    //用户扩展字段,用在呼叫上，对端收到calling时可以通过kFromUserExtension字段读到
    public var ownerRoomId: String?             //房主房间id，秀场转1v1可用
    public var rtcEngine: AgoraRtcEngineKit!    //rtc engine实例
    public var mode: CallMode = .showTo1v1      //模式
    public var role: CallRole = .callee         //角色，纯1v1需要设置成caller
    public var localView: UIView!               //显示本地流的画布
    public var remoteView: UIView!              //显示远端流的画布
    public var autoAccept: Bool = true          //是否收到被叫后自动接受，秀场转1v1可用
}


public class PrepareConfig: NSObject {
    public var autoLoginRTM: Bool = true       //是否自动登录RTM
    public var autoSubscribeRTM: Bool = true   //是否自动订阅RTM，如果为true，则autoLoginRTM必定为true
    public var autoJoinRTC: Bool = false        //是否自动登录RTC
    
    //主叫默认配置
    public class func callerConfig() -> PrepareConfig {
        let config = PrepareConfig()
        config.autoLoginRTM = false
        config.autoSubscribeRTM = false
        return config
    }
    
    
    /// 被叫默认配置
    /// - Returns: <#description#>
    public class func calleeConfig() -> PrepareConfig {
        let config = PrepareConfig()
        return config
    }
}


/// token renew时的配置
public class CallTokenConfig: NSObject {
    public var roomId: String = ""            //频道名(主叫需要设置为1v1的频道，被叫需要设置为自己的广播频道)
    public var rtcToken: String = ""          //rtc token，被叫需要使用万能token，token创建的时候channel name为空字符串
    public var rtmToken: String = ""          //rtm token
}

@objc public enum CallReason: UInt {
    case none = 0
    case joinRTCFailed        //加入RTC失败
    case rtmSetupFailed       //设置RTM失败
    case rtmSetupSuccessed    //设置RTM失败
    case messageFailed        //消息发送失败
    case localRejected        //本地用户拒绝
    case remoteRejected       //远端用户拒绝
    case remoteAccepted       //远端用户接受
    case localAccepted        //本地用户接受
    case localHangup          //本地用户挂断
    case remoteHangup         //远端用户挂断
    case localCancel          //本地用户取消呼叫
    case remoteCancel         //远端用户取消呼叫
    case recvRemoteFirstFrame //收到远端首帧
    case callingTimeout       //呼叫超时
}

@objc public enum CallEvent: UInt {
    case none = 0
    case deinitialize             //调用了deinitialize
    case missingReceipts          //没有收到消息回执
    case callingTimeout           //呼叫超时
    case joinRTCFailed            //加入RTC失败
    case joinRTCSuccessed         //加入RTC成功
    case rtmSetupFailed           //设置RTM失败
    case rtmSetupSuccessed        //设置RTM成功
    case messageFailed            //消息发送失败
    case stateMismatch            //状态流转异常
    case localRejected = 100      //本地用户拒绝
    case remoteRejected           //远端用户拒绝
    case onCalling                //变成呼叫中
    case remoteAccepted           //远端用户接收
    case localAccepted            //本地用户接收
    case localHangup              //本地用户挂断
    case remoteHangup             //远端用户挂断
    case remoteJoin               //远端用户加入RTC频道
    case remoteLeave              //远端用户离开RTC频道
    case localCancel              //本地用户取消呼叫
    case remoteCancel             //远端用户取消呼叫
    case localJoin                //本地用户加入RTC频道
    case localLeave               //本地用户离开RTC频道
    case recvRemoteFirstFrame     //收到远端首帧
}

@objc public enum CallStateType: UInt {
    case idle = 0            //空闲
    case prepared = 1        //创建1v1环境完成
    case calling = 2         //呼叫中
    case connecting = 3      //连接中
    case connected = 4       //通话中
    case failed = 10         //出现错误
}

@objc public protocol CallApiListenerProtocol: NSObjectProtocol {
    /// 状态响应回调
    /// - Parameters:
    ///   - state: 状态类型
    ///   - stateReason: 状态原因
    ///   - eventReason: 事件类型描述
    ///   - elapsed: 从触发到回调的耗时(只有呼叫到通话中间事件可统计)
    ///   - eventInfo: 扩展信息，不同事件类型参数不同，其中key为“publisher”为状态变更者id，空则表示是自己的状态变更
    func onCallStateChanged(with state: CallStateType,
                            stateReason: CallReason,
                            eventReason: String,
                            elapsed: Int,
                            eventInfo: [String: Any])
    
    /// 内部详细事件变更回调
    /// - Parameters:
    ///   - event: 事件
    ///   - elapsed: 耗时(只有呼叫到通话中间事件可统计)
    @objc optional func onCallEventChanged(with event: CallEvent, elapsed: Int)
    
    /// 第一次进入房间时获取到的1v1信息，用于异常退出之后重连，秀场转1v1模式可用
    /// - Parameters:
    ///   - oneForOneRoomId: 1v1频道号
    ///   - fromUserId: 发起呼叫的用户id
    ///   - toUserId: 接收呼叫的用户id
    @objc optional func onOneForOneCache(oneForOneRoomId: String, fromUserId: UInt, toUserId: UInt)
    
    
    /// token快要过期了
    @objc optional func tokenPrivilegeWillExpire()
    
    @objc optional func debugInfo(message: String)
    @objc optional func debugWarning(message: String)
}

@objc public protocol CallApiProtocol: NSObjectProtocol {
    
    /// 初始化配置
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - token: <#token description#>
    func initialize(config: CallConfig,
                    token: CallTokenConfig,
                    completion: @escaping ((NSError?)->()))
    
    
    /// 释放缓存
    func deinitialize(completion: @escaping (()->()))
    
    /// 更新rtc/rtm的token
    /// - Parameter config: <#config description#>
    func renewToken(with config: CallTokenConfig)
    
    /// 更新呼叫token
    /// - Parameter token: <#token description#>
    func renewRemoteCallerChannelToken(roomId: String, token: String)
    
    /// 连接(对RTM进行login和subscribe)， 观众调用
    /// - Parameters:
    ///   - config: <#config description#>
    ///   - completion: <#completion description#>
    func prepareForCall(prepareConfig: PrepareConfig, completion: ((NSError?)->())?)
    
    /// 监听远端处理的回调
    /// - Parameter listener: <#listener description#>
    func addListener(listener: CallApiListenerProtocol)
    
    /// 取消监听远端回调
    /// - Parameter listener: <#listener description#>
    func removeListener(listener: CallApiListenerProtocol)
    
    /// 发起通话，加 RTC 频道并且发流，并且发 rtm 频道消息 申请链接，调用后被叫会收到onCall
    /// - Parameters:
    ///   - roomId: 目标主播频道号
    ///   - remoteUserId: 呼叫的用户id
    ///   - completion: <#completion description#>
    func call(roomId: String, remoteUserId: UInt, completion: ((NSError?)->())?)
    
    /// 取消正在发起的通话，未接通的时候可用，调用后被叫会收到onCancel
    /// - Parameter completion: <#completion description#>
    func cancelCall(completion: ((NSError?)->())?)
    
    
    /// 接受通话，调用后主叫会收到onAccept
    /// - Parameters:
    ///   - roomId: 频道号
    ///   - remoteUserId: 呼叫的用户id
    ///   - rtcToken: roomId对应的rtc token
    ///   - completion: <#completion description#>
    func accept(roomId: String, remoteUserId: UInt, rtcToken: String, completion: ((NSError?)->())?)
    
    /// 被叫拒绝通话，调用后主叫会收到onReject
    /// - Parameters:
    ///   - roomId: 频道号
    ///   - remoteUserId: 呼叫的用户id
    ///   - reason: 拒绝原因
    ///   - completion: <#completion description#>
    func reject(roomId: String, remoteUserId: UInt, reason: String?, completion: ((NSError?)->())?)
    
    /// 结束通话，调用后被叫会收到onHangup
    /// - Parameters:
    ///   - roomId: 频道号
    ///   - completion: <#completion description#>
    func hangup(roomId: String, completion: ((NSError?)->())?)
    
    /// 获取callId，callId为通话过程中消息的标识，通过argus可以查询到从呼叫到通话的耗时和状态变迁的时间戳
    /// - Returns: callId，非呼叫到通话之外的消息为空
    func getCallId() -> String
    
    //
    @objc optional func addRTCListener(listener: AgoraRtcEngineDelegate)
    //
    @objc optional func removeRTCListener(listener: AgoraRtcEngineDelegate)
}

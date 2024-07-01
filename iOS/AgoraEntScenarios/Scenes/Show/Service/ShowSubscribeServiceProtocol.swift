//
//  ShowSubscribeServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/27.
//

import Foundation

@objc public enum ShowServiceConnectState: Int {
    case connecting = 0
    case open = 1
    case fail = 2
    case closed = 3
}

@objc public protocol ShowSubscribeServiceProtocol: NSObjectProtocol {
    
    /// 房间过期
    func onRoomExpired(channelName: String)
    
    /// 房间被销毁
    /// - Parameter channelName: <#channelName description#>
    func onRoomDestroy(channelName: String)
    
    /// 网络状态变化
    /// - Parameter state: <#state description#>
    func onConnectStateChanged(channelName: String, state: ShowServiceConnectState)
    
    /// 房间内用户数变化
    /// - Parameter userCount: <#userCount description#>
    func onUserCountChanged(channelName: String, userCount: Int)
    
    /// 用户加入房间
    /// - Parameter user: <#user description#>
    func onUserJoinedRoom(channelName: String, user: ShowUser)
    
    /// 用户离开房间
    /// - Parameter user: <#user description#>
    func onUserLeftRoom(channelName: String, user: ShowUser)
    
    
    
    /// 接收到新消息
    /// - Parameter message: <#message description#>
    func onMessageDidAdded(channelName: String, message: ShowMessage)
    
    
    /// 连麦申请列表变化
    /// - Parameter apply: <#apply description#>
    func onMicSeatApplyUpdated(channelName: String, applies: [ShowMicSeatApply])

    
    /// 收到连麦邀请/邀请被更新
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationUpdated(channelName: String, invitation: ShowMicSeatInvitation)
    
    /// 同意连麦邀请
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationAccepted(channelName: String, invitation: ShowMicSeatInvitation)
    
    /// 拒绝连麦邀请
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationRejected(channelName: String, invitation: ShowMicSeatInvitation)
    
    
    
    /// 收到PK邀请/邀请被更新
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationUpdated(channelName: String, invitation: ShowPKInvitation)

    /// 同意PK邀请
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationAccepted(channelName: String, invitation: ShowPKInvitation)
    
    /// 拒绝PK邀请
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationRejected(channelName: String, invitation: ShowPKInvitation)
    
    
    
    /// pk/连麦互动变更
    /// - Parameter interation: <#interation description#>
    func onInteractionUpdated(channelName: String, interactions: [ShowInteractionInfo])
}

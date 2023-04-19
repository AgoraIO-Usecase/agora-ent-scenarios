//
//  ShowSubscribeServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/27.
//

import Foundation

public enum ShowServiceConnectState: Int {
    case connecting = 0
    case open = 1
    case fail = 2
    case closed = 3
}

protocol ShowSubscribeServiceProtocol: NSObject {
    
    
    /// 房间过期
    func onRoomExpired()
    
    /// 网络状态变化
    /// - Parameter state: <#state description#>
    func onConnectStateChanged(state: ShowServiceConnectState)
    
    /// 房间内用户数变化
    /// - Parameter userCount: <#userCount description#>
    func onUserCountChanged(userCount: Int)
    
    /// 用户加入房间
    /// - Parameter user: <#user description#>
    func onUserJoinedRoom(user: ShowUser)
    
    /// 用户离开房间
    /// - Parameter user: <#user description#>
    func onUserLeftRoom(user: ShowUser)
    
    
    
    /// 接收到新消息
    /// - Parameter message: <#message description#>
    func onMessageDidAdded(message: ShowMessage)
    
    
    
    /// 收到连麦申请
    /// - Parameter apply: <#apply description#>
    func onMicSeatApplyUpdated(apply: ShowMicSeatApply)
    
    /// 取消连麦申请
    /// - Parameter apply: <#apply description#>
    func onMicSeatApplyDeleted(apply: ShowMicSeatApply)
    
    /// 同意连麦申请
    /// - Parameter apply: <#apply description#>
    func onMicSeatApplyAccepted(apply: ShowMicSeatApply)
    
    /// 拒绝连麦申请
    /// - Parameter apply: <#apply description#>
    func onMicSeatApplyRejected(apply: ShowMicSeatApply)
    
    
    /// 收到连麦邀请/邀请被更新
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationUpdated(invitation: ShowMicSeatInvitation)
    
    /// 取消连麦邀请
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationDeleted(invitation: ShowMicSeatInvitation)
    
    /// 同意连麦邀请
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationAccepted(invitation: ShowMicSeatInvitation)
    
    /// 拒绝连麦邀请
    /// - Parameter invitation: <#invitation description#>
    func onMicSeatInvitationRejected(invitation: ShowMicSeatInvitation)
    
    
    
    /// 收到PK邀请/邀请被更新
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationUpdated(invitation: ShowPKInvitation)

    /// 同意PK邀请
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationAccepted(invitation: ShowPKInvitation)
    
    /// 拒绝PK邀请
    /// - Parameter invitation: <#invitation description#>
    func onPKInvitationRejected(invitation: ShowPKInvitation)
    
    
    
    /// 收到一个pk/连麦互动/更新
    /// - Parameter interation: <#interation description#>
    func onInteractionBegan(interaction: ShowInteractionInfo)
    
    /// pk/连麦被停止
    /// - Parameter interaction: <#interaction description#>
    func onInterationEnded(interaction: ShowInteractionInfo)
    
    
    /// pk/连麦被更新(静音等)
    /// - Parameter interaction: <#interaction description#>
    func onInterationUpdated(interaction: ShowInteractionInfo)
}

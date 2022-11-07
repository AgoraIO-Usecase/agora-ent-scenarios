//
//  ShowServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/2.
//

import Foundation

enum ShowSubscribeStatus {
    case created     //订阅到对象创建
    case deleted     //订阅到对象删除
    case updated     //订阅到对象更新
}

protocol ShowServiceProtocol: NSObjectProtocol {
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func getRoomList(page: Int,
                     completion: @escaping (Error?, [ShowRoomListModel]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: ShowRoomListModel,
                    completion: @escaping (Error?, ShowRoomDetailModel?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息， 房间信息)
    func joinRoom(room: ShowRoomListModel,
                  completion: @escaping (Error?, ShowRoomDetailModel?) -> Void)
    
    /// 离开房间
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    
    
    
    
    
    
    /// 监听用户变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeUser(subscribeClosure: @escaping (ShowSubscribeStatus, ShowUser) -> Void)
    
    /// 获取当前房间所有用户
    /// - Parameter completion: 完成回调   (错误信息， 用户列表)
    func getAllUserList(completion: @escaping (Error?, [ShowUser]?) -> Void)
    
    
    
    
    
    /// 发送聊天消息
    /// - Parameters:
    ///   - message: <#message description#>
    ///   - completion: <#completion description#>
    func sendChatMessage(message: ShowMessage,
                         completion: ((Error) -> Void)?)
    
    /// 订阅聊天消息变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMessage) -> Void)
    
    
    
    
    
    /// 获取上麦申请列表
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void)
    
    /// 主播订阅连麦申请变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatApply(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatApply) -> Void)
    
    /// 观众申请连麦
    /// - Parameters:
    ///   - completion: <#completion description#>
    func createMicSeatApply(completion: @escaping (Error?) -> Void)
    
    /// 观众取消连麦申请
    /// - Parameters:
    ///   - completion: <#completion description#>
    func cancelMicSeatApply(completion: @escaping (Error?) -> Void)
    
    /// 主播接受连麦申请
    /// - Parameters:
    ///   - apply: 连麦申请对象
    ///   - completion: <#completion description#>
    func acceptMicSeatApply(apply:ShowMicSeatApply,
                            completion: @escaping (Error?) -> Void)
    
    /// 主播拒绝连麦申请
    /// - Parameters:
    ///   - apply: 连麦申请对象
    ///   - completion: <#completion description#>
    func rejectMicSeatApply(apply:ShowMicSeatApply,
                            completion: @escaping (Error?) -> Void)
    
    
    
    
    
    /// 获取上麦邀请列表
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatInvitationList(completion: @escaping (Error?, [ShowMicSeatInvitation]?) -> Void)
    
    /// 观众订阅连麦邀请
    /// - Parameter subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatInvitation) -> Void)
    
    /// 主播创建连麦邀请
    /// - Parameters:
    ///   - user: 邀请用户
    ///   - completion: <#completion description#>
    func createMicSeatInvitation(user: ShowUser,
                                 completion: @escaping (Error?) -> Void)
    
    /// 主播取消连麦邀请
    /// - Parameters:
    ///   - user: 用户
    ///   - completion: <#completion description#>
    func cancelMicSeatInvitation(user: ShowUser,
                                 completion: @escaping (Error?) -> Void)
    
    /// 观众同意连麦
    /// - Parameters:
    ///   - invitation: 连麦邀请对象
    ///   - completion: <#completion description#>
    func acceptMicSeatInvitation(invitation:ShowMicSeatInvitation,
                                 completion: @escaping (Error?) -> Void)
    
    /// 观众拒绝连麦
    /// - Parameters:
    ///   - invitation: 连麦邀请对象
    ///   - completion: <#completion description#>
    func rejectMicSeatInvitation(invitation:ShowMicSeatInvitation,
                                 completion: @escaping (Error?) -> Void)
    
    
    
}

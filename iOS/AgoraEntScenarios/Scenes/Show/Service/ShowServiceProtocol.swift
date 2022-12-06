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
                     completion: @escaping (NSError?, [ShowRoomListModel]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    
    
    /// 创建房间
    /// - Parameters:
    ///   - roomName: 房间名
    ///   - roomId: 房间Id
    ///   - thumbnailId: 列表缩略图
    ///   - completion: <#completion description#>
    func createRoom(roomName: String,
                    roomId: String,
                    thumbnailId: String,
                    completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息， 房间信息)
    func joinRoom(room: ShowRoomListModel,
                  completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void)
    
    /// 离开房间
    func leaveRoom(completion: @escaping (NSError?) -> Void)
    
    
    
    /// 获取当前房间所有用户
    /// - Parameter completion: 完成回调   (错误信息， 用户列表)
    func getAllUserList(completion: @escaping (NSError?, [ShowUser]?) -> Void)
    
    /// 发送聊天消息
    /// - Parameters:
    ///   - message: <#message description#>
    ///   - completion: <#completion description#>
    func sendChatMessage(message: ShowMessage,
                         completion: ((NSError?) -> Void)?)
    
    
    /// 获取上麦申请列表
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatApplyList(completion: @escaping (NSError?, [ShowMicSeatApply]?) -> Void)

    /// 观众申请连麦
    /// - Parameters:
    ///   - completion: <#completion description#>
    func createMicSeatApply(completion: @escaping (NSError?) -> Void)
    
    /// 观众取消连麦申请
    /// - Parameters:
    ///   - completion: <#completion description#>
    func cancelMicSeatApply(completion: @escaping (NSError?) -> Void)
    
    /// 主播接受连麦申请
    /// - Parameters:
    ///   - apply: 连麦申请对象
    ///   - completion: <#completion description#>
    func acceptMicSeatApply(apply:ShowMicSeatApply,
                            completion: @escaping (NSError?) -> Void)
    
    /// 主播拒绝连麦申请
    /// - Parameters:
    ///   - apply: 连麦申请对象
    ///   - completion: <#completion description#>
    func rejectMicSeatApply(apply:ShowMicSeatApply,
                            completion: @escaping (NSError?) -> Void)
    
    
    /// 获取当前连麦或PK的主播
    /// - Parameters:
    ///   - roomId: 房间ID
    ///   - completion: 回调当前用户
    func getCurrentApplyUser(roomId: String?, completion: @escaping (ShowRoomListModel?) -> Void)
    
    
    /// 获取上麦邀请列表
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatInvitationList(completion: @escaping (NSError?, [ShowMicSeatInvitation]?) -> Void)
    
    /// 主播创建连麦邀请
    /// - Parameters:
    ///   - user: 邀请用户
    ///   - completion: <#completion description#>
    func createMicSeatInvitation(user: ShowUser,
                                 completion: @escaping (NSError?) -> Void)
    
    /// 主播取消连麦邀请
    /// - Parameters:
    ///   - user: 用户
    ///   - completion: <#completion description#>
    func cancelMicSeatInvitation(userId: String,
                                 completion: @escaping (NSError?) -> Void)
    
    /// 观众同意连麦
    /// - Parameters:
    ///   - completion: <#completion description#>
    func acceptMicSeatInvitation(completion: @escaping (NSError?) -> Void)
    
    /// 观众拒绝连麦
    /// - Parameters:
    ///   - completion: <#completion description#>
    func rejectMicSeatInvitation(completion: @escaping (NSError?) -> Void)
    
    
    
    
    /// 获取可PK对象列表(目前等价getRoomList)
    /// - Parameter completion: <#completion description#>
    func getAllPKUserList(completion: @escaping (NSError?, [ShowPKUserInfo]?) -> Void)
    
    /// 获取PK邀请列表（包括发起邀请和接受的邀请，根据fromUerId区分）
    /// - Parameter completion: <#completion description#>
    func getAllPKInvitationList(completion: @escaping (NSError?, [ShowPKInvitation]?) -> Void)
    

    /// 创建PK邀请
    /// - Parameters:
    ///   - user: 邀请用户
    ///   - completion: <#completion description#>
    func createPKInvitation(room: ShowRoomListModel,
                            completion: @escaping (NSError?) -> Void)
    
    /// 同意PK
    /// - Parameters:
    ///   - completion: <#completion description#>
    func acceptPKInvitation(completion: @escaping (NSError?) -> Void)
    
    /// 拒绝PK
    /// - Parameters:
    ///   - completion: <#completion description#>
    func rejectPKInvitation(completion: @escaping (NSError?) -> Void)
    
    
    
    /// 获取互动列表
    /// - Parameter completion: <#completion description#>
    func getAllInterationList(completion: @escaping (NSError?, [ShowInteractionInfo]?) -> Void)
    
    /// 停止互动
    /// - Parameter completion: <#completion description#>
    func stopInteraction(interaction: ShowInteractionInfo, completion: @escaping (NSError?) -> Void)
    
    
    
    /// 静音设置
    /// - Parameters:
    ///   - mute: <#mute description#>
    ///   - userId: <#userId description#>
    ///   - completion: <#completion description#>
    func muteAudio(mute:Bool, userId: String, completion: @escaping (NSError?) -> Void)

    
    
    func unsubscribeEvent(delegate: ShowSubscribeServiceProtocol)
    
    func subscribeEvent(delegate: ShowSubscribeServiceProtocol)
}

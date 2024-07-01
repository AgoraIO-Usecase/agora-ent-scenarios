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
    ///   - roomId: 房间Id 
    ///   - roomName: 房间名
    ///   - completion: 完成回调
    func createRoom(roomId: String,
                    roomName: String,
                    completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息， 房间信息)
    func joinRoom(room: ShowRoomListModel,
                  completion: @escaping (NSError?, ShowRoomDetailModel?) -> Void)
    
    
    /// 离开房间
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - completion: 完成回调
    func leaveRoom(roomId: String, 
                   completion: @escaping (NSError?) -> Void)
    
    /// 获取当前房间所有用户
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - completion: 完成回调   (错误信息， 用户列表)
    func getAllUserList(roomId: String, 
                        completion: @escaping (NSError?, [ShowUser]?) -> Void)
    
    /// 发送聊天消息
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - message: 消息内容
    ///   - completion: 完成回调
    func sendChatMessage(roomId: String,
                         message: ShowMessage,
                         completion: ((NSError?) -> Void)?)
    
    /// 获取上麦申请列表
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - completion: 完成回调
    func getAllMicSeatApplyList(roomId: String, 
                                completion: @escaping (NSError?, [ShowMicSeatApply]?) -> Void)

    
    /// 观众申请连麦
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - completion: 完成回调
    func createMicSeatApply(roomId: String,
                            completion: @escaping (NSError?) -> Void)
    
    /// 观众取消连麦申请
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - completion: 完成回调
    func cancelMicSeatApply(roomId: String,
                            completion: @escaping (NSError?) -> Void)
    
    /// 主播接受连麦申请
    /// - Parameters:
    ///   - roomId: 房间Id
    ///   - userId: 用户Id
    ///   - completion: 完成回调
    func acceptMicSeatApply(roomId: String,
                            userId: String,
                            completion: @escaping (NSError?) -> Void)
    
    
    
    
    /// 主播创建连麦邀请
    /// - Parameters:
    ///   - roomId: 房间ID
    ///   - userId: 邀请用户id
    ///   - completion: 完成回调
    func createMicSeatInvitation(roomId: String,
                                 userId: String,
                                 completion: @escaping (NSError?) -> Void)
    
    
    /// 观众同意连麦
    /// - Parameters:
    ///   - roomId: 房间ID
    ///   - invitationId: 邀请id
    ///   - completion: 完成回调
    func acceptMicSeatInvitation(roomId: String,
                                 invitationId: String,
                                 completion: @escaping (NSError?) -> Void)
    
    /// 观众拒绝连麦
    /// - Parameters:
    ///   - roomId: 房间ID
    ///   - invitationId: 邀请id
    ///   - completion: 完成回调
    func rejectMicSeatInvitation(roomId: String,
                                 invitationId: String,
                                 completion: @escaping (NSError?) -> Void)
    
    
    
    
    /// 获取可PK对象列表(目前等价getRoomList)
    /// - Parameter completion: 完成回调
    func getAllPKUserList(completion: @escaping (NSError?, [ShowPKUserInfo]?) -> Void)
    
    /// 创建PK邀请
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - pkRoomId: 需要pk的房间id
    ///   - completion: 完成回调
    func createPKInvitation(roomId: String,
                            pkRoomId: String,
                            completion: @escaping (NSError?) -> Void)
    
    /// 同意PK
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - invitationId: 邀请id
    ///   - completion: 完成回调
    func acceptPKInvitation(roomId: String,
                            invitationId: String,
                            completion: @escaping (NSError?) -> Void)
    
    /// 拒绝PK
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - invitationId: 邀请id
    ///   - completion: 完成回调
    func rejectPKInvitation(roomId: String,
                            invitationId: String,
                            completion: @escaping (NSError?) -> Void)
    
    
    
    
    /// 获取当前互动信息
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - completion: 完成回调
    func getInterationInfo(roomId: String,
                           completion: @escaping (NSError?, ShowInteractionInfo?) -> Void)
    
    /// 停止互动
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - completion: 完成回调
    func stopInteraction(roomId: String,
                         completion: @escaping (NSError?) -> Void)
    
    
    
    
    /// 静音设置
    /// - Parameters:
    ///   - mute: <#mute description#>
    ///   - userId: <#userId description#>
    ///   - completion: <#completion description#>
    func muteAudio(roomId: String,
                   mute:Bool,
                   completion: @escaping (NSError?) -> Void)

    
    func getCurrentNtpTs(roomId: String) -> UInt64
    
    func unsubscribeEvent(roomId: String, delegate: ShowSubscribeServiceProtocol)
    
    func subscribeEvent(roomId: String, delegate: ShowSubscribeServiceProtocol)
}

//
//  VoiceServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

enum VoiceChatSubscribeStatus {
    case created     //订阅到对象创建
    case deleted     //订阅到对象删除
    case updated     //订阅到对象更新
}

protocol VoiceServiceProtocol: NSObjectProtocol {

    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func getRoomList(page: Int,
                     completion: @escaping (Error?, [VoiceChatRoom]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VoiceChatRoom,
                    completion: @escaping (Error?) -> Void)
    
    /// 加入房间
    /// - Parameters:
    ///   - roomName: 房间名
    ///   - completion: 完成回调   (错误信息， 房间信息)
    func joinRoom(roomName: String,
                  completion: @escaping (Error?, VoiceChatRoom?) -> Void)
    
    /// 离开房间
    func leaveRoom()
    
    /// 监听用户变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeUser(subscribeClosure: @escaping (VoiceChatSubscribeStatus, VoiceChatRoom) -> Void)
    
    /// 获取当前房间所有用户
    /// - Parameter completion: 完成回调   (错误信息， 用户列表)
    func getAllUserList(completion: @escaping (Error?, [VoiceChatUsers]?) -> Void)

    
    
    
    

    /// 设置浏览人数
    /// - Parameters:
    ///   - visitCount: 浏览人数
    ///   - completion: <#completion description#>
    func setVisit(visitCount: Int,
                  completion: @escaping (Error?) -> Void)
    
    /// 获取浏览人数
    /// - Parameter completion: <#completion description#>
    func getVisit(completion: @escaping (Error?, Int?) -> Void)
    
    /// 订阅浏览人数变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeVisit(subscribeClosure: @escaping (VoiceChatSubscribeStatus, Int) -> Void)
    
    
    
    
    
    /// 语聊机器人开关
    /// - Parameters:
    ///   - enable: true: 打开语聊机器人， false: 关闭语聊机器人
    ///   - completion: <#completion description#>
    func toggleSmartRobot(enable: Bool,
                          completion: @escaping (Error?) -> Void)
    
    /// 语聊机器人开关变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeSmartRobot(subscribeClosure: @escaping (Bool) -> Void)
    
    
    
    
    
    /// 获取所有麦位列表
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatsList(completion: @escaping (Error?, [VoiceChatMicSeat]?) -> Void)
    
    /// 更新麦位信息
    /// - Parameters:
    ///   - seat: <#seatInfo description#>
    ///   - completion: <#completion description#>
    func updateMicSeat(seat: VoiceChatMicSeat, completion: @escaping (Error?) -> Void)
    
    /// 订阅麦位变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeat(subscribeClosure: @escaping (VoiceChatSubscribeStatus, VoiceChatMicSeat) -> Void)
    
    /// 观众申请上麦
    /// - Parameters:
    ///   - seatIndex: 麦位索引
    ///   - completion: <#completion description#>
    func createMicSeatRequest(seatIndex: Int,
                              completion: @escaping (Error?) -> Void)
    
    
    ///  观众取消上麦申请
    /// - Parameters:
    ///   - seatIndex: <#seatIndex description#>
    ///   - completion: <#completion description#>
    func cancelMicSeatRequest(seatIndex: Int,
                              completion: @escaping (Error?) -> Void)
    
    /// 获取所有上麦请求
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatRequests(completion: @escaping (Error?, [VoiceChatMicSeatRequest]?) -> Void)
    
    /// 同意上麦
    /// - Parameters:
    ///   - request: 上麦请求对象
    ///   - completion: <#completion description#>
    func approveMicSeatRequest(request: VoiceChatMicSeatRequest,
                               completion: @escaping (Error?) -> Void)
        
    ///  拒接上麦
    /// - Parameters:
    ///   - request: 上麦请求对象
    ///   - completion: <#completion description#>
    func rejectMicSeatRequest(request: VoiceChatMicSeatRequest,
                              completion: @escaping (Error?) -> Void)
    
    /// 订阅上麦请求变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatRequestList(subscribeClosure: @escaping (VoiceChatSubscribeStatus, VoiceChatMicSeatRequest) -> Void)
    
    /// 主播邀请观众上麦
    /// - Parameters:
    ///   - invitation: <#invitation description#>
    ///   - completion: <#completion description#>
    func createMicSeatInvitation(invitation: VoiceChatMicSeatInvitation,
                                 completion: @escaping (Error?) -> Void)
    
    /// 主播取消观众上麦邀请
    /// - Parameters:
    ///   - invitation: <#invitation description#>
    ///   - completion: <#completion description#>
    func cancelMicSeatInvitation(invitation: VoiceChatMicSeatInvitation,
                                 completion: @escaping (Error?) -> Void)
    
    /// 观众同意上麦邀请
    /// - Parameters:
    ///   - invitation: <#invitation description#>
    ///   - completion: <#completion description#>
    func approveMicSeatInvitation(invitation: VoiceChatMicSeatInvitation,
                                  completion: @escaping (Error?) -> Void)
        
    /// 拒绝上麦邀请
    /// - Parameters:
    ///   - invitation: <#invitation description#>
    ///   - completion: <#completion description#>
    func rejectMicSeatInvitation(invitation: VoiceChatMicSeatInvitation,
                                 completion: @escaping (Error?) -> Void)
    
    /// 获取所有上麦邀请
    /// - Parameter completion: <#completion description#>
    func getAllMicSeatInvitations(completion: @escaping (Error?, [VoiceChatMicSeatInvitation]?) -> Void)
    
    /// 订阅上麦邀请变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (VoiceChatSubscribeStatus, VoiceChatMicSeatInvitation) -> Void)
    
    
    
    
    
    /// 发送聊天消息
    /// - Parameters:
    ///   - message: <#message description#>
    ///   - completion: <#completion description#>
    func sendChatMessage(message: VoiceChatMessage,
                         completion: ((Error) -> Void)?)
    
    /// 订阅聊天消息变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (VoiceChatSubscribeStatus, VoiceChatMessage) -> Void)
    
    
    
    
    
    
    /// 获取收到礼物数量
    /// - Parameter completion: <#completion description#>
    func getTotalGiftCount(completion: ((Error?, Int) -> Void)?)
    
    /// 发送礼物
    /// - Parameters:
    ///   - gift: 礼物信息
    ///   - completion: <#completion description#>
    func sendGift(gift: VoiceChatGift, completion: ((Error?) -> Void)?)
    
    /// 订阅收到礼物数量变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeTotalGiftCount(subscribeClosure: @escaping (Int) -> Void)
    
    
    
    
    
    
    /// 更新公告
    /// - Parameters:
    ///   - content: 公告信息
    ///   - completion: <#completion description#>
    func setRoomNotice(content: String, completion: ((Error?) -> Void)?)
    
    
    /// 更新公告信息(目前只有变化)
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    func subscribeRoomNotice(subscribeClosure: @escaping (VoiceChatSubscribeStatus, String) -> Void)
    
    

    /// 取消所有监听
    func unsubscribeAll()
}

//
//  TemplateServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

public enum updateRoomState {
    case activeAlien
    case announcement
    case robotVoleme
}

protocol ChatRoomServiceProtocol: NSObjectProtocol {
    /// 加入房间
    /// - Parameters:
    ///   - roomName: 房间名
    ///   - completion: <#completion description#>
    func join(roomName: String, completion: @escaping (SyncError?, ChatRoomScene.JoinResponse?) -> Void)

    /// 离开房间(观众)
    func leave(roomId: String, completion: @escaping (SyncError?, ChatRoomScene.JoinResponse?) -> Void)

    /// 删除房间(主播)
    func removeRoom()

    /// 添加用户
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func addUser(user: ChatRoomScene.UsersModel, completion: @escaping (SyncError?, ChatRoomScene.UsersModel?) -> Void)

    /// 删除一个用户
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func removeUser(user: ChatRoomScene.UsersModel, completion: @escaping (SyncError?, [ChatRoomScene.UsersModel]?) -> Void)

    /// 修改用户信息
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func updateUser(user: ChatRoomScene.UsersModel, completion: @escaping (SyncError?, ChatRoomScene.UsersModel?) -> Void)

    /// 获取所有用户
    /// - Parameter completion: <#completion description#>
    func getUserStatus(completion: @escaping (SyncError?, [ChatRoomScene.UsersModel]?) -> Void)

    /// 监听房间变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    ///   - onSubscribed: <#onSubscribed description#>
    ///   - fail: <#fail description#>
    func subscribeRoom(subscribeClosure: @escaping (ChatRoomScene.SubscribeStatus, ChatRoomScene.LiveRoomInfo?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)

    /// 监听用户变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    ///   - onSubscribed: <#onSubscribed description#>
    ///   - fail: <#fail description#>
    func subscribeUser(subscribeClosure: @escaping (ChatRoomScene.SubscribeStatus, ChatRoomScene.UsersModel?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)

    /// 取消监听
    func unsubscribe()

    /// 获取房间详情
    /// - Parameters:
    ///
    func fetchRoomDetail(_ roomId: String,
                         completion: @escaping (SyncError?, VRRoomInfo?) -> Void)

    /// 邀请上卖
    /// - Parameters:
    ///
    func inviteUserToMic(_ user: VRUser,
                         completion: @escaping (SyncError?, Bool) -> Void)

    /// 获取礼物列表
    /// - Parameters:
    ///
    func fetchGiftContribute(_ roomId: String,
                             completion: @escaping (SyncError?, VRUsers?) -> Void)
    
    /// 激活机器人,修改公告，修改机器人音量
    /// - Parameters:
    ///
    func modifyRoomInfo(_ info: (String, Any, updateRoomState),
                        completion: @escaping (SyncError?, Bool) -> Void)
    
    
    /// 禁言指定麦位
    /// - Parameters:
    ///
    func forbidMic(_ micInfo: (String, Int),
                   completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 取消禁言指定麦位
    /// - Parameters:
    ///
    func unForbidMic(_ micInfo: (String, Int),
                     completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 锁麦
    /// - Parameters:
    ///
    func lockMic(_ micInfo: (String, Int),
                 completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 取消锁麦
    /// - Parameters:
    ///
    func unLockMic(_ micInfo: (String, Int),
                   completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 踢用户下麦
    /// - Parameters:
    ///
    func kickOff(_ micInfo: (String, Int),
                 completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 下麦
    /// - Parameters:
    ///
    func leaveMic(_ micInfo: (String, Int),
                  completion: @escaping (SyncError?, Bool) -> Void)
    
    /// mute
    /// - Parameters:
    ///
    func muteLocal(_ micInfo: (String, Int),
                   completion: @escaping (SyncError?, Bool) -> Void)
    
    /// unmute
    /// - Parameters:
    ///
    func unmuteLocal(_ micInfo: (String, Int),
                     completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 换麦
    /// - Parameters:
    ///
    func changeMic(_ micInfo: (String, Int, Int),
                   completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 取消邀请
    /// - Parameters:
    ///
    func refuseInvite(_ roomId: String,
                      completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 接受邀请
    /// - Parameters:
    ///
    func agreeInvite(_ roomId: String,
                     completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 申请上麦
    /// - Parameters:
    ///
    func submitApply(_ micInfo: (String, Int?),
                     completion: @escaping (SyncError?, Bool) -> Void)
    
    /// 取消上麦
    /// - Parameters:
    ///
    func cancelApply(_ roomId: String,
                     completion: @escaping (SyncError?, Bool) -> Void)
}

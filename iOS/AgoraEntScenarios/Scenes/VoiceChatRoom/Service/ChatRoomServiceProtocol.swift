//
//  TemplateServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

private let cSceneId = "scene_ChatRoom"

public enum updateRoomState {
    case activeAlien
    case announcement
    case robotVoleme
}

// 房间内部需要用到环信KV
protocol ChatRoomServiceProtocol: NSObjectProtocol {
    
    /// 加入房间
    /// - Parameters:
    ///
    func joinRoom(_ roomId: String, completion: @escaping (Error?, VRRoomEntity?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///
    func leaveRoom(_ roomId: String, isOwner: Bool, completion: @escaping (Error?, Bool) -> Void)
    
    /// 获取房间详情
    /// - Parameters:
    ///
    func fetchRoomDetail(_ roomId: String,
                         completion: @escaping (Error?, VRRoomInfo?) -> Void)

    /// 邀请上卖
    /// - Parameters:
    ///
    func inviteUserToMic(_ user: VRUser,
                         completion: @escaping (Error?, Bool) -> Void)
    
    /// 拒绝上卖
    /// - Parameters:
    ///
    func refuseInviteToMic(_ user: VRUser,
                           completion: @escaping (Error?, Bool) -> Void)
    
    /// 发送礼物
    /// - Parameters:
    ///
    func sendGift(_ giftInfo: VoiceRoomGiftEntity,
                  completion: @escaping (Error?, Bool) -> Void)

    /// 获取礼物列表
    /// - Parameters:
    ///
    func fetchGiftContribute(_ roomId: String,
                             completion: @escaping (Error?, VRUsers?) -> Void)
    
    /// 获取人员列表
    /// - Parameters:
    ///
    func fetchRoomMembers(chatroom_id: String, completion: @escaping (Error?, VRUsers?) -> Void)

    /// 激活机器人,修改公告，修改机器人音量
    /// - Parameters:
    ///
    func modifyRoomInfo(chatroom_id: String,key: String,value: String,
                        completion: @escaping (Error?, Bool) -> Void)

    /// 禁言指定麦位
    /// - Parameters:
    ///
    func forbidMic(chatroom_id: String,mic_index: Int,
                   completion: @escaping (Error?, Bool) -> Void)

    /// 取消禁言指定麦位
    /// - Parameters:
    /// 
    func unForbidMic(chatroom_id: String,mic_index: Int,
                     completion: @escaping (Error?, Bool) -> Void)

    /// 锁麦
    /// - Parameters:
    ///
    func lockMic(chatroom_id: String,mic_index: Int,
                 completion: @escaping (Error?, Bool) -> Void)

    /// 取消锁麦
    /// - Parameters:
    ///
    func unLockMic(chatroom_id: String,mic_index: Int,
                   completion: @escaping (Error?, Bool) -> Void)

    /// 踢用户下麦
    /// - Parameters:
    ///
    func kickOff(chatroom_id: String,mic_index: Int,
                 completion: @escaping (Error?, Bool) -> Void)

    /// 下麦
    /// - Parameters:
    ///
    func leaveMic(chatroom_id: String,mic_index: Int,
                  completion: @escaping (Error?, Bool) -> Void)

    /// mute
    /// - Parameters:
    ///
    func muteLocal(chatroom_id: String,mic_index: Int,completion: @escaping (Error?, Bool) -> Void)

    /// unmute
    /// - Parameters:
    ///
    func unmuteLocal(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void)

    /// 换麦
    /// - Parameters:
    ///
    func changeMic(user: VRUser,old_index: Int,new_index:Int,completion: @escaping (Error?, Bool) -> Void)

    /// 取消邀请
    /// - Parameters:
    ///
    func refuseInvite(_ roomId: String,
                      completion: @escaping (Error?, Bool) -> Void)

    /// 接受邀请
    /// - Parameters:
    ///
    func agreeInvite(_ roomId: String,_ user: VRUser,
                     completion: @escaping (Error?, Bool) -> Void)

    /// 申请上麦
    /// - Parameters:
    ///
    func submitApply(chat_user: VRUser,
                     completion: @escaping (Error?, Bool) -> Void)

    /// 取消上麦
    /// - Parameters:
    ///
    func cancelApply(chat_uid: String,
                     completion: @escaping (Error?, Bool) -> Void)

    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func fetchRoomList(page: Int,
                     completion: @escaping (Error?, [VRRoomEntity]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
   func createRoom(room: VRRoomEntity,
                        completion: @escaping (SyncError?, VRRoomEntity?) -> Void)
}

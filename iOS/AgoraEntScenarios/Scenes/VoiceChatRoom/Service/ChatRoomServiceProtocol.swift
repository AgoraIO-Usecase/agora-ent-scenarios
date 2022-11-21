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

/// 房间内部需要用到环信KV
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
    func fetchRoomDetail(entity: VRRoomEntity, completion: @escaping (Error?, VRRoomInfo?) -> Void)

    /// 邀请上卖
    /// - Parameters:
    ///
    func inviteUserToMic(chatUid: String,index: Int?,completion: @escaping (Error?, Bool) -> Void)

    /// 获取礼物列表
    /// - Parameters:
    ///
    func fetchGiftContribute(completion: @escaping (Error?, [VRUser]?) -> Void)
    /// 获取人员列表
    /// - Parameters:
    ///
    func fetchRoomMembers(completion: @escaping (Error?, [VRUser]?) -> Void)

    /// 激活机器人,修改公告，修改机器人音量
    /// - Parameters:
    ///
    func modifyRoomInfo(key: String,value: String,
                        completion: @escaping (Error?, Bool) -> Void)

    /// 禁言指定麦位
    /// - Parameters:
    ///
    func forbidMic(mic_index: Int,
                   completion: @escaping (Error?, Bool) -> Void)

    /// 取消禁言指定麦位
    /// - Parameters:
    /// 
    func unForbidMic(mic_index: Int,
                     completion: @escaping (Error?, Bool) -> Void)

    /// 锁麦
    /// - Parameters:
    ///
    func lockMic(mic_index: Int,
                 completion: @escaping (Error?, Bool) -> Void)

    /// 取消锁麦
    /// - Parameters:
    ///
    func unLockMic(mic_index: Int,
                   completion: @escaping (Error?, Bool) -> Void)

    /// 踢用户下麦
    /// - Parameters:
    ///
    func kickOff(mic_index: Int,
                 completion: @escaping (Error?, Bool) -> Void)

    /// 下麦
    /// - Parameters:
    ///
    func leaveMic(mic_index: Int,
                  completion: @escaping (Error?, Bool) -> Void)

    /// mute
    /// - Parameters:
    ///
    func muteLocal(mic_index: Int,completion: @escaping (Error?, Bool) -> Void)

    /// unmute
    /// - Parameters:
    ///
    func unmuteLocal(mic_index: Int, completion: @escaping (Error?, Bool) -> Void)

    /// 换麦
    /// - Parameters:
    ///
    func changeMic(old_index: Int,new_index:Int,completion: @escaping (Error?, Bool) -> Void)

    /// 接受邀请
    /// - Parameters:
    ///
    func agreeInvite(completion: @escaping (Error?, Bool) -> Void)
    
    /// 拒绝邀请
    /// - Parameters:
    ///
    func refuseInvite(completion: @escaping (Error?, Bool) -> Void)

    /// 申请上麦
    /// - Parameters:
    ///
    func submitApply(index: Int?,completion: @escaping (Error?, Bool) -> Void)

    /// 取消上麦
    /// - Parameters:
    ///
    func cancelApply(chat_uid: String,
                     completion: @escaping (Error?, Bool) -> Void)
    
    /// Description 同意申请
    /// - Parameters:
    ///   - user: VRUser instance
    ///   - completion: 回调
    func agreeApply(chatUid: String, completion: @escaping (Error?) -> Void)

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
    func createRoom(room: VRRoomEntity, completion: @escaping (SyncError?, VRRoomEntity?) -> Void)
}

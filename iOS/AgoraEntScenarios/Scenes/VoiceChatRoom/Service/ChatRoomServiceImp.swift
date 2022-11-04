//
//  TemplateServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import KakaJSON

class ChatRoomServiceImp: NSObject {
    var roomId: String?
    var roomList: VRRoomsEntity?
    var userList: [VRUser]?
}

extension ChatRoomServiceImp: ChatRoomServiceProtocol {
    
    /// 加入房间
    /// - Parameters:
    ///
    func joinRoom(_ roomId: String,
                  completion: @escaping (SyncError?, Bool) -> Void) {
        
    }
    
    /// 离开房间
    /// - Parameters:
    ///
    func leaveRoom(_ roomId: String,
                   completion: @escaping (SyncError?, Bool) -> Void) {
        
    }

    /// 获取房间详情
    /// - Parameters:
    ///
    func fetchRoomDetail(_ roomId: String,
                         completion: @escaping (Error?, VRRoomInfo?) -> Void) {
        
    }
    
    /// 邀请上卖
    /// - Parameters:
    ///
    func inviteUserToMic(_ user: VRUser,
                         completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    /// 拒绝上卖
    /// - Parameters:
    ///
    func refuseInviteToMic(_ user: VRUser,
                           completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    /// 发送礼物
    /// - Parameters:
    ///
    func sendGift(_ giftInfo: (String, String, Int, String),
                  completion: @escaping (Error?, Bool) -> Void) {
        
    
    /// 获取礼物列表
    /// - Parameters:
    ///
    func fetchGiftContribute(_ roomId: String,
                             completion: @escaping (Error?, VRUsers?) -> Void) {
        
    }
    
    /// 获取人员列表
    /// - Parameters:
    ///
    func fetchRoomMembers(_ roomInfo: (String, String?, Int),
                          completion: @escaping (Error?, VRUsers?) -> Void) }

    }

    /// 禁言指定麦位
    /// - Parameters:
    ///
    func forbidMic(_ micInfo: (String, Int),
                   completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 取消禁言指定麦位
    /// - Parameters:
    ///
    func unForbidMic(_ micInfo: (String, Int),
                     completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 锁麦
    /// - Parameters:
    ///
    func lockMic(_ micInfo: (String, Int),
                 completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 取消锁麦
    /// - Parameters:
    ///
    func unLockMic(_ micInfo: (String, Int),
                   completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 踢用户下麦
    /// - Parameters:
    ///
    func kickOff(_ micInfo: (String, Int),
                 completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 下麦
    /// - Parameters:
    ///
    func leaveMic(_ micInfo: (String, Int),
                  completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// mute
    /// - Parameters:
    ///
    func muteLocal(_ micInfo: (String, Int),
                   completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// unmute
    /// - Parameters:
    ///
    func unmuteLocal(_ micInfo: (String, Int),
                     completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 换麦
    /// - Parameters:
    ///
    func changeMic(_ micInfo: (String, Int, Int),
                   completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 取消邀请
    /// - Parameters:
    ///
    func refuseInvite(_ roomId: String,
                      completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 接受邀请
    /// - Parameters:
    ///
    func agreeInvite(_ roomId: String,
                     completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 申请上麦
    /// - Parameters:
    ///
    func submitApply(_ micInfo: (String, Int?),
                     completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 取消上麦
    /// - Parameters:
    ///
    func cancelApply(_ roomId: String,
                     completion: @escaping (Error?, Bool) -> Void) {
        
    }

    /// 激活机器人,修改公告，修改机器人音量
    /// - Parameters:
    ///
    func modifyRoomInfo(_ info: (String, Any, updateRoomState),
                        completion: @escaping (SyncError?, Bool) -> Void) {
        
    }
}

// 房间外面需要用到syncManager
extension ChatRoomServiceProtocol {
    
    private func initScene(completion: @escaping () -> Void) {
        if syncUtilsInited {
            completion()
            return
        }

        SyncUtil.initSyncManager(sceneId: cSceneId) { [weak self] in
            guard let self = self else {
                return
            }
            self.syncUtilsInited = true

            completion()
        }
    }
    
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func fetchRoomList(page: Int,
                     completion: @escaping (Error?, [VRRoomEntity]?) -> Void) {
        initScene { [weak self] in
            SyncUtil.fetchAll { results in
                print("result == \(results.compactMap { $0.toJson() })")
                let entity = model(from: results.z.toDictionary() ?? [:], type: [VRRoomEntity].self) as? [VRRoomEntity]?
                self.roomList = entity
                completion(nil, entity)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity,
                    completion: @escaping (SyncError?) -> Void) {
        var room_entity: VRRoomEntity = VRRoomEntity()
        room_entity.rtc_uid: String = AppContext.shared.appId()
        room_entity.room_id: String = Date().milListamp
        room_entity.channel_id: String = Date().milListamp
        room_entity.soundEffect: String = room.sound_effect ?? "Social_Chat".localized()
        room_entity.is_private: Bool = room.is_private
        room_entity.name: String = ""
        room_entity.created_at: UInt = Int(Date().milListamp)
        room_entity.roomPassword: String = room.roomPassword
        room_entity.use_robot: Bool = false
        room_entity.robot_volume: Bool = false
        
        let owner: VRUser = VRUser()
        owner.rtc_uid: String = room_entity.rtc_uid
        owner.name: String = VLUserCenter.user.name
        room_entity.owner = owner
        
        let parmas = room_entity.z.toJson() as? [String: Any]
        
        
    }
    
    
    
    
}

extension Date {
    
    /// 获取当前 毫秒级 时间戳 - 13位
    var milListamp : String {
        let timeInterval: TimeInterval = self.timeIntervalSince1970
        let millisecond = CLongLong(round(timeInterval*1000))
        return "\(millisecond)"
    }
}


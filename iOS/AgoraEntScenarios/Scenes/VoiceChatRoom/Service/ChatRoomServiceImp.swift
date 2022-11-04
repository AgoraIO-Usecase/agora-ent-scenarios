//
//  TemplateServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import KakaJSON
import ZSwiftBaseLib

private let cSceneId = "scene_chatRoom"

class ChatRoomServiceImp: NSObject {
    var roomId: String?
    var roomList: VRRoomsEntity?
    var userList: [VRUser]?
    var syncUtilsInited: Bool = false
}

extension ChatRoomServiceImp: ChatRoomServiceProtocol {
    
    
    
    func joinRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func leaveRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func fetchRoomDetail(_ roomId: String, completion: @escaping (Error?, VRRoomInfo?) -> Void) {
        
    }
    
    func inviteUserToMic(_ user: VRUser, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func refuseInviteToMic(_ user: VRUser, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func sendGift(_ giftInfo: (String, String, Int, String), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func fetchGiftContribute(_ roomId: String, completion: @escaping (Error?, VRUsers?) -> Void) {
        
    }
    
    func fetchRoomMembers(_ roomInfo: (String, String?, Int), completion: @escaping (Error?, VRUsers?) -> Void) {
        
    }
    
    func modifyRoomInfo(_ info: (String, Any, updateRoomState), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func forbidMic(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func unForbidMic(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func lockMic(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func unLockMic(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func kickOff(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func leaveMic(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func muteLocal(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func unmuteLocal(_ micInfo: (String, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func changeMic(_ micInfo: (String, Int, Int), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func refuseInvite(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func agreeInvite(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func submitApply(_ micInfo: (String, Int?), completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func cancelApply(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }

    func initScene(completion: @escaping () -> Void) {
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
//                let entity = model(from: results.z.toDictionary() ?? [:], type: [VRRoomEntity].self as! any Convertible.Type) as? [VRRoomEntity]?
//                self.roomList = entity
//                completion(nil, entity)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity, completion: @escaping (SyncError?) -> Void) {
        var room_entity: VRRoomEntity = VRRoomEntity()
        room_entity.rtc_uid = Int(AppContext.shared.appId())
        
        let timeInterval: TimeInterval = Date().timeIntervalSince1970
        let millisecond = CLongLong(round(timeInterval*1000))
        
        room_entity.room_id = String(millisecond)
        room_entity.channel_id = String(millisecond)
        room_entity.sound_effect = room.sound_effect ?? "Social_Chat".localized()
        room_entity.is_private = room.is_private
        room_entity.name = ""
        room_entity.created_at = UInt(millisecond)
        room_entity.roomPassword = room.roomPassword
        room_entity.use_robot = false
        room_entity.robot_volume = 50
        
        let owner: VRUser = VRUser()
        owner.rtc_uid = AppContext.shared.appId()
        owner.name = VLUserCenter.user.name
        room_entity.owner = owner
        
        
    }
}


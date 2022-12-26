//
//  TemplateServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import KakaJSON
import ZSwiftBaseLib
import AgoraChat.AgoraChatError

private let cSceneId = "scene_chatRoom"

class ChatRoomServiceImp: NSObject {
    var roomId: String?
    var roomList: [VRRoomEntity]?
    var userList: [VRUser]?
    var syncUtilsInited: Bool = false
}

extension ChatRoomServiceImp: ChatRoomServiceProtocol {

    func convertError(error: AgoraChatError?) -> Error? {
        let vmError = VoiceRoomError()
        vmError.code = "\(error?.code ?? .chatroomNotJoined)"
        vmError.message = error?.errorDescription
        return vmError
    }
    
    func fetchRoomDetail(_ roomId: String, completion: @escaping (Error?, VRRoomInfo?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(chatroom_id: roomId, keys: ["owner","ranking_list","member_list"], completion: { error, map in
//            completion(self.convertError(error: error),(model(from: map ?? [:], type: VRUser.self) as! VRUser))
        })
    }
    
    func inviteUserToMic(_ user: VRUser, completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.sendCustomMessage(roomId: VoiceRoomIMManager.shared?.currentRoomId ?? "", event: VoiceRoomInviteSite, customExt: ["user" : user.kj.JSONString()], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func refuseInviteToMic(_ user: VRUser, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func sendGift(_ giftInfo: VoiceRoomGiftEntity, completion: @escaping (Error?, Bool) -> Void) {
        var map = [String:String]()
        for (key,value) in giftInfo.kj.JSONObject() {
            if key != "avatar" || key != "selected" {
                map[key] = value as? String
            }
        }
        VoiceRoomIMManager.shared?.sendCustomMessage(roomId: VoiceRoomIMManager.shared?.currentRoomId ?? "", event: VoiceRoomGift, customExt: map, completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func fetchGiftContribute(_ roomId: String, completion: @escaping (Error?, VRUsers?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(chatroom_id: roomId, keys: ["ranking_list"], completion: { error, map in
            completion(self.convertError(error: error),(model(from: map ?? [:], type: VRUsers.self) as! VRUsers))
        })
    }
    
    func fetchRoomMembers(chatroom_id: String, completion: @escaping (Error?, VRUsers?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(chatroom_id: chatroom_id, keys: ["member_list"], completion: { error, map in
            completion(self.convertError(error: error),(model(from: map ?? [:], type: VRUsers.self) as! VRUsers))
        })
    }
    
    func modifyRoomInfo(chatroom_id: String,key: String,value: String, completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: [key:value], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func forbidMic(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        if mic.status == 3 {
            mic.status = 4
        } else {
            mic.status = 2
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unForbidMic(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func lockMic(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        if mic.status == 2 {
            mic.status = 4
        } else {
            mic.status = 3
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unLockMic(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func kickOff(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        mic.status = -1
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func leaveMic(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        mic.status = -1
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func muteLocal(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        mic.status = 1
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unmuteLocal(chatroom_id: String,mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard var mic = VoiceRoomIMManager.shared?.mics?[mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: chatroom_id, attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func changeMic(user: VRUser,old_index: Int,new_index:Int,completion: @escaping (Error?, Bool) -> Void) {
        let old_mic = VRRoomMic()
        old_mic.status = -1
        old_mic.mic_index = old_index
        let new_mic = VRRoomMic()
        new_mic.status = 0
        new_mic.mic_index = new_index
        new_mic.member = user
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: VoiceRoomIMManager.shared?.currentRoomId ?? "", attributes: ["mic_\(old_index)":old_mic.kj.JSONString(),"mic_\(new_index)":new_mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func refuseInvite(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func agreeInvite(_ roomId: String,_ user: VRUser, completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        mic.mic_index = user.mic_index ?? 1
        mic.status = 0
        mic.member = user
        VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: roomId, attributes: ["mic_\(user.mic_index ?? 1)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func submitApply(chat_user: VRUser, completion: @escaping (Error?, Bool) -> Void){
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chat_user.chat_uid ?? "", event: VoiceRoomSubmitApplySite, customExt: ["chat_user" : chat_user.kj.JSONString()], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func cancelApply(chat_uid: String, completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chat_uid, event: VoiceRoomSubmitApplySite, customExt: [:], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
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
            SyncUtil.fetchAll { [weak self] results in
                print("result == \(results.compactMap { $0.toJson() })")
                
                let dataArray = results.map({ info in
                    return model(from: info.toJson()?.z.jsonToDictionary() ?? [:], VRRoomEntity.self)
                })
                self?.roomList = dataArray.sorted(by: {$0.created_at! > $1.created_at!})
                completion(nil, self?.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity, completion: @escaping (SyncError?, VRRoomEntity?) -> Void) {
        let room_entity: VRRoomEntity = VRRoomEntity()
        room_entity.rtc_uid = Int(VLUserCenter.user.id)
        
        let timeInterval: TimeInterval = Date().timeIntervalSince1970
        let millisecond = CLongLong(round(timeInterval*1000))
        
        room_entity.room_id = String(millisecond)
        room_entity.channel_id = String(millisecond)
        room_entity.sound_effect = room.sound_effect ?? "Social_Chat".localized()
        room_entity.is_private = room.is_private
        room_entity.name = room.name
        room_entity.created_at = UInt(millisecond)
        room_entity.roomPassword = room.roomPassword
        room_entity.click_count = 1
        
        let owner: VRUser = VRUser()
        owner.rtc_uid = VLUserCenter.user.id
        owner.name = VLUserCenter.user.name
        owner.uid = VLUserCenter.user.userNo
        owner.mic_index = 0
        owner.portrait = VLUserCenter.user.headUrl
        
        initIM(with: room_entity.name ?? "", pwd: room_entity.roomPassword) {[weak self] im_token, uid, room_id in
            
            owner.im_token = im_token
            owner.chat_uid = uid
            room_entity.chatroom_id = room_id
            room_entity.owner = owner
            
            //更新环信KV
            VoiceRoomIMManager.shared?.setChatroomAttributes(chatRoomId: room_id, attributes: (self?.createMics())!, completion: { error in
                if error == nil {
                    
                }
            })

            if let strongSelf = self {
                strongSelf.roomList?.append(room_entity)
                let params = room_entity.kj.JSONObject()
                strongSelf.initScene {
                    SyncUtil.joinScene(id: room_entity.room_id ?? "",
                                       userId: VLUserCenter.user.userNo,
                                       property: params) { result in
                        let model = model(from: result.toJson()?.z.jsonToDictionary() ?? [:], VRRoomEntity.self)
                        completion(nil,model)
                    } fail: { error in
                        completion(error, nil)
                    }
                }
            }
        }

    }
    
    func joinRoom(_ roomId: String, completion: @escaping (Error?, VRRoomEntity?) -> Void) {
        
        /**
         先拿到对应的房间信息
         1.修改click_count
         2.joinRTC + joinIM
         3.获取房间详情(KV）
         */
        if let roomList = self.roomList {
            for room in roomList {
                if room.room_id == roomId {
                    let updateRoom: VRRoomEntity = room
                    updateRoom.click_count = updateRoom.click_count ?? 0 + 1
                    let params = updateRoom.kj.JSONObject()
                    SyncUtil
                        .scene(id: roomId)?
                        .update(key: "",
                                data: params,
                                success: { obj in
                            print("updateUserCount success")
                            completion(nil, updateRoom)
                        },
                                fail: { error in
                            print("updateUserCount fail")
                            completion(error, nil)
                        })
                }
            }
        }
        
        VoiceRoomIMManager.shared?.joinedChatRoom(roomId: roomId, completion: { room, error in
          //  completion(self.convertError(error: error),error == nil)
//            VoiceRoomIMManager.shared?.sendCustomMessage(roomId: roomId, event: VoiceRoomJoinedMember, customExt: ["user" : user.kj.JSONString()], completion: { message, error in
//                completion(self.convertError(error: error),error != nil)
//            })
        })
    }
    
    func leaveRoom(_ roomId: String, isOwner: Bool, completion: @escaping (Error?, Bool) -> Void) {
        
        /**
         先拿到对应的房间信息
         1.如果是房主需要销毁房间，普通成员需要click_count - 1. 同时需要退出RTC+IM
         2.房主需要调用destory
         */
        if let roomList = self.roomList {
            for (index,room) in roomList.enumerated() {
                if room.room_id == roomId {
                    if isOwner {
                        SyncUtil.leaveScene(id: roomId)
                        self.roomList?.remove(at: index)
                    } else {
                        let updateRoom: VRRoomEntity = room
                        updateRoom.click_count = updateRoom.click_count ?? 0 - 1
                        let params = updateRoom.kj.JSONObject()
                        SyncUtil
                            .scene(id: roomId)?
                            .update(key: "",
                                    data: params,
                                    success: { obj in
                                print("updateUserCount success")
                            },
                                    fail: { error in
                                print("updateUserCount fail")
                            })
                    }
                }
            }
        }
        
        VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
    }
    
    func createMics() -> [String:String] {
        var mics = [VRRoomMic]()
        let mic = VRRoomMic()
        mic.mic_index = 0
        mic.status = 0
        mic.member = VRUser()
        mic.member?.uid = VLUserCenter.user.userNo
        mic.member?.chat_uid = ""
        mic.member?.mic_index = 0
        mic.member?.portrait = VoiceRoomUserInfo.shared.currentRoomOwner?.portrait
        mic.member?.rtc_uid = AppContext.shared.appId()
        mic.member?.channel_id = ""
        mic.member?.im_token = ""
        mics.append(mic)
        for i in 1...7 {
            let item = VRRoomMic()
            item.mic_index = i
            if i < 6 {
                item.mic_index = -1
            } else {
                item.mic_index = -2
            }
            mics.append(item)
        }
        VoiceRoomIMManager.shared?.mics = mics
        var micsMap = [String:String]()
        for (idx,item) in mics.enumerated() {
            micsMap["mic_\(idx)"] = item.kj.JSONString()
        }
        
        micsMap["use_robot"] = "0"
        micsMap["robot_volume"] = "50"
        micsMap["gift_amount"] = "0"
        return micsMap
    }
    
    func initIM(with roomName: String, pwd: String, completion: @escaping (String, String, String) -> Void) {

        let VMGroup = DispatchGroup()
        let VMQueue = DispatchQueue(label: "com.agora.vm.www")
        var im_token = ""
        var im_uid = ""
        var chatroom_id = ""
        
        VMGroup.enter()
        VMQueue.async {
            NetworkManager.shared.generateToken(channelName: roomName, uid: VLUserCenter.user.userNo, tokenType: .token007, type: .chat) { token in
                im_token = token ?? ""
                VMGroup.leave()
            }
        }
        
        VMGroup.enter()
        VMQueue.async {
            NetworkManager.shared.generateIMConfig(channelName: roomName, nickName: VLUserCenter.user.name, password: pwd, uid:  VLUserCenter.user.userNo) { uid, room_id in
                im_uid = uid ?? ""
                chatroom_id = room_id ?? ""
                VMGroup.leave()
            }
        }
        
        VMGroup.notify(queue: VMQueue) {[weak self] in
            completion(im_token, im_uid, chatroom_id)
        }
    }
}


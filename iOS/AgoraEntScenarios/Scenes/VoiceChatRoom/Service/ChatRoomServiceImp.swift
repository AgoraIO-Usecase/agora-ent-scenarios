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

public class ChatRoomServiceImp: NSObject {
    private static var _sharedInstance: ChatRoomServiceImp?
    var roomId: String?
    var roomList: [VRRoomEntity]?
    var userList: [VRUser]?
    public var mics: [VRRoomMic] = [VRRoomMic]()
    public var applicants: [VoiceRoomApply] = [VoiceRoomApply]()
    var syncUtilsInited: Bool = false
    @objc public weak var roomServiceDelegate:ChatRoomServiceSubscribeDelegate?
}

extension ChatRoomServiceImp: VoiceRoomIMDelegate {
    
    public func chatTokenWillExpire(code: AgoraChatErrorCode) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.chatTokenWillExpire)) {
            self.roomServiceDelegate?.chatTokenWillExpire()
        }
    }
    
    public func receiveTextMessage(roomId: String, message: VoiceRoomChatEntity) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.receiveTextMessage(roomId:message:))) {
            self.roomServiceDelegate?.receiveTextMessage(roomId: roomId, message: message)
        }
    }
    
    public func receiveGift(roomId: String, meta: [String : String]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onReceiveGift(roomId:gift:))) {
            guard let dic = meta else { return }
            let gift = model(from: dic, VoiceRoomGiftEntity.self)
            self.roomServiceDelegate?.onReceiveGift(roomId: roomId, gift: gift)
        }
    }
    
    public func receiveApplySite(roomId: String, meta: [String : String]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onReceiveSeatRequest(roomId:applicant:))) {
            guard let map = meta?["user"]?.z.jsonToDictionary() else { return }
            let apply = model(from: map, type: VoiceRoomApply.self) as! VoiceRoomApply
            self.applicants.append(apply)
            self.roomServiceDelegate?.onReceiveSeatRequest(roomId: roomId, applicant: apply)
        }
    }
    
    public func receiveCancelApplySite(roomId: String, chat_uid: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onReceiveSeatRequestRejected(roomId:chat_uid:))) {
            self.roomServiceDelegate?.onReceiveSeatRequestRejected(roomId: roomId, chat_uid: chat_uid)
        }
    }
    
    public func receiveInviteSite(roomId: String, meta: [String : String]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onReceiveSeatInvitation(roomId:user:))) {
            guard let map = meta?["user"] else { return }
            guard let user = model(from: map, VRUser.self) else { return }
            if VoiceRoomUserInfo.shared.user?.uid ?? "" != user.uid ?? "" {
                return
            }
            self.roomServiceDelegate?.onReceiveSeatInvitation(roomId: roomId, user: user)
        }
    }
    
    public func refuseInvite(roomId: String, meta: [String : String]?) {
        
    }
    
    public func userJoinedRoom(roomId: String, username: String, ext: [String : Any]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onUserJoinedRoom(roomId:user:))) {
            guard let map = ext else { return }
            self.roomServiceDelegate?.onUserJoinedRoom(roomId: roomId, user: model(from: map, VRUser.self))
        }
    }
    
    public func announcementChanged(roomId: String, content: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onAnnouncementChanged(roomId:content:))) {
            self.roomServiceDelegate?.onAnnouncementChanged(roomId: roomId, content: content)
        }
    }
    
    public func voiceRoomUpdateRobotVolume(roomId: String, volume: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onRobotVolumeUpdated(roomId:volume:))) {
            self.roomServiceDelegate?.onRobotVolumeUpdated(roomId: roomId, volume: volume)
        }
    }
    
    public func userBeKicked(roomId: String, reason: AgoraChatroomBeKickedReason) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onUserBeKicked(roomId:reason:))) {
            self.roomServiceDelegate?.onUserBeKicked(roomId: roomId, reason: ChatRoomServiceKickedReason(rawValue: UInt(reason.rawValue)) ?? .removed)
        }
    }
    
    public func roomAttributesDidUpdated(roomId: String, attributeMap: [String : String]?, from fromId: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onRoomSiteDidUpdated(roomId:attributeMap:from:))) {
            self.roomServiceDelegate?.onRoomSiteDidUpdated(roomId: roomId, attributeMap: attributeMap, from: fromId)
        }
    }
    
    public func memberLeave(roomId: String, userName: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onMemberLeave(roomId:userName:))) {
            self.roomServiceDelegate?.onMemberLeave(roomId: roomId, userName: userName)
        }
    }
    
}

extension ChatRoomServiceImp: ChatRoomServiceProtocol {
    
    func updateAnnouncement(content: String, completion: @escaping (Bool) -> Void) {
        VoiceRoomIMManager.shared?.updateAnnouncement(content: content, completion: completion)
    }
    
    func enableRobot(enable: Bool,completion: @escaping (Error?) -> Void) {
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["use_robot":(enable ? "1":"0")], completion: { error in
            completion(self.convertError(error: error))
        })
    }
    
    func updateRobotVolume(value: Int,completion: @escaping (Error?) -> Void) {
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["robot_volume":"\(value)"], completion: { error in
            completion(self.convertError(error: error))
        })
    }
    
    
    func subscribeEvent(with delegate: ChatRoomServiceSubscribeDelegate) {
        VoiceRoomIMManager.shared?.delegate = self
        VoiceRoomIMManager.shared?.addChatRoomListener()
        self.roomServiceDelegate = delegate
    }
    
    func unsubscribeEvent() {
        VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
        self.roomServiceDelegate = nil
    }
    
    // 单例
    @objc public class func getSharedInstance() -> ChatRoomServiceImp {
        guard let instance = _sharedInstance else {
            _sharedInstance = ChatRoomServiceImp()
            return _sharedInstance!
        }
        return instance
    }

    func convertError(error: AgoraChatError?) -> Error? {
        let vmError = VoiceRoomError()
        vmError.code = "\(error?.code ?? .chatroomNotJoined)"
        vmError.message = error?.errorDescription
        return error == nil ? nil:vmError
    }
    
    func fetchRoomDetail(entity: VRRoomEntity, completion: @escaping (Error?, VRRoomInfo?) -> Void) {
        let keys = ["ranking_list","member_list","gift_amount","mic_0","mic_1","mic_2","mic_3","mic_4","mic_5","mic_6","mic_7","robot_volume","use_robot"]
        let roomInfo = VRRoomInfo()
        roomInfo.room = entity
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: keys, completion: { error, map in
            if let ranking_list = map?["ranking_list"]?.toArray() {
                print("ranking_list: \(ranking_list)")
                roomInfo.room?.ranking_list = ranking_list.kj.modelArray(VRUser.self)
            } else {
                roomInfo.room?.ranking_list = [VRUser]()
            }
            if let member_list = map?["ranking_list"]?.toArray() {
                print("member_list: \(member_list)")
                roomInfo.room?.member_list = member_list.kj.modelArray(VRUser.self)
            } else {
                roomInfo.room?.member_list = [VRUser]()
            }
            if let gift_amount = map?["gift_amount"] as? String {
                roomInfo.room?.gift_amount = Int(gift_amount)
            }
            if let use_robot = map?["use_robot"] as? String {
                roomInfo.room?.use_robot = (Int(use_robot) ?? 0 > 0)
            } else {
                roomInfo.room?.use_robot = false
            }
            if let robot_volume = map?["robot_volume"] as? String {
                roomInfo.room?.robot_volume = UInt(robot_volume) ?? 50
            } else {
                roomInfo.room?.robot_volume = 50
            }
            let mics = map?.filter({
                $0.key.hasSuffix("mic_")
            })
            var micsJson = [Dictionary<String,Any>]()
            if mics?.keys.count ?? 0 > 0 {
                for key in mics!.keys {
                    micsJson.append(mics?[key]?.z.jsonToDictionary() ?? [:])
                }
                roomInfo.mic_info = micsJson.kj.modelArray(VRRoomMic.self).sorted(by: {
                    $0.mic_index < $1.mic_index
                })
            }
            if entity.owner == nil {
                roomInfo.room?.owner = roomInfo.mic_info?.first?.member
            }
            completion(self.convertError(error: error),roomInfo)
        })
    }
    
    func startMicSeatInvitation(chatUid: String,index: Int?,completion: @escaping (Error?, Bool) -> Void) {
        let user = self.userList?.first(where: { $0.chat_uid == chatUid })
        user?.mic_index = index
        VoiceRoomIMManager.shared?.sendCustomMessage(roomId: VoiceRoomIMManager.shared?.currentRoomId ?? "", event: VoiceRoomInviteSite, customExt: ["user" : user?.kj.JSONString() ?? ""], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func fetchGiftContribute(completion: @escaping (Error?, [VRUser]?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: ["ranking_list"], completion: { error, map in
            if let ranking_list = map?["ranking_list"]?.toArray() {
                completion(self.convertError(error: error),ranking_list.kj.modelArray(VRUser.self))
            }
        })
    }
    
    func fetchRoomMembers(completion: @escaping (Error?, [VRUser]?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: ["member_list"], completion: { error, map in
            if let member_list = map?["member_list"]?.toArray() {
                completion(self.convertError(error: error),member_list.kj.modelArray(VRUser.self))
            }
        })
    }
    
    func modifyRoomInfo(key: String,value: String, completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: [key:value], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func forbidMic(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 3 {
            mic.status = 4
        } else {
            mic.status = 2
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unForbidMic(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func lockMic(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 2 {
            mic.status = 4
        } else {
            mic.status = 3
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unLockMic(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func kickOff(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        mic.status = -1
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func leaveMic(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        mic.status = -1
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func muteLocal(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 1
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func unmuteLocal(mic_index: Int, completion: @escaping (Error?, Bool) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func normalError() -> VoiceRoomError {
        let error = VoiceRoomError()
        error.code = "403"
        error.message = "Dose't support!"
        return error
    }
    
    func changeMic(old_index: Int,new_index:Int,completion: @escaping (Error?, Bool) -> Void) {
        if self.mics[safe: new_index]?.member != nil {
            completion(self.normalError(),false)
            return
        }
        let old_mic = VRRoomMic()
        old_mic.status = -1
        old_mic.mic_index = old_index
        let new_mic = VRRoomMic()
        new_mic.status = 0
        new_mic.mic_index = new_index
        new_mic.member = VoiceRoomUserInfo.shared.user
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(old_index)":old_mic.kj.JSONString(),"mic_\(new_index)":new_mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func refuseInvite(completion: @escaping (Error?, Bool) -> Void) {
        
    }
    
    func acceptMicSeatInvitation(completion: @escaping (Error?, Bool) -> Void) {
        let mic = VRRoomMic()
        let user = serviceImp?.userList?.first(where: {
            $0.uid == VoiceRoomUserInfo.shared.user?.uid ?? ""
        })
        if user?.mic_index ?? 0 > 1 {
            mic.mic_index = user?.mic_index ?? 1
        } else {
            mic.mic_index = self.findMicIndex()
        }
        mic.status = 0
        mic.member = user
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(user?.mic_index ?? 1)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    /// Description 提交上麦申请
    /// - Parameters:
    ///   - chat_user: 提交的用户模型，包含申请的麦位信息，若没有顺序分配
    ///   - completion: 回调
    func startMicSeatApply(index: Int?,completion: @escaping (Error?, Bool) -> Void) {
        let apply = VoiceRoomApply()
        apply.created_at = UInt64(Date().timeIntervalSince1970)
        apply.member = VoiceRoomUserInfo.shared.user
        if let idx = index {
            apply.index = idx
        } else {
            apply.index = self.findMicIndex()
        }
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: VoiceRoomUserInfo.shared.user?.chat_uid ?? "", event: VoiceRoomApplySite, customExt: ["user" : apply.kj.JSONString()], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func endMicSeatApply(chat_uid: String, completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chat_uid, event: VoiceRoomCancelApplySite, customExt: [:], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func endMicSeatApply(chatUid: String, completion: @escaping (Error?) -> Void) {
        var mic_index = 1
        let user = self.applicants.first(where: {
            $0.member?.chat_uid ?? "" == chatUid
        })
        if user?.member?.mic_index ?? 0 < 1 {
            mic_index = self.findMicIndex()
        } else {
            mic_index = user?.member?.mic_index ?? 1
        }
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        mic.status = 0
        mic.member = user?.member
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error))
        })
    }
    
    func findMicIndex() -> Int {
        var mic_index = 0
        for i in 0...7 {
            let mic = self.mics[safe: i]
            if mic?.member == nil {
                mic_index = mic?.mic_index ?? 1
                break
            }
        }
        return mic_index
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
                self?.roomList = dataArray.sorted(by: {$0.created_at ?? 0 > $1.created_at ?? 0})
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
        room_entity.sound_effect = room.sound_effect
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
        
        initIM(with: room_entity.name ?? "", pwd: "12345678") {[weak self] im_token, uid, room_id in
            owner.chat_uid = uid
            room_entity.chatroom_id = room_id
            room_entity.owner = owner
            VLUserCenter.user.im_token = im_token
            VLUserCenter.user.chat_uid = uid
            
            if let strongSelf = self {
                strongSelf.roomList?.append(room_entity)
                let params = room_entity.kj.JSONObject()
                strongSelf.initScene {
                    SyncUtil.joinScene(id: room_entity.room_id ?? "",
                                       userId:VLUserCenter.user.userNo,
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
         1.获取用户信息
         2.修改click_count
         3.更新syncManager
         4.加入语聊房，更新KV
         */
        
        if let roomList = self.roomList {
            for room in roomList {
                if room.room_id == roomId {
                    let updateRoom: VRRoomEntity = room
                    updateRoom.member_count = updateRoom.member_count ?? 0 + 1
                    let params = updateRoom.kj.JSONObject()
                    
                    //获取IM信息
                    self.initIM(with: room.name ?? "", pwd: "12345678") { im_token, chat_uid, chatroom_id in
                        VLUserCenter.user.im_token = im_token
                        VLUserCenter.user.chat_uid = chat_uid
                        completion(nil, updateRoom)
                    }
                    
                    initScene{
                        SyncUtil
                            .scene(id: roomId)?
                            .update(key: "",
                                    data: params,
                                    success: { obj in
                                print("updateUserCount success")
                            },
                                    fail: { error in
                                print("updateUserCount fail")
                                completion(error, nil)
                            })
                    }
                }
            }
        }
        
        
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
                        updateRoom.member_count = updateRoom.member_count ?? 0 - 1
                        let params = updateRoom.kj.JSONObject()
                        SyncUtil
                            .scene(id: roomId)?
                            .update(key: "",
                                    data: params,
                                    success: { obj in
                                print("updateUserCount success")
                                VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
                            },
                                    fail: { error in
                                print("updateUserCount fail")
                                VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
                            })
                    }
                }
            }
        }
        
        
    }
    
    func createMics() -> [String:String] {
        var mics = [VRRoomMic]()
        let mic = VRRoomMic()
        mic.mic_index = 0
        mic.status = 0
        mic.member = VRUser()
        mic.member?.uid = VLUserCenter.user.userNo
        mic.member?.name = VLUserCenter.user.name
        mic.member?.chat_uid = ""
        mic.member?.mic_index = 0
        mic.member?.name = VLUserCenter.user.name
        mic.member?.portrait = VoiceRoomUserInfo.shared.currentRoomOwner?.portrait
        mic.member?.rtc_uid = AppContext.shared.appId()
        mic.member?.channel_id = ""
        mic.member?.im_token = ""
        mics.append(mic)
        for i in 1...7 {
            let item = VRRoomMic()
            item.mic_index = i
            if i < 6 {
                item.status = -1
            } else {
                item.status = -2
            }
            mics.append(item)
        }
        self.mics = mics
        var micsMap = [String:String]()
        for (idx,item) in mics.enumerated() {
            micsMap["mic_\(idx)"] = item.kj.JSONString()
        }
        micsMap["use_robot"] = "0"
        micsMap["robot_volume"] = "50"
        return micsMap
    }
    
    func initIM(with roomName: String, pwd: String, completion: @escaping (String, String, String) -> Void) {

        var im_token = ""
        var im_uid = ""
        var chatroom_id = ""
        
        NetworkManager.shared.generateIMConfig(channelName: roomName, nickName: VLUserCenter.user.name, password: pwd, uid:  VLUserCenter.user.id) { uid, room_id, token in
            im_uid = uid ?? ""
            chatroom_id = room_id ?? ""
            im_token = token ?? ""
            NetworkManager.shared.generateToken(channelName: roomName, uid: VLUserCenter.user.id, tokenType: .token007, type: .rtc) { token in
                VLUserCenter.user.agoraRTCToken = token ?? ""
                completion(im_token, im_uid, chatroom_id )
            }
        }
    }
}


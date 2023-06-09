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
import AgoraSyncManager

private let cSceneId = "scene_chatRoom_3.1.0"


private func agoraPrint(_ message: String) {
    voiceLogger.info(message, context: "Service")
}

let voiceLogger = AgoraEntLog.createLog(config: AgoraEntLogConfig.init(sceneName: "VoiceChat"))
public class ChatRoomServiceImp: NSObject {
    static var _sharedInstance: ChatRoomServiceImp?
    var roomId: String?
    var roomList: [VRRoomEntity]?
    var userList: [VRUser]?
    public var mics: [VRRoomMic] = [VRRoomMic]()
    public var applicants: [VoiceRoomApply] = [VoiceRoomApply]()
    var syncUtilsInited: Bool = false
    @objc public weak var roomServiceDelegate:ChatRoomServiceSubscribeDelegate?
    
    func cleanCache() {
        self.userList = nil
        self.roomId = nil
        self.mics.removeAll()
        self.applicants.removeAll()
    }
    
    
    fileprivate func _checkRoomExpire() {
        guard let room = self.roomList?.filter({$0.room_id == roomId}).first, let created_at = room.created_at else { return }
        
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = 20 * 60 * 1000
        agoraPrint("checkRoomExpire: \(currentTs - Int64(created_at)) / \(expiredDuration)")
        guard currentTs - Int64(created_at) > expiredDuration else { return }
        
        self.roomServiceDelegate?.onRoomExpired()
    }
    
    fileprivate func _startCheckExpire() {
        Timer.scheduledTimer(withTimeInterval: 5, repeats: true) { [weak self] timer in
            guard let self = self else { return }
            
            self._checkRoomExpire()
            if self.roomId == nil {
                timer.invalidate()
            }
        }
        
        DispatchQueue.main.async {
            self._checkRoomExpire()
        }
    }
}

//MARK: VoiceRoomIMDelegate
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
                guard let map = meta?["user"],let chatroomId = meta?["chatroomId"] else { return }
                if chatroomId != VoiceRoomIMManager.shared?.currentRoomId ?? "" {
                    return
                }
                let apply = model(from: map, type: VoiceRoomApply.self) as! VoiceRoomApply
                let user = self.applicants.first {
                    $0.member?.chat_uid ?? "" == apply.member?.chat_uid ?? ""
                }
                if user == nil {
                    self.applicants.append(apply)
                }
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
            guard let map = meta?["user"],let chatroomId = meta?["chatroomId"] else { return }
            guard let user = model(from: map, VRUser.self) else { return }
            if VoiceRoomUserInfo.shared.user?.uid ?? "" != user.uid ?? "",chatroomId != VoiceRoomIMManager.shared?.currentRoomId ?? "" {
                return
            }
            self.roomServiceDelegate?.onReceiveSeatInvitation(roomId: roomId, user: user)
        }
    }
    
    public func refuseInvite(roomId: String, chat_uid: String , meta: [String : String]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onReceiveCancelSeatInvitation(roomId:chat_uid:))) {
            guard let chatroomId = meta?["chatroomId"] else { return }
            if VoiceRoomUserInfo.shared.user?.uid ?? "" != chat_uid,chatroomId != VoiceRoomIMManager.shared?.currentRoomId ?? "" {
                return
            }
            self.roomServiceDelegate?.onReceiveCancelSeatInvitation(roomId: chatroomId, chat_uid: chat_uid)
        }
    }
    
    public func userJoinedRoom(roomId: String, username: String, ext: [String : Any]?) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onUserJoinedRoom(roomId:user:))) {
            guard let map = ext,let userMap = map["user"] as? String else { return }
            self.roomServiceDelegate?.onUserJoinedRoom(roomId: roomId, user: model(from: userMap.z.jsonToDictionary(), VRUser.self))
        }
    }
    
    public func announcementChanged(roomId: String, content: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onAnnouncementChanged(roomId:content:))) {
            self.roomServiceDelegate?.onAnnouncementChanged(roomId: roomId, content: content)
        }
    }
    
    public func userBeKicked(roomId: String, reason: AgoraChatroomBeKickedReason) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onUserBeKicked(roomId:reason:))) {
            self.roomServiceDelegate?.onUserBeKicked(roomId: roomId, reason: ChatRoomServiceKickedReason(rawValue: UInt(reason.rawValue)) ?? .removed)
        }
    }
    
    public func roomAttributesDidUpdated(roomId: String, attributeMap: [String : String]?, from fromId: String) {
        guard let map = attributeMap else { return }
        if map.keys.contains(where: { $0.hasPrefix("mic_") }) {
            if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onSeatUpdated(roomId:mics:from:))) {
                self.roomServiceDelegate?.onSeatUpdated(roomId: roomId, mics: self.parserMics(map: map), from: fromId)
            }
        }
        if map.keys.contains(where: { $0.hasPrefix("use_robot") }) {
            if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onRobotSwitch(roomId:enable:from:))) {
                guard let use_robot = map["use_robot"],let enable = Int(use_robot) else { return }
                self.roomServiceDelegate?.onRobotSwitch(roomId: roomId, enable: enable == 1, from: fromId)
            }
        }
        if map.keys.contains(where: { $0.hasPrefix("robot_volume") }) {
            guard let robot_volume = map["robot_volume"] else { return }
            self.roomServiceDelegate?.onRobotVolumeChanged(roomId: roomId, volume: UInt(robot_volume) ?? 50, from: fromId)
        }
        if map.keys.contains(where: { $0.hasPrefix("ranking_list") }) {
            guard let json = map["ranking_list"] else { return }
            let ranking_list = json.toArray()?.kj.modelArray(VRUser.self)
            self.roomServiceDelegate?.onContributionListChanged(roomId: roomId, ranking_list: ranking_list ?? [], from: fromId)
        }
    }
    
    func parserMics(map: [String:String]) -> [VRRoomMic] {
        var mics = [VRRoomMic]()
        for key in map.keys {
            if key.hasPrefix("mic_") {
                let value: String = map[key] ?? ""
                let mic_dic: [String: Any] = value.z.jsonToDictionary()
                let mic: VRRoomMic = model(from: mic_dic, type: VRRoomMic.self) as! VRRoomMic
                mics.append(mic)
            }
        }
        return mics
    }
    
    public func memberLeave(roomId: String, userName: String) {
        if self.roomServiceDelegate != nil,self.roomServiceDelegate!.responds(to: #selector(ChatRoomServiceSubscribeDelegate.onUserLeftRoom(roomId:userName:))) {
            self.mics.first { $0.member?.chat_uid ?? "" == userName }?.member = nil
            self.roomServiceDelegate?.onUserLeftRoom(roomId: roomId, userName: userName)
        }
    }
    
}

//MARK: ChatRoomServiceProtocol
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
        VoiceRoomIMManager.shared?.removeListener()
        VoiceRoomIMManager.shared?.currentRoomId = ""
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
        let group = DispatchGroup()
        group.enter()
        VoiceRoomIMManager.shared?.fetchChatroomAnnouncement(completion: { content in
            roomInfo.room?.announcement = content
            group.leave()
        })
        group.enter()
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: keys, completion: { error, map in
            if let ranking_list = map?["ranking_list"]?.toArray() {
                agoraPrint("ranking_list: \(ranking_list)")
                roomInfo.room?.ranking_list = ranking_list.kj.modelArray(VRUser.self)
            } else {
                roomInfo.room?.ranking_list = [VRUser]()
            }
            if let member_list = map?["member_list"]?.toArray() {
                agoraPrint("member_list: \(member_list)")
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
                $0.key.hasPrefix("mic_")
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
            group.leave()
        })
        group.notify(queue: .main) {
            completion(self.convertError(error: nil), roomInfo)
        }
    }
    
    func fetchGiftContribute(completion: @escaping (Error?, [VRUser]?) -> Void) {
        VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: ["ranking_list"], completion: { error, map in
            if let ranking_list = map?["ranking_list"]?.toArray() {
                completion(self.convertError(error: error),ranking_list.kj.modelArray(VRUser.self).sorted(by: { $0.amount ?? 0 > $1.amount ?? 0
                }))
            }
        })
    }
    
    func fetchRoomMembers(completion: @escaping (Error?, [VRUser]?) -> Void) {
        if self.userList?.count ?? 0 > 0 {
            var mics_id = ""
            for i in 1...6 {
                let mic = self.mics[safe: i]
                if mic?.member != nil {
                    mics_id += mic?.member?.chat_uid ?? ""
                }
            }
            let list = self.userList?.filter({
                mics_id.z.rangeOfString($0.chat_uid ?? "").length >= 0
            })
            completion(nil,list)

        } else {
            VoiceRoomIMManager.shared?.fetchChatroomAttributes(keys: ["member_list"], completion: { error, map in
                if let member_list = map?["member_list"]?.toArray() {
                    completion(self.convertError(error: error),member_list.kj.modelArray(VRUser.self))
                }
            })
        }
    }
    
    func updateRoomMembers(completion: @escaping (Error?) -> Void) {
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["member_list":self.userList?.kj.JSONString() ?? ""], completion: { error in
            completion(self.convertError(error: error))
        })
    }

    func fetchApplicantsList(completion: @escaping (Error?, [VoiceRoomApply]?) -> Void) {
        completion(nil,self.applicants)
    }
    
    func forbidMic(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 3 {
            mic.status = 4
        } else {
            mic.status = 2
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func unForbidMic(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 4 {
            mic.status = 3
        } else {
            if mic.status == 2 {
                mic.status = (mic.member == nil ? -1 : 0)
            }
        }
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func cleanUserMicIndex(mic: VRRoomMic) {
        let user = self.userList?.first(where: {
            $0.chat_uid ?? "" == mic.member?.chat_uid ?? ""
        })
        user?.mic_index = -1
    }
    
    func lockMic(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 2 {
            mic.status = 4
        } else {
            mic.status = 3
        }
        self.cleanUserMicIndex(mic: mic)
        mic.member = nil
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func unLockMic(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 4 {
            mic.status = 2
        } else {
            if mic.status == 3 {
                mic.status = (mic.member == nil ? -1 : 0)
            }
        }

        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func kickOff(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        let mic = VRRoomMic()
        guard let oldMic = self.mics[safe: mic_index] else { return }
        mic.mic_index = mic_index
        mic.status = (oldMic.status == 2 ? 2:-1)
        self.cleanUserMicIndex(mic: oldMic)
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[mic_index] = mic
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func leaveMic(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard mic_index < mics.count else {return}
        let mic = VRRoomMic()
        guard let oldMic = self.mics[safe: mic_index] else { return }
        mic.mic_index = mic_index
        mic.status = oldMic.status == 2 ? 2:-1
        self.cleanUserMicIndex(mic: self.mics[mic_index])
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { [weak self] error in
            if error == nil,self?.mics.count ?? 0 > 0 {
                self?.mics[mic_index] = mic
            }
            completion(self?.convertError(error: error),mic)
        })
    }
    
    func muteLocal(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 1
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = 1
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func unmuteLocal(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        mic.status = 0
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = 0
            }
            completion(self.convertError(error: error),mic)
        })
    }
    
    func normalError() -> VoiceRoomError {
        let error = VoiceRoomError()
        error.code = "403"
        error.message = "Doesn't support!" 
        return error
    }
    
    func changeMicUserStatus(status: Int,completion: @escaping (Error?, VRRoomMic?) -> Void) {
        guard let mic = self.mics.first(where: { $0.member?.uid ?? "" == VoiceRoomUserInfo.shared.user?.chat_uid ?? ""
        }) else { return }
        mic.member?.micStatus = status
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic.mic_index)":mic.kj.JSONString()], completion: { error in
            completion(self.convertError(error: error),mic)
        })
    }
    
    func changeMic(old_index: Int,new_index:Int,completion: @escaping (Error?, [Int:VRRoomMic]?) -> Void) {
        if self.mics[safe: new_index]?.member != nil {
            completion(self.normalError(),nil)
            return
        }
        let old_mic = mics[safe: old_index]
        let old_medber = old_mic?.member
        let new_mic = mics[safe: new_index]
        
        old_mic?.member = new_mic?.member
        new_mic?.member = old_medber
        old_mic?.mic_index = old_index
        new_mic?.mic_index = new_index
        
        if old_mic?.status == 3 || old_mic?.status == 4 {
            completion(self.normalError(),nil)
            return
        }
        if new_mic?.status == 3 || new_mic?.status == 4 {
            completion(self.normalError(),nil)
            return
        }
        new_mic?.status = (new_mic!.status != 2 ? 0:2)
        old_mic?.status = (old_mic!.status != 2 ? -1:2)
        guard old_index != new_index else { return }
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(old_index)": old_mic?.kj.JSONString() ?? "",
                                                                       "mic_\(new_index)": new_mic?.kj.JSONString() ?? ""],
                                                          completion: { error in
            if let old_mic = old_mic, let new_mic = new_mic {
                completion(self.convertError(error: error),[old_index: old_mic,
                                                            new_index: new_mic])
            }
        })
    }
    
    func refuseInvite(chat_uid: String,completion: @escaping (Error?, Bool) -> Void) {
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chat_uid, event: VoiceRoomCancelInviteSite, customExt: ["chatroomId":VoiceRoomIMManager.shared?.currentRoomId ?? ""], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func startMicSeatInvitation(chatUid: String,index: Int?,completion: @escaping (Error?, Bool) -> Void) {
        if !self.checkWhetherApplyMic() {
            completion(self.normalError(),false)
            return
        }
        let user = self.userList?.first(where: { $0.chat_uid == chatUid })
        user?.mic_index = index
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chatUid, event: VoiceRoomInviteSite, customExt: ["user" : user?.kj.JSONString() ?? "","chatroomId":VoiceRoomIMManager.shared?.currentRoomId ?? ""], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    func acceptMicSeatInvitation(index: Int?,completion: @escaping (Error?, VRRoomMic?) -> Void) {
        if !self.checkWhetherApplyMic() {
            completion(self.normalError(),nil)
            return
        }
        let mic = VRRoomMic()
        let user = ChatRoomServiceImp.getSharedInstance().userList?.first(where: {
            $0.uid == VoiceRoomUserInfo.shared.user?.uid ?? ""
        })
        if index != nil {
            mic.mic_index = index ?? self.findMicIndex()
        } else {
            mic.mic_index = self.findMicIndex()
        }
        switch self.mics[safe: mic.mic_index]?.status ?? -1 {
        case 2:
            mic.status = self.mics[mic.mic_index].status
        case 3,4:
            completion(self.normalError(),nil)
            return
        default:
            mic.status = 0
        }
        mic.member = user
        VoiceRoomIMManager.shared?.setChatroomAttributes( attributes: ["mic_\(mic.mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.userList?.first(where: {
                    $0.chat_uid ?? "" == VoiceRoomUserInfo.shared.user?.uid ?? ""
                })?.mic_index = mic.mic_index
                self.applicants.removeAll {
                    $0.member?.chat_uid ?? "" == user?.chat_uid ?? ""
                }
                let currentMic = self.mics[safe: mic.mic_index]
                if currentMic?.status ?? 0 == -1 || currentMic?.status ?? 0 == 2 {
                    self.mics[mic.mic_index]  = mic
                    completion(nil,mic)
                } else {
                    completion(self.normalError(),nil)
                    return
                }
            } else {
                completion(self.convertError(error: error),nil)
            }
            
        })
    }
    
    /// Description 提交上麦申请
    /// - Parameters:
    ///   - chat_user: 提交的用户模型，包含申请的麦位信息，若没有顺序分配
    ///   - completion: 回调
    func startMicSeatApply(index: Int?,completion: @escaping (Error?, Bool) -> Void) {
        if !self.checkWhetherApplyMic() {
            completion(nil,false)
            return
        }
        let apply = VoiceRoomApply()
        apply.created_at = UInt64(Date().timeIntervalSince1970)
        apply.member = VoiceRoomUserInfo.shared.user
        if let idx = index {
            apply.index = idx
        } else {
            apply.index = self.findMicIndex()
        }
        apply.member?.mic_index = apply.index//专门用于安卓那边数据模型复用
        VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: VoiceRoomUserInfo.shared.currentRoomOwner?.rtc_uid ?? "", event: VoiceRoomSubmitApplySite, customExt: ["user" : apply.kj.JSONString(),"chatroomId":VoiceRoomIMManager.shared?.currentRoomId ?? ""], completion: { message, error in
            completion(self.convertError(error: error),error == nil)
        })
    }
    
    private func checkWhetherApplyMic() -> Bool {
        var result = false
        var mics = [VRRoomMic]()
        mics = self.mics.filter { $0.member?.chat_uid != VoiceRoomUserInfo.shared.currentRoomOwner?.chat_uid
        }
        for mic in mics {
            if mic.status == -1 {
                result = true
            }
        }
        return result
    }
    
    func cancelMicSeatApply(chat_uid: String, completion: @escaping (Error?, Bool) -> Void) {
            VoiceRoomIMManager.shared?.sendChatCustomMessage(to_uid: chat_uid, event: VoiceRoomCancelApplySite, customExt: ["chatroomId":VoiceRoomIMManager.shared?.currentRoomId ?? ""], completion: { message, error in
                completion(self.convertError(error: error),error == nil)
            })
        }
    
    func acceptMicSeatApply(chatUid: String, completion: @escaping (Error?,VRRoomMic?) -> Void) {
        var mic_index = 1
        let user = self.applicants.first(where: {
            $0.member?.chat_uid ?? "" == chatUid
        })
        if user?.index ?? 0 < 1 {
            mic_index = self.findMicIndex()
        } else {
            mic_index = user?.index ?? 1
        }
        let mic = VRRoomMic()
        mic.mic_index = mic_index
        switch self.mics[mic_index].status {
        case 2:
            mic.status = self.mics[mic_index].status
        case 3,4:
            completion(self.normalError(),nil)
            return
        default:
            mic.status = 0
        }
        mic.member = user?.member
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: ["mic_\(mic_index)":mic.kj.JSONString()], completion: { error in
            if error == nil {
                self.applicants.removeAll {
                    $0.member?.chat_uid ?? "" == user?.member?.chat_uid ?? ""
                }
                self.userList?.first(where: { $0.chat_uid ?? "" == user?.member?.chat_uid ?? ""
                                })?.mic_index = mic_index
                let currentMic = self.mics[safe: mic_index]
                if currentMic?.status ?? 0 == -1 || currentMic?.status ?? 0 == 2 {
                    self.mics[mic_index]  = mic
                    completion(nil,mic)
                } else {
                    completion(self.normalError(),nil)
                    return
                }
            } else {
                completion(self.convertError(error: error),nil)
            }
        })
    }
    
    func findMicIndex() -> Int {
        var mic_index = 1
        for i in 0...7 {
            if let mic = self.mics[safe: i] ,mic.member == nil,mic.status != 3,mic.status != 4 {
                mic_index = mic.mic_index
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
        SyncUtil.reset()
        SyncUtil.initSyncManager(sceneId: cSceneId) {
//            guard let self = self else {
//                return
//            }
//            self.syncUtilsInited = true
//
//            completion()
        }
        
        SyncUtil.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            agoraPrint("subscribeConnectState: \(state) \(self.syncUtilsInited)")
//            self.networkDidChanged?(KTVServiceNetworkStatus(rawValue: UInt(state.rawValue)))
            guard !self.syncUtilsInited else {
                //TODO: retry get data if restore connection
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
                agoraPrint("result == \(results.compactMap { $0.toJson() })")
                
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
    
    func limitError() -> VoiceRoomError {
        let error = VoiceRoomError()
        error.code = "403"
        error.message = "Members reach limit!".localized()
        return error
    }

    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity, completion: @escaping (SyncError?, VRRoomEntity?) -> Void) {
        
        let owner: VRUser = VRUser()
        owner.rtc_uid = VLUserCenter.user.id
        owner.name = VLUserCenter.user.name
        owner.uid = VLUserCenter.user.id
        owner.mic_index = 0
        owner.portrait = VLUserCenter.user.headUrl
        
        self.roomList?.append(room)
        let params = room.kj.JSONObject()
        self.initScene {
            SyncUtil.joinScene(id: room.room_id ?? "",
                               userId:VLUserCenter.user.id,
                               isOwner: true,
                               property: params) {[weak self] result in
                let model = model(from: result.toJson()?.z.jsonToDictionary() ?? [:], VRRoomEntity.self)
                self?.roomId = room.room_id
                self?._startCheckExpire()
                //添加鉴黄接口
                NetworkManager.shared.voiceIdentify(channelName: room.channel_id ?? "", channelType: room.sound_effect == 3 ? 0 : 1, sceneType: .voice) { msg in
                    agoraPrint("\(msg == nil ? "开启鉴黄成功" : "开启鉴黄失败")")
                }
                
                var tokenMap1:[Int: String] = [:], tokenMap2:[Int: String] = [:]
                let dispatchGroup = DispatchGroup()
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: room.room_id ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    tokenMap1 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.enter()
                NetworkManager.shared.generateTokens(channelName: "\(room.room_id ?? "")_ex",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc]) { tokenMap in
                    tokenMap2 = tokenMap
                    dispatchGroup.leave()
                }
                
                dispatchGroup.notify(queue: .main){
                    guard let rtcToken = tokenMap1[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap1[NetworkManager.AgoraTokenType.rtm.rawValue],
                          let rtcPlayerToken = tokenMap2[NetworkManager.AgoraTokenType.rtc.rawValue]
                    else {
                        completion(nil, nil)
                        return
                    }
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                    completion(nil, model)
                }
                
            } fail: { error in
                completion(error, nil)
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
                    if room.member_count ?? 0 >= 19 {
                        completion(self.limitError(),nil)
                        return
                    }
                    updateRoom.member_count = (updateRoom.member_count ?? 0) + 1
                    updateRoom.click_count = (updateRoom.click_count ?? 0) + 1
                    let params = updateRoom.kj.JSONObject()
                    
                    completion(nil, updateRoom)
                    initScene{
                        SyncUtil.joinScene(id: roomId,
                                           userId: VLUserCenter.user.id,
                                           isOwner: VLUserCenter.user.id == room.owner?.uid,
                                           property: params) {[weak self] result in

                            var tokenMap1:[Int: String] = [:], tokenMap2:[Int: String] = [:]
                            let dispatchGroup = DispatchGroup()
                            dispatchGroup.enter()
                            NetworkManager.shared.generateTokens(channelName: room.room_id ?? "",
                                                                 uid: "\(UserInfo.userId)",
                                                                 tokenGeneratorType: .token006,
                                                                 tokenTypes: [.rtc, .rtm]) { tokenMap in
                                tokenMap1 = tokenMap
                                dispatchGroup.leave()
                            }
                            
                            dispatchGroup.enter()
                            NetworkManager.shared.generateTokens(channelName: "\(room.room_id ?? "")_ex",
                                                                 uid: "\(UserInfo.userId)",
                                                                 tokenGeneratorType: .token006,
                                                                 tokenTypes: [.rtc]) { tokenMap in
                                tokenMap2 = tokenMap
                                dispatchGroup.leave()
                            }
                            
                            dispatchGroup.notify(queue: .main){
                                guard let rtcToken = tokenMap1[NetworkManager.AgoraTokenType.rtc.rawValue],
                                      let rtmToken = tokenMap1[NetworkManager.AgoraTokenType.rtm.rawValue],
                                      let rtcPlayerToken = tokenMap2[NetworkManager.AgoraTokenType.rtc.rawValue]
                                else {
                                    completion(nil, nil)
                                    return
                                }
                                VLUserCenter.user.agoraRTCToken = rtcToken
                                VLUserCenter.user.agoraRTMToken = rtmToken
                                VLUserCenter.user.agoraPlayerRTCToken = rtcPlayerToken
                            }
                            
                            self?.roomId = roomId
                            self?._startCheckExpire()
//                            completion(nil, model)
                            //TODO(chenpan): without callback
                        } fail: { error in
                            completion(error, nil)
                        }
                    }
                    break
                }
            }
        }
        
        
    }
    
    // todo 去掉owner
    func leaveRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        self.roomId = nil
        /**
         先拿到对应的房间信息
         1.如果是房主需要销毁房间，普通成员需要click_count - 1. 同时需要退出RTC+IM
         2.房主需要调用destory
         */
        if let roomList = self.roomList {
            for (index,room) in roomList.enumerated() {
                if room.room_id == roomId {
                    var isOwner = false
                    if let owner_uid = room.owner?.uid {
                        isOwner = owner_uid == VLUserCenter.user.id
                    }
                    if isOwner {
                        self.roomList?.remove(at: index)
                        VoiceRoomIMManager.shared?.userDestroyedChatroom()
                        SyncUtil.scene(id: roomId)?.deleteScenes()
                    } else {
                        let updateRoom: VRRoomEntity = room
                        updateRoom.member_count = (updateRoom.member_count ?? 0) - 1
                        let params = updateRoom.kj.JSONObject()
                        SyncUtil
                            .scene(id: roomId)?
                            .update(key: "",
                                    data: params,
                                    success: { obj in
                                agoraPrint("updateUserCount success")
                            },
                                    fail: { error in
                                agoraPrint("updateUserCount fail")
                            })
                        VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
                    }
                    break
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
        mic.member?.uid = VLUserCenter.user.id
        mic.member?.name = VLUserCenter.user.name
        mic.member?.chat_uid = VLUserCenter.user.id
        mic.member?.mic_index = 0
        mic.member?.name = VLUserCenter.user.name
        mic.member?.portrait = VLUserCenter.user.headUrl
        mic.member?.rtc_uid = VLUserCenter.user.id
        mic.member?.channel_id = ""
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
    
    func initIM(with roomName: String, type: Int,chatId: String?, channelId: String, imUid: String?, pwd: String, completion: @escaping (String, String, String) -> Void) {

        var im_token = ""
        var im_uid = ""
        var chatroom_id = ""

        let impGroup = DispatchGroup()
        let imQueue = DispatchQueue(label: "com.agora.imp.www")
        let tokenQueue = DispatchQueue(label: "token")

        impGroup.enter()
        imQueue.async {
            NetworkManager.shared.generateIMConfig(type: type,channelName: roomName, nickName: VLUserCenter.user.name, chatId: chatId, imUid: imUid, password: pwd, uid:  VLUserCenter.user.id, sceneType: .voice) { uid, room_id, token in
                im_uid = uid ?? ""
                chatroom_id = room_id ?? ""
                im_token = token ?? ""
                impGroup.leave()
            }
            
        }
        
        impGroup.enter()
        tokenQueue.async {
            NetworkManager.shared.generateToken(channelName: channelId, uid: VLUserCenter.user.id, tokenType: .token007, type: .rtc) { token in
                VLUserCenter.user.agoraRTCToken = token ?? ""
                impGroup.leave()
            }
        }
        
        impGroup.notify(queue: .main) {
            completion(im_token, im_uid, chatroom_id )
        }
    }
}


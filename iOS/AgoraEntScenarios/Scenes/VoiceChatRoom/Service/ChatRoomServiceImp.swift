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
import AgoraCommon
import RTMSyncManager
import AgoraRtmKit

private let kSceneId = "scene_chatRoom_6.0.0"

@objc public class VoiceChatLog: NSObject {
    
    static let kLogKey = "VoiceChat"
    
    @objc public static func info(_ text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).info(text, context: "Service")
    }
    @objc public static func err(_ text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).error(text, context: "Service")
    }
}

public class ChatRoomServiceImp: NSObject {
    
    static var _sharedInstance: ChatRoomServiceImp?
    var roomId: String?
    var roomList: [VRRoomEntity]?
    var userList: [VRUser]?
    public var mics: [VRRoomMic] = [VRRoomMic]()
    public var applicants: [VoiceRoomApply] = [VoiceRoomApply]()
    @objc public weak var roomServiceDelegate:ChatRoomServiceSubscribeDelegate?
    
    private let user: AUIUserThumbnailInfo
    
    private lazy var roomManager = AUIRoomManagerImpl(sceneId: kSceneId)
    
    private lazy var roomService: ChatRoomService = {
        let config = AUICommonConfig()
        config.appId = AppContext.shared.appId
        config.owner = user
        config.host = AppContext.shared.roomManagerUrl
        AUIRoomContext.shared.commonConfig = config
        let service = ChatRoomService(expirationPolicy: RoomExpirationPolicy.defaultPolicy(),
                                      roomManager: roomManager)
        return service
    }()
    
    private var bgm: VoiceChatBGM? = nil
    
    private var auiUserList = [AUIUserInfo]()
    
    public override init() {
        let owner = AUIUserThumbnailInfo()
        owner.userId = VLUserCenter.user.id
        owner.userName = VLUserCenter.user.name
        owner.userAvatar = VLUserCenter.user.headUrl
        self.user = owner
        AUIRoomContext.shared.displayLogClosure = { msg in
            ShowLogger.info(msg, context: "RTMSyncManager")
        }
        super.init()
        
        AppContext.shared.agoraRTCToken = ""
        AppContext.shared.agoraRTMToken = ""
    }
    
    func cleanCache() {
        self.userList = nil
        self.roomId = nil
        self.mics.removeAll()
        self.applicants.removeAll()
    }
    
    
    fileprivate func _checkRoomExpire() {
        guard let room = self.roomList?.filter({$0.room_id == roomId}).first, let created_at = room.created_at else { return }
        
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = (AppContext.shared.sceneConfig?.chat ?? 20 * 60) * 1000
//        VoiceChatLog.info("checkRoomExpire: \(currentTs - Int64(created_at)) / \(expiredDuration)")
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
    
    func destroy() {
        
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
        if map.keys.contains(where: { $0.hasPrefix("click_count") }) {
            guard let click_count = map["click_count"] else { return }
            self.roomServiceDelegate?.onClickCountChanged(roomId: roomId, count: Int(click_count) ?? 3)
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
        let keys = ["ranking_list","member_list","gift_amount", "click_count", "mic_0","mic_1","mic_2","mic_3","mic_4","mic_5","mic_6","mic_7","robot_volume","use_robot"]
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
                VoiceChatLog.info("ranking_list: \(ranking_list)")
                roomInfo.room?.ranking_list = ranking_list.kj.modelArray(VRUser.self)
            } else {
                roomInfo.room?.ranking_list = [VRUser]()
            }
            if let member_list = map?["member_list"]?.toArray() {
                print("[ChatRoom] fetchRoomDetail - 获取房间成员列表成功: \(member_list)")
                roomInfo.room?.member_list = member_list.kj.modelArray(VRUser.self)
            } else {
                print("[ChatRoom] fetchRoomDetail - 房间成员列表为空")
                roomInfo.room?.member_list = [VRUser]()
            }
            if let gift_amount = map?["gift_amount"] as? String {
                roomInfo.room?.gift_amount = Int(gift_amount)
            }
            if let click_count = map?["click_count"] as? String {
                roomInfo.room?.click_count = Int(click_count)
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
                    print("[ChatRoom] fetchRoomMembers: \(member_list)")
                    completion(self.convertError(error: error),member_list.kj.modelArray(VRUser.self))
                }
            })
        }
    }
    
    func updateRoomMembers(completion: @escaping (Error?) -> Void) {
        let attributes = ["member_list":self.userList?.kj.JSONString() ?? ""]
        print("[ChatRoom] updateRoomMembers: \(attributes)")
        VoiceRoomIMManager.shared?.setChatroomAttributes(attributes: attributes, completion: { error in
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
        if let `index` = index {
            mic.mic_index = index
        } else if let `index` = self.findMicIndex() {
            mic.mic_index = index
        } else {
            completion(NSError(domain: "No Enabled Mic Seat Found", code: -1), nil)
            return
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
        var mic_index: Int? = nil
        let user = self.applicants.first(where: {
            $0.member?.chat_uid ?? "" == chatUid
        })
        if user?.index ?? 0 < 1 {
            mic_index = self.findMicIndex()
        } else {
            mic_index = user?.index ?? 1
        }
        guard let `mic_index` = mic_index else {
            completion(NSError(domain: "No Enabled Mic Seat Found", code: -1), nil)
            return
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
                    self.mics[mic_index] = mic
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
    
    func findMicIndex() -> Int? {
        var mic_index: Int? = nil
        for i in 0...5 {
            if let mic = self.mics[safe: i] ,mic.member == nil,mic.status != 3,mic.status != 4 {
                mic_index = mic.mic_index
                break
            }
        }
        return mic_index
    }

// MARK: Room Management
    
    private func preGenerateToken(completion:@escaping (NSError?)->()) {
        AppContext.shared.agoraRTCToken = ""
        AppContext.shared.agoraRTMToken = ""
        let date = Date()
        NetworkManager.shared.generateToken(channelName: "",
                                            uid: "\(UserInfo.userId)",
                                            tokenTypes: [.rtc, .rtm]) { token in
            guard let rtcToken = token, let rtmToken = token else {
                completion(NSError(domain: "generate token fail", code: -1))
                return
            }
            AppContext.shared.agoraRTCToken = rtcToken
            AppContext.shared.agoraRTMToken = rtmToken
            VoiceChatLog.info("[Token] sync manager login token rtm:\(rtmToken) rtc: \(rtcToken)")
            VoiceChatLog.info("[Timing]preGenerateToken rtc & rtm cost: \(Int64(-date.timeIntervalSinceNow * 1000)) ms")
            completion(nil)
        }
    }
    
    
    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func fetchRoomList(page: Int,
                       completion: @escaping (Error?, [VRRoomEntity]?) -> Void) {
        let currentUserId = user.userId
        roomService.getRoomList(lastCreateTime: 0,
                                pageSize: 50) { info in
            return info.owner?.userId == currentUserId
        } completion: { err, ts, list in
            if let err = err {
                completion(err, nil)
            } else {
                let roomList = list?.map({ $0.voice_toRoomEntity()}) ?? []
                self.roomList = roomList.sorted(by: {$0.created_at ?? 0 > $1.created_at ?? 0})
                completion(nil, self.roomList)
            }
        }
    }
    
    func limitError() -> VoiceRoomError {
        let error = VoiceRoomError()
        error.code = "403"
        error.message = "voice_members_reach_limit".voice_localized()
        return error
    }
    
    private func networkError() -> VoiceRoomError {
        let error = VoiceRoomError()
        error.code = "-1"
        error.message = "voice_network_disconnected".voice_localized()
        return error
    }

    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity, completion: @escaping (Error?, VRRoomEntity?) -> Void) {
        guard room.room_id != nil else {
            completion(NSError(domain: "error", code: -1), nil)
            return
        }
        let roomInfo = AUIRoomInfo.voice_fromVRRoomEntity(room)
        roomService.createRoom(room: roomInfo) { [weak self] err, info in
            guard let `self` = self, err == nil else {
                completion(err, nil)
                return
            }
            self.roomList?.append(room)
            self.roomId = room.room_id
            self._startCheckExpire()
            //添加鉴黄接口
            NetworkManager.shared.voiceIdentify(channelName: room.channel_id ?? "", channelType: room.sound_effect == 3 ? 0 : 1, sceneType: "voice_chat") { msg in
                VoiceChatLog.info("\(msg == nil ? "开启鉴黄成功" : "开启鉴黄失败")")
            }
            
            VoiceChatLog.info("[Token] create room token room:\(roomInfo.roomId)")
            let roomEntity = info?.voice_toRoomEntity()
            completion(nil, roomEntity)
        }
    }
    
    func joinRoom(_ roomId: String, completion: @escaping (Error?, VRRoomEntity?) -> Void) {
        guard let roomEntity = self.roomList?.first(where: {$0.room_id == roomId}) else {
            completion(nil, nil)
            return
        }
        let roomInfo = AUIRoomInfo.voice_fromVRRoomEntity(roomEntity)
        VoiceChatLog.info("joinRoom roomId: \(roomInfo.roomId) roomName: \(roomInfo.roomName)")
        roomService.enterRoom(roomInfo: roomInfo) {[weak self] err in
            guard let `self` = self else { return }
            if err != nil {
                completion(err, nil)
                return
            }
            
            self.roomId = roomId
            self._startCheckExpire()
            completion(nil, roomEntity)
        }
    }
    
    func leaveRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        if roomService.isRoomOwner(roomId: roomId) {
            VoiceRoomIMManager.shared?.userDestroyedChatroom()
            roomService.leaveRoom(roomId: roomId)
            SyncUtil.scene(id: roomId)?.deleteScenes()
        } else {
            // 更新房间列表人数信息
            VoiceRoomIMManager.shared?.userQuitRoom(completion: nil)
            roomService.leaveRoom(roomId: roomId)
        }
        completion(nil, true)
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
    
    func initIM(with roomName: String,
                type: Int,
                chatId: String?,
                channelId: String, 
                imUid: String?,
                pwd: String,
                completion: @escaping (String, String, String) -> Void) {
        NetworkManager.shared.generateIMConfig(type: type,channelName: roomName,
                                               nickName: VLUserCenter.user.name,
                                               chatId: chatId,
                                               imUid: imUid,
                                               password: pwd,
                                               uid:  VLUserCenter.user.id) { uid, room_id, token in
            completion(token ?? "", uid ?? "", room_id ?? "" )
        }
    }
}

extension ChatRoomServiceImp: AUISceneRespDelegate {
    /// 房间过期的回调
    /// - Parameter channelName: <#channelName description#>
    public func onSceneExpire(channelName: String) {
        
    }
    
    /// 房间被销毁的回调
    /// - Parameter channelName: 房间id
    public func onSceneDestroy(channelName: String) {
        roomService.leaveRoom(roomId: channelName)
        self.roomServiceDelegate?.onRoomExpired()
    }
    
    /// Description 房间异常，需要退出
    ///
    /// - Parameters:
    ///   - channelName: 房间id
    ///   - reason: 异常原因
    public func onSceneFailed(channelName: String, reason: String) {
        //login when occur error
        VoiceChatLog.info("onSceneFailed: \(channelName) reason: \(reason)")
    }
}

extension ChatRoomServiceImp: AUIArbiterDelegate {
    public func onArbiterDidChange(channelName: String, arbiterId: String) {
        
    }
    
    public func onError(channelName: String, error: NSError) {
        
    }
}

extension ChatRoomServiceImp: AUIUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.auiUserList = userList
        let count = self.auiUserList.count + 2
        self.roomServiceDelegate?.onMemberCountChanged(roomId: roomId, count: count)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        guard roomId == self.roomId else {
            return
        }
        self.auiUserList.removeAll(where: {$0.userId == userInfo.userId})
        self.auiUserList.append(userInfo)
        if let roomEntity = self.roomList?.first(where: {$0.room_id == roomId}) {
            let count = self.auiUserList.count + 2
            let roomInfo = AUIRoomInfo.voice_fromVRRoomEntity(roomEntity)
            roomInfo.customPayload["member_count"] = count
            roomManager.updateRoom(room: roomInfo) { error, info in
            }
            self.roomServiceDelegate?.onMemberCountChanged(roomId: roomId, count: count)
        }
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        guard roomId == self.roomId else {
            return
        }
        self.auiUserList.removeAll(where: {$0.userId == userInfo.userId})
        if let roomEntity = self.roomList?.first(where: {$0.room_id == roomId}) {
            let count = self.auiUserList.count + 2
            let roomInfo = AUIRoomInfo.voice_fromVRRoomEntity(roomEntity)
            roomInfo.customPayload["member_count"] = count
            roomManager.updateRoom(room: roomInfo) { error, info in
            }
            self.roomServiceDelegate?.onMemberCountChanged(roomId: roomId, count: count)
        }
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
}

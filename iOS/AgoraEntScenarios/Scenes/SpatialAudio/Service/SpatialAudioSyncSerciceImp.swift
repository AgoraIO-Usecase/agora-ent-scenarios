//
//  SpatialAudioSyncSerciceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/2/7.
//

import Foundation
import KakaJSON
import AgoraSyncManager
import ZSwiftBaseLib
import AgoraCommon

@objc public class SpatialVoiceChatLog: NSObject {
    
    static let kLogKey = "SpatialAudio"
    
    public static func info(_ text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).info(text)
    }

    public static func warn(_ text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).warning(text)
    }

    public static func error(_ text: String) {
        AgoraEntLog.getSceneLogger(with: kLogKey).error(text)
    }
}

enum SAErrorType {
    case unknown(String, String)
    case userNotFound(String)
    case roomInfoNotFound(String, String)
    case notImplemented(String)
    
    func error() ->Error {
        let saError = SAError()
        saError.code = "\(-1)"
        switch self {
        case .unknown(let context, let msg):
            saError.message = "[\(context)] error occurred: \(msg)"
        case .userNotFound(let context):
            saError.message = "[\(context)] user not found"
        case .roomInfoNotFound(let context, let roomId):
            saError.message = "[\(context)] room not found: \(roomId)"
        case .notImplemented(let context):
            saError.message = "[\(context)] not implemented!"
        }
        
        return saError
    }
}

private let cSceneId = "scene_spatialChatRoom_6.0.0"
private let kCollectionIdUser = "user_collection"
private let kCollectionIdSeatInfo = "seat_info_collection"
private let kCollectionIdSeatApply = "show_seat_apply_collection"
private let kCollectionIdRobotInfo = "robot_info_collection"
class SpatialAudioSyncSerciceImp: NSObject {
    private var _getMicSeatApplyListBlock: SuccessBlock?
    fileprivate weak var subscribeDelegate: SpatialAudioServiceSubscribeDelegate?
    fileprivate var roomId: String?
    fileprivate var roomList: [SARoomEntity] = [SARoomEntity]()
    fileprivate var syncUtilsInited: Bool = false
    public var mics: [SARoomMic] = [SARoomMic]() 
    public var userList: [SAUser] = [SAUser]()
    public var micApplys: [SAApply] = [SAApply]()
    private var robotInfo: SARobotAudioInfo?
}

//MARK: private
extension SpatialAudioSyncSerciceImp {
    
    fileprivate func initScene(completion: @escaping () -> Void) {
        if syncUtilsInited {
            completion()
            return
        }
        SyncUtil.reset()
        SyncUtil.initSyncManager(sceneId: cSceneId) {
        }
        
        SyncUtil.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            SpatialVoiceChatLog.info("subscribeConnectState: \(state) \(self.syncUtilsInited)")
//            self.networkDidChanged?(KTVServiceNetworkStatus(rawValue: UInt(state.rawValue)))
            guard !self.syncUtilsInited else {
                //TODO: retry get data if restore connection
                return
            }
            
            self.syncUtilsInited = true
            completion()
        }
    }

    func findMicIndex() -> Int {
        var mic_index = 0
        for i in 1...8 {
            if i == 8 { return 0 }
            let mic = self.mics[safe: i]
            if mic?.member == nil && mic?.status != 3 && mic?.status != 4 {
                mic_index = mic?.mic_index ?? 1
                break
            }
        }
        return mic_index
    }
    
    fileprivate func _getOwnerSeat() -> SARoomMic {
        let mic = SARoomMic()
        mic.mic_index = 0
        mic.status = 0
        mic.member = SAUser()
        mic.member?.uid = VLUserCenter.user.id
        mic.member?.name = VLUserCenter.user.name
        mic.member?.chat_uid = VLUserCenter.user.id
        mic.member?.mic_index = 0
        mic.member?.name = VLUserCenter.user.name
        mic.member?.portrait = VLUserCenter.user.headUrl
        mic.member?.rtc_uid = VLUserCenter.user.id
        mic.member?.channel_id = ""
        
        return mic
    }
    
    fileprivate func createMics(roomId: String, completion: @escaping (Error?, [SARoomMic]?)->Void) {
        _getMicSeatList(roomId: roomId) {[weak self] error, micList in
            guard let self = self else { return }
            if let err = error {
                completion(err, nil)
                return
            }
            
            var mics = micList ?? []
            
            var pendingCount = 7
            for i in 0...6 {
                print("[mic] + 开始创建麦位: \(i)")
                let item = i == 0 ? self._getOwnerSeat() : SARoomMic()
                if i != 0 {
                    item.mic_index = i
                    if i == 3 || i == 6 {
                        item.status = -2   //robot
                    } else {
                        item.status = -1   //normal
                    }
                }
                self._addMicSeat(roomId: roomId, mic: item) { error, mic in
                    DispatchQueue.main.async {
                        guard pendingCount > 0 else { return }
                        print("[mic] - 麦位创建完成: \(mic?.mic_index ?? -1) pending: \(pendingCount) mics: \(mics.count)")
                        item.objectId = mic?.objectId
                        mics.append(mic ?? item)
                        pendingCount -= 1
                        if pendingCount == 0 {
                            
                            if mics.count == 7 {
                                mics = mics.sorted {$0.mic_index < $1.mic_index}
                                self.mics = mics
                                print("[mic] - 全部麦位创建完成")
                                completion(nil, mics)
                            } else {
                                print("[mic] - 麦位创建失败")
                                completion(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to create all mic seats"]), nil)
                            }
                        }
                    }
                }
            }
        }
    }
    
    fileprivate func _subscribeAll() {
        _subscribeRoomStatusChanged()
        _subscribeMicSeatApplyChanged()
        _subscribeUsersChanged()
        _subscribeMicSeatInfoChanged()
        _subscribeRobotChanged()
    }
}


//MARK: SpatialAudioServiceProtocol
extension SpatialAudioSyncSerciceImp: SpatialAudioServiceProtocol {
    func subscribeEvent(with delegate: SpatialAudioServiceSubscribeDelegate) {
        self.subscribeDelegate = delegate
    }
    
    func unsubscribeEvent() {
        SpatialVoiceChatLog.info("imp all unsubscribe ...")
//        self.subscribeDelegate = nil
        SyncUtil
            .scene(id: self.roomId ?? "")?
            .unsubscribeScene()
    }
    
    func fetchRoomList(page: Int, completion: @escaping (Error?, [SARoomEntity]?) -> Void) {
        initScene { [weak self] in
            SyncUtil.fetchAll { [weak self] results in
                SpatialVoiceChatLog.info("result == \(results.compactMap { $0.toJson() })")
                
                let dataArray = results.map({ info in
                    return model(from: info.toJson()?.z.jsonToDictionary() ?? [:], SARoomEntity.self)
                })
                self?.roomList = dataArray.sorted(by: {$0.created_at ?? 0 > $1.created_at ?? 0})
                completion(nil, self?.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func createRoom(room: SARoomEntity, completion: @escaping (Error?, SARoomEntity?) -> Void) {
        self.roomList.append(room)
        let params = room.kj.JSONObject()
        let room_id = room.room_id ?? ""
        self.initScene {
            SyncUtil.joinScene(id: room_id,
                               userId:VLUserCenter.user.id,
                               isOwner: true,
                               property: params) {[weak self] result in
                guard let self = self else {return}
                let model = model(from: result.toJson()?.z.jsonToDictionary() ?? [:], SARoomEntity.self)
                self.roomId = model.room_id
                self.roomList.append(model)
                self._startCheckExpire()
                self._subscribeAll()
                self.updateRobotInfo(info: SARobotAudioInfo()) { error in }
                let userId = VLUserCenter.user.id
                NetworkManager.shared.generateToken(channelName: room_id,
                                                    uid: userId,
                                                    tokenTypes: [.rtc]) { token in
                    VLUserCenter.user.agoraRTCToken = token ?? ""
                    VLUserCenter.user.chat_uid = userId
                    self._addUserIfNeed(roomId: room_id) { err in
                        self.createMics(roomId: room_id) { error, micList in
                            completion(error, model)
                        }
                    }
                }
                //添加鉴黄接口
                NetworkManager.shared.voiceIdentify(channelName: room.channel_id ?? "", channelType: room.sound_effect == 3 ? 0 : 1, sceneType: "voice_chat") { msg in
                    print("\(msg == nil ? "开启鉴黄成功" : "开启鉴黄失败")")
                }
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func joinRoom(_ roomId: String, completion: @escaping (Error?, SARoomEntity?, SARobotAudioInfo?) -> Void) {
        guard let room = _roomInfo(roomId: roomId) else {
            SpatialVoiceChatLog.error("join room fail, \(roomId) not found")
            completion(SAErrorType.roomInfoNotFound("join room", roomId).error(), nil, robotInfo)
            return
        }
        
        let updateRoom: SARoomEntity = room
        updateRoom.member_count = (updateRoom.member_count ?? 0) + 1
        updateRoom.click_count = (updateRoom.click_count ?? 0) + 1
        let params = updateRoom.kj.JSONObject()
        let userId = VLUserCenter.user.id
        let room_id = room.room_id ?? ""
        SyncUtil.joinScene(id: room_id,
                           userId: userId,
                           isOwner: userId == VLUserCenter.user.id,
                           property: params) {[weak self] result in
            guard let self = self else {return}
            self.roomId = room_id
            
            self._getRobotInfo { error, info in
                self.robotInfo = info
            }
            self._startCheckExpire()
            self._subscribeAll()
            NetworkManager.shared.generateToken(channelName: room_id,
                                                uid: userId,
                                                tokenTypes: [.rtc]) { token in
                VLUserCenter.user.agoraRTCToken = token ?? ""
                VLUserCenter.user.chat_uid = userId
                self._addUserIfNeed(roomId: room_id) { err in
                    self._getMicSeatList(roomId: room_id) { error, mics in
                        self.mics = mics ?? []
                        completion(error, updateRoom, self.robotInfo)
                    }
                }
            }
        } fail: { error in
            completion(error, nil, self.robotInfo)
        }
        
    }
    
    func leaveRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void) {
        guard let room = self.roomList.filter({$0.room_id == roomId}).first else {
            SpatialVoiceChatLog.error("join room fail, \(roomId) not found")
            completion(SAErrorType.roomInfoNotFound("leave room", roomId).error(), false)
            return
        }
        
        var isOwner = false
        if let owner_uid = room.owner?.uid {
            isOwner = owner_uid == VLUserCenter.user.id
        }
        if isOwner {
//            SAIMManager.shared?.userDestroyedChatroom()
            SpatialVoiceChatLog.info("imp leaveRoom deleteScenes: \(roomId)")
            SyncUtil.scene(id: roomId)?.deleteScenes()
        } else {
//            let updateRoom: SARoomEntity = room
//            updateRoom.member_count = (updateRoom.member_count ?? 0) - 1
//            let params = updateRoom.kj.JSONObject()
//            SyncUtil
//                .scene(id: roomId)?
//                .update(key: "",
//                        data: params,
//                        success: { obj in
//                    SpatialVoiceChatLog.info("imp updateUserCount success")
//
//                },
//                        fail: { error in
//                    SpatialVoiceChatLog.info("imp updateUserCount fail")
//                })
//            SAIMManager.shared?.userQuitRoom(completion: nil)
            
            cancelMicSeatApply(chat_uid: VLUserCenter.user.id) { _, _ in }
            _removeUser(roomId: self.roomId!) { error in }
            SpatialVoiceChatLog.info("imp leaveRoom leaveScene \(roomId)")
            
            SyncUtil.leaveScene(id: self.roomId!)
        }
    }
    
    func fetchRoomDetail(entity: SARoomEntity, completion: @escaping (Error?, SARoomInfo?) -> Void) {
        let roomInfo = SARoomInfo()
        roomInfo.room = entity
        roomInfo.robotInfo = robotInfo ?? SARobotAudioInfo()
        _getMicSeatList(roomId: entity.room_id ?? "") { error, mics in
            if let error = error {
                SpatialVoiceChatLog.error(error.localizedDescription)
                completion(error, nil)
                return
            }
            roomInfo.mic_info = mics
            completion(nil, roomInfo)
        }
    }
   
    func fetchGiftContribute(completion: @escaping (Error?, [SAUser]?) -> Void) {
        completion(SAErrorType.notImplemented("fetchGiftContribute").error(), nil)
    }
    
    func fetchRoomMembers(completion: @escaping (Error?, [SAUser]?) -> Void) {
        _getUserList(roomId: self.roomId!) { error, users in
            completion(error, users?.filter({$0.uid != VLUserCenter.user.id}))
        }
    }
    
    func updateRoomMembers(completion: @escaping (Error?) -> Void) {
        completion(SAErrorType.notImplemented("updateRoomMembers").error())
    }
    
    func fetchApplicantsList(completion: @escaping (Error?, [SAApply]?) -> Void) {
        _getMicSeatApplyList(roomId: self.roomId!, completion: completion)
    }
    
    func forbidMic(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 3 {
            mic.status = 4
        } else {
            mic.status = 2
        }
        
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(error,mic)
        }
    }
    
    func unForbidMic(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
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
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(error,mic)
        }
    }
    
    func lockMic(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
        if mic.status == 2 {
            mic.status = 4
        } else {
            mic.status = 3
        }
        if let member = mic.member {
            member.status = .idle
            member.invited = false
            _updateUserInfo(roomId: roomId!, user: member) { _ in }
        }
        
        self._cleanUserMicIndex(mic: mic)
        mic.member = nil
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(error,mic)
        }
    }
    
    func unLockMic(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
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
        
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.status = mic.status
            }
            completion(error,mic)
        }
    }
    
    func kickOff(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        let mic = SARoomMic()
        let oldMic = self.mics[mic_index]
        mic.mic_index = mic_index
        mic.status = (oldMic.status == 2 ? 2 : -1)
        mic.objectId = oldMic.objectId
        self._cleanUserMicIndex(mic: oldMic)
        
        if let member = oldMic.member {
            member.status = .idle
            member.invited = false
            _updateUserInfo(roomId: roomId!, user: member) { _ in }
        }
        
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[mic_index] = mic
            }
            completion(error,mic)
        }
    }
    
    func leaveMic(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard self.mics.count > mic_index else { return }
        let mic = SARoomMic()
        let oldMic = self.mics[mic_index]
        mic.mic_index = mic_index
        mic.status = oldMic.status == 2 ? 2 : -1
        mic.objectId = oldMic.objectId
        self._cleanUserMicIndex(mic: self.mics[mic_index])
        
        if let member = oldMic.member {
            member.status = .idle
            member.invited = false
            _updateUserInfo(roomId: roomId!, user: member) { _ in }
        }
        
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil && self.mics.count > mic_index {
                self.mics[mic_index] = mic
            }
            completion(error, mic)
        }
    }
    
    func muteLocal(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
//        mic.status = 1
        mic.member?.mic_status = .mute
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.member?.mic_status = .mute
            }
            completion(error, mic)
        }
    }
    
    func unmuteLocal(mic_index: Int, completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard let mic = self.mics[safe: mic_index] else {
            return
        }
//        mic.status = 0
        mic.member?.mic_status = .unMute
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error == nil {
                self.mics[safe: mic_index]?.member?.mic_status = .unMute
            }
            completion(error, mic)
        }
    }
    
    func changeMic(old_index: Int, new_index: Int, completion: @escaping (Error?, [Int : SARoomMic]?) -> Void) {
        if self.mics[safe: new_index]?.member != nil {
            completion(SAErrorType.unknown("changeMic", "idx[\(new_index)] member = nil").error(), nil)
            return
        }
        let old_mic = SARoomMic()
        old_mic.objectId = self.mics[old_index].objectId
        switch self.mics[old_index].status {
        case 2:
            old_mic.status = self.mics[old_index].status
        case 3,4:
            completion(SAErrorType.unknown("changeMic", "old_mic idx[\(new_index)] status = 3/4").error(), nil)
            return
        default:
            old_mic.status = -1
        }
        old_mic.mic_index = old_index
        
        let new_mic = SARoomMic()
        new_mic.objectId = mics[new_index].objectId
        switch mics[new_index].status {
        case 2:
            new_mic.status = 2
        case 3,4:
            completion(SAErrorType.unknown("changeMic", "new_mic idx[\(new_index)] status = 3/4").error(), nil)
            return
        default:
            new_mic.status = 0
        }
        new_mic.mic_index = new_index
        new_mic.member = mics[old_index].member
        
        var ret = [Int: SARoomMic]()
        var retErr: Error? = nil
        _updateMicSeat(roomId: self.roomId!, mic: old_mic) { error in
            if error == nil {
                ret[old_index] = old_mic
            } else {
                retErr = error
            }
            completion(error, [old_index: old_mic])
        }
        
        _updateMicSeat(roomId: self.roomId!, mic: new_mic) { error in
            if error == nil {
                ret[new_index] = old_mic
            } else {
                retErr = error
            }
        }
        ret.forEach { (key: Int, value: SARoomMic) in
            self.mics[key] = value
        }
        completion(retErr, ret)
    }
    
    func startMicSeatInvitation(chatUid: String, index: Int?, completion: @escaping (Error?, Bool) -> Void) {
        guard let user = self.userList.first(where: { $0.uid == chatUid }) else {
            SpatialVoiceChatLog.error("startMicSeatInvitation not found")
            completion(SAErrorType.unknown("startMicSeatInvitation", "user not found").error(), false)
            return
        }
        user.mic_index = index
        user.status = .waitting
        _updateUserInfo(roomId: self.roomId!, user: user) { error in
            completion(error, error == nil)
        }
    }
    
    func acceptMicSeatInvitation(completion: @escaping (Error?, SARoomMic?) -> Void) {
        guard let user = self.userList.first(where: { $0.uid == VLUserCenter.user.id }) else {
            completion(SAErrorType.unknown("acceptMicSeatInvitation", "user not found").error(), nil)
            return
        }
        
        user.status = .accepted
        let mic = SARoomMic()
        if user.mic_index ?? 0 > 1 {
            mic.mic_index = user.mic_index ?? 1
        } else {
            mic.mic_index = self.findMicIndex()
        }
        user.mic_index = mic.mic_index
        switch self.mics[mic.mic_index].status {
        case 2:
            mic.status = self.mics[mic.mic_index].status
        case 3,4:
            completion(SAErrorType.unknown("acceptMicSeatInvitation", "mic idx[\(mic.mic_index)] status = 3/4").error(), nil)
            return
        default:
            mic.status = 0
        }
        mic.member = user
        let idx = max(user.mic_index ?? 0, 0)
        mic.objectId = self.mics[idx].objectId
        
        let impGroup = DispatchGroup()
        impGroup.enter()
        var retErr: Error? = nil
        _updateUserInfo(roomId: self.roomId!, user: user) { error in
            if error != nil {
                retErr = error
            }
            impGroup.leave()
        }
        
        impGroup.enter()
        _updateMicSeat(roomId: self.roomId!, mic: mic) { error in
            if error != nil {
                retErr = error
            }
            impGroup.leave()
        }
        
        impGroup.notify(queue: DispatchQueue.main) {
            if let error = retErr {
                completion(error, nil)
                return
            }
            
            self.userList.first(where: {
                $0.uid ?? "" == SAUserInfo.shared.user?.uid ?? ""
            })?.mic_index = mic.mic_index
            self.micApplys.removeAll {
                $0.member?.uid ?? "" == user.chat_uid ?? ""
            }
            let currentMic = self.mics[safe: mic.mic_index]
            if currentMic?.status ?? 0 == -1 || currentMic?.status ?? 0 == 2 {
                self.mics[mic.mic_index]  = mic
                completion(nil,mic)
            } else {
                completion(SAErrorType.unknown("acceptMicSeatInvitation", "currentMic.status != -1/2").error(), nil)
                return
            }
        }
    }
    
    func refuseInvite(chat_uid: String, completion: @escaping (Error?, Bool) -> Void) {
        guard let user = userList.first(where: { $0.uid == chat_uid }) else {
            SpatialVoiceChatLog.error("startMicSeatInvitation not found")
            completion(SAErrorType.unknown("startMicSeatInvitation", "user not found").error(), false)
            return
        }
        user.mic_index = -1
        user.status = .rejected
        user.invited = false
        _updateUserInfo(roomId: self.roomId!, user: user) { error in
            completion(error, error == nil)
        }
    }
    
    func startMicSeatApply(index: Int?, completion: @escaping (Error?, Bool) -> Void) {
        if let apply = self.micApplys.filter({$0.member?.uid == VLUserCenter.user.id}).first {
            apply.created_at = UInt64(Date().timeIntervalSince1970)
            apply.index = index ?? self.findMicIndex()
            _updateMicSeatApply(roomId: self.roomId!, apply: apply) { error in
                completion(error, error == nil)
            }
            return
        }
        
        let apply = SAApply()
        apply.created_at = UInt64(Date().timeIntervalSince1970)
        apply.index = index ?? self.findMicIndex()
        guard let user = self.userList.filter({$0.uid == VLUserCenter.user.id}).first else {
            completion(SAErrorType.userNotFound("startMicSeatApply").error(), false)
            return
        }
        user.status = .waitting
        apply.member = user
        _addMicSeatApply(roomId: self.roomId!, apply: apply) { error in
            completion(error, error == nil)
        }
    }
    
    func cancelMicSeatApply(chat_uid: String, completion: @escaping (Error?, Bool) -> Void) {
        guard let apply = micApplys.first(where: { $0.member?.uid == chat_uid }) else {
            SpatialVoiceChatLog.error("cancelMicSeatApply not found")
            completion(nil, true)
            return
        }
        
        _removeMicSeatApply(roomId: self.roomId!, apply: apply) { error in
            completion(error, error == nil)
        }
    }
    
    func acceptMicSeatApply(chatUid: String, completion: @escaping (Error?, SARoomMic?) -> Void) {
        var mic_index = 1
        guard let apply = self.micApplys.first(where: { $0.member?.uid ?? "" == chatUid }) else {
            completion(SAErrorType.unknown("acceptMicSeatApply", "apply not found").error(), nil)
            return
        }
        if apply.index ?? 0 < 0 {
            mic_index = self.findMicIndex()
        } else {
            mic_index = apply.index ?? 1
        }
        
        if mics[mic_index].status != -1 {
            mic_index = self.findMicIndex()
        }
        
        let mic = SARoomMic()
        mic.mic_index = mic_index
        switch self.mics[mic_index].status {
        case 2:
            mic.status = self.mics[mic_index].status
        case 3,4:
            completion(SAErrorType.unknown("acceptMicSeatApply", "mic idx[\(mic.mic_index)] status = 3/4").error(), nil)
            return
        default:
            mic.status = 0
        }
        apply.member?.mic_index = mic_index
        mic.member = apply.member
        mic.objectId = mics[mic_index].objectId
        _updateMicSeat(roomId: self.roomId!, mic: mic) { [self] error in
            if let error = error {
                completion(error, nil)
                return
            }
            
//            self.micApplys.removeAll {
//                $0.member?.chat_uid ?? "" == apply.member?.chat_uid ?? ""
//            }
            guard let user = self.userList.first(where: { $0.uid ?? "" == apply.member?.uid ?? "" }) else { return }
            user.mic_index = mic_index
            user.status = .accepted
            mic.member?.objectId = user.objectId
            _updateUserInfo(roomId: self.roomId!, user: user) { _ in
                
            }
            let currentMic = self.mics[safe: mic_index]
            if currentMic?.status ?? 0 == -1 || currentMic?.status ?? 0 == 2 {
                self.mics[mic_index] = mic
                completion(nil,mic)
            } else {
                completion(SAErrorType.unknown("acceptMicSeatApply", "currentMic.status != -1/2").error(), nil)
                return
            }
        }
        
        _removeMicSeatApply(roomId: self.roomId!, apply: apply) { error in

        }
    }
    
    func updateAnnouncement(content: String, completion: @escaping (Bool) -> Void) {
        guard let room = _roomInfo() else {
            SpatialVoiceChatLog.error("update announcement fail, \(roomId ?? "") not found")
            completion(false)
            return
        }
        room.announcement = content
        _updateRoom(with: room) { error in
            completion(error == nil)
        }
    }
    
    func enableRobot(enable: Bool, completion: @escaping (Error?) -> Void) {
//        guard let room = _roomInfo() else {
//            SpatialVoiceChatLog.error("update robot enable fail, \(roomId ?? "") not found")
//            completion(SAErrorType.roomInfoNotFound("enableRobot", roomId ?? "").error())
//            return
//        }
//        room.use_robot = enable
//        _updateRoom(with: room, completion: completion)
    }
    
    func updateRobotVolume(value: Int, completion: @escaping (Error?) -> Void) {
//        guard let room = _roomInfo() else {
//            SpatialVoiceChatLog.error("update robot volume fail, \(roomId ?? "") not found")
//            completion(SAErrorType.roomInfoNotFound("updateRobotVolume", roomId ?? "").error())
//            return
//        }
//        room.robot_volume = UInt(value)
//        _updateRoom(with: room, completion: completion)
    }
    
    func updateRobotInfo(info: SARobotAudioInfo, completion: @escaping ((Error?)->())) {
        _updateRobot(info: info, completion: completion)
    }
}

//MARK: timer
extension SpatialAudioSyncSerciceImp {
    fileprivate func _checkRoomExpire() {
        guard let room = self.roomList.filter({$0.room_id == roomId}).first, let created_at = room.created_at else { return }
        
        let currentTs = Int64(Date().timeIntervalSince1970 * 1000)
        let expiredDuration = 20 * 60 * 1000
//        SpatialVoiceChatLog.info("checkRoomExpire: \(currentTs - Int64(created_at)) / \(expiredDuration)")
        guard currentTs - Int64(created_at) > expiredDuration else { return }
        
        self.subscribeDelegate?.onRoomExpired()
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

//MARK: room info
extension SpatialAudioSyncSerciceImp {
    fileprivate func _roomInfo(roomId: String? = nil) -> SARoomEntity? {
        let room_id = roomId ?? self.roomId
        return self.roomList.filter {$0.room_id == room_id}.first
    }
    
    func _subscribeRoomStatusChanged() {
        guard let channelName = roomId else {
            SpatialVoiceChatLog.error("channelName = nil")
            return
        }
        SpatialVoiceChatLog.info("imp room subscribe...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: "",
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           guard let self = self, let jsonStr = object.toJson() else { return }
                           
                           let modelDic = jsonStr.z.jsonToDictionary()
                           if modelDic.isEmpty {
                               SpatialVoiceChatLog.info("imp room subscribe jsonStr fail = \(jsonStr)")
                           }
                           
                           let room = model(from: modelDic, SARoomEntity.self)
                           guard room.room_id == channelName else {return}
                           let origRoom = self.roomList.filter({ $0.room_id == room.room_id }).first
                           SpatialVoiceChatLog.info("imp room subscribe onUpdated...")
                           if origRoom?.announcement != room.announcement {
                               origRoom?.announcement = room.announcement
                               self.subscribeDelegate?.onRoomAnnouncementChanged(announce: room.announcement ?? "")
                           } /*else if origRoom?.use_robot != room.use_robot {
                               origRoom?.use_robot = room.use_robot
                               self.subscribeDelegate?.onRobotSwitch(roomId: channelName, enable: room.use_robot ?? false, from: room.owner?.name ?? "")
                           } else if origRoom?.robot_volume != room.robot_volume {
                               origRoom?.robot_volume = room.robot_volume
                               self.subscribeDelegate?.onRobotVolumeChanged(roomId: channelName, volume: room.robot_volume ?? 0, from: room.owner?.name ?? "")
                           }*/
                       }, onDeleted: { [weak self] object in
                           guard let model = self?.roomList.filter({ $0.room_id == object.getId() }).first,
                                 model.room_id == channelName,
                                 model.owner?.uid != VLUserCenter.user.id else {
                               return
                           }
                           SpatialVoiceChatLog.info("imp room subscribe onDeleted...")
                           self?.subscribeDelegate?.onUserBeKicked(roomId: channelName, reason: .destroyed)
//                           self?.roomStatusDidChanged?(KTVSubscribeDeleted.rawValue, model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }
    
    fileprivate func _updateRoom(with room: SARoomEntity, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            SpatialVoiceChatLog.info("_updateRoom channelName = nil")
//            userListCountDidChanged?(UInt(count))
            return
        }
        SpatialVoiceChatLog.info("imp _updateRoom")
        let params = room.kj.JSONObject()
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                SpatialVoiceChatLog.info("imp _updateRoom success")
                completion(nil)
            },fail: { error in
                SpatialVoiceChatLog.info("imp _updateRoom fail")
                completion(error)
            })
    }
}

//MARK: seat info
extension SpatialAudioSyncSerciceImp {
    fileprivate func _cleanUserMicIndex(mic: SARoomMic) {
        let user = self.userList.first(where: {
            $0.uid ?? "" == mic.member?.uid ?? ""
        })
        user?.mic_index = -1
        // TODO: 不应该拿mic.member去更新
        mic.member?.mic_index = -1
    }
    
    fileprivate func _subscribeMicSeatInfoChanged() {
        SpatialVoiceChatLog.info("imp seat info subscribe ...")
        guard let roomId = self.roomId else { return }
        SyncUtil
            .scene(id: roomId)?
            .subscribe(key: kCollectionIdSeatInfo,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           SpatialVoiceChatLog.info("imp seat info subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson() else { return }
                           let seat = model(from: jsonStr.z.jsonToDictionary(), SARoomMic.self)
                           defer {
                               self.subscribeDelegate?.onSeatUpdated(roomId: roomId, mics: [seat], from: "")
                           }
                           if let index = self.mics.firstIndex(where: { $0.objectId == seat.objectId }) {
                               self.mics[index] = seat
                           }
                       }, onDeleted: { _ in
                           SpatialVoiceChatLog.info("imp seat info subscribe onDeleted...")

                       }, onSubscribed: {
                       }, fail: { error in
                           SpatialVoiceChatLog.info("imp seat info subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    fileprivate func _getMicSeatList(roomId: String, completion: @escaping (Error?, [SARoomMic]?) -> Void) {
        SpatialVoiceChatLog.info("imp mic seat list get...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatInfo)
            .get(success: { [weak self] list in
                SpatialVoiceChatLog.info("imp mics seat list get success...")
                let mics = list.map({ $0.toJson() }).kj.modelArray(SARoomMic.self).sorted(by: {
                    $0.mic_index < $1.mic_index
                })
                self?.mics = mics
                completion(nil, mics)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp mics seat list get fail :\(error.message)...")
                completion(SAErrorType.unknown("get mic list", error.message).error(), nil)
            })
    }
    
    fileprivate func _addMicSeat(roomId: String, mic: SARoomMic, completion: @escaping (Error?, SARoomMic?) -> Void) {
        let params = mic.kj.JSONObject()
        SpatialVoiceChatLog.info("imp seat add...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatInfo)
            .add(data: params, success: { object in
                SpatialVoiceChatLog.info("imp seat add success...")
                let seat = model(from: (object.toJson() ?? "").z.jsonToDictionary(), SARoomMic.self)
                completion(nil, seat)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp seat add fail :\(error.message)...")
                completion(SAErrorType.unknown("add seat", error.message).error(), nil)
            })
    }
    
    fileprivate func _updateMicSeat(roomId: String, mic: SARoomMic, completion: @escaping (Error?) -> Void) {
        SpatialVoiceChatLog.info("imp mic seat update... ")
        guard let selfRoomId = self.roomId else {
            completion(SAErrorType.unknown("update mic seat", "room id is nil").error())
            return
        }
        var params = mic.kj.JSONObject()
        SyncUtil
            .scene(id: selfRoomId)?
            .collection(className: kCollectionIdSeatInfo)
            .update(id: mic.objectId ?? "",
                    data:params,
                    success: {
                SpatialVoiceChatLog.info("imp mic seat update success... ")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp mic seat update fail :\(error.message)... ")
                completion(SAErrorType.unknown("update mic seat", error.message).error())
            })
    }
}

//MARK: seat application
extension SpatialAudioSyncSerciceImp {
    fileprivate func _subscribeMicSeatApplyChanged() {
        SpatialVoiceChatLog.info("imp seat apply subscribe ...")
        guard let myRoomId = self.roomId else {
            return
        }
        SyncUtil
            .scene(id: myRoomId)?
            .subscribe(key: kCollectionIdSeatApply,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           SpatialVoiceChatLog.info("imp seat apply subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let roomId = self.roomId else { return }
                           let apply = model(from: jsonStr.z.jsonToDictionary(), SAApply.self)
                           defer {
                               self.subscribeDelegate?.onReceiveSeatRequest(roomId: roomId, applicant: apply)
                           }
                           self.micApplys.removeAll { $0.objectId == apply.objectId}
                           self.micApplys.append(apply)
                       }, onDeleted: {[weak self] object in
                           SpatialVoiceChatLog.info("imp seat apply subscribe onDeleted...")
                           guard let self = self,
                                 let jsonStr = object.toJson(), let roomId = self.roomId else {return}
                           let applicant = model(from: jsonStr.z.jsonToDictionary(), SAApply.self)
                           let apply: SAApply? = self.micApplys.first { $0.objectId == applicant.objectId
                           }
                           self.micApplys.removeAll { $0.objectId == applicant.objectId
                           }
                           guard let apply = apply else {return}
                           if VLUserCenter.user.id != apply.member?.uid {
                               self.subscribeDelegate?.onReceiveSeatRequestRejected(roomId: roomId, chat_uid: apply.member?.uid ?? "")
                           }
                       }, onSubscribed: {
                       }, fail: { error in
                           SpatialVoiceChatLog.info("imp seat apply subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    fileprivate func _getMicSeatApplyList(roomId: String, completion: @escaping (Error?, [SAApply]?) -> Void) {
        SpatialVoiceChatLog.info("imp seat apply list get...")
        self._getMicSeatApplyListBlock = { [weak self] list in
            SpatialVoiceChatLog.info("imp seat apply list get success...")
            let applys = list.map({$0.toJson()}).kj.modelArray(SAApply.self)
            guard let self = self else {return}
            self.micApplys.removeAll()
            applys.forEach { apply in
                if !self.micApplys.contains(where: {$0.member?.uid == apply.member?.uid}) && !self.mics.contains(where: {$0.member?.uid == apply.member?.uid}) {
                    self.micApplys.append(apply)
                }
            }
            completion(nil, applys)
        }
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatApply)
            .get(success: { [weak self] list in
                self?._getMicSeatApplyListBlock?(list)
                self?._getMicSeatApplyListBlock = nil
            }, fail: { error in
                SpatialVoiceChatLog.info("imp seat apply list get fail :\(error.message)...")
                completion(SAErrorType.unknown("get seat apply list", error.message).error(), nil)
            })
    }
    
    fileprivate func _addMicSeatApply(roomId: String, apply: SAApply, completion: @escaping (Error?) -> Void) {
        let params = apply.kj.JSONObject()
        SpatialVoiceChatLog.info("imp seat apply add...")
        SpatialVoiceChatLog.info(apply.kj.JSONString())
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatApply)
            .add(data: params, success: { object in
                SpatialVoiceChatLog.info("imp seat apply add success...")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp seat apply add fail :\(error.message)...")
                completion(SAErrorType.unknown("add seat apply", error.message).error())
            })
    }
    
    func _removeMicSeatApply(roomId: String, apply: SAApply, completion: @escaping (Error?) -> Void) {
        SpatialVoiceChatLog.info("imp seat apply remove...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatApply)
            .delete(id: apply.objectId!, success: { object in
                SpatialVoiceChatLog.info("imp seat apply remove success...")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp seat apply remove fail :\(error.message)...")
                completion(SAErrorType.unknown("remove seat apply", error.message).error())
            })
    }
    
    fileprivate func _updateMicSeatApply(roomId: String, apply: SAApply, completion: @escaping (Error?) -> Void) {
        let params = apply.kj.JSONObject()
        SpatialVoiceChatLog.info("imp seat apply update...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdSeatApply)
            .update(id: roomId,
                    data: params,
                    success: {
                SpatialVoiceChatLog.info("imp seat apply update success ...")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp seat apply update fail :\(error.message)...")
                completion(SAErrorType.unknown("update seat apply", error.message).error())
            })
    }
}

//MARK: user
extension SpatialAudioSyncSerciceImp {
    fileprivate func _addUserIfNeed(roomId: String, finished: @escaping (Error?) -> Void) {
        _getUserList(roomId: roomId) {[weak self] error, userList in
            guard let self = self else {
                return
            }
            // current user already add
            if self.userList.contains(where: { $0.uid == VLUserCenter.user.id }) {
                finished(nil)
                return
            }
            self._addUserInfo(roomId: roomId) { [weak self] user in
                if let user = user {
                    self?.userList.append(user)
                }
                finished(nil)
            }
        }
    }

    fileprivate func _getUserList(roomId: String, finished: @escaping (Error?, [SAUser]?) -> Void) {
        SpatialVoiceChatLog.info("imp user get...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdUser)
            .get(success: { [weak self] list in
                SpatialVoiceChatLog.info("imp user get success...")
                let users = list.map({ $0.toJson()}).kj.modelArray(SAUser.self)
                self?.userList = users
                self?._updateUserCount(completion: { error in
                })
                finished(nil, users)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp user get fail :\(error.message)...")
                finished(SAErrorType.unknown(roomId, error.message).error(), nil)
            })
    }

    fileprivate func _addUserInfo(roomId: String, finished: @escaping (SAUser?) -> Void) {
        let room = self.roomList.filter {$0.room_id == self.roomId}.first
        let owner: SAUser = SAUser()
        owner.rtc_uid = VLUserCenter.user.id
        owner.chat_uid = owner.rtc_uid
        owner.name = VLUserCenter.user.name
        owner.uid = VLUserCenter.user.id
        owner.mic_index = room?.owner?.uid == owner.uid ? 1 :  -1
        owner.portrait = VLUserCenter.user.headUrl

        let params = owner.kj.JSONObject()
        SpatialVoiceChatLog.info("imp user add ...")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdUser)
            .add(data: params, success: { object in
                owner.objectId = object.getId()
                SpatialVoiceChatLog.info("imp user add success...")
                finished(owner)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp user add fail :\(error.message)...")
                finished(nil)
            })
    }
    
    fileprivate func _updateUserInfo(roomId: String, user: SAUser, completion: @escaping (Error?) -> Void) {
        SpatialVoiceChatLog.info("imp user update...")
        if user.objectId == nil {
            _getUserList(roomId: roomId) { [weak self] _, users in
                guard let self = self else { return }
                user.objectId = users?.first(where: { $0.uid == user.uid })?.objectId
                self._updateUserInfoHandler(roomId: roomId, user: user, completion: completion)
            }
            return
        }
        _updateUserInfoHandler(roomId: roomId, user: user, completion: completion)
    }
    
    private func _updateUserInfoHandler(roomId: String, user: SAUser, completion: @escaping (Error?) -> Void) {
        guard user.objectId != nil else { return }
        let params = user.kj.JSONObject()
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdUser)
            .update(id: user.objectId!,
                    data:params,
                    success: {
                SpatialVoiceChatLog.info("imp user update success...")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp user update fail :\(error.message)...")
                completion(SAErrorType.unknown("update userInfo", error.message).error())
            })
    }
    
    fileprivate func _subscribeUsersChanged() {
        SpatialVoiceChatLog.info("imp user subscribe ...")
        guard let roomId = self.roomId else { return  }
        SyncUtil
            .scene(id: roomId)?
            .subscribe(key: kCollectionIdUser,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           guard let self = self,
                                 let jsonStr = object.toJson(), let roomId = self.roomId else { return }
                           SpatialVoiceChatLog.info("imp user subscribe onUpdated: \(jsonStr)")
                           let user = model(from: jsonStr.z.jsonToDictionary(), SAUser.self)
                           if VLUserCenter.user.id == user.uid {
                               if user.status == .waitting {
                                   if let index = self.userList.firstIndex(where: { $0.uid == user.uid }) {
                                       self.userList[index] = user
                                   }
                                   self.subscribeDelegate?.onReceiveSeatInvitation(roomId: roomId, user: user)
                               } else if user.status == .rejected {
                                   self.subscribeDelegate?.onReceiveCancelSeatInvitation(roomId: roomId, chat_uid: user.uid ?? "")
                               }
                               return
                           }
                           if let index = self.mics.firstIndex(where: { $0.member?.uid == user.uid }) {
                               self.mics[index].member = user
                           }
                           if !self.userList.contains(where: { $0.uid == user.uid }) {
                               self.userList.append(user)
                               self._updateUserCount { error in
                               }
                               self.subscribeDelegate?.onUserJoinedRoom(roomId: roomId, user: user)
                           }
                       }, onDeleted: { [weak self] object in
                           SpatialVoiceChatLog.info("imp user subscribe onDeleted... [\(object.getId())]")
                           guard let self = self, let roomId = self.roomId else { return }
                           var user: SAUser? = nil
                           if let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) {
                               user = self.userList[index]
                               self.userList.remove(at: index)
                               self._updateUserCount { error in
                               }
                               self.subscribeDelegate?.onReceiveCancelSeatInvitation(roomId: roomId, chat_uid: user?.uid ?? "")
                           }
                           guard let user = user else { return }
                           if user.uid == VLUserCenter.user.id {
                               self.subscribeDelegate?.onUserBeKicked(roomId: roomId, reason: SAServiceKickedReason.removed)
                           } else {
                               self.subscribeDelegate?.onUserLeftRoom(roomId: roomId, userName: user.uid ?? "")
                           }
//                           self.subscribeDelegate?.onUserCountChanged(userCount: self.userList.count)
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                       }, fail: { error in
                           SpatialVoiceChatLog.info("imp user subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }

    fileprivate func _removeUser(roomId: String, completion: @escaping (Error?) -> Void) {
        guard let objectId = userList.filter({ $0.uid == VLUserCenter.user.id }).first?.objectId else {
            SpatialVoiceChatLog.info("_removeUser objectId = nil")
            return
        }
        SpatialVoiceChatLog.info("imp user delete... [\(objectId)]")
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdUser)
            .delete(id: objectId,
                    success: { _ in
                SpatialVoiceChatLog.info("imp user delete success...")
            }, fail: { error in
                SpatialVoiceChatLog.info("imp user delete fail \(error.message)...")
                completion(SAErrorType.unknown("remove user", error.message).error())
            })
    }

    fileprivate func _updateUserCount(completion: @escaping (NSError?) -> Void) {
//        _updateUserCount(with: userList.count)
        guard let channelName = roomId,
              let roomInfo = roomList.filter({ $0.room_id == channelName }).first else {
            return
        }
        
        roomInfo.member_count = self.roomList.count + 2
        _updateRoom(with: roomInfo) { error in
            
        }
    }

    fileprivate func _updateUserCount(with count: Int) {
//        guard let channelName = roomId,
//              let roomInfo = roomList?.filter({ $0.roomId == self.getRoomId() }).first,
//              roomInfo.ownerId == VLUserCenter.user.id
//        else {
////            SpatialVoiceChatLog.info("updateUserCount channelName = nil")
////            userListCountDidChanged?(UInt(count))
//            return
//        }
//        let roomUserCount = count
//        if roomUserCount == roomInfo.roomUserCount {
//            return
//        }
//        roomInfo.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
//        roomInfo.roomUserCount = roomUserCount
//        roomInfo.objectId = channelName
//        let params = roomInfo.yy_modelToJSONObject() as! [String: Any]
//        SpatialVoiceChatLog.info("imp room update user count... [\(channelName)]")
//        SyncUtil
//            .scene(id: channelName)?
//            .update(key: "",
//                    data: params,
//                    success: { obj in
//                SpatialVoiceChatLog.info("imp room update user count success...")
//            }, fail: { error in
//                SpatialVoiceChatLog.info("imp room update user count fail \(error.message)...")
//            })

//        userListCountDidChanged?(UInt(count))
    }
}


extension SpatialAudioSyncSerciceImp {
    
    fileprivate func _subscribeRobotChanged() {
        SpatialVoiceChatLog.info("imp robot subscribe ...")
        guard let roomId = self.roomId else { return }
        SyncUtil
            .scene(id: roomId)?
            .subscribe(key: kCollectionIdRobotInfo,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           SpatialVoiceChatLog.info("imp robot subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson() else { return }
                           let robotInfo = model(from: jsonStr.z.jsonToDictionary(), SARobotAudioInfo.self)
                           self.robotInfo = robotInfo
                           self.subscribeDelegate?.onRobotUpdate(robotInfo: robotInfo)
                       }, onDeleted: { object in
                           SpatialVoiceChatLog.info("imp robot subscribe onDeleted... [\(object.getId())]")
                           SpatialVoiceChatLog.error("not implemeted")
//                           self.subscribeDelegate?.onUserCountChanged(userCount: self.userList.count)
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                       }, fail: { error in
                           SpatialVoiceChatLog.info("imp user subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    private func _getRobotInfo(completion: @escaping (Error?, SARobotAudioInfo?)->()) {
        SpatialVoiceChatLog.info("imp robot get...")
        guard let roomId = self.roomId else { return }
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdRobotInfo)
            .get(success: { [weak self] list in
                guard let self = self else {return}
                SpatialVoiceChatLog.info("imp robot get success...")
                let robotList = list.map({$0.toJson()}).kj.modelArray(SARobotAudioInfo.self)
                self.robotInfo = robotList.first
                completion(nil, self.robotInfo)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp robot get fail :\(error.message)...")
                completion(SAErrorType.unknown(roomId, error.message).error(), nil)
            })
    }
    
    private func _updateRobot(info: SARobotAudioInfo, completion: @escaping (Error?)->()) {
        SpatialVoiceChatLog.info("imp robot info update...")
        guard let roomId = self.roomId else { return }
        let params = info.kj.JSONObject()
        SyncUtil
            .scene(id: roomId)?
            .collection(className: kCollectionIdRobotInfo)
            .update(id: info.objectId,
                    data:params,
                    success: {
                SpatialVoiceChatLog.info("imp robot info success...")
                completion(nil)
            }, fail: { error in
                SpatialVoiceChatLog.info("imp robot info fail :\(error.message)...")
                completion(SAErrorType.unknown("update robot info", error.message).error())
            })
    }
}

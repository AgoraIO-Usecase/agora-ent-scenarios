//
//  ShowSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/3.
//

import Foundation

private let kSceneId = "scene_show"

private let SYNC_MANAGER_MESSAGE_COLLECTION = "show_message_collection"
private let SYNC_MANAGER_SEAT_APPLY_COLLECTION = "show_seat_apply_collection"
private let SYNC_MANAGER_SEAT_INVITATION_COLLECTION = "show_seat_invitation_collection"
private let SYNC_MANAGER_PK_INVITATION_COLLECTION = "show_pk_invitation_collection"
private let SYNC_MANAGER_INTERACTION_COLLECTION = "show_interaction_collection"

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    #if DEBUG
    assert(condition, message)
    #else
    #endif
}

private func agoraPrint(_ message: String) {
    #if DEBUG
    print(message)
    #else
    #endif
}

class ShowSyncManagerServiceImp: NSObject, ShowServiceProtocol {
    private var roomList: [ShowRoomListModel]?
    private var userList: [ShowUser] = [ShowUser]()
    private var messageList: [ShowMessage] = [ShowMessage]()
    private var seatApplyList: [ShowMicSeatApply] = [ShowMicSeatApply]()
    private var seatInvitationList: [ShowMicSeatInvitation] = [ShowMicSeatInvitation]()
    private var pkInvitationList: [ShowPKInvitation] = [ShowPKInvitation]()
    private var interactionList: [ShowInteractionInfo] = [ShowInteractionInfo]()
    
    private weak var subscribeDelegate: ShowSubscribeServiceProtocol?
    
    private var pkCreatedInvitation: ShowPKInvitation?
    
    private var syncUtilsInited: Bool = false
    private var roomId: String? {
        didSet {
            if oldValue == roomId {
                return
            }
            guard let _ = roomId else {
                return
            }

            syncUtilsInited = false
        }
    }
    
    // MARK: Private
    private func getRoomId() -> String {
        guard let _roomId = roomId else {
            agoraAssert("roomId == nil")
            return ""
        }

        return _roomId
    }
    
    private func initScene(completion: @escaping () -> Void) {
        if syncUtilsInited {
            completion()
            return
        }

        SyncUtil.initSyncManager(sceneId: kSceneId) { [weak self] in
            guard let self = self else {
                return
            }
            self.syncUtilsInited = true

            completion()
        }
    }
    
    //MARK: ShowServiceProtocol
    
    func getRoomList(page: Int, completion: @escaping (Error?, [ShowRoomListModel]?) -> Void) {
        _getRoomList(page: page) { [weak self] error, list in
            guard let self = self else { return }
            self.roomList = list
            completion(nil, list)
        }
    }
    
    func createRoom(roomName: String,
                    roomId: String,
                    thumbnailId: String,
                    completion: @escaping (Error?, ShowRoomDetailModel?) -> Void) {
        let room = ShowRoomListModel()
        room.roomName = roomName
        room.roomId = roomId
        room.thumbnailId = thumbnailId
        room.ownerId = VLUserCenter.user.id
        room.ownerName = VLUserCenter.user.name
        room.ownerAvater = VLUserCenter.user.headUrl
        room.createdAt = Date().millionsecondSince1970()
        let params = room.yy_modelToJSONObject() as? [String: Any]

        initScene { [weak self] in
            SyncUtil.joinScene(id: room.roomId!,
                               userId: room.ownerId!,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomId", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomId = channelName
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token007,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    guard let self = self,
                          let rtcToken = tokenMap[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap[NetworkManager.AgoraTokenType.rtm.rawValue]
                    else {
                        agoraAssert(tokenMap.count == 2, "rtcToken == nil || rtmToken == nil")
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    let output = ShowRoomDetailModel.yy_model(with: params!)
                    completion(nil, output)
                    self._subscribeAll()
                    self._addUserIfNeed()
                    self.roomList?.append(room)
                }
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func joinRoom(room: ShowRoomListModel,
                  completion: @escaping (Error?, ShowRoomDetailModel?) -> Void) {
        let params = room.yy_modelToJSONObject() as? [String: Any]

        initScene { [weak self] in
            SyncUtil.joinScene(id: room.roomId!,
                               userId: room.ownerId!,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomId", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomId = channelName
                NetworkManager.shared.generateTokens(channelName: channelName ?? "",
                                                     uid: "\(UserInfo.userId)",
                                                     tokenGeneratorType: .token006,
                                                     tokenTypes: [.rtc, .rtm]) { tokenMap in
                    guard let self = self,
                          let rtcToken = tokenMap[NetworkManager.AgoraTokenType.rtc.rawValue],
                          let rtmToken = tokenMap[NetworkManager.AgoraTokenType.rtm.rawValue]
                    else {
                        agoraAssert(tokenMap.count == 2, "rtcToken == nil || rtmToken == nil")
                        return
                    }
                    VLUserCenter.user.ifMaster = VLUserCenter.user.id == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    let output = ShowRoomDetailModel.yy_model(with: params!)
                    completion(nil, output)
                    self._subscribeAll()
                    self._addUserIfNeed()
                }
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func leaveRoom(completion: @escaping (Error?) -> Void) {
        guard let roomInfo = roomList?.filter({ $0.roomId == self.getRoomId() }).first else {
            agoraAssert("leaveRoom channelName = nil")
            return
        }
        
        //current user is room owner, remove room
        if roomInfo.ownerId == VLUserCenter.user.id {
            _removeRoom(completion: completion)
            return
        }
        _leaveRoom(completion: completion)
    }
    
    
    func getAllUserList(completion: @escaping (Error?, [ShowUser]?) -> Void) {
        _getUserList(finished: completion)
    }
    
    func sendChatMessage(message: ShowMessage, completion: ((Error?) -> Void)?) {
//        agoraAssert("not implemented")
        _addMessage(message: message, finished: completion)
    }
    
    func getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void) {
        _getAllMicSeatApplyList(completion: completion)
    }
    
    
    func unsubscribeEvent(delegate: ShowSubscribeServiceProtocol) {
        //TODO: weak map
        self.subscribeDelegate = nil
    }
    
    func subscribeEvent(delegate: ShowSubscribeServiceProtocol) {
        //TODO: weak map
        self.subscribeDelegate = delegate
    }
    
    
    func createMicSeatApply(completion: @escaping (Error?) -> Void) {
        let apply = ShowMicSeatApply()
        apply.userId = VLUserCenter.user.id
        apply.userName = VLUserCenter.user.name
        apply.avatar = VLUserCenter.user.headUrl
        apply.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addMicSeatApply(apply: apply, completion: completion)
    }
    
    func cancelMicSeatApply(completion: @escaping (Error?) -> Void) {
        guard let apply = self.seatApplyList.filter({ $0.userId == VLUserCenter.user.id }).first else {
            agoraAssert("cancel apply not found")
            return
        }
        _removeMicSeatApply(apply: apply, completion: completion)
    }
    
    func acceptMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        apply.status = .accepted
        _updateMicSeatApply(apply: apply, completion: completion)
        
        let interaction = ShowInteractionInfo()
        interaction.userId = apply.userId
        interaction.userName = apply.userName
        interaction.roomId = getRoomId()
        interaction.interactStatus = .onSeat
        interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addInteraction(interaction: interaction) { error in
        }
    }
    
    func rejectMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        apply.status = .rejected
        _updateMicSeatApply(apply: apply, completion: completion)
    }
    
    
    func getAllMicSeatInvitationList(completion: @escaping (Error?, [ShowMicSeatInvitation]?) -> Void) {
        _getAllMicSeatInvitationList(completion: completion)
    }
    
    func createMicSeatInvitation(user: ShowUser, completion: @escaping (Error?) -> Void) {
        user.status = .waitting
        _addMicSeatInvitation(invitation: user, completion: completion)
    }
    
    func cancelMicSeatInvitation(userId: String, completion: @escaping (Error?) -> Void) {
        guard let invitation = self.seatInvitationList.filter({ $0.userId == userId }).first else {
            agoraAssert("cancel invitation not found")
            return
        }
        
        _removeMicSeatInvitation(invitation: invitation, completion: completion)
    }
    
    func acceptMicSeatInvitation(completion: @escaping (Error?) -> Void) {
//        guard let invitation = self.seatInvitationList.filter({ $0.userId == VLUserCenter.user.userNo }).first else {
//            agoraAssert("accept invitation not found")
//            return
//        }
//        invitation.status = .accepted
//        _updateMicSeatInvitation(invitation: invitation, completion: completion)
        _getUserList { [weak self] (error, userList) in
            guard let self = self else { return }
            guard let user = self.userList.filter({ $0.userId == VLUserCenter.user.id }).first else {
                agoraAssert("accept invitation not found")
                return
            }
            user.status = .accepted
            self._updateUserInfo(user: user, completion: completion)

            let interaction = ShowInteractionInfo()
            interaction.userId = user.userId
            interaction.userName = user.userName
            interaction.roomId = self.getRoomId()
            interaction.interactStatus = .onSeat
            interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
            self._addInteraction(interaction: interaction) { error in
            }
        }
        
        
    }
    
    func rejectMicSeatInvitation(completion: @escaping (Error?) -> Void) {
//        guard let invitation = self.seatInvitationList.filter({ $0.userId == VLUserCenter.user.userNo }).first else {
//            agoraAssert("reject invitation not found")
//            return
//        }
//        invitation.status = .rejected
//        _updateMicSeatInvitation(invitation: invitation, completion: completion)
        _getUserList { [weak self] (error, userList) in
            guard let self = self else { return }
            guard let user = self.userList.filter({ $0.userId == VLUserCenter.user.id }).first else {
                agoraAssert("reject invitation not found")
                return
            }
            user.status = .rejected
            self._updateUserInfo(user: user, completion: completion)
        }
    }
    
    
    func getAllPKUserList(completion: @escaping (Error?, [ShowPKUserInfo]?) -> Void) {
        _getRoomList(page: 0) { error, list in
            let filterList = list?.filter({ $0.ownerId != VLUserCenter.user.id })
            completion(error, filterList)
        }
    }
    
    func getAllPKInvitationList(completion: @escaping (Error?, [ShowPKInvitation]?) -> Void) {
        _getAllPKInvitationList(room: nil, completion: completion)
    }
    
    func getCurrentApplyUser(roomId: String?, completion: @escaping (ShowRoomListModel?) -> Void) {
        _getCurrentApplyUser(roomId: roomId, completion: completion)
    }
    
    func createPKInvitation(room: ShowRoomListModel,
                            completion: @escaping (Error?) -> Void) {
        _getAllPKInvitationList(room: room) {[weak self] error, invitationList in
            guard let self = self, error == nil, let invitationList = invitationList else { return }
            
            defer {
                self._unsubscribePKInvitationChanged(roomId: room.roomId)
                self._subscribePKInvitationChanged(channelName: room.roomId!) { status, invitation in
                    if status == .created {
                        guard self.pkCreatedInvitation?.fromRoomId == invitation.fromRoomId else { return }
                        self.pkCreatedInvitation = invitation
                    }
                    guard let model = self.pkCreatedInvitation,
                          invitation.objectId == model.objectId else {
                        return
                    }
                    
                    if status == .deleted {
                        self._recvPKRejected(invitation: model)
                    } else {
                        model.status = invitation.status
                        switch model.status {
                        case .rejected:
                            self._recvPKRejected(invitation: model)
                        case .accepted:
                            self._recvPKAccepted(invitation: model)
                        case .ended:
                            self._recvPKFinish(invitation: model)
                        default:
                            break
                        }
                    }
                }
            }
            
            guard let invitation = invitationList.filter({ $0.fromRoomId == self.roomId }).first else {
                //not found, add invitation
                let _invitation = ShowPKInvitation()
                _invitation.userId = room.ownerId
                _invitation.userName = room.ownerName
                _invitation.roomId = room.roomId
                _invitation.fromUserId = VLUserCenter.user.id
                _invitation.fromName = VLUserCenter.user.name
                _invitation.fromRoomId = self.roomId
                _invitation.status = .waitting
                _invitation.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
                self.pkCreatedInvitation = _invitation
                self._addPKInvitation(invitation: _invitation, completion: completion)
                return
            }
            
            self.pkCreatedInvitation = invitation
//            if invitation.status == .waitting {
//                agoraPrint("pk invitaion already send ")
//                return
//            }
            
            invitation.status = .waitting
            invitation.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
            //found, update invitation
            self._updatePKInvitation(invitation: invitation, completion: completion)
        }
    }
    
    func acceptPKInvitation(completion: @escaping (Error?) -> Void) {
        guard let invitation = self.pkInvitationList.filter({ $0.userId == VLUserCenter.user.id }).first else {
            agoraAssert("accept invitation not found")
            return
        }
        invitation.status = .accepted
        _updatePKInvitation(invitation: invitation, completion: completion)
        
        let interaction = ShowInteractionInfo()
        interaction.userId = invitation.fromUserId
        interaction.userName = invitation.fromName
        interaction.roomId = invitation.fromRoomId
        interaction.interactStatus = .pking
        interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addInteraction(interaction: interaction) { error in
        }
    }
    
    func rejectPKInvitation(completion: @escaping (Error?) -> Void) {
        guard let invitation = self.pkInvitationList.filter({ $0.userId == VLUserCenter.user.id }).first else {
            agoraAssert("accept invitation not found")
            return
        }
//        invitation.status = .rejected
//        _updatePKInvitation(invitation: invitation, completion: completion)
        _removePKInvitation(invitation: invitation, completion: completion)
    }
    
    func getAllInterationList(completion: @escaping (Error?, [ShowInteractionInfo]?) -> Void) {
        _getAllInteractionList(completion: completion)
    }
    
    func stopInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
        _removeInteraction(interaction: interaction, completion: completion)
        guard interaction.interactStatus == .pking else {
            return
        }
        
        // pk invitation sender
        if let invitation = self.pkCreatedInvitation {
//            invitation.status = .ended
//            _updatePKInvitation(invitation: invitation, completion: completion)
            _removePKInvitation(invitation: invitation, completion: completion)
            
            return
        }
        
        // pk invitation recviver
        guard let invitation = self.pkInvitationList.filter({$0.fromUserId == interaction.userId }).first else {
            agoraAssert("not found invitation: \(interaction.userId ?? "") \(interaction.roomId ?? "")")
            return
        }
//        invitation.status = .ended
//        _updatePKInvitation(invitation: invitation, completion: completion)
        _removePKInvitation(invitation: invitation, completion: completion)
    }
}


//MARK: room operation
extension ShowSyncManagerServiceImp {
    func _getRoomList(page: Int, completion: @escaping (Error?, [ShowRoomListModel]?) -> Void) {
        initScene { [weak self] in
            SyncUtil.fetchAll { results in
                agoraPrint("result == \(results.compactMap { $0.toJson() })")
                let dataArray = results.map({ info in
                    return ShowRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                let roomList = dataArray.sorted(by: { ($0.updatedAt > 0 ? $0.updatedAt : $0.createdAt) > ($1.updatedAt > 0 ? $1.updatedAt : $0.createdAt) })
                completion(nil, roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    
    private func _updateRoomInteractionStatus(status: ShowInteractionStatus) {
        guard let channelName = roomId,
              let roomInfo = roomList?.filter({ $0.roomId == roomId }).first,
              roomInfo.ownerId == VLUserCenter.user.id
        else {
//            assert(false, "channelName = nil")
            agoraPrint("_updateRoomRequestStatus channelName = nil")
            return
        }
        roomInfo.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        roomInfo.interactStatus = status
        var params = roomInfo.yy_modelToJSONObject() as! [String: Any]
        let objectId = channelName
        agoraPrint("imp room update status... [\(objectId)]")
        params["objectId"] = objectId
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                agoraPrint("imp room update status success...")
            }, fail: { error in
                agoraPrint("imp room update status fail \(error.message)...")
            })
    }
    
    private func _leaveRoom(completion: @escaping (Error?) -> Void) {
        defer {
            _unsubscribeAll()
            roomId = nil
            completion(nil)
        }
        
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        _removeUser { error in
        }

        SyncUtil.leaveScene(id: channelName)
    }

    private func _removeRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.deleteScenes()
        roomId = nil
        completion(nil)
    }
    
    private func _subscribeAll() {
        _subscribeOnlineUsersChanged()
        _subscribeMessageChanged()
        _subscribeMicSeatInvitationChanged()
        _subscribeMicSeatApplyChanged()
        _subscribePKInvitationChanged()
        _subscribeInteractionChanged()
    }
    
    private func _unsubscribeAll() {
        guard let channelName = roomId else {
            return
        }
        agoraPrint("imp all unsubscribe...")
        SyncUtil
            .scene(id: channelName)?
            .unsubscribeScene()
    }
}

//MARK: user operation
extension ShowSyncManagerServiceImp {
    private func _addUserIfNeed() {
        _getUserList { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.userId == VLUserCenter.user.id }) {
                return
            }
            self._addUserInfo {
            }
        }
    }

    private func _getUserList(finished: @escaping (Error?, [ShowUser]?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp user get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp user get success...")
                let users = list.compactMap({ ShowUser.yy_model(withJSON: $0.toJson()!)! })
//            guard !users.isEmpty else { return }
                self?.userList = users
                self?._updateUserCount(completion: { error in

                })
                finished(nil, users)
            }, fail: { error in
                agoraPrint("imp user get fail :\(error.message)...")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        guard let channelName = roomId else {
//            assert(false, "channelName = nil")
            print("addUserInfo channelName = nil")
            return
        }
        let model = ShowUser()
        model.userId = VLUserCenter.user.id
        model.avatar = VLUserCenter.user.headUrl
        model.userName = VLUserCenter.user.name

        let params = model.yy_modelToJSONObject() as! [String: Any]
        agoraPrint("imp user add ...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .add(data: params, success: { [weak self] object in
                agoraPrint("imp user add success...")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = ShowUser.yy_model(withJSON: jsonStr) else {
                    return
                }
                
                if self.userList.contains(where: { $0.userId == model.userId }) {
                    return
                }
                
                self.userList.append(model)
                finished()
            }, fail: { error in
                agoraPrint("imp user add fail :\(error.message)...")
                finished()
            })
    }
    
    
    private func _updateUserInfo(user: ShowUser, completion: @escaping (Error?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp user update...")

        let params = user.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .update(id: user.objectId!,
                    data:params,
                    success: {
                agoraPrint("imp user update success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp user update fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }

    private func _subscribeOnlineUsersChanged() {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp user subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { object in
                           agoraPrint("imp user subscribe onUpdated...")
                           guard let jsonStr = object.toJson(),
                                    let model = ShowUser.yy_model(withJSON: jsonStr) else { return }
                           if self.userList.contains(where: { $0.userId == model.userId }) { return }
                           self.userList.append(model)
                           self._updateUserCount { error in
                           }
                       }, onDeleted: { object in
                           agoraPrint("imp user subscribe onDeleted...")
                           if let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) {
                               self.userList.remove(at: index)
                               self._updateUserCount { error in
                               }
                           }
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                       }, fail: { error in
                           agoraPrint("imp user subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }

    private func _removeUser(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        
        guard let objectId = userList.filter({ $0.userId == VLUserCenter.user.id }).first?.objectId else {
            agoraAssert("_removeUser objectId = nil")
            return
        }
        agoraPrint("imp user delete... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .delete(id: objectId,
                    success: {
                agoraPrint("imp user delete success...")
            }, fail: { error in
                agoraPrint("imp user delete fail \(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }

    private func _updateUserCount(completion: @escaping (Error?) -> Void) {
        _updateUserCount(with: userList.count)
    }

    private func _updateUserCount(with count: Int) {
        guard let channelName = roomId,
              let roomInfo = roomList?.filter({ $0.roomId == self.getRoomId() }).first,
              roomInfo.ownerId == VLUserCenter.user.id
        else {
//            agoraPrint("updateUserCount channelName = nil")
//            userListCountDidChanged?(UInt(count))
            return
        }
        let roomUserCount = count
        if roomUserCount == roomInfo.roomUserCount {
            return
        }
        roomInfo.updatedAt = Int64(Date().timeIntervalSince1970 * 1000)
        roomInfo.roomUserCount = roomUserCount
        roomInfo.objectId = channelName
        var params = roomInfo.yy_modelToJSONObject() as! [String: Any]
        let objectId = channelName
        params["objectId"] = objectId
        agoraPrint("imp room update user count... [\(objectId)]")
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                agoraPrint("imp room update user count success...")
            }, fail: { error in
                agoraPrint("imp room update user count fail \(error.message)...")
            })

//        userListCountDidChanged?(UInt(count))
    }
}


//MARK: message operation
extension ShowSyncManagerServiceImp {
    private func _getMessageList(finished: @escaping (Error?, [ShowMessage]?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp message get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_MESSAGE_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp user get success...")
                let messageList = list.compactMap({ ShowMessage.yy_model(withJSON: $0.toJson()!)! })
//            guard !users.isEmpty else { return }
                self?.messageList = messageList
                finished(nil, messageList)
            }, fail: { error in
                agoraPrint("imp user get fail :\(error.message)...")
                agoraPrint("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _addMessage(message:ShowMessage, finished: ((Error?) -> Void)?) {
        let channelName = getRoomId()
        agoraPrint("imp message add ...")

        let params = message.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_MESSAGE_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp message add success...")
                finished?(nil)
            }, fail: { error in
                agoraPrint("imp message add fail :\(error.message)...")
                agoraPrint(error.message)
                finished?(NSError(domain: error.message, code: error.code))
            })
    }

    private func _subscribeMessageChanged() {
        let channelName = getRoomId()
        agoraPrint("imp message subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_MESSAGE_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           agoraPrint("imp message subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowMessage.yy_model(withJSON: jsonStr)
                           else {
                               return
                           }
                           self.messageList.append(model)
                           self.subscribeDelegate?.onMessageDidAdded(message: model)
                       }, onDeleted: { object in
                           agoraPrint("imp message subscribe onDeleted...")
                           assertionFailure("not implemented")
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp message subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
}

//MARK: Seat Apply
extension ShowSyncManagerServiceImp {
    private func _getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp seat apply get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_APPLY_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp seat apply success...")
                let seatApplyList = list.compactMap({ ShowMicSeatApply.yy_model(withJSON: $0.toJson()!)! })
                self?.seatApplyList = seatApplyList
                completion(nil, seatApplyList)
            }, fail: { error in
                agoraPrint("imp seat apply fail :\(error.message)...")
                completion(error, nil)
            })
    }
    
    private func _subscribeMicSeatApplyChanged() {
        let channelName = getRoomId()
        agoraPrint("imp seat apply subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_SEAT_APPLY_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           agoraPrint("imp seat apply subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowMicSeatApply.yy_model(withJSON: jsonStr) else { return }
                           defer {
                               self.subscribeDelegate?.onMicSeatApplyUpdated(apply: model)
                           }
                           if self.seatApplyList.contains(where: { $0.userId == model.userId }) { return }
                           self.seatApplyList.append(model)
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp seat apply subscribe onDeleted...")
                           guard let self = self else {return}
                           var model: ShowMicSeatApply? = nil
                           if let index = self.seatApplyList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.seatApplyList[index]
                               self.seatApplyList.remove(at: index)
                           }
                           guard let model = model else {return}
                           self.subscribeDelegate?.onMicSeatApplyDeleted(apply: model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp seat apply subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    private func _addMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp seat apply add ...")

        let params = apply.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_APPLY_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp seat apply add success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat apply add fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _removeMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp seat apply remove...")

        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_APPLY_COLLECTION)
            .delete(id: apply.objectId!,
                    success: {
                agoraPrint("imp seat apply remove success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat apply remove fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _updateMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        let channelName = getRoomId()
        agoraPrint("imp seat apply update...")

        let params = apply.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_APPLY_COLLECTION)
            .update(id: apply.objectId!,
                    data:params,
                    success: {
                agoraPrint("imp seat apply update success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat apply update fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
}


//MARK: Seat Invitation
extension ShowSyncManagerServiceImp {
    private func _getAllMicSeatInvitationList(completion: @escaping (Error?, [ShowMicSeatInvitation]?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp seat invitation get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INVITATION_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp seat invitation success...")
                let seatInvitationList = list
                    .compactMap({ ShowMicSeatInvitation.yy_model(withJSON: $0.toJson()!)! })
                    .filter ({ $0.userId != VLUserCenter.user.id })
                self?.seatInvitationList = seatInvitationList
                completion(nil, seatInvitationList)
            }, fail: { error in
                agoraPrint("imp seat invitation fail :\(error.message)...")
                completion(error, nil)
            })
    }
    
    private func _subscribeMicSeatInvitationChanged() {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp seat invitation subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_SEAT_INVITATION_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: {[weak self] object in
                           agoraPrint("imp seat invitation subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowMicSeatInvitation.yy_model(withJSON: jsonStr) else { return }
                           defer {
                               self.subscribeDelegate?.onMicSeatInvitationUpdated(invitation: model)
                           }
                           if self.seatInvitationList.contains(where: { $0.userId == model.userId }) { return }
                           self.seatInvitationList.append(model)
                       }, onDeleted: { object in
                           agoraPrint("imp seat invitation subscribe onDeleted...")
                           var model: ShowMicSeatInvitation? = nil
                           if let index = self.seatInvitationList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.seatInvitationList[index]
                               self.seatInvitationList.remove(at: index)
                           }
                           guard let model = model else {return}
                           self.subscribeDelegate?.onMicSeatInvitationDeleted(invitation: model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp seat invitation subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    private func _addMicSeatInvitation(invitation: ShowUser, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
//            assert(false, "channelName = nil")
            agoraPrint("_addMicSeatInvitation channelName = nil")
            return
        }
        agoraPrint("imp seat invitation add ...")

        let params = invitation.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INVITATION_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp seat invitation add success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat invitation add fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _removeMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraPrint("_removeMicSeatInvitation channelName = nil")
            return
        }
        agoraPrint("imp seat invitation remove...")

        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_INVITATION_COLLECTION)
            .delete(id: invitation.objectId!,
                    success: {
                agoraPrint("imp seat invitation remove success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat invitation remove fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _updateMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraPrint("_removeMicSeatApply channelName = nil")
            return
        }
        agoraPrint("imp seat invitation update...")

        let params = invitation.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_SEAT_APPLY_COLLECTION)
            .update(id: invitation.objectId!,
                    data:params,
                    success: {
                agoraPrint("imp seat invitation update success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp seat invitation update fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
}


//MARK: PK Invitation
extension ShowSyncManagerServiceImp {
    private func _getAllPKInvitationList(room: ShowRoomListModel?,
                                         completion: @escaping (Error?, [ShowPKInvitation]?) -> Void) {
        guard let channelName = room?.roomId ?? roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation get...")
        
        if roomId != channelName {
            guard let params = room?.yy_modelToJSONObject() as? [String: Any], let ownerId = room?.ownerId else {
                agoraAssert("room convert to param fail")
                return
            }
            
            SyncUtil.joinScene(id: channelName,
                               userId: ownerId,
                               property: params) {[weak self] result in
                SyncUtil
                    .scene(id: channelName)?
                    .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
                    .get(success: {  list in
                        agoraPrint("imp pk invitation success...")
                        let pkInvitationList = list.compactMap({ ShowPKInvitation.yy_model(withJSON: $0.toJson()!)! })
                        if channelName == self?.roomId {
                            self?.pkInvitationList = pkInvitationList
                        }
                        completion(nil, pkInvitationList)
                    }, fail: { error in
                        agoraPrint("imp pk invitation fail :\(error.message)...")
                        completion(error, nil)
                    })
            } fail: { error in
                completion(error, nil)
            }
            
            return
        }
        
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp pk invitation success...")
                let pkInvitationList = list.compactMap({ ShowPKInvitation.yy_model(withJSON: $0.toJson()!)! })
                if channelName == self?.roomId {
                    self?.pkInvitationList = pkInvitationList
                }
                completion(nil, pkInvitationList)
            }, fail: { error in
                agoraPrint("imp pk invitation fail :\(error.message)...")
                completion(error, nil)
            })
        
    }
    
    private func _unsubscribePKInvitationChanged(roomId:String?) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation unsubscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .unsubscribeScene()
    }
    
    private func _subscribePKInvitationChanged() {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation subscribe ...")
        _subscribePKInvitationChanged(channelName: channelName) { [weak self] (status, invitation) in
            guard let self = self else { return }
            switch status {
            case .deleted:
                var model: ShowPKInvitation? = nil
                if let index = self.pkInvitationList.firstIndex(where: { invitation.objectId == $0.objectId }) {
                    model = self.pkInvitationList[index]
                    self.pkInvitationList.remove(at: index)
                }
                guard let model = model else { return }
                self.subscribeDelegate?.onPKInvitationRejected(invitation: model)
            case .updated:
                defer {
                    if invitation.status == .accepted {
                        self.subscribeDelegate?.onPKInvitationAccepted(invitation: invitation)
                    } else if invitation.status == .rejected {
                        self.subscribeDelegate?.onPKInvitationRejected(invitation: invitation)
                    } else {
                        self.subscribeDelegate?.onPKInvitationUpdated(invitation: invitation)
                    }
                }
                if self.pkInvitationList.contains(where: { $0.userId == invitation.userId }) {
                    return
                }
                self.pkInvitationList.append(invitation)
            default:
                break
            }
        }
    }
    
    private func _subscribePKInvitationChanged(channelName:String,
                                               subscribeClosure: @escaping (ShowSubscribeStatus, ShowPKInvitation) -> Void) {
        agoraPrint("imp pk invitation \(channelName) subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_PK_INVITATION_COLLECTION,
                       onCreated: { object in
                agoraPrint("imp pk invitation \(channelName) subscribe onUpdated...")
                guard let jsonStr = object.toJson(),
                      let model = ShowPKInvitation.yy_model(withJSON: jsonStr) else {
                    return
                }
                subscribeClosure(.created, model)
            }, onUpdated: { object in
                agoraPrint("imp pk invitation \(channelName) subscribe onUpdated...")
                guard let jsonStr = object.toJson(),
                      let model = ShowPKInvitation.yy_model(withJSON: jsonStr) else {
                    return
                }
                subscribeClosure(.updated, model)
            }, onDeleted: {[weak self] object in
                agoraPrint("imp pk invitation \(channelName) subscribe onDeleted...")
                guard let self = self else {return}
                guard channelName == self.getRoomId() else {
                    if let model = ShowPKInvitation.yy_model(withJSON: object.toJson() ?? "") {
                        subscribeClosure(.deleted, model)
                    } else {
                        agoraAssert("imp pk invitation subscribe fail")
                    }
                    return
                }
                guard let model = ShowPKInvitation.yy_model(withJSON: object.toJson() ?? "") else { return }
                subscribeClosure(.deleted, model)
            }, onSubscribed: {
            }, fail: { error in
                agoraPrint("imp pk invitation subscribe fail \(error.message)...")
                ToastView.show(text: error.message)
            })
    }
    
    private func _addPKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = invitation.roomId else {
//            assert(false, "channelName = nil")
            agoraPrint("_addPKInvitation channelName = nil")
            return
        }
        agoraPrint("imp pk invitation add ...")

        let params = invitation.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp pk invitation add success...")
                self.pkCreatedInvitation?.objectId = object.getId()
                completion(nil)
            }, fail: { error in
                agoraPrint("imp pk invitation add fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _removePKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = invitation.roomId else {
            agoraPrint("_removePKInvitation channelName = nil")
            return
        }
        agoraPrint("imp pk invitation remove...")

        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
            .delete(id: invitation.objectId!,
                    success: {
                agoraPrint("imp pk invitation remove success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp pk invitation remove fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _updatePKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = invitation.roomId else {
            agoraPrint("_updatePKInvitation channelName = nil")
            return
        }
        agoraPrint("imp pk invitation update...")

        let params = invitation.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
            .update(id: invitation.objectId!,
                    data:params,
                    success: {
                agoraPrint("imp pk invitation update success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp pk invitation update fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _recvPKRejected(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId, let pkRoomId = invitation.roomId else { return }
        _unsubscribePKInvitationChanged(roomId: pkRoomId)
        SyncUtil.scene(id: pkRoomId)?.deleteScenes()
//        guard let interaction = self.interactionList.filter({ $0.userId == invitation.userId }).first else { return }
//        _removeInteraction(interaction: interaction) { error in
//        }
        self.pkCreatedInvitation = nil
        
        self.subscribeDelegate?.onPKInvitationRejected(invitation: invitation)
    }
    
    private func _recvPKAccepted(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId else { return }
        if let _ = self.interactionList.filter({ $0.userId == invitation.userId }).first {
            return
        }
        
        let interaction = ShowInteractionInfo()
        interaction.userId = invitation.userId
        interaction.userName = invitation.userName
        interaction.roomId = invitation.roomId
        interaction.interactStatus = .pking
        interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addInteraction(interaction: interaction) { error in
        }
        
        self.subscribeDelegate?.onPKInvitationAccepted(invitation: invitation)
    }
    
    private func _recvPKFinish(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId, let pkRoomId = invitation.roomId else { return }
        _unsubscribePKInvitationChanged(roomId: pkRoomId)
        SyncUtil.scene(id: pkRoomId)?.deleteScenes()
        guard let interaction = self.interactionList.filter({ $0.userId == invitation.userId }).first else {
            return
        }
        
        _removeInteraction(interaction: interaction) { error in
        }
        self.pkCreatedInvitation = nil
    }
    
    func _getCurrentApplyUser(roomId: String?, completion: @escaping (ShowRoomListModel?) -> Void) {
        guard let channelName = roomId else {
            agoraPrint("_updatePKInvitation channelName = nil")
            return
        }
        agoraPrint("imp pk invitation update...")
        getRoomList(page: 0) { _, roomList in
            let model = roomList?.filter({ $0.roomId == channelName && $0.interactStatus != .idle }).first
            completion(model)
        }
    }
}

//MARK: Interaction
extension ShowSyncManagerServiceImp {
    private func _getAllInteractionList(completion: @escaping (Error?, [ShowInteractionInfo]?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp interaction get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_INTERACTION_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp interaction success...")
                let interactionList = list.compactMap({ ShowInteractionInfo.yy_model(withJSON: $0.toJson()!)! })
                self?.interactionList = interactionList
                completion(nil, interactionList)
            }, fail: { error in
                agoraPrint("imp pk invitation fail :\(error.message)...")
                completion(error, nil)
            })
    }
    
    private func _subscribeInteractionChanged() {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_INTERACTION_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           agoraPrint("imp pk invitation subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowInteractionInfo.yy_model(withJSON: jsonStr) else {
                               return
                           }
                           
                           defer {
                               self.subscribeDelegate?.onInteractionBegan(interaction: model)
                           }
                           
                           if self.interactionList.contains(where: { $0.userId == model.userId }) {
                               return
                           }
                           self.interactionList.append(model)
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp pk invitation subscribe onDeleted...")
                           guard let self = self else {return}
                           var model: ShowInteractionInfo? = nil
                           if let index = self.interactionList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.interactionList[index]
                               self.interactionList.remove(at: index)
                           }
                           guard let model = model else {return}
                           self.subscribeDelegate?.onInterationEnded(interaction: model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp pk invitation subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    private func _addInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
//            assert(false, "channelName = nil")
            agoraPrint("_addInteraction channelName = nil")
            return
        }
        agoraPrint("imp interaction add ...")

        let params = interaction.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_INTERACTION_COLLECTION)
            .add(data: params, success: { object in
                agoraPrint("imp interaction add success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp interaction add fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _removeInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraPrint("_removeInteraction channelName = nil")
            return
        }
        agoraPrint("imp interaction remove...")

        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_INTERACTION_COLLECTION)
            .delete(id: interaction.objectId!,
                    success: {
                agoraPrint("imp interaction remove success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp interaction remove fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    private func _updateInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
            agoraPrint("_updateInteraction channelName = nil")
            return
        }
        agoraPrint("imp interaction update...")

        let params = interaction.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_INTERACTION_COLLECTION)
            .update(id: interaction.objectId!,
                    data:params,
                    success: {
                agoraPrint("imp interaction update success...")
                completion(nil)
            }, fail: { error in
                agoraPrint("imp interaction update fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
}

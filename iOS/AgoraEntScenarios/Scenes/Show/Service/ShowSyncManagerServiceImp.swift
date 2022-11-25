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
    
    private func _unsubscribe() {
        SyncUtil.scene(id: getRoomId())?.unsubscribeScene()
    }
    
    //MARK: ShowServiceProtocol
    
    func getRoomList(page: Int, completion: @escaping (Error?, [ShowRoomListModel]?) -> Void) {
        initScene { [weak self] in
            SyncUtil.fetchAll { results in
                print("result == \(results.compactMap { $0.toJson() })")
                guard let self = self else {
                    return
                }

                let dataArray = results.map({ info in
                    return ShowRoomListModel.yy_model(with: info.toJson()!.toDictionary())!
                })
                self.roomList = dataArray.sorted(by: { ($0.updatedAt > 0 ? $0.updatedAt : $0.createdAt) > ($1.updatedAt > 0 ? $1.updatedAt : $0.createdAt) })
                completion(nil, self.roomList)
            } fail: { error in
                completion(error, nil)
            }
        }
    }
    
    func createRoom(room: ShowRoomListModel,
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
//                    self._subscribeOnlineUsers {}
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
    
    func subscribeUserChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowUser) -> Void) {
        _subscribeOnlineUsers(subscribeClosure: subscribeClosure)
    }
    
    func getAllUserList(completion: @escaping (Error?, [ShowUser]?) -> Void) {
        _getUserList(finished: completion)
    }
    
    func sendChatMessage(message: ShowMessage, completion: ((Error?) -> Void)?) {
//        agoraAssert("not implemented")
        _addMessage(message: message, finished: completion)
    }
    
    func subscribeMessageChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMessage) -> Void) {
        _subscribeMessage(subscribeClosure: subscribeClosure)
    }
    
    func getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void) {
        _getAllMicSeatApplyList(completion: completion)
    }
    
    func subscribeMicSeatApplyChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatApply) -> Void) {
        _subscribeMicSeatApplyChanged(subscribeClosure: subscribeClosure)
    }
    
    
    func createMicSeatApply(completion: @escaping (Error?) -> Void) {
        let apply = ShowMicSeatApply()
        apply.userId = VLUserCenter.user.id
        apply.userName = VLUserCenter.user.name
        apply.userAvatar = VLUserCenter.user.headUrl
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
    
    func subscribeMicSeatInvitationChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatInvitation) -> Void) {
        _subscribeMicSeatInvitationChanged(subscribeClosure: subscribeClosure)
    }
    
    func createMicSeatInvitation(user: ShowUser, completion: @escaping (Error?) -> Void) {
        let invitation = ShowMicSeatInvitation()
        invitation.userId = user.userId
        invitation.userName = user.userName
        invitation.userAvatar = user.avatar
        invitation.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        invitation.fromUserId = VLUserCenter.user.id
        
        _addMicSeatInvitation(invitation: invitation, completion: completion)
    }
    
    func cancelMicSeatInvitation(userId: String, completion: @escaping (Error?) -> Void) {
        guard let invitation = self.seatInvitationList.filter({ $0.userId == userId }).first else {
            agoraAssert("cancel invitation not found")
            return
        }
        
        _removeMicSeatInvitation(invitation: invitation, completion: completion)
    }
    
    func acceptMicSeatInvitation(completion: @escaping (Error?) -> Void) {
        guard let invitation = self.seatInvitationList.filter({ $0.userId == VLUserCenter.user.userNo }).first else {
            agoraAssert("accept invitation not found")
            return
        }
        invitation.status = .accepted
        _updateMicSeatInvitation(invitation: invitation, completion: completion)
        
        let interaction = ShowInteractionInfo()
        interaction.userId = invitation.userId
        interaction.roomId = getRoomId()
        interaction.interactStatus = .onSeat
        interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addInteraction(interaction: interaction) { error in
        }
    }
    
    func rejectMicSeatInvitation(completion: @escaping (Error?) -> Void) {
        guard let invitation = self.seatInvitationList.filter({ $0.userId == VLUserCenter.user.userNo }).first else {
            agoraAssert("reject invitation not found")
            return
        }
        invitation.status = .rejected
        _updateMicSeatInvitation(invitation: invitation, completion: completion)
    }
    
    func getAllPKInvitationList(completion: @escaping (Error?, [ShowPKInvitation]?) -> Void) {
        _getAllPKInvitationList(roomId: roomId, completion: completion)
    }
    
    func subscribePKInvitationChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowPKInvitation) -> Void) {
        _subscribePKInvitationChanged(roomId: roomId, subscribeClosure: subscribeClosure)
    }
    
    func createPKInvitation(room: ShowRoomListModel,
                            completion: @escaping (Error?) -> Void) {
        _getAllPKInvitationList(roomId: room.roomId) {[weak self] error, invitationList in
            guard let self = self, error == nil, let invitationList = invitationList else { return }
            
            defer {
                self._unsubscribePKInvitationChanged(roomId: room.roomId)
                self._subscribePKInvitationChanged(roomId: room.roomId) { status, invitation in
                    guard invitation.fromRoomId == self.roomId else {
                        return
                    }
                    if status == .deleted {
                        self._recvPKRejected(invitation: invitation)
                    } else {
                        switch invitation.status {
                        case .rejected:
                            self._recvPKRejected(invitation: invitation)
                        case .accepted:
                            self._recvPKAccepted(invitation: invitation)
                        case .ended:
                            self._recvPKFinish(invitation: invitation)
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
                _invitation.roomId = room.roomId
                _invitation.fromUserId = VLUserCenter.user.rtc_uid
                _invitation.fromName = VLUserCenter.user.name
                _invitation.fromRoomId = self.roomId
                _invitation.status = .waitting
                _invitation.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
                self._addPKInvitation(invitation: _invitation, completion: completion)
                return
            }
            
            if invitation.status == .waitting {
                agoraPrint("pk invitaion already send ")
                return
            }
            
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
    }
    
    func rejectPKInvitation(completion: @escaping (Error?) -> Void) {
        guard let invitation = self.pkInvitationList.filter({ $0.userId == VLUserCenter.user.id }).first else {
            agoraAssert("accept invitation not found")
            return
        }
        invitation.status = .rejected
        _updatePKInvitation(invitation: invitation, completion: completion)
    }
    
    func getAllInterationList(completion: @escaping (Error?, [ShowInteractionInfo]?) -> Void) {
        _getAllInteractionList(completion: completion)
    }
    
    func subscribeInteractionChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowInteractionInfo) -> Void) {
        _subscribeInteractionChanged(subscribeClosure: subscribeClosure)
    }
    
    func stopInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
        _removeInteraction(interaction: interaction, completion: completion)
    }
}


//MARK: room operation
extension ShowSyncManagerServiceImp {
    private func _leaveRoom(completion: @escaping (Error?) -> Void) {
        defer {
            _unsubscribe()
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

    private func _subscribeOnlineUsers(subscribeClosure: @escaping (ShowSubscribeStatus, ShowUser) -> Void) {
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
              roomInfo.ownerId == VLUserCenter.user.rtc_uid
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

    private func _subscribeMessage(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMessage) -> Void) {
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
                           subscribeClosure(.updated, model)
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
    
    func _getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void) {
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
    
    func _subscribeMicSeatApplyChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatApply) -> Void) {
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
                           if self.seatApplyList.contains(where: { $0.userId == model.userId }) { return }
                           self.seatApplyList.append(model)
                           subscribeClosure(.updated, model)
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp seat apply subscribe onDeleted...")
                           guard let self = self else {return}
                           var model: ShowMicSeatApply? = nil
                           if let index = self.seatApplyList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.seatApplyList[index]
                               self.seatApplyList.remove(at: index)
                           }
                           guard let model = model else {return}
                           subscribeClosure(.deleted, model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp seat apply subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    func _addMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
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
    
    func _removeMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
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
    
    func _updateMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
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
    func _getAllMicSeatInvitationList(completion: @escaping (Error?, [ShowMicSeatInvitation]?) -> Void) {
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
                let seatInvitationList = list.compactMap({ ShowMicSeatInvitation.yy_model(withJSON: $0.toJson()!)! })
                self?.seatInvitationList = seatInvitationList
                completion(nil, seatInvitationList)
            }, fail: { error in
                agoraPrint("imp seat invitation fail :\(error.message)...")
                completion(error, nil)
            })
    }
    
    func _subscribeMicSeatInvitationChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatInvitation) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp seat invitation subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_SEAT_INVITATION_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { object in
                           agoraPrint("imp seat invitation subscribe onUpdated...")
                           guard let jsonStr = object.toJson(),
                                    let model = ShowMicSeatInvitation.yy_model(withJSON: jsonStr) else { return }
                           if self.seatInvitationList.contains(where: { $0.userId == model.userId }) { return }
                           self.seatInvitationList.append(model)
                           subscribeClosure(.updated, model)
                       }, onDeleted: { object in
                           agoraPrint("imp seat invitation subscribe onDeleted...")
                           var model: ShowMicSeatInvitation? = nil
                           if let index = self.seatInvitationList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.seatInvitationList[index]
                               self.seatInvitationList.remove(at: index)
                           }
                           guard let model = model else {return}
                           subscribeClosure(.deleted, model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp seat invitation subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    func _addMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
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
    
    func _removeMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
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
    
    func _updateMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
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
    func _getAllPKInvitationList(roomId:String?,
                                 completion: @escaping (Error?, [ShowPKInvitation]?) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation get...")
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_MANAGER_PK_INVITATION_COLLECTION)
            .get(success: { [weak self] list in
                agoraPrint("imp pk invitation success...")
                let pkInvitationList = list.compactMap({ ShowPKInvitation.yy_model(withJSON: $0.toJson()!)! })
                if roomId == self?.roomId {
                    self?.pkInvitationList = pkInvitationList
                }
                completion(nil, pkInvitationList)
            }, fail: { error in
                agoraPrint("imp pk invitation fail :\(error.message)...")
                completion(error, nil)
            })
    }
    
    func _unsubscribePKInvitationChanged(roomId:String?) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation unsubscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .unsubscribeScene()
    }
    
    func _subscribePKInvitationChanged(roomId:String?,
                                       subscribeClosure: @escaping (ShowSubscribeStatus, ShowPKInvitation) -> Void) {
        guard let channelName = roomId else {
            agoraAssert("channelName = nil")
            return
        }
        agoraPrint("imp pk invitation subscribe ...")
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_MANAGER_PK_INVITATION_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           agoraPrint("imp pk invitation subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowPKInvitation.yy_model(withJSON: jsonStr) else {
                               return
                           }
                           if self.pkInvitationList.contains(where: { $0.userId == model.userId }) {
                               return
                           }
                           self.pkInvitationList.append(model)
                           subscribeClosure(.updated, model)
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp pk invitation subscribe onDeleted...")
                           guard let self = self else {return}
                           var model: ShowPKInvitation? = nil
                           if let index = self.pkInvitationList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.pkInvitationList[index]
                               self.pkInvitationList.remove(at: index)
                           }
                           guard let model = model else {return}
                           subscribeClosure(.deleted, model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp pk invitation subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    func _addPKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
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
                completion(nil)
            }, fail: { error in
                agoraPrint("imp pk invitation add fail :\(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
    
    func _removePKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
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
    
    func _updatePKInvitation(invitation: ShowPKInvitation, completion: @escaping (Error?) -> Void) {
        guard let channelName = roomId else {
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
    
    func _recvPKRejected(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId, let pkRoomId = invitation.roomId else { return }
        _unsubscribePKInvitationChanged(roomId: pkRoomId)
//        guard let interaction = self.interactionList.filter({ $0.userId == invitation.userId }).first else { return }
//        _removeInteraction(interaction: interaction) { error in
//        }
    }
    
    func _recvPKAccepted(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId else { return }
        if let _ = self.interactionList.filter({ $0.userId == invitation.userId }).first {
            return
        }
        
        let interaction = ShowInteractionInfo()
        interaction.userId = invitation.userId
        interaction.roomId = invitation.roomId
        interaction.interactStatus = .pking
        interaction.createdAt = Int64(Date().timeIntervalSince1970 * 1000)
        _addInteraction(interaction: interaction) { error in
        }
    }
    
    func _recvPKFinish(invitation: ShowPKInvitation) {
        guard roomId == invitation.fromRoomId, let pkRoomId = invitation.roomId else { return }
        _unsubscribePKInvitationChanged(roomId: pkRoomId)
        guard let interaction = self.interactionList.filter({ $0.userId == invitation.userId }).first else {
            return
        }
        
        _removeInteraction(interaction: interaction) { error in
        }
    }
}



//MARK: Interaction
extension ShowSyncManagerServiceImp {
    func _getAllInteractionList(completion: @escaping (Error?, [ShowInteractionInfo]?) -> Void) {
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
    
    func _subscribeInteractionChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowInteractionInfo) -> Void) {
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
                           if self.interactionList.contains(where: { $0.userId == model.userId }) {
                               return
                           }
                           self.interactionList.append(model)
                           subscribeClosure(.updated, model)
                       }, onDeleted: {[weak self] object in
                           agoraPrint("imp pk invitation subscribe onDeleted...")
                           guard let self = self else {return}
                           var model: ShowInteractionInfo? = nil
                           if let index = self.interactionList.firstIndex(where: { object.getId() == $0.objectId }) {
                               model = self.interactionList[index]
                               self.interactionList.remove(at: index)
                           }
                           guard let model = model else {return}
                           subscribeClosure(.deleted, model)
                       }, onSubscribed: {
                       }, fail: { error in
                           agoraPrint("imp pk invitation subscribe fail \(error.message)...")
                           ToastView.show(text: error.message)
                       })
    }
    
    func _addInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
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
    
    func _removeInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
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
    
    func _updateInteraction(interaction: ShowInteractionInfo, completion: @escaping (Error?) -> Void) {
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

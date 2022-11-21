//
//  ShowSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/3.
//

import Foundation

private let kSceneId = "scene_show"

private let SYNC_MANAGER_MESSAGE_COLLECTION = "show_message_collection"

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
    
    private var syncUtilsInited: Bool = false
    private var roomNo: String? {
        didSet {
            if oldValue == roomNo {
                return
            }
            guard let _ = roomNo else {
                return
            }

            syncUtilsInited = false
        }
    }
    
    // MARK: Private
    private func getRoomNo() -> String {
        guard let roomNo = roomNo else {
            agoraAssert("roomNo == nil")
            return ""
        }

        return roomNo
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
        SyncUtil.scene(id: getRoomNo())?.unsubscribeScene()
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
            SyncUtil.joinScene(id: room.roomNo!,
                               userId: room.ownerId!,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                NetworkManager.shared.generateAllToken(channelName: channelName ?? "",
                                                       uid: "\(UserInfo.userId)") { rtcToken, rtmToken in
                    guard let self = self,
                          let rtcToken = rtcToken,
                          let rtmToken = rtmToken
                    else {
                        agoraAssert(rtcToken != nil && rtmToken != nil, "rtcToken == nil || rtmToken == nil")
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
            SyncUtil.joinScene(id: room.roomNo!,
                               userId: room.ownerId!,
                               property: params) { result in
                //            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
                let channelName = result.getPropertyWith(key: "roomNo", type: String.self) as? String
                let userId = result.getPropertyWith(key: "creator", type: String.self) as? String ?? ""
                self?.roomNo = channelName
                NetworkManager.shared.generateAllToken(channelName: channelName ?? "",
                                                       uid: "\(UserInfo.userId)") { rtcToken, rtmToken in
                    guard let self = self,
                          let rtcToken = rtcToken,
                          let rtmToken = rtmToken
                    else {
                        agoraAssert(rtcToken != nil && rtmToken != nil, "rtcToken == nil || rtmToken == nil")
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
        guard let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first else {
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
        agoraAssert("not implemented")
    }
    
    func subscribeMicSeatApplyChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatApply) -> Void) {
        agoraAssert("not implemented")
    }
    
    func createMicSeatApply(completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func cancelMicSeatApply(completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func acceptMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func rejectMicSeatApply(apply: ShowMicSeatApply, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func getAllMicSeatInvitationList(completion: @escaping (Error?, [ShowMicSeatInvitation]?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func subscribeMicSeatInvitationChanged(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatInvitation) -> Void) {
        agoraAssert("not implemented")
    }
    
    func createMicSeatInvitation(user: ShowUser, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func cancelMicSeatInvitation(user: ShowUser, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func acceptMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func rejectMicSeatInvitation(invitation: ShowMicSeatInvitation, completion: @escaping (Error?) -> Void) {
        agoraAssert("not implemented")
    }
}


//MARK: room operation
extension ShowSyncManagerServiceImp {
    private func _leaveRoom(completion: @escaping (Error?) -> Void) {
        defer {
            _unsubscribe()
            roomNo = nil
            completion(nil)
        }
        
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        _removeUser { error in
        }

        SyncUtil.leaveScene(id: channelName)
    }

    private func _removeRoom(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.deleteScenes()
        roomNo = nil
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
        guard let channelName = roomNo else {
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
        guard let channelName = roomNo else {
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
        guard let channelName = roomNo else {
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
        guard let channelName = roomNo else {
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
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first,
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
        roomInfo.objectId = roomNo
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
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
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
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            agoraPrint("_addMessage channelName = nil")
            return
        }
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
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
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
                           subscribeClosure(ShowSubscribeStatus.created, model)
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

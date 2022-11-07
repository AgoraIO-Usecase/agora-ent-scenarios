//
//  ShowSyncManagerServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/3.
//

import Foundation

private let kSceneId = "scene_show"

private func agoraAssert(_ message: String) {
    agoraAssert(false, message)
}

private func agoraAssert(_ condition: Bool, _ message: String) {
    #if DEBUG
    assert(condition, message)
    #else
    
    #endif
}

class ShowSyncManagerServiceImp: NSObject, ShowServiceProtocol {
    private var roomList: [ShowRoomListModel]?
    private var userList: [ShowUser] = [ShowUser]()
    
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
                    VLUserCenter.user.ifMaster = VLUserCenter.user.userNo == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    let output = ShowRoomDetailModel.yy_model(with: params!)
                    completion(nil, output)
                    self._subscribeOnlineUsers {}
                    self._addUserIfNeed()
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
                    VLUserCenter.user.ifMaster = VLUserCenter.user.userNo == userId ? true : false
                    VLUserCenter.user.agoraRTCToken = rtcToken
                    VLUserCenter.user.agoraRTMToken = rtmToken
                    let output = ShowRoomDetailModel.yy_model(with: params!)
                    completion(nil, output)
                    self._subscribeOnlineUsers {}
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
        if roomInfo.ownerId == VLUserCenter.user.userNo {
            _removeRoom(completion: completion)
            return
        }
        _leaveRoom(completion: completion)
    }
    
    func subscribeUser(subscribeClosure: @escaping (ShowSubscribeStatus, ShowUser) -> Void) {
        agoraAssert("not implemented")
    }
    
    func getAllUserList(completion: @escaping (Error?, [ShowUser]?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func sendChatMessage(message: ShowMessage, completion: ((Error) -> Void)?) {
        agoraAssert("not implemented")
    }
    
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMessage) -> Void) {
        agoraAssert("not implemented")
    }
    
    func getAllMicSeatApplyList(completion: @escaping (Error?, [ShowMicSeatApply]?) -> Void) {
        agoraAssert("not implemented")
    }
    
    func subscribeMicSeatApply(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatApply) -> Void) {
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
    
    func subscribeMicSeatInvitation(subscribeClosure: @escaping (ShowSubscribeStatus, ShowMicSeatInvitation) -> Void) {
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
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        _removeUser { error in
            // TODO(wushengtao): whitout callback
//            self.updateUserCount(with: max(self.userList.count - 1, 0))
        }
        // TODO(wushengtao): bacause of removeUser can not recv callback, invoke immediately
        _updateUserCount(with: max(userList.count - 1, 0))

        SyncUtil.leaveScene(id: channelName)
        roomNo = nil
        completion(nil)
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
        _getUserInfo { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.userId == VLUserCenter.user.userNo }) {
                return
            }
            self._addUserInfo {
                self._getUserInfo { error, userList in
                }
            }
        }
    }

    private func _getUserInfo(finished: @escaping (Error?, [ShowUser]?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                let users = list.compactMap({ ShowUser.yy_model(withJSON: $0.toJson()!)! })
//            guard !users.isEmpty else { return }
                self?.userList = users
                self?._updateUserCount(completion: { error in

                })
                finished(nil, users)
            }, fail: { error in
                print("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
//            assert(false, "channelName = nil")
            print("addUserInfo channelName = nil")
            return
        }
        let model = VLUserCenter.user

        let params = model.yy_modelToJSONObject() as! [String: Any]
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION).add(data: params, success: { object in
            finished()
        }, fail: { error in
            print(error.message)
            finished()
        })
    }

    private func _subscribeOnlineUsers(finished: @escaping () -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        SyncUtil
            .scene(id: channelName)?
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { object in
                           guard let jsonStr = object.toJson(),
                                    let model = ShowUser.yy_model(withJSON: jsonStr) else { return }
                           if self.userList.contains(where: { $0.userId == model.userId }) { return }
                           self.userList.append(model)
                           self._updateUserCount { error in
                           }
                       }, onDeleted: { object in
                           if let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) {
                               self.userList.remove(at: index)
                               self._updateUserCount { error in
                               }
                           }
                       }, onSubscribed: {
//                LogUtils.log(message: "subscribe message", level: .info)
                           finished()
                       }, fail: { error in
                           ToastView.show(text: error.message)
                           finished()
                       })
    }

    private func _removeUser(completion: @escaping (Error?) -> Void) {
        guard let channelName = roomNo else {
            agoraAssert("channelName = nil")
            return
        }
        let objectId = userList.filter({ $0.userId == UserInfo.userId && $0.objectId != nil }).first?.objectId ?? ""
        SyncUtil
            .scene(id: channelName)?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .delete(id: objectId,
                    success: {
                        completion(nil)
                    },
                    fail: { error in
                        completion(NSError(domain: error.message, code: error.code))
                    })
    }

    private func _updateUserCount(completion: @escaping (Error?) -> Void) {
        _updateUserCount(with: userList.count)
    }

    private func _updateUserCount(with count: Int) {
        guard let channelName = roomNo,
              let roomInfo = roomList?.filter({ $0.roomNo == self.getRoomNo() }).first
        else {
//            assert(false, "channelName = nil")
            print("updateUserCount channelName = nil")
            return
        }
        let roomUserCount = count
        if roomUserCount == roomInfo.roomUserCount {
            return
        }
        roomInfo.updatedAt = Date().timeIntervalSince1970
        roomInfo.roomUserCount = roomUserCount
        let params = roomInfo.yy_modelToJSONObject() as! [String: Any]
        SyncUtil
            .scene(id: channelName)?
            .update(key: "",
                    data: params,
                    success: { obj in
                        print("updateUserCount success")
                    },
                    fail: { error in
                        print("updateUserCount fail")
                    })

//        userListCountDidChanged?(UInt(count))
    }
}

//
//  JoyServiceImp.swift
//  Joy
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import AgoraSyncManager
import YYModel

private func mainTreadTask(_ task: (()->())?){
    if Thread.isMainThread {
        task?()
    }else{
        DispatchQueue.main.async {
            task?()
        }
    }
}

extension SyncError {
    func toNSError() ->NSError {
        return NSError(domain: self.message, code: code, userInfo: nil)
    }
}

/// 房间内用户列表
private let kSceneId = "scene_joy_4.10.0"
private let SYNC_SCENE_ROOM_USER_COLLECTION = "userCollection"
class JoyServiceImp: NSObject {
    private var appId: String = ""
    private var user: JoyUserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var roomList: [JoyRoomInfo] = []
    private weak var listener: JoyServiceListenerProtocol?
    
    private var userList: [JoyUserInfo] = []
    
    private var refreshRoomListClosure: (([JoyRoomInfo])->Void)?
    
    convenience init(appId: String, user: JoyUserInfo?) {
        self.init()
        self.appId = appId
        self.user = user
    }
    
    private lazy var manager: AgoraSyncManager = {
        let config = AgoraSyncManager.RethinkConfig(appId: self.appId,
                                                    channelName: kSceneId)
        let manager = AgoraSyncManager(config: config, complete: { code in
            if code == 0 {
                print("SyncManager init success")
            } else {
                print("SyncManager init error")
            }
        })
        
        return manager
    }()
    
    private func initScene(_ reqId: String, completion: @escaping (String, NSError?) -> Void) {
        if syncUtilsInited {
            completion(reqId, nil)
            return
        }

        manager.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            defer {
                completion(reqId, state == .open ? nil : NSError(domain: "network error", code: 1000))
            }
            
            joyPrint("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.listener?.onNetworkStatusChanged(status: JoyServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                return
            }
            
            self.syncUtilsInited = true
        }
    }
}

extension JoyServiceImp: JoyServiceProtocol {
    func getRoomList(completion: @escaping ([JoyRoomInfo]) -> Void) {
        refreshRoomListClosure = completion
        let reqId = NSString.withUUID()
        initScene(reqId) { [weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                joyPrint("getUserList fail1: \(error.localizedDescription)")
                self?.refreshRoomListClosure?([])
                self?.refreshRoomListClosure = nil
                return
            }
            self?.manager.getScenes(success: { results in
                guard let self = self else {return}
                joyPrint("getUserList == \(results.count)")

                let roomList = results.filter({$0.getId().count > 0}).map({ info in
                    return JoyRoomInfo.yy_model(withJSON: info.toJson())!
                })
                
                self.roomList = roomList
                self.refreshRoomListClosure?(self.roomList)
                self.refreshRoomListClosure = nil
            }, fail: { error in
                self?.refreshRoomListClosure?([])
                self?.refreshRoomListClosure = nil
            })
        }
    }
    
    func createRoom(roomName: String, completion: @escaping (JoyRoomInfo?, Error?) -> Void) {
        joyPrint("createRoom start")
        guard let user = self.user else {
            completion(nil, nil)
            return
        }
        let roomInfo = JoyRoomInfo()
        roomInfo.ownerId = user.userId
        roomInfo.ownerName = user.userName
        roomInfo.ownerAvatar = user.avatar
        roomInfo.roomName = roomName
        roomInfo.assistantUid = 1000000000 + user.userId
        roomInfo.roomId = "\(arc4random_uniform(899999) + 100000)"
        roomInfo.objectId = roomInfo.roomId
        let reqId = NSString.withUUID()
        initScene(reqId) {[weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                joyPrint("createRoom fail1: \(error.localizedDescription)")
                completion(nil, error)
                return
            }
            
            let params = roomInfo.yy_modelToJSONObject() as? [String: Any]
            let scene = Scene(id: roomInfo.roomId, userId: "\(roomInfo.ownerId)", isOwner: true, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: roomInfo.roomId) { sceneRef in
                    joyPrint("createRoom success")
                    mainTreadTask {
                        self.sceneRefs[roomInfo.roomId] = sceneRef
                        self._addUserIfNeed(channelName: roomInfo.roomId) { err in
                        }
                        self._subscribeUsersChanged(channelName: roomInfo.roomId)
                        self.subscribeRoomStatusChanged(channelName: roomInfo.roomId)
                        completion(roomInfo, nil)
                    }
                } fail: { error in
                    joyPrint("createRoom fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(nil, error)
                    }
                }
            }) { error in
                joyWarn("createRoom fail3: \(error.localizedDescription)")
                mainTreadTask {
                    completion(nil, error)
                }
            }
        }
    }
    
    func joinRoom(roomInfo: JoyRoomInfo, completion: @escaping (Error?) -> Void) {
        let reqId = NSString.withUUID()
        initScene(reqId) {[weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                joyPrint("joinRoom fail1: \(error.localizedDescription)")
                completion(error)
                return
            }
            
            let params = roomInfo.yy_modelToJSONObject() as? [String: Any]
            let isOwner = roomInfo.ownerId == self?.user?.userId ? true : false
            let scene = Scene(id: roomInfo.roomId, userId: "\(roomInfo.ownerId)", isOwner: isOwner, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: roomInfo.roomId) { sceneRef in
                    joyPrint("joinRoom success")
                    mainTreadTask {
                        self.sceneRefs[roomInfo.roomId] = sceneRef
                        self._addUserIfNeed(channelName: roomInfo.roomId) { err in
                        }
                        self._subscribeUsersChanged(channelName: roomInfo.roomId)
                        self.subscribeRoomStatusChanged(channelName: roomInfo.roomId)
                        completion(nil)
                    }
                } fail: {[weak self] error in
                    joyWarn("joinRoom fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(error)
                    }
                }
            }) {[weak self] error in
                joyWarn("joinRoom fail3: \(error.localizedDescription)")
                self?.leaveRoom(roomInfo: roomInfo) { err in
                }
                mainTreadTask {
                    completion(error)
                }
            }
        }
    }
    
    func updateRoom(roomInfo: JoyRoomInfo, completion: @escaping (NSError?) -> Void) {
        let channelName = roomInfo.roomId
        guard let scene = sceneRefs[channelName], roomInfo.ownerId == user?.userId else {
            completion(NSError(domain: "no permission to update room", code: -1))
            return
        }
        assert(!roomInfo.objectId.isEmpty, "roomInfo.objectId is empty")
        let params = roomInfo.yy_modelToJSONObject() as! [String: Any]
        joyPrint("imp room update... [\(channelName)]")
        scene
            .update(key: "",
                    data: params,
                    success: { obj in
                joyPrint("imp room update user count success...")
            }, fail: { error in
                joyError("imp room update user count fail \(error.message)...")
            })
        completion(nil)
    }
    
    func leaveRoom(roomInfo: JoyRoomInfo, completion: @escaping (Error?) -> Void) {
        self._removeUser(channelName: roomInfo.roomId) { err in
        }
        if roomInfo.ownerId == user?.userId {
            sceneRefs[roomInfo.roomId]?.deleteScenes()
        } else {
            manager.leaveScene(roomId: roomInfo.roomId)
        }
        completion(nil)
    }
    
    func sendChatMessage(roomId: String,
                         message: String,
                         completion: ((NSError?) -> Void)?) {
        
    }
    
    func subscribeListener(listener: JoyServiceListenerProtocol?) {
        self.listener = listener
    }
}

//MARK: room
extension JoyServiceImp {
    func subscribeRoomStatusChanged(channelName: String) {
        guard let scene = sceneRefs[channelName] else {return}
        joyPrint("imp room subscribe...")
        scene
            .subscribe(key: "",
                       onCreated: { _ in
                       }, onUpdated: { _ in
                       }, onDeleted: { [weak self] object in
                           guard let self = self else {return}
                           guard let model = self.roomList.filter({ $0.objectId == object.getId()}).first,
                                 model.roomId == channelName
                           else {
                               return
                           }
                           joyPrint("imp room subscribe onDeleted...")
                           self.listener?.onRoomDidDestroy(roomInfo: model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }
}

//MARK: user
extension JoyServiceImp {
    private func _getUserList(channelName: String, finished: @escaping (NSError?, [JoyUserInfo]?) -> Void) {
        guard let scene = sceneRefs[channelName] else {return}
        joyPrint("imp user get...")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                joyPrint("imp user get success...")
                let users = list.compactMap({ JoyUserInfo.yy_model(withJSON: $0.toJson()!)! })
                self?.userList = users
                finished(nil, users)
            }, fail: { error in
                joyWarn("imp user get fail :\(error.message)...")
                finished(error.toNSError(), nil)
            })
    }
    
    fileprivate func _addUserIfNeed(channelName: String, finished: @escaping (NSError?) -> Void) {
        _getUserList(channelName: channelName) {[weak self] error, userList in
            guard let self = self else {
                finished(NSError(domain: "unknown error", code: -1))
                return
            }
            // current user already add
            if self.userList.contains(where: { $0.userId == self.user?.userId }) {
                finished(nil)
                return
            }
            self._addUserInfo(channelName: channelName, finished: finished)
        }
    }
    
    private func _addUserInfo(channelName: String, finished: @escaping (NSError?) -> Void) {
        guard let scene = sceneRefs[channelName], let model = user else {return}
        let params = model.yy_modelToJSONObject() as! [String: Any]
        joyPrint("imp user add ...")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .add(data: params, success: { [weak self] object in
                joyPrint("imp user add success...")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = JoyUserInfo.yy_model(withJSON: jsonStr) else {
                    return
                }
                
                if self.userList.contains(where: { $0.userId == model.userId }) {
                    return
                }
                
                self.userList.append(model)
                finished(nil)
            }, fail: { error in
                joyWarn("imp user add fail :\(error.message)...")
                finished(error.toNSError())
            })
    }
    
    private func _removeUser(channelName: String, completion: @escaping (NSError?) -> Void) {
        guard let scene = sceneRefs[channelName],
              let objectId = userList.filter({ $0.userId == self.user?.userId }).first?.objectId else {
            joyWarn("_removeUser objectId = nil")
            return
        }
        joyPrint("imp user delete... [\(objectId)]")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .delete(id: objectId,
                    success: { _ in
                joyPrint("imp user delete success...")
            }, fail: { error in
                joyWarn("imp user delete fail \(error.message)...")
                completion(error.toNSError())
            })
    }
    
    private func _subscribeUsersChanged(channelName: String) {
        guard let scene = sceneRefs[channelName] else {return}
        joyPrint("imp user subscribe ...")
        scene
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           joyPrint("imp user subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = JoyUserInfo.yy_model(withJSON: jsonStr) else { return }
                           defer{
                               self.listener?.onUserListDidChanged(userList: self.userList)
                           }
                           if self.userList.contains(where: { $0.userId == model.userId }) {
                               return
                           }
                           self.userList.append(model)
                           
                       }, onDeleted: { [weak self] object in
                           joyPrint("imp user subscribe onDeleted... [\(object.getId())]")
                           guard let self = self else { return }
                           defer{
                               self.listener?.onUserListDidChanged(userList: self.userList)
                           }
                           guard let index = self.userList.firstIndex(where: { object.getId() == $0.objectId }) else{
                               return
                           }
                           let model = self.userList[index]
                           self.userList.remove(at: index)
                       }, onSubscribed: {
                       }, fail: { error in
                           joyWarn("imp user subscribe fail \(error.message)...")
                       })
    }
}

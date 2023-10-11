//
//  ShowTo1v1ServiceImp.swift
//  ShowTo1v1
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
private let kSceneId = "scene_Livetoprivate_3.7.0"
private let SYNC_SCENE_ROOM_USER_COLLECTION = "userCollection"
class ShowTo1v1ServiceImp: NSObject {
    private var appId: String = ""
    private var user: ShowTo1v1UserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var roomList: [ShowTo1v1RoomInfo] = []
    private weak var listener: ShowTo1v1ServiceListenerProtocol?
    
    private var userList: [ShowTo1v1UserInfo] = []
    
    private var refreshRoomListClosure: (([ShowTo1v1RoomInfo])->Void)?
    
    convenience init(appId: String, user: ShowTo1v1UserInfo?) {
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
            
            showTo1v1Print("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.listener?.onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                return
            }
            
            self.syncUtilsInited = true
        }
    }
}

extension ShowTo1v1ServiceImp: ShowTo1v1ServiceProtocol {
    func getRoomList(completion: @escaping ([ShowTo1v1RoomInfo]) -> Void) {
        refreshRoomListClosure = completion
        let reqId = NSString.withUUID()
        initScene(reqId) { [weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                showTo1v1Print("getUserList fail1: \(error.localizedDescription)")
                self?.refreshRoomListClosure?([])
                self?.refreshRoomListClosure = nil
                return
            }
            self?.manager.getScenes(success: { results in
                guard let self = self else {return}
                showTo1v1Print("getUserList == \(results.count)")

                let roomList = results.filter({$0.getId().count > 0}).map({ info in
                    return ShowTo1v1RoomInfo.yy_model(withJSON: info.toJson())!
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
    
    func createRoom(roomName: String, completion: @escaping (ShowTo1v1RoomInfo?, Error?) -> Void) {
        showTo1v1Print("createRoom start")
        guard let user = self.user else {
            completion(nil, nil)
            return
        }
        let roomInfo = ShowTo1v1RoomInfo()
        roomInfo.userId = user.userId
        roomInfo.userName = user.userName
        roomInfo.avatar = user.avatar
        roomInfo.roomName = roomName
        roomInfo.roomId = "\(arc4random_uniform(899999) + 100000)"
        let reqId = NSString.withUUID()
        initScene(reqId) {[weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                showTo1v1Print("createRoom fail1: \(error.localizedDescription)")
                completion(nil, error)
                return
            }
            
            let params = roomInfo.yy_modelToJSONObject() as? [String: Any]
            let scene = Scene(id: roomInfo.roomId, userId: roomInfo.userId, isOwner: true, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: roomInfo.roomId) { sceneRef in
                    showTo1v1Print("createRoom success")
                    mainTreadTask {
                        self.sceneRefs[roomInfo.roomId] = sceneRef
                        self._addUserIfNeed(channelName: roomInfo.roomId) { err in
                        }
                        self._subscribeUsersChanged(channelName: roomInfo.roomId)
                        self.subscribeRoomStatusChanged(channelName: roomInfo.roomId)
                        completion(roomInfo, nil)
                    }
                } fail: { error in
                    showTo1v1Print("createRoom fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(nil, error)
                    }
                }
            }) { error in
                showTo1v1Print("createRoom fail3: \(error.localizedDescription)")
                mainTreadTask {
                    completion(nil, error)
                }
            }
        }
    }
    
    func joinRoom(roomInfo:ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void) {
        let reqId = NSString.withUUID()
        initScene(reqId) {[weak self] rid, error in
            guard reqId == rid else {return}
            if let error = error {
                showTo1v1Print("joinRoom fail1: \(error.localizedDescription)")
                completion(error)
                return
            }
            
            let params = roomInfo.yy_modelToJSONObject() as? [String: Any]
            let isOwner = roomInfo.userId == self?.user?.userId ? true : false
            let scene = Scene(id: roomInfo.roomId, userId: roomInfo.userId, isOwner: isOwner, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: roomInfo.roomId) { sceneRef in
                    showTo1v1Print("joinRoom success")
                    mainTreadTask {
                        self.sceneRefs[roomInfo.roomId] = sceneRef
                        self._addUserIfNeed(channelName: roomInfo.roomId) { err in
                        }
                        self._subscribeUsersChanged(channelName: roomInfo.roomId)
                        self.subscribeRoomStatusChanged(channelName: roomInfo.roomId)
                        completion(nil)
                    }
                } fail: {[weak self] error in
                    showTo1v1Print("joinRoom fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(error)
                    }
                }
            }) {[weak self] error in
                showTo1v1Print("joinRoom fail3: \(error.localizedDescription)")
                self?.leaveRoom(roomInfo: roomInfo) { err in
                }
                mainTreadTask {
                    completion(error)
                }
            }
        }
    }
    
    func leaveRoom(roomInfo: ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void) {
        self._removeUser(channelName: roomInfo.roomId) { err in
        }
        if roomInfo.userId == user?.userId {
            sceneRefs[roomInfo.roomId]?.deleteScenes()
        } else {
            manager.leaveScene(roomId: roomInfo.roomId)
        }
        completion(nil)
    }
    
    func subscribeListener(listener: ShowTo1v1ServiceListenerProtocol?) {
        self.listener = listener
    }
}

//MARK: room
extension ShowTo1v1ServiceImp {
    func subscribeRoomStatusChanged(channelName: String) {
        guard let scene = sceneRefs[channelName] else {return}
        showTo1v1Print("imp room subscribe...")
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
                           showTo1v1Print("imp room subscribe onDeleted...")
                           self.listener?.onRoomDidDestroy(roomInfo: model)
                       }, onSubscribed: {}, fail: { error in
                       })
    }
}

//MARK: user
extension ShowTo1v1ServiceImp {
    private func _getUserList(channelName: String, finished: @escaping (NSError?, [ShowTo1v1UserInfo]?) -> Void) {
        guard let scene = sceneRefs[channelName] else {return}
        showTo1v1Print("imp user get...")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                showTo1v1Print("imp user get success...")
                let users = list.compactMap({ ShowTo1v1UserInfo.yy_model(withJSON: $0.toJson()!)! })
                self?.userList = users
                finished(nil, users)
            }, fail: { error in
                showTo1v1Print("imp user get fail :\(error.message)...")
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
        showTo1v1Print("imp user add ...")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .add(data: params, success: { [weak self] object in
                showTo1v1Print("imp user add success...")
                guard let self = self,
                      let jsonStr = object.toJson(),
                      let model = ShowTo1v1UserInfo.yy_model(withJSON: jsonStr) else {
                    return
                }
                
                if self.userList.contains(where: { $0.userId == model.userId }) {
                    return
                }
                
                self.userList.append(model)
                finished(nil)
            }, fail: { error in
                showTo1v1Warn("imp user add fail :\(error.message)...")
                finished(error.toNSError())
            })
    }
    
    private func _removeUser(channelName: String, completion: @escaping (NSError?) -> Void) {
        guard let scene = sceneRefs[channelName],
              let objectId = userList.filter({ $0.userId == self.user?.userId }).first?.objectId else {
            showTo1v1Print("_removeUser objectId = nil")
            return
        }
        showTo1v1Print("imp user delete... [\(objectId)]")
        scene
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .delete(id: objectId,
                    success: { _ in
                showTo1v1Print("imp user delete success...")
            }, fail: { error in
                showTo1v1Warn("imp user delete fail \(error.message)...")
                completion(error.toNSError())
            })
    }
    
    private func _subscribeUsersChanged(channelName: String) {
        guard let scene = sceneRefs[channelName]else {return}
        showTo1v1Print("imp user subscribe ...")
        scene
            .subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                       onCreated: { _ in
                       }, onUpdated: { [weak self] object in
                           showTo1v1Print("imp user subscribe onUpdated...")
                           guard let self = self,
                                 let jsonStr = object.toJson(),
                                 let model = ShowTo1v1UserInfo.yy_model(withJSON: jsonStr) else { return }
                           defer{
                               self.listener?.onUserListDidChanged(userList: self.userList)
                           }
                           if self.userList.contains(where: { $0.userId == model.userId }) {
                               return
                           }
                           self.userList.append(model)
                           
                       }, onDeleted: { [weak self] object in
                           showTo1v1Print("imp user subscribe onDeleted... [\(object.getId())]")
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
                           showTo1v1Warn("imp user subscribe fail \(error.message)...")
                       })
    }
}

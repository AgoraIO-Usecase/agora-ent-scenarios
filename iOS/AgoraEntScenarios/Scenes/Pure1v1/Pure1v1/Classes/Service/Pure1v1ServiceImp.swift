//
//  Pure1v1ServiceImp.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation
import AgoraSyncManager
import YYModel

func pure1v1Print(_ message: String) {
    print("[Pure1v1]\(message)")
}


private func mainTreadTask(_ task: (()->())?){
    if Thread.isMainThread {
        task?()
    }else{
        DispatchQueue.main.async {
            task?()
        }
    }
}

/// 房间内用户列表
private let SYNC_SCENE_ROOM_USER_COLLECTION = "userCollection"
private let kSceneId = "pure1v1_3.0.0"
private let kDefaultRoomName = "pure1v1"
class Pure1v1ServiceImp: NSObject {
    private var appId: String = ""
    private var user: Pure1v1UserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var userList: [Pure1v1UserInfo] = []
    private var networkDidChanged: ((Pure1v1ServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?
    
    convenience init(user: Pure1v1UserInfo?) {
        self.init()
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
    
    private func initScene(completion: @escaping (NSError?) -> Void) {
        if syncUtilsInited {
            completion(nil)
            return
        }

        manager.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            defer {
                completion(state == .open ? nil : NSError(domain: "network error", code: 1000))
            }
            
            pure1v1Print("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(Pure1v1ServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                return
            }
            
            self.syncUtilsInited = true
        }
    }
}

//user
extension Pure1v1ServiceImp {
    private func _addUserIfNeed() {
        _getUserList { error, userList in
            // current user already add
            if self.userList.contains(where: { $0.userId == self.user?.userId }) {
                return
            }
            self._addUserInfo {
            }
        }
    }

    private func _getUserList(finished: @escaping (Error?, [Pure1v1UserInfo]?) -> Void) {
        pure1v1Print("imp user get...")
        let scene = sceneRefs[kSceneId]
        scene?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .get(success: { [weak self] list in
                pure1v1Print("imp user get success, count = \(list.count)...")
                let users = list.compactMap({ Pure1v1UserInfo.yy_model(withJSON: $0.toJson()!)! })
                self?.userList = users
                finished(nil, users)
            }, fail: { error in
                pure1v1Print("imp user get fail :\(error.message)...")
                pure1v1Print("error = \(error.description)")
                finished(error, nil)
            })
    }

    private func _addUserInfo(finished: @escaping () -> Void) {
        pure1v1Print("imp user add ...")
        guard let params = user?.yy_modelToJSONObject() as? [String: Any] else {
            finished()
            return
        }

        let scene = sceneRefs[kSceneId]
        scene?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .add(data: params, success: { object in
                pure1v1Print("imp user add success...")
                finished()
            }, fail: { error in
                pure1v1Print("imp user add fail :\(error.message)...")
                pure1v1Print(error.message)
                finished()
            })
    }
    
    private func _removeUser(completion: @escaping (Error?) -> Void) {
        guard let objectId = userList.filter({ $0.userId == self.user?.userId }).first?.objectId else {
//            agoraAssert("_removeUser objectId = nil")
            completion(nil)
            return
        }
        pure1v1Print("imp user delete... [\(objectId)]")
        let scene = sceneRefs[kSceneId]
        scene?
            .collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .document(id: objectId)
            .delete(success: {_ in
                pure1v1Print("imp user delete success...")
                completion(nil)
            }, fail: { error in
                pure1v1Print("imp user delete fail \(error.message)...")
                completion(NSError(domain: error.message, code: error.code))
            })
    }
}

extension Pure1v1ServiceImp: Pure1v1ServiceProtocol {
    func joinRoom(completion: @escaping (Error?) -> Void) {
        guard let user = self.user else {
            completion(nil)
            return
        }
        pure1v1Print("joinRoom start")
        let params = ["roomId": kDefaultRoomName]
        let scene = Scene(id: kSceneId, userId: user.userId, isOwner: true, property: params)
        initScene {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                pure1v1Print("joinRoom fail1: \(error.localizedDescription)")
                completion(error)
                return
            }
            self.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: kSceneId) { sceneRef in
                    pure1v1Print("joinRoom success")
                    mainTreadTask {
                        self.sceneRefs[kSceneId] = sceneRef
                        self._addUserIfNeed()
                        completion(nil)
                    }
                } fail: { error in
                    pure1v1Print("joinRoom fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(error)
                    }
                }
            }) { error in
                pure1v1Print("joinRoom fail3: \(error.localizedDescription)")
                mainTreadTask {
                    completion(error)
                }
            }
        }
        
    }
    
    func leaveRoom(completion: @escaping (Error?) -> Void) {
        _removeUser { error in
        }
        manager.leaveScene(roomId: kDefaultRoomName)
    }
    
    func getUserList(completion: @escaping ([Pure1v1UserInfo]) -> Void) {
        _getUserList { err, list in
            completion(list ?? [])
        }
    }
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (Pure1v1ServiceNetworkStatus) -> Void) {
        self.networkDidChanged = changedBlock
    }
    
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void) {
        self.roomExpiredDidChanged = changedBlock
    }
    
    func unsubscribeAll() {
        networkDidChanged = nil
        roomExpiredDidChanged = nil
    }
}

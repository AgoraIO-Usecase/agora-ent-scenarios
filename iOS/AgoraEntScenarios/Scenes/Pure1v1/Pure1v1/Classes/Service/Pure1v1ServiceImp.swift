//
//  Pure1v1ServiceImp.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation
import AgoraSyncManager
import YYModel
import SwiftyBeaver

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
private let kSceneId = "scene_1v1PrivateVideo_4.2.0"
class Pure1v1ServiceImp: NSObject {
    private var appId: String = ""
    private var user: Pure1v1UserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var userList: [Pure1v1UserInfo] = []
    private var networkDidChanged: ((Pure1v1ServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?
    private var state: SocketConnectState = .connecting
    private var refreshRoomListClosure: (([Pure1v1UserInfo], NSError?)->())?
    private var enterRoomClosure: ((NSError?)->())?
    
    convenience init(appId: String, user: Pure1v1UserInfo?) {
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
    
    private func checkState() -> NSError? {
        guard syncUtilsInited else {
            return NSError(domain: "initializing", code: -1)
        }
        
        return state == .open ? nil : NSError(domain: "network error", code: -1)
    }
    
    private func initScene(completion: @escaping (NSError?) -> Void) {
        if syncUtilsInited {
            completion(nil)
            return
        }

        manager.subscribeConnectState { [weak self] (state) in
            guard let self = self else {
                return
            }
            
            self.state = state
            defer {
                completion(state == .open ? nil : NSError(domain: "network error", code: -1))
            }
            
            pure1v1Print("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(Pure1v1ServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                return
            }
            
            self.syncUtilsInited = true
        }
    }
    
    private func _notifyRefreshListCompletion(_ userList: [Pure1v1UserInfo], _ error: NSError?) {
        refreshRoomListClosure?(userList, error)
        refreshRoomListClosure = nil
    }
    
    private func _notifyEnterCompletion(_ error: NSError?) {
        enterRoomClosure?(error)
        enterRoomClosure = nil
    }
}

extension Pure1v1ServiceImp: Pure1v1ServiceProtocol {
    func getUserList(completion: @escaping ([Pure1v1UserInfo], NSError?) -> Void) {
        self.refreshRoomListClosure = completion
        initScene { [weak self] error in
            if let error = error {
                pure1v1Print("getUserList fail1: \(error.localizedDescription)")
                completion([], error)
                return
            }
            self?.manager.getScenes(success: { results in
                pure1v1Print("getUserList == \(results.compactMap { $0.toJson() })")
                guard let self = self else {return}
                guard self.state == .open else {
                    self._notifyRefreshListCompletion([], NSError(domain: "network error", code: -1))
                    return
                }
                
                let userList = results.filter({$0.getId().count > 0}).map({ info in
                    return Pure1v1UserInfo.yy_model(withJSON: info.toJson())!
                }).sorted {$0.createdAt < $1.createdAt}
                self.userList = userList
                self._notifyRefreshListCompletion(userList, nil)
            }, fail: { error in
                self?._notifyRefreshListCompletion([], error.toNSError())
            })
        }
    }
    
    func enterRoom(completion: @escaping (NSError?) -> Void) {
        //create scene通过uid，保证一个用户不会展示多个
        guard let user = self.user, !userList.contains(where: { $0.userId == user.userId }) else {
            completion(nil)
            return
        }
        self.enterRoomClosure = completion
        initScene { [weak self] error in
            if let error = error {
                mainTreadTask {
                    self?._notifyEnterCompletion(error)
                }
                return
            }
            let params = user.yy_modelToJSONObject() as? [String: Any]
            let scene = Scene(id: user.userId, userId: user.userId, isOwner: true, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: user.userId) { sceneRef in
                    pure1v1Print("createUser success")
                    mainTreadTask {
                        self.sceneRefs[user.userId] = sceneRef
                        self._notifyEnterCompletion(nil)
                    }
                } fail: { error in
                    pure1v1Print("createUser fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        self._notifyEnterCompletion(error.toNSError())
                    }
                }
            }) { error in
                pure1v1Print("createUser fail3: \(error.localizedDescription)")
                mainTreadTask {
                    self?._notifyEnterCompletion(error.toNSError())
                }
            }
        }
    }
    
    func leaveRoom(completion: @escaping (NSError?) -> Void) {
        self.sceneRefs[user?.userId ?? ""]?.deleteScenes()
        completion(nil)
    }
    
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (Pure1v1ServiceNetworkStatus) -> Void) {
        self.networkDidChanged = changedBlock
    }
    
    func unsubscribeAll() {
        networkDidChanged = nil
        roomExpiredDidChanged = nil
    }
}

//
//  ShowTo1v1ServiceImp.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation
import AgoraSyncManager
import YYModel

func showTo1v1Print(_ message: String) {
    print("[ShowTo1v1]\(message)")
}

func showTo1v1Warn(_ message: String) {
    print("[ShowTo1v1][Warning]\(message)")
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
private let kSceneId = "showTo1v1_3.0.0"
class Pure1v1ServiceImp: NSObject {
    private var appId: String = ""
    private var user: ShowTo1v1UserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var userList: [ShowTo1v1UserInfo] = []
    private var networkDidChanged: ((ShowTo1v1ServiceNetworkStatus) -> Void)?
    private var roomExpiredDidChanged: (() -> Void)?
    
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
            
            showTo1v1Print("subscribeConnectState: \(state) \(self.syncUtilsInited)")
            self.networkDidChanged?(ShowTo1v1ServiceNetworkStatus(rawValue: state.rawValue) ?? .fail)
            guard !self.syncUtilsInited else {
                return
            }
            
            self.syncUtilsInited = true
        }
    }
}

extension Pure1v1ServiceImp: ShowTo1v1ServiceProtocol {
    func getUserList(completion: @escaping ([ShowTo1v1UserInfo]) -> Void) {
        initScene { [weak self] error in
            if let error = error {
                showTo1v1Print("getUserList fail1: \(error.localizedDescription)")
                completion([])
                return
            }
            self?.manager.getScenes(success: { results in
                showTo1v1Print("getUserList == \(results.compactMap { $0.toJson() })")

                let userList = results.map({ info in
                    return ShowTo1v1UserInfo.yy_model(withJSON: info.toJson())!
                })
                self?.userList = userList
                completion(userList)
            }, fail: { error in
                completion([])
            })
        }
    }
    
    func createRoom(completion: @escaping (Error?) -> Void) {
        guard let user = self.user, !userList.contains(where: { $0.getRoomId() == user.getRoomId() }) else {
            completion(nil)
            return
        }
        showTo1v1Print("createUser start")
        initScene {[weak self] error in
            if let error = error {
                showTo1v1Print("createUser fail1: \(error.localizedDescription)")
                completion(error)
                return
            }
            let params = user.yy_modelToJSONObject() as? [String: Any]
            let scene = Scene(id: user.getRoomId(), userId: user.userId, isOwner: true, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: user.getRoomId()) { sceneRef in
                    showTo1v1Print("createUser success")
                    mainTreadTask {
                        self.sceneRefs[user.getRoomId()] = sceneRef
                        completion(nil)
                    }
                } fail: { error in
                    showTo1v1Print("createUser fail2: \(error.localizedDescription)")
                    mainTreadTask {
                        completion(error)
                    }
                }
            }) { error in
                showTo1v1Print("createUser fail3: \(error.localizedDescription)")
                mainTreadTask {
                    completion(error)
                }
            }
        }
    }
    
    func leaveRoom(completion: @escaping (Error?) -> Void) {
        self.sceneRefs[user?.getRoomId() ?? ""]?.deleteScenes()
        completion(nil)
    }
    
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (ShowTo1v1ServiceNetworkStatus) -> Void) {
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

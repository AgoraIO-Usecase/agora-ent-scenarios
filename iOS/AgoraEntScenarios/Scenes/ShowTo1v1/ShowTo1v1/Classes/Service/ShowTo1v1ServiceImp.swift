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

/// 房间内用户列表
private let kSceneId = "showTo1v1_3.6.0"
class ShowTo1v1ServiceImp: NSObject {
    private var appId: String = ""
    private var user: ShowTo1v1UserInfo?
    private var sceneRefs: [String: SceneReference] = [:]
    private var syncUtilsInited: Bool = false
    private var roomList: [ShowTo1v1RoomInfo] = []
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

private let kRobotRoomStartId = 2023000
private let kRobotUid = 2000000001
private let robotStreamURL = [
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/show/video/bot4.mp4",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/show/video/bot5.mp4",
    "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/release/show/video/bot6.mp4"
]
private let robotRoomIds = ["1", "2", "3"]
private let robotRoomOwnerHeaders = [
    "https://download.agora.io/demo/release/bot1.png"
]
extension ShowTo1v1ServiceImp: ShowTo1v1ServiceProtocol {
    private func getRobotRoomList() -> [ShowTo1v1RoomInfo] {
        var list = [ShowTo1v1RoomInfo]()
        //create fake room
        robotRoomIds.forEach { robotId in
            let room = ShowTo1v1RoomInfo()
            let userId = "\(kRobotUid)"
            room.roomName = "Smooth \(robotId)"
            room.roomId = "\((Int(robotId) ?? 1) + kRobotRoomStartId)"
            room.userId = userId
            room.userName = userId
            room.avatar = robotRoomOwnerHeaders[((Int(robotId) ?? 1) - 1) % robotRoomOwnerHeaders.count]
            room.createdAt = Int64(robotId) ?? 0
            list.append(room)
        }
        return list
    }
    
    func getRoomList(completion: @escaping ([ShowTo1v1RoomInfo]) -> Void) {
        initScene { [weak self] error in
            if let error = error {
                showTo1v1Print("getUserList fail1: \(error.localizedDescription)")
                completion([])
                return
            }
            self?.manager.getScenes(success: { results in
                guard let self = self else {return}
                showTo1v1Print("getUserList == \(results.compactMap { $0.toJson() })")

                let roomList = results.map({ info in
                    return ShowTo1v1RoomInfo.yy_model(withJSON: info.toJson())!
                })
                self.roomList = self.getRobotRoomList() + roomList
                completion(self.roomList)
            }, fail: { error in
                completion([])
            })
        }
    }
    
    func createRoom(completion: @escaping (Error?) -> Void) {
        guard let user = self.user, !roomList.contains(where: { $0.userId == user.userId }) else {
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
            let scene = Scene(id: user.userId, userId: user.userId, isOwner: true, property: params)
            self?.manager.createScene(scene: scene, success: {[weak self] in
                guard let self = self else {return}
                self.manager.joinScene(sceneId: user.userId) { sceneRef in
                    showTo1v1Print("createUser success")
                    mainTreadTask {
                        self.sceneRefs[user.userId] = sceneRef
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
        guard let user = user else {return}
        self.sceneRefs[user.userId]?.deleteScenes()
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

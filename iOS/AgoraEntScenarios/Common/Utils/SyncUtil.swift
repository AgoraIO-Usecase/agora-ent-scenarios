//
//  SyncUtil.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/3.
//

import UIKit
 import AgoraSyncManager

class SyncUtil: NSObject {
    private static var manager: AgoraSyncManager?
    override private init() {}
    private static var sceneRefs: [String: SceneReference] = .init()

    static func initSyncManager(sceneId: String, complete: @escaping SuccessBlockVoid) {
//        let config = AgoraSyncManager.RtmConfig(appId: KeyCenter.AppId,
//                                                channelName: sceneId)
//        manager = AgoraSyncManager(config: config, complete: { code in
//            if code == 0 {
//                print("SyncManager init success")
//            } else {
//                print("SyncManager init error")
//            }
//        })
        let config = AgoraSyncManager.RethinkConfig(appId: KeyCenter.AppId,
                                                    channelName: sceneId)
//        ToastView.showWait(text: "join Scene...", view: nil)
        manager = AgoraSyncManager(config: config, complete: { code in
//            ToastView.hidden()
            if code == 0 {
                print("SyncManager init success")
                complete()
            } else {
                print("SyncManager init error")
//                ToastView.show(text: "SyncManager 连接失败")
            }
        })
    }

    class func joinScene(id: String,
                         userId: String,
                         isOwner: Bool,
                         property: [String: Any]?,
                         success: SuccessBlockObj? = nil,
                         fail: FailBlock? = nil)
    {
        guard let manager = manager else { return }
        let jsonString = JSONObject.toJsonString(dict: property) ?? ""
        let scene = Scene(id: id, userId: userId, isOwner: isOwner, property: property)
        manager.createScene(scene: scene, success: {
            manager.joinScene(sceneId: id) { sceneRef in
                sceneRefs[id] = sceneRef
                let attr = Attribute(key: id, value: jsonString)
                success?(attr)
            } fail: { error in
                fail?(error)
            }
        }) { error in
            fail?(error)
        }
    }

    class func scene(id: String) -> SceneReference? {
        sceneRefs[id]
    }

    class func fetchAll(success: SuccessBlock? = nil, fail: FailBlock? = nil) {
        manager?.getScenes(success: success, fail: fail)
    }

    class func leaveScene(id: String) {
        manager?.leaveScene(roomId: id)
        sceneRefs.removeValue(forKey: id)
    }
    
    class func subscribeConnectState(state: @escaping ConnectBlockState) {
        manager?.subscribeConnectState(state: state)
    }
}


class SyncUtilsWrapper {
    static var syncUtilsInited: Bool = false
    static private var subscribeConnectStateMap: [String: (SocketConnectState?, Bool)->Void] = [:]
    static private var currentState: SocketConnectState = .connecting
    
    class func initScene(uniqueId: String, sceneId: String, completion: @escaping (SocketConnectState?, Bool)->Void) {
        let state: SocketConnectState? = subscribeConnectStateMap[uniqueId] == nil ? currentState : nil
        let inited: Bool = state == nil ? true : false
        subscribeConnectStateMap[uniqueId] = completion
        if syncUtilsInited {
            completion(state, inited)
            return
        }
        
        SyncUtil.initSyncManager(sceneId: sceneId) {
        }
        
        SyncUtil.subscribeConnectState { state in
            if currentState == state {
                return
            }
            currentState = state
            print("subscribeConnectState: \(state)")
            let inited = syncUtilsInited
            defer {
                subscribeConnectStateMap.forEach { (key: String, value: (SocketConnectState, Bool) -> Void) in
                    value(state, inited)
                }
            }
            
            guard state == .open else { return }
            guard !syncUtilsInited else {
                return
            }
            
            syncUtilsInited = true
        }
    }
    
    class func cleanScene(uniqueId: String) {
        subscribeConnectStateMap.removeValue(forKey: uniqueId)
    }
    
    class func cleanScene() {
        syncUtilsInited = false
        currentState = .connecting
        subscribeConnectStateMap.removeAll()
    }
}

//
//  SyncUtil.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/3.
//

import UIKit
import AgoraSyncManager

public class SyncUtil: NSObject {
    private static var manager: AgoraSyncManager?
    override private init() {
        super.init()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private static var sceneRefs: [String: SceneReference] = .init()

    public static func initSyncManager(sceneId: String, complete: @escaping SuccessBlockVoid) {
//        let config = AgoraSyncManager.RtmConfig(appId: KeyCenter.AppId,
//                                                channelName: sceneId)
//        manager = AgoraSyncManager(config: config, complete: { code in
//            if code == 0 {
//                print("SyncManager init success")
//            } else {
//                print("SyncManager init error")
//            }
//        })
        let config = AgoraSyncManager.RethinkConfig(appId: AppContext.shared.appId,
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

    public class func joinScene(id: String,
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
                mainTreadTask {
                    success?(attr)
                }
            } fail: { error in
                mainTreadTask {
                    fail?(error)
                }
            }
        }) { error in
            mainTreadTask {
                fail?(error)
            }
        }
    }
    
    class func mainTreadTask(_ task: (()->())?){
        if Thread.isMainThread {
            task?()
        }else{
            DispatchQueue.main.async {
                task?()
            }
        }
    }

   public class func scene(id: String) -> SceneReference? {
        sceneRefs[id]
    }

    public class func fetchAll(success: SuccessBlock? = nil, fail: FailBlock? = nil) {
        manager?.getScenes(success: success, fail: fail)
    }

    public class func leaveScene(id: String) {
        manager?.leaveScene(roomId: id)
        sceneRefs.removeValue(forKey: id)
    }
    
    public class func subscribeConnectState(state: @escaping ConnectBlockState) {
        manager?.subscribeConnectState(state: state)
    }
    
    public class func reset() {
        manager = nil
        sceneRefs.forEach({ $0.value.unsubscribe(key: $0.key) })
        sceneRefs.removeAll()
    }
}


public class SyncUtilsWrapper {
    static var syncUtilsInited: Bool = false
    static private var subscribeConnectStateMap: [String: (SocketConnectState, Bool)->Void] = [:]
    static private var joinSceneQueue: [()->()] = []
    static private var timer: Timer?
    static private var currentState: SocketConnectState = .connecting
    
    public class func initScene(uniqueId: String,
                         sceneId: String,
                         statusSubscribeCallback: @escaping (SocketConnectState, Bool)->Void) {
        subscribeConnectStateMap[uniqueId] = statusSubscribeCallback
        if syncUtilsInited {
            statusSubscribeCallback(currentState, syncUtilsInited)
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
    
    private class func _resetTimer() {
        timer?.invalidate()
        timer = nil
    }
    
    private  class func _dequeueJoinScene() {
        guard timer == nil, let first = joinSceneQueue.first else {
            return
        }
        
        _resetTimer()
        timer = Timer.scheduledTimer(withTimeInterval: 5, repeats: false, block: { timer in
        #if DEBUG
            print("joinSceneByQueue timeout")
        #endif
            _resetTimer()
            _dequeueJoinScene()
        })
        first()
    #if DEBUG
        print("joinSceneByQueue: \(joinSceneQueue.count)")
    #endif
    }
    
    public class func joinSceneByQueue(id: String,
                                userId: String,
                                isOwner: Bool,
                                property: [String: Any]?,
                                success: SuccessBlockObj? = nil,
                                fail: FailBlock? = nil) {
//        if isOwner == false {
//            SyncUtil.joinScene(id: id, userId: userId, isOwner: isOwner, property: property, success: success, fail:fail)
//            return
//        }
        
        //TODO: syncmanager does not support parallel calls 'create'
        joinSceneQueue.append({
            SyncUtil.joinScene(id: id, userId: userId, isOwner: isOwner, property: property) { obj in
                _resetTimer()
                success?(obj)
                if joinSceneQueue.count > 0 {
                    joinSceneQueue.removeFirst()
                }
                _dequeueJoinScene()
            } fail: { err in
                _resetTimer()
                fail?(err)
                if joinSceneQueue.count > 0 {
                    joinSceneQueue.removeFirst()
                }
                _dequeueJoinScene()
            }
        })
        _dequeueJoinScene()
    }
    
    public class func cleanScene(uniqueId: String) {
        subscribeConnectStateMap.removeValue(forKey: uniqueId)
    }
    
    public class func cleanScene() {
        syncUtilsInited = false
        currentState = .connecting
        subscribeConnectStateMap.removeAll()
        joinSceneQueue.removeAll()
        _resetTimer()
    }
}

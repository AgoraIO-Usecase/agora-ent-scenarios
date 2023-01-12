//
//  SyncUtil.swift
//  BreakoutRoom
//
//  Created by zhaoyongqiang on 2021/11/3.
//

import UIKit
 import AgoraSyncManager

class SyncUtil: NSObject {
    private static var imp: SyncUtilImp?

    static func initSyncManager(sceneId: String, complete: @escaping SuccessBlockVoid) {
        imp = SyncUtilImp()
        imp?.initSyncManager(sceneId: sceneId, complete: complete)
    }

    class func joinScene(id: String,
                         userId: String,
                         isOwner: Bool,
                         property: [String: Any]?,
                         success: SuccessBlockObj? = nil,
                         fail: FailBlock? = nil)
    {
        imp?.joinScene(id: id, userId: userId, isOwner: isOwner, property: property, success: success, fail: fail)
    }

    class func scene(id: String) -> SceneReference? {
        imp?.scene(id: id)
    }

    class func fetchAll(success: SuccessBlock? = nil, fail: FailBlock? = nil) {
        imp?.fetchAll(success: success, fail: fail)
    }

    class func leaveScene(id: String) {
        imp?.leaveScene(id: id)
    }
    
    class func subscribeConnectState(state: @escaping ConnectBlockState) {
        imp?.subscribeConnectState(state: state)
    }
}


class SyncUtilImp: NSObject {
    private var manager: AgoraSyncManager?
//    override private init() {}
    private var sceneRefs: [String: SceneReference] = .init()

    func initSyncManager(sceneId: String, complete: @escaping SuccessBlockVoid) {
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

    func joinScene(id: String,
                   userId: String,
                   isOwner: Bool,
                   property: [String: Any]?,
                   success: SuccessBlockObj? = nil,
                   fail: FailBlock? = nil) {
        guard let manager = manager else { return }
        let jsonString = JSONObject.toJsonString(dict: property) ?? ""
        let scene = Scene(id: id, userId: userId, isOwner: isOwner, property: property)
        manager.createScene(scene: scene, success: {
            manager.joinScene(sceneId: id) { sceneRef in
                self.sceneRefs[id] = sceneRef
                let attr = Attribute(key: id, value: jsonString)
                success?(attr)
            } fail: { error in
                fail?(error)
            }
        }) { error in
            fail?(error)
        }
    }

    func scene(id: String) -> SceneReference? {
        sceneRefs[id]
    }

    func fetchAll(success: SuccessBlock? = nil, fail: FailBlock? = nil) {
        manager?.getScenes(success: success, fail: fail)
    }

    func leaveScene(id: String) {
        sceneRefs.removeValue(forKey: id)
    }
    
    func subscribeConnectState(state: @escaping ConnectBlockState) {
        manager?.subscribeConnectState(state: state)
    }
}

//
//  AUIScene.swift
//  FBSnapshotTestCase
//
//  Created by wushengtao on 2024/1/22.
//

import Foundation
import AgoraRtmKit

let kSceneTag = "AUIScene"
let kRoomInfoKey = "scene_room_info"
let kRoomInfoRoomId = "room_id"
let kRoomInfoRoomOwnerId = "room_owner_id"
let kRoomCreateTime = "room_create_time"
let kRoomInfoPayloadId = "room_payload_id"
public class AUIScene: NSObject {
    private var channelName: String
    public let userService: AUIUserServiceImpl
    public let arbiter: AUIArbiter
    private var removeClosure: ()->()
    private var rtmManager: AUIRtmManager
    private var enterCondition: AUISceneEnterCondition
    private var expireCondition: AUISceneExpiredCondition
    private var collectionMap: [String: IAUICollection] = [:]
    private lazy var roomCollection: AUIMapCollection = getCollection(key: kRoomInfoKey)!
    private var roomPayload: [String: Any]?
    private var enterRoomCompletion: (([String: Any]?, NSError?)-> ())?
    private var respDelegates = NSHashTable<AUISceneRespDelegate>.weakObjects()
    private var subscribeDate: Date?
    
    deinit {
        aui_info("deinit AUIScene[\(channelName)] \(self)")
        userService.unbindRespDelegate(delegate: self)
    }
    
    public required init(channelName: String, 
                         rtmManager: AUIRtmManager,
                         roomExpiration: RoomExpirationPolicy, 
                         removeClosure:@escaping ()->()) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        self.removeClosure = removeClosure
        self.userService = AUIUserServiceImpl(channelName: channelName, rtmManager: rtmManager)
        self.arbiter = AUIArbiter(channelName: channelName, rtmManager: rtmManager, userInfo: AUIRoomContext.shared.currentUserInfo)
        AUIRoomContext.shared.roomArbiterMap[channelName] = self.arbiter
        self.enterCondition = AUISceneEnterCondition(channelName: channelName, arbiter: self.arbiter)
        self.expireCondition = AUISceneExpiredCondition(channelName: channelName, roomExpiration: roomExpiration)
        super.init()
        aui_info("init AUIScene[\(channelName)] \(self)")
        userService.bindRespDelegate(delegate: self)
        
        self.enterCondition.enterCompletion = { [weak self] in
            guard let self = self else {return}
            self.enterRoomCompletion?(self.roomPayload, nil)
            self.enterRoomCompletion = nil
        }
        
        self.expireCondition.roomDidExpired = { [weak self] in
            guard let self = self else {return}
            
            for obj in self.respDelegates.allObjects {
                obj.onSceneExpire?(channelName: channelName)
            }
            
            //房主才移除
            guard AUIRoomContext.shared.isRoomOwner(channelName: channelName) else {return}
            self.cleanScene()
        }
    }
    
    public func bindRespDelegate(delegate: AUISceneRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUISceneRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    
    public func create(createTime: Int64, 
                       payload: [String: Any]?,
                       completion:@escaping (NSError?)->()) {
        create(createTime: createTime,
               ownerId: AUIRoomContext.shared.currentUserInfo.userId,
               payload: payload,
               completion: completion)
    }
    
    //TODO: 是否需要像UIKit一样传入一个房间信息对象，还是这个对象业务上自己创建map collection来写入
    public func create(createTime: Int64, 
                       ownerId: String,
                       payload: [String: Any]?,
                       completion:@escaping (NSError?)->()) {
        aui_info("create[\(channelName)] with payload \(payload ?? [:])", tag: kSceneTag)
        
        guard rtmManager.isLogin else {
            aui_error("create[\(channelName)] fail! not login", tag: kSceneTag)
            completion(NSError.auiError("create fail! not login"))
            return
        }
        
        var roomInfo = [
            kRoomInfoRoomId: channelName,
            kRoomInfoRoomOwnerId: ownerId,
            kRoomCreateTime: "\(createTime)"
        ]
        if let payload = payload {
            roomInfo[kRoomInfoPayloadId] = encodeToJsonStr(payload)
        }
        
        let date = Date()
        
        for obj in self.respDelegates.allObjects {
            let collectionDataMap = obj.onWillInitSceneMetadata?(channelName: channelName)
            collectionDataMap?.forEach({ key, value in
                if let metadata = value as? [String: Any] {
                    let collection: AUIMapCollection? = getCollection(key: key)
                    collection?.initMetaData(channelName: channelName, metadata: metadata, fetchImmediately: false, completion: { err in
                        
                    })
                } else if let metadata = value as? [[String: Any]] {
                    let collection: AUIListCollection? = getCollection(key: key)
                    collection?.initMetaData(channelName: channelName, metadata: metadata, fetchImmediately: false, completion: { err in
                    })
                } else {
                    aui_warn("init meta data fail[\(channelName)] key: \(key) value: \(value)")
                }
            })
        }
        
        roomCollection.initMetaData(channelName: channelName,
                                    metadata: roomInfo,
                                    fetchImmediately: true) { err in
            aui_benchmark("rtm initMetaData", cost: -date.timeIntervalSinceNow, tag: kSceneTag)
            if let err = err {
                completion(err)
                return
            }
            completion(nil)
        }
        userService.setUserPayload(payload: UUID().uuidString)
        getArbiter().create()
    }
    
    public func enter(completion:@escaping ([String: Any]?, NSError?)->()) {
        aui_info("enter[\(channelName)]", tag: kSceneTag)
        guard rtmManager.isLogin else {
            aui_error("enter fail! not login", tag: kSceneTag)
            completion(nil, NSError.auiError("enter fail! not login"))
            return
        }
        
        let date = Date()
        subscribeDate = date
        self.expireCondition.joinCompletion = false
        self.enterRoomCompletion = {[weak self] payload, err in
            if let err = err {
                aui_error("enterRoomCompletion fail: \(err.localizedDescription)", tag: kSceneTag)
            } else {
                aui_info("[Benchmark]enterRoomCompletion: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSceneTag)
            }
            self?.expireCondition.joinCompletion = true
            completion(payload, err)
        }
        
        if self.enterCondition.ownerId.isEmpty {
            roomCollection.getMetaData {[weak self] err, metadata in
                guard let self = self else {return}
                if let err = err {
                    self._notifyError(error: err)
                    return
                }
                guard let map = metadata as? [String: String],
                      let ownerId = map[kRoomInfoRoomOwnerId],
                      let createTimestemp = UInt64(map[kRoomCreateTime] ?? "") else {
//                    self.ownerId = "owner unknown"
                    //如果没有获取到user信息，认为房间有问题
                    self.cleanScene()
                    self._notifyError(error: NSError(domain: "get room owner fatel!", code: -1))
                    self.onMsgRecvEmpty(channelName: self.channelName)
                    aui_error("get room owner fatel!")
                    return
                }
                aui_info("getMetaData[\(ownerId)] in enter success")
                if let payloadStr = map[kRoomInfoPayloadId] {
                    self.roomPayload = decodeToJsonObj(payloadStr) as? [String: Any]
                    aui_info("getMetaData[\(ownerId)] in enter success with payload: \(payloadStr)", tag: kSceneTag)
                }
                self.enterCondition.ownerId = ownerId
                self.expireCondition.createTimestemp = createTimestemp
            }
        }
//        getArbiter().create()
        getArbiter().acquire {[weak self] err in
            //fail 走onError(channelName: String, error: NSError)，这里不处理
            if let _ = err {return}
            self?.enterCondition.lockOwnerAcquireSuccess = true
        }
        rtmManager.subscribeError(channelName: channelName, delegate: self)
        getArbiter().subscribeEvent(delegate: self)
        rtmManager.subscribe(channelName: channelName) {[weak self] error in
            guard let self = self else { return }
            if let error = error, error.code != AgoraRtmErrorCode.duplicateOperation.rawValue {
                aui_error("enterRoom subscribe fail: \(error.localizedDescription)")
                self._notifyError(error: error)
                return
            }
            aui_benchmark("[Benchmark]rtm manager subscribe", cost: -(date.timeIntervalSinceNow), tag: kSceneTag)
            aui_info("enterRoom subscribe finished \(channelName) \(error?.localizedDescription ?? "")", tag: kSceneTag)
            self.enterCondition.subscribeSuccess = true
            self.userService.setUserAttr { _ in
                //TODO: error to retry?
            }
        }
    }
    
    /// 离开scene
    public func leave() {
        aui_info("leave[\(channelName)]", tag: kSceneTag)
        getArbiter().release()
        cleanSDK()
        AUIRoomContext.shared.clean(channelName: channelName)
        removeClosure()
    }
    
    /// 销毁scene，清理所有缓存（包括rtm的所有metadata）
    public func delete() {
        aui_info("delete[\(channelName)]", tag: kSceneTag)
        cleanScene(forceClean: true)
        getArbiter().destroy()
        cleanSDK()
        AUIRoomContext.shared.clean(channelName: channelName)
        removeClosure()
    }
    
    /// 获取一个collection，例如let collection: AUIMapCollection = scene.getCollection("musicList")
    /// - Parameter key: <#sceneKey description#>
    /// - Returns: <#description#>
    public func getCollection<T: IAUICollection>(key: String) -> T? {
        if let collection = collectionMap[key] {
            return collection as? T
        }
        
        let scene = T.init(channelName: channelName, observeKey: key, rtmManager: rtmManager)
        collectionMap[key] = scene
        return scene
    }
    
    public func getRoomDuration() -> UInt64 {
        return expireCondition.roomUsageDuration() ?? 0
    }
    
    public func getCurrentTs() -> UInt64 {
        return expireCondition.roomCurrentTs() ?? 0
    }
}

//MARK: private
extension AUIScene {
    private func _notifyError(error: NSError) {
        aui_error("join[\(channelName)] fail: \(error.localizedDescription)")
        if let completion = self.enterRoomCompletion {
            completion(nil, error)
            self.enterRoomCompletion = nil
        }
    }
    
    private func getArbiter() -> AUIArbiter {
        return arbiter
    }
    
    private func cleanUserInfo(userId: String) {
        //TODO: 用户离开后，需要清理这个用户对应在collection里的信息，例如上麦信息、点歌信息等
    }
    
    private func cleanScene(forceClean: Bool = false) {
        aui_info("cleanScene[\(channelName)]", tag: kSceneTag)
        guard getArbiter().isArbiter() || forceClean else {
            return
        }
        
        _cleanScene()
    }
    
    private func _cleanScene() {
        aui_info("_cleanScene[\(channelName)]", tag: kSceneTag)
        
        //每个collection都清空，让所有人收到onMsgRecvEmpty
        rtmManager.cleanAllMedadata(channelName: channelName, lockName: "") { error in
            aui_info("cleanScene completion: \(error?.localizedDescription ?? "success")", tag: kSceneTag)
        }
        getArbiter().destroy()
    }
    
    private func cleanSDK() {
        aui_info("cleanSDK[\(channelName)]", tag: kSceneTag)
        rtmManager.unSubscribe(channelName: channelName)
        rtmManager.unsubscribeError(channelName: channelName, delegate: self)
        getArbiter().unSubscribeEvent(delegate: self)
        //TODO: syncmanager 需要logout
//        rtmManager.logout()
    }
}

//MARK: AUIRtmLockProxyDelegate
extension AUIScene: AUIArbiterDelegate {
    public func onArbiterDidChange(channelName: String, arbiterId: String) {
        aui_benchmark("onArbiterDidChange[\(channelName)] arbiterId: \(arbiterId)", cost: -(subscribeDate?.timeIntervalSinceNow ?? 0), tag: kSceneTag)
        if arbiterId.isEmpty {return}
        self.enterCondition.lockOwnerRetrived = true
    }
    
    public func onError(channelName: String, error: NSError) {
        aui_info("onError[\(channelName)]: \(error.localizedDescription)", tag: kSceneTag)
        //如果锁不存在，也认为是房间被销毁的一种
        if error.code == AgoraRtmErrorCode.lockNotExist.rawValue {
            cleanScene()
//            self.onMsgRecvEmpty(channelName: channelName)
        }
        _notifyError(error: error)
    }
}

//MARK: AUIUserRespDelegate
extension AUIScene: AUIUserRespDelegate {
    public func onUserBeKicked(roomId: String, userId: String) {
        aui_info("onUserBeKicked[\(roomId)] userId: \(userId)", tag: kSceneTag)
    }
    
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        self.expireCondition.userSnapshotList = userList
        
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot[\(roomId)]", tag: kSceneTag)
        if AUIRoomContext.shared.isRoomOwner(channelName: roomId) {
            self.expireCondition.ownerHasLeftRoom = user.customPayload == nil ? true : false
        }
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
        onUserVideoMute(userId: user.userId, mute: user.muteVideo)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter[\(roomId)] userId: \(userInfo.userId)", tag: kSceneTag)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        aui_info("onRoomUserLeave[\(roomId)] userId: \(userInfo.userId)", tag: kSceneTag)
        guard AUIRoomContext.shared.isRoomOwner(channelName: roomId, userId: userInfo.userId) else {
            cleanUserInfo(userId: userInfo.userId)
            return
        }
        cleanScene()
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserUpdate[\(roomId)] userId: \(userInfo.userId)", tag: kSceneTag)
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
    }
}

//MARK: AUIRtmErrorProxyDelegate
extension AUIScene: AUIRtmErrorProxyDelegate {
    public func onTimeStampsDidUpdate(timeStamp: UInt64) {
        if expireCondition.lastUpdateTimestemp == nil {
            expireCondition.lastUpdateTimestemp = timeStamp
        }
    }
    
    public func onTokenPrivilegeWillExpire(channelName: String?) {
        aui_info("onTokenPrivilegeWillExpire: \(channelName ?? "")", tag: kSceneTag)
        for obj in self.respDelegates.allObjects {
            obj.onTokenPrivilegeWillExpire?(channelName: channelName)
        }
    }
    
    @objc public func onMsgRecvEmpty(channelName: String) {
        aui_info("onMsgRecvEmpty[\(channelName)]", tag: kSceneTag)
        //TODO: 某个scene里拿到全空数据，定义为房间被销毁了
        self.respDelegates.allObjects.forEach { obj in
            obj.onSceneDestroy?(channelName: channelName)
        }
    }
    
    @objc public func didReceiveLinkStateEvent(event: AgoraRtmLinkStateEvent) {
        aui_info("didReceiveLinkStateEvent state: \(event.currentState.rawValue), reason: \(event.reason ?? "")", tag: kSceneTag)
        if event.currentState == .connected, event.operation == .reconnected {
            //TODO: 推荐重连后lock的snapshot来获取
            getArbiter().acquire()
        }

        if event.currentState == .failed {
            for obj in self.respDelegates.allObjects {
                obj.onSceneFailed?(channelName: channelName, reason: event.reason ?? "")
            }
        }
    }
}

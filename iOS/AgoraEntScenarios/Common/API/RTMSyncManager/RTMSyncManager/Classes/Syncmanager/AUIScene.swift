//
//  AUIScene.swift
//  FBSnapshotTestCase
//
//  Created by wushengtao on 2024/1/22.
//

import Foundation
import AgoraRtmKit

let kSceneTag = "AUIScene"
private let kRoomInfoKey = "scene_room_info"
private let kRoomInfoRoomId = "room_id"
private let kRoomInfoRoomOwnerId = "room_owner_id"
private let kRoomInfoPayloadId = "room_payload_id"
public class AUIScene: NSObject {
    private var channelName: String
    private var ownerId: String = "" {
        didSet {
            aui_info("set ownerId: \(ownerId)", tag: kSceneTag)
            AUIRoomContext.shared.roomOwnerMap[channelName] = ownerId
            checkRoomValid()
        }
    }
    private var rtmManager: AUIRtmManager
    private var collectionMap: [String: IAUICollection] = [:]
    public let userService: AUIUserServiceImpl!
    private lazy var roomCollection: AUIMapCollection = getCollection(key: kRoomInfoKey)!
    
    private var enterRoomCompletion: (([String: Any]?, NSError?)-> ())?
    private var respDelegates: NSHashTable<AUISceneRespDelegate> = NSHashTable<AUISceneRespDelegate>.weakObjects()
    private var roomPayload: [String: Any]?
    private var subscribeDate: Date?
    private var lockRetrived: Bool = false {
        didSet {
            aui_info("set lockRetrived = \(lockRetrived)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    private var subscribeSuccess: Bool = false {
        didSet {
            aui_info("set subscribeSuccess = \(subscribeSuccess)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    private var userSnapshotList: [AUIUserInfo]? {
        didSet {
            aui_info("set userSnapshotList count = \(userSnapshotList?.count ?? 0)", tag: kSceneTag)
            checkRoomValid()
        }
    }
    
    deinit {
        userService.unbindRespDelegate(delegate: self)
    }
    
    public required init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        self.userService = AUIUserServiceImpl(channelName: channelName, rtmManager: rtmManager)
        super.init()
        userService.bindRespDelegate(delegate: self)
    }
    
    public func bindRespDelegate(delegate: AUISceneRespDelegate) {
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUISceneRespDelegate) {
        respDelegates.remove(delegate)
    }
    
    //TODO: 是否需要像UIKit一样传入一个房间信息对象，还是这个对象业务上自己创建map collection来写入
    public func create(payload: [String: Any]?, completion:@escaping (NSError?)->()) {
        aui_info("create with channelName: \(channelName), with payload \(payload ?? [:])", tag: kSceneTag)
        
        guard rtmManager.isLogin else {
            aui_error("create fail! not login", tag: kSceneTag)
            completion(NSError.auiError("create fail! not login"))
            return
        }
        let ownerId = AUIRoomContext.shared.currentUserInfo.userId
        var roomInfo = [
            kRoomInfoRoomId: channelName,
            kRoomInfoRoomOwnerId: ownerId
        ]
        if let payload = payload {
            roomInfo[kRoomInfoPayloadId] = encodeToJsonStr(payload)
        }
        
        let date = Date()
        roomCollection.initMetaData(channelName: channelName,
                                    metadata: roomInfo) { err in
            aui_benchmark("rtm setMetaData", cost: -date.timeIntervalSinceNow, tag: kSceneTag)
            if let err = err {
                completion(err)
                return
            }
            completion(nil)
        }
        getArbiter().create()
    }
    
    public func enter(completion:@escaping ([String: Any]?, NSError?)->()) {
        aui_info("enter with channelName: \(channelName)", tag: kSceneTag)
        guard rtmManager.isLogin else {
            aui_error("enter fail! not login", tag: kSceneTag)
            completion(nil, NSError.auiError("enter fail! not login"))
            return
        }
        
        let date = Date()
        subscribeDate = date
        self.enterRoomCompletion = { payload, err in
            if let err = err {
                aui_error("enterRoomCompletion fail: \(err.localizedDescription)", tag: kSceneTag)
            } else {
                aui_info("[Benchmark]enterRoomCompletion: \(Int64(-date.timeIntervalSinceNow * 1000)) ms", tag: kSceneTag)
            }
            completion(payload, err)
        }
        
        if self.ownerId.isEmpty {
            roomCollection.getMetaData {[weak self] err, metadata in
                guard let self = self else {return}
                if let err = err {
                    self._notifyError(error: err)
                    return
                }
                guard let map = metadata as? [String: Any],
                      let ownerId = map[kRoomInfoRoomOwnerId] as? String else {
//                    self.ownerId = "owner unknown"
                    //如果没有获取到user信息，认为房间有问题
                    self._cleanScene()
                    self._notifyError(error: NSError(domain: "get room owner fatel!", code: -1))
                    self.onMsgRecvEmpty(channelName: self.channelName)
                    aui_error("get room owner fatel!")
                    return
                }
                aui_info("getMetaData[\(ownerId)] in enter success")
                if let payloadStr = map[kRoomInfoPayloadId] as? String {
                    self.roomPayload = decodeToJsonObj(payloadStr) as? [String: Any]
                    aui_info("getMetaData[\(ownerId)] in enter success with payload: \(payloadStr)", tag: kSceneTag)
                }
                self.ownerId = ownerId
            }
        }
//        getArbiter().create()
        getArbiter().acquire()
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
            self.subscribeSuccess = true
        }
    }
    
    /// 离开scene
    public func leave() {
        aui_info("leave", tag: kSceneTag)
        getArbiter().release()
        cleanSDK()
        AUIRoomContext.shared.clean(channelName: channelName)
    }
    
    /// 销毁scene，清理所有缓存（包括rtm的所有metadata）
    public func delete() {
        aui_info("delete", tag: kSceneTag)
        cleanScene()
        getArbiter().destroy()
        cleanSDK()
        AUIRoomContext.shared.clean(channelName: channelName)
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
}

//MARK: private
extension AUIScene {
    private func _notifyError(error: NSError) {
        aui_error("join fail: \(error.localizedDescription)")
        if let completion = self.enterRoomCompletion {
            completion(nil, error)
            self.enterRoomCompletion = nil
        }
    }
    
    private func getArbiter() -> AUIArbiter {
        if let arbiter = AUIRoomContext.shared.roomArbiterMap[channelName] {
            return arbiter
        }
        let arbiter = AUIArbiter(channelName: channelName, rtmManager: rtmManager, userInfo: AUIRoomContext.shared.currentUserInfo)
        AUIRoomContext.shared.roomArbiterMap[channelName] = arbiter
        
        return arbiter
    }
    
    //如果subscribe成功、锁也获取到、用户列表也获取到，可以检查是否是脏房间并且清理
    private func checkRoomValid() {
        aui_info("checkRoomValid subscribeSuccess: \(subscribeSuccess), lockRetrived: \(lockRetrived), ownerId: \(ownerId)", tag: kSceneTag)
        guard subscribeSuccess, lockRetrived, !ownerId.isEmpty else { return }
        if let completion = self.enterRoomCompletion {
            completion(roomPayload, nil)
            self.enterRoomCompletion = nil
        }
        
        aui_info("checkRoomValid userSnapshotList count: \(userSnapshotList?.count ?? 0)", tag: kSceneTag)
        guard let userList = userSnapshotList else { return }
        guard let _ = userList.filter({ AUIRoomContext.shared.isRoomOwner(channelName: channelName, userId: $0.userId)}).first else {
            //room owner not found, clean room
            cleanScene()
            return
        }
    }
    
    private func cleanUserInfo(userId: String) {
        //TODO: 用户离开后，需要清理这个用户对应在collection里的信息，例如上麦信息、点歌信息等
    }
    
    private func cleanScene() {
        aui_info("cleanScene", tag: kSceneTag)
        guard getArbiter().isArbiter() else {
            return
        }
        
        _cleanScene()
    }
    
    private func _cleanScene() {
        aui_info("_cleanScene", tag: kSceneTag)
        
        //每个collection都清空，让所有人收到onMsgRecvEmpty
        rtmManager.cleanAllMedadata(channelName: channelName, lockName: "") { error in
            aui_info("cleanScene completion: \(error?.localizedDescription ?? "success")", tag: kSceneTag)
        }
        getArbiter().destroy()
    }
    
    private func cleanSDK() {
        aui_info("cleanSDK", tag: kSceneTag)
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
        aui_benchmark("onArbiterDidChange", cost: -(subscribeDate?.timeIntervalSinceNow ?? 0), tag: kSceneTag)
        if arbiterId.isEmpty {return}
        self.lockRetrived = true
    }
    
    public func onError(channelName: String, error: NSError) {
        aui_info("onError: \(error.localizedDescription)", tag: kSceneTag)
        //如果锁不存在，也认为是房间被销毁的一种
        if error.code == AgoraRtmErrorCode.lockNotExist.rawValue {
            _cleanScene()
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
        self.userSnapshotList = userList
        
        guard let user = userList.filter({$0.userId == AUIRoomContext.shared.currentUserInfo.userId }).first else {return}
        aui_info("onRoomUserSnapshot", tag: kSceneTag)
        onUserAudioMute(userId: user.userId, mute: user.muteAudio)
        onUserVideoMute(userId: user.userId, mute: user.muteVideo)
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        aui_info("onRoomUserEnter[\(roomId)] userId: \(userInfo.userId)", tag: kSceneTag)
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo) {
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
//        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
//        aui_info("onUserAudioMute mute current user: \(mute)", tag: kSertviceTag)
//        rtcEngine.adjustRecordingSignalVolume(mute ? 0 : 100)
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
//        guard userId == AUIRoomContext.shared.currentUserInfo.userId else {return}
//        aui_info("onMuteVideo onUserVideoMute [\(userId)]: \(mute)", tag: kSertviceTag)
//        rtcEngine.enableLocalVideo(!mute)
//        let option = AgoraRtcChannelMediaOptions()
//        option.publishCameraTrack = !mute
//        rtcEngine.updateChannel(with: option)
    }
}

//MARK: AUIRtmErrorProxyDelegate
extension AUIScene: AUIRtmErrorProxyDelegate {
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
            obj.onSceneDestroy?(roomId: channelName)
        }
    }
    
    @objc public func onConnectionStateChanged(channelName: String,
                                               connectionStateChanged state: AgoraRtmClientConnectionState,
                                               result reason: AgoraRtmClientConnectionChangeReason) {
        aui_info("onConnectionStateChanged[\(channelName)] state: \(state.rawValue), reason: \(reason.rawValue)", tag: kSceneTag)
        if reason == .changedRejoinSuccess {
            getArbiter().acquire()
        }
        guard state == .failed, reason == .changedBannedByServer else {
            return
        }
        
        for obj in self.respDelegates.allObjects {
            obj.onSceneUserBeKicked?(roomId: channelName, userId: AUIRoomContext.shared.currentUserInfo.userId)
        }
    }
}

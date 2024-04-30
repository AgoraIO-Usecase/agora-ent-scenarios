//  AUIUserServiceImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation
import AgoraRtmKit
import YYModel

@objc open class AUIUserServiceImpl: NSObject {
    public var userList: [AUIUserInfo] = []
    private var respDelegates: NSHashTable<AnyObject> = NSHashTable<AnyObject>.weakObjects()
    private var channelName: String!
    private let rtmManager: AUIRtmManager!
    
    deinit {
        aui_info("deinit AUIUserServiceImpl", tag: "AUIUserServiceImpl")
        rtmManager.unsubscribeUser(channelName: channelName, delegate: self)
    }
    
    public init(channelName: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.channelName = channelName
        super.init()
        self.rtmManager.subscribeUser(channelName: channelName, delegate: self)
        aui_info("init AUIUserServiceImpl", tag: "AUIUserServiceImpl")
    }
}

//MARK: AUIRtmUserProxyDelegate
extension AUIUserServiceImpl: AUIRtmUserProxyDelegate {
    public func onCurrentUserJoined(channelName: String) {
        guard channelName == self.channelName else {return}
        aui_info("onCurrentUserJoined", tag: "AUIUserServiceImpl")
        _setupUserAttr(roomId: channelName) { error in
            //TODO: retry if fail
        }
    }
    
    public func onUserDidUpdated(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidUpdated: \(userId)", tag: "AUIUserServiceImpl")
        let user = AUIUserInfo.yy_model(withJSON: userInfo)!
        user.userId = userId
        if let oldUser = self.userList.first(where: {$0.userId == userId}) {
            if oldUser.muteAudio != user.muteAudio {
                oldUser.muteAudio = user.muteAudio
                self.respDelegates.allObjects.forEach { obj in
                    guard let obj = obj as? AUIUserRespDelegate else {return}
                    obj.onUserAudioMute(userId: userId, mute: user.muteAudio)
                }
            }
            
            if oldUser.muteVideo != user.muteVideo {
                oldUser.muteVideo = user.muteVideo
                self.respDelegates.allObjects.forEach { obj in
                    guard let obj = obj as? AUIUserRespDelegate else {return}
                    obj.onUserVideoMute(userId: userId, mute: user.muteVideo)
                }
            }
        }
        
        if let idx = self.userList.firstIndex(where: {$0.userId == userId}) {
            self.userList.replaceSubrange(idx...idx, with: [user])
        } else {
            self.userList.append(user)
        }
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUIUserRespDelegate else {return}
            obj.onRoomUserUpdate(roomId: channelName, userInfo: user)
        }
    }
    
    public func onUserSnapshotRecv(channelName: String, userId: String, userList: [[String : Any]]) {
        aui_info("onUserSnapshotRecv: \(userId)", tag: "AUIUserServiceImpl")
        guard let users = NSArray.yy_modelArray(with: AUIUserInfo.self, json: userList) as? [AUIUserInfo] else {
            assert(false, "onUserSnapshotRecv recv fail")
            return
        }
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUIUserRespDelegate else {return}
            self.userList = users
            obj.onRoomUserSnapshot(roomId: channelName, userList: users)
        }
    }
    
    public func onUserDidJoined(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidJoined: \(userId)", tag: "AUIUserServiceImpl")
        let user = AUIUserInfo.yy_model(withJSON: userInfo)!
        user.userId = userId
        self.userList.append(user)
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUIUserRespDelegate else {return}
            obj.onRoomUserEnter(roomId: channelName, userInfo: user)
        }
    }
    
    public func onUserDidLeaved(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidLeaved: \(userId)", tag: "AUIUserServiceImpl")
        let user = userList.filter({$0.userId == userId}).first ?? AUIUserInfo.yy_model(withJSON: userInfo)!
        self.userList = userList.filter({$0.userId != userId})
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUIUserRespDelegate else {return}
            obj.onRoomUserLeave(roomId: channelName, userInfo: user)
        }
    }
    
    public func onUserBeKicked(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserBeKicked: \(userId)", tag: "AUIUserServiceImpl")
        let user = userList.filter({$0.userId == userId}).first ?? AUIUserInfo.yy_model(withJSON: userInfo)!
        self.userList = userList.filter({$0.userId != userId})
        self.respDelegates.allObjects.forEach { obj in
            guard let obj = obj as? AUIUserRespDelegate else {return}
            obj.onUserBeKicked(roomId: channelName, userId: user.userId)
        }
    }
}

extension AUIUserServiceImpl: AUIUserServiceDelegate {
    
    public func getRoomContext() -> AUIRoomContext {
        return AUIRoomContext.shared
    }
    
    public func getChannelName() -> String {
        return channelName
    }
    
    public func bindRespDelegate(delegate: AUIUserRespDelegate) {
        aui_info("bindRespDelegate \(delegate)", tag: "AUIUserServiceImpl")
        respDelegates.add(delegate)
    }
    
    public func unbindRespDelegate(delegate: AUIUserRespDelegate) {
        aui_info("unbindRespDelegate \(delegate)", tag: "AUIUserServiceImpl")
        respDelegates.remove(delegate)
    }
    
    public func getUserInfoList(roomId: String, userIdList: [String], callback:@escaping AUIUserListCallback) {
        aui_info("getUserInfoList", tag: "AUIUserServiceImpl")
        self.rtmManager.whoNow(channelName: roomId) { error, userList in
            if let error = error {
                callback(error as NSError, nil)
                return
            }
            
            var users = [AUIUserInfo]()
            userList?.forEach { attr in
                let user = AUIUserInfo.yy_model(withJSON: attr)!
                users.append(user)
            }
            self.userList = users
            callback(nil, users)
        }
    }
    
//    public func getUserInfo(by userId: String) -> AUIUserThumbnailInfo? {
//        return self.userList.filter {$0.userId == userId}.first
//    }
    
    public func muteUserAudio(isMute: Bool, callback: @escaping AUICallback) {
        aui_info("muteUserAudio: \(isMute)", tag: "AUIUserServiceImpl")
        let currentUserId = getRoomContext().currentUserInfo.userId
        let currentUser = userList.first(where: {$0.userId == currentUserId})
        currentUser?.muteAudio = isMute
        let userAttr = currentUser?.yy_modelToJSONObject() as? [String: Any] ?? [:]
//        print("muteUserAudio user:  \(userDic)")
        self.rtmManager.setPresenceState(channelName: channelName, attr: userAttr) {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                callback(error as NSError)
                return
            }
            
            callback(nil)
            
            //自己状态不会更新，在这里手动回调
            self.respDelegates.allObjects.forEach { obj in
                guard let obj = obj as? AUIUserRespDelegate else {return}
                obj.onUserAudioMute(userId: currentUserId, mute: isMute)
            }
        }
    }
    
    public func muteUserVideo(isMute: Bool, callback: @escaping AUICallback) {
        aui_info("muteUserVideo: \(isMute)", tag: "AUIUserServiceImpl")
        let currentUserId = getRoomContext().currentUserInfo.userId
        let currentUser = userList.first(where: {$0.userId == currentUserId})
        currentUser?.muteVideo = isMute
        let userAttr = currentUser?.yy_modelToJSONObject() as? [String: Any] ?? [:]
//        print("muteUserAudio user:  \(userDic)")
        self.rtmManager.setPresenceState(channelName: channelName, attr: userAttr) {[weak self] error in
            guard let self = self else {return}
            if let error = error {
                callback(error as NSError)
                return
            }
            
            callback(nil)
            
            //自己状态不会更新，在这里手动回调
            self.respDelegates.allObjects.forEach { obj in
                guard let obj = obj as? AUIUserRespDelegate else {return}
                obj.onUserVideoMute(userId: currentUserId, mute: isMute)
            }
        }
    }
    
    public func kickUser(roomId: String, userId: String, callback: @escaping AUICallback) {
//        let model = AUIKickUserReqModel()
//        model.roomId = roomId
//        model.uid = Int(userId) ?? 0
//        model.operatorId = getRoomContext().currentUserInfo.userId
//        model.request { error, obj in
//            callback(error as? NSError)
//        }
    }
}

extension AUIUserServiceImpl {
    //设置用户属性到presence
    private func _setupUserAttr(roomId: String, completion: ((Error?) -> ())?) {
        let userId = AUIRoomContext.shared.currentUserInfo.userId
        let userInfo = self.userList.filter({$0.userId == userId}).first ?? AUIUserInfo()
        userInfo.userId = AUIRoomContext.shared.currentUserInfo.userId
        userInfo.userName = AUIRoomContext.shared.currentUserInfo.userName
        userInfo.userAvatar = AUIRoomContext.shared.currentUserInfo.userAvatar
        
        let userAttr = userInfo.yy_modelToJSONObject() as? [String: Any] ?? [:]
        aui_info("_setupUserAttr: \(roomId) : \(userAttr)", tag: "AUIUserServiceImpl")
        self.rtmManager.setPresenceState(channelName: roomId, attr: userAttr) { error in
            defer {
                completion?(error)
            }
            if let error = error {
                aui_info("_setupUserAttr: \(roomId) fail: \(error.localizedDescription)", tag: "AUIUserServiceImpl")
                //TODO: retry
                return
            }
            
            //rtm不会返回自己更新的数据，需要手动处理
            self.onUserDidUpdated(channelName: roomId, userId: userId, userInfo: userAttr)
        }
    }
}


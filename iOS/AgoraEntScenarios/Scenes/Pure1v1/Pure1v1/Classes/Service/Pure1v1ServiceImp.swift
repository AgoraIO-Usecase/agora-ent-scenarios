//
//  Pure1v1ServiceImp.swift
//  Pure1v1
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation
import RTMSyncManager
import YYModel
import SwiftyBeaver
import AgoraRtmKit
import AgoraCommon

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
private let kRoomId = "pure600"
class Pure1v1ServiceImp: NSObject {
    private var user: Pure1v1UserInfo
    private var rtmClient: AgoraRtmClientKit
    private var userDidChangedClosure: (([Pure1v1UserInfo]) -> ())?
    private var isEnterSuccess: Bool = false
    private var userList: [Pure1v1UserInfo] = [] {
        didSet {
            self.userDidChangedClosure?(userList)
        }
    }
    
    private lazy var syncManager: AUISyncManager = {
        let config = AUICommonConfig()
        config.appId = AppContext.shared.appId
        let owner = AUIUserThumbnailInfo()
        owner.userId = user.userId
        owner.userName = user.userName
        owner.userAvatar = user.avatar
        config.owner = owner
        config.host = AppContext.shared.roomManagerUrl
        let manager = AUISyncManager(rtmClient: rtmClient, commonConfig: config, logConfig: nil)
        
        return manager
    }()
    
    private lazy var userService: AUIUserServiceImpl = {
        let service = AUIUserServiceImpl(channelName: kRoomId, rtmManager: syncManager.rtmManager, autoSetUserAttr: true)
        return service
    }()
    
    required init(user: Pure1v1UserInfo, rtmClient: AgoraRtmClientKit) {
        self.user = user
        self.rtmClient = rtmClient
        AUIRoomContext.shared.displayLogClosure = { msg in
            Pure1v1Logger.info(msg, tag: "RTMSyncManager")
        }
        super.init()
    }
}

extension Pure1v1ServiceImp: Pure1v1ServiceProtocol {
    func getUserList(completion: @escaping ([Pure1v1UserInfo], NSError?) -> Void) {
        
        self.userService.getUserInfoList(roomId: kRoomId) {[weak self] err, list in
            guard let self = self else {return}
            if let err = err {
                completion([], err)
                return
            }
            
            self.onRoomUserSnapshot(roomId: kRoomId, userList: list ?? [])
            completion(self.userList, nil)
        }
    }
    
    func enterRoom(completion: @escaping (NSError?) -> Void) {        
        if isEnterSuccess {
            completion(nil)
            return
        }
        let date = Date()
        userService.bindRespDelegate(delegate: self)
        syncManager.rtmManager.subscribe(channelName: kRoomId) {[weak self] err in
            guard let self = self else {return}
            Pure1v1Logger.info("enterRoom subscribe cost: \(-Int(date.timeIntervalSinceNow * 1000)) ms")
            completion(err)
            self.isEnterSuccess = err == nil ? true : false
        }
    }
    
    func leaveRoom(completion: @escaping (NSError?) -> Void) {
        userService.unbindRespDelegate(delegate: self)
        isEnterSuccess = false
        completion(nil)
    }
    
    func subscribeUserListChanged(with changedBlock: (([Pure1v1UserInfo]) -> Void)?) {
        self.userDidChangedClosure = changedBlock
    }
    
    func unsubscribeAll() {
    }
}

extension Pure1v1ServiceImp: AUIUserRespDelegate {
    func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        let list = userList.flatMap({ $0.userName.isEmpty ? nil : Pure1v1UserInfo(userInfo: $0) }) ?? []
        self.userList = list
    }
    
    func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        self.userList.append(Pure1v1UserInfo(userInfo: userInfo))
    }
    
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        let userList = self.userList
        self.userList = userList.filter({ $0.userId != userInfo.userId})
    }
    
    func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        if let idx = self.userList.firstIndex(where: { $0.userId == userInfo.userId}) {
            self.userList[idx] = Pure1v1UserInfo(userInfo: userInfo)
            return
        }
        
        self.userList.append(Pure1v1UserInfo(userInfo: userInfo))
    }
    
    func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
}

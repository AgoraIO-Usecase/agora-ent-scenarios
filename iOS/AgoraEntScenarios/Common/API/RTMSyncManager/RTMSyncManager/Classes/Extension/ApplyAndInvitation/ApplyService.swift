//
//  ApplyService.swift
//  FBSnapshotTestCase
//
//  Created by wushengtao on 2024/6/7.
//

import Foundation

@objc public class ApplyInfo: NSObject, Codable {
    public var userId: String = ""
    public var userName: String = ""
    public var userAvatar: String = ""
    
    public override init() {
        super.init()
    }
    
    init?(userInfo: AUIUserThumbnailInfo) {
        self.userId = userInfo.userId
        self.userName = userInfo.userName
        self.userAvatar = userInfo.userAvatar
        super.init()
    }
    
    enum CodingKeys: String, CodingKey {
        case userId, userName, userAvatar
    }
}

@objc public protocol ApplyServiceProtocol: NSObjectProtocol {
    func onApplyListDidUpdate(channelName: String, list: [ApplyInfo])
}

enum ApplyCmd: String {
    case create = "createApply"
    case cancel = "cancelApply"
    case accept = "acceptApply"
    case reject = "rejectApply"
}

private let applyKey: String = "apply"

public class ApplyService: NSObject {
    private let channelName: String
    private let syncManager: AUISyncManager
    
    private var respDelegates = NSHashTable<ApplyServiceProtocol>.weakObjects()
    
    private(set) lazy var applyCollection: AUIListCollection = {
        let collection: AUIListCollection = syncManager.createScene(channelName: channelName).getCollection(key: applyKey)!
        return collection
    }()
    
    required init(channelName: String,
                  syncManager: AUISyncManager) {
        self.channelName = channelName
        self.syncManager = syncManager
        super.init()
        subscribe()
    }
    
    private func subscribe() {
        applyCollection.subscribeAttributesDidChanged {[weak self] channelName, observeKey, value in
            guard let self = self else {return}
            guard applyKey == observeKey else {return}
            let list: [ApplyInfo] = decodeModelArray(value.getList() ?? []) ?? []
            for element in self.respDelegates.allObjects {
                element.onApplyListDidUpdate(channelName: channelName, list: list)
            }
        }
        
        let scene = syncManager.getScene(channelName: channelName)
        scene?.userService.bindRespDelegate(delegate: self)
    }
}

//MARK: public method
extension ApplyService {
    public func subscribe(delegate: ApplyServiceProtocol) {
        if respDelegates.contains(delegate) {
            return
        }
        
        respDelegates.add(delegate)
    }
    
    public func unsubscribe(delegate: ApplyServiceProtocol) {
        respDelegates.remove(delegate)
    }
    
    public func addApply(userId: String, completion: ((NSError?)->())?) {
        let roomId = channelName
        guard let apply = getApplyInfo(userId: userId),
              let value = encodeModel(apply) else {
            aui_info("[\(roomId)]addApply userId: \(userId) fail", tag: "ApplyService")
            completion?(NSError(domain: "ApplyService",
                                code: 0,
                                userInfo: ["msg": "apply info is nil"]))
            return
        }
        
        aui_info("[\(roomId)]addApply userId: \(userId) start", tag: "ApplyService")
        //TODO: 保证该次申请没有在互动中，互动时不可发起申请
        applyCollection.addMetaData(valueCmd: ApplyCmd.create.rawValue,
                                    value: value,
                                    filter: [["userId": userId]]) { err in
            aui_info("[\(roomId)]addApply userId: \(userId) completion: \(err?.localizedDescription ?? "success")", tag: "ApplyService")
            completion?(err)
        }
    }
    
    public func acceptApply(userId: String, completion: ((NSError?)->())?) {
        //TODO: 观众不可以remove apply和interaction add一起调，万一一个失败了就数据错乱了
        /*
         1.仲裁者观众A发起连麦
         2.房主B收到连麦
         3.A取消连麦
         4.B没有收到取消的metadata，本地认为还是有A的申请的，这时候点击accept，就会出现这个问题
         
         点击acceptApply从
         1.查询本地状态
         2.removeApply
         3.startLinkingInteraction

         改为
         1.startLinkingInteraction
         2.仲裁者通过interaction请求，通过subscribeWillAdd回调去查apply表，看下对应的互动用户是不是在apply里
         3.如果确认可以插入interaction，顺便把apply里的这个用户移除
         */
        removeApply(applyCmd: .accept, userId: userId, completion: completion)
    }
    
    public func cancelApply(userId: String, completion: ((NSError?)->())?) {
        removeApply(applyCmd: .cancel, userId: userId, completion: completion)
    }
    
    
    public func getApplyList(completion: @escaping (NSError?, [ApplyInfo]?)->()) {
        let roomId = channelName
        aui_info("[\(roomId)]getApplyList start", tag: "ApplyService")
        applyCollection.getMetaData { err, value in
            aui_info("[\(roomId)]getApplyList completion: \(err?.localizedDescription ?? "success")", tag: "ApplyService")
            if let err = err {
                completion(err, nil)
                return
            }
            let value = value as? [[String: Any]] ?? []
            let list: [ApplyInfo] = decodeModelArray(value) ?? []
            completion(nil, list)
        }
    }
}

//MARK: private
extension ApplyService {
    private func removeApply(applyCmd: ApplyCmd, userId: String, completion: ((NSError?)->())?) {
        let roomId = channelName
        aui_info("[\(roomId)]removeApply userId: \(userId) cmd: \(applyCmd.rawValue)", tag: "ApplyService")
        applyCollection.removeMetaData(valueCmd: applyCmd.rawValue,
                                       filter: [["userId": userId]],
                                       callback: completion)
    }
    
    private func getApplyInfo(userId: String) -> ApplyInfo? {
        guard let scene = syncManager.getScene(channelName: channelName),
              let userInfo = scene.userService.userList.first(where: { $0.userId == userId }) else {
            return nil
        }
        
        return ApplyInfo(userInfo: userInfo)
    }
}

extension ApplyService: AUIUserRespDelegate {
    public func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo]) {
        
    }
    
    public func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo, reason: AUIRtmUserLeaveReason) {
        cancelApply(userId: userInfo.userId) { err in
            
        }
    }
    
    public func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo) {
        
    }
    
    public func onUserAudioMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserVideoMute(userId: String, mute: Bool) {
        
    }
    
    public func onUserBeKicked(roomId: String, userId: String) {
        
    }
    
    
}

//
//  RoomPresenceService.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/6/7.
//

import Foundation

@objcMembers public class RoomPresenceInfo: NSObject, Codable {
    public var roomId: String = "" // 唯一房间ID
    public var roomName: String = "" // 房间名
    public var ownerId: String = "" // 房主用户ID
    public var ownerName: String = "" // 房主名
    public var ownerAvatar: String = "" // 房主头像
    public var status: InteractionType = .idle
    public var interactorId: String = "" // 互动者ID
    public var interactorName: String = ""  // 互动者名
    
    private enum CodingKeys: String, CodingKey {
        case roomId
        case roomName
        case ownerId
        case ownerName
        case ownerAvatar
        case status
        case interactorId
        case interactorName
    }
        
//    required public init(from decoder: Decoder) throws {
//        let container = try decoder.singleValueContainer()
//        let statusValue = try container.decode(String.self)
//        
//        if let statusInt = Int(statusValue), let statusEnum = RoomPresenceStatus(rawValue: statusInt) {
//            self.status = statusEnum
//        }
//        
//        super.init()
//    }
}

private func convertMap(_ map: [String: Any]) -> [String: Any] {
    var convertMap = map
    convertMap["status"] = Int("\(map["status"] as? String ?? "")") ?? 0
    return convertMap
}

@objc public protocol RoomPresenceProtocol: NSObjectProtocol {
    func onUserSnapshot(channelName: String, userList: [RoomPresenceInfo])
    func onUserUpdate(channelName: String, user: RoomPresenceInfo)
    func onUserDelete(channelName: String, user: RoomPresenceInfo)
    func onUserError(channelName: String, error: NSError)
}

public class RoomPresenceService: NSObject {
    private var rtmManager: AUIRtmManager
    private var channelName: String
    private var respDelegates = NSHashTable<RoomPresenceProtocol>.weakObjects()
    private(set) var userList: [RoomPresenceInfo] = []
    
    required init(channelName: String, rtmManager: AUIRtmManager) {
        self.channelName = channelName
        self.rtmManager = rtmManager
        super.init()
    }
    
    public func subscribeChannel() {
        rtmManager.subscribeUser(channelName: channelName, delegate: self)
        rtmManager.subscribe(channelName: channelName) { err in
            
        }
    }
    
    public func unsubscribeChannel() {
        rtmManager.unSubscribe(channelName: channelName)
    }
}

//MARK: public
extension RoomPresenceService {
    public func subscribe(delegate: RoomPresenceProtocol) {
        if respDelegates.contains(delegate) {
            return
        }
        
        respDelegates.add(delegate)
    }
    
    public func unsubscribe(delegate: RoomPresenceProtocol) {
        respDelegates.remove(delegate)
    }
    
    public func setRoomPresenceInfo(user: RoomPresenceInfo, completion: ((NSError?) -> ())?) {
        aui_info("setRoomPresenceInfo ownerId: \(user.ownerId) ownerName: \(user.ownerName) roomId: \(user.roomId)", tag: "RoomPresenceService")
        guard let attr = encodeModel(user) else {
            completion?(NSError(domain: "RoomPresenceService",
                                code: 0,
                                userInfo: ["msg": "encodeModel fail"]))
            return
        }
        rtmManager.setPresenceState(channelName: channelName, attr: attr) { err in
            completion?(err)
        }
    }
    
    public func updateRoomPresenceInfo(roomId: String,
                                       status: InteractionType,
                                       interactorId: String,
                                       interactorName: String,
                                       completion: ((NSError?) -> ())?) {
        aui_info("updateRoomPresenceInfo roomId: \(roomId) status: \(status.rawValue) interactorId: \(interactorId) interactorName: \(interactorName)", tag: "RoomPresenceService")
        guard let user = getRoomPresenceInfo(roomId: roomId) else {
          completion?(NSError(domain: "RoomPresenceService",
                              code: 0,
                              userInfo: ["msg": "RoomInteractionInfo not found"]))
            return
        }
        user.status = status
        user.interactorId = interactorId
        user.interactorName = interactorName
        setRoomPresenceInfo(user: user, completion: completion)
    }
    
    public func getRoomPresenceInfo(roomId: String) -> RoomPresenceInfo? {
        let user = userList.first { $0.roomId == roomId }
        return user
    }
    
    public func getRoomPresenceInfoByOwnerId(ownerId: String) -> RoomPresenceInfo? {
        let user = userList.first { $0.ownerId == ownerId }
        return user
    }
    
    public func getAllRoomPresenceInfo(completion: @escaping (NSError?, [RoomPresenceInfo]?) -> ()) {
        rtmManager.whoNow(channelName: channelName) {[weak self] err, userList in
            guard let self = self else {return}
            if let err = err {
                completion(err, nil)
                return
            }
            let convertUserList = userList?.map { convertMap($0)} ?? []
            let userList: [RoomPresenceInfo] = decodeModelArray(convertUserList) ?? []
            self.userList = userList
            completion(nil, userList)
        }
    }
}

extension RoomPresenceService: AUIRtmUserProxyDelegate {
    
    
    public func onCurrentUserJoined(channelName: String) {
        
    }
    
    public func onUserSnapshotRecv(channelName: String, userId: String, userList: [[String : Any]]) {
        aui_info("onUserSnapshotRecv user count: \(userList.count)", tag: "RoomPresenceService")
        let convertUserList = userList.map { convertMap($0) }
        let userList: [RoomPresenceInfo] = decodeModelArray(convertUserList) ?? []
        self.userList = userList
        respDelegates.allObjects.forEach { delegate in
            delegate.onUserSnapshot(channelName: channelName, userList: userList)
        }
    }
    
    public func onUserDidJoined(channelName: String, userId: String, userInfo: [String : Any]) {
//        guard let user: RoomPresenceInfo = decodeModel(userInfo) else {return}
        
//        respDelegates.allObjects.forEach { delegate in
//            delegate.onUserUpdate(user: user)
//        }
    }
    
    public func onUserDidLeaved(channelName: String, userId: String, userInfo: [String : Any], reason: AUIRtmUserLeaveReason) {
        aui_info("onUserDidLeaved userId: \(userId)", tag: "RoomPresenceService")
        guard let user = userList.filter({ $0.ownerId == userId }).first else {return}
        self.userList.removeAll { $0.ownerId == userId }
        respDelegates.allObjects.forEach { delegate in
            delegate.onUserDelete(channelName: channelName, user: user)
        }
    }
    
    public func onUserDidUpdated(channelName: String, userId: String, userInfo: [String : Any]) {
        aui_info("onUserDidUpdated userInfo: \(userInfo)", tag: "RoomPresenceService")
        guard let user: RoomPresenceInfo = decodeModel(convertMap(userInfo)) else {return}
        
        if let index = self.userList.firstIndex(where: { $0.ownerId == userId }) {
            userList[index] = user
        } else {
            userList.append(user)
        }
        
        respDelegates.allObjects.forEach { delegate in
            delegate.onUserUpdate(channelName: channelName, user: user)
        }
    }
}

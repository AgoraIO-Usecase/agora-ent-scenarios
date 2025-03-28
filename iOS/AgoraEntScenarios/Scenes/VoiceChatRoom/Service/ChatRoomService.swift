//
//  AUIRoomService.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/5/8.
//

import Foundation
import RTMSyncManager

public class ChatRoomService: NSObject {
    private var expirationPolicy: RoomExpirationPolicy
    private var roomManager: AUIRoomManagerImpl
    private var roomInfoMap: [String: AUIRoomInfo] = [:]
    
    private var creatingRoomIds: Set<String> = Set()
    
    public required init(expirationPolicy: RoomExpirationPolicy, roomManager: AUIRoomManagerImpl) {
        self.expirationPolicy = expirationPolicy
        self.roomManager = roomManager
        super.init()
    }
    
    public func getRoomList(lastCreateTime: Int64,
                            pageSize: Int,
                            cleanClosure: ((AUIRoomInfo) ->(Bool))? = nil,
                            completion: @escaping (NSError?, Int64, [AUIRoomInfo]?)->()) {
        //房间列表会返回最新的服务端时间ts
        roomManager.getRoomInfoList(lastCreateTime: lastCreateTime, pageSize: pageSize) {[weak self] err, ts, roomList in
            guard let self = self else {return}
            if let err = err {
                completion(err, ts, nil)
                return
            }
            
            var list: [AUIRoomInfo] = []
            roomList?.forEach({ roomInfo in
                //遍历每个房间信息，查询是否已经过期
                var needCleanRoom: Bool = false
                if self.creatingRoomIds.contains(roomInfo.roomId) {
                    //正在创建，不删除，防止刷新的时候开始创建，导致因为时序问题创建的restful房间被删除了
                } else if self.expirationPolicy.expirationTime > 0, ts - roomInfo.createTime >= self.expirationPolicy.expirationTime + 60 * 1000 {
                    print("remove expired room[\(roomInfo.roomId)]")
                    needCleanRoom = true
                } else if cleanClosure?(roomInfo) ?? false {
                    print("external decision to delete room[\(roomInfo.roomId)]")
                    needCleanRoom = true
                }
                
                if needCleanRoom {
                    self.roomManager.destroyRoom(roomId: roomInfo.roomId) { _ in
                    }
                    self.roomInfoMap[roomInfo.roomId] = nil
                    return
                }
                
                list.append(roomInfo)
            })
            completion(nil, ts, list)
        }
    }
    
    //TODO: AUIRoomInfo替换成协议IAUIRoomInfo？服务端会创建房间id，这里是否roomManager创建后往外抛roomId
    public func createRoom(room: AUIRoomInfo, 
                           enterEnable: Bool = true,
                           expirationPolicy: RoomExpirationPolicy? = nil,
                           completion: @escaping ((NSError?, AUIRoomInfo?)->())) {
        let date = Date()
        creatingRoomIds.insert(room.roomId)
        let innerCompletion: ((NSError?, AUIRoomInfo?)->()) = {[weak self] error, info in
            guard let self = self else {return}
            self.creatingRoomIds.remove(room.roomId)
            if let error = error {
                //失败需要清理脏房间信息
                self.createRoomRevert(roomId: room.roomId)
                completion(error, nil)
                return
            }
            guard let info = info else {
                //失败需要清理脏房间信息
                self.createRoomRevert(roomId: room.roomId)
                print("create fail[\(room.roomId)] room info not found")
                completion(NSError(domain: "room not found", code: -1), nil)
                return
            }
            completion(nil, info)
        }
        
        roomManager.createRoom(room: room) {[weak self] err, roomInfo in
            guard let self = self else { return }
            if let err = err {
                innerCompletion(err, nil)
                return
            }
            guard let roomInfo = roomInfo else {
                assert(false)
                return
            }
            
            print("[Timing]createRoom create restful[\(roomInfo.roomId)] cost: \(Int64(-date.timeIntervalSinceNow * 1000))ms")
            self.roomInfoMap[roomInfo.roomId] = roomInfo
            //传入服务端设置的创建房间的时间戳createTime
            innerCompletion(nil, roomInfo)
        }
    }
    
    public func enterRoom(roomInfo: AUIRoomInfo, 
                          expirationPolicy: RoomExpirationPolicy? = nil,
                          completion: @escaping ((NSError?)->())) {
        self.roomInfoMap[roomInfo.roomId] = roomInfo
        completion(nil)
    }
    
    public func enterRoom(roomId: String, 
                          expirationPolicy: RoomExpirationPolicy? = nil,
                          completion: @escaping ((NSError?)->())) {
        let room = AUIRoomInfo()
        room.roomId = roomId
        self.roomInfoMap[room.roomId] = room
        completion(nil)
    }
    
    public func leaveRoom(roomId: String) {
        let isOwner = roomInfoMap[roomId]?.owner?.userId == AUIRoomContext.shared.currentUserInfo.userId
        if isOwner {
            roomManager.destroyRoom(roomId: roomId) { _ in
            }
        }
        roomInfoMap[roomId] = nil
    }
    
    public func leaveRoom(room: AUIRoomInfo) {
        roomInfoMap[room.roomId] = room
        leaveRoom(roomId: room.roomId)
    }
    
    public func getRoomInfo(roomId: String) -> AUIRoomInfo? {
        return roomInfoMap[roomId]
    }
    
    public func isRoomOwner(roomId: String) -> Bool {
        let isOwner = roomInfoMap[roomId]?.owner?.userId == AUIRoomContext.shared.currentUserInfo.userId
        return isOwner
    }
}

//MARK: private
extension ChatRoomService {
    private func createRoomRevert(roomId: String) {
        print("createRoomRevert[\(roomId)]")
        leaveRoom(roomId: roomId)
    }
    
    private func enterRoomRevert(roomId: String) {
        print("enterRoomRevert[\(roomId)]")
        leaveRoom(roomId: roomId)
    }
}


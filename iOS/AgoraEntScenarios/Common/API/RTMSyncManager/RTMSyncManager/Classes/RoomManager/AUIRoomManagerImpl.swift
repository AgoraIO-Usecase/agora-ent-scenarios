//
//  AUIRoomManagerImpl.swift
//  AUIKit
//
//  Created by wushengtao on 2023/2/24.
//

import Foundation

@objc open class AUIRoomManagerImpl: NSObject {
    private var sceneId: String
    deinit {
        aui_info("deinit AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
    }
    
    public required init(sceneId: String) {
        self.sceneId = sceneId
        super.init()
        aui_info("init AUIRoomManagerImpl", tag: "AUIRoomManagerImpl")
    }
}

extension AUIRoomManagerImpl {
    public func createRoom(room: AUIRoomInfo,
                           callback: @escaping (NSError?, AUIRoomInfo?) -> ()) {
        aui_info("enterRoom: \(room.roomName) ", tag: "AUIRoomManagerImpl")
        
        let model = SyncRoomCreateNetworkModel()
        model.sceneId = sceneId
        model.roomInfo = room
        
        var createRoomError: NSError? = nil
        var roomInfo: AUIRoomInfo? = nil
        //create a room from the server
        model.request { error, resp in
            createRoomError = error as? NSError
            roomInfo = resp as? AUIRoomInfo
            callback(createRoomError, roomInfo)
        }
    }
    
    public func updateRoom(room: AUIRoomInfo,
                           callback: @escaping (NSError?, AUIRoomInfo?) -> ()) {
        aui_info("updateRoom: \(room.roomName) ", tag: "AUIRoomManagerImpl")
        
        let model = SyncRoomUpdateNetworkModel()
        model.sceneId = sceneId
        model.roomInfo = room
        
        var createRoomError: NSError? = nil
        var roomInfo: AUIRoomInfo? = nil
        //update a room from the server
        model.request { error, resp in
            createRoomError = error as? NSError
            roomInfo = resp as? AUIRoomInfo
            callback(createRoomError, roomInfo)
        }
    }
    
    public func destroyRoom(roomId: String,
                            callback: @escaping (NSError?) -> ()) {
        aui_info("destroyRoom: \(roomId) ", tag: "AUIRoomManagerImpl")
        let model = SyncRoomDestroyNetworkModel()
        model.sceneId = sceneId
        model.userId = AUIRoomContext.shared.currentUserInfo.userId
        model.roomId = roomId
        model.request { error, _ in
            callback(error as? NSError)
        }
    }
    
    public func getRoomInfoList(lastCreateTime: Int64,
                                pageSize: Int,
                                callback: @escaping AUIRoomListCallback) {
        aui_info("getRoomInfoList", tag: "AUIRoomManagerImpl")
        let model = SyncRoomListNetworkModel()
        model.sceneId = sceneId
        model.lastCreateTime = lastCreateTime == 0 ? nil : NSNumber(value: Int(lastCreateTime))
        model.pageSize = pageSize
        model.request { error, dic in
            var ts: Int64 = 0
            var roomList: [AUIRoomInfo]? = nil
            if let dic = dic as? [String: Any] {
                ts = dic["ts"] as? Int64 ?? 0
                roomList = dic["list"] as? [AUIRoomInfo]
            }
            callback(error as NSError?, ts, roomList)
        }
    }
}

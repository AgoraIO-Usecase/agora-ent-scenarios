//
//  VLRoomListModel.swift
//  AgoraEntScenarios
//
//  Created by CP on 2023/7/13.
//

import RTMSyncManager

//class VLRoomListModel: VLBaseModel {
//    
//    //@property (nonatomic, copy) NSString *id;
//    @objc var name: String?
//    @objc var isPrivate: Bool = false
//    @objc var password: String?
//    @objc var creator: String?
//    @objc var roomNo: String?
//    @objc var isChorus: String?
//    @objc var bgOption: Int = 0
//    @objc var soundEffect: String?
//    @objc var belCanto: String?
//    @objc var createdAt: Int64 = 0
//    @objc var updatedAt: Int64 = 0
//    @objc var status: String?
//    @objc var deletedAt: String?
//    @objc var roomPeopleNum: String?
//    @objc var icon: String?
//    
//    ///新加字段 当前房间的创建者
//    @objc var creatorNo: String?
//    @objc var creatorAvatar: String?
//    @objc var creatorName: String?
//    
//    @objc var objectId: String?
//}

extension AUIRoomInfo {
    @objc var name: String {
        set {
            self.roomName = newValue
        } get {
            return self.roomName
        }
    }
    
    @objc var roomNo: String {
        set {
            self.roomId = newValue
        } get {
            return self.roomId
        }
    }
    
    @objc var roomPeopleNum: Int {
        set {
            self.customPayload["roomPeopleNum"]  = newValue
        } get {
            return self.customPayload["roomPeopleNum"] as? Int ?? 0
        }
    }
    
    @objc var password: String? {
        set {
            self.customPayload["password"]  = newValue
        } get {
            return self.customPayload["password"] as? String
        }
    }
    
    @objc var isPrivate: Bool {
        set {
            self.customPayload["isPrivate"]  = newValue
        } get {
            return self.customPayload["isPrivate"] as? Bool ?? false
        }
    }
    
    @objc var icon: String {
        set {
            self.customPayload["icon"]  = newValue
        } get {
            return self.customPayload["icon"] as? String ?? ""
        }
    }
    
    @objc var updatedAt: Int64 {
        set {
            self.customPayload["updatedAt"]  = newValue
        } get {
            return self.customPayload["updatedAt"] as? Int64 ?? 0
        }
    }
    
    @objc var creatorAvatar: String {
        set {
            if self.owner == nil {
                self.owner = AUIUserInfo()
            }
            owner?.userAvatar = newValue
        } get {
            return owner?.userAvatar ?? ""
        }
    }
    
    @objc var creatorName: String {
        set {
            if self.owner == nil {
                self.owner = AUIUserInfo()
            }
            owner?.userName = newValue
        } get {
            return owner?.userName ?? ""
        }
    }
    
    @objc var creatorNo: String {
        set {
            if self.owner == nil {
                self.owner = AUIUserInfo()
            }
            owner?.userId = newValue
        } get {
            return owner?.userId ?? ""
        }
    }
    
    
    @objc var soundEffect: String? {
        set {
            self.customPayload["soundEffect"]  = newValue
        } get {
            return self.customPayload["soundEffect"] as? String
        }
    }
    
    @objc var status: String? {
        set {
            self.customPayload["status"]  = newValue
        } get {
            return self.customPayload["status"] as? String
        }
    }
    
    @objc var bgOption: Int {
        set {
            self.customPayload["bgOption"]  = newValue
        } get {
            return self.customPayload["bgOption"] as? Int ?? 0
        }
    }
}

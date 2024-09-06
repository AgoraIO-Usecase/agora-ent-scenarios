//
//  ShowServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/2.
//

import Foundation
import VideoLoaderAPI
import AgoraCommon
import RTMSyncManager

//@objc public enum ShowRoomStatus: Int {
//    /// 直播中
//    case activity = 0
//    /// 直播结束
//    case end = 1
//}

//@objc enum ShowRoomRequestStatus: Int {
//    case idle = 0
//    /// 等待中
//    case waitting = 1
//    /// 已接受
//    case accepted = 2
//    /// 已拒绝
//    case rejected = 3
//    /// 已结束
//    case ended = 4
//}

typealias ShowInteractionStatus = InteractionType

//@objc public enum ShowInteractionStatus: Int {
//    /// 空闲
//    case idle = 0
//    /// 连麦中
//    case onSeat = 1
//    /// pk中
//    case pking = 2
//    
//    var toastTitle: String {
//        switch self {
//        case .idle: return ""
//        case .onSeat: return "show_end_broadcasting".show_localized
//        case .pking: return "show_end_pk".show_localized
//        }
//    }
//    
//    var isInteracting: Bool {
//        switch self {
//        case .onSeat, .pking:
//            return true
//        default:
//            return false
//        }
//    }
//}


/// 房间列表信息
@objcMembers
public class ShowRoomListModel: NSObject, IVideoLoaderRoomInfo {
    public func channelName() -> String {
        return roomId
    }
    
    public func userId() -> String {
        return ownerId
    }
    
    public var anchorInfoList: [AnchorInfo] {
        get {
            let anchorInfo = AnchorInfo()
            anchorInfo.channelName = roomId
            if !ownerId.isEmpty {
                anchorInfo.uid = UInt(ownerId)!
            }
            anchorInfo.token = AppContext.shared.rtcToken ?? ""
            
            return [anchorInfo] + interactionAnchorInfoList
        }
    }
    
    public var interactionAnchorInfoList: [AnchorInfo] = []
    
    public var roomId: String = ""                                //房间号
    public var roomName: String?                              //房间名
    public var roomUserCount: Int = 1                         //房间人数
//    public var thumbnailId: String?                           //缩略图id
    public var ownerId: String = ""                               //房主user id (rtc uid)
    public var ownerAvatar: String?                           //房主头像
    public var ownerName: String?                             //房主名
//    public var roomStatus: ShowRoomStatus = .activity         //直播状态
//    public var interactStatus: ShowInteractionStatus = .idle  //互动状态
    public var createdAt: Int64 = 0                           //创建时间，与19700101时间比较的毫秒数
    public var updatedAt: Int64 = 0                           //更新时间
}

//PK邀请对象
typealias ShowPKUserInfo = RoomPresenceInfo


/// 房间详情信息
public typealias ShowRoomDetailModel = ShowRoomListModel

///用户信息
public typealias ShowUser = AUIUserInfo

/// 聊天消息
@objcMembers
public class ShowMessage: NSObject, Codable {
    var userId: String = ""        //用户id (rtc uid)
    var userName: String?      //用户名
    var message: String?       //消息文本内容
    var createAt: Int64 = 0    //创建时间，与19700101时间比较的毫秒数
    
    
    enum CodingKeys: String, CodingKey {
        case userId, userName, message, createAt
    }
}

/// 连麦申请
public typealias ShowMicSeatApply = ApplyInfo
//class ShowMicSeatApply: ApplyInfo {
//    var createdAt: Int64 = 0                         //创建时间，与19700101时间比较的毫秒数
//    
//    #if DEBUG
//    override var description: String {
//        return "userId: \(userId) userName: \(userName)"
//    }
//    #endif
//}


/// 连麦邀请
public typealias ShowMicSeatInvitation = InvitationInfo

public typealias ShowPKInvitation = PKInfo
/// PK邀请
//public class ShowPKInvitation: NSObject {
//    var userId: String = ""                              //被pk用户id (rtc uid)
//    var userName: String?                            //用户名
//    var roomId: String = ""                              //被pk房间id
//    var fromUserId: String = ""                          //发起Pk用户id (rtc uid)
//    var fromName: String?                            //发起Pk用户名
//    var fromRoomId: String = ""                          //发起Pk房间id
//    var status: ShowRoomRequestStatus = .waitting    //邀请状态
//    var createdAt: Int64 = 0                         //创建时间，与19700101时间比较的毫秒数
//    
//    override public func isEqual(_ object: Any?) -> Bool {
//        guard let info = object as? ShowPKInvitation,
//              userId == info.userId,
//              userName == info.userName,
//              roomId == info.roomId,
//              fromUserId == info.fromUserId,
//              fromName == info.fromName,
//              fromRoomId == info.fromRoomId,
//              status == info.status,
//              createdAt == info.createdAt else {
//            return false
//        }
//        
//        return true
//    }
//    
//    #if DEBUG
//    override public var description: String {
//        return "userId: \(userId) roomId: \(roomId) fromUserId: \(fromUserId) fromRoomId: \(fromRoomId) status: \(status)"
//    }
//    #endif
//}

//连麦/Pk模型
public typealias ShowInteractionInfo = InteractionInfo
//public class ShowInteractionInfo: NSObject {
//    var userId: String = ""                                 //用户id (rtc uid) pk是另一个房间的房主uid，连麦是连麦观众uid
//    var userName: String?                               //用户名
//    var roomId: String = ""                                 //用户所在房间id
//    var interactStatus: ShowInteractionStatus = .idle   //交互类型
//    var createdAt: Int64 = 0                            //创建时间，与19700101时间比较的毫秒数
//    
////    #if DEBUG
//    override public var description: String {
//        return "userId: \(userId) roomId: \(roomId) status: \(interactStatus)"
//    }
////    #endif
//    
//    override public func isEqual(_ object: Any?) -> Bool {
//        guard let info = object as? ShowInteractionInfo,
//              userId == info.userId,
//              roomId == info.roomId,
//              interactStatus == info.interactStatus else {
//            return false
//        }
//        
//        return true
//    }
//}



extension AUIRoomInfo {
    @objc var roomUserCount: Int {
        set {
            self.customPayload["roomUserCount"]  = newValue
        } get {
            return self.customPayload["roomUserCount"] as? Int ?? 0
        }
    }
    
    func createShowServiceModel() -> ShowRoomListModel {
        let model = ShowRoomListModel()
        model.roomId = roomId
        model.roomName = roomName
        model.roomUserCount = customPayload["roomUserCount"] as? Int ?? 0
        model.ownerId = owner?.userId ?? ""
        model.ownerName = owner?.userName ?? ""
        model.ownerAvatar = owner?.userAvatar ?? ""
        model.createdAt = createTime
        model.updatedAt = createTime
        
//        var thumbnailId: String?                           //缩略图id
//        var roomStatus: ShowRoomStatus = .activity         //直播状态
//        var interactStatus: ShowInteractionStatus = .idle  //互动状态
        return model
    }
    
    static func convertFromShowRoomListModel(_ model: ShowRoomListModel) -> AUIRoomInfo {
        let roomInfo = AUIRoomInfo()
        roomInfo.roomId = model.roomId
        roomInfo.roomName = model.roomName ?? ""
        roomInfo.roomUserCount = model.roomUserCount
        let owner = AUIUserThumbnailInfo()
        owner.userId = model.ownerId
        owner.userName = model.ownerName ?? ""
        owner.userAvatar = model.ownerAvatar ?? ""
        roomInfo.owner = owner
        roomInfo.createTime = model.createdAt
        
        return roomInfo
    }
}

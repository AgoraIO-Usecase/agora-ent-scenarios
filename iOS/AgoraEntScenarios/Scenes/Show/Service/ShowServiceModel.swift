//
//  ShowServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/2.
//

import Foundation


@objc enum ShowRoomStatus: Int {
    /// 直播中
    case activity = 0
    /// 直播结束
    case end = 1
}

@objc enum ShowRoomRequestStatus: Int {
    case idle = 0
    /// 等待中
    case waitting = 1
    /// 已接受
    case accepted = 2
    /// 已拒绝
    case rejected = 3
    /// 已结束
    case ended = 4
}

@objc enum ShowInteractionStatus: Int {
    /// 空闲
    case idle = 0
    /// 连麦中
    case onSeat = 1
    /// pk中
    case pking = 2
}

@objcMembers
class ShowBaseInfo: NSObject {
    var objectId: String?    //SyncManager获取到的对象带的唯一标识，用于差改删
}

/// 房间列表信息
@objcMembers
class ShowRoomListModel: ShowBaseInfo {
    var roomId: String?                                //房间号
    var roomName: String?                              //房间名
    var roomUserCount: Int = 1                         //房间人数
    var thumbnailId: String?                           //缩略图id
    var ownerId: String?                               //房主user id (rtc uid)
    var ownerAvater: String?                           //房主头像
    var ownerName: String?                             //房主名
    var roomStatus: ShowRoomStatus = .activity         //直播状态
    var interactStatus: ShowInteractionStatus = .idle  //互动状态
    var createdAt: Int64 = 0                           //创建时间，与19700101时间比较的毫秒数
    var updatedAt: Int64 = 0                           //更新时间
}

//PK邀请对象
typealias ShowPKUserInfo = ShowRoomListModel


/// 房间详情信息
@objcMembers
class ShowRoomDetailModel: ShowRoomListModel {
}

///用户信息
@objcMembers
class ShowUser: ShowBaseInfo {
    var userId: String?              //用户id (rtc uid)
    var avatar: String?              //用户头像
    var userName: String?            //用户名
    var status: ShowRoomRequestStatus = .idle //申请状态
}

/// 聊天消息
@objcMembers
class ShowMessage: ShowBaseInfo {
    var userId: String?        //用户id (rtc uid)
    var userName: String?      //用户名
    var message: String?       //用户名
    var createAt: Int64 = 0    //创建时间，与19700101时间比较的毫秒数
}

/// 连麦申请
class ShowMicSeatApply: ShowBaseInfo {
    var userId: String?                              //用户id (rtc uid)
    var avatar: String?                              //用户头像
    var userName: String?                            //用户名
    var status: ShowRoomRequestStatus = .idle        //申请状态
    var createdAt: Int64 = 0                         //创建时间，与19700101时间比较的毫秒数
}


/// 连麦邀请
class ShowMicSeatInvitation: ShowBaseInfo {
    var userId: String?                              //用户id (rtc uid)
    var avatar: String?                          //用户头像
    var userName: String?                            //用户名
    var status: ShowRoomRequestStatus = .idle      //邀请状态
    var createdAt: Int64 = 0                         //创建时间，与19700101时间比较的毫秒数
}

/// PK邀请
class ShowPKInvitation: ShowBaseInfo {
    var userId: String?                              //被pk用户id (rtc uid)
    var roomId: String?                              //被pk房间id
    var fromUserId: String?                          //发起Pk用户id (rtc uid)
    var fromName: String?                            //发起Pk用户名
    var fromRoomId: String?                          //发起Pk房间id
    var status: ShowRoomRequestStatus = .waitting    //邀请状态
    var muteAudio: Bool = false                      //静音状态
    var createdAt: Int64 = 0                         //创建时间，与19700101时间比较的毫秒数
    
    override var description: String {
        return "userId: \(userId ?? "") roomId: \(roomId ?? "") fromUserId: \(fromUserId ?? "") fromRoomId: \(fromRoomId ?? "") status: \(status) objectId: \(objectId ?? "")"
    }
}

//连麦/Pk模型
class ShowInteractionInfo: ShowBaseInfo {
    var userId: String?                                 //用户id (rtc uid) pk是另一个房间的房主uid，连麦是连麦观众uid
    var roomId: String?                                 //用户所在房间id
    var interactStatus: ShowInteractionStatus = .idle   //交互类型
    var createdAt: Int64 = 0                            //创建时间，与19700101时间比较的毫秒数
}

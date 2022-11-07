//
//  ShowServiceModel.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/11/2.
//

import Foundation


enum ShowRoomStatus: Int {
    case activity = 0    //直播中
    case end = 1         //直播结束
}

enum ShowRoomRequestStatus: Int {
    /// 等待中
    case waitting = 1
    ///  已接受
    case accept = 2
    /// 已拒绝
    case refuse = 3
    /// 已结束
    case end = 4
}

@objcMembers
class ShowBaseInfo: NSObject {
    var objectId: String?    //SyncManager获取到的对象带的唯一标识，用于差改删
}

/// 房间列表信息
@objcMembers
class ShowRoomListModel: ShowBaseInfo {
    var roomNo: String?                           //房间号
    var roomName: String?                         //房间名
    var roomUserCount: Int = 1                    //房间人数
    var thumbnailId: String?                      //缩略图id
    var ownerId: String?                          //房主user id
    var ownerAvater: String?                      //房主头像
    var ownerName: String?                        //房主名
    var roomStatus: ShowRoomStatus = .activity    //直播状态
    var createdAt: Double = 0                     //创建时间，与19700101时间比较的毫秒数
    var updatedAt: Double = 0                     //更新时间
}


/// 房间详情信息
@objcMembers
class ShowRoomDetailModel: ShowRoomListModel {
}

///用户信息
@objcMembers
class ShowUser: ShowBaseInfo {
    var userId: String?              //用户id
    var avatar: String?              //用户头像
    var userName: String?            //用户名
}

/// 聊天消息
@objcMembers
class ShowMessage: ShowBaseInfo {
    var userId: String?        //用户id
    var userName: String?      //用户名
    var message: String?       //用户名
    var createAt: Double = 0   //创建时间，与19700101时间比较的毫秒数
}

/// 连麦申请
class ShowMicSeatApply: ShowBaseInfo {
    var userId: String?                              //用户id
    var userAvatar: String?                          //用户头像
    var userName: String?                            //用户名
    var status: ShowRoomRequestStatus = .waitting    //申请状态
    var createdAt: Double = 0                        //创建时间，与19700101时间比较的毫秒数
}


/// 连麦邀请
class ShowMicSeatInvitation: ShowBaseInfo {
    var userId: String?                              //用户id
    var userAvatar: String?                          //用户头像
    var userName: String?                            //用户名
    var fromUserId: String?                          //发起邀请用户id
    var status: ShowRoomRequestStatus = .waitting    //邀请状态
    var createdAt: Double = 0                        //创建时间，与19700101时间比较的毫秒数
}

/// PK邀请
class ShowPKInvitation: ShowBaseInfo {
    var userId: String?                              //被pk用户id
    var roomId: String?                              //被pk房间id
    var fromUserId: String?                          //发起Pk用户id
    var fromName: String?                            //发起Pk用户名
    var fromRoomId: String?                          //发起Pk房间id
    var status: ShowRoomRequestStatus = .waitting    //邀请状态
    var createdAt: Double = 0                        //创建时间，与19700101时间比较的毫秒数
}

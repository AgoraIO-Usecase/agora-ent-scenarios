//
//  TemplateServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

private let cSceneId = "scene_ChatRoom"

public enum updateRoomState {
    case activeAlien
    case announcement
    case robotVoleme
}

@objc public enum ChatRoomServiceConnectState: Int {
    case connecting = 0
    case open = 1
    case fail = 2
    case closed = 3
}

@objc public enum ChatRoomServiceKickedReason: UInt {
    case removed
    case destroyed
    case offLined
    
    func errorDesc() -> String {
        switch self {
        case .removed:
            return "voice_you_were_kicked_off_from_the_room".voice_localized()
        case .destroyed:
            return "voice_this_room_has_been_dissolved_by_the_host".voice_localized()
        case .offLined:
            return "voice_you_were_offline".voice_localized()
        default:
            return ""
        }
    }
}

@objc public protocol ChatRoomServiceSubscribeDelegate: NSObjectProtocol {
    
    /// 房间过期
    func onRoomExpired()
    
    /// 网络状态变化
    /// - Parameter state:
    func onConnectStateChanged(state: ChatRoomServiceConnectState)
    
    /// Description token 过期
    func chatTokenWillExpire()
    
    /// Description 收到文本消息
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - message: 消息模型
    func receiveTextMessage(roomId: String, message: VoiceRoomChatEntity)
    /// Description 收到礼物消息
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - meta: 透传信息
    func onReceiveGift(roomId: String, gift: VoiceRoomGiftEntity)
    /// Description 收到上麦申请消息 替换receiveApplySite
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - meta: 透传信息
    func onReceiveSeatRequest(roomId: String, applicant: VoiceRoomApply)
    
    /// Description 收到取消上麦申请消息 替换receiveCancelApplySite
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - chat_uid: 环信IMSDK用户id
    func onReceiveSeatRequestRejected(roomId: String, chat_uid: String)
    
    /// Description 收到邀请消息
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - meta: 透传信息
    func onReceiveSeatInvitation(roomId: String, user: VRUser)
    
    /// Description 收到邀请消息
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - meta: 透传信息
    func onReceiveCancelSeatInvitation(roomId: String, chat_uid: String)
    
    /// Description 用户加入聊天室回调，带所有用户信息
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - username: 用户uid或者说用户在环信IMSDK的用户名
    ///   - ext: 用户jsonobject信息
    func onUserJoinedRoom(roomId: String, user: VRUser)
    
    /// Description 聊天室公告发生变化
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - content: 公告变化内容
    func onAnnouncementChanged(roomId: String, content: String)
    
    /// Description 用户被踢
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - reason: 被踢原因
    func onUserBeKicked(roomId: String, reason: ChatRoomServiceKickedReason)
    
    /// Description 聊天室自定义麦位属性发生变化
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - attributeMap: 变换的属性kv
    ///   - fromId: 谁操作发生的变化
    func onSeatUpdated(roomId: String, mics: [VRRoomMic], from fromId: String)
    
    /// Description 机器人
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - enable: 机器人开关变化
    ///   - fromId: 操作人userName
    func onRobotSwitch(roomId: String, enable: Bool, from fromId: String)
    
    /// Description 机器人
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - volume: 机器人开关变化
    ///   - fromId: 操作人userName
    func onRobotVolumeChanged(roomId: String, volume: UInt, from fromId: String)
    
    /// Description 贡献榜单变化
    /// - Parameters:
    ///   - roomId: 聊天室id
    ///   - ranking_list: 排行榜
    ///   - fromId: 操作人userName
    func onContributionListChanged(roomId: String, ranking_list: [VRUser], from fromId: String)
    
    /// Description
    /// - Parameters: 观看数量发生变化
    ///   - roomId: 聊天室id
    ///   - count: 观看数量
    func onClickCountChanged(roomId: String, count: Int)
    
    /// Description
    /// - Parameters: 用户数量发生变化
    ///   - roomId: 聊天室id
    ///   - count: 用户数量
    func onMemberCountChanged(roomId: String, count: Int)
    
    /// Description 成员离开
    /// - Parameters:
    ///   - roomId: 环信IMSDK聊天室id
    ///   - userName: 离开的环信用户id
    func onUserLeftRoom(roomId: String, userName: String)
    
}

/// 房间内部需要用到环信KV
protocol ChatRoomServiceProtocol: NSObjectProtocol {
    /// 注册订阅
    /// - Parameter delegate: ChatRoomServiceSubscribeDelegate 聊天室内IM回调处理
    func subscribeEvent(with delegate: ChatRoomServiceSubscribeDelegate)
        
    /// 取消订阅
    /// - Parameter delegate: ChatRoomServiceSubscribeDelegate 聊天室内IM回调处理
    func unsubscribeEvent()

    /// 加入房间
    /// - Parameters:
    ///
    func joinRoom(_ roomId: String, completion: @escaping (Error?, VRRoomEntity?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///
    func leaveRoom(_ roomId: String, completion: @escaping (Error?, Bool) -> Void)
    
    /// 获取房间详情
    /// - Parameters:
    ///
    func fetchRoomDetail(entity: VRRoomEntity, completion: @escaping (Error?, VRRoomInfo?) -> Void)

    /// 获取礼物列表
    /// - Parameters:
    ///
    func fetchGiftContribute(completion: @escaping (Error?, [VRUser]?) -> Void)
    /// 获取人员列表
    /// - Parameters:
    ///
    func fetchRoomMembers(completion: @escaping (Error?, [VRUser]?) -> Void)
    
    /// Description update room members
    /// - Parameter completion: callback
    func updateRoomMembers(completion: @escaping (Error?) -> Void)
    
    /// Description 申请列表
    /// - Parameter completion: 回调
    func fetchApplicantsList(completion: @escaping (Error?, [VoiceRoomApply]?) -> Void)

    /// 禁言指定麦位
    /// - Parameters:
    ///
    func forbidMic(mic_index: Int,
                   completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// 取消禁言指定麦位
    /// - Parameters:
    ///
    func unForbidMic(mic_index: Int,
                     completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// 锁麦
    /// - Parameters:
    ///
    func lockMic(mic_index: Int,
                 completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// 取消锁麦
    /// - Parameters:
    ///
    func unLockMic(mic_index: Int,
                   completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// 踢用户下麦
    /// - Parameters:
    ///
    func kickOff(mic_index: Int,
                 completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// 下麦
    /// - Parameters:
    ///
    func leaveMic(mic_index: Int,
                  completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// mute
    /// - Parameters:
    ///
    func muteLocal(mic_index: Int,completion: @escaping (Error?, VRRoomMic?) -> Void)

    /// unmute
    /// - Parameters:
    ///
    func unmuteLocal(mic_index: Int, completion: @escaping (Error?, VRRoomMic?) -> Void)
    
    
    /// Description  更新当前上麦用户对自己麦克状态
    /// - Parameters:
    ///   - status: 0 自主静麦 1主动开麦
    ///   - completion: 回调
    func changeMicUserStatus(status: Int,completion: @escaping (Error?, VRRoomMic?) -> Void)
    /// 换麦
    /// - Parameters:
    ///
    func changeMic(old_index: Int,new_index:Int,completion: @escaping (Error?, [Int:VRRoomMic]?) -> Void)
    
    /// 邀请上卖
    /// - Parameters:
    ///
    func startMicSeatInvitation(chatUid: String,index: Int?,completion: @escaping (Error?, Bool) -> Void)

    /// 接受邀请
    /// - Parameters:
    ///
    func acceptMicSeatInvitation(index: Int?,completion: @escaping (Error?, VRRoomMic?) -> Void)
    
    /// 拒绝邀请
    /// - Parameters:
    ///
    func refuseInvite(chat_uid: String,completion: @escaping (Error?, Bool) -> Void)

    /// 申请上麦
    /// - Parameters:
    ///
    func startMicSeatApply(index: Int?,completion: @escaping (Error?, Bool) -> Void)

    /// 取消上麦
    /// - Parameters:
    ///
    func cancelMicSeatApply(chat_uid: String,
                     completion: @escaping (Error?, Bool) -> Void)
    
    /// Description 同意申请
    /// - Parameters:
    ///   - user: VRUser instance
    ///   - completion: 回调
    func acceptMicSeatApply(chatUid: String, completion: @escaping (Error?,VRRoomMic?) -> Void)

    /// 获取房间列表
    /// - Parameters:
    ///   - page: 分页索引，从0开始(由于SyncManager无法进行分页，这个属性暂时无效)
    ///   - completion: 完成回调   (错误信息， 房间列表)
    func fetchRoomList(page: Int, completion: @escaping (Error?, [VRRoomEntity]?) -> Void)
    
    /// 创建房间
    /// - Parameters:
    ///   - room: 房间对象信息
    ///   - completion: 完成回调   (错误信息)
    func createRoom(room: VRRoomEntity, completion: @escaping (Error?, VRRoomEntity?) -> Void)
    
    /// Description 更新公告
    /// - Parameters:
    ///   - content: content
    ///   - completion: 回调
    func updateAnnouncement(content: String,completion: @escaping (Bool) -> Void)
    
    /// Description 是否启用机器人
    /// - Parameter enable: true or false
    func enableRobot(enable: Bool,completion: @escaping (Error?) -> Void)
    
    /// Description 更新机器人音量
    /// - Parameter value: 音量值
    func updateRobotVolume(value: Int,completion: @escaping (Error?) -> Void)
    
}

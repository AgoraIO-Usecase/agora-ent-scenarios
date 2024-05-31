//
//  AUIUserServiceDelegate.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/7.
//

import Foundation

@objc public protocol AUIUserServiceDelegate: AUICommonServiceDelegate {
    
    /// 绑定响应
    /// - Parameter delegate: 需要回调的对象
    func bindRespDelegate(delegate: AUIUserRespDelegate)
    
    /// 解绑协议
    /// - Parameter delegate: 需要回调的对象
    func unbindRespDelegate(delegate: AUIUserRespDelegate)
    
    /// 获取指定频道的所有用户信息
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - callback: 操作完成回调
    func getUserInfoList(roomId: String, callback: @escaping AUIUserListCallback)

    /// 获取用户信息
    /// - Parameter userId: <#userId description#>
    /// - Returns: <#description#>
//    func getUserInfo(by userId: String) -> AUIUserThumbnailInfo?
    
    /// 对自己静音/解除静音
    /// - Parameters:
    ///   - isMute: true: 关闭麦克风 false: 开启麦克风
    ///   - callback: 操作完成回调
    func muteUserAudio(isMute: Bool, callback: @escaping AUICallback)
    
    /// 对自己禁摄像头/解禁摄像头
    /// - Parameters:
    ///   - isMute: true: 关闭摄像头 false: 开启摄像头
    ///   - callback: 操作完成回调
    func muteUserVideo(isMute: Bool, callback: @escaping AUICallback)
    
    /// Description 踢出用户
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userId: 被踢用户id
    func kickUser(roomId: String ,userId: String, callback: @escaping AUICallback)
}

@objc public protocol AUIUserRespDelegate: NSObjectProtocol {
    
    /// 用户进入房间后获取到的所有用户信息
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userList: 用户列表
    func onRoomUserSnapshot(roomId: String, userList: [AUIUserInfo])
    
    /// 用户进入房间回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo: 用户信息
    func onRoomUserEnter(roomId: String, userInfo: AUIUserInfo)
    
    ///  用户离开房间回调
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo:  用户信息
    func onRoomUserLeave(roomId: String, userInfo: AUIUserInfo)
    
    /// 用户的信息被修改
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userInfo: 用户信息
    func onRoomUserUpdate(roomId: String, userInfo: AUIUserInfo)
    
    /// 用户关闭/开启了麦克风
    /// - Parameters:
    ///   - userId: 用户id
    ///   - mute: 静音状态
    func onUserAudioMute(userId: String, mute: Bool)
    
    /// 用户关闭/开启了摄像头
    /// - Parameters:
    ///   - userId: 用户id
    ///   - mute: 摄像头状态
    func onUserVideoMute(userId: String, mute: Bool)
    
    /// Description 用户被踢出房间
    /// - Parameters:
    ///   - roomId: 房间id
    ///   - userId: 用户id
    func onUserBeKicked(roomId: String, userId: String)
    
}

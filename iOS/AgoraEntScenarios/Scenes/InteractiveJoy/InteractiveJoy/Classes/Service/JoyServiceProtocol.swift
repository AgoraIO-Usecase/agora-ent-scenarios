//
//  JoyServiceProtocol.swift
//  Joy
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation


@objc enum JoyServiceNetworkStatus: Int {
    case connecting = 0 // 连接中
    case open // 已打开
    case fail // 失败
    case closed // 已关闭
}

protocol JoyServiceListenerProtocol: NSObjectProtocol {
    
    /// 网络状况变化
    /// - Parameter status: <#status description#>
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus)
    
    /// 用户变化
    /// - Parameter userList: <#userList description#>
    func onUserListDidChanged(userList: [InteractiveJoyUserInfo])
    
    /// 房间被销毁
    /// - Parameter roomInfo: <#roomInfo description#>
    func onRoomDidDestroy(roomInfo: InteractiveJoyRoomInfo)
}

protocol JoyServiceProtocol: NSObjectProtocol {
    
    /// 创建房间
    /// - Parameters:
    ///   - gameRoomInfo: <#roomName description#>
    ///   - completion: <#completion description#>
//    func createRoom(roomName: String, gameId: Int64, password: String, completion: @escaping (InteractiveJoyRoomInfo?, Error?) -> Void)
    func createRoom(gameRoomInfo: InteractiveJoyRoomInfo, completion: @escaping (InteractiveJoyRoomInfo?, Error?) -> Void)
    /// 加入一个房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func joinRoom(roomInfo:InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func leaveRoom(roomInfo:InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 获取房间列表
    /// - Parameter completion: 完成回调
    func getRoomList(completion: @escaping ([InteractiveJoyRoomInfo]) -> Void)

    /// 订阅回调变化
    /// - Parameter listener: <#listener description#>
    func subscribeListener(listener: JoyServiceListenerProtocol?)
    
}

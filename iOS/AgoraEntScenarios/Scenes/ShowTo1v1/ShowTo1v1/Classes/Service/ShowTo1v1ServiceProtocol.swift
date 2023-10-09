//
//  ShowTo1v1ServiceProtocol.swift
//  ShowTo1v1
//
//  Created by wushengtao on 2023/7/27.
//

import Foundation


@objc enum ShowTo1v1ServiceNetworkStatus: Int {
    case connecting = 0 // 连接中
    case open // 已打开
    case fail // 失败
    case closed // 已关闭
}

protocol ShowTo1v1ServiceListenerProtocol: NSObjectProtocol {
    
    /// 网络状况变化
    /// - Parameter status: <#status description#>
    func onNetworkStatusChanged(status: ShowTo1v1ServiceNetworkStatus)
    
    /// 用户变化
    /// - Parameter userList: <#userList description#>
    func onUserListDidChanged(userList: [ShowTo1v1UserInfo])
    
    
    /// <#Description#>
    /// - Parameter roomInfo: <#roomInfo description#>
    func onRoomDidDestroy(roomInfo: ShowTo1v1RoomInfo)
}

protocol ShowTo1v1ServiceProtocol: NSObjectProtocol {
    
    /// 创建房间
    /// - Parameters:
    ///   - roomName: <#roomName description#>
    ///   - completion: <#completion description#>
    func createRoom(roomName: String, completion: @escaping (ShowTo1v1RoomInfo?, Error?) -> Void)
    
    /// 加入一个房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func joinRoom(roomInfo:ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func leaveRoom(roomInfo:ShowTo1v1RoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 获取房间列表
    /// - Parameter completion: 完成回调
    func getRoomList(completion: @escaping ([ShowTo1v1RoomInfo]) -> Void)
    
    /// 订阅回调变化
    /// - Parameter listener: <#listener description#>
    func subscribeListener(listener: ShowTo1v1ServiceListenerProtocol?)
    
}

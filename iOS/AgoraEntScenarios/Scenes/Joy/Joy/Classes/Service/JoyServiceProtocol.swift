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
    func onUserListDidChanged(userList: [JoyUserInfo])
    
    /// 接收到新消息
    /// - Parameter message: <#message description#>
    func onMessageDidAdded(message: JoyMessage)
    
    /// 房间内进行的游戏变化
    /// - Parameter gameInfo: <#gameInfo description#>
    func onStartGameInfoDidChanged(startGameInfo: JoyStartGameInfo)
    
    /// 房间信息变化
    /// - Parameter roomInfo: <#roomInfo description#>
    func onRoomDidChanged(roomInfo: JoyRoomInfo)
    
    /// 房间被销毁
    /// - Parameter roomInfo: <#roomInfo description#>
    func onRoomDidDestroy(roomInfo: JoyRoomInfo)
}

protocol JoyServiceProtocol: NSObjectProtocol {
    
    /// 创建房间
    /// - Parameters:
    ///   - roomName: <#roomName description#>
    ///   - completion: <#completion description#>
    func createRoom(roomName: String, completion: @escaping (JoyRoomInfo?, Error?) -> Void)
    
    /// 加入一个房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func joinRoom(roomInfo:JoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func leaveRoom(roomInfo:JoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 获取房间列表
    /// - Parameter completion: 完成回调
    func getRoomList(completion: @escaping ([JoyRoomInfo]) -> Void)
    
    /// 修改房间信息
    /// - Parameters:
    ///   - roomInfo: <#roomInfo description#>
    ///   - completion: <#completion description#>
    func updateRoom(roomInfo: JoyRoomInfo, completion: @escaping (NSError?) -> Void)
    
    
    /// 更新正在进行的游戏信息
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - completion: <#completion description#>
    func getStartGame(roomId: String, completion: @escaping (NSError?, JoyStartGameInfo?) -> Void) 
    
    /// 更新正在进行的游戏信息
    /// - Parameters:
    ///   - roomId: <#roomId description#>
    ///   - gameInfo: <#gameInfo description#>
    ///   - completion: <#completion description#>
    func updateStartGame(roomId: String,
                         gameInfo: JoyStartGameInfo,
                         completion: @escaping (NSError?) -> Void)
    
    /// 发送聊天消息
    /// - Parameters:
    ///   - roomId: roomId description
    ///   - message: message description
    ///   - completion: completion description
    func sendChatMessage(roomId: String,
                         message: String,
                         completion: @escaping (NSError?) -> Void)
    
    /// 订阅回调变化
    /// - Parameter listener: <#listener description#>
    func subscribeListener(listener: JoyServiceListenerProtocol?)
    
}

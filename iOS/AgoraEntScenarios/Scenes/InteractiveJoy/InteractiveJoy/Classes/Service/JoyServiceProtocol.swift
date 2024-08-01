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
    /// - Parameter status: 网络状态信息
    func onNetworkStatusChanged(status: JoyServiceNetworkStatus)
    
    /// 用户变化
    /// - Parameter userList: 用户列表
    func onUserListDidChanged(userList: [InteractiveJoyUserInfo])
    
    /// 房间被销毁
    /// - Parameter roomInfo: 房间信息
    func onRoomDidDestroy(roomInfo: InteractiveJoyRoomInfo)
    
    /// 房间机器人已同步
    /// - Parameter robots: 机器人数据
    func onRoomRobotDidLoad(robots: [PlayRobotInfo])
}

protocol JoyServiceProtocol: NSObjectProtocol {
    
    /// 创建房间
    /// - Parameters:
    ///   - gameRoomInfo: 房间信息
    ///   - completion: callback
    func createRoom(gameRoomInfo: InteractiveJoyRoomInfo, completion: @escaping (InteractiveJoyRoomInfo?, Error?) -> Void)
    
    /// 加入一个房间
    /// - Parameters:
    ///   - roomInfo: 房间信息
    ///   - completion: callback
    func joinRoom(roomInfo:InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 更新房间信息
    /// - Parameters:
    ///   - roomInfo: 房间信息
    ///   - completion: callback
    func updateRoom(roomInfo: InteractiveJoyRoomInfo, completion: @escaping (NSError?) -> Void)
    
    /// 离开房间
    /// - Parameters:
    ///   - roomInfo: 房间信息
    ///   - completion: callback
    func leaveRoom(roomInfo:InteractiveJoyRoomInfo, completion: @escaping (Error?) -> Void)
    
    /// 获取房间列表
    /// - Parameter completion: callback
    func getRoomList(completion: @escaping ([InteractiveJoyRoomInfo]) -> Void)
    
    ///获取机器人列表
    func getRobotList() -> [PlayRobotInfo]
    
    /// 订阅回调变化
    /// - Parameter listener: 代理对象
    func subscribeListener(listener: JoyServiceListenerProtocol?)
    
}

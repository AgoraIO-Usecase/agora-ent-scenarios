//
//  Pure1v1ServiceProtocol.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/20.
//

import Foundation


@objc enum Pure1v1ServiceNetworkStatus: Int {
    case connecting = 0 // 连接中
    case open // 已打开
    case fail // 失败
    case closed // 已关闭
}

protocol Pure1v1ServiceProtocol: NSObjectProtocol {
    
    /// 加入房间(默认所有人都进入一个房间，不需要房间id)
    /// - Parameters:
    ///   - completion: 完成回调
    func joinRoom(completion: @escaping (Error?) -> Void)
    
    /// 离开房间
    /// - Parameter completion: 完成回调
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    
    /// 获取用户列表
    /// - Parameter completion: 完成回调
    func getUserList(completion: @escaping ([Pure1v1UserInfo]) -> Void)
    
    
    /// 订阅网络状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (Pure1v1ServiceNetworkStatus) -> Void)
    
    /// 订阅房间过期
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void)
    
    /// 取消全部订阅
    func unsubscribeAll()
}

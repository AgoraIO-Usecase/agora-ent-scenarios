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

protocol ShowTo1v1ServiceProtocol: NSObjectProtocol {
    
    
    /// 把自己加入1v1列表
    /// - Parameter completion: <#completion description#>
    func createRoom(completion: @escaping (Error?) -> Void)
    
    
    /// 把自己移除出1v1列表
    /// - Parameter completion: <#completion description#>
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    
    /// 获取房间列表
    /// - Parameter completion: 完成回调
    func getRoomList(completion: @escaping ([ShowTo1v1RoomInfo]) -> Void)
    
    
    /// 订阅网络状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (ShowTo1v1ServiceNetworkStatus) -> Void)
    
    /// 订阅房间过期
    /// - Parameter changedBlock: 变化回调
    func subscribeRoomWillExpire(with changedBlock: @escaping () -> Void)
    
    /// 取消全部订阅
    func unsubscribeAll()
}

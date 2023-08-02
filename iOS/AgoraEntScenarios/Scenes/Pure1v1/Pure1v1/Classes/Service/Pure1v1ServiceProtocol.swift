//
//  Pure1v1ServiceProtocol.swift
//  Pure1v1
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
    
    
    /// 把自己加入1v1列表
    /// - Parameter completion: <#completion description#>
    func enterRoom(completion: @escaping (Error?) -> Void)
    
    
    /// 把自己移除出1v1列表
    /// - Parameter completion: <#completion description#>
    func leaveRoom(completion: @escaping (Error?) -> Void)
    
    
    /// 获取1v1用户列表
    /// - Parameter completion: 完成回调
    func getUserList(completion: @escaping ([Pure1v1UserInfo]) -> Void)
    
    /// 订阅网络状态变化
    /// - Parameter changedBlock: 变化回调
    func subscribeNetworkStatusChanged(with changedBlock: @escaping (Pure1v1ServiceNetworkStatus) -> Void)
    
    /// 取消全部订阅
    func unsubscribeAll()
}

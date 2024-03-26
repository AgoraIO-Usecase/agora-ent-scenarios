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
    func enterRoom(completion: @escaping (NSError?) -> Void)
    
    
    /// 把自己移除出1v1列表
    /// - Parameter completion: <#completion description#>
    func leaveRoom(completion: @escaping (NSError?) -> Void)
    
    
    /// 获取1v1用户列表
    /// - Parameter completion: 完成回调
    func getUserList(completion: @escaping ([Pure1v1UserInfo], NSError?) -> Void)
    
    /// 订阅用户列表变化
    /// - Parameter changedBlock: <#changedBlock description#>
    func subscribeUserListChanged(with changedBlock: (([Pure1v1UserInfo]) -> Void)?)
    
    /// 取消全部订阅
    func unsubscribeAll()
}

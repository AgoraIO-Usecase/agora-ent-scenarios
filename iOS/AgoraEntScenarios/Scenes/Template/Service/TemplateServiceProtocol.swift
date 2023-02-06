//
//  TemplateServiceProtocol.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import AgoraSyncManager

protocol TemplateServiceProtocol: NSObjectProtocol {
    /// 加入房间
    /// - Parameters:
    ///   - roomName: 房间名
    ///   - completion: <#completion description#>
    func join(roomName: String, completion: @escaping (SyncError?, TemplateScene.JoinResponse?) -> Void)

    /// 离开房间(观众)
    func leave()

    /// 删除房间(主播)
    func removeRoom()

    /// 添加用户
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func addUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, TemplateScene.UsersModel?) -> Void)

    /// 删除一个用户
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func removeUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, [TemplateScene.UsersModel]?) -> Void)

    /// 修改用户信息
    /// - Parameters:
    ///   - user: <#user description#>
    ///   - completion: <#completion description#>
    func updateUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, TemplateScene.UsersModel?) -> Void)

    /// 获取所有用户
    /// - Parameter completion: <#completion description#>
    func getUserStatus(completion: @escaping (SyncError?, [TemplateScene.UsersModel]?) -> Void)

    /// 监听房间变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    ///   - onSubscribed: <#onSubscribed description#>
    ///   - fail: <#fail description#>
    func subscribeRoom(subscribeClosure: @escaping (TemplateScene.SubscribeStatus, TemplateScene.LiveRoomInfo?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)

    /// 监听用户变化
    /// - Parameters:
    ///   - subscribeClosure: <#subscribeClosure description#>
    ///   - onSubscribed: <#onSubscribed description#>
    ///   - fail: <#fail description#>
    func subscribeUser(subscribeClosure: @escaping (TemplateScene.SubscribeStatus, TemplateScene.UsersModel?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)

    /// 取消监听
    func unsubscribe()
}

//
//  DocumentReference.swift
//  SyncManager
//
//  Created by ZYP on 2021/12/16.
//

import Foundation

public class SceneReference: DocumentReference {
    override public var className: String {
        id
    }

    init(manager: AgoraSyncManager, id: String, type: ProviderType = .rtm) {
        super.init(manager: manager, parent: nil, type: type, id: id)
    }

    /// 创建一个CollectionReference实体
    /// - Parameter className: CollectionReference 的 id
    public func collection(className: String) -> CollectionReference {
        switch providerType {
        case .rtm:
            return CollectionReference(manager: manager,
                                       parent: self,
                                       className: className)
        }
    }

    /// delete current scene
    override public func delete(success: SuccessBlock? = nil,
                                fail: FailBlock? = nil)
    {
        manager.delete(documentRef: self,
                       success: success,
                       fail: fail)
    }

    public func deleteScenes() {
        manager.deleteScenes(sceneIds: [id], success: { _ in 
            Log.info(text: "deleteScenes success", tag: "SceneReference")
        }, fail: { error in
            Log.error(error: error, tag: "SceneReference")
        })
    }

    public func subscribeScene(onUpdated: OnSubscribeBlock? = nil,
                               onDeleted: OnSubscribeBlock? = nil,
                               fail: FailBlock? = nil)
    {
        manager.subscribeScene(reference: self,
                               onUpdated: onUpdated,
                               onDeleted: onDeleted,
                               fail: fail)
    }

    public func unsubscribeScene(fail: FailBlock? = nil) {
        manager.unsubscribeScene(reference: self, fail: fail)
    }
}

public class DocumentReference {
    public let id: String
    public let parent: CollectionReference?
    let manager: AgoraSyncManager
    let providerType: ProviderType

    public var className: String {
        return parent!.className
    }

    init(manager: AgoraSyncManager,
         parent: CollectionReference?,
         type: ProviderType = .rtm,
         id: String)
    {
        self.manager = manager
        self.parent = parent
        self.id = id
        providerType = type
    }

    /// 获取指定属性值
    /// - Parameters:
    ///   - key: 非空字符串
    public func get(key: String,
                    success: SuccessBlockObjOptional? = nil,
                    fail: FailBlock? = nil)
    {
        manager.get(documentRef: self,
                    key: key,
                    success: success,
                    fail: fail)
    }

    /// 更新指定属性值
    /// - Parameters:
    ///   - key: 键值 非空字符串
    ///   - data: value
    public func update(key: String,
                       data: [String: Any?],
                       success: SuccessBlock? = nil,
                       fail: FailBlock? = nil)
    {
        manager.update(reference: self,
                       key: key,
                       data: data,
                       success: success,
                       fail: fail)
    }

    /// 删除房间
    public func delete(success: SuccessBlock? = nil,
                       fail: FailBlock? = nil)
    {
        manager.delete(documentRef: self,
                       success: success,
                       fail: fail)
    }

    /// 订阅属性更新事件
    /// - Parameters:
    ///   - key: 键值 非空字符串。(当监听collection的时候可以使用空字符串)
    public func subscribe(key: String,
                          onCreated: OnSubscribeBlock? = nil,
                          onUpdated: OnSubscribeBlock? = nil,
                          onDeleted: OnSubscribeBlock? = nil,
                          onSubscribed: OnSubscribeBlockVoid? = nil,
                          fail: FailBlock? = nil)
    {
        manager.subscribe(reference: self,
                          key: key,
                          onCreated: onCreated,
                          onUpdated: onUpdated,
                          onDeleted: onDeleted,
                          onSubscribed: onSubscribed,
                          fail: fail)
    }

    /// 取消订阅
    /// - Parameter key: 键值 非空字符串 (当监听collection的时候可以使用空字符串)
    public func unsubscribe(key: String) {
        manager.unsubscribe(reference: self, key: key)
    }
}

enum ProviderType {
    case rtm
}

//
//  AgoraSyncManager.swift
//  AgoraSyncManager
//
//  Created by xianing on 2021/9/12.
//

import Foundation

public class AgoraSyncManager: NSObject {
    private var proxy: ISyncManager

    deinit {
        Log.info(text: "AgoraSyncManager deinit", tag: "AgoraSyncManager")
    }

    /// init
    /// - Parameters:
    ///   - config: config of rtm
    ///   - complete: `code = 0` is success, else error
    public init(config: RtmConfig,
                complete: @escaping SuccessBlockInt)
    {
        let tempConfig = RtmSyncManager.Config(appId: config.appId,
                                               channelName: config.channelName)
        proxy = RtmSyncManager(config: tempConfig,
                               complete: complete)
    }

    /// init
    /// - Parameters:
    ///   - config: config of rtm
    ///   - complete: `code = 0` is success, else error
    public init(config: RethinkConfig,
                complete: @escaping SuccessBlockInt)
    {
        let tempConfig = RethinkSyncManager.Config(appId: config.appId,
                                                   channelName: config.channelName)
        proxy = RethinkSyncManager(config: tempConfig,
                                   complete: complete)
    }

    public func createScene(scene: Scene,
                            success: SuccessBlockVoid?,
                            fail: FailBlock?)
    {
        proxy.createScene(scene: scene,
                          success: success,
                          fail: fail)
    }

    /// 加入房间
    public func joinScene(sceneId: String,
                          success: SuccessBlockObjSceneRef?,
                          fail: FailBlock?)
    {
        proxy.joinScene(sceneId: sceneId,
                        manager: self,
                        success: success,
                        fail: fail)
    }

    /// 获取房间列表
    public func getScenes(success: SuccessBlock? = nil,
                          fail: FailBlock? = nil)
    {
        proxy.getScenes(success: success,
                        fail: fail)
    }

    /// 删除指定的房间(在房间列表)
    /// - Parameters:
    ///   - attributesByKeys: 房间id列表
    public func deleteScenes(sceneIds: [String],
                             success: SuccessBlockObjOptional? = nil,
                             fail: FailBlock? = nil)
    {
        proxy.deleteScenes(sceneIds: sceneIds,
                           success: success,
                           fail: fail)
    }

    /// 获取指定属性
    /// - Parameters:
    ///   - documentRef: `Document`类型实体
    ///   - key: 键值
    public func get(documentRef: DocumentReference,
             key: String,
             success: SuccessBlockObjOptional?,
             fail: FailBlock?)
    {
        proxy.get(documentRef: documentRef,
                  key: key,
                  success: success,
                  fail: fail)
    }

    /// 获取所有数据（Collection）
    /// - Parameters:
    ///   - collectionRef: `Collection`类型实体
    public func get(collectionRef: CollectionReference,
             success: SuccessBlock?,
             fail: FailBlock?)
    {
        proxy.get(collectionRef: collectionRef,
                  success: success,
                  fail: fail)
    }

    /// 新增一项数据（Collection）
    /// - Parameters:
    ///   - reference: `Collection`类型实体
    ///   - data: 数据
    public func add(reference: CollectionReference,
             data: [String: Any?],
             success: SuccessBlockObj?,
             fail: FailBlock?)
    {
        proxy.add(reference: reference,
                  data: data,
                  success: success,
                  fail: fail)
    }

    public func update(reference: CollectionReference,
                id: String,
                data: [String: Any?],
                success: SuccessBlockVoid?,
                fail: FailBlock?)
    {
        proxy.update(reference: reference,
                     id: id,
                     data: data,
                     success: success,
                     fail: fail)
    }

    public func delete(reference: CollectionReference,
                id: String,
                success: SuccessBlockObjOptional?,
                fail: FailBlock?)
    {
        proxy.delete(reference: reference,
                     id: id,
                     success: success,
                     fail: fail)
    }

    /// 更新或者增加数据（Document）
    /// - Parameters:
    ///   - reference: `Document`类型实体
    ///   - key: 键值
    ///   - data: 数据
    public func update(reference: DocumentReference,
                key: String,
                data: [String: Any?],
                success: SuccessBlock?,
                fail: FailBlock?)
    {
        proxy.update(reference: reference,
                     key: key,
                     data: data,
                     success: success,
                     fail: fail)
    }

    /// 删除一个document
    /// - Parameters:
    ///   - documentRef: 要删除的`Document`
    public func delete(documentRef: DocumentReference,
                success: SuccessBlock?,
                fail: FailBlock?)
    {
        proxy.delete(documentRef: documentRef,
                     success: success,
                     fail: fail)
    }

    /// 删除一个Collection
    /// - Parameters:
    ///   - collectionRef: 要删除的`Collection`
    public func delete(collectionRef: CollectionReference,
                success: SuccessBlock?,
                fail: FailBlock?)
    {
        proxy.delete(collectionRef: collectionRef,
                     success: success,
                     fail: fail)
    }

    /// 订阅属性的更新事件
    /// - Parameters:
    ///   - reference: `Document`类型
    ///   - key: 键值
    public func subscribe(reference: DocumentReference,
                   key: String?,
                   onCreated: OnSubscribeBlock?,
                   onUpdated: OnSubscribeBlock?,
                   onDeleted: OnSubscribeBlock?,
                   onSubscribed: OnSubscribeBlockVoid?,
                   fail: FailBlock?)
    {
        let key = key ?? "scene"
        return proxy.subscribe(reference: reference,
                               key: key,
                               onCreated: onCreated,
                               onUpdated: onUpdated,
                               onDeleted: onDeleted,
                               onSubscribed: onSubscribed,
                               fail: fail)
    }

    /// 取消订阅
    /// - Parameters:
    ///   - reference: `Document`类型
    ///   - key: 键值
    public func unsubscribe(reference: DocumentReference,
                     key: String)
    {
        proxy.unsubscribe(reference: reference, key: key)
    }

    public func subscribeScene(reference: SceneReference,
                        onUpdated: OnSubscribeBlock?,
                        onDeleted: OnSubscribeBlock? = nil,
                        fail: FailBlock? = nil)
    {
        proxy.subscribeScene(reference: reference,
                             onUpdated: onUpdated,
                             onDeleted: onDeleted,
                             fail: fail)
    }

    public func unsubscribeScene(reference: SceneReference, fail: FailBlock? = nil) {
        proxy.unsubscribeScene(reference: reference, fail: fail)
    }
    
    public func subscribeConnectState(state: @escaping ConnectBlockState) {
        proxy.subscribeConnectState(state: state)
    }
}

//
//  Protocols.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import Foundation

protocol ISyncManager {
    func createScene(scene: Scene,
                     success: SuccessBlockVoid?,
                     fail: FailBlock?)
    func joinScene(sceneId: String,
                   manager: AgoraSyncManager,
                   success: SuccessBlockObjSceneRef?,
                   fail: FailBlock?)
    func getScenes(success: SuccessBlock?,
                   fail: FailBlock?)
    func deleteScenes(sceneIds: [String],
                      success: SuccessBlockObjOptional?,
                      fail: FailBlock?)
    func get(documentRef: DocumentReference,
             key: String,
             success: SuccessBlockObjOptional?,
             fail: FailBlock?)
    func get(collectionRef: CollectionReference,
             success: SuccessBlock?,
             fail: FailBlock?)
    func add(reference: CollectionReference,
             data: [String: Any?],
             success: SuccessBlockObj?,
             fail: FailBlock?)
    func update(reference: CollectionReference,
                id: String,
                data: [String: Any?],
                success: SuccessBlockVoid?,
                fail: FailBlock?)
    func delete(reference: CollectionReference,
                id: String,
                success: SuccessBlockObjOptional?,
                fail: FailBlock?)
    func update(reference: DocumentReference,
                key: String,
                data: [String: Any?],
                success: SuccessBlock?,
                fail: FailBlock?)
    func delete(documentRef: DocumentReference,
                success: SuccessBlock?,
                fail: FailBlock?)
    func delete(collectionRef: CollectionReference,
                success: SuccessBlock?,
                fail: FailBlock?)
    func subscribe(reference: DocumentReference,
                   key: String,
                   onCreated: OnSubscribeBlock?,
                   onUpdated: OnSubscribeBlock?,
                   onDeleted: OnSubscribeBlock?,
                   onSubscribed: OnSubscribeBlockVoid?,
                   fail: FailBlock?)
    func unsubscribe(reference: DocumentReference, key: String) -> Void

    func subscribeScene(reference: SceneReference,
                        onUpdated: OnSubscribeBlock?,
                        onDeleted: OnSubscribeBlock?,
                        fail: FailBlock?)
    func unsubscribeScene(reference: SceneReference,
                          fail: FailBlock?)
    
    func subscribeConnectState(state: @escaping  ConnectBlockState)
}

public protocol IObject {
    func getId() -> String
    func toObject<T>() throws -> T? where T: Decodable
    func getPropertyWith(key: String, type: Any.Type) -> Any?
    func toJson() -> String?
}

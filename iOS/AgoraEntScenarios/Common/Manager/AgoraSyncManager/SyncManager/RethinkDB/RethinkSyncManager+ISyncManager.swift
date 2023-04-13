//
//  RethinkSyncManager+ISyncManager.swift
//  AgoraSyncManager
//
//  Created by zhaoyongqiang on 2022/7/5.
//

import UIKit

extension RethinkSyncManager: ISyncManager {
    func subscribeConnectState(state: @escaping (SocketConnectState) -> Void) {
        connectStateBlock = state
    }
    func createScene(scene: Scene, success: SuccessBlockVoid?, fail: FailBlock?) {
        /** add room in list **/
        let attr = Attribute(key: scene.id, value: scene.toJson())
        sceneName = scene.id
        write(channelName: channelName, data: attr.toJson(), objectId: sceneName)
        success?()
    }

    func joinScene(sceneId: String, manager: AgoraSyncManager, success: SuccessBlockObjSceneRef?, fail: FailBlock?) {
        let sceneRef = SceneReference(manager: manager,
                                      id: sceneId)
        success?(sceneRef)
    }

    func get(documentRef: DocumentReference, key: String, success: SuccessBlockObjOptional?, fail: FailBlock?) {
        onSuccessBlockObjOptional[documentRef.className + key] = success
        onFailBlock[documentRef.className + key] = fail
        query(channelName: documentRef.className + key)
    }

    func update(reference: DocumentReference, key: String, data: [String: Any?], success: SuccessBlock?, fail: FailBlock?) {
        let className = (reference.className + key) == sceneName ? channelName : reference.className + key
        onSuccessBlock[className] = success
        onFailBlock[className] = fail
        write(channelName: className, data: data, objectId: data["objectId"] as? String)
    }

    func subscribe(reference: DocumentReference, key: String, onCreated: OnSubscribeBlock?, onUpdated: OnSubscribeBlock?, onDeleted: OnSubscribeBlock?, onSubscribed: OnSubscribeBlockVoid?, fail: FailBlock?) {
        let className = (reference.className + key) == sceneName ? channelName : reference.className + key
        print("className == \(className)")
        onCreateBlocks[className] = onCreated
        onUpdatedBlocks[className] = onUpdated
        onDeletedBlocks[className] = onDeleted
        subscribe(channelName: className)
        onSubscribed?()
    }

    func unsubscribe(reference: DocumentReference, key: String) {
        let className = (reference.className + key) == sceneName ? channelName : reference.className + key
        unsubscribe(channelName: className)
        onCreateBlocks.removeValue(forKey: className)
        onUpdatedBlocks.removeValue(forKey: className)
        onDeletedBlocks.removeValue(forKey: className)
    }

    func subscribeScene(reference: SceneReference, onUpdated: OnSubscribeBlock?, onDeleted: OnSubscribeBlock?, fail: FailBlock?) {
        onFailBlock[channelName] = fail
        onUpdatedBlocks[channelName] = onUpdated
        onDeletedBlocks[channelName] = onDeleted
        subscribe(channelName: channelName)
    }

    func unsubscribeScene(reference: SceneReference, fail: FailBlock?) {
        onDeletedBlocks.removeValue(forKey: channelName)
        onFailBlock.removeValue(forKey: channelName)
        unsubscribe(channelName: channelName)
    }

    func getScenes(success: SuccessBlock?, fail: FailBlock?) {
        onSuccessBlock[channelName] = success
        onFailBlock[channelName] = fail
        query(channelName: channelName)
    }

    func deleteScenes(sceneIds: [String], success: SuccessBlockObjOptional?, fail: FailBlock?) {
        let params = sceneIds.map({ ["objectId": $0] })
        onDeleteBlockObjOptional[channelName] = success
        onFailBlock[channelName] = fail
        delete(channelName: channelName, data: params)
    }

    func get(collectionRef: CollectionReference, success: SuccessBlock?, fail: FailBlock?) {
        onSuccessBlock[collectionRef.className] = success
        onFailBlock[collectionRef.className] = fail
        query(channelName: collectionRef.className)
    }

    func add(reference: CollectionReference, data: [String: Any?], success: SuccessBlockObj?, fail: FailBlock?) {
        onSuccessBlockObj[reference.className] = success
        onFailBlock[reference.className] = fail
        let objectId = data["objectId"] as? String ?? UUID().uuid16string()
        var parasm = data
        parasm["objectId"] = objectId
        write(channelName: reference.className, data: parasm, objectId: objectId, isAdd: true)
    }

    func update(reference: CollectionReference, id: String, data: [String: Any?], success: SuccessBlockVoid?, fail: FailBlock?) {
        let className = reference.className == sceneName ? channelName : reference.className
        onSuccessBlockVoid[className] = success
        onFailBlock[className] = fail
        write(channelName: reference.className, data: data, objectId: id)
    }

    func delete(reference: CollectionReference, id: String, success: SuccessBlockObjOptional?, fail: FailBlock?) {
        let className = reference.className == sceneName ? channelName : reference.className
        print("channelName == \(channelName)")
        onDeleteBlockObjOptional[className] = success
        onFailBlock[className] = fail
        delete(channelName: className, data: ["objectId": id])
    }

    func delete(documentRef: DocumentReference, success: SuccessBlock?, fail: FailBlock?) {
        let keys = documentRef.id.isEmpty ? nil : [documentRef.id]
        let className = documentRef.className == sceneName ? channelName : documentRef.className
        onSuccessBlock[className] = success
        onFailBlock[className] = fail
        if let keys = keys {
            let params = keys.map({ ["objectId": $0] })
            delete(channelName: className, data: params)
        }
    }

    func delete(collectionRef: CollectionReference, success: SuccessBlock?, fail: FailBlock?) {
        let className = collectionRef.className == sceneName ? channelName : collectionRef.className
        onSuccessBlock[className] = success
        onFailBlock[className] = fail
        delete(channelName: className, data: [])
    }
}

//
//  TemplateServiceImp.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

class TemplateServiceImp: NSObject {
    var channelName: String?
}

extension TemplateServiceImp: TemplateServiceProtocol {
    func join(roomName: String, completion: @escaping (SyncError?, TemplateScene.JoinResponse?) -> Void) {
        let roomInfo = TemplateScene.LiveRoomInfo(roomName: roomName)
        let params = JSONObject.toJson(roomInfo)

        SyncUtil.joinScene(id: roomInfo.roomId,
                           userId: roomInfo.userId,
                           property: params) { [weak self] result in
//            LogUtils.log(message: "result == \(result.toJson() ?? "")", level: .info)
            let channelName = result.getPropertyWith(key: "roomId", type: String.self) as? String
            self?.channelName = channelName
            NetworkManager.shared.generateToken(channelName: channelName ?? "",
                                                uid: "\(UserInfo.userId)",
                                                tokenType: .token007,
                                                type: .rtc) { token in
                let resp = TemplateScene.JoinResponse(channelName: channelName ?? "", userId: "\(UserInfo.userId)")
                completion(nil, resp)
            }
        } fail: { error in
            completion(error, nil)
        }
    }

    func leave() {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }

        SyncUtil.scene(id: channelName)?.unsubscribe(key: "SmallClass")
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION).document().unsubscribe(key: "")
        SyncUtil.leaveScene(id: channelName)
    }

    func removeRoom() {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
//        SyncUtil.scene(id: channelName)?.delete(success: nil, fail: nil)
        SyncUtil.scene(id: channelName)?.deleteScenes()
    }

    func addUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, TemplateScene.UsersModel?) -> Void) {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        let params = JSONObject.toJson(user)
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION).add(data: params, success: { object in
            let model = JSONObject.toModel(TemplateScene.UsersModel.self, value: object.toJson())
            completion(nil, model)
        }, fail: { error in
            completion(error, nil)
        })
    }

    func removeUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, [TemplateScene.UsersModel]?) -> Void) {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .document(id: user.objectId ?? "")
            .delete(success: { results in
                let datas = results.compactMap({ $0.toJson() })
                    .compactMap({ JSONObject.toModel(TemplateScene.UsersModel.self, value: $0) })
                    .sorted(by: { $0.timestamp < $1.timestamp })
                completion(nil, datas)
            }, fail: { error in
                completion(error, nil)
            })
    }

    func updateUser(user: TemplateScene.UsersModel, completion: @escaping (SyncError?, TemplateScene.UsersModel?) -> Void) {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION)
            .document(id: user.objectId ?? "")
            .update(key: "", data: JSONObject.toJson(user), success: { objects in
                guard let object = objects.first, let model = JSONObject.toModel(TemplateScene.UsersModel.self, value: object.toJson()) else {
                    assertionFailure("user == nil")
                    return
                }
                completion(nil, model)
            }, fail: { error in
                completion(error, nil)
            })
    }

    func getUserStatus(completion: @escaping (SyncError?, [TemplateScene.UsersModel]?) -> Void) {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.collection(className: SYNC_SCENE_ROOM_USER_COLLECTION).get(success: { results in
            let datas = results.compactMap({ $0.toJson() })
                .compactMap({ JSONObject.toModel(TemplateScene.UsersModel.self, value: $0) })
                .sorted(by: { $0.timestamp < $1.timestamp })
            completion(nil, datas)
        }, fail: { error in
            completion(error, nil)
        })
    }

    func subscribeRoom(subscribeClosure: @escaping (TemplateScene.SubscribeStatus, TemplateScene.LiveRoomInfo?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)
    {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.subscribe(key: "",
                                                   onCreated: { object in
                                                       guard let model = JSONObject.toModel(TemplateScene.LiveRoomInfo.self, value: object.toJson()) else {
                                                           assertionFailure("LiveRoomInfo == nil")
                                                           return
                                                       }
                                                       subscribeClosure(TemplateScene.SubscribeStatus.created, model)
                                                   }, onUpdated: { object in
                                                       guard let model = JSONObject.toModel(TemplateScene.LiveRoomInfo.self, value: object.toJson()) else {
                                                           assertionFailure("LiveRoomInfo == nil")
                                                           return
                                                       }
                                                       subscribeClosure(TemplateScene.SubscribeStatus.updated, model)
                                                   }, onDeleted: { object in
                                                       let model = JSONObject.toModel(TemplateScene.LiveRoomInfo.self, value: object.toJson())
                                                       subscribeClosure(TemplateScene.SubscribeStatus.deleted, model)
                                                   }, onSubscribed: {
                                                       onSubscribed?()
                                                   }, fail: { error in
                                                       fail?(error)
                                                   })
    }

    func subscribeUser(subscribeClosure: @escaping (TemplateScene.SubscribeStatus, TemplateScene.UsersModel?) -> Void,
                       onSubscribed: (() -> Void)?,
                       fail: ((SyncError) -> Void)?)
    {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.subscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION,
                                                   onCreated: { object in
                                                       guard let model = JSONObject.toModel(TemplateScene.UsersModel.self, value: object.toJson()) else {
                                                           assertionFailure("AgoraUsersModel == nil")
                                                           return
                                                       }
                                                       subscribeClosure(TemplateScene.SubscribeStatus.created, model)
                                                   }, onUpdated: { object in
                                                       guard let model = JSONObject.toModel(TemplateScene.UsersModel.self, value: object.toJson()) else {
                                                           assertionFailure("AgoraUsersModel == nil")
                                                           return
                                                       }
                                                       subscribeClosure(TemplateScene.SubscribeStatus.updated, model)
                                                   }, onDeleted: { object in
                                                       let model = JSONObject.toModel(TemplateScene.UsersModel.self, value: object.toJson())
                                                       subscribeClosure(TemplateScene.SubscribeStatus.deleted, model)
                                                   }, onSubscribed: {
                                                       onSubscribed?()
                                                   }, fail: { error in
                                                       fail?(error)
                                                   })
    }

    func unsubscribe() {
        guard let channelName = channelName else {
            assertionFailure("channelName = nil")
            return
        }
        SyncUtil.scene(id: channelName)?.unsubscribe(key: SYNC_SCENE_ROOM_USER_COLLECTION)
        SyncUtil.scene(id: channelName)?.unsubscribe(key: SYNC_MANAGER_AGORA_VOICE_USERS)
    }
}

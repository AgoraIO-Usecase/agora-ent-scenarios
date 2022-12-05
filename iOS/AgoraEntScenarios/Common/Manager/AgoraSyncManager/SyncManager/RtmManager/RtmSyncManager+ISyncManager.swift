//
//  RtmSyncManager+ISyncManager.swift
//  SyncManager
//
//  Created by ZYP on 2021/11/15.
//

import AgoraRtmKit
import Foundation

extension RtmSyncManager: ISyncManager {
    func subscribeConnectState(state: @escaping ConnectBlockState) {
        //TODO(zhaoyongqiang): need implementation
    }
    
    public func createScene(scene: Scene,
                            success: SuccessBlockVoid?,
                            fail: FailBlock?)
    {
        /** add room in list **/
        let attr = AgoraRtmChannelAttribute()
        attr.key = scene.id
        attr.value = scene.toJson()
        let attributes = [attr]
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = false
        guard let name = channelName else {
            fatalError("must set default channel name")
        }
        createdRoomItems = attributes
        rtmKit?.addOrUpdateChannel(name,
                                   attributes: attributes,
                                   options: option,
                                   completion: { code in
                                       if code != .attributeOperationErrorOk {
                                           let error = SyncError(message: "addOrUpdate fail in joinScene", code: code.rawValue)
                                           fail?(error)
                                           return
                                       }
                                       success?()
                                   })
    }

    public func joinScene(sceneId: String,
                          manager: AgoraSyncManager,
                          success: SuccessBlockObjSceneRef?,
                          fail: FailBlock? = nil)
    {
        let sceneRef = SceneReference(manager: manager,
                                      id: sceneId)

        /** scene init **/
        guard let channel = rtmKit?.createChannel(withId: sceneId,
                                                  delegate: self)
        else {
            let error = SyncError(message: "createChannel fail \(sceneId)", code: -1)
            fail?(error)
            return
        }
        channel.join(completion: nil)
        channels[sceneId] = channel
        sceneName = sceneId
        Log.info(text: "didSet sceneName \(sceneId)", tag: "RtmSyncManager")

        /** get room list cached **/
        guard let name = channelName else {
            fatalError("must set default channel name")
        }
        rtmKit?.getChannelAllAttributes(name,
                                        completion: { [weak self] attrs, code in
                                            guard let channel = self?.rtmKit?.createChannel(withId: name, delegate: self) else {
                                                let error = SyncError(message: "createChannel fail in joinScene", code: -1)
                                                fail?(error)
                                                return
                                            }

                                            guard let self = self else { return }
                                            channel.join(completion: nil)
                                            self.cachedAttrs[channel] = attrs?.count ?? 0 > 0 ? attrs : self.createdRoomItems
                                            success?(sceneRef)
                                        })
    }

    public func getScenes(success: SuccessBlock?,
                          fail: FailBlock?)
    {
        rtmKit?.getChannelAllAttributes(channelName, completion: { list, errorCode in
            guard errorCode == .attributeOperationErrorOk else {
                let error = SyncError(message: "getChannelAllAttributes fail",
                                      code: errorCode.rawValue)
                fail?(error)
                return
            }

            let attrs = list ?? []
            var res = [Attribute]()
            for item in attrs {
                let attr = Attribute(key: item.key, value: item.value)
                res.append(attr)
            }
            success?(res)
        })
    }

    public func get(documentRef reference: DocumentReference,
                    key: String,
                    success: SuccessBlockObjOptional?,
                    fail: FailBlock?)
    {
        rtmKit?.getChannelAllAttributes(reference.className + key, completion: { res, error in
            if let res = res, res.count == 0 {
                success?(nil)
                return
            }

            guard let res = res, res.count > 0 else {
                let e = SyncError(message: "getChannelAllAttributes",
                                  code: error.rawValue)
                fail?(e)
                return
            }
            let obj = Attribute(key: res[0].key, value: res[0].value)
            success?(obj)
        })
    }

    public func get(collectionRef reference: CollectionReference,
                    success: SuccessBlock?,
                    fail: FailBlock?)
    {
        rtmKit?.getChannelAllAttributes(reference.className, completion: { [weak self] res, error in
            if let res = res, res.count == 0 {
                success?([])
                return
            }

            guard let res = res, res.count > 0 else {
                let error = SyncError(message: "yet join channel",
                                      code: -1)
                fail?(error)
                return
            }

            if let rtmChannel = self?.rtmKit?.createChannel(withId: reference.className, delegate: self) {
                if self?.cachedAttrs[rtmChannel]?.isEmpty ?? true {
                    self?.cachedAttrs[rtmChannel] = res
                }
            }
            var list = [Attribute]()
            for item in res {
                list.append(Attribute(key: item.key, value: item.value))
            }
            success?(list)
        })
    }

    public func add(reference: CollectionReference,
                    data: [String: Any?],
                    success: SuccessBlockObj?,
                    fail: FailBlock?)
    {
        if channels[reference.className] == nil {
            let rtmChannel = rtmKit?.createChannel(withId: reference.className, delegate: self)
            channels[reference.className] = rtmChannel
            rtmChannel?.join(completion: nil)
        }
        let attr = AgoraRtmChannelAttribute()
        attr.key = UUID().uuid16string()
        attr.value = Utils.getJson(dict: data as NSDictionary)
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.addOrUpdateChannel(reference.className, attributes: [attr], options: option, completion: { [weak self] error in
            guard error == .attributeOperationErrorOk else {
                let error = SyncError(message: "add data fail",
                                      code: error.rawValue)
                fail?(error)
                return
            }
            if let channel = self?.channels[reference.className] {
                if var cache = self?.cachedAttrs[channel] {
                    cache.append(attr)
                } else {
                    self?.cachedAttrs[channel] = [attr]
                }
                if let onCreateBlock = self?.onCreateBlocks[channel] {
                    onCreateBlock(attr.toAttribute())
                }
            }
            let key = attr.key
            let value = attr.toAttribute().toJson() ?? ""
            let attribute = Attribute(key: key,
                                      value: value)
            success?(attribute)
        })
    }

    /// id 是Collection item 的obj id
    public func update(reference: CollectionReference,
                       id: String,
                       data: [String: Any?],
                       success: SuccessBlockVoid?,
                       fail: FailBlock?)
    {
        let attr = AgoraRtmChannelAttribute()
        let item = Utils.getJson(dict: data as NSDictionary)
        attr.key = id
        attr.value = item
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.addOrUpdateChannel(reference.className, attributes: [attr], options: option, completion: { code in
            guard code == .attributeOperationErrorOk else {
                let error = SyncError(message: "update CollectionReference data fail",
                                      code: code.rawValue)
                fail?(error)
                return
            }
            success?()
        })
    }

    /// id 是Collection item 的obj id
    public func delete(reference: CollectionReference,
                       id: String,
                       success: SuccessBlockObjOptional?,
                       fail: FailBlock?)
    {
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.deleteChannel(reference.className,
                              attributesByKeys: [id],
                              options: option,
                              completion: { code in
                                  guard code == .attributeOperationErrorOk else {
                                      let error = SyncError(message: "update CollectionReference data fail",
                                                            code: code.rawValue)
                                      fail?(error)
                                      return
                                  }
                                  success?(nil)
                              })
    }

    public func update(reference: DocumentReference,
                       key: String,
                       data: [String: Any?],
                       success: SuccessBlock?,
                       fail: FailBlock?)
    {
        let attr = AgoraRtmChannelAttribute()
        let item = Utils.getJson(dict: data as NSDictionary)
        attr.key = reference.className + key
        attr.value = item
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.addOrUpdateChannel(reference.className + key, attributes: [attr], options: option, completion: { code in
            guard code == .attributeOperationErrorOk else {
                let error = SyncError(message: "update data fail",
                                      code: code.rawValue)
                fail?(error)
                return
            }
            let attribute = Attribute(key: reference.id, value: item)
            success?([attribute])
        })
    }

    public func deleteScenes(sceneIds: [String],
                             success: SuccessBlockObjOptional?,
                             fail: FailBlock?)
    {
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.deleteChannel(channelName,
                              attributesByKeys: sceneIds,
                              options: option,
                              completion: { errorCode in
                                  guard errorCode == .attributeOperationErrorOk else {
                                      let error = SyncError(message: "deleteScenes fail",
                                                            code: errorCode.rawValue)
                                      fail?(error)
                                      return
                                  }

                                  success?(nil)
                              })
    }

    public func delete(collectionRef: CollectionReference,
                       success: SuccessBlock?,
                       fail: FailBlock?)
    {
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        rtmKit?.clearChannel(collectionRef.className, options: option, attributesWithCompletion: { [weak self] error in
            guard let self = self else { return }
            guard error == .attributeOperationErrorOk else {
                let error = SyncError(message: "deleteScenes fail",
                                      code: error.rawValue)
                fail?(error)
                return
            }
            if let channel = self.channels[collectionRef.className] {
                let attr = self.cachedAttrs[channel]?.first
                let key = attr?.key ?? ""
                let value = attr?.toAttribute().toJson() ?? ""
                success?([Attribute(key: key, value: value)])
                self.cachedAttrs.removeValue(forKey: channel)
                self.channels.removeValue(forKey: collectionRef.className)
            } else {
                let error = SyncError(message: "deleteScenes fail",
                                      code: error.rawValue)
                fail?(error)
            }
        })
    }

    public func delete(documentRef: DocumentReference,
                       success: SuccessBlock?,
                       fail: FailBlock?)
    {
        let option = AgoraRtmChannelAttributeOptions()
        option.enableNotificationToChannelMembers = true
        let keys = documentRef.id.isEmpty ? nil : [documentRef.id]
        rtmKit?.deleteChannel(documentRef.className,
                              attributesByKeys: keys,
                              options: option,
                              completion: { [weak self] error in
                                  guard error == .attributeOperationErrorOk else {
                                      let error = SyncError(message: "delete channel fail",
                                                            code: error.rawValue)
                                      fail?(error)
                                      return
                                  }
                                  if let channel = self?.channels[documentRef.className] {
                                      if let cache = self?.cachedAttrs[channel] {
                                          var tempAttrs = [AgoraRtmChannelAttribute]()
                                          _ = cache.map {
                                              if $0.key != documentRef.id {
                                                  tempAttrs.append($0)
                                              } else {
                                                  success?([$0.toAttribute()])
                                              }
                                          }
                                          self?.cachedAttrs[channel] = tempAttrs
                                          return
                                      }
                                  }
                                  success?([])
                              })
    }

    public func subscribe(reference: DocumentReference,
                          key: String,
                          onCreated: OnSubscribeBlock?,
                          onUpdated: OnSubscribeBlock?,
                          onDeleted: OnSubscribeBlock?,
                          onSubscribed: OnSubscribeBlockVoid?,
                          fail: FailBlock?)
    {
        let name = reference.className + key

        if name == sceneName, let channel = channels[sceneName] { /** 设置监听参数：scene.id **/
            onCreateBlocks[channel] = onCreated
            onUpdatedBlocks[channel] = onUpdated
            onDeletedBlocks[channel] = onDeleted
            onSubscribed?()
            return
        }

        /** 设置监听参数：scene.id + key **/
        guard let rtmChannel = rtmKit?.createChannel(withId: name, delegate: self) else {
            let error = SyncError(message: "yet join channel",
                                  code: -1)
            fail?(error)
            return
        }
        rtmChannel.join(completion: nil)
        channels[name] = rtmChannel
        onCreateBlocks[rtmChannel] = onCreated
        onUpdatedBlocks[rtmChannel] = onUpdated
        onDeletedBlocks[rtmChannel] = onDeleted
        onSubscribed?()
    }

    public func unsubscribe(reference: DocumentReference, key: String) {
        if let rtmChannel = channels[reference.className + key] {
            onCreateBlocks.removeValue(forKey: rtmChannel)
            onUpdatedBlocks.removeValue(forKey: rtmChannel)
            onDeletedBlocks.removeValue(forKey: rtmChannel)
        }
    }

    func subscribeScene(reference: SceneReference,
                        onUpdated: OnSubscribeBlock?,
                        onDeleted: OnSubscribeBlock?,
                        fail: FailBlock?) {}

    func unsubscribeScene(reference: SceneReference, fail: FailBlock?) {}
}

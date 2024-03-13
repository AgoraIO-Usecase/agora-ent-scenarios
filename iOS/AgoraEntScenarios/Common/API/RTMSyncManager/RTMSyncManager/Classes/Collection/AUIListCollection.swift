//
//  AUIListCollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/4.
//

import Foundation

public class AUIListCollection: AUIBaseCollection {
    private var currentList: [[String: Any]] = []{
        didSet {
            if isValuesEqual(oldValue, currentList) {return}
            self.attributesDidChangedClosure?(channelName, observeKey, AUIAttributesModel(list: currentList))
        }
    }
}

//MARK: private set meta data
extension AUIListCollection {
    private func rtmAddMetaData(publisherId: String,
                                valueCmd: String?,
                                value: [String: Any],
                                filter: [[String: Any]]?,
                                callback: ((NSError?)->())?) {
        //如果filter空，默认无条件写入，如果有filter，判断条件
        if let filter = filter,
           filter.isEmpty == false,
           let _ = getItemIndexes(array: currentList, filter: filter) {
            callback?(AUICollectionOperationError.filterNotFound.toNSError("list rtmAddMetaData: '\(filter)'"))
            return
        }
        if let err = self.metadataWillAddClosure?(publisherId, valueCmd, value) {
            callback?(err)
            return
        }
        var list = currentList
        list.append(value)
        if let attr = self.attributesWillSetClosure?(channelName, 
                                                     observeKey,
                                                     valueCmd,
                                                     AUIAttributesModel(list: list)),
           let attrList = attr.getList() {
            list = attrList
        }
        guard let value = encodeToJsonStr(list) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        aui_collection_log("rtmAddMetaData valueCmd: \(valueCmd ?? "") value: \(value), \nfilter: \(filter ?? [])")
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            aui_collection_log("rtmAddMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
        currentList = list
    }
    
    private func rtmSetMetaData(publisherId: String,
                                valueCmd: String?,
                                value: [String: Any],
                                filter: [[String: Any]]?,
                                callback: ((NSError?)->())?) {
        //如果没有filter，默认每条记录都修改
        guard let itemIndexes = getItemIndexes(array: currentList, filter: filter) else {
            callback?(AUICollectionOperationError.filterNotFound.toNSError("list rtmSetMetaData: '\(filter ?? [])'"))
            return
        }
        var list = currentList
        for itemIdx in itemIndexes {
            let item = list[itemIdx]
            //once break, always break
            if let err = self.metadataWillUpdateClosure?(publisherId, valueCmd, value, item) {
                callback?(err)
                return
            }
            
            var tempItem = item
            value.forEach { (key, value) in
                tempItem[key] = value
            }
            list[itemIdx] = tempItem
        }
        if let attr = self.attributesWillSetClosure?(channelName,
                                                      observeKey,
                                                      valueCmd,
                                                      AUIAttributesModel(list: list)),
           let attrList = attr.getList() {
            list = attrList
        }
        guard let value = encodeToJsonStr(list) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        aui_collection_log("rtmSetMetaData valueCmd: \(valueCmd ?? ""), filter: \(filter ?? []), value: \(value)")
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            aui_collection_log("rtmSetMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
        currentList = list
    }
    
    private func rtmMergeMetaData(publisherId: String,
                                  valueCmd: String?,
                                  value: [String: Any],
                                  filter: [[String: Any]]?,
                                  callback: ((NSError?)->())?) {
        //如果没有filter，默认每条记录都修改
        guard let itemIndexes = getItemIndexes(array: currentList, filter: filter) else {
            callback?(AUICollectionOperationError.filterNotFound.toNSError("list rtmMergeMetaData: '\(filter ?? [])'"))
            return
        }
        
        var list = currentList
        for itemIdx in itemIndexes {
            let item = list[itemIdx]
            //once break, always break
            if let err = self.metadataWillMergeClosure?(publisherId, valueCmd, value, item) {
                callback?(err)
                return
            }
            
            let tempItem = mergeMap(origMap: item, newMap: value)
            list[itemIdx] = tempItem
        }
        if let attr = self.attributesWillSetClosure?(channelName,
                                                      observeKey,
                                                      valueCmd,
                                                      AUIAttributesModel(list: list)),
           let attrList = attr.getList() {
            list = attrList
        }
        guard let value = encodeToJsonStr(list) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        aui_collection_log("rtmMergeMetaData valueCmd: \(valueCmd ?? ""), filter: \(filter ?? []), value: \(value)")
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            aui_collection_log("rtmMergeMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
        currentList = list
    }
    
    private func rtmRemoveMetaData(publisherId: String,
                                   valueCmd: String?,
                                   filter: [[String: Any]]?,
                                   callback: ((NSError?)->())?) {
        //如果没有filter，默认删除所有
        guard let itemIndexes = getItemIndexes(array: currentList, filter: filter) else {
            callback?(AUICollectionOperationError.filterNotFound.toNSError("list rtmRemoveMetaData: '\(filter ?? [])'"))
            return
        }
        
        for itemIdx in itemIndexes {
            let item = currentList[itemIdx]
            if let err = self.metadataWillRemoveClosure?(publisherId, valueCmd, item) {
                callback?(err)
                return
            }
        }
        
        let filterList = currentList.enumerated().filter { !itemIndexes.contains($0.offset) }
        var list = filterList.map { $0.element }
        if let attr = self.attributesWillSetClosure?(channelName,
                                                      observeKey,
                                                      valueCmd,
                                                      AUIAttributesModel(list: list)),
           let attrList = attr.getList() {
            list = attrList
        }
        guard let value = encodeToJsonStr(list) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        aui_collection_log("rtmRemoveMetaData valueCmd: \(valueCmd ?? ""), filter: \(filter ?? []), value: \(value)")
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            aui_collection_log("rtmRemoveMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
        currentList = list
    }
    
    private func rtmCalculateMetaData(publisherId: String,
                                      valueCmd: String?,
                                      key: [String],
                                      value: AUICollectionCalcValue,
                                      filter: [[String: Any]]?,
                                      callback: ((NSError?)->())?) {
        //TODO: will calculate?
        
        //如果没有filter，默认每条记录都修改
        guard let itemIndexes = getItemIndexes(array: currentList, filter: filter) else {
            callback?(AUICollectionOperationError.filterNotFound.toNSError("list rtmCalculateMetaData: '\(filter ?? [])'"))
            return
        }
        
        var list = currentList
        for itemIdx in itemIndexes {
            let item = currentList[itemIdx]
            
            if let err = self.metadataWillCalculateClosure?(publisherId,
                                                            valueCmd,
                                                            item,
                                                            key,
                                                            value.value,
                                                            value.min,
                                                            value.max) {
                callback?(err)
                return
            }
            
            guard let tempItem = calculateMap(origMap: item,
                                              key: key,
                                              value: value.value,
                                              min: value.min,
                                              max: value.max) else {
                callback?(AUICollectionOperationError.calculateMapFail.toNSError())
                return
            }
            list[itemIdx] = tempItem
        }
        if let attr = self.attributesWillSetClosure?(channelName,
                                                      observeKey,
                                                      valueCmd,
                                                      AUIAttributesModel(list: list)),
           let attrList = attr.getList() {
            list = attrList
        }
        guard let value = encodeToJsonStr(list) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        aui_collection_log("rtmCalculateMetaData valueCmd: \(valueCmd ?? "") key: \(key), value: \(value)")
        self.rtmManager.setBatchMetadata(channelName: channelName,
                                         lockName: kRTM_Referee_LockName,
                                         metadata: [observeKey: value]) { error in
            aui_collection_log("rtmCalculateMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
        currentList = list
    }
    
    private func rtmCleanMetaData(callback: ((NSError?)->())?) {
        aui_collection_log("rtmCleanMetaData")
        self.rtmManager.cleanBatchMetadata(channelName: channelName,
                                           lockName: kRTM_Referee_LockName,
                                           removeKeys: [observeKey]) { error in
            aui_collection_log("rtmCleanMetaData completion: \(error?.localizedDescription ?? "success")")
            callback?(error)
        }
    }
}

//MARK: override IAUICollection
extension AUIListCollection {
    public override func updateMetaData(valueCmd: String?,
                                        value: [String : Any],
                                        filter: [[String: Any]]?,
                                        callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmSetMetaData(publisherId: currentUserId, 
                           valueCmd: valueCmd,
                           value: value, 
                           filter: filter,
                           callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .update, 
                                                  dataCmd: valueCmd,
                                                  filter: filter == nil ? nil : AUIAnyType(array: filter!),
                                                  data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
    }
    
    public override func mergeMetaData(valueCmd: String?,
                                       value: [String : Any],
                                       filter: [[String: Any]]?,
                                       callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmMergeMetaData(publisherId: currentUserId,
                             valueCmd: valueCmd,
                             value: value,
                             filter: filter,
                             callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .merge,
                                                  dataCmd: valueCmd,
                                                  filter: filter == nil ? nil : AUIAnyType(array: filter!),
                                                  data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
    }
    
    public override func addMetaData(valueCmd: String?,
                                     value: [String : Any],
                                     filter: [[String: Any]]?,
                                     callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmAddMetaData(publisherId: currentUserId,
                           valueCmd: valueCmd,
                           value: value,
                           filter: filter,
                           callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .add,
                                                  dataCmd: valueCmd,
                                                  filter: filter == nil ? nil : AUIAnyType(array: filter!),
                                                  data: AUIAnyType(map: value))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
        
    }
    
    public override func removeMetaData(valueCmd: String?,
                                        filter: [[String: Any]]?,
                                        callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmRemoveMetaData(publisherId: currentUserId, 
                              valueCmd: valueCmd,
                              filter: filter,
                              callback: callback)
            return
        }
        
        
        let payload = AUICollectionMessagePayload(type: .remove, 
                                                  dataCmd: valueCmd,
                                                  filter: filter == nil ? nil : AUIAnyType(array: filter!))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
    }
    
    public override func calculateMetaData(valueCmd: String?,
                                           key: [String],
                                           value: Int,
                                           min: Int,
                                           max: Int,
                                           filter: [[String: Any]]?,
                                           callback: ((NSError?)->())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            let currentUserId = AUIRoomContext.shared.currentUserInfo.userId
            rtmCalculateMetaData(publisherId: currentUserId,
                                 valueCmd: valueCmd,
                                 key: key,
                                 value: AUICollectionCalcValue(value: value, min: min, max: max),
                                 filter: filter,
                                 callback: callback)
            return
        }
        
        let calcData = AUICollectionCalcData(key: key,
                                             value: AUICollectionCalcValue(value: value, min: min, max: max))
        let data: [String: Any] = encodeModel(calcData) ?? [:]
        let payload = AUICollectionMessagePayload(type: .calculate,
                                                  dataCmd: valueCmd,
                                                  filter: filter == nil ? nil : AUIAnyType(array: filter!),
                                                  data: AUIAnyType(map: data))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)

        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
    }
    
    public override func cleanMetaData(callback: ((NSError?) -> ())?) {
        if AUIRoomContext.shared.getArbiter(channelName: channelName)?.isArbiter() ?? false {
            rtmCleanMetaData(callback: callback)
            return
        }
        
        let payload = AUICollectionMessagePayload(type: .clean, data: nil)
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: AUIMessageType.normal,
                                           sceneKey: observeKey,
                                           uniqueId: UUID().uuidString,
                                           payload: payload)
        
        guard let jsonStr = encodeModelToJsonStr(message) else {
            callback?(AUICollectionOperationError.removeMetaDataFail.toNSError())
            return
        }
        let userId = AUIRoomContext.shared.getArbiter(channelName: channelName)?.lockOwnerId ?? ""
        rtmManager.publishAndWaitReceipt(userId: userId,
                                         channelName: channelName,
                                         message: jsonStr,
                                         uniqueId: message.uniqueId,
                                         completion: callback)
    }
}

//MARK: override AUIRtmAttributesProxyDelegate
extension AUIListCollection {
    public override func onAttributesDidChanged(channelName: String, key: String, value: Any) {
        guard channelName == self.channelName, key == self.observeKey else {return}
        guard let list = value as? [[String: Any]] else {return}
        self.currentList = list
    }
}

//MARK: override AUIRtmMessageProxyDelegate
extension AUIListCollection {
    public override func onMessageReceive(publisher: String, message: String) {
        guard let map = decodeToJsonObj(message) as? [String: Any],
              let collectionMessage: AUICollectionMessage = decodeModel(map),
              collectionMessage.sceneKey == observeKey else {
            return
        }
        aui_collection_log("onMessageReceive: \(map)")
        let uniqueId = collectionMessage.uniqueId
        let channelName = collectionMessage.channelName
        guard channelName == self.channelName else {return}
        if collectionMessage.messageType == .receipt {
            if let callback = rtmManager.receiptCallbackMap[uniqueId]?.closure {
                rtmManager.markReceiptFinished(uniqueId: uniqueId)
                let data = collectionMessage.payload.data?.toJsonObject() as? [String : Any] ?? [:]
                let error: AUICollectionError? = decodeModel(data)
                let code = error?.code ?? 0
                let reason = error?.reason ?? "success"
                callback(code == 0 ? nil : AUICollectionOperationError.recvErrorReceipt.toNSError("code: \(code), reason: \(reason)"))
            }
            return
        }
        
        guard let updateType = collectionMessage.payload.type else {
            sendReceipt(publisher: publisher,
                        uniqueId: uniqueId,
                        error: AUICollectionOperationError.updateTypeNotFound.toNSError())
            return
        }
        
        let filter: [[String: Any]]? = collectionMessage.payload.filter?.toJsonObject() as? [[String: Any]]
        let valueCmd = collectionMessage.payload.dataCmd
        var err: NSError? = nil
        switch updateType {
        case .add, .update, .merge:
            if let value = collectionMessage.payload.data?.toJsonObject() as? [String : Any] {
                if updateType == .add {
                    rtmAddMetaData(publisherId: publisher,
                                   valueCmd: valueCmd,
                                   value: value,
                                   filter: filter) { [weak self] error in
                        self?.sendReceipt(publisher: publisher,
                                          uniqueId: uniqueId,
                                          error: error)
                    }
                } else if updateType == .merge {
                    rtmMergeMetaData(publisherId: publisher,
                                     valueCmd: valueCmd,
                                     value: value, 
                                     filter: filter) {[weak self] error in
                        self?.sendReceipt(publisher: publisher, 
                                          uniqueId: uniqueId,
                                          error: error)
                    }
                } else {
                    rtmSetMetaData(publisherId: publisher, 
                                   valueCmd: valueCmd,
                                   value: value,
                                   filter: filter) {[weak self] error in
                        self?.sendReceipt(publisher: publisher, 
                                          uniqueId: uniqueId,
                                          error: error)
                    }
                }
                return
            }
            err = AUICollectionOperationError.invalidPayloadType.toNSError()
        case .remove:
            rtmRemoveMetaData(publisherId: publisher, 
                              valueCmd: valueCmd,
                              filter: filter) {[weak self] error in
                self?.sendReceipt(publisher: publisher, 
                                  uniqueId: uniqueId,
                                  error: error)
            }
        case .clean:
            rtmCleanMetaData(callback: {[weak self] error in
                self?.sendReceipt(publisher: publisher, 
                                  uniqueId: uniqueId,
                                  error: error)
            })
        case .calculate:
            if let value = collectionMessage.payload.data?.toJsonObject() as? [String : Any],
               let data: AUICollectionCalcData = decodeModel(value) {
                rtmCalculateMetaData(publisherId: publisher,
                                     valueCmd: valueCmd,
                                     key: data.key,
                                     value: data.value,
                                     filter: filter) {[weak self] error in
                    self?.sendReceipt(publisher: publisher,
                                      uniqueId: uniqueId,
                                      error: error)
                }
                return
            }
            err = AUICollectionOperationError.invalidPayloadType.toNSError()
        }
        
        guard let err = err else {return}
        sendReceipt(publisher: publisher,
                    uniqueId: uniqueId,
                    error: err)
    }
}

//
//  AUIBaseCollection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/16.
//

import Foundation

private let kCollectionTag = "AUICollection"
func aui_collection_log(_ text: String) {
    aui_info(text, tag: kCollectionTag)
}

func aui_collection_warn(_ text: String) {
    aui_warn(text, tag: kCollectionTag)
}

@objcMembers
public class AUIBaseCollection: NSObject {
    private(set) var channelName: String
    private(set) var observeKey: String
    private(set) var rtmManager: AUIRtmManager

    private(set) var valueWillChangeClosure: AUICollectionValueWillChangeClosure?
    
    private(set) var metadataWillAddClosure: AUICollectionAddClosure?
    private(set) var metadataWillUpdateClosure: AUICollectionUpdateClosure?
    private(set) var metadataWillMergeClosure: AUICollectionUpdateClosure?
    private(set) var metadataWillRemoveClosure: AUICollectionRemoveClosure?
    private(set) var metadataWillCalculateClosure: AUICollectionCalculateClosure?
    
    private(set) var attributesWillSetClosure: AUICollectionAttributesWillSetClosure?
    private(set) var attributesDidChangedClosure: AUICollectionAttributesDidChangedClosure?
    
    deinit {
        aui_collection_log("[\(channelName)][\(observeKey)]deinit AUICollection \(self)")
        rtmManager.unsubscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.unsubscribeMessage(channelName: channelName, delegate: self)
    }
    
    public required init(channelName: String, observeKey: String, rtmManager: AUIRtmManager) {
        self.rtmManager = rtmManager
        self.observeKey = observeKey
        self.channelName = channelName
        super.init()
        aui_collection_log("[\(channelName)][\(observeKey)]init AUICollection \(self)")
        rtmManager.subscribeAttributes(channelName: channelName, itemKey: observeKey, delegate: self)
        rtmManager.subscribeMessage(channelName: channelName, delegate: self)
    }
}

extension AUIBaseCollection: IAUICollection {
    
    /// 当收到远端需要修改
    /// - Parameter callback: <#callback description#>
    public func subsceibeValueWillChange(callback: AUICollectionValueWillChangeClosure?) {
        self.valueWillChangeClosure = callback
    }
    
    public func subscribeWillAdd(callback: AUICollectionAddClosure?) {
        self.metadataWillAddClosure = callback
    }

    public func subscribeWillUpdate(callback: AUICollectionUpdateClosure?) {
        self.metadataWillUpdateClosure = callback
    }
    
    public func subscribeWillMerge(callback: AUICollectionUpdateClosure?) {
        self.metadataWillMergeClosure = callback
    }
    
    public func subscribeWillRemove(callback: AUICollectionRemoveClosure?) {
        self.metadataWillRemoveClosure = callback
    }
    
    public func subscribeWillCalculate(callback: AUICollectionCalculateClosure?) {
        self.metadataWillCalculateClosure = callback
    }
    
    public func subscribeAttributesWillSet(callback: AUICollectionAttributesWillSetClosure?) {
        self.attributesWillSetClosure = callback
    }
    
    public func subscribeAttributesDidChanged(callback: AUICollectionAttributesDidChangedClosure?) {
        self.attributesDidChangedClosure = callback
    }
    
    public func getMetaData(callback: AUICollectionGetClosure?) {
        aui_collection_log("[\(observeKey)]getMetaData")
        self.rtmManager.getMetadata(channelName: self.channelName) {[weak self] error, map in
            guard let self = self else {return}
            aui_collection_log("[\(self.observeKey)]getMetaData completion: \(error?.localizedDescription ?? "success")")
            if let error = error {
                //TODO: error
                callback?(error, nil)
                return
            }
            
            guard let jsonStr = map?[self.observeKey],
                  let jsonDict = decodeToJsonObj(jsonStr) else {
                //TODO: error
                callback?(nil, nil)
                return
            }
            
            callback?(nil, jsonDict)
        }
    }
    
    public func getLocalMetaData() -> AUIAttributesModel? {
        return nil
    }
}

extension AUIBaseCollection: AUIRtmAttributesProxyDelegate {
    public func onAttributesDidChanged(channelName: String, key: String, value: Any) {
    }
}

//MARK: AUIRtmMessageProxyDelegate
extension AUIBaseCollection: AUIRtmMessageProxyDelegate {
    func sendReceipt(publisher: String, uniqueId: String, error: NSError?) {
        let error = AUICollectionError(code: error?.code ?? 0, reason: error?.localizedDescription ?? "")
        guard let data: [String: Any] = encodeModel(error) else {
            aui_collection_warn("[\(observeKey)]sendReceipt encodeModel error fail")
            return
        }
        let payload = AUICollectionMessagePayload(data: AUIAnyType(map: data))
        let message = AUICollectionMessage(channelName: channelName,
                                           messageType: .receipt,
                                           sceneKey: observeKey,
                                           uniqueId: uniqueId,
                                           payload: payload)
        guard let jsonStr = encodeModelToJsonStr(message) else {
            aui_collection_warn("[\(observeKey)]sendReceipt fail")
            return
        }
        rtmManager.publish(userId: publisher,
                           channelName: channelName,
                           message: jsonStr) { err in
        }
    }
    
    public func onMessageReceive(publisher: String, channelName: String, message: String) {
    }
}

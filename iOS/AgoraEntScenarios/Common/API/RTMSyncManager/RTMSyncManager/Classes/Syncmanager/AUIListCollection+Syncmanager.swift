//
//  AUIListCollection+Syncmanager.swift
//  RTMSyncManager
//
//  Created by wushengtao on 2024/5/20.
//

import Foundation

extension AUIListCollection {
    func initMetaData(channelName: String,
                      metadata: [[String: Any]],
                      fetchImmediately: Bool,
                      completion: @escaping (NSError?)->()) {
        guard let value = encodeToJsonStr(metadata) else {
            completion(AUICollectionOperationError.encodeToJsonStringFail.toNSError())
            return
        }
        rtmManager.setBatchMetadata(channelName: channelName,
                                    lockName: "",
                                    metadata: [observeKey: value],
                                    fetchImmediately: fetchImmediately,
                                    completion: completion)
    }
}

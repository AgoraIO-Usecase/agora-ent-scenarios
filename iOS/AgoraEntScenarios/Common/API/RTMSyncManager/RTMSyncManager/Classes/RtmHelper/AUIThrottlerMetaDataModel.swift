//
//  AUIThrottlerMetaDataModel.swift
//  AUIKitCore
//
//  Created by wushengtao on 2023/11/14.
//

import Foundation

class AUIThrottlerUpdateMetaDataModel: NSObject {
    private(set) lazy var throttler: AUIThrottler = AUIThrottler()
    private(set) var metaData: [String: String] = [:]
    private(set) var callbacks: [((NSError?) -> ())] = []
    
    func appendMetaDataInfo(metaData: [String: String], completion:@escaping ((NSError?) -> ())) {
        metaData.forEach { key, value in
            self.metaData[key] = value
        }
        callbacks.append(completion)
    }
    
    func reset() {
        metaData.removeAll()
        callbacks.removeAll()
    }
}

class AUIThrottlerRemoveMetaDataModel: NSObject {
    private(set) lazy var throttler: AUIThrottler = AUIThrottler()
    private(set) var keys: Set<String> = Set<String>()
    private(set) var callbacks: [((NSError?) -> ())] = []
    
    func appendMetaDataInfo(keys: [String], completion:@escaping ((NSError?) -> ())) {
        self.keys.union(keys)
        callbacks.append(completion)
    }
    
    func reset() {
        keys.removeAll()
        callbacks.removeAll()
    }
}

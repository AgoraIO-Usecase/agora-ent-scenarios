//
//  Error+Collection.swift
//  AUIKitCore
//
//  Created by wushengtao on 2024/1/23.
//

import Foundation


public enum AUICollectionOperationError: Int {
    case unknown = 100      //未知错误
    
    case updateTypeNotFound = 101
    case removeMetaDataFail = 102
    case invalidPayloadType = 103
    case filterNotFound = 104
    case encodeToJsonStringFail = 105
    case calculateMapFail = 106
    case recvErrorReceipt = 107
    case unsupportedAction = 108
    case calculateMapOutOfRange = 111
    case filterFoundSame = 112
    
    public func toNSError(_ customMsg: String? = nil) -> NSError {
        func createError(msg: String) -> NSError {
            let extMsg = customMsg == nil ? "" : ": \(customMsg!)"
            return NSError(domain: "AUICollection Error",
                           code: self.rawValue,
                           userInfo: [ NSLocalizedDescriptionKey : "\(msg)\(extMsg)"])
        }
        
        switch self {
        case .updateTypeNotFound:
            return createError(msg: "update type not found")
        case .removeMetaDataFail:
            return createError(msg: "remove metaData fail")
        case .invalidPayloadType:
            return createError(msg: "invalid payload type")
        case .filterNotFound:
            return createError(msg: "filter result not found")
        case .encodeToJsonStringFail:
            return createError(msg: "encode to json string fail")
        case .calculateMapFail:
            return createError(msg: "calculate map fail")
        case .recvErrorReceipt:
            return createError(msg: "receipt error")
        case .unsupportedAction:
            return createError(msg: "action unsupported")
        case .calculateMapOutOfRange:
            return createError(msg: "calculate map out of range")
        case .filterFoundSame:
            return createError(msg: "filter result found the same value")
        default:
            return createError(msg: "unknown error")
        }
    }
}

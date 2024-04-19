//
//  RtmExtension.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation
import AgoraRtmKit

extension AgoraRtmPresenceEvent {
    func snapshotList() -> [[String: String]] {
        var userList: [[String: String]] = [[String: String]]()
        self.snapshot.forEach { user in
            var userMap: [String: String] = [:]
            userMap["userId"] = user.userId
            user.states.forEach { item in
                guard let key = item.key as? String, let value = item.value as? String else {return}
                userMap[key] = value
            }
            userList.append(userMap)
        }
        
        return userList
    }
}

extension AgoraRtmWhoNowResponse {
    func userList() -> [[String: String]] {
        var userList = [[String: String]]()
        self.userStateList.forEach { user in
            var userMap = [String: String]()
            userMap["userId"] = user.userId
            user.states.forEach { item in
                guard let key = item.key as? String, let value = item.value as? String else {return}
                aui_info("presence whoNow user: \(user.userId) \(key): \(value)", tag: "AUIRtmManager")
                userMap[key] = value
            }
            userList.append(userMap)
        }
        
        return userList
    }
}

extension AgoraRtmMetadata {
    static func createMetadata(metadata: [String: String]) -> AgoraRtmMetadata? {
        guard let data = AgoraRtmMetadata() else { return nil }
        
        var items: [AgoraRtmMetadataItem] = []
        metadata.forEach { (key: String, value: String) in
            let item = AgoraRtmMetadataItem()
            item.key = key
            item.value = value
            items.append(item)
        }
        data.items = items
        
        return data
    }
    
    static func createMetadata(keys: [String]) -> AgoraRtmMetadata? {
        guard let data = AgoraRtmMetadata() else { return nil }
        
        var items: [AgoraRtmMetadataItem] = []
        for key in keys {
            let item = AgoraRtmMetadataItem()
            item.key = key
            items.append(item)
        }
        data.items = items
        return data
    }
}
/*
extension AgoraRtmChannelErrorInfo {
    func toNSError() -> NSError? {
        if errorCode.rawValue == 0 {
            return nil
        }
        
        return AUICommonError.rtmError(Int32(errorCode.rawValue)).toNSError()
    }
}

extension AgoraRtmOperationErrorInfo {
    func toNSError() -> NSError? {
        if errorCode.rawValue == 0 {
            return nil
        }
        
        return AUICommonError.rtmError(Int32(errorCode.rawValue)).toNSError()
    }
}

extension AgoraRtmLoginErrorInfo {
    func toNSError() -> NSError? {
        if errorCode.rawValue == 0 {
            return nil
        }
        
        return AUICommonError.rtmError(Int32(errorCode.rawValue)).toNSError()
    }
}
*/

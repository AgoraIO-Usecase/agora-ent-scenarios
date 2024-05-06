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
                userMap[item.key] = item.value
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
                aui_info("presence whoNow user: \(user.userId) \(item.key): \(item.value)", tag: "AUIRtmManager")
                userMap[item.key] = item.value
            }
            userList.append(userMap)
        }
        
        return userList
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

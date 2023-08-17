//
//  AgoraRtmPresenceEvent+CallAPI.swift
//  CallAPI
//
//  Created by wushengtao on 2023/6/1.
//

import AgoraRtmKit

let kUserIdKey = "userId"
extension AgoraRtmPresenceEvent {
    func snapshotList() -> [[String: String]] {
        var userList: [[String: String]] = [[String: String]]()
        let snapshotList = snapshot as NSArray
        snapshotList.forEach { user in
            var userMap: [String: String] = [:]
            let user = user as! AgoraRtmUserState
            userMap[kUserIdKey] = user.userId
            user.states.forEach { item in
                userMap[item.key] = item.value
            }
            userList.append(userMap)
        }
        
        return userList
    }
}

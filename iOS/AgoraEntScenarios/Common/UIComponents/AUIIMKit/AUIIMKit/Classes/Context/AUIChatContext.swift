//
//  AUIChatContext.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/3.
//

import Foundation

open class AUIChatContext: NSObject {
    public static let shared: AUIChatContext = AUIChatContext()
    var currentUserInfo: AUIChatUserInfo? {
        return commonConfig?.owner
    }
    public var commonConfig: AUIChatCommonConfig?
    
    public var roomInfoMap: [String: AUIChatRoomInfo] = [:]
    public var roomConfigMap: [String: AUIChatCommonConfig] = [:]
    
    public var seatCount: UInt = 8
    public var sendMsgCallback: ((NSError?, String) -> ())?

    private var ntpTimeClosure: (()-> Int64)?
    
    public func setNtpTime(callback: @escaping ()-> Int64) {
        self.ntpTimeClosure = callback
    }
    
    public func getNtpTime() -> Int64 {
        return ntpTimeClosure?() ?? Int64(Date().timeIntervalSince1970 * 1000)
    }
    
    public func isRoomOwner(channelName: String) ->Bool {
        return isRoomOwner(channelName: channelName, userId: currentUserInfo?.userId ?? "")
    }
    
    public func isRoomOwner(channelName: String, userId: String) ->Bool {
        return roomInfoMap[channelName]?.ownerUserId == userId
    }
    
    public func clean(channelName: String) {
        roomConfigMap[channelName] = nil
        roomInfoMap[channelName] = nil
    }
}

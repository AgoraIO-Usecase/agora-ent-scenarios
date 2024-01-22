//
//  CallConnectInfo.swift
//  CallAPI
//
//  Created by wushengtao on 2023/12/14.
//

import Foundation


/// 耗时统计类型
public enum CallConnectCostType: String {
    case remoteUserRecvCall = "remoteUserRecvCall"         //主叫呼叫成功，收到呼叫成功表示已经送达对端(被叫)
    case acceptCall = "acceptCall"                         //主叫收到被叫接受呼叫(onAccept)/被叫点击接受(accept)
    case localUserJoinChannel = "localUserJoinChannel"     //本地用户加入频道
    case remoteUserJoinChannel = "remoteUserJoinChannel"   //远端用户加入频道
    case recvFirstFrame = "recvFirstFrame"                 //收到对端首帧
}

class CallConnectInfo {
    /// 开始获取视频流的时间
    private(set) var startRetrieveFirstFrame: Date?
    
    /// 是否获取到对端视频首帧
    var isRetrieveFirstFrame: Bool = false
    
    /// 呼叫的session id
    var callId: String = ""
    
    //呼叫中的频道名
    var callingRoomId: String?
    
    //呼叫中的远端用户
    var callingUserId: UInt?
    
    /// 本地是否已经同意
    var isLocalAccepted: Bool = false
    
    //呼叫开始的时间
    private(set) var callTs: Int? {
        didSet {
            callCostMap.removeAll()
        }
    }
    
    var callCostMap: [String: Int] = [:]
    
    //发起呼叫的定时器，用来处理超时
    var timer: Timer? {
        didSet {
            oldValue?.invalidate()
        }
    }
    
    func clean() {
        timer = nil
        callingRoomId = nil
        callingUserId = nil
        callTs = nil
        callId = ""
        isRetrieveFirstFrame = false
        startRetrieveFirstFrame = nil
        isLocalAccepted = false
    }
    
    func set(userId: UInt, 
             roomId: String, 
             callId: String? = nil,
             isLocalAccepted: Bool = false) {
        self.callingUserId = userId
        self.callingRoomId = roomId
        self.isLocalAccepted = isLocalAccepted
        if let callId = callId {
            self.callId = callId
        }
        if callTs == nil {
            self.callTs = Date().millisecondsSince1970()
        }
        if startRetrieveFirstFrame == nil {
            self.startRetrieveFirstFrame = Date()
        }
    }
}

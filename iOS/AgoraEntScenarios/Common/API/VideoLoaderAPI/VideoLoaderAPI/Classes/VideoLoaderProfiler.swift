//
//  VideoLoaderProfiler.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/8/14.
//

import Foundation
import AgoraRtcKit

class VideoLoaderProfiler: NSObject, AgoraRtcEngineDelegate {
    var roomId: String!
    var startTime: Int64 = 0
    
    init(roomId: String) {
        self.roomId = roomId
    }
    public func rtcEngine(_ engine: AgoraRtcEngineKit,
                          remoteVideoStateChangedOfUid uid: UInt,
                          state: AgoraVideoRemoteState,
                          reason: AgoraVideoRemoteReason,
                          elapsed: Int) {
        let channelId = ""//self.room?.roomId ?? ""
        let cost = Int64(Date().timeIntervalSince1970 * 1000) - startTime
        DispatchQueue.main.async {
            if state == .decoding /*2*/,
               ( reason == .remoteUnmuted /*6*/ || reason == .localUnmuted /*4*/ || reason == .localMuted /*3*/ )   {
                print("[VideoLoaderProfiler] show first frame (\(uid)) cost: \(cost) ms")
            }
        }
    }
}

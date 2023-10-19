//
//  VideoLoaderProfiler.swift
//  VideoLoaderAPI
//
//  Created by wushengtao on 2023/8/14.
//

import Foundation
import AgoraRtcKit


func apiPrint(_ message: String) {
    #if DEBUG
    print("\(formatter.string(from: Date()))[VideoLoaderApi]\(message)")
    #endif
}

public func debugLoaderPrint(_ message: String) {
    if let closure = VideoLoaderApiImpl.shared.printClosure {
        closure(message)
        return
    }
    apiPrint(message)
}

public func warningLoaderPrint(_ message: String) {
    if let closure = VideoLoaderApiImpl.shared.warningClosure {
        closure(message)
        return
    }
    apiPrint("[Warning]\(message)")
}

public func errorLoaderPrint(_ message: String) {
    if let closure = VideoLoaderApiImpl.shared.errorClosure {
        closure(message)
        return
    }
    apiPrint("[Error]\(message)")
}

class VideoLoaderProfiler: NSObject, AgoraRtcEngineDelegate {
    var anchorId: String!
    public internal(set) var startTime: Int64 = 0
    var firstFrameCompletion: ((Int64, UInt)->())?
    
    init(anchorId: String) {
        self.anchorId = anchorId
    }
    public func rtcEngine(_ engine: AgoraRtcEngineKit,
                          remoteVideoStateChangedOfUid uid: UInt,
                          state: AgoraVideoRemoteState,
                          reason: AgoraVideoRemoteReason,
                          elapsed: Int) {
        let channelId = ""//self.room?.roomId ?? ""
        let cost = Int64(Date().timeIntervalSince1970 * 1000) - startTime
        let anchorId = anchorId ?? ""
        #if DEBUG
        debugLoaderPrint("remoteVideoStateChangedOfUid[\(anchorId)]: \(uid) state: \(state.rawValue) reason: \(reason.rawValue)")
        #endif
        DispatchQueue.main.async {
            if state == .decoding /*2*/,
               ( reason == .remoteUnmuted /*6*/ || reason == .localUnmuted /*4*/ || reason == .localMuted /*3*/ )   {
                debugLoaderPrint("anchorId[\(anchorId)] uid[\(uid)] show first frame! cost: \(cost) ms")
                self.firstFrameCompletion?(cost, uid)
            }
        }
    }
}

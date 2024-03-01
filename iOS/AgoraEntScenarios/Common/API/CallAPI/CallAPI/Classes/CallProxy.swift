//
//  CallProxy.swift
//  CallAPI
//
//  Created by wushengtao on 2023/7/13.
//

import Foundation
import AgoraRtcKit

class CallAgoraExProxy: CallApiProxy, AgoraRtcEngineDelegate {
}

class CallLocalFirstFrameProxy: NSObject, AgoraRtcEngineDelegate {
    weak var delegate: AgoraRtcEngineDelegate?
    
    init(delegate: AgoraRtcEngineDelegate?) {
        self.delegate = delegate
    }
    
    func rtcEngine(_ engine: AgoraRtcEngineKit, firstLocalVideoFrameWith size: CGSize, elapsed: Int, sourceType: AgoraVideoSourceType) {
        delegate?.rtcEngine?(engine, firstLocalVideoFrameWith: size, elapsed: elapsed, sourceType: sourceType)
    }
}

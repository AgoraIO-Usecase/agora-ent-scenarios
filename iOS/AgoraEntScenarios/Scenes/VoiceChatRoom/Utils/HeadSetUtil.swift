//
//  HeadSetUtil.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/5/22.
//

import UIKit

class HeadSetUtil: NSObject {
    private static var headsetStatusCallBack: ((Bool) -> Void)?
    // 是否插入耳机
    static func hasHeadset() -> Bool {
        let audioSession = AVAudioSession.sharedInstance()
        let currentRoute = audioSession.currentRoute
        for output in currentRoute.outputs {
            if output.portType == .headphones
                || output.portType == .bluetoothLE
                || output.portType == .bluetoothHFP
                || output.portType == .bluetoothA2DP
                || output.portType == .builtInSpeaker
                || output.portType == .usbAudio {
                return true
            }
        }
        return false
    }
    
    static func addHeadsetObserver(callback: @escaping (Bool) -> Void) {
        headsetStatusCallBack = callback
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(headsetChangeListener(sender:)),
                                               name: AVAudioSession.routeChangeNotification,
                                               object: AVAudioSession.sharedInstance())
    }
    
    
    @objc
    private static func headsetChangeListener(sender: Notification) {
        guard let userInfo = sender.userInfo,
              let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
              let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else {
            return
        }
        switch reason {
        case .newDeviceAvailable:
            headsetStatusCallBack?(true)
            
        case .oldDeviceUnavailable:
            headsetStatusCallBack?(false)
            
        default: break
        }
    }
}

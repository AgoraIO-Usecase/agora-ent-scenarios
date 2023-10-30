//
//  HeadSetUtil.swift
//  AgoraEntScenarios
//
//  Created by zhaoyongqiang on 2023/5/22.
//

import UIKit
import AVKit
class KTVHeadSetUtil: NSObject {
    private static var headsetStatusCallBack: ((Bool) -> Void)?
    private static var soundCardStatusCallBack: ((Bool) -> Void)?
    // 是否插入耳机
    static func hasHeadset() -> Bool {
        let audioSession = AVAudioSession.sharedInstance()
        let currentRoute = audioSession.currentRoute
        for output in currentRoute.outputs {
            if output.portType == .headphones
                || output.portType == .bluetoothLE
                || output.portType == .bluetoothHFP
                || output.portType == .bluetoothA2DP
                || output.portType == .usbAudio {
                return true
            }
        }
        return false
    }
    
    static func hasSoundCard() -> Bool {
        let audioSession = AVAudioSession.sharedInstance()
        let currentRoute = audioSession.currentRoute
        for input in currentRoute.inputs {
            if input.portType == .headsetMic
                || input.portType == .usbAudio {
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
    
    static func addSoundCardObserver(callback: @escaping (Bool) -> Void) {
        soundCardStatusCallBack = callback
        NotificationCenter.default.addObserver(self, selector: #selector(handleRouteChange(_:)), name: AVAudioSession.routeChangeNotification, object: nil)
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
    
    @objc private static func handleRouteChange(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
            let reasonValue = userInfo[AVAudioSessionRouteChangeReasonKey] as? UInt,
            let reason = AVAudioSession.RouteChangeReason(rawValue: reasonValue) else {
                return
        }
        
        if reason == .oldDeviceUnavailable {
            // 设备断开
            print("Old device is unavailable")
            soundCardStatusCallBack?(false)
        } else if reason == .newDeviceAvailable {
            // 有新设备连接
            let session = AVAudioSession.sharedInstance()
            let inputs = session.currentRoute.inputs
            if let input = inputs.first,
               input.portType == .headsetMic || input.portType == .usbAudio {
                soundCardStatusCallBack?(true)
                print("Connected a wired headset with microphone")
            } else if let _ = session.currentRoute.outputs.first {
                // 连接的是不带麦克风的有线耳机
                soundCardStatusCallBack?(false)
                print("Connected a wired headset without microphone")
            }
        }
    }
}

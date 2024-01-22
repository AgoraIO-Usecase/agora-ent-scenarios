//
//  AgoraEntAuthorizedManager.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2023/3/24.
//

import Foundation
import AVFoundation
import UIKit
import AgoraCommon
@objc public enum checkAuthType: Int {
    case all
    case audio
    case video
}

open class AgoraEntAuthorizedManager: NSObject {
    @objc class func showAudioAuthorizedFail(parent: UIViewController) {
        showAuthorizedFail(parent: parent, message: commonSceneLocalized("authorized_microphone_fail"))
    }
    
    @objc class func showCameraAuthorizedFail(parent: UIViewController) {
        showAuthorizedFail(parent: parent, message: commonSceneLocalized("authorized_camera_fail"))
    }
    
    @objc public class func checkMediaAuthorized(parent: UIViewController, completion: ((Bool) -> Void)? = nil) {
        var isPermission: Bool = true
        let group = DispatchGroup()
        group.enter()
        requestAudioSession { granted in
            if !granted {
                showAudioAuthorizedFail(parent: parent)
            }
            isPermission = granted
            group.leave()
        }
        group.enter()
        requestCapture { granted in
            if !granted {
                showCameraAuthorizedFail(parent: parent)
            }
            isPermission = granted
            group.leave()
        }
        group.notify(queue: .main) {
            completion?(isPermission)
        }
    }
    
    @objc class func checkMediaAuthorized(parent: UIViewController, type: checkAuthType, completion: ((Bool) -> Void)? = nil) {
        var isPermission: Bool = true
        let group = DispatchGroup()
        if type != .video {
            group.enter()
            requestAudioSession { granted in
                if !granted {
                    showAudioAuthorizedFail(parent: parent)
                }
                isPermission = granted
                group.leave()
            }
        }
        
        if type != .audio {
            group.enter()
            requestCapture { granted in
                if !granted {
                    showCameraAuthorizedFail(parent: parent)
                }
                isPermission = granted
                group.leave()
            }
        }
        group.notify(queue: .main) {
            completion?(isPermission)
        }
    }
    
    @objc public class func checkAudioAuthorized(parent: UIViewController, completion: ((Bool) -> Void)? = nil) {
        requestAudioSession { granted in
            if !granted {
                showAudioAuthorizedFail(parent: parent)
            }
            completion?(granted)
        }
    }
    
    @objc public class func checkCameraAuthorized(parent: UIViewController, completion: ((Bool) -> Void)? = nil) {
        requestCapture { granted in
            if !granted {
                showCameraAuthorizedFail(parent: parent)
            }
            completion?(granted)
        }
    }
    
    @objc class func showAuthorizedFail(parent: UIViewController, message: String) {
        let vc = UIAlertController(title: commonSceneLocalized("authorized_title") as String,
                                   message: message,
                                   preferredStyle: .alert)
        let okAction = UIAlertAction(title: commonSceneLocalized("authorized_ok") as String,
                                     style: .default) { action in
            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
        }
        let cancelAction = UIAlertAction(title: commonSceneLocalized("authorized_cancel") as String,
                                         style: .cancel)
        vc.addAction(cancelAction)
        vc.addAction(okAction)
        
        parent.present(vc, animated: true)
    }
    
    @objc class func requestCapture(completion:@escaping (Bool)->()) {
        let status = AVCaptureDevice.authorizationStatus(for: .video)
        if status == .denied || status == .restricted {
            DispatchQueue.main.async {
                completion(false)
            }
            return
        } else if status == .authorized {
            DispatchQueue.main.async {
                completion(true)
            }
            return
        }
        
        AVCaptureDevice.requestAccess(for: .video) { granted in
            DispatchQueue.main.async {
                completion(granted)
            }
        }
    }
    
    @objc class func requestAudioSession(completion:@escaping (Bool)->()) {
        let status = AVCaptureDevice.authorizationStatus(for: .audio)
        if status == .denied || status == .restricted {
            DispatchQueue.main.async {
                completion(false)
            }
            return
        } else if status == .authorized {
            DispatchQueue.main.async {
                completion(true)
            }
            return
        }
        
        let permissionStatus = AVAudioSession.sharedInstance().recordPermission
        if permissionStatus == AVAudioSession.RecordPermission.undetermined {
            AVAudioSession.sharedInstance().requestRecordPermission { granted in
                //此处可以判断权限状态来做出相应的操作，如改变按钮状态
                DispatchQueue.main.async {
                    completion(granted)
                }
            }
        }
    }
}

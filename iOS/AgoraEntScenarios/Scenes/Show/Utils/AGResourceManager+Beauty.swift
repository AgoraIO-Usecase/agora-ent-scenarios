//
//  ResourceManager+Beauty.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2024/3/12.
//

import AGResourceManager

private let kSenseLicUri = "beauty/SenseLib_lic"
private let kSenseUri = "beauty/SenseLib"
private let kByteLicUri = "beauty/ByteEffectLib_lic"
private let kByteUri = "beauty/ByteEffectLib"
private let kFuLicUri = "beauty/FULib_lic"
private let kFuUri = "beauty/FULib"

private let kLoadingViewTag = 11223344



func updateDownloadProgress(progress: Double) {
    DispatchQueue.main.async {
        guard let view = UIApplication.topMostViewController?.view else {
            return
        }
        var progressView = view.viewWithTag(kLoadingViewTag) as? ShowDownlodingProgressView
        if progressView == nil {
            let frame = CGRect(x: (view.frame.width - 300) / 2, y: (view.frame.height - 60) / 2, width: 300, height: 60)
            let _progressView = ShowDownlodingProgressView(frame: frame)
            _progressView.tag = kLoadingViewTag
            view.addSubview(_progressView)
            progressView = _progressView
        }
        progressView?.setProgress(Int(progress * 100))
    }
}

func markProgressCompletion(err: NSError?) {
    if let err = err {
        ToastView.show(text: err.localizedDescription)
        return
    }
    guard let view = UIApplication.topMostViewController?.view else {
        return
    }
    let progressView = view.viewWithTag(kLoadingViewTag)
    progressView?.removeFromSuperview()
}

@discardableResult
private func setupStResource() -> Bool {
    let manager = AGResourceManager.shared
    guard let stLicResource = manager.getResource(uri: kSenseLicUri),
          manager.getStatus(resource: stLicResource) == .downloaded,
          let stResource = manager.getResource(uri: kSenseUri),
          manager.getStatus(resource: stResource) == .downloaded else {
        return false
    }
    
    let stLicPath = manager.getFolderPath(resource: stLicResource) + "/SENSEME.lic"
    let stResourcePath = manager.getFolderPath(resource: stResource) + "/SenseLib"
    STDynmicResourceConfig.shareInstance().licFilePath = stLicPath
    STDynmicResourceConfig.shareInstance().resourceFolderPath = stResourcePath
    return true
}

@discardableResult
private func setupBeResource() -> Bool {
    let manager = AGResourceManager.shared
    guard let beLicResource = manager.getResource(uri: kByteLicUri),
          manager.getStatus(resource: beLicResource) == .downloaded,
          let beResource = manager.getResource(uri: kByteUri),
          manager.getStatus(resource: beResource) == .downloaded else {
        return false
    }
    
    let beLicPath = manager.getFolderPath(resource: beLicResource) + "/LicenseBag.bundle/Agora_test_20240111_20240411_io.agora.test.entfull_4.5.0_1111.licbag"
    let beResourcePath = manager.getFolderPath(resource: beResource) + "/ByteEffectLib"
    BEDynmicResourceConfig.shareInstance().licFilePath = beLicPath
    BEDynmicResourceConfig.shareInstance().resourceFolderPath = beResourcePath
    return true
}

@discardableResult
private func setupFuResource() -> Bool {
    let manager = AGResourceManager.shared
    guard let fuLicResource = manager.getResource(uri: kFuLicUri),
          manager.getStatus(resource: fuLicResource) == .downloaded,
          let fuResource = manager.getResource(uri: kFuUri),
          manager.getStatus(resource: fuResource) == .downloaded else {
        return false
    }
    
    let beLicPath = manager.getFolderPath(resource: fuLicResource) + "/authpack.h"
    let beResourcePath = manager.getFolderPath(resource: fuResource) + "/FULib"
    FUDynmicResourceConfig.shareInstance().licFilePath = beLicPath
    FUDynmicResourceConfig.shareInstance().resourceFolderPath = beResourcePath
    return true
}

extension AGResourceManager {
    
    static func checkAndSetupBeautyPath(_ types: [BeautyFactoryType],
                                        completion: ((NSError?) -> Void)?) {
        let loadingToastStr = "show_beauty_resource_downloading".show_localized
        for type in types {
            switch type {
            case .sense:
                //setup senseme path
                guard setupStResource() else {
                    AGResourceManager.autoDownload(uris: [kSenseLicUri, kSenseUri]) { progress in
                        updateDownloadProgress(progress: progress)
                    } completion: {  err in
                        setupStResource()
                        markProgressCompletion(err: err)
                        completion?(err)
                    }

                    ToastView.show(text: loadingToastStr)
                    return
                }
                
                completion?(nil)
            case .byte:
                //setup byte effect path
                guard setupBeResource() else {
                    AGResourceManager.autoDownload(uris: [kByteLicUri, kByteUri]) { progress in
                        updateDownloadProgress(progress: progress)
                    } completion: { err in
                        setupBeResource()
                        markProgressCompletion(err: err)
                        completion?(err)
                    }
                    ToastView.show(text: loadingToastStr)
                    return
                }
                
                completion?(nil)
            case .fu:
                //setup fu effect path
                guard setupFuResource() else {
                    AGResourceManager.autoDownload(uris: [kFuLicUri, kFuUri]) { progress in
                        updateDownloadProgress(progress: progress)
                    } completion: {  err in
                        setupFuResource()
                        markProgressCompletion(err: err)
                        completion?(err)
                    }
                    ToastView.show(text: loadingToastStr)
                    return
                }
                
                completion?(nil)
            default:
                completion?(nil)
            }
        }
    }
}

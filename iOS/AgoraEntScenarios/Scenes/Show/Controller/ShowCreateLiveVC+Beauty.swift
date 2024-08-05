//
//  ShowCreateLiveVC+Beauty.swift
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
private let kDownloadingAlreadyErr = -100001

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
    
    let beLicPath = manager.getFolderPath(resource: beLicResource) + "/LicenseBag.bundle/Agora_test_\(Bundle.main.bundleIdentifier ?? "").licbag"
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

extension ShowCreateLiveVC {
    func isBeautyDownloaded() -> Bool {
        if setupStResource(), setupBeResource(), setupFuResource() {
            return true
        }
        checkAndSetupBeautyPath {[weak self] err in
            self?.markProgressCompletion(err: err)
        }
        return false
    }
    
    func cancelBeautyResource() {
        let manager = AGResourceManager.shared
        if let res = manager.getResource(uri: kSenseUri) {
            manager.cancelDownloadResource(resource: res)
        }
        
        if let res = manager.getResource(uri: kSenseLicUri) {
            manager.cancelDownloadResource(resource: res)
        }
        
        if let res = manager.getResource(uri: kByteUri) {
            manager.cancelDownloadResource(resource: res)
        }
        
        if let res = manager.getResource(uri: kByteLicUri) {
            manager.cancelDownloadResource(resource: res)
        }
        
        if let res = manager.getResource(uri: kFuLicUri) {
            manager.cancelDownloadResource(resource: res)
        }
        
        if let res = manager.getResource(uri: kFuUri) {
            manager.cancelDownloadResource(resource: res)
        }
    }
    
    func checkAndSetupBeautyPath(completion: ((NSError?) -> Void)?) {
        if KeyCenter.DynamicResourceUrl?.isEmpty ?? true {
            completion?(nil)
            return
        }
        if let _ = view.viewWithTag(kLoadingViewTag) {
            completion?(NSError(domain: "download already", code: kDownloadingAlreadyErr))
            return
        }
        
        guard setupStResource() else {
            let type = BeautyFactoryType.sense
            self.updateDownloadProgress(title: type.title, progress: 0)
            AGResourceManager.autoDownload(uris: [kSenseLicUri, kSenseUri]) {[weak self] progress in
                self?.updateDownloadProgress(title: type.title, progress: progress)
            } completion: {[weak self]  err in
                guard let self = self else {return}
                self.markProgressCompletion(err: err)
                if let _ = err { return }
                setupStResource()
                self.checkAndSetupBeautyPath(completion: completion)
            }
            return
        }
        
        //setup byte effect path
        guard setupBeResource() else {
            let type = BeautyFactoryType.byte
            self.updateDownloadProgress(title: type.title, progress: 0)
            AGResourceManager.autoDownload(uris: [kByteLicUri, kByteUri]) {[weak self] progress in
                self?.updateDownloadProgress(title: type.title, progress: progress)
            } completion: {[weak self] err in
                guard let self = self else {return}
                self.markProgressCompletion(err: err)
                if let _ = err { return }
                setupBeResource()
                self.checkAndSetupBeautyPath(completion: completion)
            }
            return
        }
        
        //setup fu effect path
        guard setupFuResource() else {
            let type = BeautyFactoryType.fu
            self.updateDownloadProgress(title: type.title, progress: 0)
            AGResourceManager.autoDownload(uris: [kFuLicUri, kFuUri]) {[weak self] progress in
                self?.updateDownloadProgress(title: type.title, progress: progress)
            } completion: { [weak self] err in
                guard let self = self else {return}
                self.markProgressCompletion(err: err)
                if let _ = err { return }
                setupFuResource()
                self.checkAndSetupBeautyPath(completion: completion)
            }
            return
        }
        
        completion?(nil)
    }
    
    private func updateDownloadProgress(title: String, progress: Double) {
        DispatchQueue.main.async {
            guard let view = self.view else {
                return
            }
            var progressView = view.viewWithTag(kLoadingViewTag) as? ShowDownlodingProgressView
            if progressView == nil {
                let size = CGSize(width: 250, height: 60)
                let frame = CGRect(x: (view.frame.width - size.width) / 2,
                                   y: (view.frame.height - size.height) / 2,
                                   width: size.width,
                                   height: size.height)
                let _progressView = ShowDownlodingProgressView(frame: frame)
                _progressView.tag = kLoadingViewTag
                view.addSubview(_progressView)
                progressView = _progressView
            }
            progressView?.setProgress(title, Int(progress * 100))
        }
    }

    private func markProgressCompletion(err: NSError?) {
        if let err = err {
            //already downloading
            if err.code == kDownloadingAlreadyErr { return }
            
            ToastView.show(text: err.localizedDescription)
        }
        guard let progressView = view.viewWithTag(kLoadingViewTag) else {
            return
        }
        
        progressView.removeFromSuperview()
    }
}

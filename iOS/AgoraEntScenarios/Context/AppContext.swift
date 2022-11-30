//
//  AppContext.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

@objc class AppContext: NSObject {
    @objc static let shared: AppContext = .init()
    @objc var sceneLocalizeBundleName: String?
    @objc var sceneImageBundleName: String?
    
    
    override init() {
        super.init()
        setupBugly()
    }
    
    private func setupBugly() {
#if DEBUG
#else
        let config = BuglyConfig()
        config.reportLogLevel = BuglyLogLevel.warn
        config.unexpectedTerminatingDetectionEnable = true
        config.debugMode = true
        Bugly.start(withAppId: "e188384728", config: config)
#endif
    }

    @objc func getLang() -> String {
        guard let lang = NSLocale.preferredLanguages.first else {
            return "en"
        }

        if lang.contains("zh") {
            return "zh-Hans"
        }

        return "en"
    }

    // MARK: user

    // MARK: App Config

    @objc func appId() -> String {
        return KeyCenter.AppId
    }

    @objc func appCertificate() -> String? {
        return KeyCenter.Certificate
    }

    @objc func appRtcToken() -> String? {
        return VLUserCenter.user.agoraRTCToken
    }
    
    @objc func appRtmToken() -> String? {
        return VLUserCenter.user.agoraRTMToken
    }

    @objc func appHostUrl() -> String {
        return KeyCenter.HostUrl
    }
}

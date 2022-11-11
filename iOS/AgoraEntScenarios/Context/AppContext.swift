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
    @objc var extDic: NSMutableDictionary = NSMutableDictionary()

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

    @objc func appHostUrl() -> String {
        return KeyCenter.HostUrl
    }
}

//
//  AppDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

import UIKit
@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        configKeyCenterData()
        
        NotificationCenter.default.addObserver(self, selector: #selector(didTokenExpired), name: NSNotification.Name(rawValue: "AGORAENTTOKENEXPIRED"), object: nil)
        
        window?.configRootViewController()
        return true
    }

    func configKeyCenterData() {
        AppContext.shared.appId = KeyCenter.AppId
        AppContext.shared.certificate = KeyCenter.Certificate ?? ""
        AppContext.shared.hostUrl = KeyCenter.HostUrl
        AppContext.shared.imAppKey = KeyCenter.IMAppKey ?? ""
        AppContext.shared.imClientId = KeyCenter.IMClientId ?? ""
        AppContext.shared.imClientSecret = KeyCenter.IMClientSecret ?? ""
        AppContext.shared.cloudPlayerKey = KeyCenter.CloudPlayerKey ?? ""
        AppContext.shared.cloudPlayerSecret = KeyCenter.CloudPlayerSecret ?? ""
        AppContext.shared.baseServerUrl = KeyCenter.baseServerUrl ?? ""
    }
    
    @objc func didTokenExpired() {
        VLToast.toast(NSLocalizedString("app_expire", comment: ""))
        window?.configRootViewController()
    }
}

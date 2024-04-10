//
//  AppDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

import UIKit
import AgoraCommon
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
        
        var isDebugMode = false
        if let index: Int = UserDefaults.standard.object(forKey: "TOOLBOXENV") as? Int {
            isDebugMode = index == 1
        } else {
            isDebugMode = false
        }
        
        AppContext.shared.appId = KeyCenter.AppId
        AppContext.shared.certificate = KeyCenter.Certificate ?? ""
        AppContext.shared.hostUrl = isDebugMode ? (KeyCenter.serviceHostDev) : (KeyCenter.serviceHost)
        AppContext.shared.imAppKey = KeyCenter.IMAppKey ?? ""
        AppContext.shared.imClientId = KeyCenter.IMClientId ?? ""
        AppContext.shared.imClientSecret = KeyCenter.IMClientSecret ?? ""
        AppContext.shared.cloudPlayerKey = KeyCenter.CloudPlayerKey ?? ""
        AppContext.shared.cloudPlayerSecret = KeyCenter.CloudPlayerSecret ?? ""
        AppContext.shared.baseServerUrl = isDebugMode ? (KeyCenter.baseServerUrlDev ?? "") : (KeyCenter.baseServerUrl ?? "")
        
    }
    
    @objc func didTokenExpired() {
        VLToast.toast(NSLocalizedString("app_expire", comment: ""))
        window?.configRootViewController()
    }
}

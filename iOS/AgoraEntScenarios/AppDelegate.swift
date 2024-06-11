//
//  AppDelegate.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/19.
//

import UIKit
import AgoraCommon
import AGResourceManager

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        // Override point for customization after application launch.
        configKeyCenterData()
        
        NotificationCenter.default.addObserver(self, selector: #selector(didTokenExpired), name: NSNotification.Name(rawValue: "AGORAENTTOKENEXPIRED"), object: nil)
        
        //重置ains的自定义状态
        UserDefaults.standard.setValue(false, forKey: "AINSCUSTOM")
        UserDefaults.standard.setValue(false, forKey: "AEDCUSTOM")
        UserDefaults.standard.setValue(false, forKey: "ASPTCUSTOM")
        UserDefaults.standard.synchronize()
        
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
        AppContext.shared.hostUrl = KeyCenter.HostUrl
        AppContext.shared.baseServerUrl = isDebugMode ? (KeyCenter.baseServerUrlDev ?? "") : (KeyCenter.baseServerUrl ?? "")
        AppContext.shared.roomManagerUrl = "\(AppContext.shared.baseServerUrl)/room-manager"
        AppContext.shared.imAppKey = KeyCenter.IMAppKey ?? ""
        AppContext.shared.imClientId = KeyCenter.IMClientId ?? ""
        AppContext.shared.imClientSecret = KeyCenter.IMClientSecret ?? ""
        AppContext.shared.RestfulApiKey = KeyCenter.RestfulApiKey ?? ""
        AppContext.shared.RestfulApiSecret = KeyCenter.RestfulApiSecret ?? ""
        AppContext.shared.baseServerUrl = KeyCenter.baseServerUrl ?? ""
        #if DEBUG
        #else
        AppContext.shared.cloudPlayerKey = KeyCenter.CloudPlayerKey ?? ""
        AppContext.shared.cloudPlayerSecret = KeyCenter.CloudPlayerSecret ?? ""
        #endif
        
        AGResourceManagerContext.shared.displayLogClosure = { text in
            asyncToMainThread {
                CommonLogger.default_info(text, tag: "ResourceManager")
            }
        }
        AGResourceManager.autoDownload()
    }
    
    @objc func didTokenExpired() {
        VLToast.toast(NSLocalizedString("app_expire", comment: ""))
        window?.configRootViewController()
    }
}

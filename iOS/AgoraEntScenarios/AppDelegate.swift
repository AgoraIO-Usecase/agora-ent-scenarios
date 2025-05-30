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
        AppContext.shared.appId = KeyCenter.AppId
        AppContext.shared.certificate = KeyCenter.Certificate ?? ""
        AppContext.shared.imAppKey = KeyCenter.IMAppKey ?? ""
        AppContext.shared.imClientId = KeyCenter.IMClientId ?? ""
        AppContext.shared.imClientSecret = KeyCenter.IMClientSecret ?? ""
        AppContext.shared.RestfulApiKey = KeyCenter.RestfulApiKey ?? ""
        AppContext.shared.RestfulApiSecret = KeyCenter.RestfulApiSecret ?? ""
        AppContext.shared.hyAppId = KeyCenter.HyAppId ?? ""
        AppContext.shared.hyAPIKey = KeyCenter.HyAPIKey ?? ""
        AppContext.shared.hyAPISecret = KeyCenter.HyAPISecret ?? ""
        //默认是正式/测试环境
        let isStaging: Bool
        let EnvKey = "TOOLBOXENV"
        if let index: NSNumber = UserDefaults.standard.object(forKey: EnvKey) as? NSNumber {
            isStaging = index == 0
        } else {
            // 如果用户没有主动设置过则设置一下
            isStaging = false
            if isStaging {
                UserDefaults.standard.setValue(0, forKey: EnvKey)
            } else {
                UserDefaults.standard.setValue(1, forKey: EnvKey)
            }
        }
        
        if isStaging {
            AppContext.shared.hostUrl = KeyCenter.HostUrlDev
            AppContext.shared.baseServerUrl = KeyCenter.baseServerUrlDev ?? ""
            AppContext.shared.aichatAgentHost = KeyCenter.AIChatAgentServerDevUrl
            AppContext.shared.roomManagerUrl = (KeyCenter.baseServerUrlDev ?? "") + "room-manager"
        } else {
            AppContext.shared.hostUrl = KeyCenter.HostUrl
            AppContext.shared.baseServerUrl = KeyCenter.baseServerUrl ?? ""
            AppContext.shared.aichatAgentHost = KeyCenter.AIChatAgentServerUrl
            AppContext.shared.roomManagerUrl = (KeyCenter.baseServerUrl ?? "") + "room-manager"
        }
        
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

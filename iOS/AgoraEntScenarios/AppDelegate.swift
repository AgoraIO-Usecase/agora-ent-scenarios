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
        //Security Check
        let result = SecurityManager.check()
        if !result {
            exit(0)
        }
        
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
        // 如果用户没有主动切换过，默认是正式/测试环境
        var isStaging = true
        if let index: Int = UserDefaults.standard.object(forKey: "TOOLBOXENV") as? Int {
            isStaging = index == 1
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



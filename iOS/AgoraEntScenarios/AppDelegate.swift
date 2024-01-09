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
        
        //重置ains的自定义状态
        UserDefaults.standard.setValue(false, forKey: "AINSCUSTOM")
        UserDefaults.standard.setValue(false, forKey: "AEDCUSTOM")
        UserDefaults.standard.setValue(false, forKey: "ASPTCUSTOM")
        UserDefaults.standard.synchronize()
        
        window?.configRootViewController()
        return true
    }

}

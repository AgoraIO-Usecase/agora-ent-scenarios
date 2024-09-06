//
//  UIWindow+Extension.swift
//  AgoraEntScenarios
//
//  Created by CP on 2024/1/15.
//

import Foundation
import AgoraCommon
extension UIWindow {
    func configRootViewController() {
        DispatchQueue.main.async {
            if VLUserCenter.shared().isLogin() {
                self.rootViewController = VLMainTabBarController()
            } else {
                let rootVC = VLLoginController()
                let nav = BaseNavigationController(rootViewController: rootVC)
                self.rootViewController = nav
            }
        }
    }
}

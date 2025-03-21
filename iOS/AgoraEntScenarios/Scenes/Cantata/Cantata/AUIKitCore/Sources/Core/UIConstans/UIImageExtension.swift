//
//  UIImageExtension.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import UIKit


public extension UIImage {
    convenience init?(_ bundleResourceName: String,_ type: AUIBundleType) {
//        guard let folderPath = Bundle.main.path(forResource: "auiVoiceChatTheme", ofType: "bundle") else { return }
//        guard let path =  Bundle.bundle(for: type).path(forResource: bundleResourceName, ofType: "png",inDirectory: "resource") else { return }
        if #available(iOS 13.0, *) {
            self.init(named: bundleResourceName, in: Bundle.bundle(for: type), with: nil)
        } else {
            self.init(named: bundleResourceName, in: Bundle.bundle(for: type), compatibleWith: nil)
        }
    }
}

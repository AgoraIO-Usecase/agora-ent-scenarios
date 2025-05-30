//
//  UIImage+AUIKit.swift
//  AUIKit
//
//  Created by wushengtao on 2023/4/3.
//

import UIKit
import AgoraCommon

//public var themeResourcePaths: Set<URL> = Set()
extension UIImage {
    public class func aui_Image(named: String) -> UIImage? {
        return UIImage.sceneImage(name: named, bundleName: "AUIIMKit")
    }
}


//
//  UIImage+Scene.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import UIKit

extension UIImage {
    @objc public static func sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: nil)
    }
    
    static func common_sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: "CommonResource")
    }

    @objc public static func sceneImage(name: String, bundleName: String?) -> UIImage? {
        guard let bundleName = bundleName ?? AppContext.shared.sceneImageBundleName else {
            assertionFailure("sceneImageBundleName == nil")
            return nil
        }
        
        let cacheName = "\(name)__\(bundleName)"
        if let image = AppContext.shared.imageCahe[cacheName] as? UIImage {
            return image
        }

        guard let bundlePath = Bundle.main.path(forResource: bundleName, ofType: "bundle"),
              let bundle = Bundle(path: bundlePath)
        else {
            assertionFailure("image bundle == nil")
            return nil
        }

        let components = name.components(separatedBy: ".")
        let pureName = components.first ?? name
        let suffix = (components.count == 2 ? components.last : nil) ?? "png"
        let scale = Int(UIScreen.main.scale)
        var scales = [1, 2, 3].filter { value in
            return value != scale
        }
        scales.insert(scale, at: 0)
        
        let lang = AppContext.shared.getLang()
        for value in scales {
            let imageName1 = value > 1 ? "\(pureName)-\(lang)@\(value)x" : pureName
            let imageName2 = value > 1 ? "\(pureName)@\(value)x" : pureName
            if let path = bundle.path(forResource: imageName1, ofType: suffix) {
                let image = UIImage(contentsOfFile: path)
                assert(image != nil, "image == nil \(path)")
                return image
            }
            if let path = bundle.path(forResource: imageName2, ofType: suffix) {
                let image = UIImage(contentsOfFile: path)
                assert(image != nil, "image == nil \(path)")
                AppContext.shared.imageCahe[cacheName] = image
                return image
            }
        }
//        assertionFailure("image path(\(pureName) == nil")
//        print("image path(\(pureName)) == nil")
        return nil
    }
}

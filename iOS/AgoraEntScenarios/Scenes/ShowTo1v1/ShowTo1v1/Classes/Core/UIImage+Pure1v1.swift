//
//  UIImage+Pure1v1.swift
//  AFNetworking
//
//  Created by wushengtao on 2023/7/19.
//

import UIKit

func getLang() -> String {
    guard let lang = NSLocale.preferredLanguages.first else {
        return "en"
    }

    if lang.contains("zh") {
        return "zh-Hans"
    }

    return "en"
}

extension UIImage {
    static private let agoraImageCache = NSCache<NSString, UIImage>()
    static func sceneImagePath(name: String, bundleName: String) -> String? {
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
        
        let lang = ""//getLang()
        for value in scales {
            let imageName1 = value > 1 ? "\(pureName)-\(lang)@\(value)x" : pureName
            let imageName2 = value > 1 ? "\(pureName)@\(value)x" : pureName
            if let path = bundle.path(forResource: imageName1, ofType: suffix) {
                return path
            }
            if let path = bundle.path(forResource: imageName2, ofType: suffix) {
                return path
            }
        }
//        assertionFailure("image path(\(pureName) == nil")
//        print("image path(\(pureName)) == nil")
        return nil
    }
    
    @objc static func sceneImage(name: String) -> UIImage? {
        guard let imagePath = UIImage.sceneImagePath(name: "Image/\(name)", bundleName: "ShowTo1v1") else {
            return nil
        }
        if let image = agoraImageCache.object(forKey: imagePath as NSString) {
            return image
        }
        guard let image = UIImage(contentsOfFile: imagePath) else {
            return nil
        }
        agoraImageCache.setObject(image, forKey: imagePath as NSString)
        return image
    }
}

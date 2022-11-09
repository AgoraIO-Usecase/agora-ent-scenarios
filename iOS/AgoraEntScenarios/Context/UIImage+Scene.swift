//
//  UIImage+Scene.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation
import UIKit

extension UIImage {
    @objc static func sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: nil)
    }

    @objc static func sceneImage(name: String, bundleName: String?) -> UIImage? {
        guard let bundleName = bundleName ?? AppContext.shared.sceneImageBundleName else {
            assertionFailure("sceneImageBundleName == nil")
            return nil
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
        for value in scales {
            let imageName = value > 1 ? "\(pureName)@\(value)x" : pureName
            if let path = bundle.path(forResource: imageName, ofType: suffix) {
                let image = UIImage(contentsOfFile: path)
                return image
            }
        }
        assertionFailure("image path(\(pureName) == nil")
//        print("image path(\(pureName)) == nil")
        return nil
    }
}

//
//  UIImage+KTV.swift
//  AgoraEntScenarios
//
//  Created by CP on 2024/1/16.
//

import Foundation
extension UIImage {
    @objc static func ktv_sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: "KtvResource")
    }
}

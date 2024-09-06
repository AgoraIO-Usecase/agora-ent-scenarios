//
//  UIImage+SR.swift
//  AgoraEntScenarios
//
//  Created by CP on 2024/1/16.
//

import Foundation
import AgoraCommon
extension UIImage {
    @objc static func sr_sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: "SRResource")
    }
}

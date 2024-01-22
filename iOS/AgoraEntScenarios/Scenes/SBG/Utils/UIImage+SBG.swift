//
//  UIImage+SBG.swift
//  AgoraEntScenarios
//
//  Created by CP on 2024/1/16.
//

import Foundation
import AgoraCommon
extension UIImage {
    @objc static func sbg_sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: "sbgResource")
    }
}

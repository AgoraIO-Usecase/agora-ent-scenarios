//
//  UIImage+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/2.
//

import Foundation

extension UIImage {
    @objc static func show_sceneImage(name: String) -> UIImage? {
        return sceneImage(name: name, bundleName: "showResource")
    }
    
    @objc
    static func show_beautyImage(name: String?) -> UIImage? {
        guard let imageName = name else { return nil }
        return sceneImage(name: imageName, bundleName: "BeautyResource")
    }
}

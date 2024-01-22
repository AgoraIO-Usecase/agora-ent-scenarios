//
//  UIImage+DHC.swift
//  Cantata
//
//  Created by CP on 2024/1/18.
//

import Foundation
import AgoraCommon
extension UIImage {
    @objc public static func dhc_sceneImage(with name: String)-> UIImage? {
        return UIImage.sceneImage(name: name, bundleName: "DHCResource")
    }
}

//
//  UIImageBundleExtension.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation
import UIKit

public extension UIImage {
    
    static func spatial_image(_ named: String) -> UIImage? {
        if #available(iOS 13.0, *) {
            return self.init(named: named, in: Bundle.spatial, with: nil)
        } else {
            return self.init(named: named, in: Bundle.spatial, compatibleWith: nil)
        }
    }

}

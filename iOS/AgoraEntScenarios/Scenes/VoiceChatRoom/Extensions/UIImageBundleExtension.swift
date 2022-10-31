//
//  UIImageBundleExtension.swift
//  Pods-VoiceRoomBaseUIKit_Example
//
//  Created by 朱继超 on 2022/8/24.
//

import Foundation
import UIKit

public extension UIImage {
    convenience init?(_ bundleResourceName: String) {
        self.init(named: bundleResourceName, in: Bundle.voiceRoomBundle, with: nil)
    }
}

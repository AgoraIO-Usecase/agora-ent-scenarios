//
//  AUILocalized.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/30.
//

import Foundation
import AgoraCommon

public func aui_localized(_ string: String) -> String {
    return sceneLocalized(string, bundleName: "AUIIMKit")
}

//
//  String + Extension.swift
//  AgoraScene_iOS
//
//  Created by CP on 2022/10/17.
//

import Foundation

public extension String {
    func localized() -> String {
        return LanguageManager.localValue(key: self)
    }
}

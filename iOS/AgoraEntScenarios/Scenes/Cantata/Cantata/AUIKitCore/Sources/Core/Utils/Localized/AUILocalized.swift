//
//  AUILocalized.swift
//  AUIKit
//
//  Created by wushengtao on 2023/3/30.
//

import Foundation

public func aui_localized(_ string: String, bundleName: String = "auiLocalizable") -> String {
    guard let bundlePath = Bundle.main.path(forResource: bundleName, ofType: "bundle"),
          let bundle = Bundle(path: bundlePath)
    else {
        assertionFailure("image bundle == nil")
        return string
    }

    guard let language = NSLocale.preferredLanguages.first else {
        assertionFailure("lang == nil")
        return string
    }

    var lang = language
    if lang.contains("zh") {
        lang = "zh-Hans"
    } else {
        lang = "en"
    }

    guard let path = bundle.path(forResource: lang, ofType: "lproj"),
          let langBundle = Bundle(path: path)
    else {
        assertionFailure("langBundle == nil")
        return string
    }

    let value = langBundle.localizedString(forKey: string, value: nil, table: nil)
    return Bundle.main.localizedString(forKey: string, value: value, table: nil)
}

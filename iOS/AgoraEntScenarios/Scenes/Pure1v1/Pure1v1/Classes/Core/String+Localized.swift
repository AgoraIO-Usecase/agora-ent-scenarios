//
//  SceneLocalizedString.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

extension String {
    func pure1v1Localization() -> String {
        return sceneLocalized(self as String, bundleName: "Pure1v1")
    }
}

///
/// - Parameters:
///   - string: <#string description#>
///   - bundleName: <#bundleName description#>
/// - Returns: <#description#>
func sceneLocalized(_ string: String, bundleName: String? = nil) -> String {
    guard let bundleName = bundleName else {
        assertionFailure("localizeFolder = nil")
        return string
    }

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

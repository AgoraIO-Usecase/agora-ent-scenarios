//
//  SceneLocalizedString.swift
//  AgoraEntScenarios
//
//  Created by wushengtao on 2022/10/18.
//

import Foundation

extension NSString {
    @objc public func toSceneLocalization() -> NSString {
        return sceneLocalized(self as String, bundleName: nil) as NSString
    }
    
    @objc public func toSceneLocalization(with bundleName: String) -> NSString {
        return sceneLocalized(self as String, bundleName: bundleName) as NSString
    }
}

public func commonSceneLocalized(_ string: String, bundleName: String? = nil) -> String {
    return sceneLocalized(string, bundleName: "CommonResource")
}

///
/// - Parameters:
///   - string: <#string description#>
///   - bundleName: <#bundleName description#>
/// - Returns: <#description#>
public func sceneLocalized(_ string: String, bundleName: String? = nil) -> String {
    //TODO: remove localize string of root menu from ktv resource
    if AppContext.shared.sceneLocalizeBundleName == nil {
        AppContext.shared.sceneLocalizeBundleName = "KtvResource"
    }
    guard let bundleName = bundleName ?? AppContext.shared.sceneLocalizeBundleName else {
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

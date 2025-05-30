//
//  LanguageManager.swift
//  AUIKit
//
//  Created by 朱继超 on 2023/5/15.
//

import Foundation

public enum LanguageType: String {
    case Chinese = "zh-Hans", Chinese_traditional = "zh-Hant", English = "en", Russian = "ru", German = "de", French = "fr", Japanese = "ja", Korean = "ko", Auto = "auto"
}

@objc public final class LanguageManager: NSObject {
    
    public static func localValue(key: String,type: AUIBundleType) -> String {
        LanguageManager.shared.localValue(key,type)
    }

    public static let shared = LanguageManager()

    override private init() {}

    var currentLocal: Locale {
        Locale.current
    }

    private func localValue(_ key: String,_ type: AUIBundleType) -> String {
        let bundle = Bundle.bundle(for: type)
        guard var lang = NSLocale.preferredLanguages.first else { return Bundle.main.bundlePath }
        if lang.contains("zh") {
            lang = "zh-Hans"
        } else {
            lang = "en"
        }
        let path = bundle.path(forResource: lang, ofType: "lproj") ?? ""
        let pathBundle = Bundle(path: path) ?? .main
        return pathBundle.localizedString(forKey: key, value: nil, table: nil)
    }

}

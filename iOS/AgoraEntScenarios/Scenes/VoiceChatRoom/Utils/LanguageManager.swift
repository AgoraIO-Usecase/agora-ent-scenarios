//
//  LanguageManager.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/1.
//

import Foundation
import ZSwiftBaseLib

public enum LanguageType: String {
    case Chinese = "zh-Hans", Chinese_traditional = "zh-Hant", English = "en", Russian = "ru", German = "de", French = "fr", Japanese = "ja", Korean = "ko", Auto = "auto"
}

public final class LanguageManager: NSObject {
    @UserDefault("AvatarAppLanguage", defaultValue: "") var lanuage

    static func localValue(key: String) -> String {
        LanguageManager.shared.localValue(key)
    }

    static func setLanguage(type: LanguageType) {
        LanguageManager.shared.setLanguage(type)
    }

    static let shared = LanguageManager()

    override private init() {}

    lazy var bundle: Bundle = {
        if NSLocale.preferredLanguages.first!.hasPrefix("zh") {
            return Bundle(path: Bundle.voiceRoomBundle.path(forResource: "zh", ofType: "lproj") ?? Bundle.main.bundlePath) ?? Bundle.main
        }
        return Bundle(path: Bundle.voiceRoomBundle.path(forResource: "en", ofType: "lproj") ?? Bundle.main.bundlePath) ?? Bundle.main
    }()

    var currentLocal: Locale {
        Locale.current
    }

    private func localValue(_ key: String) -> String {
        self.bundle.localizedString(forKey: key, value: nil, table: "Localizable")
    }

    private func setLanguage(_ type: LanguageType) {
        self.lanuage = type.rawValue
        // 返回项目中 en.lproj 文件的路径
        let path = self.bundle.path(forResource: type.rawValue, ofType: "lproj")
        self.bundle = Bundle(path: path!)!
        if type == .Auto {
            self.bundle = Bundle.main
        }
    }
}

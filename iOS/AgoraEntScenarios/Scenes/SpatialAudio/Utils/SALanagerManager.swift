//
//  LanguageManager.swift
//  VoiceRoomBaseUIKit
//
//  Created by 朱继超 on 2022/9/1.
//

import Foundation
import ZSwiftBaseLib

public final class SALanguageManager: NSObject {
    @UserDefault("AvatarAppLanguage", defaultValue: "") var lanuage

    static func localValue(key: String) -> String {
        SALanguageManager.shared.localValue(key)
    }

    static func setLanguage(type: LanguageType) {
        SALanguageManager.shared.setLanguage(type)
    }

    static let shared = SALanguageManager()

    override private init() {}

    lazy var bundle: Bundle = {
        guard let bundlePath = Bundle.main.path(forResource: "SpatialAudioResource", ofType: "bundle"), let bundle = Bundle(path: bundlePath) else {
            assertionFailure("vrcm bundle == nil")
            return Bundle.main
        }
        guard var lang = NSLocale.preferredLanguages.first else { return Bundle.main }
        if lang.contains("zh") {
            lang = "zh-Hans"
        } else {
            lang = "en"
        }
        let path = bundle.path(forResource: lang, ofType: "lproj") ?? ""
        return Bundle(path: path) ?? .main
    }()

    var currentLocal: Locale {
        Locale.current
    }

    private func localValue(_ key: String) -> String {
        bundle.localizedString(forKey: key, value: nil, table: nil)
    }

    private func setLanguage(_ type: LanguageType) {
        lanuage = type.rawValue
        // 返回项目中 en.lproj 文件的路径
        let path = bundle.path(forResource: type.rawValue, ofType: "lproj")
        bundle = Bundle(path: path!)!
        if type == .Auto {
            bundle = Bundle.main
        }
    }
}

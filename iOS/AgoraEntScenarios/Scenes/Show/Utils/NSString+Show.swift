//
//  NSString+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import Foundation
import AgoraCommon
extension String {
    var show_localized: String {
        let cacheKey = "\(self)__showResource"
        if let ret = AppContext.shared.localizedCache[cacheKey] {
            return ret
        }
        guard let bundlePath = Bundle.main.path(forResource: "showResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath)
        else {
            return self
        }
        
        guard var lang = NSLocale.preferredLanguages.first else {
            return self
        }
        if lang.contains("zh") {
            lang = "zh-Hans"
        } else {
            lang = "en"
        }
        
        guard let langPath = bundle.path(forResource: lang, ofType: "lproj") , let detailBundle = Bundle(path: langPath) else {
            return self
        }
        let retStr = NSLocalizedString(self,tableName: "Localizable", bundle:detailBundle ,comment: "")
        AppContext.shared.localizedCache[cacheKey] = retStr
        return retStr
    }
}

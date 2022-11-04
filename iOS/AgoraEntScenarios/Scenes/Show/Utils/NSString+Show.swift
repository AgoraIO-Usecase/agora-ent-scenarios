//
//  NSString+Show.swift
//  AgoraEntScenarios
//
//  Created by FanPengpeng on 2022/11/4.
//

import Foundation

extension String {
    var show_localized: String {
        guard let bundlePath = Bundle.main.path(forResource: "showResource", ofType: "bundle"),
              let bundle = Bundle(path: bundlePath)
        else {
            return self
        }
        return NSLocalizedString(self,tableName: "Localizable", bundle:bundle ,comment: "")
    }
}

//
//  DataExtension.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/18.
//

import Foundation

public extension Data {
    
    var a: AUIKitSwiftLib<Self> {
        return AUIKitSwiftLib.init(self)
    }
}

public extension AUIKitSwiftLib where Base == Data {
    func toDictionary() -> Dictionary<String,Any>? {
        var dic: Dictionary<String,Any>?
        do {
            dic = try JSONSerialization.jsonObject(with: base, options: .allowFragments) as? Dictionary<String,Any>
        } catch {
            aui_info("AUIKit data toDictionary() error:\(error.localizedDescription)")
        }
        return dic
    }

    func toArray() -> [Dictionary<String,Any>]? {
        var array: [Dictionary<String,Any>]?
        do {
            array = try JSONSerialization.jsonObject(with: base, options: .allowFragments) as? [Dictionary<String,Any>]
        } catch {
            aui_info("AUIKit data toArray() error:\(error.localizedDescription)")
        }
        return array
    }
    
}

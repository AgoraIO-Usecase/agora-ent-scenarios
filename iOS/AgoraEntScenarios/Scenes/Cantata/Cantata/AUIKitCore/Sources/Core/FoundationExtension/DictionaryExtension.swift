//
//  DictionaryExtension.swift
//  AUiKit
//
//  Created by 朱继超 on 2023/5/19.
//

import Foundation



public extension Dictionary {
    
    var a: AUIKitSwiftLib<Self> {
        return AUIKitSwiftLib.init(self)
    }
}

public extension AUIKitSwiftLib where Base == Dictionary<String, Any> {
    func toJsonString() -> String? {
        do {
            let data = try JSONSerialization.data(withJSONObject: base, options: [])
            let json = String(data: data, encoding: .utf8)
            return json
        } catch {
            return nil
        }
    }
}




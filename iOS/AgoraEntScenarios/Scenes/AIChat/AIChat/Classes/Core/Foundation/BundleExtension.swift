//
//  BundleExtension.swift
//  AIChat
//
//  Created by 朱继超 on 2024/8/27.
//

import Foundation

fileprivate var ChatAIResourceBundle: Bundle?

public extension Bundle {
    
    class var chatAIBundle: Bundle {
        if ChatAIResourceBundle != nil {
            return ChatAIResourceBundle!
        }
        let bundlePath = Bundle.main.path(forResource: "AIChat", ofType: "bundle") ?? ""
        ChatAIResourceBundle = Bundle(path:  bundlePath) ?? .main
        return ChatAIResourceBundle!
    }
}
